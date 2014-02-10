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

// Internal packages
import org.mulgara.client.jrdf.*;
import org.mulgara.client.jrdf.exception.*;
import org.mulgara.query.*;

/**
 * A ClosableIterator implementation for client side use, backed by an Answer.
 *
 * @created 2004-07-29
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/13 11:53:36 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 * Portions by Paul Gearon.
 * @copyright &copy;2006 <a href="http://www.herzumsoftware.com/">Herzum Software LLC</a>
 */
public class ClosableAnswerIteratorProxy implements VirtualClosableIteratorProxy<Triple> {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(
      ClosableAnswerIteratorProxy.class.getName());

  /** An error string that JRDF expects. */
  private final static String JRDF_STATE_ERROR_STRING = "Next not called or beyond end of data";

  /** Data Source used by the iterator */
  private Answer answer = null;

  /** Triple used to constrain the results from the Answer. */
  private Triple filter = null;

  /** Graph used to perform remove operations on - can be null */
  private RemoteGraphProxy graph = null;

  /** Builder used to create Graph Objects */
  private GraphElementBuilder builder = null;

  /** Next triple to be returned */
  private Triple nextTriple = null;

  /** Last triple that was returned */
  private Triple lastTriple = null;

  /** Indicates the Iterator has been closed */
  private boolean closed = false;

  /** Answer column that refers to the Subject */
  private static final int SUBJECT_COLUMN = 0;

  /** Answer column that refers to the Predicate */
  private static final int PREDICATE_COLUMN = 1;

  /** Answer column that refers to the Object */
  private static final int OBJECT_COLUMN = 2;

  /**
   * Constructor. keeps a reference to the graph that can perform remove
   * operations.
   *
   */
  public ClosableAnswerIteratorProxy(RemoteGraphProxy creator, Triple filter,
      Answer dataSource) throws JRDFClientException {

    super();

    //cannot proceed without a valid proxy
    if (dataSource == null) {

      throw new IllegalArgumentException("Answer cannot be null.");
    }

    //must have a creator
    if (creator == null) {

      throw new IllegalArgumentException("'creator' Graph cannot be null.");
    }


    this.answer = dataSource;
    this.graph = creator;

    //null filter indicates that the results are not filtered
    if (filter == null) {

      try {

        this.filter = this.builder.createTriple(null, null, null);
      }
      catch (GraphElementFactoryException factoryException) {

        //error occurred in the Builder
        throw new JRDFClientException("Could not create empty Triple.",
                                      factoryException);
      }
    }
    else {

      this.filter = filter;
    }

    //instantiate builder for creating Graph Objects
    try {

      this.builder = new GraphElementBuilder();
    }
    catch (GraphException graphException) {

      //problem instantiating Builder
      throw new JRDFClientException("Could not create GraphElementFactory.",
                                    graphException);
    }

    //cache first triple
    try {

      this.answer.beforeFirst();
      this.updateNextTriple();
    }
    catch (TuplesException tuplesException) {

      //something went wrong
      throw new JRDFClientException("Could not initialize Answer.",
                                    tuplesException);
    }
  }

