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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.mulgara.query.Constraint;
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.StoreException;
import org.mulgara.store.nodepool.NewNodeListener;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.stringpool.SPComparator;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.SPTypedLiteral;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.stringpool.SPObject.TypeCategory;
import org.mulgara.store.stringpool.xa.SPObjectFactoryImpl;
import org.mulgara.store.tuples.Annotation;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.SimpleTuplesFormat;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.store.xa.AVLComparator;
import org.mulgara.store.xa.AVLFile;
import org.mulgara.store.xa.AVLNode;
import org.mulgara.store.xa.AbstractBlockFile;
import org.mulgara.store.xa.Block;
import org.mulgara.store.xa.BlockFile;
import org.mulgara.store.xa.LockFile;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.store.xa.XANodePool;
import org.mulgara.store.xa.XAStringPool;
import org.mulgara.store.xa.XAUtils;
import org.mulgara.util.Constants;
import org.mulgara.util.LongMapper;
import org.mulgara.util.functional.Pair;
import org.mulgara.util.io.LBufferedFile;
import org.mulgara.util.io.MappingUtil;

import static org.mulgara.store.stringpool.xa11.DataStruct.*;

/**
 * This is a WORM transactional string pool. The only write operations that are permitted
 * are insertions. Deletions are ignored. The only exception to this is that rollback
 * on a transaction results in a set of writes being abandoned.
 *
 * @created Aug 11, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class XA11StringPoolImpl implements XAStringPool, XANodePool {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(XA11StringPoolImpl.class);

  /** The number of metaroots in the metaroot file. */
  private static final int NR_METAROOTS = 2;

  /** The variables to use when pretending that the string pool is a Tuples. */
  static final Variable[] VARIABLES = new Variable[] { StatementStore.VARIABLES[0] };

  /** A factory for this class. */
  static final SPObjectFactory SPO_FACTORY = SPObjectFactoryImpl.getInstance();

  /** The main data structures are rooted on this filename. */
  private String mainFilename;

  /** The flat data structures are rooted on this filename. */
  private String flatDataFilename;

  /** The index file for mapping data to a gNode. */
  private AVLFile dataToGNode;

  /** The file reader for mapping gNodes to data. */
  private LBufferedFile gNodeToDataFile;

  /** The object for creating the output appender. */
  private FileOutputStream gNodeToDataOutputStream;

  /** A writing object for the flat file for mapping gNodes to data. */
  private FileChannel gNodeToDataAppender;

  /** Indicates that the current phase has been written to. */
  private boolean dirty = true;

  /** The next-gNode value. This corresponds to the end of the flat file. */
  private long nextGNodeValue;

  /** The object for handling blank node allocation. */
  private BlankNodeAllocator blankNodeAllocator = new BlankNodeAllocator();

  /** The latest phase in the index tree. */
  private TreePhase currentPhase = null;

  /** The next-gNode for the committed phase. */
  private long committedNextGNode;

  /** The LockFile that protects the string pool from being opened twice. */
  private LockFile lockFile;

  /** The BlockFile for the node pool metaroot file. */
  private BlockFile metarootFile = null;

  /** The metaroot info in the metaroot file. */
  private Metaroot[] metaroots = new Metaroot[NR_METAROOTS];

  /** A Token on the last committed phase. */
  private TreePhase.Token committedPhaseToken = null;

  /** Object used for locking on synchronized access to the committed phase. */
  private Object committedPhaseLock = new Object();

  /** Phase reference for when the phase is being written. */
  private TreePhase.Token recordingPhaseToken = null;

  /** Indicates that the phase is written but not yet acknowledged as valid. */
  private boolean prepared = false;

  /** The valid phase index on file to use.  Must always be 0 or 1. */
  private int phaseIndex = 0;

  /** The number of the current phase.  These increase monotonically. */
  private int phaseNumber = 0;

  /** A list of listeners to inform whenever a new node is created. */
  private List<NewNodeListener> newNodeListeners = new LinkedList<NewNodeListener>();

  /** A flag used to delay throwing an exception on the file version until it is needed. */
  private boolean wrongFileVersion = false;

  /** Cache the mapping of node IDs to objects */
  private WeakHashMap<Long,SPObject> nodeCache = new WeakHashMap<Long,SPObject>();

  /**
   * Create a string pool instance using a set of directories.
   * @param basenames A list of paths for creating string pool files in.
   *        Each path is expected to be on a separate file system.
   * @throws IOException The files cannot be created or read.
   */
  public XA11StringPoolImpl(String[] basenames) throws IOException {
    distributeFilenames(basenames);

    lockFile = LockFile.createLockFile(mainFilename + ".sp.lock");

    wrongFileVersion = false;
    try {
      try {
        wrongFileVersion = !Metaroot.check(mainFilename + ".sp");
      } catch (FileNotFoundException ex) {
        // no-op
      }

      dataToGNode = new AVLFile(mainFilename + ".sp_avl", PAYLOAD_SIZE);
      gNodeToDataOutputStream = new FileOutputStream(flatDataFilename, true);
      gNodeToDataAppender = gNodeToDataOutputStream.getChannel();
      gNodeToDataFile = LBufferedFile.createReadOnly(flatDataFilename);

    } catch (IOException ex) {
      try {
        close();
      } catch (StoreException ex2) {
        // no-op
      }
      throw ex;
    }

    // clear the cache whenever the whole file is mapped
    gNodeToDataFile.registerRemapListener(new Runnable() { public void run() { nodeCache.clear(); } });
  }


  /**
   * Returns the most recent successful committed phase.
   * @see org.mulgara.store.xa.SimpleXARecoveryHandler#recover()
   */
  public int[] recover() throws SimpleXAResourceException {
    if (currentPhase != null) return new int[0];
    if (wrongFileVersion) throw new SimpleXAResourceException("Wrong metaroot file version.");

    try {
      openMetarootFile(false);
    } catch (IOException ex) {
      throw new SimpleXAResourceException("I/O error", ex);
    }

    // Count the number of valid phases.
    int phaseCount = 0;
    if (metaroots[0].getValid() != 0) ++phaseCount;
    if (metaroots[1].getValid() != 0) ++phaseCount;

    // Read the phase numbers.
    int[] phaseNumbers = new int[phaseCount];
    int index = 0;
    if (metaroots[0].getValid() != 0) phaseNumbers[index++] = metaroots[0].getPhaseNr();
    if (metaroots[1].getValid() != 0) phaseNumbers[index++] = metaroots[1].getPhaseNr();
    return phaseNumbers;
  }


  /**
   * @see org.mulgara.store.stringpool.StringPool#put(long, org.mulgara.store.stringpool.SPObject)
   */
  public void put(long node, SPObject spObject) throws StringPoolException {
    throw new UnsupportedOperationException("Cannot manually allocate a gNode for this string pool.");
  }


  /**
   * Stores an spObject and allocates a gNode to go with it.
   * @param spObject The object to store.
   * @return The new gNode associated with this object.
   * @throws StringPoolException If the string pool could not allocate space.
   */
  public synchronized long put(SPObject spObject) throws StringPoolException {
    try {
      long gNode = nextGNodeValue;
      DataStruct spObjectData = new DataStruct(spObject, nextGNodeValue);
      // this is the secret sauce - gNodes allocation moves up by size of the data
      nextGNodeValue += spObjectData.writeTo(gNodeToDataAppender);
      mapObjectToGNode(spObjectData, spObject.getSPComparator());
      informNodeListeners(gNode);
      nodeCache.put(gNode, spObject);
      return gNode;
    } catch (IOException e) {
      throw new StringPoolException("Unable to write to data files.", e);
    }
  }


  /**
   * Sets the node pool for this string pool. Not used for this implementation.
   * @param nodePool The node pool being set. Ignored.
   */
  public void setNodePool(XANodePool nodePool) {
    if (nodePool != this) throw new IllegalArgumentException("XA 1.1 data pool requires an integrated node pool.");
    if (logger.isDebugEnabled()) logger.debug("Setting a node pool for the XA 1.1 string pool. Ignored.");
  }


  /**
   * @see org.mulgara.store.stringpool.StringPool#findGNode(org.mulgara.store.stringpool.SPObject)
   */
  public long findGNode(SPObject spObject) throws StringPoolException {
    checkInitialized();
    return currentPhase.findGNode(spObject, false);
  }

  /**
   * @deprecated The <var>nodePool</var> parameter must equal this. Use {@link #findGNode(SPObject, boolean)} with a true <var>create</var> parameter instead.
   * @see org.mulgara.store.stringpool.StringPool#findGNode(org.mulgara.store.stringpool.SPObject, org.mulgara.store.nodepool.NodePool)
   */
  public long findGNode(SPObject spObject, NodePool nodePool) throws StringPoolException {
    if (nodePool != this) throw new IllegalStateException("The XA11 data store must manage its own nodes");
    checkInitialized();
    return currentPhase.findGNode(spObject, true);
  }

  /**
   * @see org.mulgara.store.stringpool.StringPool#findGNode(org.mulgara.store.stringpool.SPObject, org.mulgara.store.nodepool.NodePool)
   */
  public long findGNode(SPObject spObject, boolean create) throws StringPoolException {
    checkInitialized();
    return currentPhase.findGNode(spObject, create);
  }

  /**
   * @see org.mulgara.store.stringpool.StringPool#findGNodes(org.mulgara.store.stringpool.SPObject, boolean, org.mulgara.store.stringpool.SPObject, boolean)
   */
  public Tuples findGNodes(SPObject lowValue, boolean inclLowValue,
                           SPObject highValue, boolean inclHighValue) throws StringPoolException {
    checkInitialized();
    dirty = false;
    return currentPhase.findGNodes(lowValue, inclLowValue, highValue, inclHighValue);
  }

  /**
   * @see org.mulgara.store.stringpool.StringPool#findGNodes(org.mulgara.store.stringpool.SPObject.TypeCategory, java.net.URI)
   */
  public Tuples findGNodes(TypeCategory typeCategory, URI typeURI) throws StringPoolException {
    checkInitialized();
    dirty = false;
    return currentPhase.findGNodes(typeCategory, typeURI);
  }

  /**
   * @see org.mulgara.store.stringpool.StringPool#findSPObject(long)
   */
  public SPObject findSPObject(long node) throws StringPoolException {
    // blank nodes don't get loaded up as an SPObject
    if (BlankNodeAllocator.isBlank(node)) return null;
    // outside of the allocated range
    if (node >= nextGNodeValue) return null;
    
    // Look aside into the cache first
    SPObject cached = nodeCache.get(node);
    if (cached != null) return cached;

    try {
      return new DataStruct(gNodeToDataFile, node).getSPObject();
    } catch (IllegalArgumentException iae) {
      throw new StringPoolException("Bad node data. gNode = " + node, iae);
    } catch (IOException ioe) {
      throw new StringPoolException("Unable to load data from data pool.", ioe);
    }
  }

  /**
   * @see org.mulgara.store.stringpool.StringPool#getSPObjectFactory()
   */
  public SPObjectFactory getSPObjectFactory() {
    return SPO_FACTORY;
  }

  /**
   * @see org.mulgara.store.stringpool.StringPool#remove(long)
   * Nodes are never removed.
   * @return Always true, so that anyone thinking it should have been removed will
   *         get the answer they were expecting.
   */
  public boolean remove(long node) throws StringPoolException {
    return true;
  }


  /**
   * @see org.mulgara.store.xa.XAStringPool#close()
   */
  public void close() throws StoreException {
    try {
      close(false);
    } catch (IOException ex) {
      throw new StringPoolException("I/O error closing string pool.", ex);
    }
  }


  /**
   * @see org.mulgara.store.xa.XAStringPool#delete()
   */
  public void delete() throws StoreException {
    try {
      close(true);
    } catch (IOException ex) {
      throw new StringPoolException("I/O error deleting string pool.", ex);
    } finally {
      gNodeToDataFile = null;
      gNodeToDataAppender = null;
      dataToGNode = null;
      metarootFile = null;
    }
  }


  /**
   * @see org.mulgara.store.xa.XAStringPool#newReadOnlyStringPool()
   */
  public XAStringPool newReadOnlyStringPool() {
    return new ReadOnlyStringPool();
  }


  /**
   * @see org.mulgara.store.xa.XAStringPool#newWritableStringPool()
   */
  public XAStringPool newWritableStringPool() {
    return this;
  }


  /**
   * @see org.mulgara.store.xa.SimpleXAResource#commit()
   */
  public void commit() throws SimpleXAResourceException {
    synchronized (this) {
      if (!prepared) throw new SimpleXAResourceException("commit() called without previous prepare().");
  
      // Perform a commit.
      try {
        // New phase is now marked valid. Invalidate the metaroot of the old phase.
        Metaroot mr = metaroots[1 - phaseIndex];
        mr.setValid(0);
        mr.write();
        metarootFile.force();
  
        // Release the token for the previously committed phase.
        synchronized (committedPhaseLock) {
          if (committedPhaseToken != null) committedPhaseToken.release();
          committedPhaseToken = recordingPhaseToken;
        }
        recordingPhaseToken = null;
        blankNodeAllocator.commit();
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
  }


  /**
   * @see org.mulgara.store.xa.SimpleXAResource#getPhaseNumber()
   */
  public synchronized int getPhaseNumber() throws SimpleXAResourceException {
    checkInitialized();
    return phaseNumber;
  }


  /**
   * Writes all transactional data to disk, in preparation for a full commit.
   * @throws SimpleXAResourceException Occurs due to an IO error when writing data to disk.
   */
  public void prepare() throws SimpleXAResourceException {
    // TODO: This synchronization is possibly redundant due to the global lock in StringPoolSession
    synchronized(this) {
      checkInitialized();
  
      if (prepared) {
        // prepare already performed.
        throw new SimpleXAResourceException("prepare() called twice.");
      }
  
      try {
        // Perform a prepare.
        recordingPhaseToken = currentPhase.new Token();
        TreePhase recordingPhase = currentPhase;
        currentPhase = new TreePhase();
  
        // Ensure that all data associated with the phase is on disk.
        dataToGNode.force();
        gNodeToDataAppender.force(true);
  
        // Write the metaroot.
        int newPhaseIndex = 1 - phaseIndex;
        int newPhaseNumber = phaseNumber + 1;
  
        Metaroot metaroot = metaroots[newPhaseIndex];
        metaroot.setValid(0);
        metaroot.setPhaseNr(newPhaseNumber);
        metaroot.setFlatFileSize(DataStruct.toOffset(nextGNodeValue));
        blankNodeAllocator.prepare(metaroot);
        if (logger.isDebugEnabled()) logger.debug("Writing data pool metaroot for phase: " + newPhaseNumber);
        metaroot.addPhase(recordingPhase);
        metaroot.write();
        metarootFile.force();
        metaroot.setValid(1);
        metaroot.write();
        metarootFile.force();
  
        phaseIndex = newPhaseIndex;
        phaseNumber = newPhaseNumber;
        committedNextGNode = nextGNodeValue;
        prepared = true;
      } catch (IOException ex) {
        logger.error("I/O error while performing prepare.", ex);
        throw new SimpleXAResourceException("I/O error while performing prepare.", ex);
      } finally {
        if (!prepared) {
          logger.error("Prepare failed.");
          if (recordingPhaseToken != null) {
            recordingPhaseToken.release();
            recordingPhaseToken = null;
          }
        }
      }
    }
  }


  /**
   * Drops all data in the current transaction, recovering any used resources.
   * @throws SimpleXAResourceException Caused by any IO errors.
   */
  public void rollback() throws SimpleXAResourceException {
    // TODO: This synchronization is probably redundant due to the global lock in StringPoolSession
    SimpleXAResourceException xaEx = null; // variable to hold the first thrown exception.
    synchronized (this) {
      checkInitialized();
      try {
        if (prepared) {
          // Restore phaseIndex and phaseNumber to their previous values.
          phaseIndex = 1 - phaseIndex;
          --phaseNumber;
          recordingPhaseToken = null;
          prepared = false;
  
          // Invalidate the metaroot of the other phase.
          Metaroot mr = metaroots[1 - phaseIndex];
          mr.setValid(0);
          mr.write();
          metarootFile.force();
        }
      } catch (IOException ex) {
        xaEx = new SimpleXAResourceException("I/O error while performing rollback (invalidating metaroot)", ex);
      } finally {
        try {
          try {
            blankNodeAllocator.rollback();
            nextGNodeValue = committedNextGNode;
            long offset = DataStruct.toOffset(nextGNodeValue);
            gNodeToDataAppender.position(offset);
            // tell the read-only access to prepare for truncation
            gNodeToDataFile.truncate(offset);
            MappingUtil.truncate(gNodeToDataAppender, offset);
          } catch (IOException ioe) {
            String msg = "I/O error while performing rollback (new committed phase)";
            if (xaEx == null) xaEx = new SimpleXAResourceException(msg, ioe); // this is the first exception.
            else logger.info(msg, ioe); // another exception already occurred, log the suppressed exception.
          }
        } finally {
          try {
            currentPhase = new TreePhase(committedPhaseToken.getPhase());
          } catch (IOException ex) {
            String msg = "I/O error while performing rollback (new committed phase)";
            if (xaEx == null) xaEx = new SimpleXAResourceException(msg, ex); // this is the first exception.
            else logger.info(msg, ex); // another exception already occurred, log the suppressed exception.
          } finally {
            // This is the last thing to execute; re-throw any previously caught exception now.
            if (xaEx != null) throw xaEx;
          }
        }
      }
    }
  }


  public void refresh() throws SimpleXAResourceException {
    /* no-op */
  }

  public void release() throws SimpleXAResourceException {
    /* no-op */
  }


  /**
   * @see org.mulgara.store.xa.SimpleXARecoveryHandler#clear()
   */
  public synchronized void clear() throws IOException, SimpleXAResourceException {
    if (currentPhase == null) clear(0);
  }


  /**
   * @see org.mulgara.store.xa.SimpleXARecoveryHandler#clear(int)
   */
  public void clear(int phaseNumber) throws IOException, SimpleXAResourceException {
    if (currentPhase != null) throw new IllegalStateException("StringPool already has a current phase.");

    openMetarootFile(true);

    synchronized (committedPhaseLock) {
      committedPhaseToken = new TreePhase().new Token();
    }
    this.phaseNumber = phaseNumber;
    phaseIndex = 1;
    dataToGNode.clear();
    blankNodeAllocator.clear();

    // clear the flat file
    nextGNodeValue = NodePool.MIN_NODE;
    committedNextGNode = NodePool.MIN_NODE;
    // this forces a seek to 0
    gNodeToDataFile.truncate(0);
    MappingUtil.truncate(gNodeToDataAppender, 0);

    currentPhase = new TreePhase();
  }


  /**
   * This gets called after {@link #recover()}.
   * It selects the active phase to use, and sets all the internal data related to a phase.
   * @see org.mulgara.store.xa.SimpleXARecoveryHandler#selectPhase(int)
   */
  public void selectPhase(int phaseNumber) throws IOException, SimpleXAResourceException {
    // check if this was already called
    if (currentPhase != null) {
      if (phaseNumber != this.phaseNumber) throw new SimpleXAResourceException("selectPhase() called on initialized StringPoolImpl.");
      return;
    }
    if (metarootFile == null) throw new SimpleXAResourceException("String pool metaroot file is not open.");

    // Locate the metaroot corresponding to the given phase number.
    if (metaroots[0].getValid() != 0 && metaroots[0].getPhaseNr() == phaseNumber) {
      phaseIndex = 0;
      // A new phase will be saved in the other metaroot.
    } else if (metaroots[1].getValid() != 0 && metaroots[1].getPhaseNr() == phaseNumber) {
      phaseIndex = 1;
      // A new phase will be saved in the other metaroot.
    } else {
      throw new SimpleXAResourceException("Phase number [" + phaseNumber + "] is not present in the metaroot file. Found [" + metaroots[0].getPhaseNr() + "], [" + metaroots[1].getPhaseNr() + "]");
    }

    Metaroot metaroot = metaroots[phaseIndex];

    // Load a duplicate of the selected phase.  The duplicate will have a
    // phase number which is one higher than the original phase.
    try {
      synchronized (committedPhaseLock) {
        committedPhaseToken = new TreePhase(metaroot.block).new Token();
      }
      this.phaseNumber = phaseNumber;
    } catch (IllegalStateException ex) {
      throw new SimpleXAResourceException("Cannot construct initial phase.", ex);
    }
    // load all the remaining state for this phase
    blankNodeAllocator.setCurrentState(metaroot.getNextBlankNode());
    long fileSize = metaroot.getFlatFileSize();
    committedNextGNode = DataStruct.toGNode(fileSize);
    nextGNodeValue = committedNextGNode;
    updateAppender(fileSize);
    currentPhase = new TreePhase();

    // Invalidate the on-disk metaroot that the new phase will be saved to.
    Metaroot mr = metaroots[1 - phaseIndex];
    mr.setValid(0);
    mr.write();
    metarootFile.force();
  }


  public void newNode(long node) throws Exception {
    /* no-op: This was already allocated by this object */
  }

  public void releaseNode(long node) {
    /* no-op */
  }


  /**
   * @see org.mulgara.store.xa.XANodePool#addNewNodeListener(org.mulgara.store.nodepool.NewNodeListener)
   */
  public void addNewNodeListener(NewNodeListener l) {
    if (l != this) newNodeListeners.add(l);
  }


  /**
   * @see org.mulgara.store.xa.XANodePool#newReadOnlyNodePool()
   */
  public XANodePool newReadOnlyNodePool() {
    return this;
  }


  /**
   * @see org.mulgara.store.xa.XANodePool#newWritableNodePool()
   */
  public XANodePool newWritableNodePool() {
    return this;
  }


  /**
   * @see org.mulgara.store.xa.XANodePool#removeNewNodeListener(org.mulgara.store.nodepool.NewNodeListener)
   */
  public void removeNewNodeListener(NewNodeListener l) {
    newNodeListeners.remove(l);
  }


  /**
   * Allocate a new blank node. This interface was defined for allocating all nodes
   * but standard node allocation is now handled internally within this data pool
   * rather than calling back into this method.
   * @see org.mulgara.store.nodepool.NodePool#newNode()
   */
  public long newNode() throws NodePoolException {
    long node = blankNodeAllocator.allocate();
    return informNodeListeners(node);
  }


  /** @see org.mulgara.store.xa.XANodePool#getNodeMapper() */
  public LongMapper getNodeMapper() throws Exception {
    return new BlankNodeMapper("n2n");
  }


  /**
   * Inform all listeners that a new node was just allocated.
   * @param newNode The newly allocated node.
   * @return The node that was passed to all the listeners.
   */
  private long informNodeListeners(long newNode) {
    for (NewNodeListener l: newNodeListeners) {
      try {
        l.newNode(newNode);
      } catch (Exception e) {
        logger.error("Error informing object [" + l.getClass() + ":" + l + "] of a new node", e);
      }
    }
    return newNode;
  }


  /**
   * Inserts an object into an index, so it can be looked up to find a gNode.
   * @param spObjectData The data for the object used as a key to the index.
   * @param comparator The SPComparator used for compararing data of the provided type.
   */
  private void mapObjectToGNode(DataStruct spObjectData, SPComparator comparator) throws StringPoolException, IOException {
    checkInitialized();
    if (!dirty && currentPhase.isInUse()) {
      currentPhase = new TreePhase();
      dirty = true;
    }

    if (logger.isDebugEnabled()) logger.debug("put(" + spObjectData.getGNode() + ", " + spObjectData + ")");

    try {
      currentPhase.put(spObjectData, comparator);
    } catch (RuntimeException ex) {
      if (logger.isDebugEnabled()) logger.debug("RuntimeException in put()", ex);
      throw ex;
    } catch (Error e) {
      if (logger.isDebugEnabled()) logger.debug("Error in put()", e);
      throw e;
    } catch (StringPoolException ex) {
      if (logger.isDebugEnabled()) logger.debug("StringPoolException in put()", ex);
      throw ex;
    }
  }


  /**
   * Checks that the phase for the tree index has been set.
   * @throws IllegalStateException If the currentPhase is not initialized.
   */
  private void checkInitialized() {
    if (currentPhase == null) {
      throw new IllegalStateException("No current phase. Object Pool has not been initialized or has been closed.");
    }
  }


  /**
   * Remove all mappings of files, so we can close them, and possibly delete them.
   */
  public synchronized void unmap() {
    if (committedPhaseToken != null) {
      recordingPhaseToken = null;
      prepared = false;

      try {
        new TreePhase(committedPhaseToken.getPhase());
      } catch (Throwable t) {
        logger.warn("Exception while rolling back in unmap()", t);
      }
      currentPhase = null;

      synchronized (committedPhaseLock) {
        committedPhaseToken.release();
        committedPhaseToken = null;
      }
    }

    if (dataToGNode != null) dataToGNode.unmap();

    if (metarootFile != null) {
      if (metaroots[0] != null) metaroots[0] = null;
      if (metaroots[1] != null) metaroots[1] = null;
      metarootFile.unmap();
    }
  }


  /**
   * Closes all the files involved with a data pool
   * @param deleteFiles Remove files after closing them.
   * @throws IOException There was an error accessing the filesystem.
   */
  private void close(boolean deleteFiles) throws IOException {
    try {
      unmap();
    } finally {
      try {
        if (gNodeToDataFile != null) gNodeToDataFile.close();
      } finally {
        try {
          if (gNodeToDataAppender != null) gNodeToDataAppender.close();
        } finally {
          try {
            if (deleteFiles) new File(flatDataFilename).delete();
          } finally {
            try {
              if (dataToGNode != null) {
                if (deleteFiles) dataToGNode.delete();
                else dataToGNode.close();
              }
            } finally {
              try {
                if (metarootFile != null) {
                  if (deleteFiles) metarootFile.delete();
                  else metarootFile.close();
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
  }


  /**
   * Reads the data from the metaroot file, and initializes the files.
   *
   * @param clear If <code>true</code> then empties all files.
   * @throws IOException An error reading or writing files.
   * @throws SimpleXAResourceException An internal error, often caused by an IOException
   */
  private void openMetarootFile(boolean clear) throws IOException, SimpleXAResourceException {
    if (metarootFile == null) {
      metarootFile = AbstractBlockFile.openBlockFile(
          mainFilename + ".sp", Metaroot.getSize() * Constants.SIZEOF_LONG, BlockFile.IOType.EXPLICIT
      );

      // check that the file is the right size
      long nrBlocks = metarootFile.getNrBlocks();
      if (nrBlocks != NR_METAROOTS) {
        if (nrBlocks > 0) {
          logger.info("String pool metaroot file for triple store \"" + mainFilename +
                      "\" has invalid number of blocks: " + nrBlocks);
          // rewrite the file
          if (nrBlocks < NR_METAROOTS) {
            clear = true;
            metarootFile.clear();
          }
        } else {
          // Empty file, so initialize it
          clear = true;
        }
        // expand or contract the file as necessary
        metarootFile.setNrBlocks(NR_METAROOTS);
      }

      metaroots[0] = new Metaroot(this, metarootFile.readBlock(0));
      metaroots[1] = new Metaroot(this, metarootFile.readBlock(1));
    }

    if (clear) {
      // Invalidate the metaroots on disk.
      metaroots[0].clear().write();
      metaroots[1].clear().write();
      metarootFile.force();
    }
  }


  /**
   * Update the data appender. This moves to the end of the file if neccessary, and
   * truncates the file to this endpoint if it is too long (due to an abandoned transaction).
   * @param fileSize The end position of the file
   * @throws IOException Error moving in the file or changing its length.
   */
  private void updateAppender(long fileSize) throws IOException {
    // truncate if the file is longer than the appending position
    gNodeToDataFile.truncate(fileSize);
    MappingUtil.truncate(gNodeToDataAppender, fileSize);
    gNodeToDataAppender.position(fileSize);
  }


  /**
   * Makes the best available use of the provided paths, to reduce seek contention
   * where possible. Directories in this list are built from the head, with each
   * extra directory in the list being used to optimize operations on certain files.
   * The presumption of multiple directories is that each one will occur on
   * different filesystems.
   * @param basenames An array of paths available for storing this string pool.
   */
  private void distributeFilenames(String[] basenames) {
    if (basenames == null || basenames.length == 0) {
      throw new IllegalArgumentException("At least one directory must be provided for storing the string pool");
    }
    mainFilename = basenames[0];
    flatDataFilename = mainFilename;

    if (basenames.length > 1) {
      flatDataFilename = basenames[1];
    }

    flatDataFilename += ".sp_nd";
  }


  /**
   * This is a struct for holding metaroot information.
   */
  final static class Metaroot {

    /** Unique int value in metaroot to mark this file as a string pool. */
    static final int FILE_MAGIC = 0xa5f3f4f6;

    /** The current int version of this file format, stored in the metaroot. */
    static final int FILE_VERSION = 1;

    /** Index of the file magic number (integer) within each of the two on-disk metaroots. */
    static final int IDX_MAGIC = 0;

    /** Index of the file version number (integer) within each of the two on-disk metaroots. */
    static final int IDX_VERSION = 1;

    /** Index of the valid flag (integer) within each of the two on-disk metaroots. */
    static final int IDX_VALID = 2;

    /** The index of the phase number (integer) in the on-disk phase. */
    static final int IDX_PHASE_NUMBER = 3;

    /** The integer index of long with the committed flat file size. */
    static final int IDX_FLAT_FILE_SIZE = 4;

    /** The long index of long with the committed flat file size. */
    static final int IDX_L_FLAT_FILE_SIZE = 2;

    /** The integer index of long with the committed next blank node. */
    static final int IDX_NEXT_BLANK = 6;

    /** The long index of long with the committed next blank node. */
    static final int IDX_L_NEXT_BLANK = 3;

    /** The size of the header of a metaroot in longs. */
    static final int HEADER_SIZE_LONGS = IDX_L_NEXT_BLANK + 1;

    /** The size of the header of a metaroot in ints. */
    static final int HEADER_SIZE_INTS = HEADER_SIZE_LONGS * 2;

    /** The size of a metaroot in longs. This is the metaroot header, plus the rest of the data given to the metaroot. */
    static final int METAROOT_SIZE_LONGS = HEADER_SIZE_LONGS + TreePhase.RECORD_SIZE;

    /** The size of a metaroot in longs. */
    static final int METAROOT_SIZE_INTS = METAROOT_SIZE_LONGS * 2;

    /** The VALID flag for a metaroot. */
    int valid;

    /** The phase number for a metaroot. */
    int phaseNr;

    /** The size of the flat file described by this metaroot. */
    long flatFileSize;

    /** The metaroot description of the next blank node to be allocated. */
    long nextBlankNode;

    /** The block this data structure sits on top of. */
    final Block block;

    /**
     * Creates a new metaroot around a block.
     * @param block The block to build the structure around.
     */
    public Metaroot(XA11StringPoolImpl currentPool, Block block) throws IOException {
      this.block = block;
      read(currentPool);
    }

    /** Gets the total size of this block, in LONG values. */
    public static int getSize() { return METAROOT_SIZE_LONGS; }

    /** Gets the size of the header portion of this block, in LONG values. */
    public static int getHeaderSize() { return HEADER_SIZE_LONGS; }

    /**
     * Clears out a block holding metaroot information.
     */
    public Metaroot clear() {
      block.putInt(IDX_MAGIC, FILE_MAGIC);
      block.putInt(IDX_VERSION, FILE_VERSION);
      block.putInt(IDX_VALID, 0);
      block.putInt(IDX_PHASE_NUMBER, 0);
      block.putLong(IDX_L_FLAT_FILE_SIZE, 0);
      block.putLong(IDX_L_NEXT_BLANK, BlankNodeAllocator.FIRST);
      int[] empty = new int[METAROOT_SIZE_INTS - HEADER_SIZE_INTS];
      block.put(HEADER_SIZE_INTS, empty);
      valid = 0;
      phaseNr = 0;
      flatFileSize = 0;
      return this;
    }

    /**
     * Writes this metaroot information to a block, sans phase information
     */
    public Metaroot writeAllToBlock() {
      block.putInt(IDX_MAGIC, FILE_MAGIC);
      block.putInt(IDX_VERSION, FILE_VERSION);
      block.putInt(IDX_VALID, valid);
      block.putInt(IDX_PHASE_NUMBER, phaseNr);
      block.putLong(IDX_L_FLAT_FILE_SIZE, flatFileSize);
      block.putLong(IDX_L_NEXT_BLANK, nextBlankNode);
      // phase information is not written
      return this;
    }

    /**
     * Writes the metaroot block out.
     * @return The current metaroot.
     */
    public Metaroot write() throws IOException {
      block.write();
      return this;
    }

    /**
     * Reads metaroot information out of a block and into this structure.
     * @param currentPool Unused.
     */
    public Metaroot read(XA11StringPoolImpl currentPool) throws IOException {
      valid = block.getInt(IDX_VALID);
      phaseNr = block.getInt(IDX_PHASE_NUMBER);
      flatFileSize = block.getLong(IDX_L_FLAT_FILE_SIZE);
      nextBlankNode = block.getLong(IDX_L_NEXT_BLANK);
      return this;
    }

    /**
     * Tests if this metaroot contains appropriate metaroot information.
     * @return <code>true</code> for a block with an appropriate header, <code>false</code> otherwise.
     */
    public boolean check() {
      return FILE_MAGIC == block.getInt(IDX_MAGIC) && FILE_VERSION == block.getInt(IDX_VERSION);
    }

    /**
     * Tests if a block contains appropriate metaroot information.
     * @param block The block to test.
     * @return <code>true</code> for a block with an appropriate header, <code>false</code> otherwise.
     */
    public static boolean check(Block block) {
      return FILE_MAGIC == block.getInt(IDX_MAGIC) && FILE_VERSION == block.getInt(IDX_VERSION);
    }

    /**
     * Tests if a raw file starts with appropriate metaroot information.
     * @param filename The name of the file to test.
     * @return <code>true</code> for a file with an appropriate header, <code>false</code> otherwise.
     */
    public static boolean check(String filename) throws IOException {
      boolean failed = false;
      RandomAccessFile file = new RandomAccessFile(filename, "r");
      try {
        if (file.length() < 2 * Constants.SIZEOF_INT) return false;
        int fileMagic = file.readInt();
        int fileVersion = file.readInt();
        if (AbstractBlockFile.byteOrder != ByteOrder.BIG_ENDIAN) {
          fileMagic = XAUtils.bswap(fileMagic);
          fileVersion = XAUtils.bswap(fileVersion);
        }
        if (FILE_MAGIC != fileMagic || FILE_VERSION != fileVersion) return false;
      } catch (IOException e) {
        failed = true;
        throw e;
      } finally {
        try {
          file.close();
        } catch (IOException e) {
          if (!failed) throw e;
          else logger.info("I/O exception closing a failed file", e);
        }
      }
      return true;
    }

    public int getVersion() { return FILE_MAGIC; }
    public int getMagicNumber() { return FILE_VERSION; }
    public int getValid() { return valid; }
    public int getPhaseNr() { return phaseNr; }
    public long getFlatFileSize() { return flatFileSize; }
    public long getNextBlankNode() { return nextBlankNode; }

    public void setValid(int valid) { this.valid = valid; block.putInt(IDX_VALID, valid); }
    public void setPhaseNr(int phaseNr) { this.phaseNr = phaseNr; block.putInt(IDX_PHASE_NUMBER, phaseNr); }
    public void setFlatFileSize(long flatFileSize) { this.flatFileSize = flatFileSize; block.putLong(IDX_L_FLAT_FILE_SIZE, flatFileSize); }
    public void setNextBlankNode(long nextBlankNode) { this.nextBlankNode = nextBlankNode; block.putLong(IDX_L_NEXT_BLANK, nextBlankNode); }
    public void addPhase(TreePhase phase) { phase.avlFilePhase.writeToBlock(block, HEADER_SIZE_LONGS); }

  }

  /**
   * An internal read-only view of the current string pool.
   */
  final class ReadOnlyStringPool implements XAStringPool {

    /** Releases resources held by the string pool. Not used. */
    public void close() throws StringPoolException {
      throw new UnsupportedOperationException("Trying to close a read-only string pool.");
    }

    /** Deletes files used by the string pool. Not used. */
    public void delete() throws StringPoolException {
      throw new UnsupportedOperationException("Trying to delete a read-only string pool.");
    }

    public XAStringPool newReadOnlyStringPool() {
      throw new UnsupportedOperationException("Read-only string pools are not used to manage other string pools.");
    }

    public XAStringPool newWritableStringPool() {
      throw new UnsupportedOperationException("Read-only string pools are not used to manage other string pools.");
    }

    public int[] recover() throws SimpleXAResourceException {
      throw new UnsupportedOperationException("Attempting to recover ReadOnlyStringPool");
    }

    public void selectPhase(int phaseNumber) throws IOException, SimpleXAResourceException {
      throw new UnsupportedOperationException("Attempting to selectPhase of ReadOnlyStringPool");
    }

    public void newNode(long node) throws Exception {
      throw new UnsupportedOperationException("Cannot write to a read-only string pool.");
    }

    public void releaseNode(long node) throws Exception {
      throw new UnsupportedOperationException("Cannot write to a read-only string pool.");
    }

    public void put(long node, SPObject spObject) throws StringPoolException {
      throw new UnsupportedOperationException("Cannot write to a read-only string pool.");
    }

    public boolean remove(long node) throws StringPoolException {
      throw new UnsupportedOperationException("Cannot write to a read-only string pool.");
    }
    
    public void commit() throws SimpleXAResourceException { }
    public void prepare() throws SimpleXAResourceException { }
    public void rollback() throws SimpleXAResourceException { }
    public void clear() throws IOException, SimpleXAResourceException { }
    public void clear(int phaseNumber) throws IOException, SimpleXAResourceException { }

    public void refresh() throws SimpleXAResourceException { /* no-op */ }

    public void release() throws SimpleXAResourceException { }

    public int getPhaseNumber() throws SimpleXAResourceException {
      return phaseNumber;
    }

    public long findGNode(SPObject spObject) throws StringPoolException {
      return XA11StringPoolImpl.this.findGNode(spObject);
    }

    public long findGNode(SPObject spObject, NodePool nodePool) throws StringPoolException {
      throw new UnsupportedOperationException("Cannot manually set the node pool for an XA 1.1 store.");
    }

    public Tuples findGNodes(SPObject lowValue, boolean inclLowValue, SPObject highValue, boolean inclHighValue) throws StringPoolException {
      return XA11StringPoolImpl.this.findGNodes(lowValue, inclLowValue, highValue, inclHighValue);
    }

    public Tuples findGNodes(TypeCategory typeCategory, URI typeURI) throws StringPoolException {
      return XA11StringPoolImpl.this.findGNodes(typeCategory, typeURI);
    }

    public SPObject findSPObject(long node) throws StringPoolException {
      return XA11StringPoolImpl.this.findSPObject(node);
    }

    public SPObjectFactory getSPObjectFactory() {
      return SPO_FACTORY;
    }

    public long put(SPObject spObject) throws StringPoolException, NodePoolException {
      throw new UnsupportedOperationException("Cannot write to a read-only string pool.");
    }

    public void setNodePool(XANodePool nodePool) {
      // NO-OP
    }

    public long findGNode(SPObject spObject, boolean create) throws StringPoolException {
      if (create) throw new UnsupportedOperationException("Trying to modify a read-only string pool.");
      return XA11StringPoolImpl.this.findGNode(spObject, false);
    }

  }


  /**
   * Represents the root of an index tree. This root is updated for each new phase.
   */
  private class TreePhase {

    /** The size of a phase record, in Longs. */
    static final int RECORD_SIZE = AVLFile.Phase.RECORD_SIZE;

    /** The underlying tree to manage. */
    private AVLFile.Phase avlFilePhase;

    /**
     * Create a new phase for the tree.
     */
    public TreePhase() throws IOException {
      avlFilePhase = dataToGNode.new Phase();
    }

    /**
     * A copy constructor for a phase.
     * @param p The existing phase to build this
     * @throws IOException Caused by an IO error in the under AVL tree.
     */
    TreePhase(TreePhase p) throws IOException {
      assert p != null;

      avlFilePhase = dataToGNode.new Phase(p.avlFilePhase);
      // current phase should be set to this
      dirty = true;
    }


    /**
     * A constructor from a block on disk.
     * @param b The block to read from.
     * @throws IOException Caused by an IO error reading the block.
     */
    TreePhase(Block b) throws IOException {
      avlFilePhase = dataToGNode.new Phase(b, Metaroot.HEADER_SIZE_LONGS);
      // current phase should be set to this
      dirty = true;
    }


    /**
     * Indicates if there are any remaining readers on the current phase.
     * @return <code>true</code> if the phase is in use.
     */
    public boolean isInUse() {
      return avlFilePhase.isInUse();
    }

    /**
     * Inserts a node into the tree, mapping data onto a long.
     * @param objectData The node to insert.
     * @param spComparator The comparison mechanism to use to search the tree.
     */
    public void put(DataStruct objectData, SPComparator spComparator) throws StringPoolException {
      if (objectData.getGNode() < NodePool.MIN_NODE) throw new IllegalArgumentException("gNode < MIN_NODE. Object = " + objectData);

      AVLNode[] findResult = null;
      try {
        AVLComparator avlComparator = new DataAVLComparator(spComparator, objectData, gNodeToDataFile);

        // Find the adjacent nodes.
        findResult = avlFilePhase.find(avlComparator, null);
        if (findResult != null && findResult.length == 1) {
          throw new StringPoolException("SPObject already exists.  (existing graph node: " + findResult[0].getPayloadLong(IDX_GRAPH_NODE) + ")");
        }

        put(objectData, findResult);

      } catch (IOException ex) {
        throw new StringPoolException("I/O Error", ex);
      } finally {
        if (findResult != null) AVLFile.release(findResult);
      }
    }


    /**
     * Inserts data into the tree, allocating a new node to store the data in.
     * @param objectData The data to store.
     * @param findResult A pair of nodes that the new node must fit between,
     *        or <code>null</code> if the tree is empty.
     * @throws StringPoolException If the data is already in the tree.
     * @throws IOException If the tree could not be written to.
     */
    private void put(DataStruct objectData, AVLNode[] findResult) throws StringPoolException, IOException {
      // Create the new AVLNode.
      AVLNode newNode = avlFilePhase.newAVLNodeInstance();
      objectData.writeTo(newNode);
      newNode.write();

      if (findResult == null) {
        avlFilePhase.insertFirst(newNode);
      } else {
        // Insert the node into the tree.
        int li = AVLFile.leafIndex(findResult);
        findResult[li].insert(newNode, 1 - li);
      }
      newNode.release();
    }


    /**
     * Finds a graph node matching a given SPObject.
     * @param spObject The SPObject to search on.
     * @param create If <code>true</code> then new nodes are to be allocated when an SPObject
     *        is not found.
     * @return The graph node. <code>Graph.NONE</code> if not found and <var>create</var>
     *         is <code>false</code>.
     * @throws StringPoolException For an internal search error.
     */
    long findGNode(SPObject spObject, boolean create) throws StringPoolException {
      if (spObject == null) throw new StringPoolException("spObject parameter is null");

      long gNode;
      AVLNode[] findResult = null;
      try {
        SPComparator spComparator = spObject.getSPComparator();
        DataStruct objectData = new DataStruct(spObject);
        AVLComparator avlComparator = new DataAVLComparator(spComparator, objectData, gNodeToDataFile);

        // Find the SPObject.
        findResult = avlFilePhase.find(avlComparator, null);
        if (findResult != null && findResult.length == 1) {
          gNode = findResult[0].getPayloadLong(IDX_GRAPH_NODE);
        } else {
          if (create) {
            gNode = nextGNodeValue;
            objectData.setGNode(gNode);
            // allocated gNodes move up by the size of the data between them
            nextGNodeValue += objectData.writeTo(gNodeToDataAppender);
            put(objectData, findResult);
            informNodeListeners(gNode);
          } else {
            // Not found.
            gNode = NodePool.NONE;
          }
        }
      } catch (IOException ex) {
        throw new StringPoolException("I/O Error", ex);
      } catch (RuntimeException ex) {
        if (logger.isDebugEnabled()) logger.debug("RuntimeException in findGNode(" + spObject + ")", ex);
        throw ex;
      } catch (Error e) {
        if (logger.isDebugEnabled()) logger.debug("Error in findGNode(" + spObject + ")", e);
        throw e;
      } finally {
        if (findResult != null) AVLFile.release(findResult);
      }

      if (logger.isDebugEnabled()) logger.debug("findGNode(" + spObject + ") = " + gNode);

      return gNode;
    }


    /**
     * Finds a range of SPObjects.
     * @param lowValue The low end of the range.
     * @param inclLowValue If the low end value is to be included in the results.
     * @param highValue The high end of the range.
     * @param inclHighValue If the high end value is to be included in the results.
     * @return A range of values, in a Tuples.
     * @throws StringPoolException Any kind of error, both due to internal structure and IO errors.
     */
    Tuples findGNodes(SPObject lowValue, boolean inclLowValue, SPObject highValue, boolean inclHighValue) throws StringPoolException {
      SPObject.TypeCategory typeCategory;
      int typeId;
      AVLNode lowAVLNode;
      long highAVLNodeId;

      if (lowValue == null && highValue == null) {
        // Return all nodes in the index.
        typeCategory = null;
        typeId = SPObjectFactory.INVALID_TYPE_ID;
        lowAVLNode = avlFilePhase.getRootNode();
        if (lowAVLNode != null) lowAVLNode = lowAVLNode.getMinNode_R();
        highAVLNodeId = Block.INVALID_BLOCK_ID;
      } else {
        // Get the type category.
        SPObject typeValue = lowValue != null ? lowValue : highValue;
        typeCategory = typeValue.getTypeCategory();
        typeId = typeCategory == SPObject.TypeCategory.TYPED_LITERAL ?
                 ((SPTypedLiteral)typeValue).getTypeId() : SPObjectFactory.INVALID_TYPE_ID;

        // Check that the two SPObjects are of the same type.
        if (lowValue != null && highValue != null) {
          if (
            typeCategory != highValue.getTypeCategory() || (
                typeCategory == SPObject.TypeCategory.TYPED_LITERAL &&
                ((SPTypedLiteral)lowValue).getTypeId() != ((SPTypedLiteral)highValue).getTypeId()
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
          DataStruct lowData = new DataStruct(lowValue);
          SPComparator spComparator = lowValue.getSPComparator();
          // lowComparator = new SPAVLComparator(spComparator, typeCategory, typeId, data);
          lowComparator = new DataAVLComparator(spComparator, lowData, gNodeToDataFile);
        } else {
          // Select the first node with the current type.
          if (typeCategory == SPObject.TypeCategory.TYPED_LITERAL) {
            lowComparator = new DataCategoryTypeAVLComparator(typeCategory.ID, typeId);
          } else {
            lowComparator = new DataCategoryAVLComparator(typeCategory.ID);
          }
        }

        // Compute the comparator for highValue.
        AVLComparator highComparator;
        if (highValue != null) {
          DataStruct highData = new DataStruct(highValue);
          SPComparator spComparator = highValue.getSPComparator();
          highComparator = new DataAVLComparator(spComparator, highData, gNodeToDataFile);
        } else {
          // Select the first node past the last one that has the current type.
          if (typeCategory == SPObject.TypeCategory.TYPED_LITERAL) {
            highComparator = new DataCategoryTypeAVLComparator(typeCategory.ID, typeId + 1);
          } else {
            highComparator = new DataCategoryAVLComparator(typeCategory.ID + 1);
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
              // Did not find the node but found the location where the node would be if it existed.
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


    /**
     * Get the entire set of GNodes that match a given type.
     * @param typeCategory The category of type to match.
     * @param typeURI The specific type to search for.
     * @return A tuples containing all GNodes of the requested type.
     * @throws StringPoolException Caused by a structural error or an IO exception.
     */
    Tuples findGNodes(SPObject.TypeCategory typeCategory, URI typeURI) throws StringPoolException {

      // null paramaters mean we want all GNodes
      if (typeCategory == null) {
        if (typeURI != null) throw new StringPoolException("typeCategory is null and typeURI is not null");
        return findAllGNodes();
      }

      // Convert the type URI to a type ID.
      int typeId;
      try {
        typeId = (typeURI == null) ? SPObjectFactory.INVALID_TYPE_ID : SPO_FACTORY.getTypeId(typeURI);
      } catch (IllegalArgumentException ex) {
        throw new StringPoolException("Unsupported XSD type: " + typeURI, ex);
      }


      // get the appropriate comparators for the requested type
      Pair<AVLComparator,AVLComparator> comparators = getTypeComparators(typeCategory, typeId);
      AVLComparator lowComparator = comparators.first();
      AVLComparator highComparator = comparators.second();

      AVLNode lowAVLNode;
      long highAVLNodeId;

      AVLNode[] findResult = avlFilePhase.find(lowComparator, null);
      if (findResult == null) {
        // Empty store.
        lowAVLNode = null;
        highAVLNodeId = Block.INVALID_BLOCK_ID;
      } else {
        assert findResult.length == 2;
        lowAVLNode = findResult[1];
        if (findResult[0] != null) findResult[0].release();

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

      return new GNodeTuplesImpl(typeCategory, typeId, null, null, lowAVLNode, highAVLNodeId);
    }


    /**
     * Constructs a pair of comparators for finding the lowest and highest AVL nodes for a given type specification.
     * @param typeCategory The category of nodes from the data pool.
     * @param typeId The ID of the type, if it is a literal.
     * @return A pair of comparators for the given type.
     */
    private Pair<AVLComparator,AVLComparator> getTypeComparators(SPObject.TypeCategory typeCategory, int typeId) {
      AVLComparator lowComparator;
      AVLComparator highComparator;

      if (typeCategory == SPObject.TypeCategory.TYPED_LITERAL && typeId != SPObjectFactory.INVALID_TYPE_ID) {
        // Return nodes of the specified category and type node.
        lowComparator = new DataCategoryTypeAVLComparator(typeCategory.ID, typeId);
        highComparator = new DataCategoryTypeAVLComparator(typeCategory.ID, typeId + 1);
      } else {
        // Return nodes of the specified category.
        lowComparator = new DataCategoryAVLComparator(typeCategory.ID);
        highComparator = new DataCategoryAVLComparator(typeCategory.ID + 1);
      }
      return new Pair<AVLComparator,AVLComparator>(lowComparator, highComparator);
    }


    /**
     * Retrieves all nodes in the index.
     * @return A Tuples for all the nodes.
     */
    private Tuples findAllGNodes() {
      AVLNode lowAVLNode = avlFilePhase.getRootNode();
      if (lowAVLNode != null) lowAVLNode = lowAVLNode.getMinNode_R();
      return new GNodeTuplesImpl(null, SPObjectFactory.INVALID_TYPE_ID, null, null, lowAVLNode, Block.INVALID_BLOCK_ID);
    }


    /**
     * Load an SPObject with some type checking.
     * @param typeCategory The category of the object.
     * @param typeId The ID for the object type
     * @param avlNode The node to load the data from.
     * @return The requested object, or <code>null</code> if the object is not compatible with the request.
     */
    private SPObject loadSPObject(SPObject.TypeCategory typeCategory, int typeId, AVLNode avlNode) throws StringPoolException {
      DataStruct data = new DataStruct(avlNode);
      try {
        data.getRemainingBytes(gNodeToDataFile);
      } catch (IOException e) {
        throw new StringPoolException("Unable to read data pool", e);
      }

      int typeCategoryId = data.getTypeCategoryId();
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
      return data.getSPObject();
    }


    /**
     * Attaches a token to a phase. Used to maintain a reference.
     */
    final class Token {

      private AVLFile.Phase.Token avlFileToken;

      /** Constructs a token on the current phase */
      Token() {
        avlFileToken = avlFilePhase.use();
      }


      public TreePhase getPhase() {
        assert avlFileToken != null : "Invalid Token";
        return TreePhase.this;
      }


      public void release() {
        assert avlFileToken != null : "Invalid Token";
        avlFileToken.release();
        avlFileToken = null;
      }

    }


    /**
     * An internal representation of the data structures as a singe Tuples.
     * It would be nice to have this in an external class, but it is imtimately tied
     * into the current phase and the data pool itself. 
     */
    private class GNodeTuplesImpl implements Tuples {

      private static final int INVALID_CARDINALITY = -1;

      /** A cache for the calculated row cardinality. */
      private int rowCardinality = INVALID_CARDINALITY;

      /**
       * The low value of the range (inclusive) or null to indicate the lowest possible value
       * within the type defined by the typeCategory and typeId fields.
       */
      private SPObject lowValue;

      /**
       * The high value of the range (exclusive) or null to indicate the highest possible value
       * within the type defined by the typeCategory and typeId fields.
       */
      private SPObject highValue;

      /** The first index node in the range (inclusive) or null to indicate  an empty Tuples. */
      private AVLNode lowAVLNode;

      /**
       * The last index node in the range (exclusive) or Block.INVALID_BLOCK_ID to indicate all
       * nodes following lowAVLNode in the index.
       */
      private long highAVLNodeId;

      /** The current node. */
      private AVLNode avlNode = null;

      /** Maintains a hold on the phase of the structure being accessed. */
      AVLFile.Phase.Token avlFileToken = null;

      /** The number of nodes. */
      private long nrGNodes;

      /** This is set to true once the number of nodes is known. */
      private boolean nrGNodesValid = false;

      private boolean beforeFirst = false;

      private long[] prefix = null;

      private boolean onPrefixNode = false;

      private Variable[] variables = (Variable[])VARIABLES.clone();

      /**
       * Constructs a GNodeTuplesImpl that represents nodes in the AVLFile
       * index that range from lowAVLNode up to but not including the node with
       * ID highAVLNodeId.
       * @param typeCategory The type of data this Tuples returns.
       * @param typeId The ID of the data being returned.
       * @param lowValue The lower bound of the sequence in this tuples.
       * @param highValue The upper bound of the sequence in this tuples.
       * @param lowAVLNode the AVLNode that has the first graph node that is
       *        included in the Tuples.
       * @param highAVLNodeId the ID of the AVLNode that has the first graph
       *        node that is not included in the Tuples.
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
          avlFileToken = avlFilePhase.use();
        }

        if (typeCategory != SPObject.TypeCategory.TYPED_LITERAL) typeId = SPObjectFactory.INVALID_TYPE_ID;

        this.lowValue = lowValue;
        this.highValue = highValue;
        this.lowAVLNode = lowAVLNode;
        this.highAVLNodeId = highAVLNodeId;
      }


      /**
       * Get the gNode of from the cursor at this point. This is read directly from the AVLNode.
       * @see org.mulgara.store.tuples.Tuples#getColumnValue(int)
       */
      public long getColumnValue(int column) throws TuplesException {
        if (column != 0) throw new TuplesException("Column index out of range: " + column);
        // Handle the prefix.
        if (onPrefixNode) return prefix[0];
        if (avlNode == null) throw new TuplesException("No current row");
        return avlNode.getPayloadLong(DataStruct.IDX_GRAPH_NODE);
      }


      /**
       * @see #getColumnValue(int)
       */
      public long getRawColumnValue(int column) throws TuplesException {
        return getColumnValue(column);
      }


      /**
       * Returns the single variable name for this data.
       */
      public Variable[] getVariables() {
        // Clone the variables array in case the caller changes the returned array.
        return (Variable[])variables.clone();
      }


      /** @return 1, indicating the single column from this data. */
      public int getNumberOfVariables() {
        return 1;
      }


      /**
       * Accumulates the size of this data and returns the number of nodes.
       * This has scope for improvement, if nodes start storing the numbers of decendants.
       * @see org.mulgara.store.tuples.Tuples#getRowCount()
       */
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


      /** Delegates this work to {@link #getRowCount()} */
      public long getRowUpperBound() throws TuplesException {
        return getRowCount();
      }

      /** Delegates this work to {@link #getRowCount()} */
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
        if (rowCardinality != INVALID_CARDINALITY) return rowCardinality;

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
        rowCardinality = count == 0 ? Cursor.ZERO :
                         count == 1 ? Cursor.ONE : Cursor.MANY;
        return rowCardinality;
      }

      /* (non-Javadoc)
       * @see org.mulgara.query.Cursor#isEmpty()
       */
      public boolean isEmpty() throws TuplesException {
        return lowAVLNode == null;
      }


      /** @see org.mulgara.store.tuples.Tuples#getColumnIndex(org.mulgara.query.Variable) */
      public int getColumnIndex(Variable variable) throws TuplesException {
        if (variable == null) throw new IllegalArgumentException("variable is null");
        if (variable.equals(variables[0]))  return 0;
        throw new TuplesException("variable doesn't match any column: " + variable);
      }


      /** @see org.mulgara.store.tuples.Tuples#isColumnEverUnbound(int) */
      public boolean isColumnEverUnbound(int column) {
        return false;
      }


      /** @see org.mulgara.store.tuples.Tuples#isMaterialized() */
      public boolean isMaterialized() {
        return true;
      }


      /** @see org.mulgara.store.tuples.Tuples#isUnconstrained() */
      public boolean isUnconstrained() {
        return false;
      }


      /** @see org.mulgara.store.tuples.Tuples#hasNoDuplicates() */
      public boolean hasNoDuplicates() {
        return true;
      }


      /** @see org.mulgara.store.tuples.Tuples#getComparator() */
      public RowComparator getComparator() {
        return null;  // Unsorted
      }


      /** @see org.mulgara.store.tuples.Tuples#getOperands() */
      public java.util.List<Tuples> getOperands() {
        return java.util.Collections.emptyList();
      }


      /** @see org.mulgara.store.tuples.Tuples#beforeFirst(long[], int) */
      public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
        assert prefix != null;
        if (prefix.length > 1) throw new TuplesException("prefix.length (" + prefix.length + ") > nrColumns (1)");
        if (suffixTruncation != 0) throw new TuplesException("suffixTruncation not supported");

        beforeFirst = true;
        onPrefixNode = false;
        this.prefix = prefix;
        // check if this had been iterating, if so then forget where we were
        if (avlNode != null) {
          avlNode.release();
          avlNode = null;
        }
      }


      /** @see org.mulgara.query.Cursor#beforeFirst() */
      public void beforeFirst() throws TuplesException {
        beforeFirst(Tuples.NO_PREFIX, 0);
      }


      /** @see org.mulgara.store.tuples.Tuples#next() */
      public boolean next() throws TuplesException {
        if (beforeFirst) {
          assert prefix != null;
          assert avlNode == null;
          assert !onPrefixNode;
          beforeFirst = false;

          // Handle the prefix.
          if (prefix.length == 1) {
            // If there are no nodes this Tuples can't contain the prefix node.
            if (lowAVLNode == null) return false;

            SPObject spObject;
            try {
              // FIXME check the type category and type node.
              spObject = findSPObject(prefix[0]);
            } catch (StringPoolException ex) {
              throw new TuplesException("Exception while loading SPObject", ex);
            }

            // Check that the SPObject is within range.
            onPrefixNode = spObject != null &&
                           (lowValue == null || spObject.compareTo(lowValue) >= 0) &&
                           (highValue == null || spObject.compareTo(highValue) < 0);
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
            if (highAVLNodeId != Block.INVALID_BLOCK_ID && avlNode.getId() == highAVLNodeId ) {
              avlNode.release();
              avlNode = null;
            }
          }
        }
        onPrefixNode = false;
        return avlNode != null;
      }


      /**
       * Release the resources reserved by having this tuples refering to the phase.
       * @see org.mulgara.query.Cursor#close()
       */
      public void close() throws TuplesException {
        if (lowAVLNode != null) {
          if (avlNode != null) {
            avlNode.release();
            avlNode = null;
          }
          lowAVLNode.release();
          lowAVLNode = null;
          avlFileToken.release();
          avlFileToken = null;
        }
      }


      /** @see org.mulgara.store.tuples.Tuples#renameVariables(org.mulgara.query.Constraint) */
      public void renameVariables(Constraint constraint) {
        variables[0] = (Variable)constraint.getElement(0);
      }


      /** Duplicate this tuples and its resources. */
      public Object clone() {
        try {
          GNodeTuplesImpl t = (GNodeTuplesImpl)super.clone();
          t.variables = (Variable[])variables.clone();
          if (t.lowAVLNode != null) {
            t.lowAVLNode.incRefCount();
            t.avlFileToken = avlFilePhase.use(); // Allocate a new token.
            if (t.avlNode != null) t.avlNode.incRefCount();
          }
          return t;
        } catch (CloneNotSupportedException e) {
          throw new Error(getClass() + " doesn't support clone, which it must", e);
        }
      }


      /**
       * Iterate over this object and see it looks the same as the comparing object.
       */
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
                      for (int variableIndex = 0; variableIndex < t1.getNumberOfVariables(); variableIndex++) {
                        // If they're not equal quit the loop and set tuplesEqual to false.
                        if (t1.getColumnValue(variableIndex) != t2.getColumnValue(variableIndex)) {
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


      /**
       * Added to match {@link #equals(Object)}.
       */
      public int hashCode() {
        // This works with the above defined equals method
        return TuplesOperations.hashCode(this);
      }

      /** @see java.lang.Object#toString() */
      public String toString() {
        return SimpleTuplesFormat.format(this);
      }


      /**
       * Copied from AbstractTuples
       */
      public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
        return null;
      }

    }  // end of TreePhase.GNodeTuplesImpl
    
  }  // end of TreePhase

}
