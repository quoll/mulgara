/*
 * Copyright 2008 Fedora Commons, Inc.
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


package org.mulgara.server;

import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.MultiException;
import org.mulgara.config.JettyConnector;
import org.mulgara.config.MulgaraConfig;
import org.mulgara.util.JettyLogger;
import org.mulgara.util.Reflect;
import org.mulgara.util.TempDir;
import org.mulgara.util.functional.Fn1E;
import org.mulgara.util.functional.Pair;
import org.xml.sax.SAXException;

/**
 * Manages all the HTTP services provided by a Mulgara server.
 *
 * @created Sep 5, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 */
public class HttpServicesImpl implements HttpServices {

  /** A virtual typedef for a context starter. */
  private interface ContextStarter extends Fn1E<Server,Pair<String,String>,IOException> { }

  /** A virtual typedef for a service path. */
  private class Service extends Pair<String,String> { Service(String f, String s) { super(f,s); } }

  /** The logging category to log to. */
  protected static final Logger logger = Logger.getLogger(HttpServicesImpl.class.getName());

  /** The web application file path. */
  private final static String WEBAPP_PATH = "webapps";

  /** The Web Services web application file. */
  private final static String WEBSERVICES_WEBAPP = "webservices.war";

  /** The Web Services path. */
  private final static String WEBSERVICES_PATH = "webservices";

  /** The Web Query path. */
  private final static String WEBQUERY_PATH = "webui";

  /** The Web Tutorial path. */
  private final static String WEBTUTORIAL_PATH = "tutorial";

  /** The sparql path. */
  private final static String SPARQL_PATH = "sparql";

  /** The tql path. */
  private final static String TQL_PATH = "tql";

  /** The default service path. */
  private final static String DEFAULT_SERVICE = WEBQUERY_PATH;

  /** The key to the bound host name in the attribute map of the servlet context. */
  public final static String BOUND_HOST_NAME_KEY = "boundHostname";

  /** Key to the bound server model uri in the attribute map of the servlet context. */
  public final static String SERVER_MODEL_URI_KEY = "serverModelURI";

  /** The HTTP server instance. */
  private final Server httpServer;

  /** The Public HTTP server instance. */
  private final Server httpPublicServer;

  /** The configuration for the server. */
  private final MulgaraConfig config;

  /** The name for the host. */
  private String hostName;

  /** The host server. This may contain information useful to services. */
  private final EmbeddedMulgaraServer hostServer;


