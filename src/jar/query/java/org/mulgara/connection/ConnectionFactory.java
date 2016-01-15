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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;
import org.mulgara.util.Rmi;


/**
 * Creates new connections or reloads from a cache when possible connections.
 * This class is designed to be thread-safe, so that connections obtained from a factory
 * simultaneously from different threads will be backed by different Sessions and will not
 * interfere with each other.  When a connection is closed, it will release its underlying
 * Session back to factory to be added to a cache for re-use by other clients.
 * This factory must NOT be shared between users, as it is designed to cache security credentials!
 *
 * @created 2007-08-21
 * @author Paula Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ConnectionFactory {
  
  /** The logger. */
  private final static Logger logger = Logger.getLogger(ConnectionFactory.class.getName());

  /** String constant for localhost */
  private final static String LOCALHOST_NAME = "localhost";
  
  /** IP constant for localhost, saved as a string */
  private final static String LOCALHOST_IP = "127.0.0.1";
  
  /** Canonical hostname, used to normalize RMI connections on localhost */
  private static String LOCALHOST_CANONICAL;
  
  /** The scheme name for the local protocol */
  private final static String LOCAL_PROTOCOL = "local";
  
  /** The scheme name for the RMI protocol */
  private final static String RMI_PROTOCOL = "rmi";
  
  /** The list of known protocols. */
  private final static String[] PROTOCOLS = { RMI_PROTOCOL, "beep", LOCAL_PROTOCOL };
  
  /** The list of known local host aliases. */
  private final static List<String> LOCALHOSTS = new LinkedList<String>();
  
  /** Initialize the list of local host aliases. */
  static {
    LOCALHOSTS.add(LOCALHOST_NAME);
    LOCALHOSTS.add(LOCALHOST_IP);
    try {
      LOCALHOSTS.add(InetAddress.getLocalHost().getHostAddress());
      LOCALHOSTS.add(InetAddress.getLocalHost().getHostName());
      String name = InetAddress.getLocalHost().getCanonicalHostName();
      LOCALHOSTS.add(name);
      LOCALHOST_CANONICAL = name;
    } catch (UnknownHostException e) {
      LOCALHOST_CANONICAL = LOCALHOST_NAME;
      logger.error("Unable to get local host address", e);
    }
  }
  
  /** Cache to hold Sessions that have been released by closed connections. */
  private Map<URI,Set<Session>> cacheOnUri;
  
  /**
   * Maintain references to all active sessions to prevent them from being
   * garbage-collected.  This is necessary because we attempt to reclaim sessions from
   * connections that have been abandoned but not closed.
   */
  private Set<Session> sessionsInUse;
  
  /** Flag to determine whether to use interruptible RMI operations for remote session connections. */
  private final boolean useInterruptibleRmi;
  
  /**
   * Default constructor. Uses the configured system default behavior for interruptible RMI.
   */
  public ConnectionFactory() {
    this(Rmi.getDefaultInterrupt());
  }
  

  /**
   * Construct a connection factory, with optional support for interruptible RMI operations
   * on remote session connections.
   * @param useInterruptibleRmi If <tt>true</tt>, then the connections created by this
   * factory will support interrupting RMI operations. This behavior must also be enabled
   * on the server in order to take advantage of this feature.
   */
  public ConnectionFactory(boolean useInterruptibleRmi) {
    this.useInterruptibleRmi = useInterruptibleRmi;
    cacheOnUri = new HashMap<URI,Set<Session>>();
    sessionsInUse = new HashSet<Session>();
  }
  

  /**
   * Retrieve a connection based on a server URI.  If there is already a cached Session
   * for the server URI, it will be used; otherwise a new Session will be created when
   * the SessionConnection is instantiated.
   * @param serverUri The URI to get the connection to.
   * @return The new Connection.
   * @throws ConnectionException There was an error getting a connection.
   */
  public Connection newConnection(URI serverUri) throws ConnectionException {
    SessionConnection c = null;
    Session session = null;
    
    // Try to map all addresses for localhost to the same server URI so they can share Sessions
    serverUri = normalizeLocalUri(serverUri);
    
    synchronized(cacheOnUri) {
      session = getFromCache(serverUri);
    }
    
    // Let the existing re-try mecanism attempt to re-establish connectivity if necessary.
    if (session != null && !isSessionValid(session)) {
      session = null;
    }
      
    if (session == null) {
      boolean isRemote = !isLocalServer(serverUri);
      c = new SessionConnection(serverUri, isRemote, useInterruptibleRmi);
    } else {
      c = new SessionConnection(session, null, serverUri, useInterruptibleRmi);
    }
    c.setFactory(this);
    
    // Maintain a reference to prevent garbage collection of the Session.
    synchronized(cacheOnUri) {
      sessionsInUse.add(c.getSession());
    }
     
    return c;
  }


  /**
   * Retrieve a connection for a given session.  This method bypasses the cache altogether
   * and it is the responsibility of the client to manage the lifecycle of Connections and
   * Sessions used with this method.
   * @param session The Session the Connection will use..
   * @return The new Connection.
   * @throws ConnectionException There was an error getting a connection.
   */
  public Connection newConnection(Session session) throws ConnectionException {
    return new SessionConnection(session, null, null, useInterruptibleRmi);
  }

  
  /**
   * Close all Sessions cached by this factory. Sessions belonging to connections which are
   * still in use will not be affected. Exceptions are logged, but not acted on.
   */
  public void closeAll() {
    Set<Session> sessionsToClose = null;
    synchronized(cacheOnUri) {
      sessionsToClose = clearCache();
      sessionsToClose.addAll(sessionsInUse);
      sessionsInUse.clear();
    }
    safeCloseAll(sessionsToClose);
  }


  /**
   * Closes all sessions in a collection. Exceptions are logged, but not acted on.
   * @param sessions The sessions to close.
   */
  private void safeCloseAll(Iterable<Session> sessions) {
    for (Session s: sessions) {
      try {
        s.close();
      } catch (QueryException qe) {
        logger.warn("Unable to close session", qe);
      }
    }
  }
  
  
  /**
   * Returns a session to the cache to be re-used by new connections.  Removes it from the
   * list of active sessions.
   * @param serverUri The URI of the 
   */
  void releaseSession(URI serverUri, Session session) {
    synchronized(cacheOnUri) {
      addToCache(serverUri, session);
      // The session is now referenced by the cache, no need to hold on to a second reference
      sessionsInUse.remove(session);
    }
  }
  
  
  /**
   * Remove the session from the list of active sessions so it may be garbage-collected.
   */
  void disposeSession(Session session) {
    synchronized(cacheOnUri) {
      // The session was closed by the SessionConnection, no need to hold on to it any more.
      sessionsInUse.remove(session);
    }
  }
  
  
  /**
   * If the given server URI uses the RMI scheme and the host is an alias for localhost,
   * then attempt to construct a canonical server URI.  The purpose of this method is to
   * allow multiple aliased URI's to the same server to share the same cached Sessions.
   * @param serverUri A server URI
   * @return The normalized server URI.
   */
  public static URI normalizeLocalUri(URI serverUri)
  {
    if (serverUri == null) {
      return null;
    }
    
    URI normalized = serverUri;
    
    if (RMI_PROTOCOL.equals(serverUri.getScheme())) {
      String host = serverUri.getHost();
      
      boolean isLocal = false;
      for (String h : LOCALHOSTS) {
        if (h.equalsIgnoreCase(host)) {
          isLocal = true;
          break;
        }
      }
      
      if (isLocal) {
        try {
          normalized = new URI(RMI_PROTOCOL, null, LOCALHOST_CANONICAL, serverUri.getPort(), 
              serverUri.getPath(), serverUri.getQuery(), serverUri.getFragment());
        } catch (URISyntaxException use) {
          logger.info("Error normalizing server URI to local host", use);
        }
      }
    }
    
    return normalized;
  }
  

  /**
   * Test if a given URI is a local URI.
   * @param serverUri The URI to test.
   * @return <code>true</code> if the URI is local.
   */
  static boolean isLocalServer(URI serverUri) {
    if (serverUri == null) return false;

    String scheme = serverUri.getScheme();
    if (LOCAL_PROTOCOL.equals(scheme)) return true;
    
    // check for known protocols
    boolean found = false;
    for (String protocol: PROTOCOLS) {
      if (protocol.equals(serverUri.getScheme())) {
        found = true;
        break;
      }
    }
    if (found == false) return false;

    // protocol found.  Now test if the host appears in the localhost list
    String host = serverUri.getHost();
    for (String h: LOCALHOSTS) if (h.equalsIgnoreCase(host)) return true;

    // no matching hostnames
    return false;
  }
  
  
  /**
   * Tests whether the given cached Session is still valid.  This method uses the
   * {@link Session#ping()} method to check connectivity with the remote server, and relies
   * on the retry mechanism build into the remote session proxy to re-establish connectivity
   * if it is lost.
   * @param session A session.
   * @return <code>true</code> if connectivity on the session was established.
   */
  static boolean isSessionValid(Session session) {
    boolean valid;
    try {
      valid = session.ping();
    }
    catch (QueryException qe) {
      logger.info("Error establishing connection with remote session", qe);
      valid = false;
    }
    return valid;
  }


  /**
   * Retrieves a cached session for the given server URI.  If multiple sessions were
   * cached for this URI, the first one found is returned in no particular order.  The
   * calling code is responsible for synchronizing access to this method.  If a session is
   * found, then it is removed from the cache and returned.
   * @param serverURI A server URI
   * @return A cached session for the server URI, or <code>null</code> if none was found.
   */
  private Session getFromCache(URI serverURI) {
    Session session = null;
    
    Set<Session> sessions = cacheOnUri.get(serverURI);
    if (sessions != null) {
      Iterator<Session> iter = sessions.iterator();
      if (iter.hasNext()) {
        session = iter.next();
      }
      sessions.remove(session);
    }
    
    return session;
  }
  
  
  /**
   * Adds a session to the cache for the given URI.  The calling code is responsible for
   * synchronizing access to this method.
   * @param serverURI A server URI.
   * @param session The session to cache for the server URI.
   */
  private void addToCache(URI serverURI, Session session) {
    Set<Session> sessions = cacheOnUri.get(serverURI);
    if (sessions == null) {
      sessions = new HashSet<Session>();
      cacheOnUri.put(serverURI, sessions);
    }
    sessions.add(session);
  }
  
  
  /**
   * Clears all the contents of the cache, and returns a collection of all the Sessions that
   * were in the cache.  The calling code is responsible for synchronizing access to this method.
   * @return The cached Sessions.
   */
  private Set<Session> clearCache() {
    Set<Session> sessions = new HashSet<Session>();
    for (Map.Entry<URI,Set<Session>> entry : cacheOnUri.entrySet()) {
      Set<Session> set = entry.getValue();
      sessions.addAll(set);
      set.clear();
    }
    cacheOnUri.clear();
    return sessions;
  }
}
