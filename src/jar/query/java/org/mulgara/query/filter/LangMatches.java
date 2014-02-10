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

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.value.ValueLiteral;


/**
 * The lang matches a language pattern.
 * This is a rudimentary implementation that is equivalent to the Jena langMatch test.
 * TODO: fully implement RFC 4647: <a href="http://www.ietf.org/rfc/rfc4647.txt">http://www.ietf.org/rfc/rfc4647.txt</a>
 *
 * @created Mar 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class LangMatches extends BinaryTstFilter {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 7483330484341360061L;

  /**
   * Creates an equality test operation
   * @param lhs The first term to compare
   * @param rhs The second term to compare
   */
  public LangMatches(ValueLiteral lhs, ValueLiteral rhs) {
    super(lhs, rhs);
  }

  /** @see org.mulgara.query.filter.BinaryTstFilter#testCmp() */
  boolean testCmp() throws QueryException {
    testSimple(lhs);
    testSimple(rhs);
    String lang = ((ValueLiteral)lhs).getLang().getLexical();
    String pattern = ((ValueLiteral)rhs).getLexical();
    // the * pattern matches anything that isn't empty
    if ("*".equals(pattern)) return lang.length() != 0;
    // all other patterns are matched by prefix, with case insensitivity
    return lang.toLowerCase().startsWith(pattern.toLowerCase());
  }

  /**
   * Tests if the parameter is a {@link org.mulgara.query.filter.value.SimpleLiteral}.
   * Throws an exception if it is not.
   * @param v The parameter to test
   * @throws QueryException Thrown if v is not a simple literal.
   */
  private void testSimple(RDFTerm v) throws QueryException {
    if (!v.isLiteral() || !((ValueLiteral)v).isSimple()) throw new QueryException("Type Error: LangMatches requires simple literals");
  }

}
