/*
 * Copyright 2008 Fedora Commons, Inc.
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

package org.mulgara.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintDifference;
import org.mulgara.query.ConstraintDisjunction;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintFilter;
import org.mulgara.query.ConstraintHaving;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.ConstraintIn;
import org.mulgara.query.ConstraintIs;
import org.mulgara.query.ConstraintNotOccurs;
import org.mulgara.query.ConstraintOccurs;
import org.mulgara.query.ConstraintOccursLessThan;
import org.mulgara.query.ConstraintOccursMoreThan;
import org.mulgara.query.ConstraintOperation;
import org.mulgara.query.ConstraintOptionalJoin;
import org.mulgara.query.QueryException;
import org.mulgara.query.SingleTransitiveConstraint;
import org.mulgara.query.TransitiveConstraint;
import org.mulgara.query.WalkConstraint;

/**
 * Identity transformation on constraint expressions.
 *
 * @created July 1, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public abstract class IdentityTransformer {

  /** Maps constraint types to constructors */
  protected Map<Class<? extends Constraint>, ConstraintTypeCons<? extends Constraint>> consMap = new HashMap<Class<? extends Constraint>, ConstraintTypeCons<? extends Constraint>>();

  /**
   * Builds a transformer, with identity constructors.
   */
  public IdentityTransformer() {
    initialize(new ConsImpl(), new ConsIs(),
         new ConsNotOccurs(), new ConsOccurs(), new ConsOccursLessThan(),
         new ConsOccursMoreThan(), new ConsSingleTransitive(),
         new ConsTransitive(), new ConsWalk());
  }

  /**
   * Builds a transformer, with given constructors.
   * @param constructors The constructors to use.
   */
  public void initialize(ConstraintTypeCons<?>... constructors) {
    for (ConstraintTypeCons<?> c: constructors) insert(c);
  }

  /**
   * Gets a constructor for a give constraint type.
   * @param c A constraint of the desired type.
   * @return A constructor for the given constraint.
   */
  public ConstraintTypeCons<? extends Constraint> getCons(Constraint c) {
    return consMap.get(c.getClass());
  }

  /**
   * Maps a constraint type to its constructor. This will override existing mappings.
   * @param cons The constraint constructor.
   */
  protected void insert(ConstraintTypeCons<? extends Constraint> cons) {
    consMap.put(cons.getType(), cons);
  }

  /**
   * Transforms a {@link ConstraintExpression}.
   * @param expr The expression to transform.
   * @return The transformed expression.
   * @throws SymbolicTransformationException An error occurred in the transformation. 
   */
  public ConstraintExpression transform(ConstraintExpression expr) throws SymbolicTransformationException {
    // explicitly handle all the recursive types
    if (expr instanceof ConstraintFilter) return transformFilter(expr);
    if (expr instanceof ConstraintIn) return transformIn(expr);
    if (expr instanceof ConstraintOperation) return transformOperation(expr);
    // do the actual work of this transformer
    if (expr instanceof Constraint) return transformConstraint(expr);
    // By default we do not recognise the constraint type, so pass it unchanged.
    return expr;
  }

  /**
   * Transforms a {@link ConstraintFilter}.
   * @param expr The ConstraintFilter to transform.
   * @return The transformed ConstraintFilter.
   * @throws SymbolicTransformationException An error occurred in the transformation.
   */
  ConstraintFilter transformFilter(ConstraintExpression expr) throws SymbolicTransformationException {
    ConstraintFilter filter = (ConstraintFilter)expr;
    ConstraintExpression inner = filter.getUnfilteredConstraint();
    ConstraintExpression tx = transform(inner);
    return (tx == inner) ? filter : new ConstraintFilter(tx, filter.getFilter());
  }

  /**
   * Transforms a {@link ConstraintIn}.
   * @param expr The ConstraintIn to transform.
   * @return The transformed ConstraintIn.
   * @throws SymbolicTransformationException An error occurred in the transformation.
   */
  ConstraintIn transformIn(ConstraintExpression expr) throws SymbolicTransformationException {
    ConstraintIn in = (ConstraintIn)expr;
    ConstraintExpression inner = in.getConstraintParam();
    ConstraintExpression tx = transform(inner);
    return (tx == inner) ? in : new ConstraintIn(tx, in.getGraph());
  }

  /**
   * Transforms a {@link ConstraintOperation}.
   * @param expr The ConstraintOperation to transform.
   * @return The transformed ConstraintOperation.
   * @throws SymbolicTransformationException An error occurred in the transformation.
   */
  ConstraintOperation transformOperation(ConstraintExpression expr) throws SymbolicTransformationException {
    ConstraintOperation operation = (ConstraintOperation)expr;
    List<ConstraintExpression> ops = operation.getElements();
    List<ConstraintExpression> newOps = new ArrayList<ConstraintExpression>(ops.size());
    boolean changed = false;
    for (ConstraintExpression op: ops) {
      ConstraintExpression tx = transform(op);
      newOps.add(tx);
      if (tx != op) changed = true;
    }
    if (changed) {
      OpType operationType = OpType.getType(operation);
      if (operationType == null) throw new SymbolicTransformationException("Encountered an unknown operation type: " + operation.getClass());
      return operationType.newOp(newOps);
    }
    return operation; 
  }

  /**
   * Transforms a {@link Constraint}. This method is usually replaced to modify instances
   * of {@link ConstraintImpl}.
   * @param expr The Constraint to transform.
   * @return The transformed Constraint.
   * @throws SymbolicTransformationException An error occurred in the transformation.
   */
  Constraint transformConstraint(ConstraintExpression expr) throws SymbolicTransformationException {
    Constraint cnstr = (Constraint)expr;
    try {
      return getCons(cnstr).newConstraint(cnstr);
    } catch (NullPointerException e) {
      throw new SymbolicTransformationException("Unable to transform unknown Constraint type: " + cnstr);
    }
  }

  /**
   * This enum enumerates the ConstraintOperation types. It has been built to deal with
   * the fact that constructors for the various types cannot be passed as a lambda.
   * It also provides a map for the enumerated types to their enumerations, making it
   * easy for an operation to get to an appropriate constructor.
   */
  private static enum OpType {
    difference {
      ConstraintOperation newOp(List<ConstraintExpression> ops) { return new ConstraintDifference(ops.get(0), ops.get(1)); }
    },
    optional {
      ConstraintOperation newOp(List<ConstraintExpression> ops) { return new ConstraintOptionalJoin(ops.get(0), ops.get(1)); }
    },
    conjunction {
      ConstraintOperation newOp(List<ConstraintExpression> ops) { return new ConstraintConjunction(ops); }
    },
    disjunction {
      ConstraintOperation newOp(List<ConstraintExpression> ops) { return new ConstraintDisjunction(ops); }
    };
    abstract ConstraintOperation newOp(List<ConstraintExpression> ops);
    private static Map<Class<? extends ConstraintOperation>, OpType> opMap = new HashMap<Class<? extends ConstraintOperation>, OpType>();
    public static OpType getType(ConstraintOperation op) { return opMap.get(op.getClass()); }
    static {
      opMap.put(ConstraintDifference.class, difference);
      opMap.put(ConstraintOptionalJoin.class, optional);
      opMap.put(ConstraintConjunction.class, conjunction);
      opMap.put(ConstraintDisjunction.class, disjunction);
    }
  }

  /**
   * Interface to describe a constructor along with the type the constructor applies to.
   */
  protected interface ConstraintTypeCons<T extends Constraint> {
    /**
     * Method to construct a new constraint of the expected type.
     * @param c The old version of the constraint.
     * @return A new Constraint with the same type as <var>c</var>.
     * @throws SymbolicTransformationException There was an error in the data structure.
     */
    abstract T newConstraint(Constraint c) throws SymbolicTransformationException;
    /** @return The class handled by this type. */
    abstract Class<T> getType();
  }

  ///////////////////////////////////////////////////////////////////////
  // The following are classes for constructing each of the various types
  ///////////////////////////////////////////////////////////////////////

  protected abstract class ConsHaving<T extends ConstraintHaving> implements ConstraintTypeCons<T> {
    public T newConstraint(Constraint c) {
      ConstraintHaving h = (ConstraintHaving)c;
      ConstraintElement[] ops = new ConstraintElement[4];
      for (int i = 0; i < ops.length; i++) ops[i] = h.getElement(i);
      return newHaving(ops);
    }
    protected abstract T newHaving(ConstraintElement[] ops);
  }

  protected class ConsNotOccurs extends ConsHaving<ConstraintNotOccurs> {
    public ConstraintNotOccurs newHaving(ConstraintElement[] ops) { return new ConstraintNotOccurs(ops[0], ops[2], ops[3]); }
    public Class<ConstraintNotOccurs> getType() { return ConstraintNotOccurs.class; }
  }

  protected class ConsOccurs extends ConsHaving<ConstraintOccurs> {
    public ConstraintOccurs newHaving(ConstraintElement[] ops) { return new ConstraintOccurs(ops[0], ops[2], ops[3]); }
    public Class<ConstraintOccurs> getType() { return ConstraintOccurs.class; }
  }

  protected class ConsOccursLessThan extends ConsHaving<ConstraintOccursLessThan> {
    public ConstraintOccursLessThan newHaving(ConstraintElement[] ops) { return new ConstraintOccursLessThan(ops[0], ops[2], ops[3]); }
    public Class<ConstraintOccursLessThan> getType() { return ConstraintOccursLessThan.class; }
  }

  protected class ConsOccursMoreThan extends ConsHaving<ConstraintOccursMoreThan> {
    public ConstraintOccursMoreThan newHaving(ConstraintElement[] ops) { return new ConstraintOccursMoreThan(ops[0], ops[2], ops[3]); }
    public Class<ConstraintOccursMoreThan> getType() { return ConstraintOccursMoreThan.class; }
  }

  protected class ConsImpl implements ConstraintTypeCons<ConstraintImpl> {
    public ConstraintImpl newConstraint(Constraint c) {
      return new ConstraintImpl(c.getElement(0), c.getElement(1), c.getElement(2), c.getElement(3));
    }
    public Class<ConstraintImpl> getType() { return ConstraintImpl.class; }
  }

  protected class ConsIs implements ConstraintTypeCons<ConstraintIs> {
    public ConstraintIs newConstraint(Constraint c) {
      return new ConstraintIs(c.getElement(0), c.getElement(2), c.getElement(3));
    }
    public Class<ConstraintIs> getType() { return ConstraintIs.class; }
  }

  protected class ConsSingleTransitive implements ConstraintTypeCons<SingleTransitiveConstraint> {
    public SingleTransitiveConstraint newConstraint(Constraint c) throws SymbolicTransformationException {
      SingleTransitiveConstraint s = (SingleTransitiveConstraint)c;
      return new SingleTransitiveConstraint(transformConstraint(s.getTransConstraint()));
    }
    public Class<SingleTransitiveConstraint> getType() { return SingleTransitiveConstraint.class; }
  }

  protected class ConsTransitive implements ConstraintTypeCons<TransitiveConstraint> {
    public TransitiveConstraint newConstraint(Constraint c) throws SymbolicTransformationException {
      TransitiveConstraint t = (TransitiveConstraint)c;
      return new TransitiveConstraint(transformConstraint(t.getAnchoredConstraint()), transformConstraint(t.getUnanchoredConstraint()));
    }
    public Class<TransitiveConstraint> getType() { return TransitiveConstraint.class; }
  }

  protected class ConsWalk implements ConstraintTypeCons<WalkConstraint> {
    public WalkConstraint newConstraint(Constraint c) throws SymbolicTransformationException {
      WalkConstraint t = (WalkConstraint)c;
      try {
        return new WalkConstraint(transformConstraint(t.getAnchoredConstraint()), transformConstraint(t.getUnanchoredConstraint()));
      } catch (QueryException e) {
        throw new SymbolicTransformationException("Invalid Walk constraints being transformed", e);
      }
    }
    public Class<WalkConstraint> getType() { return WalkConstraint.class; }
  }

}
