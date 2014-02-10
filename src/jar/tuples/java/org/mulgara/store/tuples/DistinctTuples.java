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

import java.util.List;
import java.util.Collections;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Iterate-and-check removal of duplicate rows.
 *
 * @created 2003-02-05
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:10 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class DistinctTuples extends AbstractTuples {

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(DistinctTuples.class);

  /**
   * The proposition to filter.
   */
  private Tuples operand;

  /**
   * The previous distinct row emitted. At the beginning when there is no
   * previous row, it is <code>null</code>.
   */
  private long[] previous;

  /**
   * Eliminate duplicate rows in {@link Tuples} which have them.
   *
   * @param operand the tuples to filter
   * @throws IllegalArgumentException if <var>operand</var> is <code>null</code>
   *      , has no duplicate rows, or is unordered
   * @throws TuplesException EXCEPTION TO DO
   */
  DistinctTuples(Tuples operand) throws TuplesException {

    // Validate "operand" parameter
    if (operand == null) {
      throw new IllegalArgumentException("Null \"operand\" parameter");
    }

    if (operand.hasNoDuplicates()) {
      throw new IllegalArgumentException("Operand has no duplicate rows");
    }

    if (operand.getComparator() == null) {
      throw new IllegalArgumentException("Operand is unsorted");
    }

    // Initialize fields
    this.operand = (Tuples) operand.clone();
    setVariables(operand.getVariables());
  }

  /**
   * Cloning constructor.
   *
   * @param tuples PARAMETER TO DO
   *//*
  private DistinctTuples(DistinctTuples tuples) {

    // Copy the "operand" field
    operand = (Tuples) tuples.operand.clone();

    // Copy the "previous" field
    if (tuples.previous != null) {

      previous = new long[tuples.previous.length];
      System.arraycopy(tuples.previous, 0, previous, 0, previous.length);
    }
    else {

      previous = null;
    }
  }
*/
  /**
   * Gets the ColumnValue attribute of the DistinctTuples object
   *
   * @param column PARAMETER TO DO
   * @return The ColumnValue value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getColumnValue(int column) throws TuplesException {

    return operand.getColumnValue(column);
  }

  /**
   * Gets the Comparator attribute of the DistinctTuples object
   *
   * @return The Comparator value
   */
  public RowComparator getComparator() {
    return operand.getComparator();
  }

  public long getRowUpperBound() throws TuplesException {
    return operand.getRowUpperBound();
  }

  public long getRowExpectedCount() throws TuplesException {
    return operand.getRowExpectedCount();
  }

  public boolean isEmpty() throws TuplesException {
    return operand.isEmpty();
  }

  /**
   * Gets the Variables attribute of the DistinctTuples object
   *
   * @return The Variables value
   */
  public Variable[] getVariables() {
    return operand.getVariables();
  }

  /**
   * Inherits the value of the operand column.
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {

    return operand.isColumnEverUnbound(column);
  }

  /**
   * Gets the Materialized attribute of the DistinctTuples object
   *
   * @return The Materialized value
   */
  public boolean isMaterialized() {

    return false;
  }

  /**
   * @return whether every operand is unconstrained
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean isUnconstrained() throws TuplesException {

    return operand.isUnconstrained();
  }

  public List<Tuples> getOperands() {
    return Collections.singletonList(operand);
  }

  //
  // Methods implementing Tuples
  //

  /**
   * METHOD TO DO
   *
   * @param prefix PARAMETER TO DO
   * @param suffixTruncation PARAMETER TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public void beforeFirst(long[] prefix,
      int suffixTruncation) throws TuplesException {

    operand.beforeFirst(prefix, suffixTruncation);
    previous = null;
  }

  /**
   * METHOD TO DO
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public void close() throws TuplesException {

    operand.close();
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean hasNoDuplicates() throws TuplesException {

    return true;
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean next() throws TuplesException {

    boolean isDuplicate = true;

    while (isDuplicate) {

      if (operand.next()) {

        if (previous == null) {

          previous = new long[getVariables().length];

          for (int i = 0; i < getVariables().length; i++) {

            previous[i] = operand.getColumnValue(i);
          }

          isDuplicate = false;
        }
        else {

          for (int i = 0; i < getVariables().length; i++) {

            if (previous[i] != operand.getColumnValue(i)) {

              previous[i] = operand.getColumnValue(i);
              isDuplicate = false;
            }
          }
        }

        if (logger.isDebugEnabled()) {

          StringBuffer buffer = new StringBuffer();

          for (int i = 0; i < previous.length; i++) {

            buffer.append(previous[i]).append(" ");
          }

          logger.debug(buffer.append(" is ").append(isDuplicate).toString());
        }
      }
      else {

        return false;
      }
    }

    return true;
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Object clone() {
    DistinctTuples dtuples = (DistinctTuples) super.clone();

    // Copy the "operand" field
    dtuples.operand = (Tuples) operand.clone();

    // Copy the "previous" field
    if (previous != null) {
      dtuples.previous = new long[previous.length];
      System.arraycopy(previous, 0, dtuples.previous, 0, dtuples.previous.length);
    } else {
      dtuples.previous = null;
    }

    return dtuples;
  }
}
