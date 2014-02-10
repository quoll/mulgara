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

package org.mulgara.resolver.store;

// Java 2 standard packages
import java.io.File;
import java.io.IOException;
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.statement.StatementStoreException;
import org.mulgara.store.statement.xa.XAStatementStoreImpl;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.store.xa.XAResolverSession;
import org.mulgara.store.xa.XAResolverSessionFactory;
import org.mulgara.store.xa.XAStatementStore;

/**
 * Resolves constraints from the statement store.
 *
 * @created 2004-03-29
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/02/22 08:16:34 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class StatementStoreResolverFactory implements SystemResolverFactory {
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(StatementStoreResolverFactory.class.getName());

  // Generate the XA store model type URI
  private static final URI modelTypeURI = URI.create(Mulgara.NAMESPACE + "Model");

  /** The system model.  */
  private long systemModel;
  private long rdfType;

  /** The underlying transactional graph that backs the generated resolvers.  */
  protected final XAStatementStore statementStore;

  protected XAResolverSessionFactory resolverSessionFactory;

  //
  // Constructors
  //

  /**
   * Construct a resolver factory based on the statement store.
   *
   * @param initializer  {@inheritDoc}
   * @throws IllegalArgumentException {@inheritDoc}
   * @throws ResolverException {@inheritDoc}
   */
  protected StatementStoreResolverFactory(FactoryInitializer initializer,
      XAResolverSessionFactory resolverSessionFactory) throws
      InitializerException {
    // Validate parameters
    if (initializer == null) {
      throw new IllegalArgumentException("Null 'initializer' parameter");
    }

    try {
      File filePrefix = new File(initializer.getDirectory(), "xa");
      statementStore = createStore(filePrefix.toString());
      resolverSessionFactory.registerStatementStore(statementStore);
    } catch (Exception e) {
      throw new InitializerException("Couldn't initialize XA store", e);
    }

    this.resolverSessionFactory = resolverSessionFactory;
  }

  public void initialize() {
    logger.fatal("Error called initialize!");
    throw new IllegalStateException("Initialize deprecated");
  }

  //
  // Methods implementing SystemResolverFactory (excluding newResolver)
  //

  public URI getSystemModelTypeURI() {
    return modelTypeURI;
  }

  public void setDatabaseMetadata(DatabaseMetadata metadata) {
    rdfType = metadata.getRdfTypeNode();
    systemModel = metadata.getSystemModelNode();
  }

  //
  // Methods implementing ResolverFactory (excluding newResolver)
  //

  /**
   * {@inheritDoc}
   * @return <code>null</code> - no default graphs for this resolver
   */
  public Graph[] getDefaultGraphs() { return null; }
  
  /**
   * {@inheritDoc}
   * @return <code>true</code> - this graph supports exports.
   */
  public boolean supportsExport() {
    return true;
  }

  public void close() throws ResolverFactoryException {
    try {
      statementStore.close();
    } catch (StatementStoreException e) {
      throw new ResolverFactoryException("Unable to close", e);
    }
  }

  public void delete() throws ResolverFactoryException {
    try {
      statementStore.delete();
    } catch (StatementStoreException e) {
      throw new ResolverFactoryException("Unable to delete", e);
    }
  }

  /**
   * Register this resolver upon database startup.
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer
      initializer) throws InitializerException {
    throw new InitializerException("Independent StatementStore not supported");
  }

  public static ResolverFactory newInstance(FactoryInitializer initializer,
      XAResolverSessionFactory resolverSessionFactory) throws
      InitializerException {
    return new StatementStoreResolverFactory(initializer,
        resolverSessionFactory);
  }

  public int[] recover() throws SimpleXAResourceException {
    return statementStore.recover();
  }

  public void selectPhase(int phaseNumber) throws IOException,
      SimpleXAResourceException {
    statementStore.selectPhase(phaseNumber);
  }

  public void clear() throws IOException, SimpleXAResourceException {
    statementStore.clear();
  }

  public void clear(int phaseNumber) throws IOException,
      SimpleXAResourceException {
    statementStore.clear(phaseNumber);
  }

  //
  // newResolver methods.
  //

  /**
   * Obtain a statement store resolver.
   */
  public Resolver newResolver(boolean allowWrites, ResolverSession session,
      Resolver systemResolver) throws ResolverFactoryException {
    try {
      return new StatementStoreResolver(systemResolver,
          rdfType,
          systemModel,
          modelTypeURI,
          allowWrites
              ? (XAResolverSession) resolverSessionFactory.newWritableResolverSession()
              : (XAResolverSession) resolverSessionFactory.newReadOnlyResolverSession(),
          allowWrites
              ? statementStore.newWritableStatementStore()
              : statementStore.newReadOnlyStatementStore(),
          this);
    } catch (ResolverSessionFactoryException er) {
      throw new ResolverFactoryException(
          "Failed to obtain a new ResolverSession", er);
    }
  }

  /**
   * Obtain a statement store resolver.
   */
  public SystemResolver newResolver(boolean allowWrites) throws
      ResolverFactoryException {
    try {
      return new StatementStoreResolver(rdfType,
          systemModel,
          modelTypeURI,
          allowWrites ?
          (XAResolverSession) resolverSessionFactory.newWritableResolverSession()
          :
          (XAResolverSession) resolverSessionFactory.newReadOnlyResolverSession(),
          allowWrites ? statementStore.newWritableStatementStore()
          : statementStore.newReadOnlyStatementStore(),
          this);
    } catch (ResolverSessionFactoryException er) {
      throw new ResolverFactoryException(
          "Failed to obtain a new ResolverSession", er);
    }
  }


  /**
   * Creates the required type of store
   * @param filePrefix The base for the files being used for storage.
   * @return a new instance of an XAStatementStore
   * @throws IOException Error accessing the filesystem
   */
  protected XAStatementStore createStore(String filePrefix) throws IOException {
    return new XAStatementStoreImpl(filePrefix.toString());
  }
}
