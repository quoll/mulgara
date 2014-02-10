/*
 * Copyright 2008 Fedora Commons, Inc.
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

package org.mulgara.resolver.prefix;

// Java 2 standard packages
import java.net.*;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.*;

/**
 * Factory for a resolver that gets type information from the string pool
 *
 * @created 2005-4-19
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class PrefixResolverFactory implements ResolverFactory {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(PrefixResolverFactory.class.getName());

  /** The URI for the default graph. */
  private static final URI DEFAULT_GRAPH = URI.create(Mulgara.PREFIX_GRAPH);

  /** The preallocated local node representing the prefix predicate. */
  private final long mulgaraPrefix;

  /** The preallocated local node representing the string prefix predicate. */
  private final long mulgaraStringPrefix;

  //
  // Constructors
  //

  /**
   * Instantiate a {@link PrefixResolverFactory}.
   * @param initializer The environment for the constructor.
   */
  private PrefixResolverFactory(ResolverFactoryInitializer initializer) throws InitializerException {

    // Validate "resolverFactoryInitializer" parameter
    if (initializer == null) {
      throw new IllegalArgumentException("Null \"resolverFactoryInitializer\" parameter");
    }

    // intialize the fields
    mulgaraPrefix = initializer.preallocate(new URIReferenceImpl(Mulgara.PREFIX_URI));
    mulgaraStringPrefix = initializer.preallocate(new URIReferenceImpl(Mulgara.STR_PREFIX_URI));

    // no need to claim the types supported by this resolver, as this is detected in the default graphs
  }

  //
  // Methods implementing ResolverFactory
  //

  /**
   * {@inheritDoc ResolverFactory}
   */
  public void close() {
    // null implementation
  }

  /**
   * {@inheritDoc ResolverFactory}
   */
  public void delete() {
    // null implementation
  }

  /**
   * Register this resolver upon database startup.
   *
   * @param resolverFactoryInitializer  the database within which to find or
   *   create the various XML Schema resources
   * @throws InitializerException if the XML Schema resources can't be found or
   *   created
   */
  public static ResolverFactory newInstance(
    ResolverFactoryInitializer resolverFactoryInitializer
  ) throws InitializerException {
    return new PrefixResolverFactory(resolverFactoryInitializer);
  }

  /**
   * Obtain a Node Type resolver.
   *
   * @param resolverSession  the session which this query is local to
   * @param canWrite  {@inheritDoc}; ignored, as these graphs are read only
   * @throws IllegalArgumentException if <var>resolverSession</var> is
   *   <code>null</code> or canWrite is <code>true</code>
   * @throws ResolverFactoryException {@inheritDoc}
   */
  public Resolver newResolver(
      boolean canWrite, ResolverSession resolverSession, Resolver systemResolver
  ) throws ResolverFactoryException {
    if (logger.isDebugEnabled()) logger.debug("Creating new Prefix resolver");
    return new PrefixResolver(resolverSession, systemResolver, mulgaraPrefix, mulgaraStringPrefix, Mulgara.PREFIX_GRAPH_TYPE_URI);
  }

  /**
   * @return The default graph for this resolver.
   */
  public Graph[] getDefaultGraphs() {
    return new Graph[] { new Graph(DEFAULT_GRAPH, Mulgara.PREFIX_GRAPH_TYPE_URI) };
  }
  
  /**
   * {@inheritDoc}
   * @return <code>false</code> - this graph does not support exports.
   */
  public boolean supportsExport() {
    return false;
  }
}
