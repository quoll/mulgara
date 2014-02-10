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
 *    Move to associate XAResource with Resolver Factory
 *     copyright 2008 The Topaz Foundation
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.store;

// Java 2 standard packages
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.statement.StatementStoreException;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.xa.SimpleXAResource;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.store.xa.XAResolverSession;
import org.mulgara.store.xa.XAStatementStore;
import org.mulgara.util.LongMapper;

/**
 * Resolves constraints from the Mulgara {@link StatementStore}.
 *
 * @created 2003-12-01
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.11 $
 * @modified $Date: 2005/05/06 04:07:58 $ @maintenanceAuthor $Author: amuys $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2003-2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class StatementStoreResolver implements SystemResolver {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(StatementStoreResolver.class.getName());

  /**
   * A (non) constraint that selects every statement from every model.
   *
   * This is used to select the entire contents of the store during
   * backup and restore.
   */
  public static final Constraint ALL_STATEMENTS =
    new ConstraintImpl(StatementStore.VARIABLES[0],
                       StatementStore.VARIABLES[1],
                       StatementStore.VARIABLES[2],
                       StatementStore.VARIABLES[3]);

  /**
   * The preallocated node for the <code>rdf:type</code> property.
   */
  private final long rdfType;

  private final ResolverSession resolverSession;

  /**
   * The preallocated local node identifying the type of models kept in the
   * statement store.
   */
  private final URI statementStoreModelTypeURI;

  /**
   * The XA statement store shared with all resolvers from the same factory.
   */
  private final XAStatementStore statementStore;

  private final XAResolverSession xaResolverSession;

  private final XAResource xaresource;

  @SuppressWarnings("unused")
  private boolean isSystemResolver;

  private long systemModel;


  //
  // Constructors
  //

  /**
   * Construct a {@link StatementStoreResolver}.
   */
  StatementStoreResolver(long rdfType,
                         long systemModel,
                         URI modelTypeURI,
                         XAResolverSession resolverSession,
                         XAStatementStore statementStore,
                         ResolverFactory resolverFactory)
      throws IllegalArgumentException, ResolverFactoryException
  {
    // Validate parameters
    if (modelTypeURI == null) {
      throw new IllegalArgumentException("Graph type can't be Null");
    }
    if (statementStore == null) {
      throw new IllegalArgumentException("Null 'statementStore' parameter");
    }

    // Initialize fields
    this.rdfType = rdfType;
    this.systemModel = systemModel;
    this.resolverSession = resolverSession;
    this.statementStoreModelTypeURI = modelTypeURI;
    this.statementStore = statementStore;
    this.xaResolverSession = resolverSession;
    this.isSystemResolver = true;
    this.xaresource = new StatementStoreXAResource(
        10,  // transaction timeout in seconds
        resolverSession,
        new SimpleXAResource[] { statementStore },
        resolverFactory);
  }

  StatementStoreResolver(Resolver systemResolver,
                         long rdfType,
                         long systemModel,
                         URI modelTypeURI,
                         XAResolverSession resolverSession,
                         XAStatementStore statementStore,
                         ResolverFactory resolverFactory)
      throws IllegalArgumentException, ResolverFactoryException
  {
    // Validate parameters
    if (modelTypeURI == null) {
      throw new IllegalArgumentException("Graph type can't be Null");
    }
    if (statementStore == null) {
      throw new IllegalArgumentException("Null 'statementStore' parameter");
    }

    // Initialize fields
    this.rdfType = rdfType;
    this.resolverSession = resolverSession;
    this.statementStoreModelTypeURI = modelTypeURI;
    this.statementStore = statementStore;
    this.xaResolverSession = resolverSession;
    this.isSystemResolver = false;
    this.xaresource = new StatementStoreXAResource(
        10,  // transaction timeout in seconds
        resolverSession,
        new SimpleXAResource[] { statementStore },
        resolverFactory);
  }


  //
  // Methods implementing the Resolver interface
  //

  /**
  * @return a {@link StatementStoreXAResource} with a 10 second transaction timeout
  */
  public XAResource getXAResource()
  {
    return xaresource;
  }

  /**
   * Create model in the statement store.
   */
  public void createModel(long model, URI modelTypeURI) throws ResolverException, LocalizeException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Create XA store model " + model + " of type " + modelTypeURI);
    }

    // Validate the "modelType" parameter
    if (!modelTypeURI.equals(statementStoreModelTypeURI)) {
      throw new ResolverException(
        "Can't create " + model + " of type " + modelTypeURI +
        ", which was never registered by " + getClass() + ": " +
        statementStoreModelTypeURI
      );
    }
  }


  public void createSystemModel(long model, long modelType)
      throws ResolverException, LocalizeException
  {
    modifyModel(model,
        new SingletonStatements(model, rdfType, modelType),
        true);
  }


  public void write(Writer w) throws IOException, ResolverException
  {
    Tuples tuples;
    try {
      tuples = resolve(ALL_STATEMENTS);
    } catch (QueryException e) {
      throw new ResolverException("Unable to write backup", e);
    }
    assert tuples != null;

    try {
      assert tuples.getVariables()[0] == StatementStore.VARIABLES[0];
      assert tuples.getVariables()[1] == StatementStore.VARIABLES[1];
      assert tuples.getVariables()[2] == StatementStore.VARIABLES[2];
      assert tuples.getVariables()[3] == StatementStore.VARIABLES[3];

      // Dump the triples.
      for (tuples.beforeFirst(); tuples.next(); ) {
        w.write(Long.toString(tuples.getColumnValue(0)));
        w.write(' ');
        w.write(Long.toString(tuples.getColumnValue(1)));
        w.write(' ');
        w.write(Long.toString(tuples.getColumnValue(2)));
        w.write(' ');
        w.write(Long.toString(tuples.getColumnValue(3)));
        w.write('\n');
      }
    } catch (TuplesException e) {
      throw new ResolverException("Unable to write backup", e);
    } finally {
      try {
        tuples.close();
      } catch (TuplesException e) {
        logger.warn("Unable to close tuples after backup", e);
      }
    }
  }


  public void writeStringPool(Writer w) throws IOException, ResolverException
  {
    w.write("The "+getClass()+".writeStringPool method isn't implemented");
/*
    Iterator si = sessionStringPool.iterator();

    while (si.hasNext()) {

      StringPool.Entry e = (StringPool.Entry) si.next();
      w.write(Long.toString(e.getGNode()));
      w.write(' ');
                                                                                       SPObject spObject = e.getSPObject();

      if (spObject instanceof SPURI) {                                                                                                URI uri = ((SPURI) spObject).getURI();               URI relativeURI = database.getURI().relativize(uri);

        // Be suspicious about relative URIs -- we're only expecting the
        // names of models from this server
        if (!relativeURI.isAbsolute()) {

          if ((relativeURI.getAuthority() != null) ||
              ((relativeURI.getPath() != null) &&                     (relativeURI.getPath().length() > 0)) ||                     (relativeURI.getFragment() == null)) {
            logger.warn("Unusual relative URI in backup: " + relativeURI +                      " authority=\"" + relativeURI.getAuthority() + "\"" +
                " path=\"" + relativeURI.getPath() + "\"" +
                " fragment=\"" +
                relativeURI.getFragment() + "\"");
          }

          spObject = sessionStringPool.newSPObject(relativeURI);
        }
      }

      w.write(spObject.getEncodedString());
      w.write('\n');
    }
*/

    w.write("\n");
  }


  /**
   * Remove model in the statement store.
   */
  public void removeModel(long model) throws ResolverException
  {
    // Globalize model
    URIReference modelURIReference;
    try {
      Node node = resolverSession.globalize(model);
      if (!(node instanceof URIReference)) {
        throw new ResolverException(
          "Graph " + model + " is " + node + ", not a URI reference");
      }
      modelURIReference = (URIReference) node;
    } catch (GlobalizeException e) {
      throw new ResolverException("Couldn't globalize model " + model, e);
    }
    assert modelURIReference != null;

    try {
      statementStore.removeTriples(NodePool.NONE, NodePool.NONE, NodePool.NONE, model);
    } catch (StatementStoreException se) {
      throw new ResolverException ("Failed to remove statements from model " + model, se);
    }
  }

  public boolean modelExists(long model) throws ResolverException {
    try {
      return statementStore.existsTriples(model, rdfType, NodePool.NONE,
          systemModel);
    } catch (StatementStoreException se) {
      throw new ResolverException("Failed to find model " + model, se);
    }
  }

  /**
   * Insert or delete statements in a model in the statement store.
   */
  public void modifyModel(long model, Statements statements, boolean occurs)
    throws ResolverException
  {
    // Modify the membership of the statements within the model
    try {
      statements.beforeFirst();
      while (statements.next()) {
        long subject = statements.getSubject();
        long predicate = statements.getPredicate();
        long object = statements.getObject();
        
        if (occurs) {
          // statement is asserted to be true
          if (logger.isDebugEnabled()) logger.debug("Inserting statement: [" + subject + " "
                                                + predicate + " "
                                                + object + "] in " + model);

          statementStore.addTriple(subject, predicate, object, model);
        } else {
          // statement is asserted to be false
          statementStore.removeTriples(subject, predicate, object, model);
        }
      }
    } catch (StatementStoreException e) {
      try {
        logger.warn("Failed to modifyModel: [" + resolverSession.globalize(statements.getSubject()) + " "
                                               + resolverSession.globalize(statements.getPredicate()) + " "
                                               + resolverSession.globalize(statements.getObject()) + " "
                                               + resolverSession.globalize(model) + "]", e);
      } catch (Exception eg) {
        throw new ResolverException("Failed to globalize in debug", eg);
      }
      throw new ResolverException("Couldn't make statement " + occurs + " in " + model, e);
    } catch (TuplesException e) {
      throw new ResolverException("Unable to read input statements", e);
    }
  }


  /**
   * Resolve a constraint from the statement store.
   */
  public Resolution resolve(Constraint constraint) throws QueryException
  {
    try {
      if (constraintResolvable(constraint)) {
        if (!constraint.isRepeating()) {
          if (constraint instanceof ConstraintImpl) {
            return new StatementStoreResolution(constraint, statementStore);
          } else {
            throw new QueryException("Unable to resolve constraint " + constraint + " unknown type");
          }
        } else {
          throw new QueryException("Duplicate variable found during resolution");
        }
      } else {
        return new EmptyResolution(constraint, true);
      }
    } catch (TuplesException e) {
      throw new QueryException("Couldn't resolve " + constraint, e);
    }
  }


  private boolean constraintResolvable(Constraint constraint) {
    for (int i = 0; i < 3; i++) {
      if (constraint.getElement(i) instanceof LocalNode &&
          ((LocalNode)constraint.getElement(i)).getValue() < 0) {
        return false;
      }
    }
    return true;
  }

  //
  // ResolverSession methods
  //


  public Node globalize(long node) throws GlobalizeException
  {
    return resolverSession.globalize(node);
  }


  public long lookup(Node node) throws LocalizeException
  {
    return resolverSession.lookup(node);
  }

  public long lookupPersistent(Node node) throws LocalizeException
  {
    return resolverSession.lookupPersistent(node);
  }

  public long localize(Node node) throws LocalizeException
  {
    return resolverSession.localize(node);
  }

  public long localizePersistent(Node node) throws LocalizeException
  {
    return resolverSession.localizePersistent(node);
  }

  public long newBlankNode() throws NodePoolException {
    return resolverSession.newBlankNode();
  }

  public Tuples findStringPoolRange(
      SPObject lowValue, boolean inclLowValue,
      SPObject highValue, boolean inclHighValue
  ) throws StringPoolException {
    return xaResolverSession.findStringPoolRange(lowValue, inclLowValue, highValue, inclHighValue);
  }

  public Tuples findStringPoolType(
      SPObject.TypeCategory typeCategory, URI typeURI
  ) throws StringPoolException {
    return xaResolverSession.findStringPoolType(typeCategory, typeURI);
  }

  /**
   * Finds a stringpool object based on the given node.
   *
   * @param gNode The node to find the stringppol object for
   *
   * @return The string equivalent of the given node
   *
   * @throws StringPoolException
   */
  public SPObject findStringPoolObject(long gNode) throws StringPoolException {
    return resolverSession.findStringPoolObject(gNode);
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

  /** @see org.mulgara.resolver.spi.BackupRestoreSession#getRestoreMapper() */
  public LongMapper getRestoreMapper() throws Exception {
    return resolverSession.getRestoreMapper();
  }

  //
  // Internal methods
  //

  public void abort() {
    try {
      try {
        statementStore.rollback();
      } finally {
        try {
          xaResolverSession.rollback();
        } finally {
          try {
            statementStore.release();
          } finally {
            xaResolverSession.release();
          }
        }
      }
    } catch (SimpleXAResourceException es) {
      throw new IllegalStateException("Failed to Abort store", es);
    }
  }

  /**
   * @see org.mulgara.resolver.spi.SystemResolver#initializeSystemNodes(long, long, long)
   */
  public void initializeSystemNodes(long systemGraphNode, long rdfTypeNode, long systemGraphTypeNode) throws StatementStoreException {
    statementStore.initializeSystemNodes(systemGraphNode, rdfTypeNode, systemGraphTypeNode);
  }

}
