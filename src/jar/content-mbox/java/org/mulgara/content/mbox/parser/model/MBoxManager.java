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

package org.mulgara.content.mbox.parser.model;

import org.mulgara.content.Content;
import org.mulgara.content.mbox.parser.model.exception.ModelException;

/**
 * A manager for mboxes which will create and keep track of mbox objects in
 * order to allow for caching.
 *
 * @created 2004-08-25
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.7 $
 *
 * @modified $Date: 2005/01/05 04:57:42 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface MBoxManager {

  /**
   * Clears the cache of all stored mboxes.
   */
  public void clear();

  /**
   * Removes the given mbox from the cache.
   *
   * @param mbox The mbox to remove
   */
  public void delete(MBox mbox) throws ModelException;

  /**
   * Gets the mbox associated with the given stream.  If the mbox exists already
   * then the mbox will be retrieved, else a new mbox will be created and set
   * up.
   *
   * @param content The content object representing the file to process
   *
   * @return The mbox representing the stream
   *
   * @throws ModelException
   */
  public MBox getMBox(Content content) throws ModelException;

}
