/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
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
 * @created 2008-04-04
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Open Software License v3.0</a>
 */
public class PartialColumnComparator implements RowComparator {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(PartialColumnComparator.class);
  
  private int[] varMap;

  public PartialColumnComparator(int[] varMap) {
    this.varMap = varMap;
  }

  /**
   * Compares the LHS and RHS parameters as tuples which are to be compared in column order.
   * @param lhs The left hand side operand.
   * @param rhs The right hand side operand.
   * @return -1 if lhs &lt; rhs, +1 if lhs &gt; rhs, 0 otherwise.
   */
  public int compare(long[] lhs, long[] rhs) {
    if (lhs.length != rhs.length) throw new IllegalArgumentException("Tuples must be the same length for comparison.");
    if (lhs.length < varMap.length) throw new IllegalArgumentException("Too many columns to look for in the tuples.");

    // Compare each column for equality
    for (int i = 0; i < varMap.length; i++) {
      if (lhs[varMap[i]] < rhs[varMap[i]]) return -1;
      if (lhs[varMap[i]] > rhs[varMap[i]]) return +1;
    }
    // We have a duplicate row
    return 0; // indicate equality
  }

  /**
   * Compares in column order the LHS as a row of a Tuples and a RHS Tuples.
   * The lhs may be shorter than the rhs, as it can be for a beforeFirst search.
   * @param lhs An array to be treated as a Tuples row.
   * @param rhs A Tuples, with the current row to be compared.
   * @return -1 if lhs &lt; rhs, +1 if lhs &gt; rhs, 0 otherwise.
   * @throws TuplesException If there is an error accessing the Tuples.
   */
  public int compare(long[] lhs, Tuples rhs) throws TuplesException {

    if (lhs.length > rhs.getNumberOfVariables()) {
      // We permit the length of lhs to be less than the number of columns
      // of rhs to permit the use of this method where lhs is a prefix.
      throw new IllegalArgumentException("LHS array length (" + lhs.length +
              ") is greater than RHS (" + rhs.getNumberOfVariables() + ")");
    }

    // Compare each column for equality
    try {
      int searchlength = Math.min(lhs.length, varMap.length);
      for (int i = 0; i < searchlength; i++) {
        if (varMap[i] >= lhs.length) throw new IllegalArgumentException("Ordering of column search does not match search parameters");
        long rhsValue = rhs.getColumnValue(varMap[i]);
        if (lhs[varMap[i]] < rhsValue) return -1;
        if (lhs[varMap[i]] > rhsValue) return +1;
      }
    } catch (TuplesException e) {
      logger.error("Couldn't perform row comparison", e);
      throw e;
    }
    // We have a duplicate row
    return 0; // indicate equality
  }

  /**
   * Compares in column order the current rows of two Tuples.
   * @param lhs A Tuples with the current row to be compared.
   * @param rhs A Tuples with the current row to be compared.
   * @return -1 if lhs &lt; rhs, +1 if lhs &gt; rhs, 0 otherwise.
   * @throws TuplesException If there is an error accessing the Tuples.
   */
  public int compare(Tuples lhs, Tuples rhs) throws TuplesException {

    // Compare each column for equality
    int lhsLength = lhs.getNumberOfVariables();

    if (lhsLength != rhs.getNumberOfVariables()) throw new IllegalArgumentException("LHS number of variables (" + lhsLength + ") doesn't match RHS (" + rhs.getNumberOfVariables() + ")");

    if (lhsLength < varMap.length) throw new IllegalArgumentException("Too many columns to look for in the tuples.");

    assert Arrays.equals(lhs.getVariables(), rhs.getVariables()) :
        "Variables arrays must match, lhs length:" + lhsLength + " vs rhs length: " + rhs.getVariables().length + " lhs value: " + lhs + ", rhs: " + rhs;

    for (int i = 0; i < varMap.length; i++) {
      long lhsValue = lhs.getColumnValue(varMap[i]);
      long rhsValue = rhs.getColumnValue(varMap[i]);
      if (lhsValue < rhsValue) return -1;
      if (lhsValue > rhsValue) return +1;
    }

    // We have a duplicate row
    return 0; // indicate equality
  }

  public boolean equals(Object o) {
    return (o instanceof PartialColumnComparator) &&
           Arrays.equals(varMap, ((PartialColumnComparator)o).varMap);
  }

  public int hashCode() {
    return varMap.hashCode();
  }
}
