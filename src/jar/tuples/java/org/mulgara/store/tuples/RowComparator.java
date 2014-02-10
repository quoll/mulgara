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

// Local packages
import org.mulgara.query.TuplesException;

/**
 * Comparator or rows in a Tuple.
 *
 * @created 2002-12-16
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:10 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface RowComparator {

  /**
   * Compare two current rows from two tuples objects.
   *
   * @param first the first tuples object to compare.
   * @param second the second tuples object to compare.
   * @return 1 if first is larger, -1 is smaller, 0 if equal.
   * @throws TuplesException EXCEPTION TO DO
   */
  public int compare(Tuples first, Tuples second) throws TuplesException;

  /**
   * Compare a row from a tuples object to a given array
   *
   * @param array An array which represents a row.
   * @param tuples The tuples to get the other row from.
   * @return 1 if first is larger, -1 if smaller, 0 if equal.
   * @throws QueryException If there is an error accessing the tuples, or if the
   *      length of the array does not match the width of the tuples.
   * @throws TuplesException EXCEPTION TO DO
   */
  public int compare(long[] array, Tuples tuples) throws TuplesException;

  /**
   * Compare a tuples array to a given array
   *
   * @param first An array which represents a row.
   * @param second PARAMETER TO DO
   * @return 1 if first is larger, -1 if smaller, 0 if equal.
   * @throws QueryException If the length of the arrays do not match.
   * @throws TuplesException EXCEPTION TO DO
   */
  public int compare(long[] first, long[] second) throws TuplesException;
}
