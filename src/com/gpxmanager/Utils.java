package com.gpxmanager;

import com.gpxmanager.geocalc.Degree;
import com.gpxmanager.geocalc.EarthCalc;
import com.gpxmanager.gpx.beans.Waypoint;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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

public class Utils {

    public static final SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
    private static ResourceBundle labels;

    public static void copyToClipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection contents = new StringSelection(text);
        clipboard.setContents(contents, null);
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

    public static void saveError(Exception e) {
        StackTraceElement[] st = e.getStackTrace();
        String error = "";
        for (StackTraceElement s : st) {
            error = error.concat("\n" + s);
        }
        try {
            String sDir = System.getProperty("user.home");
            if (!sDir.isEmpty()) {
                sDir += File.separator + "MyPasswordManagerDebug";
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
