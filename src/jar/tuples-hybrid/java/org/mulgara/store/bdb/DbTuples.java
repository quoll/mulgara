/*
 * Copyright 2010, Paul Gearon
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

package org.mulgara.store.bdb;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.apache.log4j.Logger;

import org.mulgara.query.Constraint;
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.tuples.Annotation;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.SimpleTuplesFormat;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.util.Constants;
import org.mulgara.util.StackTrace;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Represents a group of tuples, sorted and stored in a Berkeley DB.
 *
 * @created 2010-07-11
 * @author Paul Gearon
 */
public final class DbTuples implements Tuples {

  /** A map from DbTuples objects to their comparators */
  static private Map<Integer,RowComparator> comparators = new HashMap<Integer,RowComparator>();

  protected final Variable[] vars;
  protected final int width;
  protected final RowComparator comparator;
  protected final boolean unconstrained;
  protected final boolean duplicates;
  protected final boolean[] columnEverUnbound;
  protected Tuples tuples;

  // Would be final except for clone
  // can be final once clone semantics migrated into CacheLine.
  protected boolean beforeFirstCalled;
  protected boolean nextCalled;

  /** Used to indicate that beforeFirst has set the first record to be returned from next */
  protected boolean initialRecord = false;
  /** Indicates that beforeFirst found data to be returned */
  protected boolean initialStatus = false;

  /** The prefix used in the most recent beforeFirst */
  protected long[] currentPrefix;

  /** The database used to represent these tuples */
  protected Database database;
  /** The reference count to the database */
  private int refCount = 0;

  /** The internal cursor over the BDB table */
  protected com.sleepycat.je.Cursor cursor;
  /** The data to be read from the BDB table */
  protected DatabaseEntry line = new DatabaseEntry();
  /** The empty value object that is required for reading, but will always be empty. */
  protected DatabaseEntry emptyVal = new DatabaseEntry();

  /** The buffer containing the data from the current record of the tuples. */
  protected LongBuffer tupleLine;

  protected int[] varLookupList;
  private boolean closed = false;

  // Debugging.
  private final static Logger logger = Logger.getLogger(DbTuples.class);
  private StackTrace allocatedBy;
  private StackTrace closedBy;

  /**
   * Constructs a tuples that represents another tuples with a given ordering.
   * @param tuples The original tuples to represent.
   * @param comparator The definition of the new ordering.
   * @throws TuplesException If there is a problem accessing the original tuples.
   */
  protected DbTuples(Tuples tuples, RowComparator comparator) throws TuplesException {

    if (logger.isDebugEnabled()) {
      logger.debug("DbTuples created " + System.identityHashCode(this));
    }

    // store the comparator, and map to it by the current identifier
    this.comparator = comparator;
    comparators.put(System.identityHashCode(this), comparator);

    // get some of tuples structure from the original tuples
    this.vars = tuples.getVariables();
    this.unconstrained = tuples.isUnconstrained();
    this.duplicates = !tuples.hasNoDuplicates();

    // Create a lookup up list of unique variables to their position in an index.
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
    
    // put everything into a BDB
    database = createTempDb();
    refCount++;  // created the first reference to database
    materialiseTuples(tuples, database);

    // set up some state on cursors
    this.beforeFirstCalled = false;
    this.nextCalled = false;

    // Set up details for debugging, if necessary
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
    if (!beforeFirstCalled) {
      logger.error("next() called before beforeFirst()");
      throw new TuplesException("next() called before beforeFirst()");
    }

    boolean status;
    if (initialRecord) {
      // beforeFirst already read the record, so just extract it and test it
      initialRecord = false;
      if (!initialStatus) status = false;
      else {
        tupleLine = ByteBuffer.wrap(line.getData()).asLongBuffer();
        status = testLine(tupleLine, currentPrefix);
      }
    } else {
      // subsequent record, so move to the next and test it
      status = cursor.getNext(line, emptyVal, LockMode.DEFAULT) == OperationStatus.SUCCESS;
      if (status) {
        // extract the current record
        tupleLine = ByteBuffer.wrap(line.getData()).asLongBuffer();
        // check to see if the cursor has stepped past the matching data
        status = testLine(tupleLine, currentPrefix);
      }
    }
    nextCalled = true;
    return status;
  }


