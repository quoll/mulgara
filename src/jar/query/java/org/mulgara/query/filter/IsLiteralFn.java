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
package org.mulgara.query.filter;

import org.mulgara.query.QueryException;


/**
 * Tests an element to see if it is an RDF Literal value.
 *
 * @created Mar 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class IsLiteralFn extends AbstractBooleanFn {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 3543119682508127635L;

  /**
   * Create a function for testing if a value it a Literal
   * @param operand The variable to test
   */
  public IsLiteralFn(RDFTerm operand) {
    super(operand);
  }

  /** @see org.mulgara.query.filter.AbstractBooleanFn#fnTest() */
  boolean fnTest() throws QueryException {
    return operand.isLiteral();
  }

}
