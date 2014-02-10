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

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.mulgara.query.rdf.XSD;
import org.mulgara.query.rdf.XSDAbbrev;

/**
 * A numeric value.  Expect that this will be extended into Double, Integer, Long, etc.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class NumericLiteral extends TypedLiteral implements NumericExpression {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = -4609989082999517348L;

  /**
   * Creates the value to wrap the number
   * @param n The number to wrap
   */
  public NumericLiteral(Number n) {
    super(n, numericTypeMap.get(n.getClass()));
  }

  /**
   * Creates the value to wrap a number. This is the same as the previous
   * constructor, but allows the caller to provide a hint for the URI.
   * @param n The number to wrap
   * @param typeUri The XSD URI for the type of number.
   */
  public NumericLiteral(Number n, URI typeUri) {
    super(getValueFor(n, typeUri), typeUri);
  }

  /** @see org.mulgara.query.filter.value.NumericExpression#getNumber() */
  public Number getNumber() {
    return (Number)value;
  }

  /**
   * Gets the IRI that is used to represent the given numeric type.
   * @param n The number to get the type for.
   * @return An IRI containing the XSD datatype of n.
   */
  public static IRI getTypeFor(Number n) {
    return new IRI(numericTypeMap.get(n.getClass()));
  }

  /**
   * Tests if a URI represents a numeric type.
   * @param type The URI to test.
   * @return <code>true</code> iff type represents a numeric type.
   */
  public static boolean isNumeric(URI type) {
    return XSD.isNumericType(type) || XSDAbbrev.isNumericType(type);
  }

  /**
   * Converts a Number to another Number type by using the type URI.
   * @param n The number to convert.
   * @param type The type to convert to.
   * @return A new Number, defined by type.
   */
  public static Number getValueFor(Number n, URI type) {
    return infoMap.get(type).valueOf(n);
  }

  /** A mapping of numeric types to their URIs */
  private static final Map<Class<? extends Number>,URI> numericTypeMap = new HashMap<Class<? extends Number>,URI>();
  
  static {
    numericTypeMap.put(Float.class, XSD.FLOAT_URI);
    numericTypeMap.put(Double.class, XSD.DOUBLE_URI);
    numericTypeMap.put(Long.class, XSD.LONG_URI);
    numericTypeMap.put(Integer.class, XSD.INT_URI);
    numericTypeMap.put(Short.class, XSD.SHORT_URI);
    numericTypeMap.put(Byte.class, XSD.BYTE_URI);
    numericTypeMap.put(BigDecimal.class, XSD.DECIMAL_URI);
  }

}
