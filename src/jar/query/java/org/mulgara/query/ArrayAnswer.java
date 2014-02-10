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
import java.util.ArrayList;
import java.util.List;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;

/**
 * An {@link Answer} backed by a Java array.
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
 * @copyright &copy; 2001-2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ArrayAnswer extends AbstractAnswer implements Answer, Cloneable, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = -8428820938720763295L;

  /**
   * Logger. This is named after the class.
   */
  private final static Logger logger =
      Logger.getLogger(ArrayAnswer.class.getName());

  /**
   * The columns of the {@link Answer}.
   */
  private Variable[] variables;

  /**
   * A single array containing the values of all rows.
   *
   * The length of this array must be an even multiple of the length of the
   * {@link #variables} array.  The elements of the array must be JRDF
   * {@link Node}s, other {@link Answer}s, or <code>null</code> to indicate
   * unbound variables.
   */
  private Object[] values;

  /**
   * The cursor position.
   *
   * Normally cursor positions are <code>long</code>-valued, but since the
   * fields are stored in a Java array which only supports
   * <code>int</code>-valued indexing, we only support <code>int</code>.
   */
  private int row = -1;

  /**
   * The number of rows.
   *
   * Normally cursor positions are <code>long</code>-valued, but since the
   * fields are stored in a Java array which only supports
   * <code>int</code>-valued indexing, we only support <code>int</code>.
   */
  private int rowCount;

  //
  // Constructors
  //

  /**
   * Main constructor.
   *
   * Note that it's not possible to create an unconstrained {@link Answer} with
   * this constructor.
   *
   * @param variables  the columns of the new {@link Answer}, never
   *   <code>null</code>
   * @param values  the concatenated rows of the new {@link Answer}, whose
   *   elements should be either JRDF {@link Node}s, subqueried {@link Answer}s,
   *   or <code>null</code> to indicate unbound variables
   * @throws IllegalArgumentException if either <var>variables</var> or
   *   <var>values</var> are <code>null</code>, or if the length of the
   *   <var>values</var> does not divide evenly by the length of the
   *   <var>variables</var> to produce whole rows, or if the <var>values</var>
   *   include any element which is not a {@link Node}, an {@link Answer}, or
   *   <code>null</code>
   */
  public ArrayAnswer(Variable[] variables, Object[] values) {

    // Validate "variables" parameter
    if (variables == null) {
      throw new IllegalArgumentException("Null \"variables\" parameter");
    }

    // Validate "values" parameter
    if (values == null) {
      throw new IllegalArgumentException("Null \"values\" parameter");
    }

    if (variables.length == 0) {
      if (values.length != 0) {
        throw new IllegalArgumentException(
            "Non-zero length \"values\" parameter " +
            "with zero-length \"variables\" parameter"
            );
      }
    }
    else if (values.length % variables.length != 0) {
      throw new IllegalArgumentException(
          values.length + " values don't make for an integral number of " +
          variables.length + "-tuples"
          );
    }

    for (int i = 0; i < values.length; i++) {
      if (!((values[i] == null) ||
          (values[i] instanceof Node) ||
          (values[i] instanceof Answer))) {
        throw new IllegalArgumentException(
            "Bad element " + i + " in \"values\" parameter: " + values[i] +
            " (" +
            values[i].getClass() + ")"
            );
      }
    }

    // Initialize fields
    this.variables = variables;
    this.values = values;
    this.rowCount = (variables.length == 0)
        ? 0
        : (values.length / variables.length);
  }

  /**
   * Copy constructor.
   *
   * @param answer the answer to duplicate; <code>null</code> value disallowed
   * @throws IllegalArgumentException if <var>answer</var> is <code>null</code>
   */
  public ArrayAnswer(Answer answer) throws TuplesException {

    // Validate "answer" parameter
    if (answer == null) {
      throw new IllegalArgumentException("Null \"answer\" parameter");
    }

    // Copy the variables
    variables = answer.getVariables();
    assert variables != null:"variables array is null";

    // Copy the values
    List<Object> valueList = new ArrayList<Object>();
    rowCount = 0;

    answer.beforeFirst();
    while (answer.next()) {
      rowCount++;
      for (int i = 0; i < variables.length; i++) {
        Object object = answer.getObject(i);

        // Ensure that any subanswers are Serializable
        if (object instanceof Answer && !(object instanceof Serializable)) {
          Answer subanswer = (Answer) object;
          object = new ArrayAnswer(subanswer);
          subanswer.close();
        }

        valueList.add(object);
      }
    }

    values = valueList.toArray();
  }

  //
  // Methods implementing the Answer interface
  //

  public long getRowCount() throws TuplesException {
    return rowCount;
  }

  public long getRowUpperBound() throws TuplesException {
    return getRowCount();
  }

  public long getRowExpectedCount() throws TuplesException {
    return getRowCount();
  }

  public int getRowCardinality() throws TuplesException {
    if (getRowCount() > 1) {
      return Cursor.MANY;
    }
    switch ((int) getRowCount()) {
      case 0:
        return Cursor.ZERO;
      case 1:
        return Cursor.ONE;
      default:
        throw new TuplesException("Illegal row count: " + getRowCount());
    }
  }

  /* (non-Javadoc)
   * @see org.mulgara.query.Cursor#isEmpty()
   */
  public boolean isEmpty() throws TuplesException {
    return getRowCount() == 0;
  }

  public Object getObject(int column) throws TuplesException {
    if (column < 0 || column >= variables.length) {
      throw new IllegalArgumentException("No such column " + column);
    }

    return values[(variables.length * row) + column];
  }

  public Object getObject(String columnName) throws TuplesException {
    return getObject(getColumnIndex(new Variable(columnName)));
  }

  public Variable getVariable(int column) throws TuplesException {
    if (column < 0 || column >= variables.length) {
      throw new IllegalArgumentException("No such column " + column);
    }

    return variables[column];
  }

  public Variable[] getVariables() {
    return variables;
  }

  public int getNumberOfVariables() {
    return variables.length;
  }

  public boolean isUnconstrained() throws TuplesException {
    return (variables.length == 0) && (rowCount > 0);
  }

  public int getColumnIndex(Variable column) throws TuplesException {
    // Validate "column" parameter
    if (column == null) {
      throw new IllegalArgumentException("Null \"column\" parameter");
    }

    // Look for the requested variable in the "variables" array
    for (int i = 0; i < variables.length; i++) {
      if (column.equals(variables[i])) {
        return i;
      }
    }

    // Couldn't find the requested variable
    throw new TuplesException("No such column " + column);
  }

  public void beforeFirst() throws TuplesException {
    row = -1;
  }

  /**
   * Free the {@link #variables} and {@link #values} arrays.
   */
  public void close() throws TuplesException {
    if (variables == null) {
      throw new TuplesException("Attempt to close already closed ArrayAnswer.");
    }
    variables = null;
    values = null;
  }

  public boolean next() throws TuplesException {
    if (row < (rowCount - 1)) {
      row++;
      return true;
    }
    else {
      return false;
    }
  }

  //
  // Methods overriding the Object superclass
  //

  /**
   * Subclasses are required to support cloning.
   *
   * @throws Error if this is a subclass that doesn't support cloning.
   */
  public Object clone() {

    // Clone all subclass fields
    ArrayAnswer cloned;
    cloned = (ArrayAnswer)super.clone();
    assert cloned != null;

    /*
         // Copy immutable fields by reference
         cloned.rowCount  = rowCount;
         cloned.variables = variables;
         cloned.values    = values;

         // Copy mutable fields by value
         cloned.row = row;
     */

    return cloned;
  }

  /**
   * Equality.
   *
   * @param object the object to compare against for equality, possibly
   *   <code>null</code>
   * @return whether <var>object</var> is equal to this instance; the order of
   *   both columns and rows is significant
   */
  public boolean equals(Object object) {
    // Gotta be non-null and of matching type
    if ((object != null) && (object instanceof Answer)) {
      try {
        return AnswerOperations.equal(this, (Answer) object);
      }
      catch (TuplesException e) {
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
   * @return a formatted representation of this instance
   */
  public String toString() {
    if (variables == null) {
      return "<closed ArrayAnswer>";
    }

    StringBuffer stringBuffer = new StringBuffer("[");

    // Generate the header
    for (int i = 0; i < variables.length; i++) {
      stringBuffer.append(variables[i]);
      stringBuffer.append((i == variables.length - 1) ? "" : " ");
    }

    // Generate the content
    for (int i = 0; i < values.length; i++) {
      stringBuffer.append((i % variables.length == 0) ? "]\n[" : " ");
      stringBuffer.append(values[i]);
    }
    stringBuffer.append("]");

    return stringBuffer.toString();
  }
}
