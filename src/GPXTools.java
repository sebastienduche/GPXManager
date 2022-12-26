import com.gpxmanager.gpx.GPXParser;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Route;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.gpx.beans.Waypoint;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class GPXTools {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        GPX gpx = new GPXParser().parseGPX(new FileInputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_1.com.gpxmanager.gpx"));
        LinkedList<Route> routes = gpx.getRoutes();
        System.out.println("routes=" + (routes == null ? 0 : routes.size()));
        LinkedList<Track> tracks = gpx.getTracks();
        System.out.println("tracks=" + (tracks == null ? 0 : tracks.size()));
        LinkedList<Waypoint> waypoints = gpx.getWaypoints();
        System.out.println("waypoints=" + (waypoints == null ? 0 : waypoints.size()));

        GPX gpx1 = new GPXParser().parseGPX(new FileInputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_2.com.gpxmanager.gpx"));

        gpx1.getTracks().forEach(gpx::addTrack);

        GPX gpx2 = new GPXParser().parseGPX(new FileInputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_3.com.gpxmanager.gpx"));

        gpx2.getTracks().forEach(gpx::addTrack);

        gpx.addWaypoint(new Waypoint());
        gpx.addRoute(new Route());
        new GPXParser().writeGPX(gpx, new FileOutputStream("/Users/sebastien/Downloads/Ronde_Bourgogne_Sud_Dag_all.com.gpxmanager.gpx"));
    }
}