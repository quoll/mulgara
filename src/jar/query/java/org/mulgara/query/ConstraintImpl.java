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

/**
 * A constraint. The elements within the constraint can be either variables or
 * values.
 *
 * @created 2001-07-31
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
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
public class ConstraintImpl extends AbstractConstraintExpression implements Constraint {

 /**
  * Allow newer compiled version of the stub to operate when changes
  * have not occurred with the class.
  * NOTE : update this serialVersionUID when a method or a public member is
  * deleted.
  */
  static final long serialVersionUID = -3127160729187334757L;

  // /** Logger. */
  // private final static Logger logger = Logger.getLogger(ConstraintImpl.class);

  /**
   * The 4-tuple of elements (either nodes or variables)
   */
  protected final ConstraintElement[] element;

  /**
   * True if there are repeating constraints.
   */
  private boolean isRepeating = false;

  /**
   * Constructor.
   *
   * @param e0 The first statement constraint.
   * @param e1 The second statement constraint.
   * @param e2 The third statement constraint.
   * @param e3 The fourth (meta) statement constraint.
   */
  public ConstraintImpl(ConstraintElement e0, ConstraintElement e1,
      ConstraintElement e2, ConstraintElement e3) {

    // Validate parameters
    if (e0 == null) {
      throw new IllegalArgumentException("null e0 parameter");
    }

    if (e1 == null) {
      throw new IllegalArgumentException("null e1 parameter");
    }

    if (e2 == null) {
      throw new IllegalArgumentException("null e2 parameter");
    }

    if (e3 == null) {
      throw new IllegalArgumentException("null e3 parameter");
    }

    // Initialize fields
    element = new ConstraintElement[] {e0, e1, e2, e3};
    isRepeating = (isRepeating(e0, e1)) || (isRepeating(e0, e2))
        || (isRepeating(e1, e2));
  }

  /**
   * Checks if the Constraint Elements are varaiables and are equal.
   *
   * @param e0 ContraintElement
   * @param e1 ConstraintElement
   * @return boolean
   */
  private boolean isRepeating(ConstraintElement e0, ConstraintElement e1) {

    assert e0 != null : "e0 should not be null";
    assert e1 != null : "e1 should not be null";
    return (e0 instanceof Variable) && (e0.equals(e1));
  }

  /**
   * Constructor. The meta node is initialized with the variable <code>$_from</code>
   * . This is only for back-compatibility, and will soon be deprecated.
   *
   * @param e0 The first statement constraint.
   * @param e1 The second statement constraint.
   * @param e2 The third statement constraint.
   */
  public ConstraintImpl(ConstraintElement e0, ConstraintElement e1,
      ConstraintElement e2) {
    this(e0, e1, e2, Variable.FROM);
  }

  /**
   * Anyone calling this is responsible for setting {@link #element} themselves.
   *
   */
  protected ConstraintImpl() {
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


  public ConstraintElement getModel() {
    return element[3];
  }
  
  public boolean isRepeating() {
    return isRepeating;
  }

  /**
   * Get all constraints which are variables. This
   * method uses the fourth element of the triple.
   *
   * @return A set containing all variable constraints.
   */
  public Set<Variable> getVariables() {
    if (variables == null) {
      Set<Variable> v = new HashSet<Variable>();
      for (int i = 0; i < 4; i++) {
        if (element[i] instanceof Variable && !((Variable)element[i]).getName().startsWith("_")) {
          v.add((Variable)element[i]);
        }
      }
      variables = Collections.unmodifiableSet(v);
    }
    return variables;
  }

  /**
   * Equality is by value.
   *
   * @param object The instance to compare to
   * @return <code>true</code> iff objects are compatible and contain the same data.
   */
  public boolean equals(Object object) {
    // FIXME: Refactor to exploit equals() method on ConstraintExpression.
    if (object == null) return false;
    if (object == this) return true;

    boolean returnValue = false;

    // Check that the given object is the correct class if so check each element.
    if (object.getClass() == this.getClass()) {

      Constraint tmpConstraint = (Constraint) object;

      returnValue = (element[0].equals(tmpConstraint.getElement(0)) &&
        element[1].equals(tmpConstraint.getElement(1)) &&
        element[2].equals(tmpConstraint.getElement(2)) &&
        element[3].equals(tmpConstraint.getElement(3)));

    }

    return returnValue;
  }


  /**
   * Generate a relatively unique number for the given data
   * @return A reproducible number that changes with the data
   */
  public int hashCode() {

    return element[0].hashCode() + element[1].hashCode() +
        element[2].hashCode() + element[3].hashCode();
  }


  /**
   * Creates a string representation of these constraints. A typical result
   * might be <code>[$x &lt;urn:foo&gt; 'bar' $0]</code>.
   */
  public String toString() {

    StringBuffer buffer = new StringBuffer("[");
    buffer.append(element[0]).append(" ").append(element[1]).append(" ")
        .append(element[2]).append(" ").append(element[3]);
    buffer.append("]");

    return buffer.toString();
  }
}
