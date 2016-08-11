/*
 * Copyright 2011 Paula Gearon.
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
package org.mulgara.util.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import static org.mulgara.util.io.Bytes.*;

/**
 * A file-based hashmap.
 * This class maps {@link java.nio.ByteBuffer}s of fixed length to {@link java.nio.ByteBuffer}s
 * of fixed length. To map another data type, wrap this class in a class that converts
 * keys and values into ByteBuffers.
 * For an example of mapping a fixed data type, see {@link LLHashMap}, which maps Long to Long.
 * 
 * Mapping non-fixed types can be done using a custom {@link #hashCode(Object)} function for hashing,
 * and a custom {@link #equalsKey(ByteBuffer, ByteBuffer)} method. These are protected methods that
 * can be overridden in a subclass.
 */
public class FileHashMap implements Map<ByteBuffer,ByteBuffer>, Closeable {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(FileHashMap.class);

  /** A list of prime number sizes, increasing roughly by double. */
  private static final long[] PRIMES = new long[] { 37L, 67L, 131L, 257L, 521L, 1031L, 2053L, 4099L,
       8209L, 16411L, 32771L, 65537L, 131101L, 262147L, 524309L, 1048583L, 2097169L, 4194319L,
       8388617L, 16777259L, 33554467L, 67108879L, 134217757L, 268435459L, 536870923L, 1073741827L,
       2147483659L, 4294967311L, 8589934609L, 17179869209L, 34359738421L, 68719476767L, 137438953481L,
       274877906951L, 549755813911L, 1099511627791L, 2199023255579L, 4398046511119L, 8796093022237L,
       17592186044423L, 35184372088891L, 70368744177679L, 140737488355333L, 281474976710677L,
       // from here on, all the primes are probable
       562949953421381L, 1125899906842679L, 2251799813685269L, 4503599627370517L,
       9007199254740997L, 18014398509482143L, 36028797018963971L };

  /** The default load factor, of nothing else is specified. */
  public static final float DEFAULT_LOAD_FACTOR = 0.75f;

  /** The file path for the main file. */
  private final File path;

  /** The file containing all the records. */
  private final RecordFile file;

  /** The metadata of the hash table. */
  private final MetaData md;

  /** The prime index of the current size to use. */
  private int currentIndex = 0;

  /** Total size of the file in records. fileRecords == PRIMES[currentSize] */
  private long fileRecords = 0;

  /** The size of the keys in the table. */
  private final int keySize;

  /** The size of the values in the table. */
  private final int valueSize;

  /** The size of the records in table. */
  private final int recordSize;

  /** The number of entries in this table. */
  private long entries;

  /** The max load in the table */
  private final float loadFactor;

  /** An empty value byte buffer */
  private final ByteBuffer empty;

  /** An empty key byte buffer */
  private final ByteBuffer emptyKey;

  /** An byte buffer representing an all 0 key */
  private final ByteBuffer zeroKey;

  /**
   * Constructor with the default load factor.
   * @param f The file to create.
   * @param keySize The size of a serialized key, in bytes.
   * @param valueSize The size of a serialized value, in bytes.
   * @throws IOException Caused by a file error.
   */
  public FileHashMap(File f, int keySize, int valueSize) throws IOException {
    this(f, keySize, valueSize, DEFAULT_LOAD_FACTOR, 0);
  }

