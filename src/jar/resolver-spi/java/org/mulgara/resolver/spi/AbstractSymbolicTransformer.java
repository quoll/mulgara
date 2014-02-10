/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.resolver.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintDifference;
import org.mulgara.query.ConstraintDisjunction;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintFilter;
import org.mulgara.query.ConstraintIn;
import org.mulgara.query.ConstraintOperation;
import org.mulgara.query.ConstraintOptionalJoin;

/**
 * This provides some common processing for symbolic-transformers.
 *
 * @created Dec 2, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractSymbolicTransformer implements SymbolicTransformation {
  private static final Logger logger = Logger.getLogger(AbstractSymbolicTransformer.class);

  public void transform(SymbolicTransformationContext context, MutableLocalQuery mutableLocalQuery)
        throws SymbolicTransformationException {
    if (logger.isTraceEnabled()) logger.trace("Transforming query: " + mutableLocalQuery.getConstraintExpression());

    ConstraintExpression expr = mutableLocalQuery.getConstraintExpression();
    ConstraintExpression trans = transformExpression(context, expr);

    if (logger.isTraceEnabled()) logger.trace("Transform result: " + (expr != trans ? trans : "-no-change-"));

    if (expr != trans) {
      mutableLocalQuery.setConstraintExpression(trans);
    }
  }

  /**
   * Transform the given constraint-expression. This is a dispatcher, invoking one of the other
   * transformXYZ methods as applicable or returning the given expr if none are.
   *
   * @param context the current transformation context
   * @param expr the constraint expression to transform
   * @return a new expression is something was changed, or <var>expr</var> if nothing was changed.
   * @throws SymbolicTransformationException If there is an error applying the transform
   */
  public ConstraintExpression transformExpression(SymbolicTransformationContext context,
                                                  ConstraintExpression expr)
        throws SymbolicTransformationException {
    // explicitly handle all the recursive types
    if (expr instanceof ConstraintFilter) return transformFilter(context, (ConstraintFilter)expr);
    if (expr instanceof ConstraintIn) return transformIn(context, (ConstraintIn)expr);
    if (expr instanceof ConstraintOperation) return transformOperation(context, (ConstraintOperation)expr);
    // do the actual work of this transformer
    if (expr instanceof Constraint) return transformConstraint(context, (Constraint)expr);
    // By default we do not recognise the constraint type, so pass it unchanged.
    return expr;
  }

  /**
   * Transform the filtered constraint. This invokes {@link #transformExpression} on the inner constraint.
   *
   * @param context the current transformation context
   * @param filter the constraint filter to transform
   * @return a new expression is something was changed, or <var>filter</var> if nothing was changed.
   * @throws SymbolicTransformationException If there is an error applying the transform
   */
  protected ConstraintExpression transformFilter(SymbolicTransformationContext context,
                                                 ConstraintFilter filter)
        throws SymbolicTransformationException {
    ConstraintExpression inner = filter.getUnfilteredConstraint();
    ConstraintExpression tx = transformExpression(context, inner);
    return (tx == inner) ? filter : new ConstraintFilter(tx, filter.getFilter());
  }

  /**
   * Transform the in constraint. This invokes {@link #transformExpression} on the inner constraint.
   *
   * @param context the current transformation context
   * @param in the in-constraint to transform
   * @return a new expression is something was changed, or <var>in</var> if nothing was changed.
   * @throws SymbolicTransformationException If there is an error applying the transform
   */
  protected ConstraintExpression transformIn(SymbolicTransformationContext context, ConstraintIn in)
        throws SymbolicTransformationException {
    ConstraintExpression inner = in.getConstraintParam();
    ConstraintExpression tx = transformExpression(context, inner);
    return (tx == inner) ? in : new ConstraintIn(tx, in.getGraph());
  }

  /**
   * Transform the constraint-operation. This invokes {@link #transformExpression} on all the inner
   * constraints.
   *
   * @param context the current transformation context
   * @param oper the constraint-operation to transform
   * @return a new expression is something was changed, or <var>oper</var> if nothing was changed.
   * @throws SymbolicTransformationException If there is an error applying the transform
   */
  protected ConstraintExpression transformOperation(SymbolicTransformationContext context,
                                                    ConstraintOperation oper)
        throws SymbolicTransformationException {
    List<ConstraintExpression> ops = oper.getElements();
    List<ConstraintExpression> newOps = new ArrayList<ConstraintExpression>(ops.size());
    boolean changed = false;

    for (ConstraintExpression op: ops) {
      ConstraintExpression tx = transformExpression(context, op);
      newOps.add(tx);
      if (tx != op) changed = true;
    }

    if (changed) {
      OpType operationType = OpType.getType(oper);
      if (operationType == null) throw new SymbolicTransformationException("Encountered an unknown operation type: " + oper.getClass());
      return operationType.newOp(newOps);
    }

    return oper;
  }

  /**
   * Transform the given expression. The main work of this class is usually performed in this
   * method.
   *
   * @param context the current transformation context
   * @param c the constraint to transform.
   * @return the original constraint, or a new constraint if something was changed.
   * @throws SymbolicTransformationException If there is an error applying the transform
   */
  protected abstract ConstraintExpression transformConstraint(SymbolicTransformationContext context,
                                                              Constraint c)
      throws SymbolicTransformationException;

  /**
   * This enum enumerates the ConstraintOperation types. It has been built to deal with
   * the fact that constructors for the various types cannot be passed as a lambda.
   * It also provides a map for the enumerated types to their enumerations, making it
   * easy for an operation to get to an appropriate constructor.
   */
  protected static enum OpType {
    difference {
      public ConstraintOperation newOp(List<ConstraintExpression> ops) { return new ConstraintDifference(ops.get(0), ops.get(1)); }
    },
    optional {
      public ConstraintOperation newOp(List<ConstraintExpression> ops) { return new ConstraintOptionalJoin(ops.get(0), ops.get(1)); }
    },
    conjunction {
      public ConstraintOperation newOp(List<ConstraintExpression> ops) { return new ConstraintConjunction(ops); }
    },
    disjunction {
      public ConstraintOperation newOp(List<ConstraintExpression> ops) { return new ConstraintDisjunction(ops); }
    };

    public abstract ConstraintOperation newOp(List<ConstraintExpression> ops);

    private static Map<Class<? extends ConstraintOperation>, OpType> opMap = new HashMap<Class<? extends ConstraintOperation>, OpType>();

    public static OpType getType(ConstraintOperation op) { return opMap.get(op.getClass()); }

    static {
      opMap.put(ConstraintDifference.class, difference);
      opMap.put(ConstraintOptionalJoin.class, optional);
      opMap.put(ConstraintConjunction.class, conjunction);
      opMap.put(ConstraintDisjunction.class, disjunction);
    }
  }
}
