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

import org.jrdf.vocabulary.RDF;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * The base implementation of the Triple Factory which adds to a given graph
 * reified statements, containers and collections.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public abstract class AbstractTripleFactory implements TripleFactory {

  /**
   * The graph that this factory constructs nodes for.
   */
  protected Graph graph;

  /**
   * The graph element factory.
   */
  protected GraphElementFactory elementFactory;

  /**
   * Reifies a triple.  A triple made up of the first three nodes is added to
   * graph and the reificationNode is used to reify the triple.
   *
   * @param subjectNode the subject of the triple.
   * @param predicateNode the predicate of the triple.
   * @param objectNode the object of the triple.
   * @param reificationNode a node denoting the reified triple.
   * @throws TripleFactoryException If the resource failed to be created.
   * @throws AlreadyReifiedException If there was already a triple URI for
   *     the given triple.
   */
  public void reifyTriple(SubjectNode subjectNode,
      PredicateNode predicateNode, ObjectNode objectNode,
      SubjectNode reificationNode) throws TripleFactoryException,
      AlreadyReifiedException {

    // create the reification node
    try {
      reallyReifyTriple(subjectNode, predicateNode, objectNode, reificationNode);
    }
    catch (GraphElementFactoryException gefe) {
      throw new TripleFactoryException(gefe);
    }
  }

  /**
   * Creates a reification of a triple.  The triple added to the graph and the
   * reificationNode is used to reify the triple.
   *
   * @param triple the triple to be reified.
   * @param reificationNode a node denoting the reified triple.
   * @throws TripleFactoryException If the resource failed to be created.
   * @throws AlreadyReifiedException If there was already a triple URI for
   *     the given triple.
   */
  public void reifyTriple(Triple triple, SubjectNode reificationNode)
      throws TripleFactoryException, AlreadyReifiedException {

    try {
      reallyReifyTriple(triple.getSubject(), triple.getPredicate(),
          triple.getObject(), reificationNode);
    }
    catch (GraphElementFactoryException gefe) {
      throw new TripleFactoryException(gefe);
    }
  }

  /**
   * Creates a reification of a triple.
   *
   * @param subjectNode the subject of the triple.
   * @param predicateNode the predicate of the triple.
   * @param objectNode the object of the triple.
   * @return a node denoting the reified triple.
   * @throws GraphElementFactoryException If the resource failed to be created.
   * @throws AlreadyReifiedException If there was already a triple URI for
   *     the given triple.
   */
  private Node reallyReifyTriple(SubjectNode subjectNode,
      PredicateNode predicateNode, ObjectNode objectNode, Node ru)
      throws GraphElementFactoryException, AlreadyReifiedException {

    // get the nodes used for reification
    PredicateNode hasSubject = elementFactory.createResource(RDF.SUBJECT);
    PredicateNode hasPredicate = elementFactory.createResource(RDF.PREDICATE);
    PredicateNode hasObject = elementFactory.createResource(RDF.OBJECT);
    URIReference rdfType = elementFactory.createResource(RDF.TYPE);
    URIReference rdfStatement = elementFactory.createResource(RDF.STATEMENT);

    // assert that the statement is not already reified
    try {

      // An error if ru already reifies anything but the given s, p, o.
      if (graph.contains((SubjectNode) ru, rdfType, rdfStatement) &&
          !(graph.contains((SubjectNode) ru, hasSubject, (ObjectNode) subjectNode) &&
            graph.contains((SubjectNode) ru, hasPredicate, (ObjectNode) predicateNode) &&
            graph.contains((SubjectNode) ru, hasObject, objectNode))) {

        throw new AlreadyReifiedException("SkipListNode: " + ru + " already used in " +
          "reification");
      }

      // insert the reification statements
      graph.add((SubjectNode) ru, rdfType, rdfStatement);
      graph.add((SubjectNode) ru, hasSubject, (ObjectNode) subjectNode);
      graph.add((SubjectNode) ru, hasPredicate, (ObjectNode) predicateNode);
      graph.add((SubjectNode) ru, hasObject, (ObjectNode) objectNode);
    }
    catch (GraphException e) {
      throw new GraphElementFactoryException(e);
    }

    // return the ru to make it easier for returning the value from this method
    return ru;
  }

  public void addAlternative(SubjectNode subjectNode, Alternative alternative)
      throws TripleFactoryException {
    try {

      graph.add(subjectNode,
          (PredicateNode) elementFactory.createResource(RDF.TYPE),
          (ObjectNode) elementFactory.createResource(RDF.ALT));

      addContainer(subjectNode, alternative);
    }
    catch (GraphException e) {
      throw new TripleFactoryException(e);
    }
    catch (GraphElementFactoryException e) {
      throw new TripleFactoryException(e);
    }
  }

  public void addBag(SubjectNode subjectNode, Bag bag)
      throws TripleFactoryException {
    try {

      graph.add(subjectNode,
          (PredicateNode) elementFactory.createResource(RDF.TYPE),
          (ObjectNode) elementFactory.createResource(RDF.BAG));

      addContainer(subjectNode, bag);
    }
    catch (GraphException e) {
      throw new TripleFactoryException(e);
    }
    catch (GraphElementFactoryException e) {
      throw new TripleFactoryException(e);
    }
  }

  public void addSequence(SubjectNode subjectNode, Sequence sequence)
      throws TripleFactoryException {
    try {

      graph.add(subjectNode,
          (PredicateNode) elementFactory.createResource(RDF.TYPE),
          (ObjectNode) elementFactory.createResource(RDF.SEQ));

      addContainer(subjectNode, sequence);
    }
    catch (GraphException e) {
      throw new TripleFactoryException(e);
    }
    catch (GraphElementFactoryException e) {
      throw new TripleFactoryException(e);
    }
  }

  /**
   * Creates a container.
   *
   * @param subjectNode the subject of the triple.
   * @param container the container to add.
   * @throws TripleFactoryException If the resource failed to be created.
   * @throws AlreadyReifiedException If there was already a triple URI for
   *     the given triple.
   */
  private void addContainer(SubjectNode subjectNode, Container container)
      throws TripleFactoryException {

    // assert that the statement is not already reified
    try {

      // Insert statements from colletion.
      long counter = 1;
      Iterator<ObjectNode> iter = container.iterator();

      while (iter.hasNext()) {
        ObjectNode object = iter.next();
        graph.add(subjectNode,
            (PredicateNode) elementFactory.createResource(new URI(
            RDF.BASE_URI + "_" + counter++)),
            object);
      }
    }
    catch (URISyntaxException e) {
      throw new TripleFactoryException(e);
    }
    catch (GraphElementFactoryException e) {
      throw new TripleFactoryException(e);
    }
    catch (GraphException e) {
      throw new TripleFactoryException(e);
    }
  }

  public void addCollection(SubjectNode firstNode, Collection collection)
      throws TripleFactoryException {

    try {

      // Constants.
      PredicateNode rdfFirst = (PredicateNode) elementFactory.createResource(
          RDF.FIRST);
      PredicateNode rdfRest = (PredicateNode) elementFactory.createResource(
          RDF.REST);
      ObjectNode rdfNil = (ObjectNode) elementFactory.createResource(RDF.NIL);

      // Insert statements from the Colletion using the first given node.
      SubjectNode subject = firstNode;

      // Iterate through all elements in the Collection.
      Iterator<ObjectNode> iter = collection.iterator();
      while (iter.hasNext()) {

        // Get the next object and create the new FIRST statement.
        ObjectNode object = iter.next();
        graph.add(subject, rdfFirst, object);

        // Check if there are any more elements in the Collection.
        if (iter.hasNext()) {

          // Create a new blank node, link the existing subject to it using
          // the REST predicate.
          ObjectNode newSubject = (ObjectNode) elementFactory.createResource();
          graph.add(subject, rdfRest, newSubject);
          subject = (SubjectNode) newSubject;
        }
        else {

          // If we are at the end of the list link the existing subject to NIL
          // using the REST predicate.
          graph.add(subject, rdfRest, rdfNil);
        }
      }
    }
    catch (GraphElementFactoryException e) {
      throw new TripleFactoryException(e);
    }
    catch (GraphException e) {
      throw new TripleFactoryException(e);
    }
  }
}
