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

import org.apache.log4j.Logger;

import org.mulgara.query.TuplesException;


/**
 * A dense matrix class that stores long values.  Uses a single dimension
 * array for storage to avoid nested allocations for large matrixes.  All
 * access is bounds checked.
 *
 * @author <a href="http://staff.pisoftware.com/andrae/">Andrae Muys</a>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class DenseLongMatrix {

  private final static Logger logger = Logger.getLogger(DenseLongMatrix.class);

  private long[] matrix;
  private int rows;
  private int columns;

  // Used for sort.
  private long[] pivotRow;
  private long[] tmpRow;

  public DenseLongMatrix(int rows, int columns) {
    this.rows = rows;
    this.columns = columns;
    this.matrix = new long[rows * columns];
    this.pivotRow = new long[columns];
    this.tmpRow = new long[columns];
//    logger.warn("Created matrix " + rows + " rows and " + columns + " columns");
  }


  public long get(int row, int column) {
//    logger.warn("Getting (" + row + ", " + column + ")");
    return matrix[getIndex(row, column)];
  }


  public void set(int row, int column, long value) {
//    logger.warn("Setting (" + row + ", " + column + ") -> " + value);
    matrix[getIndex(row, column)] = value;
  }


  public int getLength() {
//    logger.warn("getting length -> " + rows);
    return rows;
  }


  public int getWidth() {
//    logger.warn("getting width -> " + columns);
    return columns;
  }


  public void sort(RowComparator comparator, int size) throws TuplesException {
//    logger.warn("sorting matrix");
    qsort(comparator, 0, size);
//    logger.warn("sorted matrix");
  }


  private void qsort(RowComparator comparator, int start, int end) throws TuplesException {
    if (end - start < 2) {
      return;
    }

    int pivot = partition(comparator, start, end);

    /* If pivot <= start + 1, then (start, pivot) form a sorted pair */
    if (pivot > (start + 1)) {
      qsort(comparator, start, pivot);
    }
    /* Last element is end - 1; if pivot <= last - 1, then (pivot, last) form a sorted pair */
    if (pivot < (end - 2)) {
      qsort(comparator, pivot + 1, end);
    }
  }


  public long[] loadRow(long[] line, int row) {
//    logger.warn("loading row " + row);
    if (line == null) {
      line = new long[columns];
    }

    for (int i = 0; i < columns; i++) {
      line[i] = get(row, i);
    }

    return line;
  }


  private int partition(RowComparator comparator, int start, int end) throws TuplesException {
    final int size = end - start;
    int pivot = (size / 2) + start;
    int lhs = start;
    int rhs = end - 1;

    for (;;) {
      loadRow(pivotRow, pivot);
      while ((lhs < pivot) && (comparator.compare(pivotRow, loadRow(tmpRow, lhs)) > 0)) {
        lhs++;
      }
      while ((rhs > pivot) && (comparator.compare(pivotRow, loadRow(tmpRow, rhs)) < 0)) {
        rhs--;
      }

      if (lhs >= rhs) {
        return pivot;
      }

      swap(lhs, rhs);

      if (lhs == pivot) {
        lhs++;
        pivot = rhs;
      } else if (rhs == pivot) {
        pivot = lhs;
        rhs--;
      } else {
        lhs++;
        rhs--;
      }
    }
  }


  private void swap(int left, int right) {
    for (int i = 0; i < columns; i++) {
      long temp = get(left, i);
      set(left, i, get(right, i));
      set(right, i, temp);
    }
  }


  private int getIndex(int row, int column) {
    if (row >= rows) {
      throw new ArrayIndexOutOfBoundsException("Matrix row index(" + row + ") requires (0," + rows + "]");
    } else if (column >= columns) {
      throw new ArrayIndexOutOfBoundsException("Matrix row index(" + column + ") requires (0," + columns + "]");
    }

    return row * columns + column;
  }
}
