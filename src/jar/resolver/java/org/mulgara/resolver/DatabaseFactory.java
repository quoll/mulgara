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
import java.beans.Beans;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import javax.naming.NamingException;

// Java 2 enterprise packages
import javax.transaction.SystemException;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J

// Local packages
import org.mulgara.config.MulgaraConfig;
import org.mulgara.content.ContentHandler;
import org.mulgara.query.QueryException;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.stringpool.StringPoolException;

/**
 * Construct and configure {@link Database} instances, based on a
 * {@link MulgaraConfig} configuration.
 *
 * @created 2004-10-07
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class DatabaseFactory {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(DatabaseFactory.class.getName());

  /**
   * Creates a Database in a similar fashion to the stopgap constructor,
   * however, this version contains a configuration object which will allow the
   * server to configure the String and Node Pools as well as resolvers
   * (including the system resolver) and content handlers.
   *
   * @param uri  the unique {@link URI} naming this database, never
   *   <code>null</code>; this mustn't have a fragment part, because the
   *   fragment is used to represent models within the database
   * @param directory  an area on the filesystem for the database's use; if this
   *   is <code>null</code>, resolvers which require a filesystem can't be added
   * @param config The configuration object that will be used to determine the
   *               missing class names
   *
   * @throws ConfigurationException
   * @throws InitializerException
   * @throws LocalizeException
   * @throws NamingException
   * @throws NodePoolException
   * @throws QueryException
   * @throws ResolverException
   * @throws ResolverFactoryException
   * @throws StringPoolException
   * @throws URISyntaxException
   */
  public static Database newDatabase(URI uri, File directory, MulgaraConfig config)
    throws ConfigurationException, InitializerException, LocalizeException,
           NamingException, NodePoolException, QueryException,
           ResolverException, ResolverFactoryException,
           SecurityAdapterFactoryException, StringPoolException,
           SystemException, URISyntaxException {

    Database database = new Database(
        uri,        // database name
        new File[] {directory},  // persistence directory
        uri,        // security domain
        new JotmTransactionManagerFactory(),
        config.getTransactionTimeout(),
        config.getIdleTimeout(),
        config.getPersistentNodePoolFactory().getType(),
        subdirs(directory, config.getPersistentNodePoolFactory().getDir()),
        config.getPersistentStringPoolFactory().getType(),
        subdirs(directory, config.getPersistentStringPoolFactory().getDir()),
        config.getPersistentResolverFactory().getType(),
        subdirs(directory, config.getPersistentResolverFactory().getDir()),
        config.getTemporaryNodePoolFactory().getType(),
        subdirs(directory, config.getTemporaryNodePoolFactory().getDir()),
        config.getTemporaryStringPoolFactory().getType(),
        subdirs(directory, config.getTemporaryStringPoolFactory().getDir()),
        config.getTemporaryResolverFactory().getType(),
        subdirs(directory, config.getTemporaryResolverFactory().getDir()),
        config.getDefaultContentHandler().getType());

    if (logger.isDebugEnabled()) logger.debug("Constructed database.");

    configure(database, config);

    if (logger.isDebugEnabled()) logger.debug("Configured database.");

    return database;
  }


  static File[] subdirs(File parentDirectory, String childDirectory) throws ConfigurationException {
    if (childDirectory == null) return null;
    if (parentDirectory == null) {
      throw new ConfigurationException("Can't configure directory " + childDirectory + "because there is no PersistencePath configured");
    }
    String[] children = childDirectory.split(File.pathSeparator);
    File[] subdirs = new File[children.length];
    for (int f = 0; f < children.length; f++) subdirs[f] = subdir(parentDirectory, children[f]);
    return subdirs;
  }


  /**
   * @param parentDirectory  the absolute parent directory; this may be
   *   <code>null</code> if <var>childDirectory</var> is also <code>null</code>.
   *   Ignored if the child directory is absolute.
   * @param childDirectory If relative, then relative to the parentDirectory, otherwise absolute.
   * @return if <var>childDirectory</var> is non-<code>null</code> then the
   *   corresponding directory, otherwise <code>null</code>
   * @throws ConfigurationException if <var>childDirectory</var> is not
   *   <code>null</code> and <var>parentDirectory</var> is <code>null</code>
   */
  // TODO: Make this private once the 3-arg constructor is removed from
  //       Database.
  static File subdir(File parentDirectory, String childDirectory) throws ConfigurationException {
    if (childDirectory == null) return null;
    if (parentDirectory == null) {
      throw new ConfigurationException("Can't configure directory " + childDirectory + "because there is no PersistencePath configured");
    }
    if (childDirectory.startsWith("/")) return new File(childDirectory);
    return new File(parentDirectory, childDirectory);
  }


  /**
   * Add the additional resolver factories and content handlers specified in
   * a database's configuration file.
   * @param database  an existing {@link Database} to configure
   * @param config  the configuration specifying the additional
   *   {@link ContentHandler} and {@link ResolverFactory} instances to add
   *   to the <var>database</var>
   * @throws InitializerException if a configured {@link ResolverFactory} can't
   *   be added
   * @throws SecurityAdapterFactoryException if a configured
   *   {@link SecurityAdapter} can't be created
   */
  @SuppressWarnings("unchecked")
  static void configure(Database database, MulgaraConfig config) throws InitializerException, SecurityAdapterFactoryException {
    // Retrieve the enumeration of content handler classes
    Enumeration<org.mulgara.config.ContentHandler> contentHandlers = config.enumerateContentHandler();

    // Iterate through the enumeration and add the class names to the content
    // handlers list
    while (contentHandlers.hasMoreElements()) {
      // Retrieve the next content handler class name
      org.mulgara.config.ContentHandler contentHandler = contentHandlers.nextElement();
      if (logger.isInfoEnabled()) logger.info("Loaded content handler: " + contentHandler.getType());
      // Add the next content handler class name
      database.addContentHandler(contentHandler.getType());
    }

    // Retrieve the enumeration of resolver factory classes
    Enumeration<org.mulgara.config.ResolverFactory> resolverFactories = config.enumerateResolverFactory();

    // Iterate through the enumeration and add the class names to the resolver
    // factories list
    while (resolverFactories.hasMoreElements()) {

      // Retrieve the next resolver factory class name
      org.mulgara.config.ResolverFactory resolverFactory = resolverFactories.nextElement();

      if (logger.isInfoEnabled()) logger.info("Loaded resolver factory: " + resolverFactory.getType());

      // Determine the persistence directory
      File directory = null;
      if (resolverFactory.getDir() != null && database.getRootDirectory() != null) {
        directory = new File(database.getRootDirectory(), resolverFactory.getDir());
      }

      // Add the next resolver factory class name
      database.addResolverFactory(resolverFactory.getType(), directory);
    }

    // Add the security adapters, if configured
    Enumeration<org.mulgara.config.SecurityAdapterFactory> securityAdapterFactories = config.enumerateSecurityAdapterFactory();

    while (securityAdapterFactories.hasMoreElements()) {

      org.mulgara.config.SecurityAdapterFactory configSecurityAdapterFactory = securityAdapterFactories.nextElement();

      // Instantiate the factory bean
      SecurityAdapterFactory securityAdapterFactory;
      try {
        securityAdapterFactory = (SecurityAdapterFactory)Beans.instantiate(
              database.getClass().getClassLoader(), configSecurityAdapterFactory.getType());
      } catch (Exception e) {
        throw new SecurityAdapterFactoryException("Unable to instantiate security adapter factory " + configSecurityAdapterFactory.getType(), e);
      }
      assert securityAdapterFactory != null;

      // Add the security adapter
      database.addSecurityAdapter(securityAdapterFactory);
    }

    Enumeration<org.mulgara.config.RuleLoader> ruleLoaders = config.enumerateRuleLoader();
    while (ruleLoaders.hasMoreElements()) {
      org.mulgara.config.RuleLoader ruleLoader = ruleLoaders.nextElement();

      if (logger.isInfoEnabled()) logger.info("Loaded resolver factory: " + ruleLoader.getClass());
      database.addRuleLoader(ruleLoader.getType());
    }
  }
}
