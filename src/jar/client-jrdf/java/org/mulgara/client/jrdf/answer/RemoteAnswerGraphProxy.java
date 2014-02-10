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

package org.mulgara.client.jrdf.answer;

// Java 2 standard packages
import java.util.*;

// Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;
import org.jrdf.util.ClosableIterator;

// Local packages
import org.mulgara.client.jrdf.*;
import org.mulgara.client.jrdf.exception.*;
import org.mulgara.query.*;
import org.mulgara.server.Session;

/**
 * RemoteGraphProxy implementation that retrieves it's data from an Answer.
 *
 * @created 2004-07-29
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/13 11:54:40 $
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
public class RemoteAnswerGraphProxy implements RemoteGraphProxy {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(RemoteAnswerGraphProxy.class.getName());

  /** Data source */
  private Answer answer = null;

  /** Used to create local objects */
  private GraphElementBuilder builder = null;

  /** Map of Clasable iterator to be closed with this graph */
  private Set<ClosableIterator<Triple>> iterators = null;

  /** Indicates the Proxy has been closed */
  private boolean closed = false;

  /** Database Session */
  private Session session = null;

  /**
   * Constructor.
   *
   */
  public RemoteAnswerGraphProxy(Answer dataSource, Session session)
      throws GraphException {

    super();

    //cannot proceed without a valid data source
    if (session == null) {

      throw new GraphException("Session cannot be null.");
    }
    if (dataSource == null) {

      throw new IllegalArgumentException("Answer cannot be null.");
    }

    this.session = session;
    this.answer = dataSource;
    this.builder = new GraphElementBuilder();
    this.iterators = new HashSet<ClosableIterator<Triple>>();
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

    //ensure the graph is not closed
    if (this.closed) {

      throw new GraphException("Graph has been closed.");
    }

    //do a search for the triple
    ClosableIterator<Triple> iter = this.find(triple);

    //if a null iterator gets returned there is a problem
    if (iter == null) {

      throw new GraphException("find(Triple) returned null ClosableIterator.");
    }

    return iter.hasNext();
  }

  /**
   * Returns an iterator to a set of statements that match a given subject,
   * predicate and object.  A null value for any of the parts of a triple are
   * treated as unconstrained, any values will be returned.
   *
   * @param triple The triple to find.
   * @throws GraphException If there was an error accessing the graph.
   */
  public ClosableIterator<Triple> find(Triple triple) throws GraphException {

    //ensure the graph is not closed
    if (this.closed) {

      throw new GraphException("Graph has been closed.");
    }

    //create a closable iterator using the triple as a filter
    try {

      return this.createClosableIterator(triple, this.answer);
    } catch (JRDFClientException clientException) {

      throw new GraphException("Could not create new ClosableIterator.",
                               clientException);
    }
  }

  /**
   * Not supported.
   *
   * @param triples The triple iterator.
   * @throws GraphExcepotion If the statements can't be made.
   */
  public void add(Iterator<Triple> triples) throws GraphException {

    throw new UnsupportedOperationException("RemoteAnswerGraphProxy does not " +
                                            "support add(Triple). Graph is " +
                                            "read-only.");
  }

  /**
   * Not supported.
   *
   * @param triples The triple iterator.
   * @throws GraphExcepotion If the statements can't be revoked.
   */
  public void remove(Iterator<Triple> triples) throws GraphException {

    throw new UnsupportedOperationException("RemoteAnswerGraphProxy does not " +
                                            "support remove(Triple). Graph is " +
                                            "read-only.");
  }

  /**
   * Returns the node factory for the graph, or creates one.
   *
   * @return the node factory for the graph, or creates one.
   */
  public GraphElementFactory getElementFactory() {

    //ensure the graph is not closed
    if (this.closed) {

      throw new JRDFClientException("Graph has been closed.");
    }

    return this.builder;
  }

  /**
   * Returns the number of rows in the Answer.
   *
   * @return the number of rows in the Answer.
   */
  public long getNumberOfTriples() {

    //ensure the graph is not closed
    if (this.closed) {

      throw new JRDFClientException("Graph has been closed.");
    }

    try {

      return answer.getRowCount();
    }
    catch (TuplesException tuplesException) {

      //rethrow
      throw new JRDFClientException("Could not determine number of Triples.",
                                    tuplesException);
    }
  }

  /**
   * Returns true if the answer's row cardinality is 0.
   *
   * @return true if the answer's row cardinality is 0.
   */
  public boolean isEmpty() {

    //ensure the graph is not closed
    if (this.closed) {

      throw new JRDFClientException("Graph has been closed.");
    }

    try {

      //check if there are 0 rows (returns 0, 1 or N)
      return (this.answer.getRowCardinality() <= 0);
    } catch (TuplesException tuplesException) {

      //rethrow
      throw new JRDFClientException("Could not determine if Answer is empty.", tuplesException);
    }
  }

  /**
   * Closes the underlying Answer.
   *
   * @throws JRDFClientException
   */
  public void close() throws JRDFClientException {

//    try {

      //only close once
      if (!this.closed) {

//        this.answer.close();

        //close all created ClosableIterators
        Iterator<ClosableIterator<Triple>> iter = this.iterators.iterator();
        while (iter.hasNext()) {

          iter.next().close();
        }
      }

      this.closed = true;
//    }
//    catch (TuplesException tuplesException) {
//
//      //re-throw
//      throw new JRDFClientException("Could not close Answer", tuplesException);
//    }
  }

  /**
   * Removes the iterator from the list to be closed with the graph.
   *
   * @param iter Iterator
   */
  public void unregister(Iterator<?> iter) {

    this.iterators.remove(iter);
  }

  /**
   * Factory method used to create an iterator for the Triples.
   *
   * @param triples Triple[]
   * @return ClientClosableIterator
   */
  public ClientClosableIterator<Triple> createClosableIterator(Triple[] triples) {

    //ensure the graph is not closed
    if (this.closed) {

      throw new JRDFClientException("Graph has been closed.");
    }

    //create iterator and hold a reference to it
    ClientClosableIterator<Triple> iterator = this.builder.createClosableIterator(
        this, triples);
    this.iterators.add(iterator);

    return iterator;
  }

  /**
   * Creates a closable iterator for the Answer, using the Triple as a Filter.
   *
   * @param filter Triple
   * @param answer Answer
   * @return ClientClosableIterator
   */
  public ClientClosableIterator<Triple> createClosableIterator(Triple filter,
      Answer answer) throws JRDFClientException {

    //ensure the graph is not closed
    if (this.closed) {

      throw new JRDFClientException("Graph has been closed.");
    }

    //create a new iterator using a copy of the answer
    ClosableAnswerIteratorProxy proxy = new ClosableAnswerIteratorProxy(this, filter,
        (Answer) answer.clone());

    //create iterator and hold a reference to it (for closing)
    ClientClosableIterator<Triple> iterator = new ClientClosableIterator<Triple>(this, proxy);
    this.iterators.add(iterator);

    return iterator;
  }

  public Session getSession() {
    return session;
  }
}
