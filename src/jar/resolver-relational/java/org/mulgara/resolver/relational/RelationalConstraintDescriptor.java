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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.relational;

import java.util.Map;

// import org.apache.log4j.Logger;

import org.apache.log4j.Logger;
import org.mulgara.resolver.spi.ConstraintBindingHandler;
import org.mulgara.resolver.spi.ConstraintDescriptor;
import org.mulgara.resolver.spi.ConstraintLocalization;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.Constraint;
import org.mulgara.query.Value;
import org.mulgara.query.Variable;
import org.mulgara.store.tuples.Tuples;

// FIXME: Need to work out how to delegate this.
import org.mulgara.resolver.ConstraintOperations;

public class RelationalConstraintDescriptor implements ConstraintDescriptor, ConstraintLocalization, ConstraintBindingHandler {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(RelationalConstraintDescriptor.class.getName());

  public Class<RelationalConstraint> getConstraintClass() {
    return RelationalConstraint.class;
  }


  public Tuples resolve(QueryEvaluationContext queryContext,
                        GraphExpression modelExpr,
                        ConstraintExpression constraintExpr) throws Exception {
    assert constraintExpr instanceof Constraint;
    return ConstraintOperations.resolveModelExpression(queryContext, modelExpr, (Constraint)constraintExpr);
  }

  public Constraint rewrite(ConstraintElement newModel, Constraint constraint) throws Exception {
    if (!(constraint instanceof RelationalConstraint)) {
      throw new IllegalArgumentException("RelationalConstraintDescriptor.rewrite must be a RelationalConstraint");
    }

    // Note: mutation here might be a problem.  It shouldn't be, but if it is
    // then we need to do a clone/update/return here instead.
    ((RelationalConstraint)constraint).rewriteModel(newModel);
    return constraint;
  }

  public Constraint localize(QueryEvaluationContext context, Constraint constraint) throws Exception {
    assert constraint instanceof RelationalConstraint;
    return RelationalConstraint.localize(context, (RelationalConstraint)constraint);
  }

  public ConstraintExpression bindVariables(Map<Variable,Value> bindings,
      ConstraintExpression constraintExpr) throws Exception {
    assert constraintExpr instanceof RelationalConstraint;
    return RelationalConstraint.bind(bindings, (RelationalConstraint)constraintExpr);
  }
}
