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

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.TuplesException;

/**
 * Skip a specified number of leading rows.
 *
 * @created 2003-08-06
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
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
class OffsetTuples extends WrappedTuples {

  /**
   * Logger.
   */
  private final static Logger logger = Logger.getLogger(OffsetTuples.class);

  /**
   * The maximum number of minterms (rows) to allow through.
   */
  private long offset;

  /**
   * Whether the leading rows have been skipped yet.
   */
  private boolean beforeFirst;

  /**
   * Choose a window of tuples by their position in the current ordering.
   *
   * The cursor position of the <var>tuples</var> parameter is not preserved
   * by this wrapper.
   *
   * @param tuples PARAMETER TO DO
   * @param rowCount  the number of leading minterms (rows) to skip
   * @throws IllegalArgumentException if <var>operands</var> is
   *   <code>null</code> or <var>rowCount</var> is negative
   * @throws TuplesException EXCEPTION TO DO
   */
  public OffsetTuples(Tuples tuples, long rowCount) throws TuplesException {
    super(tuples);

    // Validate "rowCount" parameter
    if (rowCount < 0) {
      throw new IllegalArgumentException(
        "Bad \"rowCount\" parameter: "+rowCount
      );
    }

    this.tuples.beforeFirst();
    this.beforeFirst = true;
    this.offset = rowCount;
  }

  /**
   * Gets the RowCount attribute of the WrappedTuples object
   *
   * @return The RowCount value
   * @throws TuplesException EXCEPTION TO DO
   */
  public long getRowCount() throws TuplesException {
    long rowCount = tuples.getRowCount();
    return (rowCount < offset) ? 0 : (rowCount - offset);
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
    super.beforeFirst();
    beforeFirst = true;
  }

  /**
   * METHOD TO DO
   *
   * @param prefix PARAMETER TO DO
   * @param suffixTruncation PARAMETER TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public void beforeFirst(long[] prefix, int suffixTruncation)
      throws TuplesException {

    super.beforeFirst(prefix, suffixTruncation);
    beforeFirst = true;
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
    if (beforeFirst) {
      // Skip forward according to the offset
      for (int i = 0; i < offset; i++) {
        if (!tuples.next()) {
          break;
        }
      }
     beforeFirst = false;
    }
    return tuples.next();
  }
}
