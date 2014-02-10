/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.store.statement;

// third party packages
import java.util.Arrays;

import junit.framework.*;
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.Variable;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.tuples.MemoryTuples;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.store.xa.XAStatementStore;

/**
 * Test case for {@link StatementStore} implementations.
 *
 * @created 2001-07-12
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:52 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class StatementStoreAbstractUnitTest extends TestCase {

  /**
   * init the logging class
   */
  private static Logger log =
      Logger.getLogger(StatementStoreAbstractUnitTest.class.getName());

  static final protected long SYSTEM_GRAPH = 100;
  static final protected long RDF_TYPE = 101;
  static final protected long GRAPH_TYPE = 102;

  /**
   * Subclasses must initialize this field.
   */
  protected XAStatementStore store;

  /**
   * CONSTRUCTOR GraphAbstractTest TO DO
   *
   * @param name PARAMETER TO DO
   */
  public StatementStoreAbstractUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain an empty test suite from, because this test
   * can't be run (it's abstract). This must be overridden in subclasses.
   *
   * @return The test suite
   */
  public static Test suite() {

    return new TestSuite();
  }

  //
  // Test cases
  //

  /**
   * Test {@link StatementStore#isEmpty}.
   */
  public void testIsEmpty() throws Exception {

    try {

      assertTrue(!store.isEmpty());

      store.addTriple(1, 2, 3, 9);
      store.removeTriples(1, 2, 3, 1);
      store.removeTriples(1, 2, 4, 2);
      store.removeTriples(2, 5, 6, 2);
      store.removeTriples(SYSTEM_GRAPH, RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH);
      store.removeTriples(1, RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH);
      store.removeTriples(2, RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH);

      assertTrue(!store.isEmpty());

      store.removeTriples(1, 2, 3, 9);

      assertTrue(store.isEmpty());
    } catch (UnsupportedOperationException e) {
      log.warn("IsEmpty method unsupported", e);
    }
  }

  /**
   * Test {@link StatementStore#existsTriples}.
   */
  public void testExists() throws Exception {

    try {

      long n = NodePool.NONE;

      assertTrue(store.existsTriples(n, 2, 3, 1));
      assertTrue(store.existsTriples(n, 2, 4, 2));
      assertTrue(store.existsTriples(n, 5, 6, 2));
      assertTrue(!store.existsTriples(n, 3, 2, 1));
      assertTrue(!store.existsTriples(n, 9, 9, 9));

      assertTrue(store.existsTriples(1, n, 3, 1));
      assertTrue(store.existsTriples(2, n, 6, 2));
      assertTrue(!store.existsTriples(2, n, 4, 2));
      assertTrue(!store.existsTriples(9, n, 3, 1));

      assertTrue(store.existsTriples(n, n, 3, 1));
      assertTrue(store.existsTriples(n, n, 4, 2));
      assertTrue(!store.existsTriples(n, n, 3, 2));
      assertTrue(!store.existsTriples(n, n, 9, 2));

      assertTrue(store.existsTriples(1, 2, n, 1));
      assertTrue(store.existsTriples(2, 5, n, 2));
      assertTrue(!store.existsTriples(1, 3, n, 1));
      assertTrue(!store.existsTriples(2, 9, n, 2));

      assertTrue(store.existsTriples(n, 2, n, 1));
      assertTrue(store.existsTriples(n, 5, n, 2));
      assertTrue(!store.existsTriples(n, 3, n, 1));
      assertTrue(!store.existsTriples(n, 9, n, 2));

      assertTrue(store.existsTriples(1, n, n, 1));
      assertTrue(store.existsTriples(1, n, n, 2));
      assertTrue(store.existsTriples(2, n, n, 2));
      assertTrue(!store.existsTriples(3, n, n, 2));

      assertTrue(store.existsTriples(n, n, n, 1));
      assertTrue(store.existsTriples(n, n, n, 2));
      assertTrue(!store.existsTriples(n, n, n, 3));
    } catch (UnsupportedOperationException e) {
      log.warn("Exists method unsupported", e);
    }
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testDump() throws Exception {

    MemoryTuples expected = getDump();

    Tuples t = store.findTuples(NodePool.NONE, NodePool.NONE, NodePool.NONE, NodePool.NONE);
    Tuples r = TuplesOperations.project(t, Arrays.asList(StatementStore.VARIABLES), true);
    assertEquals(expected, r);
    t.close();
    r.close();
    expected.close();
  }

  /**
   * Test {@link StatementStore#removeTriples}.
   *
   * @throws GraphException on attempted removal of nonexistent triple
   * @throws Exception EXCEPTION TO DO
   */
  public void testRemoveTriples() throws Exception {

    store.removeTriples(1, 2, 3, 1);
    store.removeTriples(2, 5, 6, 7); // Non-existent triple

    assertTrue(!store.existsTriples(1, 2, 3, 1));
    assertTrue(store.existsTriples(1, 2, 4, 2));
    assertTrue(store.existsTriples(2, 5, 6, 2));

    store.removeTriples(1, 3, 2, 4); // Non-existent triple

    store.removeTriples(2, 5, 6, 2);
    store.removeTriples(1, 2, 4, 2);

    assertTrue(!store.existsTriples(NodePool.NONE, 2, 3, 1));
    assertTrue(!store.existsTriples(NodePool.NONE, 2, 4, 2));
    assertTrue(!store.existsTriples(NodePool.NONE, 5, 6, 2));
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testFindTriplesByNode0() throws Exception {

    MemoryTuples expected = new MemoryTuples();
    Variable[] vars =
        new Variable[] {StatementStore.VARIABLES[1], StatementStore.VARIABLES[2], StatementStore.VARIABLES[3]};
    add(expected, vars, new long[] {2, 3, 1});
    add(expected, vars, new long[] {2, 4, 2});
    add(expected, vars, new long[] {RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH});

    try {
      Tuples t = store.findTuples(1, NodePool.NONE, NodePool.NONE, NodePool.NONE);
      assertEquals(expected, t);
      t.close();
    } catch (IllegalArgumentException e) {
      // not supported
      return;
    } finally {
      expected.close();
    }

    expected = new MemoryTuples();
    add(expected, vars, new long[] {5, 6, 2});

    Tuples t = store.findTuples(2, NodePool.NONE, NodePool.NONE, NodePool.NONE);
    add(expected, vars, new long[] {RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH});
    assertEquals(expected, t);
    t.close();
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFindTriplesByNode1() throws Exception {

    MemoryTuples expected = new MemoryTuples();
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[2], StatementStore.VARIABLES[0], StatementStore.VARIABLES[3]};
    add(expected, vars, new long[] {3, 1, 1});
    add(expected, vars, new long[] {4, 1, 2});

    try {
      Tuples t = store.findTuples(NodePool.NONE, 2, NodePool.NONE, NodePool.NONE);
      assertEquals(expected, t);
      t.close();
    } catch (IllegalArgumentException e) {
      // not supported
    }
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFindTriplesByNode2() throws Exception {

    MemoryTuples expected = new MemoryTuples();
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[0], StatementStore.VARIABLES[1], StatementStore.VARIABLES[3]};
    add(expected, vars, new long[] {1, 2, 1});

    try {
      Tuples t = store.findTuples(NodePool.NONE, NodePool.NONE, 3, NodePool.NONE);
      assertEquals(expected, t);
      t.close();
    } catch (IllegalArgumentException e) {
      // not supported
    }
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFindTriplesByNode3() throws Exception {

    MemoryTuples expected = new MemoryTuples();
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[0], StatementStore.VARIABLES[1], StatementStore.VARIABLES[2]};
    add(expected, vars, new long[] {1, 2, 4});
    add(expected, vars, new long[] {2, 5, 6});

    Tuples t = store.findTuples(NodePool.NONE, NodePool.NONE, NodePool.NONE, 2);
    assertEquals(expected, t);
    t.close();
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testFindTriplesByNode01() throws Exception {

    MemoryTuples expected = new MemoryTuples();
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[2], StatementStore.VARIABLES[3]};
    add(expected, vars, new long[] {3, 1});
    add(expected, vars, new long[] {4, 2});

    try {
      Tuples t = store.findTuples(1, 2, NodePool.NONE, NodePool.NONE);
      assertEquals(expected, t);
      t.close();
  
      t = store.findTuples(1, 3, NodePool.NONE, NodePool.NONE);
      assertTrue(!expected.equals(t));
      t.close();
    } catch (IllegalArgumentException e) {
      // not supported
    }
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testFindTriplesByNode02() throws Exception {

    MemoryTuples expected = new MemoryTuples();
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[1], StatementStore.VARIABLES[3]};
    add(expected, vars, new long[] {2, 1});

    try {
      Tuples t = store.findTuples(1, NodePool.NONE, 3, NodePool.NONE);
      assertEquals(expected, t);
      t.close();
  
      t = store.findTuples(1, NodePool.NONE, 4, NodePool.NONE);
      assertTrue(!expected.equals(t));
      t.close();
    } catch (IllegalArgumentException e) {
      // not supported
    }
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testFindTriplesByNode03() throws Exception {

    MemoryTuples expected = new MemoryTuples();
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[1], StatementStore.VARIABLES[2]};
    add(expected, vars, new long[] {2, 4});

    Tuples t = store.findTuples(1, NodePool.NONE, NodePool.NONE, 2);
    assertEquals(expected, t);
    t.close();
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testFindTriplesByNode12() throws Exception {

    MemoryTuples expected = new MemoryTuples();
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[0], StatementStore.VARIABLES[3]};
    add(expected, vars, new long[] {1, 1});

    try {
      Tuples t = store.findTuples(NodePool.NONE, 2, 3, NodePool.NONE);
      assertEquals(expected, t);
      t.close();
  
      t = store.findTuples(NodePool.NONE, 2, 4, NodePool.NONE);
      assertTrue(!expected.equals(t));
      t.close();
    } catch (IllegalArgumentException e) {
      // not supported
    }
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testFindTriplesByNode13() throws Exception {

    MemoryTuples expected = new MemoryTuples();
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[2], StatementStore.VARIABLES[0]};
    add(expected, vars, new long[] {4, 1});

    Tuples t = store.findTuples(NodePool.NONE, 2, NodePool.NONE, 2);
    assertEquals(expected, t);
    t.close();
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testFindTriplesByNode23() throws Exception {

    MemoryTuples expected = new MemoryTuples();
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[0], StatementStore.VARIABLES[1]};
    add(expected, vars, new long[] {2, 5});

    Tuples t = store.findTuples(NodePool.NONE, NodePool.NONE, 6, 2);
    assertEquals(expected, t);
    t.close();
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testFindTriplesByNode013() throws Exception {

    MemoryTuples expected = new MemoryTuples(StatementStore.VARIABLES[2]);
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[2]};
    Tuples t = store.findTuples(2, 6, NodePool.NONE, 1);
    assertEquals(expected, t);
    t.close();

    t = store.findTuples(1, 2, NodePool.NONE, 4);
    assertEquals(expected, t);
    t.close();

    add(expected, vars, new long[] {4});
    t = store.findTuples(1, 2, NodePool.NONE, 2);
    assertEquals(expected, t);
    t.close();
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testFindTriplesByNode023() throws Exception {

    MemoryTuples expected = new MemoryTuples(StatementStore.VARIABLES[1]);
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[1]};
    Tuples t = store.findTuples(1, NodePool.NONE, 3, 4);
    assertEquals(expected, t);
    t.close();

    add(expected, vars, new long[] {5});
    t = store.findTuples(2, NodePool.NONE, 6, 2);
    assertEquals(expected, t);
    t.close();
    expected.close();
  }

  /**
   * Test {@link StatementStore#findTuples}.
   */
  public void testFindTriplesByNode123() throws Exception {

    MemoryTuples expected = new MemoryTuples(StatementStore.VARIABLES[0]);
    Variable[] vars = new Variable[] {StatementStore.VARIABLES[0]};
    Tuples t = store.findTuples(NodePool.NONE, 2, 3, 4);
    assertEquals(expected, t);
    t.close();

    add(expected, vars, new long[] {1});
    t = store.findTuples(NodePool.NONE, 2, 3, 1);
    assertEquals(expected, t);
    t.close();
    expected.close();
  }

  /**
   * Populate the test store.
   */
  protected void setUp() throws Exception {
    store.initializeSystemNodes(SYSTEM_GRAPH, RDF_TYPE, GRAPH_TYPE);

    // set up the graph instances
    store.addTriple(SYSTEM_GRAPH, RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH);
    store.addTriple(1, RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH);
    store.addTriple(2, RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH);

    store.addTriple(1, 2, 3, 1);
    store.addTriple(1, 2, 4, 2);
    store.addTriple(2, 5, 6, 2);
  }

  /**
   * Return a dump of all tuples, sorted by the primary index.
   * @return a new MemoryTuples containing all the {@link #setUp()} triples, according to the natural ordering of the store.
   */
  protected abstract MemoryTuples getDump();

  /**
   * Close the test store.
   */
  protected void tearDown() throws Exception {
    if (store != null) {
      try {
        store.close();
      } finally {
        store = null;
      }
    }
  }

  /**
   * Add a row to a tuples
   * @param tt The tuples to add to
   * @param vars The column names in the tuples
   * @param nodes The values to bind to
   */
  protected void add(MemoryTuples tt, Variable[] vars, long[] nodes) {

    if (vars.length != nodes.length) throw new AssertionError();

    for (int i = 0; i < vars.length; ++i) {
      if (i == 0) {
        tt.or(vars[i], nodes[i]);
      } else {
        tt.and(vars[i], nodes[i]);
      }
    }
  }
}
