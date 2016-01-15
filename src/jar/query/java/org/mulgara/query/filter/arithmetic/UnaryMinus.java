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
package org.mulgara.query.filter.arithmetic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.value.NumericExpression;


/**
 * Represents a numeric negation.
 *
 * @created Mar 17, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class UnaryMinus extends AbstractNumericOperation implements NumericExpression {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 33264336439539952L;

  /** The operand */
  protected NumericExpression operand;

  /**
   * Creates a negation of the given term.
   * @param operand The numeric term to negate.
   */
  public UnaryMinus(NumericExpression operand) {
    this.operand = operand;
    operand.setContextOwner(this);
  }

  /** @see org.mulgara.query.filter.RDFTerm#getVariables() */
  public Set<Variable> getVariables() {
    return operand.getVariables();
  }

  /** {@inheritDoc} */
  public boolean isGrounded() throws QueryException {
    return operand.isGrounded();
  }

  // Not using generics in NumberOps as we can't know the types at this stage, but they are handy
  // for defining the classes correctly
  /**
   * Calculate the result of this operation, returning it as a normal number.
   * @throws QueryException The values of one of the operands could not be resolved.
   * @see org.mulgara.query.filter.value.NumericExpression#getNumber()
   */
  @SuppressWarnings("unchecked")
  public Number getNumber() throws QueryException {
    Number n = operand.getNumber();
    InversionOp<Number> op = (InversionOp<Number>)opMap.get(n.getClass());
    if (op == null) throw new AssertionError("Missing entry in negation operation map");
    return op.invert(n);
  }

  /** Defines a unary negation function that returns a number of the same type as it receives */
  interface InversionOp<T extends Number> {
    public Class<T> getType();
    public Number invert(T v);
  }

  /** A map of types to the functions that multiply them with correct type promotions */
  protected static Map<Class<? extends Number>,InversionOp<? extends Number>> opMap = new HashMap<Class<? extends Number>,InversionOp<? extends Number>>();

  /** A utility to add a number function to the promotion map */
  private static void addType(InversionOp<? extends Number> op) { opMap.put(op.getType(), op); }

  static {
    addType(new InvertD());
    addType(new InvertF());
    addType(new InvertL());
    addType(new InvertI());
    addType(new InvertS());
    addType(new InvertB());
  }

  private static class InvertD implements InversionOp<Double> {
    public Class<Double> getType() { return Double.class; }
    public Number invert(Double v) { return -v; }
  }

  private static class InvertF implements InversionOp<Float> {
    public Class<Float> getType() { return Float.class; }
    public Number invert(Float v) { return -v; }
  }

  private static class InvertL implements InversionOp<Long> {
    public Class<Long> getType() { return Long.class; }
    public Number invert(Long v) { return -v; }
  }

  private static class InvertI implements InversionOp<Integer> {
    public Class<Integer> getType() { return Integer.class; }
    public Number invert(Integer v) { return -v; }
  }

  private static class InvertS implements InversionOp<Short> {
    public Class<Short> getType() { return Short.class; }
    public Number invert(Short v) { return -v; }
  }

  private static class InvertB implements InversionOp<Byte> {
    public Class<Byte> getType() { return Byte.class; }
    public Number invert(Byte v) { return -v; }
  }

}
