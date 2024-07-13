package com.gpxmanager;

import com.gpxmanager.geocalc.Degree;
import com.gpxmanager.geocalc.EarthCalc;
import com.gpxmanager.gpx.beans.Waypoint;
import com.gpxmanager.strava.StravaData;
import org.jstrava.entities.Activity;
import org.jstrava.entities.SegmentEffort;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.gpxmanager.MyGPXManager.GSON;
import static com.gpxmanager.ProgramPreferences.DIR;
import static com.gpxmanager.ProgramPreferences.STRAVA;
import static com.gpxmanager.ProgramPreferences.STRAVA_ALL_DATA;
import static com.gpxmanager.ProgramPreferences.STRAVA_ZIP_DATA;
import static com.gpxmanager.ProgramPreferences.getPreference;
import static com.gpxmanager.ProgramPreferences.setPreference;
import static java.util.stream.Collectors.groupingBy;

public class Utils {

  public static final SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
  public static final SimpleDateFormat DATE_HOUR_MINUTE = new SimpleDateFormat("yyyy-MM-dd kk:mm");
  public static final DateTimeFormatter DATE_FORMATER_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  public static final String DEBUG_DIRECTORY = "MyGPXManagerDebug";
  public static final int METER_IN_KM = 1000;

  private static ResourceBundle labels;
  private static Locale locale;
  private static boolean workDirCalculated = false;
  private static String workDir;

  public static File getOpenSaveDirectory() {
    return new File(getPreference(DIR, System.getProperty("user.home")));
  }

  public static void setOpenSaveDirectory(File file) {
    setPreference(DIR, file.getAbsolutePath());
  }

  public static void initResources(Locale locale1) {
    locale = locale1;
    labels = ResourceBundle.getBundle("label", locale);
  }

  public static String getLabel(String s) {
    if (labels == null) {
      throw new RuntimeException("Resources not initialized!");
    }
    return labels.getString(s);
  }

  public static Locale getLocale() {
    return locale;
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
    return value.trim();
  }

  public static double getTrackDistance(List<Waypoint> waypoints) {
    LinkedList<Degree> points = new LinkedList<>();
    for (Waypoint trackPoint : waypoints) {
      points.add(new Degree(trackPoint.getLongitude(), trackPoint.getLatitude()));
    }
    return EarthCalc.calculateDistance(points) / 1000;
  }

  public static double getTotalDistance(List<Activity> activities) {
    return activities.stream()
        .map(Activity::getDistance)
        .reduce(Double::sum)
        .orElseGet(() -> (double) 0) / METER_IN_KM;
  }

