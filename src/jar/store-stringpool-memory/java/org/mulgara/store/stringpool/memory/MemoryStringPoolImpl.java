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

package org.mulgara.store.stringpool.memory;

// Java 2 standard packages
import java.net.URI;
import java.util.*;

// Third-party packages
import org.apache.log4j.*;

// Locally written packages

// Use the XA implementation of SPObjectFactoryImpl.
import org.mulgara.query.Constraint;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.nodepool.memory.MemoryNodePoolImpl;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.stringpool.SPLimit;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.stringpool.xa.SPObjectFactoryImpl;
import org.mulgara.store.tuples.Annotation;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.store.xa.XANodePool;
import org.mulgara.store.xa.XAStringPool;

import gnu.trove.TLongObjectHashMap;
import gnu.trove.TObjectLongHashMap;

/**
 * A memory based StringPool implementation.
 *
 * @created 2003-09-11
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/05/16 11:07:09 $
 *
 * @maintenanceAuthor $Author: amuys $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class MemoryStringPoolImpl implements XAStringPool {

  /**
   * Logger.
   */
  private final static Logger logger =
      Logger.getLogger(MemoryStringPoolImpl.class.getName());

  final static SPObjectFactory SPO_FACTORY = SPObjectFactoryImpl.getInstance();

  /**
   * The hash map containing a long to an object.
   */
  private TLongObjectHashMap<SPObject> nodeToStringPool;

  /**
   * The hash map containing an object to a long.
   */
  private TObjectLongHashMap<SPObject> stringToNodePool;

  /**
   * An index for sorting the SPObjects.
   */
  private SortedSet<SPObject> stringIndex;

  /**
   * An array of smallest SPObject values.  Ordered by Type ID.
   */
  private SPObject[] smallestSPObjects;

  /**
   * An array of largest SPObject values.  Ordered by Type ID.
   */
  private SPObject[] largestSPObjects;

  /**
   * The node pool to allocate gNodes from.
   */
  private XANodePool xaNodePool;

  /**
   * Variables for when data is returned as a Tuples.
   */
  static final Variable[] VARIABLES = new Variable[] {
    StatementStore.VARIABLES[0]
  };

  /**
   * Create a new in memory string pool with 10 items by default.
   */
  public MemoryStringPoolImpl() {

    nodeToStringPool = new TLongObjectHashMap<SPObject>(10);
    stringToNodePool = new TObjectLongHashMap<SPObject>(10);
    stringIndex = new TreeSet<SPObject>();

    // intialise the SPObject arrays
    smallestSPObjects = new SPObject[SPObject.TypeCategory.TCID_TYPED_LITERAL + 1];
    largestSPObjects = new SPObject[SPObject.TypeCategory.TCID_TYPED_LITERAL + 1];
    for (int s = 1; s <= SPObject.TypeCategory.TCID_TYPED_LITERAL; s++) {
      smallestSPObjects[s] = new SPLimit(s, true);
      largestSPObjects[s] = new SPLimit(s, false);
    }
  }

  //
  // Methods from StringPool.
  //

  /**
   * Gets the SPObjectFactory associated with this StringPool implementation.
   */
  public SPObjectFactory getSPObjectFactory() {
    return SPO_FACTORY;
  }


  /**
   * Add a graph node:SPObject pair to the string pool. If the pair already
   * exists, do nothing. If neither the SPObject nor the graph node exists,
   * create them. If either the graph node or the SPObject exists, but the other
   * doesn't, throw an exception.
   * @deprecated
   * @param gNode the graph node half of the graph node:SPObject pair
   * @param spObject the SPObject half of the graph node:SPObject pair
   * @throws StringPoolException if only one of the graph node and SPObject
   *      exists in the pool
   */
  public void put(long gNode, SPObject spObject) throws StringPoolException {
    putInternal(gNode, spObject);
  }


  /**
   * Add a graph node:SPObject pair to the string pool. If the pair already
   * exists, do nothing. If neither the SPObject nor the graph node exists,
   * create them. If either the graph node or the SPObject exists, but the other
   * doesn't, throw an exception.
   *
   * @param gNode the graph node half of the graph node:SPObject pair
   * @param spObject the SPObject half of the graph node:SPObject pair
   * @throws StringPoolException if only one of the graph node and SPObject
   *      exists in the pool
   */
  private void putInternal(long gNode, SPObject spObject) throws StringPoolException {

    // Stop adding of nodes below a certain threshold.
    if (gNode < MemoryNodePoolImpl.MIN_NODE) {
      throw new IllegalArgumentException("gNode < MIN_NODE");
    }

    // Check to see if it already exists.
    if (nodeToStringPool.containsKey(gNode)) {
      throw new StringPoolException("Graph node already present in string pool");
    } else if (stringToNodePool.containsKey(spObject)) {
      throw new StringPoolException("SPObject already present in string pool");
    } else {
      // Add to both ways of indexing objects and nodes.
      nodeToStringPool.put(gNode, spObject);
      stringToNodePool.put(spObject, gNode);
      try {
        stringIndex.add(spObject);
      } catch (RuntimeException e) {
        throw new StringPoolException("Unable to add object: " + spObject + " for " + gNode, e);
      }
    }
  }

  public long put(SPObject spObject) throws StringPoolException, NodePoolException {
    if (xaNodePool == null) throw new IllegalStateException("No node pool set for the string pool.");
    long gNode = xaNodePool.newNode();
    putInternal(gNode, spObject);
    return gNode;
  }

  public void setNodePool(XANodePool nodePool) {
    this.xaNodePool = nodePool;
  }

  /**
   * Remove a graph node:SPObject pair from the string pool.
   *
   * @param gNode the node half of the graph node:SPObject pair
   * @return <code>true</code> if the node existed and was removed.
   * @throws StringPoolException if an internal error occurs
   */
  public boolean remove(long gNode) throws StringPoolException {

    boolean successful = false;

    if (nodeToStringPool.contains(gNode)) {

      SPObject obj = nodeToStringPool.remove(gNode);
      long node = stringToNodePool.remove(obj);
      stringIndex.remove(obj);

      if (node == gNode) {

        successful = true;
      }
      else {

        if (logger.isEnabledFor(Level.ERROR)) {

          logger.error("The retrieved node and the given node were unequal " +
              "when removing node: " + gNode);
        }

        throw new StringPoolException("The retrieved node and the given " +
            "node were unequal when removing node: " + gNode);
      }
    }

    return successful;
  }

  /**
   * @param spObject an SPObject to search for within the pool
   * @return the graph node corresponding to <var>spObject</var> , or
   * <code>null</code> if no such SPObject is in the pool.
   * @throws StringPoolException EXCEPTION TO DO
   */
  public long findGNode(SPObject spObject) throws
      StringPoolException {

    // Default result is zero
    long result = NodePool.NONE;

    if (stringToNodePool.containsKey(spObject)) {

      result = stringToNodePool.get(spObject);
    }

    return result;
  }


  public long findGNode(SPObject spObject, boolean create) throws StringPoolException {
    return findGNodeInternal(spObject, xaNodePool);
  }

  @Deprecated
  public long findGNode(SPObject spObject, NodePool nodePool) throws StringPoolException {
    return findGNodeInternal(spObject, nodePool);
  }

  public long findGNodeInternal(SPObject spObject, NodePool nodePool) throws StringPoolException {
    if (nodePool == null) {
      throw new IllegalArgumentException("nodePool parameter is null");
    }

    long result;
    if (stringToNodePool.containsKey(spObject)) {

      result = stringToNodePool.get(spObject);
    } else {
      try {
        result = nodePool.newNode();
      } catch (NodePoolException ex) {
        throw new StringPoolException("Could not allocate new node.", ex);
      }

      assert !nodeToStringPool.containsKey(result);
      nodeToStringPool.put(result, spObject);
      stringToNodePool.put(spObject, result);
    }

    return result;
  }

  /**
   * @param gNode a graph node to search for within the pool
   * @return the SPObject corresponding to <var>gNode</var> , or
   * <code>null</code> if no such graph node is in the pool
   * @throws StringPoolException EXCEPTION TO DO
   */
  public SPObject findSPObject(long gNode) throws StringPoolException {

    return (SPObject) nodeToStringPool.get(gNode);
  }

  public Tuples findGNodes(
      SPObject lowValue, boolean inclLowValue,
      SPObject highValue, boolean inclHighValue
  ) throws StringPoolException {

    SortedSet<SPObject> subset;

    // check if the low value should be excluded
    if (lowValue != null && !inclLowValue && stringIndex.contains(lowValue)) {
      // it is there, so move the lovValue on by one
      // find the tailSet including the low value
      subset = stringIndex.tailSet(lowValue);
      // find the second element
      Iterator<SPObject> it = subset.iterator();
      assert it.hasNext();
      // move to the element that needs to be dropped
      it.next();
      // see if there is more than just this element
      if (!it.hasNext()) {
        // no other items, so just exit with empty data
        return new SetWrapperTuples(new TreeSet<SPObject>());
      }
      // move the low value
      lowValue = it.next();
    }

    // check if the high value should be appended
    if (highValue != null && inclHighValue && stringIndex.contains(highValue)) {
      // need to add the high value
      // get the subset following the high value
      subset = stringIndex.tailSet(highValue);
      // go to the second element of this set
      Iterator<SPObject> it = subset.iterator();
      assert it.hasNext();
      // move to the highValue
      it.next();
      // see if there is more than just this element
      if (!it.hasNext()) {
        // no other items, so make the high value open ended
        highValue = null;
      } else {
        // move the high value
        highValue = it.next();
      }
    }

    // slice out the required data
    if (lowValue == null) {
      // get from the start of stringIndex
      if (highValue == null) {
        // get the whole set
        subset = stringIndex;
      } else {
        // get up to the high value
        subset = stringIndex.headSet(highValue);
      }
    } else {
      // get from after the low value
      if (highValue == null) {
        // get to the end
        subset = stringIndex.tailSet(lowValue);
      } else {
        // get between the two limits
        subset = stringIndex.subSet(lowValue, highValue);
      }
    }

    // return a tuples based on this set
    return new SetWrapperTuples(subset);
  }


  public Tuples findGNodes(
      SPObject.TypeCategory typeCategory, URI typeURI
  ) throws StringPoolException {
    if (typeURI != null) {
      throw new UnsupportedOperationException(
          "Finding typed literal nodes on the in memory string pool is not supported"
      );
    }

    SortedSet<SPObject> subset = typeCategory != null ?
        stringIndex.subSet(smallestSPObjects[typeCategory.ID], largestSPObjects[typeCategory.ID]) : stringIndex;

    return new SetWrapperTuples(subset);
  }

  public XAStringPool newReadOnlyStringPool() {
    throw new UnsupportedOperationException();
  }

  public XAStringPool newWritableStringPool() {
    return this;
  }

  /**
   * Releases the snapshot of the StringPool held by a read-only view.
   */
  public void release() {

    // Does nothing.
  }

  /**
   * When applied to a read-only view of a StringPool, brings the view up to
   * date with the current state of the StringPool.
   */
  public void refresh() {

    // Does nothing.
  }

  /**
   * Advise that this string pool is no longer needed.
   *
   * @throws StringPoolException EXCEPTION TO DO
   */
  public void close() throws StringPoolException {

    nodeToStringPool.clear();
    stringToNodePool.clear();
  }

  /**
   * Close this string pool, if it is currently open, and remove all files
   * associated with it.
   *
   * @throws StringPoolException EXCEPTION TO DO
   */
  public void delete() throws StringPoolException {

    nodeToStringPool = null;
    stringToNodePool = null;
  }

  public void newNode(long gNode) {
    throw new UnsupportedOperationException();
  }

  public void releaseNode(long gNode) {
    throw new UnsupportedOperationException();
  }

  public void prepare() {}
  public void commit() {}
  public int[] recover() { return new int[0]; }
  public void selectPhase(int phaseNumber) {}
  public void rollback() {}
  public void clear() {}
  public void clear(int phaseNumber) {}
  public int getPhaseNumber() { return 0; }

  /**
   * Used to wrap a set of SPObjects and return the associated GNodes as a Tuples.
   */
  private class SetWrapperTuples implements Tuples {

    /** The internal set data */
    private SortedSet<SPObject> set;

    /** The current row of data for this set */
    private SPObject currentRow;

    /** The iterator for the internal set */
    private Iterator<SPObject> internalIterator;

    /** The single column of variables for this object */
    private Variable[] variables;

    /**
     * Constructor.
     *
     * @param set The set to wrap.
     */
    public SetWrapperTuples(SortedSet<SPObject> set) {
      this.set = set;
      internalIterator = null;
      currentRow = null;
      variables = (Variable[])VARIABLES.clone();
    }

    /**
     * {@inheritDoc}
     */
    public Variable[] getVariables() {
      return variables;
    }

    /**
     * {@inheritDoc}
     */
    public long getRowCount() throws TuplesException {
      return set.size();
    }

    /**
     * {@inheritDoc}
     */
    public long getRowUpperBound() throws TuplesException {
      return set.size();
    }

    /**
     * {@inheritDoc}
     */
    public long getRowExpectedCount() throws TuplesException {
      return set.size();
    }

    /**
     * {@inheritDoc}
     */
    public int getRowCardinality() throws TuplesException {
      int size = set.size();
      return size == 0 ? ZERO :
             size == 1 ? ONE :
                         MANY;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() throws TuplesException {
      return set.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public int getColumnIndex(Variable variable) throws TuplesException {
      if (variable == null) {
        throw new IllegalArgumentException("variable is null");
      }

      if (variable.equals(variables[0])) {
        // The variable matches the one and only column.
        return 0;
      }

      throw new TuplesException("variable doesn't match any column: " + variable);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isColumnEverUnbound(int column) throws TuplesException {
      return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMaterialized() {
      return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUnconstrained() throws TuplesException {
      return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNoDuplicates() throws TuplesException {
      return true;
    }

    /**
     * {@inheritDoc}
     * data is unsorted, so return null.
     */
    public RowComparator getComparator() {
      return null;
    }

    /**
     * {@inheritDoc}
     */
    public java.util.List<Tuples> getOperands() {
      return java.util.Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    public void beforeFirst() throws TuplesException {
      internalIterator = set.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
      if (prefix.length > 1) {
        throw new TuplesException("prefix is out of range");
      }
      if (prefix == NO_PREFIX) {
        // no prefix, so just do a before first
        internalIterator = set.iterator();
      } else {
        // check if the object exists
        SPObject start = (SPObject)nodeToStringPool.get(prefix[0]);
        if (start == null) {
          Set<SPObject> empty = Collections.emptySet();
          internalIterator = empty.iterator();
        } else {
          internalIterator = Collections.singleton(start).iterator();
        }
      }
    }

    /** {@inheritDoc} */
    public long getColumnValue(int column) throws TuplesException {
      if (column != 0) {
        throw new TuplesException("Column does not exist");
      }
      return -stringToNodePool.get(currentRow);
    }

    /** {@inheritDoc} */
    public long getRawColumnValue(int column) throws TuplesException {
      return getColumnValue(column);
    }

    /**
     * {@inheritDoc}
     */
    public int getNumberOfVariables() {
      return 1;
    }

    /**
     * {@inheritDoc}
     */
    public void renameVariables(Constraint constraint) {
      variables[0] = (Variable)constraint.getElement(0);
    }

    /**
     * {@inheritDoc}
     */
    public boolean next() throws TuplesException {
      if (internalIterator.hasNext()) {
        currentRow = (SPObject)internalIterator.next();
        return true;
      } else {
        return false;
      }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws TuplesException {
      // no op
    }

    /**
     * {@inheritDoc}
     */
    public Object clone() {
      try {
        SetWrapperTuples s = (SetWrapperTuples)super.clone();
        s.currentRow = null;
        s.internalIterator = null;
        return s;
      } catch (CloneNotSupportedException e) {
        throw new AssertionError("Unable to clone tuples");
      }
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object object) {
      // check if this is a tuples
      if (!(object instanceof Tuples)) {
        return false;
      }
      // SetWrapperTuples are the same if they contain the same sets
      if (object instanceof SetWrapperTuples) {
        return set.equals(((SetWrapperTuples)object).set);
      }
      // differing types, so ask the other tuples to do all the work
      return ((Tuples)object).equals(this);
    }

    /**
     * Added to match {@link #equals(Object)}.
     */
    public int hashCode() {
      return TuplesOperations.hashCode(this);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
      return set.toString();
    }

    /**
     * Copied from AbstractTuples
     */
    public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
      return null;
    }
  }

}
