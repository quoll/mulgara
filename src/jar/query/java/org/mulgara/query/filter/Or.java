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
 * A sum of filters.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Or extends NAryOperatorFilter {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 1062167458998556025L;

  /**
   * Create a sum of other filters.
   * @param operands The operands of the sum.
   */
  public Or(Filter... operands) {
    super(operands);
  }

  /**
   * Returns the head of the list ORed with the sum of the remainder of the list.
   * <code>true</code> or Exception is <code>true</code>.
   * <code>false</code> or Exception is Exception.
   * @see org.mulgara.query.filter.Filter#test(Context)
   */
  boolean testList(Context context, List<Filter> filters) throws QueryException {
    Filter head = filters.get(0);
    if (filters.size() == 1) return head.test(context);

    boolean result;
    try {
      result = filters.get(0).test(context);
    } catch (QueryException e) {
      // true on the RHS gives true
      if (testList(context, tail(filters))) return true;
      throw e;
    }
    return result || testList(context, tail(filters));
  }

}
