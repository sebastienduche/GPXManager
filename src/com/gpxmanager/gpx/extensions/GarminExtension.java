package com.gpxmanager.gpx.extensions;

import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Route;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.gpx.beans.Waypoint;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GarminExtension implements IExtensionParser {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public static final String TRACKPOINT_EXT = "gpxtpx:TrackPointExtension";
    public static final String ATEMP = "gpxtpx:atemp";

    @Override
    public String getId() {
        return "Garmin";
    }

    @Override
    public Object parseWaypointExtension(Node node) {
        if (node == null) {
            logger.error("null node received");
            return null;
        }
        Garmin garmin = new Garmin();
        NodeList childNodes = node.getChildNodes();
        for (int idx = 0; idx < childNodes.getLength(); idx++) {
            Node currentNode = childNodes.item(idx);
            if (currentNode.getNodeName().equals(TRACKPOINT_EXT)) {
                logger.debug("found TrackPointExtension");
                parseTrackPointExtension(currentNode, garmin);
            }
        }
        return garmin;
    }

    private void parseTrackPointExtension(Node node, Garmin garmin) {
        if (node == null) {
            logger.error("null node received");
            return;
        }
        NodeList childNodes = node.getChildNodes();
        for (int idx = 0; idx < childNodes.getLength(); idx++) {
            Node currentNode = childNodes.item(idx);
            if (currentNode.getNodeName().equals(ATEMP)) {
                logger.debug("found atemp");
                garmin.setTemperature(currentNode.getTextContent());
            }
        }
    }

    @Override
    public Object parseTrackExtension(Node node) {
        return null;
    }

    @Override
    public Object parseGPXExtension(Node node) {
        return null;
    }

    @Override
    public Object parseRouteExtension(Node node) {
        return null;
    }

    @Override
    public void writeGPXExtensionData(Node node, GPX wpt, Document doc) {
    }

    @Override
    public void writeWaypointExtensionData(Node wptNode, Waypoint wpt, Document doc) {
        Garmin garmin = (Garmin) wpt.getExtensionData(getId());
        if (garmin != null) {
            if (garmin.getTemperature() != null) {
                Node node = doc.createElement(TRACKPOINT_EXT);
                wptNode.appendChild(node);
                Node aTempNode = doc.createElement(ATEMP);
                aTempNode.appendChild(doc.createTextNode(garmin.getTemperature()));
                node.appendChild(aTempNode);
            }
        }
    }

    @Override
    public void writeTrackExtensionData(Node node, Track wpt, Document doc) {
    }

    @Override
    public void writeRouteExtensionData(Node node, Route wpt, Document doc) {
    }
}
