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

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.ContextOwner;
import org.mulgara.query.filter.RDFTerm;
import org.mulgara.query.rdf.URIReferenceImpl;


/**
 * <p>An IRI value.</p>
 * <p>OK, so we're cheating.  This is a URI.</p>
 *
 * @created Mar 12, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class IRI implements RDFTerm {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 4875602788740429777L;

  /** The URI held by this object */
  private final URI value;

  private final QName qname;

  /**
   * Creates the value to wrap the uri.
   * @param u The URI to wrap
   */
  public IRI(URI u) {
    value = u;
    qname = null;
  }

  /**
   * Creates the value to wrap the uri.
   * @param u The URI to wrap
   * @param q The QName for this uri.
   */
  public IRI(URI u, QName q) {
    value = u;
    this.qname = q;
  }

  /** {@inheritDoc} */
  public URI getValue() {
    return value;
  }

  /**
   * Gets the qname for this IRI, if one exists.
   * @return The QName of the IRI, or <code>null</code> if none exists.
   */
  public QName getQName() {
    return qname;
  }

  /** {@inheritDoc} */
  public Node getJRDFValue() {
    return new URIReferenceImpl(value);
  }

  /** {@inheritDoc} */
  public boolean equals(Object o) {
    try {
      return (o instanceof RDFTerm) ? equals((RDFTerm)o) : false;
    } catch (QueryException qe) {
      return false;
    }
  }

  /** @see java.lang.Object#hashCode() */
  public int hashCode() {
    return value.hashCode();
  }

  /** {@inheritDoc} */
  public boolean equals(RDFTerm v) throws QueryException {
    return v.isIRI() && value.equals(v.getValue());
  }

  /** {@inheritDoc} */
  public boolean isBlank() { return false; }

  /** {@inheritDoc} */
  public boolean isIRI() { return true; }

  /** {@inheritDoc} */
  public boolean isURI() { return true; }

  /** {@inheritDoc} */
  public boolean isLiteral() { return false; }

  /** {@inheritDoc} */
  public boolean isGrounded() throws QueryException { return true; }

  /** {@inheritDoc} */
  public boolean sameTerm(RDFTerm v) throws QueryException {
    return equals(v);
  }

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

  public String toString() {
    return qname == null ? value.toString() : value.toString() + " (" + qname + ")";
  }

  /** @see org.mulgara.query.filter.RDFTerm#getVariables() */
  public Set<Variable> getVariables() {
    return Collections.emptySet();
  }
}
