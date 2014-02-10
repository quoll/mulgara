/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003, 2004 The JRDF Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        the JRDF Project (http://jrdf.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The JRDF Project" and "JRDF" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, please contact
 *    newmana@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "JRDF"
 *    nor may "JRDF" appear in their names without prior written
 *    permission of the JRDF Project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the JRDF Project.  For more
 * information on JRDF, please see <http://jrdf.sourceforge.net/>.
 */

package org.jrdf.graph.mem;

import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.Triple;
import org.jrdf.util.ClosableIterator;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An iterator that iterates over a group with a single fixed node.
 * Relies on internal iterators which iterate over all entries in
 * a submap, and the sets they point to.
 * The thirdIndexIterator is used to indicate the current position.
 * It will always be set to return the next value until it reaches
 * the end of the group.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @author Andrew Newman
 */
public class OneFixedIterator implements ClosableIterator<Triple> {

  /** The iterator for the second index. */
  private Iterator<Map.Entry<Long,Set<Long>>> secondIndexIterator;

  /** The iterator for the third index. */
  private Iterator<Long> thirdIndexIterator;

  /** The current element for the iterator on the second index. */
  private Map.Entry<Long,Set<Long>> secondEntry;

  /** The factory used to create the nodes to be returned in the triples. */
  private GraphElementFactoryImpl factory;

  /** The index of this iterator.  Only needed for initialization and the remove method. */
  private Map<Long,Map<Long,Set<Long>>> index;

  /** The subIndex of this iterator.  Only needed for initialization and the remove method. */
  private Map<Long,Set<Long>> subIndex;

  /** The fixed item. */
  private Long first;

  /** The offset for the index. */
  private int var;

  /** The current subject predicate and object, last returned from next().  Only needed by the remove method. */
  private Long[] currentNodes;

  /** Handles the removal of nodes */
  private GraphHandler handler;


  /**
   * Constructor.  Sets up the internal iterators.
   * @throws IllegalArgumentException Must pass in a GraphElementFactory memory implementation.
   */
  OneFixedIterator(Map<Long,Map<Long,Set<Long>>> newIndex, int newVar, Long newFirst,
      GraphElementFactory newFactory, GraphHandler newHandler, Map<Long,Set<Long>> newSubIndex) {
    if (!(newFactory instanceof GraphElementFactoryImpl)) {
      throw new IllegalArgumentException("Must use the memory implementation of GraphElementFactory");
    }

    // store the node factory and other starting data
    factory = (GraphElementFactoryImpl)newFactory;
    handler = newHandler;

    first = newFirst;
    var = newVar;
    index = newIndex;
    currentNodes = null;
    // initialise the iterators to empty
    thirdIndexIterator = null;
    secondIndexIterator = null;
    // find the subIndex from the main index
    subIndex = newSubIndex;
    // check that data exists
    if (null != subIndex) {
      // now get an iterator to the sub index map
      secondIndexIterator = subIndex.entrySet().iterator();
      // check if there is data available - structural constraints say there should be
      assert secondIndexIterator.hasNext();
    }
  }


  /**
   * Returns true if the iteration has more elements.
   * @return <code>true</code> If there is an element to be read.
   */
  public boolean hasNext() {
    // confirm we still have an item iterator, and that it has data available
    return null != thirdIndexIterator && thirdIndexIterator.hasNext() ||
        null != secondIndexIterator && secondIndexIterator.hasNext();
  }


  /**
   * Returns the next element in the iteration.
   * @return the next element in the iteration.
   * @throws NoSuchElementException iteration has no more elements.
   */
  public Triple next() throws NoSuchElementException {
    if (null == secondIndexIterator) {
      throw new NoSuchElementException();
    }
    // move to the next position
    updatePosition();
    if (null == secondIndexIterator) {
      throw new NoSuchElementException();
    }
    // get the next item
    Long third = thirdIndexIterator.next();
    // construct the triple
    Long second = secondEntry.getKey();
    // get back the nodes for these IDs and build the triple
    currentNodes = new Long[]{first, second, third};
    return new TripleImpl(factory, var, first, second, third);
  }


  /**
   * Helper method to move the iterators on to the next position.
   * If there is no next position then {@link #thirdIndexIterator thirdIndexIterator}
   * will be set to null, telling {@link #hasNext() hasNext} to return
   * <code>false</code>.
   */
  private void updatePosition() {
    // progress to the next item if needed
    if (null == thirdIndexIterator || !thirdIndexIterator.hasNext()) {
      // the current iterator been exhausted
      if (!secondIndexIterator.hasNext()) {
        // the subiterator has been exhausted
        // tell the secondIndexIterator to finish
        secondIndexIterator = null;
        return;
      }
      // get the next entry of the sub index
      secondEntry = secondIndexIterator.next();
      // get an iterator to the next set from the sub index
      thirdIndexIterator = secondEntry.getValue().iterator();
      assert thirdIndexIterator.hasNext();
    }
  }


  /**
   * Implemented for java.util.Iterator.
   */
  public void remove() {
    if (null != thirdIndexIterator) {
      // now remove from the other 2 indexes
      try {
        thirdIndexIterator.remove();
        handler.remove(currentNodes);

        // check if a set was cleaned out
        Set<Long> subGroup = secondEntry.getValue();
        if (subGroup.isEmpty()) {
          // remove the entry for the set
          secondIndexIterator.remove();
          // check if a subindex was cleaned out
          if (subIndex.isEmpty()) {
            // remove the subindex
            index.remove(first);
            subIndex = null;
          }
        }
      } catch (GraphException ge) {
        throw new IllegalStateException(ge.getMessage());
      }
    } else {
      throw new IllegalStateException("Next not called or beyond end of data");
    }
  }


  /**
   * Closes the iterator by freeing any resources that it current holds.
   * Nothing to be done for this class.
   * @return <code>true</code> indicating success.
   */
  public boolean close() {
    return true;
  }

}