  /**
   * Main constructor.
   * @param f The file to create.
   * @param keySize The size of a serialized key, in bytes.
   * @param valueSize The size of a serialized value, in bytes.
   * @param loadFactor The maximum load factor for the hash table.
   * @param initialSize The initial number of entries to build capacity for. This is a hint
   *        to help avoid unnecessary rehashing.
   * @throws IOException Caused by a file error.
   */
  public FileHashMap(File f, int keySize, int valueSize, float loadFactor, long initialSize) throws IOException {
    if (loadFactor <= 0f || loadFactor >= 1f) throw new IllegalArgumentException("Load factor must be between 0 and 1");
    path = f;
    boolean create = !f.exists();
    md = new MetaData(f);
    md.test(create, keySize, valueSize, loadFactor, f.length());
    if (create) {
      md.setKeySize(this.keySize = keySize);
      md.setValueSize(this.valueSize = valueSize);
      md.setLoadFactor(this.loadFactor = loadFactor);
      md.setPrimesIndex(currentIndex = indexOfNextSize(initialSize, loadFactor));
      md.setEntries(entries = 0L);
    } else {
      this.keySize = md.getKeySize();
      this.valueSize = md.getValueSize();
      this.loadFactor = md.getLoadFactor();
      currentIndex = md.getPrimesIndex();
      entries = md.getEntries();
    }
    recordSize = keySize + valueSize;
    fileRecords = PRIMES[currentIndex];
    file = new RecordFileImpl(f, recordSize, fileRecords);
    if (create) file.resize(fileRecords);

    empty = IOUtil.allocate(valueSize).asReadOnlyBuffer();   // all zeros
    emptyKey = IOUtil.allocate(keySize).asReadOnlyBuffer();  // all zeros
    ByteBuffer tmpZ = IOUtil.allocate(keySize);   // all ones
    for (int i = 0; i < keySize; i++) tmpZ.put(i, (byte)0xFF);
    zeroKey = tmpZ.asReadOnlyBuffer();
  }

  /**
   * @see java.io.Closeable#close()
   */
  public void close() throws IOException {
    file.close();
    md.close();
  }

  /**
   * @see java.io.Closeable#close()
   */
  public void closeAndDelete() throws IOException {
    file.close();
    path.delete();
    md.closeAndDelete();
  }

