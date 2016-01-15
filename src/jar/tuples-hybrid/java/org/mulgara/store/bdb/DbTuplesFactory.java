/*
 * Copyright 2010, Paula Gearon
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

package org.mulgara.store.bdb;


import org.mulgara.query.TuplesException;
import org.mulgara.store.tuples.DefaultRowComparator;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesFactory;

/**
 * Generates {@link BdTuples} instances.
 *
 * @created 2010-07-12
 * @author Paula Gearon
 */
public class DbTuplesFactory extends TuplesFactory {

  /** The default ordering for tuples is by node number. */
  private RowComparator rowComparator = new DefaultRowComparator();


  /**
   * Copy constructor.
   *
   * @param tuples an existing instance, whose contents will be copied into the new instance
   * @return a new {@link HybridTuples} instance
   * @throws IllegalArgumentException if <var>tuples</var> is <code>null</code>
   * @throws TuplesException if a copy of <var>tuples</var> can't be created
   */
  public Tuples newTuples(Tuples tuples) throws TuplesException {
    return newTuples(tuples, rowComparator);
  }


  /**
   * Construct a new tuples with a specified order.
   *
   * @param tuples an existing instance, whose contents will be copied into the
   *      new instance
   * @param rowComparator the desired sort order
   * @return a new {@link HybridTuples} instance
   * @throws IllegalArgumentException if <var>tuples</var> or <var>rowComparator
   *      </var> parameters are <code>null</code>
   * @throws TuplesException if a copy of <var>tuples</var> can't be created
   */
  public Tuples newTuples(Tuples tuples, RowComparator rowComparator) throws TuplesException {
    if (rowComparator == null) {
      throw new IllegalArgumentException("Null \"rowComparator\" parameter");
    }
    if (tuples == null) {
      throw new IllegalArgumentException("Null \"tuples\" parameter");
    }

    return new DbTuples(tuples, rowComparator);
  }
}
