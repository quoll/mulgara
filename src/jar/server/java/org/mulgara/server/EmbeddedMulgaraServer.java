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
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.util.*;

import javax.naming.*;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;

// log4j packages
import org.apache.log4j.*;
import org.apache.log4j.xml.DOMConfigurator;

// locally written packages
import org.mulgara.config.MulgaraConfig;
import org.mulgara.config.Connector;
import org.mulgara.config.PublicConnector;
import org.mulgara.config.XpathFunctionResolver;
import org.mulgara.query.FunctionResolverRegistry;
import org.mulgara.server.SessionFactory;
import org.mulgara.store.StoreException;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.util.Reflect;
import org.mulgara.util.Rmi;
import org.mulgara.util.StackTrace;
import org.mulgara.util.TempDir;

import static org.mulgara.server.ServerMBean.ServerState;

/**
 * Embedded production Mulgara server.
 *
 * <p> Creates a Mulgara server instance, and a SOAP server instance to handle
 * <a href="http://www.w3.org/TR/SOAP">SOAP</a> requests for the Mulgara server.</p>
 *
 * @created 2001-10-04
 *
 * @author Tom Adams
 * @author Simon Raboczi
 * @author Paula Gearon
 * @author Tate Jones
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2001-2004 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 * @see <a href="http://developer.java.sun.com/developer/JDCTechTips/2001/tt0327.html#jndi">
 *      <cite>JNDI lookup in distributed systems</cite> </a>
 */
public class EmbeddedMulgaraServer implements SessionFactoryProvider {

  /** Line separator. */
  protected static final String eol = System.getProperty("line.separator");

  /** Default port to listen for a shutdown. */
  public final static int SHUTDOWN_PORT = 6789;

  /** System property for the shutdown port. */
  public final static String SHUTDOWN_PROP = "shutdownhook.port";

  /** The request required to shutdown mulgara. */
  public final static String SHUTDOWN_MSG = "shutdownmulgara";

  /** The logging category to log to. */
  protected static final Logger log = Logger.getLogger(EmbeddedMulgaraServer.class.getName());

  /** The embedded configuration file path */
  protected static String CONFIG_PATH = "conf/mulgara-x-config.xml";

  /** The RMI permission security policy file path. */
  protected static String RMI_SECURITY_POLICY_PATH = "conf/mulgara-rmi.policy";

  /** The property for identifying the policy type. */
  private static final String SYSTEM_MAIL = "mail.smtp.host";

  /** The default server class to use for the Mulgara server. */
  private static final String DEFAULT_SERVER_CLASS_NAME = "org.mulgara.server.rmi.RmiServer";

  /** The registry context factory class. */
  private static final String CONTEXT_FACTORY = "com.sun.jndi.rmi.registry.RegistryContextFactory";

  /** The property for identifying the policy type. */
  private static final String SECURITY_POLICY_PROP = "java.security.policy";

  /** The system property to disable the HTTP service. */
  private static final String DISABLE_HTTP = "mulgara.http.disable";

  /** The system property to disable the RMI service. */
  private static final String DISABLE_RMI = "no_rmi";

  /** The Mulgara server instance. In this case, an RMIServer. */
  private ServerMBean serverManagement = null;

  /** The web service container. */
  private HttpServices webServices = null;

  /** The embedded Mulgara server configuration */
  private MulgaraConfig mulgaraConfig = null;

  /** The (RMI) name of the server. */
  private String rmiServerName = null;

  /** The path to persist server data to. */
  private String persistencePath = null;

  /** The hostname to accept SOAP requests on. */
  private String httpHostName = null;

  /** A flag to indicate if the server is configured to be started. */
  private boolean canStart = false;

  /** A flag to indicate if the http server is configured to be started. */
  private boolean httpEnabled;

