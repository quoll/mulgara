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

package org.mulgara.util;

// Java 2 standard packages
import java.net.*;

/**
 * A utility class to handle various URIs with various protcols and remove
 * the default port or to modify the URI in some way so that it is handled
 * consistently within the system.
 *
 * @created 2004-05-11
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:29 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class ServerURIHandler {

  /**
   * Converts a model URI removing the port number if a known protocol is given.
   * For example, rmi://server:1099/ becomes rmi://server/.  Unknown protocols
   * will pass through unmodified.  The fragment part of the URI will also
   * be discarded.
   *
   * @param modelURI the URI to convert.
   * @throws URISyntaxException if the newly created URI was invalid.
   * @return the newly converted URI.
   */
  public static URI convertToServerURI(final URI modelURI)
      throws URISyntaxException {

    URI returnURI;

    if ((modelURI.getScheme().equals("rmi")) && (modelURI.getPort() == 1099)) {

      returnURI  = new URI(modelURI.getScheme(), null, modelURI.getHost(),
          -1, modelURI.getPath(), null, null);
    }
    else {

      returnURI  = new URI(modelURI.getScheme(), null, modelURI.getHost(),
          modelURI.getPort(), modelURI.getPath(), null, null);
    }

    return returnURI;
  }

  /**
   * Converts a model URI removing the port number if a known protocol is given.
   * For example, rmi://server:1099/ becomes rmi://server/.  Unknown protocols
   * will pass through unmodified.  The fragment part of the URI will remain
   * intact.
   *
   * @param modelURI the URI to convert.
   * @throws URISyntaxException if the newly created URI was invalid.
   * @return the newly converted URI.
   */
  public static URI removePort(final URI modelURI) throws URISyntaxException {

    URI returnURI = modelURI;

    if (modelURI.getScheme() != null) {
      if ((modelURI.getScheme().equals("rmi")) && (modelURI.getPort() == 1099)) {
        returnURI  = new URI(modelURI.getScheme(), null, modelURI.getHost(),
            -1, modelURI.getPath(), null, modelURI.getFragment());
      }
    }

    return returnURI;
  }
}
