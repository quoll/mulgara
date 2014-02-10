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

import java.util.Collections;
import java.util.Set;

import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.ContextOwner;
import org.mulgara.query.filter.RDFTerm;
import org.mulgara.query.rdf.LiteralImpl;

import static org.mulgara.query.rdf.XSD.STRING_URI;


/**
 * A string value.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SimpleLiteral extends AbstractComparableLiteral {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = -776959229628368456L;

  /** An empty simple literal */
  public static final SimpleLiteral EMPTY = new SimpleLiteral("", "");

  /** The language tag for a simple literal */
  private SimpleLiteral lang = EMPTY;

  /** The type used for strings */
  public static final IRI STRING_TYPE = new IRI(STRING_URI);

  /**
   * Creates the value to wrap the string
   * @param s The string to wrap
   */
  public SimpleLiteral(String s) {
    super(s);
  }

  /**
   * Creates the value to wrap the string
   * @param s The string to wrap
   */
  public SimpleLiteral(String s, String l) {
    super(s);
    lang = new SimpleLiteral(l);
  }

  /**
   * @see org.mulgara.query.filter.value.AbstractComparableLiteral#getLang()
   */
  public SimpleLiteral getLang() {
    return lang;
  }

  /**
   * @see org.mulgara.query.filter.value.AbstractComparableLiteral#getLang()
   */
  public String getLexical() {
    return value == null ? (String)EMPTY.value : (String)value;
  }

  /**
   * Gets the type of this literal
   * @return Always the string type.
   */
  public IRI getType() {
    return STRING_TYPE;
  }

  /**
   * @see org.mulgara.query.filter.RDFTerm#getJRDFValue()
   */
  public Node getJRDFValue() throws QueryException {
    return lang == EMPTY ? new LiteralImpl(getLexical()) : new LiteralImpl(getLexical(), lang.getLexical());
  }

  /** @see org.mulgara.query.filter.value.ValueLiteral#isSimple() */
  public boolean isSimple() throws QueryException {
    return true;
  }

  /** {@inheritDoc} */
  public boolean isGrounded() throws QueryException {
    return true;
  }

  /**
   * @see org.mulgara.query.filter.value.ComparableExpression#test(Context)
   */
  public boolean test(Context context) throws QueryException {
    return value != null && ((String)value).length() != 0;
  }

  /** This value does not need a context */
  public ContextOwner getContextOwner() {  return null; }

  /** This value does not need a context */
  public void setContextOwner(ContextOwner owner) { }

  /** {@inheritDoc} */
  public boolean sameTerm(RDFTerm v) throws QueryException {
    if (!v.isLiteral() || !((ValueLiteral)v).isSimple()) return false;
    return getValue().equals(v.getValue()) && compareLangEquals((ValueLiteral)v);
  }

  /**
   * {@inheritDoc}
   * This method will only return <code>true</code> when the elements are identical, and
   * <code>false</code> when they are the same type, but different values.
   * Since this object is a literal, then a comparison with another type will throw an exception.
   * See: <a href="http://www.w3.org/TR/rdf-sparql-query/#func-RDFterm-equal">http://www.w3.org/TR/rdf-sparql-query/#func-RDFterm-equal</a>
   * <em>produces a type error if the arguments are both literal but are not the same RDF term</em>
   */
  public boolean equals(RDFTerm v) throws QueryException {
    if (!v.isLiteral()) return false;
    if (((ValueLiteral)v).isSimple()) {
      // check values and language codes
      return getValue().equals(v.getValue()) && compareLangEquals((ValueLiteral)v);
    }
    throw new QueryException("Type Error: Terms are not equal");
  }

  /** {@inheritDoc} */
  public boolean lessThan(ComparableExpression v) throws QueryException {
    if (!v.isLiteral() || !((ValueLiteral)v).isSimple()) throw new QueryException("Type Error: cannot compare a simple literal to a: " + v.getClass().getSimpleName());
    int result = compareLang(convertToLiteral(v));
    if (result == 0) return compare(getValue(), v.getValue()) < 0;
    else return result < 0;
  }

  /** {@inheritDoc} */
  public boolean greaterThan(ComparableExpression v) throws QueryException {
    if (!v.isLiteral() || !((ValueLiteral)v).isSimple()) throw new QueryException("Type Error: cannot compare a simple literal to a: " + v.getClass().getSimpleName());
    int result = compareLang(convertToLiteral(v));
    if (result == 0) return compare(getValue(), v.getValue()) > 0;
    else return result > 0;
  }

  /**
   * Convert a comparable expression to a Literal, or throw an exception if it cannot be resolved this way.
   * @param v The object to convert after testing
   * @return The object as a ValueLiteral
   * @throws QueryException Indicating that v is not a SimpleLiteral.
   */
  private ValueLiteral convertToLiteral(ComparableExpression v) throws QueryException {
    if (!v.isLiteral() || !((ValueLiteral)v).isSimple()) throw new QueryException("Type Error: cannot compare a simple literal to a: " + v.getClass().getSimpleName());
    return (ValueLiteral)v;
  }

  /**
   * Tests equality for the language code to the language code of the given SimpleLiteral.
   * @param sl The simple literal to compare against.
   * @return <code>true</code> if the codes are equal, <code>false</code> otherwise.
   */
  private boolean compareLangEquals(ValueLiteral sl) throws QueryException {
    return lang.equals(sl.getLang());
  }

  /**
   * Compares the language code to the language code of the given SimpleLiteral.
   * @param sl The simple literal to compare against.
   * @return -1 if this code is less than the code of s1, +1 if greater, and 0 if they are equal
   */
  private int compareLang(ValueLiteral sl) throws QueryException {
    return ((String)lang.getValue()).compareTo((String)sl.getLang().getValue());
  }

  /**@see org.mulgara.query.filter.RDFTerm#getVariables() */
  public Set<Variable> getVariables() {
    return Collections.emptySet();
  }

}
