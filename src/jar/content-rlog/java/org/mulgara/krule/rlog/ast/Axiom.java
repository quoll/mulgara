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

package org.mulgara.krule.rlog.ast;

import java.util.Set;

import org.mulgara.krule.rlog.ParseContext;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.RDFNode;
import org.mulgara.krule.rlog.rdf.URIReference;

/**
 * Represents an axiom statement.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Axiom extends Statement {

  /** The predicate defined by this axiom */
  public final Predicate predicate;

  /**
   * Takes a predicate for a statement and checks that there are not variables in it.
   * @param p A Predicate containing no variables.
   * @param context The context passed in from the parser.
   */
  public Axiom(Predicate p, ParseContext context) {
    super(context);
    predicate = p;
    if (predicate.containsVariables()) throw new IllegalArgumentException("Axioms may not contain variables.");
    predicate.setParent(this);
  }

  /** {@inheritDoc} */
  public void accept(TreeWalker walker) {
    walker.visit(this);
  }

  /** {@inheritDoc} */
  public void print(int indent) {
    System.out.println(sp(indent) + "Axiom {");
    predicate.print(indent + 1);
    System.out.println(sp(indent) + "}");
  }

  /** {@inheritDoc} */
  public boolean equals(Object o) {
    return (o instanceof Axiom) && predicate.equals(((Axiom)o).predicate);
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return 7 * predicate.hashCode();
  }

  /**
   * Gets the references refered to by this axiom.
   * @return An ordered set of URI references.
   * @throws URIParseException The referenced URIs had poor syntax.
   */
  public Set<URIReference> getReferences() throws URIParseException {
    return predicate.getReferences();
  }

  /**
   * Get the subject of this axiom.
   * @return The URIReference subject.
   * @throws URIParseException The node is a malformed URIReference
   */
  public RDFNode getSubject() throws URIParseException {
    return predicate.getSubject();
  }
  
  /**
   * Get the predicate of this axiom.
   * @return The URIReference predicate.
   * @throws URIParseException The node is a malformed URIReference
   */
  public RDFNode getPredicate() throws URIParseException {
    return predicate.getPredicate();
  }

  /**
   * Get the object of this axiom.
   * @return Either a URIReference, or a literal
   * @throws URIParseException The node is a malformed URIReference
   */
  public RDFNode getObject() throws URIParseException {
    return predicate.getObject();
  }

  @Override
  public CanonicalStatement getCanonical() {
    return new CanonicalStatement(predicate.getCanonical());
  }

  /** @see java.lang.Object#toString() */
  public String toString() {
    return predicate.toString() + ".";
  }
}

