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
package org.mulgara.store.nodepool.xa;

import java.io.*;
import java.nio.*;

// Java 2 standard packages
import java.util.*;

// Third-party packages
import org.apache.log4j.Logger;  // log4j classes

// Locally written packages
import org.mulgara.store.nodepool.*;
import org.mulgara.store.xa.*;
import org.mulgara.util.Constants;
import org.mulgara.util.IntFile;
import org.mulgara.util.LongMapper;

/**
 * A NodePool implementation which supports data integrity.
 *
 * @created 2001-09-10
 *
 * @author David Makepeace
 * @author Michael Judd
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/22 08:16:45 $ by $Author: newmana $
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class XANodePoolImpl implements XANodePool {

  /**
   * Logger.
   */
  private final static Logger logger = Logger.getLogger(XANodePoolImpl.class);

  /**
   * Description of the Field
   */
  private final static int FILE_MAGIC = 0xa5eeefe4;

  /**
   * Description of the Field
   */
  private final static int FILE_VERSION = 5;

  /**
   * Index of the file magic number within each of the two on-disk metaroots.
   */
  private final static int IDX_MAGIC = 0;

  /**
   * Index of the file version number within each of the two on-disk metaroots.
   */
  private final static int IDX_VERSION = 1;

  /**
   * Index of the valid flag (in ints) within each of the two on-disk metaroots.
   */
  private final static int IDX_VALID = 2;

  /**
   * The index of the phase number in the on-disk phase.
   */
  private final static int IDX_PHASE_NUMBER = 3;

  /**
   * The size of the header of a metaroot in ints.
   */
  private final static int HEADER_SIZE_I = 4;

  /**
   * The size of the header of a metaroot in longs.
   */
  private final static int HEADER_SIZE = (HEADER_SIZE_I + 1) / 2;

  /**
   * The size of a metaroot in longs.
   */
  private final static int METAROOT_SIZE = HEADER_SIZE +
      FreeList.Phase.RECORD_SIZE;

  /**
   * Description of the Field
   */
  private final static int NR_METAROOTS = 2;

  /**
   * The name of the triple store which forms the base name for the node pool
   * data files.
   */
  private String fileName;

  /**
   * The LockFile that protects the node pool from being opened twice.
   */
  private LockFile lockFile;

  /**
   * The BlockFile for the node pool metaroot file.
   */
  private BlockFile metarootFile = null;

  /**
   * The metaroot blocks of the metaroot file.
   */
  private Block[] metarootBlocks = new Block[NR_METAROOTS];

  /**
   * Description of the Field
   */
  private boolean wrongFileVersion = false;

  /**
   * The node ID free list.
   */
  private FreeList freeList = null;

  /**
   * The current phase. This is the phase used for allocating and freeing nodes.
   */
  private FreeList.Phase currentPhase = null;

  /**
   * The index of the on-disk metaroot where the current phase will be stored
   * when {@link #prepare} is called.
   */
  private int phaseIndex = 0;

  /**
   * Description of the Field
   */
  private int phaseNumber = 0;

  /**
   * The token used to prevent the blocks used by the committed phase from being
   * reused.
   */
  private FreeList.Phase.Token committedPhaseToken = null;

  private Object committedPhaseLock = new Object();

  /**
   * The token used to prevent the blocks used by the phase which is being (or
   * has) forced to disk by {@link #prepare} from being reused.
   */
  private FreeList.Phase.Token recordingPhaseToken = null;

  /**
   * <code>true</code> if a phase has been prepared but not yet committed.
   */
  private boolean prepared = false;

  /**
   * Description of the Field
   */
  private Set<NewNodeListener> newNodeListeners = new HashSet<NewNodeListener>();


  /**
   * Instanciates a NodePoolImpl for the specified triple store. Either {@link
   * #clear} or {@link #recover} should be called after constructing a
   * NodePoolImpl and before any other method is called.
   *
   * @param fileName the name of the triple store which forms the base name of
   *      the node pool data files.
   * @throws IOException if an I/O error occurs.
   */
  public XANodePoolImpl(String fileName) throws IOException {
    this.fileName = fileName;

    lockFile = LockFile.createLockFile(fileName + ".np.lock");

    try {
      // Open the metaroot file.
      RandomAccessFile metarootRAF = null;
      try {
        metarootRAF = new RandomAccessFile(fileName + ".np", "r");
        if (metarootRAF.length() >= 2 * Constants.SIZEOF_INT) {
          int fileMagic = metarootRAF.readInt();
          int fileVersion = metarootRAF.readInt();
          if (AbstractBlockFile.byteOrder != ByteOrder.BIG_ENDIAN) {
            fileMagic = XAUtils.bswap(fileMagic);
            fileVersion = XAUtils.bswap(fileVersion);
          }
          wrongFileVersion =
              fileMagic != FILE_MAGIC || fileVersion != FILE_VERSION;
        } else {
          wrongFileVersion = false;
        }
      } catch (FileNotFoundException ex) {
        wrongFileVersion = false;
      } finally {
        if (metarootRAF != null) {
          metarootRAF.close();
        }
      }

      freeList = FreeList.openFreeList(fileName + ".np_fl");
    } catch (IOException ex) {
      try {
        close();
      } catch (NodePoolException ex2) {
        // NO-OP
      }
      throw ex;
    }
  }


  /**
   * Gets the NrValidNodes attribute of the XANodePoolImpl object
   *
   * @return The NrValidNodes value
   */
  public synchronized long getNrValidNodes() {
    checkInitialized();
    return currentPhase.getNrValidItems();
  }


  /**
   * Gets the PhaseNumber attribute of the XANodePoolImpl object
   *
   * @return The PhaseNumber value
   * @throws SimpleXAResourceException EXCEPTION TO DO
   */
  public synchronized int getPhaseNumber() throws SimpleXAResourceException {
    checkInitialized();
    return phaseNumber;
  }


  /**
   * Adds a feature to the NewNodeListener attribute of the XANodePoolImpl
   * object
   *
   * @param l The feature to be added to the NewNodeListener attribute
   */
  public synchronized void addNewNodeListener(NewNodeListener l) {
    newNodeListeners.add(l);
  }


  /**
   * METHOD TO DO
   *
   * @param l PARAMETER TO DO
   */
  public synchronized void removeNewNodeListener(NewNodeListener l) {
    newNodeListeners.remove(l);
  }


  //
  // Methods from NodePool
  //

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   * @throws NodePoolException EXCEPTION TO DO
   */
  public synchronized long newNode() throws NodePoolException {
    checkInitialized();
    try {
      long node = freeList.allocate();

      // Notify all the NewNodeListeners.
      try {
        Iterator<NewNodeListener> it = newNodeListeners.iterator();
        while (it.hasNext()) {
          it.next().newNode(node);
        }
      } catch (Exception ex) {
        throw new NodePoolException("Call to NewNodeListener failed.", ex);
      }

      return node;
    } catch (NullPointerException ex) {
      throw new NodePoolException("Node pool not open.");
    } catch (IOException ex) {
      throw new NodePoolException("Failed to allocate new node.", ex);
    } catch (IllegalStateException ex) {
      throw new NodePoolException("Node pool already initialized.", ex);
    }
  }


  /**
   * METHOD TO DO
   *
   * @param node PARAMETER TO DO
   * @throws NodePoolException EXCEPTION TO DO
   * @throws NoSuchNodeException EXCEPTION TO DO
   */
  public synchronized void releaseNode(
      long node
  ) throws NodePoolException, NoSuchNodeException {
    checkInitialized();
    try {
      if (node < MIN_NODE || node >= freeList.getNextItem()) {
        throw new NoSuchNodeException(node, "Invalid node ID: " + node);
      }

      freeList.free(node);
    } catch (NullPointerException ex) {
      throw new NodePoolException("Node pool not open.");
    } catch (IOException ex) {
      throw new NodePoolException("Failed to free node.", ex);
    } catch (IllegalStateException ex) {
      throw new NodePoolException("Node pool already initialized.", ex);
    }
  }


  public XANodePool newReadOnlyNodePool() {
    return new ReadOnlyNodePool();
  }


  public XANodePool newWritableNodePool() {
    return this;
  }


  /**
   * METHOD TO DO
   *
   * @throws NodePoolException EXCEPTION TO DO
   */
  public synchronized void close() throws NodePoolException {
    try {
      unmap();
    } finally {
      try {
        try {
          if (metarootFile != null) metarootFile.close();
        } finally {
          if (freeList != null) freeList.close();
        }
      } catch (IOException ex) {
        throw new NodePoolException("I/O error closing node pool.", ex);
      } finally {
        if (lockFile != null) {
          lockFile.release();
          lockFile = null;
        }
      }
    }
  }


  /**
   * METHOD TO DO
   *
   * @throws NodePoolException EXCEPTION TO DO
   */
  public synchronized void delete() throws NodePoolException {
    currentPhase = null;
    try {
      unmap();
    } finally {
      try {
        try {
          if (metarootFile != null) metarootFile.delete();
        } finally {
          if (freeList != null) freeList.delete();
        }
      } catch (IOException ex) {
        throw new NodePoolException("I/O error deleting node pool.", ex);
      } finally {
        metarootFile = null;
        freeList = null;
        if (lockFile != null) {
          lockFile.release();
          lockFile = null;
        }
      }
    }
  }


  protected void finalize() throws Throwable {
    // close the node pool if it has not already been closed explicitly.
    try {
      close();
    } catch (Throwable t) {
      logger.warn(
          "Exception in finalize while trying to close the node pool.", t
      );
      throw t;
    } finally {
      super.finalize();
    }
  }


  //
  // Methods from SimpleXAResource.
  //

  /**
   * Reinitializes the NodePool so that it is empty. This should be called after
   * constructing a NodePoolImpl when creating a new triple store.
   *
   * @param phaseNumber the initial phase number.
   * @throws IOException if an I/O error occurs.
   * @throws SimpleXAResourceException if it is inappropriate to call clear() at
   *      this time.
   */
  public synchronized void clear(
      int phaseNumber
  ) throws IOException, SimpleXAResourceException {
    if (currentPhase != null) {
      throw new IllegalStateException(
          "NodePool already has a current phase."
      );
    }

    openMetarootFile(true);

    try {
      synchronized (committedPhaseLock) {
        committedPhaseToken = freeList.new Phase(MIN_NODE).use();
      }
      this.phaseNumber = phaseNumber;
      phaseIndex = 1;
      freeList.clear();
    } catch (IllegalStateException ex) {
      throw new SimpleXAResourceException("Cannot initialize free list.", ex);
    }

    currentPhase = freeList.new Phase();
  }


  /**
   * Reinitializes the NodePool so that it is empty. This should be called
   * after constructing a NodePoolImpl when creating a new triple store.
   *
   * @throws IOException if an I/O error occurs.
   * @throws SimpleXAResourceException if it is inappropriate to call clear()
   * at this time.
   */
  public synchronized void clear(
  ) throws IOException, SimpleXAResourceException {
    if (currentPhase == null) {
      clear(0);
    }

    // TODO - should throw an exception if clear() is called after any other
    // operations are performed.  Calling clear() multiple times should be
    // permitted.
  }


  /**
   * METHOD TO DO
   *
   * @throws SimpleXAResourceException EXCEPTION TO DO
   */
  public synchronized void prepare() throws SimpleXAResourceException {
    checkInitialized();

    if (prepared) {
      // prepare already performed.
      throw new SimpleXAResourceException("prepare() called twice.");
    }

    try {
      // Perform a prepare.
      recordingPhaseToken = currentPhase.use();
      FreeList.Phase recordingPhase = currentPhase;
      currentPhase = freeList.new Phase();

      // Ensure that all data associated with the phase is on disk.
      freeList.force();

      // Write the metaroot.
      int newPhaseIndex = 1 - phaseIndex;
      int newPhaseNumber = phaseNumber + 1;

      Block block = metarootBlocks[newPhaseIndex];
      block.putInt(IDX_VALID, 0); // should already be invalid.
      block.putInt(IDX_PHASE_NUMBER, newPhaseNumber);
      logger.debug("Writing node pool metaroot for phase: " + newPhaseNumber);
      recordingPhase.writeToBlock(block, HEADER_SIZE);
      block.write();
      metarootFile.force();
      block.putInt(IDX_VALID, 1);
      block.write();
      metarootFile.force();

      phaseIndex = newPhaseIndex;
      phaseNumber = newPhaseNumber;
      prepared = true;
    } catch (IOException ex) {
      logger.error("I/O error while performing prepare.", ex);
      throw new SimpleXAResourceException(
          "I/O error while performing prepare.", ex
      );
    } finally {
      if (!prepared) {
        // Something went wrong!
        logger.error("Prepare failed.");
        if (recordingPhaseToken != null) {
          recordingPhaseToken.release();
          recordingPhaseToken = null;
        }
      }
    }
  }


  /**
   * METHOD TO DO
   *
   * @throws SimpleXAResourceException EXCEPTION TO DO
   */
  public synchronized void commit() throws SimpleXAResourceException {
    if (!prepared) {
      // commit without prepare.
      throw new SimpleXAResourceException(
          "commit() called without previous prepare()."
      );
    }

    // Perform a commit.
    try {
      // Invalidate the metaroot of the old phase.
      Block block = metarootBlocks[1 - phaseIndex];
      block.putInt(IDX_VALID, 0);
      block.write();
      metarootFile.force();

      // Release the token for the previously committed phase.
      synchronized (committedPhaseLock) {
        if (committedPhaseToken != null) {
          committedPhaseToken.release();
        }
        committedPhaseToken = recordingPhaseToken;
      }
      recordingPhaseToken = null;
    } catch (IOException ex) {
      logger.fatal("I/O error while performing commit.", ex);
      throw new SimpleXAResourceException(
          "I/O error while performing commit.", ex
      );
    } finally {
      prepared = false;
      if (recordingPhaseToken != null) {
        // Something went wrong!
        recordingPhaseToken.release();
        recordingPhaseToken = null;

        logger.error("Commit failed.  Calling close().");
        try {
          close();
        } catch (Throwable t) {
          logger.error("Exception on forced close()", t);
        }
      }
    }
  }


  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   */
  public synchronized int[] recover() throws SimpleXAResourceException {
    if (currentPhase != null) {
      return new int[0];
    }
    if (wrongFileVersion) {
      throw new SimpleXAResourceException("Wrong metaroot file version.");
    }

    try {
      openMetarootFile(false);
    } catch (IOException ex) {
      throw new SimpleXAResourceException("I/O error", ex);
    }

    // Count the number of valid phases.
    int phaseCount = 0;
    if (metarootBlocks[0].getInt(IDX_VALID) != 0) {
      ++phaseCount;
    }
    if (metarootBlocks[1].getInt(IDX_VALID) != 0) {
      ++phaseCount;
    }

    // Read the phase numbers.
    int[] phaseNumbers = new int[phaseCount];
    int index = 0;
    if (metarootBlocks[0].getInt(IDX_VALID) != 0) {
      phaseNumbers[index++] = metarootBlocks[0].getInt(IDX_PHASE_NUMBER);
    }
    if (metarootBlocks[1].getInt(IDX_VALID) != 0) {
      phaseNumbers[index++] = metarootBlocks[1].getInt(IDX_PHASE_NUMBER);
    }
    return phaseNumbers;
  }


  /**
   * @param phaseNumber PARAMETER TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   */
  public synchronized void selectPhase(
      int phaseNumber
  ) throws IOException, SimpleXAResourceException {
    if (currentPhase != null) {
      throw new SimpleXAResourceException(
          "selectPhase() called on initialized NodePoolImpl."
      );
    }
    if (metarootFile == null) {
      throw new SimpleXAResourceException(
          "Node pool metaroot file is not open."
      );
    }

    // Locate the metaroot corresponding to the given phase number.
    if (
        metarootBlocks[0].getInt(IDX_VALID) != 0 &&
        metarootBlocks[0].getInt(IDX_PHASE_NUMBER) == phaseNumber
    ) {
      phaseIndex = 0;
      // A new phase will be saved in the other metaroot.
    } else if (
        metarootBlocks[1].getInt(IDX_VALID) != 0 &&
        metarootBlocks[1].getInt(IDX_PHASE_NUMBER) == phaseNumber
    ) {
      phaseIndex = 1;
      // A new phase will be saved in the other metaroot.
    } else {
      throw new SimpleXAResourceException(
          "Invalid phase number: " + phaseNumber
      );
    }

    // Load a duplicate of the selected phase.  The duplicate will have a
    // phase number which is one higher than the original phase.
    try {
      synchronized (committedPhaseLock) {
        committedPhaseToken = freeList.new Phase(
            metarootBlocks[phaseIndex], HEADER_SIZE
        ).use();
      }
      this.phaseNumber = phaseNumber;
    } catch (IllegalStateException ex) {
      throw new SimpleXAResourceException(
          "Cannot construct initial phase for free list.", ex
      );
    }
    currentPhase = freeList.new Phase();

    // Invalidate the on-disk metaroot that the new phase will be saved to.
    Block block = metarootBlocks[1 - phaseIndex];
    block.putInt(IDX_VALID, 0);
    block.write();
    metarootFile.force();
  }


  /**
   * METHOD TO DO
   *
   * @throws SimpleXAResourceException EXCEPTION TO DO
   */
  public synchronized void rollback() throws SimpleXAResourceException {
    checkInitialized();
    boolean success = false;
    try {
      if (prepared) {
        // Restore phaseIndex and phaseNumber to their previous values.
        phaseIndex = 1 - phaseIndex;
        --phaseNumber;
        recordingPhaseToken = null;
        prepared = false;

        // Invalidate the metaroot of the other phase.
        Block block = metarootBlocks[1 - phaseIndex];
        block.putInt(IDX_VALID, 0);
        block.write();
        metarootFile.force();
      }
      success = true;
    } catch (IOException ex) {
      throw new SimpleXAResourceException(
          "I/O error while performing rollback (invalidating metaroot)", ex
      );
    } finally {
      try {
        currentPhase = freeList.new Phase(committedPhaseToken.getPhase());
      } catch (IOException ex) {
        String msg = "I/O error while performing rollback (new committed phase)";
        if (success) throw new SimpleXAResourceException(msg, ex); // new exception, need to re-throw.
        else logger.info(msg, ex); // already had a different exception, suppress this one and log it. 
      }
    }
  }


  /**
   * METHOD TO DO
   */
  public void release() {
    // NO-OP
  }


  /**
   * METHOD TO DO
   */
  public void refresh() {
    // NO-OP
  }


  /**
   * Gets the Valid attribute of the XANodePoolImpl object
   *
   * @param node PARAMETER TO DO
   * @return The Valid value
   */
  public boolean isValid(long node) {
    return freeList.isValid(node);
  }


  /**
   * METHOD TO DO
   */
  public synchronized void unmap() {
    if (committedPhaseToken != null) {
      recordingPhaseToken = null;
      prepared = false;

      try {
        freeList.new Phase(committedPhaseToken.getPhase());
      } catch (Throwable t) {
        logger.warn("Exception while rolling back in unmap()", t);
      }
      currentPhase = null;

      synchronized (committedPhaseLock) {
        committedPhaseToken.release();
        committedPhaseToken = null;
      }
    }

    if (metarootFile != null) {
      if (metarootBlocks[0] != null) metarootBlocks[0] = null;
      if (metarootBlocks[1] != null) metarootBlocks[1] = null;
      metarootFile.unmap();
    }

    if (freeList != null) freeList.unmap();
  }


  /** @see org.mulgara.store.xa.XANodePool#getNodeMapper() */
  public LongMapper getNodeMapper() throws Exception {
    return IntFile.newTempIntFile("n2n");
  }

  /**
   * METHOD TO DO
   *
   * @param clear PARAMETER TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   */
  private void openMetarootFile(boolean clear)
       throws IOException, SimpleXAResourceException {
    if (metarootFile == null) {
      metarootFile = AbstractBlockFile.openBlockFile(
          fileName + ".np", METAROOT_SIZE * Constants.SIZEOF_LONG,
          BlockFile.IOType.EXPLICIT
      );

      long nrBlocks = metarootFile.getNrBlocks();
      if (nrBlocks != NR_METAROOTS) {
        if (nrBlocks > 0) {
          logger.info(
              "Node pool metaroot file for triple store \"" + fileName +
              "\" has invalid number of blocks: " + nrBlocks
          );
          if (nrBlocks < NR_METAROOTS) {
            clear = true;
            metarootFile.clear();
          }
        } else {
          // Perform initialization on empty file.
          clear = true;
        }
        metarootFile.setNrBlocks(NR_METAROOTS);
      }

      metarootBlocks[0] = metarootFile.readBlock(0);
      metarootBlocks[1] = metarootFile.readBlock(1);
    }

    if (clear) {
      // Invalidate the metaroots on disk.
      metarootBlocks[0].putInt(IDX_MAGIC, FILE_MAGIC);
      metarootBlocks[0].putInt(IDX_VERSION, FILE_VERSION);
      metarootBlocks[0].putInt(IDX_VALID, 0);
      metarootBlocks[0].write();
      metarootBlocks[1].putInt(IDX_MAGIC, 0);
      metarootBlocks[1].putInt(IDX_VERSION, 0);
      metarootBlocks[1].putInt(IDX_VALID, 0);
      metarootBlocks[1].write();
      metarootFile.force();
    }
  }


  /**
   * METHOD TO DO
   */
  private void checkInitialized() {
    if (currentPhase == null) {
      throw new IllegalStateException(
          "No current phase.  NodePool has not been initialized or has been closed."
      );
    }
  }


  final class ReadOnlyNodePool implements XANodePool {

    private FreeList.Phase phase = null;

    private FreeList.Phase.Token token = null;


    /**
     * CONSTRUCTOR ReadOnlyNodePool TO DO
     */
    ReadOnlyNodePool() {
      synchronized (committedPhaseLock) {
        if (committedPhaseToken == null) {
          throw new IllegalStateException(
              "Cannot create read only view of uninitialized NodePool."
          );
        }
      }
    }


    public synchronized long getNrValidNodes() {
      return phase.getNrValidItems();
    }


    public long newNode() {
      throw new UnsupportedOperationException("Read-only node pool.");
    }


    public void releaseNode(long node) {
      throw new UnsupportedOperationException("Read-only node pool.");
    }


    public XANodePool newReadOnlyNodePool() {
      throw new UnsupportedOperationException();
    }


    public XANodePool newWritableNodePool() {
      throw new UnsupportedOperationException();
    }


    public void close() {
      throw new UnsupportedOperationException("Read-only node pool.");
    }


    public void delete() {
      throw new UnsupportedOperationException("Read-only node pool.");
    }


    public synchronized void release() {
      try {
        if (token != null) {
          token.release();
        }
      } finally {
        phase = null;
        token = null;
      }
    }


    public synchronized void refresh() {
      synchronized (committedPhaseLock) {
        FreeList.Phase committedPhase = committedPhaseToken.getPhase();
        if (phase != committedPhase) {
          if (token != null) {
            token.release();
          }
          phase = committedPhase;
          token = phase.use();
        }
      }
    }

    public void addNewNodeListener(NewNodeListener l) {
      throw new UnsupportedOperationException();
    }

    public void removeNewNodeListener(NewNodeListener l) {
      throw new UnsupportedOperationException();
    }

    public void prepare() { }
    public void commit() { }
    public void rollback() { }
    public void clear() { }
    public void clear(int phaseNumber) { }

    public int[] recover() {
      throw new UnsupportedOperationException("Attempting to recover ReadOnlyNodePool");
    }

    public void selectPhase(int phaseNumber) {
      throw new UnsupportedOperationException("Attempting to selectPhase of ReadOnlyNodePool");
    }

    public int getPhaseNumber() {
      return phaseNumber;
    }


    public LongMapper getNodeMapper() throws Exception {
      throw new UnsupportedOperationException("Attempting to map newly allocated nodes of ReadOnlyNodePool");
    }
  }


}

