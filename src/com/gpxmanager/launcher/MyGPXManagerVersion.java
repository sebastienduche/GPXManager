package com.gpxmanager.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MyGPXManagerVersion {

    private static final String VERSION_FILE = "MyGPXManagerVersion.txt";

    public static String getLocalVersion() {
        // In directory bin
        File versionFile = new File(VERSION_FILE);
        if (versionFile.exists()) {
            try (var bufferReader = new BufferedReader(new FileReader(versionFile))) {
                return bufferReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            setLocalVersion("1.0");
        }
        return "";
    }

    public static void setLocalVersion(String version) {
        File f = new File(VERSION_FILE);
        try (var writer = new FileWriter(f)) {
            writer.write(version);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
