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
 *   getModel() contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.view;

// Java 2 standard packages
import java.io.*;
import java.net.*;
import java.util.*;
import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.URIReference;
import org.jrdf.graph.Node;
import gnu.trove.TLongArrayList;
import gnu.trove.TLongObjectHashMap;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.resolver.view.SessionView;
import org.mulgara.resolver.view.ViewMarker;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.tuples.Tuples;

/**
 * Resolves constraints in models stored on the Java heap.
 *
 * @created 2004-04-28
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.10 $
 * @modified $Date: 2005/05/02 20:07:59 $ @maintenanceAuthor $Author: raboczi $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ViewResolver implements Resolver, ViewMarker
{
  /**
   * Logger.
   */
  private static final Logger logger =
    Logger.getLogger(ViewResolver.class.getName());

  private URI systemModelTypeURI;

  private URI modelTypeURI;

  private long viewNode;
  private long typeNode;
  private long exprNode;
  private long modelNode;
  private long unionNode;
  private long intersectNode;

  private final ResolverSession resolverSession;
  private Resolver systemResolver;
  private SessionView session;

  private TLongObjectHashMap<ViewDefinition> viewCache;

  //
  // Constructors
  //

  /**
   * Construct a resolver.
   *
   * @param resolverSession  the session this resolver is associated with,
   *   never <code>null</code>
   * @param rdfType  the local node representing the <code>rdf:type</code>
   *   property
   * @param modelTypeURI  the local node representing the model type managed
   *   by this class, never {@link NodePool#NONE}
   * @throws IllegalArgumentException  {@inheritDoc}, or if <var>modelType</var>
   *   is {@link NodePool#NONE}
   */
  ViewResolver(ResolverSession resolverSession, Resolver systemResolver, long rdfType, long systemModel,
      long systemModelType, URI modelTypeURI, long viewNode,
      long typeNode, long exprNode, long modelNode, long unionNode, long intersectNode)
      throws ResolverFactoryException {

    // Validate "modelType" parameter
    if (modelTypeURI == null) {
      throw new IllegalArgumentException("Graph type can't be null");
    }
    this.resolverSession = resolverSession;
    this.systemResolver = systemResolver;
    this.modelTypeURI = modelTypeURI;
    this.viewNode = viewNode;
    this.typeNode = typeNode;
    this.exprNode = exprNode;
    this.modelNode = modelNode;
    this.unionNode = unionNode;
    this.intersectNode = intersectNode;
    this.viewCache = new TLongObjectHashMap<ViewDefinition>();
    try {
      Node systemModelTypeURIRef = resolverSession.globalize(systemModelType);
      if (systemModelTypeURIRef instanceof URIReference) {
        this.systemModelTypeURI = ((URIReference)systemModelTypeURIRef).getURI();
      } else {
        throw new ResolverFactoryException("systemModelType not a URIRef " + systemModelTypeURIRef);
      }
    } catch (GlobalizeException eg) {
      throw new ResolverFactoryException("Failed to globalize SystemModel Type", eg);
    }
  }


  //
  // Methods implementing Resolver
  //

  public XAResource getXAResource() {
    return new DummyXAResource(10);
  }


  /**
   * Create a model by treating the <var>model</var> as the {@link URL} of an
   * RDF document and downloading it into the database.
   *
   * @param model  {@inheritDoc}.  In this case, it should be the {@link URL} of
   *   an RDF/XML document.
   * @param modelTypeURI  {@inheritDoc}.  The type must match the value of
   *   {@link #modelTypeURI}.
   */
  public void createModel(long model, URI modelTypeURI)
      throws ResolverException, LocalizeException {
    if (logger.isDebugEnabled()) {
      logger.debug("Create view model " + model + " of type " + modelTypeURI);
    }

    // Validate the "modelTypeURI" parameter
    if (!modelTypeURI.equals(this.modelTypeURI)) {
      throw new ResolverException(
        "Can't create " + model + " of type " + modelTypeURI +
        ", which was never registered by " + getClass());
    }

    try {
      URIReference defNode = new URIReferenceImpl(definitionURI(model));
      long defModel = resolverSession.localizePersistent(defNode);
      systemResolver.createModel(defModel, systemModelTypeURI);

    } catch (LocalizeException el) {
      throw new ResolverException("Error localizing uri", el);
    }
  }


  /**
   * @throws ResolverException always -- not yet implemented
   */
  public void writeStringPool(Writer w) throws IOException, ResolverException
  {
    throw new ResolverException("Backup of string pool not implemented");
  }


  /**
   * Remove the cached model containing the contents of a URL.
   */
  public void removeModel(long model) throws ResolverException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Remove view model " + model);
    }

    try {
      long defModel = resolverSession.lookupPersistent(new URIReferenceImpl(definitionURI(model)));
      systemResolver.removeModel(defModel);
    } catch (LocalizeException el) {
      throw new ResolverException("Error localizing uri", el);
    }
  }

  /**
   * Insert or delete RDF statements in a model at a URL.
   */
  public void modifyModel(long model, Statements statements, boolean occurs)
    throws ResolverException
  {
    if (logger.isDebugEnabled()) {
      if (occurs) {
        logger.debug("Asserting " + statements + " in " + model);
      } else {
        logger.debug("Denying " + statements + " in " + model);
      }
    }

    // Validate "statements" parameter
    if (statements == null) {
      throw new IllegalArgumentException("Null \"statements\" parameter");
    }

    try {
      long defModel = resolverSession.lookupPersistent(new URIReferenceImpl(definitionURI(model)));
      systemResolver.modifyModel(defModel, statements, occurs);
    } catch (LocalizeException el) {
      throw new ResolverException("Error localizing uri", el);
    }
  }


  /**
   * Resolve a constraint against a model on the Java heap.
   */
  public Resolution resolve(Constraint constraint) throws QueryException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Resolve " + constraint);
    }
    ConstraintElement modelElem = constraint.getModel();
    if (!(modelElem instanceof LocalNode)) {
      if (logger.isDebugEnabled()) logger.debug("Ignoring solutions for " + constraint);
      return new EmptyResolution(constraint, false);
    }

    long model = ((LocalNode)modelElem).getValue();
    try {
      long defModel = resolverSession.lookupPersistent(new URIReferenceImpl(definitionURI(model)));

      if (!viewCache.containsKey(defModel)) {
        viewCache.put(defModel, fetchView(defModel));
      }
      ViewDefinition definition = viewCache.get(defModel);

      return definition.resolve(constraint, session);
    } catch (LocalizeException el) {
      throw new QueryException("Graph " + model + " did not exist in view expansion", el);
    } catch (ResolverException er) {
      throw new QueryException("Failed to build view definition uri", er.getCause());
    }
  }

  private ViewDefinition fetchView(long defModel) throws QueryException {
    return fetchViewExpr(defModel, viewNode);
  }

  private ViewDefinition fetchViewExpr(long defModel, long modelExpr) throws QueryException {
    Tuples typeTuples = systemResolver.resolve(new ConstraintImpl(new LocalNode(modelExpr),
                                                            new LocalNode(typeNode),
                                                            new Variable("exprType"),
                                                            new LocalNode(defModel)));
    try {
      try {
        if (typeTuples.getRowCardinality() != Cursor.ONE) {
          logger.warn("Invalid expr type in view def:" + typeTuples);
          throw new QueryException("Invalid expr type in view def");
        }

        typeTuples.beforeFirst();
        typeTuples.next();
        long type = typeTuples.getColumnValue(0);
        if (type == unionNode) {
          return fetchViewUnion(defModel, modelExpr);
        } else if (type == intersectNode) {
          return fetchViewIntersection(defModel, modelExpr);
        } else {
          throw new QueryException("Unknown expr type in view def");
        }
      } finally {
        typeTuples.close();
      }
    } catch (TuplesException et) {
      throw new QueryException("Failure in view expansion", et);
    }
  }

  private ViewDefinition fetchViewUnion(long defModel, long modelExpr) throws QueryException {
    TLongArrayList models = fetchViewModels(defModel, modelExpr);
    List<ViewDefinition> exprs = fetchViewExprs(defModel, modelExpr);

    return new ViewDefinitionUnion(models, exprs);
  }

  private ViewDefinition fetchViewIntersection(long defModel, long modelExpr) throws QueryException {
    TLongArrayList models = fetchViewModels(defModel, modelExpr);
    List<ViewDefinition> exprs = fetchViewExprs(defModel, modelExpr);

    return new ViewDefinitionIntersection(models, exprs);
  }

  private TLongArrayList fetchViewModels(long defModel, long modelExpr) throws QueryException {
    try {
      TLongArrayList models = new TLongArrayList();
      Tuples modelTuples = systemResolver.resolve(new ConstraintImpl(new LocalNode(modelExpr),
                                                                     new LocalNode(modelNode),
                                                                     new Variable("modelURI"),
                                                                     new LocalNode(defModel)));
      try {
        modelTuples.beforeFirst();
        while (modelTuples.next()) {
          models.add(modelTuples.getColumnValue(0));
        }
      } finally {
        modelTuples.close();
      }

      return models;
    } catch (TuplesException et) {
      throw new QueryException("Failed to obtain models in view expansion", et);
    }
  }

  private List<ViewDefinition> fetchViewExprs(long defModel, long modelExpr) throws QueryException {
    try {
      List<ViewDefinition> exprs = new ArrayList<ViewDefinition>();
      Tuples exprTuples  = systemResolver.resolve(new ConstraintImpl(new LocalNode(modelExpr),
                                                                     new LocalNode(exprNode),
                                                                     new Variable("exprNode"),
                                                                     new LocalNode(defModel)));
      try {
        exprTuples.beforeFirst();
        while (exprTuples.next()) {
          exprs.add(fetchViewExpr(defModel, exprTuples.getColumnValue(0)));
        }
      } finally {
        exprTuples.close();
      }

      return exprs;
    } catch (TuplesException et) {
      throw new QueryException("Failed obtaining view expressions in view expansion", et);
    }
  }


  public void setSession(SessionView session) {
    this.session = session;
  }


  private URI definitionURI(long viewNode) throws ResolverException {
    try {
      Node modelNode = resolverSession.globalize(viewNode);
      if (modelNode instanceof URIReference) {
        return definitionURI(((URIReference)modelNode).getURI());
      } else {
        throw new ResolverException("viewNode not URI");
      }
    } catch (GlobalizeException eg) {
      throw new ResolverException("Failed to globalize view", eg);
    }
  }

  private URI definitionURI(URI viewURI) throws ResolverException {
    try {
      URI defURI = new URI(viewURI.getScheme(),
                           viewURI.getAuthority(),
                           viewURI.getPath(),
                           "def",
                           viewURI.getFragment());
      if ( logger.isInfoEnabled()) {
        logger.info("Creating defModel with uri = " + defURI);
      }

      return defURI;
    } catch (URISyntaxException eu) {
      throw new ResolverException("Invalid URI", eu);
    }
  }

  public void abort() {}
}
