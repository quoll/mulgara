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
package org.mulgara.query.filter.value;

import java.util.Collections;
import java.util.Set;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.ContextOwner;
import org.mulgara.query.filter.RDFTerm;


/**
 * A blank node.
 *
 * @created Mar 12, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class BlankNodeValue implements RDFTerm {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = -3419477428127742326L;

  /** The wrapped blank node */
  private final BlankNode node;

  /**
   * Creates the value to wrap the string
   * @param node The string to wrap
   */
  public BlankNodeValue(BlankNode node) {
    this.node = node;
  }

  /** {@inheritDoc} */
  public BlankNode getValue() {
    return node;
  }

  /** {@inheritDoc} */
  public Node getJRDFValue() throws QueryException {
    return node;
  }

  /** {@inheritDoc} */
  public boolean equals(RDFTerm v) throws QueryException {
    return v.isBlank() && node.equals(v.getValue());
  }

  /** {@inheritDoc} */
  public boolean sameTerm(RDFTerm v) throws QueryException {
    return equals(v);
  }

  /** {@inheritDoc} */
  public boolean isBlank() { return true; }

  /** {@inheritDoc} */
  public boolean isIRI() { return false; }

  /** {@inheritDoc} */
  public boolean isLiteral() { return false; }

  /** {@inheritDoc} */
  public boolean isURI() { return false; }

  /** {@inheritDoc} */
  public boolean isGrounded() throws QueryException { return true; }

  /** This value does not need a context */
  public ContextOwner getContextOwner() {  return null; }

  /** This value does not need a context */
  public void setContextOwner(ContextOwner owner) { }

  /** This value does not need a context */
  public Context getCurrentContext() { return null; }

  /** This value does not need a context */
  public void setCurrentContext(Context context) { }

  /** This value does not need a context */
  public void addContextListener(ContextOwner l) { }

  public Set<Variable> getVariables() {
    return Collections.emptySet();
  }

}
