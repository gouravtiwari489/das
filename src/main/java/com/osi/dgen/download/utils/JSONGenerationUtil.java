package com.osi.dgen.download.utils;

import static com.osi.dgen.utils.DataGenUtil.removeSingleQuotes;

import com.google.gson.Gson;
import com.osi.dgen.domain.CustomUserDetails;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum JSONGenerationUtil implements GenerateDataInterface {
  INSTANCE;

  @Override
  public void generateData(
      String tableName, List<List<String>> excelData, String fileType, CustomUserDetails user) {
    Map<String, String> jsonMap = null;
    List<String> headers = excelData.get(0);
    List<Map<String, String>> finalMap = new ArrayList<>();
    for (int i = 1; i < excelData.size(); i++) {
      jsonMap = new LinkedHashMap<>();
      List<String> data = excelData.get(i);
      for (int j = 0; j < data.size(); j++) {
        jsonMap.put(headers.get(j), removeSingleQuotes(data.get(j)));
      }
      finalMap.add(jsonMap);
    }
    Gson gs = new Gson();
    String gss = gs.toJson(finalMap);
    try {
      writeToFile(gss, tableName, fileType, user);
    } catch (IOException e) {
      e.getMessage();
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
    BufferedWriter jsonFile = new BufferedWriter(new FileWriter(filePath));

    jsonFile.write((String) obj);
    jsonFile.flush();
    jsonFile.close();
  }
}
