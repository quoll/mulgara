/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The JRDF Project.  All rights reserved.
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
import java.net.URI;

/**
 * RDF literal node.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public interface Literal extends ObjectNode, TypedNodeVisitable, org.openrdf.model.Literal {

  /**
   * Obtain the text of this literal.
   *
   * @return the text of the literal, never <code>null</code>
   */
  String getLexicalForm();

  /**
   * Returns the language code of the literal, or <code>null</code> if no
   *     language specified.
   *
   * @return the language code of the literal, or <code>null</code> if no
   *     language specified.
   */
  String getLanguage();

  /**
   * Whether the literal is well formed XML.
   *
   * @return whether the literal is wll formed XML.
   */
  boolean isWellFormedXML();

  /**
   * Returns the URI of the RDF datatype of this resource, or <code>null</code>
   *     for an untyped node.
   *
   * @return the URI of the RDF datatype of this resource, or <code>null</code>
   *     for an untyped node.
   */
  URI getDatatypeURI();

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * A literal is equal to another literal if:
   * <ul>
   * <li> The strings of the two lexical forms compare equal, character by
   * character.
   * <li> Either both or neither have language tags.
   * <li> The language tags, if any, compare equal.
   * <li> Either both or neither have datatype URIs.
   * <li> The two datatype URIs, if any, compare equal, character by character.
   * </ul>
   *
   * @param obj the reference object with which to compare.
   * @return true if this object is the same as the obj argument; false otherwise.
   */
  boolean equals(Object obj);

  /**
   * Returns a hash-code value for this literal. The hash code is based upon
   * XORing all of the literal's components hash codes including the
   * lexical form, datatype, and language.
   *
   * @return a hash-code value for this literal.
   */
  int hashCode();

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
  String getEscapedForm();

  /**
   * Returns an escaped lexical form where double quotes and backslashes are
   * escaped.
   *
   * @return String the lexical form.
   */
  String getEscapedLexicalForm();

  /**
   * Returns the lexical form.
   *
   * @return the lexical form.
   */
  String toString();
}
