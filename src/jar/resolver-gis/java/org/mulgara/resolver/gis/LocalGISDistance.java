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

//Java 2 standard packages
import java.net.URI;

import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.gis.tools.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.util.*;

/**
 * The distance between two GISCoordinates, with a rdf:type and magnitude.
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
public class LocalGISDistance {

  /** The GISDistance namespace */
  public static final String NAMESPACE =
      "http://www.w3.org/2003/01/geo/wgs84_pos#distance/";

  /** The rdf:type of a GISDistance */
  public static final URIReferenceImpl RDF_TYPE;

  /** A dis:magnitude */
  public static final URIReferenceImpl MAGNITUDE;

  /** Instantiate Nodes */
  static {
    try {
      RDF_TYPE = new URIReferenceImpl(new URI(NAMESPACE + "Distance"));
      MAGNITUDE = new URIReferenceImpl(new URI(NAMESPACE + "magnitude"));
    }
    catch (Exception exception) {
      throw new ExceptionInInitializerError("Failed to create Nodes: " +
          exception.getMessage());
    }
  }

  /** Size of the distance */
  private double magnitude = -1;

  /** Node ID that represents this Distance object */
  private long nodeId = -1;

  /**
   * Constructor. Allocates a node for this object and calculates it's magnitude.
   *
   * @param xCoord LocalGISCoordinate
   * @param yCoord LocalGISCoordinate
   * @param resolverSession ResolverSession
   */
  public LocalGISDistance(LocalGISCoordinate xCoord, LocalGISCoordinate yCoord,
      ResolverSession resolverSession, DistanceCalculator calculator) throws TuplesException {

    //validate
    if (xCoord == null) {
      throw new IllegalArgumentException("'xCoord' parameter is null.");
    }
    if (yCoord == null) {
      throw new IllegalArgumentException("'yCoord' parameter is null.");
    }
    if (resolverSession == null) {
      throw new IllegalArgumentException("ResolverSession is null.");
    }
    if (calculator == null) {
      throw new IllegalArgumentException("DistanceCalculator is null.");
    }

    //obtain a node id
    try {
      String uid = UIDGenerator.generateUID();
      URIReferenceImpl node = new URIReferenceImpl(new URI(NAMESPACE + uid));
      nodeId = resolverSession.localizePersistent(node);
    }
    catch (Exception exception) {
      throw new TuplesException("Could not allocate local node ID.", exception);
    }

    //calculate size
    magnitude = calculator.distanceBetween(xCoord, yCoord);
  }

  /**
   * Returns the size of this distance in metres.
   * @return double
   */
  public double getMagnitude() {
    return magnitude;
  }

  /**
   * Returns the Local ID representing this Distance object.
   * @return long
   */
  public long getNodeId() {

    /** @todo add throws clause */
    if (nodeId == -1) {
      throw new RuntimeException("LocalGISDistance not initialized properly.");
    }

    return nodeId;
  }
}
