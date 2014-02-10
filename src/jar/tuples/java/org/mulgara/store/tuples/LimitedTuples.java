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
 * Remove trailing minterms (rows) in excess of a specified count.
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
class LimitedTuples extends WrappedTuples {

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(LimitedTuples.class);

  /**
   * The maximum number of minterms (rows) to allow through.
   */
  private long limit;

  /**
   * The current number of rows that have been allowed through.
   */
  private long count;

  /**
   * Choose a window of tuples by their position in the current ordering.
   *
   * This method does not preserve the cursor position passed to it by the
   * <var>tuples</var> parameter.
   *
   * @param tuples PARAMETER TO DO
   * @param rowCount  the number of minterms (rows) to permit before clipping
   *                  trailing minterms
   * @throws IllegalArgumentException if <var>tuples</var> is <code>null</code>
   *                                  on <var>rowCount</var> is negative
   * @throws TuplesException EXCEPTION TO DO
   */
  public LimitedTuples(Tuples tuples, long rowCount) throws TuplesException
  {
    super(tuples);

    // Validate "rowCount" parameter
    if (rowCount < 0) {
      throw new IllegalArgumentException(
        "Bad \"rowCount\" parameter: "+rowCount
      );
    }

    this.tuples.beforeFirst();  // needed to obtain a known cursor position
    this.count = 0;
    this.limit = rowCount;
  }

  /**
   * Gets the RowCount attribute of the WrappedTuples object
   *
   * @return The RowCount value
   * @throws TuplesException Error accessing underlying tuples.
   */
  public long getRowCount() throws TuplesException {
    long rowCount = tuples.getRowCount();
    return (rowCount < limit) ? rowCount : limit;
  }

  public long getRowUpperBound() throws TuplesException {
    long rowCount = tuples.getRowUpperBound();
    return (rowCount < limit) ? rowCount : limit;
  }

  public long getRowExpectedCount() throws TuplesException {
    long rowCount = tuples.getRowExpectedCount();
    return (rowCount < limit) ? rowCount : limit;
  }

  //
  // Methods implementing Tuples
  //

  public void beforeFirst() throws TuplesException {

    super.beforeFirst();
    count = 0;
  }

  public void beforeFirst(long[] prefix, int suffixTruncation)
    throws TuplesException
  {
    super.beforeFirst(prefix, suffixTruncation);
    count = 0;
  }

  /**
   * The number of times this method can be called is limited.
   *
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean next() throws TuplesException {

    assert count <= limit;

    if (count == limit) {
      return false;
    }

    count++;
    return tuples.next();
  }
}
