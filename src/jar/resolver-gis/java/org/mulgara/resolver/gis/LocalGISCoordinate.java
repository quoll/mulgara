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

package org.mulgara.resolver.gis;

// Local packages
import org.mulgara.query.*;
import org.mulgara.resolver.gis.tools.*;
import org.mulgara.resolver.spi.*;
import org.jrdf.graph.*;

/**
 * Describes a coordinate by local node ID's in the NodePool.
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
public class LocalGISCoordinate implements GISCoordinate {

  /** Used to globalize the nodes. */
  private ResolverSession resolverSession = null;

  /** The geo:Point node that represents this coordinate */
  private long point = -1;

  /** The geo:lat node that represents this coordinate's Latitude */
  private long lat = -1;

  /** The geo:lon node that represents this coordinate's Longitude */
  private long lon = -1;

  /** Global longitude */
  private Longitude longitude = null;

  /** Global latitude */
  private Latitude latitude = null;

  /**
   * Constructor.
   *
   * @param point long
   * @param lat long
   * @param lon long
   * @param resolverSession ResolverSession
   * @throws ResolverException
   */
  public LocalGISCoordinate(long point, long lat, long lon,
      ResolverSession resolverSession) throws TuplesException {

    //validate
    if (resolverSession == null) {
      throw new IllegalArgumentException("ResolverSession is null.");
    }

    this.resolverSession = resolverSession;
    this.point = point;
    this.lat = lat;
    this.lon = lon;
  }

  /**
   * Returns the Co-ordinate's Longitude.
   * @return Longitude
   */
  public Longitude getLongitude() {

    try {

      //lazily evaluate
      if (longitude == null) {
        longitude = new LongitudeImpl();
        Node longNode = resolverSession.globalize(lon);

        //node should be a Literal
        if (!(longNode instanceof Literal)) {
          throw new RuntimeException("Failed to determine Longitude.");
        }
        //convert to a double
        String longString = ((Literal) longNode).getLexicalForm();
        double angle = Double.parseDouble(longString);
        longitude.setDegrees(angle);
      }
      return longitude;
    }
    catch (GlobalizeException globalException) {

      // FIXME: add throws clause to GISCoordinate interface.
      throw new RuntimeException("Failed to determine Longitude.", globalException);
    }
  }

  /**
   * Returns the Co-ordinate's Latitude.
   * @return Latitude
   */
  public Latitude getLatitide() {

    try {
      //lazily evaluate
      if (latitude == null) {
        latitude = new LatitudeImpl();
        Node latNode = resolverSession.globalize(lat);

        //node should be a Literal
        if (!(latNode instanceof Literal)) {
          throw new RuntimeException("Failed to determine Latitude.");
        }
        //convert to a double
        String latString = ((Literal) latNode).getLexicalForm();
        double angle = Double.parseDouble(latString);
        latitude.setDegrees(angle);
      }
      return latitude;
    }
    catch (GlobalizeException globalException) {

      // FIXME: add throws clause to GISCoordinate interface.
      throw new RuntimeException("Failed to determine Latitude.", globalException);
    }
  }

  /**
   * Returns the Node ID for the geo:Point representing this coordinate.
   *
   * @return long
   */
  public long getNodeId() {
    /** @todo add throws clause */
    if (point == -1) {
      throw new RuntimeException("LocalGISCoordinate not initialized properly.");
    }
    return point;
  }

  /**
   * Compares this Coordinate to another.
   *
   * @param obj Object
   * @return boolean
   */
  public boolean equals(Object obj) {

    boolean equal = false;
    equal = (obj != null) && (obj instanceof LocalGISCoordinate);

    if (equal) {
      LocalGISCoordinate coord = (LocalGISCoordinate) obj;
      equal = (coord == this)
          || (coord.getNodeId() == getNodeId())
          || (getLatitide().equals(coord.getLatitide())
          && getLongitude().equals(coord.getLongitude()));
    }

    return equal;
  }
}
