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

// Java packages
import java.io.Serializable;
import java.sql.*;

import org.apache.log4j.Logger;

// Log4J

/**
 * An object that can be used to get information about the types and properties
 * of the columns in a <code>ResultSet</code> object. The following code
 * fragment creates the <code>ResultSet</code> object rs, creates the <code>ResultSetMetaData</code>
 * object rsmd, and uses rsmd to find out how many columns rs has and whether
 * the first column in rs can be used in a <code>WHERE</code> clause. <PRE>
 *
 *     ResultSet rs = stmt.executeQuery("SELECT a, b, c FROM TABLE2");
 *     ResultSetMetaData rsmd = rs.getMetaData();
 *     int numberOfColumns = rsmd.getColumnCount();
 *     boolean b = rsmd.isSearchable(1);
 *
 * </PRE>
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @created 2001-07-12
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/01/05 04:59:29 $
 * @maintenanceAuthor $Author: newmana $
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ResultSetMetaDataImpl implements ResultSetMetaData, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 4483688823263444933L;

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(ResultSetMetaDataImpl.class);

  /**
   * The names of the columns.
   *
   */
  protected String[] columnNames;

  /**
   * Constructor.
   *
   * @param newColumnNames the names of the columns.
   * @throws IllegalArgumentException if the given column names are null.
   * @throws IllegalArgumentException EXCEPTION TO DO
   */
  public ResultSetMetaDataImpl(String[] newColumnNames) throws
      IllegalArgumentException {

    if (newColumnNames == null) {

      throw new IllegalArgumentException("Cannot create result set metadata " +
          " with null column names");
    }

    columnNames = newColumnNames;
  }

  /**
   * Returns the number of columns in this <code>ResultSet</code> object.
   *
   * @return the number of columns
   * @exception SQLException if a database access error occurs
   */
  public int getColumnCount() throws SQLException {

    return columnNames.length;
  }

  /**
   * Indicates whether the designated column is automatically numbered, thus
   * read-only.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return <code>true</code> if so; <code>false</code> otherwise
   * @exception SQLException if a database access error occurs
   */
  public boolean isAutoIncrement(int column) throws SQLException {

    return false;
  }

  /**
   * Indicates whether a column's case matters.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return <code>true</code> if so; <code>false</code> otherwise
   * @exception SQLException if a database access error occurs
   */
  public boolean isCaseSensitive(int column) throws SQLException {

    return true;
  }

  /**
   * Indicates whether the designated column can be used in a where clause.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return <code>true</code> if so; <code>false</code> otherwise
   * @exception SQLException if a database access error occurs
   */
  public boolean isSearchable(int column) throws SQLException {

    return true;
  }

  /**
   * Indicates whether the designated column is a cash value.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return <code>true</code> if so; <code>false</code> otherwise
   * @exception SQLException if a database access error occurs
   */
  public boolean isCurrency(int column) throws SQLException {

    return false;
  }

  /**
   * Indicates the nullability of values in the designated column.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return the nullability status of the given column; one of <code>columnNoNulls</code>
   *      , <code>columnNullable</code> or <code>columnNullableUnknown</code>
   * @exception SQLException if a database access error occurs
   */
  public int isNullable(int column) throws SQLException {

    return columnNoNulls;
  }

  /**
   * Indicates whether values in the designated column are signed numbers.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return <code>true</code> if so; <code>false</code> otherwise
   * @exception SQLException if a database access error occurs
   */
  public boolean isSigned(int column) throws SQLException {

    return false;
  }

  /**
   * Indicates the designated column's normal maximum width in characters.
   *
   * @param column the first column is 1, the second is 2, ...
       * @return the normal maximum number of characters allowed as the width of the
   *      designated column
   * @exception SQLException if a database access error occurs
   */
  public int getColumnDisplaySize(int column) throws SQLException {

    return 30;
  }

  /**
   * Gets the designated column's suggested title for use in printouts and
   * displays.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return the suggested column title
   * @exception SQLException if a database access error occurs
   */
  public String getColumnLabel(int column) throws SQLException {

    try {

      return columnNames[column - 1];
    }
    catch (ArrayIndexOutOfBoundsException ex) {

      throw new SQLException("Column number out of bounds: " + column);
    }
  }

  /**
   * Get the designated column's name.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return column name
   * @exception SQLException if a database access error occurs
   */
  public String getColumnName(int column) throws SQLException {

    try {

      return columnNames[column - 1];
    }
    catch (ArrayIndexOutOfBoundsException ex) {

      throw new SQLException("Column number out of bounds: " + column);
    }
  }

  /**
   * Get the designated column's table's schema.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return schema name or "" if not applicable
   * @exception SQLException if a database access error occurs
   */
  public String getSchemaName(int column) throws SQLException {

    return "";
  }

  /**
   * Get the designated column's number of decimal digits.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return precision
   * @exception SQLException if a database access error occurs
   */
  public int getPrecision(int column) throws SQLException {

    return 0;
  }

  /**
   * Gets the designated column's number of digits to right of the decimal
   * point.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return scale
   * @exception SQLException if a database access error occurs
   */
  public int getScale(int column) throws SQLException {

    return 0;
  }

  /**
   * Gets the designated column's table name.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return table name or "" if not applicable
   * @exception SQLException if a database access error occurs
   */
  public String getTableName(int column) throws SQLException {

    return "";
  }

  /**
   * Gets the designated column's table's catalog name.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return column name or "" if not applicable
   * @exception SQLException if a database access error occurs
   */
  public String getCatalogName(int column) throws SQLException {

    return "";
  }

  /**
   * Retrieves the designated column's SQL type.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return SQL type from java.sql.Types
   * @exception SQLException if a database access error occurs
   * @see Types
   */
  public int getColumnType(int column) throws SQLException {

    return java.sql.Types.VARCHAR;
  }

  /**
   * Retrieves the designated column's database-specific type name.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return type name used by the database. If the column type is a
   *      user-defined type, then a fully-qualified type name is returned.
   * @exception SQLException if a database access error occurs
   */
  public String getColumnTypeName(int column) throws SQLException {

    // TODO - is this correct?
    return "VARCHAR";
  }

  /**
   * Indicates whether the designated column is definitely not writable.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return <code>true</code> if so; <code>false</code> otherwise
   * @exception SQLException if a database access error occurs
   */
  public boolean isReadOnly(int column) throws SQLException {

    return true;
  }

  /**
   * Indicates whether it is possible for a write on the designated column to
   * succeed.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return <code>true</code> if so; <code>false</code> otherwise
   * @exception SQLException if a database access error occurs
   */
  public boolean isWritable(int column) throws SQLException {

    return false;
  }

  /**
       * Indicates whether a write on the designated column will definitely succeed.
   *
   * @param column the first column is 1, the second is 2, ...
   * @return <code>true</code> if so; <code>false</code> otherwise
   * @exception SQLException if a database access error occurs
   */
  public boolean isDefinitelyWritable(int column) throws SQLException {

    return false;
  }

  //--------------------------JDBC 2.0-----------------------------------

  /**
   * <p>
   *
   * Returns the fully-qualified name of the Java class whose instances are
   * manufactured if the method <code>ResultSet.getObject</code> is called to
   * retrieve a value from the column. <code>ResultSet.getObject</code> may
   * return a subclass of the class returned by this method.
   *
   * @since 1.2
   * @param column PARAMETER TO DO
   * @return the fully-qualified name of the class in the Java programming
   *      language that would be used by the method <code>ResultSet.getObject</code>
   *      to retrieve the value in the specified column. This is the class name
   *      used for custom mapping.
   * @exception SQLException if a database access error occurs
       * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
  public String getColumnClassName(int column) throws SQLException {

    return "java.lang.String";
  }

  /**
   * This class does not wrap anything.
   * @param iface The interface to check for - ignored.
   * @return Always <code>false</code>.
   */
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  /**
   * This class does not wrap anything, so can't return a value.
   * @param iface The interface to check for - ignored.
   * @return Never returns normally.
   * @throws SQLException Always.
   */
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException("Interface not wrapped");
  }
}
