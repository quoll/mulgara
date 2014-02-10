/*
 * Copyright 2008 Fedora Commons, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.store.stringpool.xa11;

import java.nio.ByteBuffer;

/**
 * This class manages the allocation and detection of blank nodes.
 *
 * @created Aug 15, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class BlankNodeAllocator {

  /** The bit that indicates a blank node. */
  static final long BLANK_NODE_BIT = 0x4000000000000000L;

  /** The mask that can remove the BLANK_NODE_BIT. */
  static final private long COUNTER_MASK = 0x3FFFFFFFFFFFFFFFL;

  /** The first valid blank node value. */
  static final long FIRST = 1;
  
  /** The next node to be allocated. Initialized to 1, but usually set by the metaroot file. */
  private long nextNode = FIRST;

  /** The last committed nextNode value. */
  private long committedNextNode = FIRST;

  /**
   * The constructor for a new blank node allocator.
   */
  public BlankNodeAllocator() {
  }


  /**
   * Creates a new blank node allocator, initialized from a data buffer.
   * @param data The data to initialize from.
   */
  public BlankNodeAllocator(ByteBuffer data, int offset) {
    committedNextNode = data.getLong(offset);
    nextNode = committedNextNode;
  }


  /**
   * Writes the current state to the current position in a data buffer.
   * @param data The buffer to write to.
   */
  public void writeTo(ByteBuffer data, int offset) {
    data.putLong(offset, committedNextNode);
  }


  /**
   * Get the next blank node from this allocator.
   * @return A GNode for a new blank node.
   */
  public synchronized long allocate() {
    return nextNode++ | BLANK_NODE_BIT;
  }


  /**
   * Test if a GNode is a blank node.
   * @param gNode The gNode to test.
   * @return <code>true</code> if the gNode is for a blank node.
   */
  public static boolean isBlank(long gNode) {
    return (gNode & BLANK_NODE_BIT) != 0;
  }


  /**
   * Clear all values back to their initialized states.
   */
  public void clear() {
    nextNode = FIRST;
    committedNextNode = FIRST;
  }


  /**
   * Get the current internal state.
   * @return The next node value. This encodes all of the internal state.
   */
  public long getCurrentState() {
    return nextNode;
  }


  /**
   * Set the internal state. This is just the blank node counter.
   * @param state The state for this object.
   */
  public void setCurrentState(long state) {
    this.nextNode = state;
    committedNextNode = state;
  }


  /**
   * Prepares this object for commiting.
   * @param metaroot The object that will hold state data on disk.
   */
  public void prepare(XA11StringPoolImpl.Metaroot metaroot) {
    metaroot.setNextBlankNode(nextNode);
  }


  /**
   * Commits the prepared changes.
   */
  public void commit() {
    committedNextNode = nextNode;
  }


  /**
   * Go back to the last committed position.
   */
  public void rollback() {
    nextNode = committedNextNode;
  }


  /**
   * Convert a blank node code to a counter value.
   * @param blankGNode The blank node value.
   * @return A value with the blank node bit turned off.
   */
  public static final long nodeToCounter(long blankGNode) {
    return blankGNode & COUNTER_MASK;
  }


  /**
   * Convert a blank node code to a counter value.
   * @param counter The blank node value, with the blank node bit turned off.
   * @return A value with the blank node bit turned on.
   */
  public static final long counterToNode(long counter) {
    return counter | BLANK_NODE_BIT;
  }
}
