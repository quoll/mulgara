/*
 * Copyright 2008 The Topaz Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
 */

package org.mulgara.resolver.lucene;

import java.util.Map;

import org.apache.log4j.Logger;
import org.mulgara.resolver.ConstraintOperations;
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

/**
 * The descriptor for the {@link LuceneConstraint lucene constraint}.
 *
 * @created 2008-09-28
 * @author Ronald Tschalär
 * @licence Apache License v2.0
 */
public class LuceneConstraintDescriptor implements ConstraintDescriptor, ConstraintLocalization, ConstraintBindingHandler {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(LuceneConstraintDescriptor.class);

  public Class<LuceneConstraint> getConstraintClass() {
    return LuceneConstraint.class;
  }


  public Tuples resolve(QueryEvaluationContext queryContext, GraphExpression modelExpr,
                        ConstraintExpression constraintExpr) throws Exception {
    return ConstraintOperations.resolveModelExpression(queryContext, modelExpr, (Constraint)constraintExpr);
  }

  public Constraint rewrite(ConstraintElement newModel, Constraint constraint) throws Exception {
    throw new Exception("Not implemented");
  }

  public Constraint localize(QueryEvaluationContext context, Constraint constraint) throws Exception {
    return LuceneConstraint.localize(context, (LuceneConstraint)constraint);
  }

  public ConstraintExpression bindVariables(Map<Variable,Value> bindings,
                                            ConstraintExpression constraintExpr) throws Exception {
    return LuceneConstraint.bind(bindings, (LuceneConstraint)constraintExpr);
  }
}
