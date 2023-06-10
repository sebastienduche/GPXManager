package com.gpxmanager.watchdir;

import java.nio.file.Path;

public abstract class WatchDirListener {

    public abstract void eventCreated(Path path);

    public abstract void eventDeleted(Path path);

    public abstract void eventModified(Path path);
}
