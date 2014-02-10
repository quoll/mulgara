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

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.log4j.Logger;
import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;

/**
 * Represents a command to back data up from a server.
 *
 * @created Aug 19, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Backup extends DataOutputTx {
  
  /** The logger */
  static final Logger logger = Logger.getLogger(Backup.class.getName());

  /**
   * Creates a new Backup command.
   * 
   * This constructor is deprecated. The server URI is not part of the operation
   * and is only present to support the TqlAutoInterpreter legacy code. Use
   * {@link #Backup(URI, boolean)} or {@link #Backup(OutputStream)} instead.
   * 
   * @param serverURI The server to back up.
   * @param destination The location where to back the data up.
   *        Only file URLs supported at the moment.
   * @param local The locality of the destination URI.
   */
  @Deprecated
  public Backup(URI serverURI, URI destination, boolean local) {
    super(serverURI, destination, serverURI, local);
  }
  
  /**
   * Creates a command to backup a server to a destination file.  This is the preferred
   * constructor for API calls that use their own server connections.  The server URI
   * is not an argument for the backup operation, and will be specified by the existing
   * connection.
   * @param destination The destination file URI to receive the backup.
   * @param locality The locality of the destination file (<code>true</code> is client
   *        file system, <code>false</code> is server file system).
   */
  public Backup(URI destination, boolean locality) {
    this(null, destination, locality);
  }
  
  /**
   * Creates a command to backup a server to an output stream.  This is the preferred
   * constructor for API calls that use their own server connections.  The server URI
   * is not an argument for the backup operation, and will be specified by the existing
   * connection.
   * @param outputStream The stream which will receive the server contents.
   */
  public Backup(OutputStream outputStream) {
    this(null, null, true);
    setOverrideOutputStream(outputStream);
  }
  
  /**
   * The destination of a backup command is a database, not a graph.
   * @return The URI of the server, or <code>null</code> if the server will be found from
   * an existing connection.
   */
  @Override
  public URI getServerURI() {
    return getSource();
  }

  /**
   * Perform a backup on a server.
   * @param conn The connection to talk to the server on.
   * @return The text describing the server that was backed up.
   * @throws QueryException There was an error asking the server to perform the backup.
   * @throws MalformedURLException The destination is not a valid file.
   */
  public Object execute(Connection conn) throws QueryException {
    URI src = getSource();
    URI dest = getDestination();
    if (serverTest(src)) throw new QueryException("Cannot back up a graph. Must be a server URI.");

    if (isLocal() && !conn.isRemote()) {
      logger.error("Used a LOCAL modifier when backing up <" + src + "> to <" + dest + "> on a non-remote server.");
      throw new QueryException("LOCAL modifier is not valid for BACKUP command when not using a client-server connection.");
    }

    try {
      if (isLocal()) {
        getMarshalledData(conn);
      } else {
        doTx(conn, dest);
      }
      
      if (logger.isDebugEnabled()) logger.debug("Completed backing up " + src + " to " + dest);
      
      return setResultMessage("Successfully backed up " + src + " to " + dest + ".");
      
    } catch (IOException ioe) {
      logger.error("Error attempting to back up: " + dest, ioe);
      throw new QueryException("Error attempting to back up: " + dest, ioe);
    }
  }


  /**
   * Public interface to perform a backup into an output stream.
   * This is callable directly, without an AST interface.
   * @param conn The connection to a server to be backed up.
   * @param serverURI The URI describing the server to back up.
   * @param outputStream The output which will receive the data to be backed up.
   * @throws QueryException There was an error asking the server to perform the backup.
   */
  public static void backup(Connection conn, URI serverURI, OutputStream outputStream) throws QueryException {
    Backup backup = new Backup(serverURI, null, true);
    backup.setOverrideOutputStream(outputStream);
    backup.execute(conn);
  }

  
  /* (non-Javadoc)
   * @see org.mulgara.query.operation.DataOutputTx#getOp(java.io.OutputStream)
   */
  @Override
  protected SessionOp<Object,QueryException> getOp(final OutputStream outputStream) {
    return new SessionOp<Object,QueryException>() {
      public Object fn(Session session) throws QueryException {
        session.backup(outputStream);
        return null;
      }
    };
  }

  
  /* (non-Javadoc)
   * @see org.mulgara.query.operation.DataOutputTx#getOp(java.net.URI)
   */
  @Override
  protected SessionOp<Object,QueryException> getOp(final URI destUri) {
    return new SessionOp<Object,QueryException>() {
      public Object fn(Session session) throws QueryException {
        session.backup(destUri);
        return null;
      }
    };
  }

}
