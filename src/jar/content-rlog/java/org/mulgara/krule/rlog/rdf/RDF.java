/*
 * Copyright 2008 Fedora Commons, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.krule.rlog.rdf;

/**
 * Contains the RDF namespace details
 *
 * @created May 2, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="http://www.opensource.org/licenses/apache2.0.php">Apache License, Version 2.0</a>
 */
public class RDF {

  /** The namespace for RDF */
  public static final String NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  public static final String PREFIX = "rdf";

  public static final URIReference TYPE = URIReference.contextFreeCreate(PREFIX, "type");

  public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";

  public static final String XSD_PREFIX = "xsd";

  public static final String OWL_NS = "http://www.w3.org/2002/07/owl#";

  public static final String OWL_PREFIX = "owl";

  public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";

  public static final String RDFS_PREFIX = "rdfs";

  public static final String MULGARA_NS = "http://mulgara.org/mulgara#";

  public static final String MULGARA_PREFIX = "mulgara";

  public static final String KRULE_NS = "http://mulgara.org/owl/krule/#";

  public static final String KRULE_PREFIX = "krule";

  public static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";

  public static final String FOAF_PREFIX = "foaf";

  public static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";

  public static final String SKOS_PREFIX = "skos";

  public static final String DC_NS = "http://purl.org/dc/elements/1.1/";

  public static final String DC_PREFIX = "dc";

  // Specific URIs used by the standard parser

  public static final URIReference XSD_LONG = URIReference.contextFreeCreate(XSD_PREFIX, "long");

}
