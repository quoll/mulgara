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

// Log4j
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.store.tuples.*;

/**
 * {@link Resolution} from {@link Statements}.
 *
 * @created 2004-05-05
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/02/22 08:16:18 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class StatementsWrapperResolution extends AbstractTuples implements
    Resolution {
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(StatementsWrapperResolution.class.getName());

  /** The constraint this instance resolves.  */
  private final Constraint constraint;

  /** The statements to filter.  */
  private Statements statements;

  /** prefix passed to previous beforeFirst call */
  private long[] prefix;

  /**
   * Which column of the {@link #statements} provides the value of each
   * column of the current resolution tuple row.
   */
  private final int[] columnIndex;

  /** Precalculated return value fot the {@link #isComplete} method.  */
  private final boolean complete;

  //
  // Constructors
  //

  /**
   * Construct the resolution to a constraint from a set of statings.
   *
   * We assume that the <var>statements</var> are a complete resolution to the
   * model element of the <var>constraint</var> if that model isn't variable.
   *
   * @param constraint  the constraint to resolve, never <code>null</code>
   * @param statements  the statements to filter for constraint satisfaction,
   *   never <code>null</code>
   * @param complete  whether the <var>statements</var> are the complete and
   *   definitive solution to the <var>constraint</var>
   * @throws IllegalArgumentException if the <var>constraint</var> or
   *   <var>statements</var> are <code>null</code>
   */
  public StatementsWrapperResolution(Constraint constraint,
      Statements statements,
      boolean complete) {
    // Validate "constraint" parameter
    if (constraint == null) {
      throw new IllegalArgumentException("Null \"constraint\" parameter");
    }

    // Validate "statements" parameter
    if (statements == null) {
      throw new IllegalArgumentException("Null \"statements\" parameter");
    }

    // Initialize fields
    this.complete = complete;
    this.constraint = constraint;
    this.statements = statements;

    // Calculate columnIndex and set the variable list
    int length = 0;
    int[] temp = new int[3];
    List<Variable> variableList = new ArrayList<Variable>(3);
    for (int i = 0; i < 3; i++) {
      if (constraint.getElement(i) instanceof Variable) {
        temp[length++] = i;
        variableList.add((Variable)constraint.getElement(i));
      }
    }
    columnIndex = new int[length];
    for (int i = 0; i < length; i++) {
      columnIndex[i] = temp[i];
    }
    setVariables(variableList);
  }

  //
  // Methods implementing Resolution
  //

  public Constraint getConstraint() {
    return constraint;
  }

  /**
   * @return <code>true</code> only if the constraint specifies a model
   */
  public boolean isComplete() {
    return complete;
  }

  //
  // Methods implementing Cursor (superinterface of Statements)
  //

  public void beforeFirst(long[] prefix,
      int suffixTruncation) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("Resetting");
    }

    // Validate "prefix" parameter
    if (prefix == null) {
      throw new IllegalArgumentException("Null \"prefix\" parameter");
    }

    // Validate "suffixTruncation" parameter
    if (suffixTruncation != 0) {
      throw new IllegalArgumentException("Nonzero suffix truncationr");
    }

    // Reset fields
    statements.beforeFirst();
    this.prefix = prefix;

    if (logger.isDebugEnabled()) {
      logger.debug("Reset");
    }
  }

  public Object clone() {
    StatementsWrapperResolution cloned =
        (StatementsWrapperResolution)super.clone();

    // Copy mutable fields by value
    cloned.statements = (Statements) statements.clone();

    return cloned;
  }

  /**
   * Close the RDF/XML formatted input stream.
   */
  public void close() throws TuplesException {
    statements.close();
  }

  /**
   * @param column  0 for the subject, 1 for the predicate, 2 for the object
   */
  public long getColumnValue(int column) throws TuplesException {
    if (column < 0 || column >= columnIndex.length) {
      throw new TuplesException("No such column: " + column);
    }

    switch (columnIndex[column]) {
      case 0:
        return statements.getSubject();

      case 1:
        return statements.getPredicate();

      case 2:
        return statements.getObject();

      default:
        throw new Error("Bad columnIndex: " + columnIndex[column]);
    }
  }

  public List<Tuples> getOperands() {
    return Collections.singletonList((Tuples)statements);
  }

  public long getRowCount() throws TuplesException {
    return statements.getRowCount();
  }

  public boolean isEmpty() throws TuplesException {
    return statements.isEmpty();
  }

  public long getRowUpperBound() throws TuplesException {
    return statements.getRowUpperBound();
  }

  public long getRowExpectedCount() throws TuplesException {
    return statements.getRowExpectedCount();
  }

  public boolean hasNoDuplicates() throws TuplesException {
    return false;
  }

  public RowComparator getComparator() {
    return null;
  }

  public boolean isColumnEverUnbound(int column) throws TuplesException {
    if (column < 0 || column >= columnIndex.length) {
      throw new TuplesException("No such column: " + column);
    }

    return false;
  }

  public boolean next() throws TuplesException {
    // Advance to the next stating that satisfies the constraint
    while (statements.next()) {
      int prefixIndex = 0;
      // Test the subject
      if (constraint.getElement(0) instanceof LocalNode) {
        long subject = ((LocalNode) constraint.getElement(0)).getValue();
        if (subject != statements.getSubject()) {
          continue;
        }
      }
      else if (prefix.length > prefixIndex &&
          statements.getSubject() != prefix[prefixIndex++]) {
        continue;
      }

      // Test the predicate
      if (constraint.getElement(1) instanceof LocalNode) {
        long predicate = ((LocalNode) constraint.getElement(1)).getValue();
        if (predicate != statements.getPredicate()) {
          continue;
        }
      }
      else if (prefix.length > prefixIndex &&
          statements.getPredicate() != prefix[prefixIndex++]) {
        continue;
      }

      // Test the subject
      if (constraint.getElement(2) instanceof LocalNode) {
        long object = ((LocalNode) constraint.getElement(2)).getValue();
        if (object != statements.getObject()) {
          continue;
        }
      }
      else if (prefix.length > prefixIndex &&
          statements.getObject() != prefix[prefixIndex++]) {
        continue;
      }

      // This row satisfies the constraint
      return true;
    }

    // No more statements that satisfy the constraint
    if (logger.isDebugEnabled()) {
      logger.debug("Complete");
    }

    return false;
  }
}
