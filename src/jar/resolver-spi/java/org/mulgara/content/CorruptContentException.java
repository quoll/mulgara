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

package org.mulgara.content;

/**
 * An exception thrown by the {@link ContentHandler#parse} method to indicate
 * that the content is definitely of the indicated content type (indicated by
 * file magic, etc) but is invalid for that format.
 *
 * @created 2004-09-21
 *
 * @author <a href="http://staff.pisoftware.com/raboczi/">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:50 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004
 *   <a href="http://www.tucanatech.com/">Tucana Technology, Inc</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class CorruptContentException extends ContentHandlerException {

  /**
   * Constructor.
   *
   * @param s The message for this exception
   */
  public CorruptContentException(String s) {

    super(s);
  }

   /**
    * Constructor.
    *
    * @param s The message for this exception
    * @param e The chained exception
    */
  public CorruptContentException(String s, Exception e) {

    super(s, e);
  }

}
