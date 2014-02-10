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
import java.rmi.*;

// Third party packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Remote ITQL answer. An answer is a set of solutions, where a solution is a
 * mapping of {@link Variable}s to {@link org.mulgara.query.Value}s.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @created 2001-07-31
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:02 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface RemoteAnswer extends Remote
{
  /** Size limit on Marshalled Answers. */
  public static final int MARSHALL_SIZE_LIMIT = 
    Integer.parseInt(System.getProperty("mulgara.rmi.marshallsizelimit", "100"));

  /**
   * Reset the instance to iterate from the beginning.
   *
   * Needs to be followed by a call to {@link #next} before reading can start.
   *
   * @throws TuplesException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public AnswerPage beforeFirstAndInitPage() throws TuplesException, RemoteException;

  /**
   * Return the object at the given index.
   *
   * @param column PARAMETER TO DO
   * @return the value at the given index
   * @throws SQLException on failure
   * @throws TuplesException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public Object getObject(int column) throws TuplesException, RemoteException;

  /**
   * Return the object at the given column name.
   *
   * @param columnName the index of the object to retrieve
   * @return the value at the given index
   * @throws SQLException on failure
   * @throws TuplesException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public Object getObject(String columnName) throws TuplesException,
      RemoteException;

  /**
  * @return the number of columns
  */
  public int getNumberOfVariables() throws RemoteException;

  /**
   * The variables bound and their default collation order. The array returned
   * by this method should be treated as if its contents were immutable, even
   * though Java won't enforce this. If the elements of the array are modified,
   * there may be side effects on the past and future clones of the tuples it
   * was obtained from.
   *
   * @return the {@link Variable}s bound within this answer.
   * @throws RemoteException Due to network error.
   */
  public Variable[] getVariables() throws RemoteException;

  /**
   * Tests whether this is a unit-valued answer. A unit answer appended to
   * something yields the unit answer. A unit answer joined to something yields
   * the same something. Notionally, the unit answer has zero columns and one
   * row.
   *
   * @return The Unconstrained value
   * @throws TuplesException Error accessing the underlying data.
   * @throws RemoteException Due to network error.
   */
  public boolean isUnconstrained() throws TuplesException, RemoteException;

  /**
   * Advance to the next term in the solution.
   *
   * @return <code>false<code> if there was no further term to advance to.
   * @throws TuplesException Error accessing the underlying data.
   * @throws RemoteException Due to network error.
   */
  public boolean next() throws TuplesException, RemoteException;

  /**
   * Accessor for the binding of a given variable within the current product term (row).
   *
   * @param column PARAMETER TO DO
   * @return the bound value, or {@link org.mulgara.store.tuples.Tuples#UNBOUND}
   *      if there is no binding within the current product term (row)
   * @throws TuplesException if there is no current row (before first or after
   *      last) or if <var>variable</var> isn't an element of {@link #getVariables}
   * @throws RemoteException Due to network error. 
   */
  public int getColumnIndex(Variable column)
    throws TuplesException, RemoteException;

  /**
   * This method returns the exact number of rows which this instance contains.
   *
   * @return The exact number of rows that this instance contains.
   * @throws TuplesException Error accessing the underlying data.
   */
  public long getRowCount() throws TuplesException, RemoteException;

  /**
   * This method returns an upper bound on the number of rows which this instance contains.
   *
   * @return The upper bound of the number of rows that this instance contains.
   * @throws TuplesException Error accessing the underlying data.
   */
  public long getRowUpperBound() throws TuplesException, RemoteException;

  /**
   * This method returns an expected value of the number of rows which this instance contains.
   *
   * @return The expected value of the number of rows that this instance contains.
   * @throws TuplesException Error accessing the underlying data.
   */
  public long getRowExpectedCount() throws TuplesException, RemoteException;


  public static final int ZERO = 0;
  public static final int ONE  = 1;
  public static final int MANY = 2;

  /**
   * This method returns cardinality of the number of rows which this instance contains.
   *
   * @return The cardinality of this tuples. {0,1,N} rows.
   * @throws TuplesException Error accessing the underlying data.
   */
  public int getRowCardinality() throws TuplesException, RemoteException;
  
  public boolean isEmpty() throws TuplesException, RemoteException;

  /**
   * Free resources associated with this instance.
   *
   * @throws TuplesException Error accessing the underlying data.
   */
  public void close() throws TuplesException, RemoteException;

  /**
   * Advance to the next page in the solution.  This advances the answer by one page.
   *
   * @return The new page, or <code>null</code> if there is no data left in the answer.
   * @throws TuplesException Iterating through the answer caused a problem.
   * @throws RemoteException Required for RMI interfaces.
   */
  public AnswerPage nextPage() throws TuplesException, RemoteException;

  /**
   * Returns a clone for a remote client 
   *
   * @return a <var>RemoteAnswer</var>
   * @throws RemoteException the server is unable to perform the clone 
   */
  public RemoteAnswer remoteClone() throws RemoteException;

}
