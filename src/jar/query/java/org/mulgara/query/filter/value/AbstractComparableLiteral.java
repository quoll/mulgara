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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.RDFTerm;
import org.mulgara.query.rdf.XSD;


/**
 * Basic common representation of literals.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractComparableLiteral extends AbstractComparable implements ValueLiteral {

  /** Serialization ID */
  private static final long serialVersionUID = 8196102019234790023L;

  /** The wrapped value */
  Object value;

  /**
   * Creates a value
   * @param value The value to use
   */
  public AbstractComparableLiteral(Object value) {
    this.value = value;
  }

  /**
   * Returns the wrapped data for this value.
   * @return The wrapped data.
   */
  public Object getValue() {
    return value;
  }

  /**
   * {@inheritDoc}
   * Override this if a tagged string.
   */
  public SimpleLiteral getLang() throws QueryException {
    return SimpleLiteral.EMPTY;
  }

  /** {@inheritDoc} */
  public boolean isBlank() { return false; }

  /** {@inheritDoc} */
  public boolean isIRI() { return false; }

  /** {@inheritDoc} */
  public boolean isURI() { return false; }

  /** {@inheritDoc} */
  public boolean isLiteral() { return true; }

  /** {@inheritDoc} */
  public boolean sameTerm(RDFTerm v) throws QueryException {
    if (!v.isLiteral()) return false;
    return comparableLiteralTypes((ValueLiteral)v) && getValue().equals(v.getValue());
  }

  /**
   * {@inheritDoc}
   * This method will only return <code>true</code> when the elements are identical.
   * Since this object is a literal, then an incorrect comparison will throw an exception.
   * See: <a href="http://www.w3.org/TR/rdf-sparql-query/#func-RDFterm-equal">http://www.w3.org/TR/rdf-sparql-query/#func-RDFterm-equal</a>
   * <em>produces a type error if the arguments are both literal but are not the same RDF term</em>
   */
  public boolean equals(RDFTerm v) throws QueryException {
    if (!v.isLiteral()) return false;
    // numbers are compared differently
    if (isNumber(v) && isNumber(this)) return numericEquals(v);

    // simple strings can do the comparison for us. This object cannot be simple,
    // otherwise this method would not have been called.
    if (((ValueLiteral)v).isSimple()) return v.equals(this);

    // if the types allow for direct comparisons, then do so
    if (directlyComparableTypes(v)) return getValue().equals(v.getValue());

    // This is now RDFterm-equal, return true or an error
    // compare for type equality, then value equality
    if (comparableLiteralTypes(v) && getValue().equals(v.getValue())) return true;
    throw new QueryException("Type Error: Terms are not equal");
  }


  /**
   * Indicates that an direct compare operation is valid on these types. This is defined
   * to be valid for:
   * <ul>
   *   <li>booleans</li>
   *   <li>dateTimes</li>
   *   <li>XSD strings</li>
   * </ul>
   * Simple strings and numbers are handled elsewhere.
   * @param term The other value literal to be compared against.
   * @return <code>true</code> if direct comparison is allowed between these types.
   * @throws QueryException If there is a data error accessing the types.
   */
  private boolean directlyComparableTypes(RDFTerm term) throws QueryException {
    ValueLiteral vl = (ValueLiteral)term;
    IRI otherType = vl.getType();
    IRI thisType = getType();
    assert otherType != null && thisType != null;
    // if differing types, then can't be compared
    if (!thisType.equals(otherType)) return false;
    URI tt = thisType.getValue();
    return tt.equals(XSD.BOOLEAN_URI) || tt.equals(XSD.DATE_TIME_URI) || thisType.equals(SimpleLiteral.STRING_TYPE);
  }


  /**
   * Compares the type of this object to the type of another object. This takes into account
   * that Simple Literals claim to have a string type, when they have no type at all.
   * @param term The object to test.
   * @return <code>true</code> if the types are exactly the same. If both types are strings,
   *   then both objects have to be typed literals, or untyped literals.
   * @throws QueryException If there is an error accessing the type data.
   */
  private boolean comparableLiteralTypes(RDFTerm term) throws QueryException {
    ValueLiteral vl = (ValueLiteral)term;
    IRI opType = vl.getType();
    IRI thisType = getType();
    assert opType != null && thisType != null;
    // if the types differ, then the literals are definitely not equal
    return opType.equals(thisType);
  }


  /**
   * Extended numerical comparison function. Currently unused.
   * @param v The term to compare against.
   * @return <code>true</code> if this compares against v with semantic equivalence, regardless of lexical equivalence
   * @throws QueryException Thrown when a value cannot be resolved, or if the types are no numbers.
   */
  private boolean numericEquals(RDFTerm v) throws QueryException {
    Object ov = v.getValue();
    if (!(value instanceof Number) || !(ov instanceof Number)) throw new QueryException("Terms are not equal");
    return compare(value, ov) == 0;
  }

  /**
   * Type-based switching to handle comparison of things that may not be directly comparable.
   * @param left The first thing to compare
   * @param right The second thing to compare
   * @return -1 if left<right, +1 if left>right, and 0 if left=right
   * @throws QueryException The data could not be compared.
   */
  protected int compare(Object left, Object right) throws QueryException {
    DataCompare cmpFn = typeMap.get(left.getClass());
    if (cmpFn == null) throw new QueryException("Type Error: Cannot compare a " + left.getClass() + " to a " + right.getClass());
    return cmpFn.compare(left, right);
  }


  /**
   * Utility to test a literal to see if it is a numeric type. Accepts an RDFTerm for convenience.
   * @param t A ValueLiteral to test to see if it is a number.
   * @return <code>true</code> if the term is a number.
   * @throws QueryException If there was an error accessing the value of the term.
   */
  private static final boolean isNumber(RDFTerm t) throws QueryException {
    return NumericLiteral.isNumeric(((ValueLiteral)t).getType().getValue());
  }


  /** Map of class types to the functions used to compare those types */
  protected static Map<Class<? extends Comparable<?>>,DataCompare> typeMap = new HashMap<Class<? extends Comparable<?>>,DataCompare>();

  static {
    typeMap.put(String.class, new StringCompare());
    typeMap.put(Date.class, new DateCompare());
    typeMap.put(Boolean.class, new BooleanCompare());
    typeMap.put(Float.class, new FloatCompare());
    typeMap.put(Double.class, new DoubleCompare());
    typeMap.put(Long.class, new IntegralCompare());
    typeMap.put(Integer.class, new IntegralCompare());
    typeMap.put(Short.class, new IntegralCompare());
    typeMap.put(Byte.class, new IntegralCompare());
    typeMap.put(BigDecimal.class, new BigDecimalCompare());
  }

  /** Defines a function for comparing objects of arbitrary type */
  protected interface DataCompare {
    /**
     * Comparison method used for any kind of type that might be compared.
     * @param left The left hand side of the comparison. This must be of the correct type for the class.
     * @param right The right hand side of the comparison. This should be tested for type compatibility with the left parameter
     * @return -1 if left<right, 1 if left>right, and 0 if left==right
     * @throws QueryException Due to an error in resolution of the values to be compared
     */
    int compare(Object left, Object right) throws QueryException;
    /**
     * Creates a new ValueLiteral compatible with this comparison type, from given data
     * @param data The data in the correct native {@link java.lang.Class} to be converted.
     * @return A new literal containing the data.
     */
    ValueLiteral newLiteral(Object data);
  }

  /** Implements string comparisons */
  private static class StringCompare implements DataCompare {
    public int compare(Object left, Object right) throws QueryException {
      if (!(right instanceof String)) throw new QueryException("Type Error: Cannot compare a String to a: " + right.getClass());
      return ((String)left).compareTo((String)right);
    }
    public ValueLiteral newLiteral(Object data) { return new TypedLiteral((String)data, SimpleLiteral.STRING_TYPE.getValue()); }
  }

  /** Implements string comparisons */
  private static class DateCompare implements DataCompare {
    public int compare(Object left, Object right) throws QueryException {
      if (!(right instanceof Date)) throw new QueryException("Type Error: Cannot compare a Date to a: " + right.getClass());
      return ((Date)left).compareTo((Date)right);
    }
    public ValueLiteral newLiteral(Object data) { return new DateTime((Date)data); }
  }

  /** Implements boolean comparisons */
  private static class BooleanCompare implements DataCompare {
    public int compare(Object left, Object right) throws QueryException {
      if (!(right instanceof Boolean)) throw new QueryException("Type Error: Cannot compare a boolean to a: " + right.getClass());
      return ((Boolean)left).compareTo((Boolean)right);
    }
    public ValueLiteral newLiteral(Object data) { return new Bool((Boolean)data); }
  }

  /** Implements floating point comparisons, or double comparisons if the rhs parameter is a double */
  private static class FloatCompare implements DataCompare {
    public int compare(Object left, Object right) throws QueryException {
      Float fleft = (Float)left;
      if (!(right instanceof Number)) throw new QueryException("Type Error: Cannot compare a float to a: " + right.getClass());
      // if right has more precision, then promote lfloat, and compare the other way around
      if (right instanceof Double) return -((Double)right).compareTo(fleft.doubleValue());
      return fleft.compareTo(((Number)right).floatValue());
    }
    public ValueLiteral newLiteral(Object data) { return new NumericLiteral((Float)data); }
  }

  /** Implements double precision floating point comparisons */
  private static class DoubleCompare implements DataCompare {
    public int compare(Object left, Object right) throws QueryException {
      if (!(right instanceof Number)) throw new QueryException("Type Error: Cannot compare a double to a: " + right.getClass());
      return ((Double)left).compareTo(((Number)right).doubleValue());
    }
    public ValueLiteral newLiteral(Object data) { return new NumericLiteral((Double)data); }
  }

  /** Implements integer comparisons */
  private static class IntegralCompare implements DataCompare {
    public int compare(Object left, Object right) throws QueryException {
      if (!(right instanceof Number)) throw new QueryException("Type Error: Cannot compare a decimal number to a: " + right.getClass());
      Long lleft = ((Number)left).longValue();
      return lleft.compareTo(((Number)right).longValue());
    }
    public ValueLiteral newLiteral(Object data) { return new NumericLiteral((Number)data); }
  }

  /** Implements big-decimal comparisons */
  private static class BigDecimalCompare implements DataCompare {
    public int compare(Object left, Object right) throws QueryException {
      if (!(right instanceof Number)) throw new QueryException("Type Error: Cannot compare a decimal number to a: " + right.getClass());
      BigDecimal bleft = (BigDecimal)left;
      if (right instanceof BigDecimal) return bleft.compareTo((BigDecimal)right);
      if (right instanceof Double || right instanceof Float) {
        return bleft.compareTo(BigDecimal.valueOf(((Number)right).doubleValue()));
      }
      return bleft.compareTo(BigDecimal.valueOf(((Number)right).longValue()));
    }
    public ValueLiteral newLiteral(Object data) { return new NumericLiteral((Number)data); }
  }

}
