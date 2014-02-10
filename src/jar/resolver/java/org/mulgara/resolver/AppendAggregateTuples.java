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

package org.mulgara.resolver;

// Java 2 standard packages
import java.util.*;

// Third party packages
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.util.StackTrace;

/**
 * Wrapper around a partially-evaluated {@link Tuples} instance, evaluating
 * atomic-valued aggregate functions such as {@link Count}.
 *
 * @created 2004-02-26
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/02/22 08:16:06 $
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
class AppendAggregateTuples extends AbstractTuples {

  private final static Logger logger =
      Logger.getLogger(AppendAggregateTuples.class.getName());

  /**
   * The aggregate functions extracted from the <code>SELECT</code> clause.
   */
  private List<Query> queryList;

  /**
   * Whether the corresponding index of this instance is an index into
   * the {@link #queryList} (if <code>true</code>) or the wrapped
   * {@link #tuples} (if <code>false</code>).
   */
  private boolean[] columnIsAggregate;

  private int[] columnAggregateIndex;

  /** The session to localize into.  */
  private ResolverSession session;

  /** The wrapped {@link Answer} instance.  */
  private Tuples tuples;

  /** Cache of values calculated for the current row.  */
  private long[] cache;

  /** Whether the {@link #cache} is valid for the current row.  */
  private boolean[] isCacheValid;

  /** Exists *only* so we can call resolveMap() */
  private LocalQueryResolver context;

  /**
   * Wrap an {@link Answer} instance.
   *
   * @param session       the session against which to evaluate aggregate
   *                      functions
   * @param tuples        the {@link Tuples} to wrap
   * @param variableList  the <code>SELECT</code> clause containing the
   *                      aggregate functions to evaluate
   * @throws IllegalArgumentException  if <var>session</var> or
   *                                   <var>tuples</var> are
   *                                    <code>null</code>
   * @throws TuplesException  if there's trouble reading <var>tuples</var>
   */
  AppendAggregateTuples(ResolverSession session,
                        LocalQueryResolver context, Tuples tuples,
                        List<? extends SelectElement> variableList) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("Generating variable list for " + tuples + " and " + variableList);
      logger.debug("AppendAggregateTuples instantiated " + hashCode());
    }
    if (session == null) {
      throw new IllegalArgumentException("Null \"session\" parameter");
    }
    if (tuples == null) {
      throw new IllegalArgumentException("Null \"tuples\" parameter");
    }

    // Initialize fields
    this.context = context;
    this.columnIsAggregate = new boolean[variableList.size()];
    this.columnAggregateIndex = new int[variableList.size()];
    this.session = session;
    this.tuples = (Tuples) tuples.clone();

    // Prep variable list
    Variable[] tupleVars = this.tuples.getVariables();
    // the size of variableList may be larger due to repeated variables,
    // while the size of the tuples may be larger due to aggregates
    int uniqueTupleVarLen = Math.min(variableList.size(), tupleVars.length);

    Set<Variable> newVariableList = new LinkedHashSet<Variable>();
    for (int i = 0; i < uniqueTupleVarLen; i++) {
      assert variableList.contains(tupleVars[i]);

      newVariableList.add(tupleVars[i]);
      columnAggregateIndex[i] = -1;
      if (logger.isDebugEnabled()) {
        logger.debug("" + hashCode() + " columnAggregateIndex[" + i + "] = -1");
      }
      columnIsAggregate[i] = false;
    }
    if (logger.isDebugEnabled()) {
      logger.debug("" + hashCode() + " tupleVars.length = " + tupleVars.length + " unique vars = " + uniqueTupleVarLen);
    }

    // Calculate the rest of the variable list
    int aggregateIndex = 0;
    queryList = new ArrayList<Query>();
    for (int i = 0; i < variableList.size(); i++) {
      Object element = variableList.get(i);
      if (element instanceof Count) {
        columnAggregateIndex[uniqueTupleVarLen + aggregateIndex] = aggregateIndex;
        if (logger.isDebugEnabled()) {
          logger.debug("" + hashCode() + " columnAggregateIndex[" +
              uniqueTupleVarLen + aggregateIndex + "] = " + aggregateIndex);
        }
        newVariableList.add(((Count)element).getVariable());
        columnIsAggregate[uniqueTupleVarLen + aggregateIndex] = true;
        aggregateIndex++;

        Query query = ((Count)element).getQuery();
        queryList.add((Query)query.clone());
      }
    }

    if (logger.isDebugEnabled()) {
      logger.info("Generated variable list " + newVariableList);
    }

    setVariables(new ArrayList<Variable>(newVariableList));

    if (logger.isDebugEnabled()) {
      logger.debug("Set variable list " + Arrays.asList(getVariables()));
    }

    // Initialize cache fields dependent on queryList
    cache = new long[queryList.size()];
    isCacheValid = new boolean[cache.length];
  }

  //
  // Methods implementing AbstractTuples
  //
  public void beforeFirst() throws TuplesException {
    tuples.beforeFirst();
  }

  public void beforeFirst(long[] prefix,
      int suffixTruncation) throws TuplesException {
    if (prefix.length == 0 && suffixTruncation == 0) {
      beforeFirst();
    }
    else {
      throw new TuplesException(
          "AppendAggregateTuples.beforeFirst not implemented"
          );
    }
  }

  public Object clone() {
    AppendAggregateTuples cloned = (AppendAggregateTuples)super.clone();

    cloned.session = session;
    cloned.columnIsAggregate = cloned.columnIsAggregate;
    cloned.tuples = (Tuples) tuples.clone();
    cloned.cache = (long[]) cache.clone();
    cloned.isCacheValid = (boolean[]) isCacheValid.clone();
    cloned.queryList = new ArrayList<Query>();
    for (Query query : queryList) {
      cloned.queryList.add((Query)query.clone());
    }

    if (logger.isDebugEnabled()) {
      logger.debug("AppendAggregateTuples clone " + cloned.hashCode() + " from " + hashCode());
    }
    return cloned;
  }

  public void close() throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("closing AppendAggregateTuples " + hashCode() + "\n" + new StackTrace());
    }
    for (Query query : queryList) {
      if (logger.isDebugEnabled()) {
        logger.debug("AppendAggregateTuples " + hashCode() + " closing Query " + query.hashCode());
      }
      query.close();
    }

    tuples.close();
  }

  public long getColumnValue(int column) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("AppendAggregateTuples " + hashCode() + ": getting column " +
          column);
    }

    int index = columnAggregateIndex[column];

    if (index < 0) {
      return tuples.getColumnValue(column);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("" + hashCode() + " Column is an aggregate");
    }

    if (!isCacheValid[index]) {
      try {
        // Add the values of the current row to the WHERE clause of the
        // aggregate function's query
        Query query = queryList.get(index);

        if (logger.isDebugEnabled()) {
          logger.debug("" + hashCode() + " Base aggregate query: " + query);
        }

        // Evaluate the aggregate query
        Tuples tuples = context.resolveMap(query, createBindingMap(this.tuples));

        if (logger.isDebugEnabled()) {
          logger.debug("Resolved aggregate to " + tuples);
        }

        if (logger.isDebugEnabled()) {
          logger.debug("Row count = " + tuples.getRowCount());
        }

        cache[index] = session.localize(new LiteralImpl(tuples.getRowCount()));
        isCacheValid[index] = true;

        tuples.close();
      }
      catch (LocalizeException le) {
        throw new TuplesException("Error localising subquery", le);
      }
      catch (QueryException e) {
        throw new TuplesException("Couldn't evaluate aggregate function", e);
      }
      catch (RuntimeException re) {
        logger.error("RuntimeException thrown in " + hashCode(), re);
        throw re;
      }
    }

    // Return the evaluated column value
    assert isCacheValid[index];
    return cache[index];
  }

  private Map<Variable,Value> createBindingMap(Tuples tuples) throws TuplesException {
    Map<Variable,Value> bindings = new HashMap<Variable,Value>();
    Variable[] vars = tuples.getVariables();

    for (int i = 0; i < columnIsAggregate.length; i++) {
      if (!columnIsAggregate[i]) {
        long columnValue = tuples.getColumnValue(i);
        if (columnValue != Tuples.UNBOUND) {
          bindings.put(vars[i], new LocalNode(columnValue));
        }
      }
    }

    return bindings;
  }

  public long getRowCount() throws TuplesException {
    return tuples.getRowCount();
  }

  public long getRowUpperBound() throws TuplesException {
    return tuples.getRowUpperBound();
  }

  public long getRowExpectedCount() throws TuplesException {
    return tuples.getRowExpectedCount();
  }

  public int getRowCardinality() throws TuplesException {
    return tuples.getRowCardinality();
  }

  public boolean isEmpty() throws TuplesException {
    return tuples.isEmpty();
  }

  /**
   * @return the same value as the source column, or <code>true</code> in the
   *   case of appended aggregate columns (we have no certain way of knowing
   *   that the aggregate function is defined for the input row)
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    int index = columnAggregateIndex[column];

    if (index < 0) {
      return tuples.isColumnEverUnbound(column);
    }
    else {
      return true;
    }
  }

  public boolean hasNoDuplicates() throws TuplesException {
    return tuples.hasNoDuplicates();
  }

  public List<Tuples> getOperands() {
    return Collections.singletonList(tuples);
  }

  public boolean next() throws TuplesException {
    // The current row is changing, so the cache array will no longer be valid
    Arrays.fill(isCacheValid, false);

    return tuples.next();
  }
}
