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

package org.mulgara.server;

// Java 2 standard packages
import java.net.URI;

import org.mulgara.query.QueryException;

/**
 * Factory for queryable {@link Session}s. Implementations are responsible for
 * caching authentication data.
 *
 * @created 2001-12-14
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:21 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface SessionFactory {

  /**
   * Accessor for the factory's security domain. The URI returned should
   * uniquely identify the {@link javax.security.auth.login.Configuration}
   * (usually a JAAS configuration file) used by the factory.
   *
   * @return a unique resource name for the security domain this {@link
   *      SessionFactory} lies within, or <code>null</code> if the factory is
   *      unsecured
   * @throws QueryException if the domain was unable to be retrieved or
   *      initialized.
   */
  public URI getSecurityDomain() throws QueryException;

  /**
   * Factory method. The session generated will be an unauthenticated (<q>guest
   * </q>) session. To authenticate it, the {@link Session#login} method must be
   * used.
   *
   * @return an unauthenticated session
   * @throws QueryException if a session couldn't be generated
   */
  public Session newSession() throws QueryException;

  /**
   * Creates a session that can be used for a JRDF Graph.
   *
   * @throws QueryException
   * @return Session
   */
  public Session newJRDFSession() throws QueryException;

  /**
   * Flush and free persistent resources of the session factory.
   *
   * @throws QueryException if persistence resources couldn't be closed
   */
  public void close() throws QueryException;

  /**
   * Delete persistence resources of the session factory.
   *
   * @throws QueryException if persistence resources couldn't be freed
   */
  public void delete() throws QueryException;
}