  public static int getDaysOverHundred(List<Activity> activities) {
    AtomicInteger count = new AtomicInteger();
    Map<Integer, List<Activity>> activitiesPerDay = activities
        .stream()
        .collect(groupingBy(Utils::getStartDay));
    activitiesPerDay.forEach((s, activities1) -> {
      if (getTotalDistance(activities1) >= 100) count.getAndIncrement();
    });
    return count.get();
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

  public static List<SegmentEffort> getPersonalRecords(Activity activity) {
    if (activity.getSegmentEfforts() == null) {
      return Collections.emptyList();
    }
    return activity.getSegmentEfforts()
        .stream()
        .filter(segmentEffort -> segmentEffort.getPrRank() != null)
        .collect(Collectors.toList());
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

  public static File checkFileNameWithExtension(File file) {
    if (file == null) {
      return null;
    }
    if (!file.getName().toLowerCase().endsWith(Filter.FILTER_GPX.toString())) {
      return new File(file.getAbsolutePath() + Filter.FILTER_GPX);
    }
    return file;
  }

  public static File checkFileNameWithZIPExtension(File file) {
    if (file == null) {
      return null;
    }
    if (!file.getName().toLowerCase().endsWith(Filter.FILTER_ZIP.toString())) {
      return new File(file.getAbsolutePath() + Filter.FILTER_ZIP);
    }
    return file;
  }

  public static boolean checkFileExtension(File file) {
    return checkFileExtension(file, Filter.FILTER_GPX);
  }

  public static boolean checkFileExtension(File file, Filter filter) {
    return checkFileExtension(file, filter.toString());
  }

  public static boolean checkFileExtension(File file, String extension) {
    if (file == null) {
      return false;
    }
    if (!file.getName().toLowerCase().endsWith(extension)) {
      return false;
    }
    return true;
  }

  public static JFileChooser createFileChooser() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.removeChoosableFileFilter(fileChooser.getFileFilter());
    fileChooser.addChoosableFileFilter(Filter.FILTER_GPX);
    return fileChooser;
  }

  public static int getStartYear(Activity activity) {
    try {
      return TIMESTAMP.parse(activity.getStartDateLocal()).getYear() + 1900;
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static int getStartMonth(Activity activity) {
    try {
      return TIMESTAMP.parse(activity.getStartDateLocal()).getMonth();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static int getStartDay(Activity activity) {
    try {
      Date parse = TIMESTAMP.parse(activity.getStartDateLocal());
      return LocalDate.of(parse.getYear(), parse.getMonth() + 1, parse.getDate()).getDayOfYear();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static StravaData loadStravaDataFile() {
    String existingFile = getPreference(STRAVA_ZIP_DATA, null);
    File file = new File(existingFile);
    if (checkFileExtension(file, Filter.FILTER_ZIP) && file.exists()) {
      try {
        unzipFile(file, new File(getWorkDir()));
        ProgramPreferences.removePreference(STRAVA_ALL_DATA);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return new StravaData(new File(existingFile),
          new File(getWorkDir(), "stravaConnection.txt"),
          new File(getWorkDir(), "stravaAll.json"));
    }
    return new StravaData(null,
        new File(getPreference(STRAVA, null)),
        new File(getPreference(STRAVA_ALL_DATA, null)));
  }

  public static void saveFile(List<Activity> activities) {
    StravaData stravaData = MyGPXManager.getStravaData();
    if (checkFileExtension(stravaData.getZipFile(), Filter.FILTER_ZIP)) {
      writeToFile(GSON.toJson(activities), stravaData.getJsonDataFile());
      try {
        zipFiles(stravaData.getFilesToSave(), stravaData.getZipFile());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      String existingFile = getPreference(STRAVA_ALL_DATA, null);
      writeToFile(GSON.toJson(activities), new File(existingFile));
    }
  }

  public static String getWorkDir() {
    if (workDirCalculated) {
      return workDir;
    }
    workDirCalculated = true;
    String sDir = System.getProperty("user.home");
    if (sDir.isEmpty()) {
      workDir = "." + File.separator + "MyGpxManager";
    } else {
      workDir = sDir + File.separator + "MyGpxManager";
    }
    File file = new File(workDir);
    if (!file.exists()) {
      if (!file.mkdir()) {
        // erreur?
      }
    }
    return workDir;
  }

  public static void zipFiles(List<String> filePaths, File zipFile) throws IOException {
    if (zipFile == null) {
      throw new FileNotFoundException("zipFile is null");
    }

    final FileOutputStream fos = new FileOutputStream(zipFile);
    ZipOutputStream zipOut = new ZipOutputStream(fos);

    for (String srcFile : filePaths) {
      File fileToZip = new File(srcFile);
      if (!fileToZip.exists()) {
        continue;
      }
      FileInputStream fis = new FileInputStream(fileToZip);
      ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
      zipOut.putNextEntry(zipEntry);

      byte[] bytes = new byte[1024];
      int length;
      while ((length = fis.read(bytes)) >= 0) {
        zipOut.write(bytes, 0, length);
      }
      fis.close();
    }

    zipOut.close();
    fos.close();
  }

  public static void unzipFile(File zipFile, File targetDirectory) throws IOException {
    byte[] buffer = new byte[1024];
    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    ZipEntry zipEntry = zis.getNextEntry();
    while (zipEntry != null) {
      File newFile = newFile(targetDirectory, zipEntry);
      if (zipEntry.isDirectory()) {
        if (!newFile.isDirectory() && !newFile.mkdirs()) {
          throw new IOException("Failed to create directory " + newFile);
        }
      } else {
        // fix for Windows-created archives
        File parent = newFile.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
          throw new IOException("Failed to create directory " + parent);
        }

        // write file content
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = zis.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
      }
      zipEntry = zis.getNextEntry();
    }

    zis.closeEntry();
    zis.close();
  }

  public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }
}
