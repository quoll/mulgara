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
 * Contributor(s):
 *   SymbolicTransformation refactor contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *   External XAResource contributed by Netymon Pty Ltd on behalf of Topaz
 *   Foundation under contract.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Java 2 standard packages
import java.io.*;
import java.net.URI;
import java.util.*;

// Java 2 enterprise packages
import javax.activation.MimeType;
import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.*;

// Local packages
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.query.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.rules.*;
import org.mulgara.server.Session;
import org.mulgara.transaction.TransactionManagerFactory;
import org.mulgara.util.StackTrace;

/**
 * A database session.
 *
 * @created 2004-04-26
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class DatabaseSession implements Session {
  public static final boolean ASSERT_STATEMENTS = true;
  public static final boolean DENY_STATEMENTS = false;

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(DatabaseSession.class.getName());

  /**
   * Resolver factories that should be have access to their models cached.
   * This field is read-only.
   */
  private final Set<ResolverFactory> cachedResolverFactorySet;

  /**
   * The list of all registered {@link ResolverFactory} instances.
   * Not used in this implementation.
   */
  private final List<ResolverFactory> resolverFactoryList;

  /**
   * Map from URL protocol {@link String}s to the {@link ResolverFactory} which
   * handles external models using that protocol.
   */
  private final Map<String,ResolverFactory> externalResolverFactoryMap;

  /**
   * Map from modelType {@link LocalNode}s to the {@link ResolverFactory} which
   * handles that model type.
   */
  private final Map<URI,InternalResolverFactory> internalResolverFactoryMap;

  private final DatabaseMetadata metadata;

  /** Security adapters this instance should enforce. */
  private final List<SecurityAdapter> securityAdapterList;

  /** Symbolic transformations this instance should apply. */
  private final List<SymbolicTransformation> symbolicTransformationList;

  /** Persistent string pool. Not used, but passed in as a parameter. */
  @SuppressWarnings("unused")
  private final ResolverSessionFactory resolverSessionFactory;

  /** Factory used to obtain the SystemResolver */
  private final SystemResolverFactory systemResolverFactory;

  /** Factory used to obtain the SystemResolver */
  private final ResolverFactory temporaryResolverFactory;

  /** Source of transactions.  */
  private final MulgaraTransactionManager transactionManager;

  private MulgaraTransactionFactory transactionFactory;
  private final MulgaraInternalTransactionFactory internalFactory;
  private final MulgaraExternalTransactionFactory externalFactory;

  /** the default maximum transaction duration */
  private final long defaultTransactionTimeout;

  /** the default maximum transaction idle time */
  private final long defaultIdleTimeout;

  /** the maximum transaction duration */
  private long transactionTimeout;

  /** the maximum transaction idle time */
  private long idleTimeout;

  /** The name of the rule loader to use */
  private List<String> ruleLoaderClassNames;

  /** The registered {@link org.mulgara.content.ContentHandler} instances.  */
  private ContentHandlerManager contentHandlers;

  /** The temporary model type-URI. */
  private final URI temporaryModelTypeURI;


  /**
   * Non-rule version of the constructor.  Accepts all parameters except ruleLoaderClassName.
   */
  DatabaseSession(MulgaraTransactionManager transactionManager,
      TransactionManagerFactory transactionManagerFactory,
      List<SecurityAdapter> securityAdapterList,
      List<SymbolicTransformation> symbolicTransformationList,
      ResolverSessionFactory resolverSessionFactory,
      SystemResolverFactory systemResolverFactory,
      ResolverFactory temporaryResolverFactory,
      List<ResolverFactory> resolverFactoryList,
      Map<String,ResolverFactory> externalResolverFactoryMap,
      Map<URI,InternalResolverFactory> internalResolverFactoryMap,
      DatabaseMetadata metadata,
      ContentHandlerManager contentHandlers,
      Set<ResolverFactory> cachedResolverFactorySet,
      URI temporaryModelTypeURI) throws ResolverFactoryException {
    this(transactionManager, transactionManagerFactory, securityAdapterList, symbolicTransformationList, resolverSessionFactory,
      systemResolverFactory, temporaryResolverFactory, resolverFactoryList, externalResolverFactoryMap,
      internalResolverFactoryMap, metadata, contentHandlers, cachedResolverFactorySet,
      temporaryModelTypeURI, 0, 0, null);
  }


  /**
   * Construct a database session.
   *
   * @param transactionManager  the source of transactions for this session,
   *   never <code>null</code>
   * @param transactionManagerFactory  factory for internal jta transaction-manager
   *   for this session, never <code>null</code>
   * @param securityAdapterList  {@link List} of {@link SecurityAdapter}s to be
   *   consulted before permitting operations, never <code>null</code>
   * @param symbolicTransformationList  {@link List} of
   *   {@link SymbolicTransformation}s, never <code>null</code>
   * @param resolverSessionFactory  source of {@link ResolverSessionFactory}s,
   *   never <code>null</code>
   * @param systemResolverFactory  Source of {@link SystemResolver}s to manage
   *   persistent models, for instance the system model (<code>#</code>); never
   *   <code>null</code>
   * @param temporaryResolverFactory  Source of {@link Resolver}s to manage
   *   models which only last the duration of a transaction, for instance the
   *   contents of external RDF/XML documents; never <code>null</code>
   * @param resolverFactoryList  the list of registered {@link ResolverFactory}
   *   instances to use for constraint resolution, never <code>null</code>
   * @param externalResolverFactoryMap  map from URL protocol {@link String}s
   *   to {@link ResolverFactory} instances for models accessed via that
   *   protocol, never <code>null</code>
   * @param internalResolverFactoryMap  map from model type {@link LocalNode}s
   *   to {@link ResolverFactory} instances for that model type, never
   *   <code>null</code>
   * @param metadata  even more parameters from the parent {@link Database},
   *   never <code>null</code>
   * @param contentHandlers contains the list of valid registered content handles
   *   never <code>null</code>
   * @param temporaryModelTypeURI  the URI of the model type to use to cache
   *   external models
   * @param transactionTimeout  the default number of milli-seconds before transactions
   *   time out, or zero to take the <var>transactionManagerFactory</var>'s default;
   *   never negative
   * @param idleTimeout  the default number of milli-seconds a transaction may be idle
   *   before it is timed out, or zero to take the <var>transactionManagerFactory</var>'s
   *   default; never negative
   * @param ruleLoaderClassNames  the rule-loader classes to use; may be null 
   * @throws IllegalArgumentException if any argument is <code>null</code>
   */
  DatabaseSession(MulgaraTransactionManager transactionManager,
      TransactionManagerFactory transactionManagerFactory,
      List<SecurityAdapter> securityAdapterList,
      List<SymbolicTransformation> symbolicTransformationList,
      ResolverSessionFactory resolverSessionFactory,
      SystemResolverFactory systemResolverFactory,
      ResolverFactory temporaryResolverFactory,
      List<ResolverFactory> resolverFactoryList,
      Map<String,ResolverFactory> externalResolverFactoryMap,
      Map<URI,InternalResolverFactory> internalResolverFactoryMap,
      DatabaseMetadata metadata,
      ContentHandlerManager contentHandlers,
      Set<ResolverFactory> cachedResolverFactorySet,
      URI temporaryModelTypeURI,
      long transactionTimeout,
      long idleTimeout,
      List<String> ruleLoaderClassNames) throws ResolverFactoryException {

    if (logger.isDebugEnabled()) {
      logger.debug("Constructing DatabaseSession: externalResolverFactoryMap=" +
          externalResolverFactoryMap + " internalResolverFactoryMap=" +
          internalResolverFactoryMap + " metadata=" + metadata);
    }

    // Validate parameters
    if (transactionManager == null) {
      throw new IllegalArgumentException("Null 'transactionManager' parameter");
    } else if (transactionManagerFactory == null) {
      throw new IllegalArgumentException("Null 'transactionManagerFactory' parameter");
    } else if (securityAdapterList == null) {
      throw new IllegalArgumentException("Null 'securityAdapterList' parameter");
    } else if (symbolicTransformationList == null) {
      throw new IllegalArgumentException("Null 'symbolicTransformationList' parameter");
    } else if (resolverSessionFactory == null) {
      throw new IllegalArgumentException("Null 'resolverSessionFactory' parameter");
    } else if (systemResolverFactory == null) {
      throw new IllegalArgumentException("Null 'systemResolverFactory' parameter");
    } else if (temporaryResolverFactory == null) {
      throw new IllegalArgumentException("Null 'temporaryResolverFactory' parameter");
    } else if (resolverFactoryList == null) {
      throw new IllegalArgumentException("Null 'resolverFactoryList' parameter");
    } else if (externalResolverFactoryMap == null) {
      throw new IllegalArgumentException("Null 'externalResolverFactoryMap' parameter");
    } else if (internalResolverFactoryMap == null) {
      throw new IllegalArgumentException("Null 'internalResolverFactoryMap' parameter");
    } else if (contentHandlers == null) {
      throw new IllegalArgumentException("Null 'contentHandlers' parameter");
    } else if (metadata == null) {
      throw new IllegalArgumentException("Null 'metadata' parameter");
    } else if (cachedResolverFactorySet == null) {
      throw new IllegalArgumentException("Null 'cachedResolverFactorySet' parameter");
    } else if (temporaryModelTypeURI == null) {
      throw new IllegalArgumentException("Null 'temporaryModelTypeURI' parameter");
    } else if (transactionTimeout < 0) {
      throw new IllegalArgumentException("negative 'transactionTimeout' parameter");
    } else if (idleTimeout < 0) {
      throw new IllegalArgumentException("negative 'idleTimeout' parameter");
    } else if (ruleLoaderClassNames == null) {
      ruleLoaderClassNames = Collections.emptyList();
    }

    // Initialize fields
    this.transactionManager = transactionManager;
    this.securityAdapterList = securityAdapterList;
    this.symbolicTransformationList = symbolicTransformationList;
    this.resolverSessionFactory = resolverSessionFactory;
    this.systemResolverFactory = systemResolverFactory;
    this.temporaryResolverFactory = temporaryResolverFactory;
    this.resolverFactoryList = resolverFactoryList;
    this.externalResolverFactoryMap = externalResolverFactoryMap;
    this.internalResolverFactoryMap = internalResolverFactoryMap;
    this.metadata                   = metadata;
    this.contentHandlers            = contentHandlers;
    this.cachedResolverFactorySet   = cachedResolverFactorySet;
    this.temporaryModelTypeURI      = temporaryModelTypeURI;
    this.defaultTransactionTimeout  = transactionTimeout;
    this.defaultIdleTimeout         = idleTimeout;
    this.ruleLoaderClassNames       = ruleLoaderClassNames;

    this.transactionFactory = null;
    this.externalFactory = new MulgaraExternalTransactionFactory(this, transactionManager);
    this.internalFactory =
        new MulgaraInternalTransactionFactory(this, transactionManager, transactionManagerFactory);

    this.transactionTimeout  = defaultTransactionTimeout;
    this.idleTimeout         = defaultIdleTimeout;

    if (logger.isTraceEnabled()) logger.trace("Constructed DatabaseSession");
  }


  //
  // Internal methods required for database initialisation.
  //

  /**
   * Used by Database *only* to bootstrap the system model on DB startup.
   */
  long bootstrapSystemModel(DatabaseMetadataImpl metadata) throws QueryException {
    logger.debug("Bootstrapping System Graph");
    BootstrapOperation operation = new BootstrapOperation(metadata);
    execute(operation, "Failed to bootstrap system-model");
    systemResolverFactory.setDatabaseMetadata(metadata);
    return operation.getResult();
  }


  /**
   * Preallocate a local node number for an RDF {@link Node}.
   *
   * This method is used only by {@link DatabaseResolverFactoryInitializer}
   * and {@link DatabaseSecurityAdapterInitializer}.
   *
   * @param node  an RDF node
   * @return the preallocated local node number corresponding to the
   *   <var>node</var>, never {@link org.mulgara.store.nodepool.NodePool#NONE}
   * @throws QueryException if the local node number can't be obtained
   */
  long preallocate(Node node) throws QueryException {
    PreallocateOperation preOp = new PreallocateOperation(node);
    execute(preOp, "Failure to preallocated " + node);
    return preOp.getResult();
  }


  //
  // Methods implementing Session
  //

  public void insert(URI modelURI, Set<? extends Triple> statements) throws QueryException {
    modify(modelURI, statements, ASSERT_STATEMENTS);
  }


  public void insert(URI modelURI, Query query) throws QueryException {
    modify(modelURI, query, ASSERT_STATEMENTS);
  }


  public void delete(URI modelURI, Set<? extends Triple> statements) throws QueryException {
    modify(modelURI, statements, DENY_STATEMENTS);
  }


  public void delete(URI modelURI, Query query) throws QueryException {
    modify(modelURI, query, DENY_STATEMENTS);
  }


  /**
   * Backup all the data on the server. The database is not changed by this method.
   * @param destinationURI The URI of the file to backup into.
   * @throws QueryException if the backup cannot be completed.
   */
  public void backup(URI destinationURI) throws QueryException {
    this.backup(null, destinationURI);
  }


  /**
   * Backup all the data on the specified server to an output stream.
   * The database is not changed by this method.
   * @param outputStream The stream to receive the contents
   * @throws QueryException if the backup cannot be completed.
   */
  public void backup(OutputStream outputStream) throws QueryException {
    this.backup(outputStream, null);
  }
  
  
  /**
   * Export the data in the specified graph. The database is not changed by this method.
   * @param graphURI The URI of the graph to export.
   * @param destinationURI The URI of the file to export into.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, URI destinationURI) throws QueryException {
    this.export(null, graphURI, destinationURI, null, null);
  }
  
  
  /**
   * Export the data in the specified graph using pre-defined namespace prefixes.
   * The database is not changed by this method.
   * @param graphURI The URI of the graph to export.
   * @param destinationURI The URI of the file to export into.
   * @param prefixes An optional mapping for pre-populating the RDF/XML namespace prefixes.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, URI destinationURI, Map<String,URI> prefixes) throws QueryException {
    this.export(null, graphURI, destinationURI, prefixes, null);
  }
  
  
  /**
   * Export the data in the specified graph to an output stream.
   * The database is not changed by this method.
   * @param graphURI The URI of the server or model to export.
   * @param outputStream The stream to receive the contents
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, OutputStream outputStream, MimeType contentType) throws QueryException {
    this.export(outputStream, graphURI, null, null, contentType);
  }


  /**
   * Export the data in the specified graph to an output stream using pre-defined namespace prefixes.
   * The database is not changed by this method.
   * @param graphURI The URI of the server or model to export.
   * @param outputStream The stream to receive the contents
   * @param prefixes An optional mapping for pre-populating the RDF/XML namespace prefixes.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, OutputStream outputStream, Map<String,URI> prefixes, MimeType contentType) throws QueryException {
    this.export(outputStream, graphURI, null, prefixes, contentType);
  }


  /**
   * Restore all the data on the server. If the database is not
   * currently empty then the current contents of the database will be replaced
   * with the content of the backup file when this method returns.
   * @param sourceURI The URI of the backup file to restore from.
   * @throws QueryException if the restore cannot be completed.
   */
  public void restore(URI sourceURI) throws QueryException {
    this.restore(null, sourceURI);
  }


  /**
   * Restore all the data on the server. If the database is not
   * currently empty then the current contents of the database will be replaced
   * with the content of the backup file when this method returns.
   * @param inputStream a client supplied inputStream to obtain the restore
   *        content from. If null assume the sourceURI has been supplied.
   * @param sourceURI The URI of the backup file to restore from.
   * @throws QueryException if the restore cannot be completed.
   */
  public void restore(InputStream inputStream, URI sourceURI) throws QueryException {
    execute(new RestoreOperation(inputStream, sourceURI), "Unable to restore from " + sourceURI);
    for (ResolverFactory resFactory: resolverFactoryList) {
      createDefaultGraphs(resFactory.getDefaultGraphs());
    }
  }


  /**
   * Execute a SELECT query on the database.
   * @see org.mulgara.server.Session#query(org.mulgara.query.Query)
   */
  public Answer query(Query query) throws QueryException {
    if (logger.isDebugEnabled()) logger.debug("QUERY: " + query);

    QueryOperation queryOperation = new QueryOperation(query, this);
    execute(queryOperation, "Query failed");
    return queryOperation.getAnswer();
  }


  /**
   * Tests the database to see if a query will return any data.
   * @see org.mulgara.server.Session#query(org.mulgara.query.AskQuery)
   */
  public boolean query(AskQuery query) throws QueryException {
    if (logger.isDebugEnabled()) logger.debug("ASK QUERY: " + query);

    QueryOperation queryOperation = new QueryOperation(query, this);
    execute(queryOperation, "Query failed");
    return ((BooleanAnswer)queryOperation.getAnswer()).getResult();
  }


  /**
   * Queries the database for data that will be structured as a graph.
   * @see org.mulgara.server.Session#query(org.mulgara.query.ConstructQuery)
   */
  public GraphAnswer query(ConstructQuery query) throws QueryException {
    if (logger.isDebugEnabled()) logger.debug("CONSTRUCT QUERY: " + query);

    QueryOperation queryOperation = new QueryOperation(query, this);
    execute(queryOperation, "Query failed");
    return (GraphAnswer)queryOperation.getAnswer();
  }


  public List<Answer> query(List<Query> queryList) throws QueryException {
    if (logger.isDebugEnabled()) {
      StringBuffer log = new StringBuffer("QUERYING LIST: ");
      for (int i = 0; i < queryList.size(); i++) log.append(queryList.get(i));
      logger.debug(log.toString());
    }

    QueryOperation queryOperation = new QueryOperation(queryList, this);
    execute(queryOperation, "Failed list query");
    return queryOperation.getAnswerList();
  }



  public void createModel(URI modelURI, URI modelTypeURI) throws QueryException {
    if (logger.isDebugEnabled()) logger.debug("Creating Graph " + modelURI + " with type " + modelTypeURI);

    execute(new CreateGraphOperation(modelURI, modelTypeURI),
            "Could not commit creation of model " + modelURI + " of type " + modelTypeURI);
  }


  public boolean createDefaultGraph(URI modelURI, URI modelTypeURI) throws QueryException {
    if (logger.isDebugEnabled()) logger.debug("Creating Graph " + modelURI + " with type " + modelTypeURI + " in the system graph");

    CreateDefaultGraphOperation op = new CreateDefaultGraphOperation(modelURI, modelTypeURI);
    execute(op, "Could not commit creation of model " + modelURI + " of type " + modelTypeURI);
    return op.getResult();
  }


  public void removeModel(URI modelURI) throws QueryException {
    if (logger.isDebugEnabled()) logger.debug("REMOVE MODEL: " + modelURI);
    if (modelURI == null) throw new IllegalArgumentException("Null 'modelURI' parameter");

    execute(new RemoveGraphOperation(modelURI), "Unable to remove " + modelURI);
  }


  public boolean modelExists(URI modelURI) throws QueryException {
    GraphExistsOperation operation = new GraphExistsOperation(modelURI);
    execute(operation, "Failed to determine model existence");
    return operation.getResult();
  }


  /**
   * Define the contents of a model.
   * @param uri the {@link URI} of the model to be redefined
   * @param sourceUri the new content for the model
   * @return RETURNED VALUE TO DO
   * @throws QueryException if the model can't be modified
   */
  public synchronized long setModel(URI uri, URI sourceUri) throws QueryException {
    return this.setModel(null, uri, sourceUri, null);
  }


  /**
   * Define the contents of a model via an inputstream
   * @param inputStream a remote inputstream
   * @param destinationUri the {@link URI} of the graph to be redefined
   * @param sourceUri the new content for the graph
   * @return The number of statements loaded into the graph
   * @throws QueryException if the model can't be modified
   */
  public synchronized long setModel(InputStream inputStream,
      URI destinationUri, URI sourceUri, MimeType contentType) throws QueryException {
    if (logger.isDebugEnabled()) {
      logger.debug("SET-MODEL " + destinationUri + " to " + sourceUri + " from " + inputStream);
    }

    // Validate parameters
    if (destinationUri == null) {
      throw new IllegalArgumentException("Must provide a destination graph URI.");
    }
    if (sourceUri == null && (contentType == null || inputStream == null)) {
      throw new IllegalArgumentException("Must provide either a source URI or a source input stream/content type.");
    }

    assert sourceUri != null || contentType != null;

    // Perform the operation
    SetGraphOperation op = new SetGraphOperation(sourceUri, destinationUri,
                                  inputStream, contentType, contentHandlers);
    execute(op, "Unable to load " + sourceUri + " into " + destinationUri);

    return op.getStatementCount();
  }


  /**
   * {@inheritDoc}
   */
  public RulesRef buildRules(URI ruleModel, GraphExpression baseModel, URI destModel) throws QueryException, org.mulgara.rules.InitializerException {
    if (logger.isDebugEnabled()) logger.debug("BUILD RULES: " + ruleModel);

    BuildRulesOperation operation = new BuildRulesOperation(ruleLoaderClassNames, ruleModel, baseModel, destModel);
    execute(operation, "Failed to create rules");
    return operation.getResult();
  }


  /**
   * {@inheritDoc}
   */
  public void applyRules(RulesRef rulesRef) throws QueryException {
    execute(new ApplyRulesOperation(rulesRef), "Unable to apply rules");
  }


  public void setAutoCommit(boolean autoCommit) throws QueryException {
    if (logger.isDebugEnabled()) logger.debug("setAutoCommit(" + autoCommit + ") called.");
    assertInternallyManagedXA();
    try {
      internalFactory.setAutoCommit(autoCommit);
    } catch (MulgaraTransactionException em) {
      throw new QueryException("Error setting autocommit", em);
    }
  }


  public void commit() throws QueryException {
    logger.debug("Committing transaction");
    assertInternallyManagedXA();
    try {
      internalFactory.commit();
    } catch (MulgaraTransactionException em) {
      throw new QueryException("Error performing commit", em);
    }
  }


  public void rollback() throws QueryException {
    logger.debug("Rollback transaction");
    assertInternallyManagedXA();
    try {
      internalFactory.rollback();
    } catch (MulgaraTransactionException em) {
      throw new QueryException("Error performing rollback", em);
    }
  }


  public void close() throws QueryException {
    logger.debug("Closing session");
    try {
      if (transactionFactory != null)
        transactionFactory.closingSession();
    } catch (MulgaraTransactionException em) {
      logger.error("Error force-closing session", em);
      throw new QueryException("Error force-closing session.", em);
    } finally {
      try {
        transactionManager.closingSession(this);
      } catch (MulgaraTransactionException em2) {
        logger.error("Error force-closing session", em2);
        throw new QueryException("Error force-closing session.", em2);
      }
    }
  }


  public boolean isLocal() {
    return true;
  }


  public void login(URI securityDomain, String user, char[] password) {
    if (logger.isTraceEnabled()) logger.trace("Login of " + user + " to " + securityDomain);

    if (securityDomain.equals(metadata.getSecurityDomainURI())) {
      // Propagate the login event to the security adapters
      for (SecurityAdapter adapter: securityAdapterList) {
        adapter.login(user, password);
      }
    }
  }


  /**
   * Backup all the data on the specified server to a URI or an output stream.
   * The database is not changed by this method.
   *
   * If an outputstream is supplied then the destinationURI is ignored.
   *
   * @param outputStream Optional output stream to receive the contents
   * @param destinationURI Option URI of the file to backup into.
   * @throws QueryException if the backup cannot be completed.
   */
  private synchronized void backup(OutputStream outputStream, URI destinationURI)
        throws QueryException {
    execute(new BackupOperation(outputStream, destinationURI),
        "Unable to backup to " + destinationURI);
  }
  
  /**
   * Export the data on the specified graph to a URI or an output stream.
   * The database is not changed by this method.
   *
   * If an outputstream is supplied then the destinationURI is ignored.
   *
   * @param outputStream Optional output stream to receive the contents
   * @param graphURI The URI of the graph to export.
   * @param destinationURI Optional URI of the file to export into.
   * @param initialPrefixes An optional set of user-supplied namespace prefix mappings;
   *   may be <code>null</code> to use the generated namespace prefixes.
   * @param contentType TODO
   * @throws QueryException if the export cannot be completed.
   */
  private synchronized void export(OutputStream outputStream, URI graphURI, URI destinationURI,
        Map<String,URI> initialPrefixes, MimeType contentType) throws QueryException {
    execute(new ExportOperation(outputStream, graphURI, destinationURI, initialPrefixes, contentType, contentHandlers),
        "Unable to export " + graphURI);
  }


  //
  // Internal utility methods.
  //
  protected void modify(URI modelURI, Set<? extends Triple> statements, boolean insert) throws QueryException
  {
    if (logger.isDebugEnabled()) logger.debug("Modifying (ins:" + insert + ") : " + modelURI);
    if (logger.isTraceEnabled()) logger.trace("Modifying statements: " + statements);

    execute(new ModifyGraphOperation(modelURI, statements, insert), "Could not commit modify");
  }


  private void modify(URI modelURI, Query query, boolean insert) throws QueryException {
    if (logger.isDebugEnabled()) {
      logger.debug((insert ? "INSERT" : "DELETE") + " QUERY: " + query + " into " + modelURI);
    }

    execute(new ModifyGraphOperation(modelURI, query, insert, this), "Unable to modify " + modelURI);
  }


  /**
   * Execute an {@link Operation}.
   *
   * @param operation  the {@link Operation} to execute
   * @throws QueryException if the <var>operation</var> fails
   */
  private void execute(Operation operation, String errorString) throws QueryException {
    ensureTransactionFactorySelected();
    try {
      MulgaraTransaction transaction = transactionFactory.getTransaction(operation.isWriteOperation());
      transaction.execute(operation, metadata);
    } catch (MulgaraTransactionException em) {
      if (logger.isDebugEnabled()) logger.debug("Error executing operation: " + errorString, em);
      throw new QueryException(errorString + ": " + StackTrace.getReasonMessage(em), em);
    }
  }


  public DatabaseOperationContext newOperationContext(boolean writing) throws QueryException {
    return new DatabaseOperationContext(
        cachedResolverFactorySet,
        externalResolverFactoryMap,
        internalResolverFactoryMap,
        metadata,
        securityAdapterList,
        temporaryModelTypeURI,
        temporaryResolverFactory,
        symbolicTransformationList,
        systemResolverFactory,
        writing);
  }

  /**
   * Creates a series of default graphs for a resolver.
   * @param graphs An array of the graph names and types to create. May be null.
   * @return <code>true</code> if any graphs were created, <code>false</code> otherwise.
   * @throws QueryException If it was not possible to detect if the graph already existed.
   */
  boolean createDefaultGraphs(ResolverFactory.Graph[] graphs) throws QueryException {
    boolean result = false;
    if (graphs != null) {
      for (ResolverFactory.Graph graph: graphs) {
        result = result || createDefaultGraph(graph.getGraph(), graph.getType());
      }
    }
    return result;
  }


  private void ensureTransactionFactorySelected() throws QueryException {
    if (transactionFactory == null) assertInternallyManagedXA();
  }

  private void assertInternallyManagedXA() throws QueryException {
    if (transactionFactory == null) {
      transactionFactory = internalFactory;
    } else if (transactionFactory != internalFactory) {
      throw new QueryException("Attempt to use internal transaction control in externally managed session");
    }
  }


  private void assertExternallyManagedXA() throws QueryException {
    if (transactionFactory == null) {
      transactionFactory = externalFactory;
    } else if (transactionFactory != externalFactory) {
      throw new QueryException("Attempt to use external transaction control in internally managed session");
    }
  }


  public XAResource getXAResource() throws QueryException {
    assertExternallyManagedXA();
    return externalFactory.getXAResource(true);
  }


  public XAResource getReadOnlyXAResource() throws QueryException {
    assertExternallyManagedXA();
    return externalFactory.getXAResource(false);
  }

  public void setIdleTimeout(long millis) {
    idleTimeout = millis > 0 ? millis : defaultIdleTimeout;
  }

  public void setTransactionTimeout(long millis) {
    transactionTimeout = millis > 0 ? millis : defaultTransactionTimeout;
  }

  public long getIdleTimeout() {
    return idleTimeout;
  }

  public long getTransactionTimeout() {
    return transactionTimeout;
  }

  public boolean ping() {
    return true;
  }
}