  /**
   * Closes the iterator by freeing any resources that it current holds.  This
   * must be done as soon as possible.  Once an iterator is closed none of the
   * operations on a iterator will operate i.e. they will throw an exception.
   */
  public boolean close() {

//    try {

      //only close once
      if (!this.closed) {

//        this.answer.close();
      }

      //if there was no Exception, record the close.
      return (this.closed = true);
//    }
//    catch (TuplesException tuplesException) {
//
//      //something went wrong
//      throw new JRDFClientException("Could not close Answer.", tuplesException);
//    }
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements. (In other
   * words, returns <tt>true</tt> if <tt>next</tt> would return an element
   * rather than throwing an exception.)
   *
   * @return <tt>true</tt> if the iterator has more elements.
   */
  public boolean hasNext() {

    //ensure the iterator is not closed
    if (this.closed) {

      throw new IllegalStateException("ClosableIterator has been closed.");
    }

    return (this.nextTriple != null);
  }

  /**
   * Returns the next Triple in the iteration.
   *
   * @return the next element in the iteration.
   * @exception NoSuchElementException iteration has no more elements.
   */
  public Triple next() {

    //ensure the iterator is not closed
    if (this.closed) {

      throw new IllegalStateException("ClosableIterator has been closed.");
    }

    //check if end exceeded
    if (!this.hasNext()) {

      throw new JRDFClientException(
          "next() called after end of Answer reached.");
    }

    //value to be returned
    this.lastTriple = this.nextTriple;

    //cache next Triple
    this.updateNextTriple();

    //return "old" value
    return this.lastTriple;
  }

  /**
   * Increments the next triple.
   *
   * @throws JRDFClientException
   */
  private void updateNextTriple() throws JRDFClientException {

    //update the next Triple
    this.nextTriple = this.getNextTriple();
  }

  /**
   * Gets the next triple from the Answer's row.
   *
   * @throws JRDFClientException
   * @return Triple
   */
  private Triple getNextTriple() throws JRDFClientException {

    //value to be returned
    Triple result = null;

    //nodes being checked
    SubjectNode subject = null;
    PredicateNode predicate = null;
    ObjectNode object = null;

    try {

      //find next valid/filtered triple
      while (this.answer.next()) {

        //get next triple from the answer
        subject = this.getNextSubject();
        predicate = this.getNextPredicate();
        object = this.getNextObject();

        //check triple
        if (filterTriple(subject, predicate, object)){

          //validate triple and return
          if ( (subject == null)
              && (predicate == null)
              && (object == null)) {

            throw new JRDFClientException("Answer is invalid. Triple contains " +
                                          "null node.");
          }

          return this.builder.createTriple(subject, predicate, object);
        }
      }
    }
    catch (GraphElementFactoryException factoryException) {

      //error occurred in the Builder
      throw new JRDFClientException("Could not create next Triple.",
                                    factoryException);
    }
    catch (TuplesException tupleException) {

      //error occurred in the Answer
      throw new JRDFClientException("Could not get next Triple from Answer.",
                                    tupleException);
    }

    return result;
  }

  /**
   * Determines if the Triple is valid when applied to the filter.
   * Short-circuits as soon as a condition fails.
   *
   * @return boolean
   */
  private boolean filterTriple(SubjectNode subject, PredicateNode predicate,
                               ObjectNode object) {

    //validate arguments
    if ( (subject == null)
        || (predicate == null)
        || (object == null)) {

      return false;
    }

    //check subject (if filter field not null)
    if ( (this.filter.getSubject() != null)
        && (!this.filter.getSubject().equals(subject))) {

      //filter subject not null and subject argument does not match
      return false;
    }

    //check predicate (if filter field not null)
    if ( (this.filter.getPredicate() != null)
        && (!this.filter.getPredicate().equals(predicate))) {

      //filter predicate not null and predicate argument does not match
      return false;
    }

    //check object (if filter field not null)
    if ( (this.filter.getObject() != null)
        && (!this.filter.getObject().equals(object))) {

      //filter object not null and object argument does not match
      return false;
    }

    //all filters have passed, return true
    return true;
  }

  /**
   * Evaluates and returns a SubjectNode representing the object in subject
   * column.
   *
   * @throws JRDFClientException
   * @return SubjectNode
   */
  private SubjectNode getNextSubject() throws JRDFClientException {

    try {

      return (SubjectNode)this.answer.getObject(SUBJECT_COLUMN);
    }
    catch (TuplesException tuplesException) {

      throw new JRDFClientException("Could not get next Subject.",
                                    tuplesException);
    }
    catch (ClassCastException classException) {

      throw new JRDFClientException("Subject column contains invalid object. " +
                                    "Answer is invalid.", classException);
    }
  }

  /**
   * Evaluates and returns a PredicateNode representing the object in predicate
   * column.
   *
   * @throws JRDFClientException
   * @return PredicateNode
   */
  private PredicateNode getNextPredicate() throws JRDFClientException {

    try {

      //predicate column must contain a PredicateNode
      return (PredicateNode)this.answer.getObject(PREDICATE_COLUMN);
    }
    catch (TuplesException tuplesException) {

      throw new JRDFClientException("Could not get next Predicate.",
                                    tuplesException);
    }
    catch (ClassCastException classException) {

      throw new JRDFClientException("Predicate column contains invalid " +
                                    "object. Answer is invalid.",
                                    classException);
    }
  }

  /**
   * Evaluates and returns a ObjectNode representing the object in object column.
   *
   * @throws JRDFClientException
   * @return ObjectNode
   */
  private ObjectNode getNextObject() throws JRDFClientException {

    try {

      return (ObjectNode)this.answer.getObject(OBJECT_COLUMN);
    }
    catch (TuplesException tuplesException) {

      throw new JRDFClientException("Could not get next Object.",
                                    tuplesException);
    }
    catch (ClassCastException classException) {

      throw new JRDFClientException("Object column contains invalid object. " +
                                    "Answer is invalid.", classException);
    }
  }

  /**
   * If a Graph is supplied, remove is called on the graph, otherwise an
   * Exception is thrown.
   *
   * @exception UnsupportedOperationException If a Graph has not been supplied.
   * @exception JRDFClientException if the <tt>remove</tt> operation is not
   *            supported by the Graph.
   */
  public void remove() {

    if (this.graph != null) {

      try {

        if (this.lastTriple == null) {
          throw new IllegalStateException(JRDF_STATE_ERROR_STRING);
        }
        //wrap the last triple to be returned in an Iterator
        Set<Triple> lastTriple = new HashSet<Triple>();
        lastTriple.add(this.lastTriple);

        //remove
        this.graph.remove(lastTriple.iterator());
      }
      catch (GraphException graphException) {

        throw new JRDFClientException("Could not remove Triple.", graphException);
      }
    } else {

      throw new UnsupportedOperationException(
      "ClosableAnswerIteratorProxy.remove() not supported. Cannot access Graph.");
    }
  }
}
