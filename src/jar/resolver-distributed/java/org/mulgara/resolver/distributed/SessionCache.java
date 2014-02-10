/*
 * Copyright 2008 The Topaz Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
 */

package org.mulgara.resolver.distributed;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import org.mulgara.query.QueryException;
import org.mulgara.server.Session;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.NonRemoteSessionException;
import org.mulgara.server.driver.SessionFactoryFinder;
import org.mulgara.server.driver.SessionFactoryFinderException;

/**
 * A simple cache of {@link SessionFactory}'s and {@link Session}'s. Note that there is currently
 * no cache eviction policy, as the assumption is that this will hold a relatively small set of
 * session's.
 *
 * @created 2008-02-16
 * @author Ronald Tschalär
 * @copyright &copy;2008 <a href="http://www.topazproject.org/">Topaz Project</a>
 * @licence Apache License v2.0
 */
public class SessionCache {
  private static final Logger logger = Logger.getLogger(SessionCache.class);

  private final Map<URI,SessionFactory> factoryCache = new HashMap<URI,SessionFactory>();
  private final ConcurrentMap<URI,List<Session>> sessionCache = new ConcurrentHashMap<URI,List<Session>>();

  private SessionFactory getSessionFactory(URI serverUri)
      throws SessionFactoryFinderException, NonRemoteSessionException {
    synchronized (factoryCache) {
      SessionFactory sessionFactory = factoryCache.get(serverUri);
      if (sessionFactory == null) {
        if (logger.isDebugEnabled()) {
          logger.debug("Creating session-factory for server '" + serverUri + "'");
        }
        factoryCache.put(serverUri,
                         sessionFactory = SessionFactoryFinder.newSessionFactory(serverUri, true));
      }

      return sessionFactory;
    }
  }

  /**
   * Get a session from the cache. A new session will be created none are available.
   *
   * @param serverUri the server for which to get the session
   * @return the session
   * @throws SessionFactoryFinderException if an error occurred creating the session-factory
   * @throws NonRemoteSessionException     if an error occurred creating the session-factory
   * @throws QueryException                if an error occurred creating the session
   * @see #returnSession(URI, Session)
   */
  public Session getSession(URI serverUri)
      throws SessionFactoryFinderException, NonRemoteSessionException, QueryException {
    List<Session> sessions = sessionCache.get(serverUri);
    if (sessions == null) {
      sessionCache.putIfAbsent(serverUri, new ArrayList<Session>());
      sessions = sessionCache.get(serverUri);
    }

    synchronized (sessions) {
      if (sessions.size() > 0) {
        return sessions.remove(sessions.size() - 1);
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("Creating session for server '" + serverUri + "'");
        }
        return getSessionFactory(serverUri).newSession();
      }
    }
  }

  /**
   * Return a session to the cache.
   *
   * @param serverUri the server this session belongs to
   * @param session   the session to return
   */
  public void returnSession(URI serverUri, Session session) {
    synchronized (serverUri.toString().intern()) {
      sessionCache.get(serverUri).add(session);
    }
  }

  /**
   * Closes all sessions and factories.
   */
  public void close() {
    synchronized (factoryCache) {
      for (SessionFactory sf : factoryCache.values()) {
        try {
          sf.close();
        } catch (QueryException qe) {
          logger.error("Exception while closing session-factory", qe);
        }
      }
      factoryCache.clear();

      for (List<Session> sl : sessionCache.values()) {
        for (Session s : sl) {
          try {
            s.close();
          } catch (QueryException qe) {
            logger.error("Exception while closing session", qe);
          }
        }
      }
      sessionCache.clear();
    }
  }
}
