/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.*;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.nodepool.NodePool;

/**
 * An {@link Operation} that implements the
 * {@link org.mulgara.resolver.DatabaseSession#createDefaultGraph(URI, URI)} method.
 *
 * @created May 8, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
class CreateDefaultGraphOperation implements Operation {
  /** Logger. This is named after the class. */
  private static final Logger logger = Logger.getLogger(CreateDefaultGraphOperation.class.getName());

  /** The URI of the model to be created. */
  private final URI graphURI;

  /** The URI of the type of the model to be created. */
  private URI graphTypeURI;

  /** A flag to indicate that a graph was created by this operation. */
  private boolean created = false;

  /**
   * Sole constructor.
   * @param graphURI  the {@link URI} of the graph to be created, never <code>null</code>
   * @param graphTypeURI  thie {@link URI} of the type of graph to create, or
   *       <code>null</code> for the same type as the system graph (<code>#</code>)
   * @throws IllegalArgumentException if <var>graphURI</var> or <var>graphTypeURI</var> is <code>null</code>
   */
  CreateDefaultGraphOperation(URI graphURI, URI graphTypeURI) throws QueryException {
    // Validate parameters
    if (graphURI == null) throw new IllegalArgumentException("Null \"graphURI\" parameter");
    if (graphTypeURI == null) throw new IllegalArgumentException("Null \"graphTypeURI\" parameter");
    // Initialize fields
    this.graphURI     = graphURI;
    this.graphTypeURI = graphTypeURI;
  }

  /**
   * @see org.mulgara.resolver.Operation#execute(org.mulgara.resolver.OperationContext, org.mulgara.resolver.spi.SystemResolver, org.mulgara.resolver.spi.DatabaseMetadata)
   */
  public void execute(OperationContext operationContext, SystemResolver systemResolver,
                      DatabaseMetadata metadata) throws Exception {
    // Find the local node identifying the model
    long graph = systemResolver.localizePersistent(new URIReferenceImpl(graphURI));
    long graphType = systemResolver.localizePersistent(new URIReferenceImpl(graphTypeURI));
    assert graph != NodePool.NONE;
    assert graphType != NodePool.NONE;

    // Check graph does not already exist with a different model type.
    Resolution resolution = systemResolver.resolve(new ConstraintImpl(
        new LocalNode(graph),
        new LocalNode(metadata.getRdfTypeNode()),
        new Variable("x"),
        new LocalNode(metadata.getSystemModelNode())));

    boolean success = false;
    try {
      resolution.beforeFirst();
      if (resolution.next()) {
        Node eNode = systemResolver.globalize(resolution.getColumnValue(0));
        try {
          URIReferenceImpl existing = (URIReferenceImpl)eNode;
          if (!existing.equals(new URIReferenceImpl(graphTypeURI))) {
            throw new QueryException(graphURI + " already exists with model type " + existing +
                " in attempt to create it with type " + graphTypeURI);
          }
          // graph exists and is correct. created stays false
          return;
        } catch (ClassCastException ec) {
          throw new QueryException("Invalid model type entry in system model: " + graphURI + " <rdf:type> " + eNode);
        }
      }
      success = true;
    } finally {
      try {
        resolution.close();
      } catch (TuplesException e) {
        if (success) throw e; // This is a new exception, need to re-throw it.
        else logger.info("Suppressing exception cleaning up from failed read", e); // Log suppressed exception.
      }
    }

    // Find the local node identifying the system graph and rdf:type
    long sysGraph = metadata.getSystemModelNode();
    long rdfType = metadata.getRdfTypeNode();

    // Use the session to create the model
    systemResolver.modifyModel(sysGraph, new SingletonStatements(graph, rdfType, graphType), true);

    created = true;
  }

  /**
   * @return <code>true</code>
   */
  public boolean isWriteOperation() {
    return true;
  }

  /**
   * @return the created flag
   */
  public boolean getResult() {
    return created;
  }

}
