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

package org.mulgara.store.bdb;

// Third party packages
import junit.framework.*;

import java.io.*;

// Java 2 standard packages
import java.util.*;

import org.apache.log4j.Logger;

// Locally written packages.
import org.mulgara.query.Variable;
import org.mulgara.store.tuples.DefaultRowComparator;
import org.mulgara.store.tuples.LiteralTuples;
import org.mulgara.store.tuples.RowComparator;

/**
 * Test cases for DbTuples.
 *
 * @created 2003-02-04
 *
 * @author Paula Gearon
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:12 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class DbTuplesTest extends TestCase {
  // Larger than this and in memory structures used by the tests fail with OOM
//  private final static int LOAD_SIZE = 262142;
  private final static int LOAD_SIZE = 1000000;
  private final static int ODD_LOAD_SIZE = 200000;
  private final static int BF_SMALL_LOAD_SIZE = 10;
  private final static int BF_LARGE_LOAD_SIZE = 100;
  private final static Logger logger = Logger.getLogger(DbTuplesTest.class);
  private final static int WIDTH = 3;
  private DbTuples dbTuples;
  private RowComparator rowComparator;

  private final static Comparator<long[]> longSingletonArrayComparator =  new Comparator<long[]>() {
        public int compare(long[] a1, long[] a2) {
          return a1[0] == a2[0] ? 0 : (a1[0] < a2[0] ? -1 : +1);
        }
      };

  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public DbTuplesTest(String name) {
    super(name);
  }


  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new DbTuplesTest("testCreate"));
    suite.addTest(new DbTuplesTest("testSort"));
    suite.addTest(new DbTuplesTest("testOddLoad"));
    suite.addTest(new DbTuplesTest("testLoadSort"));
    suite.addTest(new DbTuplesTest("testEmpty"));
    suite.addTest(new DbTuplesTest("testLargeOrdered"));
    suite.addTest(new DbTuplesTest("testCloneAndClose"));

    // determine why these do not work
    // suite.addTest(new DbTuplesTest("testPositioning"));
    // suite.addTest(new DbTuplesTest("testSmallBeforeFirst"));
    // suite.addTest(new DbTuplesTest("testLargeBeforeFirst"));

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
   * @throws Exception EXCEPTION TO DO
   */
  public void setUp() throws Exception {
    boolean exceptionOccurred = true;
    try {
      rowComparator = DefaultRowComparator.getInstance();
      exceptionOccurred = false;
    } finally {
      if (exceptionOccurred) {
        tearDown();
      }
    }
  }


  /**
   * Closes the file used for testing.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void tearDown() throws Exception {
    if (dbTuples != null) {
      dbTuples.close();
      dbTuples = null;
    }
  }


  /**
   * Test creation based on a 6 element set.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testCreate() throws Exception {
    LiteralTuples tt = new LiteralTuples(new String[] { "test" });
    tt.appendTuple(new long[] { 1 });
    tt.appendTuple(new long[] { 2 });
    tt.appendTuple(new long[] { 3 });
    tt.appendTuple(new long[] { 4 });
    tt.appendTuple(new long[] { 5 });
    tt.appendTuple(new long[] { 6 });

    dbTuples = new DbTuples(tt, rowComparator);

    dbTuples.beforeFirst();

    // check that the order is as expected
    long row = 1;
    while (dbTuples.next()) {
      assertEquals(row++, dbTuples.getColumnValue(0));
    }

    // check for the correct number of elements in dbTuples
    assertTrue(row == 7);
  }


  /**
   * Test sorting
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testSort() throws Exception {
    LiteralTuples tt = new LiteralTuples(new String[] { "test" });
    tt.appendTuple(new long[] { 6 });
    tt.appendTuple(new long[] { 5 });
    tt.appendTuple(new long[] { 4 });
    tt.appendTuple(new long[] { 3 });
    tt.appendTuple(new long[] { 2 });
    tt.appendTuple(new long[] { 1 });

    dbTuples = new DbTuples(tt, rowComparator);

    dbTuples.beforeFirst();
    // check that the order is as expected
    long row = 1;
    while (dbTuples.next()) {
      assertEquals(row++, dbTuples.getColumnValue(0));
    }

    // check for the correct number of elements in dbTuples
    assertTrue(row == 7);
  }


  /**
   * Test cloneing DbTuples and closing original copy.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testCloneAndClose() throws Exception {
    LiteralTuples tt = new LiteralTuples(new String[] { "test" });
    tt.appendTuple(new long[] { 6 });
    tt.appendTuple(new long[] { 5 });
    tt.appendTuple(new long[] { 4 });
    tt.appendTuple(new long[] { 3 });
    tt.appendTuple(new long[] { 2 });
    tt.appendTuple(new long[] { 1 });

    DbTuples origTuples = new DbTuples(tt, rowComparator);
    dbTuples = (DbTuples)origTuples.clone();
    origTuples.close();

    dbTuples.beforeFirst();
    // check that the order is as expected
    long row = 1;
    while (dbTuples.next()) {
      assertEquals(row++, dbTuples.getColumnValue(0));
    }

    // check for the correct number of elements in dbTuples
    assertTrue(row == 7);
  }


  /**
   * Load test the sorting
   *
   * @throws Throwable EXCEPTION TO DO
   */
  public void testLoadSort() throws Throwable {
    Random r = new Random(42);    // use a constant seed
    long[] v = new long[] { r.nextLong() & 0xFFFFFFFFL };
    LiteralTuples tt = new LiteralTuples(new String[] { "test" });
    tt.appendTuple(v);

    ArrayList<long[]> values = new ArrayList<long[]>();
    values.add(v);

    // iterate over a large enough range to guarantee several blocks
    for (int i = 0; i < LOAD_SIZE; i++) {
      v = new long[] { r.nextLong() & 0xFFFFFFFFL };
      if (v[0] == 0) {
        v[0]++;
      }
      values.add(v);
      tt.appendTuple(v);
    }
    Collections.sort(values, longSingletonArrayComparator);

//    logger.info("LoadSort: Creating DbTuples");
    dbTuples = new DbTuples(tt, rowComparator);
//    logger.info("LoadSort: BeforeFirst");
    dbTuples.beforeFirst();

//    logger.info("LoadSort: Checking Order");
    // check that the order is as expected
    Iterator<long[]> it = values.iterator();
    int i = 0;
    try {
      while (it.hasNext()) {
        assertEquals("On iteration " + i, dbTuples.next(), true);
        v = it.next();
        i++;
//        if (i % 10000 == 0) {
//          logger.info("Checked " + i + " tuples");
//        }
        assertEquals("On iteration " + i, v[0], dbTuples.getColumnValue(0));
      }
    } catch (Throwable e) {
      // for debugging:
      // dumpFile(dbTuples);
      throw e;
    }

//    logger.info("LoadSort: Finished order check");
    // check that there aren't any extraneous values
    assertTrue(!dbTuples.next());
//    logger.info("LoadSort: Finished test");
  }


  /**
   * Load test odd sized tuples
   *
   * @throws Throwable EXCEPTION TO DO
   */
  public void testOddLoad() throws Throwable {
    Variable[] vars = new Variable[WIDTH];

    for (int c = 0; c < WIDTH; c++) {
      vars[c] = new Variable("test" + (c + 1));
    }

    // use a constant seed
    Random r = new Random(42);

    LiteralTuples tt = new LiteralTuples(vars);
    long[] v = new long[WIDTH];

    // iterate over a large enough range to guarantee several blocks
    for (int i = 0; i < ODD_LOAD_SIZE; i++) {
      for (int c = 0; c < WIDTH; c++) {
        v[c] = r.nextLong() & 0x0000FFFFL;
        if (v[c] == 0) {
          v[c]++;
        }
      }

      tt.appendTuple(v);
    }

    dbTuples = new DbTuples(tt, rowComparator);
    dbTuples.beforeFirst();

    // check that the order is as expected
    long[] tuple = new long[WIDTH];
    long[] lastTuple = new long[WIDTH];

    for (int c = 0; c < WIDTH; c++) {
      lastTuple[c] = 0;
    }

    int i = 0;
    try {
      while (dbTuples.next()) {
        for (int c = 0; c < WIDTH; c++) {
          tuple[c] = dbTuples.getColumnValue(c);
        }

        for (int c = 0; c < WIDTH; c++) {
          if (tuple[c] < lastTuple[c]) {
            logger.error("Error tuple[" + tuple[0] + "," + tuple[1] + "," +
            tuple[2] + "] < lastTuple[" + lastTuple[0] + "," + lastTuple[1] +
            "," + lastTuple[2] + "]");
          }
          assertTrue(tuple[c] >= lastTuple[c]);
          if (tuple[c] != lastTuple[c]) {
            break;
          }
        }

        for (int c = 0; c < WIDTH; c++) {
          lastTuple[c] = tuple[c];
        }

        i++;
      }
    } catch (Throwable e) {
      // for debugging:
      // dumpFile(dbTuples);
      throw e;
    }

    // check that there aren't any extraneous values
//    logger.warn("i = " + i + ", LOAD_SIZE = " + ODD_LOAD_SIZE);
    assertTrue(i == ODD_LOAD_SIZE);
  }


  /**
   * Test adding an empty tuple
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testEmpty() throws Exception {
    LiteralTuples tt = new LiteralTuples(new Variable[] {});

    dbTuples = new DbTuples(tt, rowComparator);
    dbTuples.beforeFirst();

    // check that the order is as expected
    assertTrue(!dbTuples.next());
  }


  /**
   * Test the {@link DbTuples#beforeFirst(long[], int)} and {@link
   * DbTuples#next} methods.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testPositioning() throws Exception {
    LiteralTuples tt = new LiteralTuples(new String[] { "x", "y" });
    tt.appendTuple(new long[] { 1, 2 });
    tt.appendTuple(new long[] { 3, 4 });
    tt.appendTuple(new long[] { 5, 6 });
    tt.appendTuple(new long[] { 7, 8 });
    dbTuples = new DbTuples(tt, rowComparator);

    dbTuples.beforeFirst(new long[] {7, 8} , 0);
    assertTrue(dbTuples.next());
    assertEquals(7, dbTuples.getColumnValue(0));
    assertEquals(8, dbTuples.getColumnValue(1));
    assertTrue(!dbTuples.next());

    dbTuples.beforeFirst();

    assertTrue(dbTuples.next());
    assertEquals(1, dbTuples.getColumnValue(0));
    assertEquals(2, dbTuples.getColumnValue(1));

    dbTuples.beforeFirst(new long[] {7, 8} , 0);
    assertTrue(dbTuples.next());
    assertEquals(7, dbTuples.getColumnValue(0));
    assertEquals(8, dbTuples.getColumnValue(1));

    assertTrue(!dbTuples.next());

    dbTuples.beforeFirst(new long[] {1, 2} , 0);

    assertTrue(dbTuples.next());
    assertEquals(1, dbTuples.getColumnValue(0));
    assertEquals(2, dbTuples.getColumnValue(1));

    assertTrue(!dbTuples.next());

    dbTuples.beforeFirst(new long[] {2} , 0);
    assertTrue(!dbTuples.next());

    dbTuples.beforeFirst(new long[] {3} , 0);
    assertTrue(dbTuples.next());
    assertEquals(3, dbTuples.getColumnValue(0));
    assertEquals(4, dbTuples.getColumnValue(1));

    assertTrue(!dbTuples.next());

    dbTuples.close();
    dbTuples = null;
  }


  public void testLargeOrdered() throws Throwable {
    LiteralTuples tt = new LiteralTuples(new String[] { "test" });


    ArrayList<long[]> values = new ArrayList<long[]>();

    long count = 0;
    // iterate over a large enough range to guarantee several blocks
    for (int i = 0; i < LOAD_SIZE; i++) {
      long[] v = new long[] { count++ };
      v[0] = count++;
      values.add(v);
      tt.appendTuple(v);
    }
    Collections.sort(values, longSingletonArrayComparator);

    dbTuples = new DbTuples(tt, rowComparator);
    dbTuples.beforeFirst();

    // check that the order is as expected
    Iterator<long[]> it = values.iterator();
    int i = 0;
    try {
      while (it.hasNext()) {
        assertTrue(dbTuples.next());
        long[] v = it.next();
        i++;
        assertEquals("On iteration " + i, v[0], dbTuples.getColumnValue(0));
      }
    } catch (Throwable e) {
      // for debugging:
      // dumpFile(dbTuples);
      throw e;
    }

    // check that there aren't any extraneous values
    assertTrue(!dbTuples.next());
  }


  public void testSmallBeforeFirst() throws Throwable {
    logger.warn("testSmallBeforeFirst");
    LiteralTuples tt = new LiteralTuples(new String[] { "i", "j", "k" });

    logger.warn("testSmallBeforeFirst: checkpoint 1");
    // iterate over a large enough range to guarantee several blocks
    for (int i = (BF_SMALL_LOAD_SIZE * 2) - 1; i > 0; i -= 2) {
      for (int j = (BF_SMALL_LOAD_SIZE * 2) - 1; j > 0; j -= 2) {
        for (int k = (BF_SMALL_LOAD_SIZE * 2) - 1; k > 0; k -= 2) {
          long[] v = new long[] { i, j, k };
          tt.appendTuple(v);
        }
      }
    }

    logger.warn("testSmallBeforeFirst: checkpoint 2");
    dbTuples = new DbTuples(tt, rowComparator);

    logger.warn("testSmallBeforeFirst: checkpoint 3");
    dbTuples.beforeFirst();
    // check that the order is as expected
    logger.warn("testSmallBeforeFirst: checkpoint 4");
    try {
      long count = 0;
      for (int i = 1; i < BF_SMALL_LOAD_SIZE * 2; i += 2) {
        for (int j = 1; j < BF_SMALL_LOAD_SIZE * 2; j += 2) {
          for (int k = 1; k < BF_SMALL_LOAD_SIZE * 2; k += 2) {
            count++;
            assertTrue(dbTuples.next());
            assertEquals("On iteration " + count, i, dbTuples.getColumnValue(0));
            assertEquals("On iteration " + count, j, dbTuples.getColumnValue(1));
            assertEquals("On iteration " + count, k, dbTuples.getColumnValue(2));
          }
        }
      }

      // check that there aren't any extraneous values
      assertTrue(!dbTuples.next());
    } catch (Throwable e) {
      // for debugging:
      // dumpFile(dbTuples);
      throw e;
    }
    logger.warn("testSmallBeforeFirst: checkpoint 5");

    dbTuples.beforeFirst(new long[] { 3, 5 }, 0);
    logger.warn("testSmallBeforeFirst: checkpoint 6");
    // check that the order is as expected
    try {
      long count = 0;
      int i = 3;
      int j = 5;
      for (int k = 1; k < BF_SMALL_LOAD_SIZE * 2; k += 2) {
        count++;
        assertTrue(dbTuples.next());
        assertEquals("On iteration " + count, i, dbTuples.getColumnValue(0));
        assertEquals("On iteration " + count, j, dbTuples.getColumnValue(1));
        assertEquals("On iteration " + count, k, dbTuples.getColumnValue(2));
      }

      // check that there aren't any extraneous values
      assertTrue(!dbTuples.next());
    } catch (Throwable e) {
      // for debugging:
      // dumpFile(dbTuples);
      throw e;
    }
    logger.warn("testSmallBeforeFirst: checkpoint 7");

    dbTuples.beforeFirst(new long[] { 3, 4 }, 0);
    logger.warn("testSmallBeforeFirst: checkpoint 8");
    assertFalse(dbTuples.next());
  }


  public void testLargeBeforeFirst() throws Throwable {
    logger.info("testLargeBeforeFirst");

    LiteralTuples tt = new LiteralTuples(new String[] { "i", "j", "k" });

    // iterate over a large enough range to guarantee several blocks
    for (int i = (BF_LARGE_LOAD_SIZE * 2) - 1; i > 0; i -= 2) {
      for (int j = (BF_LARGE_LOAD_SIZE * 2) - 1; j > 0; j -= 2) {
        for (int k = (BF_LARGE_LOAD_SIZE * 2) - 1; k > 0; k -= 2) {
          long[] v = new long[] { i, j, k };
          tt.appendTuple(v);
        }
      }
    }

    dbTuples = new DbTuples(tt, rowComparator);

    dbTuples.beforeFirst();
    // check that the order is as expected
    try {
      long count = 0;
      for (int i = 1; i < BF_LARGE_LOAD_SIZE * 2; i += 2) {
        for (int j = 1; j < BF_LARGE_LOAD_SIZE * 2; j += 2) {
          for (int k = 1; k < BF_LARGE_LOAD_SIZE * 2; k += 2) {
            count++;
            assertTrue(dbTuples.next());
            assertEquals("On iteration " + count, i, dbTuples.getColumnValue(0));
            assertEquals("On iteration " + count, j, dbTuples.getColumnValue(1));
            assertEquals("On iteration " + count, k, dbTuples.getColumnValue(2));
          }
        }
      }

      // check that there aren't any extraneous values
      assertTrue(!dbTuples.next());
    } catch (Throwable e) {
      // for debugging:
      // dumpFile(dbTuples);
      throw e;
    }

    dbTuples.beforeFirst(new long[] { 1, 133 }, 0);
    // check that the order is as expected
    try {
      long count = 0;
      int i = 1;
      int j = 133;
      for (int k = 1; k < BF_LARGE_LOAD_SIZE * 2; k += 2) {
        count++;
        if(!dbTuples.next()) {
          logger.error("On iteration " + count + " dbTuples failed .next()");
          assertTrue(false);
        }
        assertEquals("i On iteration " + count, i, dbTuples.getColumnValue(0));
        assertEquals("j On iteration " + count, j, dbTuples.getColumnValue(1));
        assertEquals("k On iteration " + count, k, dbTuples.getColumnValue(2));
      }

      // check that there aren't any extraneous values
      assertTrue(!dbTuples.next());
    } catch (Throwable e) {
      // for debugging:
      // dumpFile(dbTuples);
      throw e;
    }

    dbTuples.beforeFirst(new long[] { 3, 4 }, 0);
    assertFalse(dbTuples.next());
  }


  /**
   * METHOD TO DO
   *
   * @param ft PARAMETER TO DO
   * @throws Exception EXCEPTION TO DO
   */
  @SuppressWarnings("unused")
  private void dumpFile(DbTuples ft) throws Exception {
    PrintStream ps = new PrintStream(new FileOutputStream("/tmp/random.dump"));

    try {
      ft.beforeFirst();
      while (ft.next()) {
        ps.println(ft.getColumnValue(0));
      }
    } finally {
      ps.close();
    }
  }
}
