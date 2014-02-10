/*
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jrdf.util.ClosableIterator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract test case for graph implementations.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public abstract class AbstractGraphTest extends TestCase {

  /**
   * Instance of a graph object.
   */
  protected Graph graph;

  /**
   * Instance of a factory for the graph.
   */
  private GraphElementFactory elementFactory;

  /**
   * Blank node 1.
   */
  protected BlankNode blank1;

  /**
   * Blank node 2.
   */
  protected BlankNode blank2;

  private URI uri1;
  private URI uri2;
  private URI uri3;
  protected URIReference ref1;
  protected URIReference ref2;
  protected URIReference ref3;

  /**
   * Used to create literal.
   */
  private static final String TEST_STR1 = "A test string";

  /**
   * Used to create literal.
   */
  private static final String TEST_STR2 = "Another test string";

  /**
   * Literal 1.
   */
  protected static Literal l1;

  /**
   * Literal 2.
   */
  protected static Literal l2;

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
  protected AbstractGraphTest(String name) {
    super(name);
  }

  /**
   * Create test instance.
   *
   * @throws Exception A generic exception - this should cause the tests to
   *   fail.
   */
  public void setUp() throws Exception {
    graph = newGraph();
    elementFactory = graph.getElementFactory();

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
   * @throws Exception A generic exception - this should cause the tests to
   *   fail.
   * @return A new Graph.
   */
  protected abstract Graph newGraph() throws Exception;

  //
  // Test cases
  //

  /**
   * Tests that a new graph is empty.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testEmpty() throws Exception {
    assertTrue(graph.isEmpty());
    assertEquals(0, graph.getNumberOfTriples());
  }

  /**
   * Tests that it is possible to get a NodeFactory from a graph.
   */
  public void testFactory() {
    GraphElementFactory f = graph.getElementFactory();
    assertTrue(null != f);
  }

  /**
   * Tests addition.
   *
   * @throws Exception A generic exception - this should cause the tests to
   *   fail.
   */
  public void testAddition() throws Exception {

    // add in a triple by nodes
    graph.add(blank1, ref1, blank2);

    assertFalse(graph.isEmpty());
    assertEquals(1, graph.getNumberOfTriples());

    // add in a whole triple
    Triple triple2 = elementFactory.createTriple(blank2, ref1, blank2);
    graph.add(triple2);

    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // add in the first triple again
    graph.add(blank1, ref1, blank2);

    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // add in the second whole triple again
    Triple triple2b = elementFactory.createTriple(blank2, ref1, blank2);
    graph.add(triple2b);
    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // and again
    graph.add(triple2);
    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // Add using iterator
    List<Triple> list = new ArrayList<Triple>();
    list.add(elementFactory.createTriple(ref1, ref1, ref1));
    list.add(elementFactory.createTriple(ref2, ref2, ref2));

    graph.add(list.iterator());
    assertFalse(graph.isEmpty());
    assertEquals(4, graph.getNumberOfTriples());
  }

  /**
   * Tests removal.
   *
   * @throws Exception A generic exception - this should cause the tests to
   *   fail.
   */
  public void testRemoval() throws Exception {
    // add some test data
    graph.add(blank1, ref1, blank2);
    graph.add(blank1, ref2, blank2);
    graph.add(ref1, ref2, l2);
    Triple t1 = elementFactory.createTriple(blank2, ref1, blank1);
    graph.add(t1);
    Triple t2 = elementFactory.createTriple(blank2, ref2, blank1);
    graph.add(t2);
    Triple t3 = elementFactory.createTriple(blank2, ref1, l1);
    graph.add(t3);

    // check that all is well
    assertFalse(graph.isEmpty());
    assertEquals(6, graph.getNumberOfTriples());

    // delete the first statement
    graph.remove(blank1, ref1, blank2);
    assertEquals(5, graph.getNumberOfTriples());

    // delete the last statement
    graph.remove(t3);
    assertEquals(4, graph.getNumberOfTriples());

    // delete the next last statement with a new "triple object"
    t2 = elementFactory.createTriple(blank2, ref2, blank1);
    graph.remove(t2);
    assertEquals(3, graph.getNumberOfTriples());

    // delete the next last statement with a triple different to what it was built with
    graph.remove(blank2, ref1, blank1);
    assertEquals(2, graph.getNumberOfTriples());

    // delete the next last statement with a triple different to what it was built with
    graph.remove(ref1, ref2, l2);
    assertEquals(1, graph.getNumberOfTriples());

    // delete the wrong triple
    try {
      graph.remove(blank2, ref1, blank1);
      assertTrue(false);
    }
    catch (GraphException e) { /* no-op */}
    assertEquals(1, graph.getNumberOfTriples());

    // delete a triple that never existed
    try {
      graph.remove(blank2, ref2, l2);
      assertTrue(false);
    }
    catch (GraphException e) { /* no-op */}
    assertEquals(1, graph.getNumberOfTriples());

    // and delete with a triple object
    t1 = elementFactory.createTriple(blank2, ref1, blank1);
    try {
      graph.remove(t1);
      assertTrue(false);
    }
    catch (GraphException e) { /* no-op */}
    assertEquals(1, graph.getNumberOfTriples());

    // now clear out the graph
    assertFalse(graph.isEmpty());
    graph.remove(blank1, ref2, blank2);
    assertTrue(graph.isEmpty());
    assertEquals(0, graph.getNumberOfTriples());

    // check that we can't still remove things
    try {
      graph.remove(blank1, ref2, blank2);
      assertTrue(false);
    }
    catch (GraphException e) { /* no-op */}
    assertTrue(graph.isEmpty());
    assertEquals(0, graph.getNumberOfTriples());

    // Check removal using iterator
    graph.add(elementFactory.createTriple(ref1, ref1, ref1));
    graph.add(elementFactory.createTriple(ref2, ref2, ref2));

    List<Triple> list = new ArrayList<Triple>();
    list.add(elementFactory.createTriple(ref1, ref1, ref1));
    list.add(elementFactory.createTriple(ref2, ref2, ref2));
    graph.remove(list.iterator());

    // check that we can't still remove things
    try {
      graph.remove(ref2, ref2, ref2);
      assertTrue(false);
    }
    catch (GraphException e) { /* no-op */}

    assertTrue(graph.isEmpty());
    assertEquals(0, graph.getNumberOfTriples());
  }

  /**
   * Tests containership.
   *
   * @throws Exception A generic exception - this should cause the tests to
   *   fail.
   */
  public void testContains() throws Exception {
    // add some test data
    graph.add(blank1, ref1, blank2);
    graph.add(blank1, ref2, blank2);
    graph.add(ref1, ref2, l2);
    Triple t1 = elementFactory.createTriple(blank2, ref1, blank1);
    graph.add(t1);
    Triple t2 = elementFactory.createTriple(blank2, ref2, blank1);
    graph.add(t2);
    Triple t3 = elementFactory.createTriple(blank2, ref1, l1);
    graph.add(t3);

    // test containership
    assertTrue(graph.contains(blank1, ref1, blank2));
    // test with existing and built triples
    assertTrue(graph.contains(t1));
    t1 = elementFactory.createTriple(blank2, ref2, blank1);
    assertTrue(graph.contains(t1));

    // test non containership
    assertFalse(graph.contains(blank1, ref1, blank1));
    t1 = elementFactory.createTriple(blank2, ref2, ref1);
    assertFalse(graph.contains(t1));

    // test containership after removal
    graph.remove(blank1, ref1, blank2);
    assertFalse(graph.contains(blank1, ref1, blank2));
    t1 = elementFactory.createTriple(blank1, ref1, blank2);
    assertFalse(graph.contains(t1));

    // put it back in and test again
    graph.add(blank1, ref1, blank2);
    assertTrue(graph.contains(blank1, ref1, blank2));
    assertTrue(graph.contains(t1));

    // Null in contains.
    Graph newGraph = newGraph();
    assertFalse(newGraph.contains(null, null, null));

    // Add a statement
    GraphElementFactory newElementFactory = newGraph.getElementFactory();
    blank1 = newElementFactory.createResource();
    blank2 = newElementFactory.createResource();
    ref1 = newElementFactory.createResource(uri1);
    t1 = newElementFactory.createTriple(blank1, ref1, blank2);
    newGraph.add(t1);

    // Check for existance
    assertTrue(newGraph.contains(null, ref1, blank2));
    assertTrue(newGraph.contains(null, null, blank2));
    assertTrue(newGraph.contains(null, null, null));
    assertTrue(newGraph.contains(blank1, null, blank2));
    assertTrue(newGraph.contains(blank1, null, null));
    assertTrue(newGraph.contains(blank1, ref1, null));

    // Check non-existance
    assertFalse(newGraph.contains(null, ref2, blank1));
    assertFalse(newGraph.contains(null, null, blank1));
    assertFalse(newGraph.contains(blank2, null, blank1));
    assertFalse(newGraph.contains(blank2, null, null));
    assertFalse(newGraph.contains(blank2, ref2, null));
  }

  /**
   * Tests finding.
   *
   * @throws Exception A generic exception - this should cause the tests to
   *   fail.
   */
  public void testFinding() throws Exception {
    graph.add(blank1, ref1, blank2);
    graph.add(blank1, ref1, l1);
    graph.add(blank1, ref2, blank2);
    graph.add(blank1, ref1, l2);
    graph.add(blank2, ref1, blank2);
    graph.add(blank2, ref2, blank2);
    graph.add(blank2, ref1, l1);
    graph.add(blank2, ref1, l2);

    // look for the first triple and check that one is returned
    ClosableIterator<Triple> it = graph.find(blank1, ref1, blank2);
    assertTrue(it.hasNext());
    it.close();

    // look for a non-existent triple
    it = graph.find(ref1, ref1, blank1);
    assertFalse(it.hasNext());
    it.close();

    // look for doubles and check that there is data there
    it = graph.find(blank1, ref1, null);
    assertTrue(it.hasNext());
    it.close();
    it = graph.find(blank1, null, blank2);
    assertTrue(it.hasNext());
    it.close();
    it = graph.find(null, ref1, blank2);
    assertTrue(it.hasNext());
    it.close();

    // look for a non-existent double
    it = graph.find(ref1, ref1, null);
    assertFalse(it.hasNext());
    it.close();
    it = graph.find(ref1, null, blank2);
    assertFalse(it.hasNext());
    it.close();
    it = graph.find(null, ref3, blank2);
    assertFalse(it.hasNext());
    it.close();

    // look for singles
    it = graph.find(blank1, null, null);
    assertTrue(it.hasNext());
    it.close();
    it = graph.find(null, ref1, null);
    assertTrue(it.hasNext());
    it.close();
    it = graph.find(null, null, l1);
    assertTrue(it.hasNext());
    it.close();

    // look for non-existent singles
    it = graph.find(ref1, null, null);
    assertFalse(it.hasNext());
    it.close();
    it = graph.find(null, ref3, null);
    assertFalse(it.hasNext());
    it.close();
    it = graph.find(null, null, ref1);
    assertFalse(it.hasNext());
    it.close();

    // do it all again with triples

    // look for the first triple and check that one is returned
    Triple t = elementFactory.createTriple(blank1, ref1, blank2);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();

    // look for a non-existent triple
    t = elementFactory.createTriple(ref1, ref1, blank1);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();

    // look for doubles and check that there is data there
    t = elementFactory.createTriple(blank1, ref1, null);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();
    t = elementFactory.createTriple(blank1, null, blank2);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, ref1, blank2);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();

    // look for a non-existent double
    t = elementFactory.createTriple(ref1, ref1, null);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();
    t = elementFactory.createTriple(ref1, null, blank2);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, ref3, blank2);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();

    // look for singles
    t = elementFactory.createTriple(blank1, null, null);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, ref1, null);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, null, l1);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();

    // look for non-existent singles
    t = elementFactory.createTriple(ref1, null, null);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, ref3, null);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, null, ref1);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();
  }

  /**
   * Tests iteration over a found set.
   *
   * @throws Exception A generic exception - this should cause the tests to
   *   fail.
   */
  public void testIteration() throws Exception {

    GraphElementFactory factory = graph.getElementFactory();

    //create nodes
    BlankNode bNode1 = factory.createResource();
    BlankNode bNode2 = factory.createResource();
    URIReference testUri1 = factory.createResource(new URI(
        "http://tucana.org/tucana#testUri1"));
    URIReference testUri2 = factory.createResource(new URI(
        "http://tucana.org/tucana#testUri2"));
    Literal literal1 = factory.createLiteral("literal1");
    Literal literal2 = factory.createLiteral("literal2");

    //create some statements
    Triple[] triples = new Triple[16];
    triples[0] = factory.createTriple(bNode1, testUri1, literal1);
    triples[1] = factory.createTriple(bNode1, testUri1, literal2);
    triples[2] = factory.createTriple(bNode1, testUri2, literal1);
    triples[3] = factory.createTriple(bNode1, testUri2, literal2);
    triples[4] = factory.createTriple(bNode2, testUri1, literal1);
    triples[5] = factory.createTriple(bNode2, testUri1, literal2);
    triples[6] = factory.createTriple(bNode2, testUri2, literal1);
    triples[7] = factory.createTriple(bNode2, testUri2, literal2);
    triples[8] = factory.createTriple(bNode1, testUri1, bNode2);
    triples[9] = factory.createTriple(bNode1, testUri2, bNode2);
    triples[10] = factory.createTriple(bNode1, testUri1, testUri2);
    triples[11] = factory.createTriple(bNode1, testUri2, testUri1);
    triples[12] = factory.createTriple(testUri1, testUri2, bNode1);
    triples[13] = factory.createTriple(testUri2, testUri1, bNode1);
    triples[14] = factory.createTriple(testUri1, testUri2, bNode2);
    triples[15] = factory.createTriple(testUri2, testUri1, bNode2);

    //add them
    for (int i = 0; i < triples.length; i++) {

      graph.add(triples[i]);
    }

    //query them and put contents of iterator in a set for checking
    //(iterator may return results in a different order)
    Set<Triple> statements = new HashSet<Triple>();
    ClosableIterator<Triple> iter = graph.find(null, null, null);
    assertTrue("ClosableIterator is returning false for hasNext().", iter.hasNext());
    while (iter.hasNext()) {

      statements.add(iter.next());
    }
    iter.close();

    //check that the iterator contained the correct number of statements
    assertEquals("ClosableIterator is incomplete.", graph.getNumberOfTriples(),
                 statements.size());

    //check the the set contains all the original triples
    for (int i = 0; i < triples.length; i++) {

      if (!statements.contains(triples[i])) {

        fail("Iterator did not contain triple: " + triples[i] + ".");
      }
    }
  }

  /**
   * Tests iterative removal.
   *
   * @throws Exception A generic exception - this should cause the tests to
   *   fail.
   */
  public void testIterativeRemoval() throws Exception {
    // add some test data
    graph.add(blank1, ref1, blank2);
    graph.add(blank1, ref2, blank2);
    graph.add(ref1, ref2, l2);
    Triple t1 = elementFactory.createTriple(blank2, ref1, blank1);
    graph.add(t1);
    Triple t2 = elementFactory.createTriple(blank2, ref2, blank1);
    graph.add(t2);
    Triple t3 = elementFactory.createTriple(blank2, ref1, l1);
    graph.add(t3);

    // check that all is well
    assertFalse(graph.isEmpty());
    assertEquals(6, graph.getNumberOfTriples());

    // get an iterator for the blank2,ref1 elements
    ClosableIterator<Triple> ci = graph.find(blank2, ref1, null);
    checkInvalidRemove(ci);

    // remove the first element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(5, graph.getNumberOfTriples());

    // remove the second element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(4, graph.getNumberOfTriples());

    assertFalse(ci.hasNext());

    // get an iterator for the blank1 elements
    ci = graph.find(blank1, null, null);
    checkInvalidRemove(ci);

    // remove the first element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(3, graph.getNumberOfTriples());

    // remove the second element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(2, graph.getNumberOfTriples());

    assertFalse(ci.hasNext());

    // get an iterator for the ref1, ref2, l2 element
    ci = graph.find(ref1, ref2, l2);
    checkInvalidRemove(ci);

    // remove the element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(1, graph.getNumberOfTriples());

    assertFalse(ci.hasNext());

    // get an iterator for the final element
    ci = graph.find(null, null, null);
    checkInvalidRemove(ci);

    // remove the element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(0, graph.getNumberOfTriples());
    assertTrue(graph.isEmpty());

    assertFalse(ci.hasNext());
    ci.close();

    // check that we can't still remove things
    try {
      graph.remove(ref2, ref2, ref2);
      assertTrue(false);
    }
    catch (GraphException e) { /* no-op */}

  }

  private void checkInvalidRemove(ClosableIterator<Triple> ci) {
    try {
      ci.remove();
      fail("Must throw an exception.");
    }
    catch (IllegalStateException ise) {
      assertTrue(ise.getMessage().indexOf("Next not called or beyond end of data") != -1);
    }
  }

  /**
   * Tests full iterative removal.
   *
   * @throws Exception A generic exception - this should cause the tests to
   *   fail.
   */
  public void testFullIterativeRemoval() throws Exception {
    // add some test data
    graph.add(blank1, ref1, blank2);
    graph.add(blank1, ref2, blank2);
    graph.add(ref1, ref2, l2);
    Triple t1 = elementFactory.createTriple(blank2, ref1, blank1);
    graph.add(t1);
    Triple t2 = elementFactory.createTriple(blank2, ref2, blank1);
    graph.add(t2);
    Triple t3 = elementFactory.createTriple(blank2, ref1, l1);
    graph.add(t3);

    // check that all is well
    assertFalse(graph.isEmpty());
    assertEquals(6, graph.getNumberOfTriples());

    // get an iterator for all the elements
    ClosableIterator<Triple> ci = graph.find(null, null, null);
    for (int i = 5; 0 <= i; i--) {
      // remove the element
      assertTrue(ci.hasNext());
      ci.next();
      ci.remove();
      assertEquals(i, graph.getNumberOfTriples());
    }

    assertTrue(graph.isEmpty());

    assertFalse(ci.hasNext());

    ci.close();
  }

}
