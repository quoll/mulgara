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

/**
 * This is an implementation of the trans function that begins or ends
 * (anchored) from a particular subject or object and only infers direct
 * relations to the given anchored constraint.
 * <p>
 * Example to infer only statements directly: <p></p>
 * <pre>
 *   select &lt;b&gt; &lt;rdfs:subClassOf&gt; $zzz
 *   where trans(&lt;b&gt; &lt;rdfs:subClassOf&gt; $zzz) ;
 *
 *   select $xxx &lt;rdfs:subClassOf&gt; &lt;b&gt;
 *   where trans($xxx &lt;rdfs:subClassOf&gt; &lt;b&gt;) ;
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
public abstract class DirectTransitiveFunction extends TransitiveFunction {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(DirectTransitiveFunction.class.getName());

  /**
   * Resolves an unanchored transitive constraint.  This means finding all statements matching
   * the predicate statement, and infering a new set of statements based on this.
   *
   * @param query the query for a set of statements to infer over.
   * @param constraint a constraint describing a transitive predicate with a
   *      supported model type fourth element of the constraint is not a Variable
   *      or is a Variable with the name _from.
   * @param graphExpression the GraphExpression to resolve the constraint against.
   * @param session the resolverSession against which to localize nodes.
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
      logger.debug("Transitive Constraint is anchored");
    }

    // Transitive constraint must have a fixed predicate
    if (!(constraint.getElement(1) instanceof Value)) {
      throw new QueryException("The predicate: " +  constraint.getElement(1) +
          " must be a fixed value");
    }

    // Either the subject is a variable and the object is a constant or
    // vice-versa.
    if (((constraint.getElement(0) instanceof Variable) &&
        !(constraint.getElement(2) instanceof Value)) ||
        ((constraint.getElement(0) instanceof Value) &&
        !(constraint.getElement(2) instanceof Variable))) {
      throw new QueryException(
          "The subject: " + constraint.getElement(0) + " and the object: " +
          constraint.getElement(2) + " are invalid, one must be a variable " +
          "and the other a fixed value around a predicate.");
    }

    // Get the wrapped constraint.
    Constraint predConstraint = constraint.getTransConstraint();

    // ask for all statements for this predicate
    Tuples initialTuples = query.resolve(graphExpression, predConstraint);

    // prepare to iterate through anchor
    initialTuples.beforeFirst();
    boolean tuplesAvailable = initialTuples.next();

    // if nothing matches, then there is no work to do
    if (!tuplesAvailable) {
      return initialTuples;
    }

    // The value and variable in the anchored constraint
    Long value;
    Variable variable;
    Variable tmpVariable;

    // construct a constraint without the fixed anchor
    Constraint openConstraint;

    // determine if this is a forward anchor or a back anchor
    try {
      if (predConstraint.getElement(0) instanceof Variable) {
        // back anchor  [$x predicate anchor]
        value = new Long(session.lookup((Node)predConstraint.getElement(2)));
        variable = (Variable) predConstraint.getElement(0);

        // Create a temporary second variable by appending _ to the first variable name
        tmpVariable = new Variable(variable.getName() + "_");

        // build the constraint with the temporary variable name
        openConstraint = new ConstraintImpl(
            variable,
            predConstraint.getElement(1),
            tmpVariable,
            predConstraint.getModel());
      } else {
        // forward anchor  [anchor predicate $x]
        value = new Long(session.lookup((Node)predConstraint.getElement(0)));
        variable = (Variable)predConstraint.getElement(2);

        // Create a temporary second variable by appending _ to the first variable name
        tmpVariable = new Variable(variable.getName() + "_");

        // build the constraint with the temporary variable name
        openConstraint = new ConstraintImpl(
            tmpVariable,
            predConstraint.getElement(1),
            variable,
            predConstraint.getElement(3));
      }
    } catch (LocalizeException el) {
      throw new QueryException("Unable to localize anchor", el);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Variable has name: " + variable.getName() + "; Value=" + value);
    }

    // find *all* statements with the given predicate
    Tuples predTuples = query.resolve(graphExpression, openConstraint);

    // set up the final result [$x] built from [$x predicate anchor] or [anchor predicate $x]
    LiteralTuples inferredResult = new LiteralTuples(new Variable[] {variable});

    // set up given statement for inferences
    LiteralTuples given = new LiteralTuples(new Variable[] {tmpVariable});

    // if there is a zero step, then start with the value
    if (constraint.isZeroStep()) inferredResult.appendTuple(new long[] {value});

    // remember that the current value has been visited
    Set<Long> visited = new HashSet<Long>();
    visited.add(value);

    long[] tupleRow = new long[1];

    // iterate over each row of the tuples to set up a given clause for a new query
    // add the objects, and remember that we've been here

    // loop while we have data
    do {
      // add the current object to the given. Take advantage that initTuplesIndex
      // is always 0 for anchored constraints
      long var = initialTuples.getColumnValue(0);
      if (logger.isDebugEnabled()) {
        logger.debug("Found variable: " + var);
      }
      // add the object to the "given" tuples
      tupleRow[0] = var;
      given.appendTuple(tupleRow);
    } while ((tuplesAvailable = initialTuples.next()));

    // Get object/subject node value
    long subject = value.longValue();

    // start inferring for the current subject, using this first set of objects
    // modifies the inferredResult value for us.

    inferTransitiveStatements(null, subject, predTuples, given, visited, inferredResult);

    if (logger.isDebugEnabled()) {
      logger.debug("Finished all inferencing");
    }

    // clean up tuples
    initialTuples.close();
    predTuples.close();

    return inferredResult;
  }
}
