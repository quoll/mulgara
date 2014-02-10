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

/**
 * Variable nodes. These act as a label for columns in a tuple, and not as
 * elements that are resolvable in a context (compare with
 * {@link org.mulgara.query.filter.value.Var} which does do this).
 * 
 * Comparable for the sake of ordering elements by the label and not their value.
 *
 * @created 2001-07-31
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @maintenanceAuthor <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 *
 * @copyright &copy;2001-2004 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class Variable implements SelectElement, ConstraintElement, Comparable<Variable> {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
 static final long serialVersionUID = 205307605615376038L;

  /** Description of the Field */
  public final static Variable FROM = new Variable("_from");

  /** The <var>name</var> property. */
  private final String name;

  /** Indicates the the variable is an internal variable for representing blanks nodes */
  private final boolean bNodeVar;

  /**
   * Create a new variable.
   * @param name the variable name (no leading <code>$</code> character)
   * @throws IllegalArgumentException if <var>name</var> is <code>null</code>
   */
  public Variable(String name) {
    // Validate "name" parameter
    if (name == null) throw new IllegalArgumentException("Null \"name\" parameter");
    if (name.indexOf(" ") != -1) throw new IllegalArgumentException("\"" + name + "\" is a not a variable name");
    bNodeVar = (name.indexOf("*") != -1);

    this.name = name;
  }


  /**
   * Accessor for the <var>name</var> property.
   * @return The value of the Name
   */
  public String getName() {
    return name;
  }

  /**
   * Tests if this variable represents a BNode. This is equivalent to
   * testing if the variable name contains an asterisk.
   * @return <code>true</code> if this variable represents a bnode.
   */
  public boolean isBnodeVar() {
    return bNodeVar;
  }

  /**
   * Clones this variable.
   * @return A new variable that will compare as equal to this one.
   */
  public Object clone() {
    if (name.equals("_from")) return FROM;
    else {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        throw new InternalError(e.toString());  // Should not happen
      }
    }
  }


  /**
   * Variables are equal by <var>name</var> .
   *
   * @param object The other variable to compare to
   * @return <code>true</code> if object is a Variable, and has the same name.
   */
  public boolean equals(Object object) {
    if ((object == null) ||  Variable.class != object.getClass()) return false;
    return (object == this) || name.equals(((Variable)object).name);
  }

  /**
   * Calculate a hash code that is relatively unique for this variable.
   * @return An int encoded from this variable's name.
   */
  public int hashCode() {
    return name.hashCode();
  }

  /**
   * Legible representation of the variable.
   * @return a <q>$</q> prefixed to the <var>name</var>
   */
  public String toString() {
    return "$" + name;
  }


  public int compareTo(Variable o) {
    return name.compareTo(o.name);
  }
}
