package com.gpxmanager.launcher;

import com.sebastienduche.MyLauncher;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;

public class MyGPXManagerLauncher extends MyLauncher {
    private static final String VERSION = "1.0";

    private MyGPXManagerLauncher() {
        super(MyGPXManagerServer.getInstance(), MyGPXManagerVersion.getLocalVersion(), "MyGPXManager.jar");
    }

    @Override
    public void install(File[] files, File directoryToDelete) {
        MyGPXManagerServer.Debug("MyGPXManagerLauncher version " + VERSION);
        MyGPXManagerServer.Debug("Installing new version...");
        if (files == null || files.length == 0) {
            MyGPXManagerServer.Debug("ERROR: Unable to list files");
            return;
        }
        MyGPXManagerVersion.setLocalVersion(MyGPXManagerServer.getInstance().getServerVersion());
        final File file = files[0];
        MyGPXManagerServer.Debug("Copying file " + file.getName() + " to current dir");
        try {
            copyFileToDirectory(file, new File("."));
            FileUtils.deleteDirectory(directoryToDelete);
            MyGPXManagerServer.Debug("Installing new version... Done");
        } catch (IOException e) {
            showException(e);
        }
    }

    private static void showException(Exception e) {
        StackTraceElement[] st = e.getStackTrace();
        String error = "";
        for (StackTraceElement elem : st) {
            error = error.concat("\n" + elem);
        }
        showMessageDialog(null, e.toString(), "Error", ERROR_MESSAGE);
        System.exit(999);
    }

    public static void main(String[] args) {
        new MyGPXManagerLauncher();
    }
}
