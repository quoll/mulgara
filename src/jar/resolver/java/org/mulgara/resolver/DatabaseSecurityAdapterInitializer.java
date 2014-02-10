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

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.jrdf.graph.Node;

// Local packages
import org.mulgara.query.QueryException;
import org.mulgara.resolver.spi.*;
import org.mulgara.server.SessionFactory;

/**
 * The database initialiser used to provide resources to
 * {@link SecurityAdapter} instances.
 *
 * @created 2004-10-14
 * @author <a href="http://www.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:23 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class DatabaseSecurityAdapterInitializer extends DatabaseInitializer implements SecurityAdapterInitializer {

  /** The {@link Database} to initialize. */
  private final Database database;

  /** The metadata of the {link Database} to initialize. */
  private final DatabaseMetadata databaseMetadata;

  /** A {@link SessionFactory} which produces unsecured {@link org.mulgara.server.Session}s. */
  private final SessionFactory unsecuredSessionFactory;

  /**
   * Sole constructor.
   *
   * @param database  the database to initialize
   * @param databaseMetadata  the metadata about the <var>database</var>
   */
  DatabaseSecurityAdapterInitializer(Database         database,
                                     DatabaseMetadata databaseMetadata,
                                     SessionFactory   unsecuredSessionFactory)
  {
    // Initialize fields
    this.database                = database;
    this.databaseMetadata        = databaseMetadata;
    this.unsecuredSessionFactory = unsecuredSessionFactory;
  }

  //
  // Methods implementing SecurityAdapterInitializer
  //

  public URI getServerURI() throws InitializerException
  {
    checkState();

    return databaseMetadata.getURI();
  }

  public SessionFactory getUnsecuredSessionFactory() throws InitializerException
  {
    return unsecuredSessionFactory;
  }

  public long preallocate(Node node) throws InitializerException
  {
    checkState();

    try {
      //!!FIXME: Can't add preallocate to Session until we switch over.
      DatabaseSession session = (DatabaseSession) database.newSession();
      try {
        return session.preallocate(node);
      }
      finally {
        session.close();
      }
    }
    catch (QueryException eq) {
      throw new InitializerException("Failed to preallocate node", eq);
    }
  }
}
