package com.gpxmanager.csv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gpxmanager.csv.CsvToJsonUtil.readLinesFromFile;

public final class CsvToJson<T> {

  private static final String TRUE = "true";
  private static final String FALSE = "false";

  private static final Gson GSON = new Gson();
  private final CsvToJsonConverter csvToJsonConverter;
  private final Class<T> objectClass;

  public CsvToJson(CsvToJsonConverter csvToJsonConverter, Class<T> objectClass) {
    this.csvToJsonConverter = csvToJsonConverter;
    this.objectClass = objectClass;
  }

  private static Map<String, Integer> mergeHeaders(List<String> headers) {
    // Find duplicate column names
    Map<String, Integer> headerCounts = new HashMap<>();

    for (String header : headers) {
      headerCounts.merge(header, 1, Integer::sum);
    }
    return headerCounts;
  }

  private static List<String> readHeaders(List<String> lines) {
    // First row = CSV header
    List<String> headers = formatLine(lines.get(0));

    if (headers == null || headers.isEmpty()) {
      throw new IllegalArgumentException("CSV file is empty");
    }
    return headers;
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

  private static String replaceCommasInQuotes(String line) {
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

  private static List<String> formatLine(String line) {
    if (line == null) {
      return null;
    }
    line = replaceCommasInQuotes(line);
    return List.of(line.split(","));
  }

  public static void main(String[] args) throws Exception {

//    File inputFile = new File("/Users/sebastien/Downloads/activities_test.csv");
//    File outputFile = new File("/Users/sebastien/Downloads/activities_test.json");

//    new CsvToJson(new ActivityConverter()).convertCsvToJson(inputFile, outputFile);

//    String jsonString = new CsvToJson(new ReactionConverter()).convertCsvToJsonString(new File("/Users/sebastien/Downloads/export_27594890/reactions.csv"));
//    System.out.println(jsonString);
    List<Reaction> reactions = new CsvToJson<>(new ReactionConverter(), Reaction.class).convertCsvToJsonObject(
        new File("/Users/sebastien/Downloads/export_27594890/reactions.csv"));
    System.out.println(reactions.size());
  }

  public void convertCsvToJson(File inputFile, File outputFile) throws IOException {

    List<String> lines = readLinesFromFile(inputFile);
    List<String> headers = readHeaders(lines);
    // Find duplicate column names
    Map<String, Integer> headerCounts = mergeHeaders(headers);

    ObjectMapper mapper = new ObjectMapper();
    ArrayNode jsonArray = processLines(mapper, lines, headers, headerCounts);

    mapper.writerWithDefaultPrettyPrinter()
        .writeValue(
            outputFile,
            jsonArray
        );
  }

  public String convertCsvToJsonString(File inputFile) throws IOException {

    List<String> lines = readLinesFromFile(inputFile);
    List<String> headers = readHeaders(lines);
    // Find duplicate column names
    Map<String, Integer> headerCounts = mergeHeaders(headers);

    ObjectMapper mapper = new ObjectMapper();
    ArrayNode jsonArray = processLines(mapper, lines, headers, headerCounts);

    StringWriter writer = new StringWriter(10);
    mapper.writerWithDefaultPrettyPrinter().writeValue(writer, jsonArray);
    return writer.toString();
  }

  public List<T> convertCsvToJsonObject(File inputFile) throws IOException {

    List<String> lines = readLinesFromFile(inputFile);
    List<String> headers = readHeaders(lines);
    Map<String, Integer> headerCounts = mergeHeaders(headers);

    ObjectMapper mapper = new ObjectMapper();
    ArrayNode jsonArray = processLines(mapper, lines, headers, headerCounts);

    StringWriter writer = new StringWriter(1000);
    mapper.writerWithDefaultPrettyPrinter().writeValue(writer, jsonArray);
    Type arrayType = TypeToken.getArray(objectClass).getType();

    T[] objects = GSON.fromJson(writer.toString(), arrayType);
    if (objects != null && objects.length > 0) {
      return new ArrayList<>(List.of(objects));
    }
    return Collections.emptyList();
  }

  public List<T> readJson(File file) {
    T[] obj = readJsonFile(file);
    if (obj == null) {
      return Collections.emptyList();
    }
    return new ArrayList<>(List.of(obj)); // To make the list mutable
  }

  private ArrayNode processLines(ObjectMapper mapper, List<String> lines, List<String> headers, Map<String, Integer> headerCounts) {
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

        String fieldName = csvToJsonConverter.mapFieldNameToJson(jsonFieldName);
        if (fieldName == null) {
          continue; // Skip this field
        }
        addValue(
            jsonObject,
            fieldName,
            csvToJsonConverter.mapValue(fieldName, value)
        );
      }

      csvToJsonConverter.postProcessing(jsonObject, mapper);

      jsonArray.add(jsonObject);
    }
    return jsonArray;
  }

  private T[] readJsonFile(File file) {
    try {
      String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
      Type arrayType = TypeToken.getArray(objectClass).getType();
      return GSON.fromJson(json, arrayType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
