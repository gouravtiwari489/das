package com.datagenerator.demo.serviceImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
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

	public static List<LinkedHashMap<String, LinkedHashMap<String, String>>> tablFieldMappingeMap = null;

	@Autowired
	private CustomTokenConverter customTokenConverter;

	@SuppressWarnings("unchecked")
	public void generateData(String updatedMappedData, String fileType, int rowCount) {

	/*	tablFieldMappingeMap = (List<LinkedHashMap<String, LinkedHashMap<String, String>>>) customTokenConverter
				.getAdditionalInfo("mappedTables");*/
		Map<Integer, List<String>> tablesMap = (Map<Integer, List<String>>) customTokenConverter
				.getAdditionalInfo("orderedFKList");
		log.info("tablFieldMappingeMap values after getting from context", tablFieldMappingeMap);
		log.info("tablesMap values after getting from context", tablesMap);
		threadService(tablesMap,fileType,rowCount, json_to_map(updatedMappedData));

	}

	public void threadService(Map<Integer, List<String>> tablesMap, String fileType, int rowCount, Map<String, LinkedHashMap<String, String>> map) {
		String filePath = null;
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook();
			for (Map.Entry<Integer, List<String>> entry : tablesMap.entrySet()) {
				log.info("Key = " + entry.getKey() + ", Value = " + entry.getValue());
				List<String> tablesList = entry.getValue();
				ExecutorService executor = Executors.newFixedThreadPool(tablesList.size());
				for (String tableName : tablesList) {
					Runnable dataGenerationWorker = new DataGenerationWorker(tableName, workbook,map.get(tableName),rowCount,fileType);
					executor.execute(dataGenerationWorker);
				}
				executor.shutdown();
				while (!executor.isTerminated()) {
				}
			}
			Resource resource = new ClassPathResource("output");
			filePath = resource.getFile().getPath() + "\\ExcelSheet.xlsx";
			OutputStream excelFileToCreate = new FileOutputStream(new File(filePath));
			workbook.write(excelFileToCreate);
			excelFileToCreate.close();
		} catch (FileNotFoundException ex) {
			log.error("Error while creating excel", ex);
		} catch (IOException ex) {
			log.error("Error while creating excel", ex);
		} catch (Exception ex) {
			log.error("Error wrting to file", ex);
			ex.printStackTrace();
		}
	}

	public Map<String, LinkedHashMap<String, String>> json_to_map(String updatedMappedData) {
		Map<String, LinkedHashMap<String, String>> map = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			map = mapper.readValue(updatedMappedData, new TypeReference<Map<String, LinkedHashMap<String, String>>>() {
			});

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