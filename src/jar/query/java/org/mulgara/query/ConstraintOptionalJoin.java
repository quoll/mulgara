/*
 * Copyright 2009 DuraSpace.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.query;


import org.mulgara.query.filter.Filter;
import org.mulgara.query.filter.value.Bool;

/**
 * A constraint expression composed of a left-out-join conjunction of two subexpressions
 *
 * @created Apr 2, 2008
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */

public class ConstraintOptionalJoin extends ConstraintBinaryOperation {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = -6776376724963178229L;

  private Filter filter = Bool.TRUE;

  /**
   * Construct a constraint left-outer-join.
   * @param lhs a non-<code>null</code> constraint expression
   * @param rhs another non-<code>null</code> constraint expression
   */
  public ConstraintOptionalJoin(ConstraintExpression lhs, ConstraintExpression rhs) {
    super(lhs, rhs);
  }

  /**
   * Construct a constraint left-outer-join.
   * @param lhs a non-<code>null</code> constraint expression
   * @param rhs another non-<code>null</code> constraint expression
   * @param filter Filters the join.
   */
  public ConstraintOptionalJoin(ConstraintExpression lhs, ConstraintExpression rhs, Filter filter) {
    super(lhs, rhs);
    if (filter == null) throw new IllegalArgumentException("Null \"filter\" parameter");
    this.filter = filter;
  }

  /**
   * @return Get the LHS "main" parameter
   */
  public ConstraintExpression getMain() {
    return getLhs();
  }

  /**
   * @return Get the RHS "optional" parameter
   */
  public ConstraintExpression getOptional() {
    return getRhs();
  }

  /**
   * @return Get the filter parameter
   */
  public Filter getFilter() {
    return filter;
  }

  /**
   * Gets the Name attribute of the ConstraintOptionalJoin object
   * @return The Name value
   */
  String getName() {
    return " optional ";
  }

  /**
   * Optional Join is not an associative operation.
   * @return <code>false</code> since this operation is not associative.
   */
  public boolean isAssociative() {
    return false;
  }

}
