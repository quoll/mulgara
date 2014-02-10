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

package org.mulgara.client.jrdf.util;

// Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;
import org.mulgara.client.jrdf.*;
import org.mulgara.client.jrdf.exception.*;

/**
 * A Non-scalable ClosableIteratorProxy that returns Triples with an Unique
 * SubjectNode. Calling next will return the next Triple with an Unique
 * SubjectNode (Predicate and Object Nodes will be null).
 *
 * @created 2004-08-16
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:37 $
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
public class UniqueSubjectIterator
    implements VirtualClosableIteratorProxy<Triple> {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(UniqueSubjectIterator.class.getName());

  /** Needs to be closed when this Iterator is closed */
  private OrderedClosableIteratorProxy<Triple> proxy = null;

  /** Pre-cached "next" triple with an Unique SubjectNode */
  private Triple nextTriple = null;

  /** Node used to determine if the Subject is different */
  private SubjectNode currentSubject = null;

  /** Builder used to create Triples */
  private GraphElementBuilder builder = null;

  /** Indicates that the Iterator has already been closed */
  private boolean closed = false;

  /**
   * Default Constructor.
   *
   * @param proxy OrderedClosableIteratorProxy
   * @throws GraphException
   */
  public UniqueSubjectIterator(OrderedClosableIteratorProxy<Triple> proxy) throws
      GraphException {

    super();

    //validate
    if (proxy == null) {

      throw new IllegalArgumentException("OrderedClosableIteratorProxy cannot be null.");
    }

    //initialize members
    this.proxy = proxy;
    this.builder = new GraphElementBuilder();

    //cach the first unique triple
    try {

      this.getNext();
    } catch (GraphElementFactoryException factoryException) {

      throw new GraphException("Could not retrieve first Triple.", factoryException);
    }
  }

  /**
   * Closes the ClosableIteratorProxy used to create this Iterator.
   *
   * @return boolean
   */
  public boolean close() {

    //the proxy is now closed
    this.closed = true;

    //close the data source
    return this.proxy.close();
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

    //nextTriple will be null when no more unique SubjectNodes are found
    return (this.nextTriple != null);
  }

  /**
   * Returns the next Triple in the iteration and caches the next Triple
   * containing an unique SubjectNode.
   *
   * @return the next element in the iteration.
   * @exception NoSuchElementException iteration has no more elements.
   * @exception JRDFClientException Cannot retrieve next Triple.
   */
  public Triple next() {

    //ensure the iterator is not closed
    if (this.closed) {

      throw new IllegalStateException("ClosableIterator has been closed.");
    }

    //reference to "Old" nextTriple (getNext() modifies state of nextTriple)
    Triple triple = this.nextTriple;

    //cache get the next triple
    try {

      this.getNext();
    } catch (GraphElementFactoryException factoryException) {

      throw new JRDFClientException("Could not get next value.", factoryException);
    }

    return triple;
  }

  /**
   * Let the proxy do it.
   *
   * @exception UnsupportedOperationException if the <tt>remove</tt>
   *		  operation is not supported by this Iterator.
   */
  public void remove() {

    //ensure the iterator is not closed
    if (this.closed) {

      throw new IllegalStateException("ClosableIterator has been closed.");
    }

    this.proxy.remove();
  }

  /**
   * Gets the next Triple with an unique SubjectNode from the proxy.
   *
   * @throws GraphElementFactoryException
   */
  private void getNext() throws GraphElementFactoryException {

    //reset old value
    this.nextTriple = null;

    //current triple being evaluated
    Triple proxyTriple = null;

    while (this.proxy.hasNext()) {

      proxyTriple = (Triple) this.proxy.next();

      //is the Subject new?
      if (this.newSubject(proxyTriple)) {

        //create new Triple
        this.currentSubject = proxyTriple.getSubject();
        this.nextTriple = this.builder.createTriple(this.currentSubject, null, null);

        //stop looking
        break;
      }
    }
  }

  /**
   * Compares the triple's SubjectNode to the current SubjectNode. Returns
   * true if the SubjectNode's are different.
   *
   * @param triple Triple
   * @return boolean
   */
  private boolean newSubject(Triple triple) {

    //validate
    if (triple == null) {

      throw new IllegalArgumentException("Triple cannot be null.");
    }

    if (triple.getSubject() == null) {

      throw new IllegalArgumentException("Triple returned a null SubjectNode.");
    }

    //compare subjects
    return !triple.getSubject().equals(this.currentSubject);
  }
}
