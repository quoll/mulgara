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

package org.mulgara.krule.rlog.ast.output;

import org.mulgara.krule.rlog.ParseException;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.URIReference;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.nodepool.NodePoolException;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

/**
 * Writes references to a list of triples.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class ReferenceGenerator extends TripleGenerator {

  /** The collection of references that this class emits. */
  public Collection<URIReference> refs;

  /**
   * Creates a new writer for a collection of references.
   * @param refs The references to be written.
   * @param resolverSession The session used for globalizing data.
   * @throws LocalizeException If localized nodes could not be accessed.
   */
  public ReferenceGenerator(Collection<URIReference> refs, ResolverSession resolverSession) throws LocalizeException {
    super(resolverSession);
    this.refs = refs;
  }


  /**
   * {@inheritDoc} 
   * @throws URIParseException Constructing URIs for the output resulted in an invalid URI.
   * @throws NodePoolException If blank nodes could not be created.
   */
  public List<long[]> emit(List<long[]> triples) throws ParseException, NodePoolException {
    for (URIReference r: refs) emitReference(triples, r);
    return triples;
  }


  /**
   * Create the the triple representation of a reference and append it.
   * @param triples the List to append the triples to.
   * @param ref The reference to emit.
   * @throws NodePoolException If blank nodes could not be created.
   * @throws ParseException If any bad URIs or non-object literals are encountered.
   */
  private List<long[]> emitReference(List<long[]> triples, URIReference ref) throws NodePoolException, ParseException {
    // ref rdf:type kruleUriReference
    // ref rdf:value ref.getURI

    try {
      long rl = toKruleNode(ref.getRdfLabel());
      long val = toLocalNode(ref.getURI());
  
      // rl rdf:type URIReference
      // axiom pos val
      add(triples, rl, rdfType, kruleUriReference);
      add(triples, rl, getRdfValue(), val);
    } catch (URISyntaxException e) {
      throw new ParseException("Invalid URI:" + e.getMessage());
    } catch (LocalizeException e) {
      throw new NodePoolException("Unable to localize a URI", e);
    }
    
    return triples;
  }

}
