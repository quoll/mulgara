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

import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.mulgara.query.Constraint;
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.tuples.Annotation;
import org.mulgara.store.tuples.DenseLongMatrix;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.SimpleTuplesFormat;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.store.xa.AbstractBlockFile;
import org.mulgara.store.xa.BlockFile;
import org.mulgara.util.StackTrace;
import org.mulgara.util.TempDir;

/**
 *
 *
 * @created 2004-03-17
 *
 * @author Andrae Muys
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/05/16 11:07:10 $
 *
 * @maintenanceAuthor $Author: amuys $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class HybridTuples implements Tuples {
  protected static final int BLOCK_SIZE = 8192;
  protected static final int SIZEOF_NODE = 8;
  protected static final int MAX_READ_BUFFER_SIZE = 256 * 1024;
  protected static final String LISTFILE_EXT = "_ml";

  protected final Variable[] vars;
  protected final int width;
  protected final int tuplesPerBuffer;
  protected final RowComparator comparator;
  protected final boolean unconstrained;
  protected final boolean noDuplicates;
  protected final boolean[] columnEverUnbound;
  protected Tuples tuples;
  private final int source;

  protected BlockFile blockFile;
  protected RefCount blockFileRefCount;

  // Would be final except for clone
  // can be final once clone semantics migrated into CacheLine.
  protected boolean beforeFirstCalled;
  protected boolean nextCalled;
  protected CacheLine[] heapCache;

  // Mutated reqularly.
  protected long[] currTuple;
  
  // Used in restore heap
  protected long[] headTuple;
  protected long[] nextTuple;

  // Used in partition
  protected long[] pivotTuple;
  protected long[] tempTuple;

  protected int[] varLookupList;
  private boolean closed = false;

  // Debugging.
  private final static Logger logger = Logger.getLogger(HybridTuples.class);
  private StackTrace allocatedBy;
  private StackTrace closedBy;



  protected HybridTuples(Tuples tuples, RowComparator comparator)
    throws TuplesException {

    if (logger.isDebugEnabled()) {
      logger.debug("HybridTuples created " + System.identityHashCode(this));
    }

    this.source = System.identityHashCode(this);
    this.comparator = comparator;
    this.vars = tuples.getVariables();
    this.unconstrained = tuples.isUnconstrained();
    this.noDuplicates = tuples.hasNoDuplicates();

    // Create a lookup up list of unique variables to their position in an
    // index.
    HashSet<Variable> uniqueVars = new HashSet<Variable>();
    List<Variable> uniqueVarIndex = new ArrayList<Variable>();
    varLookupList = new int[vars.length];
    int varIndex = -1;
    for (int index = 0; index < vars.length; index++) {

      // Add variable to set.
      uniqueVars.add(vars[index]);

      // Check to see if variable is already in list, if not add to list.  Set
      // lookup list to current variable index value.
      int indexPos = uniqueVarIndex.indexOf(vars[index]);
      if (indexPos == -1) {
        uniqueVarIndex.add(vars[index]);
        varIndex++;
        indexPos = varIndex;
      }
      varLookupList[index] = indexPos;
    }

    this.width = uniqueVars.size();
    this.columnEverUnbound = new boolean[this.width];
    Arrays.fill(this.columnEverUnbound, false);
    this.tuplesPerBuffer = this.width > 0 ? MAX_READ_BUFFER_SIZE / (SIZEOF_NODE * this.width) : 1;

    this.blockFileRefCount = new RefCount();
//    long timer = System.currentTimeMillis();
    materialiseTuples(tuples);
//    logger.warn("Materialising tuples(" + (System.currentTimeMillis() - timer) + ") from " + TuplesOperations.formatTuplesTree(tuples));
    this.beforeFirstCalled = false;
    this.nextCalled = false;
    if (logger.isDebugEnabled()) {
      this.tuples = (Tuples)tuples.clone();
    } else {
      this.tuples = TuplesOperations.empty();
    }
    if (logger.isDebugEnabled()) this.allocatedBy = new StackTrace();
  }


  /**
   * Required by Tuples, Cursor.
   */
  public boolean next() throws TuplesException {
//    logger.warn("next() called on " + System.identityHashCode(this));
    if (!beforeFirstCalled) {
      logger.error("next() called before beforeFirst()");
      throw new TuplesException("next() called before beforeFirst()");
    }

    if (!heapCache[0].isEmpty()) {
      currTuple = heapCache[0].getCurrentTuple(currTuple);
      heapCache[0].advance();
      restoreHeap();
      nextCalled = true;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Required by Tuples.
   */
  public void beforeFirst(long[] prefix, int suffixTruncation)
      throws TuplesException {
//    logger.warn("beforeFirst() called on " + System.identityHashCode(this));
    assert prefix != null;

    if (suffixTruncation != 0) {
      logger.error("HybridTuples.beforeFirst(suffix) unimplemented");
      throw new IllegalArgumentException("HybridTuples.beforeFirst(suffix) unimplemented");
    }

    try {
      for (int i = 0; i < heapCache.length; i++) {
        heapCache[i].reset(prefix);
        heapCache[i].advance();
      }
    } catch (Throwable th) {
      logger.error("Failed to reset cachelines", th);
      throw new TuplesException("Error resetting cachelines", th);
    }
    sortHeap();

    beforeFirstCalled = true;
    nextCalled = false;
  }


  /**
   * Required by Tuples.
   */
  public long getColumnValue(int column) throws TuplesException {

    // Validate "column" parameter
    if (column < 0 || column >= width) {
      throw new TuplesException(
        "No column " + column + " in " + Arrays.asList(vars)
      );
    }

    if (!nextCalled) {
      throw new TuplesException("getColumnValue() called before next()");
    }

    return currTuple[varLookupList[column]];
  }


  /**
   * Required by Tuples.
   */
  public void renameVariables(Constraint constraint) {
    loop:
      for (int i = 0; i < this.vars.length; ++i) {
        Variable v = this.vars[i];
        for (int j = 0; j < 4; ++j) {
          // v will be a reference to one of the objects in Graph.VARIABLES[].
          if (v == StatementStore.VARIABLES[j]) {
            // The array obtained from getVariables() is modifiable.
            this.vars[i] = (Variable) constraint.getElement(j);
            continue loop;
          }
        }
        logger.error("Unexpected variable: " + v);
        throw new Error("Unexpected variable: " + v);
      }
  }


  /**
   * Required by Tuples.
   */
  public Object clone() {
    try {
      HybridTuples copy = (HybridTuples)super.clone();
      copy.heapCache = new CacheLine[heapCache.length];
      for (int i = 0; i < heapCache.length; i++) {
        copy.heapCache[i] = (CacheLine)heapCache[i].clone();
      }
      copy.currTuple = copy.heapCache[0].getCurrentTuple(null);
      if (blockFile != null) {
        blockFileRefCount.refCount++;
      }
      if (logger.isDebugEnabled()) copy.allocatedBy = new StackTrace();
      copy.tuples = (Tuples)tuples.clone();

      return copy;
    } catch (CloneNotSupportedException ce) {
      logger.error("HybridTuples.clone() threw CloneNotSupported", ce);
      throw new RuntimeException("HybridTuples.clone() threw CloneNotSupported", ce);
    }
  }


  /**
   * Required by Cursor.
   */
  public void beforeFirst() throws TuplesException {
    beforeFirst(Tuples.NO_PREFIX, 0);
  }


  public List<Tuples> getOperands() {
    return Collections.singletonList(tuples);
  }

  /**
   * Required by Cursor.
   */
  public void close() throws TuplesException {
    if (closed) {
      if (logger.isDebugEnabled()) {
          logger.debug("Attempt to close HybridTuples twice; first closed: " + closedBy);
          logger.debug("Attempt to close HybridTuples twice; second closed: " + new StackTrace());
          logger.debug("    allocated: " + allocatedBy);
      } else {
          logger.error("Attempt to close HybridTuples twice. Enable debug to trace how.");
      }
      throw new TuplesException("Attempted to close HybribTuples more than once");
    }
    closed = true;
    if (logger.isDebugEnabled()) closedBy = new StackTrace();
    
    for (int i = 0; i < heapCache.length; i++) {
      heapCache[i].close(System.identityHashCode(this));
      heapCache[i] = null;
    }
    heapCache = null;
    currTuple = null;

    tuples.close();
    tuples = null;
    if (blockFile != null && --blockFileRefCount.refCount == 0) {
      try {
        delete();
      } catch (IOException ie) {
        logger.warn("Failed to delete blockFile", ie);
        throw new TuplesException("Failed to delete blockFile", ie);
      }
    }
  }


  /**
   * Required by Cursor, Tuples.
   */
  public int getColumnIndex(Variable variable) throws TuplesException {
    for (int c = 0; c < vars.length; c++) {
      if (vars[c].equals(variable)) return c;
    }

    logger.warn("Variable not found: " + variable);
    throw new TuplesException("Variable not found: " + variable);
  }


  /** {@inheritDoc} */
  public long getRawColumnValue(int column) throws TuplesException {
    return tuples.getColumnValue(column);
  }

  /**
   * Required by Cursor.
   */
  public int getNumberOfVariables() {
    return vars != null ? vars.length : 0;
  }


  /**
   * Required by Cursor, Tuples.
   */
  public Variable[] getVariables() {
    return vars;
  }


  /**
   * Required by Cursor, Tuples.
   */
  public boolean isUnconstrained() throws TuplesException {
    return unconstrained;
  }


  /**
   * Required by Cursor, Tuples.
   */
  public long getRowCount() throws TuplesException {
    long result = 0;

    for (int i = 0; i < heapCache.length; i++) {
      result += heapCache[i].getSegmentSize();
    }

    return result;
  }


  public long getRowUpperBound() throws TuplesException {
    return getRowCount();
  }


  public long getRowExpectedCount() throws TuplesException {
    return getRowCount();
  }


  public int getRowCardinality() throws TuplesException {
    switch ((int)getRowCount()) {
      case 0:
        return Cursor.ZERO;
      case 1:
        return Cursor.ONE;
      default:
        return Cursor.MANY;
    }
  }


  public boolean isEmpty() throws TuplesException {
    return getRowCardinality() == Cursor.ZERO;
  }


  /**
   * Required by Tuples.
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    try {
      return columnEverUnbound[column];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new TuplesException("No such column "+column);
    }
  }


  /**
   * Required by Tuples.
   */
  public boolean isMaterialized() {
    return true;
  }


  /**
   * Required by Tuples.
   */
  public boolean hasNoDuplicates() {
    return noDuplicates;
  }


  /**
   * Required by Tuples.
   */
  public RowComparator getComparator() {
    return comparator;
  }


  /**
   * Required by Tuples.
   */
  public boolean equals(Object o) {
    Tuples t;
    Tuples c;

    if (o == this) {
      return true;
    }

    if (!(o instanceof Tuples)) {
      return false;
    }

    t = (Tuples)o;
    if (t instanceof HybridTuples) {
      HybridTuples ft = (HybridTuples)t;
      if (this.source == ft.source) {
        return true;
      }
      if (this.blockFile == ft.blockFile) {
        return true;
      }
    }
    try {
      Variable[] tvars = t.getVariables();
      if (this.getRowCount() != t.getRowCount() ||
          this.vars.length != tvars.length) {
        return false;
      }
      for (int v = 0; v < this.width; v++) {
        if (!this.vars[v].equals(tvars[v])) {
          return false;
        }
      }

      t = (Tuples) t.clone();
      c = (Tuples) this.clone();

      try {
        t.beforeFirst();
        c.beforeFirst();

        while(true) {
          boolean tn = t.next();
          boolean cn = c.next();
          if (!tn && !cn) {
            return true;
          }
          if ((!tn && cn) || (tn && !cn)) {
            return false;
          }
          for (int i = 0; i < width; i++) {
            if (t.getColumnValue(i) != c.getColumnValue(i)) {
              return false;
            }
          }
        }
      } finally {
        t.close();
        c.close();
      }
    } catch (TuplesException te) {
      throw new RuntimeException("Tuples Exception in HybridTuples.equals", te);
    }
  }


  /**
   * Added to match {@link #equals(Object)}. Copy of {@link org.mulgara.store.tuples.AbstractTuples#hashCode()}
   */
  public int hashCode() {
    return TuplesOperations.hashCode(this);
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public String toString() {
    return SimpleTuplesFormat.format(this);
  }


  protected void unmap() throws TuplesException {
    if (blockFile != null) {
      blockFile.unmap();
    }
  }


  protected void delete() throws IOException {
    try {
      if (blockFile != null) {
        blockFile.delete();
        blockFile = null;
      }
    } catch (IOException ie) {
      logger.warn("IO Exception thrown in HybridTuples.delete()", ie);
      throw ie;
    }
  }


  private int materialiseTuples(Tuples tuples) throws TuplesException {
//    long[][] buffer = tuples.getRowUpperBound() < this.tuplesPerBuffer ?
//                        new long[(int)tuples.getRowUpperBound() + 1][this.width] :
//                        new long[this.tuplesPerBuffer][this.width];
    DenseLongMatrix buffer = tuples.getRowUpperBound() < this.tuplesPerBuffer ?
                        new DenseLongMatrix((int)tuples.getRowUpperBound() + 1, this.width) :
                        new DenseLongMatrix(this.tuplesPerBuffer, this.width);
    tuples.beforeFirst();

    int size = primeBuffer(buffer, tuples);
    if (size < buffer.getLength()) {
      this.heapCache = new CacheLine[] { new MemoryCacheLine(buffer, size) };
    } else {
      initialiseBlockFile();

      ArrayList<BlockCacheLine> tmpHeap = new ArrayList<BlockCacheLine>();

      tmpHeap.add(new BlockCacheLine(blockFile, BLOCK_SIZE, buffer, size));
      do {
        size = primeBuffer(buffer, tuples);
        if (size > 0) {
          tmpHeap.add(new BlockCacheLine(blockFile, BLOCK_SIZE, buffer, size));
        }
      } while (size == buffer.getLength());

      this.heapCache = tmpHeap.toArray(new CacheLine[0]);
    }

    return this.heapCache.length;
  }


  private int primeBuffer(DenseLongMatrix buffer, Tuples tuples)
      throws TuplesException {
    int size;

    size = populateArray(buffer, tuples);
    buffer.sort(comparator, size);

    if (logger.isDebugEnabled()) {
      logger.debug("populateArray returned " + size);
    }

    return size;
  }


  private int populateArray(DenseLongMatrix buffer, Tuples tuples) throws TuplesException {
    for (int i = 0; i < buffer.getLength(); i++) {
      if(!tuples.next()) {
        return i;
      }
      for (int j = 0; j < buffer.getWidth(); j++) {
        buffer.set(i, j, tuples.getColumnValue(j));
        if (buffer.get(i, j) == Tuples.UNBOUND) {
          this.columnEverUnbound[j] = true;
        }
      }
    }

    return buffer.getLength();
  }


  private void restoreHeap() throws TuplesException {
    migrateEmptyHead();

    CacheLine headCache = heapCache[0];
    headTuple = headCache.getCurrentTuple(headTuple);
    
    for (int i = 0; i < heapCache.length - 1; i++) {
      CacheLine nextCache = heapCache[i + 1];
      if (nextCache.isEmpty()) {
        break;
      }

      nextTuple = nextCache.getCurrentTuple(nextTuple);
      if (!headCache.isEmpty() && comparator.compare(headTuple, nextTuple) < 0) {
        break;
      }

      heapCache[i] = nextCache;
      heapCache[i + 1] = headCache;
    }
  }

  
  /*
   * Used for debugging cache.
   */
  @SuppressWarnings("unused")
  private void checkHeapIds(String marker) throws TuplesException {
    Set<CacheLine> lines = new HashSet<CacheLine>();
    Set<Integer> dups = new HashSet<Integer>();

    for (int i = 0; i < heapCache.length; i++) {
      if (lines.contains(heapCache[i])) {
        dups.add(new Integer(System.identityHashCode(heapCache[i])));
      } else {
        lines.add(heapCache[i]);
      }
    }

    if (dups.size() > 0) {
      logger.error(marker + ": Aliasing in heapCache: " + formatHeapIds());
      logger.error(marker + ":     duplicates : " + dups);
      throw new TuplesException("Invalid heap.  Aliasing found(" + dups + ")");
    }
  }


  private String formatHeapIds() {
    StringBuffer buff = new StringBuffer();

    buff.append("Heap[" + heapCache.length + "] : [");
    for (int i = 0; i < heapCache.length; i++) {
      buff.append(" " + System.identityHashCode(heapCache[i]));
    }
    buff.append(" ]");

    return "Heap = " + buff;
  }


  private void migrateEmptyHead() throws TuplesException {
    CacheLine headCache = heapCache[0];

    if (headCache.isEmpty()) {
      for (int i = 1; i < heapCache.length; i++) {
        if (!heapCache[i].isEmpty()) {
          heapCache[i - 1] = heapCache[i];
        } else {
          heapCache[i - 1] = headCache;
          return;
        }
      }
      heapCache[heapCache.length - 1] = headCache;
    }
  }

  @SuppressWarnings("unused")
  private void dumpCacheStatus(String marker) {
    StringBuffer buff = new StringBuffer(marker + ": CacheStatus: [");
    for (int i = 0; i < heapCache.length; i++) {
      buff.append(" " + heapCache[i].isEmpty());
    }
    buff.append(" ]");
    logger.warn(buff.toString());
  }

  @SuppressWarnings("unused")
  private void debugHeapStatus() {
    if (heapCache[0].isEmpty()) {
      logger.debug("Head is still empty after restoreHeap");
      for (int i = 0; i < heapCache.length; i++) {
        if (heapCache[i] != null && !heapCache[i].isEmpty()) {
          logger.debug("RestoreHeap failed with entry " + i);
        }
      }
    }
  }


  private void sortHeap() throws TuplesException {
    int emptyCacheLineIndex = sortEmptyLines(heapCache, 0, heapCache.length - 1);
    qsort(heapCache, 0, emptyCacheLineIndex);
  }

  /**
   * Sorts all empty cache lines to end of cache. 
   * @param cache An array of cache lines, function will sort a subrange of this array.
   * @param start index of first cache line in range to sort.
   * @param end index of last cache line in range to sort.
   * @return Index of start of empty cache lines.
   */
  private int sortEmptyLines(CacheLine[] cache, int start, int end) {
    int lhs = start;
    int rhs = end;

    while (lhs < rhs) {
      if (cache[rhs].isEmpty()) {
        rhs--;
      } else if (cache[lhs].isEmpty()) {
        swap(cache, lhs, rhs);
        rhs--;
      } else {
        lhs++;
      }
    }

    if (rhs == start || rhs == end) {
      return cache[start].isEmpty() ? rhs : rhs + 1;
    } else {
      return rhs + 1;
    }
  }

  private void qsort(CacheLine[] cache, int start, int end)
      throws TuplesException {

    if (end - start < 2) {
      return;
    }

    int pivot = partition(cache, start, end);
    /* If pivot <= start + 1, then (start, pivot) form a sorted pair */
    if (pivot > (start + 1)) {
      qsort(cache, start, pivot);
    }
    /* Last element is end - 1; if pivot <= last - 1, then (pivot, last) form a sorted pair */
    if (pivot < (end - 2)) {
      qsort(cache, pivot + 1, end);
    }
  }


  private int partition(CacheLine[] cache, int start, int end)
      throws TuplesException {
    final int size = end - start;
    int pivot = (size / 2) + start;
    int lhs = start;
    int rhs = end - 1;

    for (;;) {
      pivotTuple = cache[pivot].getCurrentTuple(pivotTuple);
      while ((lhs < pivot)) {
        tempTuple = cache[lhs].getCurrentTuple(tempTuple);
        if (comparator.compare(pivotTuple, tempTuple) <= 0) {
          break;
        }
        lhs++;
      }
      while (rhs > pivot) {
        tempTuple = cache[rhs].getCurrentTuple(tempTuple);
        if (comparator.compare(pivotTuple, tempTuple) >= 0) {
          break;
        }
        rhs--;
      }

      if (lhs >= rhs) {
        return pivot;
      }

      swap(cache, lhs, rhs);

      if (lhs == pivot) {
        lhs++;
        pivot = rhs;
      } else if (rhs == pivot) {
        pivot = lhs;
        rhs--;
      } else {
        lhs++;
        rhs--;
      }
    }
  }


  private void swap(CacheLine[] cache, int left, int right) {
    CacheLine temp;

    temp = cache[left];
    cache[left] = cache[right];
    cache[right] = temp;
  }


  private void initialiseBlockFile() throws TuplesException {
    try {
      this.blockFile = AbstractBlockFile.openBlockFile(createTmpfile(), BLOCK_SIZE, BlockFile.IOType.AUTO);
      this.blockFileRefCount.refCount++;
    } catch (IOException ie) {
      logger.warn("Failed to open temporary block file.", ie);
      throw new TuplesException("Failed to open temporary block file.", ie);
    }
  }


  private File createTmpfile() throws TuplesException {
    try {
      File file = TempDir.createTempFile("tuples", LISTFILE_EXT);
      file.deleteOnExit();

      return file;
    } catch (IOException ie) {
      logger.warn("Failed to obtain tmpdir", ie);
      throw new TuplesException("Failed to obtain tmpdir", ie);
    }
  }

  protected class RefCount {
    public int refCount;
  }

  /**
   * Copied from AbstractTuples
   */
  public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
    return null;
  }
}

