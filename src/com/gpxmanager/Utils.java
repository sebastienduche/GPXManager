package com.gpxmanager;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class Utils {

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    private static ResourceBundle labels;

    public static void copyToClipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection contents = new StringSelection(text);
        clipboard.setContents(contents, null);
    }

    public static void openUrl(String url) {
        String value = url.toLowerCase().strip();
        if (!value.startsWith(HTTP) && !value.startsWith(HTTPS)) {
            value = HTTP + url;
        }
        try {
            Desktop.getDesktop().browse(URI.create(value));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
