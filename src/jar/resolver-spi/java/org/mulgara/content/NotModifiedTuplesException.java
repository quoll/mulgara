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

// Local packages
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.Resolution;

/**
 * This class is thrown by the {@link Resolution} returned by the
 * {@link ContentResolver#resolve} method when the underlying {@link Content}
 * throws a {@link NotModifiedException}.
 *
 * @created 2004-12-13
 *
 * @author Simon Raboczi
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:50 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class NotModifiedTuplesException extends TuplesException
{
  //
  // Constructors
  //

  private static final long serialVersionUID = -4903577060297708655L;

  /**
   * Sole constructor.
   *
   * @param cause  the tunneled {@link NotModifiedException} instance, never
   *   <code>null</code>
   * @throws IllegalArgumentException if <var>cause</var> is <code>null</code>
   */
  public NotModifiedTuplesException(NotModifiedException cause)
  {
    super(cause != null ? cause.getMessage() : "", cause);

    // Validate "cause" parameter
    if (cause == null) {
      throw new IllegalArgumentException("Null \"cause\" parameter");
    }
  }
}
