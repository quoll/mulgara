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

// Java packages
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jrdf.util.ClosableIterator;
import org.jrdf.vocabulary.RDF;

import java.net.URI;

/**
 * Abstract test case for graph implementations.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public abstract class AbstractTripleFactoryTest extends TestCase {

  /**
   * Instance of a graph object.
   */
  private Graph graph;

  /**
   * Instance of a factory for the graph
   */
  protected GraphElementFactory elementFactory;

  /**
   * Instance of the triple factory for the graph.
   */
  private TripleFactory tripleFactory;

  // The following are interally used "constants"
  private BlankNode blank1;
  private BlankNode blank2;

  private URI uri1;
  private URI uri2;
  private URI uri3;
  private URIReference ref1;
  private URIReference ref2;
  private URIReference ref3;

  private final String TEST_STR1 = "A test string";
  private final String TEST_STR2 = "Another test string";
  private Literal l1;
  private Literal l2;

  /**
   * Hook for test runner to obtain an empty test suite from, because this test
   * can't be run (it's abstract). This must be overridden in subclasses.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    return suite;
  }


  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public AbstractTripleFactoryTest(String name) {
    super(name);
  }

  /**
   * Create test instance.
   */
  public void setUp() throws Exception {
    graph = newGraph();
    elementFactory = graph.getElementFactory();
    tripleFactory = graph.getTripleFactory();

    blank1 = elementFactory.createResource();
    blank2 = elementFactory.createResource();

    uri1 = new URI("http://namespace#somevalue");
    uri2 = new URI("http://namespace#someothervalue");
    uri3 = new URI("http://namespace#yetanothervalue");
    ref1 = elementFactory.createResource(uri1);
    ref2 = elementFactory.createResource(uri2);
    ref3 = elementFactory.createResource(uri3);

    l1 = elementFactory.createLiteral(TEST_STR1);
    l2 = elementFactory.createLiteral(TEST_STR2);
  }

  //
  // implementation interfaces
  //

  /**
   * Create a graph implementation.
   *
   * @return A new GraphImpl.
   */
  protected abstract Graph newGraph() throws Exception;

  /**
   * Get the node used for subject reification.
   *
   * @return The subject reification node.
   */
  protected abstract PredicateNode getReifySubject() throws TripleFactoryException;

  /**
   * Get the node used for predicate reification.
   *
   * @return The predicate reification node.
   */
  protected abstract PredicateNode getReifyPredicate() throws TripleFactoryException;

  /**
   * Get the node used for object reification.
   *
   * @return The object reification node.
   */
  protected abstract PredicateNode getReifyObject() throws TripleFactoryException;

  /**
   * Get the node used for rdf:type.
   *
   * @return The object rdf:type node.
   */
  protected abstract PredicateNode getRdfType() throws TripleFactoryException;

  /**
   * Get the node used for rdf:Statement.
   *
   * @return The object rdf:statement node.
   */
  protected abstract ObjectNode getRdfStatement() throws TripleFactoryException;

  /**
   * Create a concrete Collection.
   *
   * @return the new collection.
   */
  protected abstract Collection createCollection(ObjectNode[] objects);

  /**
   * Create a concrete alternative
   *
   * @return the new alternative.
   */
  protected abstract Alternative createAlternative(ObjectNode[] objects);

  /**
   * Create a concrete bag
   *
   * @return the new bag.
   */
  protected abstract Bag createBag(ObjectNode[] objects);

  /**
   * Create a concrete sequence
   *
   * @return the new sequence.
   */
  protected abstract Sequence createSequence(ObjectNode[] objects);

  //
  // Test cases
  //

  /**
   * Tests reification.
   */
  public void testReification() throws Exception {
    PredicateNode reifySubject = getReifySubject();
    PredicateNode reifyPredicate = getReifyPredicate();
    PredicateNode reifyObject = getReifyObject();
    PredicateNode rdfType = getRdfType();
    ObjectNode rdfStatement = getRdfStatement();
    assertTrue(graph.isEmpty());

    // Make a reification about a triple that does not exist in the graph.
    URIReference u = elementFactory.createResource(uri1);
    tripleFactory.reifyTriple(blank1, ref1, blank2, u);
    assertEquals(uri1, u.getURI());
    assertEquals(4, graph.getNumberOfTriples());
    assertTrue(graph.contains(u, rdfType, rdfStatement));
    assertTrue(graph.contains(u, reifySubject, blank1));
    assertTrue(graph.contains(u, reifyPredicate, ref1));
    assertTrue(graph.contains(u, reifyObject, blank2));

    // Make a reification about a triple that does exist in the graph.
    Triple t = elementFactory.createTriple(blank1, ref2, blank2);
    u = elementFactory.createResource(uri2);
    graph.add(t);
    tripleFactory.reifyTriple(t, u);
    assertEquals(uri2, u.getURI());
    assertEquals(9, graph.getNumberOfTriples());
    assertTrue(graph.contains(u, rdfType, rdfStatement));
    assertTrue(graph.contains(u, reifySubject, blank1));
    assertTrue(graph.contains(u, reifyPredicate, ref2));
    assertTrue(graph.contains(u, reifyObject, blank2));
    assertTrue(graph.contains(blank1, ref2, blank2));

    // test for double insertion (allowed)
    tripleFactory.reifyTriple(blank1, ref1, blank2, elementFactory.createResource(uri1));
    assertEquals(9, graph.getNumberOfTriples());

    // test for double insertion (allowed)
    tripleFactory.reifyTriple(t, elementFactory.createResource(uri2));
    assertEquals(9, graph.getNumberOfTriples());

    // test for insertion with a different reference (allowed)
    tripleFactory.reifyTriple(blank1, ref1, blank2, elementFactory.createResource(uri3));
    assertEquals(13, graph.getNumberOfTriples());

    // test for insertion of a new triple with an existing reference (disallowed)
    testCantInsert(blank2, ref1, blank1, elementFactory.createResource(uri1));
    assertEquals(13, graph.getNumberOfTriples());

    // test for insertion with a different reference (disallowed)
    testCantInsert(t, uri3);
    assertEquals(13, graph.getNumberOfTriples());

    // test for insertion of a new triple with an existing reference
    testCantInsert(elementFactory.createTriple(blank2, ref2, blank2), uri2);
    assertEquals(13, graph.getNumberOfTriples());

    // do it all again for blank nodes
    // Make reification that does not exist in graph
    BlankNode b = elementFactory.createResource();
    tripleFactory.reifyTriple(blank1, ref1, l1, b);
    assertEquals(17, graph.getNumberOfTriples());
    assertTrue(graph.contains(b, rdfType, rdfStatement));
    assertTrue(graph.contains(b, reifySubject, blank1));
    assertTrue(graph.contains(b, reifyPredicate, ref1));
    assertTrue(graph.contains(b, reifyObject, l1));

    // Make a reification using a blank node for a statement that does exist
    // in the graph.
    t = elementFactory.createTriple(blank1, ref2, l2);
    graph.add(t);
    b = elementFactory.createResource();
    tripleFactory.reifyTriple(t, b);
    assertEquals(22, graph.getNumberOfTriples());
    assertTrue(graph.contains(b, rdfType, rdfStatement));
    assertTrue(graph.contains(b, reifySubject, blank1));
    assertTrue(graph.contains(b, reifyPredicate, ref2));
    assertTrue(graph.contains(b, reifyObject, l2));
    assertTrue(graph.contains(blank1, ref2, l2));

    // test for double insertion
    testCanInsert(blank1, ref1, blank2);
    // test for insertion with a a used blank reference
    testCantInsert(blank1, ref3, blank2, u);
    // test that the graph did not change with the invalid insertions
    assertEquals(26, graph.getNumberOfTriples());

    // test for double insertion
    testCanInsert(t);
    // test for insertion with a a used blank reference
    testCantInsert(elementFactory.createTriple(blank1, ref3, blank2), u.getURI());
    // test that the graph did not change with the invalid insertions
    assertEquals(30, graph.getNumberOfTriples());

    // Test reifying an existing statement
    b = elementFactory.createResource();
    graph.add(ref3, ref3, ref3);

    try {
      tripleFactory.reifyTriple(ref3, ref3, ref3, b);
    }
    catch (AlreadyReifiedException e) {
      fail("Should allow reification of an existing");
    }
  }

  /**
   * Test collections implmentations.
   */
  public void testCollections() throws Exception {

    // Ensure graph is empty before starting.
    assertTrue(graph.isEmpty());

    // Create initial statement
    SubjectNode s = (SubjectNode) elementFactory.createResource(
        new URI("http://example.org/basket"));
    PredicateNode p = (PredicateNode) elementFactory.createResource(
        new URI("http://example.org/stuff/1.0/hasFruit"));
    ObjectNode o = (ObjectNode)
        elementFactory.createResource();

    // Add to graph
    graph.add(s, p, o);

    // Create collection object.
    ObjectNode[] fruit = new ObjectNode[3];
    fruit[0] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/banana"));
    fruit[1] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/kiwi"));
    fruit[2] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/pineapple"));

    PredicateNode rdfFirst = (PredicateNode) elementFactory.createResource(
        RDF.FIRST);
    PredicateNode rdfRest = (PredicateNode) elementFactory.createResource(
        RDF.REST);
    ObjectNode rdfNil = (ObjectNode) elementFactory.createResource(
        RDF.NIL);

    // Create collection and add
    Collection collection = createCollection(fruit);

    // Add the collection to the graph.
    tripleFactory.addCollection((SubjectNode) o, collection);

    // Check we've inserted it correctly.
    assertEquals("Should have seven statements", 7, graph.getNumberOfTriples());
    assertTrue("Should have first statement", graph.contains(s, p, o));
    assertTrue("Should have first object and first collection object",
      graph.contains((SubjectNode) o, rdfFirst, fruit[0]));

    // Get all rdf:first statements
    ClosableIterator<Triple> iter = graph.find(null, rdfFirst, null);
    int counter = 0;
    while (iter.hasNext()) {
//      System.err.println(iter.next());
      iter.next();
      counter++;
    }
    assertTrue("Should have three rdf:first statements, not " + counter, 3 ==
        counter);

    // Find all three parts of the collection.
    for (int index = 0; index < fruit.length; index++) {
      assertTrue("Should contain: " + fruit[index], graph.contains(
          null, rdfFirst, fruit[index]));
    }

    // Get all rdf:rest statements
    iter = graph.find(null, rdfRest, null);
    counter = 0;
    while (iter.hasNext()) {
//      System.err.println(iter.next());
      iter.next();
      counter++;
    }
    assertTrue("Should have three rdf:rest statements", 3 == counter);

    // Get all rdf:rest with rdf:nil statements
    iter = graph.find(null, rdfRest, rdfNil);
    counter = 0;
    while (iter.hasNext()) {
//      System.err.println(iter.next());
      iter.next();
      counter++;
    }

    assertTrue("Should have one rdf:rest with rdf:nil statements", 1 ==
        counter);
  }

  /**
   * Test altnerative implementation.
   */
  public void testAlternative() throws Exception {

    // Ensure graph is empty before starting.
    assertTrue(graph.isEmpty());

    // Create initial statement
    SubjectNode s = (SubjectNode) elementFactory.createResource(
        new URI("http://example.org/favourite-bananas"));

    // Create collection object.
    ObjectNode[] fruit = new ObjectNode[4];
    fruit[0] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/banana"));
    fruit[1] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/cavendish"));
    fruit[2] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/ladyfinger"));
    fruit[3] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/banana"));

    PredicateNode rdfType = (PredicateNode) elementFactory.createResource(
        RDF.TYPE);
    ObjectNode rdfAlternative = (ObjectNode) elementFactory.createResource(
        RDF.ALT);

    // Create collection and add
    Alternative alt = createAlternative(fruit);

    // Add the collection to the graph.
    tripleFactory.addAlternative(s, alt);

    // Check we've inserted it correctly (banana is in twice should be removed)
    assertEquals("Should have five statements", 4, graph.getNumberOfTriples());
    assertTrue("Should have statement", graph.contains(s, rdfType, rdfAlternative));
    assertTrue("Should have statement", graph.contains(s, null, fruit[0]));
    assertTrue("Should have statement", graph.contains(s, null, fruit[1]));
    assertTrue("Should have statement", graph.contains(s, null, fruit[2]));
    assertTrue("Should have statement", graph.contains(s, null, fruit[3]));

    // Check that it doesn't allow duplicates.
    ClosableIterator<Triple> iter = graph.find(null, null, fruit[0]);
    int count = 0;
    while (iter.hasNext()) {
      iter.next();
      count++;
    }
    assertTrue("Should have only the same statements: " + fruit[0], 1 == count);
  }

  /**
   * Test bag implementation.
   */
  public void testBag() throws Exception {

    // Ensure graph is empty before starting.
    assertTrue(graph.isEmpty());

    // Create initial statement
    SubjectNode s = (SubjectNode) elementFactory.createResource(
        new URI("http://example.org/favourite-fruit"));

    // Create collection object.
    ObjectNode[] fruit = new ObjectNode[5];
    fruit[0] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/banana"));
    fruit[1] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/kiwi"));
    fruit[2] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/pineapple"));
    fruit[3] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/pineapple"));
    fruit[4] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/banana"));

    PredicateNode rdfType = (PredicateNode) elementFactory.createResource(
        RDF.TYPE);
    ObjectNode rdfBag = (ObjectNode) elementFactory.createResource(
        RDF.BAG);

    // Create collection and add
    Bag bag = createBag(fruit);

    // Add the collection to the graph.
    tripleFactory.addBag(s, bag);

    // Check we've inserted it correctly
    assertEquals("Should have six statements", 6, graph.getNumberOfTriples());
    assertTrue("Should have statement", graph.contains(s, rdfType, rdfBag));
    assertTrue("Should have statement", graph.contains(s, null, fruit[0]));
    assertTrue("Should have statement", graph.contains(s, null, fruit[1]));
    assertTrue("Should have statement", graph.contains(s, null, fruit[2]));

    // Check that it allows duplicates.
    ClosableIterator<Triple> iter = graph.find(s, null, fruit[2]);
    int count = 0;
    while (iter.hasNext()) {
      iter.next();
      count++;
    }
    assertTrue("Should have two of the same statements: " + fruit[2], 2 ==
        count);
  }

  /**
   * Test sequence implmentation.
   */
  public void testSequence() throws Exception {

    // Ensure graph is empty before starting.
    assertTrue(graph.isEmpty());

    // Create initial statement
    SubjectNode s = (SubjectNode) elementFactory.createResource(
        new URI("http://example.org/favourite-fruit"));

    // Create collection object.
    ObjectNode[] fruit = new ObjectNode[4];
    fruit[0] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/banana"));
    fruit[1] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/kiwi"));
    fruit[2] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/pineapple"));
    fruit[3] = (ObjectNode) elementFactory.createResource(new URI(
        "http://example.org/kiwi"));

    PredicateNode rdfType = (PredicateNode) elementFactory.createResource(
        RDF.TYPE);
    PredicateNode rdfOne = (PredicateNode) elementFactory.createResource(
        new URI(RDF.BASE_URI + "_1"));
    PredicateNode rdfTwo = (PredicateNode) elementFactory.createResource(
        new URI(RDF.BASE_URI + "_2"));
    PredicateNode rdfThree = (PredicateNode) elementFactory.createResource(
        new URI(RDF.BASE_URI + "_3"));
    PredicateNode rdfFour = (PredicateNode) elementFactory.createResource(
        new URI(RDF.BASE_URI + "_4"));
    ObjectNode rdfSequence = (ObjectNode) elementFactory.createResource(
        RDF.SEQ);

    // Create collection and add
    Sequence sequence = createSequence(fruit);

    // Add the collection to the graph.
    tripleFactory.addSequence(s, sequence);

    // Check we've inserted it correctly.
    assertEquals("Should have five statements", 5, graph.getNumberOfTriples());
    assertTrue("Should have statement", graph.contains(s, rdfType, rdfSequence));
    assertTrue("Should have statement", graph.contains(s, rdfOne, fruit[0]));
    assertTrue("Should have statement", graph.contains(s, rdfTwo, fruit[1]));
    assertTrue("Should have statement", graph.contains(s, rdfThree, fruit[2]));
    assertTrue("Should have statement", graph.contains(s, rdfFour, fruit[3]));
  }

  /**
   * Utility method to check that a triple cannot be reified.
   *
   * @param subject The subject for the triple.
   * @param predicate The predicate for the triple.
   * @param object The object for the triple.
   * @param r The reification node for the triple.
   * @throws Exception The triple could be reified.
   */
  private void testCantInsert(SubjectNode subject, PredicateNode predicate,
      ObjectNode object, SubjectNode r) throws Exception {
    try {
      tripleFactory.reifyTriple(subject, predicate, object, r);
      assertTrue(false);
    } catch (AlreadyReifiedException e) {
    }
  }


  /**
   * Utility method to check that a triple cannot be reified.
   *
   * @param triple The triple to reify.
   * @param r The reification node for the triple.
   * @throws Exception The triple could be reified.
   */
  private void testCantInsert(Triple triple, URI r) throws Exception {
    try {
      tripleFactory.reifyTriple(triple, elementFactory.createResource(r));
      assertTrue(false);
    } catch (AlreadyReifiedException e) {
    }
  }


  /**
   * Utility method to check that a triple cannot be reified with a blank node.
   *
   * @param subject The subject for the triple.
   * @param predicate The predicate for the triple.
   * @param object The object for the triple.
   * @throws Exception The triple could be reified.
   */
  private void testCanInsert(SubjectNode subject, PredicateNode predicate,
      ObjectNode object) throws Exception {
    try {
      tripleFactory.reifyTriple(subject, predicate, object,
          elementFactory.createResource());
      assertTrue(true);
    } catch (AlreadyReifiedException e) {
    }
  }


  /**
   * Utility method to check that a triple cannot be reified with a blank node.
   *
   * @param triple The triple to reify.
   * @throws Exception The triple could be reified.
   */
  private void testCanInsert(Triple triple) throws Exception {
    try {
      tripleFactory.reifyTriple(triple, elementFactory.createResource());
      assertTrue(true);
    } catch (AlreadyReifiedException e) {
    }
  }
}
