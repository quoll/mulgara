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

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.RDFTerm;

/**
 * Represents a datatype() function that accesses the datatype on a literal.
 * This operation is made to look like an IRI.
 *
 * @created Mar 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class DataTypeFn extends AbstractAccessorFn {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 5326464040907090543L;

  /**
   * Create a new language extractor function.
   * @param operand The operand for the function.
   */
  public DataTypeFn(RDFTerm operand) {
    super(operand);
  }

  /** @see org.mulgara.query.filter.RDFTerm#isLiteral() */
  public boolean isLiteral() throws QueryException {
    return false;
  }

  /** @see org.mulgara.query.filter.RDFTerm#isIRI() */
  public boolean isIRI() throws QueryException {
    return true;
  }

  /** @see org.mulgara.query.filter.value.AbstractAccessorFn#getLexical() */
  public String getLexical() throws QueryException {
    throw new QueryException("Invalid to get lexical information on an IRI.");
  }

  /** @see org.mulgara.query.filter.value.AbstractAccessorFn#getLang() */
  public SimpleLiteral getLang() throws QueryException {
    throw new QueryException("Invalid to get a language on an IRI.");
  }

  /** @see org.mulgara.query.filter.value.AbstractAccessorFn#getType() */
  public IRI getType() throws QueryException {
    throw new QueryException("Invalid to get a type IRI on an IRI.");
  }

  /** @see org.mulgara.query.filter.value.AbstractAccessorFn#test(org.mulgara.query.filter.Context) */
  public boolean test(Context context) throws QueryException {
    throw new QueryException("Invalid to get an effective boolean value on an IRI.");
  }

  /**
   * Get the type from the underlying operand
   * @return The operands type IRI
   * @throws QueryException If there was an error resolving the operand 
   */
  protected RDFTerm resolve() throws QueryException {
    if (!operand.isLiteral()) throw new QueryException("Disallowed type in DATATYPE function. Expected a Literal. Got a : " + operand.getClass().getSimpleName());
    return ((ValueLiteral)operand).getType();
  }

  /**
   * The return value of this class is not comparable, so always throw an exception.
   * @return never returns a value.
   * @throws QueryException To indicate that this class cannot be compared.
   */
  protected ComparableExpression resolveComparable() throws QueryException {
    throw new QueryException("Type error: datatypes are IRIs which cannot be compared for anything but equality");
  }

}
