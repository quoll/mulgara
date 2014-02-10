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

import java.util.Arrays;
import java.util.List;

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.ValueLiteral;


/**
 * Represents a filter that takes other filters as parameters.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class NAryOperatorFilter extends AbstractFilterValue implements ValueLiteral {

  /** Serialization ID */
  private static final long serialVersionUID = -558162654076780285L;

  /**
   * Creates a multiple operand operation
   * @param operands The operands for this filter operation.
   */
  NAryOperatorFilter(Filter... operands) {
    super(operands);
  }

  public RDFTerm resolve() throws QueryException {
    return testOp(getCurrentContext()) ? Bool.TRUE : Bool.FALSE;
  }

  /**
   * Runs the test on all the operands.
   * @param context The context to resolve the test in.
   * @return <code>true</code> if the operation passes.
   * @throws QueryException If there was an <em>unrecoverable</em> error resolving the operands 
   */
  boolean testOp(Context context) throws QueryException {
    // we can cast operands, since it was provided as a Filter[]
    return testList(context, Arrays.asList((Filter[])operands));
  }

  /**
   * An operation specific test. This will apply the test to all operands given in the list,
   * short circuiting where possible, in order to provide a correct result even in the event
   * of an exception.
   * @param context The context to resolve the list in.
   * @param filters The operands of the operation. These are resolved in the provided context.
   * @return A boolean value, according to the specifics of the test.
   * @throws QueryException If there was an <em>unrecoverable</em> error resolving the operands.
   */
  abstract boolean testList(Context context, List<Filter> filters) throws QueryException;

  /**
   * Utility to return the tail of a list. This is the entire list, minus the first entry.
   * @param l The list to get the tail of.
   * @return A new list, with a size equal to l.size()-1. ie. all of l except l.get(0)
   */
  static List<Filter> tail(List<Filter> l) {
    return l.subList(1, l.size());
  }

}
