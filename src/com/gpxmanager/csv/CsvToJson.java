package com.gpxmanager.csv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gpxmanager.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gpxmanager.Utils.TIMESTAMP;
import static java.nio.charset.StandardCharsets.UTF_8;

public class CsvToJson {

  public static final String START_DATE_LOCAL_JSOON = "start_date_local";
  public static final String ID_JSON = "id";
  public static final String NAME_JSON = "name";
  public static final String MAX_SPEED_JSON = "max_speed";
  public static final String AVERAGE_SPEED_JSON = "average_speed";
  public static final String TOTAL_ELEVATION_GAIN_JSON = "total_elevation_gain";
  public static final String COMMUTE_JSON = "commute";
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  public static final String VALUE_1_0 = "1.0";
  public static final String DISTANCE_JSON = "distance";
  public static final String MOVING_TIME_JSON = "moving_time";

  private static final List<String> ACTIVITY_IDS = List.of("Activiteits-ID", "Activity ID");
  private static final List<String> ACTIVITY_DATES = List.of("Datum van activiteit", "Activity Date");
  private static final List<String> ACTIVITY_NAMES = List.of("Naam activiteit", "Activity Name");
  private static final List<String> ACTIVITY_DISTANCES = List.of("Afstand_2", "Distance_2");
  private static final List<String> ACTIVITY_MOVING_TIME = List.of("Verstreken tijd_2", "Moving Time");
  private static final List<String> ACTIVITY_MAX_SPEED = List.of("Max. snelheid", "Max Speed");
  private static final List<String> ACTIVITY_AVG_SPEED = List.of("Gemiddelde snelheid", "Average Speed");
  private static final List<String> ACTIVITY_ELEVATION = List.of("Totale stijging", "Elevation Gain");
  private static final List<String> ACTIVITY_COMMUTE = List.of("Woon-werkverkeer", "Commute");

  public static void main(String[] args) throws Exception {

    File inputFile = new File("/Users/sebastien/Downloads/activities_test.csv");
    File outputFile = new File("/Users/sebastien/Downloads/activities_test.json");

    convertCsvToJson(inputFile, outputFile);
  }

  public static void convertCsvToJson(File inputFile, File outputFile) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    List<String> lines = Files.readAllLines(inputFile.toPath(), UTF_8);
    if (lines.isEmpty()) {
      throw new IllegalArgumentException("CSV file is empty");
    }

    // First row = CSV header
    List<String> headers = formatLine(lines.get(0));

    if (headers == null || headers.isEmpty()) {
      throw new IllegalArgumentException("CSV file is empty");
    }

    // Find duplicate column names
    Map<String, Integer> headerCounts = new HashMap<>();

    for (String header : headers) {
      headerCounts.merge(header, 1, Integer::sum);
    }

    ArrayNode jsonArray = mapper.createArrayNode();

    List<String> row;

    System.out.println("Count:" + lines.size());
    boolean skip = false;
    for (int i = 1; i < lines.size(); i++) {
      if (skip) {
        skip = false;
        continue;
      }
      String line = lines.get(i);
      System.out.println("Read line " + i + ": " + line);
      row = formatLine(line);
      if (row == null || row.isEmpty()) {
        continue;
      }
      if (row.size() < headers.size()) {
        line += lines.get(i + 1);
        System.out.println("Complete line : " + line);
        row = formatLine(line);
        skip = true;
      }

      ObjectNode jsonObject = mapper.createObjectNode();

      // Keeps track of duplicate headers for this row
      Map<String, Integer> occurrences = new HashMap<>();

      for (int j = 0; j < headers.size(); j++) {
        String header = headers.get(j);

        String value = j < row.size()
            ? row.get(j)
            : null;

        int occurrence = occurrences.merge(header, 1, Integer::sum);

        String jsonFieldName = header;
        if (headerCounts.get(header) > 1 && occurrence > 1) {
          jsonFieldName = header + "_" + occurrence;
        }

        String fieldName = mapFieldNameToJson(jsonFieldName);
        addValue(
            jsonObject,
            fieldName,
            mapValue(fieldName, value)
        );
      }

      jsonArray.add(jsonObject);
    }

