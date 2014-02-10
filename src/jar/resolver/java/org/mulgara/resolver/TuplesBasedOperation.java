/*
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

package org.mulgara.resolver;

import java.util.Arrays;

import org.mulgara.query.Variable;
import org.mulgara.store.statement.StatementStore;

/**
 * Handles mapping of Tuples to expected columns, when necessary.
 *
 * @created Jan 23, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class TuplesBasedOperation {

  /**
   * Check a variable array to see if it is in SPOG or GSPO order, and return a mapping array
   * to allow the columns to be accessed in SPOG order. If assertions are enabled, then the
   * entire structure is tested, otherwise the first column is all that is tested.
   * @param vars The variables to test for order.
   * @return A mapping array used to access variables in SPOG order.
   */
  protected static final int[] mapColumnsToStd(Variable[] vars) {
    assert vars.length == 4 : "Wrong number of variables. Expected {Subject,Predicate,Object,Meta} got " + Arrays.toString(vars);
    if (vars[0] == StatementStore.VARIABLES[0]) {
      assert vars[1] == StatementStore.VARIABLES[1] : "Expected '" + StatementStore.VARIABLES[1] + "' got '" + vars[1];
      assert vars[2] == StatementStore.VARIABLES[2] : "Expected '" + StatementStore.VARIABLES[2] + "' got '" + vars[2];
      assert vars[3] == StatementStore.VARIABLES[3] : "Expected '" + StatementStore.VARIABLES[3] + "' got '" + vars[3];
      return new int[] { 0, 1, 2, 3 };
    } else {
      assert vars[0] == StatementStore.VARIABLES[3] : "Expected '" + StatementStore.VARIABLES[3] + "' got '" + vars[0];
      assert vars[1] == StatementStore.VARIABLES[0] : "Expected '" + StatementStore.VARIABLES[0] + "' got '" + vars[1];
      assert vars[2] == StatementStore.VARIABLES[1] : "Expected '" + StatementStore.VARIABLES[1] + "' got '" + vars[2];
      assert vars[3] == StatementStore.VARIABLES[2] : "Expected '" + StatementStore.VARIABLES[2] + "' got '" + vars[3];
      return new int[] { 1, 2, 3, 0 };
    }
  }
}
