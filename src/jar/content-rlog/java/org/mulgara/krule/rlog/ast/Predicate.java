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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.mulgara.krule.rlog.ParseContext;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.RDFNode;
import org.mulgara.krule.rlog.rdf.URIReference;
import org.mulgara.krule.rlog.rdf.Var;

import static org.mulgara.util.ObjectUtil.eq;

/**
 * A predicate in a statement appearing in the AST.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public abstract class Predicate extends Node {

  /** The parent node in the AST.  Always a statement. */
  private Statement parent;

  /** The predicate value of this predicate. */
  protected RDFNode predicate = null;

  /** The subject of this predicate. */
  protected RDFNode subject = null;

  /** The object of this predicate. */
  protected RDFNode object = null;

  /** The graph of this predicate. */
  protected URIReference graphAnnotation = null;

  /** The exception generated when trying to parse the predicate. Hopwefully null. */
  protected URIParseException delayedPredicateException = null;

  /** The exception generated when trying to parse the predicate. Hopwefully null. */
  protected URIParseException delayedSubjectException = null;

  /** The exception generated when trying to parse the predicate. Hopwefully null. */
  protected URIParseException delayedObjectException = null;

  /**
   * Create this predicate instance, with the context it requires.
   * @param context The context passed in from the parser.
   */
  public Predicate(ParseContext context) {
    super(context);
  }

  /**
   * Sets the parent AST node.
   * @param parent The statement which contains this predicate.
   */
  public void setParent(Statement parent) { this.parent = parent; }

  /**
   * Gets the parent AST node.  Always returns a statement.
   * @return The Statement containing this node.
   */
  public Statement getParent() { return parent; }

  /**
   * Check if any variables are referenced by this predicate.  More efficient implementation
   * of !getVariables().isEmpty()
   * @return <code>true</code> if there are any variables in this predicate.
   */
  public boolean containsVariables() { return !getVariables().isEmpty(); }

  /**
   * Get all variables referred to in this predicate.
   * @return a collection with all the variable, or zero veriables if none are referenced.
   */
  public abstract Collection<Var> getVariables();

  /**
   * Get the subject of this predicate.
   * @return Either a URIReference, or a variable
   */
  public RDFNode getSubject() throws URIParseException {
    if (subject != null) return subject;
    throw delayedSubjectException;
  }
  
  /**
   * Get the predicate of this predicate.
   * @return Either a URIReference, or a variable
   */
  public RDFNode getPredicate() throws URIParseException {
    if (predicate != null) return predicate;
    throw delayedPredicateException;
  }

  /**
   * Get the object of this predicate.
   * @return Either a URIReference, a variable, or a literal
   */
  public RDFNode getObject() throws URIParseException {
    if (object != null) return object;
    throw delayedObjectException;
  }

  /**
   * Indicates that this predicate has an annotation on how it gets resolved.
   * @return <code>true</code> if a resolution annotation exists.
   */
  public boolean hasGraphAnnotation() {
    return graphAnnotation != null;
  }

  /**
   * Retrieves the graph annotation for this predicate.
   * @return The graph for resolving this predicate.
   */
  public URIReference getGraphAnnotation() throws URIParseException {
    return graphAnnotation;
  }

  /**
   * Gets the elements from this relationship that are references.
   * @return An ordered set containing the references.
   * @throws URIParseException The references had invalid syntax.
   */
  public Set<URIReference> getReferences() throws URIParseException {
    Set<URIReference> refs = new LinkedHashSet<URIReference>();
    RDFNode n = getSubject();
    if (n.isReference()) refs.add((URIReference)n);
    n = getPredicate();
    if (n.isReference()) refs.add((URIReference)n);
    n = getObject();
    if (n.isReference()) refs.add((URIReference)n);
    return refs;
  }

  /**
   * Tests if this predicate possibly matches a pattern of subject/predicate/object
   * @param s The subject from the pattern to test for match.
   * @param p The predicate from the pattern to test for match.
   * @param o The object from the pattern to test for match.
   * @return <code>false</code> if this predicate cannote matches the subject/predicate/object,
   *         <code>true</code> otherwise.
   */
  public boolean matches(RDFNode s, RDFNode p, RDFNode o) throws URIParseException {
    RDFNode localS = getSubject();
    RDFNode localP = getPredicate();
    RDFNode localO = getObject();
    // can only fail if both are not variable and not equal
    if (!s.isVariable() && !localS.isVariable() && !localS.equals(s)) return false;
    if (!p.isVariable() && !localP.isVariable() && !localP.equals(p)) return false;
    if (!o.isVariable() && !localO.isVariable() && !localO.equals(o)) return false;
    // this rule has the potential to match the pattern
    return true;
  }

  /** {@inheritDoc} */
  public boolean equals(Object o) {
    if (!(o instanceof Predicate)) return false;
    Predicate p = (Predicate)o;
    return eq(predicate, p.predicate) && eq(subject, p.subject) && eq(object, p.object) && eq(graphAnnotation, p.graphAnnotation);
  }

  /**
   * Returns a canonical version of this predicate.
   * @return A new canonical predicate, with canonicalized elements.
   */
  abstract CanonicalPredicate getCanonical();

  /**
   * Converts a Variable to a Var. Accepts multiple types.
   * @param v The Variable to convert. This must be a Variable.
   * @return a new Var that is equivalent to the v.
   * @throws ClassCastException If something other than a Variable is passed in.
   */
  protected Var toVar(Object v) {
    return new Var(((Variable)v).name);
  }

}

