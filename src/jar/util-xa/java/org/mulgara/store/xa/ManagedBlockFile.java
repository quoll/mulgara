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
import org.apache.log4j.Logger;
import org.mulgara.util.StackTrace;

/**
 * A class that implements efficient copy-on-write semantics for a BlockFile.
 * This is the lowest level of the phased writing implementation, and wraps
 * the implementations of {@link AbstractBlockFile}.
 *
 * @created 2001-09-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.11 $
 *
 * @modified $Date: 2005/07/05 04:23:54 $
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
public final class ManagedBlockFile {

  /** Logger. */
  private final static Logger logger = Logger.getLogger(ManagedBlockFile.class);

  /** File extension for the FreeList file. */
  private final static String FREELIST_EXT = "_fl";

  /** The name of the BlockFile. */
  private File file;

  /** The open BlockFile. */
  private BlockFile blockFile;

  /**
   * The multi-phase FreeList used for allocating and freeing blocks in the
   * BlockFile.
   */
  private FreeList freeList;

  /** The current (writable) phase.  No other phase is writable. */
  private Phase currentPhase;

  /** True if the ManagedBlockFile has not been closed. */
  private boolean isOpen;


  /**
   * Constructs a ManagedBlockFile with the specified file.
   *
   * @param file the name of the BlockFile.
   * @param blockSize the block size in bytes.
   * @param ioType the type of I/O mechanism to use.
   * @throws IOException if an I/O error occurs.
   */
  public ManagedBlockFile(File file, int blockSize,BlockFile.IOType ioType) throws IOException {
    this.file = file;
    File freeListFile = new File(file + FREELIST_EXT);

    if (file.exists() != freeListFile.exists()) {
      logger.error("ERROR: inconsistency between Block file and Free List file");
    }

    blockFile = AbstractBlockFile.openBlockFile(file, blockSize, ioType);
    freeList = FreeList.openFreeList(freeListFile);
    isOpen = true;
  }

  /**
   * Constructs a ManagedBlockFile with the specified file name.
   *
   * @param fileName the name of the BlockFile.
   * @param blockSize the block size in bytes.
   * @param ioType the type of I/O mechanism to use.
   * @throws IOException if an I/O error occurs.
   */
  public ManagedBlockFile(String fileName, int blockSize, BlockFile.IOType ioType) throws IOException {
    this(new File(fileName), blockSize, ioType);
  }

  /**
   * Truncates the file to zero length.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void clear() throws IOException {
    blockFile.clear();
    freeList.clear();
  }

  /**
   * Ensures that all data for this BlockFile is stored in persistent storage
   * before returning.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void force() throws IOException {
    blockFile.force();
    freeList.force();
  }

  /**
   * Nullifies references to MappedByteBuffers.
   */
  public void unmap() {
    if (blockFile != null) {
      blockFile.unmap();
    }

    if (freeList != null) {
      freeList.unmap();
    }
  }

  /**
   * Closes the block file.
   *
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void close() throws IOException {
    close(false);
  }

  /**
   * Closes and deletes the block file.
   *
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void delete() throws IOException {
    try {
      close(true);
    } finally {
      blockFile = null;
      freeList = null;
    }
  }

  /**
   * Closes the block file.
   *
   * @param deleteFiles true if both the block file and the free list file
   * should be deleted after they are closed.
   * @throws IOException if an I/O error occurs.
   */
  private void close(boolean deleteFiles) throws IOException {
    boolean success = false;
    try {
      if (blockFile != null) {
        try {
          if (
              !deleteFiles && isOpen &&
              freeList != null && currentPhase != null
          ) {
            long blockNr = freeList.getNextItem();
            long currentBlocks = blockFile.getNrBlocks();
            if (currentBlocks < blockNr) {
              logger.error("Block file smaller than it should be. Currently: " + currentBlocks + ". Should be >" + blockNr);
            } else {
              blockFile.setNrBlocks(blockNr);
            }
          }
        } finally {
          isOpen = false;

          if (deleteFiles) {
            blockFile.delete();
          } else {
            blockFile.close();
          }
        }
      }
      success = true;
    } finally {
      try {
        if (freeList != null) {
          if (deleteFiles) {
            freeList.delete();
          } else {
            freeList.close();
          }
        }
      } catch (IOException e) {
        if (success) throw e; // Worked up to now; re-throw this exception.
        else logger.info("Suppressing I/O exception while closing failed resource", e); // Something else already failed.
      }
    }
  }


  /**
   * Provides phased access to a {@link BlockFile}.  These phases are instantiated directly
   * by the object needing file access.
   */
  public final class Phase implements BlockFile, PersistableMetaRoot {

    /** The size of a free-list record. */
    public final static int RECORD_SIZE = FreeList.Phase.RECORD_SIZE;

    /** The phase for the free list associated with this phase. */
    private FreeList.Phase freeListPhase;

    /**
     * Creates a new writing phase for this ManagedBlockFile.
     *
     * @throws IOException If an I/O error occurred on the free list.
     */
    public Phase() throws IOException {
      freeListPhase = freeList.new Phase();
      currentPhase = this;
    }

    /**
     * Creates a new phase, duplicating another existing phase.
     *
     * @throws IOException If an I/O error occurred on the free list.
     */
    public Phase(Phase p) throws IOException {
      assert p != null;
      freeListPhase = freeList.new Phase(p.freeListPhase);
      currentPhase = this;
    }

    /**
     * Creates a new phase, using a freelist block.
     *
     * @param b The block in the freelist to start the new phase in.
     * @param offset The offset in the block to find the start of the freelist block.
     * @throws IOException If an I/O error occurred on the free list.
     */
    public Phase(Block b, int offset) throws IOException {
      freeListPhase = freeList.new Phase(b, offset);
      currentPhase = this;
      check();
    }

    /**
     * Indicates if there are any remaining references to the current phase.
     *
     * @return <code>true</code> if the phase on the free list is still being referenced.
     */
    public boolean isInUse() {
      return freeListPhase.isInUse();
    }

    /**
     * Writes this PersistableMetaRoot to the specified Block. The ints are
     * written at the specified offset.
     *
     * @param b the Block.
     * @param offset writes the free list block data at this offset in the block.
     */
    public void writeToBlock(Block b, int offset) {
      freeListPhase.writeToBlock(b, offset);
    }

    public Token use() {
      return new Token();
    }

    /**
     * Establishes that the structure of the blocks is what it should be.
     *
     * @throws Error When the file is shorter than the fields in the file indicate.
     */
    private void check() throws IOException {
      // Initial phase.
      // Check that the block file has the correct size.
      long nrValidBlocks = freeList.getNextItem();
      long nrBlocks = getNrBlocks();

      if (nrBlocks != nrValidBlocks) {
        if (nrBlocks < nrValidBlocks) {
          logger.error("File " + file + " may have been truncated.");
          throw new Error("File " + file + " may have been truncated.");
        }

        if (logger.isInfoEnabled()) {
          logger.info("File " + file +
              " may not have been closed properly on shutdown.\n  nrBlocks=" + nrBlocks +
              "  nrValidBlocks=" + nrValidBlocks + "\n" + new StackTrace());
        }
        blockFile.setNrBlocks(nrValidBlocks);
      }
    }

    /**
     * Sets the length of the file in blocks. Not supported.
     *
     * @param nrBlocks the length of the file in blocks.
     * @throws UnsupportedOperationException is always thrown.
     */
    public void setNrBlocks(long nrBlocks) {
      throw new UnsupportedOperationException(
          "Cannot change number of blocks on a ManagedBlockFile.");
    }

    /**
     * Gets the current length of the BlockFile in blocks. An implementation
     * may allow the actual disk file to be larger or smaller than this size
     * while it is open but will set the file to this size when it is closed.
     *
     * @return the current length of the BlockFile in blocks.
     */
    public long getNrBlocks() {
      return blockFile.getNrBlocks();
    }

    /**
     * Truncates the file to zero length.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void clear() throws IOException {
      throw new UnsupportedOperationException(
          "ManagedBlockFile.Phase.clear() not supported.  " +
          "Call ManagedBlockFile.clear() instead.");
    }

    /**
     * Ensures that all data for this BlockFile is stored in persistent storage
     * before returning.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void force() throws IOException {
      throw new UnsupportedOperationException(
          "ManagedBlockFile.Phase.force() not supported.  " +
          "Call ManagedBlockFile.force() instead.");
    }

    /**
     * Allocates a ByteBuffer to be used for writing to the specified block.
     * The contents of the ByteBuffer are undefined. The method {@link
     * #writeBlock} should be called to write the buffer to the block but,
     * depending on the BlockFile implementation, changes to the ByteBuffer may
     * take effect even if {@link #writeBlock} is never called.
     *
     * @param blockId The ID of the block that this buffer will be written to.
     * @return a ByteBuffer to be used for writing to the specified block.
     */
    public Block allocateBlock(long blockId) {
      throw new UnsupportedOperationException("Cannot allocate a specific block of a ManagedBlockFile.");
    }

    /**
     * Allocates a ByteBuffer which is filled with the contents of the
     * specified block. If the buffer is modified then the method {@link
     * #writeBlock} should be called to write the buffer back to the file but,
     * depending on the BlockFile implementation, changes to the ByteBuffer may
     * take effect even if {@link #writeBlock} is never called.
     *
     * @param blockId the block to read into the ByteBuffer.
     * @return The read block.
     * @throws IOException if an I/O error occurs.
     */
    public Block readBlock(long blockId) throws IOException {
      Block block = blockFile.readBlock(blockId);
      block.setBlockFile(this);
      return block;
    }

    /**
     * Writes a buffer that was allocated by calling either {@link
     * #allocateBlock} or {@link #readBlock} to the specified block. The buffer
     * may only be written to the same block as was specified when the buffer
     * was allocated.
     *
     * @param block the buffer to write to the file.
     * @throws IOException if an I/O error occurs.
     */
    public void writeBlock(Block block) throws IOException {
      assert this == currentPhase;
      blockFile.writeBlock(block);
    }

    /**
     * Changes the block ID of the specified Block. This method is called
     * copyBlock because a call to copyBlock() followed by a call to
     * writeBlock() can be used to copy the contents of a block to a new
     * location in the block file.
     *
     * @param block the Block to be copied.
     * @param dstBlockId the ID of the block to which the Block will be written
     *      when writeBlock() is called.
     * @throws IOException if an I/O error occurs.
     */
    public void copyBlock(Block block, long dstBlockId) throws IOException {
      assert this == currentPhase;
      assert block.getBlockId() != dstBlockId;

      if (freeList.isSharedItem(dstBlockId)) {
        // Choose an alternative destination block since this one is shared
        // with another phase.
        freeList.free(dstBlockId);
        dstBlockId = freeList.allocate();
        blockFile.setNrBlocks(freeList.getNextItem());
      }

      blockFile.copyBlock(block, dstBlockId);
    }

    /**
     * ManagedBlockFile effectively re-uses in the read
     */
    public Block recycleBlock(long blockId, Block block) throws IOException {
      return readBlock(blockId);
    }
 
    /**
     * Used to unmap a file.  This is supported on the enclosing class rather than here.
     */
    public void unmap() {
      throw new UnsupportedOperationException(
          "ManagedBlockFile.Phase.unmap() not supported.  " +
          "Call ManagedBlockFile.unmap() instead.");
    }

    /**
     * Closes the block file.
     */
    public void close() throws IOException {
      throw new UnsupportedOperationException(
          "ManagedBlockFile.Phase.close() not supported.  " +
          "Call ManagedBlockFile.close() instead.");
    }

    /**
     * Closes and deletes the block file.
     */
    public void delete() throws IOException {
      throw new UnsupportedOperationException(
          "ManagedBlockFile.Phase.delete() not supported.  " +
          "Call ManagedBlockFile.delete() instead.");
    }

    /**
     * Allocates a ByteBuffer to be used for writing to the specified block. The
     * contents of the ByteBuffer are undefined. The method {@link #writeBlock}
     * should be called to write the buffer to the block but, depending on the
     * BlockFile implementation, changes to the ByteBuffer may take effect even if
     * {@link #writeBlock} is never called.
     *
     * @return a ByteBuffer to be used for writing to the specified block.
     * @throws IOException if an I/O error occurs.
     */
    public Block allocateBlock() throws IOException {
      assert this == currentPhase;

      long blockId = freeList.allocate();

      blockFile.setNrBlocks(freeList.getNextItem());
      Block block = blockFile.allocateBlock(blockId);
      block.setBlockFile(this);
      return block;
    }

    /**
     * Gets a new block from the file to write block data to.
     * The block data to write is a modified version of an existing block.
     *
     * @param block The block to be copied into the new "modified" block.
     * @throws IOException if an I/O error occurs.
     */
    public void modifyBlock(Block block) throws IOException {
      assert this == currentPhase;

      if (freeList.isSharedItem(block.getBlockId())) {
        // Allocate a new block and copy block to it.
        long newBlockId = freeList.allocate();
        blockFile.setNrBlocks(freeList.getNextItem());

        // Although we use block after the block is freed, the block can't
        // be reused (by a later phase) until the current phase is committed.
        freeList.free(block.getBlockId());

        blockFile.copyBlock(block, newBlockId);
      }
    }

    /**
     * Release a block ID back into the free list.
     *
     * @param blockId The ID of the block to release.
     * @throws IOException if an I/O error occurs.
     */
    public void freeBlock(long blockId) throws IOException {
      assert this == currentPhase;
      freeList.free(blockId);
    }


    /**
     * Instances of this class represent a reference to the enclosing phase.
     */
    public final class Token {

      /** A reference to the underlying token for the free list phase. */
      private FreeList.Phase.Token freeListToken;

      /**
       * Create a new token for the encapsulating phase.
       */
      Token() {
        freeListToken = freeListPhase.use();
      }

      /**
       * Get the encapsulating phase.
       *
       * @return The encapsulating phase.
       */
      public Phase getPhase() {
        assert freeListToken != null : "Invalid Token";
        return Phase.this;
      }

      /**
       * Release this reference to the encapsulating phase.
       */
      public void release() {
        assert freeListToken != null : "Invalid Token";
        freeListToken.release();
        freeListToken = null;
      }

    }

  }

}
