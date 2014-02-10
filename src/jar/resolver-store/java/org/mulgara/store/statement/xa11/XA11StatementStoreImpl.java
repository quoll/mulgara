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
package org.mulgara.store.statement.xa11;

import java.io.*;
import java.nio.*;

// Java 2 standard packages
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.store.nodepool.*;
import org.mulgara.store.statement.*;
import org.mulgara.store.statement.xa.TripleAVLFile;
import org.mulgara.store.tuples.StoreTuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.store.xa.AbstractBlockFile;
import org.mulgara.store.xa.Block;
import org.mulgara.store.xa.BlockFile;
import org.mulgara.store.xa.LockFile;
import org.mulgara.store.xa.PersistableMetaRoot;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.store.xa.XAStatementStore;
import org.mulgara.store.xa.XAUtils;
import org.mulgara.util.Constants;

/**
 * An implementation of {@link StatementStore}.
 *
 * @created 2008-09-30
 * @author Paul Gearon
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy;2001-2004 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class XA11StatementStoreImpl implements XAStatementStore {

  /** Logger. */
  private final static Logger logger = Logger.getLogger(XA11StatementStoreImpl.class);

  /** The value of the invalid gNode */
  final static long NONE = NodePool.NONE;

  /** The subject/predicate/object index */
  final static int TI_3012 = 0;

  /** The predicate/object/subject index */
  final static int TI_3120 = 1;

  /** The object/subject/predicate index */
  final static int TI_3201 = 2;

  /** The number of indexes */
  final static int NR_INDEXES = 3;

  /** The ordering of indexes, as indexed by the TI_ values */
  private final static int[][] orders = {
      {3, 0, 1, 2},  // TI_3012
      {3, 1, 2, 0},  // TI_3120
      {3, 2, 0, 1}   // TI_3201
  };

  private final static int[] selectIndex = {
    /* 3XXX */ TI_3012,
    /* 3XX0 */ TI_3012,
    /* 3X1X */ TI_3120,
    /* 3X10 */ TI_3012,
    /* 32XX */ TI_3201,
    /* 32X0 */ TI_3201,
    /* 321X */ TI_3120,
    /* 3210 */ TI_3012
  };

  /** A number to identify the correct file type */
  private final static int FILE_MAGIC = 0xa5e7f21e;

  /** The version of file format */
  private final static int FILE_VERSION = 9;

  /** Index of the file magic number within each of the two on-disk metaroots. */
  private final static int IDX_MAGIC = 0;

  /** Index of the file version number within each of the two on-disk metaroots. */
  private final static int IDX_VERSION = 1;

  /** Index of the valid flag (in ints) within each of the two on-disk metaroots. */
  private final static int IDX_VALID = 2;

  /** The index of the phase number in the on-disk phase. */
  private final static int IDX_PHASE_NUMBER = 3;

  /** The size of the header of a metaroot in ints. */
  private final static int HEADER_SIZE_INTS = 4;

  /** The size of the header of a metaroot in longs. */
  private final static int HEADER_SIZE_LONGS = (HEADER_SIZE_INTS + 1) / 2;

  /** The size of a metaroot in longs. */
  private final static int METAROOT_SIZE = HEADER_SIZE_LONGS + Phase.RECORD_SIZE;

  /** The number of metaroots in the metaroot file. */
  private final static int NR_METAROOTS = 2;

  /** The mask for a bound Subject */
  private final static int MASK0 = 1;

  /** The mask for a bound Predicate */
  private final static int MASK1 = 2;

  /** The mask for a bound Object */
  private final static int MASK2 = 4;

  /** The mask for a bound Graph. This must always be set. */
  private final static int MASK3 = 8;

  /** The node number for the system graph. Globalized to <#>. */
  private long systemGraphNode = NONE;

  /** The node number for <rdf:type>. */
  private long rdfTypeNode = NONE;

  /** The node number for the graph class <mulgara:ModelType>. */
  private long graphTypeNode = NONE;

  /** The name of the triple store which forms the base name for the graph files. */
  private String fileName;

  /** The LockFile that protects the graph from being opened twice. */
  private LockFile lockFile;

  /** The BlockFile for the node pool metaroot file. */
  private BlockFile metarootFile = null;

  /** The metaroot blocks of the metaroot file. */
  private Block[] metarootBlocks = new Block[NR_METAROOTS];

  /** An error flag that is set during file initialization if the file version is incorrect */
  private boolean wrongFileVersion = false;

  /** The files containing indexed triples */
  private TripleAVLFile[] tripleAVLFiles = new TripleAVLFile[NR_INDEXES];

  /** The current read/write phase. Only the latest phase can write. */
  private Phase currentPhase = null;

  /**
   * Determines if modifications can be performed without creating a new
   * (in-memory) phase. If dirty is false and the current phase is in use (by
   * unclosed Tupleses) then a new phase must be created to protect the existing
   * Tupleses before any further modifications are made.
   */
  private boolean dirty = true;

  /**
   * The index of the phase in the metaroot. May be 0 or 1 as the commited phase swaps
   * between the two metaroots.
   */
  private int phaseIndex = 0;

  /** The number of the current phase */
  private int phaseNumber = 0;

  /** A reference token for keeping the commited phase available until we no longer need it */
  private Phase.Token committedPhaseToken = null;

  /** A synchronization object for locking access to the committed phase */
  private Object committedPhaseLock = new Object();

  /** A reference token for keeping the recording phase available until we no longer need it */
  private Phase.Token recordingPhaseToken = null;

  /**
   * This flag indicates that the current object has been fully written, and may be considered
   * as committed when the rest of the system is ready.
   */
  private boolean prepared = false;

  /** A set of objects to be informed when nodes are released. */
  private List<ReleaseNodeListener> releaseNodeListeners = new ArrayList<ReleaseNodeListener>();


  /**
   * Creates a statement store using a base filename.
   *
   * @param fileName The base filename to operate from.
   * @throws IOException The mass storage could not be accessed.
   */
  public XA11StatementStoreImpl(String fileName) throws IOException {
    this.fileName = fileName;

    lockFile = LockFile.createLockFile(fileName + ".g.lock");

    try {
      // Check that the metaroot file was created with a compatible version of the triplestore.
      RandomAccessFile metarootRAF = null;
      try {
        metarootRAF = new RandomAccessFile(fileName + ".g", "r");
        if (metarootRAF.length() >= 2 * Constants.SIZEOF_INT) {
          int fileMagic = metarootRAF.readInt();
          int fileVersion = metarootRAF.readInt();
          if (AbstractBlockFile.byteOrder != ByteOrder.BIG_ENDIAN) {
            fileMagic = XAUtils.bswap(fileMagic);
            fileVersion = XAUtils.bswap(fileVersion);
          }
          wrongFileVersion = fileMagic != FILE_MAGIC || fileVersion != FILE_VERSION;
        } else {
          wrongFileVersion = false;
        }
      } catch (FileNotFoundException ex) {
        wrongFileVersion = false;
      } finally {
        if (metarootRAF != null) metarootRAF.close();
      }

      for (int i = 0; i < NR_INDEXES; ++i) {
        String suffix = ".g_" + orders[i][0] + orders[i][1] + orders[i][2] + orders[i][3];
        tripleAVLFiles[i] = new TripleAVLFile(fileName + suffix, orders[i]);
      }
    } catch (IOException ex) {
      try {
        close();
      } catch (StatementStoreException ex2) {
        logger.info("Exception closing failed XA11StatementStoreImpl", ex2); 
      }
      throw ex;
    }
  }


  /**
   * Returns <code>true</code> if there are no triples in the graph
   * @return <code>true</code> if there are no triples in the graph
   */
  public synchronized boolean isEmpty() {
    checkInitialized();
    return currentPhase.isEmpty();
  }


  /**
   * Returns a count of the number of triples in the graph
   * @return a count of the number of triples in the graph
   */
  public synchronized long getNrTriples() {
    checkInitialized();
    return currentPhase.getNrTriples();
  }


  /**
   * Gets the PhaseNumber attribute of the XAGraphImpl object
   * @return The PhaseNumber value
   */
  public synchronized int getPhaseNumber() {
    checkInitialized();
    return phaseNumber;
  }


  /**
   * Adds a feature to the ReleaseNodeListener attribute of the XAGraphImpl object
   * @param l The feature to be added to the ReleaseNodeListener attribute
   */
  public synchronized void addReleaseNodeListener(ReleaseNodeListener l) {
    if (!releaseNodeListeners.contains(l)) releaseNodeListeners.add(l);
  }


  /**
   * Removes a release node listener.
   * @param l The listener to remove.
   */
  public synchronized void removeReleaseNodeListener(ReleaseNodeListener l) {
    releaseNodeListeners.remove(l);
  }


  /**
   * Adds a new triple to the graph if it doesn't already exist.
   * @param node0 the first element of the new triple
   * @param node1 the second element of the new triple
   * @param node2 the third element of the new triple
   * @param node3 the fourth element of the new triple
   * @throws StatementStoreException Due to structural or IO errors.
   */
  public synchronized void addTriple(long node0, long node1, long node2, long node3) throws StatementStoreException {
    checkInitialized();
    if (
        node0 < NodePool.MIN_NODE ||
        node1 < NodePool.MIN_NODE ||
        node2 < NodePool.MIN_NODE ||
        node3 < NodePool.MIN_NODE
    ) {
      throw new StatementStoreException(
          "Attempt to add a triple with node number out of range: " + node0 + " " + node1 + " " + node2 + " " + node3
      );
    }

    if (!dirty && currentPhase.isInUse()) {
      try {
        new Phase(true);
      } catch (IOException ex) {
        throw new StatementStoreException("I/O error", ex);
      }
    }

    currentPhase.addTriple(node0, node1, node2, node3);
  }


  /**
   * Removes all triples matching the given specification.
   * @param node0 the value for the first element of the triples
   * @param node1 the value for the second element of the triples
   * @param node2 the value for the third element of the triples
   * @param node3 the value for the fourth element of the triples
   * @throws StatementStoreException if something exceptional happens
   */
  public synchronized void removeTriples(long node0, long node1, long node2, long node3) throws StatementStoreException {
    checkInitialized();
    if (node0 != NONE && node1 != NONE && node2 != NONE && node3 != NONE) {
      if (!dirty && currentPhase.isInUse()) {
        try {
          new Phase(true);
        } catch (IOException ex) {
          throw new StatementStoreException("I/O error", ex);
        }
      }

      // Remove the triple.
      currentPhase.removeTriple(node0, node1, node2, node3);
    } else {
      // Find all the tuples matching the specification and remove them.
      StoreTuples tuples = currentPhase.findTuples(node0, node1, node2, node3);
      try {
        try {
          if (!tuples.isEmpty()) {
            // There is at least one triple to remove so protect the
            // Tuples as we make changes to the triplestore.
            try {
              new Phase(true);
            } catch (IOException ex) {
              throw new StatementStoreException("I/O error", ex);
            }

            long[] triple = new long[] { node0, node1, node2, node3 };
            int[] columnMap = tuples.getColumnOrder();
            int nrColumns = columnMap.length;
            tuples.beforeFirst();
            while (tuples.next()) {
              // Copy the row data over to the triple.
              for (int col = 0; col < nrColumns; ++col) {
                triple[columnMap[col]] = tuples.getColumnValue(col);
              }

              currentPhase.removeTriple(triple[0], triple[1], triple[2], triple[3]);
            }
          }
        } finally {
          tuples.close();
        }
      } catch (TuplesException ex) {
        throw new StatementStoreException("Exception while iterating over temporary Tuples.", ex);
      }
    }
  }


  /**
   * Finds triples matching the given specification.
   * @param node0 The 0 node of the triple to find.
   * @param node1 The 1 node of the triple to find.
   * @param node2 The 2 node of the triple to find.
   * @param node3 The 3 node of the triple to find.
   * @return A set of all the triples which match the search.
   * @throws StatementStoreException Due to a structural or IO error.
   */
  public synchronized StoreTuples findTuples(long node0, long node1, long node2, long node3) throws StatementStoreException {
    checkInitialized();
    dirty = false;
    return currentPhase.findTuples(node0, node1, node2, node3);
  }

  
  /**
   * Finds triples matching the given specification and index mask.
   * @param mask The mask of the index to use. This is only allowable for 3 variables
   *             and a given graph.
   * @param node0 The 0 node of the triple to find.
   * @param node1 The 1 node of the triple to find.
   * @param node2 The 2 node of the triple to find.
   * @param node3 The 3 node of the triple to find.
   * @return A set of all the triples which match the search.
   * @throws StatementStoreException Due to a structural or IO error.
   */
  public synchronized StoreTuples findTuples(
      int mask, long node0, long node1, long node2, long node3
  ) throws StatementStoreException {
    checkInitialized();
    dirty = false;
    if (!checkMask(mask, node0, node1, node2, node3)) throw new StatementStoreException("Bad explicit index selection for given node pattern.");
    return currentPhase.findTuples(mask, node0, node1, node2, node3);
  }


  /**
   * Tests a mask for consistency against the nodes it will be used to find.
   * @param mask The mask to test.
   * @param node0 The 0 node of the triple to find.
   * @param node1 The 1 node of the triple to find.
   * @param node2 The 2 node of the triple to find.
   * @param node3 The 3 node of the triple to find. Must not be NONE.
   * @return <code>true</code> if the mask is consistent with the given nodes.
   */
  private static boolean checkMask(int mask, long node0, long node1, long node2, long node3) {
    // The graph must be bound
    if (node3 != NONE) return false;
    if (node0 != NONE && 0 == (mask & MASK0)) return false;
    if (node1 != NONE && 0 == (mask & MASK1)) return false;
    if (node2 != NONE && 0 == (mask & MASK2)) return false;
    return true;
  }


  /**
   * Returns a StoreTuples which contains all triples in the store.  The
   * parameters provide a hint about how the StoreTuples will be used.  This
   * information is used to select the index from which the StoreTuples will be
   * obtained.
   * @param node0Bound specifies that node0 will be bound
   * @param node1Bound specifies that node1 will be bound
   * @param node2Bound specifies that node2 will be bound
   * @return the {@link StoreTuples}
   * @throws StatementStoreException if something exceptional happens
   */
  public synchronized StoreTuples findTuples(boolean node0Bound, boolean node1Bound, boolean node2Bound, boolean node3Bound) throws StatementStoreException {
    checkInitialized();
    dirty = false;
    return currentPhase.findTuples(node0Bound, node1Bound, node2Bound, node3Bound);
  }


  /**
   * Returns <code>true</code> if any triples match the given specification.
   * Allows wild cards StatementStore.NONE for any of the node numbers except node3.
   * @param node0 The 0 node of the triple to find.
   * @param node1 The 1 node of the triple to find.
   * @param node2 The 2 node of the triple to find.
   * @param node3 The 3 node of the triple to find.
   * @return <code>true</code> if any matching triples exist in the graph.
   * @throws StatementStoreException Due to a structural or IO error.
   */
  public synchronized boolean existsTriples(long node0, long node1, long node2, long node3) throws StatementStoreException {
    checkInitialized();
    return currentPhase.existsTriples(node0, node1, node2, node3);
  }


  public XAStatementStore newReadOnlyStatementStore() {
    return new ReadOnlyGraph();
  }


  public XAStatementStore newWritableStatementStore() {
    return this;
  }


  /**
   * Close all files, removing empty space from the ends as required.
   * @throws StatementStoreException if an error occurs while truncating,
   * flushing or closing one of the three files.
   */
  public synchronized void close() throws StatementStoreException {
    try {
      unmap();
    } finally {
      try {
        IOException savedEx = null;

        for (int i = 0; i < NR_INDEXES; ++i) {
          try {
            if (tripleAVLFiles[i] != null) tripleAVLFiles[i].close();
          } catch (IOException ex) {
            savedEx = ex;
          }
        }

        if (metarootFile != null) {
          try {
            metarootFile.close();
          } catch (IOException ex) {
            savedEx = ex;
          }
        }

        if (savedEx != null) throw new StatementStoreException("I/O error closing graph.", savedEx);
      } finally {
        if (lockFile != null) {
          lockFile.release();
          lockFile = null;
        }
      }
    }
  }


  /**
   * Close this graph, if it is currently open, and remove all files associated with it.
   * @throws StatementStoreException Due to an IO error.
   */
  public synchronized void delete() throws StatementStoreException {
    currentPhase = null;
    try {
      unmap();
    } finally {
      try {
        IOException savedEx = null;

        for (int i = 0; i < NR_INDEXES; ++i) {
          try {
            if (tripleAVLFiles[i] != null) tripleAVLFiles[i].delete();
          } catch (IOException ex) {
            savedEx = ex;
          }
        }

        if (metarootFile != null) {
          try {
            metarootFile.delete();
          } catch (IOException ex) {
            savedEx = ex;
          }
        }

        if (savedEx != null) throw new StatementStoreException("I/O error deleting graph.", savedEx);
      } finally {
        for (int i = 0; i < NR_INDEXES; ++i) tripleAVLFiles[i] = null;
        metarootFile = null;
        if (lockFile != null) {
          lockFile.release();
          lockFile = null;
        }
      }
    }
  }


  /**
   * Try to safely close the store if this was not done explicitly.
   */
  protected void finalize() throws Throwable {
    try {
      close();
    } catch (Throwable t) {
      logger.warn("Exception in finalize while trying to close the statement store.", t);
    } finally {
      super.finalize();
    }
  }


  /**
   * A manually tracked reference to this object was released. Does nothing.
   */
  public void release() {
    if (logger.isDebugEnabled()) logger.debug("Release " + this.getClass() + ":" + System.identityHashCode(this));
  }


  /**
   * This in called in response to the resource being manually refreshed.
   * This implementation does nothing here.
   */
  public void refresh() {
    if (logger.isDebugEnabled()) {
      logger.debug("Refresh " + this.getClass() + ":" + System.identityHashCode(this));
    }
  }


  //
  // Methods from SimpleXAResource.
  //

  /**
   * Clears this store to a fresh state.
   * @param phaseNumber The phase number to set to.
   * @throws IOException Error with file access
   * @throws SimpleXAResourceException Error with the data structures.
   */
  public synchronized void clear(int phaseNumber) throws IOException, SimpleXAResourceException {
    if (logger.isDebugEnabled()) {
      logger.debug("Clear(" + phaseNumber + ") " + this.getClass() + ":" + System.identityHashCode(this));
    }
    if (currentPhase != null) throw new IllegalStateException("Graph already has a current phase.");

    openMetarootFile(true);

    synchronized (committedPhaseLock) {
      committedPhaseToken = new Phase(true).use();
    }
    this.phaseNumber = phaseNumber;
    phaseIndex = 1;
    for (int i = 0; i < NR_INDEXES; ++i) tripleAVLFiles[i].clear();

    new Phase(true);
  }


  /**
   * Clear the state of the database.
   * @throws IOException Filesystem error
   * @throws SimpleXAResourceException Error in the data structures.
   */
  public synchronized void clear() throws IOException, SimpleXAResourceException {
    if (logger.isDebugEnabled()) logger.debug("Clear " + this.getClass() + ":" + System.identityHashCode(this));
    if (currentPhase == null) clear(0);

    // could throw an exception if clear() is called after any other
    // operations are performed.  Calling clear() multiple times should be
    // permitted.
  }


  /**
   * Perform all the operations for a commit and return when all the data structures are in place.
   * @throws SimpleXAResourceException Due to a bad transaction state, or an IO error while preparing.
   */
  public synchronized void prepare() throws SimpleXAResourceException {
    if (logger.isDebugEnabled()) logger.debug("Prepare " + this.getClass() + ":" + System.identityHashCode(this));
    checkInitialized();

    // check that prepare() was not caleld twice
    if (prepared) throw new SimpleXAResourceException("prepare() called twice.");

    Phase newCurrent = null;
    try {
      // Perform a prepare.
      recordingPhaseToken = currentPhase.use();
      Phase recordingPhase = currentPhase;
      // new Phase() has a side effect of setting the current phase, but we'll keep a local copy anyway
      newCurrent = new Phase(false);
      // could not set up the committed graphs yet, so send them in after the fact
      newCurrent.graphNodes = new LinkedHashSet<Long>(recordingPhase.graphNodes);
      if (logger.isDebugEnabled()) {
        logger.debug("Set phase graph nodes from recording phase in prepare(): " + newCurrent.graphNodes);
      }

      // Ensure that all data associated with the phase is on disk.
      for (int i = 0; i < NR_INDEXES; ++i) tripleAVLFiles[i].force();

      // Write the metaroot.
      int newPhaseIndex = 1 - phaseIndex;
      int newPhaseNumber = phaseNumber + 1;

      Block block = metarootBlocks[newPhaseIndex];
      block.putInt(IDX_VALID, 0); // should already be invalid.
      block.putInt(IDX_PHASE_NUMBER, newPhaseNumber);
      logger.debug("Writing graph metaroot for phase: " + newPhaseNumber);
      recordingPhase.writeToBlock(block, HEADER_SIZE_LONGS);
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
      throw new SimpleXAResourceException("I/O error while performing prepare.", ex);
    } finally {
      if (!prepared) {
        // Something went wrong. An exception is on its way out
        logger.error("Prepare failed.");
        if (recordingPhaseToken != null) {
          recordingPhaseToken.release();
          recordingPhaseToken = null;
        }
        try {
          if (newCurrent != null) newCurrent.graphNodes = newCurrent.scanForGraphs();
        } catch (Exception e) {
          logger.error("Error reading graphs while handling exception from phase.prepare", e);
        }
      }
    }
  }


  /**
   * Update the metadata to point to the prepared data structures.
   * @throws SimpleXAResourceException Due to a bad transaction state, or an IO error.
   */
  public synchronized void commit() throws SimpleXAResourceException {
    if (logger.isDebugEnabled()) logger.debug("Commit " + this.getClass() + ":" + System.identityHashCode(this));

    // check that prepare has been called
    if (!prepared) throw new SimpleXAResourceException("commit() called without previous prepare().");

    // Perform a commit.
    try {
      // Invalidate the metaroot of the old phase.
      Block block = metarootBlocks[1 - phaseIndex];
      block.putInt(IDX_VALID, 0);
      block.write();
      metarootFile.force();

      // Release the token for the previously committed phase.
      synchronized (committedPhaseLock) {
        if (committedPhaseToken != null) committedPhaseToken.release();
        committedPhaseToken = recordingPhaseToken;
      }
      recordingPhaseToken = null;
    } catch (IOException ex) {
      logger.fatal("I/O error while performing commit.", ex);
      throw new SimpleXAResourceException("I/O error while performing commit.", ex);
    } finally {
      prepared = false;
      if (recordingPhaseToken != null) {
        // Something went wrong! An exception is on its way out
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
   * Read the state from the metaroot file and use it to set up this object
   * @return An array of 0, 1, or 2 valid phases that can be selected as the last committed phase.
   * @throws SimpleXAResourceException Due to an IO error, or a data error in the metaroot file.
   */
  public synchronized int[] recover() throws SimpleXAResourceException {
    if (logger.isDebugEnabled()) logger.debug("Recover " + this.getClass() + ":" + System.identityHashCode(this));
    if (currentPhase != null) return new int[0];
    if (wrongFileVersion) throw new SimpleXAResourceException("Wrong metaroot file version.");

    try {
      openMetarootFile(false);
    } catch (IOException ex) {
      throw new SimpleXAResourceException("I/O error", ex);
    }

    // Count the number of valid phases.
    int phaseCount = 0;
    if (metarootBlocks[0].getInt(IDX_VALID) != 0) ++phaseCount;
    if (metarootBlocks[1].getInt(IDX_VALID) != 0) ++phaseCount;

    // Read the phase numbers.
    int[] phaseNumbers = new int[phaseCount];
    int index = 0;
    if (metarootBlocks[0].getInt(IDX_VALID) != 0) phaseNumbers[index++] = metarootBlocks[0].getInt(IDX_PHASE_NUMBER);
    if (metarootBlocks[1].getInt(IDX_VALID) != 0) phaseNumbers[index++] = metarootBlocks[1].getInt(IDX_PHASE_NUMBER);
    return phaseNumbers;
  }


  /**
   * Choose a phase from the metaroot file to use
   * @param phaseNumber The number of the phase to select. This must be one of the valid
   *        phases present in the metaroot file.
   * @throws IOException Due to an error on the filesystem
   * @throws SimpleXAResourceException If the file structures are incorrect.
   */
  public synchronized void selectPhase(int phaseNumber) throws IOException, SimpleXAResourceException {
    if (logger.isDebugEnabled()) {
      logger.debug("SelectPhase(" + phaseNumber + ") " + this.getClass() + ":" + System.identityHashCode(this));
    }
    if (currentPhase != null) throw new SimpleXAResourceException("selectPhase() called on initialized Graph.");
    if (metarootFile == null) throw new SimpleXAResourceException("Graph metaroot file is not open.");

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
      throw new SimpleXAResourceException("Invalid phase number: " + phaseNumber);
    }

    // Load a duplicate of the selected phase.  The duplicate will have a
    // phase number which is one higher than the original phase.
    try {
      synchronized (committedPhaseLock) {
        committedPhaseToken = new Phase(metarootBlocks[phaseIndex], HEADER_SIZE_LONGS).use();
      }
      this.phaseNumber = phaseNumber;
    } catch (IllegalStateException ex) {
      throw new SimpleXAResourceException("Cannot construct initial phase.", ex);
    }
    new Phase(true);

    // Invalidate the on-disk metaroot that the new phase will be saved to.
    Block block = metarootBlocks[1 - phaseIndex];
    block.putInt(IDX_VALID, 0);
    block.write();
    metarootFile.force();
  }


  /**
   * Return to the data structure state from the beginning of the transaction.
   * @throws SimpleXAResourceException Due to an IO error.
   */
  public synchronized void rollback() throws SimpleXAResourceException {
    if (logger.isDebugEnabled()) logger.debug("Rollback " + this.getClass() + ":" + System.identityHashCode(this));
    checkInitialized();
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
    } catch (IOException ex) {
      throw new SimpleXAResourceException("I/O error while performing rollback (invalidating metaroot)", ex);
    } finally {
      try {
        new Phase(committedPhaseToken.getPhase());
      } catch (IOException ex) {
        throw new SimpleXAResourceException("I/O error while performing rollback (new committed phase)", ex);
      }
    }
  }


  /**
   * Get a string representation of the current state of the graph.
   * @return A string representing the current state
   */
  public synchronized String toString() {
    if (currentPhase == null) return "Uninitialized Graph.";
    return currentPhase.toString();
  }


  /**
   * Attempt to cleanly close all mapped files.
   */
  public synchronized void unmap() {
    if (committedPhaseToken != null) {
      recordingPhaseToken = null;
      prepared = false;

      try {
        new Phase(committedPhaseToken.getPhase());
      } catch (Throwable t) {
        logger.warn("Exception while rolling back in unmap()", t);
      }
      currentPhase = null;

      synchronized (committedPhaseLock) {
        committedPhaseToken.release();
        committedPhaseToken = null;
      }
    }

    if (tripleAVLFiles != null) {
      for (int i = 0; i < NR_INDEXES; ++i) {
        if (tripleAVLFiles[i] != null) tripleAVLFiles[i].unmap();
      }
    }

    if (metarootFile != null) {
      if (metarootBlocks[0] != null) metarootBlocks[0] = null;
      if (metarootBlocks[1] != null) metarootBlocks[1] = null;
      metarootFile.unmap();
    }
  }


  /**
   * Check that the data structures are valid
   * @return The number of triples in the database
   */
  synchronized long checkIntegrity() {
    checkInitialized();
    return currentPhase.checkIntegrity();
  }


  /**
   * Open the metaroot file and read in the contents
   * @param clear If <code>true</code> then the file will be reset to empty.
   * @throws IOException Due to a filesystem error.
   * @throws SimpleXAResourceException If the data structures are inconsistent.
   */
  private void openMetarootFile(boolean clear) throws IOException, SimpleXAResourceException {
    if (metarootFile == null) {
      metarootFile = AbstractBlockFile.openBlockFile(fileName + ".g", METAROOT_SIZE * Constants.SIZEOF_LONG, BlockFile.IOType.EXPLICIT);

      long nrBlocks = metarootFile.getNrBlocks();
      if (nrBlocks != NR_METAROOTS) {
        if (nrBlocks > 0) {
          logger.info("Graph metaroot file for triple store \"" + fileName + "\" has invalid number of blocks: " + nrBlocks);
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
   * Tests that the current object has been initialized.
   * @throws IllegalStateException Throws this unchecked exception if the object is not initialized.
   */
  private void checkInitialized() {
    if (currentPhase == null) throw new IllegalStateException("No current phase.  Graph has not been initialized or has been closed.");
  }


  final class ReadOnlyGraph implements XAStatementStore {

    private Phase phase = null;

    private Phase.Token token = null;


    /**
     * Create a read-only graph attached to the current outer database
     */
    ReadOnlyGraph() {
      synchronized (committedPhaseLock) {
        if (committedPhaseToken == null) {
          throw new IllegalStateException("Cannot create read only view of uninitialized Graph.");
        }
      }
    }


    public synchronized boolean isEmpty() {
      return phase.isEmpty();
    }


    /**
     * Returns a count of the number of triples in the graph
     * @return a count of the number of triples in the graph
     */
    public synchronized long getNrTriples() {
      return phase.getNrTriples();
    }


    /**
     * Adds a triple to the graph.
     * @param node0 The 0 node of the triple.
     * @param node1 The 1 node of the triple.
     * @param node2 The 2 node of the triple.
     * @param node3 The 3 node of the triple.
     */
    public void addTriple(long node0, long node1, long node2, long node3) throws StatementStoreException {
      throw new UnsupportedOperationException("Trying to modify a read-only graph.");
    }


    /**
     * Removes all triples matching the given specification.
     * @param node0 the value for the first element of the triples
     * @param node1 the value for the second element of the triples
     * @param node2 the value for the third element of the triples
     * @param node3 the value for the fourth element of the triples
     */
    public void removeTriples(long node0, long node1, long node2, long node3) throws StatementStoreException {
      throw new UnsupportedOperationException("Trying to modify a read-only graph.");
    }


    /**
     * Finds triples matching the given specification.
     * @param node0 The 0 node of the triple to find.
     * @param node1 The 1 node of the triple to find.
     * @param node2 The 2 node of the triple to find.
     * @param node3 The 3 node of the triple to find.
     * @return A StoreTuples which contains the triples which match the search.
     */
    public synchronized StoreTuples findTuples(long node0, long node1, long node2, long node3) throws StatementStoreException {
      return phase.findTuples(node0, node1, node2, node3);
    }

    /**
     * Finds triples matching the given specification.
     * @param mask The mask of the index to use. This is only allowable for 3 variables
     *             and a given graph.
     * @param node0 The 0 node of the triple to find.
     * @param node1 The 1 node of the triple to find.
     * @param node2 The 2 node of the triple to find.
     * @param node3 The 3 node of the triple to find.
     * @return A StoreTuples which contains the triples which match the search.
     * @throws StatementStoreException A structural or IO error
     */
    public synchronized StoreTuples findTuples(int mask, long node0, long node1, long node2, long node3) throws StatementStoreException {
      if (!checkMask(mask, node0, node1, node2, node3)) throw new StatementStoreException("Bad explicit index selection for given node pattern.");
      return phase.findTuples(mask, node0, node1, node2, node3);
    }


    /**
     * Returns a StoreTuples which contains all triples in the store.  The
     * parameters provide a hint about how the StoreTuples will be used.  This
     * information is used to select the index from which the StoreTuples will
     * be obtained.
     * @param node0Bound specifies that node0 will be bound
     * @param node1Bound specifies that node1 will be bound
     * @param node2Bound specifies that node2 will be bound
     * @return the {@link StoreTuples}
     * @throws StatementStoreException if something exceptional happens
     */
    public synchronized StoreTuples findTuples(boolean node0Bound, boolean node1Bound, boolean node2Bound, boolean node3Bound) throws StatementStoreException {
      return phase.findTuples(node0Bound, node1Bound, node2Bound, node3Bound);
    }


    public synchronized boolean existsTriples(long node0, long node1, long node2, long node3) throws StatementStoreException {
      return phase.existsTriples(node0, node1, node2, node3);
    }


    public XAStatementStore newReadOnlyStatementStore() {
      throw new UnsupportedOperationException();
    }


    public XAStatementStore newWritableStatementStore() {
      throw new UnsupportedOperationException();
    }


    public void close() {
      throw new UnsupportedOperationException("Trying to close a read-only graph.");
    }


    public void delete() {
      throw new UnsupportedOperationException("Trying to delete a read-only graph.");
    }


    /**
     * Release the phase.
     */
    public synchronized void release() {
      if (logger.isDebugEnabled()) logger.debug("Releasing " + this.getClass() + ":" + System.identityHashCode(this));
      try {
        if (token != null) token.release();
      } finally {
        phase = null;
        token = null;
      }
    }


    public synchronized void refresh() {
      if (logger.isDebugEnabled()) logger.debug("Refreshing " + this.getClass() + ":" + System.identityHashCode(this));

      synchronized (committedPhaseLock) {
        Phase committedPhase = committedPhaseToken.getPhase();
        if (phase != committedPhase) {
          if (token != null) token.release();
          phase = committedPhase;
          token = phase.use();
        }
      }
    }

    public void addReleaseNodeListener(ReleaseNodeListener l) {
      throw new UnsupportedOperationException();
    }

    public void removeReleaseNodeListener(ReleaseNodeListener l) {
      throw new UnsupportedOperationException();
    }

    public void prepare() {
      if (logger.isDebugEnabled()) logger.debug("Preparing " + this.getClass() + ":" + System.identityHashCode(this));
    }

    public void commit() {
      if (logger.isDebugEnabled()) logger.debug("Commit " + this.getClass() + ":" + System.identityHashCode(this));
    }

    public void rollback() {
      if (logger.isDebugEnabled()) logger.debug("Rollback " + this.getClass() + ":" + System.identityHashCode(this));
    }

    public void clear() {
      if (logger.isDebugEnabled()) logger.debug("Clearing " + this.getClass() + ":" + System.identityHashCode(this));
    }

    public void clear(int phaseNumber) {
      if (logger.isDebugEnabled()) logger.debug("Clearing (" + phaseNumber + ") " + this.getClass() + ":" + System.identityHashCode(this));
    }

    public int[] recover() {
      if (logger.isDebugEnabled()) logger.debug("Recovering " + this.getClass() + ":" + System.identityHashCode(this));
      throw new UnsupportedOperationException("Attempting to recover ReadOnlyGraph");
    }

    public void selectPhase(int phaseNumber) {
      if (logger.isDebugEnabled()) logger.debug("Selecting Phase " + this.getClass() + ":" + System.identityHashCode(this));
      throw new UnsupportedOperationException("Attempting to selectPhase of ReadOnlyGraph");
    }

    public int getPhaseNumber() {
      return phaseNumber;
    }


    /**
     * Not used on a read-only graph
     */
    public void initializeSystemNodes(long systemGraphNode, long rdfTypeNode, long systemGraphTypeNode) {
      // do nothing
    }
  }


  /**
   * This class represents the state of the the database at a particular time. Only the most
   * recent phase can be written to.
   */
  final class Phase implements PersistableMetaRoot {

    /** The size of the data this object stores in the metaroot */
    final static int RECORD_SIZE = TripleAVLFile.Phase.RECORD_SIZE * NR_INDEXES;

    /** Maintaines parallel structural phases between all of the parallel tree data structures */
    private TripleAVLFile.Phase[] tripleAVLFilePhases = new TripleAVLFile.Phase[NR_INDEXES];

    /** The list of graphs valid in this phase. */
    private LinkedHashSet<Long> graphNodes = null;


    /**
     * Creates a new phase based on the current state of the database.
     * This sets the latest phase on the outer statement store.
     * @param initializeGraphs scan for graphs to initialize the graphs list.
     * @throws IOException Error on the filesystem.
     */
    Phase(boolean initializeGraphs) throws IOException {
      if (logger.isDebugEnabled()) logger.debug("Phase(boolean), initializeGraphs = " + initializeGraphs);

      for (int i = 0; i < NR_INDEXES; ++i) tripleAVLFilePhases[i] = tripleAVLFiles[i].new Phase();
      currentPhase = this;
      dirty = true;
      if (initializeGraphs) {
        try {
          graphNodes = scanForGraphs();
        } catch (StatementStoreException e) {
          throw new IOException("Unable to get metadata for phase: " + e.getMessage());
        }
      }
    }


    /**
     * A copy constructor for duplicating a phase structure. This sets the latest phase
     * on the outer statement store.
     * @throws IOException Error on the filesystem.
     */
    Phase(Phase p) throws IOException {
      assert p != null;

      for (int i = 0; i < NR_INDEXES; ++i) tripleAVLFilePhases[i] = tripleAVLFiles[i].new Phase(p.tripleAVLFilePhases[i]);
      currentPhase = this;
      dirty = true;
      graphNodes = new LinkedHashSet<Long>(p.graphNodes);
      if (logger.isDebugEnabled()) {
        logger.debug("Initializing graph nodes from previous phase in constructor: " + graphNodes);
      }
    }


    /**
     * Create a phase based on information found in a buffer that came from a metaroot
     * @param b The buffer containing the phase information.
     * @param offset The start of the phase information in the buffer
     * @throws IOException A filesystem error occurred while accessing the buffer.
     */
    Phase(Block b, int offset) throws IOException {
      for (int i = 0; i < NR_INDEXES; ++i) {
        tripleAVLFilePhases[i] = tripleAVLFiles[i].new Phase(b, offset);
        offset += TripleAVLFile.Phase.RECORD_SIZE;
      }
      currentPhase = this;
      dirty = false;
      try {
        graphNodes = scanForGraphs();
      } catch (StatementStoreException sse) {
        throw new IOException("Error accessing graph data during initialization");
      }
    }


    /**
     * Writes this PersistableMetaRoot to the specified Block. The ints are
     * written at the specified offset.
     * @param b The metaroot Block to write this object to.
     * @param offset The start within the buffer of where the phase information should be written to.
     */
    public void writeToBlock(Block b, int offset) {
      for (int i = 0; i < NR_INDEXES; ++i) {
        tripleAVLFilePhases[i].writeToBlock(b, offset);
        offset += TripleAVLFile.Phase.RECORD_SIZE;
      }
    }


    /**
     * Create a string representation of the current phase.
     * @return A string representing this phase
     */
    public String toString() {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < NR_INDEXES; ++i) {
        StoreTuples ts = tripleAVLFilePhases[i].allTuples();
        try {
          sb.append(ts).append('\n');
        } finally {
          try {
            ts.close();
          } catch (TuplesException ex) {
            logger.warn("TuplesException while closing Tuples", ex);
            return ex.toString();
          }
        }
      }
      return sb.toString();
    }


    /**
     * Tests if any part of this phase has a reference being kept to it
     * @return <code>true</code> if any part of this phase is being used.
     */
    boolean isInUse() {
      for (int i = 0; i < NR_INDEXES; ++i) {
        if (tripleAVLFilePhases[i].isInUse()) return true;
      }
      return false;
    }


    /**
     * Tests if the phase contains any triples
     * @return <code>true</code> if the phase contains 1 or more triples
     */
    boolean isEmpty() {
      return tripleAVLFilePhases[TI_3012].isEmpty();
    }


    /**
     * Gets the number of triples in the phase
     * @return The number of triples in this phase
     */
    long getNrTriples() {
      return tripleAVLFilePhases[TI_3012].getNrTriples();
    }


    /**
     * Adds a new triple to the graph if it doesn't already exist.
     * @param node0 the first element of the new triple
     * @param node1 the second element of the new triple
     * @param node2 the third element of the new triple
     * @param node3 the fourth element of the new triple
     * @throws StatementStoreException An IO or data structure error
     */
    void addTriple(long node0, long node1, long node2, long node3) throws StatementStoreException {
      assert node0 >= NodePool.MIN_NODE;
      assert node1 >= NodePool.MIN_NODE;
      assert node2 >= NodePool.MIN_NODE;
      assert node3 >= NodePool.MIN_NODE;

      //if (
      //  DEBUG && nodePool != null &&
      //  !nodePool.isValid(node0) && !nodePool.isValid(node1) &&
      //  !nodePool.isValid(node2) && !nodePool.isValid(node3)
      //) throw new AssertionError(
      //  "Attempt to add a triple with an invalid node"
      //);

      long[] triple = new long[]{node0, node1, node2, node3};

      for (int i = 0; i < NR_INDEXES; ++i) tripleAVLFilePhases[i].asyncAddTriple(triple);

      if (node1 == rdfTypeNode && node2 == graphTypeNode && node3 == systemGraphNode) {
        if (logger.isDebugEnabled()) logger.debug("Adding new graph node: " + node0);
        graphNodes.add(node0);
      }
    }


    /**
     * Removes the specified triple.
     * @param node0 the value for the first element of the triple
     * @param node1 the value for the second element of the triple
     * @param node2 the value for the third element of the triple
     * @param node3 the value for the fourth element of the triple
     * @throws StatementStoreException An IO or structural error
     */
    void removeTriple(long node0, long node1, long node2, long node3) throws StatementStoreException {
      if (
          node0 < NodePool.MIN_NODE ||
          node1 < NodePool.MIN_NODE ||
          node2 < NodePool.MIN_NODE ||
          node3 < NodePool.MIN_NODE
      ) {
        throw new StatementStoreException("Attempt to remove a triple with node number out of range: " + node0 + " " + node1 + " " + node2 + " " + node3);
      }

      try {
        for (int i = 0; i < NR_INDEXES; ++i) tripleAVLFilePhases[i].removeTriple(node0, node1, node2, node3);
        // removeTriple listeners can be informed here
      } catch (IOException e) {
        throw new StatementStoreException("I/O error", e);
      }
      if (node1 == rdfTypeNode && node2 == graphTypeNode && node3 == systemGraphNode) {
        if (logger.isDebugEnabled()) logger.debug("Removing graph node: " + node0);
        graphNodes.remove(node0);
      }
    }


    /**
     * Finds triples matching the given specification.
     * @param variableMask the mask used to indicate the desired index.
     * @param node0 The 0 node of the triple to find.
     * @param node1 The 1 node of the triple to find.
     * @param node2 The 2 node of the triple to find.
     * @param node3 The 3 node of the triple to find.
     * @return A StoreTuples containing all the triples which match the search.
     * @throws StatementStoreException An IO or structural error
     */
    StoreTuples findTuples(int variableMask, long node0, long node1, long node2, long node3) throws StatementStoreException {
      if (
          node0 < NodePool.NONE ||
          node1 < NodePool.NONE ||
          node2 < NodePool.NONE ||
          node3 < NodePool.NONE
      ) {
        // There is at least one query node.  Return an empty StoreTuples.
        return TuplesOperations.empty();
      }

      if (0 == (variableMask & MASK3)) throw new StatementStoreException("This version of find is for re-ordering graphs, based on a given mask.");
      try {
        switch (variableMask) {
          case MASK3:
            return tripleAVLFilePhases[TI_3012].findTuples(node3);
          case MASK0 | MASK3:
            return tripleAVLFilePhases[TI_3012].findTuples(node3);
          case MASK1 | MASK3:
            return tripleAVLFilePhases[TI_3120].findTuples(node3);
          case MASK0 | MASK1 | MASK3:
            return tripleAVLFilePhases[TI_3012].findTuples(node3);
          case MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3201].findTuples(node3);
          case MASK0 | MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3201].findTuples(node3);
          case MASK1 | MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3120].findTuples(node3);
          case MASK0 | MASK1 | MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3012].findTuples(node3);
          default:
            throw new AssertionError();
        }
      } catch (IOException ex) {
        throw new StatementStoreException("I/O error", ex);
      }
    }


    /**
     * Finds triples matching the given specification.
     * @param node0 The 0 node of the triple to find.
     * @param node1 The 1 node of the triple to find.
     * @param node2 The 2 node of the triple to find.
     * @param node3 The 3 node of the triple to find.
     * @return A StoreTuples containing all the triples which match the search.
     * @throws StatementStoreException An IO or structural error
     */
    StoreTuples findTuples(long node0, long node1, long node2, long node3) throws StatementStoreException {
      if (
          node0 < NodePool.NONE ||
          node1 < NodePool.NONE ||
          node2 < NodePool.NONE ||
          node3 < NodePool.NONE
      ) {
        // There is at least one query node.  Return an empty StoreTuples.
        return TuplesOperations.empty();
      }

      int variableMask =
        (node0 != NONE ? MASK0 : 0) |
        (node1 != NONE ? MASK1 : 0) |
        (node2 != NONE ? MASK2 : 0) |
        (node3 != NONE ? MASK3 : 0);

      if (node3 == NONE && variableMask != 0) {
        return joinGraphedTuples(variableMask, node0, node1, node2);
      }


      try {
        switch (variableMask) {
          case 0:
            return tripleAVLFilePhases[TI_3012].allTuples();
          case MASK3:
            return tripleAVLFilePhases[TI_3012].findTuples(node3);
          case MASK0 | MASK3:
            return tripleAVLFilePhases[TI_3012].findTuples(node3, node0);
          case MASK1 | MASK3:
            return tripleAVLFilePhases[TI_3120].findTuples(node3, node1);
          case MASK0 | MASK1 | MASK3:
            return tripleAVLFilePhases[TI_3012].findTuples(node3, node0, node1);
          case MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3201].findTuples(node3, node2);
          case MASK0 | MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3201].findTuples(node3, node2, node0);
          case MASK1 | MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3120].findTuples(node3, node1, node2);
          case MASK0 | MASK1 | MASK2 | MASK3:
            if (tripleAVLFilePhases[TI_3012].existsTriple(node3, node0, node1, node2)) {
              return TuplesOperations.unconstrained();
            }
            return TuplesOperations.empty();
          default:
            throw new AssertionError("Search structure incorrectly calculated");
        }
      } catch (IOException ex) {
        throw new StatementStoreException("I/O error", ex);
      }
    }


    StoreTuples findTuples(boolean node0Bound, boolean node1Bound, boolean node2Bound, boolean node3Bound) throws StatementStoreException {
      // The variable mask does not need MASK3, as this has been taken into account in selectIndex[]
      int variableMask =
          (node0Bound ? MASK0 : 0) |
          (node1Bound ? MASK1 : 0) |
          (node2Bound ? MASK2 : 0);
      if (variableMask == 0 || node3Bound) {
        return tripleAVLFilePhases[selectIndex[variableMask]].allTuples();
      } else {
        return joinGraphedTuples(variableMask);
      }
    }


    /**
     * Iterates over all graphs, finding requested tuples, and joining all the results together into a single tuples.
     * @param variableMask Pre-calculated from the bound node parameters.
     * @param node0 The bound value for node0, or &lt; 0 if not bound.
     * @param node1 The bound value for node1, or &lt; 0 if not bound.
     * @param node2 The bound value for node2, or &lt; 0 if not bound.
     * @return A StoreTuples with all the intermediate tuples appended.
     * @throws StatementStoreException On an error accessing the store.
     */
    StoreTuples joinGraphedTuples(int variableMask, long node0, long node1, long node2) throws StatementStoreException {
      try {
        assert (variableMask & MASK3) == 0 : "Must not be asking to join on multiple graphs unless graph is variable.";

        // get the graphNodes if not already configured
        if (graphNodes.isEmpty()) throw new IllegalStateException("Unable to query for variable graphs until graphs are initialized");

        if (variableMask == (MASK0 | MASK1 | MASK2)) {
          LiteralGraphTuples result = new LiteralGraphTuples(false);
          for (long graphNode: graphNodes) {
            if (tripleAVLFilePhases[TI_3012].existsTriple(graphNode, node0, node1, node2)) {
              result.appendTuple(new long[] { graphNode });
            }
          }
          return result;
        }

        ArrayList<StoreTuples> graphedTuples = new ArrayList<StoreTuples>();
        for (long graphNode: graphNodes) {
          StoreTuples partialResult = null;
          switch (variableMask) {
            case 0:
              partialResult = tripleAVLFilePhases[TI_3012].findTuplesForMeta(graphNode);
              break;
            case MASK0:
              partialResult = tripleAVLFilePhases[TI_3012].findTuplesForMeta(graphNode, node0);
              break;
            case MASK1:
              partialResult = tripleAVLFilePhases[TI_3120].findTuplesForMeta(graphNode, node1);
              break;
            case MASK0 | MASK1:
              partialResult = tripleAVLFilePhases[TI_3012].findTuplesForMeta(graphNode, node0, node1);
              break;
            case MASK2:
              partialResult = tripleAVLFilePhases[TI_3201].findTuplesForMeta(graphNode, node2);
              break;
            case MASK0 | MASK2:
              partialResult = tripleAVLFilePhases[TI_3201].findTuplesForMeta(graphNode, node2, node0);
              break;
            case MASK1 | MASK2:
              partialResult = tripleAVLFilePhases[TI_3120].findTuplesForMeta(graphNode, node1, node2);
              break;
            default:
              throw new AssertionError("Search structure incorrectly calculated");
          }
          graphedTuples.add(partialResult);
        }
        return TuplesOperations.appendCompatible(graphedTuples);
      } catch (TuplesException te) {
        throw new StatementStoreException("Error accessing Tuples", te);
      } catch (IOException ex) {
        throw new StatementStoreException("I/O error", ex);
      }
    }


    /**
     * Iterates over all graphs, getting all tuples in the requested order,
     * and joining all the results together into a single tuples.
     * @param variableMask Determines the required ordering of the data.
     * @return A StoreTuples with all the intermediate tuples appended.
     * @throws StatementStoreException On an error accessing the store.
     */
    StoreTuples joinGraphedTuples(int variableMask) throws StatementStoreException {
      try {
        assert (variableMask & MASK3) == 0 : "Must not be asking to join on multiple graphs unless graph is variable.";

        // get the graphNodes if not already configured
        if (graphNodes.isEmpty()) throw new IllegalStateException("Unable to query for variable graphs until graphs are initialized");

        ArrayList<StoreTuples> graphedTuples = new ArrayList<StoreTuples>();
        for (long graphNode: graphNodes) {
          int phaseIndex;
          switch (variableMask) {
            case 0:
            case MASK0:
            case MASK0 | MASK1:
            case MASK0 | MASK1 | MASK2:
              phaseIndex = TI_3012;
              break;
            case MASK1:
            case MASK1 | MASK2:
              phaseIndex = TI_3120;
              break;
            case MASK2:
            case MASK0 | MASK2:
              phaseIndex = TI_3201;
              break;
            default:
              throw new AssertionError("Search structure incorrectly calculated");
          }
          StoreTuples partialResult = tripleAVLFilePhases[phaseIndex].findTuplesForMeta(graphNode);
          graphedTuples.add(partialResult);
        }
        return TuplesOperations.appendCompatible(graphedTuples);
      } catch (TuplesException te) {
        throw new StatementStoreException("Error accessing Tuples", te);
      } catch (IOException ex) {
        throw new StatementStoreException("I/O error", ex);
      }
    }


    /**
     * Test is there exist triples according to a given pattern
     * @param node0 A subject gNode, or NONE
     * @param node1 A predicate gNode, or NONE
     * @param node2 A object gNode, or NONE
     * @param node3 A subject gNode. May not be NONE
     * @return <code>true</code> if there exist triples that match the pattern
     * @throws StatementStoreException A structural or IO exception
     */
    boolean existsTriples(long node0, long node1, long node2, long node3) throws StatementStoreException {
      if (node3 == NONE) throw new IllegalStateException("Graph must be specified");

      if (
          node0 < NodePool.NONE ||
          node1 < NodePool.NONE ||
          node2 < NodePool.NONE ||
          node3 < NodePool.NONE
      ) {
        // There is at least one query node (comes from the query, but not in the data pool).
        // Return an empty StoreTuples.
        return false;
      }

      int variableMask =
          (node0 != NONE ? MASK0 : 0) |
          (node1 != NONE ? MASK1 : 0) |
          (node2 != NONE ? MASK2 : 0) |
          MASK3;

      try {
        switch (variableMask) {
          case MASK3:
            return tripleAVLFilePhases[TI_3012].existsTriples(node3);
          case MASK0 | MASK3:
            return tripleAVLFilePhases[TI_3012].existsTriples(node3, node0);
          case MASK1 | MASK3:
            return tripleAVLFilePhases[TI_3120].existsTriples(node3, node1);
          case MASK0 | MASK1 | MASK3:
            return tripleAVLFilePhases[TI_3012].existsTriples(node3, node0, node1);
          case MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3201].existsTriples(node3, node2);
          case MASK0 | MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3201].existsTriples(node3, node2, node0);
          case MASK1 | MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3120].existsTriples(node3, node1, node2);
          case MASK0 | MASK1 | MASK2 | MASK3:
            return tripleAVLFilePhases[TI_3012].existsTriple(node3, node0, node1, node2);
          default:
            throw new AssertionError("Search structure incorrectly calculated");
        }
      } catch (IOException ex) {
        throw new StatementStoreException("I/O error", ex);
      }
    }


    /**
     * Check that each index contains the same number of triples
     * @throws AssertionError if the indexes contain a differing number of triples
     */ 
    long checkIntegrity() {
      long nrTriples[] = new long[NR_INDEXES];

      for (int i = 0; i < NR_INDEXES; ++i) nrTriples[i] = tripleAVLFilePhases[i].checkIntegrity();

      for (int i = 1; i < NR_INDEXES; ++i) {
        if (nrTriples[0] != nrTriples[i]) {
          StringBuffer sb = new StringBuffer("tripleAVLFiles disagree on the number of triples:");
          for (int j = 0; j < NR_INDEXES; ++j) sb.append(' ').append(nrTriples[j]);
          throw new AssertionError(sb.toString());
        }
      }

      return nrTriples[0];
    }


    /**
     * Ask the system for all the known graphs.
     * @return All the known graph nodes.
     */
    LinkedHashSet<Long> scanForGraphs() throws StatementStoreException, IOException {
      LinkedHashSet<Long> nodeList = new LinkedHashSet<Long>();

      if (systemGraphNode == NONE || rdfTypeNode == NONE || graphTypeNode == NONE) {
        if (logger.isDebugEnabled()) {
          logger.debug("Unable to scan for graphs, nodes not initialized. systemGraphNode = " +
                       systemGraphNode + ", rdfTypeNode = " + rdfTypeNode + ", graphTypeNode = " + graphTypeNode);
        }
        return nodeList;
      }

      StoreTuples graphTuples = tripleAVLFilePhases[TI_3120].findTuples(systemGraphNode, rdfTypeNode, graphTypeNode);
      assert graphTuples.getNumberOfVariables() == 1;

      try {
        graphTuples.beforeFirst();
        while (graphTuples.next()) nodeList.add(graphTuples.getColumnValue(0));
      } catch (TuplesException e) {
        throw new StatementStoreException("Unable to construct a result containing all graphs.", e);
      }

      if (logger.isDebugEnabled()) logger.debug("Initializing graph nodes from statement indexes: " + nodeList);

      return nodeList;
    }


    /**
     * Increment the reference count on this object.
     * @return A new token representing the reference.
     */
    Token use() {
      return new Token();
    }


    /**
     * A token to reference the phase, incrementing the reference count from the perpective
     * of the garbage collector.
     */
    final class Token {

      /** A list of tokens from the underlying indexes */
      private TripleAVLFile.Phase.Token[] tripleAVLFileTokens = new TripleAVLFile.Phase.Token[NR_INDEXES];

      /** The phase being referenced */
      private Phase phase = Phase.this;


      /**
       * Creates a token. This creates tokens for the underlying objects as well.
       */
      Token() {
        for (int i = 0; i < NR_INDEXES; ++i) tripleAVLFileTokens[i] = tripleAVLFilePhases[i].use();
      }


      /**
       * Get the phase that this token represents.
       * @return The phase referenced by this token.
       */
      public Phase getPhase() {
        assert tripleAVLFileTokens != null : "Invalid Token";
        return phase;
      }


      /**
       * Reduce the reference count on the referenced phase by releasing this token.
       * The token may not be used after being released.
       */
      public void release() {
        assert tripleAVLFileTokens != null : "Invalid Token";
        for (int i = 0; i < NR_INDEXES; ++i) tripleAVLFileTokens[i].release();
        tripleAVLFileTokens = null;
        phase = null;
      }

    }

  }


  /**
   * @see org.mulgara.store.xa.XAStatementStore#initializeSystemNodes(long, long, long)
   * Set the various system graph nodes. These may only be set once, but will allow duplicate calls if the values are the same.
   * @param systemGraphNode The new system graph node.
   * @param rdfTypeNode The node for rdf:graph.
   * @param systemGraphTypeNode The node for the system graph type.
   * @throws StatementStoreException 
   */
  public void initializeSystemNodes(long systemGraphNode, long rdfTypeNode, long systemGraphTypeNode) throws StatementStoreException {
    if (this.systemGraphNode != NONE && systemGraphNode != this.systemGraphNode) {
      throw new IllegalStateException("Cannot set system graph again. Was: " + this.systemGraphNode + ", now: " + systemGraphNode);
    }
    if (systemGraphNode < 0) throw new IllegalArgumentException("Attempt to set invalid system graph node");
    this.systemGraphNode = systemGraphNode;

    if (this.rdfTypeNode != NONE && rdfTypeNode != this.rdfTypeNode) {
      throw new IllegalStateException("Cannot set the rdf:type node again. Was: " + this.rdfTypeNode + ", now: " + rdfTypeNode);
    }
    if (rdfTypeNode < 0) throw new IllegalArgumentException("Attempt to set invalid rdf:type node");
    this.rdfTypeNode = rdfTypeNode;

    if (this.graphTypeNode != NONE && systemGraphTypeNode != this.graphTypeNode) {
      throw new IllegalStateException("Cannot set graph type again. Was: " + this.graphTypeNode + ", now: " + systemGraphTypeNode);
    }
    if (systemGraphTypeNode < 0) throw new IllegalArgumentException("Attempt to set invalid graph type node");
    this.graphTypeNode = systemGraphTypeNode;
    
    if (currentPhase != null) {
      try {
        currentPhase.graphNodes = currentPhase.scanForGraphs();
      } catch (IOException e) {
        throw new StatementStoreException("Error while scanning for graph nodes", e);
      }
    }
  }

}
