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

package org.mulgara.query.functions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.jrdf.graph.Literal;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.value.SimpleLiteral;

/**
 * General class for providing management information for XPath function implementations.
 *
 * @created Oct 5, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public abstract class MulgaraFunction implements XPathFunction {

  /**
   * Evaluate the method represented by this class, using the supplied arguments.
   * @see javax.xml.xpath.XPathFunction#evaluate(java.util.List)
   */
  @SuppressWarnings("rawtypes")
  public Object evaluate(List args) throws XPathFunctionException {
    checkArgs(getArity(), args);
    try {
      return eval(args);
    } catch (ClassCastException e) {
      throw new XPathFunctionException("Incorrect parameter types passed to function: " + e.getMessage());
    }
  }

  /**
   * Test that the correct number of arguments were provided to the function call.
   * @param expected The expected number of arguments.
   * @param args The list of arguments.
   * @throws XPathFunctionException Thrown if the argument list is the wrong length.
   */
  protected void checkArgs(int expected, List<?> args) throws XPathFunctionException {
    if (expected >= 0 && args.size() != expected) {
      throw new XPathFunctionException("Incorrect number of parameters. Should be " + expected + ", but was: " + args.size());
    }
  }

  /**
   * Returns the number of arguments required to use this function. Default is 1. 
   * @return The number of arguments for this function.
   */
  protected int getArity() { return 1; }

  /**
   * The name of this function, followed by its arity.
   * If not overridden then this will be the function class's name, starting with a lower-case letter.
   * @return The name/arity of this function.
   */
  public String getName() {
    StringBuilder name = new StringBuilder(getClass().getSimpleName());
    char c = name.charAt(0);
    if (Character.isUpperCase(c)) {
      name.setCharAt(0, Character.toLowerCase(c));
    }
    for (int i = 1; i < name.length(); i++) {
      c = name.charAt(0);
      if (Character.isUpperCase(c)) name.replace(i, i + 1, "-" + Character.toLowerCase(c));
    }
    name.append("/");
    int arity = getArity();
    if (arity >= 0) name.append(getArity());
    else name.append("*");
    return name.toString();
  }

  /**
   * Evaluates the operation of the function. The argument list will be the correct length.
   * @param args The arguments for the function.
   * @return The return value of the function.
   * @throws XPathFunctionException If there was an error executing the function.
   */
  protected abstract Object eval(List<?> args) throws XPathFunctionException;

  /**
   * Convert a singleton value into its effective boolean value (EBV).
   * @param o The singleton to test.
   * @return The EBV of the value.
   * @throws XPathFunctionException If a complex evaluation throws an exception.
   */
  protected static final boolean toBool(Object o) throws XPathFunctionException {
    if (o == null) return false;
    if (o instanceof String) return ((String)o).length() != 0;
    if (o instanceof Number) return ((Number)o).doubleValue() != 0 && ((Number)o).doubleValue() != Double.NaN;
    if (o instanceof Boolean) return ((Boolean)o).booleanValue();
    try {
      if (o instanceof Literal) return new SimpleLiteral(((Literal)o).getLexicalForm(), ((Literal)o).getLanguage()).test(null);
    } catch (NullPointerException e) {
      throw new XPathFunctionException("Conversion of data to a simple literal requires a context: " + e.getMessage());
    } catch (QueryException e) {
      throw new XPathFunctionException("Unable to convert data to a simple literal: " + e.getMessage());
    }
    throw new XPathFunctionException("Type error: " + o + " [" + o.getClass() + "]");
  }

}
