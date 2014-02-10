/*
 * Copyright 2008 Fedora Commons, Inc.
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
import java.math.BigInteger;
import java.util.*;

// Third party packages
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.ContextOwner;
import org.mulgara.query.filter.Filter;
import org.mulgara.query.filter.value.Bool;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.resolver.spi.TuplesContext;
import org.mulgara.store.tuples.AbstractTuples;

/**
 * Left Join operation.
 *
 * This operation is similar to a subtraction, only it returns every row from the LHS,
 * while returning rows from the RHS when available, and <code>null</code> otherwise.
 *
 * The join is performed by iterating over the lhs, and searching on the
 * RHS for matching rows.  For efficient searching, the RHS must be
 * ordered according to the matching variables.  This class is not responsible for
 * ensuring the sort order of the RHS; that responsibility falls to
 * {@link TuplesOperations#optionalJoin(Tuples, Tuples, Filter, QueryEvaluationContext)}.
 *
 * @created 2008-04-04
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 */
public class LeftJoin extends AbstractTuples implements ContextOwner {

  private static final Logger logger = Logger.getLogger(LeftJoin.class.getName());

  /** The set of tuples to return all row from. */
  protected Tuples lhs;

  /** The set of tuples to add to the lhs. */
  protected Tuples rhs;

  /** The filter to apply. */
  private Filter filter;

  /** The tuples context */
  protected TuplesContext context = null;

  /** A list of context owners that this owner provides the context for. */
  private List<ContextOwner> contextListeners = new ArrayList<ContextOwner>();

  /** The set of variables common to both the lhs and the rhs. */
  protected Set<Variable> commonVars;

  /** An array of the matching variables' columns within the lhs, indexed by the rhs position. */
  protected int[] varMap;

  /** Indicates if the RHS is currently sitting on a match. */
  private boolean rightMatches = false;
  
  /** A collection of the variables on the LHS */
  private ArrayList<Variable> lhsVars;

  /** The offset for indexing into the RHS, while avoiding LHS variables */
  private int rhsOffset;

  /** Indicates that the current row is OK, and {@link #next()} will return true. */
  private boolean currentRowValid = false;

  /** The prefix currently in use on the right */
  private long[] currentRightPrefix = Tuples.NO_PREFIX;

  /**
   * Configure a subtraction operation for lazy evaluation.
   *
   * @param lhs The original tuples, including the rows to be removed.
   * @param rhs The tuples defining the rows to be removed from the lhs.
   * @throws IllegalArgumentException If the <var>lhs</var> and  <var>rhs</var>
   *         contain no variables in common.
   */
  @SuppressWarnings("unchecked")
  LeftJoin(Tuples lhs, Tuples rhs, Filter filter, QueryEvaluationContext queryContext) throws TuplesException, IllegalArgumentException {
    // store the operands
    this.lhs = (Tuples)lhs.clone();
    this.rhs = (Tuples)rhs.clone();
    this.filter = filter;
    if (this.filter == null) this.filter = Bool.TRUE;
    if (this.filter != Bool.TRUE) {
      this.context = new TuplesContext(this, queryContext.getResolverSession());
      this.filter.setContextOwner(this);
    }
    if (this.filter == null) throw new IllegalStateException("Null Filter");

    // get the variables to merge on
    commonVars = Collections.unmodifiableSet((Set<Variable>)TuplesOperations.getMatchingVars(lhs, rhs));

    // This is more common than we expected, so just log a debug message
    if (commonVars.isEmpty()) logger.debug("Tuples should have variables in common for optional join to occur");

    // initialise the mapping of lhs columns to rhs columns
    varMap = new int[commonVars.size()];
    // iterate over the variables to do the mapping
    for (Variable var: commonVars) {
      // get the index of the variable in the rhs
      int si = rhs.getColumnIndex(var);
      // check that it is within the prefix columns. If not, then the rhs is not properly sorted.
      if (si >= varMap.length) {
        String op = "common= " + commonVars.toString();
        op += "; var= " + var + "; index in left= " + si +"; optional= [ ";
        Variable[] v = rhs.getVariables();
        for (int k = 0; k < v.length; k++) {
          op += v[k] + " ";
        }
        op += "]";
        // usually this would be an assertion, but it's too important to miss
        throw new IllegalArgumentException("Subtracted tuples not sorted correctly: " + op);
      }
      // map the rhs index of the variable to the lhs index
      varMap[si] = lhs.getColumnIndex(var);
    }

    // set the variables for this optional conjunction
    lhsVars = new ArrayList<Variable>(Arrays.asList(lhs.getVariables()));
    ArrayList<Variable> vars = (ArrayList<Variable>)lhsVars.clone();
    ArrayList<Variable> rhsVars = new ArrayList<Variable>(Arrays.asList(rhs.getVariables()));
    rhsVars.removeAll(commonVars);
    vars.addAll(rhsVars);
    setVariables(vars);
    
    // set the column offset for indexing into the RHS
    rhsOffset = lhsVars.size() - varMap.length;
    assert rhsOffset >= 0;
  }


