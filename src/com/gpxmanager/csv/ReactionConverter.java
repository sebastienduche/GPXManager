package com.gpxmanager.csv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gpxmanager.Utils;

import java.util.Date;
import java.util.List;

import static com.gpxmanager.Utils.TIMESTAMP;

public class ReactionConverter extends CsvToJsonConverter {

  public static final String DATE_JSON = "date";
  public static final String TYPE_JSON = "type";
  public static final String PARENT_TYPE_JSON = "parent_type";
  public static final String PARENT_ID_JSON = "parent_id";
  private static final List<String> REACTION_DATES = List.of("Reactiedatum", "Reaction Date");
  private static final List<String> REACTION_TYPES = List.of("Reactietype", "Reaction Type");
  private static final List<String> REACTION_PARENT_TYPES = List.of("Bovenliggend type", "Parent Type");
  private static final List<String> REACTION_PARENT_IDS = List.of("Bovenliggende ID", "Parent ID");


  @Override
  public String mapFieldNameToJson(String jsonFieldName) {
    if (matchFieldName(REACTION_DATES, jsonFieldName)) {
      return DATE_JSON;
    }
    if (matchFieldName(REACTION_TYPES, jsonFieldName)) {
      return TYPE_JSON;
    }
    if (matchFieldName(REACTION_PARENT_TYPES, jsonFieldName)) {
      return PARENT_TYPE_JSON;
    }
    if (matchFieldName(REACTION_PARENT_IDS, jsonFieldName)) {
      return PARENT_ID_JSON;
    }
    return jsonFieldName;
  }

  @Override
  public void postProcessing(ObjectNode jsonObject, ObjectMapper mapper) {
  }

  @Override
  public String mapValue(String fieldName, String value) {
    value = removeDoubleQuote(value);
    if (fieldName.equals(DATE_JSON)) {
      Date localDateTime = Utils.parseDateHourMinuteAm(value);
      if (localDateTime == null) {
        localDateTime = Utils.parseDateMmmTime(value);
      }
      if (localDateTime != null) {
        return TIMESTAMP.format(localDateTime);
      }
    }
    return value;
  }
}
