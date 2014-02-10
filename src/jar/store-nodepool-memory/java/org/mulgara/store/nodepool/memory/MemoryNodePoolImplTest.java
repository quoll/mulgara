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

package org.mulgara.store.nodepool.memory;

// Third party packages
import junit.framework.*;

// Local packages
import org.mulgara.store.nodepool.NodePool;

/**
 * Test cases for NodePoolImpl.
 *
 * @created 2003-09-11
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/22 08:16:42 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MemoryNodePoolImplTest extends TestCase {

  /**
   * Create a new test.
   *
   * @param name name of the test.
   */
  public MemoryNodePoolImplTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new MemoryNodePoolImplTest("testAllocateAndValidNodes"));

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
   * Test the allocation of nodes and the number of valid nodes.
   */
  public void testAllocateAndValidNodes() {

    try {

      MemoryNodePoolImpl nodePool = new MemoryNodePoolImpl();

      assertEquals("First node ", NodePool.MIN_NODE, nodePool.newNode());
      assertEquals("Second node ", NodePool.MIN_NODE + 1, nodePool.newNode());
      assertEquals("Total nodes ", 2, nodePool.getNrValidNodes());
    }
    catch (Exception e) {

      e.printStackTrace();
    }
  }
}
