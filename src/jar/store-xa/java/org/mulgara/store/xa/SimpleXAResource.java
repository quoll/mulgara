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

// Java 2 standard packages
import java.io.*;

// required only for javadoc's sake
import javax.transaction.xa.XAResource;

/**
 * Simplified form of the {@link XAResource} interface.
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
public interface SimpleXAResource extends SimpleXARecoveryHandler {

  /**
   * Writes the metaroot for the current Phase to the metaroot file and creates
   * a new Phase (and metaroot).  After the prepare is complete the metaroot
   * file will contain two valid metaroots.
   *
   * @throws SimpleXAResourceException if the SimpleXAResource is in an
   *      inconsistent state.
   */
  public void prepare() throws SimpleXAResourceException;

  /**
   * Removes the older of the two valid metaroots in the metaroot file.  After
   * the commit is complete the metaroot file will contain only one valid
   * metaroot.
   *
   * @throws SimpleXAResourceException if {@link #prepare} has not been called
   *      or the SimpleXAResource is in an inconsistent state.
   */
  public void commit() throws SimpleXAResourceException;

  /**
   * If a prepare has completed it will remove the newer of the two valid metaroots,
   * otherwise it will return the resource to the in memory state of the previous
   * commit.  After the rollback is complete the metaroot file will contain only one
   * valid metaroot.
   */
  public void rollback() throws SimpleXAResourceException;

  /**
   * Gets the PhaseNumber attribute of the SimpleXAResource object
   *
   * @return The PhaseNumber value
   */
  public int getPhaseNumber() throws SimpleXAResourceException;

  /**
   * When applied to a read-only view of a resource, brings the view up to date
   * with the current state of the resource.
   */
  public void refresh() throws SimpleXAResourceException;

  /**
   * Releases the snapshot of the resource held by a read-only view.
   */
  public void release() throws SimpleXAResourceException;
}
