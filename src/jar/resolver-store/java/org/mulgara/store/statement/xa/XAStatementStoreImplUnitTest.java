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
package org.mulgara.store.statement.xa;

// Java 2 standard packages
import java.io.*;
import java.util.*;

// JUnit
import junit.framework.*;

// log4j
import org.apache.log4j.*;

// locally written packages
import org.mulgara.store.nodepool.*;
import org.mulgara.store.statement.*;
import org.mulgara.store.tuples.MemoryTuples;
import org.mulgara.util.*;


/**
 * Test case for {@link XAStatementStoreImpl}.
 *
 * @author <a href="http://staff.pisoftware.com/david">David Makepeace</a>
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/02/22 08:16:34 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001-2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class XAStatementStoreImplUnitTest
    extends StatementStoreAbstractUnitTest {

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(XAStatementStoreImplUnitTest.class);

  /**
   * start of filenames to build the graph with.
   */
  private final static String DBFILENAME = "graphtest";

  /**
   * Description of the Field
   */
  private XAStatementStoreImpl xaStore;

  /**
   * Description of the Field
   */
  private Set<Long> releasedNodes = new HashSet<Long>();


  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public XAStatementStoreImplUnitTest(String name) {
    super(name);
  }


  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {
    return new TestSuite(XAStatementStoreImplUnitTest.class);
    //TestSuite suite = new TestSuite();
    //suite.addTest(new GraphImplTest("testRemoveTriples"));
    //suite.addTest(new GraphImplTest("testFindTriplesByNode0"));
    //suite.addTest(new GraphImplTest("testFindTriplesByNode1"));
    //suite.addTest(new GraphImplTest("testFindTriplesByNode2"));
    //suite.addTest(new GraphImplTest("testFindTriplesByNode01"));
    //suite.addTest(new GraphImplTest("testFindTriplesByNode02"));
    //suite.addTest(new GraphImplTest("testFindTriplesByNode12"));
    //return suite;
  }


  /**
   * Default test runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }


  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testListeners() throws Exception {
    if (!checkIfListenersEnabled()) return;

    xaStore.addReleaseNodeListener(
      new ReleaseNodeListener() {
        public void releaseNode(long node) {
          releasedNodes.add(new Long(node));
        }
      });

    // Hold on to node 6.
    store.addTriple(6, 7, 8, 2);

    releasedNodes.clear();
    store.removeTriples(1, 2, 3, 1);
    assertEquals(Collections.singleton(new Long(3)), releasedNodes);

    releasedNodes.clear();
    store.removeTriples(2, 5, 6, 2);
    assertEquals(Collections.singleton(new Long(5)), releasedNodes);
  }


  /**
   * This access is hidden in a method to convince the compiler to look at it.
   * @return <code>true</code> when listeners are enabled at build time.
   */
  private static boolean checkIfListenersEnabled() {
    return XAStatementStoreImpl.RELEASE_NODE_LISTENERS_ENABLED;
  }


  /**
   * A method to call for each graph before running tests on it.
   *
   * @throws Exception EXCEPTION TO DO
   */
  protected void setUp() throws Exception {
    boolean exceptionOccurred = true;
    try {
      // create the graph object, using a new file
      store = new XAStatementStoreImpl(
          TempDir.getTempDir().getPath() + File.separatorChar + DBFILENAME
      );
      xaStore = (XAStatementStoreImpl) store;
      xaStore.clear();
      super.setUp();
      exceptionOccurred = false;
    }
    finally {
      if (exceptionOccurred) {
        tearDown();
      }
    }
  }


  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  protected void tearDown() throws Exception {
    if (xaStore != null) {
      xaStore.unmap();
      if (System.getProperty("os.name").startsWith("Win")) {
        // Need this for Windows or truncate() always fails for mapped files.
        System.gc();
        System.runFinalization();
      }
    }
    super.tearDown();
  }

  
  /**
   * Return a dump of all tuples, sorted by the primary index: 0123.
   * @see org.mulgara.store.statement.StatementStoreAbstractUnitTest#getDump()
   */
  protected MemoryTuples getDump() {
    MemoryTuples expected = new MemoryTuples();
    add(expected, StatementStore.VARIABLES, new long[] {1, 2, 3, 1});
    add(expected, StatementStore.VARIABLES, new long[] {1, 2, 4, 2});
    add(expected, StatementStore.VARIABLES, new long[] {1, RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH});
    add(expected, StatementStore.VARIABLES, new long[] {2, 5, 6, 2});
    add(expected, StatementStore.VARIABLES, new long[] {2, RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH});
    add(expected, StatementStore.VARIABLES, new long[] {SYSTEM_GRAPH, RDF_TYPE, GRAPH_TYPE, SYSTEM_GRAPH});
    return expected;
  }

}
