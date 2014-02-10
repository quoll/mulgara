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
import java.io.Serializable;

/**
 * A variable and its ordering, for composing <code>ORDER BY</code> clauses.
 *
 * @created 2001-07-12
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class Order implements Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 8322507935942248771L;

  /**
   * The <var>variable</var> property.
   */
  private Variable variable;

  /**
   * The <var>order</var> property.
   */
  private boolean ascending;

  /**
   * Create a new ordering
   *
   * @param variable the variable to sort
   * @param ascending whether to sort in ascending order, otherwise descending
   * @throws IllegalArgumentException if <var>variable</var> is <code>null</code>
   */
  public Order(Variable variable, boolean ascending) {

    // Validate "variable" parameter
    if (variable == null) {

      throw new IllegalArgumentException("Null \"variable\" parameter");
    }

    // Initialize fields
    this.variable = variable;
    this.ascending = ascending;
  }

  /**
   * Accessor for the <var>name</var> property.
   *
   * @return The Variable value
   */
  public Variable getVariable() {

    return variable;
  }

  /**
   * Accessor for the <var>ascending</var> property.
   *
   * @return The Ascending value
   */
  public boolean isAscending() {

    return ascending;
  }

  /**
   * Legible representation of the order. Example: <xmp>$x asc</xmp>
   *
   * @return RETURNED VALUE TO DO
   */
  public String toString() {

    return variable + (ascending ? " asc" : " desc");
  }
}
