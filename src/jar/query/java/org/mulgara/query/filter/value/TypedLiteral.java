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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.ContextOwner;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.XSD;
import org.mulgara.query.rdf.XSDAbbrev;


/**
 * A literal with a URI type and a value.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class TypedLiteral extends AbstractComparableLiteral {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = -6070455650703063913L;

  /** The logger */
  private final static Logger logger = Logger.getLogger(TypedLiteral.class.getName());

  /** The type URI for this literal */
  private URI type;
  
  /** The lexical representation for this literal */
  private String lexical;

  /**
   * Creates the value to wrap the string
   * @param value The data to wrap
   * @param type The type of the literal
   */
  public TypedLiteral(Object value, URI type) {
    super(value);
    lexical = value.toString();  // lexical == value if value instanceof String
    this.type = type;
  }

  /**
   * A factory for typed literals.
   * @param value The literal data in its lexical form. This means it's a String, and can even be invalid
   * @param type The type of the literal. May be null.
   * @param lang The language code of the literal. May be null.
   */
  public static ValueLiteral newLiteral(String value, URI type, String lang) throws QueryException {
    if (type != null) {
      // get the info registered for this type URI
      TypeInfo info = infoMap.get(type);
      if (info != null) return info.newLiteral(info.toData(value), normalizeType(type));
      // no type info for the given URI, just pass through as a general typed literal
      return new TypedLiteral(value, type);
    }
    // no type info provided, so it's a simple string
    if (lang == null || lang.length() == 0) return new SimpleLiteral(value);
    return new SimpleLiteral(value, lang);
  }

  /**
   * A factory for typed literals from raw Java types. This is most likely to come
   * from literal numbers parsed by a SPARQL parser.
   * @param value The data as an object. May be a String, or some kind of {@link java.lang.Number}
   */
  public static ValueLiteral newLiteral(Object value) throws QueryException {
    DataCompare dc = typeMap.get(value.getClass());
    if (dc == null) {
      logger.info("Unrecognized data type: " + value.getClass().getSimpleName());
      return new SimpleLiteral(value.toString());
    }
    return dc.newLiteral(value);
  }

  /**
   * Gets the type of this literal
   * @return The URI for this literals type
   */
  public IRI getType() {
    return new IRI(type);
  }

  /** @see org.mulgara.query.filter.value.ValueLiteral#isSimple() */
  public boolean isSimple() throws QueryException {
    return false;
  }

  /** {@inheritDoc} */
  public boolean isGrounded() throws QueryException {
    return true;
  }

  /**
   * Gets the language ID of this literal
   * @return The language ID for this literal
   */
  public SimpleLiteral getLang() {
    return SimpleLiteral.EMPTY;
  }
  
  /** {@inheritDoc} */
  public String getLexical() {
    return lexical;
  }

  /**
   * No context needed as this is a literal value.
   * @see org.mulgara.query.filter.RDFTerm#getContextOwner()
   */
  public ContextOwner getContextOwner() {
    return null;
  }

  /**
   * A public string representation of this literal.
   */
  public String toString() {
    return "'" + lexical + "'^^<" + type + ">";
  }

  /**
   * A JRDF Node that represents this literal
   */
  public Node getJRDFValue() throws QueryException {
    return new LiteralImpl(lexical, type);
  }

  /**
   * No context needed as this is a literal value.
   * @see org.mulgara.query.filter.RDFTerm#setContextOwner(org.mulgara.query.filter.ContextOwner)
   */
  public void setContextOwner(ContextOwner owner) { }

  /** {@inheritDoc} */
  public boolean test(Context context) throws QueryException {
    if (type == null) return ((String)value).length() != 0;
    TypeInfo test = infoMap.get(type);
    if (test == null) throw new QueryException("Type Error: no effective boolean value for: " + toString());
    if (NumericLiteral.isNumeric(type)) {
      return (value instanceof Number) && test.ebv((Number)value);
    }
    return test.ebv(value.toString());
  }

  /** @see org.mulgara.query.filter.RDFTerm#getVariables() */
  public Set<Variable> getVariables() {
    return Collections.emptySet();
  }

  /**
   * Converts abbreviated URIs for XSD types into the full URI.
   * @param type The URI of the datatype.
   * @return The full URI of the datatype.
   */
  private static final URI normalizeType(URI type) {
    if (XSD.DOM.equals(type.getScheme())) {
      // fragments won't exist for xsd:....
      type = URI.create(XSD.NAMESPACE + type.getRawSchemeSpecificPart());
    }
    return type;
  }

  ///////////////////////////////////////////////////////////////////////////////////
  // Maps to data specific functionality, and supporting functions
  ///////////////////////////////////////////////////////////////////////////////////
  
  /** A map of XSD datatypes onto the data conversion operations for their types */
  protected static Map<URI,TypeInfo> infoMap = new HashMap<URI,TypeInfo>();

  /** This interface tests if datatype matches the data to give an EBV of <code>true</code> */
  public interface TypeInfo {
    /** Returns an EBV of <code>true</code> iff the data matches the type sufficiently */
    public boolean ebv(String data) throws QueryException;
    /** Returns an EBV of <code>true</code> iff the number is not zero */
    public boolean ebv(Number v) throws QueryException;
    /** Returns data parsed out of the string literal */
    public Object toData(String representation) throws QueryException;
    /** Returns the URI for this type */
    public URI getTypeURI();
    /** Returns the abbreviated URI for this type, in the absence of a namespace. */
    public URI getTypeURI2();
    /**
     * Creates a new ValueLiteral compatible for this data
     * @param data The data in the correct native {@link java.lang.Class} to be converted.
     * @param type The original datatype for the data.
     * @return A new literal containing the data.
     */
    public ValueLiteral newLiteral(Object data, URI type);
    /**
     * Get the value of a given number, according to the type of the implementing class.
     * @param n The number to convert.
     * @return The new Number of the required type.
     */
    public Number valueOf(Number n);
  }

  /** Simple extension to TypeInfo to store the type URI for all implementing classes  */
  private static abstract class AbstractXSD implements TypeInfo  {
    private final URI typeURI;
    private final URI typeURI2;
    protected AbstractXSD(URI type, URI type2) { typeURI = type; typeURI2 = type2; }
    public URI getTypeURI() { return typeURI; }
    public URI getTypeURI2() { return typeURI2; }
    public Number valueOf(Number n) { throw new UnsupportedOperationException("Numeric casts only applicable to numbers"); }
    // public ValueLiteral newLiteral(Object data, URI type) { throw new UnsupportedOperationException(); }
  }

  /**
   * Helper method for static initialization
   * @param info The info to add to the infoMap
   */
  private static void addDefaultTypeInfo(TypeInfo info) {
    infoMap.put(info.getTypeURI(), info);
    infoMap.put(info.getTypeURI2(), info);
  }

  // initialize the types
  static {
    addDefaultTypeInfo(XSDDecimal.INSTANCE);
    addDefaultTypeInfo(XSDString.INSTANCE);
    addDefaultTypeInfo(XSDBoolean.INSTANCE);
    addDefaultTypeInfo(XSDDouble.INSTANCE);
    addDefaultTypeInfo(XSDFloat.INSTANCE);
    addDefaultTypeInfo(XSDLong.INSTANCE);
    addDefaultTypeInfo(XSDInteger.INSTANCE);
    addDefaultTypeInfo(XSDShort.INSTANCE);
    addDefaultTypeInfo(XSDByte.INSTANCE);
    addDefaultTypeInfo(XSDDate.INSTANCE);
    infoMap.put(XSD.INTEGER_URI, XSDLong.INSTANCE);
    infoMap.put(XSD.NON_POSITIVE_INTEGER_URI, XSDLong.INSTANCE);
    infoMap.put(XSD.NEGATIVE_INTEGER_URI, XSDLong.INSTANCE);
    infoMap.put(XSD.NON_NEGATIVE_INTEGER_URI, XSDLong.INSTANCE);
    infoMap.put(XSD.POSITIVE_INTEGER_URI, XSDInteger.INSTANCE);
    infoMap.put(XSD.UNSIGNED_LONG_URI, XSDLong.INSTANCE);
    infoMap.put(XSD.UNSIGNED_INT_URI, XSDLong.INSTANCE);
    infoMap.put(XSD.UNSIGNED_SHORT_URI, XSDInteger.INSTANCE);
    infoMap.put(XSD.UNSIGNED_BYTE_URI, XSDShort.INSTANCE);
    infoMap.put(XSDAbbrev.INTEGER_URI, XSDLong.INSTANCE);
    infoMap.put(XSDAbbrev.NON_POSITIVE_INTEGER_URI, XSDLong.INSTANCE);
    infoMap.put(XSDAbbrev.NEGATIVE_INTEGER_URI, XSDLong.INSTANCE);
    infoMap.put(XSDAbbrev.NON_NEGATIVE_INTEGER_URI, XSDLong.INSTANCE);
    infoMap.put(XSDAbbrev.POSITIVE_INTEGER_URI, XSDInteger.INSTANCE);
    infoMap.put(XSDAbbrev.UNSIGNED_LONG_URI, XSDLong.INSTANCE);
    infoMap.put(XSDAbbrev.UNSIGNED_INT_URI, XSDLong.INSTANCE);
    infoMap.put(XSDAbbrev.UNSIGNED_SHORT_URI, XSDInteger.INSTANCE);
    infoMap.put(XSDAbbrev.UNSIGNED_BYTE_URI, XSDShort.INSTANCE);
  }

  //////////////////////////////////////////////////////////////////
  // Implementing classes
  //////////////////////////////////////////////////////////////////

  static class XSDString extends AbstractXSD {
    public static final XSDString INSTANCE = new XSDString();
    private XSDString() { super(XSD.STRING_URI, XSDAbbrev.STRING_URI); }
    public boolean ebv(String data) { return data != null && data.length() != 0; }
    public boolean ebv(Number data) { throw new IllegalArgumentException("Found a number in a string operation"); }
    public Object toData(String r) { return r; }
    public ValueLiteral newLiteral(Object data, URI type) { return new TypedLiteral((String)data, getTypeURI()); }
  }

  private static class XSDBoolean extends AbstractXSD {
    public static final XSDBoolean INSTANCE = new XSDBoolean();
    private XSDBoolean() { super(XSD.BOOLEAN_URI, XSDAbbrev.BOOLEAN_URI); }
    public boolean ebv(String data) { return Boolean.parseBoolean(data); }
    public boolean ebv(Number data) { throw new IllegalArgumentException("Found a number in a boolean operation"); }
    public Object toData(String r) { return Boolean.parseBoolean(r); }
    public ValueLiteral newLiteral(Object data, URI type) { return new Bool((Boolean)data); }
  }
  
  private static class XSDDecimal extends AbstractXSD {
    public static final XSDDecimal INSTANCE = new XSDDecimal();
    private XSDDecimal() { super(XSD.DECIMAL_URI, XSDAbbrev.DECIMAL_URI); }
    public boolean ebv(String data) {
      try {
        if (data == null) return false;
        BigDecimal d = new BigDecimal(data);
        return !BigDecimal.ZERO.equals(d);
      } catch (NumberFormatException nfe) {
        return false;
      }
    }
    public boolean ebv(Number data) {
      return !BigDecimal.ZERO.equals(((BigDecimal)data));
    }
    public Object toData(String r) throws QueryException {
      try {
        return new BigDecimal(r);
      } catch (NumberFormatException nfe) {
        throw new QueryException("Type Error: Cannot convert to a Decimal: " + r);
      }
    }
    public ValueLiteral newLiteral(Object data, URI type) {
      if (data instanceof Double) return new NumericLiteral((Double)data, type);
      if (data instanceof Long) return new NumericLiteral((Long)data, type);
      return new NumericLiteral((BigDecimal)data, type);
    }
    public Number valueOf(Number n) { return n.doubleValue(); }
  }
  
  private static class XSDDouble extends AbstractXSD {
    public static final XSDDouble INSTANCE = new XSDDouble();
    private XSDDouble() { super(XSD.DOUBLE_URI, XSDAbbrev.DOUBLE_URI); }
    public boolean ebv(String data) {
      try {
        if (data == null) return false;
        Double d = Double.parseDouble(data);
        return 0 != Double.parseDouble(data) && !d.isNaN();
      } catch (NumberFormatException nfe) {
        return false;
      }
    }
    public boolean ebv(Number data) { return data.doubleValue() != 0.0D; }
    public Object toData(String r) throws QueryException {
      try {
        return Double.parseDouble(r);
      } catch (NumberFormatException nfe) {
        throw new QueryException("Type Error: Cannot convert to a Double: " + r);
      }
    }
    public ValueLiteral newLiteral(Object data, URI type) {
      return new NumericLiteral((Double)data, type);
    }
    public Number valueOf(Number n) { return n.doubleValue(); }
  }
  
  private static class XSDFloat extends AbstractXSD {
    public static final XSDFloat INSTANCE = new XSDFloat();
    private XSDFloat() { super(XSD.FLOAT_URI, XSDAbbrev.FLOAT_URI); }
    public boolean ebv(String data) {
      try {
        if (data == null) return false;
        Float f = Float.parseFloat(data);
        return 0 != f && !f.isNaN();
      } catch (NumberFormatException nfe) {
        return false;
      }
    }
    public boolean ebv(Number data) { return data.floatValue() != 0.0; }
    public Object toData(String r) throws QueryException {
      try {
        return Float.parseFloat(r);
      } catch (NumberFormatException nfe) {
        throw new QueryException("Type Error: Cannot convert to a Float: " + r);
      }
    }
    public ValueLiteral newLiteral(Object data, URI type) {
      return new NumericLiteral((Float)data, type);
    }
    public Number valueOf(Number n) { return n.floatValue(); }
  }

  private static class XSDLong extends AbstractXSD {
    public static final XSDLong INSTANCE = new XSDLong();
    private XSDLong() { super(XSD.LONG_URI, XSDAbbrev.LONG_URI); }
    public boolean ebv(String data) {
      try {
        return data != null && 0 != Long.parseLong(data);
      } catch (NumberFormatException nfe) {
        return false;
      }
    }
    public boolean ebv(Number data) { return data.longValue() != 0L; }
    public Object toData(String r) throws QueryException {
      try {
        return Long.parseLong(r);
      } catch (NumberFormatException nfe) {
        throw new QueryException("Type Error: Cannot convert to a Long: " + r);
      }
    }
    public ValueLiteral newLiteral(Object data, URI type) {
      return new NumericLiteral((Long)data, type);
    }
    public Number valueOf(Number n) { return n.longValue(); }
  }

  private static class XSDInteger extends AbstractXSD {
    public static final XSDInteger INSTANCE = new XSDInteger();
    private XSDInteger() { super(XSD.INT_URI, XSDAbbrev.INT_URI); }
    public boolean ebv(String data) {
      try {
        return data != null && 0 != Integer.parseInt(data);
      } catch (NumberFormatException nfe) {
        return false;
      }
    }
    public boolean ebv(Number data) { return data.intValue() != 0; }
    public Object toData(String r) throws QueryException {
      try {
        return Integer.parseInt(r);
      } catch (NumberFormatException nfe) {
        throw new QueryException("Type Error: Cannot convert to an Integer: " + r);
      }
    }
    public ValueLiteral newLiteral(Object data, URI type) {
      return new NumericLiteral((Integer)data, type);
    }
    public Number valueOf(Number n) { return n.intValue(); }
  }

  private static class XSDShort extends AbstractXSD {
    public static final XSDShort INSTANCE = new XSDShort();
    private XSDShort() { super(XSD.SHORT_URI, XSDAbbrev.SHORT_URI); }
    public boolean ebv(String data) {
      try {
        return data != null && 0 != Short.parseShort(data);
      } catch (NumberFormatException nfe) {
        return false;
      }
    }
    public boolean ebv(Number data) { return data.shortValue() != 0; }
    public Object toData(String r) throws QueryException {
      try {
        return Short.parseShort(r);
      } catch (NumberFormatException nfe) {
        throw new QueryException("Type Error: Cannot convert to a Short: " + r);
      }
    }
    public ValueLiteral newLiteral(Object data, URI type) {
      return new NumericLiteral((Short)data, type);
    }
    public Number valueOf(Number n) { return n.shortValue(); }
  }

  private static class XSDByte extends AbstractXSD {
    public static final XSDByte INSTANCE = new XSDByte();
    private XSDByte() { super(XSD.BYTE_URI, XSDAbbrev.BYTE_URI); }
    public boolean ebv(String data) {
      try {
        return data != null && 0 != Byte.parseByte(data);
      } catch (NumberFormatException nfe) {
        return false;
      }
    }
    public boolean ebv(Number data) { return data.byteValue() != 0; }
    public Object toData(String r) throws QueryException {
      try {
        return Byte.parseByte(r);
      } catch (NumberFormatException nfe) {
        throw new QueryException("Type Error: Cannot convert to a Byte: " + r);
      }
    }
    public ValueLiteral newLiteral(Object data, URI type) {
      return new NumericLiteral((Byte)data, type);
    }
    public Number valueOf(Number n) { return n.byteValue(); }
  }

  private static class XSDDate extends AbstractXSD {
    public static final XSDDate INSTANCE = new XSDDate();
    private XSDDate() { super(XSD.DATE_TIME_URI, XSDAbbrev.DATE_TIME_URI); }
    public boolean ebv(String data) throws QueryException { throw new QueryException("Unable to convert a date to a boolean"); }
    public boolean ebv(Number data) throws QueryException { throw new QueryException("Unable to convert a date to a boolean"); }
    public Object toData(String r) { return DateTime.parseDate(r); }
    public ValueLiteral newLiteral(Object data, URI type) { return new DateTime((Date)data); }
  }

}
