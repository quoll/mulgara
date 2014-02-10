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
package org.mulgara.query.filter;

import java.io.Serializable;
import java.util.Set;

import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;


/**
 * Represents data that can appear in an expression, used by filters
 *
 * @created Mar 10, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public interface RDFTerm extends Serializable, ContextOwner {

  /**
   * Returns the data represented by this expression.
   * @return The represented data.
   * @throws QueryException Resolving the data for this value leads to an error.
   */
  public Object getValue() throws QueryException;

  /**
   * Returns the data represented by this expression as a JRDF node.
   * @return The represented data.
   * @throws QueryException Resolving the data for this value leads to an error.
   */
  public Node getJRDFValue() throws QueryException;

  /**
   * A value based equality test.
   * @param v The object to compare to
   * @return <code>true</code> iff v has the same value as the current object.
   * @throws QueryException Resolving the data for this value leads to an error,
   *                        or the types are incompatible.
   */
  public boolean equals(RDFTerm v) throws QueryException;

  /**
   * Tests if this object is a URI or IRI.
   * @return <code>true</code> if this object is an IRI.
   */
  public boolean isIRI() throws QueryException;
  
  /** @see #isIRI() */
  public boolean isURI() throws QueryException;

  /**
   * Tests if this value is a blank node
   * @return <code>true</code> if this expression is a blank node
   */
  public boolean isBlank() throws QueryException;

  /**
   * Tests if this value is a literal
   * @return <code>true</code> if this expression is a literal
   */
  public boolean isLiteral() throws QueryException;

  /**
   * Tests if this is the same term as another expression
   * @param v The expression to test against
   * @return <code>true</code> if the terms are the same
   */
  public boolean sameTerm(RDFTerm v) throws QueryException;

  /**
   * Tests if this term is based on an expression that uses unbound variables
   * or if it is grounded in a value.
   * @return <code>true</code> iff the expression is based only on grounded terms,
   *         or on variables which are themselves based on grounded terms.
   */
  public boolean isGrounded() throws QueryException;

  /**
   * Returns all the variables referred to by this term.
   * @return A set of variables.
   */
  public Set<Variable> getVariables();
}
