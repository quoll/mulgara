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
import java.nio.*;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.util.Constants;

/**
 * This class contains data from a disk block and is used to abstract disk access.
 * Blocks are pooled, and are used to represent an arbitrary area of a file.
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
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class Block {

  public static final long INVALID_BLOCK_ID = -1;

  private final static Logger logger = Logger.getLogger(Block.class);

  /** The property for the block type. May be "direct" or "javaHeap" */
  public static final String MEM_TYPE_PROP = "mulgara.xa.memoryType";

  /** Enumeration of the different memory types for blocks */
  enum BlockMemoryType { DIRECT, HEAP };

  /** The default value to use for the block memory type. Used when nothing is configured. */
  private static final BlockMemoryType DEFAULT = BlockMemoryType.DIRECT;

  /** The configured type of block type to use. */
  private static final BlockMemoryType BLOCK_TYPE;

  static {
    String defBlockType = System.getProperty(MEM_TYPE_PROP, DEFAULT.name());
    if (defBlockType.equalsIgnoreCase(BlockMemoryType.DIRECT.name())) {
      BLOCK_TYPE = BlockMemoryType.DIRECT;
    } else if (defBlockType.equalsIgnoreCase(BlockMemoryType.HEAP.name())) {
      BLOCK_TYPE = BlockMemoryType.HEAP;
    } else {
      logger.warn("Invalid value for property " + MEM_TYPE_PROP + ": " + defBlockType);
      BLOCK_TYPE = DEFAULT;
    }
  }

  /** The file that this block is attached to. */
  private BlockFile blockFile;

  /** The size of the data in this block. */
  private int blockSize;

  /** The ID of this block, unique within the file, and usually maps to a block position. */
  private long blockId;

  // TODO: check if this is still needed
  /**
   * Indicates if this block owns the buffer inside it.  If the buffer
   * is owned then it must be freed (and written back to disk if it is
   * dirty) when the block is released.
   */
  private boolean ownsBuffer = false;

  /** Offset of this block of data from the beginning of the buffer, in bytes. */
  private int byteOffset;

  /** Offset of this block of data from the beginning of the buffer, in 32 bit integers. */
  private int intOffset;

  /** Offset of this block of data from the beginning of the buffer, in 64 bit longs. */
  private int longOffset;

  /** The buffer containing the data of the block. */
  private ByteBuffer bb;

  /** A cached read only ByteBuffer used when a ByteBuffer source of data is needed. */
  private ByteBuffer sbb;

  /** The buffer as 32 bit integers. */
  private IntBuffer ib;

  /** The data buffer as 64 bit longs. */
  private LongBuffer lb;

  /**
   * Builds a block.
   *
   * @param blockFile The file that this data block is from.
   * @param blockSize The size of the data.
   * @param blockId The ID of the block in the file.
   * @param byteOffset The offset of the data for this block, from the start of the buffer.
   * @param bb The buffer containing the data.
   * @param sbb A cached read-only buffer, useful to use as a source for writing operations
   *            which require a buffer of data.
   * @param ib The 32 bit integer offset of the data for this block, from the start of the buffer.
   * @param lb The 64 bit long offset of the data for this block, from the start of the buffer.
   */
  private Block(
      BlockFile blockFile, int blockSize,
      long blockId, int byteOffset, ByteBuffer bb, ByteBuffer sbb,
      IntBuffer ib, LongBuffer lb
  ) {
    init(blockFile, blockSize, blockId, byteOffset, bb, sbb, ib, lb);
  }

  /**
   * Factory method to produce a block.
   *
   * @param blockFile The file to get the data from.
   * @param blockSize The size of the data.
   * @param blockId The ID of the block in the file.
   * @param byteOffset The offset of the data for this block, from the start of the buffer.
   * @param bb The buffer containing the data.
   * @param sbb A cached read-only buffer, useful to use as a source for writing operations
   *            which require a buffer of data.
   * @param ib The 32 bit integer offset of the data for this block, from the start of the buffer.
   * @param lb The 64 bit long offset of the data for this block, from the start of the buffer.
   * @return A new block.
   */
  public static Block newInstance(
      BlockFile blockFile, int blockSize, long blockId, int byteOffset, ByteBuffer bb,
      ByteBuffer sbb, IntBuffer ib, LongBuffer lb
  ) {
    return new Block(blockFile, blockSize, blockId, byteOffset, bb, sbb, ib, lb);
  }

  /**
   * Create a new block, not attached to any file.
   *
   * @param blockSize The size of the block to create.
   * @return A new block, with no file data.
   */
  public static Block newInstance(int blockSize) {
    return newInstance(null, blockSize, 0, ByteOrder.nativeOrder());
  }

  /**
   * Factory method to create a new block, allocating a new data buffer.
   *
   * @param blockFile The file to get the block from.
   * @param blockSize The size of the data in the block.
   * @param blockId The ID of the block from the file.
   * @param byteOrder Represents little endian or big endian byte ordering.
   * @return A new block.
   */
  public static Block newInstance(BlockFile blockFile, int blockSize, long blockId, ByteOrder byteOrder) {
    ByteBuffer buffer = BLOCK_TYPE == BlockMemoryType.DIRECT ?
        ByteBuffer.allocateDirect(blockSize).order(byteOrder) :
        ByteBuffer.allocate(blockSize).order(byteOrder);

    Block block = Block.newInstance(
        blockFile, blockSize, blockId, 0,
        buffer, null, null, null
    );
    block.ownsBuffer = true;
    return block;
  }

  /**
   * @author barmintor
   * @param block The buffer to be re-used by a BlockFile
   * @param blockId The ID of the block from the file.
   */
   static void recycleBuffer(Block block, long blockId) {
    block.ownsBuffer = false;
    block.bb.clear();
    block.ib.rewind();
    block.lb.rewind();
    block.init(blockId, 0, block.bb, null, block.ib, block.lb);
    block.ownsBuffer = true;
  }

  /**
   * Gets a byte from the block.
   *
   * @param offset The location of the byte within the data block.
   * @return The byte value.
   */
  public byte getByte(int offset) {
    return bb.get(byteOffset + offset);
  }

  /**
   * Gets an integer from the block
   *
   * @param offset The location of the integer within the data block.  Indexed as the
   *        number of integers into the buffer.
   * @return The int value.
   */
  public int getInt(int offset) {
    return ib.get(intOffset + offset);
  }

  /**
   * Gets a UInt from the block
   *
   * @param offset The location of the integer within the data block.  Indexed as the
   *        number of integers into the buffer.
   * @return The UInt value (as a long, so the top bit is handled correctly).
   */
  public long getUInt(int offset) {
    return (long) ib.get(intOffset + offset) & Constants.MASK32;
  }

  /**
   * Gets the Long attribute of the Block object
   *
   * @param offset The location of the long within the data block.  Indexed as the
   *        number of longs into the buffer.
   * @return The Long value
   */
  public long getLong(int offset) {
    return lb.get(longOffset + offset);
  }

  /**
   * Gets an array of bytes from the buffer.
   *
   * @param offset The location of the required byte array within the data block.
   * @param ba The array of bytes to fill.
   */
  public void get(int offset, byte[] ba) {
    int start = byteOffset + offset;

    for (int i = 0; i < ba.length; ++i) {
      ba[i] = bb.get(start + i);
    }
  }

  /**
   * Gets a buffer of bytes from the buffer.
   *
   * @param offset The location of the required buffer within the data block.
   * @param byteBuffer The buffer to fill.
   */
  public void get(int offset, ByteBuffer byteBuffer) {
    assert offset + byteBuffer.remaining() <= blockSize;

    ByteBuffer src = bb.asReadOnlyBuffer();
    int pos = byteOffset + offset;
    src.position(pos);
    src.limit(pos + byteBuffer.remaining());
    byteBuffer.put(src);
  }

  /**
   * Gets a read-only portion of the buffer.
   *
   * @param offset The location of the required buffer within the data block.
   * @param size The size of the slice to retrieve.
   * @return The read only portion of the buffer desired.
   */
  public ByteBuffer getSlice(int offset, int size) {
    assert offset + size <= blockSize;

    ByteBuffer data = bb.asReadOnlyBuffer();
    data.position(byteOffset + offset);
    data.limit(byteOffset + offset + size);
    return data.slice();
  }

  /**
   * Gets an array of integers from the buffer.
   *
   * @param offset The integer offset to get the data from.
   * @param ia The array of integers to fill.
   */
  public void get(int offset, int[] ia) {
    int start = intOffset + offset;

    for (int i = 0; i < ia.length; ++i) {
      ia[i] = ib.get(start + i);
    }
  }

  /**
   * Gets an array of longs from the buffer.
   *
   * @param offset The long offset to get the data from.
   * @param la The array of longs to fill.
   */
  public void get(int offset, long[] la) {
    int start = longOffset + offset;

    for (int i = 0; i < la.length; ++i) {
      la[i] = lb.get(start + i);
    }
  }

  /**
   * Copies bytes from this block to the specified block. Only the write
   * session should call this method.
   *
   * @param offset Offset of the data to get.
   * @param block The data block to fill.
   */
  public void get(int offset, Block block) {
    block.put(0, this, offset, block.blockSize);
  }

  /**
   * Gets the ByteBuffer attribute of the Block object
   *
   * @return The ByteBuffer value
   */
  public ByteBuffer getByteBuffer() {
    return bb;
  }

  /**
   * Gets the BlockId attribute of the Block object
   *
   * @return The BlockId value
   */
  public long getBlockId() {
    return blockId;
  }

  /**
   * Gets the ByteOffset attribute of the Block object
   *
   * @return The ByteOffset value
   */
  public int getByteOffset() {
    return byteOffset;
  }

  /**
   * Puts a byte into the buffer.
   *
   * @param offset The offset into the buffer of the byte to set.
   * @param b The value of the byte to set.
   */
  public void putByte(int offset, byte b) {
    bb.put(byteOffset + offset, b);
  }

  /**
   * Puts an int into the buffer.
   *
   * @param offset The offset into buffer measured in 32 bit integers.
   * @param i The integer value to set.
   */
  public void putInt(int offset, int i) {
    ib.put(intOffset + offset, i);
  }

  /**
   * Puts an unsigned int into the buffer.
   *
   * @param offset The offset into buffer measured in 32 bit integers.
   * @param ui The unsigned integer to set, uses a long to avoid the sign bit.
   */
  public void putUInt(int offset, long ui) {
    assert ui >= 0 && ui < (1L << 32) : ui + " isn't an unsigned int";
    ib.put(intOffset + offset, (int) ui);
  }

  /**
   * Puts a long into the data buffer.
   *
   * @param offset The offset into buffer measured in 64 bit longs.
   * @param l The long to set.
   */
  public void putLong(int offset, long l) {
    lb.put(longOffset + offset, l);
  }

  /**
   * Puts an array of bytes into the data buffer.
   *
   * @param offset The byte offset into the buffer.
   * @param ba The byte array with the data to put in the buffer.
   */
  public void put(int offset, byte[] ba) {
    assert offset + ba.length <= blockSize;

    // There is only one writer so it is safe to change the position.
    bb.position(byteOffset + offset);
    bb.put(ba);
  }

  /**
   * Puts an buffer of bytes into the data buffer.
   *
   * @param offset The byte offset into the destination buffer.
   * @param byteBuffer The byte buffer with the data to put in the destination buffer.
   */
  public void put(int offset, ByteBuffer byteBuffer) {
    assert offset + byteBuffer.remaining() <= blockSize;

    // There is only one writer so it is safe to change the position.
    bb.position(byteOffset + offset);
    bb.put(byteBuffer);
  }

  /**
   * Puts an array of integers into the data buffer.
   *
   * @param offset The offset into the buffer, indexed by 32 bit integers.
   * @param ia The array of integers to write.
   */
  public void put(int offset, int[] ia) {
    // There is only one writer so it is safe to change the position.
    ib.position(intOffset + offset);
    ib.put(ia);
  }

  /**
   * Puts an array of longs into the data buffer.
   *
   * @param offset The offset into the buffer, index by 64 bit longs.
   * @param la The array of longs to write.
   */
  public void put(int offset, long[] la) {
    // There is only one writer so it is safe to change the position.
    lb.position(longOffset + offset);
    lb.put(la);
  }

  /**
   * Puts the whole of the buffer backed by the specified block into this block
   * starting from the specified offset.
   *
   * @param offset The offset into the data buffer of this block to write to.
   * @param block The block with the data to write to the data buffer.
   */
  public void put(int offset, Block block) {
    put(offset, block, 0, block.blockSize);
  }

  /**
   * Puts some data from one block into this block.
   *
   * @param offset The offset into this block of data to put the new data.
   * @param block The block containing the data to put into this block.
   * @param srcOffset The offset into the block parameter to find the source data.
   * @param length The number of bytes from the source block to copy over.
   */
  public void put(int offset, Block block, int srcOffset, int length) {
    // There is only one writer so it is safe to change the position.
    bb.position(byteOffset + offset);
    bb.put(block.getSourceBuffer(srcOffset, length));
  }

  /**
   * Writes the data in this block out to the file that this block represents.
   * This operation must occur in a writing phase.
   *
   * @throws IOException There was an exception writing to the file.
   */
  public void write() throws IOException {
    blockFile.writeBlock(this);
  }

  /**
   * Performs a copy-on-write function for the block.  If this block is unmodified
   * then copy this block to a new block in the file, and set this block to refer
   * to the new file block.  If the block has already been modified then there is
   * no need to do anything.  This operation must occur in a writing phase.
   *
   * @throws IOException There was an exception writing to the file.
   */
  public void modify() throws IOException {
    blockFile.modifyBlock(this);
  }

  /**
   * Tells the file that this block is no longer in use and can be recycled.  This is
   * accomplished by putting it on the free list for the file.
   *
   * @throws IOException There was an error writing to the free list.
   */
  public void free() throws IOException {
    blockFile.freeBlock(blockId);
  }

  /**
   * Determines if the contents of this block are equal to the specified block.
   *
   * @param o the block to compare to.
   * @return <code>true</code> if the blocks compare equal, both in ID and in content.
   */
  public boolean equals(Object o) {
    if (!(o instanceof Block)) {
      return false;
    }

    Block block = (Block) o;

    if (blockSize != block.blockSize) {
      return false;
    }

    if (block.bb == bb && block.getBlockId() == blockId) {
      return true;
    }

    ByteBuffer bb1 = bb.asReadOnlyBuffer();
    bb1.limit(byteOffset + blockSize);
    bb1.position(byteOffset);

    ByteBuffer bb2 = block.bb.asReadOnlyBuffer();
    bb2.limit(block.byteOffset + blockSize);
    bb2.position(block.byteOffset);

    return bb1.equals(bb2);
  }

  /** @see java.lang.Object#hashCode() */
  public int hashCode() {
    return blockSize * 13 +
           ((int)(blockId >> 32) ^ (int)(blockId & 0xFFFF)) * 17 +
           bb.hashCode() * 19;
  }

  /**
   * Sets the file this block is a part of.
   * Used by {@link ManagedBlockFile} to redirect calls to itself.
   *
   * @param blockFile The new BlockFile value
   */
  void setBlockFile(BlockFile blockFile) {
    this.blockFile = blockFile;
  }

  /**
   * Sets the BlockId attribute of the Block object.
   *
   * @param blockId The new BlockId value.
   */
  void setBlockId(long blockId) {
    assert ownsBuffer;
    this.blockId = blockId;
  }

  /**
   * Gets the SourceBuffer attribute of the Block object.
   *
   * @return The SourceBuffer value.
   */
  ByteBuffer getSourceBuffer() {
    return sbb;
  }

  /**
   * Initializes a block with all of its buffers and offsets into those buffers.
   *
   * @param blockId ID of the block.
   * @param byteOffset The offset of the data for this block, from the start of the buffer.
   * @param bb The buffer containing the data.
   * @param sbb A cached read-only buffer, useful to use as a source for writing operations
   *            which require a buffer of data.
   * @param ib The 32 bit integer offset of the data for this block, from the start of the buffer.
   * @param lb The 64 bit long offset of the data for this block, from the start of the buffer.
   */
  void init(
      long blockId, int byteOffset, ByteBuffer bb, ByteBuffer sbb,
      IntBuffer ib, LongBuffer lb
  ) {
    assert !ownsBuffer;
    this.blockId = blockId;
    this.byteOffset = byteOffset;
    this.bb = bb;
    this.sbb = sbb;

    if (ib == null) {
      bb.rewind();
      ib = bb.asIntBuffer();
    }

    this.ib = ib;

    if (lb == null) {
      bb.rewind();
      lb = bb.asLongBuffer();
    }

    this.lb = lb;
    intOffset = byteOffset / Constants.SIZEOF_INT;
    longOffset = byteOffset / Constants.SIZEOF_LONG;
  }

  /**
   * Gets section out of the SourceBuffer attribute of the Block object.
   *
   * @param offset An offset into the current buffer.
   * @param length The length of the required data.
   * @return The SourceBuffer value, with a limit and position set.
   */
  private ByteBuffer getSourceBuffer(int offset, int length) {
    if (sbb == null) {
      sbb = bb.asReadOnlyBuffer();
    }

    // Must set limit before position or an exception may be thrown.
    sbb.limit(byteOffset + offset + length);
    sbb.position(byteOffset + offset);

    return sbb;
  }

  /**
   * Initializes a block for use.
   *
   * @param blockFile The file that this data block is from.
   * @param blockSize The size of the data.
   * @param blockId The ID of the block in the file.
   * @param byteOffset The offset of the data for this block, from the start of the buffer.
   * @param bb The buffer containing the data.
   * @param sbb A cached read-only buffer, useful to use as a source for writing operations
   *            which require a buffer of data.
   * @param ib The 32 bit integer offset of the data for this block, from the start of the buffer.
   * @param lb The 64 bit long offset of the data for this block, from the start of the buffer.
   */
  private void init(
      BlockFile blockFile, int blockSize,
      long blockId, int byteOffset, ByteBuffer bb, ByteBuffer sbb,
      IntBuffer ib, LongBuffer lb
  ) {
    this.blockFile = blockFile;
    this.blockSize = blockSize;
    init(blockId, byteOffset, bb, sbb, ib, lb);
  }

  /**
   * Initializes a block for use, not setting up any of the data buffer, but still pointing to
   * the file for the block..
   *
   * @param blockFile The file that data block will come from.
   * @param blockId The ID of the block within the file.
   */
  void init(BlockFile blockFile, long blockId) {
    assert ownsBuffer;
    this.blockFile = blockFile;
    this.blockId = blockId;
  }

}
