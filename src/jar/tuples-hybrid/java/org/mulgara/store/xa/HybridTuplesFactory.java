/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.store.xa;


import org.mulgara.query.TuplesException;
import org.mulgara.store.tuples.DefaultRowComparator;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesFactory;

/**
 * Generates {@link HybridTuples} instances.
 *
 * @created 2003-02-03
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:12 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class HybridTuplesFactory extends TuplesFactory {

  /**
   * The default ordering for tuples is by node number.
   */
  private RowComparator rowComparator = new DefaultRowComparator();


  /**
   * Copy constructor.
   *
   * @param tuples an existing instance, whose contents will be copied into the
   *      new instance
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

    return new HybridTuples(tuples, rowComparator);
  }
}
