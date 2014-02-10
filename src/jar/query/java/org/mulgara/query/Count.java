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

/**
 * Variable nodes.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @created 2002-May-27
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class Count extends AggregateFunction {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 1403671291133733907L;

  /**
   * Create a new <code>count()</code> column.
   *
   * @param variable the column name
   * @param query the query this aggregation function will count the rows of
   * @throws IllegalArgumentException if <var>query</var> is <code>null</code>
   */
  public Count(Variable variable, Query query) {
    super(variable, query);
  }

  /**
   * A count looks like <code>count()</code> surrounding the query it wraps.
   *
   * @return RETURNED VALUE TO DO
   */
  public String toString() {

    return "count(" + getQuery() + ")";
  }
}
