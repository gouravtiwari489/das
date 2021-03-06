package com.osi.datagen.filegeneration.util;

import static com.osi.datagen.datageneration.service.DataGenUtil.removeSingleQuotes;

import com.osi.datagen.domain.CustomUserDetails;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public enum XmlGenerationUtil implements GenerateDataInterface {
  INSTANCE;

  @Override
  public void generateData(
      String tableName, List<List<String>> excelData, String fileType, CustomUserDetails user) {

    DocumentBuilder builder = null;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (ParserConfigurationException e1) {
      e1.printStackTrace();
    }
    Document document = builder.newDocument();
    Element root = document.createElement("root");
    List<String> fields = excelData.get(0);
    for (int i = 1; i < excelData.size(); i++) {
      Element rowNode = document.createElement("row");
      List<String> values = excelData.get(i);
      for (int j = 0; j < values.size(); j++) {
        Element field = document.createElement(fields.get(j));
        field.setTextContent(removeSingleQuotes(values.get(j)));
        rowNode.appendChild(field);
      }
      root.appendChild(rowNode);
    }
    document.appendChild(root);
    excelData.clear();
    try {
      writeToFile(document, tableName, fileType, user);
    } catch (IOException e) {
      e.getMessage();
    }
  }

  @Override
  public void writeToFile(Object obj, String tableName, String fileType, CustomUserDetails user)
      throws IOException, FileNotFoundException {

    String filePath = createFolders(user, fileType, tableName);
    BufferedWriter xmlFile = new BufferedWriter(new FileWriter(filePath));

    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      Source source = new DOMSource((Document) obj);
      Result result = new StreamResult(xmlFile);
      transformer.transform(source, result);
      xmlFile.flush();
      xmlFile.close();

    } catch (TransformerConfigurationException | TransformerFactoryConfigurationError e1) {
      e1.printStackTrace();
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }
}
