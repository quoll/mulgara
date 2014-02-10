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
 * @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
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
class RestrictionTuples extends WrappedTuples {
  private final static Logger logger = Logger.getLogger(RestrictionTuples.class);

  protected RestrictPredicate pred;

  /**
   * Applies a relational restriction on a tuples using a positive predicate.
   *
   * Rows for which pred.pass() returns true are retained.
   */
  public RestrictionTuples(Tuples tuples, RestrictPredicate pred) throws TuplesException
  {
    super((Tuples)tuples.clone());

    this.pred = pred;
  }

  //
  // Methods implementing Tuples
  //

  /**
   * The number of times this method can be called is limited.
   *
   * @return RETURNED VALUE TO DO
   * @throws TuplesException EXCEPTION TO DO
   */
  public boolean next() throws TuplesException {
    while(true) {
      if (!tuples.next()) {
        return false;
      } else if (pred.pass(tuples)) {
        return true;
      }
    }
  }
}
