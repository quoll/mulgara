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
import java.util.HashSet;

import org.mulgara.krule.rlog.ParseContext;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.MulgaraGraphs;
import org.mulgara.krule.rlog.rdf.RDF;
import org.mulgara.krule.rlog.rdf.URIReference;
import org.mulgara.krule.rlog.rdf.Var;

/**
 * This class represents a unary predicate in the AST.  These are used as type specifiers
 * for their parameter.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class TypeStatement extends Predicate {

  /** The type of the object. */
  public final TypeLabel typeLabel;

  /** The object with the declared type/ */
  public final PredicateParam param;

  /**
   * Creates a unary predicate indicating a type statement.
   * @param typeLabel The declared type.
   * @param param The object declared to be of the specified type.
   * @param context The context passed in from the parser.
   */
  public TypeStatement(TypeLabel typeLabel, PredicateParam param, ParseContext context) {
    super(context);
    this.typeLabel = typeLabel;
    this.param = param;
    // get the referenced nodes, delaying any exceptions until later.
    try {
      subject = this.param.getRDFNode();
    } catch (URIParseException e) {
      delayedSubjectException = e;
    }
    predicate = RDF.TYPE;
    try {
      object = this.typeLabel.getRDFNode();
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
    return typeLabel instanceof Variable || param instanceof Variable;
  }

  // inheritdoc
  public Collection<Var> getVariables() {
    Collection<Var> vars = new HashSet<Var>();
    if (typeLabel instanceof Variable) vars.add(toVar(typeLabel));
    if (param instanceof Variable) vars.add(toVar(param));
    return vars;
  }

  // inheritdoc
  public void print(int indent) {
    System.out.println(sp(indent) + "TypeStatement {");
    System.out.println(sp(indent + 1) + "type:");
    typeLabel.print(indent + 2);
    System.out.println(sp(indent + 1) + "param:");
    param.print(indent + 2);
    System.out.println(sp(indent) + "}");
  }

  // inheritdoc
  public String toString() {
    return typeLabel.toString() + "(" + param + ")";
  }

  //inheritdoc
  public boolean equals(Object o) {
    if (o instanceof TypeStatement) {
      TypeStatement ts = (TypeStatement)o;
      return typeLabel.equals(ts.typeLabel) && param.equals(ts.param);
    } else return false;
  }

  // inheritdoc
  public int hashCode() {
    return typeLabel.hashCode() * 37 + param.hashCode();
  }


  /**
   * Creates a canonical form for this predicate.
   * @see org.mulgara.krule.rlog.ast.Predicate#getCanonical()
   */
  @Override
  CanonicalPredicate getCanonical() {
    return new CanonicalPredicate((PredicateParam)typeLabel, param);
  }


  /**
   * Search for the type in the special graphs, and annotate this predicate
   * to any detected graphs.
   */
  private void annotateGraph() {
    if (object != null && object.isReference()) {
      graphAnnotation = MulgaraGraphs.getTypeGraph(((URIReference)object).getURI());
    }
  }
}

