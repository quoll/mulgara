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
 *    XAResource addition copyright 2008 The Topaz Foundation
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.server;


// Java 2 Standard Packages
import java.net.*;
import java.util.*;
import java.io.*;

import javax.activation.MimeType;
import javax.transaction.xa.XAResource;

// Locally written packages
import org.jrdf.graph.Triple;
import org.mulgara.query.Answer;
import org.mulgara.query.AskQuery;
import org.mulgara.query.ConstructQuery;
import org.mulgara.query.GraphAnswer;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.rules.InitializerException;
import org.mulgara.rules.Rules;  // Required only for Javadoc
import org.mulgara.rules.RulesRef;

/**
 * Mulgara interaction session.
 *
 * @created 2001-11-11
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author <a href="http://staff.pisoftware.com/kkucks">Kevin Kucks</a>
 *
 * @version $Revision: 1.11 $
 *
 * @modified $Date: 2005/06/26 12:48:10 $ by $Author: pgearon $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface Session {

  /**
   * This constant can be passed to {@link #createModel} to indicate that a
   * normal model backed by a triple store is required.
   */
  public final URI MULGARA_GRAPH_URI = URI.create(Mulgara.NAMESPACE + "Model");

  /**
   * Insert statements into a model.
   *
   * @param modelURI The URI of the model to insert into.
   * @param statements The Set of statements to insert into the model.
   * @throws QueryException if the insert cannot be completed.
   */
  public void insert(URI modelURI, Set<? extends Triple> statements) throws QueryException;

  /**
   * Insert statements from the results of a query into another model.
   *
   * @param modelURI URI The URI of the model to insert into.
   * @param query The query to perform on the server.
   * @throws QueryException if the insert cannot be completed.
   */
  public void insert(URI modelURI, Query query) throws QueryException;

  /**
   * Delete the set of statements from a model.
   *
   * @param modelURI The URI of the model to delete from.
   * @param statements The Set of statements to delete from the model.
   * @throws QueryException if the deletion cannot be completed.
   */
  public void delete(URI modelURI, Set<? extends Triple> statements) throws QueryException;

  /**
   * Delete statements from a model using the results of query.
   *
   * @param modelURI The URI of the model to delete from.
   * @param query The query to perform on the server.
   * @throws QueryException if the deletion cannot be completed.
   */
  public void delete(URI modelURI, Query query) throws QueryException;

  /**
   * Backup all the data on the server. The database is not changed by
   * this method.  Does not require an exclusive lock on the database and will
   * begin with the currently committed state.
   *
   * @param destinationURI The URI of the file to backup into.
   * @throws QueryException if the backup cannot be completed.
   */
  public void backup(URI destinationURI)
    throws QueryException;

  /**
   * Backup all the data on the server to an output stream.
   * The database is not changed by this method.  Does not require an exclusive
   * lock on the database and will begin with the currently committed state.
   *
   * @param outputStream The stream to receive the contents
   * @throws QueryException if the backup cannot be completed.
   */
  public void backup(OutputStream outputStream)
    throws QueryException;
  
  /**
   * Export the data in the specified graph. The database is not changed by
   * this method.  Does not require an exclusive lock on the database and will
   * begin with the currently committed state.
   *
   * @param graphURI The URI of the graph to export.
   * @param destinationURI The URI of the file to export into.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, URI destinationURI) throws QueryException;
  
  /**
   * Export the data in the specified graph. The database is not changed by
   * this method.  Does not require an exclusive lock on the database and will
   * begin with the currently committed state.
   * If a set of namespace prefixes is supplied, it will be used to pre-populate
   * the namespace prefix definitions in the exported RDF/XML.
   *
   * @param graphURI The URI of the graph to export.
   * @param destinationURI The URI of the file to export into.
   * @param prefixes An optional set of user-supplied namespace prefix mappings;
   *   may be <code>null</code> to use the generated namespace prefixes.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, URI destinationURI, Map<String,URI> prefixes) throws QueryException;
  
  /**
   * Export the data in the specified graph to an output stream.
   * The database is not changed by this method.  Does not require an exclusive
   * lock on the database and will begin with the currently committed state.
   *
   * @param graphURI The URI of the graph to export.
   * @param outputStream The stream to receive the contents
   * @param contentType An optional content type to determine the format in which to write to the output stream.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, OutputStream outputStream, MimeType contentType) throws QueryException;

  /**
   * Export the data in the specified graph to an output stream.
   * The database is not changed by this method.  Does not require an exclusive
   * lock on the database and will begin with the currently committed state.
   * If a set of namespace prefixes is supplied, it will be used to pre-populate
   * the namespace prefix definitions in the exported RDF/XML.
   *
   * @param graphURI The URI of the graph to export.
   * @param outputStream The stream to receive the contents
   * @param prefixes An optional set of user-supplied namespace prefix mappings;
   *   may be <code>null</code> to use the generated namespace prefixes.
   * @param contentType An optional content type to determine the format in which to write to the output stream.
   * @throws QueryException if the export cannot be completed.
   */
  public void export(URI graphURI, OutputStream outputStream, Map<String,URI> prefixes, MimeType contentType) throws QueryException;

  /**
   * Restore all the data on the server. If the database is not
   * currently empty then the current contents of the database will be replaced
   * with the content of the backup file when this method returns.
   *
   * @param sourceURI The URI of the backup file to restore from.
   * @throws QueryException if the restore cannot be completed.
   */
  public void restore(URI sourceURI) throws QueryException;

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
      throws QueryException;

  /**
   * Perform a single query.
   *
   * @param query the query
   * @return a non-<code>null</code> answer to the <var>query</var>
   * @throws QueryException if <var>query</var> can't be answered
   */
  public Answer query(Query query) throws QueryException;

  /**
   * Perform an ASK query.
   *
   * @param query the query
   * @return <code>true</code> if the query results in data, <code>false</code> if
   *         it results in the empty set.
   * @throws QueryException if <var>query</var> can't be answered
   */
  public boolean query(AskQuery query) throws QueryException;

  /**
   * Perform a CONSTRUCT query.
   *
   * @param query the query
   * @return An Answer that contains triples valid for a graph.
   * @throws QueryException if <var>query</var> can't be answered
   */
  public GraphAnswer query(ConstructQuery query) throws QueryException;

  /**
   * Performs multiple queries storing the results, answers, into the returned
   * list.
   *
   * @param queries the list of query objects.
   * @return a list of non-<code>null</code> answers to the <var>queries</var>
   * @throws QueryException if <var>query</var> can't be answered
   */
  public List<Answer> query(List<Query> queries) throws QueryException;

  /**
   * Creates a new model of a given type.  The standard model type is
   * {@link #MULGARA_GRAPH_URI}.
   *
   * @param modelURI the {@link URI} of the new model
   * @param modelTypeURI the {@link URI} identifying the type of model to use
   *   (e.g. Lucene); if <code>null</code>, use the same type as the system
   *   models
   * @throws QueryException if the model can't be created
   */
  public void createModel(URI modelURI, URI modelTypeURI)
    throws QueryException;

  /**
   * Remove an existing model.
   *
   * @param uri the {@link URI} of the doomed model.
   * @throws QueryException if the model can't be removed or doesn't exist.
   */
  public void removeModel(URI uri) throws QueryException;

  /**
   * Tests for the existance of a model.
   *
   * @param uri the {@link URI} of the model.
   * @throws QueryException if the query against the system model fails.
   * @return true if the model exists or false if it doesn't.
   */
  public boolean modelExists(URI uri) throws QueryException;

  /**
   * Define the contents of a model via a {@link GraphExpression}
   *
   * @param destinationUri the {@link URI} of the model to be redefined
   * @param sourceUri the new content for the model
   * @return The number of statements inserted into the model
   * @throws QueryException if the model can't be modified
   */
  public long setModel(URI destinationUri, URI sourceUri)
    throws QueryException;

  /**
   * Define the contents of a model via an {@link InputStream}.
   *
   * @param inputStream a remote inputstream
   * @param destinationUri the {@link URI} of the model to be redefined
   * @param sourceUri the URI for the new content for the model;
   *        if inputStream is null this will be used to locate the new content.
   * @param contentType the content type being loaded, if known.
   * @return The number of statements inserted into the model
   * @throws QueryException if the model can't be modified
   */
  public long setModel(InputStream inputStream, URI destinationUri,
      URI sourceUri, MimeType contentType) throws QueryException;

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
  public RulesRef buildRules(URI ruleModel, GraphExpression baseModel, URI destModel) throws QueryException, org.mulgara.rules.InitializerException;

  /**
   * Rules a set of {@link Rules} on its defined model.
   *
   * @param rules The rules to be run.
   * @throws QueryException An error was encountered executing the rules.
   * @throws QueryException An error was encountered accessing the rules accross a network.
   */
  public void applyRules(RulesRef rules) throws QueryException;

  /**
   * Sets whether permanent changes made to the database in this session
   * occur immediately (true) or until a commit has been made (false).  A
   * session may lose autocommit false status (the write phase) if it is idle.
   * By default a session is set to true.
   *
   * @param autoCommit true to make changes available to other sessions, false
   *   to allow rollback/commit.
   * @throws QueryException if it fails to suspend or resume the transaction.
   */
  public void setAutoCommit(boolean autoCommit) throws QueryException;

  /**
   * Commits changes to the database so that other sessions can see the
   * modifications.  Requires that autocommit has been set to false.  It does
   * not change the autocommit state.
   *
   * @throws QueryException if there was an exception commiting the changes
   *   to the database.
   */
  public void commit() throws QueryException;

  /**
   * Undo the changes made to the database since autocommit has been set off.
   * It does not change the autocommit state.
   *
   * @throws QueryException if there was an exception rolling back the changes
   *   to the database.
   */
  public void rollback() throws QueryException;

  /**
   * Release resources associated with this session. The session won't be usable
   * after this method is invoked.
   */
  public void close() throws QueryException;

  /**
   * Returns true if the session is local (within the same JVM).
   *
   * @return if the session is local (within the same JVM)
   */
  public boolean isLocal();

  /**
   * Add authentication data to the session.
   *
   * @param securityDomain the URI uniquely identifying the security domain to
   *      which these credentials apply
   * @param username the identity to authenticate as
   * @param password the secret used to prove identity
   * @see SessionFactory#getSecurityDomain
   */
  public void login(URI securityDomain, String username, char[] password);

  /** 
   * The maximum time a transaction may be idle before it is aborted. If not set a default
   * value is used. This value is only used for new transactions and does not affect any currently
   * running transactions.
   *
   * <p>This currently only affects write transactions.
   * 
   * @param millis the number of milliseconds, or 0 for the default timeout
   * @throws QueryException if there was an error talking to the server
   */
  public void setIdleTimeout(long millis) throws QueryException;

  /** 
   * The maximum time a transaction may be active (started but neither committed nor rolled back)
   * before it is aborted. If not set a default value is used. This value is only used for new
   * transactions and does not affect any currently running transactions.
   *
   * <p>This currently only affects write transactions.
   * 
   * @param millis the number of milliseconds, or 0 for the default timeout
   * @throws QueryException if there was an error talking to the server
   */
  public void setTransactionTimeout(long millis) throws QueryException;

  /**
   * Obtain an XAResource for this Session.
   *
   * Use of this method is incompatible with any use of implicit or internally
   * mediated transactions with this Session.
   * Transactions initiated from the XAResource returned by the read-only
   * version of this method will be read-only.
   */
  public XAResource getXAResource() throws QueryException;
  public XAResource getReadOnlyXAResource() throws QueryException;
  
  /**
   * Test the connectivity of a session.  All implementing classes should return
   * <code>true</code>.  This method is intended for session proxies to establish
   * connectivity on a remote session.
   * @return <code>true</code> if connectivity with the session was established.
   */
  public boolean ping() throws QueryException;

  /**
   * This class is just a devious way to get static initialization for the
   * {@link Session} interface.
   */
  abstract class ConstantFactory {

    static URI getMulgaraModelURI() {
      try {
        return new URI(Mulgara.NAMESPACE + "Model");
      }
       catch (URISyntaxException e) {
        throw new Error("Bad hardcoded URI");
      }
    }
  }
}
