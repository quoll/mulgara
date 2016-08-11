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
import java.net.URISyntaxException;

import javax.activation.MimeType;

import org.apache.log4j.Logger;
import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.QueryException;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.server.Session;

/**
 * Represents a command to load data into a model.
 *
 * @created Aug 19, 2007
 * @author Paula Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Load extends DataInputTx {

  /** The logger */
  static final Logger logger = Logger.getLogger(Load.class.getName());

  /** The type of data that may be in a stream. */
  private final MimeType contentType;

  /**
   * Build a load operation, loading data from one URI into a graph specified by another URI.
   * @param sourceUri The URI of the source of the RDF data.
   * @param destinationUri The URI of the graph to receive the data.
   * @param local Set to <code>true</code> to indicate that the source is on the client system.
   */
  public Load(URI sourceUri, URI destinationUri, boolean local) {
    super(sourceUri, destinationUri, destinationUri, local);
    
    // Validate arguments.
    if (destinationUri == null) throw new IllegalArgumentException("Need a valid destination graph URI");
    contentType = null;
  }

  /**
   * Builds a load operation which will load data from the supplied input stream into the given destination graph.
   * One or both of content type and source URI must be provided in order for the server to determine
   * the format of the content (RDF/XML, Turtle, etc). If the source URI is given, it will be used as
   * the base URI for resolving relative URI references when parsing the content.
   * @param destinationUri The URI of the graph to receive the data.
   * @param stream The local input stream that is the source of the data to load.
   * @param contentType The MIME content type of the data, if known. If present, the MIME type will take
   *        precedence when selecting the parser to use.
   * @param sourceUri The source URI; this is not actually de-referenced, but if present will be used as
   *        the base URI for resolving relative URIs parsed from the content. If the content type is null
   *        or does not match a configured handler then the filename extension from the source URI will
   *        be used to select a parser.
   */
  public Load(URI destinationUri, InputStream stream, MimeType contentType, URI sourceUri) {
    super(sourceUri, destinationUri, destinationUri, true);
    
    // Validate arguments.
    if (destinationUri == null) throw new IllegalArgumentException("Need a valid destination graph URI");
    this.contentType = contentType;
    setOverrideInputStream(stream);
  }

  /**
   * Alternate constructor for creating a load operation whose source will be a local input stream,
   * without providing a base URI (this means relative URI references in the file will not be resolved).
   * @param graphURI The URI of the graph to receive the data.
   * @param stream The local input stream that is the source of data to load.
   * @param contentType the content type for the stream.
   */
  public Load(URI graphURI, InputStream stream, MimeType contentType) {
    this(graphURI, stream, contentType, (URI)null);
  }

  /**
   * Alternate constructor for creating a load operation whose source will be a local input stream,
   * and a filename has been provided. The filename will be used to construct the base URI for parsing.
   * @param graphURI The URI of the graph to receive the data.
   * @param stream The local input stream that is the source of data to load.
   * @param contentType the content type for the stream.
   * @param file A string form of the uri of the file to load; will be used to construct a base URI.
   */
  public Load(URI graphURI, InputStream stream, MimeType contentType, String file) {
    this(graphURI, stream, contentType, toUri(file));
  }

  /**
   * Load the data into the destination graph through the given connection.
   * @param conn The connection to load the data over.
   * @return The number of statements that were inserted.
   */
  public Object execute(Connection conn) throws QueryException {
    URI src = getSource();
    URI dest = getDestination();

    if (isLocal() && !conn.isRemote() && overrideInputStream == null) {
      logger.error("Used a LOCAL modifier when loading <" + src + "> to <" + dest + "> on a non-remote server.");
      throw new QueryException("LOCAL modifier is not valid for LOAD command when not using a client-server connection.");
    }

    try {
      long stmtCount = isLocal() ? sendMarshalledData(conn, true) : doTx(conn, getSource());
      if (logger.isDebugEnabled()) logger.debug("Loaded " + stmtCount + " statements from " + src + " into " + dest);
  
      if (stmtCount > 0L) setResultMessage("Successfully loaded " + stmtCount + " statements from " + 
          (src != null ? src : "input stream") + " into " + dest);
      else setResultMessage("WARNING: No valid RDF statements found in " + (src != null ? src : "input stream"));
      
      return stmtCount;
      
    } catch (IOException ex) {
      logger.error("Error attempting to load : " + src, ex);
      throw new QueryException("Error attempting to load : " + src, ex);
    }
  }


  /* (non-Javadoc)
   * @see org.mulgara.query.operation.DataInputTx#getExecutable(java.io.InputStream)
   */
  @Override
  protected SessionOp<Long,QueryException> getExecutable(final InputStream inputStream) {
    return new SessionOp<Long,QueryException>() {
      public Long fn(Session session) throws QueryException {
        return session.setModel(inputStream, getDestination(), getSource(), contentType);
      }
    };
  }


  /* (non-Javadoc)
   * @see org.mulgara.query.operation.DataInputTx#getExecutable(java.net.URI)
   */
  @Override
  protected SessionOp<Long,QueryException> getExecutable(final URI src) {
    return new SessionOp<Long,QueryException>() {
      public Long fn(Session session) throws QueryException {
        return session.setModel(getDestination(), src);
      }
    };
  }


  /**
   * Get the text of the command, or generate a virtual command if no text was parsed.
   * @return The query that created this command, or a generated query if no query exists.
   */
  public String getText() {
    String text = super.getText();
    if (text == null || text.length() == 0) text = "load <" + getSource() + "> into <" + getDestination() + ">";
    return text;
  }


  /**
   * Attempt to turn a filename into a URI. If unsuccessful return null.
   * @param filename The path for a file.
   * @return The URI for the file, or null if the filename could not be converted.
   */
  private static URI toUri(String filename) {
    if (filename == null) return null;
    try {
      return new URI(Mulgara.VIRTUAL_NS + filename);
    } catch (URISyntaxException e) {
      return null;
    }
  }
}
