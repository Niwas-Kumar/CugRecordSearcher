package com.irctc.cugDirectory.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.irctc.cugDirectory.model.CugRecord;

public interface CugRecordRepository extends JpaRepository<CugRecord,Long> {
   
    CugRecord findByCugNumber(String cugNumber);
    List<CugRecord> findByNameContainingIgnoreCase(String name);
}
