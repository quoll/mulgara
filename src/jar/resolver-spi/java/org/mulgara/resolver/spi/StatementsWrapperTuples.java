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
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.Tuples;

/**
 * {@link Tuples} from {@link Statements} based on
 * {@link StatementsWrapperResolution}.
 *
 * <p>Implements the {@link Tuples} interface whilst maintaining the
 * {@link Statements} interface.
 *
 * @created 2004-02-20
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:50 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class StatementsWrapperTuples
    extends AbstractTuples
    implements Statements {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(StatementsWrapperTuples.class.getName());

  /** The statements to wrap.  */
  private Statements statements;

  /** prefix passed to previous beforeFirst call */
  private long[] prefix;

  /**
   * Constructor.
   *
   * @param statements Statements
   */
  public StatementsWrapperTuples(Statements statements) {

    // Validate "statements" parameter
    if (statements == null) {
      throw new IllegalArgumentException("Null \"statements\" parameter");
    }

    // Initialize fields
    this.statements = statements;

    //use StatementStore variables [0,1,2] (Subject, Predicate, Object)
    setVariables(new Variable[] {
                 StatementStore.VARIABLES[0],
                 StatementStore.VARIABLES[1],
                 StatementStore.VARIABLES[2],
    });
  }

  //
  // Methods implementing Cursor (superinterface of Statements)
  //

  /**
   * Calls before first and sets the prefix for the next call to next().
   *
   * @param prefix long[]
   * @param suffixTruncation int
   * @throws TuplesException
   */
  public void beforeFirst(long[] prefix, int suffixTruncation) throws
      TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("Resetting");
    }

    // Validate "prefix" parameter
    if (prefix == null) {
      throw new IllegalArgumentException("Null \"prefix\" parameter");
    }

    // Validate "suffixTruncation" parameter
    if (suffixTruncation != 0) {
      throw new IllegalArgumentException("Nonzero suffix truncation");
    }

    // Reset fields
    statements.beforeFirst();
    this.prefix = prefix;

    if (logger.isDebugEnabled()) {
      logger.debug("Reset");
    }
  }

  /**
   * Returns a new StatementsWrapperTuples with a clone of the wrapped
   * Statements.
   * @return Object
   */
  public Object clone() {
    return new StatementsWrapperTuples( (Statements) statements.clone());
  }

  /**
   * Delegated to the wrapped statements.
   * @throws TuplesException
   */
  public void close() throws TuplesException {
    statements.close();
  }

  /**
   * Returns either Subject, Predicate or Object depending on column index.
   *
   * @param column 0 for the subject, 1 for the predicate, 2 for the object
   * @throws TuplesException
   * @throws IllegalArgumentException Ifthe column is invalid
   * @return long
   */
  public long getColumnValue(int column) throws TuplesException,
      IllegalArgumentException {

    validateColumn(column);

    switch (column) {
      case 0:
        return statements.getSubject();

      case 1:
        return statements.getPredicate();

      case 2:
        return statements.getObject();

      default:
        throw new Error("Column:" + column + "does not exist");
    }
  }

  /**
   * Returns the wrapped statements object as a singleton list.
   * @return List
   */
  public List<Tuples> getOperands() {
    return Collections.singletonList((Tuples)statements);
  }

  /**
   * Delegated to the wrapped statements.
   * @throws TuplesException
   * @return long
   */
  public long getRowCount() throws TuplesException {
    return statements.getRowCount();
  }

  /* (non-Javadoc)
   * @see org.mulgara.store.tuples.AbstractTuples#isEmpty()
   */
  @Override
  public boolean isEmpty() throws TuplesException {
    return statements.isEmpty();
  }

  /**
   * Delegated to the wrapped statements.
   * @throws TuplesException
   * @return long
   */
  public long getRowUpperBound() throws TuplesException {
    return statements.getRowUpperBound();
  }

  /**
   * Delegated to the wrapped statements.
   * @throws TuplesException
   * @return long
   */
  public long getRowExpectedCount() throws TuplesException {
    return statements.getRowExpectedCount();
  }

  /**
   * Can't guarantee the statements are all unique.
   * @return boolean
   */
  public boolean hasNoDuplicates() {
    return false;
  }

  /**
   * Returns null (Statements may not be ordered).
   * @return RowComparator
   */
  public RowComparator getComparator() {
    return null;
  }

  /**
   * Valid columns are not unbound. There are 3 columns bound to Variables:
   * Subject, Predicate, Object.
   * @param column int
   * @throws TuplesException
   * @return boolean
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    validateColumn(column);
    return false;
  }

  /**
   * Gets the Materialized attribute of the AbstractTuples object
   *
   * @return The Materialized value
   */

  public boolean isMaterialized() {
    return false;
  }

  /**
   * Ensures that the column is within bounds (3 or less, non-negative).
   *
   * @param column int
   * @throws IllegalArgumentException
   */
  private void validateColumn(int column) throws IllegalArgumentException {

    if ( (column < 0) || (column > getNumberOfVariables())) {

      throw new IllegalArgumentException("column: " + column +
                                         " is out of range.");
    }
  }

  /**
   * Goes to the next row in the statements.
   * @throws TuplesException
   * @return boolean
   */
  public boolean next() throws TuplesException {

    //short curcuit if there is no prefix
    if ( (prefix == null) || (prefix.length == 0)) {

      return statements.next();
    }

    return next(this.prefix);
  }

  /**
   * Called if a prefix has been set in last call to before first.
   *
   * @param prefix long[]
   * @throws TuplesException
   * @return boolean
   */
  private boolean next(long[] prefix) throws TuplesException {

    // Advance to the next row that satisfies the prefix
    while (statements.next()) {

      // Test the subject
      if ( (prefix.length > 0)
          && (statements.getSubject() != prefix[0])) {
        continue;
      }

      // Test the predicate
      if ( (prefix.length > 1)
          && (statements.getPredicate() != prefix[1])) {
        continue;
      }

      // Test the subject
      if ( (prefix.length > 2)
          && (statements.getObject() != prefix[2])) {
        continue;
      }

      // prefix has been satisfied
      return true;
    }

    // No more statements that satisfy the constraint
    if (logger.isDebugEnabled()) {
      logger.debug("Complete");
    }

    return false;
  }

  //
  // Statements methods
  //

  /**
   * Delegated to the wrapped statements.
   * @throws TuplesException
   * @return long
   */
  public long getSubject() throws TuplesException {
    return statements.getSubject();
  }

  /**
   * Delegated to the wrapped statements.
   * @return long
   * @throws TuplesException
   */
  public long getPredicate() throws TuplesException {
    return statements.getPredicate();
  }

  /**
   * Delegated to the wrapped statements.
   * @throws TuplesException
   * @return long
   */
  public long getObject() throws TuplesException {
    return statements.getObject();
  }
}
