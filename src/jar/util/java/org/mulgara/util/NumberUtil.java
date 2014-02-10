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

package org.mulgara.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides utilities for parsing numbers, and determining types for numbers.
 *
 * @created May 5, 2008
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class NumberUtil {

  /**
   * Converts a string to a number of an appropriate type. The integral types are Long, Integer
   * and BigDecimal. The only floating point value is Double, since it is difficult to determine
   * a more appropriate type between Float and Double. It is possible to return Bytes and Shorts,
   * but these are less commonly used, so have been ignored here.
   * @param n The string representing the number.
   * @return A {@link java.lang.Number} containing the parsed data.
   */
  static public Number parseNumber(String n) {
    if (n.indexOf('.') >= 0) return Double.valueOf(n);
    try {
      Long l = Long.valueOf(n);
      if (l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) return l.intValue();
      return l;
    } catch (NumberFormatException e) {
      return new BigInteger(n);
    }
  }

  /**
   * Gets the XSD URI for a numeric class.
   * @param cls The class to get the XSD URI for.
   * @return A URI in the XSD namespace for the numeric type given, or <code>null</code> if the
   *   numeric type is unrecognized.
   */
  static public URI getXSD(Class<? extends Number> cls) {
    NumberStructure<?> st = classToNumber.get(cls);
    return (st == null) ? null : st.getURI();
  }

  /**
   * Gets the XSD URI for a number.
   * @param n The number to get the XSD URI for.
   * @return A URI in the XSD namespace for the number given, or <code>null</code> if the
   *   numeric type is unrecognized.
   */
  static public URI getXSD(Number n) {
    NumberStructure<?> st = classToNumber.get(n.getClass());
    return (st == null) ? null : st.getURI();
  }

  /**
   * Gets the numeric Class for a numeric XSD URI.
   * @param xsdUri The URI for the numeric type.
   * @return A class that matches the URI, or <code>null</code> if the URI is not recognized.
   */
  static public Class<? extends Number> getType(URI xsdUri) {
    NumberStructure<?> st = xsdToNumber.get(xsdUri);
    return (st == null) ? null : st.getType();
  }

  /**
   * Parses a string representing a number into a numeric type defined by an XSD URI.
   * @param xsdUri The XSD URI of the numeric type to return.
   * @param str The string to be parsed for the number.
   * @return A {@link Number} representing the given string.
   * @throws NumberFormatException The number could not be parsed into the given type.
   */
  static public Number valueOf(URI xsdUri, String str) {
    NumberStructure<?> st = xsdToNumber.get(xsdUri);
    return (st == null) ? null : st.valueOf(str);
  }

  /**
   * Parses a string representing a number into a numeric type defined by an XSD URI.
   * @param cls a Numeric class to parse the number with.
   * @param str The string to be parsed for the number.
   * @return A {@link Number} representing the given string.
   * @throws NumberFormatException The number could not be parsed into the given type.
   */
  static public Number valueOf(Class<? extends Number> cls, String str) {
    NumberStructure<?> st = classToNumber.get(cls);
    return (st == null) ? null : st.valueOf(str);
  }

  /**
   * Returns The negation of a number, regardless of its type.
   * @param n The number to negate.
   * @return Negative the number, or null if the number was null.
   */
  static public Number minus(Number n) {
    if (n instanceof Long) return new Long(-n.longValue());
    if (n instanceof Integer) return new Integer(-n.intValue());
    if (n instanceof Short) return new Short((short)-n.shortValue());
    if (n instanceof Byte) return new Byte((byte)-n.byteValue());
    if (n instanceof Double) return new Double(-n.doubleValue());
    if (n instanceof Float) return new Float(-n.floatValue());
    return null;
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Internal structures for handling conversion of numeric types to XSD and back
  ///////////////////////////////////////////////////////////////////////////////

  /** A mapping of Java numeric types to XSD numeric types */
  private static Map<Class<? extends Number>,NumberStructure<? extends Number>> classToNumber = new HashMap<Class<? extends Number>,NumberStructure<? extends Number>>();

  /** A mapping of XSD numeric types to Java constructors that can represent those types. */
  private static Map<URI,NumberStructure<? extends Number>> xsdToNumber = new HashMap<URI,NumberStructure<? extends Number>>();

  /** An interface for representing a numeric class, the XSD type and a string-parsing constructor. */
  private static interface NumberStructure<T extends Number> {
    public URI getURI();
    public Class<T> getType();
    public Number valueOf(String str);
  }

  /**
   * Utility for extracting the information out of a number structure for mapping Java and XSD types
   * to one another.
   * @param nmb The number structure to get the appropriate mapping information.
   */
  private static void mapNumber(NumberStructure<?> nmb) {
    classToNumber.put(nmb.getType(), nmb);
    xsdToNumber.put(nmb.getURI(), nmb);
  }

  // Map info for each numeric type to that type's structure
  static {
    mapNumber(new ByteStruct());
    mapNumber(new ShortStruct());
    mapNumber(new IntegerStruct());
    mapNumber(new LongStruct());
    mapNumber(new BigIntegerStruct());
    mapNumber(new FloatStruct());
    mapNumber(new DoubleStruct());
    mapNumber(new BigDecimalStruct());
  }

  // Implementations of NumerStructtructor follow

  private static class ByteStruct implements NumberStructure<Byte> {
    public URI getURI() { return BYTE_URI; }
    public Class<Byte> getType() { return Byte.class; }
    public Number valueOf(String str) { return Byte.valueOf(str); }
  }

  private static class ShortStruct implements NumberStructure<Short> {
    public URI getURI() { return SHORT_URI; }
    public Class<Short> getType() { return Short.class; }
    public Number valueOf(String str) { return Short.valueOf(str); }
  }

  private static class IntegerStruct implements NumberStructure<Integer> {
    public URI getURI() { return INT_URI; }
    public Class<Integer> getType() { return Integer.class; }
    public Number valueOf(String str) { return Integer.valueOf(str); }
  }

  private static class LongStruct implements NumberStructure<Long> {
    public URI getURI() { return LONG_URI; }
    public Class<Long> getType() { return Long.class; }
    public Number valueOf(String str) { return Long.valueOf(str); }
  }

  private static class BigIntegerStruct implements NumberStructure<BigInteger> {
    public URI getURI() { return INTEGER_URI; }
    public Class<BigInteger> getType() { return BigInteger.class; }
    public Number valueOf(String str) { return new BigInteger(str); }
  }

  private static class FloatStruct implements NumberStructure<Float> {
    public URI getURI() { return FLOAT_URI; }
    public Class<Float> getType() { return Float.class; }
    public Number valueOf(String str) { return Float.valueOf(str); }
  }

  private static class DoubleStruct implements NumberStructure<Double> {
    public URI getURI() { return DOUBLE_URI; }
    public Class<Double> getType() { return Double.class; }
    public Number valueOf(String str) { return Double.valueOf(str); }
  }

  private static class BigDecimalStruct implements NumberStructure<BigDecimal> {
    public URI getURI() { return DECIMAL_URI; }
    public Class<BigDecimal> getType() { return BigDecimal.class; }
    public Number valueOf(String str) { return new BigDecimal(str); }
  }

  ////////////////////////
  // The XSD numeric types
  ////////////////////////

  /** The namespace for XSD data. */
  public final static String XSD_NS = "http://www.w3.org/2001/XMLSchema#";

  /** URI for the XML Schema <code>xsd:float</code> datatype; */
  public final static URI FLOAT_URI = URI.create(XSD_NS + "float");

  /** URI for the XML Schema <code>xsd:double</code> datatype; */
  public final static URI DOUBLE_URI = URI.create(XSD_NS + "double");

  /** URI for the XML Schema <code>xsd:decimal</code> datatype. */
  public final static URI DECIMAL_URI = URI.create(XSD_NS + "decimal");

  /** URI for the XML Schema <code>integer</code> datatype. Subtype of {@link #DECIMAL_URI}. */
  public final static URI INTEGER_URI = URI.create(XSD_NS + "integer");

  /** URI for the XML Schema <code>long</code> datatype. Subtype of {@link #INTEGER_URI}. */
  public final static URI LONG_URI = URI.create(XSD_NS + "long");

  /** URI for the XML Schema <code>int</code> datatype. Subtype of {@link #LONG_URI}. */
  public final static URI INT_URI = URI.create(XSD_NS + "int");

  /** URI for the XML Schema <code>short</code> datatype. Subtype of {@link #INT_URI}. */
  public final static URI SHORT_URI = URI.create(XSD_NS + "short");

  /** URI for the XML Schema <code>byte</code> datatype. Subtype of {@link #SHORT_URI}. */
  public final static URI BYTE_URI = URI.create(XSD_NS + "byte");


}
