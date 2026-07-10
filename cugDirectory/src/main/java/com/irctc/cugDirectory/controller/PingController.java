package com.irctc.cugDirectory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Lightweight keep-alive endpoint for Render / Railway free-tier hosting.
 *
 * Render spins down free-tier services after 15 minutes of inactivity.
 * Pointing an external uptime monitor (e.g. UptimeRobot, cron-job.org)
 * to GET /api/ping every 5 minutes prevents that spin-down.
 *
 * This endpoint:
 *  - Requires NO authentication
 *  - Makes NO database call
 *  - Returns instantly with a 200 OK + timestamp
 */
@RestController
public class PingController {

    private static final long START_TIME = System.currentTimeMillis();

    @GetMapping("/api/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        long uptimeSeconds = (System.currentTimeMillis() - START_TIME) / 1000;
        return ResponseEntity.ok(Map.of(
                "status",  "UP",
                "service", "CUG Directory",
                "time",    Instant.now().toString(),
                "uptime",  uptimeSeconds + "s"
        ));
    }
}