  /**
   * @see java.util.Map#clear()
   */
  @Override
  public void clear() {
    byte[] empty = new byte[recordSize];
    try {
      entries = 0L;
      md.setEntries(entries);
      setFileRecords(0);
      for (long i = 0; i < fileRecords; i++) {
        ByteBuffer b = file.getBuffer(i);
        b.clear();
        b.put(empty);
        file.put(b, i);
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  @Override
  public boolean containsKey(Object k) {
    if (k == null) return false;
    ByteBuffer key = sanitizeKey((ByteBuffer)k);
    try {
      long startPos = recordPosition(key);
      long pos = startPos;
      ByteBuffer record = file.getBuffer(pos);
      // search for the next empty position. Sanity guard against wraparound.
      while (!emptyKey(record) && pos != startPos - 1) {
        if (equalsKey(record, key)) return true;
        pos = incPos(pos);
      }
      return false;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This operation is inappropriate for most applications.
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  @Override
  public boolean containsValue(Object v) {
    if (v == null) return false;
    ByteBuffer value = (ByteBuffer)v;
    try {
      long entryCount = 0;
      for (long r = 0; r < PRIMES[currentIndex]; r++) {
        ByteBuffer record = file.get(r);
        if (!emptyKey(record)) {
          entryCount++;
          record.position(keySize);
          if (value.equals(record.slice())) return true;
        }
        if (entryCount >= entries) break;
      }
      return false;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see java.util.Map#entrySet()
   */
  @Override
  public Set<Map.Entry<ByteBuffer,ByteBuffer>> entrySet() {
    return new DataSet<Map.Entry<ByteBuffer,ByteBuffer>>(new EntryReader());
  }

  /**
   * @see java.util.Map#keySet()
   */
  @Override
  public Set<ByteBuffer> keySet() {
    return new DataSet<ByteBuffer>(new KeyReader());
  }

  /**
   * @see java.util.Map#values()
   */
  @Override
  public Collection<ByteBuffer> values() {
    return new DataSet<ByteBuffer>(new ValueReader());
  }

  /**
   * @see java.util.Map#remove(java.lang.Object)
   */
  @Override
  public ByteBuffer remove(Object k) {
    if (k == null) return null;
    ByteBuffer key = sanitizeKey((ByteBuffer)k);
    try {
      // get the data to be removed
      long removePos = recordPosition(key);
      ByteBuffer removeRecord = file.getBuffer(removePos);
      if (equalsKey(removeRecord, key)) {
        removeRecord.position(keySize);
        ByteBuffer value = copy(removeRecord.slice());
        removeFromPos(removePos, removeRecord);
        md.setEntries(--entries);
        return value;
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isEmpty() {
    return entries == 0L;
  }

  /**
   * @see java.util.Map#putAll(java.util.Map)
   */
  @Override
  public void putAll(Map<? extends ByteBuffer, ? extends ByteBuffer> src) {
    for (Map.Entry<? extends ByteBuffer, ? extends ByteBuffer> e: src.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  /**
   * This will return an incorrect number if the number of entries is larger than Integer.MAX_VALUE
   * @see java.util.Map#size()
   */
  @Override
  public int size() {
    return entries > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)entries;
  }

  /**
   * Returns the actual size, which may be larger than {@link Integer#MAX_VALUE}
   * @return The complete number of entries.
   */
  public long realSize() {
    return entries;
  }

  /**
   * @see java.util.Map#get(java.lang.Object)
   */
  @Override
  public ByteBuffer get(Object k) {
    if (k == null) return null;
    ByteBuffer key = sanitizeKey((ByteBuffer)k);
    try {
      long startPos = recordPosition(key);
      long pos = startPos;
      ByteBuffer record = file.getBuffer(pos);
      // search for the next empty position. Sanity guard against wraparound.
      while (!emptyKey(record) && pos != startPos - 1) {
        if (equalsKey(record, key)) {
          record.position(keySize);
          return record.slice().asReadOnlyBuffer();
        }
        pos = incPos(pos);
        record = file.getBuffer(pos);
      }
      // not found. Return null
      return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   * key and value may not be null as these cannot be round tripped. The best we could do
   * would be to use an all zero buffer, but that's not the same thing.
   */
  @Override
  public ByteBuffer put(ByteBuffer key, ByteBuffer value) {
    if (key == null) throw new IllegalArgumentException("Null keys not allowed");
    if (value == null) throw new IllegalArgumentException("Null values not allowed");
    if (loadFactor() > loadFactor) {
      try {
        rehash();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return put(key, value);
    }

    key = sanitizeKey(key);

    // find the right space
    try {
      ByteBuffer fileData;
      boolean found = false;
      // start just before the location, so can increment immediately
      long startRec = recordPosition(key) - 1;
      long rec = startRec;
      do {
        rec = incPos(rec);
        assert rec != startRec;
        fileData = file.get(rec);
      } while (!emptyKey(fileData) && !(found = equalsKey(fileData, key)));

      if (found) {
        // get the value buffer
        fileData.position(keySize);
        ByteBuffer valueData = fileData.slice();
        // copy the old value out
        byte[] resultData = new byte[valueSize];
        valueData.get(resultData);
        // write the new value into the buffer
        valueData.position(0);
        valueData.put(value);
        // flush the record buffer back to disk
        file.put(fileData, rec);
        return ByteBuffer.wrap(resultData);
      }

      // write the key and value to the record
      key.position(0);
      value.position(0);
      fileData.put(key);
      fileData.put(value);
      file.put(fileData, rec);
      md.setEntries(++entries);
      empty.position(0);
      return empty.duplicate();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Remove an entire record from a position, shifting anything eligible back to take its place.
   * @param removePos The position to remove from.
   * @param removeBuffer a buffer associated with removePos.
   * @throws IOException Due to an error accessing the file.
   */
  private void removeFromPos(long removePos, ByteBuffer removeBuffer) throws IOException {
    // look for things that can come back to this position
    long position = removePos;
    ByteBuffer scanRecord;
    // look until an empty space is found
    while (!emptyKey(scanRecord = file.get(position = incPos(position)))) {
      scanRecord.limit(keySize);
      long scanPos = recordPosition(scanRecord.slice());
      if (scanPos <= removePos) {
        // eligible to move back
        removeBuffer.position(0);
        scanRecord.clear();
        removeBuffer.put(scanRecord);
        file.put(removeBuffer, removePos);
        // now remove the scanRecord and move anything elegible back into it
        removeFromPos(position, scanRecord);
        return;
      }
    }
    // Nothing was moved back here, so clear it out
    empty.position(0);
    removeBuffer.position(0);
    removeBuffer.put(empty);
    file.put(removeBuffer, removePos);
  }

  /**
   * Increment a record position.
   * @param p The record position to increment.
   * @return The incremented position.
   */
  private long incPos(long p) {
    if (++p == fileRecords) p = 0;
    return p;
  }

  /**
   * Expand the file and put all existing data into its new position.
   * @throws IOException If there is an error modifying the file.
   */
  private void rehash() throws IOException {
    long oldRecordCount = fileRecords;
    setFileRecords(currentIndex + 1);
    long movedRecords = 0;
    // check all existing records
    for (long recPos = 0; recPos < oldRecordCount; recPos++) {
      if (movedRecords == entries) break;
      // get the record and the key from the record
      ByteBuffer record = file.get(recPos);
      record.limit(keySize);
      ByteBuffer key = record.slice();
      // move the non-empty records
      if (!emptyKey(key)) {
        // determine the new location
        long newPos = recordPosition(key);
        // move the data to that location
        if (recPos != newPos) {
          newPos = moveTo(record, recPos, newPos, recPos);
          // after moving, clear the source
          if (recPos != newPos) {
            record.clear();
            emptyKey.position(0);
            record.put(emptyKey);
            file.put(record, recPos);
          }
        }
      }
    }
  }

  /**
   * Move a buffer from a position to a new location, or the first available location after.
   * @param record The buffer contents to be moved.
   * @param srcPos The location of the source buffer.
   * @param toPos The initial location to move to.
   * @param processedRecords The number of records already processed. 1 past the offset of the last processed record.
   * @return The final location the buffer was moved to.
   */
  private long moveTo(ByteBuffer record, long srcPos, long toPos, long processedRecords) throws IOException {
    assert srcPos != toPos;

    // move forward through the records until we identify one that is available
    Availability avail;
    ByteBuffer destRecord = file.get(toPos);
    while (AvailState.OCCUPIED == (avail = availability(destRecord, toPos, processedRecords)).state) {
      toPos = incPos(toPos);
      // if we wrapped all the way around (eg. first element tries to write to occupied tail) then exit
      if (toPos == srcPos) return toPos;
      destRecord = file.get(toPos);
    }

    // if destination is waiting to move, then move it out of the way
    if (avail.state == AvailState.WAITING) {
      moveTo(destRecord, toPos, avail.pos, processedRecords);
    }

    // destination now clear, write to it
    destRecord.clear();
    record.clear();
    destRecord.put(record);
    file.put(destRecord, toPos);

    return toPos;
  }

  /**
   * Tests if a record location is available.
   * A record is available if it is empty, or the data in it has not yet been moved in a rehash.
   * @param record The record to check.
   * @param recPos The current location of the record.
   * @param processedRecords The number of records already processed. 1 past the offset of the last processed record.
   * @return An Availability, containing a state of EMPTY, OCCUPIED or WAITING. The WAITING state
   *         will also contain the new location the record is to move to.
   */
  private Availability availability(ByteBuffer record, long recPos, long processedRecords) throws IOException {
    // check for an empty cell
    if (emptyKey(record)) return Availability.A_EMPTY;

    // if record is in the already-processed area, or in the new area, then it was placed there during this rehash
    if (recPos < processedRecords || recPos >= PRIMES[currentIndex - 1]) {
      return Availability.A_OCCUPIED;
    }

    // compare the calculated position to the current position
    record.limit(keySize);
    long newPos = recordPosition(record.slice());
    if (newPos == recPos) return Availability.A_OCCUPIED;  // record is supposed to be here

    // record might still be in the right place, but he have to check

    // if record is before its desired location, it hasn't been moved yet
    if (recPos < newPos) return new Availability(AvailState.WAITING, newPos);

    // record is in unprocessed area. Search from desired space forward to the current space.
    // an empty record means it wasn't supposed to be here.
    long rec = newPos;
    while (rec != recPos) {
      ByteBuffer searchBuffer = file.get(rec);
      if (emptyKey(searchBuffer)) {
        // this record is waiting to be moved to newPos
        return new Availability(AvailState.WAITING, newPos); 
      }
      rec = incPos(rec);
    }
    // unbroken line of records to the current space, so the record is already where it needs to be
    return Availability.A_OCCUPIED;
  }

  /** Enumeration of the possible record states during rehashing */
  private enum AvailState {
    EMPTY,    // nothing here, available to store data in
    OCCUPIED, // something already at the hoped-for location
    WAITING   // something at the hoped-for location, but it is yet to be moved
  };

  /** A class for encoding availability state of a record */
  private static class Availability {
    public static final Availability A_EMPTY = new Availability(AvailState.EMPTY, 0);
    public static final Availability A_OCCUPIED = new Availability(AvailState.OCCUPIED, 0);
    final AvailState state;
    final long pos;
    Availability(AvailState s, long p) {
      state = s;
      pos = p;
    }
  }

  /**
   * Determines the load factor.
   * @return The current load factor.
   */
  private final float loadFactor() {
    return (float)((double)entries / fileRecords);
  }

  /**
   * Since all zeros in the file means there is no entry, we flip keys with all zeros to all ones. 
   * @param k The original key.
   * @return If k was all zeros, then a buffer with all ones. Else the original k.
   */
  private final ByteBuffer sanitizeKey(ByteBuffer k) {
    boolean allF = true;
    for (int i = 0; i < k.capacity(); i++) {
      byte v = k.get(i);
      if (allF && v != 0xFF) allF = false;
      if (v != 0) {  // non-zero means key is probably normal
        if (!allF) return k;  // so long as not EVERY bype is 0xFF
        // continue the search for all bytes of 0xFF
        for (i++; i < k.capacity(); i++) {
          if (k.get(i) != 0xFF) return k; // a normal key
        }
        throw new RuntimeException("Cannot accept a key of -1");
      }
    }
    return zeroKey.duplicate();
  }

  /**
   * If all the bits are one, then return the empty key. 
   * @param k The original key.
   * @return If k was all ones, then a buffer with all zeros. Else the original k.
   */
  private final ByteBuffer desanitizeKey(ByteBuffer k) {
    for (int i = 0; i < k.capacity(); i++) {
      if (k.get(i) != 0xFF) return k;
    }
    emptyKey.position(0);
    return emptyKey.duplicate();
  }

  /**
   * Tests if the key in a record is empty.
   * @param kb The data containing the key.
   * @return <code>true</code> iff the record is empty.
   */
  private final boolean emptyKey(ByteBuffer kb) {
    for (int i = keySize - 1; i >= 0; i--) {
      if (kb.get(i) != (byte)0) return false;
    }
    return true;
  }

  /**
   * Tests if a file buffer contains a given key.
   * Note that the file buffer contains the entire record (key+value).
   * Override this to expand key equality test.
   * @param filekb The byte buffer of the key.
   * @param key The key to look for.
   * @return <code>true</code> iff the buffer contains the key.
   */
  protected final boolean equalsKey(ByteBuffer filekb, ByteBuffer key) {
    // compare remaining bytes, if any
    for (int i = keySize - 1; i >= 0; i--) {
      if (filekb.get(i) != key.get(i)) return false;
    }
    return true;
  }
  
  /**
   * Sets the number of records to be used by this file, and set the file to use that size.
   * @param primeIndex The new index into the prime numbers to use.
   * @return The number of records in the new file.
   */
  private final long setFileRecords(int primeIndex) throws IOException {
    currentIndex = primeIndex;
    md.setPrimesIndex(currentIndex);
    fileRecords = PRIMES[currentIndex];
    file.resize(fileRecords);
    return fileRecords;
  }

  /**
   * Calculate the location for a record, based on its key.
   * @param key The ByteBuffer for the key.
   * @return The record number in the file that the key specifies.
   */
  private final long recordPosition(ByteBuffer key) {
    long h = IOUtil.longHash(key.hashCode());
    return h % (long)fileRecords;
  }

  /**
   * Returns the index of the first prime that is greater than or equal to the requested size.
   * @param s The requested size.
   * @return The index into PRIMES to use.
   */
  private static final int indexOfNextSize(long s, float loadFactor) {
    // look for the minimum request
    if (s == 0) return 0;
    // increase the required size to include the load factor
    long sz = (long)(s / (double)loadFactor);
    // sanity check on the size
    if (sz <= s) return PRIMES.length - 1;
    for (int i = 0; i < PRIMES.length; i++) {
      if (PRIMES[i] >= sz) return i;
    }
    return PRIMES.length - 1;
  }

  /**
   * Make a copy of a byte buffer.
   * @param bb The ByteBuffer to copy.
   * @return A new byte buffer with the same contents as bb.
   */
  private static final ByteBuffer copy(ByteBuffer bb) {
    ByteBuffer cp = IOUtil.allocate(bb.capacity());
    cp.put(bb);
    cp.position(0);
    return cp;
  }

  /**
   * An interface for generalizing data access for keys, values, and key/value pairs.
   * @param <D> The type of data being acccessed.
   */
  private interface DataReader<D> {
    /**
     * Test is data is available in the collection.
     * @param val The data to look for.
     * @return <code>true</code> iff the data is available.
     */
    public boolean contains(Object val);
    /**
     * Read out the appropriate data type.
     * @param record The location of the record with the data.
     * @return The relevant data from the record.
     */
    public D read(long record);
  }

  /**
   * A class for reading keys for the Key Set.
   */
  private class KeyReader implements DataReader<ByteBuffer> {
    public boolean contains(Object val) {
      return containsKey(val);
    }
    public ByteBuffer read(long recordId) {
      try {
        ByteBuffer data = file.get(recordId);
        data.limit(keySize);
        return desanitizeKey(data.slice()).asReadOnlyBuffer();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * A class for reading values for the Value collection.
   */
  private class ValueReader implements DataReader<ByteBuffer> {
    public boolean contains(Object val) {
      return containsValue(val);
    }
    public ByteBuffer read(long recordId) {
      try {
        ByteBuffer data = file.get(recordId);
        data.position(keySize);
        return data.slice().asReadOnlyBuffer();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /** A class for reading values for the Entry set. */
  private class EntryReader implements DataReader<Map.Entry<ByteBuffer,ByteBuffer>> {
    public boolean contains(Object val) {
      ByteBuffer b = (ByteBuffer)val;
      b.limit(keySize);
      ByteBuffer key = sanitizeKey(b.slice());
      try {
        long startPos = recordPosition(key);
        long pos = startPos;
        ByteBuffer record = file.getBuffer(pos);
        // search the valid keys
        while (!emptyKey(record) && pos != startPos - 1) {
          // find a matching key
          if (equalsKey(record, key)) {
            // check if the value is the same
            for (int i = keySize; i < recordSize; i++) {
              if (b.get(i) != record.get(i)) return false;
            }
            return true;
          }
          pos = incPos(pos);
        }
        return false;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    public Map.Entry<ByteBuffer,ByteBuffer> read(long recordId) {
      try {
        return new KeyValue(file.get(recordId), recordId);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /** A {@link Map.Entry} implementation for use with {@link FileHashMap#entrySet()}. */
  private class KeyValue implements Map.Entry<ByteBuffer, ByteBuffer> {
    private final ByteBuffer recordBuffer;
    private final ByteBuffer key;
    private final ByteBuffer value;
    private final long recordId;
    public KeyValue(ByteBuffer bb, long id) {
      recordBuffer = bb;
      recordId = id;
      bb.position(keySize);
      value = bb.slice();
      bb.position(0);
      bb.limit(keySize);
      key = desanitizeKey(bb.slice()).asReadOnlyBuffer();
    }
    public ByteBuffer getKey() { return key; }

    public ByteBuffer getValue() { return value; }

    public ByteBuffer setValue(ByteBuffer v) {
      byte[] old = new byte[valueSize];
      value.position(0);
      value.limit(valueSize);
      value.get(old);
      value.position(0);
      v.position(0);
      v.limit(valueSize);
      value.put(v);
      try {
        file.put(recordBuffer, recordId);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return ByteBuffer.wrap(old);
    }
  }

  class DataSet<T> implements Set<T> {

    private final DataReader<T> reader;

    public DataSet(DataReader<T> reader) {
      this.reader = reader;
    }

    public boolean add(T arg0) {
      throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends T> arg0) {
      throw new UnsupportedOperationException();
    }

    public void clear() {
      throw new UnsupportedOperationException();
    }

    public boolean contains(Object val) {
      return reader.contains(val);
    }

    public boolean containsAll(Collection<?> vals) {
      for (Object v: vals) {
        if (!contains(v)) return false;
      }
      return true;
    }

    public boolean isEmpty() {
      return FileHashMap.this.isEmpty();
    }

    public Iterator<T> iterator() {
      return new DataIterator();
    }

    public boolean remove(Object arg0) {
      throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> arg0) {
      throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> arg0) {
      throw new UnsupportedOperationException();
    }

    public int size() {
      return FileHashMap.this.size();
    }

    /**
     * You're kidding, right?
     * @see java.util.Set#toArray()
     */
    public Object[] toArray() {
      ByteBuffer[] b = new ByteBuffer[size()];
      return toArray(b);
    }

    /**
     * This is a really bad idea.
     * @see java.util.Set#toArray(T[])
     */
    @SuppressWarnings("unchecked")
    public <T2> T2[] toArray(T2[] a) {
      Iterator<T> i = new DataIterator();
      if (size() > a.length) a = (T2[]) new Object[size()];
      int pos = 0;
      try {
        while (i.hasNext()) {
          if (pos >= a.length) break;
          a[pos++] = (T2)i.next();
        }
      } catch (ClassCastException e) {
        throw new ArrayStoreException();
      }
      return a;
    }

    /** Private iterator for the DataSet class. Uses the encapsulated reader. */
    private class DataIterator implements Iterator<T> {

      private long pos;

      public DataIterator() {
        pos = 0;
        nextFull();
      }

      @Override
      public boolean hasNext() {
        return pos < fileRecords;
      }

      @Override
      public T next() {
        T result = reader.read(pos++);
        nextFull();
        return result;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      private void nextFull() {
        try {
          while (pos < fileRecords && emptyKey(file.get(pos))) pos++;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  static class MetaData implements Closeable {
    /** A filename extension for metadata files. */
    private static final String MD_EXT = ".hmd";
    /** The metadata offset of the entries value, in bytes */
    private static final int ENTRY_OFFSET = 0;
    /** The  metadata offset of the entries value, in longs */
    private static final int ENTRY_OFFSET_L = 0;

    /** The metadata offset of the keySize, in bytes */
    private static final int KEY_SIZE_OFFSET = ENTRY_OFFSET + LONG_SIZE;
    /** The metadata offset of the keySize, in ints */
    private static final int KEY_SIZE_OFFSET_I = KEY_SIZE_OFFSET / INT_SIZE;

    /** The metadata offset of the valueSize, in ints */
    private static final int VALUE_SIZE_OFFSET = KEY_SIZE_OFFSET + INT_SIZE;
    /** The metadata offset of the valueSize, in ints */
    private static final int VALUE_SIZE_OFFSET_I = VALUE_SIZE_OFFSET / INT_SIZE;

    /** The metadata offset of the primes Index, in bytes */
    private static final int PRIMES_INDEX_OFFSET = VALUE_SIZE_OFFSET + INT_SIZE;
    /** The metadata offset of the valueSize, in ints */
    private static final int PRIMES_INDEX_OFFSET_I = PRIMES_INDEX_OFFSET / INT_SIZE;

    /** The metadata offset of the loadFactor, in bytes */
    private static final int LOAD_FACTOR_OFFSET = PRIMES_INDEX_OFFSET + INT_SIZE;

    /** The total size of the metadata, in bytes */
    private static final int TOTAL_SIZE = LOAD_FACTOR_OFFSET + FLOAT_SIZE;

    /** The path of this metadata file. */
    private final File path;

    /** The file to access */
    private final RandomAccessFile raFile;
    
    /** File for metadata about the hash table */
    private final FileChannel mdFile;

    /** Metadata for the hash table */
    private ByteBuffer md;

    /** Metadata for the hash table in Longs */
    private LongBuffer mdLong;

    /** Metadata for the hash table in Integers */
    private IntBuffer mdInt;

    /** Metadata for the hash table in Floats */
    private FloatBuffer mdLoadFactor;

    /** Indicates that this object was created fresh */
    private final boolean created;

    public MetaData(File f) throws IOException {
      path = new File(f.getAbsolutePath() + MD_EXT);
      created = !path.exists();
      raFile = new RandomAccessFile(path, "rw");
      if (created) {
        raFile.setLength(TOTAL_SIZE);
      } else {
        long length = path.length();
        if (length < TOTAL_SIZE) throw new IOException("HashMap Metadata file too short (" + length + ")");
        if (length > TOTAL_SIZE) throw new IOException("Corrupt Metadata file: too long (" + length + ")");
      }
      mdFile = raFile.getChannel();
      md = mdFile.map(FileChannel.MapMode.READ_WRITE, 0, TOTAL_SIZE);
      mdLong = md.asLongBuffer();
      mdInt = md.asIntBuffer();
      md.position(LOAD_FACTOR_OFFSET);
      mdLoadFactor = md.slice().asFloatBuffer();
    }

    /**
     * @return <code>true</code> if the metadata was created from scratch.
     *         <code>false</code> if it was loaded from an existing file.
     */
    public boolean created() {
      return created;
    }

    /**
     * Test if the metadata matches the given values.
     * @param tableCreate Indicates that the table is to be created.
     * @param ks The provided keySize, or 0 if ignored.
     * @param vs The provided valueSize, or 0 if ignored.
     * @param l The provided loadFactor, or 0f if ignored.
     * @param len The provided file length, or 0L if ignored.
     * @throws IOException If the metadata does not match the provided info.
     */
    public void test(boolean tableCreate, int ks, int vs, float l, long len) throws IOException {
      if (created) {
        if (!tableCreate) throw new InvalidObjectException("Bad FileHashMap structure. Table exists, but metadata missing.");
        if (ks == 0) throw new IllegalArgumentException("Key size may not be zero");
        if (l == 0f || l >= 1f) throw new IllegalArgumentException("Load factor out of bounds");
      } else {
        if (tableCreate && getEntries() != 0) {
          throw new InvalidObjectException("Bad FileHashMap request. Metadata for " + getEntries() + " entries, but missing table file");
        }
        if (ks != 0 && ks != getKeySize()) {
          throw new InvalidObjectException("Bad FileHashMap request. Key size = " + ks + ", but metadata says: " + getKeySize());
        }
        if (vs != 0 && vs != getValueSize()) {
          throw new InvalidObjectException("Bad FileHashMap request. Value size = " + vs + ", but metadata says: " + getValueSize());
        }
        if (l != 0f && l != getLoadFactor()) {
          throw new InvalidObjectException("Bad FileHashMap request. Load Factor = " + vs + ", but metadata says: " + getLoadFactor());
        }
        int recordSize = getKeySize() + getValueSize();
        if (len != 0 && len != PRIMES[getPrimesIndex()] * recordSize) {
          throw new InvalidObjectException("Bad FileHashMap request. Size = " + len + ", but metadata says: " + PRIMES[getPrimesIndex()]);
        }
      }
    }

    public void close() throws IOException {
      md = null;
      mdLong = null;
      mdInt = null;
      mdLoadFactor = null;
      raFile.close(); // This calls mdFile.close() internally
    }
    
    public void closeAndDelete() throws IOException {
      close();
      path.delete();
    }
    
    public MetaData setEntries(long entries) {
      mdLong.put(ENTRY_OFFSET_L, entries);
      return this;
    }

    public long getEntries() {
      return mdLong.get(ENTRY_OFFSET_L);
    }

    public MetaData setKeySize(int keySize) {
      mdInt.put(KEY_SIZE_OFFSET_I, keySize);
      return this;
    }

    public int getKeySize() {
      return mdInt.get(KEY_SIZE_OFFSET_I);
    }

    public MetaData setValueSize(int valueSize) {
      mdInt.put(VALUE_SIZE_OFFSET_I, valueSize);
      return this;
    }

    public int getValueSize() {
      return mdInt.get(VALUE_SIZE_OFFSET_I);
    }

    public MetaData setPrimesIndex(int primesIndex) {
      mdInt.put(PRIMES_INDEX_OFFSET_I, primesIndex);
      return this;
    }

    public int getPrimesIndex() {
      return mdInt.get(PRIMES_INDEX_OFFSET_I);
    }

    public MetaData setLoadFactor(float loadFactor) {
      mdLoadFactor.put(0, loadFactor);
      return this;
    }

    public float getLoadFactor() {
      return mdLoadFactor.get(0);
    }
  }

  // debug
  byte[] dump() throws IOException {
    return ((RecordFileImpl)file).dump();
  }
}
