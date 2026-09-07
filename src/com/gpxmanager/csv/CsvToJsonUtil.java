package com.gpxmanager.csv;

import org.jstrava.entities.Activity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CsvToJsonUtil {

  public static void mergeReactions(List<Activity> activities, List<Reaction> reactions) throws IOException {

  }

  public static List<String> readLinesFromFile(File inputFile) throws IOException {
    List<String> lines = Files.readAllLines(inputFile.toPath(), UTF_8);
    if (lines.isEmpty()) {
      throw new IllegalArgumentException("CSV file is empty");
    }
    return lines;
  }

}
