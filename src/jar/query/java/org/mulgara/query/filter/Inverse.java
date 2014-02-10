/*
 * Copyright 2010 Duraspace.
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
package org.mulgara.query.filter;

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.value.Bool;


/**
 * An inversion of a test.
 * This is different to NOT in that it will pass if the test fails with an exception.
 *
 * @created Jan 8, 2010
 * @author Paul Gearon
 */
public class Inverse extends AbstractFilterValue implements Filter {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = -246496920520897841L;

  /** The filter to invert. Local storage of operands[0]. */
  Filter operand;

  /**
   * Create an inversion of a filter
   * @param operand The filter to invert
   */
  public Inverse(Filter operand) {
    super(operand);
    this.operand = operand;
  }

  /**
   * @see org.mulgara.query.filter.Filter#test(Context)
   */
  public boolean test(Context context) throws QueryException {
    setCurrentContext(context);
    try {
      return !operand.test(context);
    } catch (QueryException e) {
      return true;
    }
  }

  /** @see org.mulgara.query.filter.AbstractFilterValue#resolve() */
  protected RDFTerm resolve() throws QueryException {
    try {
      return operand.test(getCurrentContext()) ? Bool.FALSE : Bool.TRUE;
    } catch (QueryException e) {
      return Bool.TRUE;
    }
  }

}
