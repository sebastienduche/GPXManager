package com.gpxmanager.gpx;

import com.gpxmanager.gpx.beans.GPX;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.gpxmanager.gpx.GPXToolsOption.CSV_TO_GPX;
import static com.gpxmanager.gpx.GPXToolsOption.FIND_WAYPOINT;
import static com.gpxmanager.gpx.GPXToolsOption.FIX;
import static com.gpxmanager.gpx.GPXToolsOption.INVERT;
import static com.gpxmanager.gpx.GPXToolsOption.MERGE;
import static com.gpxmanager.gpx.GPXToolsOption.findByName;

public class GPXTools {

  private static final String FILE = "-file";
  private static final String FILES = "-files";
  private static final String TARGET = "-target";
  private static final String LATITUDE = "-lat";
  private static final String LONGITUDE = "-long";
  private static final String KEEP_BEFORE = "-before";

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {
    List<String> list = List.of(args);
    GPXToolsAction action = null;
    GPXToolsOption gpxToolsOption = findByName(list);
    switch (gpxToolsOption) {
      case INVERT -> {
        if (!list.contains(FILE) || !list.contains(TARGET)) {
          System.out.println("When using " + INVERT.getName() + " an input file is needed with the parameter " + FILE + " <file>" +
              " and an output file with the parameter " + TARGET + " <file>");
          displayUsage(list);
        } else {
          if (list.size() < 5 || list.indexOf(TARGET) != list.indexOf(FILE) + 2) {
            System.out.println("When using " + INVERT.getName() + " an input file is needed with the parameter " + FILE + " <file>" +
                " and an output file with the parameter " + TARGET + " <file>");
            displayUsage(list);
          }
          action = new GPXToolsAction(GPXToolsAction.Action.INVERT_ACTION, List.of(new File(list.get(list.indexOf(FILE) + 1))), new File(list.get(list.indexOf(TARGET) + 1)));
        }
      }
      case MERGE -> {
        if (!list.contains(FILES) || !list.contains(TARGET)) {
          System.out.println("When using " + MERGE.getName() + " a list of files are needed with the parameter " + FILES + " <file,file...>" +
              " and an output file with the parameter " + TARGET + " <file>");
          displayUsage(list);
        } else {
          if (list.size() < 5 || list.indexOf(TARGET) != list.indexOf(FILES) + 2) {
            System.out.println("When using " + MERGE.getName() + " an input file is needed with the parameter " + FILES + " <file,file...>" +
                " and an output file with the parameter " + TARGET + " <file>");
            displayUsage(list);
          }
          List<File> files = new ArrayList<>();
          List.of(list.get(list.indexOf(FILES) + 1).split(","))
              .forEach(f -> files.add(new File(f)));
          if (files.size() < 2) {
            System.out.println("When using " + MERGE.getName() + " at least 2 files must be provided with the parameter " + FILES + " <file,file...>");
            displayUsage(list);
          }
          action = new GPXToolsAction(GPXToolsAction.Action.MERGE_ACTION, files, new File(list.get(list.indexOf(TARGET) + 1)));
        }
      }
      case CSV_TO_GPX -> {
        if (!list.contains(FILE) || !list.contains(TARGET)) {
          System.out.println("When using " + CSV_TO_GPX.getName() + " an input CSV file is needed with the parameter " + FILE + " <file>" +
              " and an output file with the parameter " + TARGET + " <file>");
          displayUsage(list);
        } else {
          if (list.size() < 5 || list.indexOf(TARGET) != list.indexOf(FILE) + 2) {
            System.out.println("When using " + CSV_TO_GPX.getName() + " an CSV input file is needed with the parameter " + FILE + " <file>" +
                " and an output file with the parameter " + TARGET + " <file>");
            displayUsage(list);
          }
          action = new GPXToolsAction(GPXToolsAction.Action.CVS_TO_GPX_ACTION, List.of(new File(list.get(list.indexOf(FILE) + 1))), new File(list.get(list.indexOf(TARGET) + 1)));
        }
      }
      case FIND_WAYPOINT -> {
        if (!list.contains(FILE) || !list.contains(TARGET) || !(list.contains(LATITUDE) || list.contains(LONGITUDE))) {
          System.out.println("When using " + FIND_WAYPOINT.getName() + " an input file is needed with the parameter " + FILE + " <file>" +
              " and an output file with the parameter " + TARGET + " <file>.\n" +
              "Also, the latitude or the longitude must be present with the parameters " +
              LATITUDE + " <value> " + LONGITUDE + " <value>. (1 or 2 parameters can be present).\n" +
              "With the parameter " + KEEP_BEFORE + ", the track before the waypoint will be kept (default: keep the track after the waypoint).");
          displayUsage(list);
        } else {
          if (list.size() < 7 || list.indexOf(TARGET) != list.indexOf(FILE) + 2) {
            System.out.println("When using " + FIND_WAYPOINT.getName() + " an input file is needed with the parameter " + FILE + " <file>" +
                " and an output file with the parameter " + TARGET + " <file>.\n" +
                "Also, the latitude or the longitude must be present with the parameters " +
                LATITUDE + " <value> " + LONGITUDE + " <value>. (1 or 2 parameters can be present).\n" +
                "With the parameter " + KEEP_BEFORE + ", the track before the waypoint will be kept (default: keep the track after the waypoint).");
            displayUsage(list);
          }
          boolean keepFromPoint = true;
          double latitude = -1;
          double longitude = -1;
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
          action = new GPXToolsAction(GPXToolsAction.Action.FIND_WAYPOINT_ACTION, List.of(new File(list.get(list.indexOf(FILE) + 1))), new File(list.get(list.indexOf(TARGET) + 1)), latitude, longitude, keepFromPoint);
        }
      }
      case FIX -> {
        if (!list.contains(FILE) || !list.contains(TARGET)) {
          System.out.println("When using " + FIX.getName() + " an input file is needed with the parameter " + FILE + " <file>" +
              " and an output file with the parameter " + TARGET + " <file>");
          displayUsage(list);
        } else {
          if (list.size() < 5 || list.indexOf(TARGET) != list.indexOf(FILE) + 2) {
            System.out.println("When using " + FIX.getName() + " an input file is needed with the parameter " + FILE + " <file>" +
                " and an output file with the parameter " + TARGET + " <file>");
            displayUsage(list);
          }
          action = new GPXToolsAction(GPXToolsAction.Action.FIX_ACTION, List.of(new File(list.get(list.indexOf(FILE) + 1))), new File(list.get(list.indexOf(TARGET) + 1)));
        }
      }
    }

    if (action == null) {
      displayUsage(list);
      return;
    }

    for (File file : action.getFiles()) {
      if (!file.exists()) {
        System.out.println("ERROR: File '" + file + "' doesn't exist!");
        System.exit(0);
      }
    }
    switch (action.getAction()) {
      case INVERT_ACTION -> {
        System.out.println("Inverting file '" + action.getFiles().get(0) + "' into file '" + action.getTargetFile().getAbsolutePath() + "'");
        GPX gpx = GPXUtils.loadFile(action.getFiles().get(0));
        GPXUtils.invertFile(gpx);
        GPXUtils.writeFile(gpx, action.getTargetFile());
        System.out.println("Inverting completed.");
      }
      case MERGE_ACTION -> {
        System.out.println("Merging files '" + action.getFiles() + "' into file '" + action.getTargetFile().getAbsolutePath() + "'");
        GPX gpx = GPXUtils.mergeFiles(action.getFiles());
        GPXUtils.writeFile(gpx, action.getTargetFile());
        System.out.println("Merging completed.");
      }
      case CVS_TO_GPX_ACTION -> {
        System.out.println("Creating GPX file '" + action.getTargetFile().getAbsolutePath() + "' from CSV file '" + action.getFiles().get(0) + "'");
        GPX gpx = GPXUtils.csvToGpx(action.getFiles().get(0));
        GPXUtils.writeFile(gpx, action.getTargetFile());
        System.out.println("Creation completed.");
      }
      case FIND_WAYPOINT_ACTION -> {
        System.out.println("Find a waypoint");
        GPXResult gpxResult = GPXUtils.findWaypointCoordinate(action.getFiles().get(0), action.getLatitude(), action.getLongitude(), action.isKeepFromPoint());
        if (gpxResult.hasError()) {
          System.out.println(gpxResult.getResult());
          System.exit(1);
        }
        System.out.println("Creating GPX file '" + action.getTargetFile().getAbsolutePath() + "' from file '" + action.getFiles().get(0) + "'");
        GPXUtils.writeFile(gpxResult.getGpx(), action.getTargetFile());
        System.out.println("Creation completed.");
      }
      case FIX_ACTION -> {
        System.out.println("Fixing file '" + action.getFiles().get(0) + "' into file '" + action.getTargetFile().getAbsolutePath() + "'");
        GPX gpx = GPXUtils.loadFile(action.getFiles().get(0));
        GPX newGPX = GPXUtils.fixFile(gpx);
        GPXUtils.writeFile(newGPX, action.getTargetFile());
        System.out.println("Fixing completed.");
      }
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
    System.out.println("  " + INVERT.getName() + " " + FILE + " <file> " + TARGET + " <file>");
    System.out.println("  " + MERGE.getName() + " " + FILES + " <file,file...> " + TARGET + " <file>");
    System.out.println("  " + FIX.getName() + " " + FILE + " <file> " + TARGET + " <file>");
    System.out.println("  " + CSV_TO_GPX.getName() + " " + FILE + " <file> " + TARGET + " <file>");
    System.out.println("  " + FIND_WAYPOINT.getName() + " " + FILE + " <file> " + TARGET + " <file> " + LATITUDE + " <value> (" + LONGITUDE + " <value> " + KEEP_BEFORE + ")");
    System.out.println("  " + FIND_WAYPOINT.getName() + " " + FILE + " <file> " + TARGET + " <file> " + LATITUDE + " <value> " + LONGITUDE + " <value>");
    System.out.println("----------------------------------");
    System.out.println(CSVLine.explain());
    System.out.println("----------------------------------");
    System.out.println("Your command line:");
    list.forEach(s -> System.out.print(s + " "));
    System.exit(0);
  }

  private static class GPXToolsAction {
    private final Action action;
    List<File> files;
    File targetFile;
    double latitude = -1;
    double longitude = -1;
    boolean keepFromPoint = true;

    public GPXToolsAction(Action action, List<File> files, File targetFile) {
      this.action = action;
      this.files = files;
      this.targetFile = targetFile;
    }

    public GPXToolsAction(Action action, List<File> files, File targetFile, double latitude, double longitude, boolean keepFromPoint) {
      this.action = action;
      this.files = files;
      this.targetFile = targetFile;
      this.latitude = latitude;
      this.longitude = longitude;
      this.keepFromPoint = keepFromPoint;
    }

    public Action getAction() {
      return action;
    }

    public List<File> getFiles() {
      return files;
    }

    public File getTargetFile() {
      return targetFile;
    }

    public double getLatitude() {
      return latitude;
    }

    public double getLongitude() {
      return longitude;
    }

    public boolean isKeepFromPoint() {
      return keepFromPoint;
    }

    public enum Action {
      MERGE_ACTION,
      INVERT_ACTION,
      CVS_TO_GPX_ACTION,
      FIND_WAYPOINT_ACTION,
      FIX_ACTION
    }
  }

}