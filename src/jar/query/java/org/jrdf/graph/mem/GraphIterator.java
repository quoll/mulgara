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
 * An iterator that iterates over an entire graph.
 * Relies on internal iterators which iterate over all entries in
 * the first map, the maps it points to, and the sets they point to.
 * The itemIterator  is used to indicate the current position.
 * It will always be set to return the next value until it reaches
 * the end of the graph.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @author Andrew Newman
 */
public class GraphIterator implements ClosableIterator<Triple> {

  /** The iterator for the first index. */
  private Iterator<Map.Entry<Long,Map<Long,Set<Long>>>> iterator;

  /** The iterator for the second index. */
  private Iterator<Map.Entry<Long,Set<Long>>> subIterator;

  /** The iterator for the third index. */
  private Iterator<Long> itemIterator;

  /** The current element for the iterator on the first index. */
  private Map.Entry<Long,Map<Long,Set<Long>>> firstEntry;

  /** The current element for the iterator on the second index. */
  private Map.Entry<Long,Set<Long>> secondEntry;

  /** The current subject predicate and object, last returned from next().  Only needed by the remove method. */
  private Long[] currentNodes;

  /** The nodeFactory used to create the nodes to be returned in the triples. */
  private GraphElementFactoryImpl nodeFactory;

  /** Handles the removal of nodes */
  private GraphHandler handler;
  private boolean nextCalled = false;

  /**
   * Constructor.  Sets up the internal iterators.
   * @throws IllegalArgumentException Must be created with implementations from
   *   the memory package.
   */
  GraphIterator(Iterator<Map.Entry<Long,Map<Long,Set<Long>>>> newIterator, GraphElementFactory newNodeFactory, GraphHandler newHandler) {
    if (!(newNodeFactory instanceof GraphElementFactoryImpl)) {
      throw new IllegalArgumentException("Node factory must be a memory implementation");
    }

    // store the node factory
    nodeFactory = (GraphElementFactoryImpl) newNodeFactory;
    handler = newHandler;
    iterator = newIterator;
  }


  /**
   * Returns true if the iteration has more elements.
   * @return <code>true</code> If there is an element to be read.
   */
  public boolean hasNext() {
    // confirm we still have an item iterator, and that it has data available
    return null != itemIterator && itemIterator.hasNext() ||
        null != subIterator && subIterator.hasNext() ||
        null != iterator && iterator.hasNext();
  }


  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration.
   * @throws NoSuchElementException iteration has no more elements.
   */
  public Triple next() throws NoSuchElementException {
    if (null == iterator) throw new NoSuchElementException();

    // move to the next position
    updatePosition();

    if (null == iterator) throw new NoSuchElementException();

    nextCalled = true;

    // get the next item
    Long third = itemIterator.next();

    // construct the triple
    Long second = secondEntry.getKey();
    Long first = firstEntry.getKey();

    // get back the nodes for these IDs and uild the triple
    currentNodes = new Long[]{first, second, third};
    return new TripleImpl((GraphElementFactoryImpl) nodeFactory, first, second, third);
  }


  /**
   * Helper method to move the iterators on to the next position.
   * If there is no next position then {@link #itemIterator itemIterator}
   * will be set to null, telling {@link #hasNext() hasNext} to return
   * <code>false</code>.
   */
  private void updatePosition() {
    // progress to the next item if needed
    if (null == itemIterator || !itemIterator.hasNext()) {
      // the current iterator been exhausted
      if (null == subIterator || !subIterator.hasNext()) {
        // the subiterator has been exhausted
        if (!iterator.hasNext()) {
          // the main iterator has been exhausted
          // tell the iterator to finish
          iterator = null;
          return;
        }
        // move on the main iterator
        firstEntry = iterator.next();
        // now get an iterator to the sub index map
        subIterator = firstEntry.getValue().entrySet().iterator();
        assert subIterator.hasNext();
      }
      // get the next entry of the sub index
      secondEntry = subIterator.next();
      // get an interator to the next set from the sub index
      itemIterator = secondEntry.getValue().iterator();
      assert itemIterator.hasNext();
    }
  }


  /**
   * Implemented for java.util.Iterator.
   */
  public void remove() {
    if (nextCalled && null != itemIterator) {
      itemIterator.remove();
      // clean up the current index after the removal
      cleanIndex();
      // now remove from the other 2 indexes
      removeFromNonCurrentIndex();
    } else {
      throw new IllegalStateException("Next not called or beyond end of data");
    }
  }


  /**
   * Checks if a removed item is the last of its type, and removes any associated subindexes if appropriate.
   */
  private void cleanIndex() {
    // check if a set was cleaned out
    Set<Long> subGroup = secondEntry.getValue();
    Map<Long,Set<Long>> subIndex = firstEntry.getValue();
    if (subGroup.isEmpty()) {
      // remove the entry for the set
      subIterator.remove();
      // check if a subindex was cleaned out
      if (subIndex.isEmpty()) {
        // remove the subindex
        iterator.remove();
      }
    }
    //handler.clean(secondEntry, subIterator, subIndex, );
  }


  /**
   * Helper function for removal.  This removes the current statement from the indexes which
   * the current iterator is not associated with.
   */
  private void removeFromNonCurrentIndex() {
    try {
      // can instead use var here to determine how to delete, but this is more intuitive
      handler.remove(currentNodes);
    } catch (GraphException ge) {
      IllegalStateException illegalStateException = new IllegalStateException();
      illegalStateException.setStackTrace(ge.getStackTrace());
      throw illegalStateException;
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
