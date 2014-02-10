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

// Java 2 standard packages
import java.net.URI;

/**
 * Exception thrown by the {@link Content#newInputStream} method to
 * indicate that the cached version of the content has been validated against
 * the source server and doesn't need to be re-read.
 *
 * @created 2004-12-02
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
public class NotModifiedException extends Exception
{
  /**
   * The URI of the unmodified content.
   */
  private final URI uri;

  //
  // Constructors
  //

  /**
   * Sole constructor.
   *
   * @param uri  the URI of the unmodified content, never <code>null</code>
   * @throws IllegalArgumentException if <var>uri</var> is <code>null</code>
   */
  public NotModifiedException(URI uri)
  {
    super(uri + " not modified");

    // Validate "uri" parameter
    if (uri == null) {
      throw new IllegalArgumentException("Null \"uri\" parameter");
    }

    // Initialize fields
    this.uri = uri;
  }

  //
  // Accessors
  //

  /**
   * @return the URI which caused this exception
   */
  public URI getURI()
  {
    return uri;
  }
}
