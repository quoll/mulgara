/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.server.rmi;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * This Handler permits URLs with an RMI scheme to be created. It also refers an
 * {@link #openConnection(URL)} request to the {@link RmiURLConnection} class.
 *
 * @created Mar 28, 2008
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Handler extends URLStreamHandler {

  /**
   * @see java.net.URLStreamHandler#openConnection(java.net.URL)
   */
  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    URLConnection connection = new RmiURLConnection(u);
    connection.connect();
    return connection;
  }

}
