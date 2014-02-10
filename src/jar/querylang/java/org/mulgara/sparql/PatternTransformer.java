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

import org.mulgara.parser.MulgaraParserException;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintDifference;
import org.mulgara.query.ConstraintDisjunction;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintFilter;
import org.mulgara.query.ConstraintOptionalJoin;
import org.mulgara.query.filter.And;
import org.mulgara.query.filter.Filter;
import org.mulgara.query.filter.value.Bool;

/**
 * This object transforms a {@link ConstraintExpression} into a minimized {@link ConstraintExpression}.
 *
 * @created May 06, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class PatternTransformer {

  /**
   * Perform the mapping of the graph pattern and return the results as a {@link ConstraintExpression}.
   * @return The mapped constraint expression.
   */
  static public ConstraintExpression transform(ConstraintExpression constraints) throws MulgaraParserException {
    Transformer<? extends ConstraintExpression> tx = txMap.get(constraints);
    if (tx == null) return constraints;
    return tx.internalTx(constraints);
  }

  /** A mapping of constraint expressions to Transformers. */
  private static Map<Class<? extends ConstraintExpression>,Transformer<? extends ConstraintExpression>> txMap =
      new HashMap<Class<? extends ConstraintExpression>,Transformer<? extends ConstraintExpression>>();

  /**
   * The class for the mapping of {@link ConstraintExpression}s to {@link ConstraintExpression}s.
   */
  private static abstract class Transformer<T extends ConstraintExpression>  {
    /** An entry point for the tx operation. This method handles casting to be compatible with the generic template. */
    @SuppressWarnings("unchecked")
    public ConstraintExpression internalTx(ConstraintExpression constraints) throws MulgaraParserException {
      return tx((T)constraints);
    }
    public abstract ConstraintExpression tx(T constraints) throws MulgaraParserException;
    /** Identify the class to be mapped by the extension. */
    public abstract Class<T> getTxType();
  }

  /**
   * Utility method to add a transformer to the map, keyed on the class it transforms.
   * @param tx The transformer to add to the map.
   */
  static void addToMap(Transformer<?> tx) {
    txMap.put(tx.getTxType(), tx);
  }

  /**
   * Initialize the mapping of patterns to the constraint builders.
   */
  static {
    addToMap(new FilterTx());
    addToMap(new LeftJoinTx());
    addToMap(new ConjunctionTx());
    addToMap(new DisjunctionTx());
    addToMap(new DifferenceTx());
  }


  /**
   * Creates a conjunction of filters, skipping any TRUE values on the way.
   * @param lhs The first filter to join
   * @param rhs The second filter to join
   * @return A new filter that represents the conjunction of the lhs and the rhs
   */
  private static Filter and(Filter lhs, Filter rhs) {
    if (lhs == Bool.TRUE) return rhs;
    if (rhs == Bool.TRUE) return lhs;
    return new And(lhs, rhs);
  }


  /**
   * Maps a list of constraint expressions to a list of transformed constraint expressions.
   * This would be better done as a closure, but we can't, especially with the "changed" flag.
   * @param elements The list to be transformed
   * @return A new list full of transformed items, or else the original if nothing was changed.
   * @throws MulgaraParserException Due to a bad transformation.
   */
  private static List<ConstraintExpression> txList(List<ConstraintExpression> elements) throws MulgaraParserException {
    boolean changed = false;
    List<ConstraintExpression> newList = new ArrayList<ConstraintExpression>();
    for (ConstraintExpression c: elements) {
      ConstraintExpression tx = transform(c);
      if (tx != c) changed = true;
      newList.add(tx);
    }
    return changed ? newList : elements;
  }


  /**
   * Map filtered constraints to the flattening operation.
   *   Filter(X1,Filter(X2,A)) => Filter(X2 && X1, A)
   */
  private static class FilterTx extends Transformer<ConstraintFilter> {
    public Class<ConstraintFilter> getTxType() { return ConstraintFilter.class; }
    public ConstraintExpression tx(ConstraintFilter constraint) throws MulgaraParserException {
      ConstraintExpression innerConstraint = constraint.getUnfilteredConstraint();
      if (innerConstraint instanceof ConstraintFilter) {
        // found Filter(X1,Filter(X2,A))
        ConstraintFilter innerFiltered = (ConstraintFilter)transform(innerConstraint); // Filter(X2,A)
        return new ConstraintFilter(innerFiltered.getUnfilteredConstraint(), and(innerFiltered.getFilter(), constraint.getFilter()));
      }
      return constraint;
    }
  }

  /**
   * Based on the syntactic (not algebraic) transformations:
   *   LeftJoin(A, Filter(X1, B), X2) => LeftJoin(A, B, X1 && X2)
   *   LeftJoin(A, LeftJoin(B, C, X1), X2) => LeftJoin(A, LeftJoin(B, C, true), X1 && X2)
   */
  private static class LeftJoinTx extends Transformer<ConstraintOptionalJoin> {
    public Class<ConstraintOptionalJoin> getTxType() { return ConstraintOptionalJoin.class; }
    public ConstraintExpression tx(ConstraintOptionalJoin leftJoin) throws MulgaraParserException {
      ConstraintExpression op = leftJoin.getOptional();
      if (op instanceof ConstraintFilter) {
        // found LeftJoin(A, Filter(X1, B), X2)
        ConstraintFilter filter = (ConstraintFilter)transform(op);  // Filter(X1, B)
        Filter f = and(filter.getFilter(), leftJoin.getFilter());  // X1 && X2
        return new ConstraintOptionalJoin(transform(leftJoin.getMain()), filter.getUnfilteredConstraint(), f);
      }
      if (op instanceof ConstraintOptionalJoin) {
        // found LeftJoin(A, LeftJoin(B, C, X1), X2)
        ConstraintOptionalJoin subJoin = (ConstraintOptionalJoin)transform(op);  // LeftJoin(B, C, X1)
        ConstraintOptionalJoin newSubJoin = new ConstraintOptionalJoin(subJoin.getMain(), subJoin.getOptional(), Bool.TRUE);
        Filter newFilter = and(subJoin.getFilter(), leftJoin.getFilter());  // X1 && X2
        return new ConstraintOptionalJoin(transform(leftJoin.getMain()), newSubJoin, newFilter);
      }
      return leftJoin;
    }
  }

  /**
   * Recurse the transformations down.
   * Normalization to sum of products can be done here (but isn't).
   */
  private static class DifferenceTx extends Transformer<ConstraintDifference> {
    public Class<ConstraintDifference> getTxType() { return ConstraintDifference.class; }
    public ConstraintExpression tx(ConstraintDifference constraint) throws MulgaraParserException {
      ConstraintExpression minuend = transform(constraint.getLhs());
      ConstraintExpression subtrahend = transform(constraint.getRhs());
      return minuend == constraint.getLhs() && subtrahend == constraint.getRhs() ?
             new ConstraintDifference(minuend, subtrahend) : constraint;
    }
  }

  /**
   * Recurse the transformations down.
   * Normalization to sum of products can be done here (but isn't).
   */
  private static class ConjunctionTx extends Transformer<ConstraintConjunction> {
    public Class<ConstraintConjunction> getTxType() { return ConstraintConjunction.class; }
    public ConstraintExpression tx(ConstraintConjunction constraint) throws MulgaraParserException {
      List<ConstraintExpression> elements = constraint.getElements();
      List<ConstraintExpression> newElements = txList(elements);
      return elements != newElements ? new ConstraintConjunction(newElements) : constraint;
    }
  }

  /**
   * Recurse the transformations down.
   * Normalization to sum of products can be done here (but isn't).
   */
  private static class DisjunctionTx extends Transformer<ConstraintDisjunction> {
    public Class<ConstraintDisjunction> getTxType() { return ConstraintDisjunction.class; }
    public ConstraintExpression tx(ConstraintDisjunction constraint) throws MulgaraParserException {
      List<ConstraintExpression> elements = constraint.getElements();
      List<ConstraintExpression> newElements = txList(elements);
      return elements != newElements ? new ConstraintDisjunction(newElements) : constraint;
    }
  }
}
