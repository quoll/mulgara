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
import java.net.URI;
import java.rmi.*;

// Locally written packages
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;

/**
 * Java RMI proxied factory for queryable {@link Session}s.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @created 2002-01-07
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:02 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
interface RemoteSessionFactory extends Remote {

  /**
   * Accessor for the factory's security domain. The URI returned should
   * uniquely identify the {@link javax.security.auth.login.Configuration}
   * (usually a JAAS configuration file) used by the factory.
   *
   * @return a unique resource name for the security domain this
   *      {@link org.mulgara.server.SessionFactory} lies within, or
   *      <code>null</code> if the factory is unsecured
   * @throws QueryException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public URI getSecurityDomain() throws QueryException, RemoteException;

  /**
   * Get the server URI used to create the current remoteSessionFactory
   */
  public URI getServerURI() throws RemoteException;

  /**
   * Set the server URI used to create the current remoteSessionFactory
   */
  public void setServerURI( URI serverURI ) throws RemoteException;

  /**
   * Get the default URI used by this server
   */
  public URI getDefaultServerURI() throws RemoteException;

  /**
   * Factory method. The session generated will be an unauthenticated (&quot;guest
   * &quot;) session. To authenticate it, the {@link Session#login} method must be
   * used.
   *
   * @return an unauthenticated session
   * @throws QueryException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public Session newSession() throws QueryException, RemoteException;

  /**
   * Creates a session that can be used for a JRDF Graph.
   *
   * @return an unauthenticated session
   * @throws QueryException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public Session newJRDFSession() throws QueryException, RemoteException;

  /**
   * Factory method. The session generated will be an unauthenticated (&quot;guest
   * &quot;) session. To authenticate it, the {@link Session#login} method must be
   * used.  The remote session should be wrapped in a Session object.
   *
   * @return an unauthenticated RemoteSession
   * @throws QueryException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public RemoteSession newRemoteSession() throws QueryException, RemoteException;

  /**
   * Factory method. Remove session reference from factory.
   *
   * @throws QueryException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public void removeSession( RemoteSession session ) throws RemoteException;

  /**
   * Free resources claimed by the session.
   *
   * @throws QueryException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public void close() throws QueryException, RemoteException;

}
