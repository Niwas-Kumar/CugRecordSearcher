package com.irctc.cugDirectory.service;

import com.irctc.cugDirectory.model.AccessLog;
import com.irctc.cugDirectory.repository.AccessLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;

    public AccessLogService(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    public void log(String username, String action, String detail) {
        accessLogRepository.save(new AccessLog(username, action, detail == null ? "" : detail));
    }
}