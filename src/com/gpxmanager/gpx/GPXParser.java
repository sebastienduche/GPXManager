/*
 * GPXParser.java
 * 
 * Copyright (c) 2012, AlternativeVision. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.gpxmanager.gpx;

import com.gpxmanager.gpx.beans.*;
import com.gpxmanager.gpx.extensions.IExtensionParser;
import com.gpxmanager.gpx.types.FixType;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * <p>This class defines methods for parsing and writing com.gpxmanager.gpx files.</p>
 * <br>
 * Usage for parsing a com.gpxmanager.gpx file into a {@link GPX} object:<br>
 * <code>
 * GPXParser p = new GPXParser();<br>
 * FileInputStream in = new FileInputStream("inFile.com.gpxmanager.gpx");<br>
 * GPX com.gpxmanager.gpx = p.parseGPX(in);<br>
 * </code>
 * <br>
 * Usage for writing a {@link GPX} object to a file:<br>
 * <code>
 * GPXParser p = new GPXParser();<br>
 * FileOutputStream out = new FileOutputStream("outFile.com.gpxmanager.gpx");<br>
 * p.writeGPX(com.gpxmanager.gpx, out);<br>
 * out.close();<br>
 * </code>
 */
public class GPXParser {
	
	private ArrayList<IExtensionParser> extensionParsers = new ArrayList<>();
	
	/**
	 * Adds a new extension parser to be used when parsing a com.gpxmanager.gpx steam
	 * 
	 * @param parser an instance of a {@link IExtensionParser} implementation
	 */
	public void addExtensionParser(IExtensionParser parser) {
		extensionParsers.add(parser);
	}
	
	/**
	 * Removes an extension parser previously added
	 * 
	 * @param parser an instance of a {@link IExtensionParser} implementation
	 */
	public void removeExtensionParser(IExtensionParser parser) {
		extensionParsers.remove(parser);
	}
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Parses a stream containing GPX data
	 *   
	 * @param in the input stream
	 * @return {@link GPX} object containing parsed data, or null if no com.gpxmanager.gpx data was found in the seream
	 */
	public GPX parseGPX(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(in);
		Node firstChild = doc.getFirstChild();
		if( firstChild != null && GPXConstants.GPX_NODE.equals(firstChild.getNodeName())) {
			GPX gpx = new GPX();
			NamedNodeMap attrs = firstChild.getAttributes();
			for(int idx = 0; idx < attrs.getLength(); idx++) {
				Node attr = attrs.item(idx);
				if(GPXConstants.VERSION_ATTR.equals(attr.getNodeName())) {
					gpx.setVersion(attr.getNodeValue());
				} else if(GPXConstants.CREATOR_ATTR.equals(attr.getNodeName())) {
					gpx.setCreator(attr.getNodeValue());
				}
			}
			NodeList nodes = firstChild.getChildNodes();
			logger.debug("Found " +nodes.getLength()+ " child nodes. Start parsing ...");
			for(int idx = 0; idx < nodes.getLength(); idx++) {
				Node currentNode = nodes.item(idx);
				switch (currentNode.getNodeName()) {
					case GPXConstants.WPT_NODE -> {
						logger.debug("Found waypoint node. Start parsing...");
						Waypoint w = parseWaypoint(currentNode);
						if (w != null) {
							logger.info("Add waypoint to com.gpxmanager.gpx data. [waypointName=" + w.getName() + "]");
							gpx.addWaypoint(w);
						}
					}
					case GPXConstants.TRK_NODE -> {
						logger.debug("Found track node. Start parsing...");
						Track trk = parseTrack(currentNode);
						if (trk != null) {
							logger.info("Add track to com.gpxmanager.gpx data. [trackName=" + trk.getName() + "]");
							gpx.addTrack(trk);
						}
					}
					case GPXConstants.EXTENSIONS_NODE -> {
						logger.debug("Found extensions node. Start parsing...");
						for (IExtensionParser parser : extensionParsers) {
							Object data = parser.parseGPXExtension(currentNode);
							gpx.addExtensionData(parser.getId(), data);
						}
					}
					case GPXConstants.RTE_NODE -> {
						logger.debug("Found route node. Start parsing...");
						Route rte = parseRoute(currentNode);
						if (rte != null) {
							logger.info("Add route to com.gpxmanager.gpx data. [routeName=" + rte.getName() + "]");
							gpx.addRoute(rte);
						}
					}
					case GPXConstants.METADATA_NODE -> {
						logger.debug("Found metadata node. Start parsing...");
						Metadata rte = parseMetadata(currentNode);
						if (rte != null) {
							logger.info("Add metadata to com.gpxmanager.gpx data. [Name=" + rte.getName() + "]");
							gpx.setMetadata(rte);
						}
					}
				}
			}
			//TODO: parse route node
			return gpx;
		} else {
			logger.error("FATAL!! - Root node is not com.gpxmanager.gpx.");
		}
		return null;
	}
	
