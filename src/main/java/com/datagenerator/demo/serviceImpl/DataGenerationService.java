package com.datagenerator.demo.serviceImpl;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.datagenerator.demo.component.LoadFileGenerationObjects;
import com.datagenerator.demo.domain.CustomUserDetails;
import com.datagenerator.demo.download.utils.GenerateDataInterface;
import com.datagenerator.demo.utils.CustomTokenConverter;
import com.datagenerator.demo.utils.DataGenerationWorker;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataGenerationService {

  public static List<LinkedHashMap<String, LinkedHashMap<String, String>>> tablFieldMappingeMap =
      null;

  @Autowired private CustomTokenConverter customTokenConverter;
  @Autowired LoadFileGenerationObjects fileGenObj;

  @SuppressWarnings("unchecked")
  public void generateData(String updatedMappedData, String fileType, int rowCount)
      throws IOException {

    tablFieldMappingeMap =
        (List<LinkedHashMap<String, LinkedHashMap<String, String>>>)
            customTokenConverter.getAdditionalInfo("mappedTables");

    Map<Integer, List<String>> tablesMap =
        (Map<Integer, List<String>>) customTokenConverter.getAdditionalInfo("orderedFKList");
    log.info("tablFieldMappingeMap values after getting from context", tablFieldMappingeMap);
    log.info("tablesMap values after getting from context", tablesMap);
    GenerateDataInterface service = fileGenObj.getGenDataServiceMap().get(fileType);
    threadService(tablesMap, fileType, rowCount, json_to_map(updatedMappedData), service);
    customTokenConverter.setAdditionalInfo("updatedMappedData",updatedMappedData);
  }

  public void threadService(
      Map<Integer, List<String>> tablesMap,
      String fileType,
      int rowCount,
      Map<String, LinkedHashMap<String, String>> map,
      GenerateDataInterface service)
      throws IOException {
    try {
      CustomUserDetails user = (CustomUserDetails)customTokenConverter.getAdditionalInfo("CurrentUser");
      Map<String, List<String>> concurrentMap = new ConcurrentHashMap<>();
      for (Map.Entry<Integer, List<String>> entry : tablesMap.entrySet()) {
        log.info("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        List<String> tablesList = entry.getValue();
        ExecutorService executor = Executors.newFixedThreadPool(tablesList.size());
        for (String tableName : tablesList) {
          Runnable dataGenerationWorker =
              new DataGenerationWorker(
                  tableName,
                  map.get(tableName),
                  rowCount,
                  fileType,
                  tablFieldMappingeMap,
                  concurrentMap,
                  service, user);
          executor.execute(dataGenerationWorker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {}
      }
      customTokenConverter.setAdditionalInfo(fileType,String.valueOf(rowCount));
    } catch (Exception ex) {
      log.error("Error wrting to file", ex);
      ex.printStackTrace();
    }
  }

  public Map<String, LinkedHashMap<String, String>> json_to_map(String updatedMappedData) {
    Map<String, LinkedHashMap<String, String>> map = null;
    ObjectMapper mapper = new ObjectMapper();
    try {
      map =
          mapper.readValue(
              updatedMappedData,
              new TypeReference<Map<String, LinkedHashMap<String, String>>>() {});

    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return map;
  }
}
