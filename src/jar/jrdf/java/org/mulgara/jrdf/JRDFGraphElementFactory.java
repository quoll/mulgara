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

package org.mulgara.jrdf;

// Java 2 standard packages

// Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;
import java.net.URI;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.server.*;

/**
 * Creates Mulgara implemented JRDF Graph Nodes and Triples.
 *
 * @created 2004-10-12
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/07 05:49:05 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class JRDFGraphElementFactory implements GraphElementFactory {
  /**
   * Logger. This is named after the class.
   */
  private static final Logger logger =
      Logger.getLogger(JRDFGraphElementFactory.class.getName());

  /**
   * Resolver session to use for localization.
   */
  private LocalJRDFSession jrdfSession;

  /**
   * Default Constructor.
   *
   * @param existingSession resolver session to use for localization.
   */
  public JRDFGraphElementFactory (LocalJRDFSession existingSession) {
    jrdfSession = existingSession;
  }

  /**
   * Returns a new BlankNode.
   *
   * @return BlankNode
   */
  public BlankNode createResource() {
    return createBlankNode();
  }

  /**
   * Create a new blank node .
   *
   * @return a newly minted blank node.
   */
  public BlankNode createBlankNode() {
    BlankNode node = new BlankNodeImpl();
    return node;
  }

  /**
   * Returns a new URIResource. The uri is validated.
   *
   * @param uri URI
   * @return URIReference
   */
  public URIReference createResource(URI uri) {
    return createResource(uri, true);
  }

  /**
   * Returns a new URIResource.
   *
   * @param uri URI
   * @param validate boolean
   * @return URIReference
   */
  public URIReference createResource(URI uri, boolean validate) {
    return new URIReferenceImpl(uri, validate);
  }

  /**
   * Returns a new Literal with no language or datatype.
   *
   * @param lexicalValue String
   * @return Literal
   */
  public Literal createLiteral(String lexicalValue) {
    return new LiteralImpl(lexicalValue);
  }

  /**
   * Returns a new Literal with the specified language.
   *
   * @param lexicalValue String
   * @param languageType String
   * @return Literal
   */
  public Literal createLiteral(String lexicalValue, String languageType) {
    return new LiteralImpl(lexicalValue, languageType);
  }

  /**
   * Returns a new Literal with the specified datatype.
   *
   * @param lexicalValue String
   * @param datatypeURI URI
   * @return Literal
   */
  public Literal createLiteral(String lexicalValue, URI datatypeURI) {
    return new LiteralImpl(lexicalValue, datatypeURI);
  }

  /**
   * Returns a triple containing the Subject, Predicate and Object Nodes.
   *
   * @param subject SubjectNode
   * @param predicate PredicateNode
   * @param object ObjectNode
   * @return Triple
   */
  public Triple createTriple(SubjectNode subject, PredicateNode predicate,
      ObjectNode object) {
    return new TripleImpl(subject, predicate, object);
  }
}
