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

package org.mulgara.store.stringpool.xa;

// Java 2 standard packages
import java.io.*;
import java.nio.*;
import java.util.*;
import java.net.URI;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

// log4j classes
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.stringpool.*;
import org.mulgara.store.stringpool.xa.SPObjectFactoryImpl;
import org.mulgara.store.tuples.Annotation;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.SimpleTuplesFormat;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.xa.*;
import org.mulgara.util.*;

/**
 * A mapping from graph nodes to SPObjects and vice-versa.
 * SPObjects are stored in a series of files, with each file holding objects
 * of different size ranges.
 *
 * @created 2001-10-09
 *
 * @author David Makepeace
 * @author Paul Gearon
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/07/21 19:16:31 $
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
public final class XAStringPoolImpl implements XAStringPool {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(XAStringPoolImpl.class);

  /** Single setting to enable the cache. */
  private static final boolean CACHE_ENABLED = true;

  /** Use a cache for gNode to string pool object mappings. */
  private static final boolean GN2SPO_CACHE_ENABLED = CACHE_ENABLED;

  /** Use a cache for string pool object to gNode mappings. */
  private static final boolean SPO2GN_CACHE_ENABLED = CACHE_ENABLED;

  /** Unique value to mark this file as a string pool. */
  private static final int FILE_MAGIC = 0xa5f3f4f2;

  /** The current version of this file format. */
  private static final int FILE_VERSION = 10;

  /** The number of files used for the string pool. */
  private static final int NR_BLOCK_FILES = 20;


  /* Offsets of metaroot fields. */

  /** Index of the file magic number within each of the two on-disk metaroots. */
  private static final int IDX_MAGIC = 0;

  /** Index of the file version number within each of the two on-disk metaroots. */
  private static final int IDX_VERSION = 1;

  /** Index of the valid flag (in ints) within each of the two on-disk metaroots. */
  private static final int IDX_VALID = 2;

  /** The index of the phase number in the on-disk phase. */
  private static final int IDX_PHASE_NUMBER = 3;


  /** The size of the header of a metaroot in ints. */
  private static final int HEADER_SIZE_INTS = 4;

  /** The size of the header of a metaroot in longs. */
  private static final int HEADER_SIZE_LONGS = (HEADER_SIZE_INTS + 1) / 2;

  /** The size of a metaroot in longs. */
  private static final int METAROOT_SIZE = HEADER_SIZE_LONGS +
      Phase.RECORD_SIZE;

  /** The number of metaroots in the metaroot file. */
  private static final int NR_METAROOTS = 2;

  static final int MIN_BLOCK_SIZE = 16;
  static final int LOG2_MIN_BLOCK_SIZE = XAUtils.log2(MIN_BLOCK_SIZE);

  // Layout of the AVLNode and GNodeToData structures.
  static final int IDX_TYPE_CATEGORY_B = 1;
  static final int IDX_TYPE_ID_B = 2;
  static final int IDX_SUBTYPE_ID_B = 3;
  static final int IDX_DATA_SIZE_I = 1;
  static final int IDX_DATA = 1;
  static final int IDX_BLOCK_ID = 9;
  static final int IDX_GRAPH_NODE = 10;

  /** The payload size of the AVLNode in longs. */
  static final int PAYLOAD_SIZE = IDX_GRAPH_NODE + 1;

  /** The size of a G2N record in longs. */
  // A G2N record does not contain a GRAPH_NODE field.
  static final int GN2SPO_BLOCKSIZE = IDX_BLOCK_ID + 1;

  /** The maximum number of bytes of SPObject data that will fit in the AVL node. */
  static final int MAX_DIRECT_DATA_BYTES =
      (IDX_GRAPH_NODE - IDX_DATA) * Constants.SIZEOF_LONG;

  /** A factory for this class. */
  static final SPObjectFactory SPO_FACTORY = SPObjectFactoryImpl.getInstance();

  /** The variables to use when pretending that the string pool is a Tuples. */
  static final Variable[] VARIABLES = new Variable[] {
    StatementStore.VARIABLES[0]
  };

  /**
   * The name of the triple store which forms the base name for the string pool
   * data files.
   */
  private String fileName;

  /** The LockFile that protects the node pool from being opened twice. */
  private LockFile lockFile;

  /** The BlockFile for the node pool metaroot file. */
  private BlockFile metarootFile = null;

  /** The metaroot blocks of the metaroot file. */
  private Block[] metarootBlocks = new Block[NR_METAROOTS];

  /** Flag used to indicate if the file version is right for the current code. */
  private boolean wrongFileVersion = false;

  /** The index file which contains mappings to the block files. */
  private AVLFile avlFile;

  /** Maps a gNode to a tuple of all type info and the location of the SPObject. */
  private IntFile gNodeToDataFile;

  /** All of the block files for storing SPObjects. */
  private ManagedBlockFile[] blockFiles = new ManagedBlockFile[NR_BLOCK_FILES];

  /** The writing phase. */
  private Phase currentPhase = null;

  /** Maps gNodes to SPObjects. */
  private GN2SPOCache gn2spoCache = new GN2SPOCache();

  /** The node pool to use with this string pool. */
  private XANodePool xaNodePool;

  /** Indicates that the current phase has been written to. */
  private boolean dirty = true;

  /** The valid phase index on file to use.  Must always be 0 or 1. */
  private int phaseIndex = 0;

  /** The number of the current phase.  These increase monotonically. */
  private int phaseNumber = 0;

  /** A Token on the last committed phase. */
  private Phase.Token committedPhaseToken = null;

  /** Object used for locking on synchronized access to the committed phase. */
  private Object committedPhaseLock = new Object();

  /** Phase reference for when the phase is being written. */
  private Phase.Token recordingPhaseToken = null;

  /** Indicates that the phase is written but not yet acknowledged as valid. */
  private boolean prepared = false;


  /**
   * Constructor for an XAStringPool.
   *
   * @param fileName The file name base to use for the files.
   * @throws IOException If an I/O error occurred.
   */
  public XAStringPoolImpl(String fileName) throws IOException {
    this.fileName = fileName;

    lockFile = LockFile.createLockFile(fileName + ".sp.lock");

    try {
      // Open the metaroot file.
      RandomAccessFile metarootRAF = null;
      try {
        metarootRAF = new RandomAccessFile(fileName + ".sp", "r");
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

      avlFile = new AVLFile(fileName + ".sp_avl", PAYLOAD_SIZE);
      gNodeToDataFile = IntFile.open(new File(fileName + ".sp_nd"));

      for (int i = 0; i < NR_BLOCK_FILES; ++i) {
        String num = Integer.toString(i);
        if (num.length() < 2) {
          num = "0" + num;
        }
        int blockSize = MIN_BLOCK_SIZE << i;
        blockFiles[i] = new ManagedBlockFile(fileName + ".sp_" + num,
            blockSize, blockSize > MappedBlockFile.REGION_SIZE ?
            BlockFile.IOType.EXPLICIT : BlockFile.IOType.DEFAULT
        );
      }
    } catch (IOException ex) {
      try {
        close();
      } catch (StringPoolException ex2) {
        // NO-OP
      }
      throw ex;
    }
  }


  /**
   * @see org.mulgara.store.xa.XAStringPool#setNodePool(org.mulgara.store.xa.XANodePool)
   */
  public void setNodePool(XANodePool xaNodePool) {
    this.xaNodePool = xaNodePool;
  }


  /**
   * Gets the PhaseNumber attribute of the XAStringPoolImpl object
   *
   * @return The PhaseNumber value
   * @throws SimpleXAResourceException EXCEPTION TO DO
   */
  public synchronized int getPhaseNumber() throws SimpleXAResourceException {
    checkInitialized();
    return phaseNumber;
  }


  //
  // Methods from StringPool.
  //

  /**
   * Gets the SPObjectFactory associated with this StringPool implementation.
   */
  public SPObjectFactory getSPObjectFactory() {
    return SPO_FACTORY;
  }

  /**
   * Adds a new SObject into the string pool, returning the new gNode that is associated
   * with this object.
   * @param spObject The object to store.
   * @return The new gNode for the object.
   * @throws StringPoolException If the string pool could not store the object.
   * @throws NodePoolException If the node pool could not allocate a gNode.
   */
  public long put(SPObject spObject) throws StringPoolException, NodePoolException {
    long gNode = xaNodePool.newNode();
    putInternal(gNode, spObject);
    return gNode;
  }

  /**
   * Adds a new graph node / SPObject pair into the string pool. An error will
   * result if the graph node or the SPObject (or both) already exists in the
   * pool.
   * @param gNode The graph node to add.
   * @param spObject The SPObject to add.
   * @throws StringPoolException if the graph node or the SPObject already
   * exists in the pool.
   */
  @Deprecated
  public synchronized void put(long gNode, SPObject spObject) throws StringPoolException {
    putInternal(gNode, spObject);
  }


  /**
   * Adds a new graph node / SPObject pair into the string pool. An error will
   * result if the graph node or the SPObject (or both) already exists in the
   * pool.
   * @param gNode The graph node to add.
   * @param spObject The SPObject to add.
   * @throws StringPoolException if the graph node or the SPObject already
   * exists in the pool.
   */
  private synchronized void putInternal(
      long gNode, SPObject spObject
  ) throws StringPoolException {
    checkInitialized();
    if (!dirty && currentPhase.isInUse()) {
      try {
        new Phase();
      } catch (IOException ex) {
        throw new StringPoolException("I/O error", ex);
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("put(" + gNode + ", " + spObject + ")");
    }

    try {
      currentPhase.put(gNode, spObject);
    } catch (RuntimeException ex) {
      if (logger.isDebugEnabled()) {
        logger.debug("RuntimeException in put()", ex);
      }
      throw ex;
    } catch (Error e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Error in put()", e);
      }
      throw e;
    } catch (StringPoolException ex) {
      if (logger.isDebugEnabled()) {
        logger.debug("StringPoolException in put()", ex);
      }
      throw ex;
    }
  }


  /**
   * Removes a graph node / SPObject pair from the string pool.
   *
   * @param gNode the graph node.
   * @return <code>true</code> if the node existed and was removed.
   * @throws StringPoolException if an internal error occurs.
   */
  public synchronized boolean remove(long gNode) throws StringPoolException {
    checkInitialized();
    if (!dirty && currentPhase.isInUse()) {
      try {
        new Phase();
      } catch (IOException ex) {
        throw new StringPoolException("I/O error", ex);
      }
    }
    return currentPhase.remove(gNode);
  }


  /**
   * Finds a graph node based on its matching SPObject.
   *
   * @param spObject The SPObject to search on.
   * @return The graph node. <code>Graph.NONE</code> if not found.
   * @throws StringPoolException EXCEPTION TO DO
   */
  public synchronized long findGNode(SPObject spObject) throws StringPoolException {
    checkInitialized();
    return currentPhase.findGNode(spObject, null);
  }


  /**
   * @see org.mulgara.store.xa.XAStringPool#findGNode(org.mulgara.store.stringpool.SPObject, boolean)
   */
  public synchronized long findGNode(SPObject spObject, boolean create) throws StringPoolException {
    checkInitialized();
    if (xaNodePool == null) throw new IllegalArgumentException("nodePool is null");
    return currentPhase.findGNode(spObject, xaNodePool);
  }


  /**
   * @see org.mulgara.store.stringpool.StringPool#findGNode(org.mulgara.store.stringpool.SPObject, org.mulgara.store.nodepool.NodePool)
   */
  @Deprecated
  public synchronized long findGNode(SPObject spObject, NodePool nodePool) throws StringPoolException {
    checkInitialized();
    if (nodePool == null) throw new IllegalArgumentException("nodePool parameter is null");
    return currentPhase.findGNode(spObject, nodePool);
  }


  /**
   * Finds and returns the SPObject corresponding to <var>gNode</var>, or
   * <code>null</code> if no such graph node is in the pool.
   *
   * @param gNode A graph node to search for within the pool.
   * @return the SPObject corresponding to <var>gNode</var>, or
   * <code>null</code> if no such graph node is in the pool.
   * @throws StringPoolException if an internal error occurs.
   */
  public synchronized SPObject findSPObject(long gNode) throws StringPoolException {
    checkInitialized();
    return currentPhase.findSPObject(gNode);
  }


  public synchronized Tuples findGNodes(
      SPObject lowValue, boolean inclLowValue,
      SPObject highValue, boolean inclHighValue
  ) throws StringPoolException {
    checkInitialized();
    dirty = false;
    return currentPhase.findGNodes(lowValue, inclLowValue, highValue, inclHighValue);
  }


  public synchronized Tuples findGNodes(SPObject.TypeCategory typeCategory, URI typeURI) throws StringPoolException {
    checkInitialized();
    dirty = false;
    return currentPhase.findGNodes(typeCategory, typeURI);
  }


  public XAStringPool newReadOnlyStringPool() {
    return new ReadOnlyStringPool();
  }


  public XAStringPool newWritableStringPool() {
    return this;
  }


  /**
   * Close all the files used in the string pool.
   *
   * @throws StringPoolException EXCEPTION TO DO
   */
  public synchronized void close() throws StringPoolException {
    try {
      close(false);
    } catch (IOException ex) {
      throw new StringPoolException("I/O error closing string pool.", ex);
    }
  }


  /**
   * Close this string pool, if it is currently open, and remove all files
   * associated with it.
   *
   * @throws StringPoolException EXCEPTION TO DO
   */
  public synchronized void delete() throws StringPoolException {
    try {
      close(true);
    } catch (IOException ex) {
      throw new StringPoolException("I/O error deleting string pool.", ex);
    } finally {
      blockFiles = null;
      gNodeToDataFile = null;
      avlFile = null;
      metarootFile = null;
    }
  }


  protected void finalize() throws Throwable {
    // close the string pool if it has not already been closed explicitly.
    try {
      close(false);
    } catch (Throwable t) {
      logger.warn(
          "Exception in finalize while trying to close the string pool.", t
      );
    } finally {
      super.finalize();
    }
  }


  /**
   * @see org.mulgara.store.xa.SimpleXAResource#release()
   */
  public void release() throws SimpleXAResourceException {
    if (xaNodePool != null) xaNodePool.release();
  }


  /**
   * @see org.mulgara.store.xa.SimpleXAResource#refresh()
   */
  public void refresh() throws SimpleXAResourceException {
    if (xaNodePool != null) xaNodePool.refresh();
  }


  /**
   * Inserts a new node into the node pool.
   *
   * @param gNode the unique identifier to insert into the node pool.
   * @throws IOException If there was an error writing to the file.
   */
  public synchronized void newNode(long gNode) throws IOException {
    if (GN2SPO_CACHE_ENABLED) {
      gn2spoCache.remove(gNode);
    }
    long offset = gNode * GN2SPO_BLOCKSIZE;
    long bOffset = offset * Constants.SIZEOF_LONG;
    gNodeToDataFile.putByte(
        bOffset + IDX_TYPE_CATEGORY_B, (byte)SPObject.TypeCategory.TCID_FREE
    );
  }


  /**
   * Calls remove - but doesn't return if it was successful or not.
   *
   * @param gNode the graph node.
   * @throws StringPoolException if an internal error occurs.
   */
  public synchronized void releaseNode(long gNode) throws StringPoolException {
    remove(gNode);
  }


  //
  // Methods from SimpleXAResource.
  //

  /**
   * METHOD TO DO
   *
   * @param phaseNumber PARAMETER TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   */
  public synchronized void clear(int phaseNumber) throws IOException, SimpleXAResourceException {
    if (currentPhase != null) throw new IllegalStateException("StringPool already has a current phase.");

    openMetarootFile(true);

    synchronized (committedPhaseLock) {
      committedPhaseToken = new Phase().use();
    }
    this.phaseNumber = phaseNumber;
    phaseIndex = 1;
    avlFile.clear();
    gNodeToDataFile.clear();
    for (int i = 0; i < NR_BLOCK_FILES; ++i) blockFiles[i].clear();

    new Phase();
  }


  /**
   * METHOD TO DO
   *
   * @throws IOException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   */
  public synchronized void clear() throws IOException, SimpleXAResourceException {
    if (currentPhase == null) clear(0);

    // TODO - should throw an exception if clear() is called after any other
    // operations are performed.  Calling clear() multiple times should be
    // permitted.
  }


  /**
   * Writes all transactional data to disk, in preparation for a full commit.
   * @throws SimpleXAResourceException Occurs due to an IO error when writing data to disk.
   */
  public void prepare() throws SimpleXAResourceException {
    // TODO: This synchronization is probably redundant due to the global lock in StringPoolSession
    synchronized(this) {
      checkInitialized();
  
      if (prepared) {
        // prepare already performed.
        throw new SimpleXAResourceException("prepare() called twice.");
      }
  
      try {
        // Perform a prepare.
        recordingPhaseToken = currentPhase.use();
        Phase recordingPhase = currentPhase;
        new Phase();
  
        // Ensure that all data associated with the phase is on disk.
        avlFile.force();
        for (int i = 0; i < NR_BLOCK_FILES; ++i) {
          blockFiles[i].force();
        }
        gNodeToDataFile.force();
  
        // Write the metaroot.
        int newPhaseIndex = 1 - phaseIndex;
        int newPhaseNumber = phaseNumber + 1;
  
        Block block = metarootBlocks[newPhaseIndex];
        block.putInt(IDX_VALID, 0); // should already be invalid.
        block.putInt(IDX_PHASE_NUMBER, newPhaseNumber);
        logger.debug("Writing string pool metaroot for phase: " + newPhaseNumber);
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
          // Something went wrong!
          logger.error("Prepare failed.");
          if (recordingPhaseToken != null) {
            recordingPhaseToken.release();
            recordingPhaseToken = null;
          }
        }
      }
    }
    if (xaNodePool != null) xaNodePool.prepare();
  }


  /**
   * Writes to the meta-root file to make all the transaction data written in {@link #prepare()}
   * suddenly valid.
   * @throws SimpleXAResourceException Error due to IO problems.
   */
  public void commit() throws SimpleXAResourceException {
    // TODO: This synchronization is probably redundant due to the global lock in StrinPoolSession
    synchronized (this) {
      if (!prepared) {
        // commit without prepare.
        throw new SimpleXAResourceException("commit() called without previous prepare().");
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
    if (xaNodePool != null) xaNodePool.commit();
  }


  /**
   * Returns an array which contains a list of the phase numbers for all valid
   * phases in the metaroot file. The array will contain zero, one or two
   * elements. There will be no valid phases if no prepares have been
   * successfully performed since the SimpleXAResource was initially created.
   *
   * @return the array of valid phase numbers.
   * @throws SimpleXAResourceException if {@link #selectPhase} or {@link #clear}
   *      has already been called.
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
          "selectPhase() called on initialized StringPoolImpl."
      );
    }
    if (metarootFile == null) {
      throw new SimpleXAResourceException(
          "String pool metaroot file is not open."
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
        committedPhaseToken = new Phase(
            metarootBlocks[phaseIndex], HEADER_SIZE_LONGS
        ).use();
      }
      this.phaseNumber = phaseNumber;
    } catch (IllegalStateException ex) {
      throw new SimpleXAResourceException(
          "Cannot construct initial phase.", ex
      );
    }
    new Phase();

    // Invalidate the on-disk metaroot that the new phase will be saved to.
    Block block = metarootBlocks[1 - phaseIndex];
    block.putInt(IDX_VALID, 0);
    block.write();
    metarootFile.force();
  }


  /**
   * Drops all data in the current transaction, recovering any used resources.
   * @throws SimpleXAResourceException Caused by any IO errors.
   */
  public void rollback() throws SimpleXAResourceException {
    // TODO: This synchronization is probably redundant due to the global lock in StringPoolSession
    synchronized (this) {
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
          new Phase(committedPhaseToken.getPhase());
        } catch (IOException ex) {
          if (success) { // this is a new exception...
            throw new SimpleXAResourceException(
                "I/O error while performing rollback (new committed phase)", ex
            );
          } else { // something else already went wrong, log it and throw it away.
            logger.info("I/O error while performing rollback (new committed phase)", ex);
          }
        }
      }
    }
    if (xaNodePool != null) xaNodePool.rollback();
  }


  /**
   * METHOD TO DO
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

    if (blockFiles != null) {
      for (int i = 0; i < NR_BLOCK_FILES; ++i) {
        if (blockFiles[i] != null) {
          blockFiles[i].unmap();
        }
      }
    }

    if (gNodeToDataFile != null) {
      gNodeToDataFile.unmap();
    }
    if (avlFile != null) {
      avlFile.unmap();
    }

    if (metarootFile != null) {
      if (metarootBlocks[0] != null) {
        metarootBlocks[0] = null;
      }
      if (metarootBlocks[1] != null) {
        metarootBlocks[1] = null;
      }
      metarootFile.unmap();
    }
  }


  /**
   * Close all the files used in the string pool.
   *
   * @param deleteFiles PARAMETER TO DO
   * @throws IOException EXCEPTION TO DO
   */
  private void close(boolean deleteFiles) throws IOException {
    try {
      unmap();
    } finally {
      try {
        if (blockFiles != null) {
          IOException ex = null;

          for (int i = 0; i < NR_BLOCK_FILES; ++i) {
            try {
              if (blockFiles[i] != null) {
                if (deleteFiles) {
                  blockFiles[i].delete();
                } else {
                  blockFiles[i].close();
                }
              }
            } catch (IOException ex2) {
              ex = ex2;
            } finally {
              blockFiles[i] = null;
            }
          }

          if (ex != null) {
            throw ex;
          }
        }
      } finally {
        try {
          if (gNodeToDataFile != null) {
            if (deleteFiles) {
              gNodeToDataFile.delete();
            } else {
              gNodeToDataFile.close();
            }
          }
        } finally {
          try {
            if (avlFile != null) {
              if (deleteFiles) {
                avlFile.delete();
              } else {
                avlFile.close();
              }
            }
          } finally {
            try {
              if (metarootFile != null) {
                if (deleteFiles) {
                  metarootFile.delete();
                } else {
                  metarootFile.close();
                }
              }
            } finally {
              if (lockFile != null) {
                lockFile.release();
                lockFile = null;
              }
            }
          }
        }
      }
    }
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
          fileName + ".sp", METAROOT_SIZE * Constants.SIZEOF_LONG,
          BlockFile.IOType.EXPLICIT
      );

      long nrBlocks = metarootFile.getNrBlocks();
      if (nrBlocks != NR_METAROOTS) {
        if (nrBlocks > 0) {
          logger.info(
              "String pool metaroot file for triple store \"" + fileName +
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
          "No current phase.  " +
          "StringPool has not been initialized or has been closed."
      );
    }
  }


  static void get(
      IntFile intFile, long bOffset, ByteBuffer data
  ) throws IOException {
    while (data.hasRemaining()) {
      data.put(intFile.getByte(bOffset++));
    }
  }


  static void put(
      IntFile intFile, long bOffset, ByteBuffer data
  ) throws IOException {
    while (data.hasRemaining()) {
      intFile.putByte(bOffset++, data.get());
    }
  }


  final class ReadOnlyStringPool implements XAStringPool {

    private Phase phase = null;

    private Phase.Token token = null;

    /**
     * CONSTRUCTOR ReadOnlyStringPool TO DO
     */
    ReadOnlyStringPool() {
      synchronized (committedPhaseLock) {
        if (committedPhaseToken == null) {
          throw new IllegalStateException(
              "Cannot create read only view of uninitialized StringPool."
          );
        }
      }
    }


    /** Sets the node pool for this string pool. */
    public void setNodePool(XANodePool xaNodePool) {
      /* no-op */
    }


    /**
     * Gets the SPObjectFactory associated with this StringPool implementation.
     */
    public SPObjectFactory getSPObjectFactory() {
      return SPO_FACTORY;
    }


    /**
     * @see org.mulgara.store.stringpool.StringPool#put(long, SPObject)
     */
    public long put(SPObject spObject) {
      throw new UnsupportedOperationException("Trying to modify a read-only string pool.");
    }


    /**
     * @see org.mulgara.store.stringpool.StringPool#put(long, org.mulgara.store.stringpool.SPObject)
     */
    public void put(long gNode, SPObject spObject) {
      throw new UnsupportedOperationException("Trying to modify a read-only string pool.");
    }


    /**
     * Removes graph node / SPObject pair from the string pool.
     *
     * @param gNode the graph node.
     * @return <code>true</code> if the node existed and was removed.
     * @throws UnsupportedOperationException since this string pool is read
     *      only.
     */
    public boolean remove(long gNode) {
      throw new UnsupportedOperationException(
          "Trying to modify a read-only string pool."
      );
    }


    /**
     * Finds the graph node matching a given SPObject.
     *
     * @param spObject The object being searched for.
     * @return The graph node. <code>Graph.NONE</code> if not found.
     * @throws StringPoolException EXCEPTION TO DO
     */
    public synchronized long findGNode(SPObject spObject) throws StringPoolException {
      return phase.findGNode(spObject, null);
    }


    @Deprecated
    public synchronized long findGNode(SPObject spObject, NodePool nodePool) throws StringPoolException {
      throw new UnsupportedOperationException("Trying to modify a read-only string pool.");
    }

    public synchronized long findGNode(SPObject spObject, boolean create) throws StringPoolException {
      if (create) throw new UnsupportedOperationException("Trying to modify a read-only string pool.");
      return phase.findGNode(spObject, null);
    }


    /**
     * Finds and returns the SPObject corresponding to <var>gNode</var>, or
     * <code>null</code> if no such graph node is in the pool.
     *
     * @param gNode A graph node to search for within the pool.
     * @return the SPObject corresponding to <var>gNode</var>, or
     * <code>null</code> if no such graph node is in the pool.
     * @throws StringPoolException if an internal error occurs.
     */
    public synchronized SPObject findSPObject(long gNode) throws StringPoolException {
      return phase.findSPObject(gNode);
    }


    public synchronized Tuples findGNodes(
        SPObject lowValue, boolean inclLowValue,
        SPObject highValue, boolean inclHighValue
    ) throws StringPoolException {
      return phase.findGNodes(lowValue, inclLowValue, highValue, inclHighValue);
    }


    public synchronized Tuples findGNodes(SPObject.TypeCategory typeCategory, URI typeURI) throws StringPoolException {
      return phase.findGNodes(typeCategory, typeURI);
    }


    public XAStringPool newReadOnlyStringPool() {
      throw new UnsupportedOperationException();
    }


    public XAStringPool newWritableStringPool() {
      throw new UnsupportedOperationException();
    }


    /**
     * Close all the files used in the string pool.
     */
    public void close() {
      throw new UnsupportedOperationException("Trying to close a read-only string pool.");
    }


    /**
     * Close this string pool, if it is currently open, and remove all files
     * associated with it.
     */
    public void delete() {
      throw new UnsupportedOperationException("Trying to delete a read-only string pool.");
    }


    public synchronized void release() {
      try {
        if (token != null) token.release();
      } finally {
        phase = null;
        token = null;
      }
    }


    public synchronized void refresh() {
      synchronized (committedPhaseLock) {
        Phase committedPhase = committedPhaseToken.getPhase();
        if (phase != committedPhase) {
          if (token != null) token.release();
          phase = committedPhase;
          token = phase.use();
        }
      }
    }

    public void newNode(long gNode) {
      throw new UnsupportedOperationException();
    }

    public void releaseNode(long gNode) {
      throw new UnsupportedOperationException();
    }

    public void prepare() { }
    public void commit() { }
    public void rollback() { }
    public void clear() { }
    public void clear(int phaseNumber) { }

    public int[] recover() {
      throw new UnsupportedOperationException("Attempting to recover ReadOnlyStringPool");
    }

    public void selectPhase(int phaseNumber) {
      throw new UnsupportedOperationException("Attempting to selectPhase of ReadOnlyStringPool");
    }

    public int getPhaseNumber() {
      return phaseNumber;
    }
  }


  final class Phase implements PersistableMetaRoot {

    static final int RECORD_SIZE =
        AVLFile.Phase.RECORD_SIZE +
        ManagedBlockFile.Phase.RECORD_SIZE * NR_BLOCK_FILES;

    private SPO2GNCache spo2gnCache = new SPO2GNCache();

    private AVLFile.Phase avlFilePhase;

    private ManagedBlockFile.Phase[] blockFilePhases =
        new ManagedBlockFile.Phase[NR_BLOCK_FILES];


    /**
     * CONSTRUCTOR Phase TO DO
     *
     * @throws IOException EXCEPTION TO DO
     */
    Phase() throws IOException {
      avlFilePhase = avlFile.new Phase();
      for (int i = 0; i < NR_BLOCK_FILES; ++i) {
        blockFilePhases[i] = blockFiles[i].new Phase();
      }
      currentPhase = this;
      dirty = true;
    }


    /**
     * CONSTRUCTOR Phase TO DO
     *
     * @throws IOException EXCEPTION TO DO
     */
    Phase(Phase p) throws IOException {
      assert p != null;

      avlFilePhase = avlFile.new Phase(p.avlFilePhase);
      for (int i = 0; i < NR_BLOCK_FILES; ++i) {
        blockFilePhases[i] = blockFiles[i].new Phase(
            p.blockFilePhases[i]
        );
      }
      currentPhase = this;
      dirty = true;
    }


    /**
     * CONSTRUCTOR Phase TO DO
     *
     * @param b PARAMETER TO DO
     * @param offset PARAMETER TO DO
     * @throws IOException EXCEPTION TO DO
     */
    Phase(Block b, int offset) throws IOException {
      avlFilePhase = avlFile.new Phase(b, offset);
      offset += AVLFile.Phase.RECORD_SIZE;
      for (int i = 0; i < NR_BLOCK_FILES; ++i) {
        blockFilePhases[i] = blockFiles[i].new Phase(b, offset);
        offset += ManagedBlockFile.Phase.RECORD_SIZE;
      }
      currentPhase = this;
      dirty = false;
    }


    /**
     * Writes this PersistableMetaRoot to the specified Block. The ints are
     * written at the current position of the Block.
     *
     * @param b the Block.
     * @param offset PARAMETER TO DO
     */
    public void writeToBlock(Block b, int offset) {
      avlFilePhase.writeToBlock(b, offset);
      offset += AVLFile.Phase.RECORD_SIZE;
      for (int i = 0; i < NR_BLOCK_FILES; ++i) {
        blockFilePhases[i].writeToBlock(b, offset);
        offset += ManagedBlockFile.Phase.RECORD_SIZE;
      }
    }


    boolean isInUse() {
      return blockFilePhases[0].isInUse();
    }


    /**
     * Adds a new graph node / SPObject pair into the string pool. An error
     * will result if the graph node or the SPObject (or both) already exists
     * in the pool.
     *
     * @param gNode The graph node to add.
     * @param spObject The SPObject to add.
     * @throws StringPoolException if the graph node or the SPObject already
     * exists in the pool.
     */
    void put(long gNode, SPObject spObject) throws StringPoolException {
      if (gNode < NodePool.MIN_NODE) throw new IllegalArgumentException("gNode < MIN_NODE");

      AVLNode[] findResult = null;
      try {
        // Check that there is no SPObject corresponding to the graph node.
        long offset = gNode * GN2SPO_BLOCKSIZE;
        long bOffset = offset * Constants.SIZEOF_LONG;
        if (
            gNodeToDataFile.getByte(bOffset + IDX_TYPE_CATEGORY_B) !=
            SPObject.TypeCategory.TCID_FREE
        ) {
          throw new StringPoolException("Graph node already exists.  (Graph node: " + gNode + ")");
        }

        SPObject.TypeCategory typeCategory = spObject.getTypeCategory();
        int typeId;
        int subtypeId;
        if (typeCategory == SPObject.TypeCategory.TYPED_LITERAL) {
          SPTypedLiteral sptl = (SPTypedLiteral)spObject;
          typeId = sptl.getTypeId();
          subtypeId = sptl.getSubtypeId();
        } else {
          typeId = SPObjectFactory.INVALID_TYPE_ID;
          subtypeId = 0;
        }
        ByteBuffer data = spObject.getData();
        SPComparator spComparator = spObject.getSPComparator();
        AVLComparator avlComparator = new SPAVLComparator(spComparator, typeCategory, typeId, subtypeId, data);

        // Find the adjacent nodes.
        findResult = avlFilePhase.find(avlComparator, null);
        if (findResult != null && findResult.length == 1) {
          throw new StringPoolException(
              "SPObject already exists.  (existing graph node: " +
              findResult[0].getPayloadLong(IDX_GRAPH_NODE) + ")"
          );
        }

        put(gNode, findResult,typeCategory, typeId, subtypeId, data);

        if (GN2SPO_CACHE_ENABLED) gn2spoCache.put(gNode, spObject);
        if (SPO2GN_CACHE_ENABLED) spo2gnCache.put(spObject, gNode);
      } catch (IOException ex) {
        throw new StringPoolException("I/O Error", ex);
      } finally {
        if (findResult != null) {
          AVLFile.release(findResult);
        }
      }
    }


    private void put(
        long gNode, AVLNode[] findResult, SPObject.TypeCategory typeCategory,
        int typeId, int subtypeId, ByteBuffer data
    ) throws StringPoolException, IOException {
      long offset = gNode * GN2SPO_BLOCKSIZE;
      long bOffset = offset * Constants.SIZEOF_LONG;

      assert gNodeToDataFile.getByte(bOffset + IDX_TYPE_CATEGORY_B) ==
          SPObject.TypeCategory.TCID_FREE;

      // Create the new AVLNode.
      AVLNode newNode = avlFilePhase.newAVLNodeInstance();
      newNode.putPayloadByte(IDX_TYPE_CATEGORY_B, (byte)typeCategory.ID);
      newNode.putPayloadByte(IDX_TYPE_ID_B, (byte)typeId);
      newNode.putPayloadByte(IDX_SUBTYPE_ID_B, (byte)subtypeId);
      int dataSize = data.limit();
      newNode.putPayloadInt(IDX_DATA_SIZE_I, dataSize);

      long blockId = storeByteBuffer(newNode, data);

      newNode.putPayloadLong(IDX_GRAPH_NODE, gNode);
      newNode.write();

      if (findResult == null) {
        avlFilePhase.insertFirst(newNode);
      } else {
        // Insert the node into the tree.
        int li = AVLFile.leafIndex(findResult);
        findResult[li].insert(newNode, 1 - li);
      }
      newNode.release();

      gNodeToDataFile.putByte(
          bOffset + IDX_TYPE_CATEGORY_B, (byte)typeCategory.ID
      );
      gNodeToDataFile.putByte(bOffset + IDX_TYPE_ID_B, (byte)typeId);
      gNodeToDataFile.putByte(bOffset + IDX_SUBTYPE_ID_B, (byte)subtypeId);

      long iOffset = offset * (Constants.SIZEOF_LONG / Constants.SIZEOF_INT);
      gNodeToDataFile.putInt(iOffset + IDX_DATA_SIZE_I, dataSize);

      // Calculate the number of direct data bytes.
      int directDataSize;
      if (dataSize > MAX_DIRECT_DATA_BYTES) {
        // Make room for the block ID.
        directDataSize = MAX_DIRECT_DATA_BYTES - Constants.SIZEOF_LONG;

        // Store the block ID.
        gNodeToDataFile.putLong(offset + IDX_BLOCK_ID, blockId);
      } else {
        directDataSize = dataSize;
      }

      // Store the data bytes in the GN2SPO file.
      data.rewind();
      data.limit(directDataSize);
      XAStringPoolImpl.put(
          gNodeToDataFile, bOffset + IDX_DATA * Constants.SIZEOF_LONG, data
      );
    }


    /**
     * Stores as much data as possible into the space set aside for SPObject
     * data in the AVLNode and, if necessary, allocates a block in the block
     * file corresponding to the size of the remaining data, puts the contents
     * of the ByteBuffer into the new block, writes the block to the file and
     * returns the block Id of the new block.
     *
     * @param data the data to be written.  No assumption is made about the
     * current possition.  The valid data to be written ranges from 0 to
     * limit().
     * @return the block Id of the new block or Block.INVALID_BLOCK_ID if no
     * block was allocated.
     */
    private long storeByteBuffer(AVLNode avlNode, ByteBuffer data) throws IOException, StringPoolException {
      // Get the number of bytes to be written.
      int dataSize = data.limit();

      if (dataSize == 0) {
        // Special case for an empty ByteBuffer.
        return 0;
      }

      // Calculate the number of direct data bytes.
      int directDataSize;
      if (dataSize > MAX_DIRECT_DATA_BYTES) {
        // Make room for the block ID.
        directDataSize = MAX_DIRECT_DATA_BYTES - Constants.SIZEOF_LONG;
      } else {
        directDataSize = dataSize;
      }

      // Store as many bytes as possible in the AVLNode.
      data.rewind();
      data.limit(directDataSize);
      avlNode.getBlock().put(
          (AVLNode.HEADER_SIZE + IDX_DATA) * Constants.SIZEOF_LONG, data
      );

      assert !data.hasRemaining();

      long blockId;
      if (dataSize > MAX_DIRECT_DATA_BYTES) {
        // Write the overflow bytes to a new block.

        // Reset the limit to the saved value.
        data.limit(dataSize);

        int fileIndex = XAUtils.log2(
            (data.remaining() - 1) >> (LOG2_MIN_BLOCK_SIZE - 1)
        );

        if (fileIndex >= blockFilePhases.length) {
          throw new StringPoolException("Data block too large.");
        }

        ManagedBlockFile.Phase blockFilePhase = blockFilePhases[fileIndex];
        Block block = blockFilePhase.allocateBlock();
        block.put(0, data);
        block.write();

        blockId = block.getBlockId();

        // Store the block ID in the AVLNode payload.
        avlNode.putPayloadLong(IDX_BLOCK_ID, blockId);
        return blockId;
      } else {
        blockId = Block.INVALID_BLOCK_ID;
      }

      return blockId;
    }


    /**
     * Reads data from the specified block into the ByteBuffer starting at the
     * current position.  The number of bytes read is the number of remaining
     * bytes in the ByteBuffer.
     */
    private ByteBuffer retrieveRemainingBytes(ByteBuffer data, long blockId) throws IOException {
      int dataSize = data.remaining();

      if (dataSize > 0) {
        // Determine the block file to use.
        int fileIndex = XAUtils.log2(
            (dataSize - 1) >> (LOG2_MIN_BLOCK_SIZE - 1)
        );
        ManagedBlockFile.Phase blockFilePhase = blockFilePhases[fileIndex];
        Block block = blockFilePhase.readBlock(blockId);
        block.get(0, data);
      }
      return data;
    }


    // Free the data block referenced by this AVLNode, if any.
    private void freeBlock(AVLNode avlNode) throws IOException {
      int dataSize = avlNode.getPayloadInt(IDX_DATA_SIZE_I);

      // Check if a block has been allocated.
      if (dataSize > MAX_DIRECT_DATA_BYTES) {
        // The number of SPObject data bytes in the AVLNode.
        int directDataSize = MAX_DIRECT_DATA_BYTES - Constants.SIZEOF_LONG;

        // Free the block.
        long blockId = avlNode.getPayloadLong(IDX_BLOCK_ID);

        // Determine the block file to use.
        int fileIndex = XAUtils.log2(
            (dataSize - directDataSize - 1) >> (LOG2_MIN_BLOCK_SIZE - 1)
        );
        ManagedBlockFile.Phase blockFilePhase = blockFilePhases[fileIndex];
        blockFilePhase.freeBlock(blockId);
      }
    }


    /**
     * Removes a graph node / SPObject pair from the string pool.
     *
     * @param gNode the graph node.
     * @return <code>true</code> if the pair existed and was removed.
     * @throws StringPoolException if an internal error occurs.
     */
    boolean remove(long gNode) throws StringPoolException {
      if (gNode < NodePool.MIN_NODE) throw new IllegalArgumentException("gNode < MIN_NODE");

      if (avlFilePhase.isEmpty()) return false;

      try {
        // Load the SPObject from the G2N file.
        SPObject spObject = findSPObject(gNode);
        if (spObject == null) {
          // Graph node represents a blank node.
          return false;
        }
        if (SPO2GN_CACHE_ENABLED) {
          spo2gnCache.remove(spObject);
        }

        SPObject.TypeCategory typeCategory = spObject.getTypeCategory();
        int typeId;
        int subtypeId;
        if (typeCategory == SPObject.TypeCategory.TYPED_LITERAL) {
          typeId = ((SPTypedLiteral)spObject).getTypeId();
          subtypeId = ((SPTypedLiteral)spObject).getSubtypeId();
        } else {
          typeId = SPObjectFactory.INVALID_TYPE_ID;
          subtypeId = 0;
        }
        ByteBuffer data = spObject.getData();
        SPComparator spComparator = spObject.getSPComparator();
        AVLComparator avlComparator = new SPAVLComparator(spComparator, typeCategory, typeId, subtypeId, data);

        // Find the SPObject.
        AVLNode[] findResult = avlFilePhase.find(avlComparator, null);
        if (findResult == null) {
          // The AVL tree is empty.  This shouldn't happen since this was
          // checked for earlier.
          throw new Error("Index is empty");
        }

        if (findResult.length != 1) {
          // The SPObject was not found.
          AVLFile.release(findResult);
          return false;
        }

        AVLNode avlNode = findResult[0];
        if (avlNode.getPayloadLong(IDX_GRAPH_NODE) != gNode) {
          // Check the graph node for consistency.
          throw new Error("Incorect graph node in index");
        }
        avlNode.incRefCount();
        AVLFile.release(findResult);

        // Free the data block referenced by this AVLNode, if any.
        freeBlock(avlNode);

        // Remove the AVLNode from the index.
        avlNode.remove();
        return true;
      } catch (IOException ex) {
        throw new StringPoolException("I/O Error", ex);
      }
    }


    /**
     * Finds a graph node matching a given SPObject.
     *
     * @param spObject The SPObject to search on.
     * @return The graph node. <code>Graph.NONE</code> if not found.
     * @throws StringPoolException EXCEPTION TO DO
     */
    long findGNode(SPObject spObject, NodePool nodePool) throws StringPoolException {
      if (spObject == null) throw new StringPoolException("spObject parameter is null");

      long gNode;
      Long gNodeL;
      if (SPO2GN_CACHE_ENABLED) {
        gNodeL = spo2gnCache.get(spObject);
      }
      if (SPO2GN_CACHE_ENABLED && gNodeL != null) {
        // Found the entry in the cache.
        gNode = gNodeL.longValue();
      } else {
        AVLNode[] findResult = null;
        try {
          SPObject.TypeCategory typeCategory = spObject.getTypeCategory();
          int typeId;
          int subtypeId;
          if (typeCategory == SPObject.TypeCategory.TYPED_LITERAL) {
            SPTypedLiteral sptl = (SPTypedLiteral)spObject;
            typeId = sptl.getTypeId();
            subtypeId = sptl.getSubtypeId();
          } else {
            typeId = SPObjectFactory.INVALID_TYPE_ID;
            subtypeId = 0;
          }
          ByteBuffer data = spObject.getData();
          SPComparator spComparator = spObject.getSPComparator();
          AVLComparator avlComparator = new SPAVLComparator(spComparator, typeCategory, typeId, subtypeId, data);

          // Find the SPObject.
          findResult = avlFilePhase.find(avlComparator, null);
          if (findResult != null && findResult.length == 1) {
            gNode = findResult[0].getPayloadLong(IDX_GRAPH_NODE);
            if (GN2SPO_CACHE_ENABLED) {
              //gn2spoCache.put(gNode, spObject);
            }
            if (SPO2GN_CACHE_ENABLED) {
              spo2gnCache.put(spObject, gNode);
            }
          } else {
            if (nodePool != null) {
              try {
                gNode = nodePool.newNode();
              } catch (NodePoolException ex) {
                throw new StringPoolException("Could not allocate new node", ex);
              }
              put(gNode, findResult,typeCategory, typeId, subtypeId, data);
              if (GN2SPO_CACHE_ENABLED) {
                //gn2spoCache.put(gNode, spObject);
              }
              if (SPO2GN_CACHE_ENABLED) spo2gnCache.put(spObject, gNode);
            } else {
              // Not found.
              gNode = NodePool.NONE;
            }
          }
        } catch (IOException ex) {
          throw new StringPoolException("I/O Error", ex);
        } catch (RuntimeException ex) {
          if (logger.isDebugEnabled()) {
            logger.debug("RuntimeException in findGNode(" + spObject + ")", ex);
          }
          throw ex;
        } catch (Error e) {
          if (logger.isDebugEnabled()) {
            logger.debug("Error in findGNode(" + spObject + ")", e);
          }
          throw e;
        } finally {
          if (findResult != null) {
            AVLFile.release(findResult);
          }
        }
      }

      if (logger.isDebugEnabled()) logger.debug("findGNode(" + spObject + ") = " + gNode);

      return gNode;
    }


    /**
     * Finds and returns the SPObject corresponding to <var>gNode</var>, or
     * <code>null</code> if no such graph node is in the pool.
     *
     * @param gNode A graph node to search for within the pool.
     * @return the SPObject corresponding to <var>gNode</var>, or
     * <code>null</code> if no such graph node is in the pool.
     * @throws StringPoolException if an internal error occurs.
     */
    SPObject findSPObject(long gNode) throws StringPoolException {
      if (gNode < NodePool.MIN_NODE) throw new IllegalArgumentException("gNode=" + gNode + " < MIN_NODE");

      Long gNodeL = new Long(gNode);
      SPObject spObject;
      if (GN2SPO_CACHE_ENABLED) spObject = gn2spoCache.get(gNodeL);
      if (!GN2SPO_CACHE_ENABLED || spObject == null) {
        if (gn2spoCache.isBlankNode(gNodeL)) {
          if (logger.isDebugEnabled()) logger.debug("findSPObject(" + gNode + ") = Blank node");
          return null;
        }

        // Lookup the SPObject in the index.
        try {
          // Get the type category ID.
          long offset = gNode * GN2SPO_BLOCKSIZE;
          long bOffset = offset * Constants.SIZEOF_LONG;
          int typeCategoryId = gNodeToDataFile.getByte(bOffset + IDX_TYPE_CATEGORY_B);
          if (typeCategoryId == SPObject.TypeCategory.TCID_FREE) {
            // A blank node.
            if (logger.isDebugEnabled()) logger.debug("findSPObject(" + gNode + ") = Blank node");
            if (GN2SPO_CACHE_ENABLED) gn2spoCache.putBlankNode(gNode);
            return null;
          }

          // Convert the ID to a TypeCategory object.
          SPObject.TypeCategory typeCategory = SPObject.TypeCategory.forId(typeCategoryId);

          // Get the type ID and subtype ID.
          int typeId = gNodeToDataFile.getByte(bOffset + IDX_TYPE_ID_B);
          int subtypeId = gNodeToDataFile.getByte(bOffset + IDX_SUBTYPE_ID_B);

          // Retrieve the binary representation as a ByteBuffer.
          long iOffset = offset * (Constants.SIZEOF_LONG / Constants.SIZEOF_INT);
          int dataSize = gNodeToDataFile.getInt(iOffset + IDX_DATA_SIZE_I);

          // Calculate the number of direct data bytes and get the block ID.
          int directDataSize;
          long blockId;
          if (dataSize > MAX_DIRECT_DATA_BYTES) {
            // Make room for the block ID.
            directDataSize = MAX_DIRECT_DATA_BYTES - Constants.SIZEOF_LONG;
            blockId = gNodeToDataFile.getLong(offset + IDX_BLOCK_ID);
          } else {
            directDataSize = dataSize;
            blockId = Block.INVALID_BLOCK_ID;
          }

          // Retrieve bytes from the AVLNode.
          ByteBuffer data = ByteBuffer.allocate(dataSize);
          data.limit(directDataSize);
          XAStringPoolImpl.get(gNodeToDataFile, bOffset + IDX_DATA * Constants.SIZEOF_LONG, data);

          // Retrieve the remaining bytes if any.
          if (dataSize > MAX_DIRECT_DATA_BYTES) {
            data.limit(dataSize);
            retrieveRemainingBytes(data, blockId);
          }
          data.rewind();

          // Construct the SPObject and return it.
          spObject = SPO_FACTORY.newSPObject(typeCategory, typeId, subtypeId, data);

          if (GN2SPO_CACHE_ENABLED) gn2spoCache.put(gNode, spObject);
          if (SPO2GN_CACHE_ENABLED) {
            //spo2gnCache.put(spObject, gNode);
          }
        } catch (IOException ex) {
          if (logger.isDebugEnabled()) logger.debug("IOException in findSPObject(" + gNode + ")", ex);
          throw new StringPoolException("I/O Error", ex);
        } catch (RuntimeException ex) {
          if (logger.isDebugEnabled()) logger.debug("RuntimeException in findSPObject(" + gNode + ")", ex);
          throw ex;
        } catch (Error e) {
          if (logger.isDebugEnabled()) logger.debug("Error in findSPObject(" + gNode + ")", e);
          throw e;
        }
      }

      if (logger.isDebugEnabled()) logger.debug("findSPObject(" + gNode + ") = " + spObject);

      return spObject;
    }


    Tuples findGNodes(
        SPObject lowValue, boolean inclLowValue,
        SPObject highValue, boolean inclHighValue
    ) throws StringPoolException {
      SPObject.TypeCategory typeCategory;
      int typeId;
      int subtypeId;
      AVLNode lowAVLNode;
      long highAVLNodeId;

      if (lowValue == null && highValue == null) {
        // Return all nodes in the index.
        typeCategory = null;
        typeId = SPObjectFactory.INVALID_TYPE_ID;
        subtypeId = 0;
        lowAVLNode = avlFilePhase.getRootNode();
        if (lowAVLNode != null) lowAVLNode = lowAVLNode.getMinNode_R();
        highAVLNodeId = Block.INVALID_BLOCK_ID;
      } else {
        // Get the type category.
        SPObject typeValue = lowValue != null ? lowValue : highValue;
        typeCategory = typeValue.getTypeCategory();
        if (typeCategory == SPObject.TypeCategory.TYPED_LITERAL) {
          typeId = ((SPTypedLiteral)typeValue).getTypeId();
          subtypeId = ((SPTypedLiteral)typeValue).getSubtypeId();
        } else {
          typeId = SPObjectFactory.INVALID_TYPE_ID;
          subtypeId = 0;
        }

        // Check that the two SPObjects are of the same type.
        if (lowValue != null && highValue != null) {
          if (
            typeCategory != highValue.getTypeCategory() || (
                typeCategory == SPObject.TypeCategory.TYPED_LITERAL &&
                ((SPTypedLiteral)lowValue).getTypeId() !=
                    ((SPTypedLiteral)highValue).getTypeId()
            )
          ) {
            // Type mismatch.
            throw new StringPoolException("lowValue and highValue are not of the same type");
          }

          if (lowValue != null && highValue != null) {
            // Check for lowValue being higher than highValue.
            // Also check for lowValue being equal to highValue but excluded
            // by either inclLowValue or inclHighValue being false.
            int c = lowValue.compareTo(highValue);
            if (c > 0 || c == 0 && (!inclLowValue || !inclHighValue)) {
              return new GNodeTuplesImpl(
                  null, SPObjectFactory.INVALID_TYPE_ID,
                  null, null, null, Block.INVALID_BLOCK_ID
              );
            }
          }
        }

        // Compute the comparator for lowValue.
        AVLComparator lowComparator;
        if (lowValue != null) {
          ByteBuffer data = lowValue.getData();
          SPComparator spComparator = lowValue.getSPComparator();
          lowComparator = new SPAVLComparator(spComparator, typeCategory, typeId, subtypeId, data);
        } else {
          // Select the first node with the current type.
          if (typeCategory == SPObject.TypeCategory.TYPED_LITERAL) {
            lowComparator = new SPCategoryTypeAVLComparator(typeCategory.ID, typeId);
          } else {
            lowComparator = new SPCategoryAVLComparator(typeCategory.ID);
          }
        }

        // Compute the comparator for highValue.
        AVLComparator highComparator;
        if (highValue != null) {
          ByteBuffer data = highValue.getData();
          SPComparator spComparator = highValue.getSPComparator();
          highComparator = new SPAVLComparator(spComparator, typeCategory, typeId, subtypeId, data);
        } else {
          // Select the first node past the last one that has the current type.
          if (typeCategory == SPObject.TypeCategory.TYPED_LITERAL) {
            highComparator = new SPCategoryTypeAVLComparator(typeCategory.ID, typeId + 1);
          } else {
            highComparator = new SPCategoryAVLComparator(typeCategory.ID + 1);
          }
        }

        AVLNode[] findResult = avlFilePhase.find(lowComparator, null);
        if (findResult == null) {
          // Empty store.
          lowAVLNode = null;
          highAVLNodeId = Block.INVALID_BLOCK_ID;
        } else {
          if (findResult.length == 1) {
            // Found the node exactly.
            lowAVLNode = findResult[0];
            // Handle inclLowValue.
            if (!inclLowValue) {
              lowAVLNode = lowAVLNode.getNextNode_R();

              // The lowValue passed to the GNodeTuplesImpl constructor
              // is always inclusive but inclLowValue is false.
              // Recalculate lowValue.
              if (lowAVLNode != null) lowValue = loadSPObject(typeCategory, typeId, lowAVLNode);
            }
          } else {
            // Did not find the node but found the location where the node
            // would be if it existed.
            if (findResult[0] != null) findResult[0].release();
            lowAVLNode = findResult[1];
          }

          if (lowAVLNode != null) {
            // Find the high node.

            findResult = avlFilePhase.find(highComparator, null);
            if (findResult.length == 1) {
              // Found the node exactly.
              AVLNode highAVLNode = findResult[0];
              // Handle inclHighValue.
              if (inclHighValue) {
                // Step past this node so that it is included in the range.
                highAVLNode = highAVLNode.getNextNode();
                if (highAVLNode != null) {
                  highAVLNodeId = highAVLNode.getId();

                  // The highValue passed to the GNodeTuplesImpl constructor
                  // is always exclusive but inclHighValue is true.
                  // Recalculate highValue.
                  highValue = loadSPObject(typeCategory, typeId, highAVLNode);

                  highAVLNode.release();
                } else {
                  highAVLNodeId = Block.INVALID_BLOCK_ID;
                  highValue = null;
                }
              } else {
                highAVLNodeId = highAVLNode.getId();
              }
            } else {
              // Did not find the node but found the location where the node
              // would be if it existed.
              highAVLNodeId = findResult[1] != null ? findResult[1].getId() : Block.INVALID_BLOCK_ID;
            }

            AVLFile.release(findResult);
          } else {
            highAVLNodeId = Block.INVALID_BLOCK_ID;
          }
        }
      }

      return new GNodeTuplesImpl(typeCategory, typeId,lowValue, highValue, lowAVLNode, highAVLNodeId);
    }


    Tuples findGNodes(SPObject.TypeCategory typeCategory, URI typeURI) throws StringPoolException {
      int typeId;
      AVLNode lowAVLNode;
      long highAVLNodeId;

      if (typeCategory != null) {
        // Convert the type URI to a type ID.
        if (typeURI != null) {
          try {
            typeId = SPO_FACTORY.getTypeId(typeURI);
          } catch (IllegalArgumentException ex) {
            throw new StringPoolException("Unsupported XSD type: " + typeURI, ex);
          }
        } else {
          typeId = SPObjectFactory.INVALID_TYPE_ID;
        }

        AVLComparator lowComparator;
        AVLComparator highComparator;

        if (
            typeCategory == SPObject.TypeCategory.TYPED_LITERAL &&
            typeId != SPObjectFactory.INVALID_TYPE_ID
        ) {
          // Return nodes of the specified category and type node.
          lowComparator = new SPCategoryTypeAVLComparator(typeCategory.ID, typeId);
          highComparator = new SPCategoryTypeAVLComparator(typeCategory.ID, typeId + 1);
        } else {
          // Return nodes of the specified category.
          lowComparator = new SPCategoryAVLComparator(typeCategory.ID);
          highComparator = new SPCategoryAVLComparator(typeCategory.ID + 1);
        }

        AVLNode[] findResult = avlFilePhase.find(lowComparator, null);
        if (findResult == null) {
          // Empty store.
          lowAVLNode = null;
          highAVLNodeId = Block.INVALID_BLOCK_ID;
        } else {
          assert findResult.length == 2;
          lowAVLNode = findResult[1];
          if (findResult[0] != null) {
            findResult[0].release();
          }

          if (lowAVLNode != null) {
            // Find the high node.
            findResult = avlFilePhase.find(highComparator, null);
            assert findResult.length == 2;
            highAVLNodeId = findResult[1] != null ? findResult[1].getId() : Block.INVALID_BLOCK_ID;
            AVLFile.release(findResult);
          } else {
            highAVLNodeId = Block.INVALID_BLOCK_ID;
          }
        }
      } else {
        if (typeURI != null) {
          throw new StringPoolException("typeCategory is null and typeURI is not null");
        }
        typeId = SPObjectFactory.INVALID_TYPE_ID;

        // Return all nodes in the index.
        lowAVLNode = avlFilePhase.getRootNode();
        if (lowAVLNode != null) lowAVLNode = lowAVLNode.getMinNode_R();
        highAVLNodeId = Block.INVALID_BLOCK_ID;
      }

      return new GNodeTuplesImpl(typeCategory, typeId, null, null, lowAVLNode, highAVLNodeId);
    }


    // Returns the SPObject referenced by the avlNode or null if the SPObject
    // is not of the specified type.
    private SPObject loadSPObject(
        SPObject.TypeCategory typeCategory, int typeId, AVLNode avlNode
    ) throws StringPoolException {
      try {
        // Get the type category ID.
        int typeCategoryId = avlNode.getPayloadByte(IDX_TYPE_CATEGORY_B);
        if (
            typeCategoryId == SPObject.TypeCategory.TCID_FREE || // blank node
            // type mismatch
            typeCategoryId != typeCategory.ID || (
                typeCategory == SPObject.TypeCategory.TYPED_LITERAL &&
                typeId != avlNode.getPayloadByte(IDX_TYPE_ID_B)
            )
        ) {
          return null;
        }

        // Retrieve the subtype ID.
        int subtypeId = avlNode.getPayloadByte(IDX_SUBTYPE_ID_B);

        // Retrieve the binary representation as a ByteBuffer.
        int dataSize = avlNode.getPayloadInt(IDX_DATA_SIZE_I);

        // Calculate the number of direct data bytes and get the block ID.
        int directDataSize;
        long blockId;
        if (dataSize > MAX_DIRECT_DATA_BYTES) {
          // Make room for the block ID.
          directDataSize = MAX_DIRECT_DATA_BYTES - Constants.SIZEOF_LONG;
          blockId = avlNode.getPayloadLong(IDX_BLOCK_ID);
        } else {
          directDataSize = dataSize;
          blockId = Block.INVALID_BLOCK_ID;
        }

        // Retrieve bytes from the AVLNode.
        ByteBuffer data = avlNode.getBlock().getSlice((AVLNode.HEADER_SIZE + IDX_DATA) * Constants.SIZEOF_LONG, directDataSize);

        // Retrieve the remaining bytes if any.
        if (dataSize > MAX_DIRECT_DATA_BYTES) {
          // need a bigger buffer
          ByteBuffer newData = ByteBuffer.allocate(dataSize);
          newData.put(data);
          data = newData;
          retrieveRemainingBytes(data, blockId);
        }
        data.rewind();

        // Construct the SPObject and return it.
        SPObject spObject = SPO_FACTORY.newSPObject(typeCategory, typeId, subtypeId, data);

        if (logger.isDebugEnabled()) logger.debug("loadSPObject() = " + spObject);

        return spObject;
      } catch (IOException ex) {
        if (logger.isDebugEnabled()) logger.debug("IOException in loadSPObject()", ex);
        throw new StringPoolException("I/O Error", ex);
      } catch (RuntimeException ex) {
        if (logger.isDebugEnabled()) logger.debug("RuntimeException in loadSPObject()", ex);
        throw ex;
      } catch (Error e) {
        if (logger.isDebugEnabled()) logger.debug("Error in loadSPObject()", e);
        throw e;
      }
    }


    /**
     * Checks the integrity of this phase by iterating over all the nodes in
     * the index and checking for consistency and returns the number of
     * nodes in the index.
     */
    long checkIntegrity() {
      AVLNode node = avlFilePhase.getRootNode();
      if (node == null) return 0;

      //logger.warn("StringPool tree: " + node);
      node = node.getMinNode_R();

      long nodeIndex = 0;
      int prevTypeCategoryId = 0;
      int prevTypeId = SPObjectFactory.INVALID_TYPE_ID;

      do {
        int typeCategoryId = node.getPayloadByte(IDX_TYPE_CATEGORY_B);
        if (typeCategoryId == SPObject.TypeCategory.TCID_FREE) {
          throw new AssertionError("Found free node");
        }

        if (typeCategoryId < prevTypeCategoryId) {
          throw new AssertionError(
              "Type categories out of order: \"" +
              SPObject.TypeCategory.forId(typeCategoryId) + "\" comes after \"" +
              SPObject.TypeCategory.forId(prevTypeCategoryId) +
              "\" at node index: " + nodeIndex
          );
        }

        int typeId = node.getPayloadByte(IDX_TYPE_ID_B);
        if (typeCategoryId == prevTypeCategoryId && typeId < prevTypeId) {
          throw new AssertionError(
              "Type nodes out of order: \"" + typeId + "\" comes after \"" +
              prevTypeId + "\" at node index: " + nodeIndex
          );
        }
        int subtypeId = node.getPayloadByte(IDX_SUBTYPE_ID_B);

        int dataSize = node.getPayloadInt(IDX_DATA_SIZE_I);
        long blockId = node.getPayloadLong(IDX_BLOCK_ID);
        long graphNode = node.getPayloadLong(IDX_GRAPH_NODE);

        // Verify that the record in the G2N file is consistent.
        long offset = graphNode * GN2SPO_BLOCKSIZE;
        long iOffset = offset * (Constants.SIZEOF_LONG / Constants.SIZEOF_INT);
        long bOffset = offset * Constants.SIZEOF_LONG;

        int gn2spoTypeCategoryId = gNodeToDataFile.getByte(bOffset + IDX_TYPE_CATEGORY_B);
        if (gn2spoTypeCategoryId != typeCategoryId) {
          throw new AssertionError(
              "Type category mismatch.  gNode:" + graphNode +
              " AVL:" + typeCategoryId +
              " GN2SPO:" + gn2spoTypeCategoryId + " at node index: " + nodeIndex
          );
        }

        int gn2spoTypeId = gNodeToDataFile.getByte(bOffset + IDX_TYPE_ID_B);
        if (gn2spoTypeId != typeId) {
          throw new AssertionError(
              "Type ID mismatch.  gNode:" + graphNode +
              " AVL:" + typeId +
              " GN2SPO:" + gn2spoTypeId + " at node index: " + nodeIndex
          );
        }

        int gn2spoSubtypeId = gNodeToDataFile.getByte(bOffset + IDX_SUBTYPE_ID_B);
        if (gn2spoSubtypeId != subtypeId) {
          throw new AssertionError(
              "Subtype ID mismatch.  gNode:" + graphNode +
              " AVL:" + subtypeId +
              " GN2SPO:" + gn2spoSubtypeId + " at node index: " + nodeIndex
          );
        }

        int gn2spoDataSize = gNodeToDataFile.getInt(iOffset + IDX_DATA_SIZE_I);
        if (gn2spoDataSize != dataSize) {
          throw new AssertionError(
              "Data size mismatch.  gNode:" + graphNode +
              " AVL:" + dataSize +
              " GN2SPO:" + gn2spoDataSize + " at node index: " + nodeIndex
          );
        }

        if (dataSize > MAX_DIRECT_DATA_BYTES) {
          long gn2spoBlockId = gNodeToDataFile.getLong(offset + IDX_BLOCK_ID);
          if (gn2spoBlockId != blockId) {
            throw new AssertionError(
                "Block ID mismatch.  gNode:" + graphNode +
                " AVL:" + blockId +
                " GN2SPO:" + gn2spoBlockId + " at node index: " + nodeIndex
            );
          }
        }

        prevTypeCategoryId = typeCategoryId;
        prevTypeId = typeId;

        ++nodeIndex;
      } while ((node = node.getNextNode_R()) != null);

      return nodeIndex;
    }


    Token use() {
      return new Token();
    }


    final class GNodeTuplesImpl implements Tuples {

      // Defines the constraining type.
      @SuppressWarnings("unused")
      private SPObject.TypeCategory typeCategory;

      // Defines the constraining type.
      @SuppressWarnings("unused")
      private int typeId;

      // The low value of the range (inclusive) or null to indicate the lowest
      // possible value within the type defined by the typeCategory and
      // typeId fields.
      private SPObject lowValue;

      // The high value of the range (exclusive) or null to indicate the
      // highest possible value within the type defined by the typeCategory and
      // typeId fields.
      private SPObject highValue;

      // The first index node in the range (inclusive) or null to indicate
      // an empty Tuples.
      private AVLNode lowAVLNode;

      // The last index node in the range (exclusive) or Block.INVALID_BLOCK_ID
      // to indicate all nodes following lowAVLNode in the index.
      private long highAVLNodeId;

      // The current node.
      private AVLNode avlNode = null;

      private Token token;

      // The number of nodes.
      private long nrGNodes;

      // This is set to true once the number of nodes is known.
      private boolean nrGNodesValid = false;

      private boolean beforeFirst = false;

      private long[] prefix = null;

      private boolean onPrefixNode = false;

      private Variable[] variables = (Variable[])VARIABLES.clone();

      /**
       * Constructs a GNodeTuplesImpl that represents nodes in the AVLFile
       * index that range from lowAVLNode up to but not including the node with
       * ID highAVLNodeId.
       *
       * @param lowAVLNode the AVLNode that has the first graph node that is
       * included in the Tuples.
       * @param highAVLNodeId the ID of the AVLNode that has the first graph
       * node that is not included in the Tuples.
       */
      GNodeTuplesImpl(
          SPObject.TypeCategory typeCategory, int typeId,
          SPObject lowValue, SPObject highValue,
          AVLNode lowAVLNode, long highAVLNodeId
      ) {
        if (lowAVLNode != null && lowAVLNode.getId() == highAVLNodeId) {
          // Low and High are equal - Empty.
          lowAVLNode.release();
          lowAVLNode = null;
          highAVLNodeId = Block.INVALID_BLOCK_ID;
        }

        if (lowAVLNode == null) {
          // Empty tuples.
          typeCategory = null;
          lowValue = null;
          highValue = null;
          if (highAVLNodeId != Block.INVALID_BLOCK_ID) {
            if (logger.isDebugEnabled()) {
              logger.debug("lowAVLNode is null but highAVLNodeId is not " +Block.INVALID_BLOCK_ID);
            }
            highAVLNodeId = Block.INVALID_BLOCK_ID;
          }
          nrGNodes = 0;
          nrGNodesValid = true;
        } else {
          token = use();
        }

        if (typeCategory != SPObject.TypeCategory.TYPED_LITERAL) {
          typeId = SPObjectFactory.INVALID_TYPE_ID;
        }

        this.typeCategory = typeCategory;
        this.typeId = typeId;
        this.lowValue = lowValue;
        this.highValue = highValue;
        this.lowAVLNode = lowAVLNode;
        this.highAVLNodeId = highAVLNodeId;
      }

      public long getColumnValue(int column) throws TuplesException {
        if (column != 0) throw new TuplesException("Column index out of range: " + column);

        // Handle the prefix.
        if (onPrefixNode) return prefix[0];

        if (avlNode == null) throw new TuplesException("No current row");
        return avlNode.getPayloadLong(IDX_GRAPH_NODE);
      }

      public long getRawColumnValue(int column) throws TuplesException {
        return getColumnValue(column);
      }

      public Variable[] getVariables() {
        // Clone the variables array in case the caller changes the returned
        // array.
        return (Variable[])variables.clone();
      }

      public int getNumberOfVariables() {
        return 1;
      }

      public long getRowCount() throws TuplesException {
        if (!nrGNodesValid) {
          assert lowAVLNode != null;
          AVLNode n = lowAVLNode;
          n.incRefCount();
          long count = 0;
          while (n != null && (highAVLNodeId == Block.INVALID_BLOCK_ID || n.getId() != highAVLNodeId)) {
            ++count;
            n = n.getNextNode_R();
          }

          if (n != null) n.release();

          nrGNodes = count;
          nrGNodesValid = true;
        }
        return nrGNodes;
      }

      public long getRowUpperBound() throws TuplesException {
        return getRowCount();
      }

      public long getRowExpectedCount() throws TuplesException {
        return getRowCount();
      }

      /**
       * Return the cardinality of the tuples.
       *
       * @return <code>Cursor.ZERO</code> if the size of this tuples is 0,
       *         <code>Cursor.ONE</code> if the size is 1,
       *         <code>Cursor.MANY</code> if the size of this tuples is 2 or more.
       * @throws TuplesException If there is an error accessing the underlying data.
       */
      public int getRowCardinality() throws TuplesException {
        // TODO This return value may be cached
        long count = 0;
        if (nrGNodesValid) {
          count = nrGNodes;
        } else {
          assert lowAVLNode != null;
          AVLNode n = lowAVLNode;
          n.incRefCount();
          while (count < 2 && n != null && (highAVLNodeId == Block.INVALID_BLOCK_ID || n.getId() != highAVLNodeId)) {
            ++count;
            n = n.getNextNode_R();
          }

          if (n != null) n.release();
        }
        return count == 0 ? Cursor.ZERO :
               count == 1 ? Cursor.ONE :
                            Cursor.MANY;
      }

      /* (non-Javadoc)
       * @see org.mulgara.query.Cursor#isEmpty()
       */
      public boolean isEmpty() throws TuplesException {
        return lowAVLNode == null;
      }

      public int getColumnIndex(Variable variable) throws TuplesException {
        if (variable == null) throw new IllegalArgumentException("variable is null");

        if (variable.equals(variables[0])) {
          // The variable matches the one and only column.
          return 0;
        }

        throw new TuplesException("variable doesn't match any column: " + variable);
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

      public boolean hasNoDuplicates() {
        return true;
      }

      public RowComparator getComparator() {
        // Unsorted.
        return null;
      }

      public java.util.List<Tuples> getOperands() {
        return java.util.Collections.emptyList();
      }

      public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
        assert prefix != null;
        if (prefix.length > 1) throw new TuplesException("prefix.length (" + prefix.length + ") > nrColumns (1)");
        if (suffixTruncation != 0) throw new TuplesException("suffixTruncation not supported");

        beforeFirst = true;
        onPrefixNode = false;
        this.prefix = prefix;
        if (avlNode != null) {
          avlNode.release();
          avlNode = null;
        }
      }

      public void beforeFirst() throws TuplesException {
        beforeFirst(Tuples.NO_PREFIX, 0);
      }

      public boolean next() throws TuplesException {
        if (beforeFirst) {
          assert prefix != null;
          assert avlNode == null;
          assert !onPrefixNode;
          beforeFirst = false;

          if (prefix.length == 1) {
            // Handle the prefix.
            if (lowAVLNode == null) {
              // There are no nodes, so this Tuples can't contain the prefix
              // node.
              return false;
            }

            SPObject spObject;
            try {
              // FIXME check the type category and type node.
              spObject = findSPObject(prefix[0]);
            } catch (StringPoolException ex) {
              throw new TuplesException("Exception while loading SPObject", ex);
            }

            // Check that the SPObject is within range.
            onPrefixNode = (
              spObject != null &&
              (lowValue == null || spObject.compareTo(lowValue) >= 0) &&
              (highValue == null || spObject.compareTo(highValue) < 0)
            );
            return onPrefixNode;
          }

          if (lowAVLNode != null) {
            lowAVLNode.incRefCount();
            avlNode = lowAVLNode;
          }
        } else if (avlNode != null) {
          avlNode = avlNode.getNextNode_R();
          if (avlNode != null) {
            // Check if this is the highNode.
            if (
                highAVLNodeId != Block.INVALID_BLOCK_ID &&
                avlNode.getId() == highAVLNodeId
            ) {
              avlNode.release();
              avlNode = null;
            }
          }
        }
        onPrefixNode = false;
        return avlNode != null;
      }

      public void close() throws TuplesException {
        if (lowAVLNode != null) {
          if (avlNode != null) {
            avlNode.release();
            avlNode = null;
          }
          lowAVLNode.release();
          lowAVLNode = null;
          token.release();
          token = null;
        }
      }

      public void renameVariables(Constraint constraint) {
        variables[0] = (Variable)constraint.getElement(0);
      }

      public Object clone() {
        try {
          GNodeTuplesImpl t = (GNodeTuplesImpl)super.clone();
          t.variables = (Variable[])variables.clone();
          if (t.lowAVLNode != null) {
            t.lowAVLNode.incRefCount();
            t.token = use(); // Allocate a new token.
            if (t.avlNode != null) {
              t.avlNode.incRefCount();
            }
          }
          return t;
        } catch (CloneNotSupportedException e) {
          throw new Error(getClass() + " doesn't support clone, which it must", e);
        }
      }
      
      public int hashCode() {
        return 0; // Here to quiet the Fortify scan; these will never be put in a hashtable.
      }

      public boolean equals(Object o) {
        boolean isEqual = false;

        // Make sure it's not null
        if (o != null) {
          try {
            // Try and cast the passed object - if not then they aren't equal.
            Tuples testTuples = (Tuples) o;

            // Ensure that the row count is the same
            if (getRowCount() == testTuples.getRowCount()) {
              // Ensure that the variable lists are equal
              if (java.util.Arrays.asList(getVariables()).equals(
                  java.util.Arrays.asList(testTuples.getVariables()))) {
                // Clone tuples to be compared
                Tuples t1 = (Tuples) clone();
                Tuples t2 = (Tuples) testTuples.clone();

                try {
                  // Put them at the start.
                  t1.beforeFirst();
                  t2.beforeFirst();

                  boolean finished = false;
                  boolean tuplesEqual = true;

                  // Repeat until there are no more rows or we find an unequal row.
                  while (!finished) {
                    // Assume that if t1 has next so does t2.
                    finished = !t1.next();
                    t2.next();

                    // If we're not finished compare the row.
                    if (!finished) {
                      // Check if the elements in both rows are equal.
                      for (int variableIndex = 0;
                          variableIndex < t1.getNumberOfVariables();
                          variableIndex++) {
                        // If they're not equal quit the loop and set tuplesEqual to
                        // false.
                        if (t1.getColumnValue(variableIndex) !=
                            t2.getColumnValue(variableIndex)) {
                          tuplesEqual = false;
                          finished = true;
                        }
                      }
                    }
                  }

                  isEqual = tuplesEqual;
                } finally {
                  t1.close();
                  t2.close();
                }
              }
            }
          } catch (ClassCastException cce) {
            // Not of the correct type return false.
          } catch (TuplesException ex) {
            throw new RuntimeException(ex.toString(), ex);
          }
        }

        return isEqual;
      }

      public String toString() {
        return SimpleTuplesFormat.format(this);
      }

      /**
       * Copied from AbstractTuples
       */
      public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
        return null;
      }
    }


    final class SPCategoryTypeAVLComparator implements AVLComparator {

      private final int typeCategoryId;
      private final int typeId;

      SPCategoryTypeAVLComparator(int typeCategoryId, int typeId) {
        this.typeCategoryId = typeCategoryId;
        this.typeId = typeId;
        assert typeCategoryId == SPObject.TypeCategory.TCID_TYPED_LITERAL;
      }

      public int compare(long[] key, AVLNode avlNode) {
        // NOTE: ignore key.

        // First, order by type category ID.
        int nodeTypeCategoryId = avlNode.getPayloadByte(IDX_TYPE_CATEGORY_B);
        int c = typeCategoryId - nodeTypeCategoryId;
        if (c != 0) return c;

        // Second, order by type node.
        int nodeTypeId = avlNode.getPayloadByte(IDX_TYPE_ID_B);
        return typeId <= nodeTypeId ? -1 : 1;
      }

    }


    final class SPCategoryAVLComparator implements AVLComparator {

      private final int typeCategoryId;

      SPCategoryAVLComparator(int typeCategoryId) {
        this.typeCategoryId = typeCategoryId;
      }

      public int compare(long[] key, AVLNode avlNode) {
        // NOTE: ignore key.

        // First, order by type category ID.
        int nodeTypeCategoryId = avlNode.getPayloadByte(IDX_TYPE_CATEGORY_B);
        return typeCategoryId <= nodeTypeCategoryId ? -1 : 1;
      }

    }


    final class SPAVLComparator implements AVLComparator {

      private final SPComparator spComparator;
      private final SPObject.TypeCategory typeCategory;
      private final int typeId;
      private final int subtypeId;
      private final ByteBuffer data;

      SPAVLComparator(SPComparator spComparator, SPObject.TypeCategory typeCategory, int typeId, int subtypeId, ByteBuffer data) {
        this.spComparator = spComparator;
        this.typeCategory = typeCategory;
        this.typeId = typeId;
        this.subtypeId = subtypeId;
        this.data = data;
      }

      public int compare(long[] key, AVLNode avlNode) {
        // NOTE: ignore key.

        // First, order by type category ID.
        int nodeTypeCategoryId = avlNode.getPayloadByte(IDX_TYPE_CATEGORY_B);
        int c = typeCategory.ID - nodeTypeCategoryId;
        if (c != 0) return c;

        // Second, order by type node.
        int nodeTypeId = avlNode.getPayloadByte(IDX_TYPE_ID_B);
        if (typeId != nodeTypeId) return typeId < nodeTypeId ? -1 : 1;

        int nodeSubtypeId = avlNode.getPayloadByte(IDX_SUBTYPE_ID_B);

        // Finally, defer to the SPComparator.
        int dataSize = avlNode.getPayloadInt(IDX_DATA_SIZE_I);

        // Calculate the number of direct data bytes and get the block ID.
        int directDataSize;
        long blockId;
        if (dataSize > MAX_DIRECT_DATA_BYTES) {
          // Make room for the block ID.
          directDataSize = MAX_DIRECT_DATA_BYTES - Constants.SIZEOF_LONG;
          blockId = avlNode.getPayloadLong(IDX_BLOCK_ID);
        } else {
          directDataSize = dataSize;
          blockId = Block.INVALID_BLOCK_ID;
        }

        // Retrieve the binary representation as a ByteBuffer.
        ByteBuffer nodeData = ByteBuffer.allocate(dataSize);

        // Retrieve bytes from the AVLNode.
        nodeData.limit(directDataSize);
        avlNode.getBlock().get((AVLNode.HEADER_SIZE + IDX_DATA) * Constants.SIZEOF_LONG, nodeData);

        if (dataSize > MAX_DIRECT_DATA_BYTES) {
          // Save the limit of data so it can be restored later in case it is
          // made smaller by the comparePrefix method of the spComparator.
          int savedDataLimit = data.limit();

          data.rewind();
          nodeData.rewind();
          c = spComparator.comparePrefix(data, nodeData, dataSize);
          if (c != 0) return c;

          data.limit(savedDataLimit);

          try {
            // Retrieve the remaining bytes if any.
            // Set the limit before the position in case the limit was made
            // smaller by the comparePrefix method.
            nodeData.limit(dataSize);
            nodeData.position(directDataSize);
            retrieveRemainingBytes(nodeData, blockId);
          } catch (IOException ex) {
            throw new Error("I/O Error while retrieving SPObject data", ex);
          }
        }

        data.rewind();
        nodeData.rewind();
        return spComparator.compare(data, subtypeId, nodeData, nodeSubtypeId);
      }

    }


    final class Token {

      private AVLFile.Phase.Token avlFileToken;

      private ManagedBlockFile.Phase.Token[] blockFileTokens = new ManagedBlockFile.Phase.Token[NR_BLOCK_FILES];


      /**
       * CONSTRUCTOR Token TO DO
       */
      Token() {
        avlFileToken = avlFilePhase.use();
        for (int i = 0; i < NR_BLOCK_FILES; ++i)  blockFileTokens[i] = blockFilePhases[i].use();
      }


      public Phase getPhase() {
        assert avlFileToken != null : "Invalid Token";
        assert blockFileTokens != null : "Invalid Token";
        return Phase.this;
      }


      public void release() {
        assert avlFileToken != null : "Invalid Token";
        assert blockFileTokens != null : "Invalid Token";

        avlFileToken.release();
        avlFileToken = null;

        for (int i = 0; i < NR_BLOCK_FILES; ++i) {
          blockFileTokens[i].release();
        }
        blockFileTokens = null;
      }

    }

  }


  static final class SPO2GNCache {
    private static final int DEFAULT_MAX_SIZE = 1000;
    private static final int MAX_SIZE;
    private Reference<Cache<SPObject,Long>> cacheRef;

    static {
      String cacheSizeProp = System.getProperty("mulgara.sp.localizeCacheSize");
      if (cacheSizeProp == null) cacheSizeProp = System.getProperty("mulgara.sp.cacheSize");
      if (cacheSizeProp != null) {
        MAX_SIZE = Integer.parseInt(cacheSizeProp);
        if (MAX_SIZE < 1) throw new ExceptionInInitializerError("bad mulgara.sp.cacheSize property: " + cacheSizeProp);
      } else {
        MAX_SIZE = DEFAULT_MAX_SIZE;
      }
    }

    public SPO2GNCache() {
      cacheRef = new SoftReference<Cache<SPObject,Long>>(new Cache<SPObject,Long>(MAX_SIZE));
    }

    private Cache<SPObject,Long> getCache() {
      Cache<SPObject,Long> cache = cacheRef.get();
      if (cache == null) {
        cache = new Cache<SPObject,Long>(MAX_SIZE);
        cacheRef = new SoftReference<Cache<SPObject,Long>>(cache);
      }
      return cache;
    }

    public void put(SPObject spObject, long gNode) {
      assert gNode >= NodePool.MIN_NODE;
      put(spObject, new Long(gNode));
    }

    public synchronized void put(SPObject spObject, Long gNodeL) {
      assert gNodeL != null;
      assert spObject != null;

      Long old = getCache().put(spObject, gNodeL);
      assert old == null || old.equals(gNodeL);
    }

    public synchronized void remove(SPObject spObject) {
      assert spObject != null;
      getCache().remove(spObject);
    }

    public synchronized Long get(SPObject spObject) {
      assert spObject != null;
      return getCache().get(spObject);
    }

  }


  static final class GN2SPOCache {
    private static final int DEFAULT_MAX_SIZE = 1000;
    private static final int MAX_SIZE;
    private Reference<Cache<Long,SPObject>> cacheRef;

    static {
      String cacheSizeProp = System.getProperty("mulgara.sp.globalizeCacheSize");
      if (cacheSizeProp == null) cacheSizeProp = System.getProperty("mulgara.sp.cacheSize");
      if (cacheSizeProp != null) {
        MAX_SIZE = Integer.parseInt(cacheSizeProp);
        if (MAX_SIZE < 1) throw new ExceptionInInitializerError("bad mulgara.sp.cacheSize property: " + cacheSizeProp);
      } else {
        MAX_SIZE = DEFAULT_MAX_SIZE;
      }
    }

    public GN2SPOCache() {
      cacheRef = new SoftReference<Cache<Long,SPObject>>(new Cache<Long,SPObject>(MAX_SIZE));
    }

    private Cache<Long,SPObject> getCache() {
      Cache<Long,SPObject> cache = cacheRef.get();
      if (cache == null) {
        cache = new Cache<Long,SPObject>(MAX_SIZE);
        cacheRef = new SoftReference<Cache<Long,SPObject>>(cache);
      }
      return cache;
    }

    public void put(long gNode, SPObject spObject) {
      assert gNode >= NodePool.MIN_NODE;
      put(new Long(gNode), spObject);
    }

    public synchronized void put(Long gNodeL, SPObject spObject) {
      assert gNodeL != null;
      assert spObject != null;

      SPObject old = getCache().put(gNodeL, spObject);
      assert old == null || old.equals(spObject);
    }

    public void remove(long gNode) {
      assert gNode >= NodePool.MIN_NODE;
      remove(new Long(gNode));
    }

    public synchronized void remove(Long gNodeL) {
      assert gNodeL != null;
      getCache().remove(gNodeL);
    }

    public void putBlankNode(long gNode) {
      assert gNode >= NodePool.MIN_NODE;
      putBlankNode(new Long(gNode));
    }

    public synchronized void putBlankNode(Long gNodeL) {
      assert gNodeL != null;
      SPObject old = getCache().put(gNodeL, null);
      assert old == null;
    }

    public SPObject get(long gNode) {
      return get(new Long(gNode));
    }

    public synchronized SPObject get(Long gNodeL) {
      assert gNodeL != null;
      return getCache().get(gNodeL);
    }

    public boolean isBlankNode(long gNode) {
      return isBlankNode(new Long(gNode));
    }

    public synchronized boolean isBlankNode(Long gNodeL) {
      assert gNodeL != null;
      Cache<Long,SPObject> cache = getCache();
      return cache.containsKey(gNodeL) && cache.get(gNodeL) == null;
    }

  }


  /**
   * A LRU cache.
   */
  @SuppressWarnings("serial")
  static final class Cache<K,V> extends LinkedHashMap<K,V> {

    /** The load factor on the internal hash table. */
    public static final float LOAD_FACTOR = 0.75F;

    /** The largest number of elements to store in this cache. */
    final int MAX_SIZE;

    /**
     * Constucts a new cache.
     *
     * @param maxSize largest number of elements to store in the cache.
     */
    public Cache(int maxSize) {
      super((int)Math.ceil(maxSize / LOAD_FACTOR + 1), LOAD_FACTOR, true);
      MAX_SIZE = maxSize;
    }

    /**
     * Used by {@link #put} to reduce memory consumption.
     *
     * @param eldest The eldest entry in the map.  Ignored.
     * @return <code>true</code> when the cache is overfull and data should be removed.
     */
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
      return size() > MAX_SIZE;
    }

  }

}
