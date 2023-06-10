package com.gpxmanager.gpx;

import com.gpxmanager.ProgramPreferences;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Route;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.gpx.beans.Waypoint;
import com.gpxmanager.watchdir.WatchDir;
import com.gpxmanager.watchdir.WatchDirListener;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.gpxmanager.ProgramPreferences.GPS_MOUNT_ROOT;
import static com.gpxmanager.ProgramPreferences.getPreference;

public class GPXUtils {

    private final static GPXParser GPX_PARSER = new GPXParser();
    private static WatchDir watchDir;

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

    public static void writeFile(GPX gpx, File file) throws FileNotFoundException, ParserConfigurationException, TransformerException {
        GPX_PARSER.writeGPX(gpx, new FileOutputStream(file));
    }

    public static void initWatchDir(WatchDirListener watchDirListener) throws IOException {
        String rootDir = getPreference(GPS_MOUNT_ROOT, "");
        String path = ProgramPreferences.getPreference(ProgramPreferences.GPS_MOUNT_DIR, "");
        if (rootDir.isEmpty() || path.isEmpty()) {
            return;
        }
        if (watchDir == null) {
            watchDir = new WatchDir(Paths.get(rootDir), false, false);
        }
        watchDir.addWatchDirListener(watchDirListener);
        watchDir.execute();
    }

    public static boolean watchDirContains(Path path) {
        if (watchDir == null) {
            return false;
        }
        return watchDir.exist(path);
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
        String mountDir = ProgramPreferences.getPreference(ProgramPreferences.GPS_MOUNT_DIR, "");
        String targetDir = ProgramPreferences.getPreference(ProgramPreferences.GPS_TARGET_DIR, "");
        if (mountDir.isEmpty() || targetDir.isEmpty()) {
            throw new IOException("mountDir or targetDir is empty: " + mountDir + " / " + targetDir);
        }
        Path path = Paths.get(mountDir, targetDir);
        if (!path.toFile().exists()) {
            throw new IOException("The target dir in the device doesn't exist: " + path);
        }

        GPXUtils.writeFile(gpx, path.resolve(file).toFile());
    }
}
