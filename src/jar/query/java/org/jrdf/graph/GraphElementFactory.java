/*
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

// Java 2 standard packages
import java.net.URI;

/**
 * A Graph Element Factory is a class which creates the various components of a
 * graph including: resources, literals and triples.  It is generally tied to a
 * specific graph.
 *
 * @author Andrew Newman
 */
public interface GraphElementFactory {

  /**
   * Create a blank nodes that is associated with a specific graph.
   *
   * @return the newly created blank node value.
   * @throws GraphElementFactoryException if adding a blank node fails.
   */
  BlankNode createResource() throws GraphElementFactoryException;

  /**
   * Create a URI reference.
   *
   * @param uri The URI of the resource.
   * @return the newly created URI reference value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  URIReference createResource(URI uri) throws GraphElementFactoryException;

  /**
   * Create a URI reference without checking if the URI given is a valid RDF
   * URI, currently if the URI is absolute.
   *
   * @param uri The URI of the resource.
   * @param validate true if we disbale checking to see if the URI is valid.
   * @return The newly created URI reference value.
   * @throws GraphElementFactoryException
   */
  URIReference createResource(URI uri, boolean validate) throws GraphElementFactoryException;

  /**
   * Creates a new literal with the given lexical value, with no language or
   * datatype.
   *
   * @param lexicalValue The lexical value for the literal.
   * @return the newly created literal value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  Literal createLiteral(String lexicalValue) throws GraphElementFactoryException;

  /**
   * Creates a new literal with the given lexical value, with a given language
   * but no datatype.
   *
   * @param lexicalValue The lexical value for the literal.  Cannot be null.
   * @param languageType The language of the literal or null if not required.
   * @return the newly created literal value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  Literal createLiteral(String lexicalValue, String languageType)
      throws GraphElementFactoryException;

  /**
   * Creates a new literal with the given lexical value and given datatype.
   *
   * @param lexicalValue The lexical value for the literal.  Cannot be null.
   * @param datatypeURI The URI of the datatype of the literal or null if not required.
   * @return the newly created literal value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  Literal createLiteral(String lexicalValue, URI datatypeURI) throws GraphElementFactoryException;

  /**
   * Creates a new triple to be used in the graph.  Does not add it to an
   * associated graph.  Use @see Graph#add.
   *
   * @param subject The subject of the statement.
   * @param predicate The predicate of the statement.
   * @param object The object of the statement.
   * @return the newly created triple object.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  Triple createTriple(SubjectNode subject, PredicateNode predicate, ObjectNode object) throws GraphElementFactoryException;
}
