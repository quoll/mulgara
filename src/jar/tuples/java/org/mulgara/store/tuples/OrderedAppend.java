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
 * Contributor(s):
 *   DefinablePrefixAnnotation contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
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

// Third party packages
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Logical disjunction. The append is performed by iterating through the
 * operands, presenting to the output the lowest of them. It requires that the
 * operands be union-compatible (i.e. matching variable lists) and have a shared
 * sort order. The "magical" variable used from the Graph can be renamed to be
 * made union compatible with the renameVariables method.
 *
 * @created 2003-02-10
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
public class OrderedAppend extends AbstractTuples {

  /**
   * Logger.
   */
  private final static Logger logger =
      Logger.getLogger(OrderedAppend.class.getName());

  /**
   * The propositions to conjoin.
   */
  protected final Tuples[] operands;

  /**
   * The return value of the {@link #beforeFirst} method.
   */
  private boolean beforeFirst;

  /**
   * Description of the Field
   */
  private final BitSet incompleteBitSet = new BitSet();

  /**
   * Description of the Field
   */
  private int nextOperand;

  /**
   * Description of the Field
   */
  private final RowComparator rowComparator = new DefaultRowComparator();

  /**
   * Prefix set by {@link #beforeFirst} and used by {@link #next}.
   */
  private long[] prefix;

  private boolean prefixDefinable;

  /**
   * Conjoin a list of propositions.
   *
   * @param operands the propositions to conjoin; the order affects efficiency,
   *      but not the logical value of the result
   * @throws IllegalArgumentException if <var>operands</var> is
   *      <code>null</code>
   * @throws TuplesException EXCEPTION TO DO
   */
  public OrderedAppend(Tuples[] operands) throws TuplesException {

    // Validate "operands" parameter
    if (operands == null) {

      throw new IllegalArgumentException("Null \"operands\" parameter");
    }

    // Initialize fields
    this.operands = clone(operands);

    if (operands.length == 0) {

      setVariables(new Variable[] {});
    } else {
      setVariables(operands[0].getVariables());
    }
    prefixDefinable = true;
    for (int i = 0; i < operands.length; i++) {
      prefixDefinable = prefixDefinable &&
          (operands[i].getAnnotation(DefinablePrefixAnnotation.class) != null);
    }
  }

  /**
   * Copy constructor. Used for cloning.
   * @param orderedAppend Original object to clone.
   */
  private OrderedAppend(OrderedAppend orderedAppend) {
    operands = clone(orderedAppend.operands);
    setVariables(orderedAppend.getVariables());
    prefix = orderedAppend.prefix;
  }

