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
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.lang.reflect.Method;

// Third party packages
import org.apache.log4j.Logger;
import gnu.trove.TIntHashSet;

// Local packages
import org.mulgara.resolver.spi.*;
import org.mulgara.store.StoreException;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.nodepool.NodePoolFactory;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.stringpool.StringPoolFactory;
import org.mulgara.store.xa.SimpleXARecoveryHandler;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.store.xa.XANodePool;
import org.mulgara.store.xa.XAResolverSessionFactory;
import org.mulgara.store.xa.XAStatementStore;
import org.mulgara.store.xa.XAStringPool;

/**
 * A database session.
 *
 * @created 2004-04-26
 * @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/02/22 08:16:10 $ by $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class StringPoolSessionFactory implements XAResolverSessionFactory, SimpleXARecoveryHandler
{
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(StringPoolSessionFactory.class.getName());

  /** The unique {@link URI} naming this database. */
  private final URI databaseURI;

  /** The set of alternative hostnames for the current host. */
  private final Set<String> hostnameAliases;

  /** Where to store literals for this session.  */
  private final XAStringPool persistentStringPool;

  /** The source of nodes for this session.  */
  private final XANodePool persistentNodePool;

  /** The source of nodes which won't outlive this session.  */
  private final NodePoolFactory temporaryNodePoolFactory;

  /** Where to store literals which won't outlive this session.  */
  private final StringPoolFactory temporaryStringPoolFactory;

  //
  // Constructor
  //

  StringPoolSessionFactory(
    URI                        databaseURI,
    Set<String>                        hostnameAliases,
    String                     persistentStringPoolFactoryClassName,
    FactoryInitializer         persistentStringPoolFactoryInitializer,
    String                     persistentNodePoolFactoryClassName,
    FactoryInitializer         persistentNodePoolFactoryInitializer,
    String                     temporaryStringPoolFactoryClassName,
    FactoryInitializer         temporaryStringPoolFactoryInitializer,
    String                     temporaryNodePoolFactoryClassName,
    FactoryInitializer         temporaryNodePoolFactoryInitializer
  )
    throws ResolverFactoryException, NodePoolException, StringPoolException,
           InitializerException
  {
    this.databaseURI     = databaseURI;
    this.hostnameAliases = hostnameAliases;

    // Create the persistent node pool
    XANodePool nodePool = null;
    try {
      NodePoolFactory factory =
        (NodePoolFactory) newFactory(persistentNodePoolFactoryInitializer,
                                     persistentNodePoolFactoryClassName,
                                     NodePoolFactory.class);
      nodePool = (XANodePool)factory.newNodePool();
    } catch (ClassCastException ec) {
      logger.error("Error obtaining XANodePool. (" + persistentNodePoolFactoryClassName + ")");
      throw new InitializerException("Persistent Node-pool non-transactional", ec);
    }

    try {
      // Create the persistent string pool
      StringPoolFactory factory =
        (StringPoolFactory) newFactory(persistentStringPoolFactoryInitializer,
                                       persistentStringPoolFactoryClassName,
                                       StringPoolFactory.class);
      persistentStringPool = (XAStringPool)factory.newStringPool();
      assert persistentStringPool!= null;
    } catch (ClassCastException ec) {
      logger.error("Error obtaining XAStringPool. (" + persistentStringPoolFactoryClassName + ")");
      throw new InitializerException("Persistent String-pool non-transactional", ec);
    }

    if (nodePool == null) {
      if (!(persistentStringPool instanceof XANodePool)) {
        throw new IllegalArgumentException("Can only use a node pool from " + persistentNodePoolFactoryClassName + " if string pool from " + persistentStringPoolFactoryClassName + " is also a node pool.");
      }
      nodePool = (XANodePool)persistentStringPool;
    }
    // now we know the node pool, we can set the final field
    persistentNodePool = nodePool;
    persistentNodePool.addNewNodeListener(persistentStringPool);

    // Create the factory used to create node pools for temporary resources
    // within sessions
    temporaryNodePoolFactory =
      (NodePoolFactory) newFactory(temporaryNodePoolFactoryInitializer,
                                   temporaryNodePoolFactoryClassName,
                                   NodePoolFactory.class);
    assert temporaryNodePoolFactory != null;

    // Create the factory used to create string pools for temporary resources
    // within sessions
    temporaryStringPoolFactory =
      (StringPoolFactory) newFactory(temporaryStringPoolFactoryInitializer,
                                     temporaryStringPoolFactoryClassName,
                                     StringPoolFactory.class);
    assert temporaryStringPoolFactory != null;
  }


  public XAStringPool getPersistentStringPool()
  {
    return persistentStringPool;
  }

  public XANodePool getPersistentNodePool()
  {
    return persistentNodePool;
  }

  StringPoolFactory getTemporaryStringPoolFactory()
  {
    return temporaryStringPoolFactory;
  }

  NodePoolFactory getTemporaryNodePoolFactory()
  {
    return temporaryNodePoolFactory;
  }

  Object getGlobalLock() {

    return persistentStringPool;
  }

  public ResolverSession newReadOnlyResolverSession()
      throws ResolverSessionFactoryException {
    try {
      logger.debug("Obtaining new RO XAResolverSession on SP/NP : " + persistentStringPool.getClass());
      return new StringPoolSession(databaseURI,
                                   hostnameAliases,
                                   persistentStringPool.newReadOnlyStringPool(),
                                   persistentNodePool.newReadOnlyNodePool(),
                                   temporaryStringPoolFactory.newStringPool(),
                                   temporaryNodePoolFactory.newNodePool(),
                                   persistentStringPool);
    } catch (StringPoolException es) {
      throw new ResolverSessionFactoryException("Unable to obtain new StringPool", es);
    } catch (NodePoolException en) {
      throw new ResolverSessionFactoryException("Unable to obtain new NodePool", en);
    }
  }

  public ResolverSession newWritableResolverSession()
      throws ResolverSessionFactoryException {
    try {
      logger.debug("Obtaining new RW XAResolverSession on SP/NP : " + persistentStringPool.getClass());
      return new StringPoolSession(databaseURI,
                                   hostnameAliases,
                                   persistentStringPool.newWritableStringPool(),
                                   persistentNodePool.newWritableNodePool(),
                                   temporaryStringPoolFactory.newStringPool(),
                                   temporaryNodePoolFactory.newNodePool(),
                                   persistentStringPool);
    } catch (StringPoolException es) {
      throw new ResolverSessionFactoryException("Unable to obtain new StringPool", es);
    } catch (NodePoolException en) {
      throw new ResolverSessionFactoryException("Unable to obtain new NodePool", en);
    }
  }


  public void registerStatementStore(XAStatementStore store) {
    // Used for debugging?
//    store.setNodePool(persistentNodePool);
    // Set up callbacks between the components.
    store.addReleaseNodeListener(persistentNodePool);
    store.addReleaseNodeListener(persistentStringPool);
  }

  /**
   * Perform the reflective instantiation of a Factory.
   *
   * @param factoryClassName  the class name for an
   *   {@link StringPoolFactory} implementation
   * @return a new {@link StringPoolFactory} instance
   */
  private Object newFactory(FactoryInitializer initializer,
                            String             factoryClassName,
                            Class<?>              returnClass)
  {
    try {
      Class<?> factoryClass = Class.forName(factoryClassName);

      // Validate that the candidate supports the correct factory interface
      if (!returnClass.isAssignableFrom(factoryClass)) {
        throw new IllegalArgumentException(factoryClassName + " is not a " + returnClass.getName());
      }

      // Invoke the static Factory.newInstance method
      Method newInstanceMethod = factoryClass.getMethod("newInstance",
          new Class[] { FactoryInitializer.class });

      Object factory = newInstanceMethod.invoke(
          null,  // static method requires no object
          new Object[] { initializer } );

      return factory;
    } catch (Exception e) {
      throw new RuntimeException("Unable to create factory " + factoryClassName, e);
    }
  }


  public void close()
  {
    // Node pool
    try {
      persistentNodePool.close();
    } catch (StoreException e) {
      logger.error("Unable to close node pool", e);
    }

    // String pool
    try {
      persistentStringPool.close();
    } catch (StoreException e) {
      logger.error("Unable to close string pool", e);
    }
  }

  public void delete()
  {
    // Node pool
    try {
      persistentNodePool.delete();
    } catch (StoreException e) {
      logger.error("Unable to delete node pool", e);
    }

    // String pool
    try {
      persistentStringPool.delete();
    } catch (StoreException e) {
      logger.error("Unable to delete string pool", e);
    }
  }

  //
  // RecoveryHandler methods.
  //

  public int[] recover() throws SimpleXAResourceException
  {
    int[] nodePhases = persistentNodePool.recover();
    int[] stringPhases = persistentStringPool.recover();
    // If both are empty then return empty.
    if (nodePhases.length == 0 && stringPhases.length == 0) {
      return nodePhases;
    }
    // Else if the intersection is empty *error*,
    TIntHashSet phaseSet = new TIntHashSet(nodePhases);
    phaseSet.retainAll(stringPhases);
    if (phaseSet.isEmpty()) {
      throw new SimpleXAResourceException("No matching phases between Node and String Pools.");
    }
    //else return the intersection.
    return phaseSet.toArray();
  }


  public void selectPhase(int phaseNumber) throws IOException, SimpleXAResourceException
  {
    persistentNodePool.selectPhase(phaseNumber);
    persistentStringPool.selectPhase(phaseNumber);
  }


  public void clear() throws IOException, SimpleXAResourceException
  {
    persistentNodePool.clear();
    persistentStringPool.clear();
  }


  public void clear(int phaseNumber) throws IOException, SimpleXAResourceException
  {
    persistentNodePool.clear(phaseNumber);
    persistentStringPool.clear(phaseNumber);
  }
}
