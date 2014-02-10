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

package org.mulgara.store.nodepool.memory;

// Third-party packages
import org.mulgara.store.nodepool.*;
import org.mulgara.store.xa.XANodePool;
import org.mulgara.util.LongMapper;
import org.mulgara.util.MemLongMapper;

// log4j classes
import org.apache.log4j.*;

/**
 * A memory based NodePool implementation which simply increments a counter to
 * generate a new node number. Simply for use in the small temporary node pool.
 *
 * @created 2003-09-11
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/22 08:16:42 $
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
public final class MemoryNodePoolImpl implements XANodePool {

  /**
   * Logger.
   */
  private final static Logger logger =
      Logger.getLogger(MemoryNodePoolImpl.class.getName());

  /**
   * A counter which registers the current given node.
   */
  private long currentNodeId = MIN_NODE;

  /**
   * Construct a new node pool.
   */
  public MemoryNodePoolImpl() {

    // Null constructor.
  }

  /**
   * Returns a count of nodes which have been allocated but not yet released.
   *
   * @return a count of nodes which have been allocated but not yet released.
   */
  public long getNrValidNodes() {

    return currentNodeId - MIN_NODE;
  }

  /**
   * Generate a unique 32-bit node value.
   *
   * @return RETURNED VALUE TO DO
   * @throws NodePoolException EXCEPTION TO DO
   */
  public long newNode() throws NodePoolException {

    // Increment current node and return previously current node id
    currentNodeId++;

    return (currentNodeId - 1);
  }

  /**
   * Does nothing in this implementation.
   *
   * @param node a 32-bit node value previously returned by {@link #newNode}
   * @throws NoSuchNodeException never thrown.
   * @throws NodePoolException EXCEPTION TO DO
   */
  public void releaseNode(long node) throws NodePoolException,
      NoSuchNodeException {

    // Do nothing.  Nodes are never recovered.
  }

  public XANodePool newReadOnlyNodePool() {
    throw new UnsupportedOperationException();
  }

  public XANodePool newWritableNodePool() {
    return this;
  }

  public void addNewNodeListener(NewNodeListener l) { }

  public void removeNewNodeListener(NewNodeListener l) { }

  /**
   * Does nothing in this implementation.
   *
   */
  public void release() {

    // Do nothing
  }

  /**
   * Does nothing in this implementation.
   *
   */
  public void refresh() {

    // Do nothing
  }

  /**
   * Does nothing in this implementation.
   *
   * @throws NodePoolException EXCEPTION TO DO
   */
  public void close() throws NodePoolException {

    // Do nothing.
  }

  /**
   * Does nothing in this implementation.
   *
   * @throws NodePoolException EXCEPTION TO DO
   */
  public void delete() throws NodePoolException {

    // Do nothing.
  }

  public void prepare() {}
  public void commit() {}
  public int[] recover() { return new int[0]; }
  public void selectPhase(int phaseNumber) {}
  public void rollback() {}
  public void clear() {}
  public void clear(int phaseNumber) {}
  public int getPhaseNumber() { return 0; }

  public LongMapper getNodeMapper() throws Exception {
    return new MemLongMapper();
  }
}
