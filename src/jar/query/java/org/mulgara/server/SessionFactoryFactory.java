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

// Java 2 Standard Packages
import java.net.*;
import java.io.*;

// Third party packages
import org.apache.log4j.Logger; // Apache Log4J

// Locally written packages
import org.mulgara.config.MulgaraConfig;
import org.mulgara.server.SessionFactory;
import org.mulgara.util.ClasspathDesc;
import org.mulgara.util.ObjectUtil;

import java.lang.reflect.Constructor;

/**
 * Used to obtain a {@link SessionFactory} instance using a configuration file
 * or by method arguments.
 *
 * @created 2004-09-06
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/13 01:55:09 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SessionFactoryFactory {

  /** Logger. This is named after the class. */
  private static final Logger logger = Logger.getLogger(SessionFactoryFactory.class.getName());

  /** Server name used if one is not configured */
  public final static String DEFAULT_SERVER_NAME = "server1";

  /** SessionFactory implementation  */
  private String className = "org.mulgara.store.xa.XADatabaseImpl";

  /** default location of the config file */
  private static final String CONFIG_PATH = "conf/mulgara-x-config.xml";

  /** Config to use when creating new sessions */
  private MulgaraConfig mulgaraConfig;

  /**
   * Default Constructor.
   *
   * @throws SessionFactoryException
   */
  public SessionFactoryFactory() throws SessionFactoryException {

    try {

      // get a URL to the default server configuration file
      URL defaultConfigURL = ClassLoader.getSystemResource(CONFIG_PATH);
      if (defaultConfigURL == null) {
      
        defaultConfigURL = ObjectUtil.getClassLoader(this).getResource(CONFIG_PATH);
        if (defaultConfigURL == null) {

          throw new SessionFactoryException(
              "Unable to locate configuration file: " + defaultConfigURL + 
              " using the local or system classloader");
        }
      }

      SessionFactoryConfiguration config = 
          this.getConfiguration(defaultConfigURL.openStream());

      //check if the "className" has been configured or set as a system property.
      String implementation = config.getClassName();
      String tripleStoreImpl = System.getProperty("triple.store.implementation");

      //try config first
      if ( (implementation != null)
          && (!"".equals(implementation))) {

        //use config file property
        this.className = implementation;
      }
      else if ( (tripleStoreImpl != null)
               && (!"".equals(tripleStoreImpl))) {

        //use system property
        this.className = tripleStoreImpl;
      }
    } catch (IOException ioException) {

      throw new SessionFactoryException("Could not open Configuration File.",
                                        ioException);
    }
  }

  /**
   * Constructor. Sets the SessionFactory implementation to be used.
   *
   * @param className String
   * @throws SessionFactoryException
   */
  public SessionFactoryFactory(String className) throws SessionFactoryException {

    this();

    this.setSessionFactoryClass(className);
  }

  /**
   * Sets the SessionFactory implementation to be instantiated.
   *
   * @param className String
   */
  private void setSessionFactoryClass(String className) {

    this.className = className;
  }

  /**
   * Creates a new instance of the configured TripleStoreImplementation using
   * the constructor specified by argTypes using args as constructor arguments.
   *
   * @param argTypes Class[]
   * @param args Object[]
   *
   * @throws SessionFactoryException
   * @return SessionFactory
   */
  @SuppressWarnings("unchecked")
  public SessionFactory getTripleStoreImplementation(String className,
      Class<?>[] argTypes, Object[] args) throws SessionFactoryException {

    try {

      //load class
      Class<? extends SessionFactory> storeClass = (Class<? extends SessionFactory>)Class.forName(className);

      //get appropriate constructor
      Constructor<? extends SessionFactory> constructor = storeClass.getConstructor(argTypes);

      //instantiate
      return constructor.newInstance(args);
    } catch (ClassNotFoundException cnf) {
      logger.error("Could not find TripleStoreImplementation class in: " +
          ClasspathDesc.getPath());
      throw new SessionFactoryException("Could not instantiate TripleStoreImplementation from configuration.", cnf);
    } catch (Exception exception) {

      throw new SessionFactoryException("Could not instantiate " +
                                        "TripleStoreImplementation from " +
                                        "configuration.", exception);
    }
  }

  /**
   * Obtain a {@link SessionFactory} instance. Using the default server URI and
   * the tmp directory.
   *
   * @throws SessionFactoryException if a connection can't be established to
   *   the server
   * @return SessionFactory
   */
  public SessionFactory newSessionFactory()
      throws SessionFactoryException {

    URI serverURI = getDefaultServerURI();
    File directory = new File(System.getProperty("java.io.tmpdir"));

    return this.newSessionFactory(serverURI, directory);
  }

  /**
   * Obtain a {@link SessionFactory} instance.
   *
   * @param serverURI the internet server to connect this session to.
   * @param directory The directory to use for storage of triplestore data
   *
   * @throws SessionFactoryException if a connection can't be established to the server
   * @return SessionFactory
   */
  public SessionFactory newSessionFactory(URI serverURI, File directory)
                                          throws SessionFactoryException {

    //arguments to constructor
    Class<?>[] argTypes = new Class [] {
      serverURI.getClass(),
      directory.getClass(),
      mulgaraConfig.getClass()
    };
    Object[] args = new Object [] {
      serverURI,
      directory,
      mulgaraConfig
    };

    return getTripleStoreImplementation(this.className, argTypes, args);
  }

  /**
   * Obtain a {@link SessionFactory} instance.
   *
   * @param inStream  stream containing the configuration
   * @throws SessionFactoryException
   * @return SessionFactory
   */
  public SessionFactory newSessionFactory(InputStream inStream)
      throws SessionFactoryException {

    //parse the configuration file
    SessionFactoryConfiguration config = getConfiguration(inStream);

    //arguments to constructor
    Class<?>[] argTypes = config.getConfigurationTypes();
    Object[] args = config.getConfigurationObjects();

    return getTripleStoreImplementation(config.getClassName(), argTypes, args);
  }

  /**
   * Obtain a {@link SessionFactory} instance.
   *
   * @param uri URI
   * @param directory File
   * @param securityDomain URI
   * @param persistentNodePoolFactoryClassName String
   * @param persistentStringPoolFactoryClassName String
   * @param temporaryNodePoolFactoryClassName String
   * @param temporaryStringPoolFactoryClassName String
   * @param systemResolverFactoryClassName String
   * @throws SessionFactoryException
   * @return SessionFactory
   */
  public SessionFactory newSessionFactory(URI uri, File   directory,
                  URI    securityDomain, String persistentNodePoolFactoryClassName,
                  String persistentStringPoolFactoryClassName, String
                  temporaryNodePoolFactoryClassName, String
                  temporaryStringPoolFactoryClassName, String
                  systemResolverFactoryClassName)
      throws SessionFactoryException {

    //arguments to constructor
    Class<?>[] argTypes = new Class[] {
      uri.getClass(),
      directory.getClass(),
      securityDomain.getClass(),
      persistentNodePoolFactoryClassName.getClass(),
      persistentStringPoolFactoryClassName.getClass(),
      temporaryNodePoolFactoryClassName.getClass(),
      temporaryStringPoolFactoryClassName.getClass(),
      systemResolverFactoryClassName.getClass()
    };
    Object[] args = new Object[] {
      uri,
      directory,
      securityDomain,
      persistentNodePoolFactoryClassName,
      persistentStringPoolFactoryClassName,
      temporaryNodePoolFactoryClassName,
      temporaryStringPoolFactoryClassName,
      systemResolverFactoryClassName
    };

    return getTripleStoreImplementation(this.className, argTypes, args);
  }

  /**
   * Returns the default Server URI.
   *
   * <p>currently uses: rmi://localhost-address/DEFAULT_SERVER_NAME
   *
   * @return a Java RMI server {@link URI} for the local host, never
   *   <code>null</code>
   * @throws SessionFactoryException if the server URI can't be composed
   */
  public static URI getDefaultServerURI() throws SessionFactoryException {

    return getServerURI(DEFAULT_SERVER_NAME);
  }

  /**
   * Returns the local Server URI using the supplied name.
   *
   * <p>currently uses: rmi://localhost-address/serverName
   *
   * @param serverName the name of the server
   * @return a Java RMI server {@link URI} for the local host, never
   *   <code>null</code>
   * @throws SessionFactoryException if the server URI can't be composed
   */
  public static URI getServerURI(String serverName) throws
      SessionFactoryException {

    //validate
    if (serverName == null) {

      throw new IllegalArgumentException("'serverName' argument is null.");
    }

    try {

      //local host name
      String localHost = InetAddress.getLocalHost().getCanonicalHostName();

      //add '/' prefix (if not already there)
      String name = serverName;
      if (!name.startsWith("/")) {

        name = "/" + name;
      }

      return new URI("rmi", localHost, name, null);
    }
    catch (UnknownHostException hostException) {

      throw new SessionFactoryException("Couldn't determine local host name",
                                        hostException);
    }
    catch (URISyntaxException uriException) {

      throw new SessionFactoryException("Invalid local server URI.",
                                        uriException);
    }
  }

  /**
   * Loads the configuration file and sets any (relevant) properties supplied.
   *
   * @param inStream  a stream containing the configuration
   * @throws SessionFactoryException
   */
  private SessionFactoryConfiguration getConfiguration(InputStream inStream)
      throws SessionFactoryException {

    //validate
    if (inStream == null) {

      throw new IllegalArgumentException("Configuration File Input Stream is " +
                                         "null.");
    }

    try {

      //return value
      SessionFactoryConfiguration sessionConfig = new
          SessionFactoryConfiguration();

      //create a configuration object from the stream
      InputStreamReader reader = new InputStreamReader(inStream);
      mulgaraConfig = MulgaraConfig.unmarshal(reader);
      mulgaraConfig.validate();

      //set configuration properties

      //className
      String storeImpl = mulgaraConfig.getTripleStoreImplementation();
      if ((storeImpl != null)
          && (!"".equals(storeImpl))) {

        sessionConfig.setClassName(storeImpl);
      }

      //server name (sets server URI)
      String serverName = mulgaraConfig.getServerName();
      if ((serverName != null)
          && (!"".equals(serverName))) {

        URI uri = getServerURI(serverName);
        sessionConfig.setServerURI(uri.toString());
      }

      //serverURI (overrides URI set for serverName)
      String host = mulgaraConfig.getMulgaraHost();
      if ((host != null)
          && (!"".equals(host))) {

        sessionConfig.setServerURI(host);
      }

      //directory
      String persistencePath = mulgaraConfig.getPersistencePath();
      if ((persistencePath != null)
          && (!"".equals(persistencePath))) {

        // '.' and "temp" are valid options
        if (persistencePath.equalsIgnoreCase(".")) {

          persistencePath = System.getProperty("user.dir");
        }
        else if (persistencePath.equalsIgnoreCase("temp")) {

          persistencePath = System.getProperty("java.io.tmpdir");
        }

        sessionConfig.setDirectory(persistencePath);
      }

      return sessionConfig;
    }
    catch (org.exolab.castor.xml.MarshalException marshalException) {

      // log the error
      throw new SessionFactoryException("Castor Marshal Exception: ",
                                        marshalException);
    }
    catch (org.exolab.castor.xml.ValidationException validException) {

      // log the error
      throw new SessionFactoryException("Unable to load configuration - ",
                                        validException);
    }
    finally {

      //clean up
      try {

        if (inStream != null) {

          inStream.close();
        }
      }
      catch (IOException ioException) {

        //can't do anything
        if (logger.isDebugEnabled()) {

          logger.debug("Could not close file stream.", ioException);
        }
      }
    }
  }

}
