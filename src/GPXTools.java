import com.gpxmanager.gpx.GPXUtils;
import com.gpxmanager.gpx.beans.GPX;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GPXTools {

    private static final String INVERT = "-invert";
    private static final String MERGE = "-merge";
    private static final String FILE = "-file";
    private static final String FILES = "-files";
    private static final String TARGET = "-target";

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        List<String> list = List.of(args);
        boolean invert = false;
        boolean merge = false;
        List<File> files = new ArrayList<>();
        File targetFile = null;
        if (list.contains(INVERT)) {
            if (!list.contains(FILE) || !list.contains(TARGET)) {
                System.out.println("When using " + INVERT + " an input file is needed with the parameter " + FILE + " <file>" +
                        " and an output file with the parameter " + TARGET + " <file>");
                displayUsage(list);
            } else {
                if (list.size() < 5 || list.indexOf(TARGET) < list.indexOf(FILE) + 2) {
                    System.out.println("When using " + INVERT + " an input file is needed with the parameter " + FILE + " <file>" +
                            " and an output file with the parameter " + TARGET + " <file>");
                    displayUsage(list);
                }
                invert = true;
                files.add(new File(list.get(list.indexOf(FILE) + 1)));
                targetFile = new File(list.get(list.indexOf(TARGET) + 1));

            }
        }

        if (list.contains(MERGE)) {
            if (!list.contains(FILES) || !list.contains(TARGET)) {
                System.out.println("When using " + MERGE + " a list of files are needed with the parameter " + FILES + " <file,file...>" +
                        " and an output file with the parameter " + TARGET + " <file>");
                displayUsage(list);
            } else {
                if (list.size() < 5 || list.indexOf(TARGET) < list.indexOf(FILES) + 2) {
                    System.out.println("When using " + MERGE + " an input file is needed with the parameter " + FILES + " <file,file...>" +
                            " and an output file with the parameter " + TARGET + " <file>");
                    displayUsage(list);
                }
                merge = true;
                List.of(list.get(list.indexOf(FILES) + 1).split(","))
                        .forEach(f -> files.add(new File(f)));
                if (files.size() < 2) {
                    System.out.println("When using " + MERGE + " at least 2 files must be provided with the parameter " + FILES + " <file,file...>");
                    displayUsage(list);
                }
                targetFile = new File(list.get(list.indexOf(TARGET) + 1));
            }
        }

        if (!invert && !merge) {
            displayUsage(list);
        }

        for (File file : files) {
            if (!file.exists()) {
                System.out.println("ERROR: File '" + file + "' doesn't exist!");
                System.exit(0);
            }
        }
        if (invert) {
            System.out.println("Inverting file '" + files.get(0) + "' into file '" + targetFile.getAbsolutePath() + "'");
            GPX gpx = GPXUtils.loadFile(files.get(0));
            GPXUtils.invertFile(gpx);
            GPXUtils.writeFile(gpx, targetFile);
            System.out.println("Inverting completed.");
        } else {
            System.out.println("Merging files '" + files + "' into file '" + targetFile.getAbsolutePath() + "'");
            GPX gpx = GPXUtils.mergeFiles(files);
            GPXUtils.writeFile(gpx, targetFile);
            System.out.println("Merging completed.");
        }
//        GPX gpx = new GPXParser().parseGPX(new FileInputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_1.com.gpxmanager.gpx"));
//        LinkedList<Route> routes = gpx.getRoutes();
//        System.out.println("routes=" + (routes == null ? 0 : routes.size()));
//        LinkedList<Track> tracks = gpx.getTracks();
//        System.out.println("tracks=" + (tracks == null ? 0 : tracks.size()));
//        LinkedList<Waypoint> waypoints = gpx.getWaypoints();
//        System.out.println("waypoints=" + (waypoints == null ? 0 : waypoints.size()));
//
//        GPX gpx1 = new GPXParser().parseGPX(new FileInputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_2.com.gpxmanager.gpx"));
//
//        gpx1.getTracks().forEach(gpx::addTrack);
//
//        GPX gpx2 = new GPXParser().parseGPX(new FileInputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_3.com.gpxmanager.gpx"));
//
//        gpx2.getTracks().forEach(gpx::addTrack);
//
//        gpx.addWaypoint(new Waypoint());
//        gpx.addRoute(new Route());
//        new GPXParser().writeGPX(gpx, new FileOutputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_all.com.gpxmanager.gpx"));
    }

    private static void displayUsage(List<String> list) {
        System.out.println("Usages:");
        System.out.println("  " + INVERT + " " + FILE + " <file> " + TARGET + " <file>");
        System.out.println("  " + MERGE + " " + FILES + " <file,file...> " + TARGET + " <file>");
        System.out.println();
        System.out.println("Your command line:");
        list.forEach(s -> System.out.print(s + " "));
        System.exit(0);
    }
}