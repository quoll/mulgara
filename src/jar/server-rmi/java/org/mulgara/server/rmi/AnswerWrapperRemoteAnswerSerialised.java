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
import java.io.Serializable;

// Third party packages
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Serialised version of a Remote ITQL answer. An answer is a set of solutions, where a solution is a
 * mapping of {@link Variable}s to {@link org.mulgara.query.Value}s.  Designed to allow optimisations
 * for small Answers when needing to return a RemoteAnswer interface.  The naming scheme is terrible,
 * but it's consistent with {@link org.mulgara.server.rmi.AnswerWrapperRemoteAnswer}.
 * None of the methods in this class throw RemoteException, as this class just needs to meet the
 * interface, and will be serialised rather than remote.
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paula Gearon</a>
 *
 * @created 2004-03-25
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/01/27 11:58:45 $
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
public class AnswerWrapperRemoteAnswerSerialised implements RemoteAnswer,
    AnswerPage, Serializable, Cloneable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 3548636177527412538L;

  /**
   * The wrapped serializable instance.
   */
  protected final Answer answer;

  /**
   * Flag to determine availability of a next line, used to paging.
   */
  protected boolean nextCanBeCalled;

  /**
   * @param answer  the instance to wrap
   * @throws IllegalArgumentException if <var>answer</var> is <code>null</code>
   */
  public AnswerWrapperRemoteAnswerSerialised(Answer answer) throws
      RemoteException {
    // Validate "answer" parameter
    if (answer == null) {
      throw new IllegalArgumentException("Null \"answer\" parameter");
    }

    // Initialize wrapped field
    this.answer = answer;
    this.nextCanBeCalled = false;
  }

  /**
   * Reset the instance to iterate from the beginning.
   *
   * Needs to be followed by a call to {@link #next} before reading can start.
   *
   * @throws TuplesException There was an error calling beforeFirst on the wrapped answer.
   * @throws RemoteException Needed to meet the Remote interface.
   */
  public AnswerPage beforeFirstAndInitPage() throws TuplesException,
      RemoteException {
    answer.beforeFirst();
    nextCanBeCalled = true;
    return this;
  }

  /**
   * Reset the instance to iterate from the beginning.  Used to meet AnswerPage interface.
   *
   * Needs to be followed by a call to {@link #next} before reading can start.
   *
   * @throws TuplesException There was an error calling beforeFirst on the wrapped answer.
   */
  public void beforeFirstInPage() throws TuplesException {
    answer.beforeFirst();
    nextCanBeCalled = true;
  }

  /**
   * Return the object at the given index.
   *
   * @param column PARAMETER TO DO
   * @return the value at the given index
   * @throws TuplesException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public Object getObject(int column) throws TuplesException, RemoteException {
    return answer.getObject(column);
  }

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
      RemoteException {
    return answer.getObject(columnName);
  }

  /**
   * Return the object at the given index meeting the {@link AnswerPage} interface.
   *
   * @param column PARAMETER TO DO
   * @return the value at the given index
   * @throws TuplesException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public Object getObjectFromPage(int column) throws TuplesException {
    return answer.getObject(column);
  }

  /**
   * Return the object at the given column name meeting the {@link AnswerPage} interface.
   *
   * @param columnName the index of the object to retrieve
   * @return the value at the given index
   * @throws SQLException on failure
   * @throws TuplesException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public Object getObjectFromPage(String columnName) throws TuplesException {
    return answer.getObject(columnName);
  }

  /**
   * @return the number of columns
   */
  public int getNumberOfVariables() throws RemoteException {
    return answer.getNumberOfVariables();
  }

  /**
   * The variables bound and their default collation order. The array returned
   * by this method should be treated as if its contents were immutable, even
   * though Java won't enforce this. If the elements of the array are modified,
   * there may be side effects on the past and future clones of the tuples it
   * was obtained from.
   *
   * @return the {@link Variable}s bound within this answer.
   * @throws RemoteException EXCEPTION TO DO
   */
  public Variable[] getVariables() throws RemoteException {
    return answer.getVariables();
  }

  /**
   * Tests whether this is a unit-valued answer. A unit answer appended to
   * something yields the unit answer. A unit answer joined to something yields
   * the same something. Notionally, the unit answer has zero columns and one
   * row.
   *
   * @return The Unconstrained value
   * @throws TuplesException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public boolean isUnconstrained() throws TuplesException, RemoteException {
    return answer.isUnconstrained();
  }

  /**
   * Advance to the next term in the solution.
   *
   * @return <code>false<code> if there was no further term to advance to.
   * @throws TuplesException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public boolean next() throws TuplesException, RemoteException {
    nextCanBeCalled = answer.next();
    return nextCanBeCalled;
  }

  /**
   * Advance to the next term in the solution on the {@link AnswerPage} interface.
   *
   * @return <code>false<code> if there was no further term to advance to.
   * @throws TuplesException EXCEPTION TO DO
   * @throws RemoteException EXCEPTION TO DO
   */
  public boolean nextInPage() throws TuplesException {
    nextCanBeCalled = answer.next();
    return nextCanBeCalled;
  }

  /**
   * Advance to the next page in the solution.  This advances the answer by one page.
   *
   * @return The new page, or <code>null</code> if there is no data left in the answer.
   * @throws TuplesException Iterating through the answer caused a problem.
   * @throws RemoteException Required for RMI interfaces.
   */
  public AnswerPage nextPage() throws TuplesException, RemoteException {
    return (getRowCardinality() == ZERO || !nextCanBeCalled) ? null : this;
  }

  /**
   * Retrieves the number of rows in this page.
   * @return The number of rows in the current page, this is less than or equal to the page size.
   */
  public long getPageSize() throws TuplesException {
    try {
      return getRowCount();
    }
    catch (RemoteException re) {
      // Not possible to get this from a serialised class
      throw new TuplesException("Error in Answer access", re);
    }
  }

  /**
   * Accessor for the binding of a given variable within the current product
   * term (row).
   *
   * @param column PARAMETER TO DO
   * @return the bound value, or {@link org.mulgara.store.tuples.Tuples#UNBOUND}
   *      if there is no binding within the current product term (row)
   * @throws TuplesException if there is no current row (before first or after
   *      last) or if <var>variable</var> isn't an element of {@link
   *      #getVariables}
   * @throws RemoteException EXCEPTION TO DO
   */
  public int getColumnIndex(Variable column) throws TuplesException,
      RemoteException {
    return answer.getColumnIndex(column);
  }

  /**
   * This method returns the number of rows which this instance contains.
   *
   * @return the number of rows that this instance contains.
   * @throws TuplesException Error in the underlying data.
   * @throws RemoteException Due to network error.
   */
  public long getRowCount() throws TuplesException, RemoteException {
    return answer.getRowCount();
  }

  public long getRowUpperBound() throws TuplesException, RemoteException {
    return answer.getRowUpperBound();
  }

  public long getRowExpectedCount() throws TuplesException, RemoteException {
    return answer.getRowExpectedCount();
  }

  public int getRowCardinality() throws TuplesException, RemoteException {
    return answer.getRowCardinality();
  }

  public boolean isEmpty() throws TuplesException, RemoteException {
    return answer.isEmpty();
  }

  /**
   * Free resources associated with this instance.
   *
   * @throws RemoteException EXCEPTION TO DO
   */
  public void close() throws TuplesException, RemoteException {
    answer.close();
  }

  /**
   * Indicates that this is the last page constructable for the current answer.
   *
   * @return Always true.
   */
  public boolean isLastPage() {
    return true;
  }

  /**
   * Returns a copy of this object.
   *
   * @return Object
   */
  public Object clone() {

    //copy the answer too
    Answer answerClone = (Answer)this.answer.clone();

    //reset the original Answer
    try {
      answerClone.beforeFirst();
      return new AnswerWrapperRemoteAnswerSerialised(answerClone);
    } catch (TuplesException tuplesException) {
      throw new RuntimeException(tuplesException);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a copy of this object for a sudo remote client.
   * It will never throw a RemoteException as this is a
   * serializable Answer.
   *
   * @throws RemoteException
   * @return <var>RemoteAnswer</var>
   */
  public RemoteAnswer remoteClone() throws RemoteException {
    return (RemoteAnswer)this.clone();
  }
}
