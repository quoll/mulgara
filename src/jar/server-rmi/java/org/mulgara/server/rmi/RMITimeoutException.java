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

package org.mulgara.server.rmi;


/**
 * Exception thrown when {@link RemoteAnswerWrapperAnswer} prefetch thread times out.
 *
 * @created 2004-07-16
 *
 * @author Paul Gearon
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:02 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RMITimeoutException extends Exception {

  /**
   * Main Constructor.
   *
   * @param message Text description of problem
   */
  public RMITimeoutException(String message) {
    super(message);
  }

  /**
   * Chained constructor
   *
   * @param message Text description of problem
   * @param cause An exception which caused this exception to be thrown.
   */
  public RMITimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
