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

package org.mulgara.store.tuples;

// Java 2 standard packages
import java.util.*;

// Third party packages
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.statement.StatementStore;

/**
 * Implement most of the convenience methods of the {@link Tuples} interface.
 *
 * @created 2003-01-09
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/05/16 11:07:10 $
 *
 * @maintenanceAuthor $Author: amuys $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class AbstractTuples implements Tuples {
  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(AbstractTuples.class.getName());

  /** Empty variable array. */
  private final static Variable[] emptyVariables = new Variable[] {};

  /** Description of the Field */
  @SuppressWarnings("unused")
  private final static long[] NO_PREFIX = new long[] {};

  /** Description of the Field */
  private final static RowComparator defaultRowComparator = new DefaultRowComparator();

  /** The variable names of each column. */
  private Variable[] variables = null;

  /** Cache the row count when calculated. */
  protected long rowCount = -1;
  protected int rowCardinality = -1;

  //
  // Convenience methods
  //

  /**
   * @param tuples an array of {@link Tuples} to be cloned
   * @return the array of clones
   */
  protected static Tuples[] clone(Tuples[] tuples) {
    Tuples[] clonedTuples = new Tuples[tuples.length];

    for (int i = 0; i < tuples.length; i++) {
      clonedTuples[i] = (Tuples) tuples[i].clone();
    }

    return clonedTuples;
  }

  /**
   * @param tuples an array of {@link Tuples} to be closed
   * @throws TuplesException EXCEPTION TO DO
   */
  protected static void close(Tuples[] tuples) throws TuplesException {
    for (int i = 0; i < tuples.length; i++) {
      tuples[i].close();
    }
  }

  /**
   * Convert the {@link Tuples#isColumnEverUnbound} results of a
   * <var>tuples</var> into an array.
   *
   * @param tuples  an instance to copy the
   *   {@link Tuples#isColumnEverUnbound} values from
   * @return an array indexed by column number containing flags for whether
   *   the corresponding columns are ever unbound
   */
 public static boolean[] columnEverUnboundArray(Tuples tuples)
     throws TuplesException {
   boolean[] columnEverUnbound = new boolean[tuples.getNumberOfVariables()];

   for (int i = 0; i < columnEverUnbound.length; i++) {
     columnEverUnbound[i] = tuples.isColumnEverUnbound(i);
   }

   return columnEverUnbound;
 }


  /**
   * Generate a legible representation of an array of <code>long</code>-valued
   * node numbers.
   *
   * If the array passed is <code>null</code>, this generates the
   * {@link String} <code>"NULL"</code>.  Otherwise, it generates a
   * space-separated sequence of numbers in square brackets.  The value
   * {@link Tuples#UNBOUND} is represented as a wildcard asterisk rather than
   * a number.
   *
   * @param longArray  the array to format, possibly <code>null</code>
   * @return a legible representation of the <var>longArray</var>
   */
  public static String toString(long[] longArray) {
    if (longArray == null) {
      return "NULL";
    }

    StringBuffer buffer = new StringBuffer("L[");

    for (int i = 0; i < longArray.length; i++) {
      buffer.append(" ");

      if (longArray[i] == Tuples.UNBOUND) {
        buffer.append("*");
      }
      else {
        buffer.append(longArray[i]);
      }
    }

    buffer.append(" ]");

    return buffer.toString();
  }

  public static String toString(int[] intArray) {
    if (intArray == null) {
      return "NULL";
    }

    StringBuffer buffer = new StringBuffer("I[");
    for (int i = 0; i < intArray.length; i++) {
      buffer.append(" ");
      buffer.append(intArray[i]);
    }
    buffer.append(" ]");

    return buffer.toString();
  }

  public static String toString(boolean[] boolArray) {
    if (boolArray == null) {
      return "NULL";
    }

    StringBuffer buffer = new StringBuffer("B[");
    for (int i = 0; i < boolArray.length; i++) {
      buffer.append(" ");
      buffer.append(boolArray[i]);
    }
    buffer.append(" ]");

    return buffer.toString();
  }

  public static String toString(Variable[] varArray) {
    if (varArray == null) {
      return "NULL";
    }

    StringBuffer buffer = new StringBuffer("V[");
    for (int i = 0; i < varArray.length; i++) {
      buffer.append(" ");
      buffer.append(varArray[i].toString());
    }
    buffer.append(" ]");

    return buffer.toString();
  }

  //
  // Methods implementing Tuples
  //

  /**
   * Gets the ColumnValue attribute of the AbstractTuples object
   *
   * @param column The column offset to get data from
   * @return The ColumnValue value
   * @throws TuplesException If there was a Tuples specific error accessing the data.
   */
  public abstract long getColumnValue(int column) throws TuplesException;

  /**
   * Gets the raw (unfiltered) ColumnValue attribute of the AbstractTuples object.
   * By default this returns the normal column value.
   *
   * @param column The column offset to get data from
   * @return The column value as a gNode
   * @throws TuplesException If there was a Tuples specific error accessing the data.
   */
  public long getRawColumnValue(int column) throws TuplesException {
    return getColumnValue(column);
  }

  /**
   * Returns a copy of the internal variables.
   *
   * @return a copy of the internal variables.
   */
  public Variable[] getVariables() {
    Variable[] newVariables = null;

    if (variables != null) {
      newVariables = new Variable[variables.length];
      System.arraycopy(variables, 0, newVariables, 0, variables.length);
    } else {
      newVariables = emptyVariables;
    }

    return newVariables;
  }

  /**
   * Returns the number of variables in a tuples.
   *
   * @return the number of variables in a tuples.
   */
  public int getNumberOfVariables() {
    int noVars = 0;

    if (variables != null) {
      noVars = variables.length;
    }

    return noVars;
  }

  /**
   * Gets the RowCount attribute of the AbstractTuples object
   *
   * @return The RowCount value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getRowCount() throws TuplesException {
    if (rowCount != -1) {
      return rowCount;
    } else if (isMaterialized()) {
      return getRowUpperBound(); // Upper bound is accurate if tuples is materialized.
    }
    Tuples temp = (Tuples)this.clone();
    rowCount = 0;
    temp.beforeFirst();
    while (temp.next()) {
      rowCount++;
    }
    temp.close();

    return rowCount;
  }

  public abstract long getRowUpperBound() throws TuplesException;

  public abstract long getRowExpectedCount() throws TuplesException;

  public int getRowCardinality() throws TuplesException {
    if (rowCardinality != -1) return rowCardinality;

    if (rowCount > 1) {
      rowCardinality = Cursor.MANY;
    } else {
      switch ((int) rowCount) {
        case 0:
          rowCardinality = Cursor.ZERO;
          break;
        case 1:
          rowCardinality = Cursor.ONE;
          break;
        case -1:
          Tuples temp = (Tuples)this.clone();
          temp.beforeFirst();
          if (!temp.next()) {
            rowCount = 0;
            rowCardinality = Cursor.ZERO;
          } else if (!temp.next()) {
            rowCount = 1;
            rowCardinality = Cursor.ONE;
          } else {
            rowCardinality = Cursor.MANY;
          }
          temp.close();
          break;
        default:
          throw new TuplesException("Illegal rowCount " + rowCount);
      }
    }

    return rowCardinality;
  }

  /**
   * Returns true if the number of rows is zero.
   *
   * @return true if the number of rows is zero.
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean isEmpty() throws TuplesException {
    return getRowCardinality() == ZERO;
  }

  /**
   * Gets the ColumnIndex attribute of the AbstractTuples object
   *
   * @param variable PARAMETER TO DO
   * @return The ColumnIndex value
   * @throws TuplesException EXCEPTION TO DO
   */
  public int getColumnIndex(Variable variable) throws TuplesException {
    if (variable == null) {
      throw new IllegalArgumentException("Null \"variable\" parameter");
    }

    Variable[] variables = getVariables();

    for (int i = 0; i < variables.length; i++) {
      if (variables[i].equals(variable)) return i;
    }

    throw new TuplesException("No such variable " + variable + " in tuples " +
        Arrays.asList(variables) + " (" + getClass() + ")");
  }

  public abstract boolean isColumnEverUnbound(int column) throws TuplesException;

  /**
   * Gets the Materialized attribute of the AbstractTuples object
   *
   * @return The Materialized value
   */

  public boolean isMaterialized() {
    return false;
  }

  /**
   * Gets the Unconstrained attribute of the AbstractTuples object
   *
   * @return The Unconstrained value
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean isUnconstrained() throws TuplesException {
    return (getNumberOfVariables() == 0) && (getRowCardinality() > Cursor.ZERO);
  }

  /**
   * @return <code>null</code>, indicating unordered {@link Tuples}
   */
  public RowComparator getComparator() {
    return (getNumberOfVariables() == 0) ? defaultRowComparator : null;
  }

  //
  // Methods implementing Tuples interface
  //

  /**
   * Move the cursor to before the first row, optionally with a specifies list
   * of leading column values.
   *
   * @param prefix only iterate through product terms with the specified leading
   *      column values ({@link #NO_PREFIX} should be passed if all prefix
   *      values are desired
   * @param suffixTruncation the number of trailing rows to ignore when
   *      determining whether a row is distinct
   * @throws IllegalArgumentException if <var>prefix</var> is <code>null</code>
   * @throws TuplesException EXCEPTION TO DO
   */
  public abstract void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException;

  /**
   * Convenience method for the usual case of wanting to reset a tuples to
   * iterate through every single element. Equivalent to {@link
   * #beforeFirst(long[], int)}<code>({@link #NO_PREFIX}, 0)</code>.
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public void beforeFirst() throws TuplesException {
    beforeFirst(Tuples.NO_PREFIX, 0);
  }

  /**
   * Move to the next row satisfying the current prefix and suffix truncation.
   * If no such row exists, return <code>false<code> and the current row
   * becomes unspecified.  The current row is unspecified when a tuples instance
   * is created.  To specify the current row, the {@link #beforeFirst()} or
   * {@link #beforeFirst(long[], int)} methods must be invoked
   *
   * @return whether a subsequent row with the specified prefix exists
   * @throws IllegalStateException if the current row is unspecified.
   * @throws TuplesException EXCEPTION TO DO
   */
  public abstract boolean next() throws TuplesException;

  /**
   * METHOD TO DO
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public abstract void close() throws TuplesException;

  /**
   * Renames the variables which label the tuples if they have the "magic" names
   * such as "Subject", "Predicate", "Object" and "Meta".
   *
   * @param constraint PARAMETER TO DO
   */
  public void renameVariables(Constraint constraint) {
    if (variables != null) {
      loop:for (int i = 0; i < variables.length; ++i) {
        Variable v = variables[i];

        for (int j = 0; j < 4; ++j) {
          // v will be a reference to one of the objects in Graph.VARIABLES[].
          if (v.equals(StatementStore.VARIABLES[j])) {
            // The array obtained from getVariables() is modifiable.
            variables[i] = (Variable) constraint.getElement(j);

            continue loop;
          }
        }

        throw new Error("Unexpected variable: " + v);
      }
    }
  }

  /**
   * Clone this tuples.
   *
   * @return A new instance euqivalent to the current Tuples.
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error(getClass() + " doesn't support clone, which it must", e);
    }
  }

  /**
   * Tests equality of the given object.
   *
   * @param o  the object to compare.
   * @return if the object is equal by row number, variables and row.
   */
  public boolean equals(Object o) {
    return (o instanceof Tuples) && equals(this, (Tuples)o);
  }

  /**
   * Added to match {@link #equals(Object)}.
   * Builds code based on content.
   */
  public int hashCode() {
    return TuplesOperations.hashCode(this);
  }

  /**
   * Tests if two tuples are identical
   * @param first The first tuples to test.
   * @param second The second tuples to test.
   * @return true if both tuples compare equal by row number, variables and by row.
   */
  public static boolean equals(Tuples first, Tuples second) {
    boolean isEqual = false;

    if (first == second) return true;

    if (first == null || second == null) return false;

    try {
      // Ensure that the row count is the same
      if (first.getRowCount() == second.getRowCount()) {
        // Ensure that the variable lists are equal
        if (Arrays.asList(first.getVariables()).equals(
            Arrays.asList(second.getVariables()))) {
          // Clone tuples to be compared
          Tuples t1 = (Tuples) first.clone();
          Tuples t2 = (Tuples) second.clone();

          TuplesException te = null;
          try {
            // Put them at the start.
            t1.beforeFirst();
            t2.beforeFirst();

            boolean finished = false;
            boolean tuplesEqual = true;

            // Repeat until there are no more rows or we find an unequal row.
            while (!finished) {
              // Assume that if t1 has next so does t2.
              finished = !t1.next();
              t2.next();

              // If we're not finished compare the row.
              if (!finished) {
                // Check if the elements in both rows are equal.
                for (int variableIndex = 0;
                     variableIndex < t1.getNumberOfVariables();
                     variableIndex++) {
                  // If they're not equal quit the loop and set tuplesEqual to
                  // false.
                  if (t1.getColumnValue(variableIndex) !=
                      t2.getColumnValue(variableIndex)) {
                    tuplesEqual = false;
                    finished = true;
                  }
                }
              }
            }

            isEqual = tuplesEqual;
          } catch (TuplesException e) {
            te = e;
          } finally {
            try {
              t1.close();
            } catch (TuplesException e) {
              if (te == null) te = e;
            } finally {
              try {
                t2.close();
              } catch (TuplesException e2) {
                if (te == null) te = e2;
              }
            }
          }
          if (te != null) throw te;
        }
      }
    } catch (TuplesException ex) {
      throw new RuntimeException(ex.toString(), ex);
    }

    return isEqual;
  }

  /**
   * Output the contents of this tuples in a string.
   *
   * @return The string representing the tuples.
   */
  public String toString() {
    return SimpleTuplesFormat.format(this);
  }

  /**
   * Sets the internal representation of the variables from the list given.
   *
   * @param variableList the list containing variables.
   */
  protected void setVariables(List<Variable> variableList) {
    variables = variableList.toArray(new Variable[variableList.size()]);
  }

  /**
   * Sets the internal representation of the variables from an existing array.
   *
   * @param variableArray the array containing variables.
   */
  protected void setVariables(Variable[] variableArray) {
    variables = variableArray;
  }

  public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
    return null;
  }
}
