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
 *    Migration to AbstractXAResource copyright 2008 The Topaz Foundation
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.memory;

// Java 2 standard packages
import java.io.*;
import java.net.*;
import java.util.*;
import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.xa.XAResolverSession;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.util.LongMapper;
import org.mulgara.util.MemLongMapper;

/**
 * Resolves constraints in models stored on the Java heap.
 *
 * @created 2004-04-28
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.10 $
 * @modified $Date: 2005/05/02 20:07:57 $ @maintenanceAuthor $Author: raboczi $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MemoryResolver implements SystemResolver {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(MemoryResolver.class.getName());

  /**
   * The preallocated local node representing models stored on the Java heap.
   */
  private final URI memoryModelTypeURI;

  private final long rdfType;

  private final ResolverSession resolverSession;

  /**
   * The shared set of {@link Stating}s for the factory that produced this
   * instance.
   */
  private final Set<Stating> statingSet;

  private final XAResource xares;

  private final XAResolverSession xaResolverSession;

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
   * @param statingSet  the set of statings shared between all resolvers from
   *   this particular factory
   * @throws IllegalArgumentException  {@inheritDoc}, or if <var>modelType</var>
   *   is {@link NodePool#NONE}
   */
  MemoryResolver(ResolverSession resolverSession,
                 long            rdfType,
                 URI             modelTypeURI,
                 Set<Stating>    statingSet)
      throws ResolverFactoryException {
    this(resolverSession, rdfType, modelTypeURI, statingSet, null, null);
  }

  MemoryResolver(long              rdfType,
                 URI               modelTypeURI,
                 Set<Stating>      statingSet,
                 XAResolverSession resolverSession,
                 ResolverFactory   resolverFactory)
      throws ResolverFactoryException {
    this(resolverSession, rdfType, modelTypeURI, statingSet, resolverSession,
         resolverFactory);
  }

  private MemoryResolver(ResolverSession   resolverSession,
                         long              rdfType,
                         URI               modelTypeURI,
                         Set<Stating>      statingSet,
                         XAResolverSession xaResolverSession,
                         ResolverFactory   resolverFactory)
      throws ResolverFactoryException {
    // Validate "modelType" parameter
    if (modelTypeURI == null) {
      throw new IllegalArgumentException("Graph type can't be null");
    }

    // Initialize fields
    memoryModelTypeURI = modelTypeURI;
    this.rdfType = rdfType;
    this.resolverSession = resolverSession;
    this.statingSet = statingSet;
    this.xaResolverSession = xaResolverSession;

    this.xares = (xaResolverSession != null) ?
          new MemoryXAResource(10, xaResolverSession, resolverFactory) :
          new DummyXAResource(10);
  }



  //
  // Methods implementing Resolver
  //

  public XAResource getXAResource() {
    return xares;
  }


  /**
   * Create a model by treating the <var>model</var> as the {@link URL} of an
   * RDF document and downloading it into the database.
   *
   * @param model  {@inheritDoc}.  In this case, it should be the {@link URL} of
   *   an RDF/XML document.
   * @param modelTypeURI  {@inheritDoc}.  The type must match the value of
   *   {@link #memoryModelTypeURI}.
   */
  public void createModel(long model, URI modelTypeURI) throws ResolverException, LocalizeException {
    if (logger.isDebugEnabled()) {
      logger.debug("Create memory model " + model + " of type " + modelTypeURI);
    }

    // Validate the "modelTypeURI" parameter
    if (!memoryModelTypeURI.equals(modelTypeURI)) {
      throw new ResolverException(
        "Can't create " + model + " of type " + modelTypeURI +
        ", which was never registered by " + getClass()
      );
    }
  }

  public void createSystemModel(long model, long modelType)
      throws ResolverException, LocalizeException {
    modifyModel(model,
        new SingletonStatements(model, rdfType, modelType),
        true);
  }

  public boolean modelExists(long model) throws ResolverException {
    throw new UnsupportedOperationException("Not Implemented on test class");
  }

  /**
   * @throws ResolverException always -- not yet implemented
   */
  public void writeStringPool(Writer w) throws IOException, ResolverException {
    throw new ResolverException("Backup of string pool not implemented");
  }

  /**
   * Remove the cached model containing the contents of a URL.
   */
  public void removeModel(long model) throws ResolverException {
    if (logger.isDebugEnabled()) {
      logger.debug("Remove memory model " + model);
    }

    // Remove all the statings belonging to the model
    for (Iterator<Stating> i = statingSet.iterator(); i.hasNext();) {
      if (i.next().get(3) == model) i.remove();
    }
  }

  /**
   * Insert or delete RDF statements in a model at a URL.
   */
  public void modifyModel(long model, Statements statements, boolean occurs) throws ResolverException {
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

    // Modify statement occurrences
    try {
      statements.beforeFirst();
      synchronized (statingSet) {
        while (statements.next()) {
          // Create the stating
          Stating stating = new Stating(statements.getSubject(),
                                        statements.getPredicate(),
                                        statements.getObject(),
                                        model);

          // Modify the occurrence of the stating
          if (occurs) {
            if (logger.isDebugEnabled()) logger.debug("Adding " + stating);
            statingSet.add(stating);
          } else {
            if (logger.isDebugEnabled()) logger.debug("Removing " + stating);
            statingSet.remove(stating);
          }
        }
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Modified memory model " + model + ": " + statingSet);
      }
    } catch (TuplesException e) {
      throw new ResolverException("Couldn't modify model, and may have corrupted the store", e);
    }
  }


  /**
   * Resolve a constraint against a model on the Java heap.
   */
  public Resolution resolve(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled()) {
      logger.debug("Resolve " + constraint + " against " + statingSet);
    }

    return new MemoryResolution(constraint, statingSet);
  }


  //
  // ResolverSession methods
  //


  public Node globalize(long node) throws GlobalizeException {
    return resolverSession.globalize(node);
  }


  public long lookup(Node node) throws LocalizeException {
    return resolverSession.lookup(node);
  }

  public long lookupPersistent(Node node) throws LocalizeException {
    return resolverSession.lookupPersistent(node);
  }

  public long localize(Node node) throws LocalizeException {
    return resolverSession.localize(node);
  }

  public long localizePersistent(Node node) throws LocalizeException {
    return resolverSession.localizePersistent(node);
  }
  
  public long newBlankNode() throws NodePoolException {
    return resolverSession.newBlankNode();
  }


  public Tuples findStringPoolRange(
      SPObject lowValue, boolean inclLowValue,
      SPObject highValue, boolean inclHighValue
  ) throws StringPoolException {
    throw new UnsupportedOperationException("Not Implemented on test class");
  }

  public Tuples findStringPoolType(
      SPObject.TypeCategory typeCategory, URI typeURI
  ) throws StringPoolException {
    throw new UnsupportedOperationException("Not Implemented on test class");
  }

  public SPObject findStringPoolObject(long gNode) throws StringPoolException {
    throw new UnsupportedOperationException("Not Implemented on test class");
  }

  /**
   * Retrieve the SPObject factory from the stringpool to allow for the creation
   * of new SPObjects.
   *
   * @return The factory to allow for creation of SPObjects
   */
  public SPObjectFactory getSPObjectFactory() {
    return resolverSession.getSPObjectFactory();
  }

  public SPObject findSPObject(long gNode) throws StringPoolException {
    return resolverSession.findSPObject(gNode);
  }

  public long findGNode(SPObject spObject) throws StringPoolException {
    return resolverSession.findGNode(spObject);
  }

  //
  // Internal methods
  //

  /**
   * Format a stating set.
   *
   * @param statingSet  a set of statings (<code>long[4]</code> instances),
   *   possibly <code>null</code>
   * @throws IllegalArgumentException if the <var>statingSet</var> contains any
   *   element which is not a stating (<code>long[4]</code>)
   */
  @SuppressWarnings("unused")
  private static String toString(Set<Stating> statingSet) {
    if (statingSet == null) return "null";

    StringBuffer buffer = new StringBuffer("[");
    for (Iterator<Stating> i = statingSet.iterator(); i.hasNext();) {
      buffer.append(i.next());
      buffer.append(i.hasNext() ? " " : "]");
    }

    return buffer.toString();
  }

  public void abort() {
    if (xaResolverSession != null) {
      try {
        try {
          xaResolverSession.rollback();
        } finally {
          xaResolverSession.release();
        }
      } catch (SimpleXAResourceException es) {
        throw new IllegalStateException("Error aborting resolver session", es);
      }
    }
  }

  public LongMapper getRestoreMapper() throws Exception {
    return new MemLongMapper();
  }

  /**
   * Ignored in this implementation 
   */
  public void initializeSystemNodes(long systemGraphNode, long rdfTypeNode, long systemGraphTypeNode) {
    // do nothing
  }
}