  /**
   * Starts a Mulgara server and a WebServices (SOAP) server to handle SOAP queries to the
   * Mulgara server.
   * <p>Database files for the Mulgara server are written to the directory from
   * where this class was run.</p>
   * @param args command line arguments
   */
  public static void main(String[] args) {
    // report the version and build number
    System.out.println("@@build.label@@");
  
    // Set up the configuration, using command line arguments to override configured options
    EmbeddedMulgaraOptionParser optsParser = new EmbeddedMulgaraOptionParser(args);

    // load up the basic logging configuration in case we get an error before
    // we've loaded the real logging configuration
    BasicConfigurator.configure();

    try {
      // parse the command line options to the server
      optsParser.parse();

    } catch (EmbeddedMulgaraOptionParser.UnknownOptionException uoe) {
      System.err.println("ERROR: Unknown option(s): " + uoe.getOptionName());
      printUsage();
      System.exit(3);
    } catch (EmbeddedMulgaraOptionParser.IllegalOptionValueException iove) {
      System.err.println("ERROR: Illegal value '" + iove.getValue() +
          "' for option " + iove.getOption().shortForm() + "/" + iove.getOption().longForm());
      printUsage();
      System.exit(4);
    }

    try {
      // TODO: Iterate over all configured servers and start each one
      // Create the server instance
      EmbeddedMulgaraServer standAloneServer = new EmbeddedMulgaraServer(optsParser);
    
      if (standAloneServer.isStartable()) {
        // start the server, including all the configured services
        standAloneServer.startServices();

        // Setup the network service for shutting down the server
        ShutdownService shutdownServer = new ShutdownService();
        shutdownServer.start();
      }
  
    } catch (ExceptionList el) {
      for (Throwable e: (List<Throwable>)el.getCauses()) {
        log.error("ExceptionList", e);
        e.printStackTrace();
      }
      System.exit(2);
    } catch (Exception e) {
      log.error("Exception in main", e);
      e.printStackTrace();
      System.exit(5);
    }

}


  /**
   * Shutdown the Mulgara server
   */
  public static void shutdown(String[] args) {
    // create a basic Configurator for the shutdown
    BasicConfigurator.configure();

    // get the socket port
    int port = getShutdownHookPort();

    // create a socket to the local host and port
    Socket clientSocket = null;
    try {
      clientSocket = new Socket(InetAddress.getByName("localhost"), port);
      PrintWriter toServer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
      toServer.println(EmbeddedMulgaraServer.SHUTDOWN_MSG);
      toServer.flush();
      toServer.close();
    } catch (ConnectException ioCon) {
      System.out.println("Server is not currently running");
    } catch (IOException ioEx) {
      log.error("Unable to establish connection to shutdown server on port " + port, ioEx);
    } catch (SecurityException secEx) {
      log.error("Unable to establish connection shutdown server due to a security exception. Check security policy", secEx);
    } catch (Exception ex) {
      log.error("Unable to establish shutdown connection to shutdown server on port " + port, ex);
    } finally {
      // attempt to close the socket
      try {
        if (clientSocket != null) clientSocket.close();
      } catch (Exception ex) {
        /* skip */
      }
    }
  }


  /**
   * Loads the embedded logging configuration (from the JAR file).
   * @param loggingConfig the path to the logging configuration file
   */
  private static void loadLoggingConfig(String loggingConfig) {
    // get a URL from the classloader for the logging configuration
    URL log4jConfigURL = ClassLoader.getSystemResource(loggingConfig);

    // if we didn't get a URL, tell the user that something went wrong
    if (log4jConfigURL == null) {
      System.err.println("Unable to find logging configuration file in JAR " +
            "with " + loggingConfig + ", reverting to default configuration.");
      BasicConfigurator.configure();
    } else {
      try {
        // configure the logging service
        DOMConfigurator.configure(log4jConfigURL);
        if (log.isDebugEnabled()) log.debug("Using logging configuration from " + log4jConfigURL);
      } catch (FactoryConfigurationError e) {
        System.err.println("Unable to configure logging service, reverting to default configuration");
        BasicConfigurator.configure();
      } catch (Exception e) {
        System.err.println("Unable to configure logging service, reverting to default configuration");
        BasicConfigurator.configure();
      }
    }
  }


  /**
   * Loads the embedded logging configuration from an external URL.
   * @param loggingConfig the URL of the logging configuration file
   */
  private static void loadLoggingConfig(URL loggingConfig) {
    if (loggingConfig == null) throw new IllegalArgumentException("Null \"loggingConfig\" parameter");

    try {
      // configure the logging service
      DOMConfigurator.configure(loggingConfig);
      if (log.isDebugEnabled()) log.debug("Using logging configuration from " + loggingConfig);
    } catch (FactoryConfigurationError e) {
      System.err.println("Unable to configure logging service, reverting to default configuration");
      BasicConfigurator.configure();
    } catch (Exception e) {
      System.err.println("Unable to configure logging service, reverting to default configuration");
      BasicConfigurator.configure();
    }
  }


