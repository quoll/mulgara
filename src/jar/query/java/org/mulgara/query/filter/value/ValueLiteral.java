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
import org.mulgara.query.filter.Filter;
import org.mulgara.query.filter.RDFTerm;


/**
 * Represents expressions that can appear in a comparison expression, used by filters
 *
 * @created Mar 10, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public interface ValueLiteral extends RDFTerm, Filter, ComparableExpression {

  /**
   * Returns the data represented by this expression.
   * @return The represented data.
   * @throws QueryException Resolving the data for this value leads to an error.
   */
  public Object getValue() throws QueryException;

  /**
   * Returns the string that represents the value.  This is identical to the value for
   * simple literals and xsd:string, and should parse to the value for most other types.
   * However, it will also store incompatible values, in case of bad literal definitions
   * such as 'abc'^^<xsd:integer>
   * @return The represented data.
   * @throws QueryException Resolving the data for this value leads to an error.
   */
  public String getLexical() throws QueryException;

  /**
   * Gets the language code for this literal, if it exists.
   * @return a language code, or an empty string if none is present
   * @throws QueryException Resolving the data for this value leads to an error.
   */
  public SimpleLiteral getLang() throws QueryException;

  /**
   * Tests if this literal is a simple literal. If it is, then the type should be xsd:string.
   * @return <code>true</code> if this literal is simple.
   * @throws QueryException Resolving the data for this value leads to an error.
   */
  public boolean isSimple() throws QueryException;

  /**
   * Gets the type of this literal
   * @return The URI for this literals type
   * @throws QueryException Resolving the data for this value leads to an error.
   */
  public IRI getType() throws QueryException;

  /**
   * Calculates the Effective Boolean Value (EBV) of this literal.
   * @param context The context to determine this value in. Ignored because this is a literal.
   * @return <code>true</code> when the EBV conditions are met.
   * @throws QueryException There was an error during resolving the literal.
   */
  public boolean test(Context context) throws QueryException;
  
}
