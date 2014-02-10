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

package org.mulgara.query.rdf;

import java.net.URI;

/**
 * General constants.
 *
 * @created 2004-04-02
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/04/03 10:22:46 $ by $Author: tomadams $
 * @maintenanceAuthor $Author: tomadams $
 * @copyright &copy;2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class Mulgara {
  
  /** The Mulgara prefix for use in place of the namespace */
  public final static String NS_PREFIX = "mulgara";

  /** Mulgara RDF namespace. */
  public final static String NAMESPACE = "http://mulgara.org/mulgara#";
  
  /** Mulgara virtual RDF namespace. */
  public final static URI NS_URI = URI.create("http://mulgara.org/mulgara#");

  /** Mulgara virtual RDF namespace. */
  public final static String VIRTUAL_NS = "http://mulgara.org/virtual/";

  /** A general identifier for an empty graph */
  public final static String NULL_GRAPH = "sys:null";

  /** A general identifier for a default graph */
  public final static String DEFAULT_GRAPH = "sys:default";

  /** An identifier for a prefix graph */
  public final static String PREFIX_GRAPH = "sys:prefix";

  /** An identifier for a nodetype graph */
  public final static String NODE_TYPE_GRAPH = "sys:type";

  /** An identifier for a XSD resolver graph */
  public final static String XSD_GRAPH = "sys:xsd";

  /** The URI for prefix graph types. */
  public final static URI PREFIX_GRAPH_TYPE_URI = URI.create(NAMESPACE + "PrefixGraph");

  /** The URI for URI prefixes. */
  public final static URI PREFIX_URI = URI.create(NAMESPACE + "prefix");

  /** The URI for string prefixes. */
  public final static URI STR_PREFIX_URI = URI.create(NAMESPACE + "stringPrefix");

  /** The URI for binding variables. */
  public final static URI IS_URI = URI.create(NAMESPACE + "is");

}
