/*
 * Copyright 2009 DuraSpace.
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

package org.mulgara.query.xpath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mulgara.query.functions.MulgaraFunction;
import org.mulgara.query.functions.MulgaraFunctionGroup;

/**
 * Container for functions in the op pseudo-domain.
 *
 * @created Oct 5, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class OpFunctionGroup implements MulgaraFunctionGroup {

  /** The prefix for the op: namespace */
  static final String PREFIX = "op";

  /** The op: namespace */
  static final String NAMESPACE = PREFIX;

  /**
   * Get the prefix used for the namespace of these operations.
   * @return The short string used for a prefix in a QName.
   */
  public String getPrefix() {
    return PREFIX;
  }

  /**
   * Get the namespace of these operations.
   * @return The string of the namespace URI.
   */
  public String getNamespace() {
    return NAMESPACE;
  }

  /**
   * Get the set of SPARQL functions.
   * @return A set of MulgaraFunction for this entire group.
   */
  public Set<MulgaraFunction> getAllFunctions() {
    Set<MulgaraFunction> functions = new HashSet<MulgaraFunction>();
    functions.add(new NumericEqual());
    functions.add(new NumericLessThan());
    functions.add(new NumericGreaterThan());
    functions.add(new NumericIntegerDivision());
    functions.add(new NumericMod());
    return functions;
  }

  /**
   * Function to evaluate if two numbers are equal.
   * @see http://www.w3.org/TR/xpath-functions/#func-numeric-equal
   */
  static private class NumericEqual extends MulgaraFunction {
    public String getName() { return "numeric-equal/2"; }
    public int getArity() { return 2; }
    public Object eval(List<?> args) {
      Number left = (Number)args.get(0);
      Number right = (Number)args.get(1);
      return left.doubleValue() == right.doubleValue();
    }
  }

  /**
   * Function to evaluate if one number is less than another.
   * @see http://www.w3.org/TR/xpath-functions/#func-numeric-less-than
   */
  static private class NumericLessThan extends MulgaraFunction {
    public String getName() { return "numeric-less-than/2"; }
    public int getArity() { return 2; }
    public Object eval(List<?> args) {
      Number left = (Number)args.get(0);
      Number right = (Number)args.get(1);
      return left.doubleValue() < right.doubleValue();
    }
  }

  /**
   * Function to evaluate if one number is greater than another.
   * @see http://www.w3.org/TR/xpath-functions/#func-numeric-greater-than
   */
  static private class NumericGreaterThan extends MulgaraFunction {
    public String getName() { return "numeric-greater-than/2"; }
    public int getArity() { return 2; }
    public Object eval(List<?> args) {
      Number left = (Number)args.get(0);
      Number right = (Number)args.get(1);
      return left.doubleValue() > right.doubleValue();
    }
  }

  /**
   * Function to evaluate integer division between numbers. This does not meet the XPath semantics
   * perfectly, but will work for many situations.
   * @see http://www.w3.org/TR/xpath-functions/#func-numeric-integer-divide
   */
  static private class NumericIntegerDivision extends MulgaraFunction {
    public String getName() { return "numeric-integer-divide/2"; }
    public int getArity() { return 2; }
    public Object eval(List<?> args) {
      Number left = (Number)args.get(0);
      Number right = (Number)args.get(1);
      if (left instanceof BigDecimal) {
        return ((BigDecimal)left).divide(
            (right instanceof BigDecimal) ? (BigDecimal)right : new BigDecimal(right.toString())
        );
      }
      if (left instanceof BigInteger) {
        return ((BigInteger)left).divide(
            (right instanceof BigInteger) ? (BigInteger)right : new BigInteger(right.toString())
        );
      }
      return Double.valueOf(left.doubleValue() / right.doubleValue()).longValue();
    }
  }

  /**
   * Function to evaluate the numeric mod operation. This does not meet the XPath semantics
   * perfectly, but will work for most situations.
   * @see http://www.w3.org/TR/xpath-functions/#func-numeric-mod
   */
  static private class NumericMod extends MulgaraFunction {
    public String getName() { return "numeric-mod/2"; }
    public int getArity() { return 2; }
    public Object eval(List<?> args) {
      Number left = (Number)args.get(0);
      Number right = (Number)args.get(1);
      if (left instanceof Byte || left instanceof Short || left instanceof Integer || left instanceof Long) {
        return left.longValue() % right.longValue();
      }
      if (left instanceof Float || left instanceof Double) {
        return left.doubleValue() % right.doubleValue();
      }
      if (left instanceof BigDecimal) {
        return ((BigDecimal)left).remainder(
            (right instanceof BigDecimal) ? (BigDecimal)right : new BigDecimal(right.toString())
        );
      }
      if (left instanceof BigInteger) {
        return ((BigInteger)left).remainder(
            (right instanceof BigInteger) ? (BigInteger)right : new BigInteger(right.toString())
        );
      }
      return Double.valueOf(left.doubleValue() % right.doubleValue()).longValue();
    }
  }
}
