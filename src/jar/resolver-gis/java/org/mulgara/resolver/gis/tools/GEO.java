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

//Java 2 standard packages
import java.net.URI;

import org.mulgara.query.rdf.*;


/**
 * Vocabulary for describing GIS co-ordinates in RDF.
 *
 * <p>RDF Vocabulary for describing points:
 * <code>
 * &lt;rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 *       xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#">
 *   &lt;geo:Point&gt;
 *     &lt;geo:lat&gt;54.5722&lt;/geo:lat&gt;
 *     &lt;geo:long&gt;0.014024&lt;/geo:long&gt;
 *   &lt;/geo:Point&gt;
 * &lt;/rdf:RDF&gt;
 *
 * (eg. &lt;geo:Point geo:lat="54.5722" geo:long="0.014024"/&gt; )
 * </code>
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
public abstract class GEO {

  /** The Geo namespace */
  public static final String NAMESPACE = "http://www.w3.org/2003/01/geo/wgs84_pos#";

  /** A geo:Point */
  public static final URIReferenceImpl POINT;

  /** A geo:lat */
  public static final URIReferenceImpl LAT;

  /** A geo:long */
  public static final URIReferenceImpl LONG;

  /** A geo:alt */
  public static final URIReferenceImpl ALT;

  /** Instantiate Nodes */
  static {
    try {
      POINT = new URIReferenceImpl(new URI(NAMESPACE + "Point"));
      LAT = new URIReferenceImpl(new URI(NAMESPACE + "lat"));
      LONG = new URIReferenceImpl(new URI(NAMESPACE + "long"));
      ALT = new URIReferenceImpl(new URI(NAMESPACE + "alt"));
    }
    catch (Exception exception) {
      throw new ExceptionInInitializerError("Failed to create Nodes: " +
          exception.getMessage());
    }
  }
}
