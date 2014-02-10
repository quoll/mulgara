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
 * A transitive constraint. The elements within the constraint must be variable
 * for the subject and object, and a value for the predicate.  An optional anchor
 * is also provided, which must describe the same predicate and have exactly one
 * variable and one object.
 *
 * @created 2004-05-12
 *
 * @author Paul Gearon
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/05/29 08:32:40 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SingleTransitiveConstraint implements Constraint {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = -3828723182891026790L;

  // /** Logger. */
  // private final static Logger logger = Logger.getLogger(Constraint.class);

  /**
   * The constraint containing the transitive predicate.
   */
  private final Constraint transConstraint;

  /**
   * The anchor to be applied if this anchor is to be used for a chain.
   */
  private final boolean anchored;

  /** Indicates that a zero step must be included */
  private final boolean zeroStep;

  /**
   * Constructor.
   *
   * @param trans The constraint defining the transitive predicate. Expected form
   *        is: {$var &lt;uri&gt; $var}
   * @throws IllegalArgumentException If trans is null.
   */
  public SingleTransitiveConstraint(Constraint trans, boolean zeroStep) throws IllegalArgumentException {

    // Validate parameters
    if (trans == null) {
      throw new IllegalArgumentException("null trans parameter");
    }

    // Check if this constraint is anchored i.e. starting from a fixed subject
    // or object.
    if (!trans.getElement(0).getClass().equals(trans.getElement(2).getClass()) &&
        ((trans.getElement(0) instanceof Value) ||
        (trans.getElement(2) instanceof Value))) {
      anchored = true;
    }
    else {
      anchored = false;
    }

    // Initialize fields
    this.transConstraint = trans;
    this.zeroStep = zeroStep;
  }

  public SingleTransitiveConstraint(Constraint trans) throws IllegalArgumentException {
    this(trans, false);
  }

  public boolean isRepeating() {
    return false;
  }

  /**
   * Get the transitive constraint that describes the predicate.
   *
   * @return The constraint element referred to by index.
   */
  public Constraint getTransConstraint() {
    return transConstraint;
  }

  /**
   * Check if this Transitive Rule is anchored.
   *
   * @return <code>true</code> if this constraint is anchored, <code>false</code> otherwise.
   */
  public boolean isAnchored() {
    return anchored;
  }

  /**
   * Get all constraints which are variables. For back-compatibility, this
   * method currently ignores the fourth element of the triple.
   *
   * @return A set containing all variable constraints.
   */
  public Set<Variable> getVariables() {
    return transConstraint.getVariables();
  }


  /**
   * Equality is by value.
   *
   * @param object PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }

    if (object == this) {
      return true;
    }

    boolean returnValue = false;

    // Check that the given object is the correct class if so check each
    // element.
    if (object.getClass() == SingleTransitiveConstraint.class) {
      returnValue = transConstraint.equals(((SingleTransitiveConstraint)object).transConstraint);
    }

    return returnValue;
  }


  /**
   * Creates a relatively unique value representing this constraint.
   *
   * @return A numerical combination of the elements and the anchor
   */
  public int hashCode() {
    return transConstraint.hashCode();
  }

  /**
   * Get a constraint element by index.
   *
   * @param index The constraint element to retrieve, from 0 to 3.
   * @return The constraint element referred to by index.
   */
  public ConstraintElement getElement(int index) {
    return transConstraint.getElement(index);
  }

  public ConstraintElement getModel() {
    return transConstraint.getModel();
  }

  /**
   * Creates a string representation of these constraints. A typical result
   * might be <code>[$x &lt;urn:foo&gt; 'bar' $0 ] :
   * {&lt;start&gt; &lt;urn:foo&gt; &lt;end&gt;}</code>.
   *
   * @return String containing all data pertinent to the constraint.
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer("trans[");
    buffer.append(transConstraint);
    buffer.append("]");
    if (anchored) {
      buffer.append(":{anchored}");
    }
    return buffer.toString();
  }

  /**
   * If true then this constraint requires a zero step to be included in the results.
   * @return <code>true</code> when results need to include a zero step.
   */
  public boolean isZeroStep() {
    return zeroStep;
  }

  /**
   * Not sure what associativity would mean here, but it shouldn't be possible.
   * @return <code>false</code> to indicate that this operation is not associative.
   */
  public boolean isAssociative() {
    return false;
  }

}
