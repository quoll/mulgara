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

/**
* Disjunction of conjunctions with serial access to each of the disjoined
* terms.
*
* A cursor is a set of solutions, where a solution is a mapping of
* {@link Variable}s to some type specified by the subinterface.
* This is similar to a JDBC {@link java.sql.ResultSet}.
*
* @created 2004-03-12
*
* @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
*
* @version $Revision: 1.9 $
*
* @modified $Date: 2005/01/28 00:28:37 $
*
* @maintenanceAuthor $Author: newmana $
*
* @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
*
* @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
*      Software Pty Ltd</a>
*
* @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
*/
public interface Cursor {

  public static final int ZERO = 0;
  public static final int ONE  = 1;
  public static final int MANY = 2;

  /**
   * Reset to iterate through every single element.
   *
   * @throws TuplesException Due to an error accessing the underlying data.
   */
  public void beforeFirst() throws TuplesException;

  /**
   * Free resources associated with this instance.
   *
   * @throws TuplesException Due to an error accessing the underlying data.
   */
  public void close() throws TuplesException;

  /**
   * Find the index of a variable.
   *
   * @param column The variable to search for.
   * @return The ColumnIndex value
   * @throws TuplesException If the variable is null or not in this tuples.
   */
  public int getColumnIndex(Variable column) throws TuplesException;

  /**
   * Returns the number of variables (columns).
   *
   * @return the number of variables (columns)
   */
  public int getNumberOfVariables();

  /**
   * The variables bound and their default collation order. The array returned
   * by this method should be treated as if its contents were immutable, even
   * though Java won't enforce this. If the elements of the array are modified,
   * there may be side effects on the past and future clones of the tuples it
   * was obtained from.
   *
   * @return the {@link Variable}s bound within this answer.
   */
  public Variable[] getVariables();

  /**
   * Tests whether this is a unit-valued answer. A unit answer appended to
   * something yields the unit answer. A unit answer joined to something yields
   * the same something. Notionally, the unit answer has zero columns and one
   * row.
   *
   * @return The Unconstrained value
   * @throws TuplesException Due to an error accessing the underlying data.
   */
  public boolean isUnconstrained() throws TuplesException;

  /**
   * This method returns the exact number of rows which this instance contains.
   *
   * @return The exact number of rows that this instance contains.
   * @throws TuplesException Due to an error accessing the underlying data.
   */
  public long getRowCount() throws TuplesException;

  /**
   * This method returns an upper bound on the number of rows which this instance contains.
   *
   * @return The upper bound of the number of rows that this instance contains.
   * @throws TuplesException Due to an error accessing the underlying data.
   */
  public long getRowUpperBound() throws TuplesException;


  /**
   * This method returns the expected number of rows which this instance contains.
   * This number should be updated statistically over time, when possible.
   *
   * @return An expected value for the rows.
   * @throws TuplesException Due to an error accessing the underlying data.
   */
  public long getRowExpectedCount() throws TuplesException;

  /**
   * This method returns cardinality of the number of rows which this instance contains.
   *
   * @return The cardinality of this tuples. {0,1,N} rows.
   * @throws TuplesException Due to an error accessing the underlying data.
   */
  public int getRowCardinality() throws TuplesException;

  /**
   * This method tests whether the cursor is known to be empty (i.e. have zero rows).
   * It is intended for optimization purposes, when an empty test can be performed
   * without actually evaluating the cursor.  It is possible for this method to
   * return <tt>false</tt> and still have zero rows, but if this method returns
   * <tt>true</tt> then it is guaranteed that the cursor has zero rows.
   * 
   * @return <tt>true</tt> if this cursor is known to contain no rows.
   * @throws TuplesException Due to an error accessing the underlying data.
   */
  public boolean isEmpty() throws TuplesException;
  
  /**
   * Move to the next row.
   *
   * If no such row exists, return <code>false<code> and the current row
   * becomes unspecified.  The current row is unspecified when an
   * instance is created.  To specify the current row, the
   * {@link #beforeFirst()} method must be invoked
   *
   * @return whether a subsequent row exists
   * @throws IllegalStateException if the current row is unspecified.
   * @throws TuplesException Due to an error accessing the underlying data.
   */
  public boolean next() throws TuplesException;
}
