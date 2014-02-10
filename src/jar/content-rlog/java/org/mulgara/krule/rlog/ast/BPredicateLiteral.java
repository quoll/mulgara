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

/**
 * Represents a value (not a variable) at any place in a predicate.
 * Basic String and integer literals have a separate type.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class BPredicateLiteral extends PredicateLiteral implements BPredicateLabel, PredicateParam {

  /**
   * Creates a new literal, including a domain, for use in a binary predicate.
   * @param domain The domain for the name (eg. rdfs or owl).
   * @param name The name of the literal.
   * @param context The context passed in from the parser.
   */
  public BPredicateLiteral(String domain, String name, ParseContext context) {
    super(domain, name, context);
  }

  /** {@inheritDoc} */
  public void accept(TreeWalker walker) {
    walker.visit(this);
  }

  /** {@inheritDoc} */
  public void print(int indent) {
    System.out.println(sp(indent) + "BPredicateLiteral ('" + name + "')");
  }

  /** {@inheritDoc} */
  public boolean equals(Object o) {
    return o instanceof BPredicateLiteral && name.equals(((BPredicateLiteral)o).name);
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return name.hashCode() * 13;  // 13 for this type
  }

}

