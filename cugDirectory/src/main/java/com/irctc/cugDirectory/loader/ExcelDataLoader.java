package com.irctc.cugDirectory.loader;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import java.io.FileInputStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;



@Component
public class ExcelDataLoader implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception{
        FileInputStream file = new FileInputStream("./data/cug-source.xlsx");
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheet("Sheet1");
    }
}
