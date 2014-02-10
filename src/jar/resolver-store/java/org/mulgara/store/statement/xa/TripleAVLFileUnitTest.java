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

import java.io.*;

// Third party packages
import junit.framework.*;
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.xa.Block;
import org.mulgara.util.Constants;
import org.mulgara.util.TempDir;

/**
 * Test cases for TripleAVLFile.
 *
 * @created 2001-10-17
 *
 * @author David Makepeace
 * @author Paul Gearon
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:55 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class TripleAVLFileUnitTest extends TestCase {

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(TripleAVLFileUnitTest.class);

  /**
   * Description of the Field
   */
  private static Block metaroot = Block.newInstance(TripleAVLFile.Phase.RECORD_SIZE * Constants.SIZEOF_LONG);

  /**
   * Description of the Field
   */
  private TripleAVLFile tripleAVLFile;

  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public TripleAVLFileUnitTest(String name) {
    super(name);
  }


  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new TripleAVLFileUnitTest("testInsert"));
    suite.addTest(new TripleAVLFileUnitTest("testContains"));
    suite.addTest(new TripleAVLFileUnitTest("testRemove"));
    suite.addTest(new TripleAVLFileUnitTest("testReinsert"));
    suite.addTest(new TripleAVLFileUnitTest("testPersist"));
    suite.addTest(new TripleAVLFileUnitTest("testMultiphase"));
    suite.addTest(new TripleAVLFileUnitTest("testFindTuples"));
    suite.addTest(new TripleAVLFileUnitTest("testTuplesContent"));
    suite.addTest(new TripleAVLFileUnitTest("testLargeTuplesContent"));
    suite.addTest(new TripleAVLFileUnitTest("testPrefix"));
    return suite;
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
   * Creates a new file required to do the testing.
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void setUp() throws IOException {
    boolean exceptionOccurred = true;
    try {
      tripleAVLFile = new TripleAVLFile(
          new File(TempDir.getTempDir(), "tavlftest"),
          new int[] {0, 1, 2, 3}
      );
      exceptionOccurred = false;
    }
    finally {
      if (exceptionOccurred) {
        tearDown();
      }
    }
  }

  /**
   * Closes the file used for testing.
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void tearDown() throws IOException {
    if (tripleAVLFile != null) {
      try {
        tripleAVLFile.close();
      } finally {
        tripleAVLFile = null;
      }
    }
  }


  /**
   * Test insert
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testInsert() throws Exception {
    TripleAVLFile.Phase phase0 = tripleAVLFile.new Phase();
    tripleAVLFile.clear();
    TripleAVLFile.Phase.Token token0 = phase0.use();
    TripleAVLFile.Phase phase1 = tripleAVLFile.new Phase();

    phase1.addTriple(1, 2, 3, 1);
    phase1.addTriple(1, 2, 4, 2);
    phase1.addTriple(2, 5, 6, 3);
    phase1.addTriple(3, 2, 3, 4);

    tripleAVLFile.new Phase(phase0);
    token0.release();
  }


  /**
   * Test contains
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testContains() throws Exception {
    TripleAVLFile.Phase phase0 = tripleAVLFile.new Phase();
    tripleAVLFile.clear();
    TripleAVLFile.Phase.Token token0 = phase0.use();
    TripleAVLFile.Phase phase1 = tripleAVLFile.new Phase();

    assertTrue("Found node in empty tree", !phase1.existsTriple(1, 2, 4, 2));

    phase1.addTriple(1, 2, 3, 1);
    phase1.addTriple(1, 2, 4, 2);
    phase1.addTriple(2, 5, 6, 3);

    assertTrue("Node missing from tree", phase1.existsTriple(1, 2, 4, 2));

    tripleAVLFile.new Phase(phase0);
    token0.release();
  }


  /**
   * Test remove
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testRemove() throws Exception {
    TripleAVLFile.Phase phase0 = tripleAVLFile.new Phase();
    tripleAVLFile.clear();
    TripleAVLFile.Phase.Token token0 = phase0.use();
    TripleAVLFile.Phase phase1 = tripleAVLFile.new Phase();

    phase1.addTriple(1, 2, 3, 1);
    phase1.addTriple(1, 2, 4, 2);
    phase1.addTriple(2, 5, 6, 3);
    phase1.addTriple(3, 2, 3, 4);

    assertTrue(phase1.existsTriple(1, 2, 3, 1));
    assertTrue(phase1.existsTriple(1, 2, 4, 2));
    assertTrue(phase1.existsTriple(2, 5, 6, 3));
    assertTrue(phase1.existsTriple(3, 2, 3, 4));

    phase1.removeTriple(1, 2, 3, 1);
    assertTrue(!phase1.existsTriple(1, 2, 3, 1));
    assertTrue(phase1.existsTriple(1, 2, 4, 2));
    assertTrue(phase1.existsTriple(2, 5, 6, 3));
    assertTrue(phase1.existsTriple(3, 2, 3, 4));

    phase1.removeTriple(1, 2, 4, 2);
    assertTrue(!phase1.existsTriple(1, 2, 3, 1));
    assertTrue(!phase1.existsTriple(1, 2, 4, 2));
    assertTrue(phase1.existsTriple(2, 5, 6, 3));
    assertTrue(phase1.existsTriple(3, 2, 3, 4));

    phase1.removeTriple(2, 5, 6, 3);
    assertTrue(!phase1.existsTriple(1, 2, 3, 1));
    assertTrue(!phase1.existsTriple(1, 2, 4, 2));
    assertTrue(!phase1.existsTriple(2, 5, 6, 3));
    assertTrue(phase1.existsTriple(3, 2, 3, 4));

    phase1.removeTriple(3, 2, 3, 4);
    assertTrue(!phase1.existsTriple(1, 2, 3, 1));
    assertTrue(!phase1.existsTriple(1, 2, 4, 2));
    assertTrue(!phase1.existsTriple(2, 5, 6, 3));
    assertTrue(!phase1.existsTriple(3, 2, 3, 4));

    tripleAVLFile.new Phase(phase0);
    token0.release();
  }


  /**
   * Test reinsert
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testReinsert() throws Exception {
    TripleAVLFile.Phase phase0 = tripleAVLFile.new Phase();
    tripleAVLFile.clear();
    TripleAVLFile.Phase.Token token0 = phase0.use();
    TripleAVLFile.Phase phase1 = tripleAVLFile.new Phase();

    phase1.addTriple(1, 2, 3, 1);
    phase1.addTriple(1, 2, 4, 2);
    phase1.addTriple(2, 5, 6, 3);
    phase1.addTriple(3, 2, 3, 4);

    assertTrue(phase1.existsTriple(1, 2, 3, 1));
    assertTrue(phase1.existsTriple(1, 2, 4, 2));
    assertTrue(phase1.existsTriple(2, 5, 6, 3));
    assertTrue(phase1.existsTriple(3, 2, 3, 4));

    phase1.removeTriple(1, 2, 3, 1);
    assertTrue(!phase1.existsTriple(1, 2, 3, 1));
    assertTrue(phase1.existsTriple(1, 2, 4, 2));
    assertTrue(phase1.existsTriple(2, 5, 6, 3));
    assertTrue(phase1.existsTriple(3, 2, 3, 4));

    phase1.addTriple(1, 2, 3, 1);
    assertTrue(phase1.existsTriple(1, 2, 3, 1));
    assertTrue(phase1.existsTriple(1, 2, 4, 2));
    assertTrue(phase1.existsTriple(2, 5, 6, 3));
    assertTrue(phase1.existsTriple(3, 2, 3, 4));

    // set up for testPersist()
    phase1.writeToBlock(metaroot, 0);

    token0.release();
  }


  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testPersist() throws Exception {
    TripleAVLFile.Phase phase0 = tripleAVLFile.new Phase(metaroot, 0);
    TripleAVLFile.Phase.Token token0 = phase0.use();
    TripleAVLFile.Phase phase1 = tripleAVLFile.new Phase();

    assertTrue(phase1.existsTriple(1, 2, 3, 1));
    assertTrue(phase1.existsTriple(1, 2, 4, 2));
    assertTrue(phase1.existsTriple(2, 5, 6, 3));
    assertTrue(phase1.existsTriple(3, 2, 3, 4));

    tripleAVLFile.new Phase(phase0);
    token0.release();
  }


  /**
   * Test multiphasic modifications
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testMultiphase() throws Exception {
    TripleAVLFile.Phase phase0 = tripleAVLFile.new Phase();
    tripleAVLFile.clear();
    TripleAVLFile.Phase.Token token0 = phase0.use();
    TripleAVLFile.Phase phase1 = tripleAVLFile.new Phase();

    phase1.addTriple(1, 2, 3, 1);
    phase1.addTriple(1, 2, 4, 2);
    phase1.addTriple(2, 5, 6, 3);
    phase1.addTriple(3, 2, 3, 4);

    assertTrue(phase1.existsTriple(1, 2, 3, 1));
    assertTrue(phase1.existsTriple(1, 2, 4, 2));
    assertTrue(phase1.existsTriple(2, 5, 6, 3));
    assertTrue(phase1.existsTriple(3, 2, 3, 4));

    TripleAVLFile.Phase.Token token1 = phase1.use();
    TripleAVLFile.Phase phase2 = tripleAVLFile.new Phase();

    phase2.removeTriple(1, 2, 3, 1);
    assertTrue(!phase2.existsTriple(1, 2, 3, 1));
    assertTrue(phase2.existsTriple(1, 2, 4, 2));
    assertTrue(phase2.existsTriple(2, 5, 6, 3));
    assertTrue(phase2.existsTriple(3, 2, 3, 4));

    assertTrue(phase1.existsTriple(1, 2, 3, 1));
    assertTrue(phase1.existsTriple(1, 2, 4, 2));
    assertTrue(phase1.existsTriple(2, 5, 6, 3));
    assertTrue(phase1.existsTriple(3, 2, 3, 4));

    phase2.addTriple(1, 2, 3, 1);
    assertTrue(phase2.existsTriple(1, 2, 3, 1));
    assertTrue(phase2.existsTriple(1, 2, 4, 2));
    assertTrue(phase2.existsTriple(2, 5, 6, 3));
    assertTrue(phase2.existsTriple(3, 2, 3, 4));

    try {
      phase1.removeTriple(1, 2, 3, 1);
      fail("Able to remove from a read-only phase");
    } catch (IllegalStateException ex) {
      // NO-OP
    }

    try {
      phase1.addTriple(4, 2, 3, 6);
      fail("Able to insert into a read-only phase");
    } catch (IllegalStateException ex) {
      // NO-OP
    }

    token1.release();
    tripleAVLFile.new Phase(phase0);
    token0.release();
  }


  /**
   * Test finding Tuples
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFindTuples() throws Exception {
    TripleAVLFile.Phase phase0 = tripleAVLFile.new Phase();
    tripleAVLFile.clear();
    TripleAVLFile.Phase.Token token0 = phase0.use();
    TripleAVLFile.Phase phase1 = tripleAVLFile.new Phase();

    phase1.addTriple(1, 2, 3, 1);
    phase1.addTriple(1, 2, 4, 2);
    phase1.addTriple(2, 5, 6, 3);
    phase1.addTriple(3, 2, 3, 4);

    Tuples tuples = phase1.findTuples(1);
    tuples.close();

    tripleAVLFile.new Phase(phase0);
    token0.release();
  }


  /**
   * Test tuples content and iteration
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testTuplesContent() throws Exception {
    TripleAVLFile.Phase phase0 = tripleAVLFile.new Phase();
    tripleAVLFile.clear();
    TripleAVLFile.Phase.Token token0 = phase0.use();
    TripleAVLFile.Phase phase1 = tripleAVLFile.new Phase();

    phase1.addTriple(1, 2, 3, 1);
    phase1.addTriple(1, 2, 4, 2);
    phase1.addTriple(2, 5, 6, 3);
    phase1.addTriple(3, 2, 3, 4);

    Tuples tuples = phase1.findTuples(1);

    // move to the first triple
    tuples.beforeFirst();
    assertTrue(tuples.next());

    // check that this tuple is <1, 2, 3, 1>
    assertEquals(2, tuples.getColumnValue(0));
    assertEquals(3, tuples.getColumnValue(1));
    assertEquals(1, tuples.getColumnValue(2));

    // move to the second triple
    assertTrue(tuples.next());

    // check that this tuple is <1, 2, 4, 2>
    assertEquals(2, tuples.getColumnValue(0));
    assertEquals(4, tuples.getColumnValue(1));
    assertEquals(2, tuples.getColumnValue(2));

    // move to the end
    assertTrue(!tuples.next());

    tuples.close();

    tripleAVLFile.new Phase(phase0);
    token0.release();
  }


  /**
   * Test tuples content and iteration
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testLargeTuplesContent() throws Exception {
    TripleAVLFile.Phase phase0 = tripleAVLFile.new Phase();
    tripleAVLFile.clear();
    TripleAVLFile.Phase.Token token0 = phase0.use();
    TripleAVLFile.Phase phase1 = tripleAVLFile.new Phase();

    for (int i = 1; i < 5; i++) {
      for (int j = 1; j <= 500; j++) {
        phase1.addTriple(i, j, 7, 6);
      }
    }

    Tuples tuples = phase1.findTuples(3);

    tuples.beforeFirst();
    for (int j = 1; j <= 500; j++) {
      assertTrue(tuples.next());
      assertEquals(j, tuples.getColumnValue(0));
      assertEquals(7, tuples.getColumnValue(1));
      assertEquals(6, tuples.getColumnValue(2));
    }

    // move to the end
    assertTrue(!tuples.next());

    tuples.close();

    tripleAVLFile.new Phase(phase0);
    token0.release();
  }


  /**
   * Test tuples slice content
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testPrefix() throws Exception {
    TripleAVLFile.Phase phase0 = tripleAVLFile.new Phase();
    tripleAVLFile.clear();
    TripleAVLFile.Phase.Token token0 = phase0.use();
    TripleAVLFile.Phase phase1 = tripleAVLFile.new Phase();

    phase1.addTriple(1, 2, 3, 1);
    phase1.addTriple(1, 2, 4, 2);
    phase1.addTriple(2, 5, 6, 3);
    phase1.addTriple(2, 6, 7, 3);
    phase1.addTriple(2, 6, 8, 3);
    phase1.addTriple(2, 7, 9, 3);
    phase1.addTriple(3, 2, 3, 4);

    Tuples tuples = phase1.findTuples(2);

    // move to the first triple
    tuples.beforeFirst(new long[]{6}, 0);
    assertTrue(tuples.next());

    // check that this tuple is <2, 6, 7, 3>
    assertEquals(6, tuples.getColumnValue(0));
    assertEquals(7, tuples.getColumnValue(1));
    assertEquals(3, tuples.getColumnValue(2));

    // move to the second triple
    assertTrue(tuples.next());

    // check that this tuple is <2, 6, 8, 3>
    assertEquals(6, tuples.getColumnValue(0));
    assertEquals(8, tuples.getColumnValue(1));
    assertEquals(3, tuples.getColumnValue(2));

    // move to the end
    assertTrue(!tuples.next());

    tuples.close();

    tripleAVLFile.new Phase(phase0);
    token0.release();
  }

}

