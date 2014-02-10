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

import org.mulgara.store.StoreException;

/**
 * Exception thrown by {@link StringPool} operations.
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
public class StringPoolException extends StoreException {

  /** */
  private static final long serialVersionUID = -3461930028372638566L;

  /**
   * Builds an exception for a string pool operation, with a string message.
   * @param message The message about the operation error.
   */
  public StringPoolException(String message) {
    super(message);
  }

  /**
   * Builds an exception for a string pool operation, with a cause and string message.
   * @param message The message about the operation error.
   * @param throwable The cause of the error.
   */
  public StringPoolException(String message, Throwable throwable) {
    super(message, throwable);
  }

  /**
   * Builds an exception for a string pool operation, with a cause and string message.
   * @param throwable The cause of the error.
   */
  public StringPoolException(Throwable throwable) {
    super(throwable);
  }
}
