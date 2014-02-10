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

package org.mulgara.resolver.spi;

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.jrdf.graph.Node;  // JRDF

// Local packages
import org.mulgara.server.Session;
import org.mulgara.server.SessionFactory;
import org.mulgara.store.stringpool.StringPool;

/**
 * Interface defining the initialization services which a
 * {@link SecurityAdapter} if offered by the database.
 *
 * Initialization services are only provided at initialization.  If a client
 * tries to hold on to a reference to a {@link SecurityAdapterInitializer}
 * after initialization, calling any of the methods of this interface should
 * throw {@link IllegalStateException}.
 *
 * @created 2004-10-14
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface SecurityAdapterInitializer
{
  /**
   * Access the URI of the server.
   *
   * This will never have a fragment part.
   *
   * @return the URI of the server
   * @throws InitializerException if the URI can't be determined
   */
  public URI getServerURI() throws InitializerException;

  /**
   * @return a {@link SessionFactory} that produces {@link Session}s which
   *   don't apply the registered {@link SecurityAdapter}s, never
   *   <code>null</code>
   * @throws InitializerException if such a {@link SessionFactory} can't be
   *   supplied
   */
  public SessionFactory getUnsecuredSessionFactory()
    throws InitializerException;

  /**
   * Preallocate an RDF {@link Node} to exist in all sessions.
   *
   * Because they exist in all sessions and can never be reaped, they need not
   * be relocalized for every new {@link Session}.  This savings in speed and
   * hits on the {@link StringPool} is bought at the expense of cluttering up
   * the database with the preallocated nodes.
   *
   * @param node  a global RDF node to preallocate
   * @return a preallocated local node
   * @throws IllegalStateException if called outside of initialization
   * @throws InitializerException if the node can't be allocated
   */
  public long preallocate(Node node) throws InitializerException;
}
