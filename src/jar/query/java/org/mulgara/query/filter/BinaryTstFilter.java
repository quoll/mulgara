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
import org.mulgara.query.filter.value.Bool;


/**
 * A test operation for general RDF Terms, specifically equality or inequality.
 * Named with "Tst" instead of "Test" to avoid filters for the codebase tests.
 *
 * @created Mar 14, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class BinaryTstFilter extends AbstractFilterValue implements Filter {

  /** Serialization ID */
  private static final long serialVersionUID = 6169856559090192157L;

  /** The first operand */
  protected RDFTerm lhs;

  /** The second operand */
  protected RDFTerm rhs;

  /**
   * Creates a binary test, and registers this filter as the context owner
   * @param lhs The left expression
   * @param rhs The right expression
   */
  BinaryTstFilter(RDFTerm lhs, RDFTerm rhs) {
    super(lhs, rhs);
    this.lhs = lhs;
    this.rhs = rhs;
  }

  /**
   * @see org.mulgara.query.filter.Filter#test(Context)
   */
  public boolean test(Context context) throws QueryException {
    setCurrentContext(context);
    return testCmp();
  }

  /** @see org.mulgara.query.filter.AbstractFilterValue#resolve() */
  protected RDFTerm resolve() throws QueryException {
    return testCmp() ? Bool.TRUE : Bool.FALSE;
  }

  /**
   * A test specific comparison.
   * @return <code>true</code> iff the test passes.
   * @throws QueryException If there was an error resolving the operands
   */
  abstract boolean testCmp() throws QueryException;

}
