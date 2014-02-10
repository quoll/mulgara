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

package org.mulgara.client.jrdf;

// Java 2 standard packages

// Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;
import java.net.URI;
import org.jrdf.graph.mem.*;
import org.mulgara.client.jrdf.util.*;

/**
 * Builder class used to create JRDF Graph objects (eg. Nodes and Triples).
 *
 * @created 2004-07-29
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/27 11:36:13 $
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
public class GraphElementBuilder implements GraphElementFactory {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(GraphElementBuilder.class.getName());

  /** Does most of the work */
  private GraphElementFactory factory = null;

  /**
   * Default Constructor
   *
   */
  public GraphElementBuilder() throws GraphException {

    super();

    //get an in-memory factory
    this.factory = new GraphImpl().getElementFactory();
  }

  /**
   * Create a blank nodes that is associated with a specific graph.
   *
   * @return A new blank node within the graph.
   * @return the newly created blank node value.
   */
  public BlankNode createResource() throws GraphElementFactoryException {

    return factory.createResource();
  }

  /**
   * Create a URI reference.
   *
   * @param uri The URI of the resource.
   * @return the newly created URI reference value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  public URIReference createResource(URI uri) throws
      GraphElementFactoryException {

    return factory.createResource(uri);
  }

  /**
   * Create a URI reference without checking if the URI given is a valid RDF
   * URI, currently if the URI is absolute.
   *
   * @param uri The URI of the resource.
   * @param validate true if we disbale checking to see if the URI is valid.
   * @return The newly created URI reference value.
   * @throws GraphElementFactoryException
   */
  public URIReference createResource(URI uri, boolean validate) throws
      GraphElementFactoryException {

    return factory.createResource(uri, validate);
  }

  /**
   * Creates a new literal with the given lexical value, with no language or
   * datatype.
   *
   * @param lexicalValue The lexical value for the literal.
   * @return the newly created literal value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  public Literal createLiteral(String lexicalValue) throws
      GraphElementFactoryException {

    return factory.createLiteral(lexicalValue);
  }

  /**
   * Creates a new literal with the given lexical value, with a given language
   * but no datatype.
   *
   * @param lexicalValue The lexical value for the literal.  Cannot be null.
   * @param languageType The language of the literal or null if not required.
   * @return the newly created literal value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  public Literal createLiteral(String lexicalValue, String languageType) throws
      GraphElementFactoryException {

    return factory.createLiteral(lexicalValue, languageType);
  }

  /**
   * Creates a new literal with the given lexical value and given datatype.
   *
   * @param lexicalValue The lexical value for the literal.  Cannot be null.
   * @param datatypeURI The URI of the datatype of the literal or null if not
   *     required.
   * @return the newly created literal value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  public Literal createLiteral(String lexicalValue, URI datatypeURI) throws
      GraphElementFactoryException {

    return factory.createLiteral(lexicalValue, datatypeURI);
  }

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
  public Triple createTriple(SubjectNode subject, PredicateNode predicate,
                             ObjectNode object) throws
      GraphElementFactoryException {

    return factory.createTriple(subject, predicate, object);
  }

  /**
   * Factory method used to create an iterator for the Triples.
   *
   * @param triples Triple[]
   * @return ClientClosableIterator
   */
  public ClientClosableIterator<Triple> createClosableIterator(RemoteGraphProxy creator, Triple [] triples) {

    ClosableArrayIteratorProxy proxy = new ClosableArrayIteratorProxy(triples);

    return new ClientClosableIterator<Triple>(creator, proxy);
  }
}
