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

// java 2 standard packages
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

// servlet packages
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// thirdparty packages
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

// locall written packages
import org.mulgara.server.SessionFactory;
import org.mulgara.server.SessionFactoryException;
import org.mulgara.server.SessionFactoryFactory;
import org.mulgara.util.Closable;

/**
 * Servlet startup wrapper for a Mulgara server. <p>
 *
 * You can get a handle on the database created by this servlet by calling: </p>
 * <pre>
 * Database database = ServletMulgaraServer.getDatabase();
 * </pre>
 * <p>
 *
 * Note that this servlet cannot furnish requests. Any (HTTP) requests will
 * result in an exception being thrown indicating as much. It is only intended
 * to start a Mulgara instance that can be retrieved locally if needed (ie. not
 * over RMI) for speed.
 * </p>
 * <p>
 * Note. This servlet should eventually start a "normal" Mulgara server that will
 * accept requests as per normal. At the moment, queries can only be sent via
 * direct method calls.
 * </p>
 *
 * @created 2002-03-04
 * @author Tom Adams
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy;2002 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ServletMulgaraServer extends HttpServlet {

  /** Used for serializing - an unlikely occurance. */
  private static final long serialVersionUID = -5680050587689401924L;

  /** The logging category to log to */
  private final static Logger log = Logger.getLogger(ServletMulgaraServer.class.getName());

  /** the key to retreive the Mulgara server name */
  private final static String MULGARA_CONFIG_SERVERNAME = "mulgara.config.servername";

  /** the key to retrieve the database persistence path */
  private final static String MULGARA_CONFIG_PERSISTENCE_PATH = "mulgara.config.persistencepath";

  /** the key to retreive the Mulgara host name */
  private final static String MULGARA_HOSTNAME = "mulgara.hostname";

  /** the key to retreive the Mulgara LDAP security file */
  private final static String MULGARA_LOG4J_CONFIG = "mulgara.log4j.config";

  /** the key to retreive the lucene index directory */
  private final static String LUCENE_INDEX_DIR = "lucene.index.dir";


  /** the database of this server */
  private static SessionFactory database = null;


  public ServletMulgaraServer() {
  }

  /**
   * Sets the database of this server.
   * @param database the database of this server, refered to as a SessionFactory.
   */
  public static void setDatabase(SessionFactory database) {
    ServletMulgaraServer.database = database;
  }


  /**
   * Gets the sessionFactory/database of this server.
   * @return The database value as a SessionFactory
   */
  public static SessionFactory getDatabase() {
    return ServletMulgaraServer.database;
  }


  /**
   * Creates a new Mulgara database, and stores it in the servlet context.
   * @throws ServletException if the Mulgara database cannot be created for some reason
   */
  public void init() throws ServletException {

    BasicConfigurator.configure();
    try {
      // get the loction of the logging configuration file
      String log4jConfigPath = getRealPath(ServletMulgaraServer.MULGARA_LOG4J_CONFIG);

      //this.getResourceURL(ServletMulgaraServer.MULGARA_LOG4J_CONFIG);
      if (log4jConfigPath == null) throw new IOException("Unable to retrieve log4j configuration file");

      // load the logging configuration
      loadLoggingConfig((new File(log4jConfigPath)).toURI().toURL());
    } catch (Exception e) {
      // log the error
      System.err.println("Could not initialize logging configuration!");
      e.printStackTrace(System.err);
      // wrap it in a servlet exception
      throw new ServletException(e);
    }

    // log what we're doing
    if (log.isInfoEnabled()) log.info("Initialising Mulgara server servlet");

    // log that we've created a new server
    if (log.isDebugEnabled()) log.debug("Created servlet-wrapped Mulgara server");

    // if it we don't have one already, create a new database
    if (ServletMulgaraServer.getDatabase() == null) {
      try {
        ServletMulgaraServer.setDatabase(createDatabase());
      } catch (Throwable t) {
        log.info("No local database available. Using RMI.");
      }
    }

  }

  /**
   * Closes the database.
   */
  public synchronized void destroy() {

    if (getDatabase() != null) {
      // log that we're stopping the database
      if (log.isInfoEnabled()) log.info("Stopping Mulgara server");

      try {
        ((Closable)getDatabase()).close();
      } catch (Exception e) {
        log.error("Unable to close database cleanly", e);
      }
      setDatabase(null);
    }

  }


  /**
   * Closes the database at the last moment.
   * @throws Throwable General catch-all for closing problems.
   */
  protected void finalize() throws Throwable {
    try {
      destroy();
    } finally {
      super.finalize();
    }
  }


  /**
   * As this servlet cannot handle requests (its only job is to start create a
   * database), this method always throws an exception.
   * @param req The request.
   * @param res The response to the client.
   * @throws ServletException always thrown.
   */
  protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException {
    throw new ServletException("Mulgara server servlet does not handle requests");
  }


  /**
   * Returns the URI of the server based on the server name.
   * @param serverName the name of the server
   * @return the URI of the server
   * @throws URISyntaxException if the URI cannot be created
   */
  private URI getServerURI(String serverName) throws URISyntaxException {

    String hostname = getServletContext().getInitParameter(MULGARA_HOSTNAME);

    // attempt to determine the hostname if not supplied by servlet
    if (hostname == null || hostname.trim().length() == 0) {
      hostname = "localhost";
      try {
        hostname = InetAddress.getLocalHost().getHostName();
        log.info("Obtained " + hostname + " automatically for server");
      } catch (UnknownHostException e) {
        log.warn("Problem getting host name -> using localhost");
      }
    }

    // return the server URI
    return new URI("rmi", hostname, "/" + serverName, null);
  }


  /**
   * Unaliases the persistence path specified in the configuration file. <p>
   *
   * The configuration file allows the constants <code>temp</code> and <code>.</code>
   * to be used instead of a real path, to enable easier cross platform
   * deployment. These are then mapped to the system temp directory and the
   * user's current directory respectively by this method. </p>
   *
   * @param aliasedPath the persistence path as it appears in the configuration file
   * @return the unaliased persistence path
   */
  private String getPersistencePath(String aliasedPath) {

    // initialis the path to what was specified in the config
    String persistencePath = aliasedPath;

    // unalias the path (if we need to)
    if ((aliasedPath == null) || aliasedPath.equals("") || aliasedPath.equalsIgnoreCase(".")) {
      // use the current directory
      persistencePath = System.getProperty("user.dir");
    } else if (aliasedPath.equalsIgnoreCase("temp")) {
      persistencePath = System.getProperty("java.io.tmpdir");
    }

    // return the persistence path
    return persistencePath;
  }

 
  /**
   * Returns the real location of a file path specified by the <code>parameter</code>
   * in the servlet context.
   * @param param the init param in the servlet context to find the real location of
   * @return the real location of the given param
   */
  private String getRealPath(String param) {

    // get the path specified in the servlet config
    String filePath = this.getServletContext().getInitParameter(param);

    // try to find the real path
    if (filePath != null) {
      // log that we've found the config file
      if (log.isDebugEnabled()) log.debug("Found file location " + filePath + " for param " + param);

      // get the real path
      filePath = this.getServletContext().getRealPath(filePath);
    }

    // return the path we found
    return filePath;
  }


  /**
   * Creates a new database.
   * @return The created database.
   * @throws ServletException if the database could not be created.
   */
  private synchronized SessionFactory createDatabase() throws ServletException {
    try {
      // configure the system properties
      if (log.isDebugEnabled()) log.debug("Configuring system properties");

      configureSystemProperties();

      // get params we'll need to create the server
      String tmpConfiguredPath = getServletContext().getInitParameter(MULGARA_CONFIG_PERSISTENCE_PATH);
      String persistencePath = getPersistencePath(tmpConfiguredPath);
      String serverName = getServletContext().getInitParameter(MULGARA_CONFIG_SERVERNAME);

      // throw an error if anything is null
      if (serverName == null) throw new ServletException("Server name not in deployment descriptor");

      // get the server's URI and the database state path
      URI serverURI = getServerURI(serverName);
      File statePath = new File(new File(persistencePath), serverName);

      // create the state path if needed
      if (!statePath.exists()) {
        if (!statePath.mkdirs()) throw new IOException("Could not create server directory: " + statePath);
      }

      // log that we're creating a Mulgara server
      if (log.isInfoEnabled()) log.info("Starting Mulgara server at " + serverURI + " in directory " + statePath);

      // return the database
      SessionFactoryFactory factory = new SessionFactoryFactory();
      SessionFactory sessionFactory = factory.newSessionFactory(serverURI, statePath);

      return sessionFactory;

    } catch (SessionFactoryException sfe) {
      // log the error
      log.error("Error creating database", sfe);
      // wrap it in a servlet exception
      throw new ServletException(sfe);
    } catch (UnknownHostException uhe) {
      // log the error
      log.error("Error creating database", uhe);
      // wrap it in a servlet exception
      throw new ServletException(uhe);
    } catch (IOException ioe) {
      // log the error
      log.error("Error creating database", ioe);
      // wrap it in a servlet exception
      throw new ServletException(ioe);
    } catch (URISyntaxException use) {
      // log the error
      log.error("Error creating database", use);
      // wrap it in a servlet exception
      throw new ServletException(use);
    }
  }


  /**
   * Sets up any system properties needed by components.
   * @throws IOException if any files embedded within the JAR file cannot be found
   */
  private void configureSystemProperties() throws IOException {
    // set the system properties needed
    System.setProperty("org.mulgara.xml.ResourceDocumentBuilderFactory",
        "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

    // set the lucene index
    String luceneIndex = this.getServletContext().getInitParameter(ServletMulgaraServer.LUCENE_INDEX_DIR);

    if (luceneIndex != null) {
      System.setProperty(ServletMulgaraServer.LUCENE_INDEX_DIR, luceneIndex);
    } 
  }

  
  /**
   * Loads the embedded logging configuration from an external URL.
   * @param loggingConfig the URL of the logging configuration file
   */
  private void loadLoggingConfig(URL loggingConfig) {

    // validate the loggingConfig parameter
    if (loggingConfig == null) throw new IllegalArgumentException("Null \"loggingConfig\" parameter");

    // configure the logging service
    PropertyConfigurator.configure(loggingConfig);
    log.info("Using logging configuration from " + loggingConfig);
  }

}
