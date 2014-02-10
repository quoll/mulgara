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

import org.mulgara.krule.rlog.rdf.Literal;
import org.mulgara.krule.rlog.rdf.RDF;
import org.mulgara.krule.rlog.rdf.RDFNode;

/**
 * A number in the AST.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class IntegerLiteral extends Node implements PredicateParam {

  /** The value of the number. */
  public final Long value;

  /**
   * A new numeric literal.
   * @param s The string representation of the number.
   */
  public IntegerLiteral(String s) {
    this(Long.valueOf(s));
  }

  /**
   * A new numeric literal.
   * @param value The value of the number.
   */
  public IntegerLiteral(Long value) {
    super(null);
    this.value = value;
  }

  // inheritdoc
  public void accept(TreeWalker walker) {
    walker.visit(this);
  }

  // inheritdoc
  public void print(int indent) {
    System.out.println(sp(indent) + "IntegerLiteral (" + value + ")");
  }

  //inheritdoc
  public boolean equals(Object o) {
    return o instanceof IntegerLiteral && value.equals(((IntegerLiteral)o).value);
  }

  // inheritdoc
  public int hashCode() {
    return value.hashCode();
  }

  /** {@inheritDoc} */
  public RDFNode getRDFNode() {
    return new Literal(value.toString(), RDF.XSD_LONG);
  }


  /**
   * Order by PredicateLiteral, StringLiteral, IntegerLiteral, Var.
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(PredicateParam o) {
    if (o instanceof IntegerLiteral) return value.compareTo(((IntegerLiteral)o).value);
    // Smaller than Variable, larger than everything else
    return (o instanceof Variable) ? -1 : 1;
  }


  /**
   * Defines the ordering that this class occurs in, compared to other PredicateParams
   * @see org.mulgara.krule.rlog.ast.PredicateParam#orderId()
   */
  public int orderId() {
    return INTEGER_LITERAL_ID;
  }

}

