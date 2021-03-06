package com.osi.datagen.datagenerators;

import static com.osi.datagen.datageneration.service.DataGenUtil.singleQuote;

import com.osi.datagen.constant.DasConstants;
import com.osi.datagen.datageneration.service.GenerateDataAndDownloadService;
import com.osi.datagen.datageneration.service.IDataGenerator;
import com.osi.datagen.domain.Field;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public class DateDataGenerator implements IDataGenerator {

  public static List<String> pastMap = new ArrayList<String>();
  public static List<String> futureMap = new ArrayList<String>();

  public DateDataGenerator() {

    //Resource resource = new ClassPathResource(DasConstants.PAST_DATES_FIELDSETS_PATH);
    //Resource resource1 = new ClassPathResource(DasConstants.FUTURE_DATES_FIELDSETS_PATH);
    try {
      File pastFile =
          new File(
              GenerateDataAndDownloadService.fileResourcePath
                  + DasConstants.FILE_SEPRATOR
                  + DasConstants.PAST_DATES_FIELDSETS_PATH);
      pastMap = FileUtils.readLines(pastFile, "utf-8");
      File futureFile =
          new File(
              GenerateDataAndDownloadService.fileResourcePath
                  + DasConstants.FILE_SEPRATOR
                  + DasConstants.FUTURE_DATES_FIELDSETS_PATH);
      futureMap = FileUtils.readLines(futureFile, "utf-8");
    } catch (IOException e) {
      log.error("error while generating date generation", e);
      throw new RuntimeException("error while generating date generation");
    }
  }

  @Override
  public String generateData(Field field) {
    String columnName = field.getMappedCategory();
    if (pastMap.contains(columnName)) {
      return generateRandomDateBetween(
          Timestamp.valueOf(DasConstants.DATE_DATA_GENERATOR_BEGIN_DATE).getTime(),
          System.currentTimeMillis());
    } else if (futureMap.contains(columnName)) {
      return generateRandomDateBetween(
          System.currentTimeMillis(),
          Timestamp.valueOf(DasConstants.DATE_DATA_GENERATOR_END_DATE).getTime());
    }

    return generateRandomDateBetween(
        Timestamp.valueOf(DasConstants.DATE_DATA_GENERATOR_BEGIN_DATE).getTime(),
        Timestamp.valueOf(DasConstants.DATE_DATA_GENERATOR_END_DATE).getTime());
  }

  public String generateRandomDateBetween(long beginDate, long endDate) {
    long diff = endDate - beginDate + 1;
    Date date = new Date(beginDate + (long) (Math.random() * diff));
    LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    return singleQuote(localDate.toString());
  }
}
