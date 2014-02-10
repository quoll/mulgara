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
 * A literal for use in unary type predicates.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class TypeLiteral extends PredicateLiteral implements TypeLabel, PredicateParam {

  /**
   * Creates a new literal with a domain for a type predicate.
   * @param domain The domain for the literal.
   * @param name The text of the literal.
   * @param context The context passed in from the parser.
   */
  public TypeLiteral(String domain, String name, ParseContext context) {
    super(domain, name, context);
  }

  //inheritdoc
  public void accept(TreeWalker walker) {
    walker.visit(this);
  }

  //inheritdoc
  public void print(int indent) {
    System.out.println(sp(indent) + "TypeLiteral ('" + name + "')");
  }

  //inheritdoc
  public boolean equals(Object o) {
    return o instanceof TypeLiteral && name.equals(((TypeLiteral)o).name);
  }

  // inheritdoc
  public int hashCode() {
    return name.hashCode() * 17;  // 17 for this type
  }

}

