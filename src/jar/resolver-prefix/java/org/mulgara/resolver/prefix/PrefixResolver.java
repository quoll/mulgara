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

import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.*;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.stringpool.xa.SPObjectFactoryImpl;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/**
 * Resolves constraints accessible through a session.
 *
 * @created 2005-4-19
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @version $Revision: $
 * @modified $Date: $ @maintenanceAuthor $Author: $
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class PrefixResolver implements Resolver
{
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(PrefixResolver.class.getName());

  /** The session that this resolver is associated with.  */
  private final ResolverSession resolverSession;

  /** The URI of the type describing node type models.  */
  private URI modelTypeURI;

  /** The preallocated local node representing the mulgara:prefix property. */
  private final long mulgaraPrefix;

  /** The preallocated local node representing the mulgara:stringPrefix property. */
  private final long mulgaraStringPrefix;

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
  PrefixResolver(
      ResolverSession resolverSession,
      Resolver systemResolver,
      long mulgaraPrefix,
      long mulgaraStringPrefix,
      URI modelTypeURI
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
    this.modelTypeURI = modelTypeURI;
    this.mulgaraPrefix = mulgaraPrefix;
    this.mulgaraStringPrefix = mulgaraStringPrefix;
  }

  //
  // Methods implementing Resolver
  //

  /**
   * Create a model for node types.
   *
   * @param model  {@inheritDoc}.
   * @param modelTypeUri  {@inheritDoc}.  This must be the URI for Prefix models.
   */
  public void createModel(long model, URI modelTypeUri) throws ResolverException {

    if (logger.isDebugEnabled()) {
      logger.debug("Create type model " + model);
    }

    if (!this.modelTypeURI.equals(modelTypeUri)) {
      throw new ResolverException("Wrong model type provided as a Prefix model");
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
   * Insert or delete RDF statements in an existing model.
   * This is illegal for this model type.
   *
   * @param model  the local node identifying an existing model
   * @param statements  the {@link Statements} to insert into the
   *   <var>model</var>
   * @param occurs  whether to assert the <var>statements</var>, or (if
   *   <code>false</code>) to deny it
   * @throws ResolverException if the <var>statements</var> can't be
   *   added to the <var>model</var>
   */
  public void modifyModel(long model, Statements statements, boolean occurs) throws ResolverException {
    if (logger.isDebugEnabled()) {
      logger.debug("Modify prefix model " + model);
    }

    throw new ResolverException("Prefix models are read only");
  }


  /**
   * Remove the cached model containing the contents of a URL.
   */
  public void removeModel(long model) throws ResolverException {
    if (logger.isDebugEnabled()) {
      logger.debug("Remove prefix model " + model);
    }
  }

  /**
   * Resolve a constraint against an RDF/XML document.
   *
   * Resolution is by filtration of a URL stream, and thus very slow.
   */
  public Resolution resolve(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled()) {
      logger.debug("Resolve " + constraint);
    }

    // Validate "constraint" parameter
    if (constraint == null) {
      throw new IllegalArgumentException("Null \"constraint\" parameter");
    }

    if (!(constraint.getModel() instanceof LocalNode)) {
      if (logger.isDebugEnabled()) logger.debug("Ignoring solutions for " + constraint);
      return new EmptyResolution(constraint, false);
    }

    if (
        !(constraint.getElement(1) instanceof LocalNode) ||
        !(constraint.getElement(2) instanceof LocalNode)
    ) {
      throw new QueryException("Prefix resolver can only be used for fixed prefixes: " + constraint);
    }

    try {

      long property = ((LocalNode)constraint.getElement(1)).getValue();
      LocalNode object = (LocalNode)constraint.getElement(2);
      Node prefixNode = resolverSession.globalize(object.getValue());

      // check the constraint for consistency
      if ((property != mulgaraPrefix && property != mulgaraStringPrefix) || !(prefixNode instanceof Literal || prefixNode instanceof URIReference)) {
        if (logger.isDebugEnabled()) {
          logger.debug("property = " + property +", mulgaraPrefix = " + mulgaraPrefix);
          logger.debug("element(2): " + prefixNode + " [" + prefixNode.getClass().getName() + "]");
        }
        throw new QueryException("Prefix resolver can only be used for prefix constraints: " + constraint);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Evaluating " + constraint.getElement(0) + " has prefix " + constraint.getElement(2));
      }

      ConstraintElement node = constraint.getElement(0);
      assert node != null;

      Tuples tuples;

      if (node instanceof Variable) {

        // convert the prefix into a string pool object
        SPObjectFactory spoFact = SPObjectFactoryImpl.getInstance();
        SPObject startPrefixObj = getStartObject(spoFact, prefixNode, property);
        SPObject endPrefixObj = getEndObject(spoFact, prefixNode, property);

        // get the extents of the prefix from the string pool
        tuples = resolverSession.findStringPoolRange(startPrefixObj, true, endPrefixObj, false);
        assert tuples != null;
        // rename variables away from subject, predicate and object
        tuples.renameVariables(constraint);

        long resultSize;
        try {
          // Get the size of the final result.
          resultSize = tuples.getRowCount();
        } catch (TuplesException e) {
          throw new QueryException("Unable to build result", e);
        }


        if (logger.isDebugEnabled()) {
          try {
            logger.debug("tuples size = " + tuples.getRowCount() + " (should be " + resultSize + ")");
          } catch (TuplesException e) {
            logger.debug("Error getting the length of the tuples object");
          }
        }

        return new TuplesWrapperResolution(tuples, constraint);

      } else {    // if (node instanceof Variable)
        
        // node must therefore be an instanceof LocalNode
        // we can shortcut the process here
        assert node instanceof LocalNode;
        LocalNode n = (LocalNode)node;

        // get the node out of the string pool
        SPObject spo = resolverSession.findStringPoolObject(n.getValue());

        // check that the node exists
        if (spo == null) {
          tuples = TuplesOperations.empty();
        } else {

          // see if the node starts with the required prefix

          String prefix;
          // extract the string from the literal
          if (prefixNode instanceof Literal) {
            prefix = ((Literal) prefixNode).getLexicalForm();
          } else {
            prefix = ((URIReference) prefixNode).getURI().toString();
          }
          if (spo.getLexicalForm().startsWith(prefix)) {
            tuples = TuplesOperations.unconstrained();
          } else {
            tuples = TuplesOperations.empty();
          }

        }

      }

      // convert the tuples to a resolution
      return new TuplesWrapperResolution(tuples, constraint);

    } catch (GlobalizeException ge) {
      throw new QueryException("Couldn't convert internal data into a string", ge);
    } catch (StringPoolException e) {
      throw new QueryException("Couldn't query constraint", e);
    }

  }

  /**
   * Create a string pool object to represent the beginning of a search range.
   * @param factory The string pool factory that will create the object
   * @param prefixNode The data for the object.
   * @param predType Predicate to indicate the type of data to get from the string pool.
   * @return An object representing the start of a range of data to get from the string pool.
   * @throws QueryException If the predicate indicates a URI but the prefix data is not a valid URI.
   */
  private SPObject getStartObject(SPObjectFactory factory, Node prefixNode, long predType) throws QueryException {
    if (prefixNode instanceof Literal) {
      String prefix = ((Literal) prefixNode).getLexicalForm();
      if (predType == mulgaraPrefix) {
        try {
          return factory.newSPURI(new URI(prefix));
        } catch (URISyntaxException e) {
          throw new QueryException("Bad URI prefix provided: " + prefix + " (should this be using mulgara:stringPrefix?)");
        }
      } else return factory.newSPString(prefix);
    } else {
      URI startPrefixUri = ((URIReference) prefixNode).getURI();
      if (predType == mulgaraPrefix) return factory.newSPURI(startPrefixUri);
      else return factory.newSPString(startPrefixUri.toString());
    }
  }

  /**
   * Create a string pool object to represent the end of a search range.
   * @param factory The string pool factory that will create the object
   * @param prefixNode The data for the object.
   * @param predType Predicate to indicate the type of data to get from the string pool.
   * @return An object representing the end of a range of data to get from the string pool.
   * @throws QueryException If the predicate indicates a URI but the prefix data is not a valid URI.
   */
  private SPObject getEndObject(SPObjectFactory factory, Node prefixNode, long predType) throws QueryException {
    String prefix;
    if (prefixNode instanceof Literal) {
      prefix = ((Literal) prefixNode).getLexicalForm();
    } else {
      prefix = ((URIReference) prefixNode).getURI().toString();
    }

    if (predType == mulgaraPrefix) {
      try {
        return factory.newSPURI(new URI(prefix + Character.MAX_VALUE));
      } catch (URISyntaxException e) {
        throw new QueryException("Bad URI prefix provided: " + prefix + " (should this be using mulgara:stringPrefix?)");
      }
    } else return factory.newSPString(prefix + Character.MAX_VALUE);
  }

  /**
   * Close all sessions and factories used by this resolver.
   */
  public void close() {
    // no-op
  }

  public void abort() {}
}
