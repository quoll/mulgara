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

package org.mulgara.resolver;

// Java 2 standard packages;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.store.tuples.LiteralTuples;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/**
 * The base class for transitive functions.
 *
 * @created 2004-06-07
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:24 $
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
public abstract class TransitiveFunction {

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(TransitiveFunction.class.getName());

  /**
   * This method finds all statements that match a <code>given</code> tuples,
   * and adds new statements based on these results to the collection of
   * <code>inferredTuples</code>.  It then chains these results
   * (via iteration) into a new set of inferences.
   *
   * @param baseSubject The subject that this chain is inferencing for.
   * <code>null</code> if this method is to produce only a single column.
   * @param subject the internal value of the original subject.
   * @param initialTuples All existing statements in the model which have the
   *   required predicate.
   * @param given The current base of the chain, as a set of starting subjects
   *   for statements.  Closed by this method.
   * @param visited A set of all nodes which have been inferred from for
   *   <code>subject</code>. This is to prevent loops.
   * @param inferredResult An in/out parameter which holds all the inferred
   *   statements so far.  This method will append to it.
   * @throws TuplesException If there was a problem while moving around in
   *   results of the query resolutions.
   */
  protected static void inferTransitiveStatements(
      Long baseSubject, long subject, Tuples initialTuples,
      Tuples given, Set<Long> visited, LiteralTuples inferredResult)
      throws TuplesException {
    assert initialTuples.getNumberOfVariables() == 2;
    assert given.getNumberOfVariables() == 1;
    assert baseSubject == null && inferredResult.getNumberOfVariables() == 1 ||
           inferredResult.getNumberOfVariables() == 2;

    // get out the column variable being used by given
    Variable givenColumn = given.getVariables()[0];

    if (logger.isDebugEnabled()) {
      logger.debug("Inferring for subject: " + baseSubject);
      logger.debug("Given is on column: " + givenColumn.getName());
      Variable[] vars = initialTuples.getVariables();
      String varnames = "";
      for (int v = 0; v < vars.length; v++) {
        varnames += " " + vars[v];
      }
      logger.debug("InitialTuples has columns: " + varnames);
    }

    // True if we should search in s, o
    boolean searchInOrder = given.getVariables()[0].
        equals(initialTuples.getVariables()[0]);

    LiteralTuples newGiven = null;

    // Each iteration of this loop steps forward in the inference chain
    do {
      // join the current "given" on all the predicates
      Tuples joinResult = TuplesOperations.join(given, initialTuples);
      if (logger.isDebugEnabled()) {
        given.beforeFirst();
        String gs = "";
        while (given.next()) {
          gs += " " + given.getColumnValue(0);
        }
        if (logger.isDebugEnabled()) logger.debug("Joined with a given of: " + gs);
      }

      TuplesException te = null;
      try {
        // check if there is any data from the join
        joinResult.beforeFirst();
        if (!joinResult.next()) {
          break;
        }

        // determine the column numbers for the variables
        int[] tuplesIndex = new int[2];
        tuplesIndex[0] = joinResult.getColumnIndex(givenColumn);
        tuplesIndex[1] = 1 - tuplesIndex[0];

        // Create a new given tuples, with the same column variable as the original
        newGiven = new LiteralTuples(new Variable[] {givenColumn});

        // Iterate through all the results, we are already on the first line
        do {
          // get the object
          long object = joinResult.getColumnValue(tuplesIndex[1]);
          Long objectL = new Long(object);

          if (logger.isDebugEnabled()) {
            long s = joinResult.getColumnValue(tuplesIndex[0]);
            logger.debug("** found subject=" + s + "; object=" + object);
            logger.debug("** inserting [" + baseSubject + "," + object + "]");
          }

          // add this row to the final result if it doesn't exist in tuples
          if (baseSubject == null) {
            // Search for existing s, o or o, s in initial tuples.
            if ((searchInOrder && !tuplesExist(initialTuples, subject, object)) ||
               (!searchInOrder && !tuplesExist(initialTuples, object, subject))) {

              inferredResult.appendTuple(new long[] { object });
            }
          } else {
            // Search for existing s, o or o, s in initial tuples.
            if ((searchInOrder && !tuplesExist(initialTuples, subject, object)) ||
               (!searchInOrder && !tuplesExist(initialTuples, object, subject))) {
              inferredResult.appendTuple(new long[] { subject, object });
            }
          }

          // nothing more to do with this object if we have already seen it
          if (visited.contains(objectL)) {
            continue;
          }

          // add object to the new given
          newGiven.appendTuple(new long[] { object });

          // tell the visited set that we have been here
          visited.add(objectL);
        } while (joinResult.next());
      } catch (TuplesException e) {
        te = e;
      } finally {
        // clean up the tuples objects, either by falling through, or from the break above
        try {
          if (joinResult != null) joinResult.close();
        } catch (TuplesException e) {
          if (te == null) te = e;
          else logger.info("Suppressing exception closing tuples on failed transitive function", e);
        } finally {
          try {
            if (given != null) given.close();
          } catch (TuplesException e) {
            if (te == null) te = e;
            else logger.info("Suppressing exception closing tuples on failed transitive function", e);
          }
        }
        if (te != null) throw te;
      }

      // use this new given, and iterate
      given = newGiven;
    } while (newGiven.getRowCardinality() != Cursor.ZERO);

    // close the empty tuples if it exists
    if (newGiven != null) {
      newGiven.close();
    }
  }

  /**
   * Returns true if the given s, o already exists in the initial tuples.
   *
   * @return true if the given s, o already exists in the initial tuples.
   * @throws TuplesException if there's an error accessing the tuples.
   */
  protected static boolean tuplesExist(Tuples initialTuples, long s, long o)
      throws TuplesException {
    initialTuples.beforeFirst(new long[] {s, o}, 0);
    return initialTuples.next();
  }
}
