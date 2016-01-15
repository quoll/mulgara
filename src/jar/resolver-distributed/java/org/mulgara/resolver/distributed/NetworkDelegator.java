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

import org.mulgara.query.Answer;
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.LocalNode;
import org.mulgara.query.GraphResource;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.UnconstrainedAnswer;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.server.Session;
import org.mulgara.server.ServerInfo;
import org.mulgara.server.NonRemoteSessionException;
import org.mulgara.server.driver.SessionFactoryFinderException;
import org.mulgara.util.URIUtil;
import org.mulgara.resolver.distributed.remote.StatementSetFactory;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.transaction.xa.XAException;

/**
 * Resolve a constraint across a socket.
 *
 * @created 2007-03-20
 * @author <a href="mailto:gearon@users.sourceforge.net">Paula Gearon</a>
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class NetworkDelegator implements Delegator {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(NetworkDelegator.class.getName());

  /** The session to delegate resolutions through. */
  private final ResolverSession session;

  /** Whether the current transaction is r/w or r/o. */
  private final boolean forWrite;

  /** The transaction coordinator with which to register new XAResource's */
  private final TransactionCoordinator txCord;

  /** The session cache to use. */
  private final SessionCache sessionCache;

  /** The map of distributed sessions. */
  private Map<URI,Session> sessionMap = new HashMap<URI,Session>();


  /**
   * Constructs a delegator, using a given session.
   * @param session The session to delegate resolution through.
   * @param forWrite Whether to open this for writes or for read-only
   * @param txCord the transaction-coordinator being used
   * @param sessionCache the session cache to use
   */
  public NetworkDelegator(ResolverSession session, boolean forWrite, TransactionCoordinator txCord,
                          SessionCache sessionCache) {
    this.session = session;
    this.forWrite = forWrite;
    this.txCord = txCord;
    this.sessionCache = sessionCache;
  }


  /**
   * Resolve a given constraint down to the appropriate resolution.
   * @param localConstraint The constraint to resolve in local form.
   * @param localModel The LocalNode containing the model.
   * @throws QueryException A error occurred resolving the constraint.
   * @throws ResolverException A error occurred setting up the resolution.
   */
  public Resolution resolve(Constraint localConstraint, LocalNode localModel) throws QueryException, ResolverException {
    // globalize the model
    URIReferenceImpl modelRef = getModelRef(localModel);

    URI serverUri = getServerUri(modelRef);
    try {
      modelRef = new URIReferenceImpl(URIUtil.localizeGraphUri(modelRef.getURI()));
      logger.debug("Querying for: " + localConstraint + " in model: " + modelRef + " on server: " + serverUri);
  
      Answer ans = getServerSession(serverUri).query(globalizedQuery(localConstraint, modelRef));
      return new AnswerResolution(serverUri, session, ans, localConstraint);
    } catch (TuplesException te) {
      throw new ResolverException("Localization failed", te);
    } catch (URISyntaxException qe) {
      throw new QueryException("Bad graph URI provided for resolution", qe);
    }
  }


  /**
   * Add a set of statements to a model.
   * @param model The <code>long</code> containing the model gNode.
   * @param statements The statements to add to the model.
   * @throws ResolverException A delegator specific problem occurred adding the data.
   * @throws QueryException There was an error adding data at the remote end.
   */
  public void add(long model, Statements statements) throws ResolverException, QueryException {
    // globalize the model
    URIReferenceImpl modelRef = getModelRef(model);
    // find and verify the server
    URI serverUri = getServerUri(modelRef);
    logger.debug("Adding data to model: " + modelRef + " on server: " + serverUri);
    // convert the data to something shippable
    try {
      Set<Triple> statementSet = StatementSetFactory.newStatementSet(statements, session);
      getServerSession(serverUri).insert(modelRef.getURI(), statementSet);
    } catch (GlobalizeException ge) {
      throw new ResolverException("Insertion data can't be sent over a network", ge);
    } catch (TuplesException te) {
      throw new ResolverException("Insertion data inaccessible", te);
    }
  }


  /**
   * Remove a set of statements from a model.
   * @param model The <code>long</code> containing the model gNode.
   * @param statements The statements to remove from the model.
   * @throws ResolverException A delegator specific problem occurred removing the data.
   * @throws QueryException There was an error removing data at the remote end.
   */
  public void remove(long model, Statements statements) throws ResolverException, QueryException {
    // globalize the model
    URIReferenceImpl modelRef = getModelRef(model);
    // find and verify the server
    URI serverUri = getServerUri(modelRef);
    logger.debug("Removing data from model: " + modelRef + " on server: " + serverUri);
    // convert the data to something shippable
    try {
      Set<Triple> statementSet = StatementSetFactory.newStatementSet(statements, session);
      getServerSession(serverUri).delete(modelRef.getURI(), statementSet);
    } catch (GlobalizeException ge) {
      throw new ResolverException("Deletion data can't be sent over a network", ge);
    } catch (TuplesException te) {
      throw new ResolverException("Deletion data inaccessible", te);
    }
  }


  /**
   * Convert a local node representing a model into a URIReferenceImpl.
   * @param localModel The local node to convert.
   * @return The URIReference for the model
   * @throws ResolverException The Node was not recognized as a model.
   */
  protected URIReferenceImpl getModelRef(LocalNode localModel) throws ResolverException {
    return getModelRef(localModel.getValue());
  }


  /**
   * Convert a model gNode into a URIReferenceImpl.
   * @param modelGNode The gNode to convert.
   * @return The URIReference for the model
   * @throws ResolverException The gNode was not recognized as a model.
   */
  protected URIReferenceImpl getModelRef(long modelGNode) throws ResolverException {
    // globalize the model
    Node modelNode = globalizeNode(modelGNode);
    if (!(modelNode instanceof URIReference)) throw new ResolverException("Unexpected model type in constraint: (" + modelNode.getClass() + ")" + modelNode.toString());
    // convert the node to a URIReferenceImpl, which includes the Value interface
    return makeRefImpl((URIReference)modelNode);
  }


  /**
   * Create a query for a single constraint.
   * @param constraint The local constraint to query for.
   * @return The globalized query, looking for the single constraint.
   * @throws ResolverException There was an error globalizing the constraint elements.
   */
  @SuppressWarnings("unchecked")
  protected Query globalizedQuery(Constraint localConstraint, URIReferenceImpl model) throws ResolverException {
    // convert the constraint to network compatible form
    Constraint globalConstraint = new ConstraintImpl(
            globalizeConstraintElement(localConstraint.getElement(0)),
            globalizeConstraintElement(localConstraint.getElement(1)),
            globalizeConstraintElement(localConstraint.getElement(2)),
            model
    );

    // convert the variable set to a variable list - add types via unchecked casts
    List<Variable> variables = new ArrayList<Variable>((Set<Variable>)globalConstraint.getVariables());
    // build the new query
    return new Query(variables, new GraphResource(model.getURI()), globalConstraint, null, Collections.EMPTY_LIST, null, 0, true, new UnconstrainedAnswer());
  }


  /**
   * Convert a local node to a global value.
   * @param localNode The node to globalize.
   * @return The globalized node, either a BlankNode, a URIReference, or a Literal.
   * @throws ResolverException An error occurred while globalizing
   */
  protected Node globalizeNode(LocalNode localNode) throws ResolverException {
      return globalizeNode(localNode.getValue());
  }


  /**
   * Convert a gNode to a global node value.
   * @param gNode The node id to globalize.
   * @return The globalized node, either a BlankNode, a URIReference, or a Literal.
   * @throws QueryException An error occurred while globalizing
   */
  protected Node globalizeNode(long gNode) throws ResolverException {
    try {
      return session.globalize(gNode);
    } catch (GlobalizeException ge) {
      throw new ResolverException("Error globalizing gNode: " + gNode, ge);
    }
  }


  /**
   * Converts a constraint element from local form into global form.
   * @param localElement The constraint element in local form.
   * @throws ResolverException The constraint element could not be globalized.
   */
  protected ConstraintElement globalizeConstraintElement(ConstraintElement localElement) throws ResolverException {
    // return the element if it does not need to be converted
    if (!(localElement instanceof LocalNode) || (localElement instanceof URIReferenceImpl)) return localElement;

    // convert the reference to a Value
    return makeRefImpl((URIReference)globalizeNode((LocalNode)localElement));
  }


  /**
   * Guarantee that a URIReference is a URIReferenceImpl, wrapping in a new URIReferenceImpl if needed.
   * This method is required since URIReferenceImpl meets the Value interface when URIReference does not.
   * @param ref The reference to convert if needed.
   * @return A URIReferenceImpl matching ref.
   */
  protected URIReferenceImpl makeRefImpl(URIReference ref) {
    return (ref instanceof URIReferenceImpl) ? (URIReferenceImpl)ref : new URIReferenceImpl(ref.getURI());
  }


  /**
   * Tests if a model is really on a different server.  If the model is local then throw an exception.
   * @param modelUri The URI of the model to test.
   * @throws ResolverException Thrown when the model is on the current system.
   */
  protected static void testForLocality(URI modelUri) throws ResolverException {
    String protocol = modelUri.getScheme();
    if (!DistributedResolverFactory.getProtocols().contains(protocol)) {
      throw new IllegalStateException("Bad Protocol sent to distributed resolver.");
    }
    String host = modelUri.getHost();
    if (ServerInfo.getHostnameAliases().contains(host)) {
      // on the same machine.  Check if the server is different.
      URI serverUri = ServerInfo.getServerURI();
      if (serverUri != null && serverUri.getPath().equals(modelUri.getPath())) {
        throw new ResolverException("Attempt to resolve a local model through the distributed resolver.");
      }
    }
  }


  /**
   * Gets the URI for a server.
   * @param modelUri The URI of the model we are getting the server for.
   * @return A new URI containing just the server information.
   * @throws ResolverException The model is not on a remote server.
   */
  protected static URI getServerUri(URIReference model) throws ResolverException {
    try {
      // check if this model is really on a remote server
      URI modelUri = model.getURI();
      testForLocality(modelUri);
      // use the URI without the model fragment
      return new URI(modelUri.getScheme(), modelUri.getSchemeSpecificPart(), null);
    } catch (URISyntaxException use) {
      throw new AssertionError(use);
    }
  }

  /**
   * Retrieves a session for a given server URI, using a cached value if possible.
   * @param serverUri The URI of the server to get a session for.
   * @return a remote session on the host specified in serverUri.
   * @throws QueryException Thrown when the session cannot be created.
   */
  protected Session getServerSession(URI serverUri) throws QueryException {
    Session session = sessionMap.get(serverUri);
    return (session != null) ? session : newSession(serverUri);
  }


  /**
   * Get a new session and save in the cache.
   * @param serverUri The URI of the server to create a session for.
   * @return A new remote session.
   * @throws QueryException There was a problem creating the session.
   */
  protected Session newSession(URI serverUri) throws QueryException {
    try {
      // get a new session
      Session session = sessionCache.getSession(serverUri);
      sessionMap.put(serverUri, session);

      // get the XAResource and enlist it
      txCord.enlistResource(forWrite ? session.getXAResource() : session.getReadOnlyXAResource());

      // done
      return session;
    } catch (NonRemoteSessionException nrse) {
      throw new QueryException("State Error: non-local URI was mapped to a local session", nrse);
    } catch (SessionFactoryFinderException sffe) {
      throw new QueryException("Unable to get a session to the server", sffe);
    } catch (XAException xae) {
      throw new QueryException("Error enlisting xaresource", xae);
    }
  }

  /**
   * Return all sessions used by this delegator.
   */
  public void close() {
    for (Map.Entry<URI,Session> e : sessionMap.entrySet()) {
      sessionCache.returnSession(e.getKey(), e.getValue());
    }
  }
}

