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

package org.mulgara.query;

import java.util.Set;

import org.mulgara.query.filter.Filter;

// Third party packages
import org.apache.log4j.Logger;

/**
 * A constraint expression comprised of filtering the subexpression according
 * to an abstract formula specified as a {@link org.mulgara.query.filter.Filter}.
 *
 * @created Mar 24, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ConstraintFilter implements ConstraintExpression {

  @SuppressWarnings("unused")
  /** The logger */
  private final static Logger logger = Logger.getLogger(ConstraintFilter.class.getName());

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = -816733883358318430L;

  /** The filtered expression. */
  private ConstraintExpression constraintExpr;

  /** The filter construct. */
  private Filter filter;

  /**
   * Construct a filtered constraint.
   * @param constraintExpr a non-<code>null</code> constraint
   * @throws IllegalArgumentException if <var>constraint</var> is <code>null</code>
   */
  public ConstraintFilter(ConstraintExpression constraintExpr, Filter filter) {
    if (constraintExpr == null) throw new IllegalArgumentException("Null \"constraint expression\" parameter");
    if (filter == null) throw new IllegalArgumentException("Null \"filter\" parameter");
    this.constraintExpr = constraintExpr;
    this.filter = filter;
  }
  
  /**
   * Retrieve the internal filter expression. This constraint returns data whenever the filter
   * returns <code>true</code>.
   * @return the filter for this constraint.
   */
  public Filter getFilter() {
    return filter;
  }

  /**
   * Gets the constraint being filtered by this constraint.
   * @return The original constraint expression.
   */
  public ConstraintExpression getUnfilteredConstraint() {
    return constraintExpr;
  }

  /** {@inheritDoc} */
  public Set<Variable> getVariables() {
    return constraintExpr.getVariables();
  }

  /** {@inheritDoc} */
  public String toString() {
    return "filter " + constraintExpr + " by ";
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return constraintExpr.hashCode() * -1;
  }

  /** {@inheritDoc} */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (object == this) return true;

    // Check that the given object is the correct class.
    if (ConstraintFilter.class != object.getClass()) return false;
    // check each element.
    ConstraintFilter other = (ConstraintFilter)object;
    // can't do an equals() on filter, as this evaluates the filter in context
    return constraintExpr.equals(other.constraintExpr) && filter == other.filter;
  }

  /**
   * This expression is unary, so associativity is irrelevant.
   * @return <code>false</code> to indicate that this operation is not associative.
   */
  public boolean isAssociative() {
    return false;
  }

}
