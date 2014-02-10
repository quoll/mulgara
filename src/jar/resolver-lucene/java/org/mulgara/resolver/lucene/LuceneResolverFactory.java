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

package org.mulgara.resolver.lucene;

// Java 2 standard packages
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.resolver.spi.NoSystemResolverFactoryException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverFactoryInitializer;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * Resolves constraints in models defined by static RDF documents.
 *
 * @created 2004-03-31
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:47 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LuceneResolverFactory implements ResolverFactory {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(LuceneResolverFactory.class);

  public static final URI modelTypeURI = URI.create(Mulgara.NAMESPACE + "LuceneModel");
  public static final URI searchURI = URI.create(Mulgara.NAMESPACE + "search");
  public static final URI scoreURI = URI.create(Mulgara.NAMESPACE + "score");

  private final Map<String,LuceneIndexerCache> indexerCaches = new HashMap<String,LuceneIndexerCache>();
  private String directory;

  //
  // Constructors
  //

  /**
   * Instantiate a {@link LuceneResolverFactory}.
   */
  private LuceneResolverFactory(ResolverFactoryInitializer initializer) throws
      InitializerException {
    if (initializer == null) {
      throw new IllegalArgumentException(
          "Null initializer passed to LuceneResolverFactory");
    }

    try {
      // Initialize fields
      directory = initializer.getDirectory().toString();

      // Claim mulgara:LuceneModel
      initializer.addModelType(modelTypeURI, this);

    } catch (NoSystemResolverFactoryException en) {
      throw new InitializerException("Unable to obtain system resolver", en);
    }

    // Register LuceneConstraint
    initializer.registerNewConstraint(new LuceneConstraintDescriptor());

    // Register the LuceneConstraint's transformation
    initializer.addSymbolicTransformation(new LuceneTransformer(modelTypeURI, searchURI, scoreURI));
  }

  //
  // Methods implementing ResolverFactory
  //

  public void close() {
    for (LuceneIndexerCache cache : indexerCaches.values()) {
      cache.close();
    }
    indexerCaches.clear();
  }

  public void delete() {
    for (LuceneIndexerCache cache : indexerCaches.values()) {
      cache.close();
      try {
        cache.removeAllIndexes();
      } catch (IOException ioe) {
        logger.warn("Error deleting lucene index " + cache.getDirectory());
      }
    }
    indexerCaches.clear();
  }

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

  /**
   * Register this resolver upon database startup.
   *
   * @param resolverFactoryInitializer  the database within which to find or
   *   create the various XML Schema resources
   * @throws InitializerException if the XML Schema resources
   *   can't be found or created
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer resolverFactoryInitializer)
      throws InitializerException {
    if (logger.isDebugEnabled()) logger.debug("Creating Lucene resolver factory");
    return new LuceneResolverFactory(resolverFactoryInitializer);
  }

  /**
   * Obtain a Lucene resolver.
   *
   * @param resolverSession  the session which this query is local to
   * @param canWrite  {@inheritDoc}; ignored in this implementation
   * @throws IllegalArgumentException if <var>resolverSession</var> is
   *   <code>null</code>
   * @throws ResolverFactoryException {@inheritDoc}
   */
  public Resolver newResolver(boolean canWrite, ResolverSession resolverSession, Resolver systemResolver)
      throws ResolverFactoryException {
    if (logger.isDebugEnabled()) logger.debug("Creating Lucene resolver");
    return canWrite
      ? new LuceneResolver(modelTypeURI, resolverSession, this, true)
      : new ReadOnlyLuceneResolver(modelTypeURI, resolverSession, this);
  }

  /**
   * Get an indexer-cache for the given model.
   *
   * @param model the model to the indexer-cache for
   * @return the indexer-cache
   * @throws IOException if an error occurred creating an indexer-cache
   */
  LuceneIndexerCache getIndexerCache(String model) throws IOException {
    synchronized (indexerCaches) {
      LuceneIndexerCache cache = indexerCaches.get(model);
      if (cache == null) {
        indexerCaches.put(model, cache = new LuceneIndexerCache(new File(directory, model).toString()));
      }

      return cache;
    }
  }
}
