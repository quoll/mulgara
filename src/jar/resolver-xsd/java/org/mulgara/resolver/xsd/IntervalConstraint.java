/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s):
 * The copyright to this file is held by:
 *   The Australian Commonwealth Government
 *   Department of Defense
 * Developed by Netymon Pty Ltd
 * under contract 4500430665
 * contributed to the Mulgara Project under the
 *   Mozilla Public License version 1.1
 * per clause 4.1.3 of the above contract.
 *
 *  getModel() contributed by Netymon Pty Ltd on behalf of
 *  The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.xsd;

// Java 2 standard packages
import java.util.Collections;
import java.util.Set;

// Third party packages
import org.apache.log4j.Logger; // Apache Log4J

// Local classes
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.jrdf.graph.URIReference;

/**
 * A constraint representing a bounded interval between two XSD values.
 *
 * Such constraints are synthesized from pairs of regular constraints during
 * symbolic optimization.
 *
 * @created 2005-05-02
 *
 * @author <a href="mailto:raboczi@itee.uq.edu.au">Simon Raboczi</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/06/09 09:26:20 $ @maintenanceAuthor $Author: raboczi $
 *
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class IntervalConstraint implements Constraint {

  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(IntervalConstraint.class.getName());

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 7653707287858079768L;

  /**
   * The variable under constraint.
   *
   * This is never <code>null</code>.
   */
  private final Variable variable;

  /**
   * The lower bound of the interval.
   *
   * If the interval has no lower bound, this will be <code>null</code>.
   */
  private final Bound lowerBound;

  /**
   * The upper bound of the interval.
   *
   * If the interval has no upper bound, this will be <code>null</code>.
   */
  private final Bound upperBound;

  /**
   * The model used to reference the XSD Resolver from this constraint.
   */
   private final URIReference model;

  /**
   * Sole constructor.
   *
   * @param variable  the variable to constrain, never <code>null</code>
   * @param lowerBound  the lower bound of the interval, or <code>null</code>
   *   if the interval has no lower bound
   * @param upperBound  the upper bound of the interval, or <code>null</code>
   *   if the interval has no upper bound
   * @throws IllegalArgumentException if <var>variable</var> is
   *   <code>null</code>
   */
  IntervalConstraint(Variable variable, Bound lowerBound, Bound upperBound, URIReference model) {
    if (variable == null) { throw new IllegalArgumentException("Null \"variable\" parameter"); }
    if (model == null) { throw new IllegalArgumentException("Null 'model' parameter"); }

    // Initialize field
    this.variable = variable;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.model =  model;
  }

  /**
   * Used to create a copy of this constraint with a new variable and model.
   * @param variable The variable to use in the new constraint.
   * @param model The model to use in the new constraint.
   * @return A new constraint with the same properties as this one, but with a different variable and model.
   */
  public IntervalConstraint mutateTo(Variable variable, URIReference model) {
    return new IntervalConstraint(variable, lowerBound, upperBound, model);
  }

  /**
   * @param intervalConstraint  an instance constraining the same variable as
   *   this one, never <code>null</code>
   * @throws IllegalArgumentException if the <var>intervalConstraint</var> does
   *   not constrain the same variable as this instance or is <code>null</code>
   */
  IntervalConstraint conjoin(IntervalConstraint intervalConstraint) {
    // Validate "intervalConstraint" parameter
    if (intervalConstraint == null) {
      throw new IllegalArgumentException(
        "Null \"intervalConstraint\" parameter"
      );
    }
    if (!intervalConstraint.getVariable().equals(variable)) {
      throw new IllegalArgumentException(
        "Mismatch variables: " + intervalConstraint.getVariable() + " and " +
        variable
      );
    }

    return new IntervalConstraint(
      variable,
      maximumBound(lowerBound, intervalConstraint.lowerBound),
      minimumBound(upperBound, intervalConstraint.upperBound),
      model
    );
  }


  public ConstraintElement getModel() {
    return new URIReferenceImpl(model.getURI());
  }

  public ConstraintElement getElement(int index) {
    throw new IllegalStateException("Cannot index IntervalConstraint");
  }

  public boolean isRepeating() {
    return false;
  }

  /**
   * Return the maximum of two {@link Bound}s.
   *
   * @param lhs  a bound, possibly <code>null</code>
   * @param rhs  another bound, possibly <code>null</code>
   * @return the largest of <var>lhs</var> and <var>rhs</var>, or
   *   <code>null</code> if both are <code>null</code>
   */
  private static Bound maximumBound(Bound lhs, Bound rhs) {
    if (lhs == null) {
      return (rhs == null) ? null : rhs;
    } else {
      if (rhs == null) {
        return lhs;
      } else {
        if (lhs.getValue() < rhs.getValue()) return rhs;
        if (lhs.getValue() > rhs.getValue()) return lhs;
        assert lhs.getValue() == rhs.getValue();
        return lhs.isClosed() ? lhs : rhs;
      }
    }
  }

  /**
   * Return the minimum of two {@link Bound}s.
   *
   * @param lhs  a bound, possibly <code>null</code>
   * @param rhs  another bound, possibly <code>null</code>
   * @return the smallest of <var>lhs</var> and <var>rhs</var>, or
   *   <code>null</code> if both are <code>null</code>
   */
  private static Bound minimumBound(Bound lhs, Bound rhs) {
    if (lhs == null) {
      return (rhs == null) ? null : rhs;
    } else {
      if (rhs == null) {
        return lhs;
      } else {
        if (lhs.getValue() < rhs.getValue()) return lhs;
        if (lhs.getValue() > rhs.getValue()) return rhs;
        assert lhs.getValue() == rhs.getValue();
        return lhs.isClosed() ? rhs : lhs;
      }
    }
  }

  /**
   * @return the lower {@link Bound} of the constraint, or <code>null</code>
   *   if it has no lower {@link Bound}
   */
  Bound getLowerBound() {
    return lowerBound;
  }

  /**
   * @return the upper {@link Bound} of the constraint, or <code>null</code>
   *   if it has no upper {@link Bound}
   */
  Bound getUpperBound() {
    return upperBound;
  }

  /**
   * @return the constrained variable
   */
  Variable getVariable() {
    return variable;
  }

  /**
   * @return <code>true</code> if the constraint can never be satisfied (i.e.
   *   the lower bound is above the upper bound)
   */
  boolean isEmpty() {
    
    if (lowerBound == null) return false;

    if (upperBound == null) return false;

    if (lowerBound.getValue() < upperBound.getValue()) return false;

    if (lowerBound.getValue() > upperBound.getValue()) return true;

    assert lowerBound.getValue() == upperBound.getValue();
    return !(lowerBound.isClosed() && upperBound.isClosed());
  }

  /**
   * @return whether the interval admits only a single value
   */
  boolean isSingleton() {
    return lowerBound != null &&
           lowerBound.isClosed() &&
           lowerBound.equals(upperBound);
  }

  /**
   * @return whether the interval has neither a lower nor an upper bound, and
   *   is thus not really a constraint at all
   */
  boolean isUnconstrained() {
    return (lowerBound == null) && (upperBound == null);
  }

  //
  // Methods implementing ConstraintExpression
  //

  public Set<Variable> getVariables() {
    return Collections.singleton(variable);
  }

  //
  // Methods overriding Object
  //

  /**
   * Equality is by value rather than by reference.
   */
  public boolean equals(Object object) {
    if (object == null) return false;

    if (object.getClass() != IntervalConstraint.class) return false;

    assert object instanceof IntervalConstraint;
    IntervalConstraint intervalConstraint = (IntervalConstraint) object;
    
    if (isEmpty() && intervalConstraint.isEmpty()) return true;

    if (isUnconstrained() && intervalConstraint.isUnconstrained()) return true;

    return variable.equals(intervalConstraint.variable) &&
           Bound.equals(lowerBound, intervalConstraint.lowerBound) &&
           Bound.equals(upperBound, intervalConstraint.upperBound);
  }

  /** @see java.lang.Object#hashCode() */
  public int hashCode() {
    return (lowerBound == null ? 1 : lowerBound.hashCode()) +
           (upperBound == null ? 7 : upperBound.hashCode());
  }

  /**
   * @return a legible representation of the constraint, for instance
   *   <code>[1.5 &lt; $x &lt;= 2.5]</code>
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer("[");
    if (lowerBound != null) {
      buffer.append(lowerBound.getValue());
      buffer.append(lowerBound.isClosed() ? " <= " : " < ");
    }
    buffer.append(variable);
    if (upperBound != null) {
      buffer.append(upperBound.isClosed() ? " <= " : " < ");
      buffer.append(upperBound.getValue());
    }
    buffer.append("]");
    return buffer.toString();
  }

  /**
   * Not a binary operation, so not a binary constraint.
   * @return <code>false</code> to indicate that this operation is not associative.
   */
  public boolean isAssociative() {
    return false;
  }
}
