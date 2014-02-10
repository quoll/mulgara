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
import java.util.Arrays;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.TuplesException;

/**
 * Order the rows of a tuples in ascending order by node number. Columns with
 * lower index numbers are major.
 *
 * @created 2003-02-03
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/01/05 04:59:10 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class DefaultRowComparator implements RowComparator {

  /**
   * Description of the Field
   */
  private static final Logger logger = Logger.getLogger(DefaultRowComparator.class);

  /**
   * Description of the Field
   */
  private final static RowComparator INSTANCE = new DefaultRowComparator();

  /**
   * Gets the Instance attribute of the DefaultRowComparator class
   *
   * @return The Instance value
   */
  public static RowComparator getInstance() {

    return INSTANCE;
  }

  /**
   * This class is stateless, so equality is just a type check.
   */
  public boolean equals(Object o) {
    return o != null && o.getClass() == DefaultRowComparator.class;
  }

  public int hashCode() {
    return super.hashCode();
  }

  //
  // Methods implementing RowComparator interface
  //

  /**
   * METHOD TO DO
   *
   * @param lhs PARAMETER TO DO
   * @param rhs PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public int compare(long[] lhs, long[] rhs) {

    if (lhs.length != rhs.length) {

      throw new IllegalArgumentException("LHS array length (" + lhs.length +
        ") doesn't match RHS (" + rhs.length + ")");
    }

    // Compare each column for equality
    for (int i = 0; i < lhs.length; i++) {

      if (lhs[i] < rhs[i]) {

        return -1;
      }

      if (lhs[i] > rhs[i]) {

        return +1;
      }
    }

    // We have a duplicate row
    return 0; // indicate equality
  }

  /**
   * METHOD TO DO
   *
   * @param lhs PARAMETER TO DO
   * @param rhs PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public int compare(long[] lhs, Tuples rhs) throws TuplesException {

    if (lhs.length > rhs.getNumberOfVariables()) {
      // We permit the length of lhs to be less than the number of columns
      // of rhs to permit the use of this method where lhs is a prefix.
      throw new IllegalArgumentException(
          "LHS array length (" + lhs.length + ") is greater than RHS (" +
          rhs.getNumberOfVariables() + ")"
      );
    }

    // Compare each column for equality
    try {

      for (int i = 0; i < lhs.length; i++) {

        long rhsValue = rhs.getColumnValue(i);

        if (lhs[i] < rhsValue) {

          return -1;
        }

        if (lhs[i] > rhsValue) {

          return +1;
        }
      }
    }
     catch (TuplesException e) {

      logger.error("Couldn't perform row comparison", e);
      throw e;
    }

    // We have a duplicate row
    return 0; // indicate equality
  }

  /**
   * METHOD TO DO
   *
   * @param lhs PARAMETER TO DO
   * @param rhs PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public int compare(Tuples lhs, Tuples rhs) throws TuplesException {

    // Compare each column for equality
    int lhsLength = lhs.getNumberOfVariables();

    if (lhsLength != rhs.getNumberOfVariables()) {
      throw new IllegalArgumentException(
          "LHS number of variables (" + lhsLength + ") doesn't match RHS (" +
          rhs.getNumberOfVariables() + ")"
      );
    }

    assert Arrays.equals(lhs.getVariables(), rhs.getVariables()) :
        "Variables arrays must match, lhs length:" + lhsLength +
        " vs rhs length: " + rhs.getVariables().length + " lhs value: " +
        lhs + ", rhs: " + rhs;

    for (int i = 0; i < lhsLength; i++) {

      long lhsValue = lhs.getColumnValue(i);
      long rhsValue = rhs.getColumnValue(i);

      if (lhsValue < rhsValue) {

        return -1;
      }

      if (lhsValue > rhsValue) {

        return +1;
      }
    }

    // We have a duplicate row
    return 0; // indicate equality
  }
}
