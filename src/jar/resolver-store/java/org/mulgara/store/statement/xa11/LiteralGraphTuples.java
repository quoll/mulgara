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

package org.mulgara.store.statement.xa11;

import org.mulgara.query.TuplesException;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.tuples.LiteralTuples;
import org.mulgara.store.tuples.StoreTuples;

/**
 * A Tuples for representing a specified Graph.
 *
 * @created Jan 14, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class LiteralGraphTuples extends LiteralTuples implements StoreTuples {

  static final String[] META_NAMES = new String[] { StatementStore.VARIABLES[StatementStore.VARIABLES.length - 1].getName() };

  /**
   * Creates a new literal tuples containing graphs.
   * @param sorted
   */
  public LiteralGraphTuples(boolean sorted) {
    super(META_NAMES, sorted);
  }

  /**
   * Creates a new literal tuples containing graphs.
   * @param graph The localnode for the graph to be represented.
   */
  public LiteralGraphTuples(long graph) {
    super(META_NAMES, true);
    try {
      appendTuple(new long[] { graph });
    } catch (TuplesException te) {
      // should not happen
      throw new AssertionError("Unable to add an element to an array");
    }
  }

  /**
   * @see org.mulgara.store.tuples.StoreTuples#getColumnOrder()
   */
  public int[] getColumnOrder() {
    return new int[] { 0 };
  }

}
