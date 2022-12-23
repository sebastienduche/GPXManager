/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2018, Grum Ltd (Romain Gallet)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Geocalc nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.gpxmanager.geocalc;

import java.util.LinkedList;

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

/**
 * Earth related calculations.
 */
public class EarthCalc {

    public static final double EARTH_RADIUS = 6_356_752.314245D; // radius at the poles, meters

    public static class gcd {

        /**
         * Returns the distance between two points at spherical law of cosines.
         *
         * @param standPoint The standpoint
         * @param forePoint  The fore point
         * @return The distance, in meters
         */
        public static double distance(Point standPoint, Point forePoint) {

            var Δλ = toRadians(abs(forePoint.longitude - standPoint.longitude));
            var φ1 = toRadians(standPoint.latitude);
            var φ2 = toRadians(forePoint.latitude);

            //spherical law of cosines
            var sphereCos = (sin(φ1) * sin(φ2)) + (cos(φ1) * cos(φ2) * cos(Δλ));
            var c = acos(max(min(sphereCos, 1d), -1d));

            return EARTH_RADIUS * c;
        }
    }

    public static double calculateDistance(LinkedList<Degree> list) {
        double distance = 0;
        Point start = null;
        for (Degree degree : list) {
            Coordinate lat = Coordinate.fromDegrees(degree.getLatitude());
            Coordinate lng = Coordinate.fromDegrees(degree.getLongitude());
            Point point = Point.at(lat, lng);
            if (start == null) {
                start = point;
                continue;
            }

            distance += EarthCalc.gcd.distance(start, point);
            start = point;
        }
        return distance;
    }
}