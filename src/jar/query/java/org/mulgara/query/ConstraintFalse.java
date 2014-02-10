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

import java.util.*;

/**
 * A constraint expression with zero rows.
 *
 * @created 2002-02-20
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
public class ConstraintFalse implements ConstraintExpression {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 892968107239335073L;

  /**
   * The unique instance.
   *
   */
  public final static ConstraintFalse INSTANCE = new ConstraintFalse();

  //
  // Constructor
  //

  /**
   * CONSTRUCTOR ConstraintFalse TO DO
   *
   */
  private ConstraintFalse() {

  }

  /**
   * Gets the CanonicalForm attribute of the ConstraintFalse object
   *
   * @return The CanonicalForm value
   */
  public ConstraintExpression getCanonicalForm() {
    return this;
  }

  /**
   * Gets the Rows attribute of the ConstraintFalse object
   *
   * @return The Rows value
   */
  public long getRows() {
    return 0;
  }

  /**
   * Get all constraints which are variables. As this an invariant with no
   * variables it returns an empty set.
   *
   * @return A set containing all variable constraints.
   */
  public Set<Variable> getVariables() {
    return new HashSet<Variable>();
  }

  //
  // Methods implementing ConstraintExpression
  //

  /**
   * METHOD TO DO
   *
   * @param graphExpression PARAMETER TO DO
   * @param transformation PARAMETER TO DO
   * @param modelProperty PARAMETER TO DO
   * @param systemModel PARAMETER TO DO
   * @param variableFactory PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public ConstraintExpression from(GraphExpression graphExpression,
      Transformation transformation, Value modelProperty, Value systemModel,
      VariableFactory variableFactory) {
    return this;
  }

  /**
   * Falsehood is invariant between coordinate systems, so this is an identity
   * transformation.
   *
   * @param transformation The {@link Transformation} to apply.
   */
  public void transform(Transformation transformation) {
  }

  /**
   * Convert this object to a string.
   *
   * @return A string representation of this object.
   */
  public String toString() {
    return "no";
  }

  /**
   * This expression is unary, so associativity is irrelevant.
   * @return <code>false</code> to indicate that this operation is not associative.
   */
  public boolean isAssociative() {
    return false;
  }

  public boolean equals(Object o) {
    return o == INSTANCE;
  }
  
  public int hashCode() {
    return super.hashCode();
  }
}
