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

import java.util.HashSet;
import java.util.Collection;

import org.mulgara.krule.rlog.ParseContext;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.MulgaraGraphs;
import org.mulgara.krule.rlog.rdf.URIReference;
import org.mulgara.krule.rlog.rdf.Var;

/**
 * Represents a binary predicate in the AST.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class BPredicate extends Predicate {

  /** The predicate label. */
  public final BPredicateLabel label;

  /** The first parameter of the predicate. */
  public final PredicateParam left;

  /** The second parameter of the predicate. */
  public final PredicateParam right;

  /**
   * Creates a binary predicate, connecting two resources.
   * @param label The predicate name.
   * @param left The first parameter of the predicate.
   * @param right The second parameter of the predicate.
   * @param context The context passed in from the parser.
   */
  public BPredicate(BPredicateLabel label, PredicateParam left, PredicateParam right, ParseContext context) {
    super(context);
    this.label = label;
    this.left = left;
    this.right = right;
    // get the referenced nodes, delaying any exceptions until later.
    try {
      subject = this.left.getRDFNode();
    } catch (URIParseException e) {
      delayedSubjectException = e;
    }
    try {
      predicate = this.label.getRDFNode();
    } catch (URIParseException e) {
      delayedPredicateException = e;
    }
    try {
      object = this.right.getRDFNode();
    } catch (URIParseException e) {
      delayedObjectException = e;
    }
    annotateGraph();
  }

  // inheritdoc
  public void accept(TreeWalker walker) {
    walker.visit(this);
  }

  // inheritdoc
  public boolean containsVariables() {
    return label instanceof Variable || left instanceof Variable || right instanceof Variable;
  }

  // inheritdoc
  public Collection<Var> getVariables() {
    Collection<Var> vars = new HashSet<Var>();
    if (label instanceof Variable) vars.add(toVar(label));
    if (left instanceof Variable) vars.add(toVar(left));
    if (right instanceof Variable) vars.add(toVar(right));
    return vars;
  }

  // inheritdoc
  public void print(int indent) {
    System.out.println(sp(indent) + "BPredicate {");
    System.out.println(sp(indent + 1) + "label:");
    label.print(indent + 2);
    System.out.println(sp(indent + 1) + "left:");
    left.print(indent + 2);
    System.out.println(sp(indent + 1) + "right:");
    right.print(indent + 2);
    System.out.println(sp(indent) + "}");
  }

  //inheritdoc
  public String toString() {
    return label.toString() + "(" + left + "," + right + ")";
  }

  //inheritdoc
  public boolean equals(Object o) {
    if (o instanceof BPredicate) {
      BPredicate p = (BPredicate)o;
      return label.equals(p.label) && left.equals(p.left) && right.equals(p.right);
    } else return false;
  }

  // inheritdoc
  public int hashCode() {
    return label.hashCode() * 961 + left.hashCode() * 31 + right.hashCode();
  }

  /**
   * Search for the predicate/object in the special graphs, and annotate this predicate
   * to any detected graphs.
   */
  private void annotateGraph() {
    if (predicate != null && predicate.isReference()) {
      graphAnnotation = MulgaraGraphs.getPredicateGraph(((URIReference)predicate).getURI());
    }
  }

  /**
   * Creates a canonical form for this predicate.
   * @see org.mulgara.krule.rlog.ast.Predicate#getCanonical()
   */
  @Override
  CanonicalPredicate getCanonical() {
    return new CanonicalPredicate(left, (PredicateParam)label, right);
  }
}

