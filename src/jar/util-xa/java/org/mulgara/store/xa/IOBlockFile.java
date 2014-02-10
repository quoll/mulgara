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

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

// Third party packages
import org.apache.log4j.Logger;

/**
 * An implementation of BlockFile which uses regular (position/read/write) file
 * IO.
 *
 * @created 2001-09-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/06/30 01:14:40 $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class IOBlockFile extends AbstractBlockFile {

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(IOBlockFile.class);

  private static final int NOMINAL_ALLOCATION_SIZE = 1048576; // in bytes

  private long allocatedNrBlocks;

  private int allocationSize; // in blocks

  /**
   * Constructs an IOBlockFile for the file with the specified file name.
   *
   * @param file the block file.
   * @param blockSize the size of blocks in the block file.
   * @throws IOException if an I/O error occurs.
   */
  public IOBlockFile(File file, int blockSize) throws IOException {
    super(file, blockSize);

    allocatedNrBlocks = nrBlocks;
    allocationSize = blockSize < NOMINAL_ALLOCATION_SIZE ?
        NOMINAL_ALLOCATION_SIZE / blockSize : 1;
  }

  /**
   * Constructs an IOBlockFile for the file with the specified file name.
   *
   * @param fileName the file name of the block file.
   * @param blockSize the size of blocks in the block file.
   * @throws IOException if an I/O error occurs.
   */
  public IOBlockFile(String fileName, int blockSize) throws IOException {
    this(new File(fileName), blockSize);
  }

  /**
   * Sets the length of the file in blocks.
   *
   * @param nrBlocks The number of blocks in the file.
   * @throws IOException if an I/O error occurs.
   */
  public void setNrBlocks(long nrBlocks) throws IOException {
    if (nrBlocks == this.nrBlocks) return;

    super.setNrBlocks(nrBlocks);

    if (nrBlocks <= allocatedNrBlocks) return;

    allocatedNrBlocks = nrBlocks - (nrBlocks % allocationSize) + allocationSize;

    for (;;) {
      try {
        raf.setLength(allocatedNrBlocks * blockSize);
        break;
      } catch (ClosedChannelException ex) {
        // The Channel may have been inadvertently closed by another thread
        // being interrupted.  Attempt to reopen the channel.
        if (!ensureOpen()) {
          throw ex;
        }

        // Loop back and retry the setLength().
      }
    }
  }

  /**
   * Allocates a ByteBuffer to be used for writing to the specified block. The
   * contents of the ByteBuffer are undefined. The method {@link #writeBlock}
   * is called to write the buffer to the block.
   *
   * @param blockId The ID of the block that this buffer will be written to.
   * @return a ByteBuffer to be used for writing to the specified block.
   */
  public Block allocateBlock(long blockId) {
    assert(blockId >= 0) && (blockId < nrBlocks);

    return Block.newInstance(this, blockSize, blockId, byteOrder);
  }

  /**
   * Allocates a ByteBuffer which is filled with the contents of the specified
   * block. If the buffer is modified then the method {@link #writeBlock}
   * should be called to write the buffer back to the file.
   *
   * @param blockId the block to read into the ByteBuffer.
   * @return The allocated block, containing valid data from the file.
   * @throws IOException if an I/O error occurs.
   */
  public Block readBlock(long blockId) throws IOException {
    // Create the buffer to read into.
    Block block = allocateBlock(blockId);

    ByteBuffer byteBuffer = block.getByteBuffer();

    for (;;) {
      try {
        // Reset the position in this byte buffer.
        byteBuffer.rewind();

        // Read the block into the buffer.
        fc.read(byteBuffer, blockId * blockSize);

        break;
      } catch (ClosedChannelException ex) {
        // The Channel may have been inadvertently closed by another thread
        // being interrupted.  Attempt to reopen the channel.
        if (!ensureOpen()) {
          throw ex;
        }

        // Loop back and retry the read.
      }
    }

    // Return the block.
    return block;
  }

  /**
   * Writes a buffer that was allocated by calling either {@link
   * #allocateBlock} or {@link #readBlock} to the specified block. The buffer
   * may only be written to the same block as was specified when the buffer was
   * allocated.
   *
   * @param block the buffer to write to the file.
   * @throws IOException if an I/O error occurs.
   */
  public void writeBlock(Block block) throws IOException {
    long blockId = block.getBlockId();
    assert(blockId >= 0) && (blockId < nrBlocks);

    ByteBuffer byteBuffer = block.getByteBuffer();

    for (;;) {
      try {
        // Reset the position.
        byteBuffer.rewind();

        // Write the buffer to the file.
        fc.write(byteBuffer, blockId * blockSize);

        break;
      } catch (ClosedChannelException ex) {
        // The Channel may have been inadvertently closed by another thread
        // being interrupted.  Attempt to reopen the channel.
        if (!ensureOpen()) {
          throw ex;
        }

        // Loop back and retry the write.
      }
    }
  }

  /**
   * Changes the block ID of the specified Block. This method is called
   * copyBlock because a call to copyBlock() followed by a call to writeBlock()
   * can be used to copy the contents of a block to a new location in the block
   * file.
   *
   * @param block the Block to be copied.
   * @param dstBlockId the ID of the block to which the Block will be written
   *      when writeBlock() is called.
   * @throws IOException if an I/O error occurs.
   */
  public void copyBlock(Block block, long dstBlockId) {
    block.setBlockId(dstBlockId);
  }

  /**
   * Attempt to re-use the given Block and wrapped ByteBuffer to read the indicated block.
   * null ByteBuffer will behave like readBlock.
   * @author barmintor
   * @param blockId The block to read into the ByteBuffer.
   * @param block The ByteBuffer to attempt to re-use
   * @return The buffer that was read.
   * @throws IOException if an I/O error occurs.
   */
  public Block recycleBlock(long blockId, Block block) throws IOException {
    if (block == null) return readBlock(blockId);
    Block.recycleBuffer(block, blockId);
    return block;
  }
}
