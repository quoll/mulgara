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
 *   getModel() contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *   DefinablePrefixAnnotation contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.store;

// Third party packages
import org.apache.log4j.Logger;

// Standard Java packages
import java.util.*;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.ReresolvableResolution;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.statement.StatementStoreException;
import org.mulgara.store.tuples.Annotation;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.DefinablePrefixAnnotation;
import org.mulgara.store.tuples.StoreTuples;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/**
 * Tuples backed by the graph, corresponding to a particular constraint. This
 * class retains the original constraint so that the graph index it's resolved
 * against can be resolved anew as its variables are bound.
 *
 * @created 2003-08-06
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.11 $
 * @modified $Date: 2005/05/06 04:07:58 $
 * @maintenanceAuthor $Author: amuys $
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class StatementStoreResolution extends AbstractTuples implements ReresolvableResolution {
  private final static long ROWCOUNT_UNCALCULATED = -1;
  private final static int ROWCARD_UNCALCULATED = -1;

  private final static Logger logger = Logger.getLogger(StatementStoreResolution.class);

  /** The constraint these tuples were generated to satisfy. */
  private Constraint constraint;

  /** The graph from which these tuples were generated. */
  private StatementStore store;

  /** Number of rows, constrained only by the fixed-prefix */
  private long[] rowCount = new long[] { ROWCOUNT_UNCALCULATED };

  /** The row cardinality, constrained only by the fixed-prefix */
  private int[] rowCardinality = new int[] { ROWCARD_UNCALCULATED };

  /** The unrestrained statement-store indexable by the defined index */
  private Tuples indexedTuples;

  /** Mapping of statement variable order to constraint order */
  private int[] columnOrder;

  /** Prefix definition derived from the constraint */
  private boolean[] baseDefinition;

  /** Number of fixed terms in the base Definition */
  private int fixedLength;

  /** The prefix definition defining the graph-tuples'-index */
  private boolean[] prefixDefinition;

  /**
   * Number of terms defining which graph-tuples-index to use,
   * hence minimum length of prefix to beforeFirst
   */
  private int boundLength;

  /** The prefix used to index into the graph-tuples. */
  private long[] prefix;

