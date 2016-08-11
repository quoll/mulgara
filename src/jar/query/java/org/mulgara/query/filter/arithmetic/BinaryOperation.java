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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.value.NumericExpression;


/**
 * Represents a binary arithmetic operation.
 *
 * @created Mar 13, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class BinaryOperation extends AbstractNumericOperation implements NumericExpression {

  /** Serialization ID */
  private static final long serialVersionUID = -8435619400443937913L;

  /** The first operand */
  protected NumericExpression lhs;

  /** The second operand */
  protected NumericExpression rhs;

  /**
   * Creates an operation between two terms
   * @param lhs The left side of the operation
   * @param rhs The right side of the operation
   */
  BinaryOperation(NumericExpression lhs, NumericExpression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
    lhs.setContextOwner(this);
    rhs.setContextOwner(this);
  }

  /** {@inheritDoc} */
  public boolean isGrounded() throws QueryException {
    return lhs.isGrounded() && rhs.isGrounded();
  }

  /** @see org.mulgara.query.filter.RDFTerm#getVariables() */
  public Set<Variable> getVariables() {
    Set<Variable> result = new HashSet<Variable>(lhs.getVariables());
    result.addAll(rhs.getVariables());
    return result;
  }

  // Not using generics in NumberOps as we can't know the types at this stage, but they are handy
  // for defining the classes correctly
  /**
   * Calculate the result of this operation, returning it as a normal number.
   * @throws QueryException The values of one of the operands could not be resolved.
   * @see org.mulgara.query.filter.value.NumericExpression#getNumber()
   */
  public Number getNumber() throws QueryException {
    Number left = lhs.getNumber();
    Number right = rhs.getNumber();
    @SuppressWarnings("unchecked")
    NumberOps<Number, Number> op = (NumberOps<Number, Number>)opMap.get(new ClassPair(left, right));
    if (op == null) throw new AssertionError("Missing entry in operation map");
    return doOperation(op, left, right);
  }

  /**
   * Perform the operation specific to the implementing class
   * @param ops The operations to use for the current parameters
   * @param left The first operand
   * @param right The second operand
   * @return The arithmetic result of applying the operation to the parameters
   */
  abstract <L extends Number, R extends Number> Number doOperation(NumberOps<L,R> ops, L left, R right);

  /** Stores classes as an integrated pair for mapping pairs of Numbers to their appropriate functions */
  static class ClassPair {
    private Class<? extends Number> left;
    private Class<? extends Number> right;

    /**
     * Create the pair using two classes
     * @param left The left side of the pair
     * @param right The right side of the pair
     */
    public ClassPair(Class<? extends Number> left, Class<? extends Number> right) {
      this.left = left;
      this.right = right;
    }
 
    /**
     * Create the pair using two <em>instance</em> of classes
     * @param left The left side instance for the pair
     * @param right The right side instance for the pair
     */
    public ClassPair(Number left, Number right) {
      this.left = left.getClass();
      this.right = right.getClass();
    }
 
    /** @return The left side of the pair */
    public Class<? extends Number> getLeft() { return left; }
    /** @return The right side of the pair */
    public Class<? extends Number> getRight() { return right; }
 
    /** @return a hashcode determined by mising the hashcodes of the contributing classes */
    public int hashCode() { return left.hashCode() ^ ~right.hashCode(); }
    /** @return <code>true</code> iff o is a class pair containing equal elements */
    public boolean equals(Object o) {
      if (o == null || (!(o instanceof ClassPair))) return false;
      return left == ((ClassPair)o).left && right == ((ClassPair)o).right;
    }
  }

  /** Defines a binary function that takes two numbers and returns a new one */
  interface NumberOps<LT extends Number,RT extends Number> {
    /**
     * Get a ClassPair that represents this operation
     * @return a new ClassPair for this operation type
     */
    public ClassPair getClassPair();
    /**
     * Perform a multiplication on arguments of the specified types.
     * @param left The first multiplicands
     * @param right The second multiplicands
     * @return The product as a number of the type defined in this class.
     */
    public Number product(LT left, RT right);
    /**
     * Perform a sum on arguments of the specified types.
     * @param left The first addend
     * @param right The second addend
     * @return The sum as a number of the type defined in this class.
     */
    public Number sum(LT left, RT right);
    /**
     * Perform a subtraction on arguments of the specified types.
     * @param left The minuend
     * @param right The subtrahend
     * @return The difference as a number of the type defined in this class.
     */
    public Number subtract(LT left, RT right);
    /**
     * Perform a multiplication on arguments of the specified types.
     * @param left The dividend
     * @param right The divisor
     * @return The quotient as a number of the type defined in this class.
     */
    public Number divide(LT left, RT right);
  }

  /** A map of types to the functions that multiply them with correct type promotions */
  protected static Map<ClassPair,NumberOps<? extends Number,? extends Number>> opMap = new HashMap<ClassPair,NumberOps<? extends Number,? extends Number>>();

  /** A utility to add a number function to the promotion map */
  private static void addType(NumberOps<?,?> nf) { opMap.put(nf.getClassPair(), nf); }

  // The following population of the operation map was generated with this ruby script:
  /*
   * types = [ "Double", "Float", "Long", "Integer", "Short", "Byte" ]
   * puts "  static {"
   * types.each do |l|
   *   types.each do |r|
   *     puts "    addType(new #{l[0].chr}#{r[0].chr}());"
   *   end
   * end
   * puts "  }"
   * puts
   * types.each do |l|
   *   types.each do |r|
   *     puts "  private static class #{l[0].chr}#{r[0].chr} implements NumberOps<#{l},#{r}> {"
   *     puts "    public ClassPair getClassPair() { return new ClassPair(#{l}.class, #{r}.class); }"
   *     puts "    public Number product(#{l} left, #{r} right) { return left * right; }"
   *     puts "    public Number sum(#{l} left, #{r} right) { return left + right; }"
   *     puts "    public Number subtract(#{l} left, #{r} right) { return left - right; }"
   *     puts "    public Number divide(#{l} left, #{r} right) { return left / right; }"
   *     puts "  }"
   *     puts
   *   end
   * end
   */  

  static {
    addType(new DD());
    addType(new DF());
    addType(new DL());
    addType(new DI());
    addType(new DS());
    addType(new DB());
    addType(new FD());
    addType(new FF());
    addType(new FL());
    addType(new FI());
    addType(new FS());
    addType(new FB());
    addType(new LD());
    addType(new LF());
    addType(new LL());
    addType(new LI());
    addType(new LS());
    addType(new LB());
    addType(new ID());
    addType(new IF());
    addType(new IL());
    addType(new II());
    addType(new IS());
    addType(new IB());
    addType(new SD());
    addType(new SF());
    addType(new SL());
    addType(new SI());
    addType(new SS());
    addType(new SB());
    addType(new BD());
    addType(new BF());
    addType(new BL());
    addType(new BI());
    addType(new BS());
    addType(new BB());
  }

  private static class DD implements NumberOps<Double,Double> {
    public ClassPair getClassPair() { return new ClassPair(Double.class, Double.class); }
    public Number product(Double left, Double right) { return left * right; }
    public Number sum(Double left, Double right) { return left + right; }
    public Number subtract(Double left, Double right) { return left - right; }
    public Number divide(Double left, Double right) { return left / right; }
  }

  private static class DF implements NumberOps<Double,Float> {
    public ClassPair getClassPair() { return new ClassPair(Double.class, Float.class); }
    public Number product(Double left, Float right) { return left * right; }
    public Number sum(Double left, Float right) { return left + right; }
    public Number subtract(Double left, Float right) { return left - right; }
    public Number divide(Double left, Float right) { return left / right; }
  }

  private static class DL implements NumberOps<Double,Long> {
    public ClassPair getClassPair() { return new ClassPair(Double.class, Long.class); }
    public Number product(Double left, Long right) { return left * right; }
    public Number sum(Double left, Long right) { return left + right; }
    public Number subtract(Double left, Long right) { return left - right; }
    public Number divide(Double left, Long right) { return left / right; }
  }

  private static class DI implements NumberOps<Double,Integer> {
    public ClassPair getClassPair() { return new ClassPair(Double.class, Integer.class); }
    public Number product(Double left, Integer right) { return left * right; }
    public Number sum(Double left, Integer right) { return left + right; }
    public Number subtract(Double left, Integer right) { return left - right; }
    public Number divide(Double left, Integer right) { return left / right; }
  }

  private static class DS implements NumberOps<Double,Short> {
    public ClassPair getClassPair() { return new ClassPair(Double.class, Short.class); }
    public Number product(Double left, Short right) { return left * right; }
    public Number sum(Double left, Short right) { return left + right; }
    public Number subtract(Double left, Short right) { return left - right; }
    public Number divide(Double left, Short right) { return left / right; }
  }

  private static class DB implements NumberOps<Double,Byte> {
    public ClassPair getClassPair() { return new ClassPair(Double.class, Byte.class); }
    public Number product(Double left, Byte right) { return left * right; }
    public Number sum(Double left, Byte right) { return left + right; }
    public Number subtract(Double left, Byte right) { return left - right; }
    public Number divide(Double left, Byte right) { return left / right; }
  }

  private static class FD implements NumberOps<Float,Double> {
    public ClassPair getClassPair() { return new ClassPair(Float.class, Double.class); }
    public Number product(Float left, Double right) { return left * right; }
    public Number sum(Float left, Double right) { return left + right; }
    public Number subtract(Float left, Double right) { return left - right; }
    public Number divide(Float left, Double right) { return left / right; }
  }

  private static class FF implements NumberOps<Float,Float> {
    public ClassPair getClassPair() { return new ClassPair(Float.class, Float.class); }
    public Number product(Float left, Float right) { return left * right; }
    public Number sum(Float left, Float right) { return left + right; }
    public Number subtract(Float left, Float right) { return left - right; }
    public Number divide(Float left, Float right) { return left / right; }
  }

  private static class FL implements NumberOps<Float,Long> {
    public ClassPair getClassPair() { return new ClassPair(Float.class, Long.class); }
    public Number product(Float left, Long right) { return left * right; }
    public Number sum(Float left, Long right) { return left + right; }
    public Number subtract(Float left, Long right) { return left - right; }
    public Number divide(Float left, Long right) { return left / right; }
  }

  private static class FI implements NumberOps<Float,Integer> {
    public ClassPair getClassPair() { return new ClassPair(Float.class, Integer.class); }
    public Number product(Float left, Integer right) { return left * right; }
    public Number sum(Float left, Integer right) { return left + right; }
    public Number subtract(Float left, Integer right) { return left - right; }
    public Number divide(Float left, Integer right) { return left / right; }
  }

  private static class FS implements NumberOps<Float,Short> {
    public ClassPair getClassPair() { return new ClassPair(Float.class, Short.class); }
    public Number product(Float left, Short right) { return left * right; }
    public Number sum(Float left, Short right) { return left + right; }
    public Number subtract(Float left, Short right) { return left - right; }
    public Number divide(Float left, Short right) { return left / right; }
  }

  private static class FB implements NumberOps<Float,Byte> {
    public ClassPair getClassPair() { return new ClassPair(Float.class, Byte.class); }
    public Number product(Float left, Byte right) { return left * right; }
    public Number sum(Float left, Byte right) { return left + right; }
    public Number subtract(Float left, Byte right) { return left - right; }
    public Number divide(Float left, Byte right) { return left / right; }
  }

  private static class LD implements NumberOps<Long,Double> {
    public ClassPair getClassPair() { return new ClassPair(Long.class, Double.class); }
    public Number product(Long left, Double right) { return left * right; }
    public Number sum(Long left, Double right) { return left + right; }
    public Number subtract(Long left, Double right) { return left - right; }
    public Number divide(Long left, Double right) { return left / right; }
  }

  private static class LF implements NumberOps<Long,Float> {
    public ClassPair getClassPair() { return new ClassPair(Long.class, Float.class); }
    public Number product(Long left, Float right) { return left * right; }
    public Number sum(Long left, Float right) { return left + right; }
    public Number subtract(Long left, Float right) { return left - right; }
    public Number divide(Long left, Float right) { return left / right; }
  }

  private static class LL implements NumberOps<Long,Long> {
    public ClassPair getClassPair() { return new ClassPair(Long.class, Long.class); }
    public Number product(Long left, Long right) { return left * right; }
    public Number sum(Long left, Long right) { return left + right; }
    public Number subtract(Long left, Long right) { return left - right; }
    public Number divide(Long left, Long right) { return left / right; }
  }

  private static class LI implements NumberOps<Long,Integer> {
    public ClassPair getClassPair() { return new ClassPair(Long.class, Integer.class); }
    public Number product(Long left, Integer right) { return left * right; }
    public Number sum(Long left, Integer right) { return left + right; }
    public Number subtract(Long left, Integer right) { return left - right; }
    public Number divide(Long left, Integer right) { return left / right; }
  }

  private static class LS implements NumberOps<Long,Short> {
    public ClassPair getClassPair() { return new ClassPair(Long.class, Short.class); }
    public Number product(Long left, Short right) { return left * right; }
    public Number sum(Long left, Short right) { return left + right; }
    public Number subtract(Long left, Short right) { return left - right; }
    public Number divide(Long left, Short right) { return left / right; }
  }

  private static class LB implements NumberOps<Long,Byte> {
    public ClassPair getClassPair() { return new ClassPair(Long.class, Byte.class); }
    public Number product(Long left, Byte right) { return left * right; }
    public Number sum(Long left, Byte right) { return left + right; }
    public Number subtract(Long left, Byte right) { return left - right; }
    public Number divide(Long left, Byte right) { return left / right; }
  }

  private static class ID implements NumberOps<Integer,Double> {
    public ClassPair getClassPair() { return new ClassPair(Integer.class, Double.class); }
    public Number product(Integer left, Double right) { return left * right; }
    public Number sum(Integer left, Double right) { return left + right; }
    public Number subtract(Integer left, Double right) { return left - right; }
    public Number divide(Integer left, Double right) { return left / right; }
  }

  private static class IF implements NumberOps<Integer,Float> {
    public ClassPair getClassPair() { return new ClassPair(Integer.class, Float.class); }
    public Number product(Integer left, Float right) { return left * right; }
    public Number sum(Integer left, Float right) { return left + right; }
    public Number subtract(Integer left, Float right) { return left - right; }
    public Number divide(Integer left, Float right) { return left / right; }
  }

  private static class IL implements NumberOps<Integer,Long> {
    public ClassPair getClassPair() { return new ClassPair(Integer.class, Long.class); }
    public Number product(Integer left, Long right) { return left * right; }
    public Number sum(Integer left, Long right) { return left + right; }
    public Number subtract(Integer left, Long right) { return left - right; }
    public Number divide(Integer left, Long right) { return left / right; }
  }

  private static class II implements NumberOps<Integer,Integer> {
    public ClassPair getClassPair() { return new ClassPair(Integer.class, Integer.class); }
    public Number product(Integer left, Integer right) { return left * right; }
    public Number sum(Integer left, Integer right) { return left + right; }
    public Number subtract(Integer left, Integer right) { return left - right; }
    public Number divide(Integer left, Integer right) { return left / right; }
  }

  private static class IS implements NumberOps<Integer,Short> {
    public ClassPair getClassPair() { return new ClassPair(Integer.class, Short.class); }
    public Number product(Integer left, Short right) { return left * right; }
    public Number sum(Integer left, Short right) { return left + right; }
    public Number subtract(Integer left, Short right) { return left - right; }
    public Number divide(Integer left, Short right) { return left / right; }
  }

  private static class IB implements NumberOps<Integer,Byte> {
    public ClassPair getClassPair() { return new ClassPair(Integer.class, Byte.class); }
    public Number product(Integer left, Byte right) { return left * right; }
    public Number sum(Integer left, Byte right) { return left + right; }
    public Number subtract(Integer left, Byte right) { return left - right; }
    public Number divide(Integer left, Byte right) { return left / right; }
  }

  private static class SD implements NumberOps<Short,Double> {
    public ClassPair getClassPair() { return new ClassPair(Short.class, Double.class); }
    public Number product(Short left, Double right) { return left * right; }
    public Number sum(Short left, Double right) { return left + right; }
    public Number subtract(Short left, Double right) { return left - right; }
    public Number divide(Short left, Double right) { return left / right; }
  }

  private static class SF implements NumberOps<Short,Float> {
    public ClassPair getClassPair() { return new ClassPair(Short.class, Float.class); }
    public Number product(Short left, Float right) { return left * right; }
    public Number sum(Short left, Float right) { return left + right; }
    public Number subtract(Short left, Float right) { return left - right; }
    public Number divide(Short left, Float right) { return left / right; }
  }

  private static class SL implements NumberOps<Short,Long> {
    public ClassPair getClassPair() { return new ClassPair(Short.class, Long.class); }
    public Number product(Short left, Long right) { return left * right; }
    public Number sum(Short left, Long right) { return left + right; }
    public Number subtract(Short left, Long right) { return left - right; }
    public Number divide(Short left, Long right) { return left / right; }
  }

  private static class SI implements NumberOps<Short,Integer> {
    public ClassPair getClassPair() { return new ClassPair(Short.class, Integer.class); }
    public Number product(Short left, Integer right) { return left * right; }
    public Number sum(Short left, Integer right) { return left + right; }
    public Number subtract(Short left, Integer right) { return left - right; }
    public Number divide(Short left, Integer right) { return left / right; }
  }

  private static class SS implements NumberOps<Short,Short> {
    public ClassPair getClassPair() { return new ClassPair(Short.class, Short.class); }
    public Number product(Short left, Short right) { return left * right; }
    public Number sum(Short left, Short right) { return left + right; }
    public Number subtract(Short left, Short right) { return left - right; }
    public Number divide(Short left, Short right) { return left / right; }
  }

  private static class SB implements NumberOps<Short,Byte> {
    public ClassPair getClassPair() { return new ClassPair(Short.class, Byte.class); }
    public Number product(Short left, Byte right) { return left * right; }
    public Number sum(Short left, Byte right) { return left + right; }
    public Number subtract(Short left, Byte right) { return left - right; }
    public Number divide(Short left, Byte right) { return left / right; }
  }

  private static class BD implements NumberOps<Byte,Double> {
    public ClassPair getClassPair() { return new ClassPair(Byte.class, Double.class); }
    public Number product(Byte left, Double right) { return left * right; }
    public Number sum(Byte left, Double right) { return left + right; }
    public Number subtract(Byte left, Double right) { return left - right; }
    public Number divide(Byte left, Double right) { return left / right; }
  }

  private static class BF implements NumberOps<Byte,Float> {
    public ClassPair getClassPair() { return new ClassPair(Byte.class, Float.class); }
    public Number product(Byte left, Float right) { return left * right; }
    public Number sum(Byte left, Float right) { return left + right; }
    public Number subtract(Byte left, Float right) { return left - right; }
    public Number divide(Byte left, Float right) { return left / right; }
  }

  private static class BL implements NumberOps<Byte,Long> {
    public ClassPair getClassPair() { return new ClassPair(Byte.class, Long.class); }
    public Number product(Byte left, Long right) { return left * right; }
    public Number sum(Byte left, Long right) { return left + right; }
    public Number subtract(Byte left, Long right) { return left - right; }
    public Number divide(Byte left, Long right) { return left / right; }
  }

  private static class BI implements NumberOps<Byte,Integer> {
    public ClassPair getClassPair() { return new ClassPair(Byte.class, Integer.class); }
    public Number product(Byte left, Integer right) { return left * right; }
    public Number sum(Byte left, Integer right) { return left + right; }
    public Number subtract(Byte left, Integer right) { return left - right; }
    public Number divide(Byte left, Integer right) { return left / right; }
  }

  private static class BS implements NumberOps<Byte,Short> {
    public ClassPair getClassPair() { return new ClassPair(Byte.class, Short.class); }
    public Number product(Byte left, Short right) { return left * right; }
    public Number sum(Byte left, Short right) { return left + right; }
    public Number subtract(Byte left, Short right) { return left - right; }
    public Number divide(Byte left, Short right) { return left / right; }
  }

  private static class BB implements NumberOps<Byte,Byte> {
    public ClassPair getClassPair() { return new ClassPair(Byte.class, Byte.class); }
    public Number product(Byte left, Byte right) { return left * right; }
    public Number sum(Byte left, Byte right) { return left + right; }
    public Number subtract(Byte left, Byte right) { return left - right; }
    public Number divide(Byte left, Byte right) { return left / right; }
  }

}
