package com.irctc.cugDirectory.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.irctc.cugDirectory.model.CugRecord;
import com.irctc.cugDirectory.repository.CugRecordRepository;

@Service
public class CugRecordService {

    private static final int MAX_RESULTS = 60;

    private final CugRecordRepository repository;
    private final AccessLogService accessLogService;

    public CugRecordService(CugRecordRepository repository, AccessLogService accessLogService) {
        this.repository = repository;
        this.accessLogService = accessLogService;
    }

    /**
     * Single search entry point used by the frontend's one search box —
     * matches against name OR CUG number. Returns an empty list for a
     * blank/whitespace-only query instead of matching every record.
     */
    public List<CugRecord> search(String query, String username) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        // NOTE: We do NOT log here — this is called on every debounced keystroke.
        // Logging is done explicitly by the frontend only on Enter key press.
        return repository.findByNameContainingIgnoreCaseOrCugNumberContainingOrderByNameAsc(
                trimmed, trimmed, PageRequest.of(0, MAX_RESULTS));
    }

    public void logSearch(String username, String query) {
        accessLogService.log(username, "search", query);
    }

    public void logRecordView(String username, String cugNumber, String name) {
        accessLogService.log(username, "view_record", name + " (" + cugNumber + ")");
    }
}