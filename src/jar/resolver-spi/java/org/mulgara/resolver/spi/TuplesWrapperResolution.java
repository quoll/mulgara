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

package org.mulgara.resolver.spi;

// Java 2 standard packages
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J

// Locally written packages

import org.mulgara.query.*;
import org.mulgara.store.tuples.Annotation;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.Tuples;

/**
 * A {@link Resolution} which wraps a Tuples object.
 *
 * @created 2004-10-28
 * @author Paula Gearon
 * @version $Revision: 1.2 $
 * @modified $Date: 2005/05/16 11:07:07 $ @maintenanceAuthor $Author: amuys $
 * @company <a href="mailto:info@tucanatech.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technologies, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class TuplesWrapperResolution implements Resolution {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(TuplesWrapperResolution.class.getName());

  /** The constraint.  */
  private final Constraint constraint;

  /** The wrapped tuples. */
  private Tuples tuples;

  //
  // Constructor
  //

  /**
   * Construct a {@link TuplesWrapperResolution}.
   *
   * @param tuples the tuples to wrap
   * @param constraint the constraint, never <code>null</code>
   * @throws IllegalArgumentException if <var>constraint<var> is
   *   <code>null</code>
   */
  public TuplesWrapperResolution(Tuples tuples, Constraint constraint)
  {
    // Validate "constraint" parameter
    if (constraint == null) {
      throw new IllegalArgumentException("Null \"constraint\" parameter");
    }

    // Initialize fields
    this.constraint = constraint;
    this.tuples = tuples;
  }

  //
  // Methods implementing Resolution
  //

  public Constraint getConstraint()
  {
    return constraint;
  }


  /**
   * @return This is always <code>true</code>.
   */
  public boolean isComplete()
  {
    return true;
  }


  /**
   * Converts this class into a formatted string
   *
   * @return A string representation of the tuples that this wraps
   */
  public String toString() {
    return tuples.toString();
  }


  /**
   * Tests for equality.
   *
   * @param o Object to test against.
   * @return <code>true</code> if this obj is equal to this object.
   */
  public boolean equals(Object o) {
    if (!(o instanceof TuplesWrapperResolution)) return false;
    TuplesWrapperResolution t = (TuplesWrapperResolution)o;
    return constraint.equals(t.constraint) && tuples.equals(t.tuples);
  }


  /**
   * Added to match {@link #equals(Object)}.
   */
  public int hashCode() {
    return constraint.hashCode() ^ tuples.hashCode();
  }

  /**
   * Clone the tuples and constraint, and re-wrap them in a new object.
   */
  public Object clone() {
    try {
      TuplesWrapperResolution copy = (TuplesWrapperResolution)super.clone();
      copy.tuples = (Tuples)tuples.clone();

      return copy;
    } catch (CloneNotSupportedException ec) {
      throw new IncompatibleClassChangeError("CloneNotSupportedException thrown on Resolution");
    }
  }


  /**
   * The variables bound and their default collation order. The array returned
   * by this method should be treated as if its contents were immutable, even
   * though Java won't enforce this. If the elements of the array are modified,
   * there may be side effects on the past and future clones of the tuples it
   * was obtained from.
   *
   * @return The Variables value
   */
  public Variable[] getVariables() {
    return tuples.getVariables();
  }

  /**
   * This method returns an upper bound on the number of rows which this
   * instance contains, or an exact value if the instance
   * {@link #isMaterialized}.
   *
   * @return The RowCount value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getRowCount() throws TuplesException {
    return tuples.getRowCount();
  }

  /**
   * This method returns cardinality of the number of rows which this instance contains.
   *
   * @return The cardinality of this tuples. {0,1,N} rows.
   * @throws TuplesException EXCEPTION TO DO
   */
  public int getRowCardinality() throws TuplesException {
    return tuples.getRowCardinality();
  }


  /* (non-Javadoc)
   * @see org.mulgara.query.Cursor#isEmpty()
   */
  public boolean isEmpty() throws TuplesException {
    return tuples.isEmpty();
  }
  /**
   * Accessor for the binding of a given variable within the current product
   * term (row).
   *
   * @param variable the variable binding to query
   * @return the bound value, or {@link Tuples#UNBOUND} if there is no binding
   *      within the current product term (row)
   * @throws TuplesException if there is no current row (before first or after
   *      last) or if <var>variable</var> isn't an element of {@link
   *      #getVariables}
   */
  public int getColumnIndex(Variable variable) throws TuplesException {
    return tuples.getColumnIndex(variable);
  }

  /**
   * Whether a variable (column) is {@link Tuples#UNBOUND unbound} in any
   * minterm (row) of this instance.
   *
   * @param column  the variable to check
   * @return whether the <var>column</var> is {@link Tuples#UNBOUND unbound} in
   *   any minterm (row) of this instance
   * @throws TuplesException if <var>column</var> does not exist in this
   *   instance
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    return tuples.isColumnEverUnbound(column);
  }

  /**
   * If a tuples is materialized, then:
   * <ul>
   * <li>the {@link #getRowCount} method returns an exact value rather than
   *     an upper bound</li>
   * <li>element access is cached, rather than requiring recalculation</li>
   * </ul>
   *
   * @return whether this instance actually exists in physical storage, rather
   *         than being calculated on demand
   */
  public boolean isMaterialized() {
    return tuples.isMaterialized();
  }

  /**
   * An unconstrained tuples is a proposition which is always true.
   *
   * It is independent of variable bindings.
   *
   * @return whether this instance is equal to the unconstrained tuples
   * @throws TuplesException if unconstrainedness couldn't be tested
   */
  public boolean isUnconstrained() throws TuplesException {
    return tuples.isUnconstrained();
  }

 /**
   * Test for the absence of duplicate product terms (rows).
   *
   * If this is <var>false</var>, it is not guaranteed that the tuples has
   * duplicate terms.
   *
   * @return <code>true</code> only if this instance has no duplicate terms
   * @throws TuplesException if the presence of duplicate terms can't be tested
   */
  public boolean hasNoDuplicates() throws TuplesException {
    return tuples.hasNoDuplicates();
  }

  /**
   * Obtain the sort ordering of this instance.
   *
   * @return a comparator specifying the collation order of this instance, or
   *      <code>null</code> if this tuples is unsorted
   */
  public RowComparator getComparator() {
    return tuples.getComparator();
  }

  /**
   * Return the list of operands to this tuples.
   *
   * This is intended to allow debugging traversal of an unevaluated tuples hierachy.
   * Be aware that the tuples returned from this method are not cloned, and should
   * be considered immutable.
   */
  public List<Tuples> getOperands() {
    return tuples.getOperands();
  }

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
  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    tuples.beforeFirst(prefix, suffixTruncation);
  }

  /**
   * Get the binding of a variable (column) in the currect product term (row)
   *
   * @param column the column number; columns are numbered starting from 0
   * @return the value of the column, or {@link #UNBOUND}
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getColumnValue(int column) throws TuplesException {
    return tuples.getColumnValue(column);
  }

  /** @see org.mulgara.store.tuples.Tuples#getRawColumnValue(int) */
  public long getRawColumnValue(int column) throws TuplesException {
    return tuples.getColumnValue(column);
  }

  /**
   * Renames the variables which label the tuples if they have the "magic" names
   * such as "Subject", "Predicate", "Object" and "Meta".
   *
   * @param constraint PARAMETER TO DO
   */
  public void renameVariables(Constraint constraint) {
    tuples.renameVariables(constraint);
  }

  /**
   * Move to the next row satisfying the current prefix and suffix truncation.
   *
   * If no such row exists, return <code>false<code> and the current row
   * becomes unspecified.  The current row is unspecified when a tuples instance
   * is created.  To specify the current row, the {@link #beforeFirst()} or
   * {@link #beforeFirst(long[], int)} methods must be invoked
   *
   * @return whether a subsequent row with the specified prefix exists
   * @throws IllegalStateException if the current row is unspecified.
   * @throws TuplesException {@inheritDoc}
   */
  public boolean next() throws TuplesException {
    return tuples.next();
  }

  /**
   * This method returns an upper bound on the number of rows which this instance contains.
   *
   * @return The upper bound of the number of rows that this instance contains.
   * @throws TuplesException Error accessing the underlying data.
   */
  public long getRowUpperBound() throws TuplesException {
    return tuples.getRowUpperBound();
  }

  /**
   * This method returns an expected count on the number of rows which this instance contains.
   *
   * @return The expected value of the number of rows that this instance contains.
   * @throws TuplesException Error accessing the underlying data.
   */
  public long getRowExpectedCount() throws TuplesException {
    return tuples.getRowExpectedCount();
  }

  /**
   * Returns the number of variables (columns).
   *
   * @return the number of variables (columns)
   */
  public int getNumberOfVariables() {
    return tuples.getNumberOfVariables();
  }

  /**
   * Free resources associated with this instance.
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public void close() throws TuplesException {
    tuples.close();
  }

  /**
   * Reset to iterate through every single element.
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public void beforeFirst() throws TuplesException {
    tuples.beforeFirst();
  }

  /**
   * Copied from AbstractTuples
   */
  public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
    return null;
  }
}
