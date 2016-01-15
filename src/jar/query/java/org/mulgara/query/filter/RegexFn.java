/**
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */
package org.mulgara.query.filter;

import org.apache.xerces.impl.xpath.regex.RegularExpression;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.value.ValueLiteral;


/**
 * The regular expression test for values.
 *
 * @created Mar 8, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class RegexFn extends BinaryTstFilter {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 6785353529347360357L;

  /** a cache of the last RegularExpression */
  private RegularExpression re = null;

  /** a cache of the last pattern string */
  private String oldPattern = null;

  /** The expression that resolves flags */
  private ValueLiteral flagExpression = null;

  /** a cache of the last flag string */
  private String oldFlags = null;

  /**
   * Creates an RegEx test operation with default flags.
   * @param str The string value to be tested.
   * @param patternStr The pattern to match the string against.
   */
  public RegexFn(ValueLiteral str, ValueLiteral patternStr) {
    super(str, patternStr);
  }

  /**
   * Creates an RegEx test operation with specified flags.
   * @param str The string value to be tested.
   * @param patternStr The pattern to match the string against.
   * @param flagExpression The flags to be used in the expression.
   */
  public RegexFn(ValueLiteral str, ValueLiteral patternStr, ValueLiteral flagExpression) {
    super(str, patternStr);
    this.flagExpression = flagExpression;
    if (flagExpression != null) flagExpression.setContextOwner(this);
  }

  /** @see org.mulgara.query.filter.BinaryTstFilter#testCmp() */
  boolean testCmp() throws QueryException {
    return regex().matches(str());
  }

  /**
   * Gets the regular expression to use for the current variable bindings.
   * This will calculate a new pattern and flags if either change for the current variable bindings.
   * @return A RegularExpression using the existing object if there was no update.
   * @throws QueryException If the pattern string or flags string cannot be resolved.
   */
  private RegularExpression regex() throws QueryException {
    String patternStr = pattern();
    String flagsStr = flags();
    if (re == null) {
      re = new RegularExpression(patternStr, flagsStr);
      oldPattern = patternStr;
      oldFlags = flagsStr;
    } else if (!patternStr.equals(oldPattern) || notEquals(flagsStr, oldFlags)) {
      // re.setPattern(patternStr, flagsStr); // this has a Xerces bug
      re = new RegularExpression(patternStr, flagsStr);
      oldPattern = patternStr;
      oldFlags = flagsStr;
    }
    return re;
  }

  /**
   * Gets the string to be matched in this regular expression.
   * @return The string to be matched against.
   * @throws QueryException If the expression for the string cannot be resolved.
   */
  private String str() throws QueryException {
    if (!lhs.isLiteral() || !((ValueLiteral)lhs).isSimple()) throw new QueryException("Type Error: Invalid type in regular expression. Need string, got: " + lhs.getClass().getSimpleName());
    return ((ValueLiteral)lhs).getLexical();
  }

  /**
   * Gets the pattern to use for this regex call.
   * @return The pattern to use.
   * @throws QueryException The expression for the pattern cannot be resolved.
   */
  private String pattern() throws QueryException {
    if (!rhs.isLiteral() || !((ValueLiteral)rhs).isSimple()) throw new QueryException("Type Error: Invalid pattern type in regular expression. Need string, got: " + rhs.getClass().getSimpleName());
    return ((ValueLiteral)rhs).getLexical();
  }

  /**
   * Gets the flags to use for this regex call.
   * @return The flags to use, or an empty string if none was provided.
   * @throws QueryException The expression the flags are built on cannot be resolved.
   */
  private String flags() throws QueryException {
    if (flagExpression == null) return null;
    if (!flagExpression.isLiteral() || !((ValueLiteral)flagExpression).isSimple()) throw new QueryException("Type Error: Invalid flags in regular expression. Need string, got: " + flagExpression.getClass().getSimpleName());
    return flagExpression.getLexical();
  }

  /**
   * Compares two strings that may be null for inequality.
   * @param a The first string.
   * @param b The second string.
   * @return <code>false</code> if the strings represent the same value, or are both null,
   *         <code>true</code> otherwise.
   */
  private static boolean notEquals(String a, String b) {
    if (a == null) return b != null;
    return b == null || !a.equals(b);
  }
}
