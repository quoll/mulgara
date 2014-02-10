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
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.sql.*;
import java.util.*;

// third party packages
import org.apache.log4j.Logger;

// Log4J

/**
 * Implementation of {@link ResultSet} in memory. This is particularly suitable
 * for generating test cases, though its use is more widespread. It's
 * not a correct {@link ResultSet} in many respects, the foremost being an
 * absence of column typing.
 *
 * @created 2001-07-12
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
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
public class MemoryResultSet extends AbstractMulgaraResultSet
  implements Serializable {

 /**
  * Allow newer compiled version of the stub to operate when changes
  * have not occurred with the class.
  * NOTE : update this serialVersionUID when a method or a public member is
  * deleted.
  */
  static final long serialVersionUID = 447953576074487320L;

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(MemoryResultSet.class);

  /**
   * The metadata for this result set.
   */
  private ResultSetMetaData metaData;

  /**
   * The rows of the result set.
   */
  private List<ResultSetRow> rows = new RowList<ResultSetRow>();

  /**
   * The current row index.
   */
  private int currentRowIndex = 0;

  /**
   * The current row.
   */
  private ResultSetRow currentRow = null;

  //
  // Constructors
  //

  /**
   * Create a result set with named columns and no rows.
   *
   * @param newColumnNames the new column names for this result set.
   * @throws SQLException on failure
   */
  public MemoryResultSet(String[] newColumnNames) throws SQLException {

    // FIXME: should validate columnNames, checking for duplicate names, etc
    // initialize fields
    columnNames = newColumnNames;
    metaData = new ResultSetMetaDataImpl(columnNames);

    // move the cursor before the first row
    beforeFirst();
  }

  /**
   * Create a result set with specified metadata and no rows.
   *
   * @param metaData PARAMETER TO DO
   * @throws SQLException on failure
   */
  public MemoryResultSet(ResultSetMetaData metaData) throws SQLException {

    initialiseMetaData(metaData);

    // move the cursor before the first row
    beforeFirst();
  }

  /**
   * Create a result set with content copied from an existing result set.
   *
   * @param resultSet PARAMETER TO DO
   * @throws SQLException on failure
   */
  public MemoryResultSet(ResultSet resultSet) throws SQLException {

    // Validate "resultSet" parameter
    if (resultSet == null) {
      throw new IllegalArgumentException("Null \"resultSet\" parameter");
    }

    // Copy columns
    initialiseMetaData(resultSet.getMetaData());

    // Copy rows
    if (resultSet.getClass() == MemoryResultSet.class) {
      rows.addAll( ( (MemoryResultSet) resultSet).rows);
    } else {

      // Don't assume that it hasn't already been read.
      resultSet.beforeFirst();

      // Repeat until we get all of the items from the result sets.
      while (resultSet.next()) {

        ResultSetRow row = new ResultSetRow(resultSet);

        for (int i = 0; i < columnNames.length; i++) {
          row.setObject(columnNames[i], resultSet.getObject(columnNames[i]));
        }

        addRow(row);
      }
    }
  }

  /**
   * Overwrites the existing set of rows available.
   *
   * @param newRows the new rows to set.
   * @throws SQLException EXCEPTION TO DO
   */
  public void setAllRows(List<ResultSetRow> newRows) throws SQLException {
    rows = newRows;
  }

  // next()

  /**
   * Return the object at the given index.
   *
   * @param columnIndex the index of the object to retrieve
   * @return the value at the given index
   * @throws SQLException on failure
   */
  public Object getObject(int columnIndex) throws SQLException {
    // throw an error if the current row is invalid
    if (this.currentRow == null) throw new SQLException("Not on a row.");

    // get the value out of the current row
    return this.currentRow.getObject(columnIndex);
  }

  // getObject()

  /**
   * Return the string at the given index.
   *
   * @param columnIndex the index of the string to retrieve
   * @return the value at the given index (possibly <code>null</code>)
   * @throws SQLException on failure
   */
  public String getString(int columnIndex) throws SQLException {

    // throw an error if the current row is invalid
    if (this.currentRow == null) throw new SQLException("Not on a row.");

    // get the value out of the current row
    Object object = this.currentRow.getObject(columnIndex);

    return (object == null) ? null : object.toString();
  }

  // getString()

  /**
   * Returns the metadata for this result set.
   *
   * @return the metadata for this result set
   * @throws SQLException on failure
   */
  public ResultSetMetaData getMetaData() throws SQLException {
    return metaData;
  }

  /**
   * Returns whether the cursor is before the first row.
   *
       * @return true if the cursor is before the first row in the result set, false
   *      otherwise
   * @throws SQLException on failure
   */
  public boolean isBeforeFirst() throws SQLException {
    return this.currentRowIndex == 0;
  }

  /**
   * Returns true if the cursor is after the last row.
   *
   * @return true if the cursor is after the first row in the result set, false
   *      otherwise
   * @throws SQLException on failure
   */
  public boolean isAfterLast() throws SQLException {
    return this.currentRowIndex > this.rows.size();
  }

  /**
   * Returns true if the cursor is on the first row.
   *
   * @return true if the cursor is on the first row, false otherwise
   * @throws SQLException on failure
   */
  public boolean isFirst() throws SQLException {
    return this.currentRowIndex == 1;
  }

  /**
   * Returns true if the cursor is on the last row.
   *
   * @return true if the cursor is on the las row, false otherwise
   * @throws SQLException on failure
   */
  public boolean isLast() throws SQLException {
    return this.currentRowIndex == this.rows.size();
  }

  /**
   * Returns the entire rows underlying the result set.
   *
   * @return the entire rows underlying the result set.
   * @throws SQLException EXCEPTION TO DO
   */
  public List<ResultSetRow> getAllRows() throws SQLException {
    return rows;
  }

  /**
   * Returns the index of the row the cursor is on.
   *
   * @return the index of the row the cursor is on
   * @throws SQLException on failure
   */
  public int getRow() throws SQLException {
    return this.currentRowIndex;
  }

  /**
   * Returns the total size of the number of rows.
   *
   * @return the total size of the number of rows available.
   */
  public int getTotalRowSize() {
    return rows.size();
  }

  /**
   * Gets the CurrentRow attribute of the MemoryResultSet object
   *
   * @return The CurrentRow value
   */
  public ResultSetRow getCurrentRow() {
    return currentRow;
  }

  //
  // Methods implementing the ResultSet interface
  //

  /**
   * Moves the cursor down one row from its current position.
   *
   * @return true if the new current row is valid; false if there are no more
   *      rows
   * @throws SQLException on failure
   */
  public boolean next() throws SQLException {
    boolean returnState = false;

    // advance the cursor if we can
    if (!this.isLast()) {

      this.currentRowIndex++;
      this.currentRow = (ResultSetRow)this.rows.get(this.currentRowIndex - 1);
      returnState = true;
    } else {

      this.currentRow = null;
      returnState = false;
    }

    // end if
    // return
    return returnState;
  }

  /**
   * Moves the cursor to before the first row in the result set.
   *
   * @throws SQLException on failure
   */
  public void beforeFirst() throws SQLException {
    // return members to their default state
    this.currentRowIndex = 0;
    this.currentRow = null;
  }

  /**
   * Moves the cursor to after the last row in the result set.
   *
   * @throws SQLException on failure
   */
  public void afterLast() throws SQLException {
    this.currentRowIndex = this.rows.size() + 1;
    this.currentRow = null;
  }

  /**
   * Moves the cursor to the first row in this ResultSet object.
   *
   * @return true if the cursor is on a valid row; false if there are no rows in
   *      the result set
   * @throws SQLException always
   */
  public boolean first() throws SQLException {

    boolean returnState = false;

    if (this.rows.size() > 0) {
      this.beforeFirst();
      this.next();
      returnState = true;
    }

    return returnState;
  }

  /**
   * Moves the cursor to the last row in the result set.
   *
   * @return RETURNED VALUE TO DO
   * @throws SQLException always
   */
  public boolean last() throws SQLException {

    boolean returnState = false;

    if ( (this.rows != null) && (this.rows.size() > 0)) {
      this.currentRowIndex = this.rows.size();
      this.currentRow = (ResultSetRow)this.rows.get(this.currentRowIndex - 1);
      returnState = true;
    }

    return returnState;
  }

  /**
   * Adds a new row to the current set of rows.
   *
   * @param row new row to add to the end of the queue.
   */
  public void addRow(ResultSetRow row) {
    rows.add(row);
  }

  /**
   * Moves the cursor to the given row number (takes obsolute value) in this
   * ResultSet object. An attempt to position the cursor beyond the first/last
   * row in the result set leaves the cursor before the first row or after the
   * last row.
   *
   * @param row the number of the row to which the cursor should move. A
   *      positive number indicates the row number counting from the beginning
   *      of the result set; a negative number indicates the row number counting
   *      from the end of the result set
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public boolean absolute(int row) throws SQLException {

    boolean returnState = false;

    // Work forward from the start
    if (row >= 0) {

      if (row == 0) {
        beforeFirst();
      } else if (row <= this.rows.size()) {
        this.currentRowIndex = row;
        this.currentRow = this.rows.get(this.currentRowIndex - 1);
        returnState = true;
      } else {
        afterLast();
      }
    } else {
      // Work back from the end
      if ( (this.rows.size() + row) >= 0) {
        this.currentRowIndex = (this.rows.size() + 1) + row;
        this.currentRow = this.rows.get(this.currentRowIndex - 1);
        returnState = true;
      } else {
        beforeFirst();
      }
    }

    return returnState;
  }

  /**
   * Moves the cursor a relative number of rows, either positive or negative
   * from its current position. Attempting to move beyond the first/last row in
   * the result set positions the cursor before/after the the first/last row.
   * Calling relative(0) is valid, but does not change the cursor position.
   *
   * @param numRows an int specifying the number of rows to move from the
   *      current row; a positive number moves the cursor forward; a negative
   *      number moves the cursor backward
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public boolean relative(int numRows) throws SQLException {

    boolean returnState = false;

    // Work forward from the start
    if (numRows >= 0) {

      if (numRows <= (this.rows.size() - this.currentRowIndex)) {

        this.currentRowIndex += numRows;
        this.currentRow =
            (ResultSetRow)this.rows.get(this.currentRowIndex - 1);
        returnState = true;
      } else {
        afterLast();
      }
    } else {
      // Work back from the end

      if ( (this.currentRowIndex + numRows) > 0) {

        // Add 1 to size to go to end of list then add the negative row number
        this.currentRowIndex += numRows;
        this.currentRow = this.rows.get(this.currentRowIndex - 1);
        returnState = true;
      } else {
        beforeFirst();
      }
    }

    return returnState;
  }

  //
  // Relational algebra methods
  //

  /**
   * Result sets are equal if their rows are equal. Both row and column ordering
   * is signicant.
   *
   * @param object PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public boolean equals(Object object) {

    if (! (object instanceof ResultSet)) return false;

    try {

      // Convert the other result set into a comparable form
      MemoryResultSet testResultSet =
          (object instanceof MemoryResultSet) ? (MemoryResultSet) object
          : new MemoryResultSet( (ResultSet) object);

      // Compare the rows
      return rows.equals(testResultSet.rows);
    } catch (SQLException e) {
      return false;
    }
  }

  public int hashCode() {
    return rows.hashCode();
  }

  /**
   * Produce a string version of the result set. Displaying the available
   * columns and rows.
   *
   * @return the string version of the result set.
   */
  public String toString() {

    try {

      StringBuffer buffer = new StringBuffer(getColumnNames().length + " columns:");

      // Save the current state of the result set.
      int tmpCurrentRow = getRow();

      // Get the names of the columns
      for (int i = 0; i < columnNames.length; i++) {
        buffer.append(" ").append(columnNames[i]);
      }

      buffer.append(" (").append(rows.size()).append(" rows)");

      // Start at the start
      beforeFirst();

      while (next()) {
        buffer.append("\n").append(getCurrentRow());
      }

      // Restore the state of the result set.
      absolute(tmpCurrentRow);

      return buffer.toString();
    } catch (SQLException se) {
      logger.error("Failed to convert object to string", se);
      return "";
    }
  }

  /**
   * Initialises the column names and metadata from the given metadata object.
   *
   * @param newMetaData PARAMETER TO DO
   * @throws SQLException if there was an error getting the metadata attributes.
   */
  private void initialiseMetaData(ResultSetMetaData newMetaData) throws SQLException {

    int columnNameCount = newMetaData.getColumnCount();
    columnNames = new String[columnNameCount];

    for (int i = 0; i < columnNameCount; i++) {
      columnNames[i] = newMetaData.getColumnName(i + 1);
    }

    // initialise the metadata field
    metaData = new ResultSetMetaDataImpl(columnNames);
  }

  public int getHoldability() throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public Reader getNCharacterStream(int columnIndex) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public Reader getNCharacterStream(String columnLabel) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public NClob getNClob(int columnIndex) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public NClob getNClob(String columnLabel) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public String getNString(int columnIndex) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public String getNString(String columnLabel) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public RowId getRowId(int columnIndex) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public RowId getRowId(String columnLabel) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public SQLXML getSQLXML(int columnIndex) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public SQLXML getSQLXML(String columnLabel) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public boolean isClosed() throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateAsciiStream(int columnIndex, InputStream x, long length)  throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateNClob(int columnIndex, NClob clob) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateNClob(String columnLabel, NClob clob) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateNString(int columnIndex, String string) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateNString(String columnLabel, String string) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateRowId(int columnIndex, RowId x) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateRowId(String columnLabel, RowId x) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public void updateNCharacterStream(int columnIndex, Reader reader) throws SQLException {
    // Empty stub
  }

  public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
    // Empty stub
  }

  public void updateAsciiStream(int columnIndex, InputStream inputStream) throws SQLException {
    // Empty stub
  }

  public void updateBinaryStream(int columnIndex, InputStream inputStream) throws SQLException {
    // Empty stub
  }

  public void updateCharacterStream(int columnIndex, Reader reader) throws SQLException {
    // Empty stub
  }

  public void updateAsciiStream(String columnLabel, InputStream inputStream) throws SQLException {
    // Empty stub
  }

  public void updateBinaryStream(String columnLabel, InputStream inputStream) throws SQLException {
    // Empty stub
  }

  public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
    // Empty stub
  }

  public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
    // Empty stub
  }

  public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
    // Empty stub
  }

  public void updateClob(int columnIndex, Reader reader) throws SQLException {
    // Empty stub
  }

  public void updateClob(String columnLabel, Reader reader) throws SQLException {
    // Empty stub
  }

  public void updateNClob(int columnIndex, Reader reader) throws SQLException {
    // Empty stub
  }

  public void updateNClob(String columnLabel, Reader reader) throws SQLException {
    // Empty stub
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

}