  //
  // Methods implementing Tuples
  //

  /** {@inheritDoc} */
  public long getColumnValue(int column) throws TuplesException {
    int nrLeftVars = lhs.getNumberOfVariables();
    if (column < nrLeftVars) return lhs.getColumnValue(column);
    // return the column minus the LHS columns, and then skip over the matching vars
    return rightMatches && testFilter() ? rhs.getColumnValue(column - rhsOffset) : UNBOUND;
  }

  
  /** {@inheritDoc} */
  public long getRawColumnValue(int column) throws TuplesException {
    int nrLeftVars = lhs.getNumberOfVariables();
    if (column < nrLeftVars) return lhs.getColumnValue(column);
    // return the column minus the LHS columns, and then skip over the matching vars
    return rightMatches ? rhs.getColumnValue(column - rhsOffset) : UNBOUND;
  }


  /** {@inheritDoc} */
  public long getRowUpperBound() throws TuplesException {
    BigInteger rowCount = BigInteger.valueOf(lhs.getRowUpperBound());
    rowCount = rowCount.multiply(BigInteger.valueOf(rhs.getRowUpperBound()));
    return rowCount.bitLength() > 63 ? Long.MAX_VALUE : rowCount.longValue();
  }


  /** {@inheritDoc} */
  public long getRowExpectedCount() throws TuplesException {
    // TODO: work out a better expected value. Maybe add about 10%
    return lhs.getRowExpectedCount();
  }


  /** {@inheritDoc} */
  public boolean isEmpty() throws TuplesException {
    return lhs.isEmpty();
  }


  /** {@inheritDoc}  Relies on the lhs of the optional. */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    int nrLeftVars = lhs.getNumberOfVariables();
    if (column < nrLeftVars) return lhs.isColumnEverUnbound(column);
    // If there are rows on the left that are not in the right, then this columns will be unbound

