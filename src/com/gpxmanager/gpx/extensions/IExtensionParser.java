/*
 * IExtensionParser
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

package com.gpxmanager.gpx.extensions;

import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Route;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.gpx.beans.Waypoint;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This interface defines extension parsers methods.
 * <br>
 * <p>All custom extension parser must implement this interface.</p>
 * <p>Any custom parser must be added to {@link com.gpxmanager.gpx.GPXParser} as an extension parser
 * before parsing a com.gpxmanager.gpx file, or writing a {@link GPX} to a file. This is done by
 * calling addExtensionParser() method of {@link com.gpxmanager.gpx.GPXParser}
 * <p>{@link com.gpxmanager.gpx.GPXParser} parseGPX method calls several methods from the registered
 * extension parsers added at different steps of processing:</p>
 * <ul>
 * <li>parseGPXExtension() for parsing &lt;extensions&gt;  of a &lt;com.gpxmanager.gpx&gt; node</li>
 * <li>parseTrackExtension() for parsing &lt;extensions&gt; of a &lt;trk&gt; node</li>
 * <li>parseRouteExtension() for parsing &lt;extensions&gt; of a &lt;rte&gt; node</li>
 * <li>parseWaypointExtension() for parsing &lt;extensions&gt; of a &lt;wpt&gt; node</li>
 * </ul>
 * <br>
 *
 * <p>{@link com.gpxmanager.gpx.GPXParser} writeGPX method also calls several methods from the registered
 * extensions parsers at different steps of writing data:</p>
 * <ul>
 * <li>writeGPXExtensionData() when writing  the &lt;extensions&gt;  from the {@link GPX}</li>
 * <li>writeTrackExtensionData() when writing  the &lt;extensions&gt;  from the {@link Track}</li>
 * <li>writeRouteExtensionData() when writing  the &lt;extensions&gt;  from the {@link Route}</li>
 * <li>writeWaypointExtensionData() when writing  the &lt;extensions&gt;  from the {@link Waypoint}</li>
 * </ul>
 */
public interface IExtensionParser {

    String getId();

    Object parseWaypointExtension(Node node);

    Object parseTrackExtension(Node node);

    Object parseGPXExtension(Node node);

    Object parseRouteExtension(Node node);

    void writeGPXExtensionData(Node node, GPX wpt, Document doc);

    void writeWaypointExtensionData(Node node, Waypoint wpt, Document doc);

    void writeTrackExtensionData(Node node, Track wpt, Document doc);

    void writeRouteExtensionData(Node node, Route wpt, Document doc);

}
