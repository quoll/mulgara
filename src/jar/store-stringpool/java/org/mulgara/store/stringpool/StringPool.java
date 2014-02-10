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

package org.mulgara.store.stringpool;

// Java 2 standard packages
import java.net.URI;

// Local packages
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.tuples.Tuples;

/**
 * A pool of {@link SPObject}s associated with
 * {@link org.mulgara.store.statement.StatementStore} nodes.
 *
 * Both graph nodes and {@link SPObject}s are unique within the pool.  Map
 * operations are available both from graph node to {@link SPObject} and
 * {@link SPObject} to graph node.
 *
 * @created 2001-10-05
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/20 10:26:19 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface StringPool {

  /**
   * Gets the SPObjectFactory associated with this StringPool implementation.
   */
  public SPObjectFactory getSPObjectFactory();

  /**
   * Adds a [graph node:SPObject] pair to the string pool.  If either the graph
   * node or the SPObject exists a StringPoolException is thrown.
   *
   * @param gNode The graph node half of the [graph node:SPObject] pair.
   * @param spObject The SPObject half of the [graph node:SPObject] pair.
   * @throws StringPoolException if either the graph node or the SPObject.
   * already exists in the pool or an internal error occurs.
   */
  public void put(long gNode, SPObject spObject) throws StringPoolException;

  /**
   * Remove a [graph node:SPObject] pair from the string pool.
   *
   * @param gNode The node half of the graph node:SPObject pair.
   * @return <code>true</code> if the node existed and was removed.
   * @throws StringPoolException if an internal error occurs.
   */
  public boolean remove(long gNode) throws StringPoolException;

  /**
   * Finds and returns the graph node corresponding to <var>spObject</var>, or
   * <code>NodePool.NONE</code> if no such SPObject is in the pool.
   *
   * @param spObject An SPObject to search for within the pool.
   * @return the graph node corresponding to <var>spObject</var>, or
   * <code>NodePool.NONE</code> if no such SPObject is in the pool.
   * @throws StringPoolException if an internal error occurs.
   */
  public long findGNode(SPObject spObject) throws StringPoolException;

  /**
   * Finds and returns the graph node corresponding to <var>spObject</var>.  If
   * the SPObject is not in the string pool then a new node is allocated from
   * the specified node pool and the [graph node:SPObject] pair is added to the
   * string pool.  The use of this method is faster than calling the
   * findGNode(), NodePool.newNode() and put() methods separately.
   *
   * @param spObject An SPObject to search for within the pool.
   * @return the graph node corresponding to <var>spObject</var>.
   * @throws StringPoolException if an internal error occurs.
   */
  public long findGNode(
      SPObject spObject, NodePool nodePool
  ) throws StringPoolException;

  /**
   * Finds and returns the SPObject corresponding to <var>gNode</var>, or
   * <code>null</code> if no such graph node is in the pool.
   *
   * @param gNode A graph node to search for within the pool.
   * @return the SPObject corresponding to <var>gNode</var>, or
   * <code>null</code> if no such graph node is in the pool.
   * @throws StringPoolException if an internal error occurs.
   */
  public SPObject findSPObject(long gNode) throws StringPoolException;

  /**
   * Finds and returns a list of graph nodes that correspond to all the
   * SPObjects in the specified range that exist in the string pool.  The graph
   * nodes are returned as a Tuples with one column.  The order of the graph
   * nodes in the Tuples is such that the corresponding SPObjects are in
   * ascending order with respect to the SPComparator for the TypeCategory and
   * typeNode of the SPObjects.  If both <var>lowValue</var> and
   * <var>highValue</var> are <code>null</code> then graph nodes for all [graph
   * node:SPObject] pairs are returned partitioned according to their
   * TypeCategory.  If neither <var>lowValue</var> nor <var>highValue</var> are
   * <code>null</code> then both SPObjects must be of the same type (i.e. same
   * TypeCategory and typeNode).
   *
   * @param lowValue The low end of the range to select. Pass <code>null</code>
   * to specify that the range starts at the lowest SPObject for the .
   * @param inclLowValue <code>true</code> if range includes the
   * <var>lowValue</var> object.  Ignored if <var>lowValue</var> is
   * <code>null</code>.
   * @param highValue The high end of the range to select. Pass
   * <code>null</code> to specify that the range ends at the highest SPObject.
   * @param inclHighValue <code>true</code> if range includes the
   * <var>highValue</var> object.  Ignored if <var>highValue</var> is
   * <code>null</code>.
   * @return the list of graph nodes as a Tuples with one column.
   * @throws StringPoolException if the SPObjects are not of the same type or
   * an internal error occurs.
   */
  public Tuples findGNodes(
      SPObject lowValue, boolean inclLowValue,
      SPObject highValue, boolean inclHighValue
  ) throws StringPoolException;

  /**
   * Finds and returns a list of graph nodes that correspond to all the
   * SPObjects of the specified type that exist in the string pool.  The graph
   * nodes are returned as a Tuples with one column.  The order of the graph
   * nodes in the Tuples is such that the corresponding SPObjects are in
   * ascending order with respect to the SPComparator for the TypeCategory and
   * typeNode of the SPObjects.
   *
   * @param typeCategory The type category of SPObjects to select.  Pass
   * <code>null</code> to match all SPObjects.
   * @param typeURI The type URI of a typed literal or <code>null</code> to
   * match all SPObjects with the TypeCategory specified by
   * <var>typeCategory</var>.  This parameter must be <code>null</code> unless
   * <var>typeCategory</var> is equal to SPObject.TypeCategory.TYPED_LITERAL.
   * @throws StringPoolException if an internal error occurs.
   */
  public Tuples findGNodes(
      SPObject.TypeCategory typeCategory, URI typeURI
  ) throws StringPoolException;

}
