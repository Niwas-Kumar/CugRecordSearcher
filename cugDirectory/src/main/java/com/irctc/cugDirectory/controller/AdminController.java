package com.irctc.cugDirectory.controller;

import com.irctc.cugDirectory.model.AccessLog;
import com.irctc.cugDirectory.model.AppUser;
import com.irctc.cugDirectory.model.CugRecord;
import com.irctc.cugDirectory.repository.AccessLogRepository;
import com.irctc.cugDirectory.repository.AppUserRepository;
import com.irctc.cugDirectory.repository.CugRecordRepository;
import com.irctc.cugDirectory.service.AccessLogService;
import com.irctc.cugDirectory.service.ExcelImportService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Pattern CUG_PATTERN = Pattern.compile("^\\d{10}$");

    private final AccessLogRepository accessLogRepository;
    private final AppUserRepository appUserRepository;
    private final CugRecordRepository cugRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessLogService accessLogService;
    private final ExcelImportService excelImportService;

    public AdminController(AccessLogRepository accessLogRepository,
                           AppUserRepository appUserRepository,
                           CugRecordRepository cugRecordRepository,
                           PasswordEncoder passwordEncoder,
                           AccessLogService accessLogService,
                           ExcelImportService excelImportService) {
        this.accessLogRepository = accessLogRepository;
        this.appUserRepository   = appUserRepository;
        this.cugRecordRepository = cugRecordRepository;
        this.passwordEncoder     = passwordEncoder;
        this.accessLogService    = accessLogService;
        this.excelImportService  = excelImportService;
    }

    // ── Dashboard stats ───────────────────────────────────────────────────────

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of(
                "totalUsers",   appUserRepository.count(),
                "totalRecords", cugRecordRepository.count(),
                "totalLogs",    accessLogRepository.count()
        );
    }

    @GetMapping("/logs")
    public List<AccessLog> logs() {
        return accessLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 300));
    }

    // ── User management ───────────────────────────────────────────────────────

    @GetMapping("/users")
    public List<AppUser> users() {
        return appUserRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping("/users")
    public ResponseEntity<?> addUser(@RequestBody Map<String, String> body, Authentication authentication) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "");
        String role     = "ADMIN".equalsIgnoreCase(body.getOrDefault("role", "USER")) ? "ADMIN" : "USER";

        if (username.isEmpty() || password.length() < 6)
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Username required and password must be at least 6 characters."));
        if (!username.matches("^[a-zA-Z0-9._-]{3,30}$"))
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Username can only contain letters, numbers, dots, underscores, hyphens (3–30 chars)."));
        if (appUserRepository.findByUsername(username).isPresent())
            return ResponseEntity.badRequest().body(Map.of("error", "That username already exists."));

        AppUser user = new AppUser(username, passwordEncoder.encode(password), role);
        appUserRepository.save(user);
        accessLogService.log(authentication.getName(), "admin_create_user", username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/{id}/toggle")
    public ResponseEntity<?> toggleUser(@PathVariable Long id, Authentication authentication) {
        AppUser user = appUserRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setActive(!user.isActive());
        appUserRepository.save(user);
        accessLogService.log(authentication.getName(), "admin_toggle_user",
                user.getUsername() + " -> active=" + user.isActive());
        return ResponseEntity.ok(user);
    }

    // ── Record search & edit ──────────────────────────────────────────────────

    @GetMapping("/records/search")
    public List<CugRecord> searchRecords(@RequestParam("q") String query) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty()) return List.of();
        return cugRecordRepository.findByNameContainingIgnoreCaseOrCugNumberContainingOrderByNameAsc(
                trimmed, trimmed, PageRequest.of(0, 60));
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<?> updateRecord(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          Authentication authentication) {
        CugRecord record = cugRecordRepository.findById(id).orElse(null);
        if (record == null) return ResponseEntity.notFound().build();

        if (body.containsKey("name"))         record.setName(body.get("name").trim());
        if (body.containsKey("empCode"))      record.setEmpCode(body.get("empCode").trim());
        if (body.containsKey("designation"))  record.setDesignation(body.get("designation").trim());
        if (body.containsKey("department"))   record.setDepartment(body.get("department").trim());
        if (body.containsKey("grade"))        record.setGrade(body.get("grade").trim());
        if (body.containsKey("location"))     record.setLocation(body.get("location").trim());
        if (body.containsKey("office"))       record.setOffice(body.get("office").trim());
        if (body.containsKey("status"))       record.setStatus(body.get("status").trim());
        if (body.containsKey("entitlement")) {
            try { record.setEntitlement(Integer.parseInt(body.get("entitlement").trim())); }
            catch (NumberFormatException ignored) {}
        }
        if (body.containsKey("roamingCharge")) {
            try { record.setRoamingCharge(Integer.parseInt(body.get("roamingCharge").trim())); }
            catch (NumberFormatException ignored) {}
        }

        cugRecordRepository.save(record);
        accessLogService.log(authentication.getName(), "admin_edit_record",
                record.getName() + " (" + record.getCugNumber() + ")");
        return ResponseEntity.ok(record);
    }

    // ── Add single record ─────────────────────────────────────────────────────

    /**
     * Creates a brand-new CUG record.
     * CUG Number must be exactly 10 digits and unique.
     * Full Name is required.
     */
    @PostMapping("/records")
    public ResponseEntity<?> addRecord(@RequestBody Map<String, String> body,
                                       Authentication authentication) {
        String cugNumber = body.getOrDefault("cugNumber", "").trim();
        String name      = body.getOrDefault("name", "").trim();

        if (!CUG_PATTERN.matcher(cugNumber).matches())
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "CUG Number must be exactly 10 digits."));
        if (name.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Full Name is required."));
        if (cugRecordRepository.findByCugNumber(cugNumber).isPresent())
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "CUG Number " + cugNumber + " already exists in the database."));

        CugRecord record = new CugRecord();
        record.setCugNumber(cugNumber);
        record.setName(name);
        record.setEmpCode(body.getOrDefault("empCode", "").trim());
        record.setDesignation(body.getOrDefault("designation", "").trim());
        record.setDepartment(body.getOrDefault("department", "").trim());
        record.setGrade(body.getOrDefault("grade", "").trim());
        record.setLocation(body.getOrDefault("location", "").trim());
        record.setOffice(body.getOrDefault("office", "").trim());
        record.setStatus(body.getOrDefault("status", "Active").trim());
        try { record.setEntitlement(Integer.parseInt(body.getOrDefault("entitlement", "0").trim())); }
        catch (NumberFormatException e) { record.setEntitlement(0); }
        try { record.setRoamingCharge(Integer.parseInt(body.getOrDefault("roamingCharge", "0").trim())); }
        catch (NumberFormatException e) { record.setRoamingCharge(0); }

        cugRecordRepository.save(record);
        accessLogService.log(authentication.getName(), "admin_add_record",
                name + " (" + cugNumber + ")");
        return ResponseEntity.ok(record);
    }

    // ── Excel upload ──────────────────────────────────────────────────────────

    /**
     * Accepts a multipart Excel file upload and runs an upsert import.
     * Same logic as the startup loader — safe to run multiple times.
     * Returns a JSON summary: inserted, updated, skipped, warnings.
     */
    @PostMapping("/data/import")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file,
                                         Authentication authentication) {
        if (file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "No file selected."));

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")))
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Only Excel files (.xlsx or .xls) are accepted."));

        try {
            ExcelImportService.ImportResult result = excelImportService.importFromStream(
                    file.getInputStream());

            if (!result.isSuccess())
                return ResponseEntity.badRequest().body(Map.of("error", result.error));

            accessLogService.log(authentication.getName(), "admin_import_excel",
                    "inserted=" + result.inserted + " updated=" + result.updated
                            + " skipped=" + result.skipped);

            return ResponseEntity.ok(Map.of(
                    "inserted", result.inserted,
                    "updated",  result.updated,
                    "skipped",  result.skipped,
                    "warnings", result.warnings
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to process file: " + e.getMessage()));
        }
    }
}