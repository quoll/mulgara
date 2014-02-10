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

// Local packages
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * A single localized RDF statement.
 *
 * @created 2004-04-29
 * @author <a href="http://staff.tucanatech.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Tucana
 *   Technology, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class SingletonStatements implements Cloneable, Statements {
  /**
   * The columns of the single statement:
   * <code>$subject $predicate $object</code>.
   */
  private final static Variable[] variables = new Variable[] {
                                                  new Variable("subject"),
                                                  new Variable("predicate"),
                                                  new Variable("object") };

  /** Localized subject/pred/object of the single statement.  */
  private final long subject;
  private final long predicate;
  private final long object;

  /**
   * The current row index.
   *
   * This is -1 before the first (only) row, 0 on the row, and 1 after the row.
   * It should never assume any value other than these three.
   */
  private int row = -1;

  //
  // Constructor
  //

  /**
   * Construct a single localized RDF statement.
   */
  public SingletonStatements(long subject, long predicate, long object) {
    // Validate "subject" parameter
    if (subject == NONE) {
      throw new IllegalArgumentException("NONE is not a valid subject");
    }

    // Validate "predicate" parameter
    if (predicate == NONE) {
      throw new IllegalArgumentException("NONE is not a valid predicate");
    }

    // Validate "object" parameter
    if (object == NONE) {
      throw new IllegalArgumentException("NONE is not a valid object");
    }

    // Initialize fields
    this.subject   = subject;
    this.predicate = predicate;
    this.object    = object;
  }

  //
  // Methods implementing Cursor (superinterface of Statements)
  //

  public void beforeFirst() {
    row = -1;
  }

  public void close() {
    // null implementation
  }

  public int getColumnIndex(Variable variable) throws TuplesException {
    // Validate "variable" parameter
    if (variable == null) {
      throw new IllegalArgumentException("Null \"variable\" parameter");
    }

    // Return the appropriate column if cursor is currently on the row
    switch (row) {
      case -1:
        throw new TuplesException("Before first row");

      case 0:
        for (int i = 0; i < 3; i++) {
          if (variables[i] == variable) {
            return i;
          }
        }
        throw new TuplesException("No such column " + variable);

      case 1:
        throw new TuplesException("After last row");

      default:
        throw new Error("Impossible row value: " + row);
    }
  }

  public int getNumberOfVariables() {
    return 3;
  }

  public long getRowCount() {
    return 1;
  }

  public long getRowExpectedCount() {
    return 1;
  }

  public long getRowUpperBound() {
    return getRowCount();
  }

  public int getRowCardinality() {
    return Cursor.ONE;
  }

  public boolean isEmpty() throws TuplesException {
    return false;
  }

  public Variable[] getVariables() {
    return variables;
  }

  public boolean isUnconstrained() {
    return false;
  }

  public boolean next() throws TuplesException {
    switch (row) {
    case -1:
      row++;
      return true;

    case 0:
      row++;
      return false;

    case 1:
      throw new TuplesException("Already after last row");

    default:
      throw new Error("Impossible row value: " + row);
    }
  }

  //
  // Methods implementing Statements
  //

  public long getSubject() throws TuplesException {
    return subject;
  }

  public long getPredicate() throws TuplesException {
    return predicate;
  }

  public long getObject() throws TuplesException {
    return object;
  }

  /**
   * Cloning is always supported.
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error(getClass() + " doesn't support cloning", e);
    }
  }

  public String toString() {
    return "Singleton[" + subject + " " + predicate + " " + object + "]";
  }
}
