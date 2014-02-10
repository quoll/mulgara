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

import java.util.List;

import org.mulgara.query.QueryException;


/**
 * A product of filters.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class And extends NAryOperatorFilter {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = -8652610238525527869L;

  /**
   * Create a product of other filters.
   * @param operands The operands of the product
   */
  public And(Filter... operands) {
    super(operands);
  }

  /**
   * Returns the head of the list ANDed with the product of the remainder of the list.
   * <code>false</code> and Exception is <code>false</code>.
   * <code>true</code> and Exception is Exception.
   * @see org.mulgara.query.filter.Filter#test(Context)
   */
  boolean testList(Context context, List<Filter> filters) throws QueryException {
    Filter head = filters.get(0);
    if (filters.size() == 1) return head.test(context);

    boolean result = false;
    try {
      result = head.test(context);
    } catch (QueryException e) {
      // false on the remainder gives false
      if (!testList(context, tail(filters))) return false;
      throw e;
    }
    return result && testList(context, tail(filters));
  }

}
