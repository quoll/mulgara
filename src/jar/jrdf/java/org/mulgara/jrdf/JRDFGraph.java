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
import java.net.*;
import java.util.*;

// Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;
import org.jrdf.graph.Graph;
import org.jrdf.util.ClosableIterator;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.server.*;

/**
 * A {@link org.jrdf.graph.Graph} contained within this database. Represents a
 * Graph/Resolver via an URI and a Session.
 *
 * @created 2004-10-12
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/07 05:48:17 $
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
public class JRDFGraph implements Graph {

  private static final long serialVersionUID = -7013950226111362834L;

  /**
   * Logger. This is named after the class.
   */
  private final static Logger logger =
      Logger.getLogger(JRDFGraph.class.getName());

  /**
   * The current session.
   */
  private LocalJRDFSession session = null;

  /**
   * Graph URI.
   */
  private URI graphURI = null;

  /**
   * Used to create Nodes and Triples for this Graph.
   */
  private JRDFGraphElementFactory elementFactory = null;

  /**
   * Used to reify Triples and add containers and collections.
   */
  private JRDFTripleFactory tripleFactory = null;

  /**
   * Create an object mapped to a given graph for an existing database. It will
   * create the graph in the database if it doesn't exist. This will always
   * create a new session to the database.
   *
   * @param existingSession an existing database implementation that has
   *   already been created. Must be a Local JRDF Session.
   * @param newGraphURI the URI of the graph to use and possibly create.
   * @throws GraphException
   */
  public JRDFGraph(JRDFSession existingSession,
      URI newGraphURI) throws GraphException {

    //validate
    if (!(existingSession instanceof LocalJRDFSession)) {
      throw new IllegalArgumentException("'existingSession is not a " +
          "local JRDFSession implementation.");
    }

    try {

      // Set model URI
      graphURI = newGraphURI;

      // Get session and create model
      session = (LocalJRDFSession) existingSession;

      //instantiate Factories
      elementFactory = new JRDFGraphElementFactory(session);
      tripleFactory = new JRDFTripleFactory(this, elementFactory);

      // Create model if it doesn't exist.
      if (!session.modelExists(graphURI)) {
        session.createModel(graphURI, new URI(Mulgara.NAMESPACE + "Model"));
      }
    }
    catch (URISyntaxException uriException) {
      throw new GraphException("Bad graph URI", uriException);
    }
    catch (QueryException queryException) {
      throw new GraphException("Error creating new model", queryException);
    }
  }

  /**
   * Returns the number of triples in the graph.
   *
   * @return the number of triples in the graph.
   */
  public long getNumberOfTriples() {
    return session.getNumberOfTriples(graphURI);
  }

  /**
   * Returns true if the graph is empty.
   *
   * A graph is empty if the number of triples is 0.
   *
   * @return true if the graph is empty
   */
  public boolean isEmpty() {
    return session.getNumberOfTriples(graphURI) == 0;
  }

  /**
   * Test the graph for the occurrence of a triple.
   *
   * @param subject The subject.
   * @param predicate The predicate.
   * @param object The object.
   * @return True if the triple is found in the model, otherwise false.
   * @throws GraphException If there was an error accessing the model.
   */
  public boolean contains(SubjectNode subject, PredicateNode predicate,
      ObjectNode object) throws GraphException {

    Triple triple = elementFactory.createTriple(subject, predicate, object);
    return contains(triple);
  }

  /**
   * Test the graph for the occurrence of a triple.
   *
   * @param triple The triple.
   * @return True if the triple is found in the graph, otherwise false.
   * @throws GraphException If there was an error accessing the graph.
   */
  public boolean contains(Triple triple) throws GraphException {

    //ask the session
    return session.contains(graphURI, triple.getSubject(), triple.getPredicate(),
        triple.getObject());
  }

  /**
   * Returns an iterator to a set of statements that match a given subject,
   * predicate and object. A null value for any of the parts of a triple are
   * treated as unconstrained, any values will be returned.
   *
   * @param subject The subject to find or null to indicate any subject.
   * @param predicate The predicate to find or null to indicate any predicate.
   * @param object ObjectNode The object to find or null to indicate any object.
   * @throws GraphException If there was an error accessing the graph.
   * @return ClosableIterator
   */
  public ClosableIterator<Triple> find(SubjectNode subject, PredicateNode predicate,
      ObjectNode object) throws GraphException {

    return find(elementFactory.createTriple(subject, predicate, object));
  }

  /**
   * Returns an iterator to a set of statements that match a given subject,
   * predicate and object. A null value for any of the parts of a triple are
   * treated as unconstrained, any values will be returned.
   *
   * @param triple The triple to find.
   * @throws GraphException If there was an error accessing the graph.
   * @return ClosableIterator
   */
  public ClosableIterator<Triple> find(Triple triple) throws GraphException {
    Answer answer = session.find(graphURI, triple.getSubject(),
        triple.getPredicate(), triple.getObject());
    return new AnswerClosableIteratorImpl(answer, this);
  }

  /**
   * Adds a triple to the graph.
   *
   * @param triple The triple to add.
   * @throws GraphException If there was an error adding the triple.
   * @throws IllegalArgumentException if the triple object or any of the
   *     subject, predicate or object nodes are null.
   */
  public void add(Triple triple) throws GraphException,
      IllegalArgumentException {

    if (triple == null) {
      throw new IllegalArgumentException("Null \"triple\" parameter");
    }

    try {

      session.insert(graphURI, Collections.singleton(triple));
    }
    catch (QueryException queryException) {

      throw new GraphException("Failed to add Triple.", queryException);
    }
  }

  /**
   * Adds an iterator containing triples into the graph.
   *
   * @param triples The triple iterator.
   * @throws GraphException If the statements can't be made.
   */
  public void add(Iterator<Triple> triples) throws GraphException {

    //validate
    if (triples == null) {

      throw new IllegalArgumentException("Null \"Iterator\" parameter");
    }

    try {

      //add each Triple
      while (triples.hasNext()) {

        this.add(triples.next());
      }
    }
    catch (ClassCastException castException) {

      //re-throw
      throw new GraphException("Object is not of type \"Triple\".",
          castException);
    }
  }

  /**
   * Adds a triple to the graph.
   *
   * @param subject The subject.
   * @param predicate The predicate.
   * @param object The object.
   * @throws GraphException If there was an error adding the triple.
   * @throws IllegalArgumentException if any of the subject, predicate or
   *     object nodes are null.
   */
  public void add(SubjectNode subject, PredicateNode predicate,
      ObjectNode object) throws GraphException,
      IllegalArgumentException {

    // Validate "subject" parameter
    if (subject == null) {
      throw new IllegalArgumentException("Null \"subject\" parameter");
    }

    // Validate "predicate" parameter
    if (predicate == null) {
      throw new IllegalArgumentException("Null \"predicate\" parameter");
    }

    // Validate "object" parameter
    if (object == null) {
      throw new IllegalArgumentException("Null \"object\" parameter");
    }

    add(elementFactory.createTriple(subject, predicate, object));
  }

  /**
   * Removes a triple from to the graph.
   *
   * @param subject The subject.
   * @param predicate The predicate.
   * @param object The object.
   * @throws GraphException If there was an error revoking the triple, for
   *     example if it didn't exist.
   * @throws IllegalArgumentException if any of the subject, predicate or
   *     object nodes are null.
   */
  public void remove(SubjectNode subject, PredicateNode predicate,
      ObjectNode object) throws GraphException,
      IllegalArgumentException {

    // Validate "subject" parameter
    if (subject == null) {
      throw new IllegalArgumentException("Null \"subject\" parameter");
    }

    // Validate "predicate" parameter
    if (predicate == null) {
      throw new IllegalArgumentException("Null \"predicate\" parameter");
    }

    // Validate "object" parameter
    if (object == null) {
      throw new IllegalArgumentException("Null \"object\" parameter");
    }

    remove(elementFactory.createTriple(subject, predicate, object));
  }

  /**
   * Removes a triple to the graph.
   *
   * @param triple the triple to remove.
   * @throws GraphException If there was an error revoking the triple, for
   *     example if it didn't exist.
   */
  public void remove(Triple triple) throws GraphException {

    if (triple == null) {
      throw new IllegalArgumentException("Null \"triple\" parameter");
    }

    //You can only delete a triple that exists in the graph
    if (!contains(triple)) {

      throw new GraphException("Graph does not contain Triple.");
    }

    try {

      session.delete(graphURI, Collections.singleton(triple));
    }
    catch (QueryException queryException) {

      throw new GraphException("Failed to remove Triple.", queryException);
    }
  }

  /**
   * Removes an iterator containing triples from the graph.
   *
   * @param triples The triple iterator.
   * @throws GraphException
   */
  public void remove(Iterator<Triple> triples) throws GraphException {

    //validate
    if (triples == null) {

      throw new IllegalArgumentException("Null \"Iterator\" parameter");
    }

    try {

      //remove each Triple
      while (triples.hasNext()) {

        this.remove(triples.next());
      }
    }
    catch (ClassCastException castException) {

      //re-throw
      throw new GraphException("Object is not of type \"Triple\".",
          castException);
    }
  }

  /**
   * Closes the resources currently held by the graph.
   */
  public void close() {

    // Close session.
    if (session != null) {
      try {
        session.close();
      }
      catch (QueryException queryException) {
        //cant do anything but log it
        logger.error("Failed to close Session.", queryException);
      }
      finally {
        session = null;
      }
    }
  }

  /**
   * Returns the global node factory for the graph, or creates one.
   *
   * @return the global node factory for the graph, or creates one.
   */
  public GraphElementFactory getElementFactory() {

    return elementFactory;
  }

  /**
   * Returns the triple factory for the graph, or creates one.
   *
   * @return the triple factory for the graph, or creates one.
   */
  public TripleFactory getTripleFactory() {
    return tripleFactory;
  }

}
