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
import java.util.HashSet;
import java.util.Set;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.LiteralTuples;
import org.mulgara.store.tuples.Tuples;

/**
 * This is an implementation of the a trans function that simply infers all
 * statements of a given predicate.
 * <p>
 * Example: <p></p>
 * <pre>
 *   select $xxx &lt;rdfs:subClassOf&gt; $zzz
 *   where trans($xxx &lt;rdfs:subClassOf&gt; $zzz);
 *
 *   select $xxx &lt;rdfs:subClassOf&gt; $zzz
 *   where trans(&lt;b&gt; &lt;rdfs:subClassOf&gt; $zzz
 *   and $xxx &lt;rdfs:subClassOf&gt; $zzz);
 *
 *   select $xxx &lt;rdfs:subClassOf&gt; $zzz
 *   where trans($xxx &lt;rdfs:subClassOf&gt; &lt;b&gt;
 *   and $xxx &lt;rdfs:subClassOf&gt; $zzz);
 * </pre></p>
 *
 * @created 2004-06-07
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:23 $
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
public abstract class ExhaustiveTransitiveFunction extends TransitiveFunction {

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(
      ExhaustiveTransitiveFunction.class.getName());

  /**
   * Resolves a transitive constraint that is unanchored.  This means finding
   * all statements matching the predicate statement, and infering a new set
   * of statements based on this.
   *
   * @param query the local calling query object.
   * @param constraint a constraint describing a transitive predicate with a
   *      supported model type fourth element of the constraint is not a Variable
   *      or is a Variable with the name _from.
   * @return the answer satisfying the <var>constraint</var>
   * @throws QueryException if the <var>constraint</var> has a model of an
   *      unsupported type, or if the answer otherwise couldn't be determined
   * @throws TuplesException If there was a problem while moving around in results
   *      from various query resolutions.
   */
  public static Tuples infer(QueryEvaluationContext query, SingleTransitiveConstraint constraint,
      GraphExpression graphExpression, ResolverSession session)
      throws QueryException, TuplesException {

    if (logger.isDebugEnabled()) {
      logger.debug("Transitive Constraint is unanchored");
    }

    // Transitive constraint must be a fixed predicate
    if (!(constraint.getElement(1) instanceof Value)) {
      throw new QueryException("Predicate in transitive function, " +
          constraint.getElement(1) + ", currently must be bound to a value.");
    }

    // The subject and object must be variables.
    if (!((constraint.getElement(0) instanceof Variable) &&
        (constraint.getElement(2) instanceof Variable))) {
      throw new QueryException(
          "The subject: " + constraint.getElement(0) + " and the object: " +
          constraint.getElement(2) + " are invalid, both must be variables.");
    }

    Tuples initialTuples = null;
    Tuples workingTuples = null;

    boolean success = false;
    try {

      // Get the wrapped constraint
      Constraint predConstraint = constraint.getTransConstraint();

      // ask for all statements for this predicate
      initialTuples = query.resolve(graphExpression, predConstraint);

      // prepare to iterate through anchor, or set of all predicate statements
      initialTuples.beforeFirst();
      boolean tuplesAvailable = initialTuples.next();

      // if nothing matches, then there is no work to do
      if (!tuplesAvailable) {
        return initialTuples;  // return nothing but the original set
      }

      // Copy initial tuples to be worked on.
      workingTuples = (Tuples) initialTuples.clone();
      workingTuples.beforeFirst();

      // the variables for subject and object
      Variable subjectVariable = (Variable) predConstraint.getElement(0);
      Variable objectVariable = (Variable) predConstraint.getElement(2);

      // not anchored, so need indexes in the correct order
      int subjectIndex = initialTuples.getColumnIndex(subjectVariable);
      int objectIndex = initialTuples.getColumnIndex(objectVariable);

      // set up the final result [$x $y] built from [$x predicate $y]
      //   or [$y $x] when using a back anchor
      LiteralTuples inferredResult = new LiteralTuples(new Variable[] {
          subjectVariable, objectVariable
      });

      if (logger.isDebugEnabled()) {
        logger.debug("Iterating through initial tuples");
        logger.debug("First variable="+subjectVariable.getName()+"; Second variable="+objectVariable.getName());
      }

      // iterate over all subjects
      while (tuplesAvailable) {
        // set up given statement, this will have the same variable object as the predicate constraint
        // so create [$x] from [$x predicate $y]
        LiteralTuples given = new LiteralTuples(new Variable[] {subjectVariable});

        // iterate over each row of the tuples to set up a given clause for a new query
        // add the objects, and remember that we've been here

        // get the subject and object of the statement, these names are inverted for back anchors
        long subject = initialTuples.getColumnValue(subjectIndex);
        long object = initialTuples.getColumnValue(objectIndex);

        Long subjectL = new Long(subject);

        if (logger.isDebugEnabled()) {
          logger.debug("Found tuple: subject="+subject+"; object="+object);
        }

        // if using zero-step transitivity then add the subject-subject row
        if (constraint.isZeroStep()) inferredResult.appendTuple(new long[] {subject, subject});

        // remember that the subject has been visited
        Set<Long> visited = new HashSet<Long>();
        visited.add(subjectL);

        // add the object to the "given" tuples
        long[] tupleRow = new long[] { object };
        given.appendTuple(tupleRow);

        // go forward adding objects until the subject changes

        // loop while we have data
        while ((tuplesAvailable = initialTuples.next())) {
          // check that the subject has not changed, continue with the inferencing if it has
          if (subject != initialTuples.getColumnValue(subjectIndex)) {
            break;
          }
          // add the current object to the given. Take advantage that initTuplesIndex
          // is always 0 for anchored constraints
          object = initialTuples.getColumnValue(objectIndex);
          tupleRow[0] = object;
          given.appendTuple(tupleRow);

          if (logger.isDebugEnabled()) {
            logger.debug("Found subsequent tuple: *** object="+object);
          }
          // if using zero-step transitivity then add the object-object row
          if (constraint.isZeroStep()) inferredResult.appendTuple(new long[] {object, object});
        }

        // start inferring for the current subject, using this first set of objects
        // modifies the inferredResult value for us.
        inferTransitiveStatements(subjectL, subjectL.longValue(),
            workingTuples, given, visited, inferredResult
        );
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Finished all inferencing");
      }

      success = true;
      return inferredResult;
    } finally {
      try {
        try {
          if (initialTuples != null) initialTuples.close();
        } finally {
          if (workingTuples != null) workingTuples.close();
        }
      } catch (TuplesException e) {
        if (success) throw e; // Everything worked up to this point, re-throw this one.
        else logger.info("Suppressing exception closing failed tuples", e); // Log and ignore redundant exception.
      }
    }
  }

  /**
   * Resolves a transitive constraint that is unanchored which has a starting
   * point.  This means starting from a known subject, predicate or predicate,
   * object and infering a new set of statements based on this.
   *
   * @param query the local calling query object.
   * @param constraint a constraint describing a transitive predicate with a
   *      supported model type fourth element of the constraint is not a Variable
   *      or is a Variable with the name _from.
   * @return the answer satisfying the <var>constraint</var>
   * @throws QueryException if the <var>constraint</var> has a model of an
   *      unsupported type, or if the answer otherwise couldn't be determined
   * @throws TuplesException If there was a problem while moving around in results
   *      from various query resolutions.
   */
  public static Tuples infer(QueryEvaluationContext query, TransitiveConstraint constraint,
      GraphExpression graphExpression, ResolverSession session)
      throws QueryException, TuplesException {

    if (logger.isDebugEnabled()) {
      logger.debug("Transitive Constraint is unanchored");
    }

    // Transitive constraint must be a fixed predicate
    if (!(constraint.getAnchoredConstraint().getElement(1) instanceof Value)) {
      throw new QueryException("Predicate in transitive function, " +
          constraint.getAnchoredConstraint().getElement(1) +
          ", currently must be bound to a value.");
    }

    // Either the subject is a variable and the object is a constant or
    // vice-versa in the first constraint
    if (((constraint.getAnchoredConstraint().getElement(0) instanceof Variable) &&
        !(constraint.getAnchoredConstraint().getElement(2) instanceof Value)) ||
        ((constraint.getAnchoredConstraint().getElement(0) instanceof Value) &&
        !(constraint.getAnchoredConstraint().getElement(2) instanceof Variable))) {
      throw new QueryException(
          "The subject: " + constraint.getAnchoredConstraint().getElement(0) +
          " and the object: " + constraint.getAnchoredConstraint().getElement(2) +
          " are invalid, one must be a variable and the other a fixed value" +
          " around a predicate.");
    }

    // Transitive constraint must be a fixed predicate
    if (!(constraint.getUnanchoredConstraint().getElement(1) instanceof Value)) {
      throw new QueryException("Predicate in transitive function, " +
          constraint.getElement(1) + ", currently must be bound to a value.");
    }

    // The subject and object must be variables.
    if (!((constraint.getUnanchoredConstraint().getElement(0) instanceof Variable) &&
        (constraint.getUnanchoredConstraint().getElement(2) instanceof Variable))) {
      throw new QueryException(
          "The subject: " + constraint.getUnanchoredConstraint().getElement(0) +
          " and the object: " + constraint.getUnanchoredConstraint().getElement(2) +
          " are invalid, both must be variables.");
    }

    Tuples initialTuples = null;
    Tuples workingTuples = null;

    boolean success = false;
    try {
      // Get the wrapped constraint
      Constraint anchoredConstraint = constraint.getAnchoredConstraint();
      Constraint unanchoredConstraint = constraint.getUnanchoredConstraint();

      // Create a walk constraint
      WalkConstraint walkConstraint = new WalkConstraint(anchoredConstraint,
          unanchoredConstraint);

      // Walk down the graph getting all the statements for the given predicate
      initialTuples = WalkFunction.walk(query, walkConstraint,
          graphExpression, session);

      // prepare to iterate through anchor, or set of all predicate statements
      initialTuples.beforeFirst();
      boolean tuplesAvailable = initialTuples.next();

      // if nothing matches, then there is no work to do
      if (!tuplesAvailable) {
        return initialTuples; // return nothing but the original set
      }

      // Copy initial tuples to be worked on.
      workingTuples = (Tuples) initialTuples.clone();
      workingTuples.beforeFirst();

      // the variables for subject and object
      Variable subjectVariable = (Variable) unanchoredConstraint.getElement(0);
      Variable objectVariable = (Variable) unanchoredConstraint.getElement(2);

      // not anchored, so need indexes in the correct order
      int subjectIndex = initialTuples.getColumnIndex(subjectVariable);
      int objectIndex = initialTuples.getColumnIndex(objectVariable);

      // set up the final result [$x $y] built from [$x predicate $y]
      //   or [$y $x] when using a back anchor
      LiteralTuples inferredResult = new LiteralTuples(
          new Variable[] { subjectVariable, objectVariable });

      if (logger.isDebugEnabled()) {
        logger.debug("Iterating through initial tuples");
        logger.debug(
            "First variable=" + subjectVariable.getName() + "; Second variable=" +
            objectVariable.getName());
      }

      // iterate over all subjects
      while (tuplesAvailable) {
        // set up given statement, this will have the same variable object as the predicate constraint
        // so create [$x] from [$x predicate $y]
        LiteralTuples given = new LiteralTuples(new Variable[] { subjectVariable });

        // iterate over each row of the tuples to set up a given clause for a new query
        // add the objects, and remember that we've been here

        // get the subject and object of the statement, these names are inverted for back anchors
        long subject = initialTuples.getColumnValue(subjectIndex);
        long object = initialTuples.getColumnValue(objectIndex);

        Long subjectL = new Long(subject);

        if (logger.isDebugEnabled()) {
          logger.debug("Found tuple: subject=" + subject + "; object=" + object);
        }

        // remember that the subject has been visited
        Set<Long> visited = new HashSet<Long>();
        visited.add(subjectL);

        // add the object to the "given" tuples
        long[] tupleRow = new long[] { object };
        given.appendTuple(tupleRow);

        // go forward adding objects until the subject changes

        // loop while we have data
        while ((tuplesAvailable = initialTuples.next())) {
          // check that the subject has not changed, continue with the inferencing if it has
          if (subject != initialTuples.getColumnValue(subjectIndex)) {
            break;
          }

          // add the current object to the given. Take advantage that initTuplesIndex
          // is always 0 for anchored constraints
          object = initialTuples.getColumnValue(objectIndex);
          tupleRow[0] = object;
          given.appendTuple(tupleRow);

          if (logger.isDebugEnabled()) {
            logger.debug("Found subsequent tuple: *** object=" + object);
          }
        }

        // start inferring for the current subject, using this first set of objects
        // modifies the inferredResult value for us.
        inferTransitiveStatements(subjectL, subjectL.longValue(),
            workingTuples, given, visited, inferredResult);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Finished all inferencing");
      }

      success = true;
      return inferredResult;
    } finally {
      try {
        try {
          if (initialTuples != null) initialTuples.close();
        } finally {
          if (workingTuples != null) workingTuples.close();
        }
      } catch (TuplesException e) {
        if (success) throw e; // Everything worked up to this point, re-throw this one.
        else logger.info("Suppressing exception closing failed tuples", e); // Log and ignore redundant exception.
      }
    }
  }
}
