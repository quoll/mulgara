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
import java.util.Iterator;
import java.util.Set;

import org.mulgara.query.GraphResource;

/**
 * An AST element for server-based commands.
 *
 * @created 2007-08-22
 * @author Paula Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class ServerCommand extends AbstractCommand {

  /** The graph being referred to on the server. */
  private final GraphResource serverGraph;
  
  /**
   * Creates a new command, with a principle graph URI.
   * @param serverGraphUri The URI of the graph.
   */
  public ServerCommand(URI serverGraphUri) {
    serverGraph = (serverGraphUri != null) ? new GraphResource(serverGraphUri) : null;
  }
  
  
  /**
   * Finds the server URI for the graph.
   * @return The URI used to find the server.
   */
  public URI getServerURI() {
    // Short-circuit for backup and restore (don't need a server URI if executed with an existing connection)
    if (serverGraph == null) {
      return null;
    }
    
    Set<URI> gs = serverGraph.getDatabaseURIs();
    URI serverUri = null;
    Iterator<URI> iter = gs.iterator();
    if (iter.hasNext()) {
      serverUri = iter.next();
    }
    return serverUri;
  }


  /**
   * Indicates that this operation is not local.
   * @return Always <code>false</code> to indicate this command is not local.
   */
  public final boolean isLocalOperation() {
    return false;
  }

  
  /**
   * Indicates that this operation is not a UI command by default.
   * @return By default this will be <code>false</code> as a server side operation should not
   *         affect the local UI.
   */
  public boolean isUICommand() {
    return false;
  }
}
