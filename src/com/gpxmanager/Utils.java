package com.gpxmanager;

import com.gpxmanager.geocalc.Degree;
import com.gpxmanager.geocalc.EarthCalc;
import com.gpxmanager.gpx.beans.Waypoint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.gpxmanager.ProgramPreferences.DIR;
import static com.gpxmanager.ProgramPreferences.getPreference;
import static com.gpxmanager.ProgramPreferences.setPreference;

public class Utils {

    public static final SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
    public static final SimpleDateFormat DATE_HOUR_MINUTE = new SimpleDateFormat("yyyy-MM-dd kk:mm");
    public static final DateTimeFormatter DATE_FORMATER_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final String DEBUG_DIRECTORY = "MyGPXManagerDebug";
    public static final int METER_IN_KM = 1000;

    private static ResourceBundle labels;

    public static File getOpenSaveDirectory() {
        return new File(getPreference(DIR, System.getProperty("user.home")));
    }

    public static void setOpenSaveDirectory(File file) {
        setPreference(DIR, file.getAbsolutePath());
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

    public static String meterPerSecondToKmH(double value) {
        NumberFormat instance = DecimalFormat.getInstance();
        instance.setMaximumFractionDigits(2);
        return instance.format(value * 3.6);
    }

    public static int safeParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void writeToFile(String content, File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
