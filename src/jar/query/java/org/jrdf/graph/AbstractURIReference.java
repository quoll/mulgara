/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003, 2004 The JRDF Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        the JRDF Project (http://jrdf.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The JRDF Project" and "JRDF" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, please contact
 *    newmana@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "JRDF"
 *    nor may "JRDF" appear in their names without prior written
 *    permission of the JRDF Project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the JRDF Project.  For more
 * information on JRDF, please see <http://jrdf.sourceforge.net/>.
 */

package org.jrdf.graph;

// Java 2 standard packages

import java.io.Serializable;
import java.net.URI;

import org.openrdf.model.util.URIUtil;

/**
 * A base implementation of an RDF {@link URIReference}.
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Andrew Newman
 * @author Paul Gearon
 */
public abstract class AbstractURIReference implements org.openrdf.model.URI, URIReference, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 9005249520402745489L;

  /** The URI of the node. */
  private URI uri;

  /** An index indicating the first character of the local name in the URI string, -1 if not yet set. */
  private int localNameIdx = -1;

  /**
   * Constructor.
   * Enforces a non-<code>null</code> and absolute <var>newUri</var> parameter.
   * @param newUri the URI to use in creation.
   * @throws IllegalArgumentException if <var>newUri</var> is <code>null</code> or
   *     not absolute
   */
  protected AbstractURIReference(URI newUri) {
    // Validate "newUri" parameter
    if (null == newUri) throw new IllegalArgumentException("Null \"newUri\" parameter");

    if (!newUri.isAbsolute()) {
      throw new IllegalArgumentException("\"" + newUri + "\" is not absolute");
    }

    // Initialize the field
    uri = newUri;
  }

  /**
   * Constructor.
   * Enforces a non-<code>null</code> parameter.  Use only for applications
   * where enforcement of valid URIs is too expensive or not necessary.
   * @param newUri the URI to use in creation.
   * @param validate whether to enforce valid RDF URIs.
   * @throws IllegalArgumentException if <var>newUri</var> is not absolute and
   *   validate is true.
   */
  protected AbstractURIReference(URI newUri, boolean validate) {
    // Validate "newUri" parameter
    if (null == newUri) throw new IllegalArgumentException("Null \"newUri\" parameter");

    if (validate && !newUri.isAbsolute()) {
      throw new IllegalArgumentException("\"" + newUri + "\" is not absolute");
    }

    // Initialize the field
    uri = newUri;
  }

  /**
   * The {@link URI} identifiying this resource.
   * @return the {@link URI} identifying this resource.
   */
  public URI getURI() {
    return uri;
  }

  /**
   * Accept a call from a TypedNodeVisitor.
   * @param visitor the object doing the visiting.
   */
  public void accept(TypedNodeVisitor visitor) {
    visitor.visitURIReference(this);
  }

  /**
   * Returns the String-representation of this URI.
   * @return The String-representation of this URI.
   */
  public String toString() {
    return uri.toString();
  }

  /**
   * Returns the String-representation of this URI.
   * @return The String-representation of this URI.
   */
  public String stringValue() {
    return uri.toString();
  }

  /**
   * Gets the namespace of this URI. The namespace is defined as per the
   * algorithm described in the class documentation.
   * @return The URI's namespace.
   */
  public String getNamespace() {
    if (!uri.isAbsolute()) return "";
    if (localNameIdx < 0) localNameIdx = URIUtil.getLocalNameIndex(uri.toString());
    return uri.toString().substring(0, localNameIdx);
  }

  /**
   * Gets the local name of this URI. The local name is defined as per the
   * algorithm described in the class documentation.
   * @return The URI's local name.
   */
  public String getLocalName() {
    if (!uri.isAbsolute()) return uri.toString();
    if (localNameIdx < 0) localNameIdx = URIUtil.getLocalNameIndex(uri.toString());
    return uri.toString().substring(localNameIdx);
  }

  /**
   * Compares a URI object to another object.
   * @param o The object to compare this URI to.
   * @return <tt>true</tt> if the other object is an instance of {@link URI}
   *         and their String-representations are equal, <tt>false</tt>
   *         otherwise.
   */
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof org.openrdf.model.URI)) return false;
    return toString().equals(o.toString());
  }

  /**
   * The hash code of a URI is defined as the hash code of its
   * String-representation: <tt>toString().hashCode</tt>.
   * @return A hash code for the URI.
   */
  public int hashCode() {
    return toString().hashCode();
  }

  public boolean isLiteral() { return false; }

  public boolean isBlankNode() { return false; }

  public boolean isURIReference() { return true; }
}
