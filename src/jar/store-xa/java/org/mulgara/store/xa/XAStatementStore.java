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

// Locally written packages
import org.mulgara.store.nodepool.ReleaseNodeListener;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.statement.StatementStoreException;

/**
 * Interface for transactional StatementStores.
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
public interface XAStatementStore extends SimpleXAResource, StatementStore {
  /**
   * Obtain a new handle on a read-phase of the StatementStore.
   */
  public XAStatementStore newReadOnlyStatementStore();

  /**
   * Obtain a new handle on the write-phase of the StatementStore.
   */
  public XAStatementStore newWritableStatementStore();

  /**
   * Add a new listener for notification of node releases.
   */
  public void addReleaseNodeListener(ReleaseNodeListener l);

  /**
   * Remove a new listener from notification of node releases.
   */
  public void removeReleaseNodeListener(ReleaseNodeListener l);

  /**
   * Advise that this statement store is no longer needed.
   */
  public void close() throws StatementStoreException;

  /**
   * Close this statement store, if it is currently open, and remove all files
   * associated with it.
   */
  public void delete() throws StatementStoreException;

  /**
   * Informs the statement store of the metadata it uses for managing data.
   * @param systemGraphNode The node for the system graph.
   * @param rdfTypeNode The node representing rdf:type.
   * @param systemGraphTypeNode The node used for the SystemResolver graph type.
   * @throws StatementStoreException If unable to read the graphs after initializing.
   */
  public void initializeSystemNodes(long systemGraphNode, long rdfTypeNode, long systemGraphTypeNode) throws StatementStoreException;

}
