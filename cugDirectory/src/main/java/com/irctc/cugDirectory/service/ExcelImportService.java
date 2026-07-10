package com.irctc.cugDirectory.service;

import com.irctc.cugDirectory.model.CugRecord;
import com.irctc.cugDirectory.repository.CugRecordRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Reusable Excel import logic — used both by ExcelDataLoader (startup) and
 * AdminController (runtime UI upload). Extracted here so we don't duplicate code.
 *
 * Expected sheet name : CUG_Records
 * Required columns    : CUG Number | Full Name
 * Behaviour           : Upsert by CUG Number (safe to re-run).
 */
@Service
public class ExcelImportService {

    private static final String SHEET_NAME = "CUG_Records";
    private static final Pattern CUG_PATTERN = Pattern.compile("^\\d{10}$");
    private static final int MAX_CONSECUTIVE_BLANK_ROWS = 5;

    private final CugRecordRepository repository;

    public ExcelImportService(CugRecordRepository repository) {
        this.repository = repository;
    }

    /**
     * Imports / upserts CUG records from the given Excel stream.
     * The caller is responsible for closing the stream.
     */
    public ImportResult importFromStream(InputStream inputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                return ImportResult.error("Sheet '" + SHEET_NAME + "' not found. "
                        + "Rename your data sheet to exactly: " + SHEET_NAME);
            }

            Map<String, Integer> headerMap = readHeaderMap(sheet.getRow(0));
            if (!headerMap.containsKey("CUG Number") || !headerMap.containsKey("Full Name")) {
                return ImportResult.error(
                        "Required columns 'CUG Number' and 'Full Name' not found. "
                        + "Columns detected: " + headerMap.keySet());
            }

            Set<String> seenInThisImport = new HashSet<>();
            List<String> warnings = new ArrayList<>();
            int inserted = 0, updated = 0, skipped = 0, consecutiveBlank = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                int excelRowNum = i + 1;

                String cugNumber = getCellAsString(getCell(row, headerMap, "CUG Number"));
                String name      = getCellAsString(getCell(row, headerMap, "Full Name"));

                // Detect true end-of-data
                if (row == null || (cugNumber.isEmpty() && name.isEmpty())) {
                    consecutiveBlank++;
                    if (consecutiveBlank >= MAX_CONSECUTIVE_BLANK_ROWS) break;
                    continue;
                }
                consecutiveBlank = 0;

                // Validate
                if (!CUG_PATTERN.matcher(cugNumber).matches()) {
                    warnings.add("Row " + excelRowNum + ": invalid CUG Number '" + cugNumber + "' — skipped");
                    skipped++;
                    continue;
                }
                if (!seenInThisImport.add(cugNumber)) {
                    warnings.add("Row " + excelRowNum + ": duplicate CUG Number '" + cugNumber + "' in this file — skipped");
                    skipped++;
                    continue;
                }
                if (name.isEmpty()) {
                    warnings.add("Row " + excelRowNum + ": CUG '" + cugNumber + "' has no name — skipped");
                    skipped++;
                    continue;
                }

                String designation = getCellAsString(getCell(row, headerMap, "Designation"));
                String department  = getCellAsString(getCell(row, headerMap, "Department"));
                String empCode     = getCellAsString(getCell(row, headerMap, "Employee Code"));
                String grade       = getCellAsString(getCell(row, headerMap, "Grade"));
                String location    = getCellAsString(getCell(row, headerMap, "Location"));
                String office      = getCellAsString(getCell(row, headerMap, "Office / Unit"));
                String status      = getCellAsString(getCell(row, headerMap, "Status"));

                Integer entitlement = getCellAsIntOrNull(getCell(row, headerMap, "Entitlement (Rs)"));
                if (entitlement == null) entitlement = 0;

                Integer roamingCharge = getCellAsIntOrNull(getCell(row, headerMap, "Roaming Plan"));
                if (roamingCharge == null) roamingCharge = 0;

                // Upsert
                Optional<CugRecord> existing = repository.findByCugNumber(cugNumber);
                CugRecord record = existing.orElseGet(CugRecord::new);

                record.setCugNumber(cugNumber);
                record.setName(name);
                record.setDesignation(designation);
                record.setDepartment(department);
                record.setEmpCode(empCode);
                record.setGrade(grade);
                record.setEntitlement(entitlement);
                record.setRoamingCharge(roamingCharge);
                record.setOffice(office);
                record.setLocation(location);
                record.setStatus(status);

                repository.save(record);
                if (existing.isPresent()) updated++; else inserted++;
            }

            return new ImportResult(inserted, updated, skipped, warnings, null);
        }
    }

    // ── Cell helpers ──────────────────────────────────────────────────────────

    private Map<String, Integer> readHeaderMap(Row headerRow) {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (headerRow == null) return map;
        for (Cell cell : headerRow) {
            String header = getCellAsString(cell);
            if (!header.isEmpty()) map.put(header, cell.getColumnIndex());
        }
        return map;
    }

    private Cell getCell(Row row, Map<String, Integer> headerMap, String columnName) {
        if (row == null) return null;
        Integer idx = headerMap.get(columnName);
        return idx == null ? null : row.getCell(idx);
    }

    private String getCellAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default:      return "";
        }
    }

    private Integer getCellAsIntOrNull(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC: return (int) cell.getNumericCellValue();
            case STRING:
                String s = cell.getStringCellValue().trim();
                if (s.isEmpty()) return null;
                try { return Integer.parseInt(s); }
                catch (NumberFormatException e) { return null; }
            default: return null;
        }
    }

    // ── Result DTO ────────────────────────────────────────────────────────────

    public static class ImportResult {
        public final int inserted;
        public final int updated;
        public final int skipped;
        public final List<String> warnings;
        public final String error; // null on success

        public ImportResult(int inserted, int updated, int skipped,
                            List<String> warnings, String error) {
            this.inserted = inserted;
            this.updated  = updated;
            this.skipped  = skipped;
            this.warnings = warnings != null ? warnings : List.of();
            this.error    = error;
        }

        public static ImportResult error(String message) {
            return new ImportResult(0, 0, 0, List.of(), message);
        }

        public boolean isSuccess() { return error == null; }
    }
}