  /**
   * Required by Tuples.
   */
  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    assert prefix != null;

    if (suffixTruncation != 0) {
      logger.error("DbTuples.beforeFirst(suffix) unimplemented");
      throw new IllegalArgumentException("DbTuples.beforeFirst(suffix) unimplemented");
    }
 
    // copy the prefix so we know when we've left the search range
    currentPrefix = new long[prefix.length];
    System.arraycopy(prefix, 0, currentPrefix, 0, prefix.length);

    // get a new cursor
    if (cursor == null) cursor = database.openCursor(null, null);

    // construct the buffer that cursor will search on
    ByteBuffer bb = ByteBuffer.allocate(width * Constants.SIZEOF_LONG);
    LongBuffer lb = bb.asLongBuffer();
    for (int i = 0; i < prefix.length; i++) lb.put(i, prefix[i]);
    line.setData(bb.array());
    initialStatus = cursor.getSearchKey(line, emptyVal, LockMode.DEFAULT) == OperationStatus.SUCCESS;

    // remember state
    initialRecord = true;
    beforeFirstCalled = true;
    nextCalled = false;
  }


  /**
   * Required by Tuples.
   */
  public long getColumnValue(int column) throws TuplesException {
    if (column < 0 || column >= width) {
      throw new TuplesException("No column " + column + " in " + Arrays.asList(vars));
    }

    if (!nextCalled) throw new TuplesException("getColumnValue() called before next()");

    return tupleLine.get(varLookupList[column]);
  }


  /**
   * Required by Tuples.
   */
  public void renameVariables(Constraint constraint) {
    for (int i = 0; i < vars.length; i++) {
      Variable v = vars[i];
      boolean found = false;
      for (int j = 0; j < 4; j++) {
        // v will be a reference to one of the objects in Graph.VARIABLES[]
        if (v == StatementStore.VARIABLES[j]) {
          // The array obtained from getVariables() is modifiable.
          vars[i] = (Variable)constraint.getElement(j);
          found = true;
          break;
        }
      }
      if (!found) {
        throw new Error("Unexpected variable: " + v);
      }
    }
  }


  /**
   * Required by Tuples.
   */
  public Object clone() {
    try {
      DbTuples copy = (DbTuples)super.clone();
      // we now have a new reference to database
      refCount++;
      if (logger.isDebugEnabled()) copy.allocatedBy = new StackTrace();
      copy.tuples = (Tuples)tuples.clone();
      copy.currentPrefix = currentPrefix == null ? null : currentPrefix.clone();
      copy.line = new DatabaseEntry(line.getData());
      if (tupleLine != null ) {
        tupleLine.position(0);
        copy.tupleLine = ByteBuffer.allocate(width * Constants.SIZEOF_LONG)
                         .asLongBuffer().put(tupleLine);
      }

      return copy;
    } catch (CloneNotSupportedException ce) {
      throw new RuntimeException("DbTuples.clone() threw CloneNotSupported", ce);
    }
  }


  /**
   * This could fall back to beforeFirst(Tuples.NO_PREFIX), but this is more efficient
   */
  public void beforeFirst() throws TuplesException {
    currentPrefix = Tuples.NO_PREFIX;

    // get a new cursor
    if (cursor == null) cursor = database.openCursor(null, null);

    initialStatus = cursor.getFirst(line, emptyVal, LockMode.DEFAULT) == OperationStatus.SUCCESS;

    // remember state
    initialRecord = true;
    beforeFirstCalled = true;
    nextCalled = false;
  }


  /** @see org.mulgara.store.tuples.Tuples#getOperands() */
  public List<Tuples> getOperands() {
    return Collections.singletonList(tuples);
  }

  /**
   * Required by Cursor.
   */
  public void close() throws TuplesException {
    if (closed) {
      if (logger.isDebugEnabled()) {
        logger.debug("Attempt to close DbTuples twice; first closed: " + closedBy);
        logger.debug("Attempt to close DbTuples twice; second closed: " + new StackTrace());
        logger.debug("    allocated: " + allocatedBy);
      } else {
        logger.error("Attempt to close HybridTuples twice. Enable debug to trace how.");
      }
      throw new TuplesException("Attempted to close HybribTuples more than once");
    }
    closed = true;
    if (logger.isDebugEnabled()) closedBy = new StackTrace();

    comparators.remove(System.identityHashCode(this));
    try {
      tuples.close();
      tuples = null;
    } finally {
      try {
        if (cursor != null) {
          cursor.close();
          cursor = null;
        }
      } finally {
        assert refCount > 0 : "Released all BDB reference counts before closing Tuples";
        refCount--;
        if (refCount == 0 && database != null) {
          database.close();
          database = null;
        }
      }
    }
  }


  /** {@inheritDoc} */
  public int getColumnIndex(Variable variable) throws TuplesException {
    for (int c = 0; c < vars.length; c++) if (vars[c].equals(variable)) return c;

    throw new TuplesException("Variable not found: " + variable);
  }


  /** {@inheritDoc} */
  public long getRawColumnValue(int column) throws TuplesException {
    return UNBOUND;
  }


  /** {@inheritDoc} */
  public int getNumberOfVariables() {
    return vars != null ? vars.length : 0;
  }


  /** {@inheritDoc} */
  public Variable[] getVariables() {
    return vars;
  }


  /** {@inheritDoc} */
  public boolean isUnconstrained() throws TuplesException {
    return unconstrained;
  }


  /** {@inheritDoc} */
  public long getRowCount() throws TuplesException {
    return database.count();
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
    return getRowCount() == 0;
  }


  /** {@inheritDoc} */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    try {
      return columnEverUnbound[column];
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new TuplesException("No such column "+column);
    }
  }


  /** {@inheritDoc} */
  public boolean isMaterialized() {
    return true;
  }


  /** {@inheritDoc} */
  public boolean hasNoDuplicates() {
    return !duplicates;
  }


  /** {@inheritDoc} */
  public RowComparator getComparator() {
    return comparator;
  }

  /** Get the comparator for the given DbTuples ID */
  public static RowComparator getCmp(int id) {
    return comparators.get(id);
  }

  /** {@inheritDoc} */
  public boolean equals(Object o) {
    Tuples t;
    Tuples c;

    if (o == this) return true;

    if (!(o instanceof Tuples)) return false;

    t = (Tuples)o;
    if (t instanceof DbTuples) return this.database == ((DbTuples)t).database;

    try {
      Variable[] tvars = t.getVariables();
      if (this.getRowCount() != t.getRowCount() || this.vars.length != tvars.length) {
        return false;
      }

      for (int v = 0; v < this.width; v++) {
        if (!this.vars[v].equals(tvars[v])) return false;
      }

      t = (Tuples)t.clone();
      c = (Tuples)this.clone();

      try {
        t.beforeFirst();
        c.beforeFirst();

        while(true) {
          boolean tn = t.next();
          boolean cn = c.next();
          if (!tn && !cn) return true;
          if ((!tn && cn) || (tn && !cn)) return false;
          for (int i = 0; i < width; i++) {
            if (t.getColumnValue(i) != c.getColumnValue(i)) return false;
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

  /** {@inheritDoc} */
  public int hashCode() {
    long rowcount;
    try {
      rowcount = getRowCount();
    } catch (TuplesException e) { rowcount = 0; }
    return getVariables().hashCode() * 3 + (int)(rowcount & 0xFFFFFFFF) * 7 +
           (int)(rowcount >> 32) * 11;
  }

  /** {@inheritDoc} */
  public String toString() {
    return SimpleTuplesFormat.format(this);
  }


  /**
   * Load all of the tuples into the local database.
   * @param tuples The tuples to load.
   * @param db The database to insert into.
   * @return The number of insertions made. This will be the row count if duplicates are allowed.
   * @throws TuplesException Due to an error reading from the tuples.
   */
  private long materialiseTuples(Tuples tuples, Database db) throws TuplesException {
    tuples.beforeFirst();

    ByteBuffer bb = ByteBuffer.allocate(width * Constants.SIZEOF_LONG);
    LongBuffer lb = bb.asLongBuffer();
    byte[] array = bb.array();
    DatabaseEntry key = new DatabaseEntry(array);

    byte[] valArray;
    LongBuffer valLB = null;
    if (duplicates) {
      ByteBuffer valB = ByteBuffer.allocate(Constants.SIZEOF_LONG);
      valLB = valB.asLongBuffer();
      valArray = valB.array();
    } else {
      valArray = new byte[0];
    }
    // The ignored value. If we can have duplicates, then make this an incrementing number.
    DatabaseEntry emptyVal = new DatabaseEntry(valArray);

    long count = 0;
    while (tuples.next()) {
      for (int i = 0; i < width; i++) lb.put(i, tuples.getColumnValue(i));
      if (duplicates) valLB.put(0, count);
      db.put(null, key, emptyVal);
      count++;
    }
    return count;
  }

  private Database createTempDb() throws TuplesException {
    Environment env = DbEnvironment.getEnv();
    try {
      DatabaseConfig dbCfg = new DatabaseConfig();
      dbCfg.setAllowCreate(true);
      dbCfg.setTemporary(true);
      dbCfg.setSortedDuplicates(duplicates);
      int id = System.identityHashCode(this);
      dbCfg.setBtreeComparator(new DbComparator(id, comparator));
      return env.openDatabase(null, "tuples_" + id, dbCfg);
    } catch (DatabaseException dbe) {
      throw new TuplesException("Error creating BDB database: " + dbe.getMessage());
    }

  }

  /**
   * Tests if a long buffer matches a long array, starting at the end of the array
   * and working backwards. Only the length of the array is tested.
   * @param l The buffer to compare.
   * @param p The array to compare. This also sets the comparison length.
   * @return <code>true</code> if all the elements of the array match the corresponding
   *         elements in the buffer.
   */
  private static final boolean testLine(LongBuffer l, long[] p) {
    for (int i = p.length - 1; i >= 0; i--) {
      if (l.get(i) != p[i]) return false;
    }
    return true;
  }


  /** Copied from AbstractTuples */
  public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
    return null;
  }


  public static class DbComparator implements Comparator<byte[]>, Serializable {

    /** The serialization ID */
    private static final long serialVersionUID = -2986622291706235593L;

    /** An internal identifier */
    private final Integer id;

    /** The wrapped RowComparator that does the actual work */
    private transient RowComparator rc;

    /**
     * Creates a comparator, based on a row comparator
     * @param rc The comparator to do the real work.
     */
    public DbComparator(int id, RowComparator rc) {
      this.id = id;
      this.rc = rc;
    }

    /** @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) */
    public int compare(byte[] o1, byte[] o2) {
      try {
        LongBuffer lb1 = ByteBuffer.wrap(o1).asLongBuffer();
        LongBuffer lb2 = ByteBuffer.wrap(o2).asLongBuffer();
        long[] l1 = new long[lb1.capacity()];
        long[] l2 = new long[lb2.capacity()];
        lb1.get(l1);
        lb2.get(l2);
        return rc.compare(l1, l2);
      } catch (TuplesException e) {
        throw new RuntimeException("Error reading values for comparison in the BDB table", e);
      }
    }

    /** The identifier assigned to this comparator */
    public Integer getId() {
      return id;
    }

    /**
     * After reading back this object, find the row comparator that was assigned for it.
     * @param in The input stream to get the object from.
     * @throws IOException Error reading from the stream.
     * @throws ClassNotFoundException If the class in the stream is not in the classpath.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      rc = getCmp(id);
    }

  }

}

