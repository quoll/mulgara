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

// Java 2 standard packages
import java.util.*;

// Log4J
import org.apache.log4j.Logger;

// JRDF
import org.mulgara.client.jrdf.*;

/**
 * A Non-scalable ClosableIteratorProxy that orders the contents of another
 * ClosableIteratorProxy using a Comparator. Ordered in-memory.
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
public class OrderedClosableIteratorProxy<T>
    implements VirtualClosableIteratorProxy<T> {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(
      OrderedClosableIteratorProxy.class.getName());

  /** Data Source used by the iterator */
  private List<T> data = null;

  /** Does all the work */
  private Iterator<T> iterator = null;

  /** Needs to be closed when this Iterator is closed */
  private VirtualClosableIteratorProxy<T> proxy = null;

  /** Indicates that the Iterator has already been closed */
  private boolean closed = false;

  /**
   * Default Constructor.
   *
   * @param proxy VirtualClosableIteratorProxy
   * @param comparator Comparator
   */
  public OrderedClosableIteratorProxy(VirtualClosableIteratorProxy<T> proxy,
                                      Comparator<T> comparator) {

    super();

    //validate
    if (proxy == null) {

      throw new IllegalArgumentException("VirtualClosableIteratorProxy cannot "+
                                         "be null.");
    }

    this.proxy = proxy;

    //copy data to List
    this.data = new ArrayList<T>();
    while (this.proxy.hasNext()) {

      this.data.add(this.proxy.next());
    }

    //sort
    Collections.sort(this.data, comparator);

    //get iterator
    this.iterator = this.data.iterator();
  }

  /**
   * Closes the ClosableIteratorProxy used to create this Iterator.
   *
   * @return boolean
   */
  public boolean close() {

    //empty the List
    this.data.clear();
    this.data = null;

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

    return this.iterator.hasNext();
  }

  /**
   * Returns the next Triple in the iteration.
   *
   * @return the next element in the iteration.
   * @exception NoSuchElementException iteration has no more elements.
   */
  public T next() {

    //ensure the iterator is not closed
    if (this.closed) {

      throw new IllegalStateException("ClosableIterator has been closed.");
    }

    return this.iterator.next();
  }

  /**
   * Let the iterator do it.
   *
   * @exception UnsupportedOperationException if the <tt>remove</tt>
   *		  operation is not supported by this Iterator.
   */
  public void remove() {

    //ensure the iterator is not closed
    if (this.closed) {

      throw new IllegalStateException("ClosableIterator has been closed.");
    }

    this.iterator.remove();
  }
}
