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

package org.mulgara.connection;

import java.net.URI;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * A connection for accepting state changes at the local end with no server involvement
 *
 * @created 2007-09-25
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class DummyConnection extends CommandExecutor implements Connection {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(DummyConnection.class.getName());
  
  /** Indicates the current autocommit state */
  private boolean autoCommit = true;

  /**
   * Creates a new connection.
   */
  public DummyConnection() {
    this(null);
  }


  /**
   * Creates a new connection, using the given factory for proxy threads.
   * This constructor is mostly for testing purposes; operations on a dummy connection
   * are executed locally and don't need a proxy.
   * @param threadFactory
   */
  public DummyConnection(ThreadFactory threadFactory) {
    super(threadFactory);
  }


  /**
   * Give login credentials and security domain to a session.  This operation is ignored.
   * @param securityDomainUri The security domain for the login.
   * @param user The username.
   * @param password The password for the given username.
   */
  public void setCredentials(URI securityDomainUri, String user, char[] password) {
    logger.warn("Setting credentials on a dummy connection");
  }


  /**
   * Give login credentials for the current security domain to the current session.
   * This operation is ignored.
   * @param user The username.
   * @param password The password for the given username.
   */
  public void setCredentials(String user, char[] password) {
    logger.warn("Setting credentials on a dummy connection");
  }


  /**
   * @return always null
   */
  public Session getSession() {
    return null;
  }


  /**
   * Starts and commits transactions on this connection, by turning the autocommit
   * flag on and off. 
   * @param autoCommit <code>true</code> if the flag is to be on.
   * @throws QueryException The session could not change state.
   */
  public void setAutoCommit(boolean autoCommit) throws QueryException {
    this.autoCommit = autoCommit;
  }


  /**
   * @return the autoCommit value
   */
  public boolean getAutoCommit() {
    return autoCommit;
  }


  /**
   * Closes the current connection.  Does nothing for this class.
   */
  public void close() throws QueryException {
  }
  
  
  /**
   * Disposes of the current connection.  Does nothing for this class.
   */
  public void dispose() throws QueryException {
  }


  /**
   * Always returns <code>false</code>.
   */
  public boolean isRemote() {
    return false;
  }


  public Graph connectGraph(String graphURI) {
    throw new UnsupportedOperationException();
  }


  public Graph connectGraph(String graphURI, boolean createIfDoesNotExist) {
    throw new UnsupportedOperationException();
  }


  public Graph connectGraph(URI graphURI, boolean createIfDoesNotExist) {
    throw new UnsupportedOperationException();
  }


  public Model connectModel(String graphURI) {
    throw new UnsupportedOperationException();
  }


  public Model connectModel(String graphURI, boolean createIfDoesNotExist) {
    throw new UnsupportedOperationException();
  }


  public Model connectModel(URI graphURI, boolean createIfDoesNotExist) {
    throw new UnsupportedOperationException();
  }


  public Graph createGraph(String graphURI) {
    throw new UnsupportedOperationException();
  }


  public Model createModel(String graphURI) {
    throw new UnsupportedOperationException();
  }


  public void dropGraph(String graphURI) {
    throw new UnsupportedOperationException();
  }


  public void dropGraph(URI graphURI) {
    throw new UnsupportedOperationException();
  }
  
}
