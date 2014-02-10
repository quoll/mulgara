/*
 * Copyright 2010 Duraspace, Inc.
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

// Java 2 standard packages
import java.util.*;

// Locally written packages
import org.apache.log4j.Logger;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.util.functional.C;

/**
 * Projection to include new columns that will be unbound. This is a thin wrapper
 * and will maintain almost all the properties of the wrapped Tuples.
 *
 * @created 2010-01-06
 * @author Paul Gearon
 * @copyright &copy; 2010 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
class ExpandedProjection extends AbstractTuples {

  /** Logger. */
  private final static Logger logger = Logger.getLogger(ExpandedProjection.class);

  /** The proposition to project. */
  private final Tuples operand;

  /** Projected variable array. */
  private final Variable[] variables;

  /** Width of the original tuples. */
  private final int opWidth;

  /**
   * Eliminate columns from a {@link Tuples}. This does not eliminate
   * duplicates; {@link DistinctTuples} should be used to produce a formal
   * relational projection.
   *
   * @param operand the tuples to project
   * @param newVars the columns to add
   * @throws IllegalArgumentException if <var>operand</var> is <code>null</code>
   */
  ExpandedProjection(Tuples operand, List<Variable> newVars) {

    assert operand != null;
    assert newVars != null;

    assert !newVars.isEmpty();

    // get the existing a new variables
    Variable[] opVars = operand.getVariables();

    assert C.intersect(newVars, opVars).isEmpty();

    if (logger.isDebugEnabled() && newVars.isEmpty()) {
      logger.debug("No extra variables in tuples expansion");
    }

    // Initialize fields
    opWidth = opVars.length;
    this.operand = (Tuples)operand.clone();
    this.variables = new Variable[opWidth + newVars.size()];

    // copy the original columns to the start of the variables array
    System.arraycopy(opVars, 0, this.variables, 0, opWidth);
    // copy the new "unbound" columns to the end of the variables array
    int i = opWidth;
    for (Variable v: newVars) this.variables[i++] = v;
    assert i == this.variables.length;
  }

  /**
   * Cloning constructor.
   * @param parent The original Tuples to be cloned from
   */
  private ExpandedProjection(ExpandedProjection parent) {
    operand = (Tuples)parent.operand.clone();
    variables = parent.variables;
    opWidth = parent.opWidth;
  }

  /**
   * Gets the ColumnValue attribute of the OrderedProjection object
   * @param column Specifies the column number to get the value for.
   * @return The value for the variable binding, unbound if in an expanded column.
   * @throws TuplesException Error accessing the underlying Tuples
   */
  public long getColumnValue(int column) throws TuplesException {
    assert (column >= 0) && (column < variables.length) : "Invalid column " + column;
    return column < opWidth ? operand.getColumnValue(column) : UNBOUND;
  }

  /**
   * Gets the Comparator attribute of the OrderedProjection object
   * @return The Comparator value
   */
  public RowComparator getComparator() {
    return operand.getComparator();
  }

  /**
   * Gets the RowCount attribute of the OrderedProjection object
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

  public int getRowCardinality() throws TuplesException {
    return operand.getRowCardinality();
  }

  public boolean isEmpty() throws TuplesException {
    return operand.isEmpty();
  }

  /**
   * Gets the Variables attribute of the OrderedProjection object
   * @return The Variables value
   */
  public Variable[] getVariables() {
    return variables;
  }

  /**
  * A column may be unbound if it's unbound in the projection operand.
  */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    assert (column >= 0) && (column < variables.length) : "Invalid column " + column;
    // yes there's a boolean expression for the following, but this is easier to read
    return column < opWidth ? operand.isColumnEverUnbound(column) : true;
  }

  /**
   * Gets the Materialized attribute of the OrderedProjection object
   * @return The Materialized value
   */
  public boolean isMaterialized() {
    return operand.isMaterialized();
  }

  /**
   * @return whether every operand is unconstrained
   * @throws TuplesException Error in the underlying data
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
   * Go the the first binding that matches a given prefix.
   * @param prefix A pattern specifying the first thing to test against.
   * @param suffixTruncation Not accepted. Must be 0.
   * @throws TuplesException Error in the underlying Tuples, or a non-zero suffixTruncation.
   */
  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    if (suffixTruncation != 0) throw new TuplesException("Suffix truncation not implemented");
    // truncate the prefix if it is wider than the first operand. It can't match to UNBOUND anyway.
    if (prefix.length > opWidth) {
      long[] tmp = prefix;
      prefix = new long[opWidth];
      System.arraycopy(tmp, 0, prefix, 0, opWidth);
    }
    operand.beforeFirst(prefix, 0);
  }

  /**
   * Closes this resource and all attached resources.
   * @throws TuplesException Error in the underlying tuples.
   */
  public void close() throws TuplesException {
    operand.close();
  }

  /**
   * Tests if this tuples has duplicate rows
   * @return <code>true</code> iff the underyling tuples has no duplicates.
   * @throws TuplesException Error in the underlying Tuples
   */
  public boolean hasNoDuplicates() throws TuplesException {
    return operand.hasNoDuplicates();
  }

  /**
   * Move to the next binding on this Tuples.
   * @return <code>true</code> if there is more data to move to.
   * @throws TuplesException Error in the udnerlying Tuples
   */
  public boolean next() throws TuplesException {
    return operand.next();
  }

  //
  // Methods overriding Object
  //

  /**
   * Clones this object
   * @return A new and identical tuples
   */
  public Object clone() {
    return new ExpandedProjection(this);
  }
}
