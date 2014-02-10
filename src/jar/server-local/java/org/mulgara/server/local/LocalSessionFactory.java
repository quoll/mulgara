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

package org.mulgara.server.local;

// Java 2 standard packages
import java.io.File;
import java.net.*;

// Log4J
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.SessionFactoryFactory;
import org.mulgara.util.TempDir;

/**
 * {@link SessionFactory} for downloading and querying static RDF web pages.
 *
 * @created 2002-01-14
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Andrew Newman
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:01 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2002-2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LocalSessionFactory
    implements SessionFactory {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(LocalSessionFactory.class.getName());

  /**
   * The local database.
   */
  private static SessionFactory sessionFactory = null;

  /**
   * The local database the static RDF document is loaded into.
   */
  private URI serverURI;

  /**
   * The persistence directory for the local database.
   */
  private File directory;

  //
  // Constructor
  //

  /**
   * Create a local server.
   *
   * @param documentURI PARAMETER TO DO
   * @throws QueryException if the remote document can't be created in a local
   *      database
   */
  public LocalSessionFactory(URI documentURI) throws QueryException {
    serverURI = documentURI;
  }

  /**
   * Sets the directory to create the local database.  If this is set before
   * creating the first session the database will be created in the given
   * directory.  Otherwise, it will create it in the temp directory and
   * trying to set it again will cause the IllegalStateException to be thrown.
   *
   * @param newDirectory directory name.
   * @throws IllegalStateException if this is called after newSession has
   *   been called for the first time.
   */
  public void setDirectory(File newDirectory) throws IllegalStateException {
    if (sessionFactory == null) {
      directory = newDirectory;
    }
    else {
      throw new IllegalStateException("Session factory already created");
    }
  }

  /**
   * Returns the value of the directory that the sessionFactory will be
   * created in.
   *
   * @return the value of the directory that the sessionFactory will be
   *   created in.
   */
  public File getDirectory() {
    return directory;
  }

  /**
   * Sets the server URI to use to construct the Session Factory. Trying to
   * set it after the first session has been created will cause the
   * IllegalStateException to be thrown.
   *
   * @param newServerURI server URI.
   * @throws IllegalStateException if this is called after newSession has
   *   been called for the first time.
   */
  public void setServerURI(URI newServerURI) throws IllegalStateException {
    if (sessionFactory == null) {
      serverURI = newServerURI;
    }
    else {
      throw new IllegalStateException("Session factory already created");
    }
  }

  /**
   * Returns the value of the server URI that the sessionFactory will be
   * created with.
   *
   * @return the value of the server URI that the sessionFactory will be
   *   created with.
   */
  public URI getServerURI() {
    return serverURI;
  }

  //
  // Methods implementing SessionFactory
  //

  /**
   * @return <code>null</code> (this factory is unsecured)
   */
  public URI getSecurityDomain() {
    return null;
  }

  /**
   * Factory method.
   *
   * @return an unauthenticated session
   * @throws QueryException if a session couldn't be generated
   */
  public Session newSession() throws QueryException {

    try {

      // Initialize session factory if not already in use.
      if (sessionFactory == null) {
        createDirectory();

        SessionFactoryFactory factoryFinder = new SessionFactoryFactory();
        sessionFactory = factoryFinder.newSessionFactory(serverURI, directory);
      }
    }
    catch (Exception e) {
      throw new QueryException("Couldn't create local database", e);
    }

    return sessionFactory.newSession();
  }

  /**
   * Factory method.
   *
   * @return an unauthenticated session
   * @throws QueryException if a session couldn't be generated
   */
  public Session newJRDFSession() throws QueryException {

    try {

      // Initialize session factory if not already in use.
      if (sessionFactory == null) {
        createDirectory();

        SessionFactoryFactory factoryFinder = new SessionFactoryFactory();
        sessionFactory = factoryFinder.newSessionFactory(serverURI, directory);
      }
    }
    catch (Exception e) {
      throw new QueryException("Couldn't create local database", e);
    }

    return sessionFactory.newJRDFSession();
  }

  /**
   * Closes the local database.
   *
   * @throws QueryException EXCEPTION TO DO
   */
  public void close() throws QueryException {
    if ( sessionFactory != null ) {
      sessionFactory.close();
      sessionFactory = null;
    }
  }

  /**
   * Calls delete on the session factory.
   *
   * @throws QueryException EXCEPTION TO DO
   */
  public void delete() throws QueryException {
    if ( sessionFactory != null ) {    
      sessionFactory.delete();
      sessionFactory = null;
    }
    removeContents(directory);    
  }

  /**
   * Sets the directory if it is not set and creates it if it doesn't already
   * exist.
   *
   * @throws QueryException if there was an error creating the directory.
   */
  private void createDirectory() throws QueryException {

    // Create if directory has not been set.
    if (directory == null) {
      directory = new File(TempDir.getTempDir(), "local");
    }

    // Create the directory if it doesn't exist.
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new QueryException(
            "Couldn't create temporary database directory " + directory);
      }
    }
  }

  /**
   * Remove the contents of the given directory.
   *
   * @param dir the file handle to the directory to remove.
   */
  private void removeContents(File dir) {
    File[] files = dir.listFiles();
    if (files != null)
      for (int i = 0; i < files.length; ++i)
        if (files[i].isFile()) files[i].delete();
  }
}
