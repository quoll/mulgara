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

// Local packages
import org.mulgara.util.Constants;

/**
 * A file of AVLNodes forming an AVL tree.
 * AVL trees are used for storing data in sorted order.
 * Branches of these trees are also used to create new "phases"
 * which allow modification of the file without affecting what
 * other threads see.  This allows a single phase to be a "writing"
 * phase, while other phases all get to read their own immutable
 * version of the file.
 *
 * @created 2001-10-03
 *
 * @author David Makepeace
 * @author Paul Gearon
 *
 * @version $Revision: 1.10 $
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
public final class AVLFile {

  /** Logger.  */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(AVLFile.class);

  /** The underlying block file */
  private ManagedBlockFile avlBlockFile;

  /** The size of the user data stored in each node of the tree. */
  @SuppressWarnings("unused")  // keep this member, since it isn't kept elsewhere
  private int payloadSize;

  /** The most recent phase for the file. */
  private Phase currentPhase;


  /**
   * Creates a new block file which contains an AVL tree.
   *
   * @param file A {@link java.io.File} object giving the details of the file.
   * @param payloadSize Size of the payload in longs.  Must be at least 1.
   * @throws IOException If an i/o error occurs.
   */
  public AVLFile(File file, int payloadSize) throws IOException {
    if (payloadSize < 1) {
      throw new IllegalArgumentException("payloadSize is less than 1");
    }

    avlBlockFile = new ManagedBlockFile(file, (AVLNode.HEADER_SIZE + payloadSize) * Constants.SIZEOF_LONG,
        BlockFile.IOType.MAPPED);
    this.payloadSize = payloadSize;
  }

  /**
   * Creates a new block file which contains an AVL tree.
   *
   * @param fileName The name of the file to create.
   * @param payloadSize Size of the payload in longs.  Must be at least 1.
   * @throws IOException If an i/o error occurs.
   */
  public AVLFile(String fileName, int payloadSize) throws IOException {
    this(new File(fileName), payloadSize);
  }

  /**
   * Returns the index of the leaf node in the find result which was returned
   * when a node was not found.
   *
   * @param findResult A two element array resulting from a tree search.
   * @return The higher node which is valid.
   */
  public static int leafIndex(AVLNode[] findResult) {
    if (findResult.length != 2) {
      throw new IllegalArgumentException("findResult contains found node.");
    }

    return (
        findResult[0] == null ||
        findResult[0].getRightId() != AVLNode.NULL_NODE
    ) ? 1 : 0;
  }

  /**
   * Releases the nodes returned from a find when a result was not found.
   *
   * @param avlNodes A two element array of AVLNodes.  One or the other may be null.
   */
  public static void release(AVLNode[] avlNodes) {
    if (avlNodes[0] != null) {
      avlNodes[0].release();
      avlNodes[0] = null;
    }

    if ( (avlNodes.length == 2) && (avlNodes[1] != null)) {
      avlNodes[1].release();
      avlNodes[1] = null;
    }
  }

  /**
   * Reduces the file down to an empty tree.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void clear() throws IOException {
    avlBlockFile.clear();
  }

  /**
   * Wait until all data has been written to the file.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void force() throws IOException {
    avlBlockFile.force();
  }

  /**
   * Unmaps the file.  This will have no effect if the file is not mapped.
   */
  public synchronized void unmap() {
    if (avlBlockFile != null) {
      avlBlockFile.unmap();
    }
  }

  /**
   * Close the file.
   *
   * @throws IOException If an I/O error occurs.
   */
  public synchronized void close() throws IOException {
    if (avlBlockFile != null) {
      avlBlockFile.close();
    }
  }

  /**
   * Deletes the file.
   *
   * @throws IOException If and I/O error occurs.
   */
  public synchronized void delete() throws IOException {
    try {
      if (avlBlockFile != null) {
        avlBlockFile.delete();
      }
    } finally {
      avlBlockFile = null;
    }
  }

  /**
   * This class represents a set of branches in the file which contain
   * a unique view of the data.
   */
  public final class Phase implements PersistableMetaRoot {

    // in longs
    /** The total size of a node and Phase information, in longs.  Not used. */
    public final static int RECORD_SIZE =
        AVLNode.HEADER_SIZE + ManagedBlockFile.Phase.RECORD_SIZE;

    /** The offset of the root ID, in longs. */
    private final static int IDX_ROOT_ID = 0;

    /** The offset of the number of nodes in the phase, in longs. */
    private final static int IDX_NR_NODES = 1;

    /** The size of the Phase header, in longs. */
    private final static int HEADER_SIZE = 2;

    /** The ID of the block at the root of this phase tree. */
    private long rootId;

    /** The number of nodes in this pase tree. */
    private long nrNodes;

    /** The phase on the block file. */
    private ManagedBlockFile.Phase avlBlockFilePhase;

    /**
     * Creates a new phase for this AVL tree.
     *
     * @throws IOException If and I/O error occurs.
     */
    public Phase() throws IOException {
      if (currentPhase == null) {
        rootId = AVLNode.NULL_NODE;
        nrNodes = 0;
      } else {
        rootId = currentPhase.rootId;
        nrNodes = currentPhase.nrNodes;
      }

      avlBlockFilePhase = avlBlockFile.new Phase();
      check();
      currentPhase = this;
    }

    /**
     * Constructs a phase, copying another phase.
     *
     * @param p The phase to copy.
     * @throws IOException If an I/O error occurs.
     */
    public Phase(Phase p) throws IOException {
      assert p != null;

      rootId = p.rootId;
      nrNodes = p.nrNodes;

      avlBlockFilePhase = avlBlockFile.new Phase(p.avlBlockFilePhase);
      check();
      currentPhase = this;
    }

    /**
     * Construct a phase, copy the phase data found in an existing block.
     *
     * @param b The block containing the phase data.
     * @param offset The offset of the phase data within the block.
     * @throws IOException If an I/O error occurs.
     */
    public Phase(Block b, int offset) throws IOException {
      rootId = b.getLong(offset + IDX_ROOT_ID);
      nrNodes = b.getLong(offset + IDX_NR_NODES);
      avlBlockFilePhase = avlBlockFile.new Phase(b, offset + HEADER_SIZE);
      check();
      currentPhase = this;
    }


    /**
     * Indicates if there are any remaining references to the current phase.
     *
     * @return <code>true</code> if the phase on the free list is still being referenced.
     */
    public boolean isInUse() {
      return avlBlockFilePhase.isInUse();
    }

    /**
     * Retrieve the number of AVL nodes in this phase.
     *
     * @return The number of AVL nodes in the tree.
     */
    public long getNrNodes() {
      return nrNodes;
    }

    /**
     * Get the AVLNode of the root of this phase tree.
     *
     * @return The AVL node at the root of this tree.
     */
    public AVLNode getRootNode() {
      return rootId != AVLNode.NULL_NODE ? AVLNode.newInstance(this, null, 0, rootId) : null;
    }

    /**
     * Check if the tree contains any data.
     *
     * @return <code>true</code> if the tree is empty.
     */
    public boolean isEmpty() {
      return rootId == AVLNode.NULL_NODE;
    }

    /**
     * Writes this PersistableMetaRoot to the specified Block. The ints are
     * written at the specified offset.
     *
     * @param b the Block.
     * @param offset the offset into the Block to start writing.
     */
    public void writeToBlock(Block b, int offset) {
      check();
      b.putLong(offset + IDX_ROOT_ID, rootId);
      b.putLong(offset + IDX_NR_NODES, nrNodes);
      avlBlockFilePhase.writeToBlock(b, offset + HEADER_SIZE);
    }

    /**
     * Checks if this phase if the current phase (the writing phase).
     *
     * @return <code>true</code> if this phase is the current one.
     */
    boolean isCurrent() {
      return this == currentPhase;
    }

    /**
     * Insert the first AVLNode in the tree.
     *
     * @param newNode The node to become the only node in the tree.
     * @throws IOException If an I/O error occurred.
     */
    public void insertFirst(AVLNode newNode) throws IOException {
      if (!isEmpty()) {
        throw new IllegalStateException(
            "insertFirst() called on AVL tree that is not empty");
      }

      // Tree is empty.  Set the root.
      setRootId(newNode.getId());
      incNrNodes();
    }

    /**
     * Finds an AVLNode containing the requested data.
     *
     * @param comparator The means of comparing the key to the payload in the AVLNodes.
     * @param key The data to search for.
     * @return An array of nodes.  If the tree is empty, then <code>null</code>.
     *     If the data exists, then a single element array containing the required node.
     *     If the data does not exist then return the pair of nodes that the data exists between.
     */
    public AVLNode[] find(AVLComparator comparator, long[] key) {

      AVLNode rootNode = getRootNode();
      if (rootNode == null) return null;

      try {
        return AVLNode.findDown(rootNode, comparator, key);
      } finally {
        rootNode.release();
      }
    }

    /**
     * Get a new AVLNode, unattached to any data.
     *
     * @return The new node.
     * @throws IOException If an I/O error occurred.
     */
    public AVLNode newAVLNodeInstance() throws IOException {
      return AVLNode.newInstance(this);
    }

    /**
     * Add a new reference to this phase.
     *
     * @return a {@link Token} representing a reference to this phase.
     */
    public Token use() {
      return new Token();
    }

    /**
     * Sets the root of this phase to a particular node.
     *
     * @param rootId The ID of the AVLNode to use.
     */
    void setRootId(long rootId) {
      if (this != currentPhase) {
        throw new IllegalStateException(
            "Attempt to set the rootId on a read-only phase."
        );
      }

      this.rootId = rootId;
    }

    /**
     * Get the phase of the underlying block file.
     *
     * @return The {@link ManagedBlockFile.Phase} associated with this phase.
     */
    ManagedBlockFile.Phase getAVLBlockFilePhase() {
      return avlBlockFilePhase;
    }

    /**
     * Increment the number of nodes in this phase of the AVLTree.
     */
    void incNrNodes() {
      ++nrNodes;
    }

    /**
     * Decrement the number of nodes in this phase of the AVLTree.
     */
    void decNrNodes() {
      assert nrNodes > 0;
      --nrNodes;
    }

    /**
     * Debug check that the structure of the tree is as expected.
     */
    private void check() {
      assert rootId != AVLNode.NULL_NODE || nrNodes == 0 :
          "AVLFile is empty but nrNodes == " + nrNodes;
      assert rootId == AVLNode.NULL_NODE || nrNodes > 0 :
          "AVLFile not empty but nrNodes == " + nrNodes;
    }

    /**
     * This class represents a reference to the enclosing phase.
     */
    public final class Token {

      /** Token for the matching phase of the underlying block file. */
      private ManagedBlockFile.Phase.Token avlBlockFileToken;

      /**
       * Creates the token, adding a reference count to the block file phase.
       */
      Token() {
        avlBlockFileToken = avlBlockFilePhase.use();
      }

      /**
       * Get the phase for this token.
       *
       * @return The encapsulating phase.
       */
      public Phase getPhase() {
        assert avlBlockFileToken != null : "Invalid Token";

        return Phase.this;
      }

      /**
       * Releases this reference to the encapsulating phase.
       */
      public void release() {
        assert avlBlockFileToken != null : "Invalid Token";
        avlBlockFileToken.release();
        avlBlockFileToken = null;
      }
    }
  }
}
