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
 * The Initial Developer of the Original Code is Andrew Newman. Portions
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

// Java 2 Standard Packages
import java.rmi.*;
import java.net.*;

// Jena packages
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node_Variable;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.server.*;

/**
 * Java RMI remote interface for drivers.
 *
 * @author Andrew Newman
 *
 * @created 2005-01-08
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/01/27 11:22:39 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy; 2005 Andrew Newman
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
interface RemoteJenaSession extends RemoteSession {

  /**
   * Insert a statement into a model.
   *
   * @param modelURI The URI of the model to delete from.
   * @param subject the subject of the statement.
   * @param predicate the predicate of the statement.
   * @param object the object of the statement.
   * @throws QueryException if the deletion cannot be completed.
   */
  public void insert(URI modelURI,
      com.hp.hpl.jena.graph.Node subject, com.hp.hpl.jena.graph.Node predicate,
      com.hp.hpl.jena.graph.Node object) throws QueryException, RemoteException;

  /**
   * Delete a statement from a model.
   *
   * @param modelURI The URI of the model to delete from.
   * @param subject the subject of the statement.
   * @param predicate the predicate of the statement.
   * @param object the object of the statement.
   * @throws QueryException if the deletion cannot be completed.
   */
  public void delete(URI modelURI, com.hp.hpl.jena.graph.Node subject,
      com.hp.hpl.jena.graph.Node predicate, com.hp.hpl.jena.graph.Node object)
      throws QueryException, RemoteException;

  /**
   * Return the number of statements in a particular graph.
   *
   * @param modelURI the uri for the garph.
   * @throws QueryException if it fails to access the store.
   */
  public long getNumberOfStatements(URI modelURI) throws QueryException, RemoteException;

  /**
   * Returns the Jena factory associated with the session.
   *
   * @return the Jena factory associated with the session.
   */
  public JenaFactory getJenaFactory() throws RemoteException;

  /**
   * Finds the unique values for a given column. The column is based on the
   * given Variable (column).
   *
   * @param modelURI The URI of the model to search against.
   * @param column Variable The Variable that will be projected.
   * @throws QueryException if there was an error accessing the store.
   * @return ClosableIterator An iterator for the column.
   */
  public com.hp.hpl.jena.util.iterator.ClosableIterator findUniqueValues(
      URI modelURI, Node_Variable column)
      throws QueryException, RemoteException;

  /**
   * Insert an array of triples into a model.
   *
   * @param modelURI The URI of the model to delete from.
   * @param triples the statements.
   * @throws QueryException if the deletion cannot be completed.
   */
  public void insert(URI modelURI, com.hp.hpl.jena.graph.Triple[] triples)
      throws QueryException, RemoteException;

  /**
   * Delete an array of triples from a model.
   *
   * @param modelURI The URI of the model to delete from.
   * @param triples the statements.
   * @throws QueryException if the deletion cannot be completed.
   */
  public void delete(URI modelURI, com.hp.hpl.jena.graph.Triple[] triples)
      throws QueryException, RemoteException;

  /**
   * Test the graph for the occurrence of a triple.
   *
   * @param modelURI The URI of the model to insert into.
   * @param subject The subject.
   * @param predicate The predicate.
   * @param object The object.
   * @return True if the triple is found in the model, otherwise false.
   * @throws QueryException If there was an error accessing the store.
   */
  public boolean contains(URI modelURI, com.hp.hpl.jena.graph.Node subject,
      com.hp.hpl.jena.graph.Node predicate, com.hp.hpl.jena.graph.Node object)
      throws QueryException, RemoteException;

  /**
   * Returns an iterator to a set of statements that match a given subject,
   * predicate and object.  A null value for any of the parts of a triple are
   * treated as unconstrained, any values will be returned.
   *
   * @param modelURI The URI of the model to search against.
   * @param subject The subject to find or null to indicate any subject.
   * @param predicate The predicate to find or null to indicate any predicate.
   * @param object ObjectNode The object to find or null to indicate any object.
   * @throws QueryException If there was an error accessing the store.
   */
  public com.hp.hpl.jena.util.iterator.ClosableIterator find(URI modelURI,
      com.hp.hpl.jena.graph.Node subject, com.hp.hpl.jena.graph.Node predicate,
      com.hp.hpl.jena.graph.Node object)
      throws QueryException, RemoteException;
}
