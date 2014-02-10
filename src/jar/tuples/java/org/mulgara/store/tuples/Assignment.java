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

// Third party packages
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * A {@link Tuples} representing a single variable assignment.
 *
 * A variable assignment has one column (the variable) and one row, binding that
 * one column to the value.
 *
 * @created 2004-03-19
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
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class Assignment extends AbstractTuples {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(Assignment.class.getName());

  /**
   * The value the variable is assigned.
   */
  private long value;

  /**
   * The cursor position.
   *
   * The only values this field takes are {@link #BEFORE_ROW}, {@link #ON_ROW}
   * and {@link #AFTER_ROW}.
   */
  private int row = BEFORE_ROW;

  /**
   * Value of {@link #row} indicating that the cursor is before the single row.
   */
  private static final int BEFORE_ROW = -1;

  /**
   * Value of {@link #row} indicating that the cursor is on the single row.
   */
  private static final int ON_ROW = 0;

  /**
   * Value of {@link #row} indicating that the cursor is after the single row.
   */
  private static final int AFTER_ROW = 1;

  //
  // Constructor
  //

  /**
   * Construct an assignment.
   *
   * @param variable  the variable to bind
   * @param value     the value to bind the <var>variable</var>
   * @throws IllegalArgumentException if <var>variable</var> is
   *   <code>null</code> or <var>value</var> is {@link #UNBOUND}
   */
  Assignment(Variable variable, long value) {
    // Validate "variable" parameter
    if (variable == null) {
      throw new IllegalArgumentException("Null \"variable\" parameter");
    }

    // Validate "value" parameter
    if (value == UNBOUND) {
      throw new IllegalArgumentException("Unbound \"value\" parameter");
    }

    // Initialize fields
    setVariables(new Variable[] {variable});
    this.value = value;
  }

  //
  // Methods implementing AbstractTuples
  //

  /**
   * Gets the ColumnValue attribute of the AbstractTuples object
   *
   * @param column PARAMETER TO DO
   * @return The ColumnValue value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getColumnValue(int column) throws TuplesException {
    // Make sure we're asking for column zero (the only column, in this case)
    if (column != 0) {
      throw new TuplesException("No column " + column);
    }

    return value;
  }

  /**
   * Move the cursor to before the first row, optionally with a specifies list
   * of leading column values.
   *
   * @param prefix only iterate through product terms with the specified leading
   *      column values ({@link #NO_PREFIX} should be passed if all prefix
   *      values are desired
   * @param suffixTruncation the number of trailing rows to ignore when
   *      determining whether a row is distinct
   * @throws IllegalArgumentException if <var>prefix</var> is <code>null</code>
   * @throws TuplesException EXCEPTION TO DO
   */
  public void beforeFirst(long[] prefix, int suffixTruncation) throws
      TuplesException {
    // Validate "prefix" parameter
    if (prefix == null) {
      throw new IllegalArgumentException("Null \"prefix\" parameter");
    }

    switch (prefix.length) {
      case 0:
        row = BEFORE_ROW;
        break;

      case 1:

        // We want a subsequent call to next() to move the cursor to be on the
        // single row if the prefix value matches the assignment value, or to
        // move the cursor to after the single row if the prefix value doesn't
        // match the assignment value.
        row = (value == prefix[0]) ? BEFORE_ROW : ON_ROW;
        break;

      default:

        // Any prefix longer than the number of columns (1) makes no sense
        throw new IllegalArgumentException(
            "Value " + toString(prefix) +
            " for parameter \"prefix\" is too long"
            );
    }
  }

  /**
   * Convenience method for the usual case of wanting to reset a tuples to
   * iterate through every single element.
   *
   * Equivalent to
   * {@link #beforeFirst(long[], int)}<code>({@link #NO_PREFIX}, 0)</code>.
   *
   * @throws TuplesException {@inheritDoc}
   */
  public void beforeFirst() throws TuplesException {
    row = BEFORE_ROW; // before the first row
  }

  /**
   * Move to the next row satisfying the current prefix and suffix truncation.
   *
   * If no such row exists, return <code>false<code> and the current row
   * becomes unspecified.  The current row is unspecified when a tuples instance
   * is created.  To specify the current row, the {@link #beforeFirst()} or
   * {@link #beforeFirst(long[], int)} methods must be invoked
   *
   * @return whether a subsequent row with the specified prefix exists
   * @throws IllegalStateException if the current row is unspecified.
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean next() throws TuplesException {
    switch (row) {
      case BEFORE_ROW:
        row = ON_ROW;
        return true;

      case ON_ROW:
        row = AFTER_ROW;
        return false;

      case AFTER_ROW:
        throw new TuplesException("Already after last row");

      default: // any other row value should never occur
        throw new Error("Impossible row value: " + row);
    }
  }

  /**
   * METHOD TO DO
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public void close() throws TuplesException {
    // null implementation
  }

  /**
   * An assignment always has a single row.
   *
   * @return 1
   */
  public long getRowCount() {
    return 1;
  }

  public long getRowUpperBound() {
    return getRowCount();
  }

  public long getRowExpectedCount() {
    return getRowCount();
  }

  /* (non-Javadoc)
   * @see org.mulgara.store.tuples.AbstractTuples#isEmpty()
   */
  @Override
  public boolean isEmpty() throws TuplesException {
    return false;
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
   * An unbound assignment would be a contradiction.
   *
   * @return <code>false</code>
   */
  public boolean isColumnEverUnbound(int column) {
    return false;
  }

  /**
   * An assignment is stored in RAM.
   *
   * @return <code>true</code>
   */
  public boolean isMaterialized() {
    return true;
  }

  public List<Tuples> getOperands() {
    return Collections.emptyList();
  }

  public RowComparator getRowComparator() {
    return DefaultRowComparator.getInstance();
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Object clone() {
    Assignment cloned = (Assignment)super.clone();

    // Copy immutable fields by reference
    cloned.value = value;

    // Copy mutable fields by value
    cloned.row = row;

    return cloned;
  }
}
