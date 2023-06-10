package com.gpxmanager.watchdir;
/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDir {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean trace;
    private final boolean recursive;
    private boolean running = false;

    private final List<WatchDirListener> watchDirListeners = new ArrayList<>();

    private final List<Path> paths = new ArrayList<>();

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        if (!dir.toFile().exists()) {
            return;
        }
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its subdirectories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and subdirectories
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    public WatchDir(Path dir, boolean isRecursive, boolean hasTrace) throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        keys = new HashMap<>();
        recursive = isRecursive;
        trace = hasTrace;
        new Thread(() -> {
            try {
                if (recursive) {
                    if (trace) {
                        System.out.format("Scanning %s ...\n", dir);
                    }
                    registerAll(dir);
                    if (trace) {
                        System.out.println("Done.");
                    }
                } else {
                    register(dir);
                }

                // enable trace after initial registration
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void addWatchDirListener(WatchDirListener watchDirListener) {
        watchDirListeners.add(watchDirListener);
    }

    public void removeWatchDirListener(WatchDirListener watchDirListener) {
        watchDirListeners.remove(watchDirListener);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    public void execute() {
        if (running) {
            return;
        }
        running = true;
        new Thread(() -> {
            for (; ; ) {
                // wait for key to be signalled
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return;
                }

                Path dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();

                    // TBD - provide example of how OVERFLOW event is handled
                    if (kind == OVERFLOW) {
                        continue;
                    }

                    // Context for directory entry event is the file name of entry
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    if (trace) {
                        // print out event
                        System.out.format("%s: %s\n", event.kind().name(), child);
                    }

                    // if directory is created, and watching recursively, then
                    // register it and its subdirectories
                    if (recursive && (kind == ENTRY_CREATE)) {
                        try {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException x) {
                            // ignore to keep sample readable
                        }
                    }

                    if (kind.equals(ENTRY_CREATE)) {
                        paths.add(child);
                        eventCreated(child);
                    } else if (kind.equals(ENTRY_MODIFY)) {
                        eventModified(child);
                    } else if (kind.equals(ENTRY_DELETE)) {
                        paths.remove(child);
                        eventDeleted(child);
                    }
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);

                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }
        }).start();
    }

    static void usage() {
        System.err.println("usage: java WatchDir [-r] dir");
        System.exit(-1);
    }

    public void eventCreated(Path child) {
        watchDirListeners.forEach(watchDirListener -> watchDirListener.eventCreated(child));
    }

    public void eventDeleted(Path child) {
        watchDirListeners.forEach(watchDirListener -> watchDirListener.eventDeleted(child));
    }

    public void eventModified(Path child) {
        watchDirListeners.forEach(watchDirListener -> watchDirListener.eventModified(child));
    }

    public boolean exist(Path path) {
        return paths.contains(path) || path.toFile().exists();
    }

    public static void main(String[] args) throws IOException {
        // parse arguments
//        if (args.length == 0 || args.length > 2)
//            usage();
//        boolean recursive = false;
//        int dirArg = 0;
//        if (args[0].equals("-r")) {
//            if (args.length < 2)
//                usage();
//            recursive = true;
//            dirArg++;
//        }

        // register directory and process its events
//        Path dir = Paths.get(args[dirArg]);
        WatchDir watchDir = new WatchDir(Paths.get("/Volumes"), false, false);
        watchDir.addWatchDirListener(new WatchDirListener() {
            @Override
            public void eventCreated(Path child) {
                System.out.println("Created: " + child);
            }

            @Override
            public void eventDeleted(Path child) {
                System.out.println("Deleted: " + child);
            }

            @Override
            public void eventModified(Path child) {
                System.out.println("Modified: " + child);
            }
        });
        watchDir.execute();
    }
}
