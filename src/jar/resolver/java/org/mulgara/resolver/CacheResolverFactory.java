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

// Local packages
import org.mulgara.query.LocalNode;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * Wrapper around an external {@link ResolverFactory} that applies caching to
 * its models using a specified internal {@link ResolverFactory}.
 *
 * @created 2004-11-10
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class CacheResolverFactory implements ResolverFactory
{
  /**
   * The factory whose access is being cached.
   */
  private final ResolverFactory externalResolverFactory;

  /**
   * The model type used to store the cache.
   */
  private final URI temporaryModelTypeURI;

  /**
   * The factory used to store the cache.
   */
  private final ResolverFactory temporaryResolverFactory;

  /**
   * The set of cached models.
   *
   * The elements of this set are {@link LocalNode}s.
   * This is basically the counterpart of the system model (<code>#</code>)
   * for temporary models instead of persistent ones.
   */
  private final Set<LocalNode> cachedModelSet;

  /**
   * The set of cached models which have been modified.
   *
   * The elements of this set are {@link LocalNode}s.
   * When the cache is cleared, these models will need to have their changes
   * written back.
   */
  private final Set<LocalNode> changedCachedModelSet;

  //
  // Constructor
  //

  /**
   * Sole constructor.
   *
   * @param cachedModelSet  set of {@link LocalNode}s
   * @param changedCachedModelSet  set of {@link LocalNode}s
   */
  CacheResolverFactory(ResolverFactory externalResolverFactory,
                       ResolverFactory temporaryResolverFactory,
                       URI             temporaryModelTypeURI,
                       Set<LocalNode>  cachedModelSet,
                       Set<LocalNode>  changedCachedModelSet)
  {
    assert externalResolverFactory  != null;
    assert temporaryResolverFactory != null;
    assert temporaryModelTypeURI    != null;
    assert cachedModelSet           != null;
    assert changedCachedModelSet    != null;

    // initialize fields
    this.externalResolverFactory  = externalResolverFactory;
    this.temporaryResolverFactory = temporaryResolverFactory;
    this.temporaryModelTypeURI    = temporaryModelTypeURI;
    this.cachedModelSet           = cachedModelSet;
    this.changedCachedModelSet    = changedCachedModelSet;
  }

  //
  // Methods implementing ResolverFactory
  //

  public void close() throws ResolverFactoryException
  {
    // null implementation
  }

  public void delete() throws ResolverFactoryException
  {
    // null implementation
  }

  /**
   * {@inheritDoc}
   * @return <code>null</code> - no default graphs for this resolver
   */
  public Graph[] getDefaultGraphs() { return null; }
  
  /**
   * {@inheritDoc}
   * @return externalResolverFactory.supportsExport() - should always be true, but check anyways.
   */
  public boolean supportsExport() {
    return externalResolverFactory.supportsExport();
  }

  public Resolver newResolver(boolean         canWrite,
                              ResolverSession resolverSession,
                              Resolver        systemResolver)
    throws ResolverFactoryException
  {
    return new CacheResolver(canWrite,
                             resolverSession,
                             systemResolver,
                             externalResolverFactory,
                             temporaryResolverFactory,
                             temporaryModelTypeURI,
                             cachedModelSet,
                             changedCachedModelSet);
  }
}
