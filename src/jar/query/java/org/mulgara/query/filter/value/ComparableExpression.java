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
public interface ComparableExpression extends RDFTerm, Filter {

  /**
   * Returns the data represented by this expression.
   * @return The represented data.
   * @throws QueryException Resolving the data for this value leads to an error.
   */
  public Object getValue() throws QueryException;

  /**
   * A value based magnitude test.
   * @param v The object to compare to
   * @return <code>true</code> iff v has a greater value than the current object.
   * @throws QueryException Resolving the data for this value leads to an error,
   *                        or the types are incompatible.
   */
  public boolean lessThan(ComparableExpression v) throws QueryException;

  /**
   * A value based magnitude test.
   * @param v The object to compare to
   * @return <code>true</code> iff v has a lesser value than the current object.
   * @throws QueryException Resolving the data for this value leads to an error,
   *                        or the types are incompatible.
   */
  public boolean greaterThan(ComparableExpression v) throws QueryException;
  
  /**
   * A value based magnitude test.
   * @param v The object to compare to
   * @return <code>false</code> iff v has a lesser value than the current object.
   * @throws QueryException Resolving the data for this value leads to an error,
   *                        or the types are incompatible.
   */
  public boolean lessThanEqualTo(ComparableExpression v) throws QueryException;
  
  /**
   * A value based magnitude test.
   * @param v The object to compare to
   * @return <code>false</code> iff v has a greater value than the current object.
   * @throws QueryException Resolving the data for this value leads to an error,
   *                        or the types are incompatible.
   */
  public boolean greaterThanEqualTo(ComparableExpression v) throws QueryException;
}
