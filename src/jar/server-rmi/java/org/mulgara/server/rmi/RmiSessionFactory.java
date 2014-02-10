/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.server.rmi;

// Java 2 standard packages
import java.net.*;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.*;

// Log4J
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.QueryException;
import org.mulgara.server.NonRemoteSessionException;
import org.mulgara.server.Session;
import org.mulgara.server.SessionFactory;
import org.mulgara.util.ServerInfoRef;

/**
 * Proxy for a remote SessionFactory connected via Java RMI.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @created 2001-07-12
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:02 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RmiSessionFactory implements SessionFactory {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(RmiSessionFactory.class.getName());

  /**
   * The RMI stub for the {@link RemoteSessionFactory} wrapping the proxied
   * {@link SessionFactory}.
   */
  private final RemoteSessionFactory remoteSessionFactory;

  //
  // Constructor
  //

  /**
   * Generate a proxy for a {@link SessionFactory} which is being served via an
   * {@link RmiServer}.
   *
   * @param serverURI the URI of the server to proxy
   * @throws NamingException if a session factory can't be obtained from the RMI
   *      registry indicated by <var>serverURI</var>
   */
  public RmiSessionFactory(URI serverURI) throws NamingException, NonRemoteSessionException, RemoteException {

    // Validate "serverURI" parameter
    if (serverURI == null) {
      throw new IllegalArgumentException("Null \"serverURI\" parameter");
    }

    if (!"rmi".equals(serverURI.getScheme())) {
      throw new IllegalArgumentException(serverURI + " doesn't use the rmi: protocol");
    }

    if ( (serverURI.getPath() == null) || !serverURI.getPath().startsWith("/")) {
      throw new IllegalArgumentException(serverURI + " isn't a valid RMI server URI");
    }

    if (serverURI.getFragment() != null) {
      throw new IllegalArgumentException(serverURI + " is a model URI, not a server");
    }

    // Get the RMI registry as a JNDI naming context
    Hashtable<String,String> environment = new Hashtable<String,String>();
    environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
    environment.put(Context.PROVIDER_URL, "rmi://" + serverURI.getRawAuthority());

    Context rmiRegistryContext = new InitialContext(environment);

    // Look up the session factory in the RMI registry
    remoteSessionFactory = (RemoteSessionFactory) rmiRegistryContext.lookup(serverURI.getPath().substring(1));

    URI remoteURI = remoteSessionFactory.getDefaultServerURI();
    URI localURI = ServerInfoRef.getServerURI();
    if (logger.isDebugEnabled()) logger.debug("remoteURI=" + remoteURI+" localURI="+localURI);
    if (remoteURI == null) {
      logger.warn("host uri is not set, local = " + (localURI == null ? "<client>" : localURI.toString()) + ", remote = " + remoteURI);
      throw new NamingException("host uri is not set, local = " + (localURI == null ? "<client>" : localURI.toString()) + ", remote = " + remoteURI);
    }
    if (remoteURI.equals(localURI) && !serverURI.equals(localURI)) {
      logger.warn("Using non-standard server name: " + serverURI + "  Should be: " + localURI);
      try {
        close();
      } catch (QueryException qe) {
        // Don't care about closure.  Fall through to next exception
      }
      throw new NonRemoteSessionException("The URI provided was not for a remote session");
    }

    remoteSessionFactory.setServerURI(serverURI);

    // Postconditions
    //assert remoteSessionFactory != null;
  }

  //
  // Methods implementing SessionFactory
  //

  /**
   * Accessor for the factory's security domain. The URI returned should
   * uniquely identify the {@link javax.security.auth.login.Configuration}
   * (usually a JAAS configuration file) used by the factory.
   *
   * @return a unique resource name for the security domain this {@link
   *      SessionFactory} lies within, or <code>null</code> if the factory is
   *      unsecured
   * @throws QueryException EXCEPTION TO DO
   */
  public URI getSecurityDomain() throws QueryException {
    try {
      return remoteSessionFactory.getSecurityDomain();
    } catch (RemoteException e) {
      throw new QueryException("Couldn't contact server", e);
    }
  }

  /**
   * Get the the remoteSessionFactory created at construction
   */
  public RemoteSessionFactory getRemoteSessionFactory() {
    return this.remoteSessionFactory;
  }

  /**
   * Factory method. The session generated will be an unauthenticated (&quot;guest
   * &quot;) session. To authenticate it, the {@link Session#login} method must be
   * used.
   *
   * @return an unauthenticated session
   * @throws QueryException if a session couldn't be generated
   */
  public Session newSession() throws QueryException {
    try {
      return remoteSessionFactory.newSession();
    } catch (RemoteException e) {
      throw new QueryException("Couldn't contact server", e);
    }
  }

  public Session newJRDFSession() throws QueryException {
    try {
      return remoteSessionFactory.newJRDFSession();
    } catch (RemoteException e) {
      throw new QueryException("Couldn't contact server", e);
    }
  }

  /**
   * Close the remote session factory.
   */
  public void close() throws QueryException {
    try {
      remoteSessionFactory.close();
    } catch (RemoteException e) {
      throw new QueryException("Couldn't close remote session factory", e);
    }
  }

  /**
   * Remove this factory and all associated resources.  No op.
   */
  public void delete() {
    // null implementation
  }

}
