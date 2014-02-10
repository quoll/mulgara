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

package org.mulgara.itql;


/**
 * Exception thrown by {@link TqlInterpreter} operations when the query returns
 * an expection.
 *
 * @created 2002-01-10
 *
 * @author <a href="http://staff.pisoftware.com/tate">Tate Jones</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:15 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ItqlInterpreterException extends Exception {

  /** Serialization ID */
  private static final long serialVersionUID = 2343059534661825090L;

  /**
   * Constructor for the ItqlInterpreterException object
   *
   * @param reason The reason for the exception.
   */
  public ItqlInterpreterException(String reason) {
    super(reason);
  }

  /**
   * Constructor for the ItqlInterpreterException object
   *
   * @param t The reason for the exception.
   */
  public ItqlInterpreterException(Throwable t) {
    super(t);
  }
}
