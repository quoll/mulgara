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
import org.jrdf.graph.*;

/**
 * A VirtualClosableIteratorProxy implementation that uses an array of Triples.
 * This might have been a general implementation but was specifically created for Triples.
 *
 * @created 2004-07-29
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
public class ClosableArrayIteratorProxy
    implements VirtualClosableIteratorProxy<Triple> {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(
      ClosableArrayIteratorProxy.class.
      getName());

  /** Data Source used by the iterator */
  private List<Triple> data = null;

  /** does all the work */
  private Iterator<Triple> iterator = null;

  /** Indicates the Iterator has been closed */
  private boolean closed = false;


  /**
   * Default Constructor
   *
   */
  public ClosableArrayIteratorProxy(Triple [] dataSource) {

    super();

    //cannot proceed without valid data
    if (dataSource == null) {

      throw new IllegalArgumentException("Triple [] cannot be null.");
    }

    //copy data to List
    this.data = new ArrayList<Triple>();
    final int length = dataSource.length;
    for (int i = 0; i < length; i++) {

      this.data.add(dataSource[i]);
    }

    //get the List iterator
    this.iterator = this.data.iterator();
  }

  /**
   * Closes the iterator by freeing any resources that it current holds.  This
   * must be done as soon as possible.  Once an iterator is closed none of the
   * operations on a iterator will operate i.e. they will throw an exception.
   */
  public boolean close() {

    //empty the List
    this.data.clear();
    this.data = null;

    this.closed = true;

    //success...
    return true;
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
  public Triple next() {

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
