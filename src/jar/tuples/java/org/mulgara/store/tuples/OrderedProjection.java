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

// Locally written packages
import org.apache.log4j.Logger;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Relational projection, eliminating columns and preserving sort order. If you
 * want to reorder columns rather than just eliminating them, use {@link
 * UnorderedProjection} instead.
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
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class OrderedProjection extends AbstractTuples {

  /**
   * Logger.
   *
   */
  private final static Logger logger = Logger.getLogger(OrderedProjection.class);

  /**
   * The proposition to project.
   *
   */
  private final Tuples operand;

  /**
   * Projected variable array.
   *
   */
  private final Variable[] variables;

  /**
   * Array indexed on operand columns, whose values are projected columns.
   *
   */
  private final int[] columnMapping;

  /**
   * Eliminate columns from a {@link Tuples}. This does not eliminate
   * duplicates; {@link DistinctTuples} should be used to produce a formal
   * relational projection.
   *
   * @param operand the tuples to project
   * @param variables the columns to retain
   * @throws IllegalArgumentException if <var>operand</var> is <code>null</code>
   * @throws TuplesException Error while accessing the operand data.
   */
  OrderedProjection(Tuples operand, Collection<Variable> variables)
      throws TuplesException {

    // Validate "operand" parameter
    if (operand == null) throw new IllegalArgumentException("Null \"operand\" parameter");

    // Validate "variables" parameter
    if (variables == null) {

      throw new IllegalArgumentException("Null \"variables\" parameter");
    }

    // Determine which columns haven't been projected away
    Variable[] operandVariables = operand.getVariables();
    List<Integer> columnMappingList = new ArrayList<Integer>(operandVariables.length);
    List<Variable> variableList = new ArrayList<Variable>(operandVariables.length);

    for (int i = 0; i < operandVariables.length; i++) {
      if (variables.contains(operandVariables[i])) {
        variableList.add(operandVariables[i]);
        columnMappingList.add(new Integer(i));
      }
    }

    // Initialize fields
    this.operand = (Tuples)operand.clone();
    this.variables = variableList.toArray(new Variable[variableList.size()]);

    // Create column mapping
    columnMapping = new int[columnMappingList.size()];

    for (int i = 0; i < columnMapping.length; i++) {
      columnMapping[i] = ( (Integer) columnMappingList.get(i)).intValue();
    }

    assert columnMapping.length == this.variables.length:
        "Column mapping length: " +
        columnMapping.length + " , variables: " + this.variables.length;
  }

  /**
   * Cloning constructor.
   *
   * @param parent PARAMETER TO DO
   */
  private OrderedProjection(OrderedProjection parent) {

    operand = (Tuples) parent.operand.clone();
    variables = parent.variables;
    columnMapping = parent.columnMapping;
  }

  /**
   * Gets the ColumnValue attribute of the OrderedProjection object
   *
   * @param column PARAMETER TO DO
   * @return The ColumnValue value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getColumnValue(int column) throws TuplesException {

    assert(column >= 0) && (column < variables.length):"Invalid column " +
        column;

    return operand.getColumnValue(columnMapping[column]);
  }

  /**
   * Gets the Comparator attribute of the OrderedProjection object
   *
   * @return The Comparator value
   */
  public RowComparator getComparator() {

    if (operand.getComparator() == null) {

      return null;
    }
    else {

      logger.warn("Discarding sort order for " + operand);

      return null;
    }
  }

  /**
   * Gets the RowCount attribute of the OrderedProjection object
   *
   * @return The RowCount value
   * @throws TuplesException Error on underlying data
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
   * Gets the Variables attribute of the OrderedProjection object
   *
   * @return The Variables value
   */
  public Variable[] getVariables() {

    return variables;
  }

  /**
  * A column may be unbound if it's unbound in the projection operand.
  */
  public boolean isColumnEverUnbound(int column) throws TuplesException {

    assert(column >= 0) && (column < variables.length):"Invalid column " +
        column;

    return operand.isColumnEverUnbound(columnMapping[column]);
  }

  /**
   * Gets the Materialized attribute of the OrderedProjection object
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

    if (suffixTruncation != 0) {

      throw new TuplesException("Suffix truncation not implemented");
    }

    if (logger.isDebugEnabled()) {

      logger.debug("Beware, OrderedProjection's next() method is only correct " +
          "for the graph tuples.");
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

    return (operand.getVariables().length == variables.length)
        ? operand.hasNoDuplicates() : false;
  }

  /**
   * @return RETURNED VALUE TO DO
   * @deprecated This method works to remove the $_from column from graph
   *      tuples, but only because of the special properties of the metanode
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

    return new OrderedProjection(this);
  }
}