	/**
	 * Parses a wpt node into a Waypoint object 
	 * 
	 * @return Waypoint object with info from the received node
	 */
	private Waypoint parseWaypoint(Node node) {
		if(node == null) {
			logger.error("null node received");
			return null;
		}
		Waypoint w = new Waypoint();
		NamedNodeMap attrs = node.getAttributes();
		//check for lat attribute
		Node latNode = attrs.getNamedItem(GPXConstants.LAT_ATTR);
		if(latNode != null) {
			Double latVal = null;
			try {
				latVal = Double.parseDouble(latNode.getNodeValue());
			} catch(NumberFormatException ex) {
				logger.error("bad lat value in waypoint data: " + latNode.getNodeValue());
			}
			w.setLatitude(latVal);
		} else {
			logger.warn("no lat value in waypoint data.");
		}
		//check for lon attribute
		Node lonNode = attrs.getNamedItem(GPXConstants.LON_ATTR);
		if(lonNode != null) {
			Double lonVal = null;
			try {
				lonVal = Double.parseDouble(lonNode.getNodeValue());
			} catch(NumberFormatException ex) {
				logger.error("bad lon value in waypoint data: " + lonNode.getNodeValue());
			}
			w.setLongitude(lonVal);
		} else {
			logger.warn("no lon value in waypoint data.");
		}
		
		NodeList childNodes = node.getChildNodes();
		if(childNodes != null) {
			for(int idx = 0; idx < childNodes.getLength(); idx++) {
				Node currentNode = childNodes.item(idx);
				switch (currentNode.getNodeName()) {
					case GPXConstants.ELE_NODE -> {
						logger.debug("found ele node in waypoint data");
						w.setElevation(getNodeValueAsDouble(currentNode));
					}
					case GPXConstants.TIME_NODE -> {
						logger.debug("found time node in waypoint data");
						w.setTime(getNodeValueAsDate(currentNode));
					}
					case GPXConstants.NAME_NODE -> {
						logger.debug("found name node in waypoint data");
						w.setName(getNodeValueAsString(currentNode));
					}
					case GPXConstants.CMT_NODE -> {
						logger.debug("found cmt node in waypoint data");
						w.setComment(getNodeValueAsString(currentNode));
					}
					case GPXConstants.DESC_NODE -> {
						logger.debug("found desc node in waypoint data");
						w.setDescription(getNodeValueAsString(currentNode));
					}
					case GPXConstants.SRC_NODE -> {
						logger.debug("found src node in waypoint data");
						w.setSrc(getNodeValueAsString(currentNode));
					}
					case GPXConstants.MAGVAR_NODE -> {
						logger.debug("found magvar node in waypoint data");
						w.setMagneticDeclination(getNodeValueAsDouble(currentNode));
					}
					case GPXConstants.GEOIDHEIGHT_NODE -> {
						logger.debug("found geoidheight node in waypoint data");
						w.setGeoidHeight(getNodeValueAsDouble(currentNode));
					}
					case GPXConstants.LINK_NODE -> logger.debug("found link node in waypoint data");

					//TODO: parse link
					//w.setGeoidHeight(getNodeValueAsDouble(currentNode));
					case GPXConstants.SYM_NODE -> {
						logger.debug("found sym node in waypoint data");
						w.setSym(getNodeValueAsString(currentNode));
					}
					case GPXConstants.FIX_NODE -> {
						logger.debug("found fix node in waypoint data");
						w.setFix(getNodeValueAsFixType(currentNode));
					}
					case GPXConstants.TYPE_NODE -> {
						logger.debug("found type node in waypoint data");
						w.setType(getNodeValueAsString(currentNode));
					}
					case GPXConstants.SAT_NODE -> {
						logger.debug("found sat node in waypoint data");
						w.setSat(getNodeValueAsInteger(currentNode));
					}
					case GPXConstants.HDOP_NODE -> {
						logger.debug("found hdop node in waypoint data");
						w.setHdop(getNodeValueAsDouble(currentNode));
					}
					case GPXConstants.VDOP_NODE -> {
						logger.debug("found vdop node in waypoint data");
						w.setVdop(getNodeValueAsDouble(currentNode));
					}
					case GPXConstants.PDOP_NODE -> {
						logger.debug("found pdop node in waypoint data");
						w.setPdop(getNodeValueAsDouble(currentNode));
					}
					case GPXConstants.AGEOFGPSDATA_NODE -> {
						logger.debug("found ageofgpsdata node in waypoint data");
						w.setAgeOfGPSData(getNodeValueAsDouble(currentNode));
					}
					case GPXConstants.DGPSID_NODE -> {
						logger.debug("found dgpsid node in waypoint data");
						w.setDgpsid(getNodeValueAsInteger(currentNode));
					}
					case GPXConstants.EXTENSIONS_NODE -> {
						logger.debug("found extensions node in waypoint data");
						for (IExtensionParser parser : extensionParsers) {
							Object data = parser.parseWaypointExtension(currentNode);
							w.addExtensionData(parser.getId(), data);
						}
					}
				}
			}
		} else {
			logger.debug("no child nodes found in waypoint");
		}
		
		return w;
	}
	
