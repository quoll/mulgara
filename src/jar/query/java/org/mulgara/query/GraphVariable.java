/**
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.query;

// Java 2 standard packages
import java.net.*;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;

/**
 * A leaf expression containing a variable that is used for the model expression.
 *
 * @created Apr 22, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class GraphVariable implements Graph {

  /** Used for serializing. */
  static final long serialVersionUID = 5132086338306266830L;

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(GraphVariable.class);

  /** The variable for the graph */
  private Variable variable;

  //
  // Constructors
  //

  /**
   * Construct a model from a variable.
   *
   * @param variable The variable this expression represents.
   * @throws IllegalArgumentException if <var>url</var> is <code>null</code>
   */
  public GraphVariable(Variable variable) {
    if (variable == null) throw new IllegalArgumentException("Null variable parameter");
    this.variable = variable;
  }

  //
  // Methods implementing GraphExpression
  //

  /**
   * Gets a set of database URIs to operate against.
   * @return We don't know what is in the variable, so return the empty {@link Set}
   */
  public Set<URI> getDatabaseURIs() {
    return Collections.emptySet();
  }

  /**
   * Gets a set of graph URIs this represents.
   * @return We don't know what is in the variable, so return the empty {@link Set}
   */
  public Set<URI> getGraphURIs() {
    return Collections.emptySet();
  }

  //
  // API methods
  //

  /**
   * Accessor for the <var>variable</var> property.
   * @return a {@link Variable} instance
   */
  public Variable getVariable() {
    return variable;
  }

  //
  // Methods extending Object
  //

  /**
   * The text representation of the URI.
   * @return the text representation of the URI.
   */
  public String toString() {
    return variable.toString();
  }

  //
  // Methods overriding Object
  //

  /**
   * Return true if the variables of a GraphVariable are equal.
   * @param object GraphVariable to test equality.
   * @return true if the variables of a GraphVariable are equal.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (object == this) return true;

    if (!(object instanceof GraphVariable)) return false;
    GraphVariable modelVar = (GraphVariable)object;
    return variable.equals(modelVar.variable);
  }

  /**
   * Returns the hashCode of a Variable.
   * @return the hashCode of a Variable.
   */
  public int hashCode() {
    return variable.hashCode();
  }

  /**
   * Returns just the default Object clone.
   * @return just the default Object clone.
   */
  public Object clone() {
    try {
      GraphVariable cloned = (GraphVariable)super.clone();
      cloned.variable = variable;
      return cloned;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("GraphVariable not cloneable");
    }
  }

}
