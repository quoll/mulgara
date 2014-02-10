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
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.query;

// Java 2 standard packages
import java.util.*;

// Third party packages
// import org.apache.log4j.Logger;

/**
 * A constraint expression composed of two subexpressions and a dyadic operator.
 * Can be transformed.
 *
 * @created 2001-08-12
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class ConstraintOperation extends AbstractConstraintExpression {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = -236847137057853871L;

  // /** Logger. */
  // private static Logger logger = Logger.getLogger(ConstraintOperation.class);

  //
  // Constructor
  //

  /**
   * Construct a constraint operation. Subclasses are compelled to use this
   * constructor, guaranteeing that the operands are always non-<code>null</code>
   * .
   *
   * @param lhs a non-<code>null</code> model expression
   * @param rhs another non-<code>null</code> model expression
   */
  protected ConstraintOperation(ConstraintExpression lhs, ConstraintExpression rhs) {
    // Validate "lhs" parameter
    if (lhs == null) throw new IllegalArgumentException("Null \"lhs\" parameter");

    // Validate "rhs" parameter
    if (rhs == null) throw new IllegalArgumentException("Null \"rhs\" parameter");

    // Initialize fields
    elements = new ArrayList<ConstraintExpression>(2);

    // Add the LHS
    if (isAssociative() && lhs.getClass() == getClass()) {
      elements.addAll(((ConstraintOperation)lhs).getElements());
    } else {
      elements.add(lhs);
    }

    // Add the RHS
    if (isAssociative() && rhs.getClass() == getClass()) {
      elements.addAll( ( (ConstraintOperation) rhs).getElements());
    } else {
      elements.add(rhs);
    }
  }

  /**
   * Creates a new ConstraintOperation build on a list of ConstraintExpression
   *
   * @param elements A list of ConstraintExpression to be the parameters of this expression
   */
  protected ConstraintOperation(List<ConstraintExpression> elements) {
    // Validate "elements" parameter
    if (elements == null) throw new IllegalArgumentException("Null \"elements\" parameter");
    // assert elements.size() > 1;

    // Initialize fields
    this.elements = new ArrayList<ConstraintExpression>();

    // add all the elements, flattening if needed
    for (ConstraintExpression op: elements) {
      if (op.isAssociative() && op.getClass() == getClass()) {
        this.elements.addAll(((ConstraintOperation)op).getElements());
      } else {
        this.elements.add(op);
      }
    }
  }


  /**
   * Accessor method for the operands.
   *
   * @return a list of {@link ConstraintExpression}s
   */
  public List<ConstraintExpression> getElements() {
    return Collections.unmodifiableList(elements);
  }

  /**
   * Get a constraint element by index.
   *
   * @param index The constraint element to retrieve, from 0 to 3.
   * @return The constraint element referred to by index.
   */
  public ConstraintExpression getOperand(int index) {
    return (ConstraintExpression) elements.get(index);
  }


  /**
   * Defines Structual equality.
   *
   * @return equality.
   */
  public boolean equals(Object o) {
    if (!super.equals(o)) return false;

    ConstraintOperation co = (ConstraintOperation) o;
    if (elements.size() != co.elements.size()) return false;

    Iterator<ConstraintExpression> lhs = elements.iterator();
    Iterator<ConstraintExpression> rhs = co.elements.iterator();
    while (lhs.hasNext()) {
      if (!(lhs.next().equals(rhs.next()))) return false;
    }

    return true;
  }

  /**
   * Get all constraints which are variables. For back-compatibility, this
   * method currently ignores the fourth element of the triple.
   *
   * @return A set containing all variable constraints.
   */
  public Set<Variable> getVariables() {
    // Check to see if there variables have been retrieved.
    if (variables == null) {
      Set<Variable> v = new HashSet<Variable>();

      for (ConstraintExpression expr: getElements()) v.addAll(expr.getVariables());

      variables = Collections.unmodifiableSet(v);
    }

    return variables;
  }


  /**
   * Generate hashcode for this object.
   *
   * @return This objects hascode.
   */
  public int hashCode() {
    int hashCode = 0;
    Iterator<ConstraintExpression> i = elements.iterator();
    while (i.hasNext()) hashCode ^= i.next().hashCode();
    return hashCode;
  }

  /**
   * Convert this object to a string.
   *
   * @return A string representation of this object.
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer("(" + getName());
    Iterator<ConstraintExpression> i = getElements().iterator();

    while (i.hasNext()) {
      buffer.append(i.next().toString());
      if (i.hasNext()) buffer.append(" ");
    }

    buffer.append(")");

    return buffer.toString();
  }

  /**
   * Indicates if this operation is associative.
   * @return <code>true</code> iff this operation is associative.
   */
  public boolean isAssociative() {
    return true;
  }

  /**
   * Gets the Name attribute of the ConstraintOperation object
   *
   * @return The Name value
   */
  abstract String getName();


  /**
   * Remove the constraint expressions from the product that have non-intersecting variables.
   *
   * @param product The list of constraints to test and modify.
   */
  protected static void filter(List<ConstraintExpression> product) {
  
    Set<Variable> o1 = new HashSet<Variable>();
  
    // Variables which occur at least once.
    Set<Variable> o2 = new HashSet<Variable>();
  
    // Variables which occur two or more times.
    // Get a set of variables which occur two or more times.
    for (ConstraintExpression oc: product) {
  
      Set<Variable> ocVars = oc.getVariables();
      Set<Variable> vars = new HashSet<Variable>(ocVars);
      vars.retainAll(o1);
      o2.addAll(vars);
      o1.addAll(ocVars);
    }
  
    // remove the expressions which have non-intersecting variables
    for (Iterator<ConstraintExpression> pIt = product.iterator(); pIt.hasNext(); ) {
  
      ConstraintExpression oc = pIt.next();
      Set<Variable> vars = new HashSet<Variable>(oc.getVariables());
      vars.retainAll(o2);
  
      if (vars.isEmpty()) pIt.remove();
    }
  }

}
