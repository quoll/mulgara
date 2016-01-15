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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;

/**
 * Static utility methods for common actions on IO.
 */
public class IOUtil {

  /** The logger. */
  private static final Logger logger = Logger.getLogger(IOUtil.class);

  /** The system property for the byte order. */
  public static final String BYTE_ORDER_PROPERTY = "mulgara.xa.useByteOrder";

  /** The property for the block type. May be "direct" or "javaHeap" */
  public static final String MEM_TYPE_PROP = "mulgara.xa.memoryType";

  /** Native ordering of the bytes */
  public static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

  /** The endianness of this computer. */
  private static final ByteOrder byteOrder;

  /** Enumeration of the different memory types for blocks */
  public enum BlockMemoryType { DIRECT, HEAP };

  /** The default value to use for the block memory type. Used when nothing is configured. */
  private static final BlockMemoryType DEFAULT = BlockMemoryType.DIRECT;

  /** The configured type of block type to use. */
  private static final BlockMemoryType BLOCK_TYPE;

  static {
    // Determine the byte order of this machine, and select an ordering to use. *
    String useByteOrderProp = System.getProperty(BYTE_ORDER_PROPERTY, "native");
    ByteOrder bo = ByteOrder.nativeOrder();
    if (useByteOrderProp != null) {
      if (useByteOrderProp.equalsIgnoreCase("native")) {
        bo = ByteOrder.nativeOrder();
      } else if (useByteOrderProp.equalsIgnoreCase("big_endian")) {
        bo = ByteOrder.BIG_ENDIAN;
      } else if (useByteOrderProp.equalsIgnoreCase("little_endian")) {
        bo = ByteOrder.LITTLE_ENDIAN;
      } else {
        logger.warn("Invalid value for property mulgara.xa.useByteOrder: " + useByteOrderProp);
      }
    }
    byteOrder = bo;

    // initialize the type of memory block to be used: heap or direct
    // configured with mulgara.xa.memoryType
    String defBlockType = System.getProperty(MEM_TYPE_PROP, DEFAULT.name());
    if (defBlockType.equalsIgnoreCase(BlockMemoryType.DIRECT.name())) {
      BLOCK_TYPE = BlockMemoryType.DIRECT;
    } else if (defBlockType.equalsIgnoreCase(BlockMemoryType.HEAP.name())) {
      BLOCK_TYPE = BlockMemoryType.HEAP;
    } else {
      logger.warn("Invalid value for property " + MEM_TYPE_PROP + ": " + defBlockType);
      BLOCK_TYPE = DEFAULT;
    }
  }

  /**
   * Retrieves the configured byte ordering to use. Uses the default if nothing is set.
   * @return The configured byte order.
   */
  public static final ByteOrder getByteOrder() {
    return byteOrder;
  }
  
  /**
   * Reads the next non-empty line of text from a buffered reader, up to a given
   * number of characters.
   * 
   * @param br The BufferedReader to get a line from.
   * @param maxlen The maximum length of string to read from the line.
   * @return A line of text, not including any line-termination characters,
   *         or null if the end of stream has been reached. An empty string will
   *         indicate that the maxlen number of characters was reached before any
   *         text could be read. 
   */
  public static final String readLine(BufferedReader br, int maxlen) throws IOException {
    StringBuilder s = new StringBuilder();

    for (int i = 0; i < maxlen; i++) {
      int c = br.read();
      if (c == -1) {
        if (s.length() == 0) return null;
        break;
      }
      if (c == '\n' || c == '\r') {
        if (s.length() == 0) continue;
        break;
      }
      s.appendCodePoint(c);
    }
    return s.toString();
  }


  /**
   * Allocates data according to the system configured memory model.
   * @param size The number of bytes in the allocated buffer.
   * @return the allocated buffer.
   */
  public static final ByteBuffer allocate(int size) {
    return allocate(size, byteOrder);
  }

  /**
   * Allocates data according to the system configured memory model.
   * @param size The number of bytes in the allocated buffer.
   * @param order The byteorder to use in the buffer.
   * @return the allocated buffer.
   */
  public static final ByteBuffer allocate(int size, ByteOrder order) {
    return BLOCK_TYPE == BlockMemoryType.DIRECT ?
        (order == NATIVE_ORDER ? ByteBuffer.allocateDirect(size) : ByteBuffer.allocateDirect(size).order(order)) :
        (order == NATIVE_ORDER ? ByteBuffer.allocate(size) : ByteBuffer.allocate(size).order(order));
  }

  /**
   * Mix an int into a long in a reasonably cheap way.
   * @param The int to mix.
   * @return A long value with the integer mixed into it. Positive numbers only.
   */
  public static final long longHash(int h) {
    long v = (long)h;
    v ^= v << 5;
    v ^= (v << 11) ^ 1049;
    v ^= ((v >> 32) | (v << 32));
    v ^= (v << 17) ^ 131041;
    v ^= ((v >> 56) | (v << 56));
    v ^= (v << 23) ^ 8313581;
    v ^= (((v >> 8) & 0xFF000000L) | ((v << 8) & 0xFF00000000L));
    v ^= (v << 37) ^ 2147483659L;
    v ^= ((v >> 32) | (v << 32));
    return v & 0x7FFFFFFFFFFFFFFFL;
  }

  /**
   * Convert an int into a long hash in a cheap way.
   * @param The int to hash.
   * @return A long value as an integer hash.
   */
  public static final long hashCode(int h) {
    return ((long)h | (long)h << 32);
  }

  public static final long hashCode(long h) {
    return h;
  }

  /**
   * Determines the hashCode of a string, stretched out to a long, instead of just an int.
   * @param s The string to determine the hashCode for.
   * @return The hashcode.
   */
  public static final long hashCode(String s) {
    long h = 0;
    char val[] = s.toCharArray();
    for (int i = 0; i < val.length; i++) h = 31 * h + val[i];
    return h;
  }

  public static final long hashCode(ByteBuffer bb) {
    long h = 1;
    int p = bb.position();
    for (int i = bb.limit() - 1; i >= p; i--) h = 31 * h + (long)bb.get(i);
    return h;
  }
}
