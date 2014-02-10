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
import org.jrdf.util.ClosableIterator;
import java.util.*;

/**
 * A JRDF Graph implementation for client side use.
 *
 * @created 2004-07-29
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/13 11:52:22 $
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
public class ClientGraph implements Graph {

  /** Generated serialization ID */
  private static final long serialVersionUID = 4410126271252082706L;

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(ClientGraph.class.getName());

  /** Proxy that does all of the real work */
  private RemoteGraphProxy proxy = null;

  /** TripleFactory used to insert and reify statements */
  private ClientTripleFactory tripleFactory = null;

  /**
   * Default Constructor
   *
   * @param proxy RemoteGraphProxy
   * @throws GraphException
   */
  public ClientGraph(RemoteGraphProxy proxy) throws GraphException {

    super();

    //cannot proceed without a valid proxy
    if (proxy == null) {

      throw new IllegalArgumentException("RemoteGraphProxy cannot be null.");
    }

    this.proxy = proxy;
  }

  /**
   * Test the graph for the occurrence of a statement.  A null value for any
   * of the parts of a triple are treated as unconstrained, any values will be
   * returned.
   *
   * @param subject The subject to find or null to indicate any subject.
   * @param predicate The predicate to find or null to indicate any predicate.
   * @param object The object to find or null to indicate any object.
   * @return True if the statement is found in the model, otherwise false.
   * @throws GraphException If there was an error accessing the graph.
   */
  public boolean contains(SubjectNode subject, PredicateNode predicate, ObjectNode object) throws GraphException {

    //create a triple from the arguments and use
    try {

      Triple triple = this.getElementFactory().createTriple(subject, predicate, object);

      return this.contains(triple);

    } catch (GraphElementFactoryException factoryException) {

      throw new GraphException("Could not create Triple.", factoryException);
    }
  }

  /**
   * Test the graph for the occurrence of the triple.  A null value for any
   * of the parts of a triple are treated as unconstrained, any values will be
   * returned.
   *
   * @param triple The triple to find.
   * @return True if the triple is found in the graph, otherwise false.
   * @throws GraphException If there was an error accessing the graph.
   */
  public boolean contains(Triple triple) throws GraphException {

    return proxy.contains(triple);
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

    //create a triple from the arguments and use
    try {

      Triple triple = this.getElementFactory().createTriple(subject, predicate, object);

      return this.find(triple);

    } catch (GraphElementFactoryException factoryException) {

      throw new GraphException("Could not create Triple.", factoryException);
    }
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

    return proxy.find(triple);
  }

  /**
   * Adds a triple to the graph.
   *
   * @param subject The subject.
   * @param predicate The predicate.
   * @param object The object.
   * @throws GraphException If the statement can't be made.
   */
  public void add(SubjectNode subject, PredicateNode predicate, ObjectNode object) throws GraphException {

    //create a triple from the arguments and use
    try {

      Triple triple = this.getElementFactory().createTriple(subject, predicate, object);

      this.add(triple);

    } catch (GraphElementFactoryException factoryException) {

      throw new GraphException("Could not create Triple.", factoryException);
    }
  }

  /**
   * Adds a triple to the graph.
   *
   * @param triple The triple.
   * @throws GraphException If the statement can't be made.
   */
  public void add(Triple triple) throws GraphException {

    //create an Iterator from a single element Triple array
    Iterator<Triple> iter = proxy.createClosableIterator(new Triple [] {triple});

    this.add(iter);
  }

  /**
   * Adds an iterator containing triples into the graph.
   *
   * @param triples The triple iterator.
   * @throws GraphException
   */
  public void add(Iterator<Triple> triples) throws GraphException {

     proxy.add(triples);
  }

  /**
   * Removes a triple from the graph.
   *
   * @param subject The subject.
   * @param predicate The predicate.
   * @param object The object.
   * @throws GraphException If there was an error revoking the statement, For
   *     example if it didn't exist.
   */
  public void remove(SubjectNode subject, PredicateNode predicate,
      ObjectNode object) throws GraphException {

    //create a triple from the arguments and use
    try {

      Triple triple = this.getElementFactory().createTriple(subject, predicate, object);

      this.remove(triple);
    } catch (GraphElementFactoryException factoryException) {

      throw new GraphException("Could not create Triple.", factoryException);
    }
  }

  /**
   * Removes a triple from the graph.
   *
   * @param triple The triple.
   * @throws GraphException If there was an error revoking the statement, For
   *     example if it didn't exist.
   */
  public void remove(Triple triple) throws GraphException {

    //create an Iterator from a single element Triple array
    Iterator<Triple> iter = proxy.createClosableIterator(new Triple [] {triple});

    this.remove(iter);
  }

  /**
   * Removes an iterator containing triples from the graph.
   *
   * @param triples The triple iterator.
   * @throws GraphException
   */
  public void remove(Iterator<Triple> triples) throws GraphException {

    proxy.remove(triples);
  }

  /**
   * Returns the node factory for the graph, or creates one.
   *
   * @return the node factory for the graph, or creates one.
   */
  public GraphElementFactory getElementFactory() {

    return proxy.getElementFactory();
  }

  /**
   * Returns the triple factory for the graph, or creates one.
   *
   * @return the triple factory for the graph, or creates one.
   */
  public TripleFactory getTripleFactory() {

    return this.tripleFactory;
  }

  /**
   * Returns the number of triples in the graph.
   *
   * @return the number of triples in the graph.
   */
  public long getNumberOfTriples() {

    return proxy.getNumberOfTriples();
  }

  /**
   * Returns true if the graph is empty i.e. the number of triples is 0.
   *
   * @return true if the graph is empty i.e. the number of triples is 0.
   */
  public boolean isEmpty() {

    return proxy.isEmpty();
  }

  /**
   * Closes the Graph and any underlying data source.
   *
   */
  public void close() {

    this.proxy.close();
  }
}
