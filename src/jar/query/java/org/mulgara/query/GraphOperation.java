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
import java.net.URI;
import java.util.*;

/**
 * A model expression composed of two subexpressions and a dyadic operator.
 *
 * @created 2001-07-12
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $
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
public abstract class GraphOperation implements GraphExpression {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 7647456202844495785L;

  /**
   * The two operands.
   */
  private GraphExpression lhs;

  /**
   * The two operands.
   */
  private GraphExpression rhs;

  //
  // Constructor
  //

  /**
   * Construct a model operation. Subclasses are compelled to use this
   * constructor, guaranteeing that the operands are always
   * non-<code>null</code>.
   *
   * @param lhs a non-<code>null</code> model expression
   * @param rhs another non-<code>null</code> model expression
   */
  protected GraphOperation(GraphExpression lhs, GraphExpression rhs) {

    // Validate "lhs" parameter
    if (lhs == null) {

      throw new IllegalArgumentException("Null \"lhs\" parameter");
    }

    // Validate "rhs" parameter
    if (rhs == null) {

      throw new IllegalArgumentException("Null \"rhs\" parameter");
    }

    // Initialize fields
    this.lhs = lhs;
    this.rhs = rhs;
  }

  //
  // Methods implementing the GraphExpression abstract class
  //

  /**
   * Gets the DatabaseURIs attribute of the GraphOperation object
   *
   * @return The DatabaseURIs value
   */
  public Set<URI> getDatabaseURIs() {

    Set<URI> databaseURIs = new HashSet<URI>();
    databaseURIs.addAll(lhs.getDatabaseURIs());
    databaseURIs.addAll(rhs.getDatabaseURIs());

    return databaseURIs;
  }

  /**
   * Calculate the graphs involved in this expression.
   *
   * @return a set containing the {@link URI}s of the graphs
   */
  public Set<URI> getGraphURIs() {
    
    Set<URI> graphURIs = new HashSet<URI>();
    graphURIs.addAll(lhs.getGraphURIs());
    graphURIs.addAll(rhs.getGraphURIs());

    return graphURIs;
  }

  //
  // Additional API
  //

  /**
   * Accessor for the <var>lhs</var> property.
   *
   * @return The LHS value
   */
  public GraphExpression getLHS() {

    return lhs;
  }

  /**
   * Accessor for the <var>rhs</var> property.
   *
   * @return The RHS value
   */
  public GraphExpression getRHS() {

    return rhs;
  }

  /**
   * Transform to an equivalent WHERE clause expression.
   *
   * @param m The object to compare against.
   * @return <code>true</code> if the objects are the same type,
   *         and applied to the same operands 
   */
  public boolean equals(Object m) {
    
    if (!(m instanceof GraphOperation)) return false;
    if ((m == null) || m.getClass() !=getClass()) return false;
    if (m == this) return true;

    Class<?> type = m.getClass();

    Set<GraphExpression> otherExpressions = new HashSet<GraphExpression>();
    ((GraphOperation)m).flattenExpression(otherExpressions, type);

    Set<GraphExpression> myExpressions = new HashSet<GraphExpression>();
    flattenExpression(myExpressions, type);

    return myExpressions.equals(otherExpressions);
  }

  /**
   * Creates a hash code, based on the child expressions and the current operation type.
   *
   * @return The hash code for this object.
   */
  public int hashCode() {

    Set<GraphExpression> myExpressions = new HashSet<GraphExpression>();
    flattenExpression(myExpressions, getClass());

    return (getClass().hashCode() * 7) + myExpressions.hashCode();
  }

  /**
   * Traverse down the binary tree of the current object, and merge any nodes
   * of the current type into a flattened set.
   *
   * @param expressions The set to be built up containing all nodes being
   *        operated on in the same way.
   * @param type The class representing the operation type.
   */
  private void flattenExpression(Set<GraphExpression> expressions, Class<?> type) {

    if (lhs.getClass() == type) {
      ((GraphOperation)lhs).flattenExpression(expressions, type);
    } else {
      expressions.add(lhs);
    }

    if (rhs.getClass() == type) {
      ((GraphOperation)rhs).flattenExpression(expressions, type);
    } else {
      expressions.add(rhs);
    }
  }

  /**
   * Clones sets of models in the rhs and lhs objects.
   */
  public Object clone() {

    try {
      GraphOperation cloned = (GraphOperation)super.clone();

      // Copy database URIs.
      cloned.lhs = (GraphExpression)lhs.clone();
      cloned.rhs = (GraphExpression)rhs.clone();

      return cloned;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("GraphOperation subclass " + getClass() + " not cloneable");
    }
  }


  public String toString() {
    return getClass().toString() + ":[(" + lhs.toString() + ") . (" + rhs.toString() +")]";
  }
}
