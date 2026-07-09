package com.irctc.cugDirectory.loader;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.irctc.cugDirectory.model.CugRecord;
import com.irctc.cugDirectory.repository.CugRecordRepository;

@Component
public class ExcelDataLoader implements CommandLineRunner {

    private final CugRecordRepository repository;

    public ExcelDataLoader(CugRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
       try( FileInputStream file = new FileInputStream("./data/cug-source.xlsx");
        Workbook workbook = new XSSFWorkbook(file)){
        Sheet sheet = workbook.getSheet("Sheet1");

        Row headerRow = sheet.getRow(1);
        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim();
            if (header.isEmpty()) {
                continue;
            }
            int index = cell.getColumnIndex();
            headerMap.put(header, index);
        }

        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String name = getCellAsString(row.getCell(headerMap.get("Name")));
            String designation = getCellAsString(row.getCell(headerMap.get("Designation")));
            String department = getCellAsString(row.getCell(headerMap.get("Dept.")));
            String empCode = getCellAsString(row.getCell(headerMap.get("EMP/Code")));
            String grade = getCellAsString(row.getCell(headerMap.get("Grade")));
            String cugNumber = getCellAsString(row.getCell(headerMap.get("CUG number")));
            int entitlement = getCellAsInt(row.getCell(headerMap.get("Entitelment")));
            int roamingCharge = getCellAsInt(row.getCell(headerMap.get("Roaming Charges")));

            CugRecord record = new CugRecord();
            record.setName(name);
            record.setDesignation(designation);
            record.setDepartment(department);
            record.setEmpCode(empCode);
            record.setGrade(grade);
            record.setCugNumber(cugNumber);
            record.setEntitlement(entitlement);
            record.setRoamingCharge(roamingCharge);

            repository.save(record);
        }

        System.out.println(">>> Excel import complete!");
    }
 }

    private String getCellAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private int getCellAsInt(Cell cell) {
        if (cell == null) return 0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }
}