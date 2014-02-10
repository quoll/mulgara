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

import static org.mulgara.query.rdf.Krule.KRULE;
import static org.mulgara.query.rdf.Krule.LITERAL;
import static org.mulgara.query.rdf.Krule.URI_REF;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.jrdf.vocabulary.RDF;
import org.mulgara.krule.rlog.ParseException;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.nodepool.NodePoolException;

/**
 * Outputs a set of rules as triples.
 *
 * @created Feb 25, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="http://www.opensource.org/licenses/apache2.0.php">Apache License, Version 2.0</a>
 */
public abstract class TripleGenerator {

  /** The domain for KRULE, sans the fragment tag */
  private static final String KRULE_SHORT = KRULE.substring(0, KRULE.length() - 1);

  /** The session for localizing strings and URIs */
  ResolverSession resolverSession;

  /** The node for rdf:type */
  protected final long rdfType;

  /** The node for kruleURIReference. This might not be used. */
  protected final long kruleUriReference;

  /** The node for rdf:value. This might not be used. */
  private Long rdfValue;

  /** The node for krule:Literal. This might not be used. */
  private Long kruleLiteral;

  /**
   * Creates a triple generator and initializes it with shared nodes.
   * @param resolverSession The session used for identifying nodes.
   * @throws LocalizeException There was an error localizing nodes.
   */
  protected TripleGenerator(ResolverSession resolverSession) throws LocalizeException {
    this.resolverSession = resolverSession;
    rdfType = resolverSession.lookup(new URIReferenceImpl(RDF.TYPE));
    kruleUriReference = resolverSession.localize(URI_REF);
  }


  /**
   * Fill a list with the triples to be generated.
   * @param triples The list to fill.
   * @return The filled list.
   * @throws NodePoolException If blank nodes could not be created.
   */
  abstract List<long[]> emit(List<long[]> triples) throws LocalizeException, ParseException, NodePoolException;


  /**
   * Convenience for adding a new triple to the list
   * @param triples The list to add to.
   * @param s The subject for the triple
   * @param p The predicate for the triple
   * @param o The object for the triple
   */
  static final protected void add(List<long[]> triples, long s, long p, long o) {
    triples.add(new long[] { s, p, o });
  }


  /**
   * Creates a localized URIReference for a name in the KRULE domain
   * @param label the unqualified name
   * @return A localized gNode for the URI
   * @throws LocalizeException If the URI could not be localized
   * @throws URISyntaxException If the URI was malformed
   */
  protected long toKruleNode(String label) throws LocalizeException, URISyntaxException {
    String fullname;
    fullname = (label.charAt(0) == '#') ? KRULE_SHORT + label : KRULE + label;
    return resolverSession.localize(new URIReferenceImpl(new URI(fullname)));
  }


  /**
   * Creates a localized URIReference for a general URI
   * @param u The node to localize.
   * @return A localized gNode for the URI
   * @throws LocalizeException If the URI could not be localized
   */
  protected long toLocalNode(URI u) throws LocalizeException, URISyntaxException {
    return resolverSession.localize(new URIReferenceImpl(u));
  }


  /**
   * Creates a new blank node to use in this session.
   * @return The ID for the new blank node.
   * @throws LocalizeException If the blank node could not be created.
   */
  protected long newBlankNode() throws LocalizeException {
    return resolverSession.localize(new BlankNodeImpl());
  }


  /**
   * Retrive a cached value for rdf:Value
   * @return The local node for rdf:Value
   * @throws LocalizeException If a required lookup was unsuccessful
   */
  protected long getRdfValue() throws LocalizeException {
    if (rdfValue == null) {
      rdfValue = resolverSession.localize(new URIReferenceImpl(RDF.VALUE));
    }
    return rdfValue;
  }


  /**
   * Retrive a cached value for krule:Literal
   * @return The local node for krule:Literal
   * @throws LocalizeException If a required lookup was unsuccessful
   */
  protected long getKruleLiteral() throws LocalizeException {
    if (kruleLiteral == null) kruleLiteral = resolverSession.localize(LITERAL);
    return kruleLiteral;
  }

}
