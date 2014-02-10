/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.gis.tools;

// Log4J
import org.apache.log4j.Logger;

/**
 * Estimates the distance between two co-ordinates on the surface of a Sphere
 * with a specific radius.
 *
 * @created 2004-11-17
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.6 $
 *
 * @modified $Date: 2005/01/05 04:58:29 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SphericalDistanceEstimator implements DistanceCalculator {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(SphericalDistanceEstimator.class.getName());

  /** Size of the sphere (in metres). */
  private int radius = 0;


  /**
   * Constructor. Sets the size of the Sphere.
   *
   * @param radius int
   */
  public SphericalDistanceEstimator(int radius) {
    super();
    //validate
    if (radius <= 0) {
      throw new IllegalArgumentException("Sphere radius cannot be negative " +
          "or zero.");
    }
    this.radius = radius;
  }

  /**
   * Returns the "Great Circle" distance (in metres) between the two points.
   *
   * <p>
   * algorithm derived from: http://www.ga.gov.au/nmd/geodesy/datums/distance.jsp
   *
   * D = RADIUS * ARCOS ( SIN(L1) * SIN(L2) + COS(L1) * COS(L2) * COS(DG))
   * L1 = latitude at the first point (radians)
   * L2 = latitude at the second point (radians)
   * G1 = longitude at the first point (radians)
   * G2 = longitude at the second point (radians)
   * DG = longitude of the second point minus longitude of the first point (radians)
   * DL = latitude of the second point minus latitude of the first point (radians)
   * D = computed distance (m)
   *
   * @param pointA GISCoordinate
   * @param pointB GISCoordinate
   * @return double
   */
  public double distanceBetween(GISCoordinate pointA, GISCoordinate pointB) {

    //validate
    if (pointA == null) {
      throw new IllegalArgumentException("'pointA' parameter is null.");
    }
    if (pointB == null) {
      throw new IllegalArgumentException("'pointB' parameter is null.");
    }

    //short circuit if they are equal (same place/coordinate)
    if (pointA.equals(pointB)) {
      return 0.0;
    }

    //get the latitude and longitude of each point in radians
    double latA = pointA.getLatitide().asRadians();
    double longA = pointA.getLongitude().asRadians();
    double latB = pointB.getLatitide().asRadians();
    double longB = pointB.getLongitude().asRadians();
    return radius * (Math.acos(Math.sin(latA) * Math.sin(latB) +
        Math.cos(latA) * Math.cos(latB) * Math.cos(longB - longA)));
  }

  /**
   * Returns the "Great Circle" distance (in metres) between the two points.
   *
   * @param pointA GISCoordinate
   * @param pointB GISCoordinate
   * @return double
   */
//  public double distanceBetween(GISCoordinate pointA, GISCoordinate pointB) {
//
//    //validate
//    if (pointA == null) {
//      throw new IllegalArgumentException("'pointA' parameter is null.");
//    }
//    if (pointB == null) {
//      throw new IllegalArgumentException("'pointB' parameter is null.");
//    }
//
//    //short circuit if they are equal (same place/coordinate)
//    if (pointA.equals(pointB)) {
//      return 0.0;
//    }
//
//    //get the latitude and longitude of each point in radians
//    double latA = pointA.getLatitide().asRadians();
//    double longA = pointA.getLongitude().asRadians();
//    double latB = pointB.getLatitide().asRadians();
//    double longB = pointB.getLongitude().asRadians();
//
//    //convert co-ordinates to 3D vectors (x,y,z)
//    double xA = Math.cos(latA) * Math.cos(longA);
//    double yA = Math.cos(latA) * Math.sin(longA);
//    double zA = Math.sin(latA);
//
//    double xB = Math.cos(latB) * Math.cos(longB);
//    double yB = Math.cos(latB) * Math.sin(longB);
//    double zB = Math.sin(latB);
//
//    //find their dot product
//    double x = xA * xB;
//    double y = yA * yB;
//    double z = zA * zB;
//
//    //determine the great circle distance of their dot-product
//    return (Math.acos(x+y+z) * radius);
//  }
}
