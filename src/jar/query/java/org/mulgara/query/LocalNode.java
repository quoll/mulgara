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
 * Nodes representing resources, literal, or semantics-free graph vertices.
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @created 2001-07-13
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/22 08:16:04 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LocalNode implements Comparable<LocalNode>, Value {

  /** Serialization ID */
  private static final long serialVersionUID = 2569319494439016623L;

  /**
   * The <var>value</var> property.
   */
  protected long value;

  /**
   * Constructor.
   *
   * @param value the node id.
   */
  public LocalNode(long value) {

    this.value = value;
  }

  /**
   * Protected constructor for use with Database.
   */
  protected LocalNode() {

  }

  /**
   * Accessor for the <var>value</var> property. Will return a negative value if
   * the local node is a query node ie. only created when querying.
   *
   * @return the value of <var>value</var> .
   */
  public long getValue() {

    return value;
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public ConstraintElement copy() {

    return new LocalNode(value);
  }

  //
  // Method implementing Comparable
  //

  /**
   * Nodes compare based on their node number.
   *
   * @param node the object to compare against.
   * @return -1 if less than, 0 if equal, +1 if greater than the argument.
   * @throws IllegalArgumentException if the object is not the correct type.
   */
  public int compareTo(LocalNode node) throws IllegalArgumentException {

      long ov = node.getValue();

      return (ov == value) ? 0 : ( (ov < value) ? ( -1) : 1);
  }

  //
  // Methods overriding Object
  //

  /**
   * Tests if another Node is equal to this one. Nodes are equal by value.
   *
   * @param object the object to compare against.
   * @return <code>true</code> If the nodes match.
   */
  public boolean equals(Object object) {

    boolean result;

    if (object == null) {
      return false;
    }

    try {

      LocalNode localNode = (LocalNode) object;
      result = (getValue() == localNode.getValue());
    }
    catch (ClassCastException cce) {

      result = false;
    }

    return result;
  }

  /**
   * The hashcode is equal to the node number.
   *
   * @return RETURNED VALUE TO DO
   */
  public int hashCode() {

    return (int) value;
  }

  /**
   * The textual representation of a local node is the node number, prefixed
   * with the letters <q>gn</q> for global node or <q>qn</q> for query node. For
   * example, <code>gn123</code> or <code>qn1</code>.
   *
   * @return RETURNED VALUE TO DO
   */
  public String toString() {

    String strValue;

    if (value < 0) {

      strValue = "qn" + (value * -1);
    }
    else {

      strValue = "gn" + value;
    }

    return strValue;
  }
}
