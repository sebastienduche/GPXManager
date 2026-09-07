package com.gpxmanager.csv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public abstract class CsvToJsonConverter {
  protected static final String TRUE = "true";
  protected static final String FALSE = "false";
  protected static final String VALUE_1_0 = "1.0";

  public static boolean matchFieldName(List<String> fieldNames, String jsonFieldName) {
    return fieldNames.stream().anyMatch(e -> e.equalsIgnoreCase(jsonFieldName));
  }

  public static String removeDoubleQuote(String value) {
    return value.replaceAll("^\"|\"$", "");
  }

  public abstract String mapFieldNameToJson(String jsonFieldName);

  public abstract void postProcessing(ObjectNode jsonObject, ObjectMapper mapper);

  public abstract String mapValue(String fieldName, String value);

}
