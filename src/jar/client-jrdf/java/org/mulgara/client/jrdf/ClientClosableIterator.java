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
import org.jrdf.util.ClosableIterator;

/**
 * A ClosableIterator implementation for client side use.
 *
 * @created 2004-07-29
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:35 $
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
public class ClientClosableIterator<T> implements ClosableIterator<T> {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(ClientClosableIterator.class.getName());

  /** Proxy that does all of the real work */
  private VirtualClosableIteratorProxy<T> proxy = null;

  /** Graph that created this iterator */
  private RemoteGraphProxy creator = null;

  /**
   * Default Constructor
   *
   */
  public ClientClosableIterator(RemoteGraphProxy creator,
                                VirtualClosableIteratorProxy<T> proxy) {

    super();

    //validate
    if (creator == null) {

      throw new IllegalArgumentException("'creator' Graph cannot be null.");
    }
    if (proxy == null) {

      throw new IllegalArgumentException("VirtualClosableIteratorProxy cannot be null.");
    }

    this.creator = creator;
    this.proxy = proxy;
  }

  /**
   * Closes the iterator by freeing any resources that it current holds.  This
   * must be done as soon as possible.  Once an iterator is closed none of the
   * operations on a iterator will operate i.e. they will throw an exception.
   */
  public boolean close() {

    //remove the iterator from the graph
    this.creator.unregister(this);
    return proxy.close();
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements. (In other
   * words, returns <tt>true</tt> if <tt>next</tt> would return an element
   * rather than throwing an exception.)
   *
   * @return <tt>true</tt> if the iterator has more elements.
   */
  public boolean hasNext() {

    return proxy.hasNext();
  }

  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration.
   * @exception NoSuchElementException iteration has no more elements.
   */
  public T next() {

    return proxy.next();
  }

  /**
   *
   * Removes from the underlying collection the last element returned by the
   * iterator (optional operation).  This method can be called only once per
   * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
   * the underlying collection is modified while the iteration is in
   * progress in any way other than by calling this method.
   *
   * @exception UnsupportedOperationException if the <tt>remove</tt>
   *		  operation is not supported by this Iterator.

   * @exception IllegalStateException if the <tt>next</tt> method has not
   *		  yet been called, or the <tt>remove</tt> method has already
   *		  been called after the last call to the <tt>next</tt>
   *		  method.
   */
  public void remove() {

    proxy.remove();
  }

}
