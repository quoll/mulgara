/*
 * Copyright 2009 Revelytix.
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
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimeType;

import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;

/**
 * Represents a command to export data from a graph.
 *
 * @created Jun 23, 2008
 * @author Alex Hall
 * @copyright &copy; 2008 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Export extends DataOutputTx {

  /** Optional user-defined namespace prefix mappings. */
  private Map<String,URI> namespacePrefixes;
  
  /** Optional content type specifier. */
  private MimeType contentType = null;
  
  /**
   * Creates a new Export command, exporting data from the graph URI to a file or output stream.
   * @param graphURI The graph to export.
   * @param destination The location to export the data. Only file URLs supported at the moment.
   *        May be null if an output stream will be provided.
   * @param local Set to <code>true</code> to indicate that the source is on the client system.
   */
  public Export(URI graphURI, URI destination, boolean local) {
    super(graphURI, destination, graphURI, local);
    if (graphURI == null) throw new IllegalArgumentException("Need a valid source graph URI");
  }
  
  /**
   * Alternate constructor for creating a command to export data from a graph to an output stream.
   * @param graphURI  The graph to export.
   * @param outputStream The stream that will receive the contents of the exported graph.
   */
  public Export(URI graphURI, OutputStream outputStream) {
    this(graphURI, null, true);
    setOverrideOutputStream(outputStream);
  }
  
  public Export(URI graphURI, OutputStream outputStream, MimeType contentType) {
    this(graphURI, outputStream);
    this.contentType = contentType;
  }
  
  /**
   * Provide a set of namespace prefix mappings which will be used to pre-populate the namespace
   * prefix definitions in the exported RDF/XML.
   * @param prefixes A mapping of prefix string to namespace URI.
   */
  public void setNamespacePrefixes(Map<String,URI> prefixes) {
    namespacePrefixes = new HashMap<String,URI>(prefixes);
  }
  
  /**
   * Perform an export on a graph.
   * @param conn The connection to talk to the server on.
   * @return The text describing the graph that was exported.
   * @throws QueryException There was an error asking the server to perform the export.
   */
  public Object execute(Connection conn) throws QueryException {
    URI src = getSource();
    URI dest = getDestination();

    if (isLocal() && !conn.isRemote()) {
      logger.error("Used a LOCAL modifier when exporting <" + src + "> to <" + dest + "> on a non-remote server.");
      throw new QueryException("LOCAL modifier is not valid for EXPORT command when not using a client-server connection.");
    }

    try {
      if (isLocal()) {
        getMarshalledData(conn);
      } else {
        doTx(conn, dest);
      } 
      
      if (logger.isDebugEnabled()) logger.debug("Completed backing up " + src + " to " + dest);
      
      return setResultMessage("Successfully exported " + src + " to " + 
          (dest != null ? dest : "output stream") + ".");
    }
    catch (IOException ioe) {
      logger.error("Error attempting to export: " + src, ioe);
      throw new QueryException("Error attempting to export: " + src, ioe);
    }
  }
  
  /**
   * Public interface to perform an export into an output stream.
   * This is callable directly, without an AST interface.
   * @param conn The connection to a server to perform the export.
   * @param graphURI The URI describing the graph on the server to export.
   * @param outputStream The output which will receive the data to be exported.
   * @throws QueryException There was an error asking the server to perform the export.
   */
  public static void export(Connection conn, URI graphURI, OutputStream outputStream) throws QueryException {
    Export export = new Export(graphURI, null, true);
    export.setOverrideOutputStream(outputStream);
    export.execute(conn);
  }

  /* (non-Javadoc)
   * @see org.mulgara.query.operation.DataOutputTx#getOp(java.io.OutputStream)
   */
  @Override
  protected SessionOp<Object,QueryException> getOp(final OutputStream outputStream) {
    return new SessionOp<Object,QueryException>() {
      public Object fn(Session session) throws QueryException {
        session.export(getSource(), outputStream, namespacePrefixes, contentType);
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
        session.export(getSource(), destUri, namespacePrefixes);
        return null;
      }
    };
  }

}
