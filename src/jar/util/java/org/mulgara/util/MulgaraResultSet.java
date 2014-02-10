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

package org.mulgara.util;

// Java 2 standard packages
import java.sql.*;
import java.util.*;

/**
 * Implementation of {@link ResultSet} suitable for generating test cases. It's
 * not a correct {@link ResultSet} in many respects, the foremost being an
 * absence of column typing.
 *
 * @created 2001-07-12
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Andrew Newman
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:29 $
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
public interface MulgaraResultSet extends ResultSet {

  /**
   * Adds a new row to the current set of rows.
   *
   * @param row new row to add to the end of the queue.
   */
  public void addRow(ResultSetRow row);

  /**
   * Returns the entire rows underlying the result set.
   *
   * @return the entire rows underlying the result set.
   * @throws SQLException EXCEPTION TO DO
   */
  public List<ResultSetRow> getAllRows() throws SQLException;

  /**
   * Overwrites the existing set of rows available.
   *
   * @param newRows the new rows to set.
   * @throws SQLException EXCEPTION TO DO
   */
  public void setAllRows(List<ResultSetRow> newRows) throws SQLException;

  /**
   * Returns the total size of the number of rows.
   *
   * @return the total size of the number of rows available.
   */
  public int getTotalRowSize();

  /**
   * Perform a <dfn>natural join</dfn> between this result set and another. The
   * join will be performed based on matching column names. See Elmasri &amp;
   * Navathe, <cite>Fundamentals of Database Systems</cite> , p. 158.
   *
   * @param resultSet the other result set to join with
   * @return the result of the join operation
   * @throws SQLException if the join fails
   */
  //public MulgaraResultSet join(MulgaraResultSet resultSet) throws SQLException;

  /**
   * Perform a natural join between this result set and another, specifying the
   * column names upon which to join.
   *
   * @param resultSet the other result set to join with
   * @param columnNames the list of column names in <code>this</code> on which
   *      to join; all the named columns must occur in <code>this</code>, and
   *      the length of the array must match <var>resultSetColumnNames</var>
   * @param resultSetColumnNames the list of column names in <var>resultSet
   *      </var> on which to join; all the named columns must occur in <var>
   *      resultSet</var> , and the length of the array must match <var>
   *      columnNames</var>
   * @return the result of the join operation
   * @throws IllegalArgumentException if the <var>columnName</var> and <var>
   *      resultSetColumnNames</var> arguments are incompatible, or if any of
   *      the arguments are <code>null</code>
   * @throws SQLException if the join fails
   */
  //public MulgaraResultSet join(MulgaraResultSet resultSet, String[] columnNames,
  //    String[] resultSetColumnNames) throws SQLException;

  /**
   * Truncate trailing rows.
   *
   * @param limit the maximum number of rows to retain
   */
  //public void limit(int limit);

  /**
   * Truncate leading rows.
   *
   * @param offset the number of leading rows to truncate
   */
  //public void offset(int offset);

  /**
   * Perform a relational algebra <dfn>project</dfn> operation. This operation
   * filters columns out of result sets. The columns to be retained are
   * specified by name.
   *
   * @param columnNames the column names to retain, all of which must exist in
   *      this result set
   * @return a result set containing only columns named in <code>columnNames</code>
   * @throws SQLException if the projection fails
   */
  //public MulgaraResultSet project(String[] columnNames) throws SQLException;

  /**
   * Perform a relational algebra <dfn>project</dfn> operation. This operation
   * filters columns out of result sets. The columns to be retained are
   * specified by name.
   *
   * @param columnNames the column names to retain, which may or may not exist
   *      in this result set
   * @return a result set containing only columns named in <code>columnNames</code>
   * @throws SQLException if the projection fails
   */
  //public MulgaraResultSet project2(String[] columnNames) throws SQLException;

  /**
   * Perform a relational algebra <dfn>self join</dfn> operation. This operation
   * filters duplicate rows out of result sets. In this implementation, order is
   * not preserved.
   *
   */
  //public void removeDuplicateRows();

  /**
   * Sort according to a passed comparator.
   *
   * @param comparator a comparator for {@link ResultSetRow}s
   */
  //public void sort(Comparator comparator);

  /**
   * Test this result set for equality with another, ignoring any differences
   * between row ordering. Column ordering is still significant.
   *
   * @param object the result set to check to see if it is equal.
   * @return true if the result set is equal ignoring row order.
   */
  //public boolean equalsIgnoreOrder(Object object);

  /**
   * Returns the array of column names given the result set metadata.
   *
   * @return any array (starting at 0) of the column names.
   * @throws SQLException if there was an error getting the column count or
   *      other problem from the metadata.
   */
  public String[] getColumnNames() throws SQLException;

  /**
   * Gets the CurrentRow attribute of the MulgaraResultSet object
   *
   * @return The CurrentRow value
   */
  public ResultSetRow getCurrentRow();

  /**
   * Returns the index to the column based on its name.
   *
   * @param columnName the name of the column to search for.
   * @return the index (starting at 1) of the column.
   * @throws IllegalArgumentException if the column was not found.
   * @throws SQLException if there was a problem getting the column names.
   */
  public int columnForName(String columnName) throws IllegalArgumentException,
      SQLException;
}