//  /** Variable array */
//  private Variable[] variables;

  /** mapping between variable index and tuples index. */
  private int[] variableToColumn;

  /**
   * true if this tuples should appear to be empty because a query node exists
   * in the constraint.
   * TODO remove this when the statement store permits query nodes.
   */
  private boolean isEmpty;

  /** Used to uniquely identify an equiv-class of StatementStoreResolution across clones. */
  private final int id;

  /**
   * Find a graph index that satisfies a constraint.
   *
   * @param constraint the constraint to satisfy
   * @param store the store to resolve against
   * @throws IllegalArgumentException if <var>constraint</var> or <var>graph
   *      </var> is <code>null</code>
   * @throws TuplesException EXCEPTION TO DO
   */
  StatementStoreResolution(Constraint constraint, StatementStore store) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("Resolving constraint against statementStore: " + constraint);
    }
    this.constraint = constraint;
    this.store = store;

    // TODO remove this when the statement store permits query nodes.
    isEmpty =
        toGraphTuplesIndex(constraint.getElement(0)) < 0 ||
        toGraphTuplesIndex(constraint.getElement(1)) < 0 ||
        toGraphTuplesIndex(constraint.getElement(2)) < 0 ||
        toGraphTuplesIndex(constraint.getModel()) < 0;

    baseDefinition = calculateBaseDefinition(constraint);
    fixedLength = calculatePrefixLength(baseDefinition);
    id = System.identityHashCode(baseDefinition);

    defineIndex(baseDefinition);
  }


  private static long calculateRowCount(Constraint constraint, StatementStore store) throws TuplesException {
    try {
      Tuples countTuples = store.findTuples(toGraphTuplesIndex(constraint.getElement(0)),
                                            toGraphTuplesIndex(constraint.getElement(1)),
                                            toGraphTuplesIndex(constraint.getElement(2)),
                                            toGraphTuplesIndex(constraint.getModel()));
      long rowCount = countTuples.getRowCount();
      countTuples.close();

      return rowCount;
    } catch (StatementStoreException es) {
      throw new TuplesException("Error accessing StatementStore", es);
    }
  }

  private static int calculateRowCardinality(Constraint constraint, StatementStore store) throws TuplesException {

    Tuples countTuples = null;
    int cardinality = 0;

    try {
      countTuples = store.findTuples(toGraphTuplesIndex(constraint.getElement(0)),
                                     toGraphTuplesIndex(constraint.getElement(1)),
                                     toGraphTuplesIndex(constraint.getElement(2)),
                                     toGraphTuplesIndex(constraint.getModel()));
      int rowCount = 0;
      countTuples.beforeFirst();
      while (countTuples.next()) {
        if (++rowCount > 1) {
          break;
        }
      }
      switch (rowCount) {
        case 0:
          cardinality = Cursor.ZERO;
          break;
        case 1:
          cardinality = Cursor.ONE;
          break;
        default:
          cardinality = Cursor.MANY;
      }
    } catch (StatementStoreException es) {
      throw new TuplesException("Error accessing StatementStore", es);
    } catch (TuplesException te) {
      if (countTuples != null) {
        try {
          countTuples.close();
        } catch (TuplesException e) { /* Already throwing an exception, so ignore */ }
      }
      throw te;
    }
    countTuples.close();
    return cardinality;
  }

  private static long toGraphTuplesIndex(ConstraintElement constraintElement) throws TuplesException {
    if (constraintElement instanceof Variable) {
      return NodePool.NONE;
    }
    if (constraintElement instanceof LocalNode) {
      return ((LocalNode) constraintElement).getValue();
    }

    throw new TuplesException("Unsupported constraint element: " + constraintElement + " (" + constraintElement.getClass() + ")");
  }


  private static boolean[] calculateBaseDefinition(Constraint constraint) {
    return new boolean[] {
        constraint.getElement(0) instanceof LocalNode,
        constraint.getElement(1) instanceof LocalNode,
        constraint.getElement(2) instanceof LocalNode,
        constraint.getModel() instanceof LocalNode
    };
  }


  private static int calculatePrefixLength(boolean[] definition) {
    int length = 0;
    for (int i = 0; i < definition.length; i++) {
      if (definition[i]) {
        length++;
      }
    }

    return length;
  }

  /**
   * @param bound constraints to be bound post-beforeFirst.  In constraint-order.
   */
  @SuppressWarnings("unchecked")
  protected void defineIndex(boolean[] bound) throws TuplesException {
    assert bound.length == 4;

    if (isEmpty) {
      setVariables(Collections.EMPTY_LIST);
      return;
    }

    boundLength = calculateBoundPrefixLength(bound);
    prefixDefinition = (boolean[])bound.clone();

    if (indexedTuples != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Orig indexedTuples.variables = " + toString(indexedTuples.getVariables()));
      }
      indexedTuples.close();
    }
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("findingTuples for prefixDefinition(" + prefixDefinition[0] + ", " + prefixDefinition[1] + ", " + prefixDefinition[2] + ", " + prefixDefinition[3] + ") on " + id);
      }
      indexedTuples = store.findTuples(prefixDefinition[0],
                                       prefixDefinition[1],
                                       prefixDefinition[2],
                                       prefixDefinition[3]);

      columnOrder = ((StoreTuples)indexedTuples).getColumnOrder();
      if (logger.isDebugEnabled()) {

        logger.debug("prefixDefinition = " + toString(prefixDefinition));
        logger.debug("indexedTuples.variables = " + toString(indexedTuples.getVariables()));
        logger.debug("columnOrder = " + toString(columnOrder));
        logger.debug("Column order returned " + toString(columnOrder));
      }
    } catch (StatementStoreException es) {
      throw new TuplesException("findTuples failed", es);
    }

    prefix = new long[4];
    List<Variable> variableList = new ArrayList<Variable>();
    variableToColumn = new int[4];
    int variableIndex = 0;
    // Note:  baseDefinition is in 'constraint-order'
    //        prefixDefinition is cloned from bound, hence in 'constraint-order'
    //        constraint.getElement is in 'constraint-order'
    //        prefix is in 'column-order'
    //        variableList is in 'column-order'
    //        columnOrder[] converts 'column-order' -> 'constraint-order'
    //        variableToColumn converts 'variableIndex' -> 'columnIndex'
    //  i is iterating over prefix, so is in 'column-order'
    if (logger.isDebugEnabled()) {
      logger.debug("Initialising prefix[] on " + id + " baseDefn = " + toString(baseDefinition) + " constraint = " + constraint + " prefixDefn = " + toString(prefixDefinition));
    }

    for (int i = 0; i < columnOrder.length; i++) {
      if (baseDefinition[columnOrder[i]]) {
        if ((i > 0) && (prefix[i - 1] == 0)) {
          throw new TuplesException("Undefined hole in prefix returned from findTuples/4b. " +
                                    "Requested: " + toString(prefixDefinition) + " " +
                                    "Recvd: " + toString(columnOrder));
        }
        long node = ((LocalNode)constraint.getElement(columnOrder[i])).getValue();
        if (node <= 0) {
          throw new TuplesException(
              "Bad LocalNode in constraint.  constraint.getElement(" +
              columnOrder[i] + ") returned a LocalNode with value: " + node +
              " constraint=" + constraint
          );
        }
        prefix[i] = node;
      } else {
        Variable var = (Variable)constraint.getElement(columnOrder[i]);
        if (!var.equals(Variable.FROM)) {
          variableList.add(var);
          prefix[i] = prefixDefinition[columnOrder[i]] ? -1 : 0;
          variableToColumn[variableIndex++] = i;
        }
      }
    }

    if (variableIndex < variableToColumn.length) {
      // Resize variableToColumn.
      int[] v2c = new int[variableIndex];
      System.arraycopy(variableToColumn, 0, v2c, 0, v2c.length);
      variableToColumn = v2c;
    }

    if (logger.isDebugEnabled()) {
      logger.debug("prefix defined = " + toString(prefix));
    }
    setVariables(variableList);
  }


  private int calculateBoundPrefixLength(boolean[] bound) throws TuplesException {
    int boundLength = 0;
    for (int i = 0; i < 4; i++) {
      if (baseDefinition[i] && !bound[i]) {   // Check prefix definition matches base definition derived from constraint.
        throw new TuplesException("index request dosn't match constraint");
      }
      if (bound[i]) {
        boundLength++;
      }
    }
    assert boundLength >= fixedLength;

    return boundLength;
  }


  public ReresolvableResolution reresolve(final Map<? extends ConstraintElement, Long> bindings) throws TuplesException {
    boolean reconstrain = false;
    ConstraintElement[] e = new ConstraintElement[4];
    for (int i = 0; i < 4; i++) {
      e[i] = constraint.getElement(i);

      if (e[i] instanceof Variable) {
        Long value = bindings.get(e[i]);
        if (value != null) {
          e[i] = new LocalNode(value.longValue());
          reconstrain = true;
        }
      }
    }
    if (reconstrain) {
      ConstraintImpl newConstraint = new ConstraintImpl(e[0], e[1], e[2], e[3]);
      return new StatementStoreResolution(newConstraint, store);
    } else {
      return null;
    }
  }


  public void beforeFirst() throws TuplesException {
    beforeFirst(Tuples.NO_PREFIX, 0);
  }


  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    if (isEmpty) return;

    if (logger.isDebugEnabled()) {
      logger.debug("beforeFirst called on: " + TuplesOperations.tuplesSummary(this) + " with prefix: " + AbstractTuples.toString(prefix));
    }

    if (prefix.length > 4) {
      throw new TuplesException("Prefix too long");
    }
    long[] fullPrefix = calcFullPrefix(prefix);
    indexedTuples.beforeFirst(fullPrefix, suffixTruncation);
  }


  private long[] calcFullPrefix(long[] providedPrefix) throws TuplesException {
    long[] fullPrefix = new long[fixedLength + providedPrefix.length];

    if (logger.isDebugEnabled()) {
      logger.debug("calcFullPrefix on " + TuplesOperations.tuplesSummary(this));
      logger.debug("providedPrefix = " + toString(providedPrefix) + " on " + id);
      logger.debug("fullPrefix.length = " + fullPrefix.length);
      logger.debug("prefix = " + toString(prefix));
    }

    if (fullPrefix.length < boundLength) {
      throw new TuplesException("Prefix failed to meet defined minimum prefix");
    }

    int variableIndex = 0;
    for (int i = 0; i < fullPrefix.length; i++) {
      if (prefix[i] < -1) {
        throw new TuplesException("Query Node used in constraint.");
      } else if (prefix[i] > 0) {
        fullPrefix[i] = prefix[i];
      } else {
        // Check for query nodes in the providedPrefix.
        // TODO remove this when the statement store permits query nodes.
        if (providedPrefix[variableIndex] < 0) {
          // A query node.  Force a prefix that results in an empty set of
          // matching rows.
          return new long[] { Long.MAX_VALUE };
        }

        fullPrefix[i] = providedPrefix[variableIndex++];
      }
    }

    return fullPrefix;
  }


  public boolean next() throws TuplesException {
    return !isEmpty && indexedTuples.next();
  }


  public long getColumnValue(int column) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("getColumnValue(" + column + ") on " + id + " with variables = " + toString(getVariables()) + " columnOrder = " + toString(columnOrder) + " var->col = " + toString(variableToColumn) + " constraint = " + constraint);
    }
    return indexedTuples.getColumnValue(variableToColumn[column]);
  }


  public List<Tuples> getOperands() {
    return Collections.emptyList();
  }


  public long getRowCount() throws TuplesException {
    if (isEmpty) return 0;

    if (rowCount[0] == ROWCOUNT_UNCALCULATED) {
      rowCount[0] = calculateRowCount(constraint, store);
    }

    return rowCount[0];
  }

  public long getRowUpperBound() throws TuplesException {
    return getRowCount();
  }

  public long getRowExpectedCount() throws TuplesException {
    return getRowCount();
  }

  public int getRowCardinality() throws TuplesException {
    if (isEmpty) return Cursor.ZERO;

    if (rowCardinality[0] == ROWCARD_UNCALCULATED) {
      long count = rowCount[0];
      if (count != ROWCOUNT_UNCALCULATED) {
        rowCardinality[0] =
           count == 0 ? Cursor.ZERO : count == 1 ? Cursor.ONE : Cursor.MANY;
      } else {
        rowCardinality[0] = calculateRowCardinality(constraint, store);
      }
    }

    return rowCardinality[0];
  }

  public boolean isColumnEverUnbound(int column) {
    return false;
  }


  public boolean hasNoDuplicates() {
    return true;
  }


  public void renameVariables(Constraint constraint) {
    throw new UnsupportedOperationException("We really don't want to do that");
  }


  /**
   * Gets the Constraint attribute of the StatementStoreResolution object
   *
   * @return The Constraint value
   */
  public Constraint getConstraint() {
    return constraint;
  }


  /**
   * Gets the store attribute of the StatementStoreResolution object
   *
   * @return The store
   */
  public StatementStore getGraph() {
    return store;
  }


  public boolean isComplete() {
    return false;
  }


  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Object clone() {
    StatementStoreResolution cloned = (StatementStoreResolution)super.clone();

    // constraint immutable
    // store immutable
    // rowCount native
    cloned.indexedTuples = (Tuples)indexedTuples.clone();
    // columnOrder derived from indexedTuples, realloced if changed
    // baseDefinition derived from immutable constraint
    // fixedLength derived from baseDefinition
    // prefixDefinition realloced before write
    // boundLength native
    // prefix realloced before write

    return cloned;
  }


  public void close() throws TuplesException {
    if (indexedTuples != null) {
      indexedTuples.close();
    }
  }

  public String toString() {
    return indexedTuples.toString() + " from constraint " + constraint;
  }

  protected StatementStoreResolution getSSR() {
    return this;
  }

  public Annotation getAnnotation(Class<? extends Annotation> annotation) {
    if (annotation == DefinablePrefixAnnotation.class) {
      return new DefinablePrefixAnnotation() {
        public void definePrefix(Set<Variable> boundVars) throws TuplesException {
          boolean[] bound = new boolean[4];
          Constraint constraint = getConstraint();
          for (int i = 0; i < 4; i++) {
            ConstraintElement elem = constraint.getElement(i);
            if (elem instanceof LocalNode) {
              bound[i] = true;
            } else if (boundVars.contains(elem)) {
              bound[i] = true;
            } else {
              bound[i] = false;
            }
          }
          if (logger.isDebugEnabled()) {
            logger.debug("Tuples: " + TuplesOperations.tuplesSummary(getSSR()));
            logger.debug("binding definition = " + AbstractTuples.toString(bound));
          }
          defineIndex(bound);
        }
      };
    } else {
      return null;
    }
  }
}
