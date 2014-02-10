/*
 * Copyright 2008 Fedora Commons, Inc.
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

// Java 2 standard packages
import java.util.*;

// Third party packages
// import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.filter.RDFTerm;

/**
 * A constraint for assigning a value to a variable.
 *
 * @created July 2, 2009
 *
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class ConstraintAssignment implements ConstraintExpression {

  /** Serialization ID */
  private static final long serialVersionUID = 6393521993437251578L;

  // /** Logger. */
  // private final static Logger logger = Logger.getLogger(ConstraintAssignment.class);

  protected ConstraintExpression context;

  protected Variable var;

  protected RDFTerm expr;

  Set<Variable> variables = null;

  /**
   * Constructor.
   *
   * @param context The constraint that provides context to set this variable in.
   * @param var The variable to be assigned.
   * @param expr The expression to assign to the variable.
   */
  public ConstraintAssignment(ConstraintExpression context, Variable var, RDFTerm expr) {

    // Validate parameters
    if (context == null) {
      throw new IllegalArgumentException("null constraint context");
    }
    if (var == null) {
      throw new IllegalArgumentException("null variable");
    }

    if (expr == null) {
      throw new IllegalArgumentException("null expression for the variable");
    }

    this.context = context;
    this.var = var;
    this.expr = expr;
  }


  /**
   * Get all constraints which are variables. This
   * method now uses the fourth element of the triple.
   *
   * @return A set containing all variable constraints.
   */
  public Set<Variable> getVariables() {
    if (variables == null) {
      Set<Variable> v = new HashSet<Variable>(context.getVariables());
      v.add(var);
      variables = Collections.unmodifiableSet(v);
    }
    return variables;
  }


  /**
   * Equality is by value.
   *
   * @param object The ConstraintIs object to compare to
   * @return <code>true</code> if object is the same as this.
   */
  public boolean equals(Object object) {
    if (object == null) return false;

    if (object == this) return true;

    // Check that the given object is the correct class
    if (object.getClass() != this.getClass()) return false;

    // Check each element.
    ConstraintAssignment tmpConstraint = (ConstraintAssignment)object;

    if (!context.equals(tmpConstraint.context) || var.equals(tmpConstraint.var)) return false;

    // fudge the value comparison
    boolean exprEq = false;
    Object v1 = null;
    Object v2 = null;
    try {
      v1 = expr.getValue();
      try {
        v2 = tmpConstraint.expr.getValue();
        exprEq = v1.equals(v2);
      } catch (QueryException e2) { /* unbound values are unequal, so continue to return false */ }
    } catch (QueryException e) {
      try {
        v2 = tmpConstraint.expr.getValue();
      } catch (QueryException e2) { exprEq = true; }
    }

    return exprEq;
  }


  /**
   * Retrieve the variable part of this constraint.
   * @return the variable being bound.
   */
  public ConstraintExpression getContextConstraint() {
    return context;
  }


  /**
   * Retrieve the variable part of this constraint.
   * @return the variable being bound.
   */
  public Variable getVariable() {
    return var;
  }


  /**
   * Retrieve the value part of this constraint.
   * @return the expression to bind the variable to.
   */
  public RDFTerm getExpression() {
    return expr;
  }


  /**
   * Calculate a semi-unique integer for this object
   *
   * @return a semi-unique integer for this object
   */
  public int hashCode() {
    return context.hashCode() + var.hashCode() + expr.hashCode();
  }


  /**
   * Creates a string representation of these constraints. A typical result
   * might be <code>[$x &lt;mulgara:is&gt; 'bar' $0]</code>.
   *
   * @return String representation of this object
   */
  public String toString() {

    StringBuffer buffer = new StringBuffer(" LET (");
    buffer.append(var).append(" := ").append(expr).append(")");
    return buffer.toString();
  }

  /**
   * This expression is non associative.
   * @return <code>false</code> since this operation is not associative.
   */
  public boolean isAssociative() {
    return false;
  }

}