  /**
   * Attempts to obtain the localhost name or defaults to the IP address of the localhost
   * @return the hostname this Mulgara server is bound to.
   */
  public static String getResolvedLocalHost() {
    String hostname = null;

    try {
      // attempt for the localhost canonical host name
      hostname = InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException uhe) {
      try {
        // attempt to get the IP address for the localhost
        hostname = InetAddress.getByName("localhost").getHostAddress();
        log.info("Obtain localhost IP address of " + hostname);
      } catch (UnknownHostException uhe2 ) {
        // default to the localhost IP
        hostname = "127.0.0.1";
        log.info("Defaulting to 127.0.0.1 IP address");
      }
    }

    return hostname;
  }


  /**
   * Get the shutdown hook port to allow the BootStrap to shutdown the server
   * from the same machine but different JVM To override the default port of
   * 6789 set a system property called shutdownhook.port
   * @return the shutdown port for this server
   */
  private static int getShutdownHookPort() {
    int port = EmbeddedMulgaraServer.SHUTDOWN_PORT;

    // check if the default shutdown port has been overrided by a system property.
    String portString = System.getProperty(SHUTDOWN_PROP);
    if ((portString != null) && (portString.length() > 0)) {
      try {
        port = Integer.parseInt(portString);
        if (log.isInfoEnabled()) log.info("Override default shutdown hook port to " + port);
      } catch (NumberFormatException ex) {
        log.error("Unable to convert supplied port " + portString + " to int " +
            " for shutdown hook. Defaulting to port :" + port, ex);
      }
    }

    return port;
  }


  ///////////////////////////////////////////////////////////////
  // Member methods
  ///////////////////////////////////////////////////////////////

  EmbeddedMulgaraServer(EmbeddedMulgaraOptionParser options) throws IOException, ClassNotFoundException,
            SAXException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    // TODO: Attach ServerInfo to all databases, so it can be instantiated per server
    // configure the server, and set up the global ServerInfo
    canStart = configure(options);

