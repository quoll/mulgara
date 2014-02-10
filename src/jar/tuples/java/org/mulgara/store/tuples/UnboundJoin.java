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
 *  Various bug fixes copyright Netymon Pty Ltd (info@netymon.com) under
 *  contract to The Topaz Foundation (info@topazproject.org)
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.store.tuples;

// Java 2 standard packages
import java.math.BigInteger;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.tuples.AbstractTuples;

/**
 * Logical conjunction implemented as a relational join operation.
 *
 * The join is performed using a series of nested loops, with the
 * lower-indexed elements of the {@link #operands} array forming the outer loops
 * and the higher-indexed forming the inner loops.  If the sort ordering of the
 * operand columns is such that it can be taken advantage of, this can be very
 * efficient.  If not, it degrades to the equivalent of an inner-outer loop
 * join (Cartesian product).  This class is not responsible for optimizing the
 * order of the operands presented to it; that responsibility falls to
 * {@link TuplesOperations#join}.
 *
 * @created 2003-09-01
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/03/07 19:42:40 $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class UnboundJoin extends AbstractTuples {

  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(UnboundJoin.class.getName());

  /**
   * Version of {@link #operandBinding}} including only columns to the left of
   * the first unbound column.
   */
  protected long[][] operandBindingPrefix;

  /**
   * For each column of the joined result, which operand contains the first
   * occurrence of that variable.
   */
  protected int[] mapOperand;

  /**
   * For each column of the joined result, which column of the operand
   * determined by {@link #mapOperand} contains the first occurrence of that
   * variable.
   */
  protected int[] mapColumn;

  /**
   * Magic value within the {@link #fooOperand} array, indicating that a column
   * is bound to one of the columns of the <var>prefix</var> parameter to
   * {@link #next}.
   */
  protected static final int PREFIX = -1;

  /**
   * For each column of each operand, which operand contains the first
   * occurrence of that variable, or {@link #PREFIX} if the prefix specified
   * to {@link #next} contains the occurrence.
   */
  protected int[][] fooOperand;

  /**
   * For each column of each operand, which column of the operand determined by
   * {@link #fooOperand} contains the first occurrence of that variable, or if
   * the corresponding value of {@link #fooOperand} is {@link #PREFIX}, which
   * column of the prefix specified to {@link #next} contains the occurrence.
   */
  protected int[][] fooColumn;

  /**
   * Whether each column of this instance might contain {@link #UNBOUND} rows.
   */
  protected boolean[] columnEverUnbound;

  /**
   * The propositions to conjoin.
   */
  protected Tuples[] operands;

  /**
   * The required values of the columns of each operand. A value of {@link
   * Tuples#UNBOUND} indicates that the column is free to vary.
   */
  protected long[][] operandBinding;

  /**
   * For each operand, for each variable, which output column contains the same variable.
   */
  protected int[][] operandOutputMap;

  /**
   * Do any of the operands with variables matching this output variable contain UNBOUND?
   */
  protected boolean[][] columnOperandEverUnbound;

  /**
   * Flag indicating that the cursor is before the first row.
   */
  protected boolean isBeforeFirst = true;

  /**
   * Flag indicating that the cursor is after the last row.
   */
  protected boolean isAfterLast = false;

  /**
   * Do any of the operands contain duplicates.  Used to shortcircuit hasNoDuplicates.
   */
  protected boolean operandsContainDuplicates;

  /**
   * The prefix of the index.
   */
  protected long[] prefix = null;

  /**
   * The variable groups formed in this operation. If more than one there will be a cartesian product in the result.
   */
  protected List<VarGroup> varGroups = null;

  /**
   * Conjoin a list of propositions.
   *
   * @param operands the propositions to conjoin; the order affects efficiency,
   *      but not the logical value of the result
   * @throws IllegalArgumentException if <var>operands</var> is
   *                                  <code>null</code>
   * @throws TuplesException EXCEPTION TO DO
   */
  UnboundJoin(Tuples[] operands) throws TuplesException {
    // Validate "operands" parameter
    if (operands == null) {
        throw new IllegalArgumentException("Null \"operands\" parameter");
    }

    // Initialize fields
    this.operands = clone(operands);
    operandBinding = new long[operands.length][];
    operandBindingPrefix = new long[operands.length][];
    this.operandsContainDuplicates = false;
    for (int i = 0; i < operands.length; i++) {
      // Debug
      if (logger.isDebugEnabled()) {
        logger.debug("Operands " + i + " : " + operands[i]);
        logger.debug("Operands variables " + i + " : " + Arrays.asList(operands[i].getVariables()));
        logger.debug("Operands types " + i + " : " + operands[i].getClass());
      }
      operandBinding[i] = new long[operands[i].getVariables().length];
      if (!operands[i].hasNoDuplicates()) {
        this.operandsContainDuplicates = true;
      }
    }

    fooOperand = new int[operands.length][];
    fooColumn = new int[operands.length][];
    operandOutputMap = new int[operands.length][];

    // Calculate the variables present and their mappings from operand
    // columns to result columns
    List<Variable> variableList = new ArrayList<Variable>();
    List<Integer> mapOperandList = new ArrayList<Integer>();
    List<Integer> mapColumnList = new ArrayList<Integer>();
    List<Integer> fooOperandList = new ArrayList<Integer>();
    List<Integer> fooColumnList = new ArrayList<Integer>();

    for (int i = 0; i < operands.length; i++) {
      fooOperandList.clear();
      fooColumnList.clear();

      Variable[] operandVariables = operands[i].getVariables();

      operandOutputMap[i] = new int[operandVariables.length];

      for (int j = 0; j < operandVariables.length; j++) {
        int k = variableList.indexOf(operandVariables[j]);

        if (k == -1) {
          mapOperandList.add(new Integer(i));
          mapColumnList.add(new Integer(j));
          fooOperandList.add(new Integer(PREFIX));
          fooColumnList.add(new Integer(variableList.size()));
          variableList.add(operandVariables[j]);
          operandOutputMap[i][j] = j;
        } else {
          fooOperandList.add(mapOperandList.get(k));
          fooColumnList.add(mapColumnList.get(k));
          operandOutputMap[i][j] = k;
        }
      }

      // Convert per-operand lists into arrays
      assert fooOperandList.size() == fooColumnList.size();
      fooOperand[i] = new int[fooOperandList.size()];
      fooColumn[i] = new int[fooColumnList.size()];

      for (int j = 0; j < fooOperand[i].length; j++) {
        fooOperand[i][j] = ((Integer) fooOperandList.get(j)).intValue();
        fooColumn[i][j] = ((Integer) fooColumnList.get(j)).intValue();
      }
    }

    // Convert column mappings from lists to arrays
    setVariables(variableList);

    mapOperand = new int[mapOperandList.size()];
    mapColumn = new int[mapColumnList.size()];

    for (int i = 0; i < mapOperand.length; i++) {
      mapOperand[i] = ((Integer) mapOperandList.get(i)).intValue();
      mapColumn[i] = ((Integer) mapColumnList.get(i)).intValue();
    }

    // Determine which columns are ever unbound
    columnEverUnbound = new boolean[variableList.size()];
    columnOperandEverUnbound = new boolean[operands.length][variableList.size()];
    Arrays.fill(columnEverUnbound, true);

    for (int i = 0; i < operands.length; i++) {
      Arrays.fill(columnOperandEverUnbound[i], false);
      Variable[] variables = operands[i].getVariables();
      for (int j = 0; j < variables.length; j++) {
        if (!operands[i].isColumnEverUnbound(j)) {
          columnEverUnbound[getColumnIndex(variables[j])] = false;
        } else {
          columnOperandEverUnbound[i][getColumnIndex(variables[j])] = true;
        }
      }
    }

    buildVarGroups();
  }

  /**
   * @return {@inheritDoc}  This occurs if and only if every one of the
   *   {@link #operands} is unconstrained.
   * @throws TuplesException {@inheritDoc}
   */
  public boolean isUnconstrained() throws TuplesException {
    for (int i = 0; i < operands.length; i++) {
      if (!operands[i].isUnconstrained()) {
        return false;
      }
    }

    return true;
  }

  public List<Tuples> getOperands() {
    return Arrays.asList(operands);
  }

  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    if (prefix == null) {
      throw new IllegalArgumentException("Null \"prefix\" parameter");
    }

    if (suffixTruncation != 0) {
      throw new TuplesException("Suffix truncation not implemented");
    }

    assert operands != null;
    assert operandBinding != null;

    isBeforeFirst = true;
    isAfterLast = false;
    this.prefix = prefix;
  }

  //
  // Methods implementing Tuples
  //

  /**
   * @param column {@inheritDoc}
   * @return {@inheritDoc}
   * @throws TuplesException {@inheritDoc}
   */
  public long getColumnValue(int column) throws TuplesException {
    if ((column < 0) || (column >= getNumberOfVariables())) {
      throw new TuplesException("Invalid column: " + column);
    }

    long result = operands[mapOperand[column]].getColumnValue(mapColumn[column]);
    if (result != Tuples.UNBOUND) {
      return result;
    }

    // Brute force search for a bound instance of variable in operands.
    // Note: No operands to the left of the mapOperand[column] contain desired variable.
    Variable desired = getVariables()[column];
    for (int i = mapOperand[column] + 1; i < operands.length; i++) {
      Variable[] v = operands[i].getVariables();
      for (int j = 0; j < v.length; j++) {
        if (v[j].equals(desired)) {
          result = operands[i].getColumnValue(j);
          if (result != Tuples.UNBOUND) {
            return result;
          }
        }
      }
    }

    return Tuples.UNBOUND;
  }

  /**
   * @return {@inheritDoc}  This is estimated as the size of the Cartesian
   *   product, by multiplying the row counts of all the {@link #operands}.
   * @throws TuplesException {@inheritDoc}
   */
  public long getRowUpperBound() throws TuplesException {
    if (operands.length == 0) return 0;
    if (operands.length == 1) return operands[0].getRowUpperBound();

    BigInteger rowCount = BigInteger.valueOf(operands[0].getRowUpperBound());

    for (int i = 1; i < operands.length; i++) {
      rowCount = rowCount.multiply(BigInteger.valueOf(operands[i].getRowUpperBound()));
      if (rowCount.bitLength() > 63)
        return Long.MAX_VALUE;
    }

    return rowCount.longValue();
  }

  /**
   * @return {@inheritDoc}  This is estimated as the size of the minumum
   *         of the row counts of all the {@link #operands}.
   * @throws TuplesException {@inheritDoc}
   */
  public long getRowExpectedCount() throws TuplesException {
    if (operands.length == 0) return 0;
    if (operands.length == 1) return operands[0].getRowExpectedCount();

    // simple joined group. Get the minimum as a guess.
    if (varGroups.size() == 1) {
      long result = operands[0].getRowExpectedCount();
      for (int i = 1; i < operands.length; i++) {
        result = Math.min(result, operands[i].getRowExpectedCount());
      }
      return result;
    } else {
      // cartesian product. Get the simple joins, and multiply.
      BigInteger rowCount = null;
      for (VarGroup vg: varGroups) {
        // calculate the size of this group
        List<Integer> ops = vg.getOps();
        long groupResult = operands[ops.get(0)].getRowExpectedCount();
        for (int i = 1; i < ops.size(); i++) {
          groupResult = Math.min(groupResult, operands[ops.get(i)].getRowExpectedCount());
        }
        // merge the current group into the running total
        if (rowCount == null) rowCount = BigInteger.valueOf(groupResult);
        else rowCount = rowCount.multiply(BigInteger.valueOf(groupResult));
      }
      return (rowCount == null) ? 0L : rowCount.longValue();
    }
  }

  public boolean isEmpty() throws TuplesException {
    for (Tuples op : operands) {
      if (op.isEmpty()) return true;
    }
    return false;
  }

  public boolean isColumnEverUnbound(int column) throws TuplesException {
    try {
      return columnEverUnbound[column];
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new TuplesException("No such column " + column, e);
    }
  }

  /**
   * @return {@inheritDoc}
   * @throws TuplesException {@inheritDoc}
   */
  public boolean next() throws TuplesException {
    // Validate parameters
    if (prefix == null) {
      throw new IllegalArgumentException("Null \"prefix\" parameter");
    }

    // Short-circuit execution if this tuples' cursor is after the last row
    if (isAfterLast) {
      return false;
    }

    if (isBeforeFirst) {
      // Flag that we're no longer before the first row
      isBeforeFirst = false;

      // The first row has to be advanced from leftmost to rightmost operand in
      // order to initialize the leftward dependencies of the operand prefixes
      for (int i = 0; i < operands.length; i++) {
        updateOperandPrefix(i);
        operands[i].beforeFirst(operandBindingPrefix[i], 0);

        if (!advance(i)) {
          return false;
        }
      }

      return true;
    } else {
      // We know at this point that we're on a row satisfying the current
      // prefix.  Advance the rightmost operand and let rollover do any
      // right-to-left advancement required
      boolean b = advance(operands.length - 1);
      assert b || isAfterLast;
      return b;
    }
  }

  public boolean hasNoDuplicates() {
    return operandsContainDuplicates == false;
  }

  /**
   * Closes all the {@link #operands}.
   *
   * @throws TuplesException  if any of the {@link #operands} can't be closed
   */
  public void close() throws TuplesException {
    close(operands);
  }

  /**
   * @return {@inheritDoc}
   */
  public Object clone() {
    UnboundJoin cloned = (UnboundJoin)super.clone();

    // Copy immutable fields by reference
    cloned.operandBinding = operandBinding;
    cloned.operandBindingPrefix = operandBindingPrefix;
    cloned.mapOperand = mapOperand;
    cloned.mapColumn = mapColumn;
    cloned.fooOperand = fooOperand;
    cloned.fooColumn = fooColumn;
    cloned.prefix = prefix;

    // Copy mutable fields by value
    cloned.operands = clone(operands);
    cloned.isBeforeFirst = isBeforeFirst;
    cloned.isAfterLast = isAfterLast;

    return cloned;
  }

  /**
   * Get the number of groups in this join, based on their shared variables.
   * This indicates the number of cartesian products required.
   * @return The number of variable groups discovered between the operands
   */
  int getNrGroups() {
    return varGroups.size();
  }

  //
  // Internal methods
  //

  /**
   * Calculate the correct value for one of the elements of {@link
   * #operandBinding} and its corresponding {@link #operandBindingPrefix}. This
   * method has no return value, only side-effects upon {@link #operandBinding}
   * and {@link #operandBindingPrefix}.
   *
   * @param i  the index of the element in the {@link #operandBinding} array to
   *           calculate
   * @throws TuplesException  if the {@link #operands} can't be accessed
   */
  private void updateOperandPrefix(int i) throws TuplesException {
    assert i >= 0;
    assert i < operandBinding.length;

    for (int j = 0; j < operandBinding[i].length; j++) {
      if (fooOperand[i][j] == PREFIX) {
        // Variable first bound to a next method parameter prefix column passed to beforeFirst.
        operandBinding[i][j] = (fooColumn[i][j] < prefix.length) ? prefix[fooColumn[i][j]] : Tuples.UNBOUND;
      } else {
        // Variable first bound to a leftward operand column
        operandBinding[i][j] = operands[fooOperand[i][j]].getColumnValue(fooColumn[i][j]);
      }
    }

    // Determine the length of the advancement prefix
    int prefixLength = 0;
    while ((prefixLength < operandBinding[i].length) &&
        (operandBinding[i][prefixLength] != Tuples.UNBOUND) &&
        (columnOperandEverUnbound[i][operandOutputMap[i][prefixLength]] == false)) {
      prefixLength++;
    }

    assert prefixLength >= 0;
    assert prefixLength <= operandBinding[i].length;

    // Generate the advancement prefix
    assert operandBindingPrefix != null;

    if ((operandBindingPrefix[i] == null) || (operandBindingPrefix[i].length != prefixLength)) {
      operandBindingPrefix[i] = new long[prefixLength];
    }

    System.arraycopy(operandBinding[i], 0, operandBindingPrefix[i], 0, prefixLength);
  }

  /**
   * Advance one of the joined operands.
   *
   * @param i  the index of the operand to advance
   * @return whether a row was found to satisfy
   * @throws TuplesException if the {@link #operands} can't be accessed
   */
  private final boolean advance(int i) throws TuplesException {
    assert i >= 0;
    assert i < operands.length;
    assert!isAfterLast;

    B:while (true) {
      if (!operands[i].next()) {
        // Roll this column...
        if (i == 0) {
          isAfterLast = true;
          prefix = null;
          return false;
        } else {
          // roll the leftward row
          if (!advance(i - 1)) {
            return false;
          }

          // reset the current row
          updateOperandPrefix(i);
          operands[i].beforeFirst(operandBindingPrefix[i], 0);

          continue B;
        }
      }

      // Check that any suffix conditions are satisfied
      for (int j = operandBindingPrefix[i].length; j < operandBinding[i].length; j++) {
        if ((operandBinding[i][j] != Tuples.UNBOUND) &&
            (operandBinding[i][j] != operands[i].getColumnValue(j)) &&
            (operands[i].getColumnValue(j) != Tuples.UNBOUND)) {
          continue B;
        }
      }

      return true;
    }
  }


  /**
   * Creates the groupings of variables formed during this join.
   * An inner join will form if there is just one group.
   */
  void buildVarGroups() {
    varGroups = new LinkedList<VarGroup>();

    // go over all the operands
    G: for (int i = 0; i < operands.length; i++) {
      Variable[] vars = operands[i].getVariables();
      // test if any group already matches this operand
      for (VarGroup v: varGroups) {
        if (v.joinsTo(vars)) {
          // found a match, so add it
          v.addOperand(i);
          // this may join in other groups, so test
          Iterator<VarGroup> vgi = varGroups.iterator();
          while (vgi.hasNext()) {
            VarGroup ov = vgi.next();
            // don't test if this group joins to itself
            if (ov == v) continue;
            if (v.joinsTo(ov)) {
              // groups join, so merge them
              v.merge(ov);
              vgi.remove();
            }
          }
          // we've matched this operand in, so move to the next operand
          continue G;
        }
      }
      // no matches, so create a new group
      varGroups.add(new VarGroup(i));
    }
  }


  /**
   * A class to record a group of variables and the operands they are associated with.
   */
  class VarGroup {
    /** The variables for the group */
    HashSet<Variable> variables = new HashSet<Variable>();

    /** The operands this group's variables can be found in */
    ArrayList<Integer> opList = new ArrayList<Integer>();

    /**
     * Create a group, starting with a given operand.
     * @param opIndex The index of the operand to seed the group with.
     */
    public VarGroup(int opIndex) {
      addOperand(opIndex);
    }


    /**
     * Adds a new operand's variables to the group.
     * @param i The index of the operand to add.
     */
    public void addOperand(int i) {
      assert !opList.contains(operands[i]);
      opList.add(i);
      for (Variable v: operands[i].getVariables()) {
        variables.add(v);
      }
    }


    /**
     * Adds another group to this one, based on shared variables.
     * @param v The other variable group to merge.
     */
    @SuppressWarnings("unchecked")
    public void merge(VarGroup v) {
      // check that some variables are shared
      assert ((HashSet<Variable>)variables.clone()).removeAll(v.variables);
      // check that no operands are shared
      assert !((ArrayList<Integer>)opList.clone()).removeAll(v.opList);
      variables.addAll(v.variables);
      opList.addAll(v.opList);
    }


    /**
     * Tests if this group joins to a given set of variables.
     * @param vars An array of variables to test.
     * @return <code>true</code> if the variables join to this group.
     */
    public boolean joinsTo(Variable[] vars) {
      for (Variable v: vars) {
        if (variables.contains(v)) return true;
      }
      return false;
    }


    /**
     * Tests if this group joins to another group.
     * @param og The other group.
     * @return <code>true</code> if the groups share variables.
     */
    @SuppressWarnings("unchecked")
    public boolean joinsTo(VarGroup og) {
      return ((HashSet<Variable>)variables.clone()).removeAll(og.variables);
    }


    /**
     * Get the list of operands for this group.
     * @return The operand list.
     */
    public List<Integer> getOps() {
      return opList;
    }
  }
}