  /**
   * Creates the web services object.
   * @param hostServer The Server that started these Web services.
   * @param hostName The name of the HTTP host this object is setting up.
   * @param config The configuration to use.
   * @throws IOException Exception setting up with files or network.
   * @throws SAXException Problem reading XML configurations.
   * @throws ClassNotFoundException An expected class was not found.
   * @throws NoSuchMethodException A configured class was not built as expected.
   * @throws InvocationTargetException A configured class did not behave as expected.
   * @throws IllegalAccessException A configured class was not accessible.
   */
  public HttpServicesImpl(EmbeddedMulgaraServer hostServer, String hostName, MulgaraConfig config)  throws IOException, SAXException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    this.hostServer = hostServer;
    this.config = config;
    this.hostName = hostName;
    assert !config.getJetty().isDisabled();
    // get servers as a pair so we can set them here. Needed because they are final.
    Pair<Server,Server> servers = createHttpServers();
    httpServer = servers.first();
    httpPublicServer = servers.second();
  }


  /**
   * @see org.mulgara.server.HttpServices#start()
   */
  @SuppressWarnings("unchecked")
  public void start() throws ExceptionList, Exception {
    try {
      if (httpServer != null) httpServer.start();
      if (httpPublicServer != null) httpPublicServer.start();
    } catch (MultiException e) {
      throw new ExceptionList(e.getThrowables());
    }
  }


  /**
   * @see org.mulgara.server.HttpServices#stop()
   */
  public void stop() throws Exception {
    try {
      if (httpServer != null) httpServer.stop();
    } finally {
      if (httpPublicServer != null) httpPublicServer.stop();
    }
  }


  /**
   * Creates an HTTP server.
   * @return a pair of private/public servers.
   * @throws IOException if the server configuration cannot be found
   * @throws SAXException if the HTTP server configuration file is invalid
   * @throws ClassNotFoundException if the HTTP server configuration file contains a reference to an unkown class
   * @throws NoSuchMethodException if the HTTP server configuration file contains a reference to an unkown method
   * @throws InvocationTargetException if an error ocurrs while trying to configure the HTTP server
   * @throws IllegalAccessException If a class loaded by the server is accessed in an unexpected way.
   */
   Pair<Server,Server> createHttpServers() throws IOException, SAXException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if (logger.isDebugEnabled()) logger.debug("Creating HTTP server instance");

    // Set the magic logging property for Jetty to use Log4j
    System.setProperty(JettyLogger.LOGGING_CLASS_PROPERTY, JettyLogger.class.getCanonicalName());
    JettyLogger.setEnabled(false);
    org.eclipse.jetty.util.log.Log.setLog(new JettyLogger());

    // create and register a new HTTP server
    Server privateServer = buildAndConfigure(config.getJetty().getConnector(), ServerInfo.getHttpPort());
    Server publicServer = buildAndConfigure(config.getJetty().getPublicConnector(), ServerInfo.getPublicHttpPort());


    if (privateServer != null) {
      // Accumulator for all the services
      Map<String,String> privateServices = new HashMap<String,String>();

      // start all the private configured services.
      for (ContextStarter starter: getContextStarters()) {
        try {
          starter.fn(privateServer).addTo(privateServices);
        } catch (IllegalStateException e) {
          // not fatal, so just log the problem and go on
          logger.warn("Unable to start web service", e.getCause());
        }
      }

      // we have all the services, so now instantiate the service listing service
      addWebServiceListingContext(privateServer, privateServices);
    }
    
    if (publicServer != null) {
      // start the public contexts
      for (ContextStarter starter: getPublicContextStarters()) {
        try {
          starter.fn(publicServer);
        } catch (IllegalStateException e) {
          logger.warn("Unable to start public web service", e.getCause());
        }
      }
    }

    // configure the private handlers
    List<Handler> privateHandlers = Collections.emptyList();
    if (privateServer != null) {
      privateHandlers = Arrays.asList(privateServer.getChildHandlers());
      configureHandlers(privateHandlers, config.getJetty().getConnector());
    }
    // configure the private handlers
    List<Handler> publicHandlers = Collections.emptyList();
    if (publicServer != null) {
      publicHandlers = Arrays.asList(publicServer.getChildHandlers());
      configureHandlers(publicHandlers, config.getJetty().getPublicConnector());
    }

    // get all the handlers in use by both servers
    List<Handler> handlers = new ArrayList<Handler>(privateHandlers);
    handlers.addAll(publicHandlers);

    // add our class loader as the classloader of all contexts, unless this is a webapp in which case we wrap it
    ClassLoader classLoader = this.getClass().getClassLoader();
    for (Handler handler: handlers) {
      if (handler instanceof WebAppContext) ((WebAppContext)handler).setClassLoader(new WebAppClassLoader(classLoader, (WebAppContext)handler));
      else if (handler instanceof ContextHandler) ((ContextHandler)handler).setClassLoader(classLoader);
    }

    // return the servers
    return new Pair<Server,Server>(privateServer, publicServer);
  }


  /**
   * Create a server object, and configure it.
   * @param cfg The Jetty configuration for the server.
   * @return The created server.
   * @throws UnknownHostException The configured host name is invalid.
   */
  Server buildAndConfigure(JettyConnector cfg, int port) throws UnknownHostException {
    Server s = null;
    boolean disabled = (cfg != null && cfg.hasDisabled() && cfg.isDisabled());
    if (!disabled) {
      if (cfg != null) {
        s = new Server();
        addConnector(s, cfg, port);
      } else {
        s = new Server(port);
      }
    }
    return s;
  }


  /**
   * Configure all of the handlers with a connector configuration.
   * @param handlers The handlers to configure.
   * @param conCfg The connector configuration to use.
   */
  void configureHandlers(List<Handler> handlers, JettyConnector conCfg) {
    if (conCfg == null) return;
    for (Handler handler: handlers) {
      // expect each handler to be an instance of a ContextHandler, but we still check
      if (handler instanceof ContextHandler) {
        ContextHandler ctx = (ContextHandler)handler;
        // test for parameters, and if present, then set on the handler
        if (conCfg.hasMaxFormContentSize()) {
          ctx.setMaxFormContentSize(conCfg.getMaxFormContentSize());
        }
      }
    }
  }


  /**
   * Creates a list of functions for starting contexts.
   * <strong>This defines the list of services to be run.</strong>
   * @return A list that can start all the configured contexts.
   */
  private List<ContextStarter> getContextStarters() {
    List<ContextStarter> starters = new ArrayList<ContextStarter>();
    starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
      return addWebServicesWebAppContext(s);
    } });
    starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
      return addWebQueryContext(s, "User Interface", "org.mulgara.webquery.QueryServlet", WEBQUERY_PATH);
    } });
    starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
      return addWebQueryContext(s, "User Tutorial", "org.mulgara.webquery.TutorialServlet", WEBTUTORIAL_PATH);
    } });
    // expect to get the following from a config file
    // TODO: create a decent configuration object, instead of just handing out a Server
    starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
      return addServletContext(s, "org.mulgara.protocol.http.SparqlServlet", SPARQL_PATH, "SPARQL HTTP Service");
    } });
    starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
      return addServletContext(s, "org.mulgara.protocol.http.TqlServlet", TQL_PATH, "TQL HTTP Service");
    } });
    return starters;
  }


  /**
   * Creates a list of functions for starting public contexts.
   * <strong>This defines the list of services to be run.</strong>
   * @return A list that can start all the configured contexts.
   */
  private List<ContextStarter> getPublicContextStarters() {
    List<ContextStarter> starters = new ArrayList<ContextStarter>();
    starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
      return addServletContext(s, "org.mulgara.protocol.http.PublicSparqlServlet", SPARQL_PATH, "SPARQL HTTP Service");
    } });
    return starters;
  }


  /**
   * Adds a listener to the <code>httpServer</code>. The listener is created and configured
   * according to the Jetty configuration.
   * @param httpServer the server to add the listener to
   * @param jettyConfig The configuraton for the server. Do not read the port at this point.
   * @param port The port to listen on. The configuration may have been overriden by this value.
   * @throws UnknownHostException if an invalid hostname was specified in the Mulgara server configuration
   */
  @SuppressWarnings("deprecation")
  private void addConnector(Server httpServer, JettyConnector jettyConfig, int port) throws UnknownHostException {
    if (httpServer == null) throw new IllegalArgumentException("Null \"httpServer\" parameter");

    if (logger.isDebugEnabled()) logger.debug("Adding socket listener");

    // create and configure a listener
    AbstractConnector connector = new BlockingChannelConnector();
    if ((hostName != null) && !hostName.equals("")) {
      connector.setHost(hostName);
      if (logger.isDebugEnabled()) logger.debug("Servlet container listening on host " + hostName);
    } else {
      hostName = EmbeddedMulgaraServer.getResolvedLocalHost();
      if (logger.isDebugEnabled()) logger.debug("Servlet container listening on all host interfaces");
    }

    // Each connector will get its own thread pool, so that they may be configured separately.
    // If a connector does not have its own thread pool, it inherits one from the server that it might
    // share with other connectors.
    QueuedThreadPool threadPool = new QueuedThreadPool();
    if (jettyConfig.hasMaxThreads()) {
      threadPool.setMaxThreads(jettyConfig.getMaxThreads());
    }
    connector.setThreadPool(threadPool);

    connector.setPort(port);
    if (jettyConfig.hasMaxIdleTimeMs()) connector.setMaxIdleTime(jettyConfig.getMaxIdleTimeMs());
    if (jettyConfig.hasLowResourceMaxIdleTimeMs()) connector.setLowResourceMaxIdleTime(jettyConfig.getLowResourceMaxIdleTimeMs());
    if (jettyConfig.hasAcceptors()) {
      int acceptors = jettyConfig.getAcceptors();
      // Acceptors are part of the thread pool, but they delegate handling of servlet
      // requests to another thread in the pool.  Therefore, the number of acceptors
      // must be strictly less than the maximum number of threads in the pool.
      int acceptorLimit = threadPool.getMaxThreads() - 1;
      if (acceptors > acceptorLimit) {
        logger.warn("Acceptor threads set beyond HTTP Server limits. Reducing from" + acceptors + " to " + acceptorLimit);
        acceptors = acceptorLimit;
      }
      connector.setAcceptors(acceptors);
    }

    // add the listener to the http server
    httpServer.addConnector(connector);
  }


  /**
   * Creates the Mulgara Descriptor UI. Once created, a description of the created services is returned.
   * @return A Service description of the Descriptor services that were started.
   * @throws IOException if the driver WAR file is not readable
   */
  private Service addWebServicesWebAppContext(Server server) throws IOException {
    // get the URL to the WAR file
    URL webServicesWebAppURL = ClassLoader.getSystemResource(WEBAPP_PATH + "/" + WEBSERVICES_WEBAPP);

    if (webServicesWebAppURL == null) {
      logger.warn("Couldn't find resource: " + WEBAPP_PATH + "/" + WEBSERVICES_WEBAPP);
      return null;
    }

    String warPath = extractToTemp(WEBAPP_PATH + "/" + WEBSERVICES_WEBAPP);
    
    // Add Descriptors and Axis
    String webPath = "/" + WEBSERVICES_PATH;
    WebAppContext descriptorWARContext = new WebAppContext(getHandlerCollection(server), warPath, webPath);

    // make some attributes available
    descriptorWARContext.setAttribute(BOUND_HOST_NAME_KEY, ServerInfo.getBoundHostname());
    descriptorWARContext.setAttribute(SERVER_MODEL_URI_KEY, ServerInfo.getServerURI().toString());

    // log that we're adding the test webapp context
    if (logger.isDebugEnabled()) logger.debug("Added Web Services webapp context");
    return new Service("Web Services", webPath);
  }


  /**
   * Creates and registers a web servlet. The servlet will need to know the server name.
   * @param server The server to connect the servlet to.
   * @param name The human-readable name of the servlet to create.
   * @param servletClass The name of the class that will perform the servlet functions.
   * @param servletPath The path on the server for the servlet to be loaded onto.
   * @return A description of the servlet that was set up.
   * @throws IOException if the servlet cannot talk to the network.
   */
  private Service addWebQueryContext(Server server, String name, String servletClass, String servletPath) throws IOException {
    if (logger.isDebugEnabled()) logger.debug("Adding Web servlet context: " + name);

    // create the web context
    try {
      String rmiName = hostServer.getServerName();
      Servlet servlet = (Servlet)Reflect.newInstance(Class.forName(servletClass), hostName, rmiName, hostServer);
      String webPath = "/" + servletPath;
      new org.eclipse.jetty.servlet.ServletContextHandler(getHandlerCollection(server), webPath, SESSIONS).addServlet(new ServletHolder(servlet), "/*");
      return new Service(name, webPath);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Not configured to use the requested servlet: " + name + "(" + servletClass + ")");
    }
  }


  /**
   * Creates a servlet that requires a Server as a constructor parameter. This is similar to {@link #addWebQueryContext(Server, String, String, String)}
   * except that the servlet does not need to know the server name.
   * @param server The server to connect the servlet to.
   * @param servletClass The name of the servlet class.
   * @param path A relative HTTP path for attaching the servlet. 
   * @param description A description for the servlet.
   * @return The Service describing the new servlet.
   * @throws IOException Due to problems with the file system or the network.
   * @throws IllegalStateException if an unavailable servlet has been requested.
   */
  private Service addServletContext(Server server, String servletClass, String path, String description) throws IOException {
    if (logger.isDebugEnabled()) logger.debug("Adding " + description + " servlet context");

    // create the web query context
    try {
      Servlet servlet = (Servlet)Reflect.newInstance(Class.forName(servletClass), hostServer);
      String webPath = "/" + path;
      new org.eclipse.jetty.servlet.ServletContextHandler(getHandlerCollection(server), webPath, SESSIONS).addServlet(new ServletHolder(servlet), "/*");
      return new Service(description, webPath);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Not configured to use the requested servlet: " + description);
    }
  }

  /**
   * Creates the servlet used to list the other servlets.
   * @param server The server to register this servlet with.
   * @param services The list of service names and paths.
   * @throws IOException If the servlet cannot talk to the network.
   */
  private void addWebServiceListingContext(Server server, Map<String,String> services) throws IOException {
    if (logger.isDebugEnabled()) logger.debug("Adding the service lister context");
    Servlet servlet = new ServiceListingServlet(services, "/" + DEFAULT_SERVICE);
    new org.eclipse.jetty.servlet.ServletContextHandler(getHandlerCollection(server), "/", SESSIONS).addServlet(new ServletHolder(servlet), "/*");
  }

  /**
   * Retrieves a handler collection from the server, setting it if the server does not hold a collection.
   * @param server The server to get a handler collection from.
   * @return A handler collection for the server.
   */
  private HandlerContainer getHandlerCollection(Server server) {
    HandlerCollection handlerCollection;
    Handler handler = server.getHandler();
    if (handler instanceof HandlerCollection) {
      handlerCollection = (HandlerCollection)handler;
    } else {
      handlerCollection = new HandlerCollection();
      if (handler != null) handlerCollection.addHandler(handler);
      server.setHandler(handlerCollection);
    }
    return handlerCollection;
  }

  /**
   * Extracts a resource from the environment (a jar in the classpath) and writes
   * this to a file in the working temporary directory.
   * @param resourceName The name of the resource. This is a relative file path in the jar file.
   * @return The absolute path of the file the resource is extracted to, or <code>null</code>
   *         if the resource does not exist.
   * @throws IOException If there was an error reading the resource, or writing to the extracted file.
   */
  private String extractToTemp(String resourceName) throws IOException {
    // Find the resource
    URL resourceUrl = ClassLoader.getSystemResource(resourceName);
    if (resourceUrl == null) return null;

    // open the resource and the file where it will be copied to
    File outFile = new File(TempDir.getTempDir(), new File(resourceName).getName());
    logger.info("Extracting: " + resourceUrl + " to " + outFile);

    InputStream in = null;
    OutputStream out = null;
    try {
      in = resourceUrl.openStream();
      out = new FileOutputStream(outFile);
  
      // loop to copy from the resource to the output file
      byte[] buffer = new byte[10240];
      int len;
      while ((len = in.read(buffer)) >= 0) out.write(buffer, 0, len);
    } finally {
      if (in != null) in.close();
      if (out != null) out.close();
    }

    // return the file that the resource was extracted to
    return outFile.getAbsolutePath();
  }


}
