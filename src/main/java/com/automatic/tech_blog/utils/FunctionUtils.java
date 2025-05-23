package com.automatic.tech_blog.utils;

import com.google.api.client.util.DateTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FunctionUtils {
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public static Date convertGoogleDateTimeToDate(DateTime googleDateTime) {
    try {
      // 1. Throw exception if input is null
      if (googleDateTime == null) {
        log.error("Input DateTime is null");
        throw new IllegalArgumentException("Input DateTime is null");
      }
      // 2. Google DateTime -> long milliseconds -> formatted Date String -> Date
      String formattedDate = DATE_FORMAT.format(new Date(googleDateTime.getValue()));
      return DATE_FORMAT.parse(formattedDate); // Return as java.util.Date
    } catch (ParseException e) {
      log.error("Error parsing date: {}", e.getMessage());
      return null;
    }
  }
}