    // Can't know for sure if the column is unbound without iterating through
    if (rhs.isColumnEverUnbound(column - rhsOffset)) return true;
    // err on the side of caution
    return true;
  }


  /** {@inheritDoc} */
  public int getColumnIndex(Variable variable) throws TuplesException {
    if (lhsVars.contains(variable)) return lhs.getColumnIndex(variable);
    return rhs.getColumnIndex(variable) + rhsOffset;
  }


  /**
   * {@inheritDoc}
   * @return Always <code>false</code>.
   */
  public boolean isMaterialized() {
    return false;
  }


  /**
   * {@inheritDoc}
   * If filtering, then this is necessarily false since there could be multiple matches
   * on the right that all filter out.
   */
  public boolean hasNoDuplicates() throws TuplesException {
    return lhs.hasNoDuplicates() && filter == Bool.TRUE;
  }


  /** {@inheritDoc} */
  public RowComparator getComparator() {
    RowComparator lhsComp = lhs.getComparator();
    RowComparator rhsComp = lhs.getComparator();
    // build a new comparator, if both left and right have comparators, else null
    if (lhsComp == null || rhsComp == null) return null;
    return new MergedComparator(lhsComp, rhsComp);
  }


  /** {@inheritDoc} */
  public List<Tuples> getOperands() {
    return Collections.unmodifiableList(Arrays.asList(new Tuples[] {lhs, rhs}));
  }


  /** {@inheritDoc} */
  public boolean isUnconstrained() throws TuplesException {
    return lhs.isUnconstrained();
  }


  /** {@inheritDoc} */
  public void renameVariables(Constraint constraint) {
    lhs.renameVariables(constraint);
    rhs.renameVariables(constraint);
  }


  /**
   * {@inheritDoc}
   * This method matches what it can on the LHS, and saves the rest for later searches
   * on the RHS. Searches on the RHS only happen when the LHS iterates to valid data.
   */
  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    int lhsVars = lhs.getNumberOfVariables();
    // The tail of the prefix is the part that searches on the RHS
    int lhsOnlyVars = (lhsVars - commonVars.size());
    int tailLen = prefix.length - lhsOnlyVars;
    if (tailLen <= 0) {
      // search on the LHS only
      lhs.beforeFirst(prefix, suffixTruncation);
      // store the prefix for use on the right
      currentRightPrefix = Tuples.NO_PREFIX;
    } else {
      // search on the LHS, and do the remainder of the search of the RHS
      lhs.beforeFirst(reduce(prefix, lhsVars), suffixTruncation);
      // store the data to search on the right
      currentRightPrefix = reduce(prefix, lhsOnlyVars, tailLen);
    }
    currentRowValid = false;
    rightMatches = false;
  }


  /** {@inheritDoc} */
  public boolean next() throws TuplesException {
    // check if we are already on a matched line
    if (rightMatches) {
      // we are, so move on the right
      rightMatches = rhs.next();
      // if we moved off on the right, then we need to move on the left
      if (!rightMatches) {
        currentRowValid = lhs.next();
        // now that the left has moved, need to search on the right
        updateRight();
      }
      return currentRowValid;
    }
    // There was no current match on the right, so always move on the left
    currentRowValid = lhs.next();
    // now that the left has moved, need to search on the right
    updateRight();
    return currentRowValid;
  }


  /**
   * Moves the RHS to data matching the LHS, if the LHS is valid.
   * Sets the value of {@link #rightMatches} to indicate if the RHS has matching data.
   * @throws TuplesException If there is an error searching the RHS or moving to the first matching row.
   */
  private void updateRight() throws TuplesException {
    // for a valid row on the left, search on the right
    if (currentRowValid) {
      long[] prefix = calculateRHSPrefix();
      // always doing a bound search, so a null search means no common vars
      if (prefix.length > 0) {
        rhs.beforeFirst(prefix, 0);
        // and see if the rhs data was found
        rightMatches = rhs.next();
      } else {
        // no search required
        assert commonVars.isEmpty();
        rightMatches = false;
      }
    } else {
      // since the left is not valid, neither is the right
      rightMatches = false;
    }
  }


  /**
   * Determine the prefix to use on the RHS, given a possible search already set for this tuples.
   * @return The prefix to be used for finding a current RHS row.
   * @throws TuplesException If the LHS could not be accessed.
   */
  private long[] calculateRHSPrefix() throws TuplesException {
    // if the currentRightPrefix is longer than the matching vars, then just return it
    if (currentRightPrefix.length >= commonVars.size()) return currentRightPrefix;
    // fill in a prefix with the currentRightPrefix
    long[] prefix = new long[commonVars.size()];
    for (int c = 0; c < currentRightPrefix.length; c++) {
      prefix[c] = currentRightPrefix[c];
      assert prefix[c] == lhs.getColumnValue(varMap[c]);
    }
    // finish the prefix with the columns that are supposed to match
    for (int c = currentRightPrefix.length; c < prefix.length; c++) {
      prefix[c] = lhs.getColumnValue(varMap[c]);
    }
    return prefix;
  }


  /**
   * Tests a filter using the current context.
   * @return The test result.
   * @throws QueryException If there was an error accessing data needed for the test.
   */
  private boolean testFilter() {
    // re-root the filter expression to this Tuples
    filter.setContextOwner(this);
    try {
      return filter.test(context);
    } catch (QueryException qe) {
      return false;
    }
  }


  /**
   * Closes all the operands.
   * @throws TuplesException If either the lhs or the rhs can't be closed.
   */
  public void close() throws TuplesException {
    lhs.close();
    rhs.close();
  }


  /**
   * @return {@inheritDoc}
   */
  public Object clone() {
    LeftJoin cloned = (LeftJoin)super.clone();

    // Copy mutable fields by value
    cloned.lhs = (Tuples)lhs.clone();
    cloned.rhs = (Tuples)rhs.clone();
    cloned.context = (context == null) ? null : new TuplesContext(cloned, context);
    if (cloned.filter == null) throw new IllegalStateException("Unexpectedly lost a filter: " + filter);

    return cloned;
  }

  /**
   * Tells a filter what the current context is.
   * @see org.mulgara.query.filter.ContextOwner#getCurrentContext()
   */
  public Context getCurrentContext() {
    return context;
  }


  /**
   * Allows the context to be set manually. This is not expected.
   * @see org.mulgara.query.filter.ContextOwner#setCurrentContext(org.mulgara.query.filter.Context)
   */
  public void setCurrentContext(Context context) {
    if (!(context instanceof TuplesContext)) throw new IllegalArgumentException("LeftJoin can only accept a TuplesContext.");
    this.context = (TuplesContext)context;
    for (ContextOwner l: contextListeners) l.setCurrentContext(context);
  }


  /**
   * This provides a context, and does not need to refer to a parent.
   * @see org.mulgara.query.filter.ContextOwner#getContextOwner()
   */
  public ContextOwner getContextOwner() {
    throw new IllegalStateException("Should never be asking for the context owner of a Tuples");
  }


  /**
   * The owner of the context for a Tuples is never needed, since it is always provided by the Tuples.
   * @see org.mulgara.query.filter.ContextOwner#setContextOwner(org.mulgara.query.filter.ContextOwner)
   */
  public void setContextOwner(ContextOwner owner) {
  }

  /**
   * Adds a context owner as a listener so that it will be updated with its context
   * when this owner gets updated.
   * @param l The context owner to register.
   */
  public void addContextListener(ContextOwner l) {
    contextListeners.add(l);
  }

  //
  // Internal methods and classes
  //


  /**
   * A comparator that is only relevant to the LeftJoin operation. Orders first on the left, then on the right
   * This comparator passes portions of the comparison down to the left and the right.
   */
  class MergedComparator implements RowComparator {

    /** The LHS comparator */
    private RowComparator left;

    /** The RHS comparator */
    private RowComparator right;

    /**
     * Constructs a merging of the two comparators.
     * @param l The comparator for the LHS.
     * @param r The comparator for the RHS.
     */ 
    MergedComparator(RowComparator l, RowComparator r) {
      left = l;
      right = r;
    }

    /**
     * Compares tuples with the presumption that they are both LeftJoin operations.
     * @see org.mulgara.store.tuples.RowComparator#compare(org.mulgara.store.tuples.Tuples, org.mulgara.store.tuples.Tuples)
     */
    public int compare(Tuples first, Tuples second) throws TuplesException {
      if (!(first instanceof LeftJoin) || !(second instanceof LeftJoin)) throw new IllegalArgumentException("Merged Comparators can only operate on LeftJoins");
      int nrVars = lhs.getNumberOfVariables();
      int result = left.compare(new ReducedTuples(first, nrVars), new ReducedTuples(second, nrVars));
      if (result != 0) return result;
      nrVars = rhs.getNumberOfVariables() - commonVars.size();
      return right.compare(new ReducedTuples(first, rhsOffset, nrVars), new ReducedTuples(second, rhsOffset, nrVars));
    }

    /**
     * Compares an array and a tuples, part at a time. Makes the presumption that the
     * tuples is a LeftJoin operation. 
     * @see org.mulgara.store.tuples.RowComparator#compare(long[], org.mulgara.store.tuples.Tuples)
     */
    public int compare(long[] array, Tuples tuples) throws TuplesException {
      if (!(tuples instanceof LeftJoin)) throw new IllegalArgumentException("Merged Comparators can only operate on LeftJoins");
      int nrVars = lhs.getNumberOfVariables();
      int result = left.compare(reduce(array, nrVars), reduce(tuples, nrVars));
      if (result != 0) return result;
      nrVars = rhs.getNumberOfVariables() - commonVars.size();
      return right.compare(reduce(array, rhsOffset, nrVars), reduce(tuples, rhsOffset, nrVars));
    }

    /**
     * Compares two arrays, part at a time.
     * @see org.mulgara.store.tuples.RowComparator#compare(long[], long[])
     */
    public int compare(long[] first, long[] second) throws TuplesException {
      int nrVars = lhs.getNumberOfVariables();
      int result = left.compare(reduce(first, nrVars), reduce(second, nrVars));
      if (result != 0) return result;
      nrVars = rhs.getNumberOfVariables() - commonVars.size();
      return right.compare(reduce(first, rhsOffset, nrVars), reduce(second, rhsOffset, nrVars));
    }

    public boolean equals(Object o) {
      return o instanceof MergedComparator &&
             left.equals(((MergedComparator)o).left) &&
             right.equals(((MergedComparator)o).right);
    }

    /**
     * Added to match {@link #equals(Object)}.
     */
    public int hashCode() {
      return left.hashCode() + 3 * right.hashCode();
    }

  }

  /**
   * Truncates an array to a given width.
   * @param array The array to truncate.
   * @param width The width to reduce to.
   * @return If width is smaller than the array length,
   *   then a truncated array, else the original array.
   */
  private static long[] reduce(long[] array, int width) {
    if (width < array.length) {
      long[] tmp = new long[width];
      System.arraycopy(array, 0, tmp, 0, width);
      array = tmp;
    }
    return array;
  }

  /**
   * Slices an array down to a subarray.
   * @param array The array to truncate.
   * @param offset The offset for the slice.
   * @param width The width of the slice.
   * @return If width is smaller than the array length,
   *   then a truncated array, else the original array.
   */
  private static long[] reduce(long[] array, int offset, int width) {
    if (width < array.length || offset > 0) {
      if (offset + width > array.length) throw new IllegalArgumentException("Cannot slice an array outside of its bounds");
      long[] tmp = new long[width];
      System.arraycopy(array, offset, tmp, 0, width);
      array = tmp;
    }
    return array;
  }

  /**
   * Truncates a Tuples to a given width.
   * @param tuples The tuples to truncate.
   * @param width The width to reduce to.
   * @return A new Tuples which contains the same data as the original, but with fewer columns.
   */
  private static Tuples reduce(Tuples tuples, int width) {
    return new ReducedTuples(tuples, width);
  }

  /**
   * Reduces a tuples to a given subset of the original columns.
   * @param tuples The tuples to truncate.
   * @param offset The offset for the slice.
   * @param width The width of the slice.
   * @return A new Tuples which contains the same data as the original, but with fewer columns.
   */
  private static Tuples reduce(Tuples tuples, int offset, int width) {
    return new ReducedTuples(tuples, offset, width);
  }

  /** Wraps a tuples, reducing its width according to a set of parameters. */
  static class ReducedTuples implements Tuples {

    /** The Tuples to wrap. */
    private Tuples wrapped;

    /** The offset of the variables being exposed. */
    private int offset;

    /** The number of variables in this tuples. */
    private int nrVars;

    /**
     * Wrap a tuples, reducing its width.
     * @param t The tuples to wrap.
     * @param w The width to present.
     */
    ReducedTuples(Tuples t, int w) {
      wrapped = t;
      offset = 0;
      nrVars = w;
    }

    /**
     * Wrap a tuples, reducing its width.
     * @param t The tuples to wrap.
     * @param o the offset to start presenting variables.
     * @param w The width to present.
     */
    ReducedTuples(Tuples t, int o, int w) {
      wrapped = t;
      offset = o;
      nrVars = w;
    }

    /** @see org.mulgara.store.tuples.Tuples#clone() */
    public Object clone() {
      ReducedTuples result;
      try {
        result = (ReducedTuples)super.clone();
      } catch (CloneNotSupportedException e) {
        throw new AssertionError("Unable to clone ReducedTuples");
      }
      return result;
    }

    /**
     * Pass through, but reduce the prefix if it is too long.
     * @see org.mulgara.store.tuples.Tuples#beforeFirst(long[], int)
     */
    public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
      // Unable to manage this if selecting on th RHS of a LeftJoin
      if (offset > 0) throw new UnsupportedOperationException("Unable to perform beforeFirst on an optional sub-tuples");
      wrapped.beforeFirst(reduce(prefix, nrVars), suffixTruncation);
    }

    /** @see org.mulgara.store.tuples.Tuples#getAnnotation(java.lang.Class) */
    public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
      return wrapped.getAnnotation(annotationClass);
    }

    /** @see org.mulgara.store.tuples.Tuples#getColumnIndex(org.mulgara.query.Variable) */
    public int getColumnIndex(Variable variable) throws TuplesException {
      int col = wrapped.getColumnIndex(variable);
      if (col >= nrVars || col < offset) throw new TuplesException("No such variable: " + variable);
      return col - offset;
    }

    /** @see org.mulgara.store.tuples.Tuples#getColumnValue(int) */
    public long getColumnValue(int column) throws TuplesException {
      if (column >= nrVars) throw new TuplesException("Invalid column: " + column);
      return wrapped.getColumnValue(column + offset);
    }

    /** @see org.mulgara.store.tuples.Tuples#getRawColumnValue(int) */
    public long getRawColumnValue(int column) throws TuplesException {
      return getColumnValue(column);
    }

    /** @see org.mulgara.store.tuples.Tuples#getComparator() */
    public RowComparator getComparator() {
      return wrapped.getComparator();
    }

    /** @see org.mulgara.store.tuples.Tuples#getOperands() */
    public List<Tuples> getOperands() {
      return wrapped.getOperands();
    }

    /** @see org.mulgara.store.tuples.Tuples#getRowCount() */
    public long getRowCount() throws TuplesException {
      return wrapped.getRowCount();
    }

    /* (non-Javadoc)
     * @see org.mulgara.query.Cursor#isEmpty()
     */
    public boolean isEmpty() throws TuplesException {
      return wrapped.isEmpty();
    }

    /** @see org.mulgara.store.tuples.Tuples#getVariables() */
    public Variable[] getVariables() {
      Variable[] vars = wrapped.getVariables();
      // cut down the variables to those selected
      if (vars.length > nrVars || offset > 0) {
        assert offset + nrVars <= vars.length;
        Variable[] tmp = new Variable[nrVars];
        System.arraycopy(vars, offset, tmp, 0, nrVars);
        vars = tmp;
      }
      return vars;
    }

    /** @see org.mulgara.store.tuples.Tuples#hasNoDuplicates() */
    public boolean hasNoDuplicates() throws TuplesException {
      return wrapped.hasNoDuplicates();
    }

    /** @see org.mulgara.store.tuples.Tuples#isColumnEverUnbound(int) */
    public boolean isColumnEverUnbound(int column) throws TuplesException {
      return wrapped.isColumnEverUnbound(column);
    }

    /** @see org.mulgara.store.tuples.Tuples#isMaterialized() */
    public boolean isMaterialized() {
      return wrapped.isMaterialized();
    }

    /** @see org.mulgara.store.tuples.Tuples#isUnconstrained() */
    public boolean isUnconstrained() throws TuplesException {
      return wrapped.isUnconstrained();
    }

    /** @see org.mulgara.store.tuples.Tuples#next() */
    public boolean next() throws TuplesException {
      return wrapped.next();
    }

    /** @see org.mulgara.store.tuples.Tuples#renameVariables(org.mulgara.query.Constraint) */
    public void renameVariables(Constraint constraint) {
      wrapped.renameVariables(constraint);
    }

    /** @see org.mulgara.query.Cursor#beforeFirst() */
    public void beforeFirst() throws TuplesException {
      wrapped.beforeFirst();
    }

    /** @see org.mulgara.query.Cursor#close() */
    public void close() throws TuplesException {
      wrapped.close();
    }

    /** @see org.mulgara.query.Cursor#getNumberOfVariables() */
    public int getNumberOfVariables() {
      return nrVars;
    }

    /** @see org.mulgara.query.Cursor#getRowCardinality() */
    public int getRowCardinality() throws TuplesException {
      return wrapped.getRowCardinality();
    }

    /** @see org.mulgara.query.Cursor#getRowUpperBound() */
    public long getRowUpperBound() throws TuplesException {
      return wrapped.getRowUpperBound();
    }
    
    /** @see org.mulgara.query.Cursor#getRowExpectedCount() */
    public long getRowExpectedCount() throws TuplesException {
      return wrapped.getRowExpectedCount();
    }

    /**
     * Required by Tuples.
     */
    public boolean equals(Object o) {
      return (o instanceof Tuples) && AbstractTuples.equals(this, (Tuples)o);
    }

    /**
     * Added to match {@link #equals(Object)}.
     */
    public int hashCode() {
      return TuplesOperations.hashCode(this);
    }
  }
}
