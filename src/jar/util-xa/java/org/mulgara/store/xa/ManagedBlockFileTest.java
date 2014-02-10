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
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class ManagedBlockFileTest extends TestCase {

  /**
   * Description of the Field
   *
   */
  public final static int BLOCK_SIZE = 256;

  /**
   * Description of the Field
   *
   */
  public final static String STR0 = "String in block 0.";

  /**
   * Description of the Field
   *
   */
  public final static String STR1 = "String in block 1.";

  /**
   * Description of the Field
   *
   */
  public final static String STR2 = "String in block 2.";

  /**
   * Description of the Field
   *
   */
  public final static String STR3 = "String in block 3.";

  /**
   * Description of the Field
   *
   */
  public final static String STR4 = "String in block 4.";

  /**
   * Description of the Field
   *
   */
  public final static String STR5 = "String in block 5.";

  /**
   * Description of the Field
   *
   */
  protected static Block metaroot = Block.newInstance(ManagedBlockFile.Phase.RECORD_SIZE * Constants.SIZEOF_LONG);

  /**
   * Logger.
   *
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(ManagedBlockFileTest.class);

  /**
   * Description of the Field
   *
   */
  protected ManagedBlockFile blockFile;

  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public ManagedBlockFileTest(String name) {
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
        blockFile.close();
      } finally {
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
    ManagedBlockFile.Phase phase0 = blockFile.new Phase();
    blockFile.clear();

    Block blk = phase0.allocateBlock();
    assertNotNull(blk);
  }

  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testWrite() throws IOException {
    ManagedBlockFile.Phase phase0 = blockFile.new Phase();
    blockFile.clear();

    @SuppressWarnings("unused")
    ManagedBlockFile.Phase.Token token0 = phase0.use();
    ManagedBlockFile.Phase phase1 = blockFile.new Phase();

    Block blk = phase1.allocateBlock();
    assertEquals(0, blk.getBlockId());
    putString(blk, STR0);
    blk.write();

    blk = phase1.allocateBlock();
    assertEquals(1, blk.getBlockId());
    putString(blk, STR1);
    blk.write();

    blk = phase1.allocateBlock();
    assertEquals(2, blk.getBlockId());
    putString(blk, STR2);
    blk.write();

    blk = phase1.allocateBlock();
    assertEquals(3, blk.getBlockId());
    putString(blk, STR3);
    blk.write();

    // Check what was written.
    blk = phase1.readBlock(2);
    assertEquals(STR2, getString(blk));

    blk = phase1.readBlock(0);
    assertEquals(STR0, getString(blk));

    blk = phase1.readBlock(1);
    assertEquals(STR1, getString(blk));

    blk = phase1.readBlock(3);
    assertEquals(STR3, getString(blk));

    phase1.writeToBlock(metaroot, 0);
  }

  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testPersist() throws IOException {
    ManagedBlockFile.Phase phase1 = blockFile.new Phase(metaroot, 0);
    @SuppressWarnings("unused")
    ManagedBlockFile.Phase.Token token1 = phase1.use();
    ManagedBlockFile.Phase phase2 = blockFile.new Phase();

    assertEquals(4, phase2.getNrBlocks());

    Block blk = phase2.readBlock(2);
    assertEquals(STR2, getString(blk));

    blk = phase2.readBlock(0);
    assertEquals(STR0, getString(blk));

    blk = phase2.readBlock(1);
    assertEquals(STR1, getString(blk));

    blk = phase2.readBlock(3);
    assertEquals(STR3, getString(blk));

    phase2.writeToBlock(metaroot, 0);
  }

  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testDuplicate() throws IOException {
    ManagedBlockFile.Phase phase2 = blockFile.new Phase(metaroot, 0);
    @SuppressWarnings("unused")
    ManagedBlockFile.Phase.Token token2 = phase2.use();
    ManagedBlockFile.Phase phase3 = blockFile.new Phase();
    @SuppressWarnings("unused")
    ManagedBlockFile.Phase.Token token3 = phase3.use();

    Block blk = phase3.readBlock(3);
    assertEquals(STR3, getString(blk));
    blk.modify();

    long dup3Id = blk.getBlockId();
    assertEquals(4, dup3Id);
    blk.write();

    blk = phase3.allocateBlock();
    assertEquals(5, blk.getBlockId());
    putString(blk, STR5);
    blk.write();

    blk = phase3.readBlock(5);
    assertEquals(STR5, getString(blk));
    blk.modify();
    assertEquals(5, blk.getBlockId());
    blk.write();

    // Check that the duplicate of block 3 has the same contents as block 3.
    blk = phase3.readBlock(dup3Id);
    assertEquals(STR3, getString(blk));

    ManagedBlockFile.Phase phase4 = blockFile.new Phase();

    blk = phase4.readBlock(5);
    blk.modify();
    assertEquals(6, blk.getBlockId());

    Block blk5 = phase4.readBlock(5);
    assertEquals(
        blk5.getByteBuffer().position(0), blk.getByteBuffer().position(0)
    );
    blk.write();

    phase4.writeToBlock(metaroot, 0);
  }

  /**
   * Gets the String attribute of the ManagedBlockFileTest object
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
    } catch (UnsupportedEncodingException ex) {
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
    } catch (UnsupportedEncodingException ex) {
      // Shouldn't happen.
    }

    block.put(0, new byte[] {(byte)strBytes.length});
    block.put(1, strBytes);
  }

}
