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

package org.mulgara.resolver.lucene;


/**
 * Base class for exceptions thrown by full text string index.
 *
 * @created 2001-10-05
 *
 * @author <a href="http://staff.pisoftware.com/tate">Tate Jones</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:47 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FullTextStringIndexException extends Exception {

  /** Generated serialization ID */
  private static final long serialVersionUID = 787298465503301231L;

  /**
   * Create a new exception for the full text string index.
   * @param message The error message
   */
  public FullTextStringIndexException(String message) {
    super(message);
  }

  /**
   * Create a new exception with chaining for the full text string index.
   * @param message The eror message
   * @param cause The exception to be chained
   */
  public FullTextStringIndexException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a new exception with chaining for the full text string index.
   * @param cause The exception to be chained
   */
  public FullTextStringIndexException(Throwable cause) {
    super(cause);
  }
}