  /**
   * Gets the ColumnValue attribute of the OrderedAppend object
   *
   * @param column PARAMETER TO DO
   * @return The ColumnValue value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getColumnValue(int column) throws TuplesException {

    if (nextOperand == -1) {

      throw new TuplesException("No row");
    }

    if ((column < 0) || (column >= getNumberOfVariables())) {

      throw new TuplesException("Invalid column: " + column);
    }

    return operands[nextOperand].getColumnValue(column);
  }

  /**
   * Gets the RowCount attribute of the OrderedAppend object
   *
   * @return The RowCount value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getRowCount() throws TuplesException {
    rowCount = 0;
    for (int i = 0; i < operands.length; i++) {
      rowCount += operands[i].getRowCount();
      if (rowCount < 0)
        return Long.MAX_VALUE;
    }

    return rowCount;
  }

  public long getRowUpperBound() throws TuplesException {
    long bound = 0;

    for (int i = 0; i < operands.length; i++) {
      bound += operands[i].getRowUpperBound();
      if (bound < 0)
        return Long.MAX_VALUE;
    }

    return bound;
  }

  public long getRowExpectedCount() throws TuplesException {
    long bound = 0;

    for (int i = 0; i < operands.length; i++) {
      bound += operands[i].getRowExpectedCount();
      if (bound < 0) return Long.MAX_VALUE;
    }

    return bound;
  }

  public boolean isEmpty() throws TuplesException {
    for (Tuples op : operands) {
      if (!op.isEmpty()) return false;
    }
    return true;
  }

  /**
   * A column is unbound if it's unbound in any of the operands.
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    for (int i = 0; i < operands.length; i++) {
      if (operands[i].isColumnEverUnbound(column)) {
        return true;
      }
    }

    return false;
  }

  public boolean hasNoDuplicates() {
    return false;
  }

  /**
   * @return whether any operand is unconstrained
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean isUnconstrained() throws TuplesException {
    for (int i = 0; i < operands.length; i++) {
      if (operands[i].isUnconstrained()) {
        return true;
      }
    }

    return false;
  }


  public List<Tuples> getOperands() {
    return Arrays.asList(operands);
  }


  //
  // Methods implementing Tuples
  //

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
  public void beforeFirst(long[] prefix, int suffixTruncation)
      throws TuplesException {

    for (int i = 0; i < operands.length; i++) {

      operands[i].beforeFirst(prefix, suffixTruncation);
      incompleteBitSet.set(i);
    }

    beforeFirst = true;
    nextOperand = -1;
    this.prefix = prefix;
  }

  /**
   * METHOD TO DO
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public void close() throws TuplesException {
    close(operands);
  }

  /**
   * Move to the next row satisfying the current prefix and suffix truncation.
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

    if (beforeFirst) {

      beforeFirst = false;

      for (int i = 0; i < operands.length; i++) {
        if (!operands[i].next()) {
          incompleteBitSet.clear(i);
        }
      }
    }
    else if (nextOperand == -1) {
      return false;
    }
    else {
      if (!operands[nextOperand].next()) {
        incompleteBitSet.clear(nextOperand);
      }
    }

    nextOperand = seekLowOperand();

    return nextOperand != -1;
  }

  /**
   * Renames the variables which label the tuples if they have the "magic" names
   * such as "Subject", "Predicate", "Object" and "Meta". This includes the
   * operands of an ordered append.
   *
   * Sets the the current
   *
   * @param constraint PARAMETER TO DO
   */
  public void renameVariables(Constraint constraint) {

    //super.renameVariables(constraint);

    for (int index = 0; index < operands.length; index++) {

      if (logger.isDebugEnabled()) {

        logger.debug("!! Renaming tuples index: " + index);
      }

      operands[index].renameVariables(constraint);
    }

    if (logger.isDebugEnabled()) {

      logger.debug("!! Tuples after rename: " + this.toString());
    }
  }

  /**
   * Returns a copy of the internal variables.
   *
   * @return a copy of the internal variables.
   */
  public Variable[] getVariables() {

    // Container for our variables
    ArrayList<Variable> variablesList = new ArrayList<Variable>();

    // Iterate through the tuples objects and get the variables for each
    for (int index = 0; index < operands.length; index++) {

      // Get the array of variables for the current tuples object
      Variable[] variableArray = operands[index].getVariables();

      // We need to prevent duplicate variables so iterate through the new array
      // and remove duplicates
      for (int i = 0; i < variableArray.length; i++) {

        if (!variablesList.contains(variableArray[i])) {

          // If we don't have the variable already, add it
          variablesList.add(variableArray[i]);
        }
      }
    }

    // Convert the list to an array
    Variable[] newVariables = new Variable[variablesList.size()];
    newVariables = variablesList.toArray(newVariables);

    return newVariables;
  }


  public Annotation getAnnotation(Class<? extends Annotation> annotation) {
    if (annotation == DefinablePrefixAnnotation.class && prefixDefinable) {
      return new DefinablePrefixAnnotation() {
        public void definePrefix(Set<Variable> boundVars) throws TuplesException {
          for (int i = 0; i < operands.length; i++) {
            DefinablePrefixAnnotation annotation = 
                (DefinablePrefixAnnotation)operands[i].getAnnotation(DefinablePrefixAnnotation.class);
            // Note: this should also probably check variable orderings, but this is deferred until
            //   we are doing this more generally.  See bug report logged in mulgara tracking system:
            //   http://mulgara.org/jira/browse/MGR-15
            annotation.definePrefix(boundVars);
          }
        }
      };
    } else {
      return null;
    }
  }


  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Object clone() {
    return new OrderedAppend(this);
  }

  /**
   * @return the index of the lowest operand, or -1 if no operand has further
   *      tuples
   * @throws TuplesException EXCEPTION TO DO
   */
  private int seekLowOperand() throws TuplesException {

    // Find the first incomplete operand
    int lowOperand = incompleteBitSet.nextSetBit(0);

    if (lowOperand == -1) {

      return -1;
    }

    // Find the lowest incomplete operand
    for (int i = lowOperand + 1; i < operands.length; i++) {

      if (incompleteBitSet.get(i) &&
          (rowComparator.compare(operands[lowOperand], operands[i]) > 0)) {

        lowOperand = i;
      }
    }

    return lowOperand;
  }
}
