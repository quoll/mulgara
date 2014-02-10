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
import java.rmi.RemoteException;

// Third party packages
import org.apache.log4j.*;

// Local packages
import org.mulgara.query.AbstractAnswer;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.server.rmi.AnswerPage;
import org.mulgara.util.StackTrace;

/**
 * Wrap a {@link RemoteAnswer} to make it into an {@link Answer}.
 *
 * @created 2004-03-17
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/01/05 04:59:02 $ by $Author: newmana $
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class RemoteAnswerWrapperAnswer extends AbstractAnswer implements Answer, Cloneable {
  /** logger */
  private static final Logger logger =
      Logger.getLogger(AnswerWrapperRemoteAnswer.class.getName());

  /**
   * Default timeout period to wait for a new page.
   */
  public static final String PREFETCH_TIMEOUT_PROPERTY = "mulgara.rmi.pagetimeout";

  /**
   * Default timeout period to wait for a new page.
   */
  public static final int DEFAULT_PREFETCH_TIMEOUT = 60000;

  /**
   * The wrapped instance.
   */
  private RemoteAnswer remoteAnswer;

  /**
   * Immutable variables.
   */
  private final Variable[] variables;

  /**
   * The current page of data to return.
   */
  private AnswerPage currentPage = null;

  /**
   * The timeout period to wait for a new page.
   */
  private int timeout;

  /**
   * The thread used for prefetching the next page of answers.
   */
  private PrefetchThread prefetchThread;

  /**
   * Optimisation to prevent dropping of the first page when beforeFirst is called.
   */
  private boolean onFirstPage = false;

  /**
   * Keeps the stack trace of where the answer was originally closed.
   */
  private StackTrace closedTrace;
  private boolean closed = false;

  //
  // Constructor
  //

  /**
   * Wrap a {@link RemoteAnswer} to make it into an {@link Answer}.
   *
   * @param remoteAnswer  the instance to wrap
   * @throws IllegalArgumentException  if <var>remoteAnswer</var> is
   *   <code>null</code>
   */
  RemoteAnswerWrapperAnswer(RemoteAnswer remoteAnswer) throws RemoteException {
    // Validate "remoteAnswer" parameter
    if (remoteAnswer == null) {
      throw new IllegalArgumentException("Null \"remoteAnswer\" parameter");
    }

    // Initialize the wrapped instance
    this.remoteAnswer = remoteAnswer;
    variables = remoteAnswer.getVariables();
    currentPage = null;
    onFirstPage = false;
    prefetchThread = null;

    // Initialize the page timeout
    timeout = Integer.getInteger(PREFETCH_TIMEOUT_PROPERTY,
                                 DEFAULT_PREFETCH_TIMEOUT).intValue();
  }

  //
  // Methods implementing Answer
  //

  /**
   * Clone the current RemoteAnswer wrapper, and increment the reference count to the RemoteAnswer.
   *
   * @return The new RemoteAnswer wrapper.  This refers to the same remote answer as the original.
   */
  public Object clone() {

    try {
      // protect the RMI threading model
      waitForPrefetchThread();
      RemoteAnswerWrapperAnswer a = (RemoteAnswerWrapperAnswer)super.clone();
      a.remoteAnswer = this.remoteAnswer.remoteClone();
      a.currentPage = null;
      a.onFirstPage = false;
      a.prefetchThread = null;
      return a;
    } catch (RMITimeoutException rmie) {
      throw new RuntimeException("Timeout waiting on server", rmie);
    } catch (RemoteException rex) {
      throw new RuntimeException("Clone failed on server", rex);
    }
  }

  /**
   * Return the object at the given index.
   *
   * @param column  column numbering starts from zero
   * @return the value at the given index
   * @throws SQLException on failure
   * @throws TuplesException EXCEPTION TO DO
   */
  public Object getObject(int column) throws TuplesException {
    assert currentPage != null;
    try {
      Object obj = currentPage.getObjectFromPage(column);
      // If this is a remote answer then wrap it
      if (obj instanceof RemoteAnswer) {
        obj = new RemoteAnswerWrapperAnswer( (RemoteAnswer) obj);
      }
      return obj;
    } catch (RemoteException e) {
      throw new TuplesException("Unable to get column " + column, e);
    }
  }

  /**
   * Return the object at the given column name.
   *
   * @param columnName the index of the object to retrieve
   * @return the value at the given index
   * @throws SQLException on failure
   * @throws TuplesException EXCEPTION TO DO
   */
  public Object getObject(String columnName) throws TuplesException {
    assert currentPage != null;
    try {
      Object obj = currentPage.getObjectFromPage(columnName);
      // If this is a remote answer then wrap it
      if (obj instanceof RemoteAnswer) {
        obj = new RemoteAnswerWrapperAnswer( (RemoteAnswer) obj);
      }
      return obj;
    } catch (RemoteException e) {
      throw new TuplesException("Unable to get column \"" + columnName + "\"", e);
    }
  }

  //
  // Methods implementing Cursor
  //

  /**
   * Reset to iterate through every single element.
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public synchronized void beforeFirst() throws TuplesException {
    try {
      waitForPrefetchThread();
      if (onFirstPage) {
        currentPage.beforeFirstInPage();
      } else {
        currentPage = remoteAnswer.beforeFirstAndInitPage();
        // make onFirstPage false if the page is invalid
        onFirstPage = (currentPage != null);
        // Abandon the last prefetched page, and start a new prefetch thread
        prefetchThread = new PrefetchThread();
      }
    } catch (RemoteException er) {
      logger.warn("RemoteException thrown in beforeFirst", er);
      throw new TuplesException("Couldn't reset remote cursor", er);
    } catch (RMITimeoutException te) {
      throw new TuplesException("Couldn't reset remote cursor", te);
    }
  }

  /**
   * Free resources associated with this instance.
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public void close() throws TuplesException {

    //ensure the prefetchThread is not fetching next page
    if (prefetchThread != null) {
      try {
        prefetchThread.join(timeout);
      } catch (InterruptedException ie) {
        logger.info("Join on prefetchThread interrupted.", ie);
      }
      if (!prefetchThread.hasFinished()) {
        logger.warn("No RMI data returned within " + timeout + "ms while closing");
      }
      prefetchThread = null;
    }

    if (closed) {
      logger.warn("Was already closed.");
      if (closedTrace != null) logger.debug("Originally closed at: " + closedTrace);
      throw new TuplesException("Attempting to close answer twice.\n" + new StackTrace());
    }
    closed = true;
    if (logger.isDebugEnabled()) closedTrace = new StackTrace();

    // no more references left, close the remote object
    try {
      remoteAnswer.close();
      currentPage = null;
      onFirstPage = false;
    } catch (RemoteException e) {
      throw new TuplesException("Couldn't close remote cursor", e);
    }
    // set the remote answer to null for the sake of the finalize method below
    remoteAnswer = null;
  }

  /**
   * Find the index of a variable.
   *
   * @param column PARAMETER TO DO
   * @return The ColumnIndex value
   * @throws TuplesException EXCEPTION TO DO
   */
  public int getColumnIndex(Variable column) throws TuplesException {
    if (variables == null) {

      throw new TuplesException("No columns in Answer");
    }

    // Validate "column" parameter
    if (column == null) {
      throw new IllegalArgumentException("Null \"column\" parameter");
    }

    // Look for the requested variable in the "variables" array
    for (int i = 0; i < this.getNumberOfVariables(); i++) {
      if (column.equals(variables[i])) {
        return i;
      }
    }

    // Couldn't find the requested variable
    throw new TuplesException("No such column " + column);
  }

  /**
   * Returns the number of variables (columns).
   *
   * @return the number of variables (columns)
   */
  public int getNumberOfVariables() {
    int noVars = 0;

    if (variables != null) {
      noVars = variables.length;
    }

    return noVars;
  }

  /**
   * The variables bound and their default collation order. The array returned
   * by this method should be treated as if its contents were immutable, even
   * though Java won't enforce this. If the elements of the array are modified,
   * there may be side effects on the past and future clones of the tuples it
   * was obtained from.
   *
   * @return the {@link Variable}s bound within this answer.
   */
  public Variable[] getVariables() {
    return variables;
  }

  /**
   * Tests whether this is a unit-valued answer. A unit answer appended to
   * something yields the unit answer. A unit answer joined to something yields
   * the same something. Notionally, the unit answer has zero columns and one
   * row.
   *
   * @return The Unconstrained value
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean isUnconstrained() throws TuplesException {
    try {
      waitForPrefetchThread();
      assert prefetchThread == null || prefetchThread.hasFinished();
      return remoteAnswer.isUnconstrained();
    } catch (RMITimeoutException rmie) {
      throw new RuntimeException("Timeout waiting on server", rmie);
    } catch (RemoteException e) {
      throw new TuplesException("Can't test for unconstrained", e);
    }
  }

  /**
   * This method returns the number of rows which this instance contains.
   *
   * @return an upper bound on the number of rows that this instance contains.
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getRowCount() throws TuplesException {
    try {
      waitForPrefetchThread();
      assert prefetchThread == null || prefetchThread.hasFinished();
      return remoteAnswer.getRowCount();
    } catch (RMITimeoutException rmie) {
      throw new RuntimeException("Timeout waiting on server", rmie);
    } catch (RemoteException e) {
      throw new TuplesException("Can't get remote row count", e);
    }
  }

  public long getRowUpperBound() throws TuplesException {
    try {
      waitForPrefetchThread();
      assert prefetchThread == null || prefetchThread.hasFinished();
      return remoteAnswer.getRowUpperBound();
    } catch (RMITimeoutException rmie) {
      throw new RuntimeException("Timeout waiting on server", rmie);
    } catch (RemoteException e) {
      throw new TuplesException("Can't get remote row upper bound", e);
    }
  }

  public long getRowExpectedCount() throws TuplesException {
    try {
      waitForPrefetchThread();
      assert prefetchThread == null || prefetchThread.hasFinished();
      return remoteAnswer.getRowExpectedCount();
    } catch (RMITimeoutException rmie) {
      throw new RuntimeException("Timeout waiting on server", rmie);
    } catch (RemoteException e) {
      throw new TuplesException("Can't get remote expected row count", e);
    }
  }

  public int getRowCardinality() throws TuplesException {
    try {
      waitForPrefetchThread();
      assert prefetchThread == null || prefetchThread.hasFinished();
      return remoteAnswer.getRowCardinality();
    } catch (RMITimeoutException rmie) {
      throw new RuntimeException("Timeout waiting on server", rmie);
    } catch (RemoteException e) {
      throw new TuplesException("Can't get remote row cardinality", e);
    }
  }


  public boolean isEmpty() throws TuplesException {
    try {
      waitForPrefetchThread();
      assert prefetchThread == null || prefetchThread.hasFinished();
      return remoteAnswer.isEmpty();
    } catch (RMITimeoutException rmie) {
      throw new RuntimeException("Timeout waiting on server", rmie);
    } catch (RemoteException e) {
      throw new TuplesException("Can't get remote isEmpty", e);
    }
  }
  /**
   * Move to the next row.
   *
   * If no such row exists, return <code>false<code> and the current row
   * becomes unspecified.  The current row is unspecified when an
   * instance is created.  To specify the current row, the
   * {@link #beforeFirst()} method must be invoked.
   * Behaviour is undefined if next() is called again after it returns <code>false</code>.
   *
   * @return whether a subsequent row exists
   * @throws IllegalStateException if the current row is unspecified.
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean next() throws TuplesException {
    try {
      // Check if there is a current page, if there is then move to the next and check for validity
      if (currentPage == null || !currentPage.nextInPage()) {

        // no valid page.  Get the next page and initialise.

        // moving onto the first page only if there was no current page
        onFirstPage = (currentPage == null);

        // move to the next page
        if (currentPage != null && currentPage.isLastPage()) {
          currentPage = null;
        } else {
          currentPage = nextPage();
        }

        // Move to the first item in the returned page
        if (currentPage != null) {
          boolean test = currentPage.nextInPage();
          assert test || ! (currentPage instanceof AnswerPageImpl);
          // instances of AnswerPageImpl should be null if it contains no valid rows
        } else {
          // no valid page: if it was the first page, then turn the flag off
          onFirstPage = false;
        }

      }
      // Return true if we have a current valid page (page can't be finished at this point)
      return currentPage != null;
    } catch (RemoteException e) {
      throw new TuplesException("Can't advance remote cursor", e);
    } catch (RMITimeoutException te) {
      throw new TuplesException("Can't get to next page of answers", te);
    }
  }

  /**
   * Retrieves the next page from remoteAnswer, and starts a new thread to prefetch the
   * page that comes in next.
   *
   * @return The next page from the answer.
   */
  protected AnswerPage nextPage() throws RMITimeoutException, TuplesException, RemoteException {

    waitForPrefetchThread();
    assert prefetchThread == null || prefetchThread.hasFinished();

    AnswerPage page = null;

    if (prefetchThread != null) {
      // a finished thread exists
      page = prefetchThread.getPendingPage();
    } else {
      // no old thread
      page = remoteAnswer.nextPage();
    }
    // launch new prefetch thread
    prefetchThread = new PrefetchThread();

    return page;
  }

  /**
   * Wait on the prefetched page if it is already coming in.
   *
   * @throws RMITimeoutException
   */
  private void waitForPrefetchThread() throws RMITimeoutException {

    if (prefetchThread != null) {
      try {
        prefetchThread.join(timeout);
      } catch (InterruptedException ie) {
        // Not concerned about interruptions, only in finishing
      }
      if (!prefetchThread.hasFinished()) {
        // abandon the joining thread
        prefetchThread = null;
        throw new RMITimeoutException("No data returned within " + timeout + "ms");
      }
    }
  }

  /**
   * Clean up the remote object if it has not already been done.
   */
  protected void finalize() throws Throwable {
    try {
      if (remoteAnswer != null) remoteAnswer.close();
    } finally {
      remoteAnswer = null;
      super.finalize();
    }
  }

  /**
   * Thread to prefetch the next page from the remoteAnswer
   */
  private class PrefetchThread
      extends Thread {

    /** The page to be fetched */
    private AnswerPage page;

    /** Flag indicating that this thread has completed its task */
    private boolean finished;

    /** Stack Trace for client-side invokation */
    private final StackTrace caller;

    /**
     * Main constructor.  Starts the current thread.
     */
    public PrefetchThread() {

      this.caller = logger.isDebugEnabled() ? new StackTrace() : null;
      page = null;
      finished = false;
      start();
    }

    /**
     * The main code in this thread.  Simply requests a new page.
     */
    public void run() {
      try {
        page = remoteAnswer.nextPage();
        finished = true;
      } catch (Exception e) {
        // finished will never be set

        // log exception and include the stack trace that created this Thread.
        logger.warn("Exception thrown in prefetchThread.");
        if (caller != null && logger.isDebugEnabled()) logger.debug("Prefetch caller: " + caller);
        logger.warn("Caused by", e);
      }
    }

    /**
     * Indicates if the thread has run to successful completion
     *
     * @return <code>true</code> if the thread has finished.
     */
    public boolean hasFinished() {
      return finished;
    }

    /**
     * Returned the prefetched page.
     *
     * @return The page that was fetched by this thread.
     */
    public AnswerPage getPendingPage() {
      if (!finished) {
        throw new IllegalStateException(
            "Unable to request pages until the prefetch thread has completed");
      }
      return page;
    }

  }

}
