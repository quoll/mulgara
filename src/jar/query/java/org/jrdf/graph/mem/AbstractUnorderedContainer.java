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
import org.jrdf.graph.Container;
import org.jrdf.graph.ObjectNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The base class for the implementation of Bag and Alternative.
 *
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public abstract class AbstractUnorderedContainer implements Container {

  /**
   * The hashmap containing the elements.
   */
  Map<Long,ObjectNode> elements = new HashMap<Long,ObjectNode>();

  /**
   * Counter used to generate keys to add to the hashmap.
   */
  long key;

  public int size() {
    return elements.values().size();
  }

  public boolean isEmpty() {
    return elements.values().isEmpty();
  }

  public boolean contains(Object o) {
    return elements.values().contains(o);
  }

  public Iterator<ObjectNode> iterator() {
    return elements.values().iterator();
  }

  public ObjectNode[] toArray() {
    return (ObjectNode[])elements.values().toArray();
  }

  public ObjectNode[] toArray(ObjectNode[] a) {
    return (ObjectNode[])elements.values().toArray(a);
  }

  public boolean add(ObjectNode o) throws IllegalArgumentException {
    if (!(o instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only add Object nodes");
    }

    elements.put(new Long(key++), o);
    return true;
  }

  public boolean remove(Object o) throws IllegalArgumentException {
    if (!(o instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only add Object nodes");
    }

    Iterator<Map.Entry<Long,ObjectNode>> iter = elements.entrySet().iterator();
    boolean found = false;

    // Removes the first entry in the map that matches the given object.
    while (!found && iter.hasNext()) {
      Map.Entry<Long,ObjectNode> entry = iter.next();
      if (o.equals(entry.getValue())) {
        elements.remove(o);
        found = true;
      }
    }

    return found;
  }

  public void clear() {
    key = 0;
    elements.clear();
  }

  public int hashCode() {
    return elements.hashCode();
  }

  public abstract boolean equals(Object o);
}