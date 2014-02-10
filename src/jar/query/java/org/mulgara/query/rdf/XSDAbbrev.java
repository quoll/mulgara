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

package org.mulgara.query.rdf;

// Java 2 standard packages
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a hack to take into account bad data that sometimes arrives with
 * references to the XSD namespace, but no declaration of it..
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/">XML Schema Part 2: Datatypes</a>
 * @created 2008-04-29
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class XSDAbbrev {

  /**
   * XML namespace prefix for XML Schema datatypes:
   * <code>http://www.w3.org/2001/XMLSchema#</code>.
   *
   * Note that this does not represent a declared namespace, because if it
   * did then this class would not be needed.
   */
  public final static String NAMESPACE = "xsd:";

  /** URI for the XML Schema <code>xsd:string</code> datatype. */
  public final static URI STRING_URI = URI.create(NAMESPACE + "string");

  ///////////////////////
  // Start of Numeric types
  ///////////////////////

  /** URI for the XML Schema <code>xsd:float</code> datatype; */
  public final static URI FLOAT_URI = URI.create(NAMESPACE + "float");

  /** URI for the XML Schema <code>xsd:double</code> datatype; */
  public final static URI DOUBLE_URI = URI.create(NAMESPACE + "double");

  /** URI for the XML Schema <code>xsd:decimal</code> datatype. */
  public final static URI DECIMAL_URI = URI.create(NAMESPACE + "decimal");

  /** URI for the XML Schema <code>integer</code> datatype. Subtype of {@link #DECIMAL_URI}. */
  public final static URI INTEGER_URI = URI.create(NAMESPACE + "integer");

  /** URI for the XML Schema <code>nonPositiveInteger</code> datatype. Subtype of {@link #INTEGER_URI}. */
  public final static URI NON_POSITIVE_INTEGER_URI = URI.create(NAMESPACE + "nonPositiveInteger");

  /** URI for the XML Schema <code>negativeInteger</code> datatype. Subtype of {@link #NON_POSITIVE_INTEGER_URI}. */
  public final static URI NEGATIVE_INTEGER_URI = URI.create(NAMESPACE + "negativeInteger");

  /** URI for the XML Schema <code>long</code> datatype. Subtype of {@link #INTEGER_URI}. */
  public final static URI LONG_URI = URI.create(NAMESPACE + "long");

  /** URI for the XML Schema <code>int</code> datatype. Subtype of {@link #LONG_URI}. */
  public final static URI INT_URI = URI.create(NAMESPACE + "int");

  /** URI for the XML Schema <code>short</code> datatype. Subtype of {@link #INT_URI}. */
  public final static URI SHORT_URI = URI.create(NAMESPACE + "short");

  /** URI for the XML Schema <code>byte</code> datatype. Subtype of {@link #SHORT_URI}. */
  public final static URI BYTE_URI = URI.create(NAMESPACE + "byte");

  /** URI for the XML Schema <code>nonNegativeInteger</code> datatype. Subtype of {@link #INTEGER_URI}. */
  public final static URI NON_NEGATIVE_INTEGER_URI = URI.create(NAMESPACE + "nonNegativeInteger");

  /** URI for the XML Schema <code>positiveInteger</code> datatype. Subtype of {@link #NON_NEGATIVE_INTEGER_URI}. */
  public final static URI POSITIVE_INTEGER_URI = URI.create(NAMESPACE + "positiveInteger");

  /** URI for the XML Schema <code>unsignedLong</code> datatype. Subtype of {@link #NON_NEGATIVE_INTEGER_URI}. */
  public final static URI UNSIGNED_LONG_URI = URI.create(NAMESPACE + "unsignedLong");

  /** URI for the XML Schema <code>unsignedInt</code> datatype. Subtype of {@link #UNSIGNED_LONG_URI}. */
  public final static URI UNSIGNED_INT_URI = URI.create(NAMESPACE + "unsignedInt");

  /** URI for the XML Schema <code>unsignedShort</code> datatype. Subtype of {@link #UNSIGNED_INT_URI}. */
  public final static URI UNSIGNED_SHORT_URI = URI.create(NAMESPACE + "unsignedShort");

  /** URI for the XML Schema <code>unsignedByte</code> datatype. Subtype of {@link #UNSIGNED_SHORT_URI}. */
  public final static URI UNSIGNED_BYTE_URI = URI.create(NAMESPACE + "unsignedByte");

  /** The set of all numeric types/ */
  static final private Set<URI> numericTypes = new HashSet<URI>();

  /**
   * Tests if a URI is for a numeric type.
   * @param type The URI of the type to test.
   * @return <code>true</code> iff the type is an XSD numeric type.
   */
  public static final boolean isNumericType(URI type) {
    return numericTypes.contains(type);
  }

  // Initialize the set of number types
  static {
    numericTypes.add(FLOAT_URI);
    numericTypes.add(DOUBLE_URI);
    numericTypes.add(DECIMAL_URI);
    numericTypes.add(INTEGER_URI);
    numericTypes.add(NON_POSITIVE_INTEGER_URI);
    numericTypes.add(NEGATIVE_INTEGER_URI);
    numericTypes.add(LONG_URI);
    numericTypes.add(INT_URI);
    numericTypes.add(SHORT_URI);
    numericTypes.add(BYTE_URI);
    numericTypes.add(NON_NEGATIVE_INTEGER_URI);
    numericTypes.add(POSITIVE_INTEGER_URI);
    numericTypes.add(UNSIGNED_LONG_URI);
    numericTypes.add(UNSIGNED_INT_URI);
    numericTypes.add(UNSIGNED_SHORT_URI);
    numericTypes.add(UNSIGNED_BYTE_URI);
  }
  ///////////////////////
  // End of Numeric types
  ///////////////////////

  /** URI for the XML Schema <code>xsd:date</code> datatype. */
  public final static URI DATE_URI = URI.create(NAMESPACE + "date");

  /** URI for the XML Schema <code>xsd:dateTime</code> datatype. */
  public final static URI DATE_TIME_URI = URI.create(NAMESPACE + "dateTime");

  /** URI for the XML Schema <code>xsd:gYearMonth</code> datatype; */
  public final static URI GYEARMONTH_URI = URI.create(NAMESPACE + "gYearMonth");

  /** URI for the XML Schema <code>xsd:gYear</code> datatype; */
  public final static URI GYEAR_URI = URI.create(NAMESPACE + "gYear");

  /** URI for the XML Schema <code>xsd:gMonthDay</code> datatype; */
  public final static URI GMONTHDAY_URI = URI.create(NAMESPACE + "gMonthDay");

  /** URI for the XML Schema <code>xsd:gDay</code> datatype; */
  public final static URI GDAY_URI = URI.create(NAMESPACE + "gDay");

  /** URI for the XML Schema <code>xsd:gMonth</code> datatype; */
  public final static URI GMONTH_URI = URI.create(NAMESPACE + "gMonth");

  /** URI for the XML Schema <code>xsd:boolean</code> datatype; */
  public final static URI BOOLEAN_URI = URI.create(NAMESPACE + "boolean");

  /** URI for the XML Schema <code>xsd:hexBinary</code> datatype; */
  public final static URI HEX_BINARY_URI = URI.create(NAMESPACE + "hexBinary");

  /** URI for the XML Schema <code>xsd:base64Binary</code> datatype; */
  public final static URI BASE64_BINARY_URI = URI.create(NAMESPACE + "base64Binary");

}
