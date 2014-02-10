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

package org.mulgara.store.xa;

// Internal packages.
import org.mulgara.store.StoreException;
import org.mulgara.store.nodepool.*;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.StringPool;
import org.mulgara.store.stringpool.StringPoolException;

/**
 * Interface for transactional StringPools.
 *
 * @created 2001-09-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/22 08:17:02 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001-2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface XAStringPool extends SimpleXAResource, NewNodeListener,
    ReleaseNodeListener, StringPool {

  /**
   * Obtain a new handle on a read-phase of the StringPool.
   */
  public XAStringPool newReadOnlyStringPool();

  /**
   * Obtain a new handle on a read-phase of the StringPool.
   */
  public XAStringPool newWritableStringPool();

  /**
   * Advise that this string pool is no longer needed.
   */
  public void close() throws StoreException;

  /**
   * Close this string pool, if it is currently open, and remove all files
   * associated with it.
   */
  public void delete() throws StoreException;

  /**
   * Adds a [graph node:SPObject] pair to the string pool.  If the SPObject exists
   * a StringPoolException is thrown.
   * @param spObject The SPObject to be stored.
   * @return The gNode allocated to store the SPObject.
   * @throws StringPoolException if either the graph node or the SPObject.
   * already exists in the pool or an internal error occurs.
   */
  public long put(SPObject spObject) throws StringPoolException, NodePoolException;

  /**
   * Finds and returns the graph node corresponding to <var>spObject</var>.  If
   * the SPObject is not in the string pool then a new node is allocated from
   * the internal node pool and the [graph node:SPObject] pair is added to the
   * string pool.  The use of this method is faster than calling the
   * findGNode(), NodePool.newNode() and put() methods separately.
   *
   * @param spObject An SPObject to search for within the pool.
   * @param create A flag to indicate that new nodes should be created if needed.
   * @return the graph node corresponding to <var>spObject</var>.
   * @throws StringPoolException if an internal error occurs.
   */
  public long findGNode(SPObject spObject, boolean create) throws StringPoolException;

  /**
   * Sets the node pool to be used in association with this StringPool.
   * @param nodePool The node pool this string pool will allocate nodes from.
   */
  public void setNodePool(XANodePool nodePool);
}
