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

/**
 * A constraint expression composed of the conjunction (logical AND) of two
 * subexpressions.
 *
 * @created 2001-08-12
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
public class ConstraintConjunction extends ConstraintOperation {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.l
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 7601600010765365077L;

  //
  // Constructor
  //

  /**
   * Construct a constraint conjunction.
   *
   * @param lhs a non-<code>null</code> constraint expression
   * @param rhs another non-<code>null</code> constraint expression
   */
  public ConstraintConjunction(ConstraintExpression lhs, ConstraintExpression rhs) {
    super(lhs, rhs);
  }

  /**
   * Build a conjunction with a {@link List} of several children constraint expressions
   * @param elements a {@link List} of constraint expressions.
   */
  public ConstraintConjunction(List<ConstraintExpression> elements) {
    super(elements);
  }

  /**
   * Build a conjunction with a {@link Collection} of several children constraint expressions
   * @param elements a {@link Collection} of {@link ConstraintExpression}s.
   */
  public ConstraintConjunction(Collection<ConstraintExpression> elements) {
    super(new ArrayList<ConstraintExpression>(elements));
  }


  /**
   * Build a conjunction with a {@link Collection} of several children constraint expressions
   * @param elements a {@link Collection} of {@link ConstraintExpression}s.
   */
  public ConstraintConjunction(ConstraintExpression... elements) {
    super(Arrays.asList(elements));
  }


  /**
   * Gets the Filtered attribute of the ConstraintConjunction object
   *
   * @return The Filtered value
   */
  public ConstraintConjunction getFiltered() {

    List<ConstraintExpression> elements = new ArrayList<ConstraintExpression>(this.getElements());
    filter(elements);

    return new ConstraintConjunction(elements);
  }


  /**
   * Gets the Name attribute of the ConstraintConjunction object
   *
   * @return The Name value
   */
  String getName() {
    return " and ";
  }
}
