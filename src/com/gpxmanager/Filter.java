package com.gpxmanager;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Filter extends FileFilter {

    public static final Filter FILTER_GPX = new Filter("gpx", Utils.getLabel("filter.file"));

    private final List<String> suffixes;
    private final String description;

    public Filter(List<String> suffixes, String description) {
        this.suffixes = suffixes.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        this.description = description;
    }

    private Filter(String suffixe, String description) {
        this(List.of(suffixe), description);
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String suffixe = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            suffixe = s.substring(i + 1).toLowerCase().strip();
        }
        return suffixe != null && suffixes.contains(suffixe);
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String toString() {
        return "." + suffixes.get(0);
    }

}
