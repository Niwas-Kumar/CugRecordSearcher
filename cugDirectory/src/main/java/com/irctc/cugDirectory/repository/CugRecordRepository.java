package com.irctc.cugDirectory.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.irctc.cugDirectory.model.CugRecord;

public interface CugRecordRepository extends JpaRepository<CugRecord,Long> {
   
   
    List<CugRecord> findByNameContainingIgnoreCaseOrCugNumberContainingOrderByNameAsc(String name, String cugNumber, Pageable pageable);
    Optional<CugRecord> findByCugNumber(String cugNumber);
}
