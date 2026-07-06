package com.irctc.cugDirectory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.irctc.cugDirectory.model.CugRecord;
import com.irctc.cugDirectory.repository.CugRecordRepository;

@Component
public class SampleDataLoader implements CommandLineRunner {
    
    private final CugRecordRepository repository;

    public SampleDataLoader(CugRecordRepository repository) {
        this.repository = repository;
    }
    
   @Override
public void run(String... args) throws Exception {

    CugRecord r1 = new CugRecord();
    r1.setCugNumber("9876543210");
    r1.setName("Ravi Kumar");
    r1.setDepartment("IT");
    r1.setDesignation("Manager");
    r1.setEmpCode("E001");
    r1.setGrade("A");
    r1.setEntitlement(500);
    r1.setRoamingCharge(0);
    r1.setOffice("Delhi");
    repository.save(r1);

    CugRecord r2 = new CugRecord();
    r2.setCugNumber("8765432109");
    r2.setName("Anita Sharma");
    r2.setDepartment("HR");
    r2.setDesignation("Executive");
    r2.setEmpCode("E002");
    r2.setGrade("B");
    r2.setEntitlement(300);
    r2.setRoamingCharge(50);
    r2.setOffice("Mumbai");
    repository.save(r2);

    CugRecord r3 = new CugRecord();
    r3.setCugNumber("7654321098");
    r3.setName("Suresh Rao");
    r3.setDepartment("Finance");
    r3.setDesignation("Analyst");
    r3.setEmpCode("E003");
    r3.setGrade("B");
    r3.setEntitlement(300);
    r3.setRoamingCharge(0);
    r3.setOffice("Bengaluru");
    repository.save(r3);

    System.out.println(">>> Sample data loaded!");
}
     
}
