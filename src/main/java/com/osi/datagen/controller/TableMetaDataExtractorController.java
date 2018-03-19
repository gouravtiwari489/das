package com.osi.datagen.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.osi.datagen.domain.Table;
import com.osi.datagen.utils.FindWordMatchingPossibilities;
import com.osi.datagen.utils.TableStructureExtractor;

@RestController
@RequestMapping("/extractor")
public class TableMetaDataExtractorController {

  @Autowired private TableStructureExtractor tableStructureExtractor;

  @Autowired private FindWordMatchingPossibilities findWordMatchingPossibilities;

  //  @Autowired private CustomTokenConverter customTokenConverter;

  @GetMapping
  public ResponseEntity<List<Table>> extractTableMetaData()
      throws ClassNotFoundException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("Dump20180122.sql").getFile());
    List<Table> tableList= new ArrayList<>();
    try {
    	tableList = tableStructureExtractor.searchforTableName(file, false);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseEntity<List<Table>>(tableList, HttpStatus.OK);
  }

  @PostMapping("/findMatch")
  public ResponseEntity<Map<String, List<String>>> matchWords(
      @RequestParam(name = "wordToFind", required = true) String wordToFind,
      @RequestParam(name = "domainType", required = true) String domainType)
      throws ClassNotFoundException, IOException {
    Map<String, List<String>> matchingMap =
        findWordMatchingPossibilities.findMatchingWord(wordToFind, domainType,null);
    return new ResponseEntity<Map<String, List<String>>>(matchingMap, HttpStatus.OK);
  }
}