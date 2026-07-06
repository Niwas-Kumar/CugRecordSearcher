package com.irctc.cugDirectory.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.irctc.cugDirectory.model.CugRecord;
import com.irctc.cugDirectory.repository.CugRecordRepository;

@Service
public class CugRecordService {
    private final CugRecordRepository repository;

    public CugRecordService(CugRecordRepository repository) {
        this.repository = repository;
    }

    public CugRecord searchByCug(String cugNumber){
        return repository.findByCugNumber(cugNumber);
    }

    public List<CugRecord> searchByName(String name){
        return repository.findByNameContainingIgnoreCase(name);
    }
}
