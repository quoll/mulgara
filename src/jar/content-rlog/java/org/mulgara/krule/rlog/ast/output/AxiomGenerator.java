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
import org.mulgara.krule.rlog.ast.Axiom;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.Literal;
import org.mulgara.krule.rlog.rdf.RDFNode;
import org.mulgara.krule.rlog.rdf.URIReference;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.nodepool.NodePoolException;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import static org.mulgara.query.rdf.Krule.*;

/**
 * Writes variables to a list of triples.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class AxiomGenerator extends TripleGenerator {

  /** The collection of axioms that this class emits. */
  public Collection<Axiom> axioms;

  /** The node for the krule:Axiom type */
  private final long kruleAxiom;

  /** The node for the krule:subject predicate */
  private final long kruleSubject;

  /** The node for the krule:predicate predicate */
  private final long krulePredicate;

  /** The node for the krule:object predicate */
  private final long kruleObject;


  /**
   * Creates a new writer for a collection of axioms.
   * @param axioms The axioms to be written.
   * @param resolverSession the resolver-session
   * @throws LocalizeException If localized nodes could not be accessed.
   */
  public AxiomGenerator(Collection<Axiom> axioms, ResolverSession resolverSession) throws LocalizeException {
    super(resolverSession);
    this.axioms = axioms;
    kruleAxiom = resolverSession.localize(AXIOM);
    kruleSubject = resolverSession.localize(AXIOM_SUBJECT);
    krulePredicate = resolverSession.localize(AXIOM_PREDICATE);
    kruleObject = resolverSession.localize(AXIOM_OBJECT);
  }


  /**
   * {@inheritDoc} 
   * @throws URIParseException Constructing URIs for the output resulted in an invalid URI.
   * @throws NodePoolException If blank nodes could not be created.
   */
  public List<long[]> emit(List<long[]> triples) throws ParseException, NodePoolException {
    for (Axiom a: axioms) emitAxiom(triples, a);
    return triples;
  }


  /**
   * Create the the triple representation of an axiom and append it.
   * @param triples the List to append the triples to
   * @param a The axiom to emit.
   * @throws NodePoolException If blank nodes could not be created.
   * @throws ParseException If any bad URIs or non-object literals are encountered.
   */
  private List<long[]> emitAxiom(List<long[]> triples, Axiom a) throws NodePoolException, ParseException {
    try {
      long axiom = newBlankNode();

      add(triples, axiom, rdfType, kruleAxiom);

      addLocalTriples(triples, a.getSubject(), kruleSubject, axiom);
      addLocalTriples(triples, a.getPredicate(), krulePredicate, axiom);
      addLocalTriples(triples, a.getObject(), kruleObject, axiom);
    } catch (LocalizeException e) {
      throw new NodePoolException("Unable to localize data", e);
    } catch (URISyntaxException e) {
      throw new ParseException("Bad syntax in URI:" + e.getMessage());
    } catch (URIParseException e) {
      throw new ParseException("Bad syntax in URI:" + e.getMessage());
    }
    
    return triples;
  }


  protected void addLocalTriples(List<long[]> triples, RDFNode n, long pos, long axiom)
          throws ParseException, NodePoolException, LocalizeException, URISyntaxException {
    if (n.isReference()) {
      long rl = toKruleNode(((URIReference)n).getRdfLabel());

      // rl rdf:type URIReference
      // axiom pos rl
      add(triples, rl, rdfType, kruleUriReference);
      add(triples, axiom, pos, rl);

    } else if (n.isVariable()) {

      long blank = newBlankNode();
      add(triples, axiom, pos, blank);

    } else {
      Literal l = (Literal)n;
      if (pos != kruleObject) throw new ParseException("Literal in unexpected position: " + l.getLexical());
      long ll = resolverSession.localize(new LiteralImpl(l.getLexical()));
      long blank = newBlankNode();

      // axiom pos blank
      // blank rdf:type krule:Literal
      // blank rdf:value "....."
      add(triples, axiom, pos, blank);
      add(triples, blank, rdfType, getKruleLiteral());
      add(triples, blank, getRdfValue(), ll);
    }
  }

}
