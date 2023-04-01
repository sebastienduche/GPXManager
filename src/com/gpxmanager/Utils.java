package com.gpxmanager;

import com.gpxmanager.geocalc.Degree;
import com.gpxmanager.geocalc.EarthCalc;
import com.gpxmanager.gpx.beans.Waypoint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class Utils {

    public static final SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
    public static final SimpleDateFormat DATE_HOUR_MINUTE = new SimpleDateFormat("yyyy-MM-dd kk:mm");
    public static final DateTimeFormatter DATE_FORMATER_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final String DEBUG_DIRECTORY = "MyGPXManagerDebug";

    private static ResourceBundle labels;
    private static final Preferences prefs = Preferences.userNodeForPackage(Utils.class);

    public static File getOpenSaveDirectory() {
        return new File(prefs.get("dir", System.getProperty("user.home")));
    }

    public static void setOpenSaveDirectory(File file) {
        prefs.put("dir", file.getAbsolutePath());
    }

    public static void initResources(Locale locale) {
        labels = ResourceBundle.getBundle("label", locale);
    }

    public static String getLabel(String s) {
        if (labels == null) {
            throw new RuntimeException("Resources not initialized!");
        }
        return labels.getString(s);
    }

    public static void saveError(Throwable e) {
        StackTraceElement[] st = e.getStackTrace();
        String error = "";
        for (StackTraceElement s : st) {
            error = error.concat("\n" + s);
        }
        try {
            String sDir = System.getProperty("user.home");
            if (!sDir.isEmpty()) {
                sDir += File.separator + DEBUG_DIRECTORY;
            }
            FileWriter errorFile = new FileWriter(new File(sDir, "Errors.log"), true);
            errorFile.write("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "]: " + error + "\n");
            errorFile.flush();
            errorFile.close();
        } catch (IOException ignored) {
        }
    }

    public static String cleanString(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.startsWith("\"")) {
            value = value.substring(1);
        }
        if (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    public static double getTrackDistance(List<Waypoint> waypoints) {
        LinkedList<Degree> points = new LinkedList<>();
        for (Waypoint trackPoint : waypoints) {
            points.add(new Degree(trackPoint.getLongitude(), trackPoint.getLatitude()));
        }
        return EarthCalc.calculateDistance(points) / 1000;
    }

    public static String roundValue(double value) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        return nf.format(value);
    }

}
