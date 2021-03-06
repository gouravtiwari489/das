package com.osi.datagen.parsing.service;

import com.osi.datagen.constant.DasConstants;
import com.osi.datagen.domain.CheckConstraint;
import com.osi.datagen.domain.Constraint;
import com.osi.datagen.domain.Field;
import com.osi.datagen.domain.ForigenKeyConstraint;
import com.osi.datagen.domain.Table;
import com.osi.datagen.exception.DependencyException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TableStructureExtractor {

  @Value("${dependencycheck.toggle}")
  String toggleCheck;

  private static final String ENGINE = "ENGINE";
  private static final String REFERENCES = "REFERENCES";
  private static final String FOREIGN_KEY = "FOREIGN KEY";
  private static final String PRIMARY_KEY = "PRIMARY KEY";
  private static final String CREATE_TABLE = "CREATE TABLE ";
  private static final String CONSTRAINT = "CONSTRAINT";
  private static final String CHECK = "CHECK";
  private static final String UNIQUE_KEY = "UNIQUE KEY";
  private static final String FIELDEXP = "[^\\()_,a-zA-Z0-9]+";
  private static final String CHECKEXP = "[^\\>=_ ,a-zA-Z0-9]+";
  private static final String PRIFOREIGNEXP = "[^\\_,a-zA-Z0-9]+";
  private static final String COMMENGTEXP = "(--.*)|(((/\\*)+?[\\w\\W]+?(\\*/)+))";
  private static final String START_COMMENT = "/*";
  private static final String END_COMMENT = "*/";
  private static final String INVERTED_COMMA = "`";
  private static final String NO_SPACE = "";
  private static final String SPACE = " ";

  public List<Table> searchforTableName(File file, boolean dependencyCheck)
      throws DependencyException, Exception {
    List<Field> fieldsList = new ArrayList<>();
    List<Constraint> constraintList = new ArrayList<>();
    List<ForigenKeyConstraint> forigenKeysList = new ArrayList<>();
    List<CheckConstraint> checkConstraintsList = new ArrayList<>();
    List<Table> tableList = new ArrayList<>();
    Table table = null;
    final Scanner scanner = new Scanner(file);
    String tableName = NO_SPACE;
    String primaryKey = NO_SPACE;
    String multiLineUncommented = NO_SPACE;
    boolean isMultiLine = false;
    int count = 0;
    int fkCount = 1;
    while (scanner.hasNextLine()) {
      final String readLine = scanner.nextLine().trim();
      String lineFromFile = readLine.replaceAll(COMMENGTEXP, NO_SPACE); //remove Single line comment
      //remove Multiline comments
      if (lineFromFile.indexOf(START_COMMENT) > -1) {
        multiLineUncommented = lineFromFile.substring(0, lineFromFile.indexOf(START_COMMENT));
        isMultiLine = true;
      }
      if (isMultiLine && !(lineFromFile.indexOf(END_COMMENT) > -1)) {
        continue;
      } else if (isMultiLine) {
        isMultiLine = false;
        lineFromFile = multiLineUncommented;
      }
      if (lineFromFile != null
          && !lineFromFile.isEmpty()
          && !lineFromFile.startsWith(START_COMMENT)
          && !lineFromFile.startsWith("--")) {
        if (lineFromFile.contains(CREATE_TABLE)) {
          table = new Table();
          tableName = NO_SPACE;
          fkCount = 1;
          String[] matchString = lineFromFile.split(CREATE_TABLE);
          tableName = matchString[1].split(SPACE)[0].replace(INVERTED_COMMA, NO_SPACE);
          count = 1;
          table.setTableName(tableName);
        } else if (lineFromFile.contains(PRIMARY_KEY)) {
          List<String> pkColumList = new ArrayList<>();
          Constraint constraint = new Constraint();
          constraint.setConstraintType(PRIMARY_KEY);
          primaryKey = NO_SPACE;
          count = 0;
          String[] pkString = lineFromFile.split(PRIMARY_KEY);
          primaryKey = pkString[1].replaceAll("[^\\,_,a-zA-Z0-9]+", NO_SPACE);
          String[] pkSplit = primaryKey.split(DasConstants.COMMA_SEPRATOR);
          for (String pkColumn : pkSplit) {
            if (pkColumn != null && !pkColumn.isEmpty()) {
              pkColumList.add(pkColumn);
            }
          }
          constraint.setColumns(pkColumList);
          constraintList.add(constraint);
        } else if (lineFromFile.contains(CONSTRAINT)) {
          if (lineFromFile.contains(FOREIGN_KEY)) {
            ForigenKeyConstraint fkConstraint = new ForigenKeyConstraint();
            String[] fieldString = lineFromFile.split(FOREIGN_KEY);
            fkConstraint.setConstraintName(
                fieldString[0]
                    .replace(CONSTRAINT, NO_SPACE)
                    .replaceAll(INVERTED_COMMA, NO_SPACE)
                    .trim());
            String[] fieldString2 = fieldString[1].split(REFERENCES);
            String test1 =
                fieldString2[0]
                    .replace("(", NO_SPACE)
                    .replace(")", NO_SPACE)
                    .replace(INVERTED_COMMA, NO_SPACE)
                    .replace(SPACE, NO_SPACE);

            count = 0;
            fkCount++;
            fkConstraint.setKeyName(test1);
            String test2 =
                fieldString2[1]
                    .replace(SPACE, NO_SPACE)
                    .replace(INVERTED_COMMA, NO_SPACE)
                    .replace(DasConstants.COMMA_SEPRATOR, NO_SPACE)
                    .replace("ONUPDATECASCADE", NO_SPACE);
            String[] test3 = test2.split("\\(");
            fkConstraint.setReferenceTable(test3[0]);
            fkConstraint.setReferenceColumn(test3[1].replaceAll(PRIFOREIGNEXP, NO_SPACE));
            forigenKeysList.add(fkConstraint);
          } else if (lineFromFile.contains(CHECK)) {
            CheckConstraint checkConstraint = new CheckConstraint();
            String[] chkConstraintSplit = lineFromFile.split(CHECK);
            checkConstraint.setConstraintName(
                chkConstraintSplit[0]
                    .replace(CONSTRAINT, NO_SPACE)
                    .replaceAll(INVERTED_COMMA, NO_SPACE)
                    .trim());
            checkConstraint.setValue(chkConstraintSplit[1].replaceAll(CHECKEXP, NO_SPACE).trim());
            checkConstraintsList.add(checkConstraint);
          }
        } else if (lineFromFile.contains(UNIQUE_KEY)) {
          Constraint constraint = new Constraint();
          constraint.setConstraintType(UNIQUE_KEY);
          List<String> uniqueList = new ArrayList<>();
          String uniquekey = lineFromFile.replace(UNIQUE_KEY, NO_SPACE);
          String[] split1 = uniquekey.split(SPACE);
          if (split1.length > 1) {
            constraint.setConstraintName(split1[1]);
            String uniqueKey = split1[2].replaceAll("[^\\,_,a-zA-Z0-9]+", NO_SPACE);
            String[] pkSplit = uniqueKey.split(DasConstants.COMMA_SEPRATOR);
            for (String uqniColumn : pkSplit) {
              if (uqniColumn != null && !uqniColumn.isEmpty()) {
                uniqueList.add(uqniColumn);
              }
            }
          } else {
            String unqKeyColumn = uniquekey.replaceAll("[^\\,_,a-zA-Z0-9]+", "");
            uniqueList.add(unqKeyColumn);
          }
          constraint.setColumns(uniqueList);
          constraintList.add(constraint);

        } else if (lineFromFile.contains(CHECK)) {
          CheckConstraint checkConstraint = new CheckConstraint();
          String[] chkConstraintSplit = lineFromFile.split(CHECK);
          checkConstraint.setConstraintName(CHECK);
          checkConstraint.setValue(chkConstraintSplit[0]);
          checkConstraintsList.add(checkConstraint);
        } else if (lineFromFile.contains(ENGINE)) {
          count = 0;
          table.setFields(fieldsList);
          table.setCheckConstraints(checkConstraintsList);
          table.setConstraints(constraintList);
          table.setForigenKeys(forigenKeysList);
          tableList.add(table);
          table = new Table();
          fieldsList = new ArrayList<>();
          constraintList = new ArrayList<>();
          forigenKeysList = new ArrayList<>();
          checkConstraintsList = new ArrayList<>();
        } else if (count == 1) {
          List<String> pkColumList = new ArrayList<>();
          Constraint constraint = new Constraint();
          Field coulmnField = new Field();
          String lineFromFile1 = lineFromFile.trim().replaceAll(FIELDEXP, SPACE).trim();
          String[] splitString = lineFromFile1.split(SPACE);
          coulmnField.setColumnName(splitString[0]);
          String[] lengthSplit = splitString[1].split("\\(");
          if (lengthSplit.length > 1) {
            coulmnField.setDataType(lengthSplit[0].replaceAll("[^\\,a-zA-Z0-9]+", NO_SPACE));
            coulmnField.setLength(lengthSplit[1].replaceAll("[^\\,a-zA-Z0-9]+", NO_SPACE));
          } else {
            coulmnField.setDataType(lengthSplit[0].replace(DasConstants.COMMA_SEPRATOR, NO_SPACE));
          }
          if (splitString.length > 2) {
            coulmnField.setDefaultValue(
                splitString[2]
                    + SPACE
                    + splitString[3].replace(DasConstants.COMMA_SEPRATOR, NO_SPACE));
            constraint.setConstraintType(
                splitString[2]
                    + SPACE
                    + splitString[3].replace(DasConstants.COMMA_SEPRATOR, NO_SPACE));
            pkColumList.add(splitString[0]);
            if (splitString.length > 4) {
              coulmnField.setIncrementValue(
                  splitString[4].replace(DasConstants.COMMA_SEPRATOR, NO_SPACE));
            }
          }
          constraint.setColumns(pkColumList);
          if (constraint.getConstraintType() != null && !constraint.getConstraintType().isEmpty()) {
            constraintList.add(constraint);
          }
          fieldsList.add(coulmnField);
          String[] fieldString = lineFromFile1.split(SPACE);
          if (fieldString.length >= 2) {
            String fieldType = fieldString[1];
            String[] fieldType2 = null;
            if (fieldType.endsWith(DasConstants.COMMA_SEPRATOR)) {
              fieldType2 = fieldType.split(DasConstants.COMMA_SEPRATOR);
              fieldType = fieldType2[0];
            }
          }
        }
      }
    }
    scanner.close();
    return tableList;
  }
}
