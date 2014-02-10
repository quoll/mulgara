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
 * These are the nodes which make up an AVL tree.
 * Each node contains up to two children, with the difference between the maximum
 * depths of the left and right children being no more than one.  This difference
 * is called the balance.
 *
 * @created 2002-10-04
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
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class AVLNode {

  /** The index in longs within the block of the left child node. */
  private final static int IDX_LEFT = 0;

  /** The index in longs within the block of the right child node. */
  private final static int IDX_RIGHT = IDX_LEFT + 1;

  /** The size in longs of the header data for the node. */
  public final static int HEADER_SIZE = IDX_RIGHT + 1;

  /** The size in bytes of the header data for the node. */
  private final static int HEADER_SIZE_B = HEADER_SIZE * Constants.SIZEOF_LONG;

  /** The size in bytes of the header data for the node. */
  public final static int HEADER_SIZE_I = HEADER_SIZE * 2;

  /** The index in bytes within the block of the balance of the node. */
  private final static int IDX_BALANCE_B = HEADER_SIZE_B;

  /** The index in bytes within the block of the payload of the node. */
  public final static int IDX_PAYLOAD_B = HEADER_SIZE_B;

  /** The index in integers within the block of the payload of the node. */
  public final static int IDX_PAYLOAD_I = HEADER_SIZE_I;

  /** The index in longs within the block of the payload of the node. */
  public final static int IDX_PAYLOAD = HEADER_SIZE;

  /** An ID to indicate that a Node is not valid. */
  final static long NULL_NODE = Block.INVALID_BLOCK_ID;

  /** The logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(AVLNode.class);

  /** The most recent phase that this node belongs to. */
  private AVLFile.Phase phase;

  /** The parent node of this node. */
  private AVLNode parentNode;

  /** Cached copy of left child node. */
  private AVLNode leftChildNode;

  /** Cached copy of right child node. */
  private AVLNode rightChildNode;

  /** The index of this node within the parent node. */
  private int childIndex;

  /** The block containing the data for this node. */
  private Block block;

  /** Whether or not this node can be modified. */
  private boolean writable;

  /** Whether or not this node has been modified. */
  private boolean dirty;

  //  private StackTrace trace;

  /** The number of references to this node. */
  private int refCount;

  // TODO we only need one of these per session - move it to the object
  // pool.

  /** Gets new calculated balances in the left and right children of this node. */
  private int[] newBalances = new int[2];

  /**
   * Creates a new AVLNode for a given AVLFile phase, and data in the file.
   *
   * @param phase The phase from the AVLFile.
   * @param parentNode The parent node to this node.
   * @param childIndex The index (left or right) of this node in relation to its parent.
   * @param nodeId The ID of this node, used for finding it in the file..
   */
  private AVLNode(AVLFile.Phase phase, AVLNode parentNode, int childIndex, long nodeId) {
    init(phase, parentNode, childIndex, nodeId);
  }

  /**
   * Constructs a new node for a given AVLFile phase.
   *
   * @param phase The phase form the AVLFile.
   * @throws IOException If there was an I/O exception.
   */
  private AVLNode(AVLFile.Phase phase) throws IOException {
    init(phase);
  }

  /**
   * Factory method for an AVLNode.
   *
   * @param phase The phase form the AVLFile.
   * @return The new node, backed by a new block in the file.
   * @throws IOException If there was an I/O exception.
   */
  public static AVLNode newInstance(AVLFile.Phase phase) throws IOException {
    if (!phase.isCurrent()) {
      throw new IllegalStateException("Attempt to allocate a new AVL node on a read-only phase.");
    }
    return new AVLNode(phase);
  }

  /**
   * Factory method for an AVLNode.
   *
   * @param phase The phase form the AVLFile.
   * @param parentNode The parent to this node.
   * @param childIndex The index of this node within its parent, either left or right.
   * @param nodeId The ID to find the data for this node in the file.
   * @return The new node, read from file, or with new file info in it.
   * @throws IOException If there was an I/O exception.
   */
  static AVLNode newInstance(AVLFile.Phase phase, AVLNode parentNode, int childIndex, long nodeId) {
    return new AVLNode(phase, parentNode, childIndex, nodeId);
  }

  /**
   * Calculates new balance factors and the change in height of a subtree which
   * has undergone a single rotate.
   *
   * @param balance The balance of the initial root of the subtree.
   * @param balanceL The balance of the left subnode when heavy on the left,
   *     or the right subnode when heavy on the right.
   * @param newBalances new balances for the left and right subnodes.
   * @return The balance of th new root of the subtree.
   */
  private static int calcNewBalances(
      int balance, int balanceL, int[] newBalances
  ) {
    // variable names presume being heavy on the left.
    // invert the names when heavy on the right.
    int heightL = relHeight(balance);
    int heightR = relHeight( -balance);
    int heightLL = heightL + relHeight(balanceL);
    int heightLR = heightL + relHeight( -balanceL);
    int newHeightR = Math.max(heightLR, heightR) + 1;
    int newHeight = Math.max(heightLL, newHeightR) + 1;
    newBalances[0] = heightLL - newHeightR;
    newBalances[1] = heightLR - heightR;

    return newHeight;
  }

  /**
   * Get a relative height for children nodes, based on a balance factor.
   *
   * @param balance The balance factor for a node.
   * @return A relative height value, based on balances.
   */
  private static int relHeight(int balance) {
    return (balance < 0 ? balance : 0) - 1;
  }

  /**
   * Walks up the tree to get a reference to the root node corresponding
   * to the given node.  The refCount is not incremented so the caller
   * should call incRefCount() if the lifetime of the returned root node
   * should be longer than that of the given node.
   *
   * @param node the given node.
   * @return the root node corresponding to the given node.
   */
  public static AVLNode getRootNode(AVLNode node) {
    if (node != null) {
      while (node.parentNode != null) node = node.parentNode;
    }

    return node;
  }

  /**
   * Returns an AVLNode array with a single element (the found node) if the
   * node was found or with two elements if the node was not found. If the node
   * is not found then the nodes returned are the nodes that would preceed and
   * follow the node if it existed in the tree. One of the two elements is
   * either a leaf node or the parent of a leaf node and the other node may be
   * null.
   *
   * @param node the starting node.
   * @param comparator the comparator to use to compare a key and an AVLNode.
   * @param key the key.
   * @return the one or two element array of AVLNodes.
   */
  public static AVLNode[] find(
      AVLNode node, AVLComparator comparator, long[] key
  ) {
    if (node == null) return null;

    if (comparator.compare(key, node) == 0) {
      node.incRefCount();
      return new AVLNode[] {node};
    }

    // Walk to the root.
    while (node.parentNode != null) {
      node = node.parentNode;
    }

    return findDown(node, comparator, key);
  }

  /**
   * Returns an AVLNode array with a single element (the found node) if the
   * node was found or with two elements if the node was not found. If the node
   * is not found then the nodes returned are the nodes that would preceed and
   * follow the node if it existed in the tree. One of the two elements is
   * either a leaf node or the parent of a leaf node and the other node may be
   * null.
   *
   * @param node the starting node.
   * @param comparator the comparator to use to compare a key and an AVLNode.
   * @param key the key.
   * @return the one or two element array of AVLNodes.
   */
  static AVLNode[] findDown(
      AVLNode node, AVLComparator comparator, long[] key
  ) {
    assert node != null;

    node.incRefCount();

    // Now search down the tree.
    AVLNode nextNode;
    AVLNode lastLeftChildNode = null;
    AVLNode lastRightChildNode = null;

    int c;
    while ((c = comparator.compare(key, node)) != 0) {
      if (c < 0) {
        if ((nextNode = node.getLeftChildNode_N()) == null) {
          if (lastRightChildNode != null) {
            lastRightChildNode.incRefCount();
          }
          return new AVLNode[] {lastRightChildNode, node};
        }
        lastLeftChildNode = node;
      } else {
        if ((nextNode = node.getRightChildNode_N()) == null) {
          if (lastLeftChildNode != null) {
            lastLeftChildNode.incRefCount();
          }
          return new AVLNode[] {node, lastLeftChildNode};
        }
        lastRightChildNode = node;
      }

      node = nextNode;
    }

    return new AVLNode[] {node};
  }

  /**
   * Gets the Id attribute of the AVLNode object
   *
   * @return The Id value
   */
  public long getId() {
    return block.getBlockId();
  }

  /**
   * Gets the LeafNode attribute of the AVLNode object
   *
   * @return The LeafNode value
   */
  public boolean isLeafNode() {
    return (block.getLong(IDX_LEFT) == NULL_NODE) &&
        (block.getLong(IDX_RIGHT) == NULL_NODE);
  }

  /**
   * Gets the ParentNode attribute of the AVLNode object and releases the current node.
   * @deprecated
   *
   * @return The ParentNode value
   */
  public AVLNode getParentNode_R() {
    AVLNode node = getParentNode();
    release();

    return node;
  }

  /**
   * Gets the ParentNode attribute of the AVLNode object
   *
   * @return The ParentNode value
   */
  public AVLNode getParentNode() {
    if (parentNode != null) parentNode.incRefCount();

    return parentNode;
  }

  /**
   * Returns the left child node or null if there is no left child node. The
   * current node is released.
   *
   * @return the left child node or null if there is no left child node.
   */
  public AVLNode getLeftChildNode_R() {
    AVLNode node = getLeftChildNode_N();

    if (node == null) {
      release();
    }

    return node;
  }

  /**
   * Returns the left child node or null if there is no left child node. The
   * current node is not released.
   *
   * @return the left child node or null if there is no left child node.
   */
  public AVLNode getLeftChildNode() {
    assert refCount > 0;

    AVLNode node = leftChildNode;

    if (node != null) {
      assert node.parentNode == this;
      assert node.getId() == getLeftId();
      node.incRefCount();

      return node;
    }

    node = getChildNode_N(IDX_LEFT);
    leftChildNode = node;

    if (node != null) {
      incRefCount();
    }

    return node;
  }

  /**
   * Returns the right child node or null if there is no right child node. The
   * current node is released.
   *
   * @return the right child node or null if there is no right child node.
   */
  public AVLNode getRightChildNode_R() {
    AVLNode node = getRightChildNode_N();

    if (node == null) {
      release();
    }

    return node;
  }

  /**
   * Returns the right child node or null if there is no right child node. The
   * current node is not released.
   *
   * @return the right child node or null if there is no right child node.
   */
  public AVLNode getRightChildNode() {
    assert refCount > 0;

    AVLNode node = rightChildNode;

    if (node != null) {
      assert node.parentNode == this;
      assert node.getId() == getRightId();
      node.incRefCount();

      return node;
    }

    node = getChildNode_N(IDX_RIGHT);
    rightChildNode = node;

    if (node != null) {
      incRefCount();
    }

    return node;
  }

  /**
   * Gets the PrevNode attribute of the AVLNode object.  The current node is released.
   *
   * @return The PrevNode_R value
   */
  public AVLNode getPrevNode_R() {
    AVLNode node = getPrevNode();
    release();

    return node;
  }

  /**
   * Gets the PrevNode attribute of the AVLNode object.  The current node is not released.
   *
   * @return The PrevNode value
   */
  public AVLNode getPrevNode() {
    AVLNode node;

    if ((node = getLeftChildNode()) != null) {
      return node.getMaxNode_R();
    }

    node = this;

    while (node.childIndex == IDX_LEFT) {
      if ((node = node.parentNode) == null) {
        return null;
      }
    }

    node = node.parentNode;

    if (node != null) {
      node.incRefCount();
    }

    return node;
  }

  /**
   * Gets the NextNode attribute of the AVLNode object.  The current node is released.
   *
   * @return The NextNode_R value
   */
  public AVLNode getNextNode_R() {
    AVLNode node = getNextNode();
    release();

    return node;
  }

  /**
   * Gets the NextNode attribute of the AVLNode object.  The current node is not released.
   *
   * @return The NextNode value
   */
  public AVLNode getNextNode() {
    AVLNode node;

    if ((node = getRightChildNode()) != null) {
      return node.getMinNode_R();
    }

    node = this;

    while (node.childIndex == IDX_RIGHT) {
      if ((node = node.parentNode) == null) {
        return null;
      }
    }

    node = node.parentNode;

    if (node != null) {
      node.incRefCount();
    }

    return node;
  }

  /**
   * Gets the Block of the AVLNode object
   *
   * @return The Block value
   */
  public Block getBlock() {
    return block;
  }

  /**
   * Gets an integer from the AVLNode payload
   *
   * @param offset The offset into the payload
   * @return The requested integer value
   */
  public int getPayloadInt(int offset) {
    assert offset > 0;
    return block.getInt(IDX_PAYLOAD_I + offset);
  }

  /**
   * Gets an unsigned integer from the AVLNode payload
   *
   * @param offset The offset into the payload
   * @return The requested unsigned integer value
   */
  public long getPayloadUInt(int offset) {
    assert offset > 0;
    return block.getUInt(IDX_PAYLOAD_I + offset);
  }

  /**
   * Gets a long from the AVLNode payload
   *
   * @param offset The offset into the payload (in longs).
   * @return The requested long value.
   */
  public long getPayloadLong(int offset) {
    assert offset > 0;
    return block.getLong(IDX_PAYLOAD + offset);
  }

  /**
   * Gets a byte from the AVLNode payload
   *
   * @param offset The offset into the payload (in bytes).
   * @return The requested byte value
   */
  public int getPayloadByte(int offset) {
    assert offset > 0;
    return block.getByte(IDX_PAYLOAD_B + offset);
  }

  /**
   * Gets the minimum node to the left of this AVLNode (the smallest node
   * in this subtree).  The current node has its reference released.
   *
   * @return The node under this node containing the smallest values, or
   *     the current node if this is the smallest.
   */
  public AVLNode getMinNode_R() {
    AVLNode node = this;
    AVLNode nextNode;

    while ((nextNode = node.getLeftChildNode_N()) != null) {
      node = nextNode;
    }

    return node;
  }

  /**
   * Gets the minimum node to the left of this AVLNode (the smallest node
   * in this subtree).  The current node is not released.
   *
   * @return The node under this node containing the smallest values, or
   *     the current node if this is the smallest.
   */
  public AVLNode getMinNode() {
    incRefCount();

    return getMinNode_R();
  }

  /**
   * Gets the maximum node to the right of this AVLNode (the largest node
   * in this subtree).  The current node has its reference released.
   *
   * @return The node under this node containing the largest values, or
   *     the current node if this is the largest.
   */
  public AVLNode getMaxNode_R() {
    AVLNode node = this;
    AVLNode nextNode;

    while ((nextNode = node.getRightChildNode_N()) != null) {
      node = nextNode;
    }

    return node;
  }

  /**
   * Gets the maximum node to the right of this AVLNode (the largest node
   * in this subtree).  The current node is not released.
   *
   * @return The node under this node containing the largest values, or
   *     the current node if this is the largest.
   */
  public AVLNode getMaxNode() {
    incRefCount();

    return getMaxNode_R();
  }

  /**
   * Gets the Height attribute of the AVLNode object
   *
   * @return The Height value
   */
  public int getHeight() {
    AVLNode lChildNode = getLeftChildNode();
    int leftHeight = (lChildNode != null) ? lChildNode.getHeight() : 0;

    if (lChildNode != null) {
      lChildNode.release();
    }

    AVLNode rChildNode = getRightChildNode();
    int rightHeight = (rChildNode != null) ? rChildNode.getHeight() : 0;

    if (rChildNode != null) {
      rChildNode.release();
    }

    // Check the balance value.
    int balance = leftHeight - rightHeight;

    if (balance != block.getByte(IDX_BALANCE_B)) {
      throw new RuntimeException(
          "Incorrect balance for node " + getId() +
          ".  Expected: " + balance + " but was: " +
          block.getByte(IDX_BALANCE_B)
      );
    }

    return Math.max(leftHeight, rightHeight) + 1;
  }

  /**
   * Increment the reference count of this node.
   * This means that the node will need to be released one
   * more time before it can be considered unused.
   */
  public void incRefCount() {
    assert refCount > 0;
    ++refCount;
  }

  /**
   * If the block belongs to an older phase, move it to the new phase
   * by creating a copy.
   *
   * @throws IOException If an I/O error occurred.
   */
  public void modify() throws IOException {
    assert refCount > 0;

    if (writable) {
      dirty = true;

      return;
    }

    if (!phase.isCurrent()) {
      throw new IllegalStateException(
          "Attempt to modify a node for a read-only phase."
      );
    }

    long id = block.getBlockId();
    block.modify();
    writable = true;
    dirty = true;

    long newId = block.getBlockId();

    if (newId != id) {
      if (parentNode != null) {
        parentNode.modify();
        parentNode.block.putLong(childIndex, newId);
      } else {
        phase.setRootId(newId);
      }
    }
  }

  /**
   * Puts an integer into the payload of this node.
   *
   * @param offset The offset into the payload, in ints.
   * @param i The value to put into the payload.
   */
  public void putPayloadInt(int offset, int i) {
    assert dirty;
    assert offset > 0;
    block.putInt(IDX_PAYLOAD_I + offset, i);
  }

  /**
   * Puts an unsigned integer into the payload of this node.
   *
   * @param offset The offset into the payload, in ints.
   * @param ui The unsigned integer value to put into the payload.
   */
  public void putPayloadUInt(int offset, long ui) {
    assert dirty;
    assert offset > 0;
    block.putUInt(IDX_PAYLOAD_I + offset, ui);
  }

  /**
   * Puts a long into the payload of this node.
   *
   * @param offset The offset into the payload, in longs.
   * @param l The value to put into the payload.
   */
  public void putPayloadLong(int offset, long l) {
    assert dirty;
    assert offset > 0;
    block.putLong(IDX_PAYLOAD + offset, l);
  }

  /**
   * Puts an array of longs into the payload of this node.
   *
   * @param offset The offset into the payload for the beginning
   *     of the array, in longs.
   * @param la The array to put into the payload.
   */
  public void putPayload(int offset, long[] la) {
    assert dirty;
    assert offset > 0;
    block.put(IDX_PAYLOAD + offset, la);
  }

  /**
   * Puts a byte into the payload of this node.
   *
   * @param offset The offset into the payload, in bytes.
   * @param b The value to put into the payload.
   */
  public void putPayloadByte(int offset, byte b) {
    assert dirty;
    assert offset > 0;
    block.putByte(IDX_PAYLOAD_B + offset, b);
  }

  // The following methods work on the current phase tree

  /**
   * Inserts an AVLNode into the current phase tree, under the current node.
   * The current node must not have a child under it on the <em>ci</em> side.
   *
   * @param newNode The node to insert.
   * @param ci The index for insertion - {@link #IDX_LEFT} or {@link #IDX_RIGHT}
   * @throws IOException If an I/O error occurred.
   */
  public void insert(AVLNode newNode, int ci) throws IOException {
    if (newNode.parentNode != null) {
      throw new IllegalArgumentException("newNode already has parent node.");
    }

    if (ci != 0 && ci != 1) {
      throw new IllegalArgumentException("ci: " + ci);
    }

    if (block.getLong(ci) != NULL_NODE) {
      throw new IllegalStateException("Non-leaf inserts are illegal.");
    }

    modify();
    newNode.parentNode = this;
    newNode.childIndex = ci;
    incRefCount();

    if (ci == IDX_LEFT) {
      leftChildNode = newNode;
    } else {
      rightChildNode = newNode;
    }

    block.putLong(ci, newNode.getId());
    rebalanceInsert(ci);
    phase.incNrNodes();
  }

  /**
   * Remove this node from the AVL Tree.
   *
   * @throws IOException An I/O error occurred.
   */
  public void remove() throws IOException {
    assert refCount == 1;

    if (isLeafNode()) {
      assert leftChildNode == null;
      assert rightChildNode == null;

      if (parentNode == null) {
        phase.setRootId(NULL_NODE);
      } else {
        AVLNode node = parentNode;
        node.modify();
        parentNode = null;

        if (childIndex == IDX_LEFT) {
          node.leftChildNode = null;
        } else {
          node.rightChildNode = null;
        }

        node.block.putLong(childIndex, NULL_NODE);
        node.rebalanceRemove(childIndex);
        node.release();
      }
    } else {
      // Move an adjacent node to replace this node in the tree.
      AVLNode childNode;
      AVLNode adjacentNode;
      int ci;

      if (block.getByte(IDX_BALANCE_B) > 0) {
        childNode = getLeftChildNode();
        adjacentNode = childNode.getMaxNode_R();
        ci = IDX_LEFT;
      } else {
        childNode = getRightChildNode();
        adjacentNode = childNode.getMinNode_R();
        ci = IDX_RIGHT;
      }

      // Save adjacent node pointers.
      long adjacentNodeChildId = adjacentNode.block.getLong(ci);
      AVLNode adjacentParent = adjacentNode.parentNode;
      int adjacentParentChildIndex = adjacentNode.childIndex;

      // Replace this node in the tree with the adjacent node.
      adjacentNode.modify();
      adjacentNode.parentNode = parentNode;
      adjacentNode.leftChildNode = null;
      adjacentNode.rightChildNode = null;
      adjacentNode.childIndex = childIndex;
      adjacentNode.block.putLong(IDX_LEFT, block.getLong(IDX_LEFT));
      adjacentNode.block.putLong(IDX_RIGHT, block.getLong(IDX_RIGHT));
      adjacentNode.block.putByte(IDX_BALANCE_B, block.getByte(IDX_BALANCE_B));

      if (parentNode == null) {
        phase.setRootId(adjacentNode.getId());
      } else {
        if (childIndex == IDX_LEFT) {
          parentNode.leftChildNode = adjacentNode;
        } else {
          parentNode.rightChildNode = adjacentNode;
        }

        parentNode.modify();
        parentNode.block.putLong(childIndex, adjacentNode.getId());
      }

      assert refCount == 2;
      assert adjacentNode.refCount == 1;
      parentNode = null;
      leftChildNode = null;
      rightChildNode = null;
      refCount = 1;

      if (childNode != adjacentNode) {
        childNode.parentNode = adjacentNode;

        if (ci == IDX_LEFT) {
          adjacentNode.leftChildNode = childNode;
        } else {
          adjacentNode.rightChildNode = childNode;
        }

        // Reparent the child of adjacentNode if any.
        if (adjacentParentChildIndex == IDX_LEFT) {
          adjacentParent.leftChildNode = null;
        } else {
          adjacentParent.rightChildNode = null;
        }

        adjacentParent.modify();
        adjacentParent.block.putLong(
            adjacentParentChildIndex, adjacentNodeChildId
        );

        adjacentParent.rebalanceRemove(adjacentParentChildIndex);
        adjacentParent.release();
      } else {
        adjacentNode.modify();
        adjacentNode.block.putLong(
            adjacentParentChildIndex, adjacentNodeChildId
        );
        adjacentNode.rebalanceRemove(ci);
        adjacentNode.release();
      }
    }

    phase.decNrNodes();
    free();
  }

  /**
   * Writes the Block for this node to the file.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void write() throws IOException {
    if (!writable) {
      throw new IllegalStateException("AVLNode not writable");
    }

    if (dirty) {
      block.write();
      dirty = false;
    }
  }

  /**
   * Release a reference to this AVLNode.  When there are no references
   * left the block is written back to disk, and the node is put back
   * into the node pool.
   */
  public void release() {
    AVLNode avlNode = this;

    do {
      assert avlNode.refCount > 0;

      if (--avlNode.refCount > 0) return;

      assert avlNode.leftChildNode == null;
      assert avlNode.rightChildNode == null;

      avlNode.phase = null;

      if (avlNode.block != null) {
        if (avlNode.writable) {
          try {
            avlNode.write();
          } catch (IOException ex) {
            throw new Error("IOException", ex);
          }
        }

        avlNode.block = null;
      }

      avlNode.writable = false;

      //X     avlNode.trace = null;
      AVLNode prevNode = avlNode;
      avlNode = avlNode.parentNode;
      prevNode.parentNode = null;

      if (avlNode != null) {
        if (prevNode.childIndex == IDX_LEFT) {
          avlNode.leftChildNode = null;
        } else {
          avlNode.rightChildNode = null;
        }
      }
    }
    while (avlNode != null);
  }

  /**
   * Gets a string representation of the subtree rooted at this node.
   *
   * @return A string showing the structure of the tree.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    toString(sb);

    return sb.toString();
  }

  /**
   * Gets the ID of the left child node to this node.
   *
   * @return The ID of the left child node.
   */
  long getLeftId() {
    return block.getLong(IDX_LEFT);
  }

  /**
   * Gets the ID of the right child node to this node.
   *
   * @return The ID of the right child node.
   */
  long getRightId() {
    return block.getLong(IDX_RIGHT);
  }

  /**
   * Gets the Balance of the AVLNode
   *
   * @return The Balance value
   */
  int getBalance() {
    return block.getByte(IDX_BALANCE_B);
  }

  /**
   * Returns the left child node or null if there is no left child node. The
   * current node is released if and only if there is a left child node.
   *
   * @return the left child node or null if there is no left child node.
   */
  private AVLNode getLeftChildNode_N() {
    AVLNode node = leftChildNode;

    if (node != null) {
      assert refCount > 1;
      assert node.parentNode == this;
      assert node.getId() == getLeftId();
      node.incRefCount();
      --refCount;

      return node;
    }

    assert refCount > 0;
    node = getChildNode_N(IDX_LEFT);
    leftChildNode = node;

    return node;
  }

  /**
   * Returns the right child node or null if there is no right child node. The
   * current node is released if and only if there is a right child node.
   *
   * @return the right child node or null if there is no right child node.
   */
  private AVLNode getRightChildNode_N() {
    AVLNode node = rightChildNode;

    if (node != null) {
      assert refCount > 1;
      assert node.parentNode == this;
      assert node.getId() == getRightId();
      node.incRefCount();
      --refCount;

      return node;
    }

    assert refCount > 0;
    node = getChildNode_N(IDX_RIGHT);
    rightChildNode = node;

    return node;
  }

  /**
   * Gets the ChildNode on a given side of the current node.
   *
   * @param index The index of the child.  Either {@link #IDX_LEFT}
   * or {@link #IDX_RIGHT}
   * @return The Child node, or <code>null</code> if there is no
   * child on the given side.
   */
  private AVLNode getChildNode_N(int index) {
    assert refCount > 0;

    long nodeId = block.getLong(index);

    return nodeId == NULL_NODE ? null : newInstance(phase, this, index, nodeId);
  }

  /**
   * Initialises a node on the given information.
   *
   * @param phase The phase that the node exists in.
   * @param parentNode The parent node for this node.  Only
   *     <code>null</code> at the root of the tree.
   * @param childIndex Indicates if this is to the left or the
   *     right of the parent node.  Either {@link #IDX_LEFT} or
   *     {@link #IDX_RIGHT}.
   * @param nodeId This ID for this node.
   */
  private void init(AVLFile.Phase phase, AVLNode parentNode, int childIndex, long nodeId) {

    this.phase = phase;
    this.parentNode = parentNode;
    this.childIndex = childIndex;
    this.leftChildNode = null;
    this.rightChildNode = null;

    //X    trace = new StackTrace();
    refCount = 1;
    writable = false;
    dirty = false;

    try {
      block = phase.getAVLBlockFilePhase().readBlock(nodeId);
    } catch (IOException ex) {
      throw new Error("IOException", ex);
    }
  }

  /**
   * Initialises a node on the given information.
   *
   * @param phase The phase that the node exists in.
   * @throws IOException If an I/O error occurs.
   */
  private void init(AVLFile.Phase phase) throws IOException {

    this.phase = phase;
    parentNode = null;
    childIndex = 0;
    this.leftChildNode = null;
    this.rightChildNode = null;

    //X    trace = new StackTrace();
    refCount = 1;
    writable = true;
    dirty = true;
    block = phase.getAVLBlockFilePhase().allocateBlock();
    block.putLong(IDX_LEFT, NULL_NODE);
    block.putLong(IDX_RIGHT, NULL_NODE);
    block.putByte(IDX_BALANCE_B, (byte) 0);
  }

  /**
   * Frees all resources from this node, and releases its reference.
   * This should be the last reference to the node.
   *
   * @throws IOException If an I/O error occurs.
   */
  private void free() throws IOException {
    writable = false;
    dirty = false;
    block.free();
    block = null;

    release();
  }

  /**
   * Perform a rebalance from this node down, after having done an insert.
   *
   * @param ci The index of this node in its parent.  Either {@link #IDX_LEFT}
   * or {@link #IDX_RIGHT}.
   * @throws IOException If an I/O error occurs.
   */
  private void rebalanceInsert(int ci) throws IOException {
    AVLNode node = this;
    int balance;

    do {
      // Assumes ci is 0 for left and 1 for right.
      balance = (node.block.getByte(IDX_BALANCE_B) + 1) - (ci * 2);
      node.modify();
      node.block.putByte(IDX_BALANCE_B, (byte) balance);

      if (balance == 2) {
        node.incRefCount();
        node.rotateL();
        node.release();

        break;
      } else if (balance == -2) {
        node.incRefCount();
        node.rotateR();
        node.release();

        break;
      }

      ci = node.childIndex;

      // break if balance is zero
    }
    while (balance != 0 && (node = node.parentNode) != null);
  }

  /**
   * Perform a rebalance from this node up, after having removed a node.
   *
   * @param ci The index of this node in its parent.  Either {@link #IDX_LEFT}
   * or {@link #IDX_RIGHT}.
   * @throws IOException If an I/O error occurs.
   */
  private void rebalanceRemove(int ci) throws IOException {
    AVLNode node = this;
    int balance;

    do {
      // Assumes ci is 0 for left and 1 for right.
      balance = node.block.getByte(IDX_BALANCE_B) - (1 - (ci * 2));
      node.modify();
      node.block.putByte(IDX_BALANCE_B, (byte) balance);

      if (balance == 2) {
        node.incRefCount();

        int deltaHeight = node.rotateL();
        node.release();

        if (deltaHeight == 0) {
          break;
        }

        // Continue processing for a remove.
        node = node.parentNode;
        balance = 0;
      } else if (balance == -2) {
        node.incRefCount();

        int deltaHeight = node.rotateR();
        node.release();

        if (deltaHeight == 0) {
          break;
        }

        // Continue processing for a remove.
        node = node.parentNode;
        balance = 0;
      }

      ci = node.childIndex;

      // break if balance is non-zero
    }
    while (balance == 0 && (node = node.parentNode) != null);
  }

  /**
   * The rotate which is performed when the node is heavy on the left
   * sub-branch.
   *
   * @return the change in height of the node.
   * @throws IOException If an I/O error occurs.
   */
  private int rotateL() throws IOException {
    AVLNode nodeL = getLeftChildNode();
    if (nodeL == null) throw new IllegalStateException("Invalid tree structure on disk");
    int deltaHeight = (nodeL.block.getByte(IDX_BALANCE_B) >= 0) ?
        rotateLL(nodeL) : rotateLR(nodeL);
    nodeL.release();

    return deltaHeight;
  }

  /**
   * The rotate which is performed when the node is heavy on the right
   * sub-branch.
   *
   * @return the change in height of the node.
   * @throws IOException If an I/O error occurs.
   */
  private int rotateR() throws IOException {
    AVLNode nodeR = getRightChildNode();
    if (nodeR == null) throw new IllegalStateException("Invalid tree structure on disk");
    int deltaHeight = (nodeR.block.getByte(IDX_BALANCE_B) <= 0) ?
        rotateRR(nodeR) : rotateRL(nodeR);
    nodeR.release();

    return deltaHeight;
  }

  /**
   * The rotate which is performed when the node is heavy on the left child's
   * left sub-branch.
   *
   * @param nodeL a reference to the left child.
   * @return the change in height of the node.
   * @throws IOException If an I/O error occurs.
   */
  private int rotateLL(AVLNode nodeL) throws IOException {
    assert refCount > 1;
    assert nodeL.parentNode == this;
    assert leftChildNode == nodeL;
    assert nodeL.getId() == getLeftId();

    leftChildNode = nodeL.rightChildNode;
    nodeL.rightChildNode = this;

    nodeL.modify();
    modify();
    block.putLong(IDX_LEFT, nodeL.block.getLong(IDX_RIGHT));
    nodeL.block.putLong(IDX_RIGHT, getId());

    // Change the parent node's child pointer.
    if (parentNode != null) {
      if (childIndex == IDX_LEFT) {
        parentNode.leftChildNode = nodeL;
      } else {
        parentNode.rightChildNode = nodeL;
      }

      parentNode.modify();
      parentNode.block.putLong(childIndex, nodeL.getId());
    }

    if (leftChildNode != null) {
      leftChildNode.parentNode = this;
      leftChildNode.childIndex = IDX_LEFT;
    } else {
      --refCount;
      nodeL.incRefCount();
    }

    nodeL.parentNode = parentNode;
    nodeL.childIndex = childIndex;
    parentNode = nodeL;
    childIndex = IDX_RIGHT;

    // Recalculate the balances.
    int deltaHeight = calcNewBalances(
        block.getByte(IDX_BALANCE_B), nodeL.block.getByte(IDX_BALANCE_B),
        newBalances
    );
    nodeL.block.putByte(IDX_BALANCE_B, (byte) newBalances[0]);
    block.putByte(IDX_BALANCE_B, (byte) newBalances[1]);

    if (nodeL.parentNode == null) {
      phase.setRootId(nodeL.getId());
    }

    return deltaHeight;
  }

  /**
   * The rotate which is performed when the node is heavy on the right child's
   * right sub-branch.
   *
   * @param nodeR a reference to the right child.
   * @return the change in height of the node.
   * @throws IOException If an I/O error occurs.
   */
  private int rotateRR(AVLNode nodeR) throws IOException {
    assert refCount > 1;
    assert nodeR.parentNode == this;
    assert rightChildNode == nodeR;
    assert nodeR.getId() == getRightId();

    rightChildNode = nodeR.leftChildNode;
    nodeR.leftChildNode = this;

    nodeR.modify();
    modify();
    block.putLong(IDX_RIGHT, nodeR.block.getLong(IDX_LEFT));
    nodeR.block.putLong(IDX_LEFT, getId());

    // Change the parent node's child pointer.
    if (parentNode != null) {
      if (childIndex == IDX_LEFT) {
        parentNode.leftChildNode = nodeR;
      } else {
        parentNode.rightChildNode = nodeR;
      }

      parentNode.modify();
      parentNode.block.putLong(childIndex, nodeR.getId());
    }

    if (rightChildNode != null) {
      rightChildNode.parentNode = this;
      rightChildNode.childIndex = IDX_RIGHT;
    } else {
      --refCount;
      nodeR.incRefCount();
    }

    nodeR.parentNode = parentNode;
    nodeR.childIndex = childIndex;
    parentNode = nodeR;
    childIndex = IDX_LEFT;

    // Recalculate the balances.
    int deltaHeight = calcNewBalances(
        -block.getByte(IDX_BALANCE_B), -nodeR.block.getByte(IDX_BALANCE_B),
        newBalances
    );
    nodeR.block.putByte(IDX_BALANCE_B, (byte) - newBalances[0]);
    block.putByte(IDX_BALANCE_B, (byte) - newBalances[1]);

    if (nodeR.parentNode == null) {
      phase.setRootId(nodeR.getId());
    }

    return deltaHeight;
  }

  /**
   * The rotate which is performed when the node is heavy on the left child's
   * right sub-branch.
   *
   * @param nodeL a reference to the left child.
   * @return the change in height of the node.
   * @throws IOException If an I/O error occurs.
   */
  private int rotateLR(AVLNode nodeL) throws IOException {
    // First perform rotateRR on nodeL.
    AVLNode nodeLR = nodeL.getRightChildNode();
    if (nodeLR == null) throw new IllegalStateException("Invalid tree structure on disk");
    int deltaHeightL = nodeL.rotateRR(nodeLR);

    // Adjust the balance of this node.
    if (deltaHeightL != 0) {
      modify();
      block.putByte(
          IDX_BALANCE_B, (byte)(block.getByte(IDX_BALANCE_B) + deltaHeightL)
      );
    }

    // Finally do a rotateLL on this node.
    int deltaHeight = deltaHeightL + rotateLL(nodeLR);
    nodeLR.release();

    return deltaHeight;
  }

  /**
   * The rotate which is performed when the node is heavy on the right child's
   * left sub-branch.
   *
   * @param nodeR a reference to the right child.
   * @return the change in height of the node.
   * @throws IOException If an I/O error occurs.
   */
  private int rotateRL(AVLNode nodeR) throws IOException {
    // First perform rotateLL on nodeR.
    AVLNode nodeRL = nodeR.getLeftChildNode();
    int deltaHeightR = nodeR.rotateLL(nodeRL);

    // Adjust the balance of this node.
    if (deltaHeightR != 0) {
      modify();
      block.putByte(
          IDX_BALANCE_B, (byte)(block.getByte(IDX_BALANCE_B) - deltaHeightR)
      );
    }

    // Finally do a rotateRR on this node.
    int deltaHeight = deltaHeightR + rotateRR(nodeRL);
    nodeRL.release();

    return deltaHeight;
  }

  /**
   * Writes a string representation of this tree structure into a
   * {@link java.lang.StringBuffer}.
   *
   * @param sb The string buffer to write to.
   */
  private void toString(StringBuffer sb) {
    sb.append(getId());
    sb.append(" [");

    AVLNode tmp;

    if ((tmp = getLeftChildNode()) == null) {
      sb.append("-");
    } else {
      tmp.toString(sb);
    }

    sb.append(", ");

    if ((tmp = getRightChildNode()) == null) {
      sb.append("-");
    } else {
      tmp.toString(sb);
    }

    sb.append("]");
  }

  //X  protected void finalize() {
  //X    if (trace != null) logger.warn(
  //X      "Unpooled AVLNode.  refCount=" + refCount + ", dirty=" + dirty + "\n " + trace
  //X    );
  //X  }

}
