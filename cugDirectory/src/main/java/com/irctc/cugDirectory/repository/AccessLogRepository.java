package com.irctc.cugDirectory.repository;

import com.irctc.cugDirectory.model.AccessLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    List<AccessLog> findAllByOrderByTimestampDesc(Pageable pageable);
}