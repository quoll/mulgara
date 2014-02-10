package org.mulgara.query.filter.arithmetic;

import java.util.HashSet;
import java.util.Set;

import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.ContextOwner;
import org.mulgara.query.filter.RDFTerm;
import org.mulgara.query.filter.value.AbstractComparable;
import org.mulgara.query.filter.value.IRI;
import org.mulgara.query.filter.value.NumericExpression;
import org.mulgara.query.filter.value.NumericLiteral;
import org.mulgara.query.filter.value.SimpleLiteral;

/**
 * Implements common functionality for arithmetic operations.
 *
 * @created Apr 10, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractNumericOperation extends AbstractComparable implements NumericExpression {

  /** Serialization ID */
  private static final long serialVersionUID = -6795347129998821301L;

  /** The owner of the context for resolving here */
  private ContextOwner owner = null;

  public AbstractNumericOperation() {
    super();
  }

  /** @see org.mulgara.query.filter.value.ComparableExpression#getValue() */
  public Number getValue() throws QueryException { return getNumber(); }

  /** @see org.mulgara.query.filter.RDFTerm#getJRDFValue() */
  public Node getJRDFValue() throws QueryException {
    return new NumericLiteral(getNumber()).getJRDFValue();
  }

  /** @see org.mulgara.query.filter.RDFTerm#equals(RDFTerm) */
  public boolean equals(RDFTerm v) throws QueryException { return compare(getNumber(), v.getValue()) == 0; }

  /** @see org.mulgara.query.filter.RDFTerm#isBlank() */
  public boolean isBlank() throws QueryException { return false; }

  /** @see org.mulgara.query.filter.RDFTerm#isIRI() */
  public boolean isIRI() throws QueryException { return false; }

  /** @see org.mulgara.query.filter.RDFTerm#isLiteral() */
  public boolean isLiteral() throws QueryException { return true; }

  /** @see org.mulgara.query.filter.RDFTerm#isURI() */
  public boolean isURI() throws QueryException { return false; }

  /** @see org.mulgara.query.filter.RDFTerm#sameTerm(RDFTerm) */
  public boolean sameTerm(RDFTerm v) throws QueryException { return getNumber().equals(v.getValue()); }

  /** @see org.mulgara.query.filter.Filter#test(Context) */
  public boolean test(Context context) throws QueryException {
    setCurrentContext(context);
    return getNumber().doubleValue() != 0.0;
  }

  /** @see org.mulgara.query.filter.RDFTerm#setContextOwner(org.mulgara.query.filter.ContextOwner) */
  public void setContextOwner(ContextOwner owner) {
    this.owner = owner;
    owner.addContextListener(this);
  }

  /** @see org.mulgara.query.filter.RDFTerm#getContextOwner() */
  public ContextOwner getContextOwner() {
    return owner;
  }

  /** @see org.mulgara.query.filter.value.NumericExpression#getNumber() */
  public abstract Number getNumber() throws QueryException;


  //////////////////////////////////////////////////////////////
  // Implementation of AbstractComparable.compare(Object,Object)
  //////////////////////////////////////////////////////////////
  
  /** The set of classes to be compared as floating point */
  private static Set<Class<? extends Number>> floatUpcast = new HashSet<Class<? extends Number>>();
  
  /* Initialize the set of classes to be compared as floating point */
  static {
    floatUpcast.add(Double.class);
    floatUpcast.add(Float.class);
  }

  /**
   * @see org.mulgara.query.filter.value.AbstractComparable#compare(java.lang.Object, java.lang.Object)
   */
  protected int compare(Object left, Object right) throws QueryException {
    return compare((Number)left, right);
  }

  /**
   * Compares 2 numbers of unknown type.  If they are both floating point, then use Double compare,
   * otherwise use Long compare.
   * @param left The first number in the comparison.
   * @param right The second number. This cannot be assumed to be a number due to poorly formed queries.
   * @return -1 if left<right, +1 if left>right, 0 if left==right
   * @throws QueryException if right is a type other than {@link java.lang.Number}.
   */
  protected int compare(Number left, Object right) throws QueryException {
    if (!(right instanceof Number)) throw new QueryException("Cannot compare a number to a: " + right.getClass().getSimpleName());
    if (floatUpcast.contains(left.getClass()) || floatUpcast.contains(right.getClass())) {
      return Double.valueOf(left.doubleValue()).compareTo(((Number)right).doubleValue());
    }
    return Long.valueOf(left.longValue()).compareTo(((Number)right).longValue());
  }


  /////////////////////////////////
  // Implementation of ValueLiteral
  /////////////////////////////////

  /** @see org.mulgara.query.filter.value.ValueLiteral#getLexical() */
  public String getLexical() throws QueryException {
    return getValue().toString();
  }

  /** @see org.mulgara.query.filter.value.ValueLiteral#getLang() */
  public SimpleLiteral getLang() {
    return SimpleLiteral.EMPTY;
  }


  /** @see org.mulgara.query.filter.value.ValueLiteral#getType() */
  public IRI getType() throws QueryException {
    return NumericLiteral.getTypeFor(getValue());
  }

  /** @see org.mulgara.query.filter.value.ValueLiteral#isSimple() */
  public boolean isSimple() throws QueryException {
    return false;
  }

}