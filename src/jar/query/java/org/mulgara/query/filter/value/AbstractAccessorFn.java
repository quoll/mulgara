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

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.AbstractFilterValue;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.RDFTerm;


/**
 * Represents a function that accesses an RDF value.
 * This node acts as an RDFTerm and as a ValueLiteral, so that it can act as
 * either a Literal (Simple or Typed) or an IRI.
 * By default, this object will act as a literal.
 *
 * @created Mar 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractAccessorFn extends AbstractFilterValue implements ValueLiteral {

  /** Serialization ID */
  private static final long serialVersionUID = 291544759144763946L;

  /** The operand for the function */
  RDFTerm operand;

  /**
   * Single argument constructor.
   * @param operand The single argument for this function.
   */
  public AbstractAccessorFn(RDFTerm operand) {
    super(operand);
    this.operand = operand;
  }

  /**
   * Multiple argument constructor.
   * @param operands All the arguments for this function, in order.
   */
  public AbstractAccessorFn(RDFTerm... operands) {
    super(operands);
    this.operand = (operands.length > 0) ? operands[0] : null;
  }

  /** @see org.mulgara.query.filter.value.ValueLiteral#getType() */
  public IRI getType() throws QueryException {
    return SimpleLiteral.STRING_TYPE;
  }

  /**
   * @see org.mulgara.query.filter.ContextOwner#setCurrentContext(org.mulgara.query.filter.Context)
   * Overridden from AbstractFilterValue to provide a different error message.
   */
  public void setCurrentContext(Context context) {
    if (!(context.equals(getContextOwner().getCurrentContext()))) throw new AssertionError("Function context being set differently to initial calling context.");
  }

  public boolean greaterThan(ComparableExpression v) throws QueryException {
    return resolveComparable().greaterThan(v);
  }

  /** @see org.mulgara.query.filter.value.ComparableExpression#greaterThanEqualTo(org.mulgara.query.filter.value.ComparableExpression) */
  public boolean greaterThanEqualTo(ComparableExpression v) throws QueryException {
    return resolveComparable().greaterThanEqualTo(v);
  }

  /** @see org.mulgara.query.filter.value.ComparableExpression#lessThan(org.mulgara.query.filter.value.ComparableExpression) */
  public boolean lessThan(ComparableExpression v) throws QueryException {
    return resolveComparable().lessThan(v);
  }

  /** @see org.mulgara.query.filter.value.ComparableExpression#lessThanEqualTo(org.mulgara.query.filter.value.ComparableExpression) */
  public boolean lessThanEqualTo(ComparableExpression v) throws QueryException {
    return resolveComparable().lessThanEqualTo(v);
  }

  /**
   * Resolves this function to a value that can be compared, or throws an exception if it cannot be compared.
   * @return A value that can be compared for inequality.
   * @throws QueryException If there was a problem resolving the function, or if the value is not comparable.
   */
  protected ComparableExpression resolveComparable() throws QueryException {
    RDFTerm result = resolve();
    if (!(result instanceof ComparableExpression)) throw new QueryException("Type error: cannot compare with a: " + result.getClass().getSimpleName());
    return (ComparableExpression)result;
  }
}
