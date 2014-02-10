/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The JRDF Project.  All rights reserved.
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

// Java 2 standard packages

import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.Sequence;

import java.util.ArrayList;

/**
 * An implementation of {@link Sequence}.
 *
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public class SequenceImpl extends ArrayList<ObjectNode> implements Sequence {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = -8659292687902645229L;

  public SequenceImpl() {
  }


  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, Sequence.
   */
  public boolean addAll(java.util.Collection<? extends ObjectNode> c)
      throws IllegalArgumentException {
    if (!(c instanceof Sequence)) {
      throw new IllegalArgumentException("Can only add sequences to other " +
          "sequences");
    }

    return super.addAll(c);
  }

  public boolean addAll(Sequence s) throws IllegalArgumentException {
    return super.addAll(s);
  }

  public boolean addAll(int index, java.util.Collection<? extends ObjectNode> c) throws
      IllegalArgumentException {
    if (!(c instanceof Sequence)) {
      throw new IllegalArgumentException("Can only add sequences to other sequences");
    }

    return super.addAll(index, c);
  }

  public boolean addAll(int index, Sequence s) {
    return super.addAll(index, s);
  }

  public boolean containsAll(java.util.Collection<?> c) {
    if (!(c instanceof Sequence)) {
      throw new IllegalArgumentException("Can only add sequences to other sequences");
    }
    return super.containsAll(c);
  }

  public boolean containsAll(Sequence s) {
    return super.containsAll(s);
  }

  public boolean removeAll(java.util.Collection<?> c) throws IllegalArgumentException {
    if (!(c instanceof Sequence)) {
      throw new IllegalArgumentException("Can only add bags to other bags");
    }
    return super.removeAll(c);
  }

  public boolean removeAll(Sequence s) throws IllegalArgumentException {
    return super.removeAll(s);
  }

  public boolean retainAll(java.util.Collection<?> c) throws IllegalArgumentException {
    if (!(c instanceof Sequence)) {
      throw new IllegalArgumentException("Can only add bags to other bags");
    }
    return super.retainAll(c);
  }

  public boolean retainAll(Sequence s) throws IllegalArgumentException {
    return super.retainAll(s);
  }


  public boolean contains(ObjectNode o) {
    return super.contains(o);
  }


  public int indexOf(ObjectNode o) throws IllegalArgumentException {
    return super.indexOf(o);
  }


  public int lastIndexOf(ObjectNode o) throws IllegalArgumentException {
    return super.lastIndexOf(o);
  }


  public boolean remove(ObjectNode o) throws IllegalArgumentException {
    return super.remove(o);
  }

}