    // start the server if we're allowed to
    if (canStart) {
      // create the params need for a new Mulgara instance
      File statePath = new File(new File(getPersistencePath()), getServerName());
      String tripleStoreClassName = getConfig().getTripleStoreImplementation();
      String hostname = ServerInfo.getBoundHostname();
      int rmiPort = ServerInfo.getRMIPort();

      //set the tripleStoreImplemention property
      System.setProperty("triple.store.implementation", tripleStoreClassName);

      // create a Mulgara server instance
      serverManagement = createServer(
          getServerName(), statePath, hostname,
          rmiPort, tripleStoreClassName, DEFAULT_SERVER_CLASS_NAME
      );

      // install a shutdown hook for System.exit(#);
      if (log.isDebugEnabled()) log.debug("Registering shutdown hook");
      Runtime.getRuntime().addShutdownHook(new RuntimeShutdownHook(this));

      // create a web service
      if (httpEnabled) {
        // create a HTTP server instance
        if (log.isDebugEnabled()) log.debug("Configuring HTTP server");
        try {
          webServices = createHttpServices(this, httpHostName, mulgaraConfig);
        } catch (Exception e) {
          String message = e.getMessage();
          if (message == null) message = StackTrace.throwableToString(e);
          log.error("Unable to start web services due to: " + message + " [Continuing]");
          if (log.isDebugEnabled()) log.debug("Web Server problem", e);
        }
      }
    }
  }


  /**
   * Starts the Mulgara server.
   * @throws IllegalStateException if this method is called before the servers have been created
   * @throws IOException if the Mulgara server cannot access its state keeping files
   * @throws NamingException if the Mulgara server cannot communicate with the RMI registry
   * @throws ExceptionList if an error ocurrs while starting up the SOAP server
   * @throws SimpleXAResourceException If operations on the database cannot be instigated
   * @throws Exception General catch all exception
   * @throws StoreException if the database could not be started
   */
  public void startServices() throws IOException, NamingException,
      ExceptionList, SimpleXAResourceException, StoreException, Exception {
  
    if (serverManagement == null) throw new IllegalStateException("Servers must be created before they can be started");
  
    // log that we're starting a Mulgara server
    if (log.isDebugEnabled()) log.debug("Starting server");
  
    // start the Mulgara server
    serverManagement.init();
    serverManagement.start();

    // get the configured factory and URI and set the ServerInfo for this Mulgara server
    ServerInfo.setLocalSessionFactory(((AbstractServer)serverManagement).getSessionFactory());
  
    // start the HTTP server if required
    if (webServices != null) {
      if (log.isDebugEnabled()) log.debug("Starting HTTP server");
      webServices.start();
    }
  }


  /**
   * Returns the flag that indicates if the server was configured to be started.
   * @return <code>true</code> if the server is configured to be started.
   */
  private boolean isStartable() {
    return canStart;
  }


  /**
   * Returns a reference to the local {@link org.mulgara.server.SessionFactory} of
   * the underlying database.
   * @return a {@link org.mulgara.server.SessionFactory} from the underlying database
   */
  public SessionFactory getSessionFactory() {
    SessionFactory sessionFactory = null;
    if (serverManagement != null) sessionFactory = ((AbstractServer)serverManagement).getSessionFactory();
    return sessionFactory;
  }


  /**
   * Returns the Mulgara server instance.
   * @return the Mulgara server instance
   */
  ServerMBean getServerMBean() {
    return serverManagement;
  }


  /**
   * Returns the embedded Mulgara server configuration.
   * @return the embedded Mulgara server configuration
   */
  private MulgaraConfig getConfig() {
    return mulgaraConfig;
  }


  /**
   * Returns the (RMI) name of the server.
   * @return the (RMI) name of the server
   */
  String getServerName() {
    return rmiServerName;
  }


  /**
   * Returns the path to persist server data to.
   * @return the path to persist server data to
   */
  private String getPersistencePath() {
    return persistencePath;
  }


  /**
   * Configures an embedded Mulgara server.
   * @param parser the options parser containing the command line arguments to the server
   * @return true if the server is allowed to start
   */
  private boolean configure(EmbeddedMulgaraOptionParser parser) {
    // flag to indicate whether we can start the server
    boolean startServer = true;

    try {
      // find out if the user wants help
      if (parser.getOptionValue(EmbeddedMulgaraOptionParser.HELP) != null) {
        // print the help
        printUsage();

        // don't start the server
        startServer = false;
      } else if (parser.getOptionValue(EmbeddedMulgaraOptionParser.SHUTDOWN) != null) {
        // shut down the remote server
        shutdown(new String[0]);

        // don't start the server
        startServer = false;
      } else {
        // load the Mulgara configuration file
        String configURLStr = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.SERVER_CONFIG);
        mulgaraConfig = new MulgaraUserConfig(configURLStr);

        // set up any local registries used in the system
        configureRegistries();

        // disable automatic starting of the RMI registry
        if (parser.getOptionValue(EmbeddedMulgaraOptionParser.NO_RMI) != null) {
          // disable automatic starting of the RMI Registry
          System.setProperty(DISABLE_RMI, DISABLE_RMI);
        }

        // disable automatic starting of the HTTP server
        if (parser.getOptionValue(EmbeddedMulgaraOptionParser.NO_HTTP) != null) {
          System.setProperty(DISABLE_HTTP, DISABLE_HTTP);
        }

        // set the hostname to bind Mulgara to
        String host = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.SERVER_HOST);

        if (host != null) {
          ServerInfo.setBoundHostname(host);
        } else {
          // get the hostname from configuration file
          String configHost = mulgaraConfig.getMulgaraHost();
          // obtain the default host name if none is configured
          if ((configHost == null) || configHost.equals("")) configHost = getResolvedLocalHost();
          // set the host name
          ServerInfo.setBoundHostname(configHost);
        }

        // set up the client peer port in RMI
        Integer rmiObjectPort = (Integer)parser.getOptionValue(EmbeddedMulgaraOptionParser.RMI_OBJECT_PORT);
        if (rmiObjectPort != null) Rmi.setDefaultPort(rmiObjectPort);

        // set the port on which the RMI registry will be created
        String rmiPortStr = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.RMI_PORT);
        int rmiPort = (rmiPortStr != null) ? Integer.parseInt(rmiPortStr) : mulgaraConfig.getRMIPort();
        ServerInfo.setRMIPort(rmiPort);
        System.setProperty(Context.PROVIDER_URL, "rmi://" + ServerInfo.getBoundHostname() + ":" + rmiPort + "/");

        // set up the default graph to use for SPARQL
        String defaultGraph = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.DEFAULT_GRAPH);
        if (defaultGraph != null) {
          ServerInfo.setDefaultGraphURI(new URI(defaultGraph));
        } else {
          defaultGraph = mulgaraConfig.getDefaultGraph();
          if (defaultGraph != null) ServerInfo.setDefaultGraphURI(new URI(defaultGraph));
        }

        // set up system properties that are used by external packages
        configureSystemProperties();

        // load an external logging configuration
        String loggingConfig = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.LOG_CONFIG);
        if (loggingConfig != null) {
          loadLoggingConfig(new URL(loggingConfig));
        } else {
          loadLoggingConfig(mulgaraConfig.getExternalConfigPaths().getMulgaraLogging());
        }

        if (System.getProperty(DISABLE_HTTP) == null && !mulgaraConfig.getJetty().getDisabled()) {
          httpEnabled = true;
          Connector httpConnector = mulgaraConfig.getJetty().getConnector();
          PublicConnector httpPublicConnector = mulgaraConfig.getJetty().getPublicConnector();
    
          String httpHost = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.HTTP_HOST);
          httpHostName = (httpHost != null || httpConnector == null) ? httpHost : httpConnector.getHost();
    
          // set the port on which to accept HTTP requests
          String httpPort = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.PORT);
          if (httpPort != null) {
            ServerInfo.setHttpPort(Integer.parseInt(httpPort));
          } else {
            if (httpConnector != null) ServerInfo.setHttpPort(httpConnector.getPort());
          }

          // set the port on which to accept public HTTP requests
          httpPort = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.PUBLIC_PORT);
          if (httpPort != null) {
            ServerInfo.setPublicHttpPort(Integer.parseInt(httpPort));
          } else {
            if (httpPublicConnector != null) ServerInfo.setPublicHttpPort(httpPublicConnector.getPort());
          }
        } else {
          httpEnabled = false;
          httpHostName = "";
        }

        // set the (RMI) name of the server, preferencing the command line
        String serverName = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.SERVER_NAME);
        rmiServerName = (serverName != null) ? serverName : mulgaraConfig.getServerName();

        // set the server's persistence path
        persistencePath = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.PERSISTENCE_PATH);
        if (persistencePath == null) persistencePath = mulgaraConfig.getPersistencePath();

        // if the persistence path was one we know about, substitute it
        if (persistencePath.equalsIgnoreCase(".")) {
          persistencePath = System.getProperty("user.dir");
        } else if (persistencePath.equalsIgnoreCase("temp")) {
          persistencePath = System.getProperty("java.io.tmpdir");
        }

        // set the smtp name of the server
        String smtpServer = (String)parser.getOptionValue(EmbeddedMulgaraOptionParser.SMTP_SERVER);
        if (smtpServer == null) smtpServer = mulgaraConfig.getSmtp();
       // set the property for mail package to pickup
        System.setProperty(SYSTEM_MAIL, smtpServer);
      }
    } catch (MalformedURLException mue) {
      log.warn("Invalid URL on command line - " + mue.getMessage());
      printUsage();
      startServer = false;
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
      printUsage();
      startServer = false;
    } catch (NumberFormatException nfe) {
      log.warn("Invalid port specified on command line: " + nfe.getMessage());
      printUsage();
      startServer = false;
    } catch (org.exolab.castor.xml.MarshalException me) {
      log.warn("Castor Marshal Exception: " + me.getMessage(), me);
      printUsage();
      startServer = false;
    } catch (org.exolab.castor.xml.ValidationException ve) {
      log.warn("Unable to load configuration - " + ve.getMessage());
      printUsage();
      startServer = false;
    } catch (Exception e) {
      log.warn("Could not start embedded Mulgara server", e);
      startServer = false;
    }

    // return true if the server should be started, false otherwise
    return startServer;
  }


  /**
   * Creates a Mulgara server.
   * @param serverName the RMI binding name of the server
   * @param statePath the path to the directory containing server state
   * @param hostname the hostname to bind the Mulgara server to
   * @param providerClassName  class name of a {@link org.mulgara.server.Session} implementation
   * @param serverClassName    class name of a {@link org.mulgara.server.ServerMBean}
   * @return a Mulgara server
   * @throws ClassNotFoundException if <var>serverClassName</var> isn't in the classpath
   * @throws IOException if the <var>statePath</var> is invalid
   */
  public ServerMBean createServer(String serverName, File statePath, String hostname,
      int portNumber, String providerClassName, String serverClassName)
      throws ClassNotFoundException, IOException {

    // log that we're createing a Mulgara server
    if (log.isDebugEnabled()) {
      log.debug("Creating server instance at rmi://" + hostname + "/" + serverName + " in directory " + statePath);
    }

    // Create the Mulgara server
    ServerMBean mbean = (ServerMBean)Beans.instantiate(getClass().getClassLoader(),serverClassName);

    // Set ServerMBean properties
    mbean.setDir(statePath);
    File tempDir = new File(statePath,"temp");
    mbean.setTempDir(tempDir);
    mbean.setConfig(mulgaraConfig);

    if (log.isDebugEnabled()) log.debug("Set config to be: " + mulgaraConfig);

    // set the directory that all temporary files will be created in.
    if (!tempDir.mkdirs()) {
      tempDir = TempDir.getTempDir();
    } else {
      TempDir.setTempDir(tempDir);
    }

    // remove any temporary files 
    cleanUpTemporaryFiles();

    mbean.setProviderClassName(providerClassName);

    // Check to see if the port number is not 1099 and we're using the RMI server.
    if ((portNumber != 1099) && (serverClassName.equals(DEFAULT_SERVER_CLASS_NAME))) {
      mbean.setPortNumber(portNumber);
    }
    mbean.setHostname(hostname);

    // Set protocol-specific properties (FIXME: hardcoded to do "name" only)
    try {
      Method setter = new PropertyDescriptor("name", mbean.getClass()).getWriteMethod();
      try {
        setter.invoke(mbean, new Object[] {serverName});
      } catch (InvocationTargetException e) {
        log.warn(mbean + " doesn't have a name property", e);
      }

      // Now the mbean has the hostname and server name, we can set the URI for the server info
      ServerInfo.setServerURI(mbean.getURI());

    } catch (IllegalAccessException e) {
      log.warn(serverClassName + " doesn't have a public name property", e);
    } catch (IntrospectionException e) {
      log.warn(serverClassName + " doesn't have a name property", e);
    }

    // return the newly created server instance
    return mbean;
  }


  /**
   * Sets up any system properties needed by system components like JNDI and security.
   * @throws IOException if any files embedded within the JAR file cannot be found
   */
  protected final void configureSystemProperties() throws IOException {
    boolean startedLocalRMIRegistry = false;

    // attempt to start a rmiregistry
    if (System.getProperty(DISABLE_RMI) == null) {
      try {
        // start the registry
        LocateRegistry.createRegistry(ServerInfo.getRMIPort());
        if (log.isInfoEnabled()) log.info("RMI Registry started automatically on port " + ServerInfo.getRMIPort());
        // set the flag
        startedLocalRMIRegistry = true;
      } catch (java.rmi.server.ExportException ex) {
        log.info("Existing RMI registry found on port " + ServerInfo.getRMIPort());
      } catch (Exception ex) {

        log.error("Failed to start or detect RMI Registry", ex);
      }
    }

    // set system properties needed for RMI
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);

    if (log.isDebugEnabled()) log.debug("No system security manager set");

    // only set the security policy if a RMI registry has started
    if (startedLocalRMIRegistry) {
      if (System.getProperty(SECURITY_POLICY_PROP) == null) {
        if (log.isDebugEnabled()) log.debug("Started local RMI registry -> setting security policy");

        URL mulgaraSecurityPolicyURL = ClassLoader.getSystemResource(RMI_SECURITY_POLICY_PATH);
        System.setProperty(SECURITY_POLICY_PROP, mulgaraSecurityPolicyURL.toString());

        if (log.isInfoEnabled()) log.info("java.security.policy set to " + mulgaraSecurityPolicyURL.toString());
      }

      // create a security manager
      System.setSecurityManager(new RMISecurityManager());
    }
  }

  /**
   * Configure any singleton registries based on the configuration file.
   */
  protected void configureRegistries() {
    FunctionResolverRegistry fnReg = FunctionResolverRegistry.getFunctionResolverRegistry();
    for (XpathFunctionResolver r: mulgaraConfig.getXpathFunctionResolver()) {
      try {
        fnReg.register(r.getType());
      } catch (ClassNotFoundException e) {
        log.error("Unable to load the XPathFunctionResolver: " + r.getType(), e);
      }
    }
  }

  /**
   * Prints the usage instructions for starting the server.
   */
  public static void printUsage() {
    // build the usage message
    StringBuilder usage = new StringBuilder();
    usage.append("Usage: java -jar <jarfile> ");
    usage.append("[-h] ");
    usage.append("[-n] ");
    usage.append("[-x] ");
    usage.append("[-l <url>] ");
    usage.append("[-c <path>] ");
    usage.append("[-k <hostname>] ");
    usage.append("[-o <hostname>] ");
    usage.append("[-p <port>] ");
    usage.append("[-r <port>] ");
    usage.append("[-s <servername>] ");
    usage.append("[-a <path>] ");
    usage.append("[-m <smtp>]" + eol);
    usage.append("" + eol);
    usage.append("-h, --help          display this help screen" + eol);
    usage.append("-n, --normi         disable automatic starting of the RMI registry" + eol);
    usage.append("-w, --nohttp        disable the HTTP web service" + eol);
    usage.append("-x, --shutdown      shutdown the local running server" + eol);
    usage.append("-l, --logconfig     use an external logging configuration file" + eol);
    usage.append("-c, --serverconfig  use an external server configuration file" + eol);
    usage.append("-k, --serverhost    the hostname to bind the server to" + eol);
    usage.append("-o, --httphost      the hostname for HTTP requests" + eol);
    usage.append("-p, --port          the port for HTTP requests" + eol);
    usage.append("-g, --defaultgraph  the default graph to use for SPARQL connections" + eol);
    usage.append("-r, --rmiport       the RMI registry port" + eol);
    usage.append("-t, --rmiobjectport the RMI peer port for objects" + eol);
    usage.append("-s, --servername    the (RMI) name of the server" + eol);
    usage.append("-a, --path          the path server data will persist to, specifying " + eol +
        "                    '.' or 'temp' will use the current working directory " + eol +
        "                    or the system temporary directory respectively" + eol);
    usage.append("-m, --smtp          the SMTP server for email notifications" + eol);
    usage.append(eol);

    usage.append("Note 1. A server can be started without any options, all options" + eol +
        "override default settings." + eol + eol);
    usage.append("Note 2. If an external configuration file is used, and other options" + eol +
        "are specified, the other options will take precedence over any settings" + eol +
        "specified in the configuration file." + eol + eol);

    // print the usage
    System.out.println(usage.toString());
  }


  /**
   * Clean up any temporary files and directories. In some instances the VM does
   * not remove temporary files and directories. ie Windows JVM Some files maybe
   * left due to file locking, however they will be deleted on the next clean
   * up.
   *
   */
  private static void cleanUpTemporaryFiles() {
    File tempDirectory = TempDir.getTempDir(false);

    // Add a filter to ensure we only delete the correct files
    File[] list = tempDirectory.listFiles(new TemporaryFileNameFilter());

    // nothing to be removed
    if (list == null) return;

    // Remove the top level files and recursively remove all files in each directory.
    for (File f: list) {
      if (f.isDirectory()) removeDirectory(f);
      f.delete();
    }
  }


  /**
   * Remove the contents of a directory.
   * @param directory A {@link java.io.File} representing a directory.
   */
  private static void removeDirectory(File directory) {
    File[] list = directory.listFiles();

    if (list == null) {
      log.error("Unable to remove directory: \"" + directory + "\"");
      return;
    }

    for (File f: list) {
      if (f.isDirectory()) removeDirectory(f);
      f.delete();
    }
  }

  /**
   * Create an HttpServices implementation without requiring the source to be available
   * at compile time. Ensures that the original exception type is thrown.
   * @param server The server to create the services for.
   * @param httpHostName The name of the host for the HTTP server.
   * @param mulgaraConfig The configuration for the server.
   * @return A new HttpServices object.
   * @throws IOException Exception setting up with files or network.
   * @throws SAXException Problem reading XML configurations.
   * @throws ClassNotFoundException An expected class was not found.
   * @throws NoSuchMethodException A configured class was not built as expected.
   * @throws InvocationTargetException A configured class did not behave as expected.
   * @throws IllegalAccessException A configured class was not accessible.
   */
  private static HttpServices createHttpServices(EmbeddedMulgaraServer server, String httpHostName, MulgaraConfig mulgaraConfig)
        throws IOException, SAXException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> servicesClass = Class.forName(HttpServices.IMPL_CLASS_NAME);
    try {
      return (HttpServices)Reflect.newInstance(servicesClass, server, httpHostName, mulgaraConfig);
    } catch (RuntimeException e) {
      Throwable wrapped = e.getCause();
      if (wrapped instanceof IOException) {
        throw (IOException)wrapped;
      } else if (wrapped instanceof SAXException) {
        throw (SAXException)wrapped;
      } else if (wrapped instanceof ClassNotFoundException) {
        throw (ClassNotFoundException)wrapped;
      } else if (wrapped instanceof NoSuchMethodException) {
        throw (NoSuchMethodException)wrapped;
      } else if (wrapped instanceof InvocationTargetException) {
        throw (InvocationTargetException)wrapped;
      } else if (wrapped instanceof IllegalAccessException) {
        throw (IllegalAccessException)wrapped;
      } else throw e;
    }
  }

  /**
   * Filter class for detecting temporary files created by Mulgara
   */
  private static class TemporaryFileNameFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
      // check for files and directories with
      // mulgara*.jar , Jetty-*.war and JettyContext*.tmp
      return (((name.indexOf("mulgara") == 0) && (name.indexOf(".jar") > 0)) ||
             ((name.indexOf("Jetty-") == 0) && (name.indexOf(".war") > 0)) ||
             ((name.indexOf("JettyContext") == 0) &&
             (name.indexOf(".tmp") > 0)));
    }
  }


  /**
   * A server side shutdown service to allow the BootStrap class to force a
   * shutdown from another JVM while on the same machine only. To override the
   * default port of 6789 set a system property called shutdownhook.port
   */
  private static class ShutdownService extends Thread {

    private ServerSocket shutdownSocket;

    public ShutdownService() {
      // register a thread name
      this.setName("Server side shutdown hook");
      this.setDaemon(true);
    }

    public void run() {
      boolean stop = false;

      // get the current shutdownhook port
      int port = EmbeddedMulgaraServer.getShutdownHookPort();

      // bind to the specified socket on the local host
      Socket socket = null;
      BufferedReader input = null;
      try {
        shutdownSocket = new ServerSocket(port, 0, InetAddress.getByName("localhost"));

        // wait until a request to stop the server
        while (!stop) {
          // wait for a shutdown request
          socket = shutdownSocket.accept();

          // read the response from the client
          input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

          // check if the require request is correct
          String message = null;
          try {
            message = org.mulgara.util.io.IOUtil.readLine(input, EmbeddedMulgaraServer.SHUTDOWN_MSG.length() + 2);
          } finally {
            socket.close();
          }

          if ((message != null) && message.equals(EmbeddedMulgaraServer.SHUTDOWN_MSG)) {
            stop = true;
          } else {
            if (message != null) log.error("Incorrect request to shutdown mulgara");
          }
        }
      } catch (IOException ioEx) {
        log.error("Unable to establish shutdown socket due to an I/O exception on port " + port, ioEx);
      } catch (SecurityException secEx) {
        log.error("Unable to establish shutdown socket due to a security exception. Check security policy", secEx);
      } catch (Exception ex) {
        log.error("Unable to establish shutdown socket on port " + port, ex);
      } finally {
        if (input != null) {
          try {
            input.close();
          } catch (IOException e) {
            log.error("Unexpected problem closing input from a socket");
          }
        }
        if (socket != null) {
          try {
            socket.close();
          } catch (IOException e) {
            log.error("Unexpected problem closing socket");
          }
        }
        // attempt to close the socket
        try {
          shutdownSocket.close();
        } catch (Exception ex) {
          log.error("Unexpected problem closing the shutdown socket", ex);
        }
      }

      // log that we're sutting down the servers
      if (log.isInfoEnabled()) {
        log.info("Started system exit.");
      }

      // finally
      // issue the shutdown
      if (stop) System.exit(0);
    }
  }


  /**
   * The standard shutdown hook that will get run when this server is killed normally.
   * This gets registered with the Runtime.
   */
  private static class RuntimeShutdownHook extends Thread {
    
    EmbeddedMulgaraServer server;
  
    public RuntimeShutdownHook(EmbeddedMulgaraServer server) {
      this.server = server;
      // register a thread name
      this.setName("Standard shutdown hook");
    }
  
    public void run() {
      // log that we're sutting down the servers
      if (log.isInfoEnabled()) {
        log.info("Shutting down server, please wait...");
      } else {
        // regardless of the log level output this to stdout.
        // Note. "\n" Will give us a new line beneath a Ctrl-C
        System.out.println("\nShutting down server, please wait...");
      }

      // stop RMI service
      ServerMBean mbean = server.getServerMBean();
      if (mbean != null) {
        ServerState state = mbean.getState();
  
        if (state == ServerState.STARTED) {
          try {
            mbean.stop();
          } catch (Exception e) {
            log.error("Couldn't stop server", e);
          }
        }
  
        // close the server
        if (state == ServerState.STARTED || state == ServerState.STOPPED) {
          try {
            mbean.destroy();
          } catch (Exception e) {
            log.error("Couldn't destroy server", e);
          }
        }
      }

      // shut down the SOAP server
      try {
        if (server.webServices != null) server.webServices.stop();
      } catch (Exception e) {
        log.error("Couldn't destroy http server", e);
      }

      // log that we've shut down the servers
      if (log.isInfoEnabled()) {
        log.info("Completed shutting down server");
      } else {
        // regardless of the log level out this to stdout.
        System.out.println("Completed shutting down server");
      }

      // Clean up any temporary directories and files
      cleanUpTemporaryFiles();
    }
  }
}
