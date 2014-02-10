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
import java.io.Serializable;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;
import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.jrdf.graph.Triple;
import org.mulgara.query.Answer;
import org.mulgara.query.AskQuery;
import org.mulgara.query.ConstructQuery;
import org.mulgara.query.GraphAnswer;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.rules.RulesRef;
import org.mulgara.server.NonRemoteSessionException;
import org.mulgara.server.Session;

import javax.activation.MimeType;
import javax.naming.*;

/**
 * Wrapper around a {@link RemoteSession} to make it look like a {@link
 * Session}. The only real functionality this wrapper implements is to nest any
 * {@link RemoteException}s inside {@link QueryException}s.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @created 2002-01-03
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class RemoteSessionWrapperSession implements Serializable, Session {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(RemoteSessionWrapperSession.class.getName());

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = -2647357071965350751L;

  /** The number of times to retry a call. */
  protected static final int RETRY_COUNT = 2;

  /** The number of times remaining to retry the current call. */
  protected int retryCount;

  /** The wrapped {@link RemoteSession} */
  private RemoteSession remoteSession;

  /** The serverURI of the remoteSessionFactory. Used to reconnect sessions. */
  protected URI serverURI = null;

  /**
   * Maintain the state of autcommit to determine if an exception
   * needs to be throw if a reconnection is attempted when a transaction
   * was previously in process when the server was bounced (a rollback has
   * occured).
   */
  protected boolean autoCommit = true;

  //
  // Constructor
  //

  /**
   * Wrap a remote session to make it appear as a local session.
   * @param remoteSession the wrapped remote session.
   * @param serverURI The server the session is connecting to.
   * @throws IllegalArgumentException if <var>remoteSession</var> is <code>null</code>
   */
  protected RemoteSessionWrapperSession(RemoteSession remoteSession, URI serverURI) {

    // Validate "remoteSession" parameter
    if (remoteSession == null) {
      throw new IllegalArgumentException("Null \"remoteSession\" parameter");
    }

    // Initialize fields
    this.remoteSession = remoteSession;
    this.serverURI = serverURI;

    resetRetries();
  }


  /**
   * Sets the contents of a model, via a model expression.
   *
   * @param uri The name of the model to set.
   * @param sourceUri The expression describing the data to put in the model.
   * @return The number of statements inserted into the model.
   * @throws QueryException An error getting data for the model, or inserting into the new model.
   */
  public long setModel(URI uri, URI sourceUri) throws QueryException {

    try {
      long r = remoteSession.setModel(uri, sourceUri);
      resetRetries();
      return r;
    } catch (RemoteException e) {
      testRetry(e);
      return setModel(uri, sourceUri);
    }
  }


  /**
   * Define the contents of a model via an {@link InputStream}.
   *
   * @param inputStream a remote inputstream
   * @param uri the {@link URI} of the model to be redefined
   * @param sourceUri the new content for the model
   * @return The number of statements inserted into the model
   * @throws QueryException if the model can't be modified
   */
  public long setModel(InputStream inputStream, URI uri, URI sourceUri, MimeType contentType) throws QueryException {

    try {

      long r = remoteSession.setModel(inputStream, uri, sourceUri, contentType);
      resetRetries();
      return r;
    } catch (RemoteException e) {
      testRetry(e);
      return setModel(inputStream, uri, sourceUri, contentType);
    }
  }


  /**
   * Sets the AutoCommit attribute of the RemoteSessionWrapperSession object
   *
   * @param autoCommit The new AutoCommit value
   * @throws QueryException Autocommit cannot be changed.
   */
  public void setAutoCommit(boolean autoCommit) throws QueryException {

    try {
      remoteSession.setAutoCommit(autoCommit);

      // autoCommit has been successfully issued on the
      // first attempt.
      this.autoCommit = autoCommit;

      resetRetries();
    } catch (RemoteException e) {

      // if autocommit was set the off/false then an
      // exception would be thrown by testRetry informing
      // the user of a rollback

      testRetry(e);

      // a successful retry to re-establish server
      // connectivity has been made.  Autocommit will
      // now default to true (a new session).

      // set the requested value of autocommit
      setAutoCommit(autoCommit);
    }
  }


  //
  // Methods implementing the Session interface
  //

  /**
   * {@inheritDoc}
   */
  public void insert(URI modelURI, Set<? extends Triple> statements) throws QueryException {

    try {
      remoteSession.insert(modelURI, statements);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      insert(modelURI, statements);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void insert(URI modelURI, Query query) throws QueryException {

    try {
      remoteSession.insert(modelURI, query);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      insert(modelURI, query);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void delete(URI modelURI, Set<? extends Triple> statements) throws QueryException {

    try {
      remoteSession.delete(modelURI, statements);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      delete(modelURI, statements);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void delete(URI modelURI, Query query) throws QueryException {

    try {
      remoteSession.delete(modelURI, query);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      delete(modelURI, query);
    }
  }


 /**
  * Backup all the data on the specified server. The database is not changed by
  * this method.
  *
  * @param destinationURI The URI of the file to backup into.
  * @throws QueryException if the backup cannot be completed.
  */
  public void backup(URI destinationURI) throws QueryException {

    try {
      remoteSession.backup(destinationURI);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      backup(destinationURI);
    }
  }


  /**
   * Backup all the data on the specified server to an output stream.
   * The database is not changed by this method.
   *
   * @param outputStream The stream to receive the contents
   * @throws QueryException if the backup cannot be completed.
   */
  public void backup(OutputStream outputStream) throws QueryException {

    try {
      remoteSession.backup(outputStream);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      backup(outputStream);
    }
  }
  
  
  /**
   * Export the data in the specified graph. The database is not changed by this method.
   * 
   * @param graphURI The URI of the graph to export.
   * @param destinationURI The URI of the file to export into.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, URI destinationURI) throws QueryException {
    try {
      remoteSession.export(graphURI, destinationURI);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      export(graphURI, destinationURI);
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
  public void export(URI graphURI, URI destinationURI, Map<String,URI> prefixes) throws QueryException {
    try {
      remoteSession.export(graphURI, destinationURI, prefixes);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      export(graphURI, destinationURI);
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
  public void export(URI graphURI, OutputStream outputStream, MimeType contentType) throws QueryException {
    try {
      remoteSession.export(graphURI, outputStream, contentType);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      export(graphURI, outputStream, contentType);
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
  public void export(URI graphURI, OutputStream outputStream, Map<String,URI> prefixes, MimeType contentType) throws QueryException {
    try {
      remoteSession.export(graphURI, outputStream, prefixes, contentType);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      export(graphURI, outputStream, prefixes, contentType);
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
  public void restore(URI sourceURI) throws QueryException {

    try {
      remoteSession.restore(sourceURI);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      restore(sourceURI);
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
  public void restore(InputStream inputStream, URI sourceURI)
      throws QueryException {

    try {
      remoteSession.restore(inputStream, sourceURI);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      restore(inputStream, sourceURI);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void createModel(URI modelURI, URI modelTypeURI) throws QueryException {

    try {
      remoteSession.createModel(modelURI, modelTypeURI);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      createModel(modelURI, modelTypeURI);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void removeModel(URI uri) throws QueryException {

    try {
      remoteSession.removeModel(uri);
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      removeModel(uri);
    }
  }


  /**
   * {@inheritDoc}
   */
  public boolean modelExists(URI uri) throws QueryException {
    try {
      boolean modelExists = remoteSession.modelExists(uri);
      resetRetries();
      return modelExists;
    } catch (RemoteException e) {
      testRetry(e);
      return modelExists(uri);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void commit() throws QueryException {

    try {
      remoteSession.commit();
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      commit();
    }
  }


  /**
   * {@inheritDoc}
   */
  public void rollback() throws QueryException {

    try {
      remoteSession.rollback();
      resetRetries();
    } catch (RemoteException e) {
      testRetry(e);
      rollback();
    }
  }


  /**
   * {@inheritDoc}
   */
  public List<Answer> query(List<Query> queries) throws QueryException {

    try {

      List<Object> remoteAnswers = remoteSession.query(queries);
      resetRetries();
      List<Answer> localAnswers = new ArrayList<Answer>(remoteAnswers.size());

      for (Object ans: remoteAnswers) {
        if (!(ans instanceof RemoteAnswer) && !(ans instanceof Answer)) {
          throw new QueryException("Non-answer returned from query.");
        }
        if (ans instanceof RemoteAnswer) localAnswers.add(new RemoteAnswerWrapperAnswer((RemoteAnswer)ans));
        else localAnswers.add((Answer)ans);
      }

      return localAnswers;
    } catch (RemoteException e) {
      testRetry(e);
      return query(queries);
    }
  }


  /**
   * {@inheritDoc}
   */
  public Answer query(Query query) throws QueryException {

    try {
      RemoteAnswer ans = remoteSession.query(query);
      resetRetries();
      return new RemoteAnswerWrapperAnswer(ans);
    } catch (RemoteException e) {
      testRetry(e);
      return query(query);
    }
  }


  /**
   * {@inheritDoc}
   * Provices a lightweight GraphAnswer wrapper over the returned Answer (which is already
   * a GraphAnswer at the server end).
   */
  public GraphAnswer query(ConstructQuery query) throws QueryException {
    try {
      RemoteAnswer ans = remoteSession.query(query);
      resetRetries();
      return new GraphAnswer(new RemoteAnswerWrapperAnswer(ans));
    } catch (RemoteException e) {
      testRetry(e);
      return query(query);
    }
  }


  /**
   * {@inheritDoc}
   */
  public boolean query(AskQuery query) throws QueryException {
    try {
      return remoteSession.query(query);
    } catch (RemoteException e) {
      testRetry(e);
      return query(query);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void close() throws QueryException {

    try {
      remoteSession.close();
      resetRetries();
    } catch (java.rmi.NoSuchObjectException e) {
      // do nothing as the RMI server has removed
      // the reference
    } catch (RemoteException e) {
      // no need to retry, since the session is gone
      throw new QueryException("Java RMI failure", e);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void login(URI securityDomain, String username, char[] password) {

    try {
      remoteSession.login(securityDomain, username, password);
    } catch (RemoteException e) {
      try {
        // test if this should be retried
        testRetry(e);
        try {
          // successfully got new session.  Try to log in.
          remoteSession.login(securityDomain, username, password);
        } catch (RemoteException re) {
          // unable to log in to a new session
          logger.warn("Cannot log in to remote session", re);
        } finally {
          resetRetries();
        }
      } catch (QueryException qe) {
        // retry not possible
        logger.warn("Cannot connect to remote session", qe);
      }
    }
  }


  /**
   * Tests if an RMIException was caused by a retryable condition.
   * If so, then obtains a new session for retrying.
   *
   * @throws QueryException if remote method can't be retried.
   */
  protected void testRetry(RemoteException e) throws QueryException {

    // determine if a retry should be attempted.
    if (!(e instanceof java.rmi.ConnectException) || retryCount == 0) {
      resetRetries();
      throw new QueryException("Java RMI failure", e);
    }

    try {

      RmiSessionFactory rmiSessionFactory = null;

      // create a new RMI session factory
      try {
        rmiSessionFactory = new RmiSessionFactory(serverURI);
      } catch (NamingException ex) {
        throw new QueryException("Java RMI reconnection failure", ex);
      } catch (NonRemoteSessionException nrse) {
        throw new QueryException("Server name modification during a query reconnection");
      }

      // obtain a new remoteSession and replace the current one
      remoteSession = rmiSessionFactory.getRemoteSessionFactory().newRemoteSession();

      // was a transaction in progress before the server connectivity was lost?
      if (!autoCommit) {

        // all new sessions will result in automcommit set to on;
        autoCommit = true;

        // since a transaction was in progress when server connectivity
        // was lost we must notify the user a possible rollback
        throw new QueryException("Connectivity to server "+
                                 this.serverURI + " was lost during a "+
                                "transaction, which has resulted in a "+
                                "transaction rollback.  "+
                                "Connectivity has now been re-established.");
      }


    } catch (RemoteException re) {
      throw new QueryException("Java RMI reconnection failure", re);
    }
    retryCount--;
  }


  /**
   * Resets the retry count.
   */
  protected void resetRetries() {
    retryCount = RETRY_COUNT;
  }


  /**
   * {@inheritDoc}
   */
  public boolean isLocal() {
    return false;
  }


  /**
   * {@inheritDoc}
   */
  public RulesRef buildRules(URI ruleModel, GraphExpression baseModel, URI destModel) throws QueryException, org.mulgara.rules.InitializerException {
    try {
      RulesRef ref = remoteSession.buildRules(ruleModel, baseModel, destModel);
      if (logger.isDebugEnabled()) logger.debug("got rules from RMI");
      return ref;
    } catch (RemoteException re) {
      Throwable cause = re.getCause();
      if (cause != null) throw new org.mulgara.rules.InitializerException("Unable to load rules: " + cause.getMessage(), cause);
      throw new org.mulgara.rules.InitializerException("Unable to load rules", re);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void applyRules(RulesRef rules) throws QueryException {
    try {
      remoteSession.applyRules(rules);
    } catch (RemoteException re) {
      Throwable cause = re.getCause();
      if (cause != null) throw new QueryException("Error applying rules: " + cause.getMessage(), cause);
      throw new QueryException("Error applying rules", re);
    }
  }

  public XAResource getXAResource() throws QueryException {
    try {
      return new RemoteXAResourceWrapperXAResource(remoteSession.getXAResource());
    } catch (RemoteException re){
      throw new QueryException("Java RMI failure", re);
    }
  }

  public XAResource getReadOnlyXAResource() throws QueryException {
    try {
      return new RemoteXAResourceWrapperXAResource(remoteSession.getReadOnlyXAResource());
    } catch (RemoteException re){
      throw new QueryException("Java RMI failure", re);
    }
  }

  public void setIdleTimeout(long millis) throws QueryException {
    try {
      remoteSession.setIdleTimeout(millis);
      resetRetries();
    } catch (RemoteException re){
      testRetry(re);
      setIdleTimeout(millis);
    }
  }

  public void setTransactionTimeout(long millis) throws QueryException {
    try {
      remoteSession.setTransactionTimeout(millis);
      resetRetries();
    } catch (RemoteException re){
      testRetry(re);
      setTransactionTimeout(millis);
    }
  }

  public boolean ping() throws QueryException {
    try {
      boolean ping = remoteSession.ping();
      resetRetries();
      return ping;
    } catch (RemoteException re) {
      testRetry(re);
      return ping();
    }
  }
}
