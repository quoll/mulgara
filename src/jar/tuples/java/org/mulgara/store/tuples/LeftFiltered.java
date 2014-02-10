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
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.resolver.spi.TuplesContext;
import org.mulgara.store.tuples.AbstractTuples;

/**
 * Left Filtering operation.
 *
 * This operation is used for the case where an OPTIONAL join is being filtered
 * based on variables that appear in both the LHS and the RHS of the OPTIONAL.
 * 
 * According to SPARQL:
 * Diff(Ω1, Ω2, expr) = { μ | μ in Ω1 such that for all μ′ in Ω2,
 *    either μ and μ′ are not compatible or μ and μ' are compatible and
 *    expr(merge(μ, μ')) has an effective boolean value of false }
 * http://www.w3.org/TR/rdf-sparql-query/#defn_algDiff
 *
 * In this case, no variables are common, which simplifies our situation to being
 * compatible (since all rows are compatible when no variables are shared). So
 * we just need every μ from Ω1 where every μ' yields an expression of false.

 * The join is performed by iterating over the lhs, and searching on the
 * RHS for true rows. If one is found, then the lhs must iterate again.
 *
 * @created 2009-12-18
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 */
public class LeftFiltered extends AbstractTuples implements ContextOwner {

  private static final Logger logger = Logger.getLogger(LeftFiltered.class.getName());

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

  /** A collection of the variables on the LHS */
  private ArrayList<Variable> lhsVars;

  /** The offset for indexing into the RHS, while avoiding LHS variables */
  private int rhsOffset;

  /** Indicates that the current row is OK, and {@link #next()} will return true. */
  private boolean currentRowValid = false;

  /**
   * Configure a filtering join on the left hand side.
   *
   * @param lhs The original tuples, including the rows to be removed.
   * @param rhs The tuples to be joined in cross product for testing.
   * @param filter The filter that must return FALSE for everything in order to have a LHS row returned.
   * @throws IllegalArgumentException If the <var>lhs</var> and  <var>rhs</var>
   *         contain variables in common.
   */
  @SuppressWarnings("unchecked")
  LeftFiltered(Tuples lhs, Tuples rhs, Filter filter, QueryEvaluationContext queryContext) throws TuplesException, IllegalArgumentException {
    if (logger.isDebugEnabled()) {
      logger.debug("Filtering " + lhs + " by " + rhs + " with expression=" + filter);
    }
    // store the operands
    this.lhs = (Tuples)lhs.clone();
    this.rhs = (Tuples)rhs.clone();
    this.filter = filter;
    if (this.filter == null || filter.getVariables().size() == 0) throw new IllegalArgumentException("No need to filter on unfiltered data");
    this.context = new TuplesContext(this, queryContext.getResolverSession());
    this.filter.setContextOwner(this);

    // get the variables to merge on
    Set<Variable> commonVars = Collections.unmodifiableSet((Set<Variable>)TuplesOperations.getMatchingVars(lhs, rhs));

    // This is more common than we expected, so just log a debug message
    if (!commonVars.isEmpty()) throw new IllegalArgumentException("Cannot left filter when data has non-trivial compatability.");

    // set the variables for this optional conjunction
    lhsVars = new ArrayList<Variable>(Arrays.asList(lhs.getVariables()));
    ArrayList<Variable> vars = (ArrayList<Variable>)lhsVars.clone();
    ArrayList<Variable> rhsVars = new ArrayList<Variable>(Arrays.asList(rhs.getVariables()));
    vars.addAll(rhsVars);
    setVariables(vars);
    
    // set the column offset for indexing into the RHS
    rhsOffset = lhsVars.size();
    assert rhsOffset > 0;
  }


  //
  // Methods implementing Tuples
  //

  /** {@inheritDoc} */
  public long getColumnValue(int column) throws TuplesException {
    int nrLeftVars = lhs.getNumberOfVariables();
    return (column < nrLeftVars) ? lhs.getColumnValue(column) : UNBOUND;
  }

  
  /** {@inheritDoc} */
  public long getRawColumnValue(int column) throws TuplesException {
    int nrLeftVars = lhs.getNumberOfVariables();
    if (column < nrLeftVars) return lhs.getColumnValue(column);
    return rhs.getColumnValue(column - rhsOffset);
  }


  /** {@inheritDoc} */
  public long getRowUpperBound() throws TuplesException {
    return lhs.getRowUpperBound();
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
    return (column >= nrLeftVars) || lhs.isColumnEverUnbound(column);
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
   */
  public boolean hasNoDuplicates() throws TuplesException {
    return lhs.hasNoDuplicates();
  }


  /** {@inheritDoc} */
  public RowComparator getComparator() {
    return lhs.getComparator();
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
    int tailLen = prefix.length - lhsVars;
    if (tailLen <= 0) {
      // search on the LHS only
      lhs.beforeFirst(prefix, suffixTruncation);
    } else {
      // looking for something that doesn't exist
      lhs.beforeFirst(new long[] {-1}, suffixTruncation);
    }
    currentRowValid = false;
  }


  /** {@inheritDoc} */
  public boolean next() throws TuplesException {
    while ((currentRowValid = lhs.next())) {
      if (!testRhs()) break;
    }
    return currentRowValid;
  }

  /**
   * Tests if any row on the right is true
   * @return true if any row on the right evaluates to true
   * @throws TuplesException If the RHS cannot be tested.
   */
  private boolean testRhs() throws TuplesException {
    rhs.beforeFirst();
    while (rhs.next()) {
      if (testFilter()) return true;
    }
    return false;
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
    LeftFiltered cloned = (LeftFiltered)super.clone();

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

}
