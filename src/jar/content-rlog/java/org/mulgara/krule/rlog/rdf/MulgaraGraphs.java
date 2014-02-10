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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mulgara.krule.rlog.rdf.RDF.MULGARA_PREFIX;
import static org.mulgara.krule.rlog.rdf.RDF.RDFS_PREFIX;

/**
 * Contains information about specific Mulgara graphs that can be used in rlog.
 *
 * @created May 3, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="http://www.opensource.org/licenses/apache2.0.php">Apache License, Version 2.0</a>
 */
public class MulgaraGraphs {

  public static final URIReference MULGARA_PRE = URIReference.contextFreeCreate(MULGARA_PREFIX, "prefix");

  public static final URIReference MULGARA_URI_REF = URIReference.contextFreeCreate(MULGARA_PREFIX, "UriReference");

  public static final URIReference MULGARA_LITERAL = URIReference.contextFreeCreate(RDFS_PREFIX, "Literal");

  public static final URIReference MULGARA_PRE_GRAPH = URIReference.create(MULGARA_PREFIX, "prefixGraph", "sys:prefix");

  public static final URIReference MULGARA_TYPE_GRAPH = URIReference.create(MULGARA_PREFIX, "typeGraph", "sys:type");

  /** Maps special predicates to graphs that can handle them. */
  private static Map<URI,URIReference> specialPredicateGraphs = new HashMap<URI,URIReference>();

  /** Maps special types to graphs that can handle them. */
  private static Map<URI,URIReference> specialTypeGraphs = new HashMap<URI,URIReference>();

  /** Holds a copy of all the special URIs. */
  private static List<URIReference> specialUriRefs = new ArrayList<URIReference>();
  
  static {
    specialPredicateGraphs.put(MULGARA_PRE.getURI(), MULGARA_PRE_GRAPH);
    specialTypeGraphs.put(MULGARA_URI_REF.getURI(), MULGARA_TYPE_GRAPH);
    specialTypeGraphs.put(MULGARA_LITERAL.getURI(), MULGARA_TYPE_GRAPH);
    specialUriRefs.add(MULGARA_PRE);
    specialUriRefs.add(MULGARA_URI_REF);
    specialUriRefs.add(MULGARA_LITERAL);
    specialUriRefs.add(MULGARA_PRE_GRAPH);
    specialUriRefs.add(MULGARA_TYPE_GRAPH);
  }
  
  public static URIReference getPredicateGraph(URI predicate) {
    return specialPredicateGraphs.get(predicate);
  }
  
  public static URIReference getTypeGraph(URI type) {
    return specialTypeGraphs.get(type);
  }

  public static List<URIReference> getSpecialUriRefs() {
    return Collections.unmodifiableList(specialUriRefs);
  }
}
