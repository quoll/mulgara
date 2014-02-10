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

package org.mulgara.server.rmi;

// Java 2 standard packages
import java.net.URI;
import java.rmi.*;
import java.util.*;

// JRDF
import org.jrdf.graph.*;

// Locally written packages
import org.mulgara.jrdf.*;
import org.mulgara.query.*;
import org.mulgara.server.JRDFSession;

/**
 * Wrapper around a {@link LocalJRDFSession} to make it look like a
 * {@link RemoteJRDFSession}.
 *
 * @author Andrew Newman
 *
 * @created 2004-11-02
 *
 * @version $Revision: 1.14 $
 *
 * @modified $Date: 2005/01/28 00:30:36 $ by $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class JRDFSessionWrapperRemoteJRDFSession extends SessionWrapperRemoteSession
    implements RemoteJRDFSession {

  /**
   * The wrapped {@link JRDFSession}
   */
  private final LocalJRDFSession jrdfSession;

  /**
   * Contains a reference of in memory blank nodes to internal node ids.  Reset
   * every transaction.
   */
  private HashMap<BlankNode,BlankNode> bNodeMap = new HashMap<BlankNode,BlankNode>();
//  private HashMap<org.jrdf.graph.mem.BlankNodeImpl,BlankNode> bNodeMap = new HashMap<org.jrdf.graph.mem.BlankNodeImpl,BlankNode>();

  //
  // Constructor
  //

  JRDFSessionWrapperRemoteJRDFSession(LocalJRDFSession session) {

    super(session);

    // Initialize fields
    this.jrdfSession = session;
  }

  public void commit() throws QueryException, RemoteException {
    // Clear node map.
    bNodeMap.clear();
    super.commit();
  }

  public void rollback() throws QueryException, RemoteException {
    // Clean node map.
    bNodeMap.clear();
    super.rollback();
  }

  public void setAutoCommit(boolean autoCommit) throws QueryException,
      RemoteException {
    // Turning autocommit on resets map.
    if (autoCommit) {
      bNodeMap.clear();
    }
    super.setAutoCommit(autoCommit);
  }

  public RemoteAnswer find(URI modelURI, SubjectNode subject,
      PredicateNode predicate, ObjectNode object)
      throws GraphException, RemoteException {

    try {
      JRDFGraph graph = new JRDFGraph((JRDFSession) jrdfSession, modelURI);
      SubjectNode subjectNode = subject;
      PredicateNode predicateNode = predicate;
      ObjectNode objectNode = object;
      if (subjectNode instanceof org.jrdf.graph.mem.BlankNodeImpl) {
        subjectNode = createBlankNode(
            ((org.jrdf.graph.mem.BlankNodeImpl) subjectNode),
            graph.getElementFactory());
      }
      if (objectNode instanceof org.jrdf.graph.mem.BlankNodeImpl) {
        objectNode = createBlankNode(
            ((org.jrdf.graph.mem.BlankNodeImpl) objectNode),
            graph.getElementFactory());
      }

      // Wrap results from regular find in a wrapper to convert blank nodes.
      Answer ans = new BlankNodeWrapperAnswer(jrdfSession.find(modelURI,
          subjectNode, predicateNode, objectNode), bNodeMap);

      try {
        if (ans.getRowExpectedCount() <= RemoteAnswer.MARSHALL_SIZE_LIMIT) {
          RemoteAnswer serialAnswer = new AnswerWrapperRemoteAnswerSerialised(new
              ArrayAnswer(ans));
          ans.close();
          return serialAnswer;
        }
        else {
          return new AnswerWrapperRemoteAnswer(ans);
        }
      }
      catch (TuplesException e) {
        throw new QueryException("Error getting information for answer", e);
      }
    }
    catch (Throwable t) {
      throw convertToGraphException(t);
    }
  }

  public boolean contains(URI modelURI, SubjectNode subject,
      PredicateNode predicate, ObjectNode object)
      throws GraphException, RemoteException {
    try {

      JRDFGraph graph = new JRDFGraph((JRDFSession) jrdfSession, modelURI);
      SubjectNode subjectNode = subject;
      PredicateNode predicateNode = predicate;
      ObjectNode objectNode = object;
      if (subjectNode instanceof org.jrdf.graph.mem.BlankNodeImpl) {
        subjectNode = createBlankNode(
            ((org.jrdf.graph.mem.BlankNodeImpl) subjectNode),
            graph.getElementFactory());
      }
      if (objectNode instanceof org.jrdf.graph.mem.BlankNodeImpl) {
        objectNode = createBlankNode(
            ((org.jrdf.graph.mem.BlankNodeImpl) objectNode),
            graph.getElementFactory());
      }

      return jrdfSession.contains(modelURI, subjectNode, predicateNode,
          objectNode);
    }
    catch (Throwable t) {
      throw convertToGraphException(t);
    }
  }

  public long getNumberOfTriples(URI graphURI) {
    try {
      return jrdfSession.getNumberOfTriples(graphURI);
    }
    catch (Throwable t) {
      return -1;
    }
  }

  /**
   * Perform a query locally and return an Answer object that can be exported
   * over a network.
   *
   * @param query The query to perform.
   * @return A wrapper object that holds an answer, but implements the RemoteAnswer interface.
   *         This object can be exported across a network.
   * @throws QueryException An exception was performed while executing a query.
   * @throws RemoteException Should not occur at this end of a connection.
   */
  public RemoteAnswer query(Query query) throws QueryException, RemoteException {

    // Generate the answer locally
    Answer localAnswer = jrdfSession.query(query);

    return convertToRemoteAnswer(localAnswer);
  }

  public boolean query(AskQuery query) throws QueryException, RemoteException {
    return jrdfSession.query(query);
  }

  /**
   * Queries the local server and returns a remote reference to an Answer.
   *
   * @param query The query to perform.
   * @return A remote reference to an Answer.
   * @throws QueryException The query caused an exception.
   * @throws RemoteException Thrown when there is a network error.
   */
  public RemoteAnswer query(ConstructQuery query) throws QueryException, RemoteException {
    return convertToRemoteAnswer(jrdfSession.query(query));
  }


  public void insert(URI modelURI, Set<? extends Triple> statements)
      throws QueryException, RemoteException {

    try {
      JRDFGraph graph = new JRDFGraph((JRDFSession) jrdfSession, modelURI);
      HashSet<Triple> newStatements = new HashSet<Triple>();

      // Iterator through the statements replacing JRDF memory blank nodes with
      // server specific blank nodes from the map.
      Iterator<? extends Triple> iter = statements.iterator();
      while (iter.hasNext()) {
        Triple tmpTriple = (Triple) iter.next();
        SubjectNode subjectNode = tmpTriple.getSubject();
        PredicateNode predicateNode = tmpTriple.getPredicate();
        ObjectNode objectNode = tmpTriple.getObject();

        // Replace subject blank nodes.
        if (subjectNode instanceof org.jrdf.graph.mem.BlankNodeImpl) {
          subjectNode = createBlankNode(
              ((org.jrdf.graph.mem.BlankNodeImpl) subjectNode),
              graph.getElementFactory());
        }

        // Replace object blank nodes.
        if (objectNode instanceof org.jrdf.graph.mem.BlankNodeImpl) {
          objectNode = createBlankNode(
              ((org.jrdf.graph.mem.BlankNodeImpl) objectNode),
              graph.getElementFactory());
        }
        newStatements.add(new org.mulgara.query.rdf.TripleImpl(subjectNode,
            predicateNode, objectNode));
      }
      super.insert(modelURI, newStatements);
    }
    catch (GraphException ge) {
      throw new QueryException("Failed to insert statements", ge);
    }
    catch (GraphElementFactoryException gef) {
      throw new QueryException("Failed to insert statements", gef);
    }
  }

  public void delete(URI modelURI, Set<? extends Triple> statements) throws QueryException,
      RemoteException {

    try {
      JRDFGraph graph = new JRDFGraph((JRDFSession) jrdfSession, modelURI);
      HashSet<Triple> newStatements = new HashSet<Triple>();

      // Iterator through the statements replacing JRDF memory blank nodes with
      // server specific blank nodes from the map.
      Iterator<? extends Triple> iter = statements.iterator();
      while (iter.hasNext()) {
        Triple tmpTriple = (Triple) iter.next();
        SubjectNode subjectNode = tmpTriple.getSubject();
        PredicateNode predicateNode = tmpTriple.getPredicate();
        ObjectNode objectNode = tmpTriple.getObject();

        // Replace subject blank nodes.
        if (subjectNode instanceof org.jrdf.graph.mem.BlankNodeImpl) {
          subjectNode = removeBlankNode((org.jrdf.graph.mem.BlankNodeImpl) subjectNode,
              graph.getElementFactory());
        }

        // Replace object blank nodes.
        if (objectNode instanceof org.jrdf.graph.mem.BlankNodeImpl) {
          objectNode = removeBlankNode((org.jrdf.graph.mem.BlankNodeImpl) objectNode,
              graph.getElementFactory());
        }

        // Create new statemet.
        newStatements.add(new org.mulgara.query.rdf.TripleImpl(subjectNode,
            predicateNode, objectNode));
      }
      super.delete(modelURI, newStatements);
    }
    catch (GraphException ge) {
      throw new QueryException("Failed to delete statements", ge);
    }
    catch (GraphElementFactoryException gef) {
      throw new QueryException("Failed to delete statements", gef);
    }
  }

  /**
   * Used when adding statements with blank nodes in them.  Returns one if
   * it already exists in the map or creates a new one (adding it to the map).
   *
   * @param existingNode the existing memory blank node.
   * @param factory the factory to be used to create new resources.
   * @throws GraphElementFactoryException if there was an exception creating
   *   new resources.
   * @return the blank node from the map or a new blank node.
   */
  private BlankNode createBlankNode(org.jrdf.graph.mem.BlankNodeImpl existingNode,
      GraphElementFactory factory) throws GraphElementFactoryException {
    if (bNodeMap.containsKey(existingNode)) {
      return (BlankNode) bNodeMap.get(existingNode);
    } else {
      BlankNode bNode = factory.createResource();
      bNodeMap.put(existingNode, bNode);
      return bNode;
    }
  }

  /**
   * Used when removing statements with blank nodes in them.  Returns one if
   * it already exists in the map or creates a new one (without adding it to
   * the map).
   *
   * @param existingNode the existing memory blank node.
   * @param factory the factory to be used to create new resources.
   * @throws GraphElementFactoryException if there was an exception creating
   *   new resources.
   * @return the blank node from the map or a new blank node.
   */
  private BlankNode removeBlankNode(org.jrdf.graph.mem.BlankNodeImpl existingNode,
      GraphElementFactory factory) throws GraphElementFactoryException {
    if (bNodeMap.containsKey(existingNode)) {
      BlankNode bNode = (BlankNode) bNodeMap.get(existingNode);
      return bNode;
    }
    else {
      return factory.createResource();
    }
  }

  /**
   * Return t if it is already a QueryException or wrap it as one.
   *
   * @return t if it is already a QueryException or wrap it as one.
   */
  protected GraphException convertToGraphException(Throwable t) {
    t = mapThrowable(t);
    if (t instanceof GraphException) return (GraphException) t;
    return new GraphException(t.toString(), t);
  }

  private RemoteAnswer convertToRemoteAnswer(Answer ans) throws QueryException, RemoteException {
    try {
      if (ans.getRowExpectedCount() <= RemoteAnswer.MARSHALL_SIZE_LIMIT) {
        RemoteAnswer serialAnswer = new AnswerWrapperRemoteAnswerSerialised(new ArrayAnswer(ans));
        ans.close();
        return serialAnswer;
      } else {
        return new AnswerWrapperRemoteAnswer(ans);
      }
    } catch (TuplesException e) {
      throw new QueryException("Unable to resolve answer", e);
    }
  }

}
