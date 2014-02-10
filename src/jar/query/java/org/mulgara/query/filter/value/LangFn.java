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
import org.mulgara.query.filter.RDFTerm;

/**
 * Represents a Lang() function that accesses the language on a literal.
 * This operation is made to look like a SimpleLiteral.
 *
 * @created Mar 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class LangFn extends AbstractAccessorFn {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = -6744958736249165056L;

  /**
   * Create a new language extractor function.
   * @param operand The operand for the function.
   */
  public LangFn(RDFTerm operand) {
    super(operand);
  }

  /**
   * @see org.mulgara.query.filter.value.ValueLiteral#getLang()
   * @return Always empty, as the result of this operation is a simple literal, which has no type.
   */
  public SimpleLiteral getLang() throws QueryException {
    return SimpleLiteral.EMPTY;
  }

  /**
   * @see org.mulgara.query.filter.RDFTerm#isLiteral()
   * May be overridden.
   */
  public boolean isLiteral() throws QueryException {
    return true;
  }

  /** @see org.mulgara.query.filter.AbstractFilterValue#isSimple() */
  public boolean isSimple() throws QueryException {
    return ((ValueLiteral)resolve()).isSimple();
  }

  /**
   * Applies this function to its operands to get back a result.
   * @return A language code for the operand.
   * @throws QueryException If the operand was not a literal.
   */
  protected RDFTerm resolve() throws QueryException {
    if (!operand.isLiteral()) throw new QueryException("Disallowed type in LANG function. Expected a Literal. Got a : " + operand.getClass().getSimpleName());
    return ((ValueLiteral)operand).getLang();
  }
}
