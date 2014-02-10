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

package org.mulgara.descriptor;

/**
 * @created 2002-03-15
 *
 * @author Keith Ahern
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:11 $
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
public class Param {

  /**
   * Description of the Field
   */
  private String name = null;

  /**
   * Description of the Field
   */
  private Object value = null;

  /**
   * Description of the Field
   */
  private boolean required;

  /**
   * CONSTRUCTOR Param TO DO
   *
   * @param name PARAMETER TO DO
   * @param value PARAMETER TO DO
   */
  public Param(String name, Object value) {
    this(name, value, false);
  }

  /**
   * CONSTRUCTOR Param TO DO
   *
   * @param name PARAMETER TO DO
   * @param value PARAMETER TO DO
   * @param required PARAMETER TO DO
   */
  public Param(String name, Object value, boolean required) {

    if (name == null) {

      throw new IllegalArgumentException("Name must not be null!");
    }

    this.name = name;
    this.value = value;
    this.required = required;
  }

  /**
   * METHOD TO DO
   *
   * @param params PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public static String toString(Param[] params) {

    StringBuffer out = new StringBuffer();

    for (int i = 0; i < params.length; i++) {

      out.append(i);
      out.append(" = ");
      out.append(params[i].toString());
      out.append(", ");
    }

    return out.toString();
  }

  /**
   * Sets the Value attribute of the Param object
   *
   * @param value The new Value value
   */
  public void setValue(Object value) {

    this.value = value;
  }

  /**
   * Gets the Name attribute of the Param object
   *
   * @return The Name value
   */
  public String getName() {

    return name;
  }

  /**
   * Gets the Value attribute of the Param object
   *
   * @return The Value value
   */
  public Object getValue() {

    return value;
  }

  /**
   * Gets the Required attribute of the Param object
   *
   * @return The Required value
   */
  public boolean isRequired() {

    return required;
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public String toString() {

    return name + "=" + value + " required = " + required;
  }
}
