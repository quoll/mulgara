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

package org.mulgara.resolver.distributed;

// Java 2 standard packages
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverFactoryInitializer;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.InitializerException;

/**
 * Factory for a resolver that delegates resolution to another server.
 *
 * @created 2007-03-20
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @version $Revision: $
 * @modified $Date: $
 * @maintenanceAuthor $Author: $
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class DistributedResolverFactory implements ResolverFactory {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(DistributedResolverFactory.class);

  /** Protocols which are handled by the served resolver. */
  private static final Set<String> protocols = new HashSet<String>(Arrays.asList("rmi"));

  /** The session cache to use */
  private final SessionCache sessionCache = new SessionCache();

  /**
   * Instantiate a {@link DistributedResolverFactory}.
   * @param initializer The system initializer to be registered with.
   * @throws InitializerException An error occurred while registering this resolver type.
   */
  private DistributedResolverFactory(ResolverFactoryInitializer initializer) throws InitializerException {
    // Validate "resolverFactoryInitializer" parameter
    if (initializer == null) {
      throw new IllegalArgumentException("Null \"resolverFactoryInitializer\" parameter");
    }

    // Claim the protocols supported by the resolver, and initialize the local protocol set
    for (String p: protocols) {
      initializer.addProtocol(p, this);
    }
  }

  /**
   * {@inheritDoc ResolverFactory}
   */
  public void close() {
    sessionCache.close();
  }

  /**
   * {@inheritDoc ResolverFactory}
   */
  public void delete() {
    // null implementation
  }

  /**
   * {@inheritDoc}
   * @return <code>null</code> - no default graphs for this resolver
   */
  public Graph[] getDefaultGraphs() { return null; }


  /**
   * {@inheritDoc}
   * @return <code>true</code> - any constraint can be resolved against a remote server.
   */
  public boolean supportsExport() {
    return true;
  }


  /**
   * Register this resolver upon database startup.
   *
   * @param initializer the database within which to find or create
   *        the various XML Schema resources
   * @throws InitializerException if the XML Schema resources can't be found or created
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer initializer) throws InitializerException {
    logger.debug("Creating new distributed resolver factory");
    return new DistributedResolverFactory(initializer);
  }


  /**
   * Obtain a distributed resolver.
   *
   * @param resolverSession the session which this query is local to
   * @param canWrite {@inheritDoc}; ignored, as these models are read only
   * @throws IllegalArgumentException if <var>resolverSession</var> is
   *         <code>null</code> or canWrite is <code>true</code>
   * @throws ResolverFactoryException {@inheritDoc}
   */
  public Resolver newResolver(
      boolean canWrite, ResolverSession resolverSession, Resolver systemResolver
  ) throws ResolverFactoryException {

    if (resolverSession == null) throw new IllegalArgumentException("No session provided for the resolver!");
    if (logger.isDebugEnabled()) {
      logger.debug("Creating new distributed resolver");
      if (canWrite) logger.debug("Expecting to write to distributed resolver.");
    }
    return new DistributedResolver(resolverSession, this, canWrite, sessionCache);
  }


  /**
   * Gets the protocols recognized by this resolver.
   * @return A set of the recognized protocols.
   */
  public static Set<String> getProtocols() {
    return protocols;
  }
}
