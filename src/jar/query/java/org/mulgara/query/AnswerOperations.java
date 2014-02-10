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

// Third party packages
import org.apache.log4j.Logger;

/**
* Operations permitted on {@link Answer}s.
*
* @created 2004-03-09
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
* @copyright &copy;2001-2003 <a href="http://www.pisoftware.com/">Plugged In
*      Software Pty Ltd</a>
*
* @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
*/
public abstract class AnswerOperations {

  /**
  * Logger.
  *
  * This is named after the class.
  */
  private final static Logger logger = Logger.getLogger(AnswerOperations.class);

  /**
  * Equality is by value, with both row and column order significant.
  *
  * @param lhs  one of the operands to compare (never <code>null</code>)
  * @param rhs  the other operand to compare (never <code>null</code>)
  * @return whether the two operands are equal
  * @throws IllegalArgumentException if <var>lhs</var> or <var>rhs</var> are
  *                                  <code>null</code>
  * @throws TuplesException if <var>lhs</var> or <var>rhs</var> can't be read
  */
  public static boolean equal(Answer lhs, Answer rhs) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("Checking equality between\nLHS "+lhs+" and\nRHS "+rhs);
    }

    // Validate "lhs" parameter
    if (lhs == null) {
      throw new IllegalArgumentException("Null \"lhs\" parameter");
    }

    // Validate "rhs" parameter
    if (rhs == null) {
      throw new IllegalArgumentException("Null \"rhs\" parameter");
    }

    // Check that columns match
    int numberOfVariables = lhs.getNumberOfVariables();
    if (rhs.getNumberOfVariables() != numberOfVariables) {
      return false;
    }
    for (int i=0; i<numberOfVariables; i++) {
      if (!lhs.getVariables()[i].equals(rhs.getVariables()[i])) {
        return false;
      }
    }

    // Special treatment for unconstrained and empty answers
    if (numberOfVariables == 0) {
      return (lhs.getRowCardinality() == Cursor.ZERO) == (rhs.getRowCardinality() == Cursor.ZERO);
    }

    // Clone the arguments to avoid interfering with their cursors
    lhs = (Answer) lhs.clone();
    try {
      rhs = (Answer) rhs.clone();
      // Check that all row values match
      lhs.beforeFirst();
      rhs.beforeFirst();
      for (;;) {
        // Advance to the next row
        boolean lhsFinished = !lhs.next();
        boolean rhsFinished = !rhs.next();

        // If there's no next row, stop looping
        if (lhsFinished || rhsFinished) {
          return lhsFinished && rhsFinished;
        }

        // Verify that the rows match
        for (int i=0; i<numberOfVariables; i++) {

          if (lhs.getObject(i) != null && rhs.getObject(i) != null) {

            // Return false if one object is null
            if (lhs.getObject(i) == null) {
              return false;
            }
            if (rhs.getObject(i) == null) {
              return false;
            }

            // Compare
            if (!lhs.getObject(i).equals(rhs.getObject(i))) {
              return false;
            }
          }
          // Both objects were null
          else {
            return true;
          }
        }
      }
    }
    finally {
      // Close our clones
      try {
        lhs.close();
      } finally {
        if (rhs != null) rhs.close();
      }
    }
  }
  
  public static int hashCode(Answer ans) {
    try {
      // Clone the arguments to avoid interfering with their cursors
      Answer a = (Answer)ans.clone();
  
      // Check that columns match
      int numberOfVariables = a.getNumberOfVariables();
      int result = numberOfVariables;
  
      try {
        // Iterate over the rows
        a.beforeFirst();
        while (a.next()) {
          // add in all of the row
          for (int i = 0; i < numberOfVariables; i++) {
            Object o = a.getObject(i);
            if (o != null) result ^= o.hashCode();
          }
        }
      } finally {
        // Close the clone
        a.close();
      }
      return result;
    } catch (TuplesException e) {
      throw new RuntimeException("Unable to generate hashCode for answer", e);
    }
  }
}
