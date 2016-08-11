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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.mulgara.util.io.Bytes.LONG_SIZE;

/**
 * A file-based hash map mapping long to long
 */
public class LLHashMap implements Map<Long,Long> {

  /** The internal map that maps ByteBuffer to ByteBuffer */
  private final FileHashMap map;

  /**
   * Creates a hash map for Long to Long.
   * @param f The file to put the table into.
   * @throws IOException If there is an error accessing the file.
   */
  public LLHashMap(File f) throws IOException {
    map = new FileHashMap(f, LONG_SIZE, LONG_SIZE);
  }

  /**
   * Creates a hash map for Long to Long.
   * @param f The file to put the table into.
   * @param loadFactor The fraction of the table above with the table will get expanded.
   * @param initialSize The initial number of slots available to put data into before being rehashed.
   * @throws IOException If there is an error accessing the file.
   */
  public LLHashMap(File f, float loadFactor, long initialSize) throws IOException {
    map = new FileHashMap(f, LONG_SIZE, LONG_SIZE, loadFactor, initialSize);
  }

  /**
   * @see java.io.Closeable#close()
   */
  public void close() throws IOException {
    map.close();
  }

  /**
   * Closes the object and removes it from the filesystem.
   * @throws IOException If there is an error closing or removing the files.
   */
  public void closeAndDelete() throws IOException {
    map.closeAndDelete();
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(toBytes(key));
  }

  public boolean containsKey(long key) {
    return map.containsKey(toBytes(key));
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(toBytes(value));
  }

  public boolean containsValue(long value) {
    return map.containsValue(toBytes(value));
  }

  @Override
  public Set<Map.Entry<Long, Long>> entrySet() {
    return new ArrayBufferSetWrapper<Map.Entry<Long,Long>,Map.Entry<ByteBuffer,ByteBuffer>>(map.entrySet(),
        new SetDataConverter<Map.Entry<Long,Long>, Map.Entry<ByteBuffer,ByteBuffer>>() {
          public Map.Entry<ByteBuffer,ByteBuffer> toSetData(Map.Entry<Long,Long> d) {
            return new BBKeyValue(toBytes(d.getKey()), toBytes(d.getValue()));
          }
          public Map.Entry<Long,Long> fromSetData(Map.Entry<ByteBuffer,ByteBuffer> b) {
            return new KeyValue<Long,Long>(fromBytes(b.getKey()), fromBytes(b.getValue()));
          }
        });
  }

  @Override
  public Long get(Object key) {
    ByteBuffer b = map.get(toBytes(key));
    return b == null ? null : fromBytes(b);
  }

  public long get(long key) {
    return fromBytes(map.get(toBytes(key)));
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Set<Long> keySet() {
    return new ArrayBufferSetWrapper<Long,ByteBuffer>(map.keySet(),
        new SetDataConverter<Long, ByteBuffer>() {
          public ByteBuffer toSetData(Long d) { return toBytes(d); }
          public Long fromSetData(ByteBuffer b) { return fromBytes(b); }
        });
  }

  @Override
  public Long put(Long key, Long value) {
    ByteBuffer[] pair = toBytesPair(key, value);
    return fromBytes(map.put(pair[0], pair[1]));
  }

  public long put(long key, long value) {
    ByteBuffer[] pair = toBytesPair(key, value);
    return fromBytes(map.put(pair[0], pair[1]));
  }

  @Override
  public void putAll(Map<? extends Long, ? extends Long> m) {
    for (java.util.Map.Entry<? extends Long, ? extends Long> e: m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public Long remove(Object key) {
    return fromBytes(map.remove(toBytes(key)));
  }

  public long remove(long key) {
    return fromBytes(map.remove(toBytes(key)));
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public Collection<Long> values() {
    return new ArrayBufferSetWrapper<Long,ByteBuffer>((Set<ByteBuffer>)map.values(),
        new SetDataConverter<Long, ByteBuffer>() {
          public ByteBuffer toSetData(Long d) { return toBytes(d); }
          public Long fromSetData(ByteBuffer b) { return fromBytes(b); }
        });
  }

  /**
   * Convenience method to avoid casting Object that will then be sent
   * for auto unboxing.
   * @param l A Long value as an Object.
   * @return A new {@link ByteBuffer} containing the number. Allocated according to the type
   *         configured with {@link IOUtil#MEM_TYPE_PROP}
   */
  private static final ByteBuffer toBytes(Object l) {
    ByteBuffer b = IOUtil.allocate(LONG_SIZE);
    b.asLongBuffer().put(0, ((Long)l).longValue());
    return b;
  }

  /**
   * Converts a pair of long numbers into a pair of ByteBuffers.
   * @param first The first number to convert.
   * @param second The second number to convert.
   * @return An array containing the new {@link ByteBuffer}s containing the numbers.
   *         Allocated according to the type configured with {@link IOUtil#MEM_TYPE_PROP}
   */
  private static final ByteBuffer[] toBytesPair(long first, long second) {
    ByteBuffer[] pair = new ByteBuffer[2];
    ByteBuffer b = toBytes(first, second);
    b.position(LONG_SIZE);
    pair[1] = b.slice();
    b.flip();
    pair[0] = b.slice();
    return pair;
  }

  /**
   * Converts a pair of long numbers into a ByteBuffer.
   * @param first The first number to convert.
   * @param second The second number to convert.
   * @return A {@link ByteBuffer}s containing the numbers.
   *         Allocated according to the type configured with {@link IOUtil#MEM_TYPE_PROP}
   */
  private static final ByteBuffer toBytes(long first, long second) {
    ByteBuffer b = IOUtil.allocate(LONG_SIZE << 1);
    b.asLongBuffer().put(0, first).put(1, second);
    return b;
  }

  /**
   * Converts a ByteBuffer into a long value.
   * @param bb The buffer to convert.
   * @return The long value from the buffer.
   */
  private static final long fromBytes(ByteBuffer bb) {
    return bb == null ? -1L : bb.asLongBuffer().get(0);
  }

  /** A {@link Map.Entry} implementation for use with {@link FileHashMap#entrySet()}. */
  public static class KeyValue<K,V> implements Map.Entry<K,V> {
    private final K key;
    protected V value;
    public KeyValue(K k, V v) { key = k; value = v; }
    public K getKey() { return key; }
    public V getValue() { return value; }
    public V setValue(V v) {
      V old = value;
      value = v;
      return old;
    }
  }

  /** A {@link Map.Entry} implementation for use with {@link FileHashMap#entrySet()}. */
  public static class BBKeyValue extends KeyValue<ByteBuffer,ByteBuffer> {
    public BBKeyValue(ByteBuffer k, ByteBuffer v) { super(k, v); }
    public ByteBuffer setValue(ByteBuffer v) {
      int size = value == null ? v.capacity() : value.capacity();
      byte[] old = new byte[size];
      value.position(0);
      value.limit(size);
      value.get(old);
      value.position(0);
      v.position(0);
      v.limit(size);
      value.put(v);
      return ByteBuffer.wrap(old);
    }
  }


  // debug
  byte[] dump() throws IOException {
    return map.dump();
  }
}
