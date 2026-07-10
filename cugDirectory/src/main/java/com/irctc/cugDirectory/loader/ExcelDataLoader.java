package com.irctc.cugDirectory.loader;

/**
 * ExcelDataLoader — DISABLED.
 *
 * The startup Excel import has been removed because:
 *  1. All data is already persisted in MySQL — no need to reload on every restart.
 *  2. Admins can upload a new Excel file at any time via the UI at /admin/records.
 *
 * The import logic itself lives in ExcelImportService and is fully functional
 * via the admin upload endpoint (POST /api/admin/data/import).
 *
 * To re-enable startup import if ever needed, add @Component and
 * implement CommandLineRunner back to this class.
 */
public class ExcelDataLoader {
    // Intentionally empty — startup import disabled.
}