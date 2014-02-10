/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
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

// Java 2 standard
import org.jrdf.util.EscapeUtil;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import static org.mulgara.util.ObjectUtil.eq;

import java.io.Serializable;
import java.net.URI;

/**
 * A base implementation of an RDF {@link Literal}.
 *
 * @author Andrew Newman
 * @author Simon Raboczi
 *
 * @version $Revision: 624 $
 */
public abstract class AbstractLiteral extends LiteralImpl implements Literal, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 1191947630907453592L;

  /**
   * Whether the literal is well formed XML.
   */
  private boolean wellFormedXML;

  /**
   * RDF datatype URI, <code>null</code> for untyped literal.
   */
  private URI datatypeURI;

  /**
   * Construct a plain literal.
   *
   * @param newLexicalForm  the text part of the literal
   * @throws IllegalArgumentException if <var>newLexicalForm</var> is <code>null</code>
   */
  protected AbstractLiteral(String newLexicalForm) {
    super(newLexicalForm);

    // Validate "newLexicalForm" parameter
    if (null == newLexicalForm) {
      throw new IllegalArgumentException("Null \"newLexicalForm\" parameter");
    }

    // Initialize fields
    datatypeURI = null;
  }

  /**
   * Construct a literal with language.
   *
   * @param newLexicalForm  the text part of the literal
   * @param newLanguage  the language code, possibly the empty string but not
   *    <code>null</code>
   * @throws IllegalArgumentException if <var>lexicalForm</var> or
   *    <var>lang</var> are <code>null</code>
   */
  protected AbstractLiteral(String newLexicalForm, String newLanguage) {
    super(newLexicalForm, "".equals(newLanguage) ? null : newLanguage);

    // Validate "lexicalForm" parameter
    if (null == newLexicalForm) {
      throw new IllegalArgumentException("Null \"lexicalForm\" parameter");
    }

    // Validate "language" parameter
    if (null == newLanguage) {
      throw new IllegalArgumentException("Null \"language\" parameter");
    }

    // Initialize fields
    datatypeURI = null;
  }

  /**
   * Construct a datatyped literal.
   *
   * @param newLexicalForm  the text part of the literal
   * @param newDatatypeURI  the URI for a datatyped literal
   * @throws IllegalArgumentException if <var>lexicalForm</var> or
   *     <var>datatype</var> are <code>null</code>
   */
  protected AbstractLiteral(String newLexicalForm, URI newDatatypeURI) {
    super(newLexicalForm, newDatatypeURI != null ? new URIImpl(newDatatypeURI.toString()) : null);

    // Validate "lexicalForm" parameter
    if (null == newLexicalForm) {
      throw new IllegalArgumentException("Null \"lexicalForm\" parameter");
    }

    // Validate "datatype" parameter
    if (null == newDatatypeURI) {
      throw new IllegalArgumentException("Null \"datatype\" parameter");
    }

    // Initialize fields
    datatypeURI = newDatatypeURI;
  }

  /**
   * Obtain the text of this literal.
   *
   * @return the text of the literal, never <code>null</code>
   */
  public String getLexicalForm() {
    return getLabel();
  }

  /**
   * Whether the literal is well formed XML.
   *
   * @return whether the literal is wll formed XML.
   */
  public boolean isWellFormedXML() {
    return wellFormedXML;
  }

  /**
   * Returns the URI of the RDF datatype of this resource, or <code>null</code>
   *     for a plain literal.
   *
   * @return the URI of the RDF datatype of this resource, or <code>null</code>
   *     for a plain literal.
   */
  public URI getDatatypeURI() {
    return datatypeURI;
  }

  /**
   * Accept a call from a TypedNodeVisitor.
   *
   * @param visitor the object doing the visiting.
   */
  public void accept(TypedNodeVisitor visitor) {
    visitor.visitLiteral(this);
  }

  /**
   * Provide a legible representation of a literal, following the N-Triples
   * format defined in
   * <a href="http://www.w3.org/TR/2004/REC-rdf-testcases-20040210/#ntrip_strings">&sect;3.2</a>
   * of the <a href="http://www.w3.org/">W3C</a>'s (World Wide Web Consortium)
   * <a href="http://www.w3.org/TR/2004/REC-rdf-testcases-20040210">RDF Test Cases</a> Recommendation.
   *
   * Well-formed Unicode surrogate pairs in the lexical form are escaped as a
   * single 8-digit hexadecimal <code>\U</code> escape sequence rather than a
   * pair of 4-digit <code>&x5C;u</code> sequences representing the surrogates.
   *
   * @return this instance in N-Triples format
   */
  public String getEscapedForm() {
    String escaped = EscapeUtil.escapeUTF8(getLexicalForm());
    return '\"' + escaped + '\"' + appendType();
  }

  /**
   * Returns the lexical form.
   *
   * @return the lexical form.
   */
  public String toString() {
    return '\"' + getEscapedLexicalForm() + '\"' + appendType();
  }

  public String getEscapedLexicalForm() {
    return getLexicalForm().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\"",
        "\\\\\\\"");
  }

  /**
   * Appends the datatype URI or language code of a literal.
   *
   * @return String the datatype URI in the form ^^<->, or language code @- or
   *   an empty string.
   */
  private String appendType() {
    String appendString = "";

    if (null != getDatatypeURI()) {
      appendString = "^^<" + getDatatypeURI() + '>';
    }
    else if (getLanguage() != null) {
      appendString = '@' + getLanguage();
    }

    return appendString;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Literal)) return false;
    Literal l = (Literal)o;
    return eq(getLexicalForm(), l.getLexicalForm()) &&
           eq(getDatatypeURI(), l.getDatatypeURI()) &&
           eq(getLanguage(), l.getLanguage());
  }

  public int hashCode() {
    return super.hashCode();
  }

  public boolean isLiteral() { return true; }

  public boolean isBlankNode() { return false; }

  public boolean isURIReference() { return false; }

}
