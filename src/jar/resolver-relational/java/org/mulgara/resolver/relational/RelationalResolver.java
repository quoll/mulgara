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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.relational;

// Java 2 standard packages
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.URIReference;
import org.jrdf.graph.Node;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.LocalNode;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.relational.d2rq.Definition;
import org.mulgara.resolver.spi.DummyXAResource;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.EmptyResolution;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;


/**
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RelationalResolver implements Resolver {

  /** Logger */
  private static final Logger logger = Logger.getLogger(RelationalResolver.class);

  /** System model's type URI */
  private URI systemModelTypeURI;

  /** URI representing the file system model's type */
  private URI modelTypeURI;

  /** Node ID for the rdf:type node */
  private long rdfType;

  /** The system model's resolver */
  private Resolver systemResolver;

  /** Resolver session for node resolution */
  private ResolverSession resolverSession;


  /**
   * Constructor.
   *
   * @param resolverSession The session in which the resolver exists
   * @param systemResolver The resolver used for the system model
   * @param rdfType The rdf:type predicate
   * @param systemModel The system model node id
   * @param systemModelType The system model's type node id
   * @param modelTypeURI The URI representing the file system's model type
   *
   * @throws ResolverFactoryException
   */
  RelationalResolver(ResolverSession resolverSession, Resolver systemResolver,
                     long rdfType, long systemModel, long systemModelType,
                     URI modelTypeURI, long relationalTypeNode)
      throws ResolverFactoryException {

    if (modelTypeURI == null) {
      throw new IllegalArgumentException("Graph type can't be null");
    }

    // Store the global variable values
    this.systemResolver = systemResolver;
    this.modelTypeURI = modelTypeURI;
    this.rdfType = rdfType;
    this.resolverSession = resolverSession;
    try {
      Node systemModelTypeNode = resolverSession.globalize(systemModelType);
      if (systemModelTypeNode instanceof URIReference) {
        this.systemModelTypeURI = ((URIReference)systemModelTypeNode).getURI();
      } else {
        throw new ResolverFactoryException("SystemModelType is not a URI: " + systemModelTypeNode);
      }
    } catch (GlobalizeException eg) {
      throw new ResolverFactoryException("Error globalizing SystemModelTypeURI", eg);
    }
  }

  public XAResource getXAResource() {
    return new DummyXAResource(10);
  }

  /**
   * model of type modelTypeURI is already created by wrapper, so we just need to create the 
   * definition model.
   */
  public void createModel(long model, URI modelTypeURI) throws
      ResolverException, LocalizeException {

    if (logger.isDebugEnabled()) {
      logger.debug("Create Relational model " + model + " of type " + modelTypeURI);
    }

    // Validate the "modelTypeURI" parameter
    if (!modelTypeURI.equals(this.modelTypeURI)) {
      throw new ResolverException(
          "Can't create " + model + " of type " + modelTypeURI +
          ", which was never registered by " + getClass());
    }

    // Obtain the node representing the ?def URI of the model
    URIReference defNode = new URIReferenceImpl(definitionURI(model));

    long defModel = 0;

    try {
      // Localise the definition node
      defModel = resolverSession.localizePersistent(defNode);
    } catch (LocalizeException localiseException) {
      throw new ResolverException("Error localizing relational definition model", localiseException);
    }

    systemResolver.createModel(defModel, systemModelTypeURI);
  }

  /**
   * Remove the model.
   */
  public void removeModel(long model) throws ResolverException {
    if (logger.isDebugEnabled()) {
      logger.debug("Remove Relational model " + model);
    }

    // Container for the definition model node id
    long defModel = 0;

    try {
      // Obtain the node id of the definition model
      defModel = resolverSession.lookupPersistent(new URIReferenceImpl(definitionURI(model)));
    } catch (LocalizeException localiseException) {
      throw new ResolverException("Error localizing uri when removing " +
                                  "file system definition model",
                                  localiseException);
    }

    // Remove the definition model
    systemResolver.removeModel(defModel);
  }

  /**
   * We do not support modification of the underlying relational model.
   * Definition model can be modified directly.
   *
   * @throws ResolverException
   */
  public void modifyModel(long model, Statements statements, boolean occurs)
      throws ResolverException {

    throw new ResolverException("Relational models are read-only");
  }

  /**
   * Resolve a constraint against a model on the Java heap.
   *
   * @param constraint The constraint to resolve against the model
   *
   * @return The resolution of our constraint against the model
   *
   * @throws QueryException
   */
  public Resolution resolve(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled()) {
      logger.debug("!! Resolve " + constraint);
    }

    if (!(constraint instanceof RelationalConstraint)) {
      if (constraint.getModel() instanceof Variable) return new EmptyResolution(constraint, false);
      throw new QueryException("Relational Resolver unable to resolve constraint: " + constraint);
    }
    
    // Obtain the model of the constraint
    ConstraintElement modelElem = constraint.getModel();

    // Verify our model node is a local node
    if (!(modelElem instanceof LocalNode)) {
      if (logger.isDebugEnabled()) logger.debug("Ignoring non-local model in constraint " + constraint);
      return new EmptyResolution(constraint, false);
    }

    // Get the model id for the node
    long model = ((LocalNode)modelElem).getValue();

    // Container for our definition model node id
    long defModel = 0;

    try {
      // Obtain the definition mode node id
      defModel = resolverSession.lookupPersistent(new URIReferenceImpl(
          definitionURI(model)));
    } catch (LocalizeException localiseException) {
      throw new QueryException("Graph " + model +
                               " did not exist in Relational" +
                               " expansion", localiseException);
    } catch (ResolverException resolverException) {
      throw new QueryException("Failed to find Relational definition node id",
                               resolverException.getCause());
    }

    return resolveRelationalConstraint(defModel, (RelationalConstraint)constraint);
  }

  private Resolution resolveRelationalConstraint(long defModel, RelationalConstraint constraint) 
      throws QueryException {
    Definition defn;
    defn = new Definition(systemResolver, resolverSession, rdfType, defModel);

    try {
      RelationalResolution r = new RelationalResolution(constraint, defn, resolverSession);
      return r;
    } catch (TuplesException et) {
      throw new QueryException("Error creating resolution", et);
    }
  }

  public static String parseTableFromColumn(String column) {
    String result = column.substring(0, column.indexOf('.'));

    return result;
  }


  public static Set<String> extractTablesFromJoin(String join) {
    Pattern p = Pattern.compile("\\w+\\.\\w+");
    Matcher m = p.matcher(join);
    Set<String> result =  new HashSet<String>();
    while (m.find()) {
      result.add(parseTableFromColumn(m.group()));
    }

    return result;
  }


  private URI definitionURI(long model) throws ResolverException {
    try {
      Node modelNode = resolverSession.globalize(model);

      if (modelNode instanceof URIReference) {
        return definitionURI(((URIReference)modelNode).getURI());
      } else {
        throw new ResolverException("model is not URI: " + modelNode);
      }
    } catch (GlobalizeException eg) {
      throw new ResolverException("Failed to globalize model", eg);
    }
  }

  private URI definitionURI(URI modelURI) throws ResolverException {
    try {
      URI defURI = new URI(modelURI.getScheme(),
                           modelURI.getAuthority(),
                           modelURI.getPath(),
                           "def",
                           modelURI.getFragment());

      return defURI;
    } catch (URISyntaxException eu) {
      throw new ResolverException("Invalid URI", eu);
    }
  }

  public void abort() {
    // We need to clear the JDBC connections here, but we don't have a handle on
    // the Resolutions.
  }
}
