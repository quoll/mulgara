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
 *   SymbolicTransformationContext contributed by Netymon Pty Ltd on behalf of
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.SecurityAdapter;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.SumOfProductExpansionTransformer;
import org.mulgara.resolver.spi.SymbolicTransformation;
import org.mulgara.resolver.spi.SymbolicTransformationContext;
import org.mulgara.resolver.spi.SystemResolver;
import org.mulgara.resolver.spi.SystemResolverFactory;
import org.mulgara.resolver.spi.TuplesWrapperStatements;
import org.mulgara.resolver.view.ViewMarker;
import org.mulgara.resolver.view.SessionView;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/**
 * Services provided by {@link DatabaseSession} to invocations of the
 * {@link Operation#execute} method.
 *
 * @created 2004-11-08
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.10 $
 * @modified $Date: 2005/05/02 20:07:56 $ by $Author: raboczi $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class DatabaseOperationContext implements OperationContext, SessionView, SymbolicTransformationContext {
 
  /** Logger. */
  private static final Logger logger = Logger.getLogger(DatabaseOperationContext.class.getName());

  /** Logger for {@link SymbolicTransformation} plugins. */
  private static final Logger symbolicLogger =
    Logger.getLogger(DatabaseOperationContext.class.getName() + "#symbolic");

  /**
   * The graphs from external resolvers which have been cached as temporary graphs.
   * Every graph in this set can be manipulated by resolvers from the {@link #temporaryResolverFactory}.
   */
  private final Set<LocalNode> cachedGraphSet;

  /**
   * The graphs from external resolvers which have been cached as temporary graphs and modified.
   * Every graph in this set can be manipulated by resolvers from the {@link #temporaryResolverFactory}.
   */
  private final Set<LocalNode> changedCachedGraphSet;

  /**
   * A map from {@link URI}s of graphs to {@link LocalNode}s representing the localized type of the graph.
   * This is populated by {@link #findGraphTypeURI} and cleared by clear()
   */
  private final Map<LocalNode,URI> systemGraphCacheMap = new WeakHashMap<LocalNode,URI>();

  /** Resolver used for accessing the system graph (<code>#</code>).  */
  protected SystemResolverFactory systemResolverFactory;
  protected SystemResolver systemResolver;

  /** The transaction associated with these operations */
  private MulgaraTransaction transaction;

  // Immutable properties of the containing DatabaseSession
  private final Set<ResolverFactory> cachedResolverFactorySet;  // NOTE: Currently unused
  private final Map<ResolverFactory,Resolver> enlistedResolverMap;
  private final Map<String,ResolverFactory> externalResolverFactoryMap;
    // Hold a specific type of ResolverFactory to restrict what should be in there
  private final Map<URI,InternalResolverFactory> internalResolverFactoryMap;
  private final DatabaseMetadata   metadata;
  private final List<SecurityAdapter> securityAdapterList;
  private final URI                temporaryGraphTypeURI;
  private final ResolverFactory    temporaryResolverFactory;
  /** Symbolic transformations this instance should apply. */
  private final List<SymbolicTransformation> symbolicTransformationList;
  private final boolean            isWriting;

  // Used as a set, all values are null.  Java doesn't provide a WeakHashSet.
  private WeakHashMap<TransactionalAnswer,Object> answers;

  DatabaseOperationContext(Set<ResolverFactory> cachedResolverFactorySet,
                           Map<String,ResolverFactory> externalResolverFactoryMap,
                           Map<URI,InternalResolverFactory> internalResolverFactoryMap,
                           DatabaseMetadata      metadata,
                           List<SecurityAdapter> securityAdapterList,
                           URI                   temporaryGraphTypeURI,
                           ResolverFactory       temporaryResolverFactory,
                           List<SymbolicTransformation> symbolicTransformationList,
                           SystemResolverFactory systemResolverFactory,
                           boolean               isWriting)
  {
    assert cachedResolverFactorySet   != null;
    assert externalResolverFactoryMap != null;
    assert internalResolverFactoryMap != null;
    assert metadata                   != null;
    assert securityAdapterList        != null;
    assert temporaryGraphTypeURI      != null;
    assert temporaryResolverFactory   != null;
    assert symbolicTransformationList != null;
    assert systemResolverFactory      != null;

    this.cachedResolverFactorySet   = cachedResolverFactorySet;
    this.externalResolverFactoryMap = externalResolverFactoryMap;
    this.internalResolverFactoryMap = internalResolverFactoryMap;
    this.metadata                   = metadata;
    this.securityAdapterList        = securityAdapterList;
    this.temporaryGraphTypeURI      = temporaryGraphTypeURI;
    this.temporaryResolverFactory   = temporaryResolverFactory;
    this.symbolicTransformationList = symbolicTransformationList;
    this.isWriting                  = isWriting;
    this.systemResolverFactory      = systemResolverFactory;

    this.cachedGraphSet             = new HashSet<LocalNode>();
    this.changedCachedGraphSet      = new HashSet<LocalNode>();
    this.enlistedResolverMap        = new HashMap<ResolverFactory,Resolver>();
    this.answers                    = new WeakHashMap<TransactionalAnswer,Object>();
  }

  //
  // Methods implementing OperationContext
  //

  public ResolverFactory findModelResolverFactory(long graph) throws QueryException {

    if (logger.isDebugEnabled()) logger.debug("Finding resolver factory for graph " + graph);

    // See if the graph is an internal one, with a graph type
    try {
      URI graphTypeURI = findGraphTypeURI(graph);
      if (graphTypeURI != null) {
        // The graph had a type recorded in the system graph, so it's internal
        if (logger.isDebugEnabled()) {
          logger.debug("Graph " + graph + " type is " + graphTypeURI);
        }
        InternalResolverFactory internalResolverFactory = internalResolverFactoryMap.get(graphTypeURI);

        if (internalResolverFactory == null) {
          throw new QueryException("Unsupported graph type for graph " + graph);        }

        return internalResolverFactory;
      } else {
        // This might be an external graph or an aliased internal graph.
        // get the graph URI
        Node graphNode = systemResolver.globalize(graph);
        if (!(graphNode instanceof URIReference)) {
          throw new QueryException(graphNode.toString() + " is not a valid Graph");
        }
        URI graphURI = ((URIReference)graphNode).getURI();

        // check if this is really a reference to a local graph, using a different server name
        Node aliasedNode = getCanonicalAlias(graphURI);
        if (aliasedNode != null) {
          long aliasedGraph = systemResolver.localize(aliasedNode);
          // In some situations, a canonical alias can localize back to the original graph node.
          if (aliasedGraph != graph) {
            return findModelResolverFactory(aliasedGraph);
          }
        }

        // test the graph URI against the current server
        try {
          if (logger.isDebugEnabled()) {
            logger.debug("Comparing " + metadata.getURI().toString() + " to " + (new URI(graphURI.getScheme(),
                    graphURI.getSchemeSpecificPart(), null)).toString());
          }

          // Check all the hostname aliases to see if we're attempting to
          // contact the local server.
          URI tmpGraphName = new URI(graphURI.getScheme(),
              graphURI.getSchemeSpecificPart(), null);
          String host = tmpGraphName.getHost();

          // Ensure that the host name can be extracted - in case there's an
          // opaque hostname.
          if (tmpGraphName.isOpaque()) {
            throw new QueryException("Graph not in local storage, and not able to be found with the \"" + graphURI.getScheme() + "\" scheme: <" + tmpGraphName + ">");
          }

          // Do not test for locality if jar or file protocol
          if (!(graphURI.getScheme().startsWith("file")) &&
            !(graphURI.getScheme().startsWith("jar"))) {

            // Check that it's the same host name and server name.
            if ((metadata.getHostnameAliases().contains(host)) &&
                (metadata.getServerName().equals(metadata.getServerName(graphURI)))) {
              // should be on the current server, but was not found here
              throw new QueryException(graphNode.toString() +
                  " has a URI indicating the local server, but was not found");
            }
          }
        } catch (URISyntaxException use) {
          throw new QueryException("Internal error.  Graph URI cannot be manipulated.");
        }

        // This is not a local graph, get the protocol
        String graphProtocol = findProtocol(graph);
        if (logger.isDebugEnabled()) {
          logger.debug("Graph " + graph + " protocol is " + graphProtocol);
        }

        // find the factory for this protocol
        ResolverFactory resolverFactory = externalResolverFactoryMap.get(graphProtocol);
        if (resolverFactory == null) {
          throw new QueryException(
              "Graph <" + graph + "> was not found locally, and the \"" + graphProtocol + "\" protocol is unsupported");
        }

        // For the moment, not applying caching to any external graphs
        // TODO: add a method to ResolverFactory interface to test whether
        //       caching is appropriate for that particular implementation
        if (cachedResolverFactorySet.contains(resolverFactory)) {
          return new CacheResolverFactory(resolverFactory,
                                          temporaryResolverFactory,
                                          temporaryGraphTypeURI,
                                          cachedGraphSet,
                                          changedCachedGraphSet);
        } else {
          return resolverFactory;
        }
      }
    } catch (GlobalizeException eg) {
      throw new QueryException("Unable to globalize graph type", eg);
    } catch (LocalizeException el) {
      throw new QueryException("Unable to localize graph", el);
    }
  }

  /**
   * Find a cached resolver factory for write back.
   *
   * @return a completely unwrapped resolver factory
   */
  // TODO: Common code with findModelResolverFactory should be consolidated.
  private ResolverFactory findResolverFactory(long graph) throws QueryException {

    if (logger.isDebugEnabled()) logger.debug("Finding raw resolver factory for graph " + graph);

    try {
      // get the graph URI
      Node graphNode = systemResolver.globalize(graph);
      if (!(graphNode instanceof URIReference)) {
        throw new QueryException(graphNode.toString() + " is not a valid Graph");
      }
      URI graphURI = ((URIReference)graphNode).getURI();

      // test the graph URI against the current server
      try {
        if (logger.isDebugEnabled()) {
          logger.debug("Comparing " + metadata.getURI().toString() + " to "
              + (new URI(graphURI.getScheme(), graphURI.getSchemeSpecificPart(), null)).toString());
        }
        if (metadata.getURI().equals(
            new URI(graphURI.getScheme(), graphURI.getSchemeSpecificPart(), null))) {
          // should be on the current server, but was not found here
          throw new QueryException(graphNode.toString() + " is not a Graph");
        }
      } catch (URISyntaxException use) {
        throw new QueryException("Internal error.  Graph URI cannot be manipulated.");
      }

      // This is not a local graph, get the protocol
      String graphProtocol = findProtocol(graph);
      if (logger.isDebugEnabled()) {
        logger.debug("Graph " + graph + " protocol is " + graphProtocol);
      }

      // find the factory for this protocol
      ResolverFactory resolverFactory = externalResolverFactoryMap.get(graphProtocol);
      if (resolverFactory == null) {
        throw new QueryException(
            "Unsupported protocol for destination graph (" +
            graphProtocol + ", " + graph + " : '" + graphProtocol + "')");
      }

      return resolverFactory;
    } catch (GlobalizeException eg) {
      throw new QueryException("Unable to globalize graph type", eg);
    }
  }

  public ResolverFactory findModelTypeResolverFactory(URI graphTypeURI) throws QueryException {
    return internalResolverFactoryMap.get(graphTypeURI);
  }

  public List<SecurityAdapter> getSecurityAdapterList() {
    return securityAdapterList;
  }

  public Resolver obtainResolver(ResolverFactory resolverFactory) throws QueryException {

    // Obtain a resolver
    Resolver resolver = enlistedResolverMap.get(resolverFactory);
    if (resolver != null) {
      return resolver;
    }

    try {
      resolver = resolverFactory.newResolver(isWriting, systemResolver, systemResolver);

      // FIXME: This is a kludge.  This should be done using a query rewriting
      //        hook in the ResolverFactory interface.  This hook is also
      //        required for efficient evaluation of XSD/Type constraints
      //        (specifically intervals), and distributed queries
      //        (specificially appended joins).
      if (resolver instanceof ViewMarker) {
        ((ViewMarker) resolver).setSession(this);
      }
    } catch (ResolverFactoryException e) {
      throw new QueryException("Unable to obtain resolver", e);
    }

    assert resolver != null;

    try {
      transaction.enlist(resolver);
    } catch (Exception e) {
      logger.warn("Failed to enlist resolver, aborting resolver");
      resolver.abort();
      throw new QueryException("Unable to enlist " + resolver + " into transaction", e);
    }

    enlistedResolverMap.put(resolverFactory, resolver);

    return resolver;
  }


  /**
   * Returns the canonical form of the graph, leaving the graph alone if it is recognised or unknown.
   *
   * @param graph The graph to check.
   * @return The new graph node, or the current graph if it is already canonical or unknown.
   */
  public long getCanonicalModel(long graph) {
    // globalize to a URI
    try {
      Node graphNode = systemResolver.globalize(graph);
      if (!(graphNode instanceof URIReference)) {
        logger.warn(graphNode.toString() + " is not a valid Graph");
        return graph;
      }
      URI graphURI = ((URIReference)graphNode).getURI();

      // check if this is really a reference to a local graph, using a different server name
      Node aliasedNode = getCanonicalAlias(graphURI);
      if (aliasedNode != null) {
        return systemResolver.localize(aliasedNode);
      }
    } catch (Exception e) {
      // unable to get a canonical form, so leave this graph alone
    }
    // graph was not recognised as being on this server, so leave it alone
    return graph;
  }

  //
  // Methods required by SymbolicTransformationContext
  //

  public URI mapToModelTypeURI(URI graphURI) throws QueryException {
    try {
      if (logger.isDebugEnabled()) logger.debug("Finding graphTypeURI for " + graphURI);
      long rawGraph = systemResolver.localize(new URIReferenceImpl(graphURI, false));
      long canGraph = getCanonicalModel(rawGraph);

      URI graphTypeURI = findGraphTypeURI(canGraph);

      if (logger.isDebugEnabled()) {
        logger.debug("Mapped " + graphURI + " via " + rawGraph + ":" + canGraph + " to GraphTypeURI: " + graphTypeURI);
      }

      return graphTypeURI;
    } catch (GlobalizeException eg) {
      throw new QueryException("Failed to map graph to graphType", eg);
    } catch (LocalizeException el) {
      throw new QueryException("Failed to map graph to graphType", el);
    }
  }

  //
  // Internal methods
  //

  /**
   * Find the type of a graph.
   *
   * @param graph  the local node of a graph
   * @return the local node representing the type of the <var>graph</var>, or
   *   {@link org.mulgara.store.nodepool.NodePool#NONE} if the <var>graph</var>
   *   isn't stored within the system
   * @throws QueryException if the graph type can't be determined
   */
  private URI findGraphTypeURI(long graph) throws QueryException, GlobalizeException {
    // If graph is a query-node, graph cannot exist in the system graph so return null.
    if (graph < 0) return null;

    // Check our cached version of the system graph
    LocalNode graphLocalNode = new LocalNode(graph);
    URI graphTypeURI = systemGraphCacheMap.get(graphLocalNode);
    if (graphTypeURI != null) {
      return graphTypeURI;
    }

    // Query the system graph for the type of the graph
    Variable graphTypeVariable = new Variable("graphType");
    Constraint graphConstraint =
      new ConstraintImpl(new LocalNode(graph),
                         new LocalNode(metadata.getRdfTypeNode()),
                         graphTypeVariable,
                         new LocalNode(metadata.getSystemModelNode()));
    Resolution resolution = systemResolver.resolve(graphConstraint);
    assert resolution != null;

    // Check the solution and extract the graph type (if any) from it
    try {
      resolution.beforeFirst();
      if (resolution.next()) {
        long graphType = resolution.getColumnValue(
            resolution.getColumnIndex(graphTypeVariable));

        if (resolution.next()) {
          throw new QueryException("Graph " + graph + " has more than one type!");
        }
        Node graphNode = systemResolver.globalize(graphType);
        assert graphNode instanceof URIReferenceImpl;
        graphTypeURI = ((URIReferenceImpl) graphNode).getURI();
        systemGraphCacheMap.put(graphLocalNode, graphTypeURI);

        return graphTypeURI;
      } else {
        return null;
      }
    } catch (TuplesException e) {
      throw new QueryException("Unable to determine graph type of " + graph, e);
    } finally {
      if ( resolution != null ) {
        try {
          resolution.close();
        } catch (TuplesException e) {
          logger.warn("Unable to close find graph type resolution to graph " + graph, e);
        }
      }
    }
  }

  /**
   * @param n  the local node corresponding to the URI reference
   * @return  the scheme part of the <var>node</var>'s URI reference
   * @throws QueryException if the <var>node</var> can't be globalized or
   *   isn't a URI reference
   */
  private String findProtocol(long n) throws QueryException {
    try {
      // Globalize the node
      Node node = (Node) systemResolver.globalize(n);
      if (!(node instanceof URIReference)) {
        throw new QueryException(node + " is not a URI reference");
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Graph URI for graph " + n + " is " + node);
      }
      // Return the protocol
      return ((URIReference) node).getURI().getScheme();
    } catch (GlobalizeException e) {
      throw new QueryException("Unable to globalize node " + n, e);
    }
  }

  /**
   * Check if the given graph actually refers to a graph on the local server.
   *
   * @param graphURI The URI of the graph being searched for.
   * @return The Node for the local graph, or <code>null</code> if not found.
   * @throws QueryException When the graph URI cannot be manipulated.
   */
  private Node getCanonicalAlias(URI graphURI) throws QueryException {
    if (logger.isDebugEnabled()) {
      logger.debug("Checking for an alias on: " + graphURI);
    }

    // extract the host name
    String host = graphURI.getHost();
    if (host == null) {
      return null;
    }
    // Check if this host has been heard of before
    if (metadata.getHostnameAliases().contains(host)) {
      // this name is acceptable, so leave it alone
      return null;
    }
    // Check with a DNS server to see if this host is recognised
    InetAddress addr = null;
    try {
      addr = InetAddress.getByName(host);
    } catch (UnknownHostException uhe) {
      // The host was unknown, so allow resolution to continue as before
      return null;
    }
    // check the various names against known aliases
    if (metadata.getHostnameAliases().contains(addr.getHostName()) ||
        metadata.getHostnameAliases().contains(addr.getCanonicalHostName()) ||
        metadata.getHostnameAliases().contains(addr.getHostAddress())) {
      // change the host name to one that is recognised
      // Make sure that it doesn't canonicalize to the original URI; this could cause an infinite loop.
      URI newGraphURI = getLocalURI(graphURI);
      if (!graphURI.equals(newGraphURI)) {
        return new URIReferenceImpl(graphURI);
      }
    }

    // not found, so return nothing
    return null;
  }


  /**
   * Convert a URI to a URIReference which refers to the canonical local machine name.
   *
   * @param uri The URI to update.
   * @return The URIReference representing the same URI as the parameter, with the host name updated.
   * @throws QueryException When the uri cannot be manipulated.
   */
  private URI getLocalURI(URI uri) throws QueryException {
    // use the system graph to find the local host name
    String newHost = metadata.getSystemModelURI().getHost();
    // update the URI
    try {
      URI newGraphURI = new URI(uri.getScheme(), uri.getUserInfo(), newHost, uri.getPort(),
                                uri.getPath(), uri.getQuery(), uri.getFragment());
      logger.debug("Changing graph URI from " + uri + " to " + newGraphURI);

      return newGraphURI;
    } catch (URISyntaxException e) {
      throw new QueryException("Internal error.  Graph URI cannot be manipulated.");
    }
  }

  /**
   * Resolve a localized constraint into the tuples which satisfy it.
   *
   * This method must be called within a transactional context.
   *
   * Will be made package-scope as soon as the View kludge is resolved.
   *
   * Deprecation warning removed to assist development.
   *
   * @param constraint  a localized constraint
   * @return the tuples satisfying the <var>constraint</var>
   * @throws IllegalArgumentException if <var>constraint</var> is
   *   <code>null</code>
   * @throws QueryException if the <var>constraint</var> can't be resolved
   */
  public Tuples resolve(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled()) {
      logger.debug("Resolving " + constraint);
    }

    // Validate "constraint" parameter
    if (constraint == null) {
      throw new IllegalArgumentException("Null \"constraint\" parameter");
    }

    ConstraintElement graphElem = constraint.getModel();
    if (graphElem instanceof Variable) {
      return resolveVariableGraph(constraint);
    } else if (graphElem instanceof LocalNode) {
      long graph = ((LocalNode) graphElem).getValue();
      long realGraph = getCanonicalModel(graph);

      // Make sure security adapters are satisfied
      for (SecurityAdapter securityAdapter: securityAdapterList) {

        // Lie to the user
        if (!securityAdapter.canSeeModel(realGraph, systemResolver)) {
          try {
            throw new QueryException("No such graph " + systemResolver.globalize(realGraph));
          } catch (GlobalizeException e) {
            logger.warn("Unable to globalize graph " + realGraph);
            throw new QueryException("No such graph");
          }
        }
      }

      for (SecurityAdapter securityAdapter: securityAdapterList) {

        // Tell a different lie to the user
        if (!securityAdapter.canResolve(realGraph, systemResolver)) {
          return TuplesOperations.empty();
        }
      }

      // if the graph was changed then update the constraint
      if (graph != realGraph) {
        constraint = ConstraintOperations.rewriteConstraintModel(new LocalNode(realGraph), constraint);
      }

      // Evaluate the constraint
      Tuples result = obtainResolver(findModelResolverFactory(realGraph)).resolve(constraint);
      assert result != null;

      return result;
    } else {
      throw new QueryException("Non-localized graph in resolve: " + graphElem);
    }
  }

  /**
  * Resolve a {@link Constraint} in the case where the graph isn't fixed.
  *
  * This is mostly relevant in the case where the <code>in</code> clause takes
  * a variable parameter.  It's tricky to resolve because external graphs may
  * be accessible to the system, but aren't known to it unless they're named.
  * The policy we take is to only consider internal graphs.
  *
  * @param constraint  a constraint with a {@link Variable}-valued graph
  *   element, never <code>null</code>
  * @return the solutions to the <var>constraint</var> occurring in all
  *   internal graphs, never <code>null</code>
  * @throws QueryException if the solution can't be evaluated
  */
  private Tuples resolveVariableGraph(Constraint constraint) throws QueryException {
    assert constraint != null;
    assert constraint.getElement(3) instanceof Variable;

    Tuples tuples = TuplesOperations.empty();

    // This is the alternate code we'd use if we were to consult external
    // graphs as well as internal graphs during the resolution of variable IN
    // clauses:
    //
    //Iterator i = resolverFactoryList.iterator();

    for (ResolverFactory resolverFactory: internalResolverFactoryMap.values()) {
      assert resolverFactory != null;

      // Resolve the constraint
      Resolver resolver = obtainResolver(resolverFactory);
      if (logger.isDebugEnabled()) {
        logger.debug("Resolving " + constraint + " against " + resolver);
      }
      Resolution resolution = resolver.resolve(constraint);
      assert resolution != null;

      try {
        // If this is a complete resolution of the constraint, we won't have to
        // consider any of the other resolvers
        if (resolution.isComplete()) {
          if (logger.isDebugEnabled()) {
            logger.debug("Returning complete resolution from " + resolver);
          }
          tuples.close();

          return resolution;
        } else {
          // Append the resolution to the overall solutions
          if (logger.isDebugEnabled()) {
            logger.debug("Appending " + resolver);
          }
          Tuples oldTuples = tuples;
          try {
            tuples = TuplesOperations.append(tuples, resolution);
          } finally {
            resolution.close();
          }
          oldTuples.close();
        }
      } catch (TuplesException e) {
        throw new QueryException("Unable to resolve " + constraint, e);
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Resolved " + constraint + " to " +
          TuplesOperations.formatTuplesTree(tuples));
    }

    return tuples;
  }

  Tuples innerCount(Query query) throws QueryException {
    if (query == null) {
      throw new IllegalArgumentException("Null \"query\" parameter");
    }

    if (logger.isInfoEnabled()) {
      logger.info("Inner Count: " + query);
    }
    try {
      query = transform(query);
      LocalQueryResolver lq = new LocalQueryResolver(this, systemResolver);
      Tuples result = lq.resolveE(query);
      query.close();

      return result;
    } catch (QueryException eq) {
      throw eq;
    } catch (Exception e) {
      throw new QueryException("Failed to evaluate count", e);
    }
  }

  protected void doModify(URI graphURI, Statements statements, boolean insert) throws Throwable {
    long graph = systemResolver.localize(new URIReferenceImpl(graphURI));
    graph = getCanonicalModel(graph);

    // Make sure security adapters are satisfied
    for (SecurityAdapter securityAdapter: securityAdapterList) {

      // Lie to the user
      if (!securityAdapter.canSeeModel(graph, systemResolver)) {
        throw new QueryException("No such graph " + graphURI);
      }

      // Tell the truth to the user
      if (!securityAdapter.canModifyModel(graph, systemResolver)) {
        throw new QueryException("You aren't allowed to modify " + graphURI);
      }
    }

    // Obtain a resolver for the destination graph type
    Resolver resolver = obtainResolver(findModelResolverFactory(graph));
    assert resolver != null;

    if (logger.isDebugEnabled()) {
      logger.debug("Modifying " + graphURI + " using " + resolver);
    }

    resolver.modifyModel(graph, statements, insert);

    if (logger.isDebugEnabled()) {
      logger.debug("Modified " + graphURI);
    }
  }

  public Answer doQuery(Query query) throws Exception {
    Answer result;

    query = transform(query);

    LocalQueryResolver localQuery = new LocalQueryResolver(this, systemResolver);

    // Complete the numerical phase of resolution
    Tuples tuples = localQuery.resolveE(query);
    if (query instanceof AskQuery) {
      // strip the answer down to true/false
      result = new BooleanAnswer(tuples.getRowCardinality() != 0);
    } else {
      result = new TransactionalAnswer(transaction, new SubqueryAnswer(this, systemResolver, tuples, query.getVariableList()));
      answers.put((TransactionalAnswer)result, null);

      // check if the query was a CONSTRUCT, and wrap in a graph filter if needed
      if (query instanceof ConstructQuery) result = new GraphAnswer(result);
    }
    tuples.close();

    return result;
  }

  /**
   * Apply the registered transformations to the query until we reach a
   * fixed-point.
   */
  Query transform(Query query) throws Exception {
    // Start with the symbolic phase of resolution
    if (symbolicLogger.isDebugEnabled()) {
      symbolicLogger.debug("Before transformation: " + query);
    }

    MutableLocalQueryImpl mutable = new MutableLocalQueryImpl(query);
    List<SymbolicTransformation> symTxList;
    symTxList = symbolicTransformationList;

    // non-DISTINCT queries need to be expanded so the disjunction is at the root
    if (!query.isDistinct()) {
      symTxList = new ArrayList<SymbolicTransformation>(symbolicTransformationList);
      symTxList.add(new SumOfProductExpansionTransformer());
    }

    Iterator<SymbolicTransformation> i = symTxList.iterator();
    while (i.hasNext()) {
      SymbolicTransformation symbolicTransformation = i.next();
      assert symbolicTransformation != null;

      symbolicTransformation.transform(this, mutable);

      // When a transformation succeeds, we rewind and start from the
      // beginning of the symbolicTransformationList again
      if (mutable.isModified()) {
        if (symbolicLogger.isDebugEnabled()) {
          symbolicLogger.debug("Symbolic transformation: " + mutable);
        }

        Query tmp = query;
        query = new Query(query, mutable.getConstraintExpression());
        tmp.close();

        mutable = new MutableLocalQueryImpl(query);

        // start again
        i = symTxList.iterator();
      }
    }

    return query;
  }

  void clear() throws QueryException {
    Throwable error = null;
    try {
      for (TransactionalAnswer answer : answers.keySet()) {
        try {
          answer.sessionClose();
        } catch (Throwable th) {
          if (error == null) {
            error = th;
          }
        }
      }
      answers.clear();
    } finally {
      try {
        clearCache();
      } finally {
        systemResolver = null;
        systemGraphCacheMap.clear();
        enlistedResolverMap.clear();
        if (error != null) {
          throw new QueryException("Error force-closing answers", error);
        }
      }
    }
  }

  public SystemResolver getSystemResolver() {
    return systemResolver;
  }


  /**
   * Clear the cache of temporary graphs.
   */
  private void clearCache() {
    // Clear the temporary graphs
    if (!cachedGraphSet.isEmpty()) {
      try {
        Resolver temporaryResolver =
          temporaryResolverFactory.newResolver(true, systemResolver, systemResolver);
        for (Iterator<LocalNode> i = cachedGraphSet.iterator(); i.hasNext();) {
          LocalNode graphLocalNode = i.next();
          long graph = graphLocalNode.getValue();

          if (changedCachedGraphSet.contains(graphLocalNode)) {
            // Write back the modifications to the original graph
            try {
              Resolver resolver =
                findResolverFactory(graph).newResolver(true, systemResolver, systemResolver);
              Variable s = new Variable("s");
              Variable p = new Variable("p");
              Variable o = new Variable("o");
              resolver.modifyModel(graph,
                new TuplesWrapperStatements(temporaryResolver.resolve(
                    new ConstraintImpl(s, p, o, graphLocalNode)), s, p, o),
                true  // insert the content
              );
            } catch (Exception e) {
              logger.error("Failed to write back cached graph " + graph + " after transaction", e);
            }
            changedCachedGraphSet.remove(graphLocalNode);
          }

          // Remove the cached graph
          try {
            temporaryResolver.removeModel(graph);
          } catch (Exception e) {
            logger.error("Failed to clear cached graph " + graph + " after transaction", e);
          }
          i.remove();
        }
      } catch (Exception e) {
        logger.error("Failed to clear cached graphs after transaction", e);
      }
    }
  }

  public void initiate(MulgaraTransaction transaction) throws QueryException {
    try {
      this.transaction = transaction;
      this.systemResolver = systemResolverFactory.newResolver(isWriting);
      transaction.enlist(systemResolver);
    } catch (Exception e) {
      throw new QueryException("Unable to enlist systemResolver:" + 
          systemResolver + " into transaction", e);
    }
  }
}