    mapper.writerWithDefaultPrettyPrinter()
        .writeValue(
            outputFile,
            jsonArray
        );
  }

  private static String mapValue(String fieldName, String value) {
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

  /**
   * Converts the CSV value to an appropriate JSON type.
   */
  private static void addValue(
      ObjectNode object,
      String fieldName,
      String value) {

    // Empty CSV field -> JSON null
    if (value == null || value.trim().isEmpty()) {
      object.putNull(fieldName);
      return;
    }

    String v = value.trim();

    // Boolean
    if (TRUE.equalsIgnoreCase(v)) {
      object.put(fieldName, true);
      return;
    }

    if (FALSE.equalsIgnoreCase(v)) {
      object.put(fieldName, false);
      return;
    }

    // Integer / Long
    if (v.matches("-?\\d+")) {
      try {
        object.put(
            fieldName,
            Long.parseLong(v)
        );
        return;
      } catch (NumberFormatException ignored) {
        // Keep as String if number is too large
      }
    }

    // Decimal
    if (v.matches("-?\\d+\\.\\d+")) {
      try {
        object.put(
            fieldName,
            Double.parseDouble(v)
        );
        return;
      } catch (NumberFormatException ignored) {
        // Keep as String
      }
    }

    // Everything else -> String
    object.put(fieldName, value);
  }

  private static String removeDoubleQuote(String value) {
    return value.replaceAll("^\"|\"$", "");
  }

  public static String replaceCommasInQuotes(String line) {
    String newLine = line;
    int start = 0;
    int begin = line.indexOf('"', start);
    while (begin >= 0) {
      newLine = newLine.substring(0, begin);
      int end = line.indexOf('"', begin + 1);
      if (end > begin) {
        String temp = line.substring(begin, end);
        temp = temp.replace(",", "-");
        newLine += temp + "\"" + line.substring(end + 1);
        start = end + 1;
        begin = line.indexOf('"', start);
      } else {
        return line;
      }
    }
    return newLine;
  }

  private static String mapFieldNameToJson(String jsonFieldName) {
    if (ACTIVITY_IDS.stream().anyMatch(e -> e.equalsIgnoreCase(jsonFieldName))) {
      return ID_JSON;
    }
    if (ACTIVITY_DATES.stream().anyMatch(e -> e.equalsIgnoreCase(jsonFieldName))) {
      return START_DATE_LOCAL_JSOON;
    }
    if (ACTIVITY_NAMES.stream().anyMatch(e -> e.equalsIgnoreCase(jsonFieldName))) {
      return NAME_JSON;
    }
    if (ACTIVITY_DISTANCES.stream().anyMatch(e -> e.equalsIgnoreCase(jsonFieldName))) {
      return DISTANCE_JSON;
    }
    if (ACTIVITY_MOVING_TIME.stream().anyMatch(e -> e.equalsIgnoreCase(jsonFieldName))) {
      return MOVING_TIME_JSON;
    }
    if ("Activity Type".equals(jsonFieldName)) {
      return jsonFieldName;
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
    if (ACTIVITY_COMMUTE.stream().anyMatch(e -> e.equalsIgnoreCase(jsonFieldName))) {
      return COMMUTE_JSON;
    }
    if ("Activity Private Note".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Activity Gear".equals(jsonFieldName)) {
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
//    if ("Distance_2".equals(jsonFieldName)) {
//      return "distance";
//    }
    if (ACTIVITY_MAX_SPEED.stream().anyMatch(e -> e.equalsIgnoreCase(jsonFieldName))) {
      return MAX_SPEED_JSON;
    }
    if (ACTIVITY_AVG_SPEED.stream().anyMatch(e -> e.equalsIgnoreCase(jsonFieldName))) {
      return AVERAGE_SPEED_JSON;
    }
    if (ACTIVITY_ELEVATION.stream().anyMatch(e -> e.equalsIgnoreCase(jsonFieldName))) {
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
    if ("Type".equals(jsonFieldName)) {
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
    if ("Bike".equals(jsonFieldName)) {
      return jsonFieldName;
    }
    if ("Gear".equals(jsonFieldName)) {
      return "gear";
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

  public static List<String> formatLine(String line) {
    if (line == null) {
      return null;
    }
    line = replaceCommasInQuotes(line);
    return List.of(line.split(","));
  }
}
