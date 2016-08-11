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
 *   getModel() contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
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
import org.jrdf.graph.*;


/**
 * An equality constraint. The elements within the constraint can be either variables or
 * a variable and a value.
 *
 * @created 2004-08-12
 *
 * @author <a href="mailto:pag@tucanatech.com">Paula Gearon</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/05/29 08:32:39 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class ConstraintHaving extends AbstractConstraintExpression implements Constraint {

  // /** Logger. */
  // private final static Logger logger = Logger.getLogger(ConstraintHaving.class);

  /** Serialization ID */
  private static final long serialVersionUID = -802805844572698506L;

  /** The 4-tuple of elements (either nodes or variables) */
  protected final ConstraintElement[] element;


  /**
   * Constructor.
   *
   * @param e0 The first statement constraint.
   * @param e2 The third statement constraint.
   * @param e3 The fourth (meta) statement constraint.
   */
  protected ConstraintHaving(ConstraintElement e0, ConstraintElement e1, ConstraintElement e2, ConstraintElement e3) {

    // Validate parameters
    if (e0 == null) {
      throw new IllegalArgumentException("null e0 parameter");
    }
    if (!(e0 instanceof Variable)) {
      throw new IllegalArgumentException(" Subject of special predicate must be a variable: " + e0);
    }

    if (e2 == null) {
      throw new IllegalArgumentException("null e2 parameter");
    }
    if (!(e2 instanceof Variable || e2 instanceof Literal)) {
      throw new IllegalArgumentException(" Object of special predicate must " +
          "be a variable or literal.  Was: " + e2);
    }

    if (e3 == null) {
      throw new IllegalArgumentException("null e3 parameter");
    }

    // Initialize fields
    element = new ConstraintElement[] {e0, e1, e2, e3};
  }


  public ConstraintElement getModel() {
    return element[3];
  }

  /**
   * Constructor. The meta node is initialized with the variable <code>$_from</code>.
   * This is only for back-compatibility, and will soon be deprecated.
   *
   * @param e0 The first statement constraint.
   * @param e1 The second statement constraint.
   * @param e2 The third statement constraint.
   */
  protected ConstraintHaving(ConstraintElement e0, ConstraintElement e1, ConstraintElement e2) {
    this(e0, e1, e2, Variable.FROM);
  }


  /**
   * Anyone calling this is responsible for setting {@link #element} themselves.
   *
   */
  protected ConstraintHaving() {
    element = null;
  }


  /**
   * Get a constraint element by index.
   *
   * @param index The constraint element to retrieve, from 0 to 3.
   * @return The constraint element referred to by index.
   */
  public ConstraintElement getElement(int index) {
    return element[index];
  }


  /**
   * Get all constraints which are variables. For back-compatibility, this
   * method currently ignores the fourth element of the triple.
   *
   * @return A set containing all variable constraints.
   */
  public Set<Variable> getVariables() {
    if (variables == null) {
      Set<Variable> v = new HashSet<Variable>();
      Variable e = getVariable();
      if (!e.getName().startsWith("_")) v.add(e);
      if (element[3] instanceof Variable && !((Variable)element[3]).getName().startsWith("_")) {
        v.add((Variable)element[3]);
      }
      variables = Collections.unmodifiableSet(v);
    }
    return variables;
  }


  /**
   * Equality is by value.
   *
   * @param object The ConstraintHaving object to compare to
   * @return <code>true</code> if object is the same as this.
   */
  public boolean equals(Object object) {
    if (object == null) return false;

    if (object == this) return true;

    // Check that the given object is the correct class
    if (object.getClass() != this.getClass()) return false;

    // Check each element.
    ConstraintHaving tmpConstraint = (ConstraintHaving) object;

    return element[0].equals(tmpConstraint.getElement(0)) &&
           element[1].equals(tmpConstraint.getElement(1)) &&
           element[2].equals(tmpConstraint.getElement(2)) &&
           element[3].equals(tmpConstraint.getElement(3));
  }



  /**
   * Retrieve the variable part of this constraint.
   *
   * @return the first element, returned as a variable.
   */
  public Variable getVariable() {
    return (Variable)element[0];
  }


  /**
   * Retrieve the value part of this constraint.
   *
   * @return the third element.
   */
  public ConstraintElement getValueNode() {
    return element[2];
  }


  /**
   * Calculate a semi-unique integer for this object
   *
   * @return a semi-unique integer for this object
   */
  public int hashCode() {
    return element[0].hashCode() + element[1].hashCode() + element[2].hashCode() + element[3].hashCode();
  }


  /**
   * Creates a string representation of these constraints. A typical result
   * might be <code>[$x &lt;mulgara:is&gt; 'bar' $0 ~100]</code> where <code>100</code>
   * is the number of rows this constraint is estimated to produce during query
   * resolution.
   *
   * @return String representation of this object
   */
  public String toString() {

    StringBuffer buffer = new StringBuffer("[");
    buffer.append(element[0]).append(" ");
    buffer.append(element[1]).append(" ");
    buffer.append(element[2]).append(" ").append(element[3]);
    buffer.append("]");

    return buffer.toString();
  }
}
