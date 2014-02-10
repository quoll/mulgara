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
 * Describes a test function on an RDFTerm.
 *
 * @created Mar 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractBooleanFn extends AbstractFilterValue implements Filter {

  /** Serialization ID */
  private static final long serialVersionUID = -5045050008524194860L;

  /** The variable to test */
  RDFTerm operand;

  /**
   * Create a function for testing a term
   * @param operand The term to test
   */
  public AbstractBooleanFn(RDFTerm operand) {
    super(operand);
    this.operand = operand;
  }

  /**
   * @see org.mulgara.query.filter.Filter#test(Context)
   */
  public boolean test(Context context) throws QueryException {
    setCurrentContext(context);
    return fnTest();
  }

  /** @see org.mulgara.query.filter.AbstractFilterValue#resolve() */
  protected RDFTerm resolve() throws QueryException {
    return fnTest() ? Bool.TRUE : Bool.FALSE;
  }

  /**
   * An implementation specific test
   * @return <code>true</code> when this test passes.
   * @throws QueryException Thrown when an error occurs while trying to resolve the value of the operand.
   */
  abstract boolean fnTest() throws QueryException;
}
