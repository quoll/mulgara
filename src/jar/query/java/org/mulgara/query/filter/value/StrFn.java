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

import org.jrdf.graph.BlankNode;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.RDFTerm;

/**
 * Represents a str() function that stringizes any underlying data.
 * This operation is made to look like a SimpleLiteral.
 *
 * @created Mar 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class StrFn extends AbstractAccessorFn {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 5983891032753800787L;

  /**
   * Create a new string extractor function.
   * @param operand The operand for the function.
   */
  public StrFn(RDFTerm operand) {
    super(operand);
  }

  /**
   * @see org.mulgara.query.filter.value.ValueLiteral#getLang()
   * @return Always empty, as the result of this operation is a simple literal, which has no type.
   */
  public SimpleLiteral getLang() throws QueryException {
    return SimpleLiteral.EMPTY;
  }

  /** @see org.mulgara.query.filter.AbstractFilterValue#isSimple() */
  public boolean isSimple() throws QueryException {
    return ((ValueLiteral)resolve()).isSimple();
  }

  /**
   * @see org.mulgara.query.filter.value.ValueLiteral#getLexical()
   * @throws QueryException if the operand does not resolve
   */
  public String getLexical() throws QueryException {
    Object value = operand.getValue();
    // Works fine without this test, but the SPARQL spec wants to prevent it
    if (value instanceof BlankNode) throw new QueryException("Not permitted to convert a blank node to a String in SPARQL");
    return operand.getValue().toString();
  }

  /**
   * Resolve the value of the operand, and create a literal for it.
   * @return The operand
   * @throws QueryException if the operand does not resolve
   */
  protected RDFTerm resolve() throws QueryException {
    return new SimpleLiteral(getLexical());
  }
}
