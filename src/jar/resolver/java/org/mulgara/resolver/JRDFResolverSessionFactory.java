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
import java.util.Set;
import java.io.IOException;
import java.io.File;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.resolver.spi.*;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.xa.SimpleXARecoveryHandler;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.store.xa.XANodePool;
import org.mulgara.store.xa.XAStatementStore;
import org.mulgara.store.xa.XAStringPool;


/**
 * Creates Resolver Sessions that support JRDF operations.
 *
 * @created 2004-10-20
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:23 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class JRDFResolverSessionFactory implements ResolverSessionFactory,
    SimpleXARecoveryHandler

{
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(JRDFResolverSessionFactory.class.getName());

  /** The unique {@link URI} naming this database. */
  private final URI databaseURI;

  /** The set of alternative hostnames for the current host. */
  private final Set hostnameAliases;

  /** The directory used to store resources */
  private File directory = null;

  /** Wrapped ResolverSessionFactory */
  private StringPoolSessionFactory sessionFactory = null;

  /**
   * Constructor.
   *
   * @param sessionFactory StringPoolSessionFactory
   * @throws ResolverFactoryException
   * @throws NodePoolException
   * @throws StringPoolException
   * @throws InitializerException
   */
  JRDFResolverSessionFactory(DatabaseFactoryInitializer initializer,
                             StringPoolSessionFactory sessionFactory)
      throws ResolverFactoryException, NodePoolException, StringPoolException, InitializerException
  {
    this.databaseURI = initializer.getDatabaseURI();
    this.hostnameAliases = initializer.getHostnameAliases();
    this.directory = initializer.getDirectory();
    this.sessionFactory = sessionFactory;
  }

  /**
   * Creates a new Read Only ResolverSession that supports JRDF.
   *
   * @throws ResolverSessionFactoryException
   * @return ResolverSession
   */
  public ResolverSession newReadOnlyResolverSession()
      throws ResolverSessionFactoryException {
    try {
      logger.debug("Obtaining new RO XAResolverSession on SP/NP : " +
                   sessionFactory.getPersistentStringPool().getClass());
      return new JRDFResolverSession(databaseURI,
                                     hostnameAliases,
                                     sessionFactory.getPersistentStringPool().newReadOnlyStringPool(),
                                     sessionFactory.getPersistentNodePool().newReadOnlyNodePool(),
                                     sessionFactory.getTemporaryStringPoolFactory().newStringPool(),
                                     sessionFactory.getTemporaryNodePoolFactory().newNodePool(),
                                     sessionFactory.getGlobalLock());
    } catch (StringPoolException es) {
      throw new ResolverSessionFactoryException("Unable to obtain new StringPool", es);
    } catch (NodePoolException en) {
      throw new ResolverSessionFactoryException("Unable to obtain new NodePool", en);
    }
  }

  /**
   * Creates a new Read/Write ResolverSession that supports JRDF.
   *
   * @throws ResolverSessionFactoryException
   * @return ResolverSession
   */
  public ResolverSession newWritableResolverSession()
      throws ResolverSessionFactoryException {
    try {
      logger.debug("Obtaining new RW XAResolverSession on SP/NP : " +
                   sessionFactory.getPersistentStringPool().getClass());
      return new JRDFResolverSession(databaseURI,
                                     hostnameAliases,
                                     sessionFactory.getPersistentStringPool().newWritableStringPool(),
                                     sessionFactory.getPersistentNodePool().newWritableNodePool(),
                                     sessionFactory.getTemporaryStringPoolFactory().newStringPool(),
                                     sessionFactory.getTemporaryNodePoolFactory().newNodePool(),
                                     sessionFactory.getGlobalLock());
    } catch (StringPoolException es) {
      throw new ResolverSessionFactoryException("Unable to obtain new StringPool", es);
    } catch (NodePoolException en) {
      throw new ResolverSessionFactoryException("Unable to obtain new NodePool", en);
    }
  }

  /**
   * Closes the factory and frees any resources associated with it.
   */
  public void close()
  {
    sessionFactory.close();
    if (directory != null) {
      directory.delete();
    }
  }

  /**
   * Deletes resources used by the factory.
   */
  public void delete()
  {
    sessionFactory.delete();
    if (directory != null) {
      directory.delete();
    }
  }

  public void clear() throws IOException, SimpleXAResourceException
  {
    sessionFactory.clear();
  }


  public void clear(int phaseNumber) throws IOException, SimpleXAResourceException
  {
    sessionFactory.clear(phaseNumber);
  }

  /**
   * recover
   *
   * @return int[]
   * @throws SimpleXAResourceException
   */
  public int[] recover() throws SimpleXAResourceException {
    return sessionFactory.recover();
  }

  /**
   * selectPhase
   *
   * @param phaseNumber int
   * @throws SimpleXAResourceException
   * @throws IOException
   */
  public void selectPhase(int phaseNumber) throws SimpleXAResourceException,
      IOException {

    sessionFactory.selectPhase(phaseNumber);
  }

  /**
   * registerStatementStore
   *
   * @param store XAStatementStore
   */
  public void registerStatementStore(XAStatementStore store) {

    sessionFactory.registerStatementStore(store);
  }

  /**
   * getPersistentNodePool
   *
   * @return XANodePool
   */
  public XANodePool getPersistentNodePool() {
    return sessionFactory.getPersistentNodePool();
  }

  /**
   * getPersistentStringPool
   *
   * @return XAStringPool
   */
  public XAStringPool getPersistentStringPool() {
    return sessionFactory.getPersistentStringPool();
  }
}
