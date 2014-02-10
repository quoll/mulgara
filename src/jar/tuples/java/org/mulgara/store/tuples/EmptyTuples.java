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

package org.mulgara.store.tuples;

import java.util.Collections;
import java.util.List;

// Locally written packages
import org.mulgara.query.TuplesException;

/**
 * A tuples with no rows or columns.
 *
 * @created 2004-03-19
 *
 * @author Andrew Newman
 * @author <a href="http://staff.pisoftware.com/raboczi/">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:10 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class EmptyTuples extends AbstractTuples implements StoreTuples {

  /**
   * The singleton version of the empty tuples.
   */
  private static final EmptyTuples tuples = new EmptyTuples();

  private static final int[] columnOrder = new int[0];

  /**
   * Returns the singleton of this class.
   *
   * @return the singleton of this class.
   */
  public static EmptyTuples getInstance() {

    return tuples;
  }

  /**
   * Generate an empty expression.
   */
  protected EmptyTuples() {

  }

  //
  // Methods implementing Tuples interface
  //

  /**
   * METHOD TO DO
   *
   * @param prefix PARAMETER TO DO
   * @param suffixTruncation PARAMETER TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public void beforeFirst(long[] prefix, int suffixTruncation)
      throws TuplesException {

    // null implementation
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean next() throws TuplesException {

    return false;
  }

  /**
   * Gets the ColumnValue attribute of the MemoryTuples object
   *
   * @param column PARAMETER TO DO
   * @return The ColumnValue value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getColumnValue(int column) throws TuplesException {

    throw new TuplesException("No column "+column);
  }

  /**
   * Empty tuples always returns 0.
   *
   * @return The RowCount value
   */
  public long getRowCount() {
    return 0;
  }

  public long getRowUpperBound() {
    return 0;
  }

  public long getRowExpectedCount() {
    return 0;
  }

  public boolean isEmpty() throws TuplesException {
    return true;
  }

  /**
   * Empty has no rows that can be unbound.
   *
   * @return <code>false</code>
   */
  public boolean isColumnEverUnbound(int column) {

    return false;
  }

  public boolean hasNoDuplicates() {
    return true;
  }

  /**
   * Always returns true - empty is always materialized.
   *
   * @return <code>true</code> - empty is always materialized.
   */
  public boolean isMaterialized() {

    return true;
  }

  public List<Tuples> getOperands() {
    return Collections.emptyList();
  }

  /**
   * Returns an array which maps from column index to triple node index
   * (Subject=0, Predicate=1, Object=2, Meta=3).
   *
   * @return the array describing the column mapping.
   * @throws TuplesException EXCEPTION TO DO
   */
  public int[] getColumnOrder() {
    return columnOrder;
  }

  //
  // Methods overriding the Object class
  //

  /**
   * METHOD TO DO
   *
   * @param object PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public boolean equals(Object object) {

    // If we're equals by reference, we're trivially equal
    if (object == tuples) {
      return true;
    }

    // If we don't satisfy the Tuples interface, we can't be equal
    if (!(object instanceof Tuples)) {
      return false;
    }

    // Otherwise, we're empty if we have zero rows
    try {
      return ((Tuples) object).getRowCardinality() == ZERO;
    }
    catch (TuplesException e) {
      throw new RuntimeException("Couldn't get row count from tuples", e);
    }
  }

  public int hashCode() {
    return columnOrder.hashCode();
  }

  /**
   * Returns itself.
   *
   * @return itself.
   */
  public Object clone() {

    return tuples;
  }

  /**
   * METHOD TO DO
   */
  public void close() {

    // NO-OP
  }
}
