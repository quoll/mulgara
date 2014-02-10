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
 *   getModel() contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.*;
import java.util.*;
import javax.transaction.xa.*;

// Local packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.*;

/**
 * Cached access to models managed by some other {@link ResolverFactory}.
 *
 * @created 2004-11-10
 *
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

class CacheResolver implements Resolver {
  private final Set<LocalNode> cachedModelSet;
  private final boolean canWrite;
  private final Set<LocalNode> changedCachedModelSet;
  private final ResolverFactory externalResolverFactory;
  private final ResolverSession resolverSession;
  private final Resolver systemResolver;
  private final URI temporaryModelTypeURI;
  private final ResolverFactory temporaryResolverFactory;

  //
  // Constructor
  //

  /**
   * Sole constructor.
   */
  CacheResolver(boolean canWrite,
      ResolverSession resolverSession,
      Resolver systemResolver,
      ResolverFactory externalResolverFactory,
      ResolverFactory temporaryResolverFactory,
      URI temporaryModelTypeURI,
      Set<LocalNode> cachedModelSet,
      Set<LocalNode> changedCachedModelSet) {
    // Initialize fields
    this.cachedModelSet = cachedModelSet;
    this.canWrite = canWrite;
    this.changedCachedModelSet = changedCachedModelSet;
    this.externalResolverFactory = externalResolverFactory;
    this.resolverSession = resolverSession;
    this.systemResolver = systemResolver;
    this.temporaryModelTypeURI = temporaryModelTypeURI;
    this.temporaryResolverFactory = temporaryResolverFactory;
  }

  //
  // Methods implementing Resolver
  //

  public void createModel(long model, URI modelType) throws ResolverException,
      LocalizeException {
    // Create external
    // Create temporary (assumes models are empty when created)
    // Add to cachedModelSet

    throw new ResolverException("Cached content can't be created");
  }

  public XAResource getXAResource() {
    // xaResource.end(): write temporary model to external if modified

    return new DummyXAResource(3600 /* one hour timeout */);
  }

  public void modifyModel(long model, Statements statements, boolean occurs) throws ResolverException {
    LocalNode modelLocalNode = new LocalNode(model);

    // Obtain the resolver against the cached version
    Resolver temporaryResolver;
    try {
      temporaryResolver = getTemporaryResolver(modelLocalNode);
    } catch (Exception e) {
      throw new ResolverException("Unable to cache model " + modelLocalNode, e);
    }
    assert temporaryResolver != null;

    // Modify the cached model
    temporaryResolver.modifyModel(model, statements, occurs);

    // Mark cached version of model as changed, needing write-back
    changedCachedModelSet.add(modelLocalNode);
  }

  public void removeModel(long model) throws ResolverException {
    // Remove external
    // Remove temporary
    // Delete from cachedModelSet

    throw new ResolverException("Cached content can't be removed");
  }

  public Resolution resolve(Constraint constraint) throws QueryException {
    // Validate "constraint" parameter
    if (constraint == null) {
      throw new IllegalArgumentException("Null \"constraint\" parameter");
    }
    if (!(constraint.getModel() instanceof LocalNode)) {
      throw new QueryException(
          "Unable to resolve constraint with unbound model: " + constraint
          );
    }
    assert constraint.getModel() instanceof LocalNode;

    LocalNode modelLocalNode = (LocalNode)constraint.getModel();

    // Obtain the resolver against the cached version
    Resolver temporaryResolver;
    try {
      temporaryResolver = getTemporaryResolver(modelLocalNode);
    } catch (Exception e) {
      throw new QueryException("Unable to cache model " + modelLocalNode, e);
    }
    assert temporaryResolver != null;

    return temporaryResolver.resolve(constraint);
  }

  //
  // Internal methods
  //

  /**
   * Obtain a resolver that manipulates the cached version of a specified
   * model.
   *
   * @param modelLocalNode  the model from which to obtain a resolver
   * @return a resolver that manipulates the cached version of the model
   */
  private Resolver getTemporaryResolver(LocalNode modelLocalNode) throws
      LocalizeException, QueryException, ResolverException,
      ResolverFactoryException, TuplesException {
    Resolver temporaryResolver = temporaryResolverFactory.newResolver(
        true, resolverSession, systemResolver
        );
    assert temporaryResolver != null;

    // Ensure that the model is cached
    if (!cachedModelSet.contains(modelLocalNode)) {
      long model = modelLocalNode.getValue();

      // Create the cache model
      temporaryResolver.createModel(model, temporaryModelTypeURI);

      // Obtain the content to cache
      Resolver externalResolver = externalResolverFactory.newResolver(
          canWrite, resolverSession, systemResolver
          );
      assert externalResolver != null;

      Variable s = new Variable("s");
      Variable p = new Variable("p");
      Variable o = new Variable("o");
      Statements statements = new CacheStatements(
          new TuplesWrapperStatements(
          externalResolver.resolve(new ConstraintImpl(s, p, o, modelLocalNode)),
          s, // subject variable
          p, // predicate variable
          o // object variable
          )
          );

      // Add the content to the cache model
      temporaryResolver.modifyModel(model, statements, true);

      // Flag the model as cached
      cachedModelSet.add(modelLocalNode);
    }
    assert cachedModelSet.contains(modelLocalNode);

    return temporaryResolver;
  }

  public void abort() {}
}
