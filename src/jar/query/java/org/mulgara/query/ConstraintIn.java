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

// Java 2 standard packages
import java.util.*;

// Third party packages
// import org.apache.log4j.Logger;

// Local packages
import org.apache.log4j.Logger;

/**
 * A constraint expression for setting the GRAPHs of all child expressions.
 *
 * @created Apr 22, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ConstraintIn implements ConstraintExpression {

  /** Serialization ID for marshalling */
  private static final long serialVersionUID = 1248304769395263538L;

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(ConstraintIn.class);

  /** The filtered expression. */
  private ConstraintExpression constraint;

  /** The IN element to set subconstraints to. */
  private ConstraintElement graph;
  
  private Set<Variable> variables;

  /**
   * Construct a filtered constraint.
   * @param constraint a non-<code>null</code> constraint.
   * @param graph the value to set all the graphs of all sub-constraints to.
   * @throws IllegalArgumentException if <var>constraint</var> is <code>null</code>
   */
  public ConstraintIn(ConstraintExpression constraint, ConstraintElement graph) {
    if (constraint == null) throw new IllegalArgumentException("Null \"constraint\" parameter");
    if (graph == null) throw new IllegalArgumentException("Null graph parameter");
    this.constraint = constraint;
    this.graph = graph;
  }
  
  /**
   * Retrieve the graph to use.
   * @return the graph value for this constraint.
   */
  public ConstraintElement getGraph() {
    return graph;
  }

  /**
   * Gets the constraint being modified by this constraint.
   * @return The original constraint expression.
   */
  public ConstraintExpression getConstraintParam() {
    return constraint;
  }

  /**
   * @return The graph described in this constraint
   */
  public ConstraintElement getModel() {
    return graph;
  }


  /** {@inheritDoc} */
  public Set<Variable> getVariables() {
    if (variables == null) {
      if (graph instanceof Variable) {
        Set<Variable> vars = new HashSet<Variable>(constraint.getVariables());
        vars.add((Variable)graph);
        variables = Collections.unmodifiableSet(vars);
      } else {
        variables = constraint.getVariables();
      }
    }
    return variables;
  }


  /**
   * @return <code>true</code> If the constraint wrapped by this constraint is a ConstraintIs.
   */
  public boolean isInnerConstraintIs() {
    return constraint instanceof ConstraintIs;
  }

  /** {@inheritDoc} */
  public String toString() {
    return "GRAPH " + graph + " { " + constraint + " }";
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return constraint.hashCode() * -7;
  }

  /** {@inheritDoc} */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (object == this) return true;

    // Check that the given object is the correct class.
    if (ConstraintIn.class != object.getClass()) return false;
    // check each element.
    ConstraintIn other = (ConstraintIn)object;
    // can't do an equals() on filter, as this evaluates the filter in context
    return constraint.equals(other.constraint) && graph.equals(other.graph);
  }

  /**
   * This expression is unary, so associativity is irrelevant.
   * @return <code>false</code> to indicate that this operation is not associative.
   */
  public boolean isAssociative() {
    return false;
  }

}
