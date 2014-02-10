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

package org.mulgara.content.mbox.parser.exception;

/**
 * When header parsing or mime message splitting has failed.
 *
 * @created 2001-06-27
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.7 $
 *
 * @modified $Date: 2005/01/05 04:57:41 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001
 *   <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 *
 * @licence <A href="{@docRoot}/../../LICENCE">Quoll Licence</A>
 */
public class MimeProcessingException extends Exception {

  /** Generated serialization ID. */
  private static final long serialVersionUID = 9086445488321381612L;

  /**
   * Constructor.
   *
   * @param s The message for this exception
   */
  public MimeProcessingException(String s) {
    super(s);
  }

   /**
    * Constructor.
    *
    * @param s The message for this exception
    * @param e The chained exception
    */
  public MimeProcessingException(String s, Exception e) {
    super(s, e);
  }

}
