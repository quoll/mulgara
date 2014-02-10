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
 * The interface required by any persistent resource that wishes to participate
 * in the Mulgara Database phase recovery protocol.
 *
 * Note: This interface is likely to be migrated to JTA/XAResource/XID in the near
 *       future.
 *
 * @created 2001-09-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/22 08:17:01 $
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
public interface SimpleXARecoveryHandler {

  /**
   * Returns an array which contains a list of the phase numbers for all valid
   * phases in the metaroot file. The array will contain zero, one or two
   * elements. There will be no valid phases if no prepares have been
   * successfully performed since the SimpleXAResource was initially created.
   *
   * @return the array of valid phase numbers.
   * @throws SimpleXAResourceException if {@link #selectPhase} or {@link #clear}
   *      has already been called.
   */
  public int[] recover() throws SimpleXAResourceException;

  /**
   * Selects one of the valid phases in the metaroot file. If another valid
   * phase exists in the metaroot file it will be invalidated.
   *
   * @param phaseNumber the phase number to select.
   * @throws IOException if an I/O error occurs.
   * @throws SimpleXAResourceException if the phase number does not correspond
   *      to a valid phase in the metaroot file or the SimpleXAResource is in
   *      an inconsistent state.
   */
  public void selectPhase(int phaseNumber) throws IOException,
      SimpleXAResourceException;

  /**
   * Invalidates all phases in the metaroot file.
   *
   * @throws IOException if an I/O error occurs.
   * @throws SimpleXAResourceException if the SimpleXAResource is in an
   *      inconsistent state.
   */
  public void clear() throws IOException, SimpleXAResourceException;

  /**
   * Invalidates all phases in the metaroot file and creates an initial phase
   * with the specified phase number.
   *
   * @param phaseNumber the initial phase number.
   * @throws IOException if an I/O error occurs.
   * @throws SimpleXAResourceException if the SimpleXAResource is in an
   *      inconsistent state.
   */
  public void clear(int phaseNumber) throws IOException,
      SimpleXAResourceException;
}
