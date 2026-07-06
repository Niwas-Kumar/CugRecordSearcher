package com.irctc.cugDirectory.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.irctc.cugDirectory.model.CugRecord;
import com.irctc.cugDirectory.service.CugRecordService;

@RestController
@RequestMapping("/api/cug")
public class CugRecordController {

    private final CugRecordService service;

    public CugRecordController(CugRecordService service) {
        this.service = service;
    }

    @GetMapping("/search/cug")
    public CugRecord cugSearch(@RequestParam("value") String cugNo){
        return service.searchByCug(cugNo);
    }

    @GetMapping("/search/name")
    public List<CugRecord> cugName(@RequestParam("value") String name) {
        return service.searchByName(name);
    }
    
}
