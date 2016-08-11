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
import java.io.InputStream;
import java.net.URI;

import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;

/**
 * Represents a command to restore a server from backup data.
 *
 * @created Aug 19, 2007
 * @author Paula Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Restore extends DataInputTx {

  /**
   * Creates a restore operation, restoring the server from backup data at the given location.
   * 
   * This constructor is deprecated. The server URI is not part of the operation
   * and is only present to support the TqlAutoInterpreter legacy code. Use
   * {@link #Restore(URI, boolean)} or {@link #Restore(InputStream)} instead.
   * 
   * @param source The location of the backup data to restore from.
   * @param serverURI The URI of the server to restore (may be null if the operation is being
   *        executed on an existing connection).
   * @param local Locality of the backup data.
   */
  @Deprecated
  public Restore(URI source, URI serverURI, boolean local) {
    super(source, serverURI, serverURI, local);
  }
  
  /**
   * Creates a command to restore a server from a source file.  This is the preferred
   * constructor for API calls that use their own server connections.  The server URI
   * is not an argument for the restore operation, and will be specified by the existing
   * connection.
   * @param source The source file URI for the restore data.
   * @param local The locality of the source file (<code>true</code> is client
   *        file system, <code>false</code> is server file system).
   */
  public Restore(URI source, boolean local) {
    this(source, null, local);
  }
  
  /**
   * Creates a command to restore a server from an input stream.  This is the preferred
   * constructor for API calls that use their own server connections.  The server URI
   * is not an argument for the restore operation, and will be specified by the existing
   * connection.
   * @param inputStream The input stream that will provide the restore contents.
   */
  public Restore(InputStream inputStream) {
    this(null, null, true);
    setOverrideInputStream(inputStream);
  }

  /**
   * The destination of a restore command is a database, not a graph.
   * @return The URI of the destination server, or <code>null</code> if the server will be found from
   * an existing connection.
   */
  @Override
  public URI getServerURI() {
    return getDestination();
  }

  /**
   * Restore the data into the destination graph through the given connection.
   * @param conn The connection to restore the data over.
   * @return A text string describing the operation.
   */
  public Object execute(Connection conn) throws QueryException {
    URI src = getSource();
    URI dest = getDestination();
    if (serverTest(dest)) throw new QueryException("Cannot restore to a graph. Must be a server URI.");

    if (isLocal() && !conn.isRemote()) {
      logger.error("Used a LOCAL modifier when restoring <" + src + "> to <" + dest + "> on a non-remote server.");
      throw new QueryException("LOCAL modifier is not valid for RESTORE command when not using a client-server connection.");
    }

    try {
      if (isLocal()) sendMarshalledData(conn, false);
      else doTx(conn, src);

      String message;
      if (dest == null) message = "Successfully restored from " + src;
      else message = "Successfully restored " + dest + " from " + src;

      if (logger.isDebugEnabled()) logger.debug(message);
  
      return setResultMessage(message);

    } catch (IOException ex) {
      logger.error("Error attempting to restore: " + src, ex);
      throw new QueryException("Error attempting to restore: " + src, ex);
    }
  }

  /* (non-Javadoc)
   * @see org.mulgara.query.operation.DataInputTx#getExecutable(java.io.InputStream)
   */
  @Override
  protected SessionOp<Long,QueryException> getExecutable(final InputStream inputStream) {
    return new SessionOp<Long,QueryException>() {
      public Long fn(Session session) throws QueryException {
        session.restore(inputStream, getSource());
        return 0L;
      }
    };
  }

  /* (non-Javadoc)
   * @see org.mulgara.query.operation.DataInputTx#getExecutable(java.lang.Object)
   */
  @Override
  protected SessionOp<Long,QueryException> getExecutable(final URI srcUri) {
    return new SessionOp<Long,QueryException>() {
      public Long fn(Session session) throws QueryException {
        session.restore(srcUri);
        return 0L;
      }
    };
  }

}
