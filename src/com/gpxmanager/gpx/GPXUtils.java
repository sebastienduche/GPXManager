package com.gpxmanager.gpx;

import com.gpxmanager.Utils;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Route;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.gpx.beans.Waypoint;
import com.gpxmanager.watchdir.WatchDirUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.abs;
import static java.util.Comparator.comparingDouble;

public class GPXUtils {

  private static final String CUT_HERE_KEYWORD = "Cut Here";
  private final static GPXParser GPX_PARSER = new GPXParser();

  public static GPXParser getGpxParser() {
    return GPX_PARSER;
  }

  public static GPX loadFile(File file) {
    if (file == null) {
      return null;
    }
    try {
      return GPX_PARSER.parseGPX(new FileInputStream(file));
    } catch (ParserConfigurationException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static void invertFile(GPX gpx) {
    if (gpx.getTracks() != null) {
      gpx.getTracks().forEach(track -> reverseAndCleanTime(track.getTrackPoints()));
    }
    if (gpx.getRoutes() != null) {
      gpx.getRoutes().forEach(route -> reverseAndCleanTime(route.getRoutePoints()));
    }
    if (gpx.getWaypoints() != null) {
      reverseAndCleanTime(gpx.getWaypoints());
    }
  }

  public static GPX mergeFiles(List<File> files) throws IOException, ParserConfigurationException, SAXException {
    GPX gpx = null;
    LinkedList<Track> tracks = null;
    LinkedList<Route> routes = null;
    LinkedList<Waypoint> waypoints = null;
    for (File file : files) {
      if (gpx == null) {
        gpx = new GPXParser().parseGPX(new FileInputStream(file));
        if (gpx.getTracks() == null) {
          gpx.setTracks(new LinkedList<>());
        }
        tracks = gpx.getTracks();
        if (gpx.getRoutes() == null) {
          gpx.setRoutes(new LinkedList<>());
        }
        routes = gpx.getRoutes();
        if (gpx.getWaypoints() == null) {
          gpx.setWaypoints(new LinkedList<>());
        }
        waypoints = gpx.getWaypoints();
      } else {
        GPX gpx1 = new GPXParser().parseGPX(new FileInputStream(file));
        if (gpx1.getTracks() != null) {
          tracks.addAll(gpx1.getTracks());
        }
        if (gpx1.getRoutes() != null) {
          routes.addAll(gpx1.getRoutes());
        }
        if (gpx1.getWaypoints() != null) {
          waypoints.addAll(gpx1.getWaypoints());
        }
      }
    }
    return gpx;
  }

  static GPXResult findWaypointCoordinate(File gpxFile, double latitude, double longitude, boolean keepFromPoint) throws IOException, ParserConfigurationException, SAXException, TransformerException {
    if (!Utils.checkFileExtension(gpxFile, ".gpx")) {
      return new GPXResult(null, "Input file is not a GPX File");
    }
    GPX gpx = new GPXParser().parseGPX(new FileInputStream(gpxFile));

    int count = 0;
    List<ArrayList<Waypoint>> allWaypoints = new ArrayList<>();
    if (gpx.getTracks() != null) {
      count = gpx.getTracks().size();
      List<ArrayList<Waypoint>> waypointsList = gpx.getTracks()
          .stream()
          .map(Track::getTrackPoints)
          .toList();
      allWaypoints.addAll(waypointsList);
    }
    if (gpx.getRoutes() != null) {
      count += gpx.getRoutes().size();
      List<ArrayList<Waypoint>> routesWaypoints = gpx.getRoutes()
          .stream()
          .map(Route::getRoutePoints)
          .toList();
      allWaypoints.addAll(routesWaypoints);
    }
    if (gpx.getWaypoints() != null) {
      count++;
      allWaypoints.add(new ArrayList<>(gpx.getWaypoints()));
    }

    if (markBestPosition(allWaypoints, latitude, longitude)) {
      if (count != 1) {
        // Unable to cut, return the file with the tagged waypoint.
        return new GPXResult(gpx, "Unable to remove part of the route because the current file contains multiple tracks/routes...\n" +
            " The found waypoint is marked with the name 'Cut Here'.");
      }
      // Only 1 route / track... we can cut
      ArrayList<Waypoint> waypoints = allWaypoints.get(0);
      ArrayList<Waypoint> finalList = new ArrayList<>();
      for (Waypoint waypoint : waypoints) {
        if (CUT_HERE_KEYWORD.equals(waypoint.getName())) {
          if (keepFromPoint) {
            waypoints.removeAll(finalList);
            finalList = waypoints;
          }
          if (!gpx.getTracks().isEmpty()) {
            gpx.getTracks().get(0).setTrackPoints(finalList);
          } else if (!gpx.getRoutes().isEmpty()) {
            gpx.getRoutes().get(0).setRoutePoints(finalList);
          } else {
            gpx.setWaypoints(new LinkedList<>(finalList));
          }
          break;
        } else {
          finalList.add(waypoint);
        }
      }
    } else {
      // Unable to mark the best waypoint
      return new GPXResult(gpx, "Unable to found a waypoint. The file is not modified.");
    }
    return new GPXResult(gpx);
  }

  private static boolean markBestPosition(List<ArrayList<Waypoint>> list, double latitude, double longitude) {
    List<Waypoint> foundWaypoints = new ArrayList<>();
    list
        .forEach(waypoints -> foundWaypoints.addAll(
            waypoints
                .stream()
                .sorted(comparingDouble(compare(latitude, longitude)))
                .toList()));
    if (foundWaypoints.isEmpty()) {
      return false;
    }
    foundWaypoints.get(0).setName(CUT_HERE_KEYWORD);
    return true;
  }

  private static ToDoubleFunction<Waypoint> compare(double latitude, double longitude) {
    return waypoint ->
        latitude == -1 ?
            abs(longitude - waypoint.getLongitude())
            : longitude == -1 ?
            abs(latitude - waypoint.getLatitude())
            : abs(latitude - waypoint.getLatitude()) + abs(longitude - waypoint.getLongitude());
  }


  public static void writeFile(GPX gpx, File file) throws FileNotFoundException, ParserConfigurationException, TransformerException {
    GPX_PARSER.writeGPX(gpx, new FileOutputStream(file));
  }

  private static void reverseAndCleanTime(List<Waypoint> waypoints) {
    Collections.reverse(waypoints);
    waypoints.forEach(waypoint -> waypoint.setTime(null));
  }

  public static void uploadToDevice(GPX gpx, String file) throws IOException, ParserConfigurationException, TransformerException {
    if (file == null || file.isEmpty()) {
      throw new IOException("File is null or doesn't exists: " + file);
    }
    if (gpx == null) {
      throw new IOException("Gpx file is null");
    }
    String mountDir = WatchDirUtil.getInstance().getMountDir();
    String targetDir = WatchDirUtil.getInstance().getTargetDir();
    if (mountDir.isEmpty() || targetDir.isEmpty()) {
      throw new IOException("mountDir or targetDir is empty: " + mountDir + " / " + targetDir);
    }
    Path path = Paths.get(mountDir, targetDir);
    if (!path.toFile().exists()) {
      throw new IOException("The target dir in the device doesn't exist: " + path);
    }

    GPXUtils.writeFile(gpx, path.resolve(file).toFile());
  }

  public static GPX csvToGpx(File file) {
    ArrayList<Waypoint> trackPoints = new ArrayList<>();
    LinkedList<Waypoint> waypoints = new LinkedList<>();
    parseCSVFile(file).forEach(csvLine -> {
      Waypoint waypoint = csvLine.buildWaypoint();
      trackPoints.add(waypoint);
      if (csvLine.waypoint()) {
        waypoints.add(waypoint);
      }
    });
    File gpxFile = getFileFromResource("empty.gpx");
    GPX gpx = loadFile(gpxFile);
    Track track = new Track();
    track.setTrackPoints(trackPoints);
    LinkedList<Track> tracks = new LinkedList<>();
    tracks.add(track);
    gpx.setTracks(tracks);
    gpx.setWaypoints(waypoints);
    return gpx;
  }

  public static List<CSVLine> parseCSVFile(File csvFile) {
    try (FileReader fileReader = new FileReader(csvFile);
         BufferedReader bufferedReader = new BufferedReader(fileReader)) {
      return bufferedReader.lines()
          .map(CSVLine::parse)
          .filter(Objects::nonNull)
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static File getFileFromResource(final String filename) {
    URL stream = GPXUtils.class.getClassLoader().getResource("resources/" + filename);
    if (stream == null) {
      return null;
    }
    return new File(stream.getFile());
  }

  public static GPX fixFile(GPX oldGpx) {
    File gpxFile = getFileFromResource("empty.gpx");
    GPX gpx = loadFile(gpxFile);
    gpx.setWaypoints(oldGpx.getWaypoints());
    gpx.setTracks(oldGpx.getTracks());
    gpx.setRoutes(oldGpx.getRoutes());
    gpx.setMetadata(oldGpx.getMetadata());
    return gpx;
  }
}

class GPXResult {
  private final GPX gpx;
  private final String result;

  public GPXResult(GPX gpx, String result) {
    this.gpx = gpx;
    this.result = result;
  }

  public GPXResult(GPX gpx) {
    this.gpx = gpx;
    result = null;
  }

  public boolean hasError() {
    return result != null;
  }

  public GPX getGpx() {
    return gpx;
  }

  public String getResult() {
    return result;
  }
}
