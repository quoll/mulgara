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
 * Test cases for AVLFile.
 *
 * @created 2001-09-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:31 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class AVLFileTest extends TestCase {

  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(AVLFileTest.class);

  /**
   * Description of the Field
   *
   */
  private static AVLComparator comparator = new IntComparator();

  /**
   * Description of the Field
   *
   */
  private static Block metaroot = Block.newInstance(AVLFile.Phase.RECORD_SIZE * Constants.SIZEOF_LONG);

  /**
   * Description of the Field
   *
   */
  private AVLFile avlFile;

  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public AVLFileTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new AVLFileTest("testInsert"));
    suite.addTest(new AVLFileTest("testFind"));
    suite.addTest(new AVLFileTest("testRemove"));
    suite.addTest(new AVLFileTest("testReinsert"));
    suite.addTest(new AVLFileTest("testPersist"));
    suite.addTest(new AVLFileTest("testMultiphase"));

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
   * Gets the Key attribute of the AVLFileTest class
   *
   * @param node PARAMETER TO DO
   * @return The Key value
   */
  private static int getKey(AVLNode node) {

    return (node == null) ? 0 : node.getPayloadInt(1);
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
      avlFile = new AVLFile(new File(dir, "avlfiletest"), 1);
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

    if (avlFile != null) {

      try {

        avlFile.unmap();

        if (System.getProperty("os.name").startsWith("Win")) {

          // Need this for Windows or truncate() always fails for mapped files.
          System.gc();
          System.runFinalization();
        }

        avlFile.close();
      }
      finally {

        avlFile = null;
      }
    }
  }

  /**
   * Test insert
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testInsert() throws IOException {

    AVLFile.Phase phase0 = avlFile.new Phase();
    avlFile.clear();

    //AVLNode node = phase0.newAVLNodeInstance();
    insert(phase0, 60);
    assertEquals(1, getHeight(phase0));
    insert(phase0, 50);
    assertEquals(2, getHeight(phase0));
    insert(phase0, 70);
    assertEquals(2, getHeight(phase0));
    insert(phase0, 80);
    assertEquals(3, getHeight(phase0));

    // test RR rotation
    insert(phase0, 90);
    assertEquals(3, getHeight(phase0));

    // test RL rotation
    insert(phase0, 65);
    assertEquals(3, getHeight(phase0));

    insert(phase0, 75);
    assertEquals(3, getHeight(phase0));
    insert(phase0, 77);
    assertEquals(4, getHeight(phase0));

    try {

      insert(phase0, 50);
      fail("Able to insert the same node values twice");
    }
    catch (IllegalArgumentException e) {

    }
  }

  /**
   * Test find
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testFind() throws IOException {

    AVLFile.Phase phase0 = avlFile.new Phase();
    avlFile.clear();

    AVLNode[] nodes = find(phase0, 5);

    if (nodes != null) {

      fail("Found node in empty tree");
    }

    insert(phase0, 6);
    insert(phase0, 5);
    insert(phase0, 8);

    assertFound(find(phase0, 5), 5);

    assertNotFound(find(phase0, 7), 6, 8);
  }

  /**
   * Test remove
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testRemove() throws IOException {

    AVLFile.Phase phase0 = avlFile.new Phase();
    avlFile.clear();

    insert(phase0, 60);
    assertEquals(1, getHeight(phase0));
    insert(phase0, 50);
    assertEquals(2, getHeight(phase0));
    insert(phase0, 70);
    assertEquals(2, getHeight(phase0));
    insert(phase0, 80);
    assertEquals(3, getHeight(phase0));
    insert(phase0, 90);
    assertEquals(3, getHeight(phase0));
    insert(phase0, 65);
    assertEquals(3, getHeight(phase0));
    insert(phase0, 75);
    assertEquals(3, getHeight(phase0));
    insert(phase0, 77);
    assertEquals(4, getHeight(phase0));

    find(phase0, 60)[0].remove();
    assertEquals(4, getHeight(phase0));
    assertNotFound(find(phase0, 60), 50, 65);
    assertFound(find(phase0, 65), 65);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 80), 80);
    assertFound(find(phase0, 90), 90);
    assertFound(find(phase0, 65), 65);
    assertFound(find(phase0, 75), 75);
    assertFound(find(phase0, 77), 77);

    find(phase0, 50)[0].remove();
    assertEquals(3, getHeight(phase0));
    assertNotFound(find(phase0, 50), 0, 65);
    assertFound(find(phase0, 65), 65);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 75), 75);
    assertFound(find(phase0, 77), 77);
    assertFound(find(phase0, 80), 80);
    assertFound(find(phase0, 90), 90);

    find(phase0, 75)[0].remove();
    assertEquals(3, getHeight(phase0));
    assertNotFound(find(phase0, 75), 70, 77);
    assertFound(find(phase0, 65), 65);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 77), 77);
    assertFound(find(phase0, 80), 80);
    assertFound(find(phase0, 90), 90);

    find(phase0, 65)[0].remove();
    assertEquals(3, getHeight(phase0));
    assertNotFound(find(phase0, 65), 0, 70);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 77), 77);
    assertFound(find(phase0, 80), 80);
    assertFound(find(phase0, 90), 90);

    find(phase0, 90)[0].remove();
    assertEquals(2, getHeight(phase0));
    assertNotFound(find(phase0, 90), 80, 0);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 77), 77);
    assertFound(find(phase0, 80), 80);

    find(phase0, 80)[0].remove();
    assertEquals(2, getHeight(phase0));
    assertNotFound(find(phase0, 80), 77, 0);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 77), 77);

    find(phase0, 70)[0].remove();
    assertEquals(1, getHeight(phase0));
    assertNotFound(find(phase0, 70), 0, 77);
    assertFound(find(phase0, 77), 77);

    find(phase0, 77)[0].remove();
    assertNull(phase0.getRootNode());
    assertNull(find(phase0, 77));
  }

  /**
   * Test reinsert
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testReinsert() throws IOException {

    AVLFile.Phase phase0 = avlFile.new Phase();
    avlFile.clear();

    insert(phase0, 60);
    insert(phase0, 50);
    insert(phase0, 70);
    insert(phase0, 80);

    assertFound(find(phase0, 60), 60);
    assertFound(find(phase0, 50), 50);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 80), 80);

    find(phase0, 60)[0].remove();
    assertNotFound(find(phase0, 60), 50, 70);
    assertFound(find(phase0, 50), 50);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 80), 80);

    insert(phase0, 60);
    assertFound(find(phase0, 60), 60);
    assertFound(find(phase0, 50), 50);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 80), 80);

    // set up for testPersist()
    phase0.writeToBlock(metaroot, 0);
  }

  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testPersist() throws IOException {

    AVLFile.Phase phase0 = avlFile.new Phase(metaroot, 0);
    @SuppressWarnings("unused")
    AVLFile.Phase.Token token0 = phase0.use();
    AVLFile.Phase phase1 = avlFile.new Phase();

    assertFound(find(phase1, 60), 60);
    assertFound(find(phase1, 50), 50);
    assertFound(find(phase1, 70), 70);
    assertFound(find(phase1, 80), 80);
  }

  /**
   * Test multiphasic modifications
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testMultiphase() throws IOException {

    AVLFile.Phase phase0 = avlFile.new Phase();
    avlFile.clear();

    insert(phase0, 60);
    insert(phase0, 50);
    insert(phase0, 70);
    insert(phase0, 80);

    assertFound(find(phase0, 60), 60);
    assertFound(find(phase0, 50), 50);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 80), 80);

    @SuppressWarnings("unused")
    AVLFile.Phase.Token token0 = phase0.use();
    AVLFile.Phase phase1 = avlFile.new Phase();

    find(phase1, 60)[0].remove();
    assertNotFound(find(phase1, 60), 50, 70);
    assertFound(find(phase1, 50), 50);
    assertFound(find(phase1, 70), 70);
    assertFound(find(phase1, 80), 80);

    assertFound(find(phase0, 60), 60);
    assertFound(find(phase0, 50), 50);
    assertFound(find(phase0, 70), 70);
    assertFound(find(phase0, 80), 80);

    insert(phase1, 60);
    assertFound(find(phase1, 60), 60);
    assertFound(find(phase1, 50), 50);
    assertFound(find(phase1, 70), 70);
    assertFound(find(phase1, 80), 80);

    try {

      find(phase0, 60)[0].remove();
      fail("Able to remove from a read-only phase");
    }
    catch (IllegalStateException ex) {

    }

    try {

      insert(phase0, 75);
      fail("Able to insert into a read-only phase");
    }
    catch (IllegalStateException ex) {

    }
  }

  /**
   * METHOD TO DO
   *
   * @param phase PARAMETER TO DO
   */
  void dumpTree(AVLFile.Phase phase) {

    AVLNode node = phase.getRootNode();
    System.out.println(toString(node));

    if (node != null) {

      node.release();
    }
  }

  /**
   * METHOD TO DO
   *
   * @param node PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  String toString(AVLNode node) {

    if (node == null) {

      return ".";
    }

    AVLNode lNode = node.getLeftChildNode();
    AVLNode rNode = node.getRightChildNode();
    String str =
        "<" + getKey(node) + "[" + toString(lNode) + "," + toString(rNode) +
        "]>";

    if (lNode != null) {

      lNode.release();
    }

    if (rNode != null) {

      rNode.release();
    }

    return str;
  }

  /**
   * Gets the Height attribute of the AVLFileTest object
   *
   * @param phase PARAMETER TO DO
   * @return The Height value
   */
  private int getHeight(AVLFile.Phase phase) {

    AVLNode rootNode = phase.getRootNode();
    int height = rootNode.getHeight();
    rootNode.release();

    return height;
  }

  /**
   * METHOD TO DO
   *
   * @param phase PARAMETER TO DO
   * @param value PARAMETER TO DO
   * @throws IOException EXCEPTION TO DO
   */
  private void insert(AVLFile.Phase phase, int value) throws IOException {

    AVLNode[] findResult = find(phase, value);

    try {

      AVLNode newNode = phase.newAVLNodeInstance();

      try {

        newNode.putPayloadInt(1, value);
        newNode.write();

        if (findResult == null) {

          phase.insertFirst(newNode);
        }
        else {

          // Insert the node into the tree.
          int li = AVLFile.leafIndex(findResult);
          findResult[li].insert(newNode, 1 - li);
        }
      }
      finally {

        newNode.release();
      }
    }
    finally {

      if (findResult != null) {

        AVLFile.release(findResult);
      }
    }
  }

  /**
   * METHOD TO DO
   *
   * @param phase PARAMETER TO DO
   * @param value PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  private AVLNode[] find(AVLFile.Phase phase, int value) {

    return phase.find(comparator, new long[] {value});
  }

  /**
   * METHOD TO DO
   *
   * @param nodes PARAMETER TO DO
   * @param value PARAMETER TO DO
   */
  private void assertFound(AVLNode[] nodes, int value) {

    try {

      assertNotNull("No present node found in occupied tree.", nodes);
      assertEquals("Incorrect number of nodes returned from find " +
          ( (nodes.length == 2)
          ? ("(" + getKey(nodes[0]) + ", " + getKey(nodes[1]) + ")") : ""), 1,
          nodes.length);
      assertEquals("Incorrect node found", value, getKey(nodes[0]));
    } finally {

      AVLFile.release(nodes);
    }
  }

  /**
   * METHOD TO DO
   *
   * @param nodes PARAMETER TO DO
   * @param valueL PARAMETER TO DO
   * @param valueR PARAMETER TO DO
   */
  private void assertNotFound(AVLNode[] nodes, int valueL, int valueR) {

    try {

      assertNotNull("No in-between nodes found in occupied tree.", nodes);
      assertEquals("Incorrect number of nodes returned from find", 2,
          nodes.length);

      if (valueL == 0) {

        assertNull("Non-null node found when searching for small node", nodes[0]);
      }
      else {

        assertNotNull("Null node returned as first node from find", nodes[0]);
      }

      if (valueR == 0) {

        assertNull("Non-null node found when searching for large node", nodes[1]);
      }
      else {

        assertNotNull("Null node returned as first node from find", nodes[1]);
      }

      assertEquals("Incorrect node found (other node is " + getKey(nodes[1]) +
          ")", valueL, getKey(nodes[0]));
      assertEquals("Incorrect node found", valueR, getKey(nodes[1]));
    }
    finally {

      AVLFile.release(nodes);
    }
  }

  static class IntComparator implements AVLComparator {

    public int compare(long[] key, AVLNode node) {

      return (int) key[0] - node.getPayloadInt(1);
    }
  }
}
