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

import java.net.URISyntaxException;

import org.mulgara.krule.rlog.ParseContext;
import org.mulgara.krule.rlog.parser.NSUtils;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.RDFNode;
import org.mulgara.krule.rlog.rdf.URIReference;

/**
 * Represents a value (not a variable) at any place in a predicate.
 * Basic String and integer literals have a separate type.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public abstract class PredicateLiteral extends Node implements BPredicateLabel, PredicateParam {

  /** The text found in this literal. */
  public final String name;

  /** The reference of this node. If null then the URI syntax is invalid. */
  public final URIReference ref;

  /**
   * Creates a new literal, including a domain, for use in a binary predicate.
   * @param domain The domain for the name (eg. rdfs or owl).
   * @param name The name of the literal.
   * @param context The context passed in from the parser.
   */
  public PredicateLiteral(String domain, String name, ParseContext context) {
    super(context);
    // use some indirect references to let the compiler know we are really setting the final values
    // storing null in ref means that the URI syntax is invalid
    URIReference r = null;
    String n = null;
    try {
      if (domain == null) {
        n = NSUtils.newName(name);
        r = new URIReference(name, context);
      } else {
        n = NSUtils.newName(domain, name);
        r = new URIReference(domain, name, context);
      }
    } catch (URISyntaxException e) { /* r == null */ }
    this.name = n;
    this.ref = r;
  }

  /** {@inheritDoc} */
  public abstract void accept(TreeWalker walker);

  /** {@inheritDoc} */
  public abstract void print(int indent);

  /**
   * {@inheritDoc} 
   * @throws URISyntaxException If the label is not a valid URI.
   */
  public RDFNode getRDFNode() throws URIParseException {
    if (ref == null) throw new URIParseException(name);
    return ref;
  }

  /**
   * Order by PredicateLiteral, StringLiteral, IntegerLiteral, Var.
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(PredicateParam o) {
    // smaller than everything except other PredicateLiterals
    return (o instanceof PredicateLiteral) ? name.compareTo(((PredicateLiteral)o).name) : -1;
  }

  /**
   * Defines the ordering that this class occurs in, compared to other PredicateParams
   * @see org.mulgara.krule.rlog.ast.PredicateParam#orderId()
   */
  public int orderId() {
    return PREDICATE_LITERAL_ID;
  }

  /** @see java.lang.Object#toString() */
  public String toString() {
    return name;
  }

}

