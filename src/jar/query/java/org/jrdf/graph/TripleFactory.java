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

package org.jrdf.graph;

/**
 * A Triple Factory is a class which defines the creation of certain sets of
 * triples.  This includes reification, containers and collections.
 *
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public interface TripleFactory {

  /**
   * Reifies a triple.  A triple made up of the first three nodes is added to
   * graph and the reificationNode is used to reify the triple.
   *
   * @param subjectNode the subject of the triple.
   * @param predicateNode the predicate of the triple.
   * @param objectNode the object of the triple.
   * @param reificationNode a node denoting the reified triple.
   * @throws TripleFactoryException If the resource failed to be added.
   * @throws AlreadyReifiedException If there was already a triple URI for
   *     the given triple.
   */
  void reifyTriple(SubjectNode subjectNode,
      PredicateNode predicateNode, ObjectNode objectNode,
      SubjectNode reificationNode) throws TripleFactoryException,
      AlreadyReifiedException;

  /**
   * Reifies a triple.  The triple added to the graph and the
   * reificationNode is used to reify the triple.
   *
   * @param triple the triple to be reified.
   * @param reificationNode a node denoting the reified triple.
   * @throws TripleFactoryException If the resource failed to be added.
   * @throws AlreadyReifiedException If there was already a triple URI for
   *     the given triple.
   */
  void reifyTriple(Triple triple, SubjectNode reificationNode)
      throws TripleFactoryException, AlreadyReifiedException;

  /**
   * Inserts a alternative using the given subject.  The subject is also
   * the object of a proceeding statement that identifies the container.
   *
   * @param subjectNode the subject node of the triple.
   * @param alternative the alternative to add.
   * @throws TripleFactoryException If the resources were failed to be added.
   */
  void addAlternative(SubjectNode subjectNode, Alternative alternative)
      throws TripleFactoryException;

  /**
   * Inserts a bag using the given subject.  The subject is also
   * the object of a proceeding statement that identifies the container.
   *
   * @param subjectNode the subject node of the triple.
   * @param bag the bag to add.
   * @throws TripleFactoryException If the resources were failed to be added.
   */
  void addBag(SubjectNode subjectNode, Bag bag)
      throws TripleFactoryException;

  /**
   * Inserts a sequence using the given subject.  The subject is also
   * the object of a proceeding statement that identifies the container.
   *
   * @param subjectNode the subject node of the triple.
   * @param sequence the sequence to add.
   * @throws TripleFactoryException If the resources were failed to be added.
   */
  void addSequence(SubjectNode subjectNode, Sequence sequence)
      throws TripleFactoryException;

  /**
   * Inserts a collection using the given subject.  The subject is also
   * the object of a proceeding statement that identifies the collection.
   *
   * @param firstNode the subject node of the triple.
   * @param collection the collection to add.
   * @throws TripleFactoryException If the resources were failed to be added.
   */
  void addCollection(SubjectNode firstNode, Collection collection)
      throws TripleFactoryException;
}
