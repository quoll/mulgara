/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The JRDF Project.  All rights reserved.
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

import org.jrdf.graph.*;
import org.jrdf.util.ClosableIterator;

import java.util.NoSuchElementException;

/**
 * An iterator that returns only a single triple, if any exists.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 *
 * @version $Revision: 624 $
 */
public class ThreeFixedIterator implements ClosableIterator<Triple> {

  /** The graph this iterator will operate on.  Only needed by the remove method. */
  private Graph graph;

  /** The triple to return on */
  private TripleImpl triple;

  /** The triple to remove */
  private TripleImpl removeTriple;


  /**
   * Constructor.
   */
  ThreeFixedIterator(Graph newGraph, Node subject, Node predicate, Node object) throws GraphException {
    graph = newGraph;
    if (newGraph.contains((SubjectNode)subject, (PredicateNode)predicate, (ObjectNode)object)) {
      triple = new TripleImpl((SubjectNode)subject, (PredicateNode)predicate, (ObjectNode)object);
    }
    removeTriple = null;
  }


  /**
   * Returns true if the iteration has more elements.
   * @return <code>true</code> If there is an element to be read.
   */
  public boolean hasNext() {
    return null != triple;
  }


  /**
   * Returns the next element in the iteration.
   * @return the next element in the iteration.
   * @throws NoSuchElementException iteration has no more elements.
   */
  public Triple next() throws NoSuchElementException {
    if (null == triple) throw new NoSuchElementException();
    // return the triple, clearing it first so next will fail on a subsequent call
    removeTriple = triple;
    triple = null;
    return removeTriple;
  }


  /**
   * @see java.util.Iterator#remove()
   */
  public void remove() {
    if (null != removeTriple) {
      try {
        graph.remove(removeTriple);
        removeTriple = null;
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
