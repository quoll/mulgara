/**
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

package org.mulgara.query.filter.value;

import java.net.URI;

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.AbstractContextOwner;

/**
 * Represents literal values that can be compared.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractComparable extends AbstractContextOwner implements ComparableExpression {

  /** Serialization ID */
  private static final long serialVersionUID = -8790392353983171058L;

  /** {@inheritDoc} */
  public boolean lessThan(ComparableExpression v) throws QueryException {
    compatibilityTest(v);
    return compare(getValue(), v.getValue()) < 0;
  }

  /** {@inheritDoc} */
  public boolean greaterThan(ComparableExpression v) throws QueryException {
    compatibilityTest(v);
    return compare(getValue(), v.getValue()) > 0;
  }

  /** {@inheritDoc} */
  public boolean lessThanEqualTo(ComparableExpression v) throws QueryException {
    return !greaterThan(v);
  }

  /** {@inheritDoc} */
  public boolean greaterThanEqualTo(ComparableExpression v) throws QueryException {
    return !lessThan(v);
  }

  /**
   * Equality comparison used for other objects that are explicitly comparable.
   *
   * @param v the expression to compare against
   * @return true if this values equals the expression's value
   */
  public boolean equals(ComparableExpression v) throws QueryException {
    return compare(getValue(), v.getValue()) == 0;
  }

  /**
   * Tests a value to see if it is a simple literal, and throws an exception if it is.
   * Simple literals do a similar test when compared with a ComparableExpression.
   * @param v The comparable expression to test.
   * @throws QueryException If the comparable expression resolves to a {@link SimpleLiteral}.
   */
  private void compatibilityTest(ComparableExpression v) throws QueryException {
    boolean lhsLiteral = isLiteral();
    boolean rhsLiteral = v.isLiteral();
    if (rhsLiteral && ((ValueLiteral)v).isSimple()) typeError(v);
    // if one is literal and the other is not, then these cannot be compared
    if (lhsLiteral ^ rhsLiteral) typeError(v);
    // if neither are literal, then we can't test further
    if (lhsLiteral && rhsLiteral) {
      // both are literal
      URI lhsType = ((ValueLiteral)this).getType().getValue();
      URI rhsType = ((ValueLiteral)v).getType().getValue();
      boolean lhsNumeric = NumericLiteral.isNumeric(lhsType);
      boolean rhsNumeric = NumericLiteral.isNumeric(rhsType);
      // if one is numeric and the other is not, then cannot continue
      if (lhsNumeric ^ rhsNumeric) typeError(v);
      // if neither are numeric, then the types must be identical
      if (!lhsNumeric && !rhsNumeric) {
        if (!lhsType.equals(rhsType)) typeError(v);
      }
    }
  }

  /**
   * Throws an exception due to incompatible types.
   * @param v The object with the incompatible type.
   * @throws QueryException Always thrown, to indicate that v is incompatible with this object.
   */
  private void typeError(ComparableExpression v) throws QueryException {
    throw new QueryException("Type Error: cannot compare a " + getClass().getSimpleName() + " with a " + v.getClass().getSimpleName());
  }

  /**
   * Compares elements of the type handled by the implementing class.
   * @param left The LHS of the comparison
   * @param right The RHS of the comparison
   * @return -1 if left<right, +1 if left>right, 0 if left==right
   * @throws QueryException If getting the values for the comparison is invalid.
   */
  protected abstract int compare(Object left, Object right) throws QueryException;

}
