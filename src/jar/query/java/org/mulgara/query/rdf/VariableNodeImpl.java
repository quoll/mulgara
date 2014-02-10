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

package org.mulgara.query.rdf;


// Java 2 standard packages
import java.io.Serializable;

// JRDF
import org.jrdf.graph.*;

// Local packages
import org.mulgara.query.Value;

/**
 * A variable node used in a query.  Contains a variable name to blank node
 * binding.  Can only be used in the Subject or Object of a statement.
 *
 * @created 2004-05-13
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class VariableNodeImpl
    extends AbstractBlankNode
    implements BlankNode, Value, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = -3081099447689085329L;

  /**
   * The unique string identification of the variable.
   */
  private final String variableName;

  /**
   * Create a new variableNode.
   *
   * @param newVariableName the variable name to set.
   */
  public VariableNodeImpl(final String newVariableName) {

    variableName = newVariableName;
  }

  /**
   * Returns the variable name.
   *
   * @return the variable name.
   */
  public String getVariableName() {

    return variableName;
  }

  /**
   * Return itself - variables are immutable.
   *
   * @return a copy.
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return this;
    }
  }

  /**
   * Compare variable name for equality.  Blank node is not used in the
   * comparison.
   *
   * @param obj The object to compare against.
   * @return True if the objects have the same variable name.
   */
  public boolean equals(Object obj) {

    // Object must not be null
    if (obj != null) {

      try {
        VariableNodeImpl tmpNode = (VariableNodeImpl) obj;

        if (getVariableName().equals(tmpNode.getVariableName())) return true;
      } catch (ClassCastException cce) {
        // Leave return value to be false.
      }
    }

    return super.equals(obj);
  }

  public int hashCode() {
    return variableName.hashCode();
  }

  public String getID() {
    return variableName;
  }

  /**
   * Provide a legible representation of the variable.  Returns the variable
   * name and blank node.
   *
   * @return the variable name and blank node.
   */
  public String toString() {

    return "_variable " + variableName;
  }
}
