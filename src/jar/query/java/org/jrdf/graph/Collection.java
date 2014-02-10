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

package org.jrdf.graph;

import java.util.Iterator;

/**
 * A Collection holds all the statements of a particular group.  A linked list
 * is created by using pointer statements (REST) to the next item in the
 * collection.  The group is closed, where the last statement in the group
 * points to a NIL entry.
 *
 * See <A HREF="http://www.w3.org/TR/rdf-primer/#collections">4.1 RDF Collections</A>.
 *
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public interface Collection {

  /**
   * Add an ${@link ObjectNode} to the collection.
   *
   * @param element object to add.
   * @return true if the object was added successfully.
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  boolean add(ObjectNode element) throws IllegalArgumentException;

  /**
   * Add an ${@link ObjectNode} to the collection at the given position.
   *
   * @param index the index into the collection to add.
   * @param element the object to add.
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  void add(int index, ObjectNode element) throws IllegalArgumentException;

  /**
   * Add a collection of ${@link ObjectNode}s to this one.
   *
   * @param c the collection to add.
   * @return true if the object was added successfully.
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, Collection.
   */
  boolean addAll(java.util.Collection<ObjectNode> c) throws IllegalArgumentException;

  /**
   * Add a collection of ${@link ObjectNode}s to this one starting at the given
   * index.
   *
   * @param index the index into the collection to start adding.
   * @param c the collection to add.
   * @return true if the object was added successfully.
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, Collection.
   */
  boolean addAll(int index, java.util.Collection<ObjectNode> c) throws
      IllegalArgumentException;

  /**
   * Add an ${@link ObjectNode} to the start of the collection.
   *
   *
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  void addFirst(ObjectNode element);

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  void addLast(ObjectNode element);

  /**
   * Search the collection and return if the object was found or not.
   *
   * @return true of the object was found.
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  boolean contains(ObjectNode element);

  /**
   * Search the collection and return if all of the given objects were found.
   *
   * @param c the collection containing the elements to search.
   * @return true if all of the given objects were found. 
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, Collection.
   */
  boolean containsAll(java.util.Collection<ObjectNode> c);

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  int indexOf(ObjectNode element) throws IllegalArgumentException;

  Iterator<ObjectNode> iterator();

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  int lastIndexOf(ObjectNode element) throws IllegalArgumentException;

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  boolean remove(ObjectNode element) throws IllegalArgumentException;

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, Collection.
   */
  boolean removeAll(java.util.Collection<ObjectNode> c) throws IllegalArgumentException;

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, Collection.
   */
  boolean retainAll(java.util.Collection<ObjectNode> c) throws IllegalArgumentException;

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  Object set(int index, ObjectNode element) throws IllegalArgumentException;
}
