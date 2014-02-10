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
 * A constant value occuring in the <code>SELECT</code> clause.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @created 2003-Jul-08
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2003 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ConstantValue implements SelectElement, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 3820524293803821921L;

  /**
   * The column name.
   */
  private Variable variable;

  /**
   * The embedded value.
   */
  private Value value;

  /**
   * This constructor ensures that all subclasses set the <var>value</var>
   * property.
   *
   * @param variable the column name
   * @param value the value of the constant
   * @throws IllegalArgumentException if <var>value</var> is <code>null</code>
   */
  public ConstantValue(Variable variable, Value value) {

    // Validate "variable" parameter
    if (variable == null) {

      throw new IllegalArgumentException("Null \"variable\" parameter");
    }

    // Validate "value" parameter
    if (value == null) {

      throw new IllegalArgumentException("Null \"value\" parameter");
    }

    // Initialize fields
    this.variable = variable;
    this.value = value;
  }

  /**
   * @return the column name
   */
  public Variable getVariable() {

    return variable;
  }

  /**
   * @return the embedded value
   */
  public Value getValue() {

    return value;
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public String toString() {

    return variable + "=" + value;
  }

  /*
     //
     // Method implementing the Transformable interface
     //
     public void transform(Transformation transformation)
    throws TransformationException
     {
    query.transform(transformation);
     }
   */
}
