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
 * Contributor(s):
 *   getModel() contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Java 2 standard packages;
import java.util.HashSet;
import java.util.Set;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.LiteralTuples;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/**
 * This is an implementation of the walk function that begins or ends
 * (anchored) from a particular subject or object.
 * <p>
 * Example of traversing statements directly: <p></p>
 * <pre>
 *   select $xxx &lt;rdfs:subClassOf&gt; $zzz
 *   where trans(&lt;b&gt; &lt;rdfs:subClassOf&gt; $zzz and
 *   and $xxx &lt;rdfs:subClassOf&gt; $zzz) ;
 *
 *   select $xxx &lt;rdfs:subClassOf&gt; &lt;b&gt;
 *   where trans($xxx &lt;rdfs:subClassOf&gt; &lt;b&gt; and
 *   and $xxx &lt;rdfs:subClassOf&gt; $zzz) ;
 * </pre></p>
 *
 * @created 2004-06-07
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/02/22 08:16:10 $
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
public abstract class WalkFunction {

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(WalkFunction.class.getName());

  /**
   * Resolves a walk constraint.  This means finding all statements matching
   * the predicate statement and returning them.
   *
   * @param predConstraint a constraint describing a predicate to traverse with
   *     a supported model type fourth element of the constraint is not a Variable
   *     or is a Variable with the name _from.
   * @param graphExpression  the Graph to resolve the constraint against if the
   *     fourth element of the constraint is a Variable with the name _from.
   * @return the answer satisfying the <var>constraint</var>
   * @throws QueryException if the <var>constraint</var> has a model of an
   *     unsupported type, or if the answer otherwise couldn't be determined
   * @throws TuplesException If there was a problem while moving around in results
   *     from various query resolutions.
   */
  public static Tuples walk(QueryEvaluationContext query, WalkConstraint predConstraint,
      GraphExpression graphExpression, ResolverSession session)
      throws QueryException, TuplesException {

    if (logger.isDebugEnabled()) {
      logger.debug("Executing walk constraint");
    }

    if (!(predConstraint.getAnchoredConstraint().getElement(1) instanceof Value)) {
      throw new QueryException("Predicate in walk function, " +
          predConstraint.getAnchoredConstraint().getElement(1) +
          ", currently must be bound to a value.");
    }

    // Either the subject is a variable and the object is a constant or
    // vice-versa in the first constraint
    if (((predConstraint.getAnchoredConstraint().getElement(0) instanceof Variable) &&
        !(predConstraint.getAnchoredConstraint().getElement(2) instanceof Value)) ||
        ((predConstraint.getAnchoredConstraint().getElement(0) instanceof Value) &&
        !(predConstraint.getAnchoredConstraint().getElement(2) instanceof Variable))) {
      throw new QueryException(
          "The subject: " + predConstraint.getAnchoredConstraint().getElement(0) +
          " and the object: " + predConstraint.getAnchoredConstraint().getElement(2) +
          " are invalid, one must be a variable and the other a fixed value" +
          " around a predicate.");
    }

    // Transitive constraint must be a fixed predicate
    if (!(predConstraint.getUnanchoredConstraint().getElement(1) instanceof Value)) {
      throw new QueryException("Predicate in transitive function, " +
          predConstraint.getElement(1) + ", currently must be bound to a value.");
    }

    // The subject and object must be variables.
    if (!((predConstraint.getUnanchoredConstraint().getElement(0) instanceof Variable) &&
        (predConstraint.getUnanchoredConstraint().getElement(2) instanceof Variable))) {
      throw new QueryException(
          "The subject: " + predConstraint.getUnanchoredConstraint().getElement(0) +
          " and the object: " + predConstraint.getUnanchoredConstraint().getElement(2) +
          " are invalid, both must be variables.");
    }

    Tuples initialTuples = null;
    Tuples predTuples = null;

    boolean success = false;
    try {

      // ask for all statements for this predicate
      initialTuples = query.resolve(graphExpression, predConstraint.getAnchoredConstraint());

      // prepare to iterate through anchor
      initialTuples.beforeFirst();
      boolean tuplesAvailable = initialTuples.next();

      // if nothing matches, then there is no work to do
      if (!tuplesAvailable) {
        return initialTuples;
      }

      // The value and variable in the anchored constraint
      Long value;
      Variable anchoredConstraintVariable;
      Variable tmpVariable;

      // construct a constraint without the fixed anchor
      Constraint openConstraint;
      Constraint anchoredConstraint = predConstraint.getAnchoredConstraint();

      LiteralTuples inferredResult;

      if ( logger.isInfoEnabled() ) {
        logger.info("Anchored constraint = " + anchoredConstraint);
      }
      // determine if this is a forward anchor or a back anchor
      try {
        if (anchoredConstraint.getElement(0) instanceof Variable) {
          // back anchor  [$x predicate anchor]
          ConstraintElement element = anchoredConstraint.getElement(2);
          if (element instanceof LocalNode) {
            value = new Long(((LocalNode)element).getValue());
          } else {
            value = new Long(session.lookup((Node)element));
          }
          anchoredConstraintVariable = (Variable) anchoredConstraint.getElement(0);

          // Create a temporary second variable by appending _ to the first variable name
          tmpVariable = new Variable(anchoredConstraintVariable.getName() + "_");

          // build the constraint with the temporary variable name
          openConstraint = new ConstraintImpl(anchoredConstraintVariable,
              anchoredConstraint.getElement(1), tmpVariable,
              anchoredConstraint.getModel());
          // set up the final result built from the second constraint
          inferredResult = new LiteralTuples(new Variable[] {
              (Variable) predConstraint.getUnanchoredConstraint().getElement(2),
              (Variable) predConstraint.getUnanchoredConstraint().getElement(0)
          });
        } else {
          // forward anchor  [anchor predicate $x]
          ConstraintElement element = anchoredConstraint.getElement(0);
          if (element instanceof LocalNode) {
            value = new Long(((LocalNode)element).getValue());
          } else {
            value = new Long(session.lookup((Node)element));
          }
          anchoredConstraintVariable = (Variable) anchoredConstraint.getElement(2);

          // Create a temporary second variable by appending _ to the first variable name
          tmpVariable = new Variable(anchoredConstraintVariable.getName() + "_");

          // build the constraint with the temporary variable name
          openConstraint = new ConstraintImpl(tmpVariable,
              anchoredConstraint.getElement(1), anchoredConstraintVariable,
              anchoredConstraint.getModel());

          // set up the final result built from the second constraint
          inferredResult = new LiteralTuples(new Variable[] {
              (Variable) predConstraint.getUnanchoredConstraint().getElement(0),
              (Variable) predConstraint.getUnanchoredConstraint().getElement(2)
          });
        }
      } catch (LocalizeException el) {
        throw new QueryException("Failed to localize walk component", el);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Variable has name: " +
            anchoredConstraintVariable.getName() + "; Value=" + value);
      }

      // find *all* statements with the given predicate
      predTuples = query.resolve(graphExpression, openConstraint);

      // remember that the current value has been visited
      Set<Long> visited = new HashSet<Long>();
      visited.add(value);

      // Get object/subject node value
      long subject = value.longValue();

      // set up given statement for inferences
      LiteralTuples given = new LiteralTuples(new Variable[] {tmpVariable});
      given.appendTuple(new long[] { subject });

      // start inferring for the current subject, using this first set of objects
      // modifies the inferredResult value for us.
      walkStatements(predTuples, given, visited, inferredResult);

      if (logger.isDebugEnabled()) {
        logger.debug("Finished all inferencing");
      }

      success = true;
      return inferredResult;
    } finally {
      // clean up tuples
      try {
        try {
          if (initialTuples != null) initialTuples.close();
        } finally {
          if (predTuples != null) predTuples.close();
        }
      } catch (TuplesException e) {
        if (success) throw e; // Everything worked up to this point, re-throw this one.
        else logger.info("Suppressing exception closing failed tuples", e); // Log and ignore redundant exception.
      }
    }
  }

  /**
   * This traverses statements from the <code>given</code> tuples which
   * consists of a single subject node.  It then returns existing tuples
   * into the inferredResult via iteration.
   *
   * @param initialTuples All existing statements in the model which have the
   *   required predicate.
   * @param given The current base of the chain, as single named column of the
   *   starting subject.  Closed by this method.
   * @param visited A set of all nodes which have been inferred from for
   *   subject in the given clause. This is to prevent loops.
   * @param inferredResult An in/out parameter which holds all the inferred
   *   statements so far.  This method will append to it.
   * @throws TuplesException If there was a problem while moving around in
   *   results of the query resolutions.
   * @throws QueryException If there was a problem while moving around in
   *   results of the query resolutions.
   * @throws StatementStoreException If there was a problem while moving
   *   around in results of the query resolutions.
   */
  public static void walkStatements(Tuples initialTuples,
      Tuples given, Set<Long> visited, LiteralTuples inferredResult)
      throws TuplesException, QueryException {

    assert initialTuples.getNumberOfVariables() == 2;
    assert given.getNumberOfVariables() == 1;
    assert inferredResult.getNumberOfVariables() == 2;

    // get out the column variable being used by given
    Variable givenColumn = given.getVariables()[0];

    // Print out debugging information of given and initial tuples.
    if (logger.isDebugEnabled()) {

      logger.debug("Given is on column: " + givenColumn.getName());
      initialTuples.beforeFirst();
      initialTuples.next();
      do {

        logger.debug("Initial tuples: " + initialTuples.getColumnValue(0) +
            "," + initialTuples.getColumnValue(1));
      } while (initialTuples.next());
      initialTuples.beforeFirst();

      Variable[] vars = initialTuples.getVariables();
      String varnames = "";
      for (int v = 0; v < vars.length; v++) {
        varnames += " " + vars[v];
      }
      logger.debug("InitialTuples has columns: " + varnames);
    }

    LiteralTuples newGiven = null;

    // Each iteration of this loop steps forward in the inference chain
    do {

      // join the current "given" on all the predicates
      Tuples joinResult = TuplesOperations.join(given, initialTuples);

      // Print out given debugging information.
      if (logger.isDebugEnabled()) {
        given.beforeFirst();
        String gs = "";
        while (given.next()) gs += " " + given.getColumnValue(0);
        logger.debug("Joined with a given of: " + gs);
      }

      boolean success = false;
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
        newGiven = new LiteralTuples(new Variable[] {
            givenColumn
        });

        // Iterate through all the results, we are already on the first line
        do {

          // get the object
          long object = joinResult.getColumnValue(tuplesIndex[1]);
          Long objectL = new Long(object);

          if (logger.isDebugEnabled()) {
            long s = joinResult.getColumnValue(tuplesIndex[0]);
            logger.debug("** found subject=" + s + "; object=" + object);
          }

          // Add existing s, o or o, s from initial tuples.
          inferredResult.appendTuple(new long[] {
              joinResult.getColumnValue(tuplesIndex[0]), object
          });

          // nothing more to do with this object if we have already seen it
          if (visited.contains(objectL)) {
            continue;
          }

          // add object to the new given
          newGiven.appendTuple(new long[] {
              object
          });

          // tell the visited set that we have been here
          visited.add(objectL);
        }
        while (joinResult.next());
        success = true;
      }
      finally {
        // clean up the tuples objects, either by falling through, or from the break above
        try {
          try {
            if (joinResult != null) joinResult.close();
          } finally {
            if (given != null) given.close();
          }
        } catch (TuplesException e) {
          if (success) throw e; // Everything worked up to this point, re-throw this one.
          else logger.info("Suppressing exception closing failed tuples", e); // Log and ignore redundant exception.
        }
      }

      // use this new given, and iterate
      given = newGiven;
    }
    while (newGiven.getRowCardinality() != Cursor.ZERO);

    // close the empty tuples if it exists
    if (newGiven != null) {
      newGiven.close();
    }
  }
}
