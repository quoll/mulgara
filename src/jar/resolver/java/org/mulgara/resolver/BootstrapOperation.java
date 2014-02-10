package org.mulgara.resolver;

// Java 2 standard packages

// Java 2 enterprise packages

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.SingletonStatements;
import org.mulgara.resolver.spi.SystemResolver;

class BootstrapOperation implements Operation
{
  /** Logger */
  private static final Logger logger =
    Logger.getLogger(BootstrapOperation.class.getName());

  /**
   * The URI of the model to be created.
   */
  private final DatabaseMetadataImpl databaseMetadata;

  private long result;

  BootstrapOperation(DatabaseMetadataImpl databaseMetadata) {
    if (databaseMetadata == null) {
      throw new IllegalArgumentException("BootstrapSystemModel - databaseMetadata null ");
    }

    this.databaseMetadata = databaseMetadata;
    this.result = -1; // Return invalid node by default.
  }

  public void execute(OperationContext       operationContext,
                      SystemResolver         systemResolver,
                      DatabaseMetadata       metadata) throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Creating bootstrap nodes");
    // Find the local node identifying the model
    long graph = systemResolver.localizePersistent(
        new URIReferenceImpl(databaseMetadata.getSystemModelURI()));
    long rdfType = systemResolver.localizePersistent(
        new URIReferenceImpl(databaseMetadata.getRdfTypeURI()));
    long graphType = systemResolver.localizePersistent(
        new URIReferenceImpl(databaseMetadata.getSystemModelTypeURI()));

    // set up the system resolver to understand the system graph
    systemResolver.initializeSystemNodes(graph, rdfType, graphType);

    if (logger.isDebugEnabled()) logger.debug("Creating bootstrap statements");
    // Use the session to create the model
    systemResolver.modifyModel(graph, new SingletonStatements(graph, rdfType, graphType), true);
    databaseMetadata.initializeSystemNodes(graph, rdfType, graphType);

    // set up the default graph
    long defaultGraph = systemResolver.localizePersistent(
        new URIReferenceImpl(databaseMetadata.getDefaultGraphURI()));
    systemResolver.modifyModel(graph, new SingletonStatements(defaultGraph, rdfType, graphType), true);

    long preSubject = systemResolver.localizePersistent(
        new URIReferenceImpl(databaseMetadata.getPreallocationSubjectURI()));
    long prePredicate = systemResolver.localizePersistent(
        new URIReferenceImpl(databaseMetadata.getPreallocationPredicateURI()));
    long preModel = systemResolver.localizePersistent(
        new URIReferenceImpl(databaseMetadata.getPreallocationModelURI()));

    // Every node cached by DatabaseMetadata must be preallocated
    systemResolver.modifyModel(preModel,
        new SingletonStatements(preSubject, prePredicate, graph),
        true);
    systemResolver.modifyModel(preModel,
        new SingletonStatements(preSubject, prePredicate, rdfType),
        true);
    systemResolver.modifyModel(preModel,
        new SingletonStatements(preSubject, prePredicate, graphType),
        true);
    systemResolver.modifyModel(preModel,
        new SingletonStatements(preSubject, prePredicate, preSubject),
        true);
    systemResolver.modifyModel(preModel,
        new SingletonStatements(preSubject, prePredicate, prePredicate),
        true);
    systemResolver.modifyModel(preModel,
        new SingletonStatements(preSubject, prePredicate, preModel),
        true);

    databaseMetadata.initializePreallocationNodes(preSubject, prePredicate, preModel);

    result = graph;
  }

  public boolean isWriteOperation()
  {
    return true;
  }

  public long getResult() {
    return result;
  }
}
