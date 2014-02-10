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
import java.io.*;
import java.sql.*;

// Log4J
import org.apache.log4j.*;

/**
 * Rows within the result set.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Andrew Newman
 *
 * @created 2001-07-12
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
public class ResultSetRow implements Comparable<ResultSetRow>, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 6269054577611383745L;

  /** Logger. */
  private static final Logger logger = Logger.getLogger(ResultSetRow.class.getName());

  /**
   * The column entries for this row.
   */
  private Object[] columns;

  /**
   * The names of the columns.
   */
  private String[] columnNames;

  /**
   * Create a result set row based on the metadata from the given ResultSet
   * object.
   *
   * @param resultSet the ResultSet that we are using to get the metadata to
   *      create the column names.
   */
  public ResultSetRow(ResultSet resultSet) {

    try {

      // Get the number of columns from the metadata
      ResultSetMetaData metadata = resultSet.getMetaData();
      int noColumns = metadata.getColumnCount();

      // Initialize arrays
      columnNames = new String[noColumns];
      columns = new Object[noColumns];

      // Set column names
      for (int index = 1; index <= noColumns; index++) {

        columnNames[index - 1] = metadata.getColumnName(index);
      }
    }
    catch (SQLException se) {

      logger.fatal("Failed to get columns or number of columns", se);
    }
  }

  /**
   * Create a row at the end of the result set, populated with
   * <code>null</code>.
   *
   * @param newColumnNames the new column names for the row.
   * @param newColumns the results of the row.
   */
  public ResultSetRow(String[] newColumnNames, Object[] newColumns) {

    columnNames = newColumnNames;
    columns = newColumns;
  }

  /**
   * Sets new column values.
   *
   * @param newColumns entries for this row.
   */
  public void setColumns(Object[] newColumns) {

    columns = newColumns;
  }

  /**
   * Sets the given column index with the given value.
   *
   * @param columnIndex the index into the row.
   * @param value the value of the column.
   */
  public void setInt(int columnIndex, int value) {

    setObject(columnIndex, new Integer(value));
  }

  /**
   * Sets the given column name with the given value.
   *
   * @param columnName the name of the column to set.
   * @param value the value of the column.
   */
  public void setInt(String columnName, int value) {

    setInt(columnForName(columnName), value);
  }

  /**
   * Set a field to a Java {@link Object} value. In order to allow {@link
   * MemoryResultSet} to be {@link Serializable}, we require that all object that
   * appear as field values are also {@link Serializable}.
   *
   * @param columnIndex the index into the column to get the object.
   * @param value a {@link Serializable} object
   * @throws IllegalArgumentException if <var>value</var> isn't {@link
   *      Serializable}
   */
  public void setObject(int columnIndex, Object value) {

    // Validate "value" parameter
    if ( (value != null) && ! (value instanceof Serializable)) {

      throw new IllegalArgumentException("Non-serializable value: " + value);
    }

    try {

      columns[columnIndex - 1] = value;
    }
    catch (ArrayIndexOutOfBoundsException e) {

      logger.error("columnIndex: " + columnIndex, e);
      throw e;
    }
  }

  /**
   * Set a field based on a column name.
   *
   * @param columnName the name of the column to set.
   * @param value a {@link Serializable} object
   * @throws IllegalArgumentException if <var>value</var> isn't {@link
   *      Serializable}
   */
  public void setObject(String columnName, Object value) {

    setObject(columnForName(columnName), value);
  }

  /**
   * Returns an int value from the given column index - if possible.
   *
   * @param columnIndex the index in the column to return the data.
   * @return an int value from the given column index - if possible.
   * @throws SQLException if the column is not an integer.
   */
  public int getInt(int columnIndex) throws SQLException {

    Object object = getObject(columnIndex);

    if (! (object instanceof Integer)) {

      throw new SQLException("Column " + columnIndex + " isn't an integer");
    }

    return ( (Integer) object).intValue();
  }

  /**
   * Returns an int value from the given column name - if possible.
   *
   * @param columnName the name of the column to return the data.
   * @return an int value from the given column index - if possible.
   * @throws SQLException if the column is not an integer.
   */
  public int getInt(String columnName) throws SQLException {

    return getInt(columnForName(columnName));
  }

  /**
   * Returns the object value based on the given column index.
   *
   * @param columnIndex the index into the column to get the object.
   * @return the object's value.
   */
  public Object getObject(int columnIndex) {

    try {

      return columns[columnIndex - 1];
    }
    catch (ArrayIndexOutOfBoundsException e) {

      logger.error("columnIndex: " + columnIndex);
      throw e;
    }
  }

  /**
   * Returns the object value based on the column name.
   *
   * @param columnName name of the column.
   * @return the object's value.
   */
  public Object getObject(String columnName) {

    return getObject(columnForName(columnName));
  }

  /**
   * Returns true if the object is of the same type, the same length and its
   * column values are equal.
   *
   * @param object the object to test for equality.
   * @return true if the object is of the same type, the same length and its
   *      column values are equal.
   */
  public boolean equals(Object object) {

    // The classes must match
    if (object == null) {

      return false;
    }

    if (!object.getClass().equals(ResultSetRow.class)) {

      return false;
    }

    // Rows must be the same length
    ResultSetRow row = (ResultSetRow) object;

    if (columns.length != row.columns.length) {

      return false;
    }

    // Column entries aren't allowed to mismatch
    for (int i = 0; i < columns.length; i++) {

      if ( (row.columns[i] == null) ? (columns[i] != null)
          : (!row.columns[i].equals(columns[i]))) {

        return false;
      }
    }

    // The rows must be equal if we got this far
    return true;
  }

  /**
   * Returns and XOR of all the column of this row.
   *
   * @return the XOR of all the columns of this row
   */
  public int hashCode() {

    int hashCode = 0;

    for (int i = 0; i < columns.length; i++) {

      if (columns[i] != null) {

        hashCode ^= columns[i].hashCode();
      }
    }

    return hashCode;
  }

  /**
   * Returns a string representation of the row displaying the column name and
   * the value stored in them.
   *
   * @return a string representation of the row displaying the column name and
   *      the value stored in them.
   */
  public String toString() {

    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < columns.length; i++) {

      buffer.append("\t").append(columnNames[i]).append("=").append(columns[i]);
    }

    return buffer.toString();
  }

  /**
   * Returns a negative number if the object is less than the given object, a
   * positive number if larger and 0 if equal. Accepts an object of the correct
   * type.
   *
   * @param row the row object to compare it.
   * @return a negative number if the object is less than the given object, a
   *      positive number if larger and 0 if equal.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public int compareTo(ResultSetRow row) {

    // Longer result sets are larger
    if (row.columns.length != columns.length) {
      return columns.length - row.columns.length;
    }

    for (int i = 0; i < columns.length; i++) {

      if (columns[i] == null) {
        // nulls come after other values
        return (row.columns[i] == null) ? 0 : 1;
      } else if (!(columns[i] instanceof Comparable)) {
        throw new RuntimeException(
            "Badly implemented code can't cope with non-Comparable " +
            columns[i] + " of type " + columns[i].getClass());
      }

      int result;

      try {

        result = ((Comparable)columns[i]).compareTo(row.columns[i]);
      } catch (ClassCastException e) {

        logger.warn("Incomparable rows", e);
        result =
            columns[i].getClass().toString().compareTo(row.columns[i].getClass()
            .toString());
      }

      if (result != 0) {

        return result;
      }
    }

    // The rows must be equal
    return 0;
  }

  /**
   * Returns the index of the given column name or throws an
   * IllegalArgumentException if not found.
   *
   * @param columnName the name of the column to search for.
   * @return the index into the array (beginning at 1 not 0) where the column is
   *      located.
   * @throws java.lang.IllegalArgumentException if the given column name does
   *      not exist.
   * @throws IllegalArgumentException EXCEPTION TO DO
   */
  private int columnForName(String columnName) throws IllegalArgumentException {

    for (int i = 0; i < columnNames.length; i++) {

      if (columnNames[i].equals(columnName)) {

        return i + 1;
      }
    }

    throw new IllegalArgumentException("No such column " + columnName);
  }
}
