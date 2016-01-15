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

import org.mulgara.krule.rlog.ParseContext;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.Literal;
import org.mulgara.krule.rlog.rdf.RDFNode;
import org.mulgara.krule.rlog.rdf.URIReference;
import static org.mulgara.util.ObjectUtil.eq;

/**
 * A quoted string in the AST.
 * 
 * @created May 16, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StringLiteral extends Node implements PredicateParam {

  /** The string value. */
  public final String value;

  /** The language code for the string. */
  public String lang = null;

  /** The datatype for the literal. */
  public PredicateLiteral type = null;

  /**
   * A new string literal.
   * @param value The contents of the quoted string.
   * @param context The context passed in from the parser. Keeping this in case we need it
   *        in future for datatyped literals.
   */
  public StringLiteral(String value, ParseContext context) {
    super(context);
    this.value = value;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public void setType(PredicateLiteral type) {
    this.type = type;
  }

  // inheritdoc
  public void accept(TreeWalker walker) {
    walker.visit(this);
  }

  // inheritdoc
  public void print(int indent) {
    System.out.println(sp(indent) + "StringLiteral ('" + value + "')");
  }

  //inheritdoc
  public boolean equals(Object o) {
    if (o instanceof StringLiteral) {
      StringLiteral ol = (StringLiteral)o;
      return value.equals(ol.value) && eq(lang, ol.lang) && eq(type, ol.type);
    }
    return false;
  }

  // inheritdoc
  public int hashCode() {
    return value.hashCode();
  }

  /** {@inheritDoc} */
  public RDFNode getRDFNode() throws URIParseException {
    if (lang != null) return new Literal(value, lang);
    if (type != null) return new Literal(value, (URIReference)type.getRDFNode());
    return new Literal(value.toString());
  }

  /**
   * Order by PredicateLiteral, StringLiteral, IntegerLiteral, Var.
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(PredicateParam o) {
    if (o instanceof StringLiteral) return value.compareTo(((StringLiteral)o).value);
    // larger than PredicateLiteral, smaller than everything else
    return (o instanceof PredicateLiteral) ? 1 : -1;
  }

  /**
   * Defines the ordering that this class occurs in, compared to other PredicateParams
   * @see org.mulgara.krule.rlog.ast.PredicateParam#orderId()
   */
  public int orderId() {
    return STRING_LITERAL_ID;
  }

}

