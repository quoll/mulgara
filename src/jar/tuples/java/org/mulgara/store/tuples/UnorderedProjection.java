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

// Java 2 standard packages
import java.util.*;

// Log4J
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.util.StackTrace;

/**
 * Rearrange columns, discarding any existing sort order. If columns need to be
 * discarded but not rearranged, {@link OrderedProjection} should be used
 * instead.
 *
 * @created 2003-02-04
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
class UnorderedProjection extends AbstractTuples {

  /**
   * Logger.
   */
  private final static Logger logger = Logger.getLogger(UnorderedProjection.class);

  /**
   * The proposition to project.
   */
  private final Tuples operand;

  /**
   * Array indexed on operand columns, whose values are projected columns.
   */
  private final int[] columnMapping;

  /**
   * Value within the {@link #columnMapping} array to indicate that the operand
   * lacks a given column.
   */
  private final int ABSENT_COLUMN = -1;

  /**
   * Eliminate columns from a {@link Tuples}. This does not eliminate
   * duplicates; {@link DistinctTuples} should be used to produce a formal
   * relational projection.
   *
   * @param operand the tuples to project
   * @param columnList the rearranged columns
   * @throws IllegalArgumentException if <var>operand</var> is <code>null</code>
   * @throws TuplesException EXCEPTION TO DO
   */
  UnorderedProjection(Tuples operand, List<Variable> columnList) throws TuplesException {

    // Validate "operand" parameter
    if (operand == null) {
      throw new IllegalArgumentException("Null \"operand\" parameter");
    }

    // Validate "variables" parameter
    if (columnList == null) {
      throw new IllegalArgumentException("Null \"columnList\" parameter");
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Projecting columns " + columnList + " on " + operand);
    }

    // Initialize fields
    this.operand = (Tuples)operand.clone();
    setVariables(columnList);

    // Create column mapping
    columnMapping = new int[getNumberOfVariables()];

    Variable[] vars = getVariables();
    Arrays.fill(columnMapping, ABSENT_COLUMN);

    Variable[] operandVariables = operand.getVariables();

    for (int i = 0; i < getNumberOfVariables(); i++) {
      for (int j = 0; j < operandVariables.length; j++) {
        if (vars[i].equals(operandVariables[j])) {
          columnMapping[i] = j;

          break;
        }
      }
    }
  }

  /**
   * Cloning constructor.
   *
   * @param parent PARAMETER TO DO
   */
  private UnorderedProjection(UnorderedProjection parent) {

    operand = (Tuples) parent.operand.clone();
    setVariables(parent.getVariables());
    columnMapping = parent.columnMapping;
  }

  /**
   * Gets the ColumnValue attribute of the UnorderedProjection object
   *
   * @param column The 0-indexed column number to get the value from
   * @return The value for the column binding, or {@link Tuples#UNBOUND} if the value is not bound.
   * @throws TuplesException If there is an error accessing one of the original Tuples.
   */
  public long getColumnValue(int column) throws TuplesException {
    assert((column >= 0) && (column < getNumberOfVariables())) ||
        (column == ABSENT_COLUMN):"Invalid column " + column;

    if (columnMapping[column] == ABSENT_COLUMN) {
      if (logger.isDebugEnabled()) {
        logger.debug(getVariables()[column] + " is never bound\n " + new StackTrace());
      }
      return Tuples.UNBOUND;
    } else {
      return operand.getColumnValue(columnMapping[column]);
    }
  }

  /**
   * Gets the Comparator attribute of the UnorderedProjection object
   *
   * @return The Comparator value
   */
  public RowComparator getComparator() {

    return null;
  }

  /**
   * Gets the RowCount attribute of the UnorderedProjection object
   *
   * @return The RowCount value
   * @throws TuplesException Error accessing underlying data
   */
  public long getRowCount() throws TuplesException {
    return operand.getRowCount();
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
   * A column is unbound if it is unbound in the source of the projection.
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    assert((column >= 0) && (column < getNumberOfVariables())) ||
        (column == ABSENT_COLUMN):"Invalid column " + column;

    if (columnMapping[column] == ABSENT_COLUMN) {

      if (logger.isInfoEnabled()) {
        logger.info(getVariables()[column] + " is never bound", new Throwable());
      }

      return true;
    }
    else {

      return operand.isColumnEverUnbound(columnMapping[column]);
    }
  }

  /**
   * Gets the Materialized attribute of the UnorderedProjection object
   *
   * @return The Materialized value
   */
  public boolean isMaterialized() {

    return operand.isMaterialized();
  }

  /**
   * @return whether every operand is unconstrained
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean isUnconstrained() throws TuplesException {
    return (((getVariables().length == 0 &&
        operand.getRowCardinality() != Cursor.ZERO)) ||
        (getVariables().length > 0 && operand.isUnconstrained()));
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

    if (suffixTruncation != 0) {

      throw new TuplesException("Suffix truncation not supported");
    }

    if (prefix.length > 0) {
      throw new TuplesException("Prefix not supported in UnorderedProjection" +
          "- use TuplesOperations.sort() to provide prefix support");
    }

    operand.beforeFirst(prefix, 0);
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

    return (operand.getVariables().length <= getNumberOfVariables())
        ? operand.hasNoDuplicates() : false;
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean next() throws TuplesException {

    return operand.next();
  }

  //
  // Methods overriding Object
  //

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Object clone() {

    return new UnorderedProjection(this);
  }
}
