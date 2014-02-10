/*
 * Copyright 2009 DuraSpace.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.query.operation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mulgara.connection.Connection;

/**
 * Represents a command to move data in or out of a graph or server.
 *
 * @created Aug 13, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class DataTx extends ServerCommand {

  /** The logger */
  static final Logger logger = Logger.getLogger(DataTx.class.getName());

  /** The source of the data. */
  private final URI source;
  
  /** The destination of the data. */
  private final URI destination;

  /** Indicates that data is to be loaded locally from the client. */
  private final boolean local;
  
  /**
   * Create a new data transfer command for moving data into or out of a graph or server.
   * If local is <code>true</code> then source or destination may be null, but 
   * an overriding input or output stream must be set before executing the operation.
   * @param source The source of data to insert.
   * @param destination The graph or server to load data into.
   * @param serverGraphURI The URI of the server or graph being operated on.  This
   *        parameter is primarily for use by the TqlAutoInterpreter for discovering
   *        server URI's of commands, and may be omitted if working directly with an
   *        existing {@link Connection}.
   * @param local If <code>true</code>, the source for load/restore or destination for
   *        export/backup will be a file or stream on the local system that is marshalled
   *        to/from the remote server.  If <code>false</code>, it will be a file on
   *        the remote server filesystem. 
   */
  public DataTx(URI source, URI destination, URI serverGraphURI, boolean local) {
    super(serverGraphURI);
    this.source = source;
    this.destination = destination;
    this.local = local;
  }


  /**
   * @return the URI of the source data.
   */
  public URI getSource() {
    return source;
  }


  /**
   * @return the destination URI for the data.
   */
  public URI getDestination() {
    return destination;
  }


  /**
   * @return the locality flag for the data.
   */
  public boolean isLocal() {
    return local;
  }


  /** The known set of schemas describing servers. */
  private static Set<String> knownSchemas = new HashSet<String>();
  static {
    knownSchemas.add("rmi");
    knownSchemas.add("local");
    knownSchemas.add("beep");
  }


  /**
   * Tests if a URI can potentially refer to a server. This will only apply for known schemas.
   * If the URI is null, treat this as a valid server URI.  This accounts for the fact that creating
   * a backup or restore operation with an explicit server URI is only to support legacy 
   * TqlAutoInterpreter code.  Commands created directly from the API to use with an existing
   * connection should not have server URI set.
   * 
   * @param serverURI The URI to check.
   * @return <code>true</code> if the URI is known to refer to a graph. <code>false</code> if we can't
   *   tell or it is known to refer to a server.
   */
  protected boolean serverTest(URI serverURI) {
    if (serverURI == null) return false;
    if (knownSchemas.contains(serverURI.getScheme())) return serverURI.getFragment() != null;
    return true;
  }
  
  
  /**
   * Determine the URI to be used for a server when processing a backup.
   * @param uri Can contain the URI of a graph, or of an entire server.
   * @return The URI for the server containing the uri.
   */
  public static URI calcServerUri(URI uri) {
    URI calcUri = null;
    
    // check if backing up a graph or a server
    String fragment = uri.getFragment();
    if (fragment == null) {
      if (logger.isDebugEnabled()) logger.debug("Backup for server: " + uri);
      calcUri = uri;
    } else {
      String serverUriString = uri.toString().replaceAll("#" + fragment, "");
      try {
        calcUri = new URI(serverUriString);
      } catch (URISyntaxException e) {
        throw new Error("Unable to truncate a fragment from a valid URI");
      }
    }
    return calcUri;
  }

}
