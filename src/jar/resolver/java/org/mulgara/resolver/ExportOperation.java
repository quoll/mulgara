/*
 * Copyright 2009 Revelytix.
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
package org.mulgara.resolver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import javax.activation.MimeType;

import org.apache.log4j.Logger;
import org.mulgara.content.Content;
import org.mulgara.content.ContentHandler;
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.LocalNode;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.SystemResolver;
import org.mulgara.resolver.spi.TuplesWrapperStatements;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.tuples.Tuples;

/**
 * An {@link Operation} that serializes the contents of an RDF graph to either
 * an output stream or a destination file.
 *
 * @created Jun 25, 2008
 * @author Alex Hall
 * @copyright &copy; 2008 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
class ExportOperation extends OutputOperation {

  private static final Logger logger = Logger.getLogger(ExportOperation.class);
  
  private final URI graphURI;
  private final Map<String,URI> prefixes;
  private final MimeType contentType;
  private final ContentHandlerManager contentManager;

  /**
   * Create an {@link Operation} which exports the contents of the specified RDF graph
   * to a URI or to an output stream.
   *
   * The database is not changed by this method.
   * If an {@link OutputStream} is supplied then the destinationURI is ignored.
   *
   * @param outputStream An output stream to receive the contents, may be
   *   <code>null</code> if a <var>destinationURI</var> is specified
   * @param graphURI The URI of the graph to export, never <code>null</code>.
   * @param destinationURI The URI of the file to export into, may be
   *   <code>null</code> if an <var>outputStream</var> is specified
   * @param initialPrefixes An optional set of user-supplied namespace prefix mappings;
   *   may be <code>null</code> to use the generated namespace prefixes.
   */
  public ExportOperation(OutputStream outputStream, URI graphURI, URI destinationURI,
      Map<String,URI> initialPrefixes, MimeType contentType, ContentHandlerManager contentManager) {
    super(outputStream, destinationURI);

    if (graphURI == null) {
      throw new IllegalArgumentException("Graph URI may not be null.");
    }
    if (contentManager == null) {
      throw new IllegalArgumentException("Content manager may not be null.");
    }
    this.graphURI = graphURI;
    this.contentType = contentType;
    this.prefixes = initialPrefixes;
    this.contentManager = contentManager;
  }

  /* (non-Javadoc)
   * @see org.mulgara.resolver.OutputOperation#execute(org.mulgara.resolver.OperationContext, org.mulgara.resolver.spi.SystemResolver, org.mulgara.resolver.spi.DatabaseMetadata)
   */
  @Override
  public void execute(OperationContext operationContext, SystemResolver systemResolver,
                      DatabaseMetadata metadata) throws Exception {
    // Verify that the graph is of a type that supports exports.
    long graph = systemResolver.localize(new URIReferenceImpl(graphURI));
    ResolverFactory resolverFactory = operationContext.findModelResolverFactory(graph);

    if (resolverFactory.supportsExport()) {
      OutputStream os = getOutputStream();
      assert os != null;

      boolean success = false;
      try {
        Content content = new StreamContent(os, destinationURI, contentType);
        ContentHandler handler = contentManager.getContentHandler(content);
        if (handler == null) throw new QueryException("Unable to determine content handler for " + destinationURI);

        // create a constraint to get all statements
        Variable[] vars = new Variable[] {
            StatementStore.VARIABLES[0],
            StatementStore.VARIABLES[1],
            StatementStore.VARIABLES[2]
        };
        Constraint constraint = new ConstraintImpl(vars[0], vars[1], vars[2], new LocalNode(graph));

        // Get all statements from the graph.  Delegate to the operation context to do the security check.
        Tuples resolution = operationContext.resolve(constraint);
        Statements graphStatements = new TuplesWrapperStatements(resolution, vars[0], vars[1], vars[2]);

        // Do the writing.
        try {
          handler.serialize(graphStatements, content, systemResolver, prefixes);
        } finally {
          // This will close the wrapped resolution as well.
          graphStatements.close();
        }
        success = true;
      } finally {
        // Clean up.
        if (os != null) {
          // Close the os if it exists.
          try {
            os.close();
          } catch (IOException e) {
            if (success) throw e; // The export worked but we couldn't close the stream, so re-throw.
            else logger.info("Suppressing I/O exception closing failed export output", e); // Log and ignore.
          }
        }
      }
    } else {
      throw new QueryException("Graph " + graphURI + " does not support export.");
    }
  }
}
