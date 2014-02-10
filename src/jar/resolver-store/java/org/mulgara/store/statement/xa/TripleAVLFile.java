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
package org.mulgara.store.statement.xa;

// Java 2 standard packages
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.statement.*;
import org.mulgara.store.tuples.*;
import org.mulgara.store.xa.*;
import org.mulgara.util.Constants;

/**
 * @created 2001-10-13
 *
 * @author David Makepeace
 * @author Paul Gearon
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/05/16 11:07:08 $ @maintenanceAuthor $Author: amuys $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class TripleAVLFile {

  /**
   * Logger.
   */
  private final static Logger logger = Logger.getLogger(TripleAVLFile.class);

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  private final static String BLOCKFILE_EXT = "_tb";

  private final static int SIZEOF_TRIPLE = 4;

  private final static int BLOCK_SIZE = 8 * 1024;

  private final static int MAX_TRIPLES =
      BLOCK_SIZE / Constants.SIZEOF_LONG / SIZEOF_TRIPLE;

  private final static int IDX_NR_TRIPLES_I = 1;

  private final static int IDX_LOW_TRIPLE = 1;

  private final static int IDX_HIGH_TRIPLE = IDX_LOW_TRIPLE + SIZEOF_TRIPLE;

  private final static int IDX_BLOCK_ID = IDX_HIGH_TRIPLE + SIZEOF_TRIPLE;

  private final static int PAYLOAD_SIZE = IDX_BLOCK_ID + 1;

  @SuppressWarnings("unused")
  private File file;

  private AVLFile avlFile;

  private ManagedBlockFile blockFile;

  private Phase currentPhase;

  private TripleWriteThread tripleWriteThread;

  private int order0;

  private int order1;

  private int order2;

  private int order3;

  private int[] sortOrder;

  private AVLComparator avlComparator;

  private TripleComparator tripleComparator;


  /**
   * CONSTRUCTOR TripleAVLFile TO DO
   *
   * @param file PARAMETER TO DO
   * @param sortOrder PARAMETER TO DO
   * @throws IOException EXCEPTION TO DO
   */
  public TripleAVLFile(File file, int[] sortOrder) throws IOException {
    this.file = file;
    this.sortOrder = sortOrder;

    order0 = sortOrder[0];
    order1 = sortOrder[1];
    order2 = sortOrder[2];
    order3 = sortOrder[3];

    avlFile = new AVLFile(file, PAYLOAD_SIZE);
    blockFile = new ManagedBlockFile(file + BLOCKFILE_EXT, BLOCK_SIZE, BlockFile.IOType.DEFAULT);
    avlComparator = new TripleAVLComparator(sortOrder);
    tripleComparator = new TripleComparator(sortOrder);

    tripleWriteThread = new TripleWriteThread(file);
  }


  /**
   * CONSTRUCTOR TripleAVLFile TO DO
   *
   * @param fileName PARAMETER TO DO
   * @param sortOrder PARAMETER TO DO
   * @throws IOException EXCEPTION TO DO
   */
  public TripleAVLFile(String fileName, int[] sortOrder) throws IOException {
    this(new File(fileName), sortOrder);
  }


  /**
   * Binary search for a triple given a range to work within.
   *
   * @param triples The int buffer holding the triples to search on.
   * @param comp The comparator to use based on the ordering within <i>triples
   *      </i>.
   * @param left The start of the range to search in (inclusive).
   * @param right The end of the range to search in (exclusive).
   * @param triple The triple to search for.
   * @return The index of the found triple in the int buffer, or if not found
   *      then -index-1 of the triple above the point where the triple would be.
   */
  private static int binarySearch(
      Block triples, TripleComparator comp,
      int left, int right, long[] triple
  ) {
    for (;;) {
      // if the range is zero then the node was not found.
      // return the next node up.
      if (left >= right) {
        return -right - 1;
      }

      // find the middle of this range
      int middle = (left + right) >>> 1;
      // determine if the required triple is above or below the middle
      int c = comp.compare(triple, triples, middle);

      // if it's in the middle then return it
      if (c == 0) {
        return middle;
      }

      if (c < 0) {
        // if it's below the middle then search there
        right = middle;
      } else {
        // if it's below the middle then search there
        left = middle + 1;
      }
    }
  }


  /**
   * METHOD TO DO
   *
   * @param triples PARAMETER TO DO
   * @param nrTriples PARAMETER TO DO
   * @param index PARAMETER TO DO
   * @param triple PARAMETER TO DO
   */
  private static void insertTripleInBlock(
      Block triples, int nrTriples, int index, long[] triple
  ) {
    if (index < nrTriples) {
      triples.put(
          (index + 1) * SIZEOF_TRIPLE * Constants.SIZEOF_LONG,
          triples, index * SIZEOF_TRIPLE * Constants.SIZEOF_LONG,
          (nrTriples - index) * SIZEOF_TRIPLE * Constants.SIZEOF_LONG
      );
    }

    //triples.put(index * SIZEOF_TRIPLE, triple);
    int pos = index * SIZEOF_TRIPLE;
    triples.putLong(pos++, triple[0]);
    triples.putLong(pos++, triple[1]);
    triples.putLong(pos++, triple[2]);
    triples.putLong(pos, triple[3]);
  }


  /**
   * METHOD TO DO
   *
   * @param triples PARAMETER TO DO
   * @param nrTriples PARAMETER TO DO
   * @param index PARAMETER TO DO
   */
  private static void removeTripleFromBlock(
      Block triples, int nrTriples, int index
  ) {
    if (index + 1 < nrTriples) {
      triples.put(
          index * SIZEOF_TRIPLE * Constants.SIZEOF_LONG,
          triples, (index + 1) * SIZEOF_TRIPLE * Constants.SIZEOF_LONG,
          (nrTriples - index - 1) * SIZEOF_TRIPLE * Constants.SIZEOF_LONG
      );
    }
  }


  /**
   * Truncates the file to zero length.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void clear() throws IOException {
    avlFile.clear();
    blockFile.clear();
  }


  /**
   * Ensures that all data for this BlockFile is stored in persistent storage
   * before returning.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void force() throws IOException {
    avlFile.force();
    blockFile.force();
  }


  /**
   * METHOD TO DO
   */
  public synchronized void unmap() {
    if (tripleWriteThread != null) {
      tripleWriteThread.close();
      tripleWriteThread = null;
    }

    if (avlFile != null) {
      avlFile.unmap();
    }
    if (blockFile != null) {
      blockFile.unmap();
    }
  }


  /**
   * Closes the block file.
   *
   * @throws IOException EXCEPTION TO DO
   */
  public synchronized void close() throws IOException {
    boolean success = false;
    try {
      if (avlFile != null) {
        avlFile.close();
      }
      success = true;
    } finally {
      if (blockFile != null) {
        try {
          blockFile.close();
        } catch (IOException e) {
          if (success) throw e; // This is a new exception, need to re-throw it.
          else logger.info("Suppressing I/O exception cleaning up from failed write", e); // Log suppressed exception.
        }
      }
    }
  }


  /**
   * Closes and deletes the block file.
   *
   * @throws IOException EXCEPTION TO DO
   */
  public synchronized void delete() throws IOException {
    try {
      try {
        if (avlFile != null) {
          avlFile.delete();
        }
      } finally {
        if (blockFile != null) {
          blockFile.delete();
        }
      }
    } finally {
      avlFile = null;
      blockFile = null;
    }
  }


  private final static class TripleLocation {

    public AVLNode node;

    public int offset;


    /**
     * CONSTRUCTOR TripleLocation TO DO
     *
     * @param node PARAMETER TO DO
     * @param offset PARAMETER TO DO
     */
    TripleLocation(AVLNode node, int offset) {
      this.node = node;
      this.offset = offset;
    }


    public boolean equals(Object o) {
      if (o == null) {
        return false;
      }
      if (o == this) {
        return true;
      }

      TripleLocation tl;
      try {
        tl = (TripleLocation) o;
      } catch (ClassCastException ex) {
        return false;
      }

      return tl.node == null && node == null || (
          tl.node != null && node != null &&
          tl.node.getId() == node.getId() &&
          tl.offset == offset
      );
    }


    public int hashCode() {
      return node == null ? 0 : ((int) node.getId() + 1) * 17 + offset;
    }

  }


  private static final class TripleAVLComparator implements AVLComparator {

    private TripleComparator tripleComparator;

    /**
     * CONSTRUCTOR TripleAVLComparator TO DO
     *
     * @param sortOrder PARAMETER TO DO
     */
    TripleAVLComparator(int[] sortOrder) {
      this.tripleComparator = new TripleComparator(
          AVLNode.IDX_PAYLOAD + IDX_LOW_TRIPLE, sortOrder
      );
    }

    public int compare(long[] key, AVLNode node) {
      Block block = node.getBlock();
      if (tripleComparator.compare(key, block, 0) < 0) {
        return -1;
      }
      if (tripleComparator.compare(key, block, 1) > 0) {
        return 1;
      }
      return 0;
    }

  }


  /**
   * Inner class to compare two triples in an int buffer, using a given ordering
   * for each of the node columns.
   */
  private final static class TripleComparator implements Comparator<Object> {

    private int offset;

    /**
     * The required ordering of node columns.
     */
    private int c0, c1, c2, c3;


    /**
     * Construct a comparator according to requested ordering.
     *
     * @param offset PARAMETER TO DO
     * @param sortOrder The order of the columns to sort on
     */
    TripleComparator(int offset, int[] sortOrder) {
      this.offset = offset;
      c0 = sortOrder[0];
      c1 = sortOrder[1];
      c2 = sortOrder[2];
      c3 = sortOrder[3];
    }


    /**
     * Construct a comparator according to requested ordering.
     *
     * @param sortOrder The order of the columns to sort on
     */
    TripleComparator(int[] sortOrder) {
      this(0, sortOrder);
    }


    /**
     * Compare triples in 2 int buffers.
     *
     * @param key PARAMETER TO DO
     * @param b PARAMETER TO DO
     * @param i PARAMETER TO DO
     * @return -1 If the first triple is smaller than the second, 1 if the first
     *      triple is larger than the second, and 0 if they are equal.
     */
    public int compare(long[] key, Block b, int i) {
      // calculate the offset into the buffers of each triple
      int index = offset + i * SIZEOF_TRIPLE;
      int c;

      if (
          (c = XAUtils.compare(key[c0], b.getLong(index + c0))) == 0 &&
          (c = XAUtils.compare(key[c1], b.getLong(index + c1))) == 0 &&
          (c = XAUtils.compare(key[c2], b.getLong(index + c2))) == 0
      ) {
        c = XAUtils.compare(key[c3], b.getLong(index + c3));
      }
      return c;
    }


    /**
     * Compare triples in 2 int arrays.
     *
     * @param t1 The array holding the first triple.
     * @param t2 The array holding the second triple.
     * @return -1 If the first triple is smaller than the second, 1 if the first
     *      triple is larger than the second, and 0 if they are equal.
     */
    public int compare(long[] t1, long[] t2) {
      int c;
      if (
          (c = XAUtils.compare(t1[c0], t2[c0])) == 0 &&
          (c = XAUtils.compare(t1[c1], t2[c1])) == 0 &&
          (c = XAUtils.compare(t1[c2], t2[c2])) == 0
      ) {
        c = XAUtils.compare(t1[c3], t2[c3]);
      }
      return c;
    }

    /**
     * Compare triples in 2 long arrays.
     *
     * @param t1 The array holding the first triple.
     * @param t2 The array holding the second triple.
     * @return -1 If the first triple is smaller than the second, 1 if the first
     *      triple is larger than the second, and 0 if they are equal.
     */
    public int compare(Object t1, Object t2) {
      return compare((long[])t1, (long[])t2);
    }

  }


  static String toString(long[] la) {
    StringBuffer sb = new StringBuffer("[");
    for (int i = 0; i < la.length; ++i) {
      if (i != 0) sb.append(',');
      sb.append(la[i]);
    }
    sb.append(']');
    return sb.toString();
  }


  static String toString(int[] ia) {
    StringBuffer sb = new StringBuffer("[");
    for (int i = 0; i < ia.length; ++i) {
      if (i != 0) sb.append(',');
      sb.append(ia[i]);
    }
    sb.append(']');
    return sb.toString();
  }


  public final class Phase implements PersistableMetaRoot {

    // in longs
    private final static int HEADER_SIZE = 1;

    public final static int RECORD_SIZE =
        HEADER_SIZE + AVLFile.Phase.RECORD_SIZE +
        ManagedBlockFile.Phase.RECORD_SIZE;

    @SuppressWarnings("unused")
    private final static int IDX_NR_FILE_TRIPLES = 0;

    private long nrFileTriples;

    private AVLFile.Phase avlFilePhase;

    private ManagedBlockFile.Phase blockFilePhase;

    private AVLNode cachedNode = null;

    private Block cachedBlock = null;


    /**
     * CONSTRUCTOR Phase TO DO
     *
     * @throws IOException EXCEPTION TO DO
     */
    public Phase() throws IOException {
      if (tripleWriteThread != null) tripleWriteThread.drain();

      if (currentPhase == null) {
        nrFileTriples = 0;
      } else {
        nrFileTriples = currentPhase.nrFileTriples;
      }
      avlFilePhase = avlFile.new Phase();
      blockFilePhase = blockFile.new Phase();
      check();
      if (tripleWriteThread != null) tripleWriteThread.setPhase(this);
      currentPhase = this;
    }


    /**
     * CONSTRUCTOR Phase TO DO
     *
     * @throws IOException EXCEPTION TO DO
     */
    public Phase(Phase p) throws IOException {
      assert p != null;

      if (tripleWriteThread != null) {
        if (p == currentPhase) tripleWriteThread.drain();
        else tripleWriteThread.abort();
      }

      nrFileTriples = p.nrFileTriples;
      avlFilePhase = avlFile.new Phase(p.avlFilePhase);
      blockFilePhase = blockFile.new Phase(p.blockFilePhase);
      check();
      if (tripleWriteThread != null) tripleWriteThread.setPhase(this);
      currentPhase = this;
    }


    /**
     * CONSTRUCTOR Phase TO DO
     *
     * @param b PARAMETER TO DO
     * @param offset PARAMETER TO DO
     * @throws IOException EXCEPTION TO DO
     */
    public Phase(Block b, int offset) throws IOException {
      if (tripleWriteThread != null) tripleWriteThread.drain();

      nrFileTriples = b.getLong(offset++);
      avlFilePhase = avlFile.new Phase(b, offset);
      offset += AVLFile.Phase.RECORD_SIZE;
      blockFilePhase = blockFile.new Phase(b, offset);
      check();
      if (tripleWriteThread != null) tripleWriteThread.setPhase(this);
      currentPhase = this;
    }


    public long getNrTriples() {
      if (this == currentPhase && tripleWriteThread != null)
        tripleWriteThread.drain();

      return nrFileTriples;
    }


    public boolean isInUse() {
      return blockFilePhase.isInUse();
    }


    public boolean isEmpty() {
      if (this == currentPhase && tripleWriteThread != null)
        tripleWriteThread.drain();

      return avlFilePhase.isEmpty();
    }


    /**
     * Writes this PersistableMetaRoot to the specified Block. The ints are
     * written at the specified offset.
     *
     * @param b the Block.
     * @param offset PARAMETER TO DO
     */
    public void writeToBlock(Block b, int offset) {
      if (tripleWriteThread != null) tripleWriteThread.drain();
      check();
      b.putLong(offset++, nrFileTriples);
      avlFilePhase.writeToBlock(b, offset);
      offset += AVLFile.Phase.RECORD_SIZE;
      blockFilePhase.writeToBlock(b, offset);
    }


    /**
     * Adds a triple to the graph.
     *
     * @param node0 The 0 node of the triple.
     * @param node1 The 1 node of the triple.
     * @param node2 The 2 node of the triple.
     * @param node3 The 3 node of the triple.
     * @throws IOException EXCEPTION TO DO
     */
    public void addTriple(long node0, long node1, long node2, long node3) throws IOException {
      addTriple(new long[] {node0, node1, node2, node3});
    }


    /**
     * Adds a triple to the graph.
     *
     * @param triple The triple to add
     * @throws IOException EXCEPTION TO DO
     */
    void addTriple(long[] triple) throws IOException {
      if (this != currentPhase) throw new IllegalStateException("Attempt to modify a read-only phase.");

      if (tripleWriteThread != null) tripleWriteThread.drain();

      boolean success = false;
      try {
        syncAddTriple(triple);
        success = true;
      } finally {
        try {
          releaseCache();
        } catch (IOException e) {
          if (success) throw e; // This is a new exception, need to re-throw it.
          else logger.info("Suppressing I/O exception cleaning up from failed write", e); // Log suppressed exception.
        }
      }
    }


    /**
     * Adds multiple triples to the graph.
     *
     * @param triples The triples to add
     * @throws IOException EXCEPTION TO DO
     */
    void syncAddTriples(long[][] triples) throws IOException {
      if (this != currentPhase) throw new IllegalStateException("Attempt to modify a read-only phase.");

      Arrays.sort(triples, tripleComparator);
      boolean success = false;
      try {
        for (int i = 0; i < triples.length; ++i) {
          long[] triple = triples[i];
          triples[i] = null; // Allow early garbage collection.

          // Add the triple to the TripleAVLFile and check that the triple
          // wasn't already there.
          syncAddTriple(triple);
        }
        success = true;
      } finally {
        try {
          releaseCache();
        } catch (IOException e) {
          if (success) throw e; // This is a new exception, need to re-throw it.
          else logger.info("Suppressing I/O exception cleaning up from failed write", e); // Log suppressed exception.
        }
      }
    }


    private void releaseCache() throws IOException {
      try {
        if (cachedNode != null) cachedNode.release();
        if (cachedBlock != null) {
          cachedBlock.write();
        }
      } finally {
        cachedNode = null;
        cachedBlock = null;
      }
    }


    private void releaseBlockToCache(Block block) throws IOException {
      if (cachedBlock != null) {
        cachedBlock.write();
      }
      cachedBlock = block;
    }


    private Block getCachedBlock(long blockId) {
      if (cachedBlock != null && blockId == cachedBlock.getBlockId()) {
        // Block is cached.  Remove from cache and return it.
        Block block = cachedBlock;
        cachedBlock = null;
        return block;
      }

      return null;
    }


    private void releaseNodeToCache(AVLNode node) {
      if (cachedNode != null) cachedNode.release();
      cachedNode = node;
    }


    /**
     * Adds a triple to the graph.
     *
     * @param triple The triple to add
     * @throws IOException EXCEPTION TO DO
     */
    private void syncAddTriple(long[] triple) throws IOException {
      AVLNode startNode;
      if (cachedNode == null) {
        startNode = avlFilePhase.getRootNode();
      } else {
        startNode = cachedNode;
        cachedNode = null;
      }

      AVLNode[] findResult = AVLNode.find(startNode, avlComparator, triple);
      if (startNode != null) startNode.release();

      if (findResult == null) {
        // Tree is empty.  Create a node and allocate a triple block.
        Block newTripleBlock = blockFilePhase.allocateBlock();
        AVLNode newNode = avlFilePhase.newAVLNodeInstance();
        newNode.putPayloadLong(IDX_LOW_TRIPLE, triple[0]);
        newNode.putPayloadLong(IDX_LOW_TRIPLE + 1, triple[1]);
        newNode.putPayloadLong(IDX_LOW_TRIPLE + 2, triple[2]);
        newNode.putPayloadLong(IDX_LOW_TRIPLE + 3, triple[3]);
        newNode.putPayloadLong(IDX_HIGH_TRIPLE, triple[0]);
        newNode.putPayloadLong(IDX_HIGH_TRIPLE + 1, triple[1]);
        newNode.putPayloadLong(IDX_HIGH_TRIPLE + 2, triple[2]);
        newNode.putPayloadLong(IDX_HIGH_TRIPLE + 3, triple[3]);
        newNode.putPayloadInt(IDX_NR_TRIPLES_I, 1);
        newNode.putPayloadLong(
            IDX_BLOCK_ID, newTripleBlock.getBlockId()
        );
        newNode.write();

        newTripleBlock.put(0, triple);
        //newTripleBlock.write();
        releaseBlockToCache(newTripleBlock);

        avlFilePhase.insertFirst(newNode);
        releaseNodeToCache(newNode);
        incNrTriples();
        return;
      }

      AVLNode node;
      if (findResult.length == 1 || findResult[1] == null) {
        node = findResult[0];
      } else if (findResult[0] == null) {
        node = findResult[1];
      } else {
        // Between two nodes.

        //// Choose the node with the smaller number of triples.
        //if (
        //    findResult[0].getPayloadInt(IDX_NR_TRIPLES_I) <=
        //    findResult[1].getPayloadInt(IDX_NR_TRIPLES_I)
        //) {
        //  node = findResult[0];
        //} else {
        //  node = findResult[1];
        //}

        // Preferentially choose the lower node.
        if (
            findResult[0].getPayloadInt(IDX_NR_TRIPLES_I) < MAX_TRIPLES ||
            findResult[1].getPayloadInt(IDX_NR_TRIPLES_I) == MAX_TRIPLES
        ) {
          node = findResult[0];
        } else {
          node = findResult[1];
        }
      }
      node.incRefCount();

      boolean success = false;
      Block tripleBlock = null;
      boolean tripleBlockDirty = false;
      try {
        int nrTriples = node.getPayloadInt(IDX_NR_TRIPLES_I);
        if (findResult.length == 1) {
          // Found the node.  See if it matches the high or low triple.
          if (
              // Low triple.
              node.getPayloadLong(IDX_LOW_TRIPLE) == triple[0] &&
              node.getPayloadLong(IDX_LOW_TRIPLE + 1) == triple[1] &&
              node.getPayloadLong(IDX_LOW_TRIPLE + 2) == triple[2] &&
              node.getPayloadLong(IDX_LOW_TRIPLE + 3) == triple[3]
          ) return;

          if (
              nrTriples > 1 &&
              // High triple.
              node.getPayloadLong(IDX_HIGH_TRIPLE) == triple[0] &&
              node.getPayloadLong(IDX_HIGH_TRIPLE + 1) == triple[1] &&
              node.getPayloadLong(IDX_HIGH_TRIPLE + 2) == triple[2] &&
              node.getPayloadLong(IDX_HIGH_TRIPLE + 3) == triple[3]
          ) return;
        }

        // Get the triple block.
        long blockId = node.getPayloadLong(IDX_BLOCK_ID);
        tripleBlock = getCachedBlock(blockId);
        if (tripleBlock == null) {
          tripleBlock = blockFilePhase.readBlock(blockId);
        } else {
          // Blocks in the cache are always dirty.
          tripleBlockDirty = true;
        }

        int index;
        if (findResult.length == 2) {
          index = node == findResult[0] ? nrTriples : 0;
        } else {
          // Find the triple or where the triple should be inserted.
          index = binarySearch(
              tripleBlock, tripleComparator, 0, nrTriples, triple
          );
          if (index >= 0) return;
          index = -index - 1;
          // Make index positive.
        }

        // Tell the node that it will be modified.
        node.modify();

        // Split the node if the triple block is full.
        if (nrTriples == MAX_TRIPLES) {
          // Split the block.  Allocate a new node and block to take the upper
          // portion of the current block.
          //int splitPoint = MAX_TRIPLES / 2;
          int splitPoint = index == 0 ? 0 : (
              index == MAX_TRIPLES ? MAX_TRIPLES : MAX_TRIPLES / 2
          );
          assert splitPoint > 0 || index == 0;
          assert splitPoint < MAX_TRIPLES || index == MAX_TRIPLES;

          Block newTripleBlock = blockFilePhase.allocateBlock();
          AVLNode newNode = avlFilePhase.newAVLNodeInstance();

          // Low triple.
          if (splitPoint < MAX_TRIPLES) {
            int pos = splitPoint * SIZEOF_TRIPLE;
            newNode.putPayloadLong(
                IDX_LOW_TRIPLE, tripleBlock.getLong(pos++)
            );
            newNode.putPayloadLong(
                IDX_LOW_TRIPLE + 1, tripleBlock.getLong(pos++)
            );
            newNode.putPayloadLong(
                IDX_LOW_TRIPLE + 2, tripleBlock.getLong(pos++)
            );
            newNode.putPayloadLong(
                IDX_LOW_TRIPLE + 3, tripleBlock.getLong(pos)
            );
          }

          // High triple.
          if (index < MAX_TRIPLES) {
            int pos = IDX_HIGH_TRIPLE;
            newNode.putPayloadLong(
                IDX_HIGH_TRIPLE, node.getPayloadLong(pos++)
            );
            newNode.putPayloadLong(
                IDX_HIGH_TRIPLE + 1, node.getPayloadLong(pos++)
            );
            newNode.putPayloadLong(
                IDX_HIGH_TRIPLE + 2, node.getPayloadLong(pos++)
            );
            newNode.putPayloadLong(
                IDX_HIGH_TRIPLE + 3, node.getPayloadLong(pos)
            );
          }

          if (index < MAX_TRIPLES && index <= splitPoint) {
            newNode.putPayloadInt(IDX_NR_TRIPLES_I, MAX_TRIPLES - splitPoint);
            newNode.putPayloadLong(
                IDX_BLOCK_ID, newTripleBlock.getBlockId()
            );
          }

          // Update existing node's high triple unless it will be updated
          // later or will not change.
          if (index != splitPoint) {
            // If splitPoint is zero then index must also be zero.
            // If splitPoint is MAX_TRIPLES then index is also MAX_TRIPLES.
            // If splitPoint is MAX_TRIPLES then no change is required.
            int pos = (splitPoint - 1) * SIZEOF_TRIPLE;
            node.putPayloadLong(
                IDX_HIGH_TRIPLE, tripleBlock.getLong(pos++)
            );
            node.putPayloadLong(
                IDX_HIGH_TRIPLE + 1, tripleBlock.getLong(pos++)
            );
            node.putPayloadLong(
                IDX_HIGH_TRIPLE + 2, tripleBlock.getLong(pos++)
            );
            node.putPayloadLong(
                IDX_HIGH_TRIPLE + 3, tripleBlock.getLong(pos)
            );
          }

          // Copy the top portion of the full block to the new block.
          if (splitPoint < MAX_TRIPLES) {
            newTripleBlock.put(
                0, tripleBlock,
                splitPoint * SIZEOF_TRIPLE * Constants.SIZEOF_LONG,
                (MAX_TRIPLES - splitPoint) * SIZEOF_TRIPLE *
                    Constants.SIZEOF_LONG
            );
          }

          // Update the number of triples unless it will be updated later or
          // will not change.
          if (index > splitPoint) {
            node.putPayloadInt(IDX_NR_TRIPLES_I, splitPoint);
          }

          // Insert the new node into the tree.
          if (findResult.length == 1 || findResult[0] == null) {
            node.incRefCount();
            AVLFile.release(findResult);
            findResult = null;
            findResult = new AVLNode[] {
                node, node.getNextNode()
            };
          }
          int li = AVLFile.leafIndex(findResult);
          findResult[li].insert(newNode, 1 - li);

          if (index == MAX_TRIPLES || index > splitPoint) {
            nrTriples = MAX_TRIPLES - splitPoint;
            index -= splitPoint;
            if (tripleBlockDirty) tripleBlock.write();
            tripleBlock = newTripleBlock;
            node.write();
            node.release();
            node = newNode;
          } else {
            nrTriples = splitPoint;
            newTripleBlock.write();
            newNode.write();
            newNode.release();
          }

          // In case nodes are written by insert().
          node.modify();
        }

        // Duplicate the triple block.
        tripleBlock.modify();
        node.putPayloadLong(IDX_BLOCK_ID, tripleBlock.getBlockId());

        insertTripleInBlock(tripleBlock, nrTriples, index, triple);
        //tripleBlock.write();
        tripleBlockDirty = true;

        node.putPayloadInt(IDX_NR_TRIPLES_I, nrTriples + 1);
        if (index == nrTriples) {
          node.putPayloadLong(IDX_HIGH_TRIPLE, triple[0]);
          node.putPayloadLong(IDX_HIGH_TRIPLE + 1, triple[1]);
          node.putPayloadLong(IDX_HIGH_TRIPLE + 2, triple[2]);
          node.putPayloadLong(IDX_HIGH_TRIPLE + 3, triple[3]);
        }
        if (index == 0) {
          node.putPayloadLong(IDX_LOW_TRIPLE, triple[0]);
          node.putPayloadLong(IDX_LOW_TRIPLE + 1, triple[1]);
          node.putPayloadLong(IDX_LOW_TRIPLE + 2, triple[2]);
          node.putPayloadLong(IDX_LOW_TRIPLE + 3, triple[3]);
        }
        node.write();
        incNrTriples();
        success = true;
        return;
      } finally {
        if (tripleBlock != null) {
          try {
            if (tripleBlockDirty) releaseBlockToCache(tripleBlock);
          } catch (IOException e) {
            if (success) throw e; // Otherwise succeeded, need to throw this new exception.
            else logger.info("Suppressing I/O exception for failed AVL file", e);
          }
        }
        AVLFile.release(findResult);
        releaseNodeToCache(node);
      }
    }


    /**
     * Asynchronously adds a triple to the graph.
     *
     * @param triple The triple to add
     */
    public void asyncAddTriple(long[] triple) {
      if (this != currentPhase) {
        throw new IllegalStateException("Attempt to modify a read-only phase.");
      }

      tripleWriteThread.addTriple(triple);
    }


    /**
     * Remove a triple from the graph.
     *
     * @param node0 The 0 node of the triple to remove.
     * @param node1 The 1 node of the triple to remove.
     * @param node2 The 2 node of the triple to remove.
     * @param node3 PARAMETER TO DO
     * @throws IOException EXCEPTION TO DO
     */
    public void removeTriple(long node0, long node1, long node2, long node3) throws IOException {
      if (this != currentPhase) {
        throw new IllegalStateException("Attempt to modify a read-only phase.");
      }

      long[] triple = new long[] {node0, node1, node2, node3};

      if (tripleWriteThread != null) tripleWriteThread.drain();

      AVLNode[] findResult = avlFilePhase.find(avlComparator, triple);

      if (findResult == null || findResult.length == 2) {
        if (findResult != null) AVLFile.release(findResult);
        // Triple not found
        return;
      }

      // Found the node.
      AVLNode node = findResult[0];
      node.incRefCount();
      AVLFile.release(findResult);
      int nrTriples = node.getPayloadInt(IDX_NR_TRIPLES_I);

      if (nrTriples == 1) {
        // Free the triple block and the avl node.
        blockFilePhase.freeBlock(node.getPayloadLong(IDX_BLOCK_ID));
        node.remove();
        decNrTriples();
        return;
      }

      if (nrTriples == 2) {
        // Special case.  If the triple matches the high triple and there are
        // only two triples in this node then we can simply set the high triple
        // to the low triple and decrement the number of triples.
        if (
            // High triple.
            node.getPayloadLong(IDX_HIGH_TRIPLE) == node0 &&
            node.getPayloadLong(IDX_HIGH_TRIPLE + 1) == node1 &&
            node.getPayloadLong(IDX_HIGH_TRIPLE + 2) == node2 &&
            node.getPayloadLong(IDX_HIGH_TRIPLE + 3) == node3
        ) {
          node.modify();
          node.putPayloadLong(
              IDX_HIGH_TRIPLE, node.getPayloadLong(IDX_LOW_TRIPLE)
          );
          node.putPayloadLong(
              IDX_HIGH_TRIPLE + 1, node.getPayloadLong(IDX_LOW_TRIPLE + 1)
          );
          node.putPayloadLong(
              IDX_HIGH_TRIPLE + 2, node.getPayloadLong(IDX_LOW_TRIPLE + 2)
          );
          node.putPayloadLong(
              IDX_HIGH_TRIPLE + 3, node.getPayloadLong(IDX_LOW_TRIPLE + 3)
          );
          node.putPayloadInt(IDX_NR_TRIPLES_I, 1);
          node.write();
          node.release();
          decNrTriples();
          return;
        }
      }

      // Get the triple block.
      Block tripleBlock = blockFilePhase.readBlock(node.getPayloadLong(IDX_BLOCK_ID));
      try {
        // Find the triple.
        int index = binarySearch(tripleBlock, tripleComparator, 0, nrTriples, triple);
        // If triple not found
        if (index < 0) return;

        // Duplicate both the AVLNode and the triple block.
        node.modify();
        tripleBlock.modify();
        node.putPayloadLong(IDX_BLOCK_ID, tripleBlock.getBlockId());

        removeTripleFromBlock(tripleBlock, nrTriples, index);
        tripleBlock.write();

        node.putPayloadInt(IDX_NR_TRIPLES_I, nrTriples - 1);
        if (index == nrTriples - 1) {
          int pos = (index - 1) * SIZEOF_TRIPLE;
          node.putPayloadLong(IDX_HIGH_TRIPLE, tripleBlock.getLong(pos++));
          node.putPayloadLong(IDX_HIGH_TRIPLE + 1, tripleBlock.getLong(pos++));
          node.putPayloadLong(IDX_HIGH_TRIPLE + 2, tripleBlock.getLong(pos++));
          node.putPayloadLong(IDX_HIGH_TRIPLE + 3, tripleBlock.getLong(pos));
        } else if (index == 0) {
          node.putPayloadLong(IDX_LOW_TRIPLE, tripleBlock.getLong(0));
          node.putPayloadLong(IDX_LOW_TRIPLE + 1, tripleBlock.getLong(1));
          node.putPayloadLong(IDX_LOW_TRIPLE + 2, tripleBlock.getLong(2));
          node.putPayloadLong(IDX_LOW_TRIPLE + 3, tripleBlock.getLong(3));
        }
        node.write();
        decNrTriples();
        return;
      } finally {
        node.release();
      }
    }

    public StoreTuples findTuples(long node0) throws IOException {
      long[] startTriple = new long[SIZEOF_TRIPLE];
      long[] endTriple = new long[SIZEOF_TRIPLE];
      startTriple[order0] = node0;
      endTriple[order0] = node0 + 1;
      return new TuplesImpl(startTriple, endTriple, 1);
    }


    public StoreTuples findTuples(long node0, long node1) throws IOException {
      long[] startTriple = new long[SIZEOF_TRIPLE];
      long[] endTriple = new long[SIZEOF_TRIPLE];
      startTriple[order0] = node0;
      startTriple[order1] = node1;
      endTriple[order0] = node0;
      endTriple[order1] = node1 + 1;
      return new TuplesImpl(startTriple, endTriple, 2);
    }


    public StoreTuples findTuples(long node0, long node1, long node2) throws IOException {
      long[] startTriple = new long[SIZEOF_TRIPLE];
      long[] endTriple = new long[SIZEOF_TRIPLE];
      startTriple[order0] = node0;
      startTriple[order1] = node1;
      startTriple[order2] = node2;
      endTriple[order0] = node0;
      endTriple[order1] = node1;
      endTriple[order2] = node2 + 1;
      return new TuplesImpl(startTriple, endTriple, 3);
    }


    public StoreTuples findTuplesForMeta(long graph) throws IOException {
      long[] startTriple = new long[SIZEOF_TRIPLE];
      long[] endTriple = new long[SIZEOF_TRIPLE];
      startTriple[order0] = graph;
      endTriple[order0] = graph + 1;
      return new MetaTuplesImpl(startTriple, endTriple, 1);
    }


    public StoreTuples findTuplesForMeta(long graph, long node1) throws IOException {
      long[] startTriple = new long[SIZEOF_TRIPLE];
      long[] endTriple = new long[SIZEOF_TRIPLE];
      startTriple[order0] = graph;
      startTriple[order1] = node1;
      endTriple[order0] = graph;
      endTriple[order1] = node1 + 1;
      return new MetaTuplesImpl(startTriple, endTriple, 2);
    }


    public StoreTuples findTuplesForMeta(long graph, long node1, long node2) throws IOException {
      long[] startTriple = new long[SIZEOF_TRIPLE];
      long[] endTriple = new long[SIZEOF_TRIPLE];
      startTriple[order0] = graph;
      startTriple[order1] = node1;
      startTriple[order2] = node2;
      endTriple[order0] = graph;
      endTriple[order1] = node1;
      endTriple[order2] = node2 + 1;
      return new MetaTuplesImpl(startTriple, endTriple, 3);
    }


    public StoreTuples allTuples() {
      return new TuplesImpl();
    }


    public boolean existsTriples(long node0) throws IOException {
      long[] triple = new long[SIZEOF_TRIPLE];
      triple[order0] = node0;

      if (this == currentPhase && tripleWriteThread != null) tripleWriteThread.drain();

      AVLNode[] findResult = avlFilePhase.find(avlComparator, triple);
      // If Triplestore is empty.
      if (findResult == null) return false;

      try {
        // Found the node.
        AVLNode node = findResult[findResult.length == 1 ? 0 : 1];
        // If Triple is less than the minimum or greater than the maximum.
        if (node == null) return false;

        int nrTriples = node.getPayloadInt(IDX_NR_TRIPLES_I);
        // If exact match on a node that only contains one triple.
        if (findResult.length == 1 && nrTriples == 1) return true;

        // See if it matches the high or low triple.
        if (node.getPayloadLong(IDX_LOW_TRIPLE + order0) == node0) return true;
        // If this triple's value falls between two nodes.
        if (findResult.length == 2) return false;
        if (node.getPayloadLong(IDX_HIGH_TRIPLE + order0) == node0) return true;

        // Check if there is no point looking inside the triple block since we have
        // already checked the only two triples for this node.
        if (nrTriples == 2) return false;

        // Get the triple block.
        Block tripleBlock = blockFilePhase.readBlock(node.getPayloadLong(IDX_BLOCK_ID));

        // Find the triple.
        int index = binarySearch(tripleBlock, tripleComparator, 0, nrTriples, triple);
        boolean exists;
        if (index >= 0) {
          exists = true;
        } else {
          index = -index - 1;
          exists = index < nrTriples && tripleBlock.getLong(index * SIZEOF_TRIPLE + order0) == node0;
        }
        return exists;
      } finally {
        AVLFile.release(findResult);
      }
    }


    public boolean existsTriples(long node0, long node1) throws IOException {
      long[] triple = new long[SIZEOF_TRIPLE];
      triple[order0] = node0;
      triple[order1] = node1;

      if (this == currentPhase && tripleWriteThread != null)tripleWriteThread.drain();

      AVLNode[] findResult = avlFilePhase.find(avlComparator, triple);
      // If Triplestore is empty.
      if (findResult == null) return false;

      try {
        // Found the node.
        AVLNode node = findResult[findResult.length == 1 ? 0 : 1];
        // Check if Triple is less than the minimum or greater than the maximum.
        if (node == null) return false;

        int nrTriples = node.getPayloadInt(IDX_NR_TRIPLES_I);
        // Check if exact match on a node that only contains one triple.
        if (nrTriples == 1) return true;

        // See if it matches the high or low triple.
        if (
            node.getPayloadLong(IDX_LOW_TRIPLE + order0) == node0 &&
            node.getPayloadLong(IDX_LOW_TRIPLE + order1) == node1
        ) {
          return true;
        }
        // Check if this triple's value falls between two nodes.
        if (findResult.length == 2) return false;
        if (
            node.getPayloadLong(IDX_HIGH_TRIPLE + order0) == node0 &&
            node.getPayloadLong(IDX_HIGH_TRIPLE + order1) == node1
        ) {
          return true;
        }
        // Check if there is no point looking inside the triple block since we have
        // already checked the only two triples for this node.
        if (nrTriples == 2) return false;

        // Get the triple block.
        Block tripleBlock = blockFilePhase.readBlock(node.getPayloadLong(IDX_BLOCK_ID));

        // Find the triple.
        int index = binarySearch(tripleBlock, tripleComparator, 0, nrTriples, triple);
        boolean exists;
        if (index >= 0) {
          exists = true;
        } else {
          index = -index - 1;
          exists = index < nrTriples &&
              tripleBlock.getLong(index * SIZEOF_TRIPLE + order0) == node0 &&
              tripleBlock.getLong(index * SIZEOF_TRIPLE + order1) == node1;
        }
        return exists;
      } finally {
        AVLFile.release(findResult);
      }
    }


    public boolean existsTriples(long node0, long node1, long node2) throws IOException {
      long[] triple = new long[SIZEOF_TRIPLE];
      triple[order0] = node0;
      triple[order1] = node1;
      triple[order2] = node2;

      if (this == currentPhase && tripleWriteThread != null) tripleWriteThread.drain();

      AVLNode[] findResult = avlFilePhase.find(avlComparator, triple);
      // Check if Triplestore is empty.
      if (findResult == null) return false;

      try {
        // Found the node.
        AVLNode node = findResult[findResult.length == 1 ? 0 : 1];
        // Check if Triple is less than the minimum or greater than the maximum.
        if (node == null) return false;

        int nrTriples = node.getPayloadInt(IDX_NR_TRIPLES_I);
        // Check if exact match on a node that only contains one triple.
        if (nrTriples == 1) return true;

        // See if it matches the high or low triple.
        if (
            node.getPayloadLong(IDX_LOW_TRIPLE + order0) == node0 &&
            node.getPayloadLong(IDX_LOW_TRIPLE + order1) == node1 &&
            node.getPayloadLong(IDX_LOW_TRIPLE + order2) == node2
        ) {
          return true;
        }
        // Check if this triple's value falls between two nodes.
        if (findResult.length == 2) return false;

        if (
            node.getPayloadLong(IDX_HIGH_TRIPLE + order0) == node0 &&
            node.getPayloadLong(IDX_HIGH_TRIPLE + order1) == node1 &&
            node.getPayloadLong(IDX_HIGH_TRIPLE + order2) == node2
        ) {
          return true;
        }
        // Check if there is no point looking inside the triple block since we have
        // already checked the only two triples for this node.
        if (nrTriples == 2) return false;
        // Get the triple block.
        Block tripleBlock = blockFilePhase.readBlock(node.getPayloadLong(IDX_BLOCK_ID));

        // Find the triple.
        int index = binarySearch(tripleBlock, tripleComparator, 0, nrTriples, triple);
        boolean exists;
        if (index >= 0) {
          exists = true;
        } else {
          index = -index - 1;
          exists = index < nrTriples &&
              tripleBlock.getLong(index * SIZEOF_TRIPLE + order0) == node0 &&
              tripleBlock.getLong(index * SIZEOF_TRIPLE + order1) == node1 &&
              tripleBlock.getLong(index * SIZEOF_TRIPLE + order2) == node2;
        }
        return exists;
      } finally {
        AVLFile.release(findResult);
      }
      
    }


    public boolean existsTriple(long node0, long node1, long node2, long node3) throws IOException {
      long[] triple = new long[SIZEOF_TRIPLE];
      triple[order0] = node0;
      triple[order1] = node1;
      triple[order2] = node2;
      triple[order3] = node3;

      return existsTriple(triple);
    }


    public Token use() {
      return new Token();
    }


    public long checkIntegrity() {
      if (this == currentPhase && tripleWriteThread != null)
        tripleWriteThread.drain();

      AVLNode node = avlFilePhase.getRootNode();
      if (node == null) return 0;

      node = node.getMinNode_R();
      long[] prevTriple = new long[] {0, 0, 0, 0};
      long[] triple = new long[SIZEOF_TRIPLE];
      long nodeIndex = 0;
      long totalNrTriples = 0;

      do {
        int nrTriples = node.getPayloadInt(IDX_NR_TRIPLES_I);
        if (nrTriples < 1 || nrTriples > MAX_TRIPLES) {
          throw new Error(
              "NR_TRIPLES (" + nrTriples + ") is out of bounds in node: " +
              node.getId() + " (index " + nodeIndex + ")"
          );
        }

        triple[0] = node.getPayloadLong(IDX_LOW_TRIPLE);
        triple[1] = node.getPayloadLong(IDX_LOW_TRIPLE + 1);
        triple[2] = node.getPayloadLong(IDX_LOW_TRIPLE + 2);
        triple[3] = node.getPayloadLong(IDX_LOW_TRIPLE + 3);

        if (tripleComparator.compare(prevTriple, triple) >= 0) {
          throw new Error(
              "LOW_TRIPLE (" +
              triple[0] + " " + triple[1] + " " +
              triple[2] + " " + triple[3] +
              ") is not greater than prevTriple (" +
              prevTriple[0] + " " + prevTriple[1] + " " +
              prevTriple[2] + " " + prevTriple[3] +
              ") in node: " + node.getId() + " (index " + nodeIndex + ")"
          );
        }

        Block block;
        try {
          block = blockFilePhase.readBlock(node.getPayloadLong(IDX_BLOCK_ID));
        } catch (IOException ex) {
          throw new Error("I/O Error", ex);
        }

        if (tripleComparator.compare(triple, block, 0) != 0) {
          long[] firstTriple = new long[SIZEOF_TRIPLE];
          block.get(0, firstTriple);
          throw new Error(
              "LOW_TRIPLE (" +
              triple[0] + " " + triple[1] + " " +
              triple[2] + " " + triple[3] +
              ") is not equal to the first triple (" +
              firstTriple[0] + " " +
              firstTriple[1] + " " +
              firstTriple[2] + " " +
              firstTriple[3] + ") in node: " + node.getId() +
              " (index " + nodeIndex + ")"
          );
        }
        System.arraycopy(triple, 0, prevTriple, 0, SIZEOF_TRIPLE);

        for (int i = 1; i < nrTriples; ++i) {
          block.get(i * SIZEOF_TRIPLE, triple);
          if (tripleComparator.compare(prevTriple, triple) >= 0) {
            throw new Error(
                "Triple #" + i + " (" +
                " not greater than previous triple in block in node: " +
                node.getId() + " (index " + nodeIndex + ")"
            );
          }
          System.arraycopy(triple, 0, prevTriple, 0, SIZEOF_TRIPLE);
        }

        triple[0] = node.getPayloadLong(IDX_HIGH_TRIPLE);
        triple[1] = node.getPayloadLong(IDX_HIGH_TRIPLE + 1);
        triple[2] = node.getPayloadLong(IDX_HIGH_TRIPLE + 2);
        triple[3] = node.getPayloadLong(IDX_HIGH_TRIPLE + 3);
        if (tripleComparator.compare(triple, prevTriple) != 0) {
          throw new Error(
              "HIGH_TRIPLE (" +
              triple[0] + " " + triple[1] + " " +
              triple[2] + " " + triple[3] +
              ") is not equal to the last triple (" +
              prevTriple[0] + " " + prevTriple[1] + " " +
              prevTriple[2] + " " + prevTriple[3] +
              ") in node: " + node.getId() + " (index " + nodeIndex + ")"
          );
        }

        ++nodeIndex;
        totalNrTriples += nrTriples;
      } while ((node = node.getNextNode_R()) != null);

      if (totalNrTriples != nrFileTriples) {
        throw new Error(
            "nrFileTriples (" + nrFileTriples +
            ") does not match actual number of triples (" + totalNrTriples +
            ") in: " + order0 + order1 + order2 + order3
        );
      }
      return totalNrTriples;
    }


    private void check() {
      assert !isEmpty() || nrFileTriples == 0 :
          "AVLFile not empty but nrFileTriples == 0";
      assert isEmpty() || nrFileTriples > 0 :
          "AVLFile is empty but nrFileTriples == " + nrFileTriples;
    }


    private void incNrTriples() {
      ++nrFileTriples;
    }


    private void decNrTriples() {
      assert nrFileTriples > 0;
      --nrFileTriples;
    }


    private boolean existsTriple(long[] triple)
         throws IOException {
      if (this == currentPhase && tripleWriteThread != null)
        tripleWriteThread.drain();

      AVLNode[] findResult = avlFilePhase.find(avlComparator, triple);
      // Check if Triplestore is empty.
      if (findResult == null) return false;

      try {
        // Check if triple not found.
        if (findResult.length == 2) return false;

        // Found the node.
        AVLNode node = findResult[0];

        int nrTriples = node.getPayloadInt(IDX_NR_TRIPLES_I);
        if (nrTriples == 1) return true;

        // See if it matches the high or low triple.
        if (
            (
                // Low triple.
                node.getPayloadLong(IDX_LOW_TRIPLE) == triple[0] &&
                node.getPayloadLong(IDX_LOW_TRIPLE + 1) == triple[1] &&
                node.getPayloadLong(IDX_LOW_TRIPLE + 2) == triple[2] &&
                node.getPayloadLong(IDX_LOW_TRIPLE + 3) == triple[3]
            ) || (
                // High triple.
                node.getPayloadLong(IDX_HIGH_TRIPLE) == triple[0] &&
                node.getPayloadLong(IDX_HIGH_TRIPLE + 1) == triple[1] &&
                node.getPayloadLong(IDX_HIGH_TRIPLE + 2) == triple[2] &&
                node.getPayloadLong(IDX_HIGH_TRIPLE + 3) == triple[3]
            )
            ) {
          return true;
        }

        if (nrTriples == 2) {
          return false;
        }

        // Get the triple block.
        Block tripleBlock = blockFilePhase.readBlock(node.getPayloadLong(IDX_BLOCK_ID));

        // Find the triple.
        int index = binarySearch(tripleBlock, tripleComparator, 0, nrTriples, triple);
        return index >= 0;
      } finally {
        AVLFile.release(findResult);
      }
    }


    private TripleLocation findTriple(long[] triple) throws IOException {
      AVLNode[] findResult = avlFilePhase.find(avlComparator, triple);
      if (findResult == null) return null;

      AVLNode node;
      if (findResult.length == 2) {
        node = findResult[1];
        if (node != null) node.incRefCount();
        AVLFile.release(findResult);
        return new TripleLocation(node, 0);
      }

      // Found the node.
      node = findResult[0];
      node.incRefCount();
      AVLFile.release(findResult);

      int nrTriples = node.getPayloadInt(IDX_NR_TRIPLES_I);
      if (nrTriples == 1) {
        return new TripleLocation(node, 0);
      }

      // See if it matches the low triple.
      if (
          // Low triple.
          node.getPayloadLong(IDX_LOW_TRIPLE) == triple[0] &&
          node.getPayloadLong(IDX_LOW_TRIPLE + 1) == triple[1] &&
          node.getPayloadLong(IDX_LOW_TRIPLE + 2) == triple[2] &&
          node.getPayloadLong(IDX_LOW_TRIPLE + 3) == triple[3]
      ) {
        return new TripleLocation(node, 0);
      }

      if (nrTriples == 2) {
        return new TripleLocation(node, 1);
      }

      // See if it matches the high triple.
      if (
          // High triple.
          node.getPayloadLong(IDX_HIGH_TRIPLE) == triple[0] &&
          node.getPayloadLong(IDX_HIGH_TRIPLE + 1) == triple[1] &&
          node.getPayloadLong(IDX_HIGH_TRIPLE + 2) == triple[2] &&
          node.getPayloadLong(IDX_HIGH_TRIPLE + 3) == triple[3]
      ) {
        return new TripleLocation(node, nrTriples - 1);
      }

      // Get the triple block.
      Block tripleBlock = blockFilePhase.readBlock(node.getPayloadLong(IDX_BLOCK_ID));

      // Find the triple.
      int index = binarySearch(tripleBlock, tripleComparator, 0, nrTriples, triple);
      if (index < 0) index = -index - 1;
      return new TripleLocation(node, index);
    }


    public final class Token {

      private AVLFile.Phase.Token avlFileToken;

      private ManagedBlockFile.Phase.Token blockFileToken;

      private Phase phase = Phase.this;


      /**
       * CONSTRUCTOR Token TO DO
       */
      Token() {
        avlFileToken = avlFilePhase.use();
        blockFileToken = blockFilePhase.use();
      }


      public Phase getPhase() {
        assert avlFileToken != null : "Invalid Token";
        assert blockFileToken != null : "Invalid Token";
        return phase;
      }


      public void release() {
        assert avlFileToken != null : "Invalid Token";
        assert blockFileToken != null : "Invalid Token";
        avlFileToken.release();
        avlFileToken = null;
        blockFileToken.release();
        blockFileToken = null;
        phase = null;
      }

    }


    private abstract class AbstractStoreTuples implements StoreTuples {

      // keep a stack trace of the instantiation of this object
      protected List<Integer> objectIds = new ArrayList<Integer>();

      private long[] startTriple;

      private TripleLocation start;

      private long[] endTriple;

      private TripleLocation end;

      protected Token token;

      private long nrTriples;

      private boolean nrTriplesValid = false;

      @SuppressWarnings("unused")
      private int prefixLength;

      protected int[] columnMap;

      protected Variable[] variables;

      private long[] tmpTriple = new long[SIZEOF_TRIPLE];

      protected AVLNode node;

      protected boolean beforeStart;

      private int nrBlockTriples = 0;

      protected Block tripleBlock = null;

      protected int offset;

      private long endBlockId = Block.INVALID_BLOCK_ID;

      private int endOffset = 0;

      private long[] prefix = null;

      private int rowCardinality = -1;

      /**
       * CONSTRUCTOR TuplesImpl TO DO
       *
       * @param startTriple PARAMETER TO DO
       * @param endTriple PARAMETER TO DO
       * @param prefixLength PARAMETER TO DO
       * @throws IOException EXCEPTION TO DO
       */
      AbstractStoreTuples(
          long[] startTriple, long[] endTriple,
          int prefixLength
      ) throws IOException {
        assert prefixLength >= 1 && prefixLength < SIZEOF_TRIPLE;

        if (Phase.this == currentPhase && tripleWriteThread != null) tripleWriteThread.drain();

        this.startTriple = startTriple;
        this.endTriple = endTriple;
        this.prefixLength = prefixLength;

        start = findTriple(startTriple);
        end = findTriple(endTriple);

        if (end != null && end.node != null) {
          endBlockId = end.node.getId();
          endOffset = end.offset;
        }
        if (start != null && start.node != null && (start.node.getId() != endBlockId || start.offset < endOffset)) {
          token = use();
          beforeStart = true;
        } else {
          close();
        }
      }


      /**
       * CONSTRUCTOR TuplesImpl TO DO
       */
      AbstractStoreTuples() {
        if (Phase.this == currentPhase && tripleWriteThread != null) tripleWriteThread.drain();

        this.nrTriples = Phase.this.nrFileTriples;
        this.nrTriplesValid = true;

        columnMap = sortOrder;
        this.variables = new Variable[] {
            StatementStore.VARIABLES[order0],
            StatementStore.VARIABLES[order1],
            StatementStore.VARIABLES[order2],
            StatementStore.VARIABLES[order3]
        };

        startTriple = new long[SIZEOF_TRIPLE];
        endTriple = new long[] { Constants.MASK63, Constants.MASK63, Constants.MASK63, Constants.MASK63 };
        prefixLength = 0;

        AVLNode node = avlFilePhase.getRootNode();
        if (node != null) {
          start = new TripleLocation(node.getMinNode_R(), 0);
          token = use();
          end = new TripleLocation(null, 0);
          beforeStart = true;
        } else {
          close();
        }
      }


      public int[] getColumnOrder() {
        return (int[])columnMap.clone();
      }


      public long getRawColumnValue(int column) throws TuplesException {
        return getColumnValue(column);
      }

      public long getColumnValue(int column) throws TuplesException {
        try {
          return tripleBlock.getLong(offset * SIZEOF_TRIPLE + columnMap[column]);
        } catch (ArrayIndexOutOfBoundsException ex) {
          if (column < 0 || column >= variables.length) {
            throw new TuplesException("Column index out of range: " + column);
          }
          throw ex;
        } catch (NullPointerException ex) {
          if (beforeStart || node == null) {
            throw new TuplesException("No current row.  Before start: " + beforeStart + " node: " + node);
          }
          throw ex;
        }
      }


      public Variable[] getVariables() {
        return (Variable[])variables.clone();
      }

      public int getNumberOfVariables() {
        return variables.length;
      }

      public long getRowCount() throws TuplesException {
        if (nrTriplesValid)  return nrTriples;
        nrTriplesValid = true;

        if (start == null) return nrTriples = 0;

        long n = endOffset - start.offset;
        AVLNode curNode = start.node;
        curNode.incRefCount();
        while (curNode != null && curNode.getId() != endBlockId) {
          n += curNode.getPayloadInt(IDX_NR_TRIPLES_I);
          curNode = curNode.getNextNode_R();
        }
        if (curNode != null) {
          curNode.release();
        }

        return nrTriples = n;
      }


      public long getRowUpperBound() throws TuplesException {
        return getRowCount();
      }


      public long getRowExpectedCount() throws TuplesException {
        return getRowCount();
      }


      public int getRowCardinality() throws TuplesException {
        if (rowCardinality != -1) return rowCardinality;
        Tuples temp = (Tuples)this.clone();
        temp.beforeFirst();
        if (!temp.next()) {
          rowCardinality = Cursor.ZERO;
        } else if (!temp.next()) {
          rowCardinality = Cursor.ONE;
        } else {
          rowCardinality = Cursor.MANY;
        }
        temp.close();

        return rowCardinality;
      }



      public int getColumnIndex(Variable variable) throws TuplesException {
        for (int i = 0; i < variables.length; ++i) {
          if (variables[i].equals(variable)) return i;
        }

        throw new TuplesException("variable doesn't match any column");
      }


      public boolean isColumnEverUnbound(int column) {
        return false;
      }


      public boolean isMaterialized() {
        return true;
      }


      public boolean isUnconstrained() {
        return false;
      }


      public RowComparator getComparator() {
        return DefaultRowComparator.getInstance();
      }


      public List<Tuples> getOperands() {
        return Collections.emptyList();
      }


      public boolean isEmpty() {
        return start == null;
      }


      public boolean next() throws TuplesException {
        if (prefix.length > variables.length) throw new TuplesException("prefix too long: " + prefix.length);

        // Move to the next row.
        if (!advance()) {
          return false;
        }
        if (prefix.length == 0) return true;

        // See if the current row matches the prefix.
        RowComparator comparator = getComparator();
        int c = comparator.compare(prefix, this);
        if (c == 0) return true;

        closeIterator();
        if (c < 0) return false;

        // Reorder the prefix to create a triple.
        System.arraycopy(startTriple, 0, tmpTriple, 0, SIZEOF_TRIPLE);
        for (int i = 0; i < prefix.length; ++i) tmpTriple[columnMap[i]] = prefix[i];

        // Check if the prefix is past the end triple.
        if (tripleComparator.compare(tmpTriple, endTriple) >= 0) return false;

        // Locate the first triple greater than or equal to the prefix.
        TripleLocation tLoc;
        try {
          tLoc = findTriple(tmpTriple);
        } catch (IOException ex) {
          throw new TuplesException("I/O error", ex);
        }

        if (tLoc.node != null) {
          if (tLoc.node.getId() != endBlockId || tLoc.offset < endOffset) {
            node = tLoc.node;
            offset = tLoc.offset;
            readTripleBlock();
            return comparator.compare(prefix, this) == 0;
          } else {
            tLoc.node.release();
          }
        }
        return false;
      }


      public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
        if (prefix == null) throw new IllegalArgumentException("Null \"prefix\" parameter");
        if (suffixTruncation != 0) throw new TuplesException("Suffix truncation not implemented");

        beforeStart = true;
        this.prefix = prefix;
      }


      public void beforeFirst() {
        beforeStart = true;
        prefix = Tuples.NO_PREFIX;
      }


      public boolean hasNoDuplicates() {
        return true;
      }

      /**
       * Renames the variables which label the tuples if they have the "magic"
       * names such as "Subject", "Predicate", "Object" and "Meta".
       *
       * @param constraint PARAMETER TO DO
       */
      public void renameVariables(Constraint constraint) {
        if (logger.isDebugEnabled()) {
          logger.debug("Renaming variables.  before: " + Arrays.asList(variables) + " constraint: " + constraint);
        }

        for (int i = 0; i < columnMap.length; ++i) variables[i] = (Variable) constraint.getElement(columnMap[i]);

        if (logger.isDebugEnabled()) {
          logger.debug("Renaming variables.  after: " + Arrays.asList(variables));
        }
      }


      public String toString() {
        Tuples cloned = (Tuples) clone();

        NumberFormat formatter = new DecimalFormat("000000");

        try {
          StringBuffer buffer = new StringBuffer(eol + "{");

          Variable[] variables = cloned.getVariables();

          for (int i = 0; i < variables.length; i++) {
            buffer.append(variables[i]);

            for (int j = 0; j < (6 - variables[i].toString().length()); j++) buffer.append(" ");

            buffer.append("  ");
          }

          if (cloned.isMaterialized()) {
            buffer.append("(");
            buffer.append(cloned.getRowCount());
            buffer.append(" rows)" + eol);
          } else {
            buffer.append("(unevaluated, ");
            buffer.append(cloned.getRowCount());
            buffer.append(" rows max)" + eol);
          }

          cloned.beforeFirst();

          int rowNo = 0;

          while (cloned.next()) {
            if (++rowNo > 20) {
              buffer.append("..." + eol);
              break;
            }

            buffer.append("[");

            for (int i = 0; i < variables.length; i++) {
              buffer.append(formatter.format(cloned.getColumnValue(i)));
              buffer.append("  ");
            }

            buffer.append("]" + eol);
          }

          buffer.append("}");

          return buffer.toString();
        } catch (TuplesException e) {
          return e.toString();
        } finally {
          try {
            cloned.close();
          } catch (Exception e) {
            logger.warn("Failed to close tuples after serializing", e);
          }
        }
      }


      public Object clone() {
        try {
          AbstractStoreTuples copy = (AbstractStoreTuples) super.clone();
          tmpTriple = new long[SIZEOF_TRIPLE];
          if (start != null) {
            start.node.incRefCount();
            if (end.node != null) end.node.incRefCount();
            copy.token = use();
            copy.tripleBlock = null;
            copy.node = null;
            copy.beforeFirst();
          }

          copy.variables = getVariables();

          copy.objectIds = new ArrayList<Integer>(objectIds);
          copy.objectIds.add(new Integer(System.identityHashCode(this)));

          return copy;
        } catch (CloneNotSupportedException ex) {
          throw new Error();
        }
      }


      public void close() {
        closeIterator();

        if (token != null) {
          token.release();
          token = null;
        }

        startTriple = null;
        if (start != null) {
          if (start.node != null) start.node.release();
          start = null;
        }
        endTriple = null;
        if (end != null) {
          if (end.node != null) end.node.release();
          end = null;
        }
      }


      /* Don't enable this in production unless you want a significant increase in heap usage,
       * out-of-heap errors, and 60% slow-down of the queries.
      public void finalize() {
        if (logger.isDebugEnabled()) {
          if (stack != null) {
            logger.debug("TuplesImpl not closed (" + System.identityHashCode(this) + ")\n" + stack);
            logger.debug("----Provenance : " + objectIds);
          }
        }
      }
      */


      public boolean equals(Object o) {
        if (o == null) {
          return false;
        }
        if (o == this) return true;

        // Make sure the object is a Tuples.
        Tuples t;
        try {
          t = (Tuples) o;
        } catch (ClassCastException ex) {
          return false;
        }

        Tuples t1 = null;
        Tuples t2 = null;
        try {
          if (getRowCount() != t.getRowCount()) return false;
          if (getRowCount() == 0) return true;

          // Return false if the column variable names don't match or the number
          // of columns differ.
          if (!Arrays.asList(getVariables()).equals(Arrays.asList(t.getVariables()))) return false;

          // Clone the two Tuples objects.
          t1 = (Tuples) this.clone();
          t2 = (Tuples) t.clone();

          // Get the default comparator.
          RowComparator comp = getComparator();

          t1.beforeFirst();
          t2.beforeFirst();
          while (t1.next()) {
            if (!t2.next() || comp.compare(t1, t2) != 0) return false;
          }
          return !t2.next();
        } catch (TuplesException ex) {
          throw new RuntimeException(ex.toString(), ex);
        } finally {
          try {
            try {
              if (t1 != null) t1.close();
            } finally {
              if (t2 != null) t2.close();
            }
          } catch (TuplesException ex) {
            throw new RuntimeException(ex.toString(), ex);
          }
        }
      }


      /**
       * Added to match {@link #equals(Object)}.
       * Based on the same approach as {@link AbstractTuples#hashCode()}
       */
      public int hashCode() {
        return TuplesOperations.hashCode(this);
      }


      private boolean advance() throws TuplesException {
        if (beforeStart) {
          // Reset the iterator position to the start.
          beforeStart = false;

          if (start != null) {
            // Tuples is not empty.  Reset to first triple.
            closeIterator();
            node = start.node;
            node.incRefCount();
            offset = start.offset;
          }
        } else if (node != null) {
          if (++offset == nrBlockTriples) {
            offset = 0;
            tripleBlock = null;
            node = node.getNextNode_R();
          }

          if (
              node != null && node.getId() == endBlockId && offset >= endOffset
          ) {
            closeIterator();
          }
        }
        readTripleBlock();

        return node != null;
      }


      private void closeIterator() {
        if (tripleBlock != null) tripleBlock = null;
        if (node != null) {
          node.release();
          node = null;
        }
      }


      private void readTripleBlock() throws TuplesException {
        if (tripleBlock == null && node != null) {
          nrBlockTriples = node.getPayloadInt(IDX_NR_TRIPLES_I);
          try {
            tripleBlock = blockFilePhase.readBlock(node.getPayloadLong(IDX_BLOCK_ID));
          } catch (IOException ex) {
            throw new TuplesException("I/O error", ex);
          }
        }
      }

      /**
       * Copied from AbstractTuples
       */
      public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
        return null;
      }
    }

    /**
     * The standard implementation of the StoreTuples for this phase.
     */
    private final class TuplesImpl extends AbstractStoreTuples {
      TuplesImpl(long[] startTriple, long[] endTriple, int prefixLength) throws IOException {
        super(startTriple, endTriple, prefixLength);
        int nrColumns = SIZEOF_TRIPLE - prefixLength;

        variables = new Variable[nrColumns];
        // Set up a column order which moves the prefix columns to the end.
        columnMap = new int[nrColumns];

        for (int i = 0; i < nrColumns; ++i) {
          columnMap[i] = sortOrder[(i + prefixLength) % SIZEOF_TRIPLE];
          variables[i] = StatementStore.VARIABLES[columnMap[i]];
        }

      }

      TuplesImpl() {
        super();
      }
    }

    /**
     * A version of StoreTuples which is designed to set the Meta variable to a requested value.
     *
     * @created Dec 22, 2008
     * @author Paul Gearon
     * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
     * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
     */
    private final class MetaTuplesImpl extends AbstractStoreTuples {

      /** The meta node this tuples comes from. */
      private final long metaNode;

      /**
       * Constructs the Tuples to come from the store.
       * @param startTriple The first triple for this tuples.
       * @param endTriple The first triple pattern that is NOT part of this tuples.
       *                  This may not appear in the store, but if anything is &lt;= this triple
       *                  then it is NOT in the tuples.
       * @param prefixLength The number of elements used to identify the requires triples.
       * @throws IOException If there was an I/O error accessing the triples data in the store.
       */
      MetaTuplesImpl(long[] startTriple, long[] endTriple, int prefixLength) throws IOException {
        super(startTriple, endTriple, prefixLength);
        int nrColumns = SIZEOF_TRIPLE - prefixLength;
        variables = new Variable[nrColumns + 1];

        // Set up a column order which moves the prefix columns to the end.
        columnMap = new int[nrColumns + 1];

        for (int i = 0; i < nrColumns; ++i) {
          columnMap[i] = sortOrder[(i + prefixLength) % SIZEOF_TRIPLE];
          variables[i] = StatementStore.VARIABLES[columnMap[i]];
        }
        // make the last variable "Meta"
        columnMap[nrColumns] = nrColumns;
        variables[nrColumns] = StatementStore.VARIABLES[StatementStore.VARIABLES.length - 1];
        metaNode = startTriple[order0];
      }

      /**
       * Get the value from the given column on the current row.
       * @param column The column to get the value of the binding from.
       * @return The binding for the given column on the current row.
       * @throws TuplesException If the current state is wrong, or if the column requested is invalid.
       */
      public long getColumnValue(int column) throws TuplesException {
        if (column == variables.length - 1) return metaNode;
        // inlining rather than calling to super
        try {
          return tripleBlock.getLong(offset * SIZEOF_TRIPLE + columnMap[column]);
        } catch (ArrayIndexOutOfBoundsException ex) {
          if (column < 0 || column >= variables.length) {
            throw new TuplesException("Column index out of range: " + column);
          }
          throw ex;
        } catch (NullPointerException ex) {
          if (beforeStart || node == null) {
            throw new TuplesException("No current row.  Before start: " + beforeStart + " node: " + node);
          }
          throw ex;
        }
      }

    }
  }

}
