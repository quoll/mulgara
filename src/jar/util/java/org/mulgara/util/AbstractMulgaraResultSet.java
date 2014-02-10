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
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.*;

// Third party packages
import org.apache.log4j.*;

/**
 * Abstract class for creating specialized implementations of {@link ResultSet}.
 *
 * @created 2001-07-12
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:13 $
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
abstract class AbstractMulgaraResultSet
  implements MulgaraResultSet, Cloneable {

  /**
   * The {@link SQLException} message for unimplemented methods.
   */
  protected final static String NOT_IMPLEMENTED = "Method not implemented.";

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger logger =
    Logger.getLogger(AbstractMulgaraResultSet.class.getName());

  /**
   * The names of the columns.
   */
  protected String[] columnNames;

  /**
   * Flag recalling whether the last column read returned a null value.
   */
  protected boolean wasNull = false;

  /**
   * @param direction The new FetchDirection value
   * @throws SQLException on failure
   */
  public void setFetchDirection(int direction) throws SQLException {

    // null implementation
  }

  /**
   * @param rows The new FetchSize value
   * @throws SQLException on failure
   */
  public void setFetchSize(int rows) throws SQLException {

    // null implementation
  }

  //
  // Methods for accessing results by column index
  //

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The String value
   * @throws SQLException on failure
   */
  public abstract String getString(int columnIndex) throws SQLException;

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Boolean value
   * @throws SQLException on failure
   */
  public boolean getBoolean(int columnIndex) throws SQLException {

    return Boolean.valueOf(getString(columnIndex)).booleanValue();
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Byte value
   * @throws SQLException on failure
   */
  public byte getByte(int columnIndex) throws SQLException {

    try {

      return Byte.parseByte(getString(columnIndex));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Short value
   * @throws SQLException on failure
   */
  public short getShort(int columnIndex) throws SQLException {

    try {

      return Short.parseShort(getString(columnIndex));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Int value
   * @throws SQLException on failure
   */
  public int getInt(int columnIndex) throws SQLException {

    try {

      return Integer.parseInt(getString(columnIndex));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Long value
   * @throws SQLException on failure
   */
  public long getLong(int columnIndex) throws SQLException {

    try {

      return Long.parseLong(getString(columnIndex));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Float value
   * @throws SQLException on failure
   */
  public float getFloat(int columnIndex) throws SQLException {

    try {

      return Float.parseFloat(getString(columnIndex));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Double value
   * @throws SQLException on failure
   */
  public double getDouble(int columnIndex) throws SQLException {

    try {

      return Double.parseDouble(getString(columnIndex));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnIndex The column number to retrieve
   * @param scale The number of digits to the right of the decimal point
   * @return The BigDecimal value
   * @throws SQLException on failure
   * @deprecated Deprecated in {@link java.sql.ResultSet}
   */
  public BigDecimal getBigDecimal(int columnIndex, int scale)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Bytes value
   * @throws SQLException on failure
   */
  public byte[] getBytes(int columnIndex) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Date value
   * @throws SQLException on failure
   */
  public java.sql.Date getDate(int columnIndex) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Time value
   * @throws SQLException on failure
   */
  public java.sql.Time getTime(int columnIndex) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Timestamp value
   * @throws SQLException on failure
   */
  public java.sql.Timestamp getTimestamp(int columnIndex)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The AsciiStream value
   * @throws SQLException on failure
   */
  public java.io.InputStream getAsciiStream(int columnIndex)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex The column number to get data from
   * @return The UnicodeStream value
   * @throws SQLException on failure
   * @deprecated Deprecated in the original {@link java.sql.ResultSet}
   */
  public java.io.InputStream getUnicodeStream(int columnIndex)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The BinaryStream value
   * @throws SQLException on failure
   */
  public java.io.InputStream getBinaryStream(int columnIndex)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  //
  // Methods for accessing results by column name
  //

  /**
   * @param columnName PARAMETER TO DO
   * @return The String value
   * @throws SQLException on failure
   */
  public String getString(String columnName) throws SQLException {

    return getString(findColumn(columnName));
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Boolean value
   * @throws SQLException on failure
   */
  public boolean getBoolean(String columnName) throws SQLException {

    return Boolean.valueOf(getString(columnName)).booleanValue();
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Byte value
   * @throws SQLException on failure
   */
  public byte getByte(String columnName) throws SQLException {

    try {

      return Byte.parseByte(getString(columnName));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Short value
   * @throws SQLException on failure
   */
  public short getShort(String columnName) throws SQLException {

    try {

      return Short.parseShort(getString(columnName));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Int value
   * @throws SQLException on failure
   */
  public int getInt(String columnName) throws SQLException {

    try {

      return Integer.parseInt(getString(columnName));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Long value
   * @throws SQLException on failure
   */
  public long getLong(String columnName) throws SQLException {

    try {

      return Long.parseLong(getString(columnName));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Float value
   * @throws SQLException on failure
   */
  public float getFloat(String columnName) throws SQLException {

    try {

      return Float.parseFloat(getString(columnName));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Double value
   * @throws SQLException on failure
   */
  public double getDouble(String columnName) throws SQLException {

    try {

      return Double.parseDouble(getString(columnName));
    }
     catch (NumberFormatException ex) {

      throw new SQLException(ex.toString());
    }
  }

  /**
   * @param columnName The column containing a number
   * @param scale Number of digits after the decimal point
   * @return The BigDecimal value
   * @throws SQLException on failure
   * @deprecated Deprecated in the original {@link java.sql.ResultSet}
   */
  public BigDecimal getBigDecimal(String columnName, int scale)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Bytes value
   * @throws SQLException on failure
   */
  public byte[] getBytes(String columnName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Date value
   * @throws SQLException on failure
   */
  public java.sql.Date getDate(String columnName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Time value
   * @throws SQLException on failure
   */
  public java.sql.Time getTime(String columnName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The Timestamp value
   * @throws SQLException on failure
   */
  public java.sql.Timestamp getTimestamp(String columnName)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The AsciiStream value
   * @throws SQLException on failure
   */
  public java.io.InputStream getAsciiStream(String columnName)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName The name of the column with the data to retrieve
   * @return The UnicodeStream value
   * @throws SQLException on failure
   * @deprecated This has been deprecated in the original {@link ResultSet#getUnicodeStream(String)}
   */
  public java.io.InputStream getUnicodeStream(String columnName)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The BinaryStream value
   * @throws SQLException on failure
   */
  public java.io.InputStream getBinaryStream(String columnName)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  //
  // Advanced features:
  //

  /**
   * @return The Warnings value
   * @throws SQLException on failure
   */
  public SQLWarning getWarnings() throws SQLException {

    return null;
  }

  /**
   * @return The CursorName value
   * @throws SQLException on failure
   */
  public String getCursorName() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @return The MetaData value
   * @throws SQLException on failure
   */
  public abstract ResultSetMetaData getMetaData() throws SQLException;

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The Object value
   * @throws SQLException on failure
   */
  public Object getObject(int columnIndex) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * Return the object at the given column name.
   *
   * @param columnName the index of the object to retrieve
   * @return the value at the given index
   * @throws SQLException on failure
   */
  public Object getObject(String columnName) throws SQLException {

    return getObject(findColumn(columnName));
  }

  //--------------------------JDBC 2.0-----------------------------------
  //
  // Getters and Setters
  //

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The CharacterStream value
   * @throws SQLException on failure
   */
  public Reader getCharacterStream(int columnIndex) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The CharacterStream value
   * @throws SQLException on failure
   */
  public Reader getCharacterStream(String columnName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The BigDecimal value
   * @throws SQLException on failure
   */
  public BigDecimal getBigDecimal(int columnIndex) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The BigDecimal value
   * @throws SQLException on failure
   */
  public BigDecimal getBigDecimal(String columnName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  //
  // Traversal/Positioning
  //

  /**
   * @return The BeforeFirst value
   * @throws SQLException on failure
   */
  public abstract boolean isBeforeFirst() throws SQLException;

  /**
   * @return The AfterLast value
   * @throws SQLException on failure
   */
  public abstract boolean isAfterLast() throws SQLException;

  /**
   * @return The First value
   * @throws SQLException on failure
   */
  public abstract boolean isFirst() throws SQLException;

  /**
   * @return The Last value
   * @throws SQLException on failure
   */
  public abstract boolean isLast() throws SQLException;

  /**
   * @return The Row value
   * @throws SQLException on failure
   */
  public abstract int getRow() throws SQLException;

  /**
   * @return The FetchDirection value
   * @throws SQLException on failure
   */
  public int getFetchDirection() throws SQLException {

    return FETCH_FORWARD;
  }

  /**
   * @return The FetchSize value
   * @throws SQLException on failure
   */
  public int getFetchSize() throws SQLException {

    return 1;
  }

  /**
   * @return The Type value
   * @throws SQLException on failure
   */
  public int getType() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @return The Concurrency value
   * @throws SQLException on failure
   */
  public int getConcurrency() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @return The Statement value
   * @throws SQLException on failure
   */
  public Statement getStatement() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param i PARAMETER TO DO
   * @param map PARAMETER TO DO
   * @return The Object value
   * @throws SQLException on failure
   */
  public Object getObject(int i, java.util.Map<String,Class<?>> map) throws SQLException {
    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param i PARAMETER TO DO
   * @return The Ref value
   * @throws SQLException on failure
   */
  public Ref getRef(int i) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param i PARAMETER TO DO
   * @return The Blob value
   * @throws SQLException on failure
   */
  public Blob getBlob(int i) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param i PARAMETER TO DO
   * @return The Clob value
   * @throws SQLException on failure
   */
  public Clob getClob(int i) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param i PARAMETER TO DO
   * @return The Array value
   * @throws SQLException on failure
   */
  public Array getArray(int i) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param colName PARAMETER TO DO
   * @param map PARAMETER TO DO
   * @return The Object value
   * @throws SQLException on failure
   */
  public Object getObject(String colName, java.util.Map<String,Class<?>> map)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param colName PARAMETER TO DO
   * @return The Ref value
   * @throws SQLException on failure
   */
  public Ref getRef(String colName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param colName PARAMETER TO DO
   * @return The Blob value
   * @throws SQLException on failure
   */
  public Blob getBlob(String colName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param colName PARAMETER TO DO
   * @return The Clob value
   * @throws SQLException on failure
   */
  public Clob getClob(String colName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param colName PARAMETER TO DO
   * @return The Array value
   * @throws SQLException on failure
   */
  public Array getArray(String colName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param cal PARAMETER TO DO
   * @return The Date value
   * @throws SQLException on failure
   */
  public java.sql.Date getDate(int columnIndex, Calendar cal)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param cal PARAMETER TO DO
   * @return The Date value
   * @throws SQLException on failure
   */
  public java.sql.Date getDate(String columnName, Calendar cal)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param cal PARAMETER TO DO
   * @return The Time value
   * @throws SQLException on failure
   */
  public java.sql.Time getTime(int columnIndex, Calendar cal)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param cal PARAMETER TO DO
   * @return The Time value
   * @throws SQLException on failure
   */
  public java.sql.Time getTime(String columnName, Calendar cal)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param cal PARAMETER TO DO
   * @return The Timestamp value
   * @throws SQLException on failure
   */
  public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param cal PARAMETER TO DO
   * @return The Timestamp value
   * @throws SQLException on failure
   */
  public java.sql.Timestamp getTimestamp(String columnName, Calendar cal)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  //
  // JDBC 3.0 methods introduced in JDK 1.4
  //

  /**
   * @param columnIndex PARAMETER TO DO
   * @return The URL value
   * @throws SQLException on failure
   */
  public URL getURL(int columnIndex) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @return The URL value
   * @throws SQLException on failure
   */
  public URL getURL(String columnName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * Returns the array of column names given the result set metadata.
   *
   * @return any array (starting at 0) of the column names.
   * @throws SQLException if there was an error getting the column count or
   *      other problem from the metadata.
   */
  public String[] getColumnNames() throws SQLException {

    if (columnNames == null) {

      ResultSetMetaData metadata = getMetaData();
      String[] mdataColumnNames = new String[metadata.getColumnCount()];

      for (int i = 0; i < mdataColumnNames.length; i++) {

        mdataColumnNames[i] = metadata.getColumnName(i + 1);
      }

      columnNames = mdataColumnNames;
    }

    return columnNames;
  }

  /**
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public abstract boolean next() throws SQLException;

  /**
   * @throws SQLException on failure
   */
  public void close() throws SQLException {

    // null implementation
  }

  /**
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public boolean wasNull() throws SQLException {

    return wasNull;
  }

  /**
   * @throws SQLException on failure
   */
  public void clearWarnings() throws SQLException {

    // null implementation
  }

  //----------------------------------------------------------------

  /**
   * @param columnName PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public int findColumn(String columnName) throws SQLException {

    // linear search for the named column  (FIXME: sort column names)
    ResultSetMetaData rsmd = getMetaData();

    for (int i = 1; i <= rsmd.getColumnCount(); i++) {

      if (rsmd.getColumnName(i).equals(columnName)) {

        return i;
      }
    }

    // No such column found
    throw new SQLException("Unknown column name: " + columnName);
  }

  /**
   * @throws SQLException on failure
   */
  public abstract void beforeFirst() throws SQLException;

  /**
   * @throws SQLException on failure
   */
  public abstract void afterLast() throws SQLException;

  /**
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public abstract boolean first() throws SQLException;

  /**
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public abstract boolean last() throws SQLException;

  /**
   * @param row PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public abstract boolean absolute(int row) throws SQLException;

  /**
   * @param rows PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public abstract boolean relative(int rows) throws SQLException;

  /**
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public boolean previous() throws SQLException {

    return isAfterLast() ? last() : relative(-1);
  }

  //
  // Updates
  //

  /**
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public boolean rowUpdated() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public boolean rowInserted() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @return RETURNED VALUE TO DO
   * @throws SQLException on failure
   */
  public boolean rowDeleted() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateNull(int columnIndex) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateBoolean(int columnIndex, boolean x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateByte(int columnIndex, byte x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateShort(int columnIndex, short x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateInt(int columnIndex, int x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateLong(int columnIndex, long x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateFloat(int columnIndex, float x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateDouble(int columnIndex, double x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateBigDecimal(int columnIndex, BigDecimal x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateString(int columnIndex, String x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateBytes(int columnIndex, byte[] x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateDate(int columnIndex, java.sql.Date x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateTime(int columnIndex, java.sql.Time x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateTimestamp(int columnIndex, java.sql.Timestamp x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @param length PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateAsciiStream(int columnIndex, InputStream x, int length)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @param length PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateBinaryStream(int columnIndex, InputStream x, int length)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @param length PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateCharacterStream(int columnIndex, Reader x, int length)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @param scale PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateObject(int columnIndex, Object x, int scale)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateObject(int columnIndex, Object x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateNull(String columnName) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateBoolean(String columnName, boolean x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateByte(String columnName, byte x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateShort(String columnName, short x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateInt(String columnName, int x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateLong(String columnName, long x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateFloat(String columnName, float x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateDouble(String columnName, double x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateBigDecimal(String columnName, BigDecimal x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateString(String columnName, String x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateBytes(String columnName, byte[] x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateDate(String columnName, java.sql.Date x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateTime(String columnName, java.sql.Time x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateTimestamp(String columnName, java.sql.Timestamp x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @param length PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateAsciiStream(String columnName, InputStream x, int length)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @param length PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateBinaryStream(String columnName, InputStream x, int length)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param reader PARAMETER TO DO
   * @param length PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateCharacterStream(String columnName, Reader reader, int length)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @param scale PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateObject(String columnName, Object x, int scale)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateObject(String columnName, Object x)
    throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @throws SQLException on failure
   */
  public void insertRow() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @throws SQLException on failure
   */
  public void updateRow() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @throws SQLException on failure
   */
  public void deleteRow() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @throws SQLException on failure
   */
  public void refreshRow() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @throws SQLException on failure
   */
  public void cancelRowUpdates() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @throws SQLException on failure
   */
  public void moveToInsertRow() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @throws SQLException on failure
   */
  public void moveToCurrentRow() throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateArray(int columnIndex, Array x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateArray(String columnName, Array x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateBlob(int columnIndex, Blob x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateBlob(String columnName, Blob x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateClob(int columnIndex, Clob x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateClob(String columnName, Clob x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnIndex PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateRef(int columnIndex, Ref x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * @param columnName PARAMETER TO DO
   * @param x PARAMETER TO DO
   * @throws SQLException on failure
   */
  public void updateRef(String columnName, Ref x) throws SQLException {

    throw new SQLException(NOT_IMPLEMENTED);
  }

  /**
   * Returns the index to the column based on its name.
   *
   * @param columnName the name of the column to search for.
   * @return the index (starting at 1) of the column.
   * @throws IllegalArgumentException if the column was not found.
   * @throws SQLException if there was a problem getting the column names.
   */
  public int columnForName(String columnName)
    throws IllegalArgumentException, SQLException {

    String[] columnNames = getColumnNames();

    for (int i = 0; i < columnNames.length; i++) {

      if (columnNames[i].equals(columnName)) {

        return i + 1;
      }
    }

    throw new IllegalArgumentException("No such column " + columnName);
  }
}
