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

package org.mulgara.store.xa;

// Java 2 standard packages
import java.io.*;

// Third party packages
import junit.framework.*;
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.util.Constants;
import org.mulgara.util.TempDir;


/**
 * Test cases for FreeList.
 *
 * @created 2001-09-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/07/21 19:13:49 $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FreeListTest extends TestCase {

  /**
   * Description of the Field
   */
  static Block metaroot = Block.newInstance(FreeList.Phase.RECORD_SIZE * Constants.SIZEOF_LONG);

  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(FreeListTest.class);

  /**
   * Description of the Field
   */
  FreeList freeList;

  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public FreeListTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new FreeListTest("testAllocateAndRelease"));
    suite.addTest(new FreeListTest("testPersist"));

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

      File dir = TempDir.getTempDir();
      freeList = FreeList.openFreeList(new File(dir, "freelisttest"));
      exceptionOccurred = false;
    } finally {

      if (exceptionOccurred) {

        tearDown();
      }
    }
  }

  /**
   * The teardown method for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void tearDown() throws IOException {

    if (freeList != null) {

      try {

        freeList.unmap();

        if (System.getProperty("os.name").startsWith("Win")) {

          // Need this for Windows or truncate() always fails for mapped files.
          System.gc();
          System.runFinalization();
        }

        freeList.close();
      }
      finally {

        freeList = null;
      }
    }
  }

  /**
   * @throws IOException EXCEPTION TO DO
   */
  public void testAllocateAndRelease() throws IOException {

    FreeList.Phase phase0 = freeList.new Phase(1);
    freeList.clear();

    // The first allocated item is 1.
    assertEquals(1, freeList.getNextItem());
    assertEquals(1, freeList.allocate());
    assertEquals(2, freeList.allocate());
    assertEquals(3, freeList.allocate());
    assertEquals(4, freeList.allocate());

    // There is only one phase so we can get back (during this phase)
    // any node that we release during this phase.
    freeList.free(3);

    // free: 3
    assertEquals(5, freeList.getNextItem());
    assertEquals(3, freeList.allocate());
    assertEquals(5, freeList.getNextItem());

    @SuppressWarnings("unused")
    FreeList.Phase.Token token0 = phase0.use();
    FreeList.Phase phase1 = freeList.new Phase();

    // There are now two phases, so we will not get back (during this phase)
    // nodes that we release during this phase unless they were originally
    // allocated during this phase.
    freeList.free(2);

    // free: | 2
    assertEquals(5, freeList.allocate());
    assertEquals(6, freeList.allocate());
    assertEquals(7, freeList.allocate());
    assertEquals(8, freeList.allocate());
    freeList.free(4);

    // free: | 2 4
    @SuppressWarnings("unused")
    FreeList.Phase.Token token1 = phase1.use();
    @SuppressWarnings("unused")
    FreeList.Phase phase2 = freeList.new Phase();

    phase1.writeToBlock(metaroot, 0);
  }

  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testPersist() throws IOException {

    FreeList.Phase phase1 = freeList.new Phase(metaroot, 0);
    FreeList.Phase.Token token1 = phase1.use();
    FreeList.Phase phase2 = freeList.new Phase();

    // There are two phases, so we will not get back (during this
    // phase) any nodes that we release during this phase.
    freeList.free(6);

    // free: 2 4 | 6
    FreeList.Phase.Token token2 = phase2.use();
    FreeList.Phase phase3 = freeList.new Phase();

    // There are now two phases, so we will not get back (during this
    // phase) any nodes that we release during this phase.
    freeList.free(8);

    // free: 2 4 | 6 | 8
    // 2 and 4 are allocated from the free list.
    assertEquals(2, freeList.allocate());
    assertEquals(4, freeList.allocate());
    token1.release();

    // free: 6 | 8
    assertEquals(6, freeList.allocate());

    // 9 and 10 are allocated for the first time.
    assertEquals(9, freeList.allocate());
    assertEquals(10, freeList.allocate());

    @SuppressWarnings("unused")
    FreeList.Phase.Token token3 = phase3.use();
    FreeList.Phase phase4 = freeList.new Phase();
    assertEquals(11, freeList.allocate());

    token2.release();
    assertEquals(8, freeList.allocate());

    @SuppressWarnings("unused")
    FreeList.Phase.Token token4 = phase4.use();
    @SuppressWarnings("unused")
    FreeList.Phase phase5 = freeList.new Phase();
  }
}
