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
import java.util.List;

// Local packages
import org.mulgara.query.Constraint;
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * A structure similar to a {@link java.sql.ResultSet}.
 *
 * Theoretically, it's a
 * logical expression of variable assignments (bindings of {@link Variable}s to
 * <code>long</code> values composed using conjunctions (AND) and disjunctions
 * (OR). Negation is not supported. In practice, the expression is arranged as a
 * sum of products. The product terms are usually sorted and without duplicates.
 * The product terms are accessed by iterating through them; a tuples has a
 * cursor position which starts before the first term and finishes after the
 * last. The choice of a tabular structure is based on the tendency for the same
 * small set of variables to appear in every term. As this assumption breaks
 * down, unbound column values appear in the terms. Tuples are partially
 * immutable in that the logical expression cannot change after construction,
 * but mutable because of the changing cursor position (the {@link Cursor
 * superinterface}).  They are persistent, so the {@link #close} method must be
 * invoked before they are abandoned to garbage collection. The source of new
 * tuples is the {@link org.mulgara.store.tuples.TuplesOperations} class.
 *
 * @created 2002-12-03
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/05/16 11:07:10 $
 *
 * @maintenanceAuthor $Author: amuys $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2003 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface Tuples extends Cursor, Cloneable {

  /**
   * A magical non-node value indicating that the variable associated with a
   * column is unconstrained.
   *
   * People usually think of this as the <q>null</q> column value.
   */
  public final static long UNBOUND = 0;

  /**
   * The zero length column prefix.
   */
  public final static long[] NO_PREFIX = new long[] {};

  /**
   * The variables bound and their default collation order. The array returned
   * by this method should be treated as if its contents were immutable, even
   * though Java won't enforce this. If the elements of the array are modified,
   * there may be side effects on the past and future clones of the tuples it
   * was obtained from.
   *
   * @return The Variables value
   */
  public Variable[] getVariables();

  /**
   * This method returns an upper bound on the number of rows which this
   * instance contains, or an exact value if the instance
   * {@link #isMaterialized}.
   *
   * @return The RowCount value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getRowCount() throws TuplesException;

  /**
   * Accessor for the column index of a given variable in this Tuple.
   *
   * @param variable The column variable to query
   * @return The index of the named column.  Columns are indexed starting at 0.
   * @throws TuplesException if <var>variable</var> isn't an element of {@link #getVariables()}
   */
  public int getColumnIndex(Variable variable) throws TuplesException;

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
  public boolean isColumnEverUnbound(int column) throws TuplesException;

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
  public boolean isMaterialized();

  /**
   * An unconstrained tuples is a proposition which is always true.
   *
   * It is independent of variable bindings.
   *
   * @return whether this instance is equal to the unconstrained tuples
   * @throws TuplesException if unconstrainedness couldn't be tested
   */
  public boolean isUnconstrained() throws TuplesException;

  /**
   * Test for the absence of duplicate product terms (rows).
   *
   * If this is <var>false</var>, it is not guaranteed that the tuples has
   * duplicate terms.
   *
   * @return <code>true</code> only if this instance has no duplicate terms
   * @throws TuplesException if the presence of duplicate terms can't be tested
   */
  public boolean hasNoDuplicates() throws TuplesException;

  /**
   * Obtain the sort ordering of this instance.
   *
   * @return a comparator specifying the collation order of this instance, or
   *      <code>null</code> if this tuples is unsorted
   */
  public RowComparator getComparator();

  /**
   * Return the list of operands to this tuples.
   *
   * This is intended to allow debugging traversal of an unevaluated tuples hierachy.
   * Be aware that the tuples returned from this method are not cloned, and should
   * be considered immutable.
   */
  public List<Tuples> getOperands();

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
  public void beforeFirst(long[] prefix,
      int suffixTruncation) throws TuplesException;

  /**
   * Get the binding of a variable (column) in the currect product term (row)
   *
   * @param column the column number; columns are numbered starting from 0
   * @return the value of the column, or {@link #UNBOUND}
   * @throws TuplesException If there was a Tuples specific error accessing the data.
   */
  public long getColumnValue(int column) throws TuplesException;

  /**
   * Gets the raw (unfiltered) ColumnValue attribute of the AbstractTuples object.
   * This is only useful for filtered tuples. By default will return the normal column value.
   *
   * @param column The column offset to get data from
   * @return The column value as a gNode
   * @throws TuplesException If there was a Tuples specific error accessing the data.
   */
  public long getRawColumnValue(int column) throws TuplesException;

  /**
   * Renames the variables which label the tuples if they have the "magic" names
   * such as "Subject", "Predicate", "Object" and "Meta".
   *
   * @param constraint PARAMETER TO DO
   */
  public void renameVariables(Constraint constraint);

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
  public boolean next() throws TuplesException;

  // The preceding declaration only adds documentation to the existing next()
  // method of the Cursor interface

  //
  // Methods overriding Object
  //

  /**
   * All implementations must support cloning.
   *
   * Because tuples are immutable, cloning is a very frequent operation.
   *
   * @return the cloned instance
   */
  public Object clone();

  /**
   * Tuples are equal by sort order and row content.
   *
   * @param object  the instance to compare for equality
   * @return whether the <var>object</var> is another {@link Tuples} with the
   *         same sort order and row content
   */
  public boolean equals(Object object);

  /**
   * METHOD TO DO
   *
   * @return {@inheritDoc}
   */
  public String toString();

  /**
   * Return an Annotation object representing an extension or hint
   * to this interface.
   *
   * @return An annotation of the class requested, or null if none exists.
   */
  public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException;
}
