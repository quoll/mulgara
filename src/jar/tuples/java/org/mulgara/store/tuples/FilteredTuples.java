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
 * Filtering operation. This class wraps another Tuples, removing those elements that don't
 * pass the filter.
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class FilteredTuples extends AbstractTuples implements ContextOwner {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(FilteredTuples.class.getName());

  /** The inner tuples to filter. */
  protected Tuples unfiltered;

  /** The filter to apply. */
  protected Filter filter;

  /** The tuples context */
  protected TuplesContext context;

  /** A list of context owners that this owner provides the context for. */
  private List<ContextOwner> contextListeners = new ArrayList<ContextOwner>();

  /**
   * Configure a tuples for filtering.
   *
   * @param unfiltered The original tuples.
   * @param filter The filter to apply.
   * @param queryContext The context to evaluate the tuples in.
   * @throws IllegalArgumentException If the <var>unfiltered</var> tuples is null.
   */
  FilteredTuples(Tuples unfiltered, Filter filter, QueryEvaluationContext queryContext) throws IllegalArgumentException {
    // store the operands
    this.filter = filter;
    this.unfiltered = (Tuples)unfiltered.clone();
    this.context = new TuplesContext(this.unfiltered, queryContext.getResolverSession());
    filter.setContextOwner(this);
    setVariables(this.unfiltered.getVariables());
  }


  /** {@inheritDoc} */
  public long getColumnValue(int column) throws TuplesException {
    return unfiltered.getColumnValue(column);
  }

  
  /** {@inheritDoc} */
  public long getRawColumnValue(int column) throws TuplesException {
    return unfiltered.getColumnValue(column);
  }


  /** {@inheritDoc} */
  public long getRowUpperBound() throws TuplesException {
    return unfiltered.getRowUpperBound();
  }


  /** {@inheritDoc} */
  public long getRowExpectedCount() throws TuplesException {
    return (long)(unfiltered.getRowExpectedCount() * getMatchRatio());
  }


  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() throws TuplesException {
    return unfiltered.isEmpty();
  }


  /** {@inheritDoc} */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    return unfiltered.isColumnEverUnbound(column);
  }


  /** {@inheritDoc} */
  public Variable[] getVariables() {
    return unfiltered.getVariables();
  }


  /** {@inheritDoc} */
  public int getColumnIndex(Variable variable) throws TuplesException {
    return unfiltered.getColumnIndex(variable);
  }


  /** {@inheritDoc} */
  public boolean isMaterialized() {
    return false;
  }


  /** {@inheritDoc} */
  public boolean hasNoDuplicates() throws TuplesException {
    return unfiltered.hasNoDuplicates();
  }


  /** {@inheritDoc} */
  public RowComparator getComparator() {
    return unfiltered.getComparator();
  }


  /** {@inheritDoc} */
  public List<Tuples> getOperands() {
    return Collections.unmodifiableList(Arrays.asList(new Tuples[] {unfiltered}));
  }


  /** {@inheritDoc} */
  public boolean isUnconstrained() throws TuplesException {
    return unfiltered.isUnconstrained();
  }


  /** {@inheritDoc} */
  public void renameVariables(Constraint constraint) {
    unfiltered.renameVariables(constraint);
  }


  /** {@inheritDoc} */
  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    unfiltered.beforeFirst(prefix, suffixTruncation);
  }

  
  /**
   * @return {@inheritDoc}
   * @throws TuplesException {@inheritDoc}
   */
  public boolean next() throws TuplesException {
    do {
      // move to the next on the unfiltered
      boolean currentNext = unfiltered.next();
      // Short-circuit execution if this tuples' cursor is after the last row
      if (!currentNext) return false;
      // check if the filter passes the current row on the unfiltered
    } while (!testFilter());

    return true;
  }


  /** {@inheritDoc} */
  public void close() throws TuplesException {
    unfiltered.close();
  }


  /** @return {@inheritDoc} */
  public Object clone() {
    FilteredTuples cloned = (FilteredTuples)super.clone();

    // Clone the mutable fields as well
    cloned.unfiltered = (Tuples)unfiltered.clone();
    cloned.context = new TuplesContext(cloned.unfiltered, context);
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
    if (!(context instanceof TuplesContext)) throw new IllegalArgumentException("FilteredTuples can only accept a TuplesContext.");
    this.context = (TuplesContext)context;
    for (ContextOwner l: contextListeners) l.setCurrentContext(context);
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

  /**
   * The expected ratio for matching on the filter. This value should update over time.
   * TODO: calculate this value, update it over time, and record it against the filter pattern.
   * @return A value between 1.0 (100% match) and 0.0 (no match).
   */
  private double getMatchRatio() {
    return 0.5;
  }
}
