package com.gpxmanager.gpx;

import com.gpxmanager.gpx.beans.GPX;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GPXTools {

  private static final String INVERT = "-invert";
  private static final String MERGE = "-merge";
  private static final String FILE = "-file";
  private static final String FILES = "-files";
  private static final String TARGET = "-target";
  private static final String CSV_TO_GPX = "-csvgpx";
  private static final String FIND_WAYPOINT = "-find";
  private static final String LATITUDE = "-lat";
  private static final String LONGITUDE = "-long";
  private static final String KEEP_BEFORE = "-before";

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {
    List<String> list = List.of(args);
    Action action = Action.NONE;
    List<File> files = new ArrayList<>();
    File targetFile = null;
    double latitude = -1;
    double longitude = -1;
    boolean keepFromPoint = true;
    if (list.contains(INVERT)) {
      if (!list.contains(FILE) || !list.contains(TARGET)) {
        System.out.println("When using " + INVERT + " an input file is needed with the parameter " + FILE + " <file>" +
            " and an output file with the parameter " + TARGET + " <file>");
        displayUsage(list);
      } else {
        if (list.size() < 5 || list.indexOf(TARGET) != list.indexOf(FILE) + 2) {
          System.out.println("When using " + INVERT + " an input file is needed with the parameter " + FILE + " <file>" +
              " and an output file with the parameter " + TARGET + " <file>");
          displayUsage(list);
        }
        action = Action.INVERT;
        files.add(new File(list.get(list.indexOf(FILE) + 1)));
        targetFile = new File(list.get(list.indexOf(TARGET) + 1));

      }
    }

    if (list.contains(MERGE)) {
      if (!list.contains(FILES) || !list.contains(TARGET)) {
        System.out.println("When using " + MERGE + " a list of files are needed with the parameter " + FILES + " <file,file...>" +
            " and an output file with the parameter " + TARGET + " <file>");
        displayUsage(list);
      } else {
        if (list.size() < 5 || list.indexOf(TARGET) != list.indexOf(FILES) + 2) {
          System.out.println("When using " + MERGE + " an input file is needed with the parameter " + FILES + " <file,file...>" +
              " and an output file with the parameter " + TARGET + " <file>");
          displayUsage(list);
        }
        action = Action.MERGE;
        List.of(list.get(list.indexOf(FILES) + 1).split(","))
            .forEach(f -> files.add(new File(f)));
        if (files.size() < 2) {
          System.out.println("When using " + MERGE + " at least 2 files must be provided with the parameter " + FILES + " <file,file...>");
          displayUsage(list);
        }
        targetFile = new File(list.get(list.indexOf(TARGET) + 1));
      }
    }

    if (list.contains(CSV_TO_GPX)) {
      if (!list.contains(FILE) || !list.contains(TARGET)) {
        System.out.println("When using " + CSV_TO_GPX + " an input CSV file is needed with the parameter " + FILE + " <file>" +
            " and an output file with the parameter " + TARGET + " <file>");
        displayUsage(list);
      } else {
        if (list.size() < 5 || list.indexOf(TARGET) != list.indexOf(FILE) + 2) {
          System.out.println("When using " + CSV_TO_GPX + " an CSV input file is needed with the parameter " + FILE + " <file>" +
              " and an output file with the parameter " + TARGET + " <file>");
          displayUsage(list);
        }
        action = Action.CVS_TO_GPX;
        files.add(new File(list.get(list.indexOf(FILE) + 1)));
        targetFile = new File(list.get(list.indexOf(TARGET) + 1));
      }
    }

    if (list.contains(FIND_WAYPOINT)) {
      if (!list.contains(FILE) || !list.contains(TARGET) || !(list.contains(LATITUDE) || list.contains(LONGITUDE))) {
        System.out.println("When using " + FIND_WAYPOINT + " an input file is needed with the parameter " + FILE + " <file>" +
            " and an output file with the parameter " + TARGET + " <file>.\n" +
            "Also, the latitude or the longitude must be present with the parameters " +
            LATITUDE + " <value> " + LONGITUDE + " <value>. (1 or 2 parameters can be present).\n" +
            "With the parameter " + KEEP_BEFORE + ", the track before the waypoint will be kept (default: keep the track after the waypoint).");
        displayUsage(list);
      } else {
        if (list.size() < 7 || list.indexOf(TARGET) != list.indexOf(FILE) + 2) {
          System.out.println("When using " + FIND_WAYPOINT + " an input file is needed with the parameter " + FILE + " <file>" +
              " and an output file with the parameter " + TARGET + " <file>.\n" +
              "Also, the latitude or the longitude must be present with the parameters " +
              LATITUDE + " <value> " + LONGITUDE + " <value>. (1 or 2 parameters can be present).\n" +
              "With the parameter " + KEEP_BEFORE + ", the track before the waypoint will be kept (default: keep the track after the waypoint).");
          displayUsage(list);
        }
        if (list.contains(KEEP_BEFORE)) {
          keepFromPoint = false;
        }
        int indexLatitude = list.indexOf(LATITUDE);
        if (indexLatitude != -1) {
          String value = list.get(indexLatitude + 1);
          try {
            latitude = Double.parseDouble(value);
          } catch (NumberFormatException e) {
            System.out.println("The latitude is not valid. Example: 45.12345");
            displayUsage(list);
          }
        }
        int indexLongitude = list.indexOf(LONGITUDE);
        if (indexLongitude != -1) {
          String value = list.get(indexLongitude + 1);
          try {
            longitude = Double.parseDouble(value);
          } catch (NumberFormatException e) {
            System.out.println("The longitude is not valid. Example: 45.12345");
            displayUsage(list);
          }
        }
        action = Action.FIND_WAYPOINT;
        files.add(new File(list.get(list.indexOf(FILE) + 1)));
        targetFile = new File(list.get(list.indexOf(TARGET) + 1));
      }
    }

    if (action == Action.NONE) {
      displayUsage(list);
    }

    for (File file : files) {
      if (!file.exists()) {
        System.out.println("ERROR: File '" + file + "' doesn't exist!");
        System.exit(0);
      }
    }
    if (action == Action.INVERT) {
      System.out.println("Inverting file '" + files.get(0) + "' into file '" + targetFile.getAbsolutePath() + "'");
      GPX gpx = GPXUtils.loadFile(files.get(0));
      GPXUtils.invertFile(gpx);
      GPXUtils.writeFile(gpx, targetFile);
      System.out.println("Inverting completed.");
    } else if (action == Action.MERGE) {
      System.out.println("Merging files '" + files + "' into file '" + targetFile.getAbsolutePath() + "'");
      GPX gpx = GPXUtils.mergeFiles(files);
      GPXUtils.writeFile(gpx, targetFile);
      System.out.println("Merging completed.");
    } else if (action == Action.CVS_TO_GPX) {
      System.out.println("Creating GPX file '" + targetFile.getAbsolutePath() + "' from CSV file '" + files.get(0) + "'");
      GPX gpx = GPXUtils.csvToGpx(files.get(0));
      GPXUtils.writeFile(gpx, targetFile);
      System.out.println("Creation completed.");
    } else if (action == Action.FIND_WAYPOINT) {
      System.out.println("Find a waypoint");
      GPXResult gpxResult = GPXUtils.findWaypointCoordinate(files.get(0), latitude, longitude, keepFromPoint);
      if (gpxResult.hasError()) {
        System.out.println(gpxResult.getResult());
        System.exit(1);
      }
      System.out.println("Creating GPX file '" + targetFile.getAbsolutePath() + "' from file '" + files.get(0) + "'");
      GPXUtils.writeFile(gpxResult.getGpx(), targetFile);
      System.out.println("Creation completed.");
    }
//        GPX gpx = new GPXParser().parseGPX(new FileInputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_1.com.gpxmanager.gpx"));
//        LinkedList<Route> routes = gpx.getRoutes();
//        System.out.println("routes=" + (routes == null ? 0 : routes.size()));
//        LinkedList<Track> tracks = gpx.getTracks();
//        System.out.println("tracks=" + (tracks == null ? 0 : tracks.size()));
//        LinkedList<Waypoint> waypoints = gpx.getWaypoints();
//        System.out.println("waypoints=" + (waypoints == null ? 0 : waypoints.size()));
//
//        GPX gpx1 = new GPXParser().parseGPX(new FileInputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_2.com.gpxmanager.gpx"));
//
//        gpx1.getTracks().forEach(gpx::addTrack);
//
//        GPX gpx2 = new GPXParser().parseGPX(new FileInputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_3.com.gpxmanager.gpx"));
//
//        gpx2.getTracks().forEach(gpx::addTrack);
//
//        gpx.addWaypoint(new Waypoint());
//        gpx.addRoute(new Route());
//        new GPXParser().writeGPX(gpx, new FileOutputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_all.com.gpxmanager.gpx"));
  }

  private static void displayUsage(List<String> list) {
    System.out.println("Usages:");
    System.out.println("  " + INVERT + " " + FILE + " <file> " + TARGET + " <file>");
    System.out.println("  " + MERGE + " " + FILES + " <file,file...> " + TARGET + " <file>");
    System.out.println("  " + CSV_TO_GPX + " " + FILE + " <file> " + TARGET + " <file>");
    System.out.println("  " + FIND_WAYPOINT + " " + FILE + " <file> " + TARGET + " <file> " + LATITUDE + " <value> (" + LONGITUDE + " <value> " + KEEP_BEFORE + ")");
    System.out.println("  " + FIND_WAYPOINT + " " + FILE + " <file> " + TARGET + " <file> " + LATITUDE + " <value> " + LONGITUDE + " <value>");
    System.out.println("----------------------------------");
    System.out.println(CSVLine.explain());
    System.out.println("----------------------------------");
    System.out.println("Your command line:");
    list.forEach(s -> System.out.print(s + " "));
    System.exit(0);
  }

  enum Action {
    NONE,
    MERGE,
    INVERT,
    CVS_TO_GPX,
    FIND_WAYPOINT
  }
}