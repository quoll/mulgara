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
 *   XAResource access copyright 2007 The Topaz Foundation.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.server.rmi;

// Java 2 standard packages
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.Unreferenced;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.MimeType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jrdf.graph.Triple;
import org.mulgara.query.Answer;
import org.mulgara.query.ArrayAnswer;
import org.mulgara.query.AskQuery;
import org.mulgara.query.ConstructQuery;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.rules.InitializerException;
import org.mulgara.rules.Rules;
import org.mulgara.rules.RulesRef;
import org.mulgara.server.Session;


/**
 * Wrapper around a {@link Session} to make it look like a
 * {@link RemoteSession}.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @created 2002-01-03
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class SessionWrapperRemoteSession implements RemoteSession, Unreferenced  {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(SessionWrapperRemoteSession.class.getName());

  /** The wrapped {@link Session} */
  private final Session session;

  //
  // Constructor
  //

  /**
   * @param session the wrapped session
   * @throws IllegalArgumentException if <var>session</var> is <code>null</code>
   */
  protected SessionWrapperRemoteSession(Session session) {

    // Validate "session" parameter
    if (session == null) throw new IllegalArgumentException("Null \"session\" parameter");

    // Initialize fields
    this.session = session;
  }

  /**
   * Sets the Graph attribute of the SessionWrapperRemoteSession object
   *
   * @param destinationUri The new Graph value
   * @param sourceUri The new Graph value
   * @return RETURNED VALUE TO DO
   * @throws QueryException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public long setModel(URI destinationUri, URI sourceUri) throws QueryException, RemoteException {
    try {
      return session.setModel(destinationUri, sourceUri);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Define the contents of a model via an inputstream.
   *
   * @param inputStream a remote inputstream
   * @param destinationUri the {@link URI} of the model to be redefined
   * @param sourceUri the new content for the model
   * @return The number of statements inserted into the model
   * @throws QueryException if the model can't be modified
   */
  public long setModel(InputStream inputStream, URI destinationUri, URI sourceUri, MimeType contentType) throws QueryException {
    try {
      return session.setModel(inputStream, destinationUri, sourceUri, contentType);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }


  /**
   * Sets the AutoCommit attribute of the SessionWrapperRemoteSession object
   *
   * @param autoCommit The new AutoCommit value
   * @throws QueryException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public void setAutoCommit(boolean autoCommit) throws QueryException, RemoteException {
    try {
      session.setAutoCommit(autoCommit);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  //
  // Methods implementing the RemoteSession interface
  //

  public void insert(URI modelURI, Set<? extends Triple> statements) throws QueryException, RemoteException {
    try {
      session.insert(modelURI, statements);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  public void insert(URI modelURI, Query query) throws QueryException, RemoteException {
    try {
      session.insert(modelURI, query);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  public void delete(URI modelURI, Set<? extends Triple> statements) throws QueryException, RemoteException {
    try {
      session.delete(modelURI, statements);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  public void delete(URI modelURI, Query query) throws QueryException, RemoteException {
    try {
      session.delete(modelURI, query);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Backup all the data on the specified server. The database is not changed by
   * this method.
   *
   * @param destinationURI The URI of the file to backup into.
   * @throws QueryException if the backup cannot be completed.
   */
  public void backup(URI destinationURI) throws QueryException, RemoteException {
    try {
      session.backup(destinationURI);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Backup all the data on the specified server to an output stream.
   * The database is not changed by this method.
   *
   * @param outputStream The stream to receive the contents
   * @throws QueryException if the backup cannot be completed.
   */
  public void backup(OutputStream outputStream) throws QueryException, RemoteException {
    try {
      session.backup(outputStream);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }
  
  /**
   * Export the data in the specified graph. The database is not changed by this method.
   * 
   * @param graphURI The URI of the graph to export.
   * @param destinationURI The URI of the file to export into.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, URI destinationURI) throws QueryException, RemoteException {
    try {
      session.export(graphURI, destinationURI);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }
  
  /**
   * Export the data in the specified graph using predefined namespace prefixes.
   * The database is not changed by this method.
   * 
   * @param graphURI The URI of the graph to export.
   * @param destinationURI The URI of the file to export into.
   * @param prefixes An optional mapping for pre-populating the RDF/XML namespace prefixes.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, URI destinationURI, Map<String,URI> prefixes) throws QueryException, RemoteException {
    try {
      session.export(graphURI, destinationURI, prefixes);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }
  
  /**
   * Export the data in the specified graph to an output stream.
   * The database is not changed by this method.
   * 
   * @param graphURI The URI of the server or model to export.
   * @param outputStream The stream to receive the contents
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, OutputStream outputStream, MimeType contentType) throws QueryException, RemoteException {
    try {
      session.export(graphURI, outputStream, contentType);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Export the data in the specified graph to an output stream using predefined namespace prefixes.
   * The database is not changed by this method.
   * 
   * @param graphURI The URI of the server or model to export.
   * @param outputStream The stream to receive the contents
   * @param prefixes An optional mapping for pre-populating the RDF/XML namespace prefixes.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, OutputStream outputStream, Map<String,URI> prefixes, MimeType contentType) throws QueryException, RemoteException {
    try {
      session.export(graphURI, outputStream, prefixes, contentType);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Restore all the data on the server. If the database is not
   * currently empty then the current contents of the database will be replaced
   * with the content of the backup file when this method returns.
   *
   * @param sourceURI The URI of the backup file to restore from.
   * @throws QueryException if the restore cannot be completed.
   */
  public void restore(URI sourceURI) throws QueryException, RemoteException {
    try {
      session.restore(sourceURI);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Restore all the data on the server. If the database is not
   * currently empty then the current contents of the database will be replaced
   * with the content of the backup file when this method returns.
   *
   * @param inputStream a client supplied inputStream to obtain the restore
   *        content from. If null assume the sourceURI has been supplied.
   * @param sourceURI The URI of the backup file to restore from.
   * @throws QueryException if the restore cannot be completed.
   */
  public void restore(InputStream inputStream, URI sourceURI) throws QueryException, RemoteException {
    try {
      session.restore(inputStream, sourceURI);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }


  /**
   * Adds a new graph to the SystemGraph set.
   *
   * @param graphURI The URI of the graph to create.
   * @param graphTypeURI The URI of the type for the new graph.
   * @throws QueryException Unable to create the new graph.
   * @throws RemoteException Network error attempting to create the new graph.
   */
  public void createModel(URI graphURI, URI graphTypeURI) throws QueryException, RemoteException {
    try {
      session.createModel(graphURI, graphTypeURI);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Remove a graph and all its statements.
   *
   * @param uri The URI of the graph.
   * @throws QueryException Unable to remove the graph.
   * @throws RemoteException Network error attempting to remove the new graph.
   */
  public void removeModel(URI uri) throws QueryException, RemoteException {
    try {
      session.removeModel(uri);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Tests if a graph exists.
   * @param uri the URI of the graph.
   * @see org.mulgara.server.rmi.RemoteSession#modelExists(java.net.URI)
   */
  public boolean modelExists(URI uri) throws QueryException, RemoteException {
    try {
      return session.modelExists(uri);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Commits a transaction on this session.
   * NOTE: This is not for general use. Use the transaction API.
   *
   * @throws QueryException Unable to commit the transaction.
   * @throws RemoteException There was a network error.
   */
  public void commit() throws QueryException, RemoteException {
    try {
      session.commit();
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Rolls back a transaction on this session.
   * NOTE: This is not for general use. Use the transaction API.
   *
   * @throws QueryException Unable to roll back the transaction.
   * @throws RemoteException There was a network error.
   */
  public void rollback() throws QueryException, RemoteException {
    try {
      session.rollback();
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Queries the local server and returns a remote reference to an Answer.
   *
   * @param query The query to perform.
   * @return A remote reference to an Answer.
   * @throws QueryException The query caused an exception.
   * @throws RemoteException Thrown when there is a network error.
   */
  public RemoteAnswer query(Query query) throws QueryException, RemoteException {
    return convertToRemoteAnswer(session.query(query));
  }

  /**
   * Queries the local server and returns the boolean result.
   *
   * @param query The query to perform.
   * @return <code>true</code> if the query returns a non-empty result.
   * @throws QueryException The query caused an exception.
   * @throws RemoteException Thrown when there is a network error.
   */
  public boolean query(AskQuery query) throws QueryException, RemoteException {
    try {
      return session.query(query);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
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
    return convertToRemoteAnswer(session.query(query));
  }

  /**
   * Queries a local server for a list of queries.  Wraps the resulting answers in remote
   * objects before returning them.
   *
   * @param queries A List of Query objects to be executed in order.
   * @return A list of remote references to Answer objects.  This list gets fully marshalled for returning.
   * @throws QueryException There was an exception on one of the queries, or a query returned a non-Answer.
   * @throws RemoteException Thrown when there is a network error.
   */
  public List<Object> query(List<Query> queries) throws QueryException, RemoteException {

    try {
      List<Answer> localAnswers = session.query(queries);
      List<Object> servedAnswers = new ArrayList<Object>(localAnswers.size());

      Iterator<Answer> i = localAnswers.iterator();
      while (i.hasNext()) {
        Object servedAnswer = null;
        Answer ans = i.next();
        try {
          if (ans.getRowExpectedCount() <= RemoteAnswer.MARSHALL_SIZE_LIMIT) {
            // don't need to wrap this in an
            // AnswerWrapperRemoteAnswerSerialised as the other end can handle
            // any kind of object as it comes out of the list
            servedAnswer = new ArrayAnswer(ans);
          } else {
            servedAnswer = new AnswerWrapperRemoteAnswer(ans);
          }
          ans.close();
        } catch (TuplesException e) {
          throw new QueryException("Error getting information for answer", e);
        }
        servedAnswers.add(servedAnswer);
      }

      return servedAnswers;
    }
    catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Extract {@link Rules} from the data found in a model.
   *
   * @param ruleModel The URI of the model with the rule structure.
   * @param baseModel The graph expression with the base data to read.
   * @param destModel The URI of the model to receive the entailed data.
   * @return The extracted rule structure.
   * @throws InitializerException If there was a problem accessing the rule loading module.
   * @throws QueryException If there was a problem loading the rule structure.
   */
  public RulesRef buildRules(URI ruleModel, GraphExpression baseModel, URI destModel) throws QueryException, org.mulgara.rules.InitializerException, RemoteException {
    RulesRef r = null;
    try {
      r = session.buildRules(ruleModel, baseModel, destModel);
    } catch (QueryException qe) {
      throw qe;
    } catch (org.mulgara.rules.InitializerException ie) {
      throw ie;
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
    return r;
  }

  /**
   * Rules a set of {@link Rules} on its defined model.
   *
   * @param rules The rules to be run.
   */ 
  public void applyRules(RulesRef rules) throws QueryException, RemoteException {
    try {
      session.applyRules(rules);
    } catch (QueryException re) {
      throw re;
    } catch (Throwable t) {
      throw new QueryException(t.toString(), t);
    }
  }

  /**
   * METHOD TO DO
   *
   * @throws RemoteException EXCEPTION TO DO
   */
  public void close() throws QueryException, RemoteException {

    try {
      session.close();
    }
    catch (Throwable t) {
      throw convertToQueryException(t);
    }

  }

  /**
   * METHOD TO DO
   *
   * @param securityDomain PARAMETER TO DO
   * @param username PARAMETER TO DO
   * @param password PARAMETER TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public void login(URI securityDomain, String username,
      char[] password) throws RemoteException {

    session.login(securityDomain, username, password);
  }


  public RemoteXAResource getXAResource() throws QueryException, RemoteException {
    try {
      return new XAResourceWrapperRemoteXAResource(session.getXAResource());
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  public RemoteXAResource getReadOnlyXAResource() throws QueryException, RemoteException {
    try {
      return new XAResourceWrapperRemoteXAResource(session.getReadOnlyXAResource());
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  public void setIdleTimeout(long millis) throws QueryException, RemoteException {
    try {
      session.setIdleTimeout(millis);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  public void setTransactionTimeout(long millis) throws QueryException, RemoteException {
    try {
      session.setTransactionTimeout(millis);
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  public boolean ping() throws QueryException, RemoteException {
    try {
      return session.ping();
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  public void unreferenced() {
    if (logger.isDebugEnabled()) logger.debug("Closing unreferenced remote session " + session);

    try {
      close();
    } catch (Exception e) {
      if (logger.isEnabledFor(Level.WARN)) logger.warn("Error closing unreferenced session " + session, e);
    }
  }

  // Construct an exception chain that will pass over RMI.
  protected Throwable mapThrowable(Throwable t) {
    Throwable cause = t.getCause();
    Throwable mappedCause = cause != null ? mapThrowable(cause) : null;
    Class<? extends Throwable> tClass = t.getClass();
    String tClassName = tClass.getName();

    if (
        t instanceof QueryException || (
        t instanceof Error ||
        t instanceof RuntimeException
        ) && tClassName.startsWith("java.")
        ) {
      // This exception can pass over RMI - but maybe not the cause.

      // Check if the cause has been reinstantiated.
      if (cause == mappedCause) {
        // There has been no change to the cause chain so just return this
        // Throwable unchanged.
        return t;
      }

      // TODO use reflection to instantiate a Throwable of the same class.
      // for now we just fall through and construct a QueryException.
    }

    String message = t.getMessage();
    if (!(t instanceof QueryException)) {
      // Prepend the class name to the message
      message = tClassName + ": " + message;
    }

    QueryException qe = new QueryException(message, mappedCause);
    qe.setStackTrace(t.getStackTrace());
    return qe;
  }

  /**
   * Converts an Answer to a RemoteAnswer. Closure of the original Answer is handled.
   * @param ans The Answer to convert.
   * @return A new RemoteAnswer containing the same data as the original Answer. This
   *         needs to be closed when it is finished with.
   * @throws QueryException Accessing the data caused an exception.
   * @throws RemoteException Thrown when there is a network error.
   */
  private RemoteAnswer convertToRemoteAnswer(Answer ans) throws QueryException, RemoteException {
    try {
      try {
        if (ans.getRowExpectedCount() <= RemoteAnswer.MARSHALL_SIZE_LIMIT) {
          RemoteAnswer serialAnswer = new AnswerWrapperRemoteAnswerSerialised(new ArrayAnswer(ans));
          ans.close();
          return serialAnswer;
        } else {
          return new AnswerWrapperRemoteAnswer(ans);
        }
      } catch (TuplesException e) {
        throw new QueryException("Error getting information for answer", e);
      }
    } catch (Throwable t) {
      throw convertToQueryException(t);
    }
  }

  /**
   * Return t if it is already a QueryException or wrap it as one.
   *
   * @return t if it is already a QueryException or wrap it as one.
   */
  private QueryException convertToQueryException(Throwable t) {
    t = mapThrowable(t);
    if (t instanceof QueryException)return (QueryException) t;
    return new QueryException(t.toString(), t);
  }
}
