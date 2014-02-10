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

import org.mulgara.krule.rlog.ParseContext;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.RDFNode;
import org.mulgara.krule.rlog.rdf.Var;

/**
 * Represents the inversion of a standard predicate.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class InvertedPredicate extends Predicate {

  public final Predicate invertPredicate;

  /**
   * Create this inverted predicate instance, with the context it requires.
   * @param context The context passed in from the parser.
   */
  public InvertedPredicate(Predicate typeStatement, ParseContext context) {
    super(context);
    this.invertPredicate = typeStatement;
  }

  public void accept(TreeWalker walker) {
    walker.visit(this);
  }

  public void print(int indent) {
    System.out.println(sp(indent) + "InvertedType {");
    invertPredicate.print(indent + 1);
    System.out.println(sp(indent) + "}");
  }

  /**
   * @return the wrapped Predicate
   */
  public Predicate getInvertPredicate() {
    return invertPredicate;
  }

  @Override
  public Collection<Var> getVariables() {
    return invertPredicate.getVariables();
  }

  /**
   * Get the subject of this predicate.
   * @return Either a URIReference, or a variable
   * @throws URIParseException The node is a malformed URIReference
   */
  public RDFNode getSubject() throws URIParseException {
    return invertPredicate.getSubject();
  }
  
  /**
   * Get the predicate value of this predicate.
   * @return Either a URIReference, or a variable
   */
  public RDFNode getPredicate() throws URIParseException {
    return invertPredicate.getPredicate();
  }

  /**
   * Get the object of this predicate.
   * @return Either a URIReference, a variable, or a literal
   * @throws URIParseException The node is a malformed URIReference
   */
  public RDFNode getObject() throws URIParseException {
    return invertPredicate.getObject();
  }

  @Override
  CanonicalPredicate getCanonical() {
    return invertPredicate.getCanonical().invert();
  }

}

