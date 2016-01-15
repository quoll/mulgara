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
import org.mulgara.server.NonRemoteSessionException;
import org.mulgara.server.Session;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;
import org.mulgara.server.driver.SessionFactoryFinderException;
import org.neilja.net.interruptiblermi.InterruptibleRMIThreadFactory;

/**
 * A connection for sending commands to a server using a session object.
 *
 * @created 2007-08-21
 * @author Paula Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SessionConnection extends CommandExecutor implements Connection {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(SessionConnection.class.getName());
  
  /** Thread factory used to proxy session operations when interruptible RMI is enabled. */
  private static final ThreadFactory interruptibleFactory = InterruptibleRMIThreadFactory.getInstance();
  
  /** Flag to control whether interruptible operations on remote RMI sessions are supported. */
  private final boolean useInterruptibleRmi;
  
  /** The URI for the server to establish a session on. */
  private URI serverUri;
  
  /** The security domain URI. */
  private URI securityDomainUri;
  
  /** The session to use for this connection. */
  Session session;
  
  /** The factory used to create this connection */
  private ConnectionFactory factory = null;
  
  /** Indicates the current autocommit state */
  private boolean autoCommit = true;
  
  /** Indicates the connection has been closed */
  private boolean closed = false;

  /**
   * Creates a new connection, given a URI to a server.
   * @param serverUri The URI to connect to.
   * @throws ConnectionException There was a problem establishing the details needed for a connection.
   */
  public SessionConnection(URI serverUri) throws ConnectionException {
    this(serverUri, true);
  }


  /**
   * Creates a new connection, given a URI to a server,
   * and a flag to indicate if the server should be "remote".
   * @param serverUri The URI to connect to.
   * @param isRemote <code>true</code> for a remote session, <code>false</code> for local.
   * @throws ConnectionException There was a problem establishing the details needed for a connection.
   */
  public SessionConnection(URI serverUri, boolean isRemote) throws ConnectionException {
    this(serverUri, isRemote, false);
  }


  /**
   * Creates a new connection, given a URI to a server, a flag to indicate if the server
   * should be "remote", and another to indicate whether to use interruptible RMI operations.
   * @param serverUri The URI to connect to.
   * @param isRemote <code>true</code> for a remote session, <code>false</code> for local.
   * @param useInterruptibleRmi <code>true</code> to support interruptible RMI operations on remote sessions.
   * @throws ConnectionException There was a problem establishing the details needed for a connection.
   */
  public SessionConnection(URI serverUri, boolean isRemote, boolean useInterruptibleRmi) throws ConnectionException {
    super(null);
    this.useInterruptibleRmi = useInterruptibleRmi;
    setServerUri(serverUri, isRemote);
  }


  /**
   * Creates a new connection, given a preassigned session.
   * @param session The session to connect with.
   */
  public SessionConnection(Session session) {
    this(session, null, null, false);
  }
  
  /**
   * Creates a new connection, given a preassigned session.
   * @param session The session to connect with.
   * @param securityDomainUri The security domain URI for the session
   */
  public SessionConnection(Session session, URI securityDomainUri) {
    this(session, securityDomainUri, null, false);
  }
  
  
  /**
   * Creates a new connection, given a preassigned session
   * @param session The session to connect with
   * @param securityDomainUri The security domain URI for the session
   * @param serverUri The server URI, needed for re-caching the session with the factory
   */
  public SessionConnection(Session session, URI securityDomainUri, URI serverUri) {
    this(session, securityDomainUri, serverUri, false);
  }
  
  
  /**
   * Creates a new connection, given a preassigned session
   * @param session The session to connect with
   * @param securityDomainUri The security domain URI for the session
   * @param serverUri The server URI, needed for re-caching the session with the factory
   * @param useInterruptibleRmi <code>true</code> to support interruptible RMI operations on remote sessions.
   */
  public SessionConnection(Session session, URI securityDomainUri, URI serverUri, boolean useInterruptibleRmi) {
    super(null);
    if (session == null) throw new IllegalArgumentException("Cannot create a connection without a server.");
    this.useInterruptibleRmi = useInterruptibleRmi;
    setSession(session, securityDomainUri, serverUri);    
  }
  
    
  /**
   * If a Connection was abandoned by the client without being closed first, attempt to
   * reclaim the session for use by future clients.
   */
  protected void finalize() throws Throwable {
    try {
      if (!closed) {
        close();
      }
    } finally {
      super.finalize();
    }
  }
  
  /**
   * Used to set a reference back to the factory that created it.  If the factory
   * reference is set, then the session will be re-cached when this connection is closed.
   * @param factory The factory that created this connection.
   */
  void setFactory(ConnectionFactory factory) {
    this.factory = factory;
  }
  
  
  /**
   * Give login credentials and security domain to the current session.  This should only be needed
   * once since the session does not change.
   * @param securityDomainUri The security domain for the login.
   * @param user The username.
   * @param password The password for the given username.
   */
  public void setCredentials(URI securityDomainUri, String user, char[] password) {
    checkState();
    if (securityDomainUri == null) throw new IllegalArgumentException("Must have a security domain to yuse credentials");
    this.securityDomainUri = securityDomainUri;
    setCredentials(user, password);
  }


  /**
   * Give login credentials for the current security domain to the current session.
   * This should only be needed
   * once since the session does not change.
   * @param user The username.
   * @param password The password for the given username.
   */
  public void setCredentials(String user, char[] password) {
    checkState();
    if (securityDomainUri == null) throw new IllegalArgumentException("Must have a security domain to yuse credentials");
    session.login(securityDomainUri, user, password);
  }


  /**
   * @return the session
   */
  public Session getSession() {
    checkState();
    return session;
  }


  /**
   * Starts and commits transactions on this connection, by turning the autocommit
   * flag on and off. 
   * @param autoCommit <code>true</code> if the flag is to be on.
   * @throws QueryException The session could not change state.
   */
  public void setAutoCommit(boolean autoCommit) throws QueryException {
    checkState();
    if (this.autoCommit != autoCommit) {
      this.autoCommit = autoCommit;
      session.setAutoCommit(autoCommit);
    }
  }


  /**
   * @return the autoCommit value
   */
  public boolean getAutoCommit() {
    checkState();
    return autoCommit;
  }


  /**
   * Closes the current connection.
   */
  public void close() throws QueryException {
    checkState();
    closed = true;
    
    if (factory != null) {
      factory.releaseSession(serverUri, session);
    }
  }
  
  
  /**
   * Disposes of the current connection and any underlying resources.
   */
  public void dispose() throws QueryException {
    checkState();
    closed = true;
    
    if (factory != null) {
      factory.disposeSession(session);
    }
    
    if (session != null) {
      session.close();
      session = null;
    }
  }

  // Private methods //

  /**
   * @return the serverUri
   */
  URI getServerUri() {
    return serverUri;
  }


  /**
   * @return the securityDomainUri
   */
  URI getSecurityDomainUri() {
    return securityDomainUri;
  }

  
  /**
   * Throws an IllegalStateException if the connection has already been closed.
   */
  private void checkState() {
    if (closed) {
      throw new IllegalStateException("Attempt to access a closed connection");
    }
  }

  /**
   * Sets the session information for this connection
   * @param session The session to set to.
   * @param securityDomainUri The security domain to use for the session.
   * @param serverUri The server the session is connected to.
   */
  private void setSession(Session session, URI securityDomainUri, URI serverUri) {
    this.session = session;
    this.securityDomainUri = securityDomainUri;
    this.serverUri = serverUri;
    if (this.useInterruptibleRmi && !session.isLocal()) {
      setThreadFactory(interruptibleFactory);
    }
    if (logger.isDebugEnabled()) logger.debug("Set server URI to: " + serverUri);
  }


  /**
   * Establishes a session for this connection.
   * @param uri The URI to set for the server.
   * @param isRemote <code>true</code> for a remote session, <code>false</code> for local.
   * @throws ConnectionException There was a problem establishing a session.
   */
  private void setServerUri(URI uri, boolean isRemote) throws ConnectionException {
    
    try {
      if (uri == null) {
        // no model given, and the factory didn't cache a connection, so make one up.
        uri = SessionFactoryFinder.findServerURI();
      }

      if (logger.isDebugEnabled()) logger.debug("Finding session factory for " + uri);
      
      SessionFactory sessionFactory = SessionFactoryFinder.newSessionFactory(uri, isRemote);
      if (logger.isDebugEnabled()) logger.debug("Found " + sessionFactory.getClass() +
          " session factory, obtaining session with " + uri);

      // create a new session and set this connection to it
      if (securityDomainUri == null) securityDomainUri = sessionFactory.getSecurityDomain();
      setSession(sessionFactory.newSession(), sessionFactory.getSecurityDomain(), uri);

    } catch (SessionFactoryFinderException e) {
      throw new ConnectionException("Unable to connect to a server", e);
    } catch (NonRemoteSessionException e) {
      throw new ConnectionException("Error connecting to the local server", e);
    } catch (QueryException e) {
      throw new ConnectionException("Data error in connection attempt", e);
    }
    assert session != null;
  }


  /**
   * Tests if the Connection is being conducted over a network.
   * @return <code>true</code> if the underlying session is not local.
   */
  public boolean isRemote() {
    return session != null && !session.isLocal();
  }


  /**
   * Provides access to a JenaConnection. This interface is isolated from this class
   * to avoid needing Jena on the classpath.
   * @return A new JenaConnection object which refers back to this object.
   */
  public JenaConnection getJenaConnection() {
    return new JenaConnectionImpl(this);
  }

}
