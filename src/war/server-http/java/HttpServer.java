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

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.server.*;

/**
 * Java HTTP server. This class would more properly be called a <q>system
 * property server</q> . It exports the Mulgara {@link SessionFactory} object as
 * a system property, where a web application running in the same VM can access
 * it to provide HTTP network export.
 *
 * @created 2002-01-21
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2004/12/22 05:04:47 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2002-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class HttpServer extends AbstractServer {

  /**
   * Logger. This is named after the classname.
   */
  private final static Logger logger = Logger.getLogger(HttpServer.class);

  //
  // Constructor
  //

  /**
   * CONSTRUCTOR HttpServer TO DO
   */
  public HttpServer(URI serverURI) {

    setURI(serverURI);
  }

  //
  // Additional API
  //

  /**
   * Gets the SessionFactory attribute of the HttpServer object
   */
  public SessionFactory getSessionFactory() {

    return super.getSessionFactory();
  }

  //
  // Methods implementing AbstractServer
  //

  /**
   * Start the server.
   */
  protected void startService() {

    // null implementation
  }

  /**
   * Stop the server.
   */
  protected void stopService() {

    // null implementation
  }
}
