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

package org.mulgara.query;

// Java 2 standard packages
import java.io.Serializable;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J

/**
 * An {@link Answer} which is true no matter what value any variable takes.
 *
 * Notionally, this is an {@link Answer} with zero columns and one row.
 *
 * @created 2004-03-22
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class UnconstrainedAnswer extends AbstractAnswer implements Answer, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 9046716742513870488L;

  /**
   * Logger.
   *
   * This is named after the class.
   */
  private static final Logger logger = Logger.getLogger(UnconstrainedAnswer.class);

  /**
   * The current row number.
   *
   * The first (and last, and only) row is 0.  Before the first row is -1.
   * After the last row is 1.  This field should never assume anything other
   * than these three values.
   */
  private int row = -1;

  /**
   * The value returns by the {@link #getVariables} method.
   *
   * This is a zero-length array.
   */
  private static final Variable[] variables = new Variable[] {};

  /**
   * Generate an unconstrained {@link Answer}.
   */
  public UnconstrainedAnswer() {
    // null implementation
  }

  //
  // Methods implementing Answer
  //

  /*
   * @throws TuplesException {@inheritDoc}
   */
  public void beforeFirst() throws TuplesException {
    row = -1;
  }

  /**
   * This instance has no resources to free.
   */
  public void close() {
    // null implementation
  }

  /**
   * @return {@inheritDoc}
   * @throws TuplesException {@inheritDoc}
   */
  public boolean next() throws TuplesException {
    switch (row) {
      case -1:
        row = 0;
        return true;
      case 0:
        row = 1;
        return false;
      case 1:
        throw new TuplesException("Already after last row");
      default:
        throw new Error("Impossible row value: " + row);
    }
  }

  /**
   * @param variable  {@inheritDoc}; note that because the unconstrained
   *   {@link Answer} has no columns, there's no valid value for this
   * @return never
   * @throws TuplesException  always, because there is no valid
   *   <var>variable</var> parameter
   */
  public int getColumnIndex(Variable variable) throws TuplesException {
    throw new TuplesException("No column " + variable);
  }

  /**
   * @return <code>0</code>
   */
  public int getNumberOfVariables() {
    return 0;
  }

  /**
   * @param column  a column value; note that because the unconstrained
   *   {@link Answer} has no columns, there's no valid value for this
   * @return never
   * @throws TuplesException  always, because there is no valid
   *   <var>column</var> parameter
   */
  public Object getObject(int column) throws TuplesException {
    throw new TuplesException("No column " + column);
  }

  /**
   * @param columnName  a column name; note that because the unconstrained
   *   {@link Answer} has no columns, there's no valid value for this
   * @return never
   * @throws TuplesException  always, because there is no valid
   *   <var>column</var> parameter
   */
  public Object getObject(String columnName) throws TuplesException {
    throw new TuplesException("No column named " + columnName);
  }

  /**
   * @return <code>1</code>
   */
  public long getRowCount() {
    return 1;
  }

  public long getRowUpperBound() {
    return getRowCount();
  }

  public long getRowExpectedCount() {
    return getRowCount();
  }

  public int getRowCardinality() {
    return Cursor.ONE;
  }

  public boolean isEmpty() throws TuplesException {
    return false;
  }

  /**
   * @return a zero-length array
   */
  public Variable[] getVariables() {
    return variables;
  }

  /**
   * @return <code>true</code>
   */
  public boolean isMaterialized() {
    return true;
  }

  /**
   * @return <code>true</code>
   */
  public boolean isUnconstrained() {
    return true;
  }

  //
  // Methods overriding the Object class
  //

  /**
   * @return {@inheritDoc}
   */
  public Object clone() {
    return (UnconstrainedAnswer)super.clone();
  }

  /**
   * @param object {@inheritDoc}
   * @return <code>true</code> if the <var>object</var> is another
   *   {@link Answer} which {@link #isUnconstrained}.
   */
  public boolean equals(Object object) {
    // Gotta be non-null and of matching type
    if ((object != null) && (object instanceof Answer)) {
      try {
        return AnswerOperations.equal(this, (Answer)object);
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

  /**
   * This currently tries to mimic the
   * {@link org.mulgara.util.MemoryResultSet#toString} method.
   *
   * @return <code>"0 columns: (1 rows)"</code>
   */
  public String toString() {
    return "0 columns: (1 rows)";
  }
}
