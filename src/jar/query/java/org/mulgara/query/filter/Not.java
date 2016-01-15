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
package org.mulgara.query.filter;

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.value.Bool;


/**
 * An inversion of a test.
 *
 * @created Mar 7, 2008
 * @author Paula Gearon
 */
public class Not extends AbstractFilterValue implements Filter {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 1225895946822519277L;

  /** The filter to invert. Local storage of operands[0]. */
  Filter operand;

  /**
   * Create an inversion of a filter
   * @param operand The filter to invert
   */
  public Not(Filter operand) {
    super(operand);
    this.operand = operand;
  }

  /**
   * @see org.mulgara.query.filter.Filter#test(Context)
   */
  public boolean test(Context context) throws QueryException {
    setCurrentContext(context);
    return !operand.test(context);
  }

  /** @see org.mulgara.query.filter.AbstractFilterValue#resolve() */
  protected RDFTerm resolve() throws QueryException {
    return operand.test(getCurrentContext()) ? Bool.FALSE : Bool.TRUE;
  }

}
