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
import java.sql.*;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;
import org.mulgara.util.ResultSetRow;
import org.mulgara.util.MemoryResultSet;

/**
 * ITQL answer. An answer is a set of solutions, where a solution is a mapping
 * of {@link Variable}s to {@link Value}s.
 *
 * @created 2001-07-31
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $
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
public class AnswerImpl extends AbstractAnswer implements Answer, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
   static final long serialVersionUID = -8022357347937695884L;

  /** Description of the Field */
  private final static MemoryResultSet ZERO;

  /** Description of the Field */
  public final static Answer EMPTY;

  /** Logger. This is named after the class. */
  private final static Logger logger = Logger.getLogger(AnswerImpl.class);

  static {
    try {
      ZERO  = new MemoryResultSet(new String[] {});
      EMPTY = new AnswerImpl(ZERO);
    } catch (TuplesException e) {
      throw new ExceptionInInitializerError(e);
    } catch (SQLException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  /** The columns of the result set. */
  public Variable[] variables;

  /** The wrapped {@link ResultSet}. */
  private ResultSet resultSet;

  /**
   * Copy constructor.  TODO: when Answer becomes immutable, we won't need
   * by-value copies
   *
   * @param answer the answer to duplicate; <code>null</code> value disallowed
   * @throws IllegalArgumentException if <var>answer</var> is <code>null</code>
   */
  public AnswerImpl(Answer answer) throws TuplesException {

    // Validate "answer" parameter
    if (answer == null) throw new IllegalArgumentException("Null \"answer\" parameter");
    logger.debug("Creating AnswerImpl ");
    // Copy the variables
    variables = answer.getVariables();

    // Convert variables to column name array
    String[] columnNames = new String[variables.length];
    for (int i=0; i<variables.length; i++) columnNames[i] = variables[i].getName();

    // Copy the content
    if (logger.isDebugEnabled()) logger.debug("Adding "+answer.getRowCount()+" rows");

    try {
      resultSet = new MemoryResultSet(columnNames);
      answer.beforeFirst();
      logger.debug("Reset source / Iterating Answer");
      while (answer.next()) {
        logger.debug("Creating a row");
        Object[] columnValues = new Object[answer.getNumberOfVariables()];
        logger.debug("Populating a row");
        try {
          for (int i = 0; i < columnValues.length; i++) {
            columnValues[i] = answer.getObject(i);
            if (columnValues[i] instanceof Answer && !(columnValues[i] instanceof Serializable)) {
              Answer ans = (Answer)columnValues[i];
              columnValues[i] = new AnswerImpl(ans);
              try {
                ans.close();
              } catch (TuplesException et) {
                logger.error("TuplesException thrown in AnswerImpl.close()", et);
                throw et;
              } catch (RuntimeException rt) {
                logger.error("RuntimeException thrown in AnswerImpl.close()", rt);
                throw rt;
              }
            }
          }
        } catch (RuntimeException re) {
          logger.error("Runtime Exception thrown in loop", re);
          throw re;
        }
        logger.debug("Adding a row");
        ((MemoryResultSet)resultSet).addRow(new ResultSetRow(columnNames, columnValues));
      }
      logger.debug("Finished iterating answer");
    } catch (SQLException e) {
      logger.warn("Failed to copy content", e);
      throw new TuplesException("Couldn't copy answer", e);
    }
    if (logger.isDebugEnabled()) logger.warn("Completed construction of " + getRowCount() + " rows");
  }

  /**
   * Empty constructor. This produces an answer that explicitly binds 0 results
   * to the specified variables.
   *
   * @param variableList a list of {@link Variable}s
   * @throws IllegalArgumentException if <var>variableList</var> is <code>null</code>
   */
  public AnswerImpl(List<Variable> variableList) {

    String[] columns = new String[variableList.size()];
    int j = 0;

    for (Iterator<Variable> i = variableList.iterator(); i.hasNext(); j++) {
      columns[j++] = i.next().getName();
    }

    try {
      resultSet = new MemoryResultSet(columns);
    } catch (SQLException e) {
      throw new Error("Inexplicable constructor failure", e);
    }
  }

  /**
   * Construct an answer from an SQL result set.
   *
   * @param resultSet PARAMETER TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public AnswerImpl(ResultSet resultSet) throws TuplesException {

    if (resultSet == null) throw new IllegalArgumentException("Null \"resultSet\" parameter");

    try {
      this.variables = resultSetToVariables(resultSet);
      // this.resultSet = new MemoryResultSet(resultSet);
      this.resultSet = resultSet;
    } catch (SQLException e) {
      throw new TuplesException("Couldn't create answer", e);
    }
  }

  /**
   * The default constructor produces an answer with zero rows and zero columns,
   * the algebraic zero for the append/join field.
   *
   */
  protected AnswerImpl() {
    variables = new Variable[] {};
    resultSet = ZERO;
  }

  /**
   * @deprecated The internal result set shouldn't really be exposed
   */
  public ResultSet getResultSet() {
    return resultSet;
  }

  //
  // Methods implementing the Answer interface
  //

  public long getRowUpperBound() throws TuplesException {
    return getRowCount();
  }

  public long getRowExpectedCount() throws TuplesException {
    return getRowCount();
  }

  public int getRowCardinality() throws TuplesException {
    switch ((int)getRowCount()) {
      case 0:
        return Cursor.ZERO;
      case 1:
        return Cursor.ONE;
      default:
        return Cursor.MANY;
    }
  }

  /* (non-Javadoc)
   * @see org.mulgara.query.Cursor#isEmpty()
   */
  public boolean isEmpty() throws TuplesException {
    return getRowCardinality() == Cursor.ZERO;
  }

  /**
   * How many solution terms does this answer comprise?
   *
   * @return the number of minterms (rows) in this sum of products
   * @throws TuplesException if the number of rows cannot be determined
   */
  public long getRowCount() throws TuplesException {
    try {
      int row = resultSet.getRow();

      try {
        resultSet.last();
        return resultSet.getRow();
      } finally {
        if (row == 0) {
          try {
            resultSet.beforeFirst();
          } catch (SQLException e) {
            logger.warn("Couldn't reset cursor to before first row", e);
          }
        } else {
          if (!resultSet.absolute(row)) {
            logger.warn("Couldn't reset cursor to row " + row);
          }
        }
      }
    } catch (SQLException e) {
      throw new TuplesException("Couldn't determine solution count", e);
    }
  }

  /**
   * Gets the Object attribute of the AnswerImpl object
   *
   * @param column PARAMETER TO DO
   * @return The Object value
   * @throws TuplesException EXCEPTION TO DO
   */
  public Object getObject(int column) throws TuplesException {

    try {
      return resultSet.getObject(column+1);
    } catch (SQLException e) {
      throw new TuplesException("Couldn't read field", e);
    }
  }

  /**
   * Gets the Object attribute of the AnswerImpl object
   *
   * @param columnName PARAMETER TO DO
   * @return The Object value
   * @throws TuplesException EXCEPTION TO DO
   */
  public Object getObject(String columnName) throws TuplesException {

    try {
      return resultSet.getObject(columnName);
    } catch (SQLException e) {
      throw new TuplesException("Couldn't read field: " + columnName, e);
    }
  }

  /**
   * Gets the variables name of the answer object - starting at 0.
   *
   * @param column the column number columns start at 0.
   * @return the {@link Variable}s bound within this answer.
   * @throws TuplesException if there was an error accessing the variable.
   */
  public Variable getVariable(int column) throws TuplesException {

    try {
      return new Variable(resultSet.getMetaData().getColumnName(column + 1));
    } catch (SQLException e) {
      throw new TuplesException("Couldn't get variable for column " + (column + 1), e);
    }
  }

  /**
   * Returns a single varieable bound to the column - starting at 1.
   *
   * @return the {@link Variable}s bound within this answer.
   */
  public Variable[] getVariables() {
    return variables;
  }

  /**
   * Returns the number of variables in a tuples.
   *
   * @return the number of variables in a tuples.
   */
  public int getNumberOfVariables() {
    int noVars = 0;
    if (variables != null) noVars = variables.length;
    return noVars;
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
    return (getVariables().length == 0) && (getRowCardinality() > Cursor.ZERO);
  }

  //
  // Methods implementing the Tuples interface
  //

  /**
   * Gets the ColumnIndex attribute of the AnswerImpl object
   *
   * @param column PARAMETER TO DO
   * @return The ColumnIndex value
   * @throws TuplesException EXCEPTION TO DO
   */
  public int getColumnIndex(Variable column) throws TuplesException {

    // Validate "column" parameter
    if (column == null) throw new IllegalArgumentException("Null \"column\" parameter");

    // Look for the requested variable in the "variables" array
    for (int i = 0; i < variables.length; i++) {
      if (column.equals(variables[i])) return i;
    }

    // Couldn't find the requested variable
    throw new TuplesException("No such column " + column);
  }

  /**
   * Reset the cursor. Calling {@link #next()} after calling this method will move to the first row.
   *
   * @throws TuplesException There was an error accessing the underlying data.
   */
  public void beforeFirst() throws TuplesException {

    try {
      resultSet.beforeFirst();
    } catch (SQLException e) {
      throw new TuplesException("Couldn't rewind solution", e);
    }
  }

  /**
   * Advance to the next term in the solution.
   *
   * @return <code>false<code> if there was no further term to advance to.
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean next() throws TuplesException {
    try {
      return resultSet.next();
    } catch (SQLException e) {
      throw new TuplesException("Couldn't advance cursor", e);
    }
  }

  //
  // Methods overriding the Object superclass
  //

  /**
   * Equality.
   *
   * @param object the object to compare against for equality, possibly <code>null</code>
   * @return whether <var>object</var> is equal to this instance; the order of
   *         both columns and rows is significant
   */
  public boolean equals(Object object) {
    // Gotta be non-null and of matching type
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

  /**
   * Generate a legible representation of the answer.
   *
   * @return a string representation of the results.
   */
  public String toString() {

    return resultSet.toString();
  }

  /**
   * Creates a copy of this object and its resources.
   *
   * @return A new, independent copy of this object.
   */
  public Object clone() {
    try {
      AnswerImpl a = (AnswerImpl)super.clone();
      a.resultSet = new MemoryResultSet(resultSet);
      return a;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Release all resources associated with this object
   */
  public void close() {
    // resultSet = null;
    // variables = null;
  }

  /**
   * Convert the column names of a result set to an array of Variables.
   *
   * @param resultSet The ResultSet to convert.
   * @return An array of variables corresponding to the columns resultSet.
   * @throws SQLException An error accessing the ResultSet metadata.
   */
  private Variable[] resultSetToVariables(ResultSet resultSet) throws SQLException {

    ResultSetMetaData rsmd = resultSet.getMetaData();
    Variable[] variables = new Variable[rsmd.getColumnCount()];

    for (int i = 0; i < rsmd.getColumnCount(); i++) {
      variables[i] = new Variable(rsmd.getColumnName(i + 1));
    }

    return variables;
  }
}
