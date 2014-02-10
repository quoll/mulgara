/*
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

package org.jrdf.vocabulary;

// Java 2 standard
import java.net.URI;

/**
 * A set of constants for the standard RDF vocabulary.
 *
 * @author Andrew Newman
 */
public abstract class RDF extends Vocabulary {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 5974585938932893808L;

  /** The URI of the RDF name space. */
  public static final String DOM = "rdf";

  /** The URI of the RDF name space. */
  public static final URI BASE_URI = URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#");

  /** The class of unordered containers. */
  public static final URI BAG = URI.create(BASE_URI + "Bag");

  /** The class of ordered containers. */
  public static final URI SEQ = URI.create(BASE_URI + "Seq");

  /** The class of containers of alternatives. */
  public static final URI ALT = URI.create(BASE_URI + "Alt");

  /** The class of RDF statements. */
  public static final URI STATEMENT = URI.create(BASE_URI + "Statement");

  /** The class of RDF properties. */
  public static final URI PROPERTY = URI.create(BASE_URI + "Property");

  /** The class of XML literal values. */
  public static final URI XML_LITERAL = URI.create(BASE_URI + "XMLLiteral");

  /** The class of RDF Lists. */
  public static final URI LIST = URI.create(BASE_URI + "List");

  /**
   * A special property element that is equivalent to rdf:_1, rdf:_2 in order.
   * Only used in RDF/XML as inserting members of containers using LI normally
   * will result in duplicate instances not being recorded.
   */
  public static final URI LI = URI.create(BASE_URI + "li");

  /**
   * The empty list, with no items in it. If the rest of a list is nil then
   * the list has no more items in it.
   */
  public static final URI NIL = URI.create(BASE_URI + "nil");

  /** The subject of the subject RDF statement. */
  public static final URI SUBJECT = URI.create(BASE_URI + "subject");

  /** The predicate of the subject RDF statement. */
  public static final URI PREDICATE = URI.create(BASE_URI + "predicate");

  /** The object of the subject RDF statement. */
  public static final URI OBJECT = URI.create(BASE_URI + "object");

  /** The subject is an instance of a class. */
  public static final URI TYPE = URI.create(BASE_URI + "type");

  /** Idiomatic property used for structured values. */
  public static final URI VALUE = URI.create(BASE_URI + "value");

  /** The first item in the subject RDF list. */
  public static final URI FIRST = URI.create(BASE_URI + "first");

  /** The rest of the subject RDF list after the first item. */
  public static final URI REST = URI.create(BASE_URI + "rest");

  /** The abbreviation for the class of XML literal values. */
  public static final URI XML_LITERAL_ABBR = URI.create(DOM + ":XMLLiteral");

  static {
    // Add Classes
    classes.add(SEQ);
    classes.add(BAG);
    classes.add(ALT);
    classes.add(STATEMENT);
    classes.add(PROPERTY);
    classes.add(XML_LITERAL);
    classes.add(LIST);
    classes.add(NIL);
    classes.add(LI);

    // Add Properties
    properties.add(SUBJECT);
    properties.add(PREDICATE);
    properties.add(OBJECT);
    properties.add(TYPE);
    properties.add(VALUE);
    properties.add(FIRST);
    properties.add(REST);

    resources.addAll(classes);
    resources.addAll(properties);
  }
}
