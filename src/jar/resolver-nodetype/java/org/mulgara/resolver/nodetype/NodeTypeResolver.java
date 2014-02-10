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

package org.mulgara.resolver.nodetype;

// Java 2 standard packages
import java.net.*;
import java.util.*;
import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/**
 * Resolves constraints accessible through a session.
 *
 * @created 2004-10-27
 * @author Paul Gearon
 * @version $Revision: 1.10 $
 * @modified $Date: 2005/05/15 01:04:25 $ @maintenanceAuthor $Author: pgearon $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class NodeTypeResolver implements Resolver
{
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(NodeTypeResolver.class.getName());

  /** The session that this resolver is associated with.  */
  private final ResolverSession resolverSession;

  /** The URI of the type describing node type graphs.  */
  private URI graphTypeURI;

  /** The node for the type describing node type graphs.  */
  @SuppressWarnings("unused")
  private long graphType;

  /** The preallocated local node representing the rdf:type property. */
  private long rdfType;

  /** The local node represening the RDFS literal type. */
  private long rdfsLiteral;

  /** The local node represening the URI Reference type. */
  private long mulgaraUriReference;


  //
  // Constructors
  //

  /**
   * Construct a resolver.
   *
   * @param resolverSession  the session this resolver is associated with
   * @throws IllegalArgumentException  if <var>resolverSession</var> is
   *   <code>null</code>
   * @throws ResolverFactoryException  if the superclass is unable to handle its arguments
   */
  NodeTypeResolver(
      ResolverSession resolverSession,
      Resolver systemResolver,
      long rdfType,
      long systemGraph,
      long rdfsLiteral,
      long mulgaraUriReference,
      long graphType,
      URI graphTypeURI
  ) throws ResolverFactoryException {

    if (logger.isDebugEnabled()) {
      logger.debug("Instantiating a node type resolver");
    }

    // Validate "resolverSession" parameter
    if (resolverSession == null) {
      throw new IllegalArgumentException( "Null \"resolverSession\" parameter");
    }

    // Initialize fields
    this.resolverSession = resolverSession;
    this.graphTypeURI = graphTypeURI;
    this.graphType = graphType;
    this.rdfType = rdfType;
    this.rdfsLiteral = rdfsLiteral;
    this.mulgaraUriReference = mulgaraUriReference;
  }

  //
  // Methods implementing Resolver
  //

  /**
   * Create a graph for node types.
   *
   * @param graph  {@inheritDoc}.
   * @param graphType  {@inheritDoc}.  This must be the URI for NodeType graphs.
   */
  public void createModel(long graph, URI graphType) throws ResolverException {

    if (logger.isDebugEnabled()) logger.debug("Create type graph " + graph);
    if (!this.graphTypeURI.equals(graphType)) {
      throw new ResolverException("Wrong graph type provided as a Node Type graph");
    }
  }


  /**
   * Expose a callback object for enlistment by a transaction manager.
   * Uses a dumy xa resource for the moment, but may need to create a fully
   * functional xa resource which is mapped to a session.
   *
   * @return an {@link XAResource} that can be used by a transaction manager to
   *   coordinate this resolver's participation in a distributed transaction.
   *   For now this is a {@link DummyXAResource} with a 10 second transaction timeout
   * @see javax.resource.spi.ManagedConnection#getXAResource
   */
  public XAResource getXAResource()
  {
    return new DummyXAResource(
      10  // seconds before transaction timeout
    );
  }


  /**
   * Insert or delete RDF statements in an existing graph.
   * This is illegal for this graph type.
   *
   * @param graph  the local node identifying an existing graph
   * @param statements  the {@link Statements} to insert into the
   *   <var>graph</var>
   * @param occurs  whether to assert the <var>statements</var>, or (if
   *   <code>false</code>) to deny it
   * @throws ResolverException if the <var>statements</var> can't be
   *   added to the <var>graph</var>
   */
  public void modifyModel(long graph, Statements statements, boolean occurs)
    throws ResolverException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Modify Node Type graph " + graph);
    }

    throw new ResolverException("Node Type graphs are read only");
  }


  /**
   * Remove the cached graph containing the contents of a URL.
   */
  public void removeModel(long graph) throws ResolverException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Remove Node Type graph " + graph);
    }
  }

  /**
   * Resolve a constraint against an RDF/XML document.
   *
   * Resolution is by filtration of a URL stream, and thus very slow.
   */
  public Resolution resolve(Constraint constraint) throws QueryException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Resolve " + constraint);
    }

    // Validate "constraint" parameter
    if (constraint == null) {
      throw new IllegalArgumentException("Null \"constraint\" parameter");
    }
    if (!(constraint.getModel() instanceof LocalNode)) {
      if (logger.isDebugEnabled()) logger.warn("Ignoring solutions for non-local graph " + constraint);
      return new EmptyResolution(constraint, false);
    }

    /*
    // Convert the constraint's graph to a URI reference
    URIReference graphUriRef;
    long graph = ((LocalNode) constraint.getModel()).getValue();
    if (findModelType(graph) != graphType) {
      throw new QueryException("TypeGraph query was made on a graph which is not a Type Graph");
    }
    */

    try {

      long property = ((LocalNode) constraint.getElement(1)).getValue();

      // check the constraint for consistency
      if (property != rdfType ||
          constraint.getElement(2) instanceof Variable) {
        throw new QueryException("Type resolver can only be used for type constraints: " + constraint);
      }

      long type = ((LocalNode) constraint.getElement(2)).getValue();

      // Evaluate "type"
      if (logger.isDebugEnabled()) {
        logger.debug("Evaluating " + constraint.getElement(0) +
            " has type " +
            constraint.getElement(2));
      }

      ConstraintElement node = constraint.getElement(0);
      assert node != null;

      Tuples tuples;

      if (node instanceof Variable) {

        // select the type being extracted from the string pool
        if (type == rdfsLiteral) {
          // literals currently fall into 2 categories: typed and untyped
          // extract each type into a tuples
          Tuples[] t = new Tuples[2];
          t[0] = resolverSession.findStringPoolType(SPObject.TypeCategory.UNTYPED_LITERAL, null);
          t[1] = resolverSession.findStringPoolType(SPObject.TypeCategory.TYPED_LITERAL, null);
          assert t[0] != null && t[1] != null;

          t[0].renameVariables(constraint);
          t[1].renameVariables(constraint);

          long resultSize;
          try {
            // Get the size of the final result.
            resultSize = t[0].getRowCount() + t[1].getRowCount();

            if (logger.isDebugEnabled()) {
              logger.debug("resolved " + t[0].getRowCount() + " untyped literal");
              logger.debug("resolved " + t[1].getRowCount() + " typed literals");
            }

            // union the 2 tuples together
            tuples = TuplesOperations.append(Arrays.asList(t));
            t[0].close();
            t[1].close();
          } catch (TuplesException te) {
            throw new QueryException("Unable to build result", te);
          }

          if (logger.isDebugEnabled()) {
            try {
              logger.debug("Appended tuples = " + tuples.getRowCount() + " (should be " +
                  resultSize + ")");
            } catch (TuplesException te) {
              logger.debug("Error getting the length of the tuples object");
            }
          }
        } else if (type == mulgaraUriReference) {
          // just need the URI type from the string pool
          tuples = resolverSession.findStringPoolType(SPObject.TypeCategory.URI, null);
          assert tuples != null;
          tuples.renameVariables(constraint);
        } else {

          throw new QueryException("Type resolver can only find literals and URI References: " + constraint);
        }

        if (logger.isDebugEnabled()) {
          logger.debug("Evaluated " + constraint.getElement(0) +
              " has type " + constraint.getElement(2) + ": " + tuples);
        }

        return new TuplesWrapperResolution(tuples, constraint);
      } else {    // if (node instanceof Variable)
        // node must therefore be an instanceof LocalNode
        // we can shortcut the process here
        assert node instanceof LocalNode;
        LocalNode n = (LocalNode)node;

        SPObject spo = resolverSession.findStringPoolObject(n.getValue());

        if (spo == null) {
          tuples = TuplesOperations.empty();
        } else {

          if (type == rdfsLiteral) {
            if (
                spo.getTypeCategory() == SPObject.TypeCategory.UNTYPED_LITERAL ||
                spo.getTypeCategory() == SPObject.TypeCategory.TYPED_LITERAL
            ) {
              tuples = TuplesOperations.unconstrained();
            } else {
              tuples = TuplesOperations.empty();
            }

          } else if (type == mulgaraUriReference) {

            if (spo.getTypeCategory() == SPObject.TypeCategory.URI) {
              tuples = TuplesOperations.unconstrained();
            } else {
              tuples = TuplesOperations.empty();
            }

          } else {

            throw new QueryException("Type resolver can not find blank nodes: " + constraint);
          }
        }

      }

      // convert the tuples to a resolution
      return new TuplesWrapperResolution(tuples, constraint);

    } catch (StringPoolException e) {
      throw new QueryException("Couldn't query constraint", e);
    }

  }


  /**
   * Close all sessions and factories used by this resolver.
   */
  public void close() {
    // no-op
  }

  public void abort() {}
}
