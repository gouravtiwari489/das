package com.datagenerator.demo.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Slf4j
public class DataGenerationWorker implements Runnable {

  private String tableName;
  private XSSFWorkbook workbook;
  private LinkedHashMap<String, String> fieldMap;
  private int rowCount;
  private String fileType;

  public DataGenerationWorker(
      String tableName,
      XSSFWorkbook workbook,
      LinkedHashMap<String, String> fieldMap,
      int rowCount,
      String fileType) {

    this.tableName = tableName;
    this.workbook = workbook;
    this.fieldMap = fieldMap;
    this.rowCount = rowCount;
    this.fileType = fileType;
  }

  @Override
  public void run() {

    try {
      generateData(tableName);
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("data generation completed for " + tableName);
  }

  private void generateData(String tableName) {
    try {

      List<List<String>> excelData = GenerateSampleDataUtil.generateData(fieldMap, rowCount);
      GenerateExcelUtil.createAndInsertDataIntoSheet(workbook, tableName, excelData);
      writeToFile(fileType, workbook, tableName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void writeToFile(String fileType, XSSFWorkbook workbook, String tableName)
      throws IOException, FileNotFoundException {
    String fileExtension;
    String filePath;
    fileExtension = fileType.equals("xlsx") ? "xlsx" : "csv";
    Resource resource = new ClassPathResource("output");
    filePath = String.format("%s\\%s.%s", resource.getFile().getPath(), tableName, fileExtension);
    OutputStream excelFileToCreate = new FileOutputStream(new File(filePath));
    workbook.write(excelFileToCreate);
    excelFileToCreate.close();
  }
}