	private Track parseTrack(Node node) {
		if(node == null) {
			logger.error("null node received");
			return null;
		}
		Track trk = new Track();
		NodeList nodes = node.getChildNodes();
		if(nodes != null) {
			for(int idx = 0; idx < nodes.getLength(); idx++) {
				Node currentNode = nodes.item(idx);
				switch (currentNode.getNodeName()) {
					case GPXConstants.NAME_NODE -> {
						logger.debug("node name found");
						trk.setName(getNodeValueAsString(currentNode));
					}
					case GPXConstants.CMT_NODE -> {
						logger.debug("node cmt found");
						trk.setComment(getNodeValueAsString(currentNode));
					}
					case GPXConstants.DESC_NODE -> {
						logger.debug("node desc found");
						trk.setDescription(getNodeValueAsString(currentNode));
					}
					case GPXConstants.SRC_NODE -> {
						logger.debug("node src found");
						trk.setSrc(getNodeValueAsString(currentNode));
					}
					case GPXConstants.LINK_NODE -> logger.debug("node link found");

					//TODO: parse link
					//trk.setLink(getNodeValueAsLink(currentNode));
					case GPXConstants.NUMBER_NODE -> {
						logger.debug("node number found");
						trk.setNumber(getNodeValueAsInteger(currentNode));
					}
					case GPXConstants.TYPE_NODE -> {
						logger.debug("node type found");
						trk.setType(getNodeValueAsString(currentNode));
					}
					case GPXConstants.TRKSEG_NODE -> {
						logger.debug("node trkseg found");
						trk.setTrackPoints(parseTrackSeg(currentNode));
					}
					case GPXConstants.EXTENSIONS_NODE -> {
						Iterator<IExtensionParser> it = extensionParsers.iterator();
						while (it.hasNext()) {
							logger.debug("node extensions found");
							while (it.hasNext()) {
								IExtensionParser parser = it.next();
								Object data = parser.parseTrackExtension(currentNode);
								trk.addExtensionData(parser.getId(), data);
							}
						}
					}
				}
			}
		}
		
		return trk;
	}
	
	
	private Route parseRoute(Node node) {
		if(node == null) {
			logger.error("null node received");
			return null;
		}
		Route rte = new Route();
		NodeList nodes = node.getChildNodes();
		if(nodes != null) {
			for(int idx = 0; idx < nodes.getLength(); idx++) {
				Node currentNode = nodes.item(idx);
				switch (currentNode.getNodeName()) {
					case GPXConstants.NAME_NODE -> {
						logger.debug("node name found");
						rte.setName(getNodeValueAsString(currentNode));
					}
					case GPXConstants.CMT_NODE -> {
						logger.debug("node cmt found");
						rte.setComment(getNodeValueAsString(currentNode));
					}
					case GPXConstants.DESC_NODE -> {
						logger.debug("node desc found");
						rte.setDescription(getNodeValueAsString(currentNode));
					}
					case GPXConstants.SRC_NODE -> {
						logger.debug("node src found");
						rte.setSrc(getNodeValueAsString(currentNode));
					}
					case GPXConstants.LINK_NODE -> logger.debug("node link found");

					//TODO: parse link
					//rte.setLink(getNodeValueAsLink(currentNode));
					case GPXConstants.NUMBER_NODE -> {
						logger.debug("node number found");
						rte.setNumber(getNodeValueAsInteger(currentNode));
					}
					case GPXConstants.TYPE_NODE -> {
						logger.debug("node type found");
						rte.setType(getNodeValueAsString(currentNode));
					}
					case GPXConstants.RTEPT_NODE -> {
						logger.debug("node rtept found");
						Waypoint wp = parseWaypoint(currentNode);
						if (wp != null) {
							rte.addRoutePoint(wp);
						}
					}
					case GPXConstants.EXTENSIONS_NODE -> {
						Iterator<IExtensionParser> it = extensionParsers.iterator();
						while (it.hasNext()) {
							logger.debug("node extensions found");
							while (it.hasNext()) {
								IExtensionParser parser = it.next();
								Object data = parser.parseRouteExtension(currentNode);
								rte.addExtensionData(parser.getId(), data);
							}
						}
					}
				}
			}
		}
		
		return rte;
	}

