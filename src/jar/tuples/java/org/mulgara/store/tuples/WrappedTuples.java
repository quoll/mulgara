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
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * An implementation of {@link Tuples} wrapping another {@link Tuples} instance.
 * This implementation is entirely transparent; variant functionality is
 * expected to be added by overriding subclasses.
 *
 * @created 2003-08-06
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
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class WrappedTuples implements Tuples {

  /**
   * Logger.
   */
  private final static Logger logger =
      Logger.getLogger(WrappedTuples.class.getName());

  /**
   * The tuples obtained from the graph which satisfy the constraint.
   */
  protected Tuples tuples;

  /**
   * Disjoin a list of propositions.
   *
   * @param tuples PARAMETER TO DO
   * @throws IllegalArgumentException if <var>tuples</var> is <code>null</code>
   * @throws TuplesException EXCEPTION TO DO
   */
  protected WrappedTuples(Tuples tuples) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("Constructing " + getClass());
    }

    init(tuples);
  }

  /** 
   * Must be followed by a call to init.
   */
  protected WrappedTuples() throws TuplesException { }

  protected void init(Tuples tuples) {
    // Validate "tuples" parameter
    if (tuples == null) {
      throw new IllegalArgumentException("Null \"tuples\" parameter");
    }

    this.tuples = tuples;
  }


  /**
   * Gets the ColumnIndex attribute of the WrappedTuples object
   *
   * @param variable PARAMETER TO DO
   * @return The ColumnIndex value
   * @throws TuplesException EXCEPTION TO DO
   */
  public int getColumnIndex(Variable variable) throws TuplesException {

    return tuples.getColumnIndex(variable);
  }

  /**
   * Get the binding of a variable (column) in the currect product term (row)
   *
   * @param column the column number; columns are numbered starting from 0
   * @return the value of the column, or {@link #UNBOUND}
   * @throws TuplesException If there was a Tuples specific error accessing the data.
   */
  public long getColumnValue(int column) throws TuplesException {

    if (logger.isDebugEnabled()) {
      logger.debug("Getting column "+column+" from wrapped "+tuples.getClass());
    }
    return tuples.getColumnValue(column);
  }

  /**
   * Gets the raw (unfiltered) ColumnValue attribute of the AbstractTuples object.
   * This is only useful for filtered tuples. By default will return the normal column value.
   *
   * @param column The column offset to get data from
   * @return The column value as a gNode
   * @throws TuplesException If there was a Tuples specific error accessing the data.
   */
  public long getRawColumnValue(int column) throws TuplesException {
    return getColumnValue(column);
  }

  /**
   * Gets the Comparator attribute of the WrappedTuples object
   *
   * @return The Comparator value
   */
  public RowComparator getComparator() {

    return tuples.getComparator();
  }

  /**
   * Gets the RowCount attribute of the WrappedTuples object
   *
   * @return The RowCount value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getRowCount() throws TuplesException {

    return tuples.getRowCount();
  }

  public long getRowUpperBound() throws TuplesException {
    return tuples.getRowUpperBound();
  }

  public long getRowExpectedCount() throws TuplesException {
    return tuples.getRowExpectedCount();
  }

  public int getRowCardinality() throws TuplesException {
    return tuples.getRowCardinality();
  }

  public boolean isEmpty() throws TuplesException {
    return tuples.isEmpty();
  }

  /**
   * Gets the Variables attribute of the WrappedTuples object
   *
   * @return The Variables value
   */
  public Variable[] getVariables() {
    return tuples.getVariables();
  }

  /**
   * Gets the NumberOfVariables attribute of the WrappedTuples object
   *
   * @return The NumberOfVariables value
   */
  public int getNumberOfVariables() {
    return tuples.getNumberOfVariables();
  }

  /**
   * Delegates to the {@link Tuples#isColumnEverUnbound} method of the wrapped
   * instance.
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    return tuples.isColumnEverUnbound(column);
  }

  /**
   * Gets the Materialized attribute of the WrappedTuples object
   *
   * @return The Materialized value
   */
  public boolean isMaterialized() {

    return tuples.isMaterialized();
  }

  public List<Tuples> getOperands() {
    return Collections.singletonList(tuples);
  }


  /**
   * @return whether any operand is unconstrained
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean isUnconstrained() throws TuplesException {

    return tuples.isUnconstrained();
  }

  //
  // Methods implementing Tuples
  //

  /**
   * METHOD TO DO
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public void beforeFirst() throws TuplesException {

    tuples.beforeFirst();
  }

  /**
   * METHOD TO DO
   *
   * @param prefix PARAMETER TO DO
   * @param suffixTruncation PARAMETER TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public void beforeFirst(long[] prefix,
      int suffixTruncation) throws TuplesException {

    tuples.beforeFirst(prefix, suffixTruncation);
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Object clone() {

    try {

      WrappedTuples cloned = (WrappedTuples)super.clone();
      cloned.tuples = (Tuples) tuples.clone();

      return cloned;
    }
    catch (CloneNotSupportedException e) {

      throw new Error("Clone ought to be supported!", e);
    }
  }

  /**
   * @return this instance in {@link SimpleTuplesFormat}
   */
  public String toString() {

    return SimpleTuplesFormat.format(this);
  }

  /**
   * METHOD TO DO
   *
   * @throws TuplesException EXCEPTION TO DO
   */
  public void close() throws TuplesException {

    tuples.close();
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean hasNoDuplicates() throws TuplesException {

    return tuples.hasNoDuplicates();
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean next() throws TuplesException {

    return tuples.next();
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
   * Required by Tuples.
   */
  public boolean equals(Object o) {
    return (o instanceof Tuples) && AbstractTuples.equals(this, (Tuples)o);
  }

  /**
   * Added to match {@link #equals(Object)}.
   */
  public int hashCode() {
    return TuplesOperations.hashCode(this);
  }

  /**
   * Copied from AbstractTuples.
   */
  public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
    return null;
  }
}
