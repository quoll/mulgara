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
 * An iterator that iterates over a group with a two fixed nodes.
 * Relies on an internal iterator which iterates over all entries in
 * a set, found in a subIndex.
 * 
 * The thirdIndexIterator is used to indicate the current position.
 * It will always be set to return the next value until it reaches
 * the end of the group.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public class TwoFixedIterator implements ClosableIterator<Triple> {

  /** The iterator for the third index. */
  private Iterator<Long> thirdIndexIterator;

  /** The nodeFactory used to create the nodes to be returned in the triples. */
  private GraphElementFactoryImpl nodeFactory;

  /** The index of this iterator.  Only needed for initialization and the remove method. */
  private Map<Long,Map<Long,Set<Long>>> index;

  /** The subIndex of this iterator.  Only needed for initialization and the remove method. */
  private Map<Long,Set<Long>> subIndex;

  /** The subSubIndex of this iterator.  Only needed for initialization and the remove method. */
  private Set<Long> subGroup;

  /** The current subject predicate and object, last returned from next().  Only needed by the remove method. */
  private Long[] currentNodes;

  /** The first fixed item. */
  private Long first;

  /** The second fixed item. */
  private Long second;

  /** The current third item */
  private Long third;

  /** If there are anymore items left */
  private boolean hasNext;

  /** The offset for the index. */
  private int var;

  /** Handles the removal of nodes */
  private GraphHandler handler;

  /**
   * Constructor.  Sets up the internal iterators.
   */
  TwoFixedIterator(Map<Long,Map<Long,Set<Long>>> newIndex, int newVar, Long newFirst, Long newSecond,
      GraphElementFactory newFactory, GraphHandler newHandler, Map<Long,Set<Long>> newSubIndex) {

    if (!(newFactory instanceof GraphElementFactoryImpl)) {
      throw new IllegalArgumentException("Must use the memory implementation of GraphElementFactory");
    }

    // store the node factory and other starting data
    nodeFactory = (GraphElementFactoryImpl) newFactory;
    handler = newHandler;
    index = newIndex;
    first = newFirst;
    second = newSecond;
    var = newVar;
    currentNodes = null;

    // find the subIndex from the main index
    subIndex = newSubIndex;

    // check that data exists
    if (null != subIndex) {
      // now find the set from the sub index map
      subGroup = subIndex.get(second);
      if (null != subGroup) {
        // get an iterator for the set
        thirdIndexIterator = subGroup.iterator();
        hasNext = thirdIndexIterator.hasNext();
      }
    }
  }


  /**
   * Returns true if the iteration has more elements.
   *
   * @return <code>true</code> If there is an element to be read.
   */
  public boolean hasNext() {
    return hasNext;
  }


  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration.
   * @throws NoSuchElementException iteration has no more elements.
   */
  public Triple next() throws NoSuchElementException {
    if (!hasNext()) throw new NoSuchElementException();

    // Get next node.
    third = (Long) thirdIndexIterator.next();
    hasNext = thirdIndexIterator.hasNext();
    currentNodes = new Long[]{first, second, third};
    return new TripleImpl(nodeFactory, var, first, second, third);
  }


  /**
   * Implemented for java.util.Iterator.
   */
  public void remove() {
    if (null != third) {
      try {
        thirdIndexIterator.remove();
        handler.remove(currentNodes);

        // check if a set was cleaned out
        if (subGroup.isEmpty()) {
          // remove the entry for the set
          subIndex.remove(second);
          // check if a subindex was cleaned out
          if (subIndex.isEmpty()) {
            // remove the subindex
            index.remove(first);
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