	private Metadata parseMetadata(Node node) {
		if(node == null) {
			logger.error("null node received");
			return null;
		}
		Metadata metadata = new Metadata();
		NodeList nodes = node.getChildNodes();
		for(int idx = 0; idx < nodes.getLength(); idx++) {
			Node currentNode = nodes.item(idx);
			switch (currentNode.getNodeName()) {
				case GPXConstants.METADATA_NAME -> {
					logger.debug("node name found");
					metadata.setName(getNodeValueAsString(currentNode));
				}
				case GPXConstants.METADATA_DESCRIPTION -> {
					logger.debug("node desc found");
					metadata.setDescription(getNodeValueAsString(currentNode));
				}
				case GPXConstants.METADATA_AUTHOR -> {
					logger.debug("node author found");
					metadata.setAuthor(getNodeValueAsString(currentNode));
				}
				case GPXConstants.METADATA_KEYWORDS -> {
					logger.debug("node keywords found");
					metadata.setKeywords(getNodeValueAsString(currentNode));
				}
				case GPXConstants.METADATA_TIME -> {
					logger.debug("node time found");
					metadata.setTime(getNodeValueAsDate(currentNode));
				}
			}
		}

		return metadata;
	}
	
	private ArrayList<Waypoint> parseTrackSeg(Node node) {
		if(node == null) {
			logger.error("null node received");
			return null;
		}
		ArrayList<Waypoint> trkpts = new ArrayList<Waypoint>();

		NodeList nodes = node.getChildNodes();
		if(nodes != null) {
			for(int idx = 0; idx < nodes.getLength(); idx++) {
				Node currentNode = nodes.item(idx);
				if(GPXConstants.TRKPT_NODE.equals(currentNode.getNodeName())) {
					logger.debug("node name found");
					Waypoint wp = parseWaypoint(currentNode);
					if(wp!=null) {
						trkpts.add(wp);
					}
				} else if(GPXConstants.EXTENSIONS_NODE.equals(currentNode.getNodeName())) {
					logger.debug("node extensions found");
					/*
					Iterator<IExtensionParser> it = extensionParsers.iterator();
					while(it.hasNext()) {
						IExtensionParser parser = it.next();
						Object data = parser.parseWaypointExtension(currentNode);
						//.addExtensionData(parser.getId(), data);
					}
					*/
				}
			}
		}
		return trkpts;
	}
	
	private Double getNodeValueAsDouble(Node node) {
		Double val = null;
		try {
			val = Double.parseDouble(node.getFirstChild().getNodeValue());
		} catch (Exception ex) {
			logger.error("error parsing Double value form node. val=" + node.getNodeValue(), ex);
		}
		return val;
	}
	
