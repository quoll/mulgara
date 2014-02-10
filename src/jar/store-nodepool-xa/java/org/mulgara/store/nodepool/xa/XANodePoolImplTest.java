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
package org.mulgara.store.nodepool.xa;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.mulgara.store.nodepool.NoSuchNodeException;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.util.TempDir;

/**
 * Test cases for NodePoolImpl.
 *
 * @created 2001-09-20
 * @author David Makepeace
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/02/22 08:16:45 $ @maintenanceAuthor $Author: newmana $
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class XANodePoolImplTest extends TestCase {

  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(XANodePoolImplTest.class);

  /**
   * Description of the Field
   */
  XANodePoolImpl nodePool;


  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public XANodePoolImplTest(String name) {
    super(name);
  }


  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new XANodePoolImplTest("testAllocateAndRelease"));
    suite.addTest(new XANodePoolImplTest("testPersist"));
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
      nodePool = new XANodePoolImpl(
          TempDir.getTempDir().getPath() + File.separatorChar + "nodepooltest"
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
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void tearDown() throws Exception {
    if (nodePool != null) {
      try {
        nodePool.unmap();
        if (System.getProperty("os.name").startsWith("Win")) {
          // Need this for Windows or truncate() always fails for mapped files.
          System.gc();
          System.runFinalization();
        }
        nodePool.close();
      }
      finally {
        nodePool = null;
      }
    }
  }


  /**
   * @throws IOException EXCEPTION TO DO
   * @throws NodePoolException EXCEPTION TO DO
   * @throws NoSuchNodeException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   * @throws InterruptedException EXCEPTION TO DO
   */
  public void testAllocateAndRelease()
       throws IOException, NodePoolException, NoSuchNodeException,
      SimpleXAResourceException, InterruptedException {
    nodePool.clear();

    // The first allocated node is 1.
    assertEquals(1, nodePool.newNode());
    assertEquals(2, nodePool.newNode());
    assertEquals(3, nodePool.newNode());
    assertEquals(4, nodePool.newNode());

    // There are always at least two phases, so we will not get back (during
    // this phase) any nodes that we release during this phase unless they were
    // originally allocated during this phase.
    nodePool.releaseNode(3);
    // free: 3
    assertEquals(3, nodePool.newNode());
    assertEquals(5, nodePool.newNode());
    nodePool.releaseNode(3);
    // free: 3

    nodePool.prepare();
    // phase 1
    nodePool.releaseNode(2);
    // free: | 3 2

    assertEquals(6, nodePool.newNode());
    assertEquals(7, nodePool.newNode());
    assertEquals(8, nodePool.newNode());
    assertEquals(9, nodePool.newNode());
    nodePool.releaseNode(4);
    // free: | 3 2 4
    nodePool.commit();
    // phase 1

    nodePool.prepare();
    // phase 2
    nodePool.commit();
    // phase 2
  }


  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   * @throws NodePoolException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   * @throws InterruptedException EXCEPTION TO DO
   */
  public void testPersist()
       throws IOException, NodePoolException, SimpleXAResourceException,
      InterruptedException {
    int[] phases = nodePool.recover();
    assertEquals(1, phases.length);
    assertEquals(2, phases[0]);
    nodePool.selectPhase(phases[0]);
    // read phase 2 (dup to phase 3)

    // There are two phases, so we will not get back (during this phase) any
    // nodes that we release during this phase unless they were originally
    // allocated during this phase.

    nodePool.releaseNode(6);
    // free: 3 2 4 | 6

    nodePool.prepare();
    nodePool.commit();

    // There are two phases, so we will not get back (during this
    // phase) any nodes that we release during this phase.
    nodePool.releaseNode(8);
    // free: 3 2 4 | 6 8 or 3 2 4 6 | 8 (when commit done)

    // 3, 2 and 4 are allocated from the free list.
    assertEquals(3, nodePool.newNode());
    assertEquals(2, nodePool.newNode());
    assertEquals(4, nodePool.newNode());
    assertEquals(6, nodePool.newNode());

    // 10 and 11 are allocated for the first time.
    assertEquals(10, nodePool.newNode());
    assertEquals(11, nodePool.newNode());

    nodePool.prepare();
    assertEquals(12, nodePool.newNode());

    nodePool.commit();
    assertEquals(8, nodePool.newNode());

    nodePool.prepare();
    nodePool.commit();
  }

}
