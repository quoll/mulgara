/*
 * Copyright 2009 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.resolver.spi;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintDisjunction;
import org.mulgara.query.ConstraintFilter;

/**
 * A transformer that works on the basis of expanding a product of sums into a sum of products.
 * This is needed because our disjunctions (sums) are very inefficient, particularly for searching.
 * Performs the following transforms:
 * A and (B or C) -> (A and B) or (A and C)
 * FILTER(A or B, F) -> FILTER(A, F) or FILTER(B, F)
 * TODO:
 * A and ((B or C) - D) -> (A and (B - D)) or (A and (C - D))
 * A - (B or C) -> (A - B) - C, iff B and C share all variables
 *
 * @created August 7, 2009
 * @author Paula Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class SumOfProductExpansionTransformer extends AbstractSymbolicTransformer {
  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(SumOfProductExpansionTransformer.class.getName());


  @Override
  public ConstraintExpression transformExpression(SymbolicTransformationContext context, ConstraintExpression expr) throws SymbolicTransformationException {
    // This is the main case.
    if (expr instanceof ConstraintConjunction) {
      return transformConjunction(context, (ConstraintConjunction)expr);
    }

    // all else go through the default handling
    return super.transformExpression(context, expr);
  }

  
  protected ConstraintExpression transformConjunction(SymbolicTransformationContext context, ConstraintConjunction expr) throws SymbolicTransformationException {
    List<ConstraintExpression> args = expr.getElements();
    for (int i = 0; i < args.size(); i++) {
      ConstraintExpression arg = args.get(i);

      // depth first
      ConstraintExpression tx = transformExpression(context, arg);
      if (tx != arg) {
        // there was a change, so reset and start again
        List<ConstraintExpression> newArgs = new ArrayList<ConstraintExpression>(args);
        newArgs.set(i, tx);
        return new ConstraintConjunction(newArgs);
      }

      // test for expansion
      if (arg instanceof ConstraintDisjunction) {
        return expandConstraint((ConstraintDisjunction)arg, args, i);
      }

      // test filtered constraints for expansion
      if (arg instanceof ConstraintFilter) {
        ConstraintFilter filtered = (ConstraintFilter)arg;
        ConstraintExpression innerArg = filtered.getUnfilteredConstraint();
        if (innerArg instanceof ConstraintDisjunction) {
          return new ConstraintFilter(expandConstraint((ConstraintDisjunction)innerArg, args, i), filtered.getFilter());
        }
      }
    }
    // no expandable terms found
    return expr;
  }


  /**
   * Creates a new ConstraintDisjunction of conjunctions with one fewer disjunctive term.
   * @param arg The disjunction to be expanded into the outer conjunction.
   * @param outerArgs The arguments of the parent expression to be distributed into.
   * @param argOffset The position of the argument to be replaced in the outerArgs
   * @return
   */
  private ConstraintDisjunction expandConstraint(ConstraintDisjunction arg, List<ConstraintExpression> outerArgs, int argOffset) {
    // need to expand by duplicating the conjunction for each element in the disjunction
    List<ConstraintExpression> disjArgs = arg.getElements();
    // accumulate the arguments to the new parent disjunction
    List<ConstraintExpression> expandedDisjArgs = new ArrayList<ConstraintExpression>();
    for (ConstraintExpression disjArg: disjArgs) {
      // copy the original arguments
      List<ConstraintExpression> conjArgs = new ArrayList<ConstraintExpression>(outerArgs);
      // replace the disjunction element with one of the arguments from the disjunction
      conjArgs.set(argOffset, disjArg);
      // create a new conjunction with these modified arguments
      expandedDisjArgs.add(new ConstraintConjunction(conjArgs));
    }
    // check that the distibution is the same size
    assert expandedDisjArgs.size() == disjArgs.size();
    return new ConstraintDisjunction(expandedDisjArgs);
  }

  @Override
  protected ConstraintExpression transformConstraint(SymbolicTransformationContext context, Constraint c)
      throws SymbolicTransformationException {
    return c;
  }

}
