package com.irctc.cugDirectory.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.irctc.cugDirectory.model.CugRecord;
import com.irctc.cugDirectory.service.CugRecordService;

@RestController
@RequestMapping("/api/cug")
public class CugRecordController {

    private final CugRecordService service;

    public CugRecordController(CugRecordService service) {
        this.service = service;
    }

    /**
     * Single search box on the frontend calls this one endpoint with whatever
     * the user typed — could be a name fragment or a CUG number.
     */
    @GetMapping("/search")
    public List<CugRecord> search(@RequestParam("q") String query, Authentication authentication) {
        return service.search(query, authentication.getName());
    }

    /**
     * Frontend calls this ONLY when the user presses Enter on the search box.
     * This produces one clean log entry per intentional search,
     * instead of one entry per debounced keystroke.
     */
    @PostMapping("/log-search")
    public void logSearch(@RequestBody Map<String, String> body, Authentication authentication) {
        String query = body.getOrDefault("query", "").trim();
        if (!query.isEmpty()) {
            service.logSearch(authentication.getName(), query);
        }
    }

    /**
     * Frontend calls this (fire-and-forget) whenever a user opens a record's
     * detail view, so the admin log shows exactly which record was looked at,
     * not just what was typed into the search box.
     */
    @PostMapping("/view")
    public void logView(@RequestBody Map<String, String> body, Authentication authentication) {
        service.logRecordView(
                authentication.getName(),
                body.getOrDefault("cugNumber", ""),
                body.getOrDefault("name", "")
        );
    }
}