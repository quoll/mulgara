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

package org.mulgara.store.nodepool;

/**
 * Manager for sets of <code>long</code> elements.
 *
 * @created 2001-12-18
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/22 08:16:41 $
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
public interface NodePool {

  /**
   * The non-node value returned to indicate that no node matches a graph query,
   * or passed to indicate that a triple index is variable.
   */
  public static final long NONE = 0;

  /**
   * The lowest valid node ID.
   */
  public static final long MIN_NODE = 1;

  /**
   * Generate a unique 32-bit node value.
   *
   * @return RETURNED VALUE TO DO
   * @throws NodePoolException EXCEPTION TO DO
   */
  public long newNode() throws NodePoolException;

  /**
   * Release a node back into the pool for reuse.
   *
   * @param node a 32-bit node value previously returned by {@link #newNode}
   * @throws NoSuchNodeException if <var>node</var> isn't a value from {@link
   *      #newNode}, or has already been released
   * @throws NodePoolException EXCEPTION TO DO
   */
  public void releaseNode(long node) throws NodePoolException,
      NoSuchNodeException;

  /**
   * Adds a feature to the NewNodeListener attribute of the XANodePoolImpl
   * object
   *
   * @param l The feature to be added to the NewNodeListener attribute
   */
  public void addNewNodeListener(NewNodeListener l);
}
