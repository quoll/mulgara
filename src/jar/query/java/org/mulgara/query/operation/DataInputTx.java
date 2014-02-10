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
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.rmi.NoSuchObjectException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.QueryException;
import org.mulgara.util.Rmi;

import edu.emory.mathcs.util.remote.io.RemoteInputStream;
import edu.emory.mathcs.util.remote.io.server.impl.RemoteInputStreamSrvImpl;

/**
 * Represents a command to move data into a graph (Load) or server (Restore).
 * @param SourceType The type of object that provides the input for the command.
 * 
 * @created Jun 27, 2008
 * @author Alex Hall
 * @copyright &copy; 2008 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class DataInputTx extends DataTx {

  /** String constant for the extension of gzip files. */
  private static final String GZIP_EXTENSION = ".gz";

  /** String constant for the extension of zip files. */
  private static final String ZIP_EXTENSION = ".zip";

  /** A stream to enable an API to load or restore data directly. */
  protected InputStream overrideInputStream = null;
  
  /**
   * Create a new data transfer command for moving data into a graph or server.
   * If local is <code>true</code> then source may be null, but an overriding input 
   * stream must be set before executing the operation.
   * @param source The source of data to insert.
   * @param destination The graph or server to load data into.
   * @param serverGraphURI The URI of the server or graph being operated on.  This
   *        parameter is primarily for use by the TqlAutoInterpreter for discovering
   *        server URI's of commands, and may be omitted if working directly with an
   *        existing {@link Connection}.
   * @param local If <code>true</code>, the source will be a file or stream on the 
   *        local system that is marshalled to the remote server.  If <code>false</code>, 
   *        it will be a file on the remote server filesystem. 
   */
  public DataInputTx(URI source, URI destination, URI serverGraphURI, boolean local) {
    super(source, destination, serverGraphURI, local);
    if (!local && source == null) throw new IllegalArgumentException("Need a valid remote source");
  }

  
  /**
   * Allows an API to set an input stream for loading or restoring, instead of 
   * getting it from the source URI.
   * @param overrideStream The stream to use as the data source.
   */
  public void setOverrideInputStream(InputStream overrideStream) {
    this.overrideInputStream = overrideStream;
  }
  
  
  /**
   * Perform the input transfer with the configured datastream.
   * @return The number of statements affected, or <code>null</code> if this is not relevant.
   */
  protected Long doTx(Connection conn, InputStream inputStream) throws QueryException {
    return conn.execute(getExecutable(inputStream));
  }
  
  /**
   * Perform the input transfer with the configured source.
   */
  protected Long doTx(Connection conn, URI src) throws QueryException {
    return conn.execute(getExecutable(src));
  }
  
  /**
   * Get the operation that will transfer from the given source stream.
   */
  protected abstract SessionOp<Long,QueryException> getExecutable(InputStream inputStream);
  
  /**
   * Get the operation that will transfer from the given source object.
   */
  protected abstract SessionOp<Long,QueryException> getExecutable(URI src);
  
  /**
   * Wrap the local source data (input stream or file URI) in an RMI object for marshalling, 
   * and send over the connection. Used by Load and Restore. Delegates to the {@link #doTx(Connection, InputStream)}
   * abstract method to send the data over the connection.
   * @param conn The connection to the server.
   * @param compressable If <code>true</code> and the source is a file URI, file decompression will be
   *        applied to the contents of the source file before sending over the connection.
   * @return The number of statements inserted.
   * @throws QueryException There was an error working with data at the server end.
   * @throws IOException There was an error transferring data over the network.
   */
  protected long sendMarshalledData(Connection conn, boolean compressable) throws QueryException, IOException {
    if (logger.isInfoEnabled()) logger.info("Sending local resource : " + getSource());

    InputStream inputStream = getLocalInputStream(compressable);

    // If the connection is local, then no need to wrap
    if (!conn.isRemote()) {
      try {
        return doTx(conn, inputStream);
      } finally {
        inputStream.close();
      }
    }

    RemoteInputStreamSrvImpl srv = null;
    RemoteInputStream remoteInputStream = null;
    try {
      // open and wrap the inputstream
      srv = new RemoteInputStreamSrvImpl(inputStream);
      Rmi.export(srv);
      remoteInputStream = new RemoteInputStream(srv);

      // call back to the implementing class
      return doTx(conn, remoteInputStream);

    } finally {
      // clean up the RMI object
      if (srv != null) {
        try {
          Rmi.unexportObject(srv, false);
        } catch (NoSuchObjectException ex) { /* nothing to clean up, so continue */ };
      }
      try {
        if (remoteInputStream != null) remoteInputStream.close();
      } catch (Exception e) {
        logger.warn("Unable to cleanly close remote data stream", e);
      }
    }
  }
  
  
  /**
   * Gets the local input stream for a Load or Restore operation.  If an input stream has been
   * specified by {@link #setOverrideInputStream(InputStream)} then it will be returned; otherwise,
   * an input stream will be opened for the source file URI.
   * @param compressable If <code>true</code> and the source is a file URI, file decompression will be
   *        applied to its contents before returning.
   * @return A stream for the local data source.
   * @throws QueryException If no valid data source was set.
   * @throws IOException If an error occurred opening the local source.
   */
  protected InputStream getLocalInputStream(boolean compressable) throws QueryException, IOException {
    // Use provided input stream if there is one.
    InputStream stream = overrideInputStream;
    
    if (stream == null) {
      // No input stream was provided, need to open from local source URI.
      URI sourceUri = getSource();
      if (sourceUri == null) {
        throw new QueryException("Attempt to execute data operation without a valid local source");
      }
      
      // is the file/stream compressed?
      URL sourceUrl = sourceUri.toURL();
      if (compressable) {
        stream = adjustForCompression(sourceUrl);
      } else {
        stream = sourceUrl.openStream();
      }
    }
    
    return stream;
  }
  
  
  /**
   * Gets a stream for a file URL.  Determines if the stream is compressed by inspecting
   * the fileName extension.
   *
   * @param fileLocation String The URL for the file being loaded
   * @throws IOException An error while reading from the input stream.
   * @return InputStream A new input stream which supplies uncompressed data.
   */
  private InputStream adjustForCompression(URL fileLocation) throws IOException {

    if (fileLocation == null) throw new IllegalArgumentException("File name is null");

    InputStream stream = fileLocation.openStream();

    // wrap the stream in a decompressor if the suffixes indicate this should happen.
    String fileName = fileLocation.toString();
    if (fileName.toLowerCase().endsWith(GZIP_EXTENSION)) {
      stream = new GZIPInputStream(stream);
    } else if (fileName.toLowerCase().endsWith(ZIP_EXTENSION)) {
      stream = new ZipInputStream(stream);
    }

    assert stream != null;
    return stream;
  }

}
