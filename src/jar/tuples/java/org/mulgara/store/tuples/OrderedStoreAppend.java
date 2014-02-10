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

package org.mulgara.store.tuples;

import org.mulgara.query.TuplesException;

/**
 * A class for performing appends between identically structured StoreTuples.
 *
 * @created Dec 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class OrderedStoreAppend extends OrderedAppend implements StoreTuples {

  /**
   * @param operands
   * @throws TuplesException
   */
  public OrderedStoreAppend(StoreTuples[] operands) throws TuplesException {
    super(operands);
    assert operands.length >= 2 : "Performing join on fewer than 2 operands";
    assert operandsMatch(operands) : "Operands do not match in OrderedStoreAppend";
  }

  /**
   * @see org.mulgara.store.tuples.StoreTuples#getColumnOrder()
   */
  public int[] getColumnOrder() {
    return ((StoreTuples)operands[0]).getColumnOrder();
  }

  /**
   * Test if all the operands match, like they are supposed to.
   * @param operands The operands to test.
   * @return <code>true</code> iff the operands all share column ordering.
   */
  private boolean operandsMatch(StoreTuples[] operands) {
    int[] order = operands[0].getColumnOrder();
    for (int i = 1; i < operands.length; i++) {
      int[] newOrder = operands[i].getColumnOrder();
      if (newOrder.length != order.length) return false;
      for (int c = 0; c < order.length; c++) if (newOrder[c] != order[c]) return false;
    }
    return true;
  }

}
