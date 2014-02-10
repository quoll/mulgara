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

package org.mulgara.resolver;

// Java 2 standard packages
import java.util.Arrays;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.AbstractAnswer;
import org.mulgara.query.Answer;
import org.mulgara.query.AnswerOperations;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.Tuples;

/**
 * Wrapper around database-local {@link Tuples} instances, converting them
 * into globally valid {@link Answer}s.
 *
 * @created 2003-10-28
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:23 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2003-2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class GlobalizedAnswer extends AbstractAnswer implements Answer, Cloneable {
  /** Logger.  */
  private final static Logger logger =
      Logger.getLogger(GlobalizedAnswer.class.getName());

  /** The session against which globalization occurs.  */
  protected ResolverSession resolverSession;

  /** The wrapped {@link Tuples} instance.  */
  protected Tuples tuples;

  /** Have we reached the last tuple from the {@link #tuples}?  */
  protected boolean beforeEnd;

  /**
   * Construct an {@link Answer} by wrapping {@link Tuples}.
   *
   * @param tuples  the local result to wrap
   * @param resolverSession  the session used to globalize the <var>tuples</var>
   * @throws IllegalArgumentException  if the <var>tuples</var> or
   *   <var>resolverSession</var> are <code>null</code>
   */
  GlobalizedAnswer(Tuples tuples, ResolverSession resolverSession) {
    // Validate "tuples" parameter
    if (tuples == null) {
      throw new IllegalArgumentException("Null \"tuples\" parameter");
    }

    // Validate "resolverSession" parameter
    if (resolverSession == null) {
      throw new IllegalArgumentException("Null \"resolverSession\" parameter");
    }

    // Initialize fields
    if (logger.isDebugEnabled()) {
      try {
        logger.debug("Globalizing " + tuples.getRowCount() + " rows");
      } catch (TuplesException e) {
        logger.debug("Globalizing indeterminate number of rows");
      }
    }

    // Initialize fields
    this.beforeEnd = true;
    this.resolverSession = resolverSession;
    this.tuples = (Tuples) tuples.clone();

    // Tuples require that beforeFirst is called before use, but Answers don't
    // so make sure our tuples are initialized
    try {
      this.tuples.beforeFirst();
    } catch (TuplesException e) {
      logger.warn("Unable to reset tuples", e);
    }
  }

  //
  // Methods implementing Cursor superinterface of Answer
  //

  public void beforeFirst() throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("Resetting " + getRowCount() + " rows of " +
          tuples.getClass() + " with columns " +
          Arrays.asList(tuples.getVariables()));
    }

    tuples.beforeFirst();
    beforeEnd = true;
  }

  public void close() throws TuplesException {
    tuples.close();
  }

  public int getColumnIndex(Variable variable) throws TuplesException {
    return tuples.getColumnIndex(variable);
  }

  public int getNumberOfVariables() {
    return tuples.getNumberOfVariables();
  }

  public long getRowCount() throws TuplesException {
    return tuples.getRowCount();
  }

  public long getRowUpperBound() throws TuplesException {
    return tuples.getRowUpperBound();
  }

  public long getRowExpectedCount() throws TuplesException {
    return tuples.getRowExpectedCount();
  }

  public int getRowCardinality() throws TuplesException {
    return tuples.getRowCardinality();
  }

  public boolean isEmpty() throws TuplesException {
    return tuples.isEmpty();
  }

  public Variable[] getVariables() {
    return tuples.getVariables();
  }

  public boolean isUnconstrained() throws TuplesException {
    return tuples.isUnconstrained();
  }

  public boolean next() throws TuplesException {
    if (beforeEnd) {
      beforeEnd = tuples.next();
    }

    return beforeEnd;
  }

  //
  // Methods implementing Answer
  //

  public Object getObject(int column) throws TuplesException {
    try {
      long l = tuples.getColumnValue(column);
      return (l == Tuples.UNBOUND) ? null : resolverSession.globalize(l);
    } catch (GlobalizeException e) {
      throw new TuplesException("Couldn't globalize column " + column, e);
    }
  }

  public Object getObject(String columnName) throws TuplesException {
    return getObject(getColumnIndex(new Variable(columnName)));
  }

  public Object clone() {
    // Copy immutable fields by reference
    GlobalizedAnswer cloned = (GlobalizedAnswer)super.clone();
    assert cloned != null;

    // Copy mutable fields by value
    cloned.tuples = (Tuples) tuples.clone();

    return cloned;
  }

  public boolean equals(Object object) {
    if ((object != null) && (object instanceof Answer)) {
      try {
        return AnswerOperations.equal(this, (Answer) object);
      } catch (TuplesException e) {
        logger.fatal("Couldn't test equality of answers", e);
      }
    }
    return false;
  }

  /**
   * Added to match {@link #equals(Object)}.
   */
  public int hashCode() {
    return super.hashCode();
  }
}
