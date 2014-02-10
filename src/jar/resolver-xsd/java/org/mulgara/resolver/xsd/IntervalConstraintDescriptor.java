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
 * The copyright to this file is held by:
 *   The Australian Commonwealth Government
 *   Department of Defense
 * Developed by Netymon Pty Ltd
 * under contract 4500430665
 * contributed to the Mulgara Project under the
 *   Mozilla Public License version 1.1
 * per clause 4.1.3 of the above contract.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.xsd;

// Third party packages
import org.apache.log4j.Logger; // Apache Log4J

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.ConstraintDescriptor;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/**
 * {@link Resolution} from the Java heap for XSD related queries.
 *
 * @created 2005-05-02
 * @author <a href="mailto:raboczi@itee.uq.edu.au">Simon Raboczi</a>
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class IntervalConstraintDescriptor implements ConstraintDescriptor {

  /** Logger */
  private static final Logger logger = Logger.getLogger(IntervalConstraintDescriptor.class.getName());

  /**
   * Sole constructor.
   */
  IntervalConstraintDescriptor() {
    // null implementation
  }

  //
  // Methods implementing ConstraintDescriptor
  //

  public Class<IntervalConstraint> getConstraintClass() {
    return IntervalConstraint.class;
  }

  /**
   * @param constraintExpression  an {@link IntervalConstraint}
   * @throws IllegalArgumentException if <var>constraintExpression</var> isn't
   *   an {@link IntervalConstraint}
   */
  public Tuples resolve(QueryEvaluationContext queryEvaluationContext,
                        GraphExpression        graphExpression,
                        ConstraintExpression   constraintExpression)
    throws Exception {

    // Validate "constraintExpression" parameter
    if (constraintExpression.getClass() != IntervalConstraint.class) {
      throw new IllegalArgumentException("Bad \"constraintExpression\" type: " + constraintExpression + " of class " + constraintExpression.getClass());
    }

    IntervalConstraint intervalConstraint = (IntervalConstraint) constraintExpression;

    // If unbounded in both directions, short-circuit execution
    if (intervalConstraint.isUnconstrained()) {
      if (logger.isDebugEnabled()) logger.debug("Unconstrained interval");
      return TuplesOperations.unconstrained();
    }

    // If empty, short-circuit execution
    if (intervalConstraint.isEmpty()) {
      if (logger.isDebugEnabled()) logger.debug("Empty interval");
      return TuplesOperations.empty();
    }

    ResolverSession resolverSession = queryEvaluationContext.getResolverSession();

    SPObject lowValue = (intervalConstraint.getLowerBound() == null)
      ? resolverSession.getSPObjectFactory().newSPDouble(Double.NEGATIVE_INFINITY)
      : resolverSession.getSPObjectFactory().newSPDouble(intervalConstraint.getLowerBound().getValue());

    SPObject highValue = (intervalConstraint.getUpperBound() == null)
      ? resolverSession.getSPObjectFactory().newSPDouble(Double.POSITIVE_INFINITY)
      : resolverSession.getSPObjectFactory().newSPDouble(intervalConstraint.getUpperBound().getValue());

    boolean inclLowValue = (intervalConstraint.getLowerBound() == null)
      ? true
      : intervalConstraint.getLowerBound().isClosed();

    boolean inclHighValue = (intervalConstraint.getUpperBound() == null)
      ? true
      : intervalConstraint.getUpperBound().isClosed();

    assert lowValue != null;
    assert highValue != null;

    Tuples tuples = resolverSession.findStringPoolRange(lowValue, inclLowValue,
                                                      highValue, inclHighValue);

    Variable variable = intervalConstraint.getVariable();
    tuples.renameVariables(new ConstraintImpl(variable, variable, variable));

    if (logger.isDebugEnabled()) {
      logger.debug("Resolved interval " + (inclLowValue ? "(" : "[") +
                   lowValue + "..." + highValue + (inclHighValue ? ")" : "]") +
                   " to " + tuples);
    }
    return tuples;
  }

  /**
   * @throws Exception always (not implemented)
   */
  public Constraint rewrite(ConstraintElement newModel, Constraint constaint) throws Exception {
    throw new Exception("Not implemented");
  }
}
