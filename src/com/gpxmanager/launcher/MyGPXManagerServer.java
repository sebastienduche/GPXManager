package com.gpxmanager.launcher;

import com.sebastienduche.Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static com.gpxmanager.MyGPXManager.INTERNAL_VERSION;

public class MyGPXManagerServer extends Server {

    private static final MyGPXManagerServer INSTANCE = new MyGPXManagerServer();

    private MyGPXManagerServer() {
        super("https://github.com/sebastienduche/gpxmanager/raw/master/Build/", "MyGPXManagerVersion.txt", "MyGPXManager", "MyGPXManagerDebug");
    }

    public static MyGPXManagerServer getInstance() {
        return INSTANCE;
    }

    public static void Debug(String sText) {
        getInstance().debug(sText);
    }

    public static String getLocalVersion() {
        // In directory bin
        File versionFile = new File(getInstance().getVersionFileName());
        if (versionFile.exists()) {
            try (var bufferReader = new BufferedReader(new FileReader(versionFile))) {
                return bufferReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            setLocalVersion(INTERNAL_VERSION);
            return INTERNAL_VERSION;
        }
        return "";
    }

    public static void setLocalVersion(String version) {
        File f = new File(getInstance().getVersionFileName());
        try (var writer = new FileWriter(f)) {
            writer.write(version);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
