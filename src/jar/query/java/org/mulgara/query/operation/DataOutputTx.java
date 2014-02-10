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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.NoSuchObjectException;

import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.QueryException;
import org.mulgara.util.Rmi;

import edu.emory.mathcs.util.remote.io.RemoteOutputStream;
import edu.emory.mathcs.util.remote.io.server.impl.RemoteOutputStreamSrvImpl;

/**
 * Represents a command to move data out of a graph (Export) or server (Backup).
 * 
 * @created Jun 27, 2008
 * @author Alex Hall
 * @copyright &copy; 2008 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class DataOutputTx extends DataTx {

  /** String constant for the file URL protocol. */
  protected static final String FILE_SCHEME = "file";

  /** A stream to enable an API to export or backup data directly. */
  private OutputStream overrideOutputStream = null;

  /**
   * Create a new data transfer command for moving data otu of a graph or server.
   * If local is <code>true</code> then destination may be null, but an overriding output 
   * stream must be set before executing the operation.
   * @param source The graph or server to get data from.
   * @param destination The destination of the graph or server content.
   * @param serverGraphURI The URI of the server or graph being operated on.  This
   *        parameter is primarily for use by the TqlAutoInterpreter for discovering
   *        server URI's of commands, and may be omitted if working directly with an
   *        existing {@link Connection}.
   * @param local If <code>true</code>, the destination will be a file or stream on the 
   *        local system that is marshalled from the remote server.  If <code>false</code>, 
   *        it will be a file on the remote server filesystem. 
   */
  public DataOutputTx(URI source, URI destination, URI serverGraphURI, boolean local) {
    super(source, destination, serverGraphURI, local);
    if (!local && destination == null) throw new IllegalArgumentException("Need a valid remote destination");
    if (destination != null && !destination.getScheme().equals(FILE_SCHEME)) {
      throw new IllegalArgumentException("Output must be sent to a file");
    }
  }

  
  /**
   * Allows an API to set an output stream for exporting or backing up, instead of
   * getting it from the destination URI.
   * @param overrideStream The stream to use as the destination.
   */
  public void setOverrideOutputStream(OutputStream overrideStream) {
    this.overrideOutputStream = overrideStream;
  }

  
  /**
   * Perform the output transfer with the configured datastream.
   */
  protected void doTx(Connection conn, OutputStream outputStream) throws QueryException {
    conn.execute(getOp(outputStream));
  }
  
  /**
   * Perform the output transfer to the configured destination URI.
   */
  protected void doTx(Connection conn, URI destUri) throws QueryException {
    conn.execute(getOp(destUri));
  }

  /**
   * Get the operation that will be trasnfer to the output stream.
   */
  protected abstract SessionOp<Object,QueryException> getOp(final OutputStream outputStream);

  /**
   * Get the operation that will be transfer to the destination URI.
   */
  protected abstract SessionOp<Object,QueryException> getOp(final URI destUri);

  /**
   * Wrap the local destination object data (output stream or file URI) in an RMI object for marshalling, 
   * and receive over the connection. Delegates to the {@link #doTx(Connection, OutputStream)}
   * abstract method to send the data over the connection.
   * @param conn The connection to the server.
   * @throws QueryException There was an error working with data at the server end.
   * @throws IOException There was an error transferring data over the network.
   */
  protected void getMarshalledData(Connection conn) throws QueryException, IOException {
    if (logger.isInfoEnabled()) logger.info("Receiving local resource : " + getDestination());
    
    RemoteOutputStreamSrvImpl srv = null;
    RemoteOutputStream remoteOutputStream = null;
    
    try {
      OutputStream outputStream = getLocalOutputStream();
      
      // open and wrap the output stream
      srv = new RemoteOutputStreamSrvImpl(outputStream);
      Rmi.export(srv);
      remoteOutputStream = new RemoteOutputStream(srv);
      
      // call back to the implementing class
      doTx(conn, remoteOutputStream);
      
    } finally {
      // cleanup the output
      if (remoteOutputStream != null) {
        try {
          remoteOutputStream.close();
        } catch (IOException ioe ) { 
          logger.warn("Unable to cleanly close remote data stream", ioe);
        }
      }
      
      // cleanup the RMI for the output stream
      if (srv != null) {
        try {
          Rmi.unexportObject(srv, false);
        } catch (NoSuchObjectException ex) { /* nothing to clean up, so continue */ };
        try {
          srv.close();
        } catch (IOException e) {
          logger.warn("Unable to cleanly close data stream to remote object", e);
        }
      }
    }
  }
  
  
  /**
   * Gets the local output stream for an Export or Backup operation.  If an output stream has been
   * specified by {@link #setOverrideOutputStream(OutputStream)} then it will be returned; otherwise,
   * an output stream will be opened for the destination file URI.
   * @return A stream for the local data destination.
   * @throws QueryException If no valid data destination was set.
   * @throws IOException If an error occurred opening the local destination.
   */
  private OutputStream getLocalOutputStream() throws QueryException, IOException {
    // Use provided output stream if there is one.
    OutputStream stream = overrideOutputStream;
    
    if (stream == null) {
      // No output stream was provided, need to open from local destination URI.
      URI destUri = getDestination();
      if (destUri == null) {
        throw new QueryException("Attempt to execute data operation without a valid local destination");
      }
      
      String destinationFile = this.getDestination().toURL().getPath();
      try {
        stream = new FileOutputStream(destinationFile);
      } catch (FileNotFoundException ex) {
        throw new QueryException("File " + destinationFile + " cannot be created for output.", ex);
      }
    }
    
    return stream;
  }

}
