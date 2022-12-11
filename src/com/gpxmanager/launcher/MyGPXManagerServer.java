package com.gpxmanager.launcher;

import com.sebastienduche.Server;

public class MyGPXManagerServer extends Server {

    private static final MyGPXManagerServer INSTANCE = new MyGPXManagerServer();

    private MyGPXManagerServer() {
        super("https://github.com/sebastienduche/mygpxmanager/raw/master/Build/", "MyGPXManagerVersion.txt", "MyGPXManager", "MyGPXManagerDebug");
    }

    public static MyGPXManagerServer getInstance() {
        return INSTANCE;
    }

    public static void Debug(String sText) {
        getInstance().debug(sText);
    }
}