	private Date getNodeValueAsDate(Node node) {
		//2012-02-25T09:28:45Z
		Date val = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
			val = sdf.parse(node.getFirstChild().getNodeValue());
		} catch (Exception ex) {
			logger.error("error parsing Date value form node. val=" + node.getNodeName(), ex);
		}
		return val;
	}
	
	private String getNodeValueAsString(Node node) {
		String val = null;
		try {
			val = node.getFirstChild().getNodeValue();
		} catch (Exception ex) {
			logger.error("error getting String value form node. val=" + node.getNodeName(), ex);
		}
		return val;
	}
	
	private FixType getNodeValueAsFixType(Node node) {
		FixType val = null;
		try {
			val = FixType.returnType(node.getFirstChild().getNodeValue());
		} catch (Exception ex) {
			logger.error("error getting FixType value form node. val=" + node.getNodeName(), ex);
		}
		return val;
	}
	
	private Integer getNodeValueAsInteger(Node node) {
		Integer val = null;
		try {
			val = Integer.parseInt(node.getFirstChild().getNodeValue());
		} catch (Exception ex) {
			logger.error("error parsing Integer value form node. val=" + node.getNodeValue(), ex);
		}
		return val;
	}
	
	public void writeGPX(GPX gpx, OutputStream out) throws ParserConfigurationException, TransformerException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		Node gpxNode = doc.createElement(GPXConstants.GPX_NODE);
		addBasicGPXInfoToNode(gpx, gpxNode, doc);
		if(gpx.getWaypoints() != null) {
			for (Waypoint waypoint : gpx.getWaypoints()) {
				addWaypointToGPXNode(waypoint, gpxNode, doc);
			}
			for (Track track : gpx.getTracks()) {
				addTrackToGPXNode(track, gpxNode, doc);
			}
			for (Route route : gpx.getRoutes()) {
				addRouteToGPXNode(route, gpxNode, doc);
			}
		}
		
		doc.appendChild(gpxNode);
		
		// Use a Transformer for output
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(out);
		transformer.transform(source, result); 
	}
	
	private void addWaypointToGPXNode(Waypoint wpt, Node gpxNode, Document doc) {
		addGenericWaypointToGPXNode(GPXConstants.WPT_NODE, wpt, gpxNode, doc);
	}
	
	private void addGenericWaypointToGPXNode(String tagName,Waypoint wpt, Node gpxNode, Document doc) {
		Node wptNode = doc.createElement(tagName);
		NamedNodeMap attrs = wptNode.getAttributes();
		if(wpt.getLatitude() != null) {
			Node latNode = doc.createAttribute(GPXConstants.LAT_ATTR);
			latNode.setNodeValue(wpt.getLatitude().toString());
			attrs.setNamedItem(latNode);
		}
		if(wpt.getLongitude() != null) {
			Node longNode = doc.createAttribute(GPXConstants.LON_ATTR);
			longNode.setNodeValue(wpt.getLongitude().toString());
			attrs.setNamedItem(longNode);
		}
		if(wpt.getElevation() != null) {
			Node node = doc.createElement(GPXConstants.ELE_NODE);
			node.appendChild(doc.createTextNode(wpt.getElevation().toString()));
			wptNode.appendChild(node);
		}
		if(wpt.getTime() != null) {
			Node node = doc.createElement(GPXConstants.TIME_NODE);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'");
			node.appendChild(doc.createTextNode(sdf.format(wpt.getTime())));
			wptNode.appendChild(node);
		}
		if(wpt.getMagneticDeclination() != null) {
			Node node = doc.createElement(GPXConstants.MAGVAR_NODE);
			node.appendChild(doc.createTextNode(wpt.getMagneticDeclination().toString()));
			wptNode.appendChild(node);
		}
		if(wpt.getGeoidHeight() != null) {
			Node node = doc.createElement(GPXConstants.GEOIDHEIGHT_NODE);
			node.appendChild(doc.createTextNode(wpt.getGeoidHeight().toString()));
			wptNode.appendChild(node);
		}
		if(wpt.getName() != null) {
			Node node = doc.createElement(GPXConstants.NAME_NODE);
			node.appendChild(doc.createTextNode(wpt.getName()));
			wptNode.appendChild(node);
		}
		if(wpt.getComment() != null) {
			Node node = doc.createElement(GPXConstants.CMT_NODE);
			node.appendChild(doc.createTextNode(wpt.getComment()));
			wptNode.appendChild(node);
		}
		if(wpt.getDescription() != null) {
			Node node = doc.createElement(GPXConstants.DESC_NODE);
			node.appendChild(doc.createTextNode(wpt.getDescription()));
			wptNode.appendChild(node);
		}
		if(wpt.getSrc() != null) {
			Node node = doc.createElement(GPXConstants.SRC_NODE);
			node.appendChild(doc.createTextNode(wpt.getSrc()));
			wptNode.appendChild(node);
		}
		//TODO: write link node
		if(wpt.getSym() != null) {
			Node node = doc.createElement(GPXConstants.SYM_NODE);
			node.appendChild(doc.createTextNode(wpt.getSym()));
			wptNode.appendChild(node);
		}
		if(wpt.getType() != null) {
			Node node = doc.createElement(GPXConstants.TYPE_NODE);
			node.appendChild(doc.createTextNode(wpt.getType()));
			wptNode.appendChild(node);
		}
		if(wpt.getFix() != null) {
			Node node = doc.createElement(GPXConstants.FIX_NODE);
			node.appendChild(doc.createTextNode(wpt.getFix().toString()));
			wptNode.appendChild(node);
		}
		if(wpt.getSat() != null) {
			Node node = doc.createElement(GPXConstants.SAT_NODE);
			node.appendChild(doc.createTextNode(wpt.getSat().toString()));
			wptNode.appendChild(node);
		}
		if(wpt.getHdop() != null) {
			Node node = doc.createElement(GPXConstants.HDOP_NODE);
			node.appendChild(doc.createTextNode(wpt.getHdop().toString()));
			wptNode.appendChild(node);
		}
		if(wpt.getVdop() != null) {
			Node node = doc.createElement(GPXConstants.VDOP_NODE);
			node.appendChild(doc.createTextNode(wpt.getVdop().toString()));
			wptNode.appendChild(node);
		}
		if(wpt.getPdop() != null) {
			Node node = doc.createElement(GPXConstants.PDOP_NODE);
			node.appendChild(doc.createTextNode(wpt.getPdop().toString()));
			wptNode.appendChild(node);
		}
		if(wpt.getAgeOfGPSData() != null) {
			Node node = doc.createElement(GPXConstants.AGEOFGPSDATA_NODE);
			node.appendChild(doc.createTextNode(wpt.getAgeOfGPSData().toString()));
			wptNode.appendChild(node);
		}
		if(wpt.getDgpsid() != null) {
			Node node = doc.createElement(GPXConstants.DGPSID_NODE);
			node.appendChild(doc.createTextNode(wpt.getDgpsid().toString()));
			wptNode.appendChild(node);
		}
		if(wpt.getExtensionsParsed() > 0) {
			Node node = doc.createElement(GPXConstants.EXTENSIONS_NODE);
			for (IExtensionParser extensionParser : extensionParsers) {
				extensionParser.writeWaypointExtensionData(node, wpt, doc);
			}
			wptNode.appendChild(node);
		}
		gpxNode.appendChild(wptNode);
	}

	private void addTrackToGPXNode(Track trk, Node gpxNode, Document doc) {
		Node trkNode = doc.createElement(GPXConstants.TRK_NODE);
		
		if(trk.getName() != null) {
			Node node = doc.createElement(GPXConstants.NAME_NODE);
			node.appendChild(doc.createTextNode(trk.getName()));
			trkNode.appendChild(node);
		}
		if(trk.getComment() != null) {
			Node node = doc.createElement(GPXConstants.CMT_NODE);
			node.appendChild(doc.createTextNode(trk.getComment()));
			trkNode.appendChild(node);
		}
		if(trk.getDescription() != null) {
			Node node = doc.createElement(GPXConstants.DESC_NODE);
			node.appendChild(doc.createTextNode(trk.getDescription()));
			trkNode.appendChild(node);
		}
		if(trk.getSrc() != null) {
			Node node = doc.createElement(GPXConstants.SRC_NODE);
			node.appendChild(doc.createTextNode(trk.getSrc()));
			trkNode.appendChild(node);
		}
		//TODO: write link
		if(trk.getNumber() != null) {
			Node node = doc.createElement(GPXConstants.NUMBER_NODE);
			node.appendChild(doc.createTextNode(trk.getNumber().toString()));
			trkNode.appendChild(node);
		}
		if(trk.getType() != null) {
			Node node = doc.createElement(GPXConstants.TYPE_NODE);
			node.appendChild(doc.createTextNode(trk.getType()));
			trkNode.appendChild(node);
		}
		if(trk.getExtensionsParsed() > 0) {
			Node node = doc.createElement(GPXConstants.EXTENSIONS_NODE);
			for (IExtensionParser extensionParser : extensionParsers) {
				extensionParser.writeTrackExtensionData(node, trk, doc);
			}
			trkNode.appendChild(node);
		}
		if(trk.getTrackPoints() != null) {
			Node trksegNode = doc.createElement(GPXConstants.TRKSEG_NODE);
			for (Waypoint waypoint : trk.getTrackPoints()) {
				addGenericWaypointToGPXNode(GPXConstants.TRKPT_NODE, waypoint, trksegNode, doc);
			}
			trkNode.appendChild(trksegNode);
		}
		gpxNode.appendChild(trkNode);
	}
	
	private void addRouteToGPXNode(Route rte, Node gpxNode, Document doc) {
		Node trkNode = doc.createElement(GPXConstants.TRK_NODE);
		
		if(rte.getName() != null) {
			Node node = doc.createElement(GPXConstants.NAME_NODE);
			node.appendChild(doc.createTextNode(rte.getName()));
			trkNode.appendChild(node);
		}
		if(rte.getComment() != null) {
			Node node = doc.createElement(GPXConstants.CMT_NODE);
			node.appendChild(doc.createTextNode(rte.getComment()));
			trkNode.appendChild(node);
		}
		if(rte.getDescription() != null) {
			Node node = doc.createElement(GPXConstants.DESC_NODE);
			node.appendChild(doc.createTextNode(rte.getDescription()));
			trkNode.appendChild(node);
		}
		if(rte.getSrc() != null) {
			Node node = doc.createElement(GPXConstants.SRC_NODE);
			node.appendChild(doc.createTextNode(rte.getSrc()));
			trkNode.appendChild(node);
		}
		//TODO: write link
		if(rte.getNumber() != null) {
			Node node = doc.createElement(GPXConstants.NUMBER_NODE);
			node.appendChild(doc.createTextNode(rte.getNumber().toString()));
			trkNode.appendChild(node);
		}
		if(rte.getType() != null) {
			Node node = doc.createElement(GPXConstants.TYPE_NODE);
			node.appendChild(doc.createTextNode(rte.getType()));
			trkNode.appendChild(node);
		}
		if(rte.getExtensionsParsed() > 0) {
			Node node = doc.createElement(GPXConstants.EXTENSIONS_NODE);
			for (IExtensionParser extensionParser : extensionParsers) {
				extensionParser.writeRouteExtensionData(node, rte, doc);
			}
			trkNode.appendChild(node);
		}
		if(rte.getRoutePoints() != null) {
			Iterator<Waypoint> it = rte.getRoutePoints().iterator();
			while(it.hasNext()) {
				addGenericWaypointToGPXNode(GPXConstants.RTEPT_NODE, it.next(), trkNode, doc);
			}
		}
		gpxNode.appendChild(trkNode);
	}
	
	private void addBasicGPXInfoToNode(GPX gpx, Node gpxNode, Document doc) {
		NamedNodeMap attrs = gpxNode.getAttributes();
		if(gpx.getVersion() != null) {
			Node verNode = doc.createAttribute(GPXConstants.VERSION_ATTR);
			verNode.setNodeValue(gpx.getVersion());
			attrs.setNamedItem(verNode);
		}
		if(gpx.getCreator() != null) {
			Node creatorNode = doc.createAttribute(GPXConstants.CREATOR_ATTR);
			creatorNode.setNodeValue(gpx.getCreator());
			attrs.setNamedItem(creatorNode);
		}
		
		if(gpx.getExtensionsParsed() > 0) {
			Node node = doc.createElement(GPXConstants.EXTENSIONS_NODE);
			for (IExtensionParser extensionParser : extensionParsers) {
				extensionParser.writeGPXExtensionData(node, gpx, doc);
			}
			gpxNode.appendChild(node);
		}
	}
}