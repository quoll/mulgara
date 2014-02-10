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

// Third party packages
import junit.framework.*;

import java.io.*;

import org.apache.log4j.Logger;

/**
 * Test cases for BlockFile.
 *
 * @created 2001-09-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:31 $
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
public abstract class BlockFileTest extends TestCase {

  /**
   * Description of the Field
   */
  public final static int BLOCK_SIZE = 256;

  /**
   * Description of the Field
   */
  public final static String STR0 = "String in block 0.";

  /**
   * Description of the Field
   */
  public final static String STR1 = "String in block 1.";

  /**
   * Description of the Field
   */
  public final static String STR2 = "String in block 2.";

  /**
   * Description of the Field
   */
  public final static String STR3 = "String in block 3.";

  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(BlockFileTest.class);

  /**
   * Description of the Field
   */
  protected BlockFile blockFile;

  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public BlockFileTest(String name) {
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

  /**
   * The teardown method for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void tearDown() throws IOException {

    if (blockFile != null) {

      try {

        blockFile.unmap();

        if (System.getProperty("os.name").startsWith("Win")) {

          // Need this for Windows or truncate() always fails for mapped files.
          System.gc();
          System.runFinalization();
        }

        blockFile.close();
      }
      finally {

        blockFile = null;
      }
    }
  }

  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testAllocate() throws IOException {

    blockFile.clear();
    blockFile.setNrBlocks(1);

    Block blk = blockFile.allocateBlock(0);
    assertNotNull(blk);
  }

  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testWrite() throws IOException {

    blockFile.setNrBlocks(4);

    Block blk = blockFile.allocateBlock(0);
    putString(blk, STR0);
    blk.write();

    blk = blockFile.allocateBlock(3);
    putString(blk, STR3);
    blk.write();

    blk = blockFile.allocateBlock(2);
    putString(blk, STR2);
    blk.write();

    blk = blockFile.allocateBlock(1);
    putString(blk, STR1);
    blk.write();

    // Check what was written.
    blk = blockFile.readBlock(2);
    assertEquals(STR2, getString(blk));

    blk = blockFile.readBlock(0);
    assertEquals(STR0, getString(blk));

    blk = blockFile.readBlock(1);
    assertEquals(STR1, getString(blk));

    blk = blockFile.readBlock(3);
    assertEquals(STR3, getString(blk));
  }

  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testPersist() throws IOException {

    assertEquals(4, blockFile.getNrBlocks());

    Block blk = blockFile.readBlock(2);
    assertEquals(STR2, getString(blk));

    blk = blockFile.readBlock(0);
    assertEquals(STR0, getString(blk));

    blk = blockFile.readBlock(1);
    assertEquals(STR1, getString(blk));

    blk = blockFile.readBlock(3);
    assertEquals(STR3, getString(blk));
  }

  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testPerformance() throws IOException {

    int nrBlocks = 100000;
    blockFile.clear();
    blockFile.setNrBlocks(nrBlocks);

    for (int i = 0; i < nrBlocks; ++i) {
      Block blk = blockFile.allocateBlock(i);
      blk.putInt(0, i + 5);
      blk.write();
    }

    for (int i = 0; i < nrBlocks; ++i) {
      Block blk = blockFile.readBlock(i);
      assertEquals(i + 5, blk.getInt(0));
    }

    for (int pass = 0; pass < 10; ++pass) {
      for (int i = 0; i < nrBlocks; ++i) {
        Block blk = blockFile.readBlock(i);
        blk.putInt(0, i ^ pass);
        blk.write();
      }

      for (int i = 0; i < nrBlocks; ++i) {
        Block blk = blockFile.readBlock(i);
        assertEquals(i ^ pass, blk.getInt(0));
      }
    }

    blockFile.clear();
  }

  /**
   * Gets the String attribute of the BlockFileTest object
   *
   * @param block PARAMETER TO DO
   * @return The String value
   */
  private String getString(Block block) {

    byte[] len = new byte[1];
    block.get(0, len);

    byte[] strBytes = new byte[ (int) len[0]];
    block.get(1, strBytes);

    try {

      return new String(strBytes, "UTF-8");
    }
    catch (UnsupportedEncodingException ex) {

      // Shouldn't happen.
      return null;
    }
  }

  /**
   * METHOD TO DO
   *
   * @param block PARAMETER TO DO
   * @param str PARAMETER TO DO
   */
  private void putString(Block block, String str) {

    byte[] strBytes = null;

    try {

      strBytes = str.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException ex) {

      // Shouldn't happen.
    }

    block.put(0, new byte[] {
         (byte) strBytes.length});
    block.put(1, strBytes);
  }
}
