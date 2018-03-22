package com.osi.datagen.filegeneration.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import com.osi.datagen.domain.CustomUserDetails;

public enum SQLGenerationUtil implements GenerateDataInterface {
  INSTANCE;

  @Override
  public void generateData(
      String tableName, List<List<String>> excelData, String fileType, CustomUserDetails user) {
    try {
      List<String> rows = new ArrayList<>();
      List<String> headers = excelData.get(0);
      excelData.remove(0);
      for (List<String> list : excelData) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + tableName + " (");
        for (String header : headers) {
          sb.append(header + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(") values (");
        for (String value : list) {
          sb.append(value + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(");");
        rows.add(sb.toString().toUpperCase());
      }
      writeToFile(rows, tableName, fileType, user);
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

  @Override
  public void writeToFile(Object obj, String tableName, String fileType, CustomUserDetails user)
      throws IOException, FileNotFoundException {
    String filePath;
    File resource = new File(fileDownloadPath + user.getUsername());
    if (!resource.exists()) {
      new File(fileDownloadPath + user.getUsername()).mkdir();
    }
    File file = new File(resource.getAbsoluteFile().getPath() + "/" + fileType);
    if (!file.exists()) {
      file.mkdir();
    }
    filePath =
        String.format(
            "%s\\%s.%s",
            resource.getAbsoluteFile().getPath() + "\\" + fileType, tableName, fileType);
    List<String> rows = (List<String>) obj;
    File fout = new File(filePath);
    FileOutputStream fos = new FileOutputStream(fout);
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
    for (String row : rows) {
      writer.write(row);
      writer.newLine();
    }
    writer.close();
  }
}