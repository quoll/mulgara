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

// Java 2 standard packages
import java.lang.ref.*;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;
import org.mulgara.util.IntFile;
import org.mulgara.util.StackTrace;
import org.mulgara.util.functional.C;

/**
 * A fifo of integer items. A list of "phases" is maintained where each phase
 * represents a snapshot of the state of the free list at a point in time. Items
 * added to the free list will not be returned by {@link #allocate} until all
 * current phases have been closed (and at least one new phase created).
 *
 * @created 2001-09-20
 *
 * @author Paul Gearon
 * @author David Makepeace
 *
 * @version $Revision: 1.13 $
 *
 * @modified $Date: 2005/07/21 19:13:48 $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001-2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class FreeList {

  /** Logger. */
  private final static Logger logger = Logger.getLogger(FreeList.class);

  /** The name extension of the file used for the freelist. */
  private final static String INTFILE_EXT = "_ph";

  /**
   * The size of a block of free items in bytes. The free list file is always a
   * multiple of this size.
   */
  private final static int BLOCK_SIZE_B = 32768;

  /** The size of a block of free items in longs. This includes the header. */
  private final static int BLOCK_SIZE = BLOCK_SIZE_B / 8;

  /** The default IOType to use for the BlockFile. */
  private final static BlockFile.IOType DEF_IO_TYPE = BlockFile.IOType.EXPLICIT;

  /**
   * The index of the pointer to the next block of free items. Blocks of free
   * items are chained into a doubly-linked circular list.
   */
  private final static int IDX_NEXT = 0;

  /**
   * The index of the pointer to the previous block of free items. Blocks of
   * free items are chained into a doubly-linked circular list.
   */
  private final static int IDX_PREV = 1;

  /** The size of the block header in ints. */
  private final static int HEADER_SIZE_I = 2;

  /** The size of the block header in longs. */
  private final static int HEADER_SIZE = (HEADER_SIZE_I + 1) / 2;

  /** The free list file. */
  private File file;

  /** The free list BlockFile. */
  private BlockFile blockFile;

  /** A persistent map of items to their corresponding phases. */
  private IntFile itemToPhaseSeqMap;

  /**
   * This is used to prevent items that are still in use from being returned by
   * {@link #allocate}. Items between firstHead and {@link Phase#head} are
   * reserved. This is a copy of the head of the first Phase in the {@link
   * #phases} list.
   */
  private long firstHead;

  /**
   * <code>true</code> if {@link #headBlock} has been modified.
   */
  private boolean headBlockDirty = false;

  /**
   * This buffer used for reading and writing the block which contains the item
   * pointed to by {@link Phase#head}.
   */
  private Block headBlock = null;

  /**
   * The pointer to the first item which is not a possible candidate for
   * reallocation. Items between firstFree (inclusive) and reallocate
   * (noninclusive) are possible candidates for reallocation.
   */
  private long reallocate;

  /**
   * The pointer to the start of the list of items in the free list which were
   * freed during the current phase.
   */
  private long firstFree;

  /** Set to <code>true</code> if the {@link #reallocateBlock} is dirty. */
  private boolean reallocateBlockDirty = false;

  /** The buffer used for reading and writing the block containing the {@link #reallocate} item. */
  private Block reallocateBlock = null;

  /**
   * This is used to protect items in the free list from being reused as the
   * list grows so that the state can be safely rolled back to a previous phase
   * after a crash. Items between firstTail and {@link Phase#tail} can be
   * returned by {@link #allocate} but they are protected from being overwritten
   * until the phases which contain them are closed.
   */
  private long firstTail;

  /**
   * This buffer used for reading and writing the block which contains the item
   * pointed to by {@link Phase#tail}.
   */
  private Block tailBlock = null;

  /** The list of Phases from oldest to newest. */
  private LinkedList<Phase> phases = new LinkedList<Phase>();

  /** The newest (writing) phase. */
  private Phase currentPhase = null;

  /**
   * The index of the next block to insert into the chain of blocks that
   * constitute the free list. This is the index of the block after the last
   * block in the file.
   */
  private int nextBlockId;

  /**
   * Constructs a FreeList which uses the specified file (if it exists) or
   * creates a new file (if it doesn't already exist).
   *
   * @param file the file.
   * @param ioType the IOType to use for the BlockFile.
   * @throws IOException if an I/O error occurs.
   */
  private FreeList(File file, BlockFile.IOType ioType) throws IOException {
    this.file = file;
    blockFile = AbstractBlockFile.openBlockFile(file, BLOCK_SIZE_B, ioType);
    itemToPhaseSeqMap = IntFile.open(file + INTFILE_EXT);
    itemToPhaseSeqMap.clear();
  }

  /**
   * Factory method for a FreeList instance which uses the specified file
   * (if it exists) or creates a new file (if it doesn't already exist).
   *
   * @param file the file.
   * @param ioType the IOType to use for the BlockFile.
   * @return The new FreeList instance.
   * @throws IOException if an I/O error occurs.
   */
  public static FreeList openFreeList(File file, BlockFile.IOType ioType) throws IOException {
    return new FreeList(file, ioType);
  }

  /**
   * Creates a FreeList instance which uses the specified file (if it exists) or
   * creates a new file (if it doesn't already exist).  Uses the default IO file type.
   *
   * @param file the file.
   * @return The new FreeList instance.
   * @throws IOException if an I/O error occurs.
   */
  public static FreeList openFreeList(File file) throws IOException {
    return openFreeList(file, DEF_IO_TYPE);
  }

  /**
   * Creates a FreeList instance which uses the file with the specified file
   * name (if the file exists) or creates a new file (if it doesn't already
   * exist).
   *
   * @param fileName the file name of the file.
   * @return The new FreeList instance.
   * @throws IOException if an I/O error occurs.
   */
  public static FreeList openFreeList(String fileName) throws IOException {
    return openFreeList(new File(fileName));
  }

  /**
   * Initializes a FreeList block file or repairs the back-pointers in
   * an existing file.  This is called from Phase.init(llll).
   */
  private void initFreeListFile(boolean clear) throws IOException {
    if (clear || blockFile.getNrBlocks() < 2) {
      // File is too small to be valid.  Reset to two blocks.
      nextBlockId = 2;
      blockFile.setNrBlocks(nextBlockId);
      allocateHeadBlock(0);
      headBlock.putInt(IDX_NEXT, 1);
      headBlock.putInt(IDX_PREV, 1);
      headBlockDirty = true;
      allocateHeadBlock(1);
      headBlock.putInt(IDX_NEXT, 0);
      headBlock.putInt(IDX_PREV, 0);
      headBlockDirty = true;
      force();
    } else {
      nextBlockId = findMaxBlockId() + 1;
      blockFile.setNrBlocks(nextBlockId);
    }
  }

  /**
   * The next item to allocate if the free list is empty or there are no items
   * between {@link Phase#tail} and {@link #firstHead}. This is also one
   * greater than the largest item in use.
   *
   * @return the next item to allocate if the free list is empty or there are
   * no items between {@link Phase#tail} and {@link #firstHead}.
   */
  public long getNextItem() {
    return currentPhase.getNextItem();
  }

  /**
   * Gets the SharedItem attribute of the FreeList object.  This indicates that
   * the item appears in more than one phase.
   *
   * @param item An item in the free list.
   * @return The SharedItem value
   */
  public synchronized boolean isSharedItem(long item) {
    return itemToPhaseSeqMap.getInt(item) != currentPhase.getSequenceNumber();
  }

  /**
   * Truncates the free list file to a single block. This should only be called
   * immediately after constructing the free list and adding a single
   * initialized phase.
   *
   * @throws IOException if an I/O error occurs.
   * @throws IllegalStateException if it is inappropriate to call clear because
   *      the free list hasn't been correctly initialized.
   */
  public synchronized void clear() throws IOException {
    if ( (phases.size() != 1) ||
        (currentPhase.getHead() != HEADER_SIZE) ||
        (currentPhase.getTail() != HEADER_SIZE)) {
      throw new IllegalStateException(
          "FreeList does not have a single initialized phase."
      );
    }

    releaseTailBlock();
    releaseReallocateBlock();
    releaseHeadBlock();
    nextBlockId = 2;
    blockFile.setNrBlocks(nextBlockId);
    allocateHeadBlock(0);
    headBlock.putInt(IDX_NEXT, 1);
    headBlock.putInt(IDX_PREV, 1);
    headBlockDirty = true;
    allocateHeadBlock(1);
    headBlock.putInt(IDX_NEXT, 0);
    headBlock.putInt(IDX_PREV, 0);
    headBlockDirty = true;
    force();
  }

  /**
   * Releases all mapped resources for the file, allowing the VM to unmap the file.
   * No operations are possible after calling this method without first
   * reinitializing the object.
   */
  public synchronized void unmap() {
    if (itemToPhaseSeqMap != null) {
      itemToPhaseSeqMap.unmap();
    }

    if (blockFile != null) {
      try {
        releaseHeadBlock();
        releaseReallocateBlock();
        releaseTailBlock();
      } catch (IOException ex) {
        logger.warn("IOException in unmap()", ex);
      }

      blockFile.unmap();
    }
  }

  /**
   * Closes the file.
   *
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void close() throws IOException {
    try {
      unmap();
    } finally {
      IOException ex = null;
      try {
        if (itemToPhaseSeqMap != null) itemToPhaseSeqMap.delete();
      } catch (IOException e) {
        ex = e;
      } finally {
        itemToPhaseSeqMap = null;
      }
      try {
        blockFile.close();
      } catch (IOException e) {
        if (ex == null) ex = e;
        else logger.info("Suppressing exception deleting file for failed FreeList", e);
      }
      if (ex != null) throw ex;
    }
  }

  /**
   * Closes and deletes the file.
   *
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void delete() throws IOException {
    try {
      unmap();
    } finally {
      IOException ex = null;
      try {
        if (itemToPhaseSeqMap != null) itemToPhaseSeqMap.delete();
      } catch (IOException e) {
        ex = e;
      } finally {
        itemToPhaseSeqMap = null;
      }
      try {
        blockFile.delete();
      } catch (IOException e) {
        if (ex == null) ex = e;
        else logger.info("Suppressing exception deleting file for failed FreeList", e);
      }
      if (ex != null) throw ex;
    }
  }

  /**
   * Ensures that all data associated with the free list has been written to
   * persistent storage.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void force() throws IOException {
    writeHeadBlock();
    writeReallocateBlock();
    blockFile.force();
    itemToPhaseSeqMap.force();
  }

  /**
   * Adds an item to the free list.
   *
   * @param item the item to add to the free list.
   * @throws IOException if an I/O error occurs.
   * @throws IllegalStateException if there are no phases.
   */
  public synchronized void free(long item) throws IOException {
    removeClosedPhases();

    if (currentPhase == null) throw new IllegalStateException("FreeList has no phases.");

    if ( (item < 0) || (item >= currentPhase.getNextItem())) {
      throw new IllegalArgumentException("Trying to free item that was never allocated: " + item);
    }

    // if (!isValid(item)) throw new AssertionError("Attempt to free an invalid item: " + item);

    long head = currentPhase.getHead();
    readHeadBlock(getBlockId(head));

    int offset = getBlockOffset(head++);
    headBlock.putLong(offset, item);
    headBlockDirty = true;

    if (!isSharedItem(item)) reallocate = head;

    if (getBlockOffset(head) == 0) {
      // Go to the next block.
      int nextHeadBlockId = headBlock.getInt(IDX_NEXT);

      if (nextHeadBlockId == getBlockId(firstTail)) {
        // Move the current head buffer aside so that we can update the next
        // pointer later.
        Block prevHeadBlock = headBlock;
        headBlock = null;
        headBlockDirty = false;

        // Allocate new buffer at the end of the file and set its next pointer
        // to point to the block which contains firstTail.
        int newHeadBlockId = nextBlockId++;
        blockFile.setNrBlocks(nextBlockId);
        allocateHeadBlock(newHeadBlockId);
        headBlock.putInt(IDX_NEXT, nextHeadBlockId);
        headBlock.putInt(IDX_PREV, (int) prevHeadBlock.getBlockId());
        headBlockDirty = true;
        force();

        // Update the next pointer of the block which contained head to point
        // to the new block.
        prevHeadBlock.putInt(IDX_NEXT, newHeadBlockId);
        prevHeadBlock.write();

        // Update the prev pointer of the next block to point back to the new
        // block.
        Block nextHeadBlock = findBlock(nextHeadBlockId);

        if (nextHeadBlock == null) {
          nextHeadBlock = blockFile.readBlock(nextHeadBlockId);
        }

        nextHeadBlock.putInt(IDX_PREV, newHeadBlockId);
        nextHeadBlock.write();

        nextHeadBlockId = newHeadBlockId;
      }

      head = ((long)nextHeadBlockId * BLOCK_SIZE) + HEADER_SIZE;
    }

    if (phases.size() == 1) firstHead = head;

    currentPhase.setHead(head);
    currentPhase.decNrValidItems();
  }

  /**
   * Allocates an item from the free list or, if there are no available items,
   * allocates a new item with the value of nextItem and increments nextItem.
   *
   * @return the allocated item.
   * @throws IOException if an I/O error occurs.
   * @throws IllegalStateException if there are no phases.
   */
  public synchronized long allocate() throws IOException {
    removeClosedPhases();

    if (currentPhase == null) throw new IllegalStateException("FreeList has no phases.");

    long item;

    if (phases.size() > 1) {
      // First try to reallocate an item that was freed during the current
      // phase.  Only items that were originally allocated during the current
      // phase (and then freed) can be reallocated.
      long head = currentPhase.getHead();

      while (reallocate != firstFree) {
        if (getBlockOffset(reallocate) == HEADER_SIZE) {
          // Change reallocate to point to last item of previous block.
          readReallocateBlock(getBlockId(reallocate));

          int blockId = reallocateBlock.getInt(IDX_PREV);
          reallocate = ((blockId * BLOCK_SIZE) + BLOCK_SIZE) - 1;
        } else {
          --reallocate;
        }

        // Fetch the candidate item.
        readReallocateBlock(getBlockId(reallocate));

        int offset = getBlockOffset(reallocate);
        item = reallocateBlock.getLong(offset);

        // Return the item if it was originally allocated during the current
        // phase.
        if (!isSharedItem(item)) {
          // Remove this item by moving the last free item to this item's
          // position.
          if (getBlockOffset(head) == HEADER_SIZE) {
            readHeadBlock(getBlockId(head));

            int blockId = headBlock.getInt(IDX_PREV);
            head = ( ( (long) blockId * BLOCK_SIZE) + BLOCK_SIZE) - 1;
          } else {
            --head;
          }

          currentPhase.setHead(head);

          if (reallocate != head) {
            readHeadBlock(getBlockId(head));

            long tItem = headBlock.getLong(getBlockOffset(head));
            readReallocateBlock(getBlockId(reallocate));
            reallocateBlock.putLong(getBlockOffset(reallocate), tItem);
            reallocateBlockDirty = true;
          }

          currentPhase.incNrValidItems();

          return item;
        }
      }

      // Don't search this part of the free list again.
      reallocate = head;
      firstFree = head;
    }

    long tail = currentPhase.getTail();

    if (tail == firstHead) {
      // No available free items on the free list.
      long nextItem = currentPhase.getNextItem();
      item = nextItem++;
      currentPhase.setNextItem(nextItem);
    } else {
      readTailBlock(getBlockId(tail));

      int offset = getBlockOffset(tail++);
      item = tailBlock.getLong(offset);

      if (getBlockOffset(tail) == 0) {
        // Go to the next block.
        int blockId = tailBlock.getInt(IDX_NEXT);
        tail = ( (long) blockId * BLOCK_SIZE) + HEADER_SIZE;
      }

      if (phases.size() == 1) firstTail = tail;

      currentPhase.setTail(tail);
    }

    itemToPhaseSeqMap.putInt(item, currentPhase.getSequenceNumber());
    currentPhase.incNrValidItems();

    return item;
  }

  /**
   * Gets the Valid attribute of the FreeList object.
   * Checks if the item is out of the possible range for nodes, which would
   * make it invalid.  Then it checks the current free list.  If the item
   * is in the free list, then it is invalid.  If the item cannot be found
   * in the free list then it must still be in use, and therefor valid.
   *
   * @param item The item to check.
   * @return <code>true</code> if the item is valid..
   */
  public synchronized boolean isValid(long item) {
    removeClosedPhases();

    if (currentPhase == null) throw new IllegalStateException("FreeList has no phases.");

    if ( (item < 0) || (item >= currentPhase.getNextItem())) return false;

    long index = currentPhase.getTail();
    long head = currentPhase.getHead();

    try {
      while (index != head) {
        readTailBlock(getBlockId(index));

        int offset = getBlockOffset(index++);
        long blockItem;
        blockItem = tailBlock.getLong(offset);

        if (item == blockItem) return false;

        if (getBlockOffset(index) == 0) {
          // Go to the next block.
          int blockId = tailBlock.getInt(IDX_NEXT);
          index = ( (long) blockId * BLOCK_SIZE) + HEADER_SIZE;
        }
      }
    } catch (IOException ex) {
      throw new Error(ex);
    }

    return true;
  }

  /**
   * Returns the ID of the block which contains the item with the specified
   * index.
   *
   * @param index the index of the item.
   * @return the block ID.
   */
  private int getBlockId(long index) {
    return (int)(index / BLOCK_SIZE);
  }

  /**
   * Returns the offset (in ints) of the item with the specified index within
   * the block that contains it.
   *
   * @param index the index of the item.
   * @return the offset (in ints).
   */
  private int getBlockOffset(long index) {
    return (int)(index % BLOCK_SIZE);
  }

  /**
   * Follows the chain of free list blocks and returns the highest block ID
   * encountered.
   *
   * @return the highest block ID.
   * @throws IOException if an I/O error occurs.
   */
  private int findMaxBlockId() throws IOException {
    int maxBlockId = 0;
    int nextBlockId = 0;
    int prevBlockId = -1;

    do {
      Block block = blockFile.readBlock(nextBlockId);

      if ( (prevBlockId >= 0) && (block.getInt(IDX_PREV) != prevBlockId)) {
        // Bad previous block pointer.  Fix it.
        logger.warn("Free list back pointer does not agree with forward pointer.  Fixed.");
        block.putInt(IDX_PREV, prevBlockId);
        block.write();
      }

      prevBlockId = nextBlockId;
      nextBlockId = block.getInt(IDX_NEXT);

      if (nextBlockId > maxBlockId) maxBlockId = nextBlockId;

    } while (nextBlockId != 0);

    if (prevBlockId < 0) throw new AssertionError("prevBlockId is negative.");

    // Check the previous block pointer in block 0.
    Block block = blockFile.readBlock(0);

    if (block.getInt(IDX_PREV) != prevBlockId) {
      // Bad previous block pointer.  Fix it.
      logger.warn("Free list back pointer does not agree with forward pointer.  Fixed.");
      block.putInt(IDX_PREV, prevBlockId);
      block.write();
    }

    return maxBlockId;
  }

  /**
   * Removes any unused phases from the phase list.
   *
   * {@link #firstHead} and {@link #firstTail} will
   * be updated to the oldest phase still in use.
   */
  private void removeClosedPhases() {
    // Caller must be synchronized.
    assert Thread.holdsLock(this);

    Phase phase = phases.getFirst();

    while (!phase.isInUse() && (phases.size() > 1)) {
      phases.removeFirst();
      phase = phases.getFirst();

      firstHead = phase.getHead();
      firstTail = phase.getTail();
    }
  }

  /**
   * Allocates a buffer from blockFile to hold the head block. If the current
   * head buffer already holds the head block then nothing is done. If the head
   * buffer contains a different block then it is released first (writing the
   * head block to the file).  This method uses a previous version of the
   * block, unless no block with that ID exists yet, in which case a new block
   * is read in, or allocated if it does not exist in the file either.  If the
   * block existed in the file, then its contents will be ignored.  Contrast
   * this to {@link #readHeadBlock} where the contents of the block on the disk
   * are always read.
   *
   * @param blockId The block number of the head block.
   * @throws IOException if an I/O error occurs.
   */
  private void allocateHeadBlock(int blockId) throws IOException {
    if (headBlock != null) {
      if (blockId == (int) headBlock.getBlockId()) {
        headBlockDirty = false;
        return;
      }
      releaseHeadBlock();
    }

    headBlock = findBlock(blockId);
    if (headBlock == null) headBlock = blockFile.allocateBlock(blockId);
    headBlockDirty = false;
  }

  /**
   * Reads the head block from blockFile. If the current head buffer already
   * holds the head block then nothing is done. If the head buffer contains a
   * different block then it is freed first (writing it to the file).  This is
   * similar to {@link #allocateHeadBlock} only it will not allocate a new
   * block from the file, and always reads the contents of the existing block
   * in the file.
   *
   * @param blockId The block number of the head block.
   * @throws IOException if an I/O error occurs.
   */
  private void readHeadBlock(int blockId) throws IOException {
    if (headBlock != null) {
      if (blockId == (int) headBlock.getBlockId()) return;
      releaseHeadBlock();
    }

    headBlock = findBlock(blockId);
    if (headBlock == null) headBlock = blockFile.readBlock(blockId);
    headBlockDirty = false;
  }

  /**
   * Writes the current head buffer back to the block file. The write is only
   * done if the buffer is valid and is dirty. The dirty flag will be set to
   * false at the end of this operation.
   *
   * @throws IOException if an I/O error occurs.
   */
  private void writeHeadBlock() throws IOException {
    if ( (headBlock != null) && headBlockDirty) {
      headBlock.write();
      headBlockDirty = false;
    }
  }

  /**
   * Releases the current head buffer. After releasing, the head buffer is made
   * invalid. If the buffer is dirty and valid then it is written to the
   * blockFile first.
   *
   * @throws IOException if an I/O error occurs.
   */
  private void releaseHeadBlock() throws IOException {
    if (headBlock != null) {
      if (headBlockDirty) writeHeadBlock();
      headBlock = null;
    }
  }

  /**
   * Reads the reallocate block from blockFile. If the current reallocate buffer
   * already holds the reallocate block then nothing is done. If the reallocate
   * buffer contains a different block then it is released first.
   *
   * @param blockId The block number of the reallocate block.
   * @throws IOException if an I/O error occurs.
   */
  private void readReallocateBlock(int blockId) throws IOException {
    if (reallocateBlock != null) {
      if (blockId == (int) reallocateBlock.getBlockId()) return;
      releaseReallocateBlock();
    }

    reallocateBlock = findBlock(blockId);
    if (reallocateBlock == null) reallocateBlock = blockFile.readBlock(blockId);
    reallocateBlockDirty = false;
  }

  /**
   * Writes the current reallocate buffer back to the block file. The write is
   * only done if the buffer is valid and is dirty. The dirty flag will be set
   * to false at the end of this operation.
   *
   * @throws IOException if an I/O error occurs.
   */
  private void writeReallocateBlock() throws IOException {
    if ((reallocateBlock != null) && reallocateBlockDirty) {
      reallocateBlock.write();
      reallocateBlockDirty = false;
    }
  }

  /**
   * Releases the current reallocate buffer. After releasing, the reallocate
   * buffer is made invalid. If the buffer is dirty and valid then it is written
   * to the blockFile first.
   *
   * @throws IOException if an I/O error occurs.
   */
  private void releaseReallocateBlock() throws IOException {
    if (reallocateBlock != null) {
      if (reallocateBlockDirty) writeReallocateBlock();
      reallocateBlock = null;
    }
  }

  /**
   * Reads the tail block from blockFile. If the current tail buffer already
   * holds the tail block then nothing is done. If the tail buffer contains a
   * different block then it is freed first.
   *
   * @param blockId The block number to use as the tail buffer.
   * @throws IOException if an I/O error occurs.
   */
  private void readTailBlock(int blockId) throws IOException {
    if (tailBlock != null) {
      if (blockId == (int) tailBlock.getBlockId()) return;
      releaseTailBlock();
    }

    tailBlock = findBlock(blockId);
    if (tailBlock == null) tailBlock = blockFile.readBlock(blockId);
  }

  /**
   * Releases the current tail buffer. After releasing, the tail buffer is made invalid.
   */
  private void releaseTailBlock() {
    if (tailBlock != null) tailBlock = null;
  }

  /**
   * Retrieve the block matching a given ID from one of the matching blocks.
   * This only finds blocks that are currently the head, tail, or reallocation
   * block.  Once a block is found, it gets written to disk and the reference
   * that was using that block is cleared, so there will be only a single
   * reference to the block.
   *
   * @param blockId ID of the block to retrieve.
   * @return The block requested.
   * @throws IOException If an I/O error occurs.
   */
  private Block findBlock(int blockId) throws IOException {
    if ( (headBlock != null) && ( (int) headBlock.getBlockId() == blockId)) {
      writeHeadBlock();

      Block block = headBlock;
      headBlock = null;
      headBlockDirty = false;

      return block;
    }

    if ((reallocateBlock != null) && ((int)reallocateBlock.getBlockId() == blockId)) {
      writeReallocateBlock();

      Block block = reallocateBlock;
      reallocateBlock = null;
      reallocateBlockDirty = false;

      return block;
    }

    if ( (tailBlock != null) && ( (int) tailBlock.getBlockId() == blockId)) {
      Block block = tailBlock;
      tailBlock = null;
      return block;
    }

    return null;
  }

  /**
   * This class represents the freed items in a single transaction phase.
   */
  public final class Phase implements PersistableMetaRoot {

    /** The size of an on-disk phase in longs. */
    public final static int RECORD_SIZE = 4;

    /** The index of the head pointer in the on-disk phase. */
    final static int IDX_HEAD = 0;

    /** The index of the tail pointer in the on-disk phase. */
    final static int IDX_TAIL = 1;

    /** The index of the next item field in the on-disk phase. */
    final static int IDX_NEXT_ITEM = 2;

    /** The index of the number of valid items field in the on-disk phase. */
    final static int IDX_NR_VALID_ITEMS = 3;

    /**
     * The number of the current transaction phase. Older phases have smaller
     * numbers.
     */
    private int sequenceNumber;

    /**
     * The head of the freed items for this phase. This moves as the phase frees
     * more items.  This value is an index into the free list file.
     */
    private long head;

    /**
     * The tail of the freed items for this phase.  This value is an index into
     * the free list file.
     */
    private long tail;

    /** The next item available for allocation for this phase. */
    private long nextItem;

    /**
     * The number of valid items in this phase.  This is the number of
     * allocated items that have not yet been freed.
     */
    private long nrValidItems;

    /**
     * The number of references to this phase.  The phase is no longer in use
     * when this is decremented to 0.
     */
    private int refCount = 0;

    /**
     * Reference to the token for this phase or <code>null</code> if there is no
     * current token.  This is collectable by the GC and can be used to indicate
     * that the phase is no longer in use.
     */
    private Reference<Token> tokenRef = null;


    /** Holds stack traces of use so we can tell where problems occurred. */
    private List<StackTrace> stack = null;


    /**
     * Default constructor. This creates a new phase as a duplicate of the most
     * recent phase, and then adds it to the phase list of the enclosing
     * FreeList object. If no previous phases exist, then this new phase will
     * have its head, tail and nextItem valules all appropriately initialized.
     *
     * @throws IOException if an I/O error occurs.
     */
    public Phase() throws IOException {
      synchronized (FreeList.this) {
        if (currentPhase != null) {
          removeClosedPhases();
          sequenceNumber = currentPhase.sequenceNumber + 1;
          head = currentPhase.head;
          tail = currentPhase.tail;
          nextItem = currentPhase.nextItem;
          nrValidItems = currentPhase.nrValidItems;
          init();
        } else {
          init(HEADER_SIZE, HEADER_SIZE, 0, 0);
        }
      }
    }

    /**
     * Constructor to build an empty transaction phase, which starts with a
     * given nextItem value. The enclosing FreeList object is initialized to use
     * this single phase.
     *
     * @param nextItem The starting point for new item allocations from this
     *      phase.
     * @throws IllegalStateException if the enclosing FreeList already has a
     *      phase, or is in an invalid state.
     * @throws IOException if an I/O error occurs.
     */
    public Phase(long nextItem) throws IOException {
      this(HEADER_SIZE, HEADER_SIZE, nextItem, 0);
    }

    /**
     * Constructor. This creates a new phase as a duplicate of the specified phase,
     * and then adds it to the phase list of the enclosing FreeList object.
     *
     * @throws IOException if an I/O error occurs.
     */
    public Phase(Phase p) throws IOException {
      synchronized (FreeList.this) {
        removeClosedPhases();

        int index = phases.indexOf(p);
        if (index == -1) throw new IllegalStateException("Attempt to rollback to closed phase.");

        while (phases.size() > (index + 1)) {
          Phase removedPhase = (Phase)phases.removeLast();
          assert removedPhase != p;
        }

        sequenceNumber = p.sequenceNumber + 1;
        head = p.head;
        tail = p.tail;
        nextItem = p.nextItem;
        nrValidItems = p.nrValidItems;
        init();

      }
    }

    /**
     * Constructor to build a transaction phase, initializing its internal
     * values with a given Block. The enclosing FreeList object is initialized
     * to use this single phase.
     *
     * @param b The Block with the initialization values for the phase, in the
     *      order: phase number; head; tail; nextItem.
     * @param offset The offset within the block to find the phase data.  This
            offset can be due to a header appearing before the freelist data.
     * @throws IllegalStateException if the enclosing FreeList already has a
     *      phase, or is in an invalid state.
     * @throws IOException if an I/O error occurs.
     */
    public Phase(Block b, int offset) throws IOException {
      this(
          b.getLong(offset + IDX_HEAD), b.getLong(offset + IDX_TAIL),
          b.getLong(offset + IDX_NEXT_ITEM),
          b.getLong(offset + IDX_NR_VALID_ITEMS)
      );
    }

    /**
     * Constructor to build a transaction phase, initializing its internal
     * values with given values. The enclosing FreeList object is initialized to
     * use this single phase.
     *
     * @param head The initial head of the freed items list. This will be
     *      updated as new items are freed in the phase.
     * @param tail The tail of the freed items list.
     * @param nextItem The next item to allocate for this phase.
     * @param nrValidItems The number of valid items. This is the number of
     *      items that have been allocated but not yet freed.
     * @throws IllegalStateException if the enclosing FreeList already has a
     *      phase, or is in an invalid state.
     * @throws IOException if an I/O error occurs.
     */
    private Phase(
        long head, long tail, long nextItem, long nrValidItems
    ) throws IOException {
      synchronized (FreeList.this) {
        if (currentPhase != null) {
          throw new IllegalStateException(
              "FreeList already has one initialized phase."
          );
        }

        init(head, tail, nextItem, nrValidItems);

        if (
            (getBlockId(head) >= nextBlockId) ||
            (getBlockId(tail) >= nextBlockId)
        ) {
          throw new IllegalStateException(
              "Free list file may have been truncated: " + file
          );
        }
      }
    }

    /**
     * Checks if this phase is in currently in use. If not then it may be
     * destroyed.
     *
     * @return true if this phase is in currently in use, false otherwise.
     */
    public synchronized boolean isInUse() {
      getToken();

      return refCount > 0;
    }

    /**
     * Gets the number of valid items in this phase.
     *
     * @return The number of valid items in this phase.
     */
    public long getNrValidItems() {
      return nrValidItems;
    }

    /**
     * Writes the details of the current phase into a block.
     *
     * @param b The block to put the details into.
     * @param offset The distance of the logical phase block from the beginning
     *      of <i>b</i>.
     */
    public void writeToBlock(Block b, int offset) {
      b.putLong(offset + IDX_HEAD, head);
      b.putLong(offset + IDX_TAIL, tail);
      b.putLong(offset + IDX_NEXT_ITEM, nextItem);
      b.putLong(offset + IDX_NR_VALID_ITEMS, nrValidItems);
    }

    /**
     * Indicate that this phase is in use.  Increments the reference count.
     *
     * @return A {@link Token} to represent the phase use.
     */
    public synchronized Token use() {
      Token token = getToken();

      if (token == null) {
        token = new Token();
        tokenRef = new WeakReference<Token>(token);
      }

      ++refCount;

      // record the stack if debug is enabled
      if (logger.isDebugEnabled()) {
        assert stack != null;
        stack.add(new StackTrace());
      }

      return token;
    }

    /**
     * Set a new head index within the file for this phase.
     *
     * @param head The new head index.
     */
    void setHead(long head) {
      this.head = head;
    }

    /**
     * Set a new tail index within the file for this phase.
     *
     * @param tail The new tail index.
     */
    void setTail(long tail) {
      this.tail = tail;
    }

    /**
     * Set the next unused item for this phase.
     *
     * @param nextItem the next item to allocate when the current
     *      phase's free list is empty..
     */
    void setNextItem(long nextItem) {
      this.nextItem = nextItem;
    }

    /**
     * Gets the sequence number for the current phase.  These start at zero
     * each time the Database is started.
     *
     * @return The sequence number for this phase.
     */
    int getSequenceNumber() {
      return sequenceNumber;
    }

    /**
     * Gets the index of the head within the file for this phase.
     *
     * @return The head index for this phase.
     */
    long getHead() {
      return head;
    }

    /**
     * Gets the index of the tail within the file for this phase.
     *
     * @return The tail index for this phase.
     */
    long getTail() {
      return tail;
    }

    /**
     * The next free item for this phase.  Only useful for the writing phase.
     *
     * @return The next item from this phase.
     */
    long getNextItem() {
      return nextItem;
    }

    /**
     * Increment the number of valid items in this phase.
     */
    void incNrValidItems() {
      ++nrValidItems;
    }

    /**
     * Reduce the number of valid items in this phase by one.
     */
    void decNrValidItems() {
      assert nrValidItems > 0;
      --nrValidItems;
    }

    /**
     * Gets the token which represents a reference to the current phase.
     * A new token is created if no token is available and the phase is in use.
     */
    private Token getToken() {
      Token token = (tokenRef != null) ? tokenRef.get() : null;

      if ( (token == null) && (refCount > 0)) {
        if (logger.isDebugEnabled()) {
          assert stack != null;
          logger.info("Lost phase token. Used " + stack.size() + " times:\n" + C.join(stack, "\n\n"));
        }
        refCount = 0;
      }

      return token;
    }

    /**
     * Sets initial values for this phase. Used by the various constructors.
     *
     * @param head The initial head of the freed items list. This will be
     *      updated as new items are freed in the phase.
     * @param tail The tail of the freed items list.
     * @param nextItem The next item to allocate for this phase.
     * @param nrValidItems The number of valid items in this phase.
     * @throws IOException if an I/O error occurs.
     */
    private void init(
        long head, long tail, long nextItem, long nrValidItems
    ) throws IOException {
      // Caller must be synchronized on FreeList.this.

      initFreeListFile(head == HEADER_SIZE && tail == HEADER_SIZE);

      long sizeInLongs = (nextItem + 1) / 2;
      itemToPhaseSeqMap.setSize(sizeInLongs);
      if ((nextItem & 1) == 1) {
        // Zero the unused part of the last long in the file.
        itemToPhaseSeqMap.putInt(nextItem, 0);
      }

      sequenceNumber = 0;

      this.head = head;
      firstHead = head;

      this.tail = tail;
      firstTail = tail;

      this.nextItem = nextItem;
      this.nrValidItems = nrValidItems;
      init();
    }

    /**
     * Adds this phase to the phase list of the enclosing FreeList object.
     */
    private void init() {
      if (logger.isDebugEnabled()) stack = new LinkedList<StackTrace>();

      // Caller must be synchronized on FreeList.this.
      firstFree = head;
      reallocate = head;

      currentPhase = this;
      phases.addLast(this);
    }

    /**
     * Releases a reference to the current phase.  When all references have been
     * released then the phase will no longer be valid.
     *
     * @return <code>true</code> when the phase has been completely release, and
     *     <code>false</code> when there are remaining references.
     */
    private synchronized boolean release() {
      assert getToken() != null:"released() when there is no valid token";

      if (refCount == 0) throw new AssertionError("Attempt to release Phase with refCount == 0.");

      if (--refCount == 0) {
        tokenRef = null;
        return true;
      }

      return false;
    }

    /**
     * A class to represent a reference to the enclosing phase.
     * Used to hold onto phases, and to clean up any abandoned phases.
     */
    public final class Token {

      private boolean valid = true;

      /**
       * Empty constructor, limits construction to the enclosing classes.
       */
      Token() {
        // NO-OP
      }

      /**
       * Gets the {@link FreeList.Phase} that this token refers to.
       *
       * @return The Phase referred to by this token.
       */
      public Phase getPhase() {
        assert valid:"Invalid Token";

        return Phase.this;
      }

      /**
       * Release this reference to the phase.
       */
      public void release() {
        assert valid:"Invalid Token";

        if (Phase.this.release()) {
          valid = false;
        }
      }
    }
  }
}
