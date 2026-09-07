package com.gpxmanager.csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gpxmanager.Utils;

import java.util.Date;
import java.util.List;

import static com.gpxmanager.Utils.TIMESTAMP;

public class ActivityConverter extends CsvToJsonConverter {
  private static final String START_DATE_LOCAL_JSOON = "start_date_local";
  private static final String ID_JSON = "id";
  private static final String NAME_JSON = "name";
  private static final String MAX_SPEED_JSON = "max_speed";
  private static final String AVERAGE_SPEED_JSON = "average_speed";
  private static final String TOTAL_ELEVATION_GAIN_JSON = "total_elevation_gain";
  private static final String COMMUTE_JSON = "commute";
  private static final String DISTANCE_JSON = "distance";
  private static final String MOVING_TIME_JSON = "moving_time";
  private static final String TYPE_JSON = "type";
  private static final String GEAR_ID_JSON = "gear_id";
  private static final String GEAR_JSON = "gear";
  private static final List<String> ACTIVITY_IDS = List.of("Activiteits-ID", "Activity ID");
  private static final List<String> ACTIVITY_DATES = List.of("Datum van activiteit", "Activity Date");
  private static final List<String> ACTIVITY_NAMES = List.of("Naam activiteit", "Activity Name");
  private static final List<String> ACTIVITY_DISTANCES = List.of("Afstand_2", "Distance_2");
  private static final List<String> ACTIVITY_MOVING_TIMES = List.of("Verstreken tijd_2", "Moving Time");
  private static final List<String> ACTIVITY_MAX_SPEEDS = List.of("Max. snelheid", "Max Speed");
  private static final List<String> ACTIVITY_AVG_SPEEDS = List.of("Gemiddelde snelheid", "Average Speed");
  private static final List<String> ACTIVITY_ELEVATIONS = List.of("Totale stijging", "Elevation Gain");
  private static final List<String> ACTIVITY_COMMUTES = List.of("Woon-werkverkeer", "Commute");
  private static final List<String> ACTIVITY_TYPES = List.of("Activiteitstype", "Activity Type");
  private static final List<String> ACTIVITY_BIKES = List.of("Fiets", "Bike");
  private static final List<String> ACTIVITY_EQUIPEMENTS = List.of("Uitrusting voor activiteit", "Activity Gear");

  @Override
  public String mapFieldNameToJson(String jsonFieldName) {
    if (matchFieldName(ACTIVITY_IDS, jsonFieldName)) {
      return ID_JSON;
    }
    if (matchFieldName(ACTIVITY_DATES, jsonFieldName)) {
      return START_DATE_LOCAL_JSOON;
    }
    if (matchFieldName(ACTIVITY_NAMES, jsonFieldName)) {
      return NAME_JSON;
    }
    if (matchFieldName(ACTIVITY_DISTANCES, jsonFieldName)) {
      return DISTANCE_JSON;
    }
    if (matchFieldName(ACTIVITY_MOVING_TIMES, jsonFieldName)) {
      return MOVING_TIME_JSON;
    }
    if (matchFieldName(ACTIVITY_TYPES, jsonFieldName)) {
      return TYPE_JSON;
    }
    if (matchFieldName(ACTIVITY_BIKES, jsonFieldName)) {
      return GEAR_ID_JSON;
    }
    if (matchFieldName(ACTIVITY_EQUIPEMENTS, jsonFieldName)) {
      return GEAR_JSON;
    }
    if ("Activity Description".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Elapsed Time".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Max Heart Rate".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Relative Effort".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if (matchFieldName(ACTIVITY_COMMUTES, jsonFieldName)) {
      return COMMUTE_JSON;
    }
    if ("Activity Private Note".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Filename".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Athlete Weight".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Bike Weight".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Elapsed Time_2".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if (matchFieldName(ACTIVITY_MAX_SPEEDS, jsonFieldName)) {
      return MAX_SPEED_JSON;
    }
    if (matchFieldName(ACTIVITY_AVG_SPEEDS, jsonFieldName)) {
      return AVERAGE_SPEED_JSON;
    }
    if (matchFieldName(ACTIVITY_ELEVATIONS, jsonFieldName)) {
      return TOTAL_ELEVATION_GAIN_JSON;
    }
    if ("Elevation Loss".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Elevation Low".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Elevation High".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Max Grade".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Average Grade".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Average Positive Grade".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Average Negative Grade".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Max Cadence".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Average Cadence".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Max Heart Rate_2".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Average Heart Rate".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Max Watts".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Average Watts".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Calories".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Max Temperature".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Average Temperature".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Relative Effort_2".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Total Work".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Number of Runs".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Uphill Time".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Downhill Time".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Other Time".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Perceived Exertion".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Start Time".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Weighted Average Power".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Power Count".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Prefer Perceived Exertion".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Perceived Relative Effort".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Commute_2".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Total Weight Lifted".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("From Upload".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Grade Adjusted Distance".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Weather Observation Time".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Weather Condition".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Weather Temperature".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Apparent Temperature".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Dewpoint".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Humidity".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Weather Pressure".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Wind Speed".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Wind Gust".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Wind Bearing".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Precipitation Intensity".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Sunrise Time".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Sunset Time".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Moon Phase".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Precipitation Probability".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Precipitation Type".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Cloud Cover".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Weather Visibility".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("UV Index".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Weather Ozone".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Jump Count".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Total Grit".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Average Flow".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Flagged".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Average Elapsed Speed".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Dirt Distance".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Newly Explored Distance".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Newly Explored Dirt Distance".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Activity Count".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Total Steps".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Carbon Saved".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Pool Length".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Training Load".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Intensity".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Average Grade Adjusted Pace".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Timer Time".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Total Cycles".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Recovery".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("With Pet".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Competition".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Long Run".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("For a Cause".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("With Kid".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Downhill Distance".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Total Sets".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Total Reps".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Media".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    return jsonFieldName;
  }

  @Override
  public String mapValue(String fieldName, String value) {
    value = removeDoubleQuote(value);
    if (fieldName.equals(START_DATE_LOCAL_JSOON)) {
      Date localDateTime = Utils.parseDateHourMinuteAm(value);
      if (localDateTime == null) {
        localDateTime = Utils.parseDateMmmTime(value);
      }
      if (localDateTime != null) {
        return TIMESTAMP.format(localDateTime);
      }
    }
    if (fieldName.equals(COMMUTE_JSON)) {
      if (VALUE_1_0.equals(value) || TRUE.equalsIgnoreCase(value)) {
        return TRUE;
      } else {
        return FALSE;
      }
    }
    return value;
  }

  @Override
  public void postProcessing(ObjectNode jsonObject, ObjectMapper mapper) {
    if (jsonObject.hasNonNull(GEAR_JSON)) {
      createGearObject(jsonObject, mapper);
    }
  }

  /**
   * Replace the simple "gear: bikeName" by an object with properties
   */
  public void createGearObject(ObjectNode jsonObject, ObjectMapper mapper) {
    JsonNode gearNode = jsonObject.get(GEAR_JSON);
    String name = gearNode.asText();
    jsonObject.remove(GEAR_JSON);
    if (jsonObject.hasNonNull(GEAR_ID_JSON)) {
      String gearId = jsonObject.get(GEAR_ID_JSON).asText();
      ObjectNode enrichedGearNode = mapper.createObjectNode();
      enrichedGearNode.put(ID_JSON, gearId);
      enrichedGearNode.put(NAME_JSON, name);
      jsonObject.set(GEAR_JSON, enrichedGearNode);
    }
  }
}
