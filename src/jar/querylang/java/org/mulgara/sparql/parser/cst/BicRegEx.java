/*
 * Copyright 2008 Fedora Commons
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.sparql.parser.cst;


/**
 * An invocation to the REGEX function
 *
 * @created Feb 13, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class BicRegEx implements BuiltInCall, LogicExpression {
  /** The expression being tested */
  private Expression expr;

  /** The pattern to test for */
  private Expression pattern;

  /** The flags to use for matching */
  private Expression flags;

  /** The parameter separator in the string represenation */
  private static final String COMMA = ", ";

  /**
   * Construct an invocation to the REGEX function
   * @param expr The expression to test
   * @param pattern The pattern to test for
   * @param flags The flags to use for matching
   */
  public BicRegEx(Expression expr, Expression pattern, Expression flags) {
    this.expr = expr;
    this.pattern = pattern;
    this.flags = flags;
  }

  /**
   * @return the expr
   */
  public Expression getExpr() {
    return expr;
  }

  /**
   * @return the pattern
   */
  public Expression getPattern() {
    return pattern;
  }

  /**
   * @return the flags
   */
  public Expression getFlags() {
    return flags;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    StringBuffer result = new StringBuffer("REGEX(");
    result.append(expr.getImage()).append(COMMA);
    result.append(pattern.getImage());
    if (flags != null) result.append(COMMA).append(flags.getImage());
    result.append(")");
    return result.toString();
  }

}
