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

package org.mulgara.util;

// Java 2 standard packages
import java.io.*;
import java.util.Map;
import java.util.LinkedHashMap;

// Third party packages
import org.apache.log4j.Logger;

import org.mulgara.util.TempDir;


/**
 * A temporary disk based hash map that maps strings of up to 255 characters
 * to unsigned longs with values of up to 2^56.  String to long map entries
 * can be added but not removed.  This class does not implement the Map
 * interface.  Although this map is disk based it can not be used for
 * persistent storage.
 *
 * @created 2004-05-07
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:29 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class StringToLongMap {

  private static final Logger logger = Logger.getLogger(StringToLongMap.class);

  // Hash bucket format (16 bytes):
  // [ hash code (4)      ][ string offset (4)  ]
  // [ value (8)                                ]
  //
  // A Hash bucket is free if the value is zero.

  private static final int MASK31 = 0x7fffffff;
  private static final long MASK32 = 0xffffffffL;

  /**
   * This is a hint for the initial number of buckets to use.  This will be
   * rounded up to a prime number obtained from the {@link #primes} array.
   */
  private static final int MIN_NR_BUCKETS = 2000;

  /**
   * This is used to determine when a rehash should be performed.
   * @see #rehash
   */
  private static final float REHASH_LIMIT = 0.8f;

  /**
   * This is a list of primes where each prime is roughly double the previous
   * one.  It is used to select a hash table size which is a prime number.
   */
  private static final int[] primes = {
    503, 1009, 2027, 4057, 8117, 16249, 32503, 65011, 130027, 260081,
    520193, 1040387, 2080777, 4161557, 8323151, 16646317, 33292687,
    66585377, 133170769, 266341583, 532683227, 1065366479, 2130732959
  };

  private Cache cache = new Cache(100);

  private File hashFileName;

  private IntFile hashFile;

  private File stringFileName;

  private RandomAccessFile stringFile;

  private int nrUsedBuckets = 0;

  private int nrBuckets;

  public StringToLongMap() throws IOException {
    hashFileName = TempDir.createTempFile("strToLong", ".hash");
    hashFile = IntFile.open(hashFileName);

    stringFileName = TempDir.createTempFile("strToLong", ".str");
    stringFile = new RandomAccessFile(stringFileName, "rw");

    nrBuckets = findPrime(MIN_NR_BUCKETS);
    clear();
  }

  public void clear() throws IOException {
    hashFile.setSize(0);
    stringFile.setLength(0);
    cache.clear();
  }

  /**
   * Associates a long with a String.
   *
   * @param str the String.
   * @throws IOException if an I/O error occurs.
   */
  public void put(String str, long value) throws IOException {
    getAndPut(str, value);
  }

  /**
   * Returns the long associated with the String or 0 if there is no long
   * associated with the string.
   *
   * @param str the String.
   * @return the long associated with the String or 0 if there is no long
   * associated with the string.
   * @throws IOException if an I/O error occurs.
   */
  public long get(String str) throws IOException {
    return getAndPut(str, 0);
  }

  /**
   * Returns the long associated with the String or 0 if there is no long
   * associated with the string.  If newValue is not equal to 0 then this
   * becomes the new value associated with the string.
   *
   * @param str the String.
   * @param newValue if not equal to 0, the new value to be associated with
   * the string.
   * @return the long currently associated with the String or 0 if there is no
   * long associated with the string.
   * @throws IOException if an I/O error occurs or the hash table is full.
   */
  public long getAndPut(String str, long newValue) throws IOException {
    if (str == null) {
      throw new IllegalArgumentException("str is null");
    }

    if (newValue == 0) {
      Long l = cache.get(str);
      if (l != null) {
        return l.longValue();
      }
    }

    int hashCode = str.hashCode();
    int startBucket = calcBucket(hashCode);
    int bucket = startBucket;
    long value;

    // Try buckets until we find the correct string or an empty bucket.
    while ((value = getValue(bucket)) != 0) {
      // Check the hash code.
      long offset = (long)bucket * 4;
      if (hashCode == hashFile.getInt(offset)) {
        // Fetch the string and compare with the target string.
        String bucketStr = readString(hashFile.getUInt(offset + 1));
        if (str.equals(bucketStr)) {
          if (newValue != 0) {
            hashFile.putLong((long)bucket * 2 + 1, newValue);
            // Add the new value to the cache.
            cache.put(str, newValue);
          } else {
            // Add the value to the cache.
            cache.put(str, value);
          }
          return value;
        }
      }

      // Try the next bucket.
      bucket = (bucket + 1) % nrBuckets;
      if (bucket == startBucket) {
        throw new IOException("Hash table full");
      }
    }

    if (newValue != 0) {
      // Add the new value to the cache.
      cache.put(str, newValue);

      // Add a new hash bucket.
      long bucketOffset = (long)bucket * 4;
      hashFile.putInt(bucketOffset, hashCode);
      hashFile.putUInt(bucketOffset + 1, writeString(str));
      hashFile.putLong((long)bucket * 2 + 1, newValue);
      ++nrUsedBuckets;

      if (nrUsedBuckets >= (int)(nrBuckets * REHASH_LIMIT)) {
        rehash(findPrime(nrBuckets));
      }
    }

    return 0;
  }

  /**
   * Closes and deletes the files.
   */
  public void delete() {
    if (hashFile != null) {
      try {
        hashFile.delete();
      } catch (IOException ex) {
        logger.warn(
            "An I/O error occurred while deleting: " + hashFileName, ex
        );
      } finally {
        hashFile = null;
      }
    }

    if (stringFile != null) {
      try {
        stringFile.close();
        stringFileName.delete();
      } catch (IOException ex) {
        logger.warn(
            "An I/O error occurred while deleting: " + stringFileName, ex
        );
      } finally {
        stringFile = null;
      }
    }
  }

  /**
   * Ensure that resources are cleaned up correctly, if not already done.
   * @see java.lang.Object#finalize()
   */
  protected void finalize() throws Throwable {
    try {
      delete();
    } finally {
      super.finalize();
    }
  }

  /**
   * Reorgainizes all the key/value pairs in the hash table to accomodate the
   * new size.
   *
   * @param newNrBuckets The new size.
   */
  private void rehash(int newNrBuckets) throws IOException {
    if (newNrBuckets == nrBuckets) return;

    int oldNrBuckets = nrBuckets;
    nrBuckets = newNrBuckets;

    // Iterate over the buckets, moving items to their correct locations.
    for (int bucket = 0; ; ++bucket) {
      long value;
      if ((value = getValue(bucket)) != 0) {
        // This bucket is in use.
        long offset = (long)bucket * 4;
        int hashCode = hashFile.getInt(offset);
        int destBucket = calcBucket(hashCode);

        // Work out where the item belongs.  Abort if it ends up back here.
        while (destBucket != bucket) {
          if (getValue(destBucket) == 0) {
            // Found an empty bucket.  Move the item here.
            long destOffset = (long)destBucket * 4;
            hashFile.putInt(destOffset, hashCode);
            hashFile.putInt(destOffset + 1, hashFile.getInt(offset + 1));
            hashFile.putLong((long)destBucket * 2 + 1, value);

            // Clear the old bucket.
            hashFile.putLong((long)bucket * 2 + 1, 0);
            break;
          }

          // Try the next bucket.
          destBucket = (destBucket + 1) % nrBuckets;
        }
      } else {
        // An empty bucket.
        // Stop if we have processed all of the old buckets.
        // NOTE: We potentially process more than oldNrBuckets buckets
        // so that we pick up any items that clashed and were bounced
        // past the end of this range 0..(oldNrBuckets-1).
        if (bucket >= oldNrBuckets) break;
      }
    }
  }

  /**
   * Finds a prime number larger than the given number.
   *
   * @param n the number.
   * @return a prime number larger than n or n if there are no primes larger
   *         than n in the primes array.
   */
  private int findPrime(int n) {
    for (int i = 0; i < primes.length; ++i)
      if (primes[i] > n) return primes[i];
    return n;
  }

  private int calcBucket(int hashCode) {
    return (hashCode & MASK31) % nrBuckets;
  }

  private long getValue(int bucket) throws IOException {
    return hashFile.getLong((long)bucket * 2 + 1);
  }

  private String readString(long strOffset) throws IOException {
    stringFile.seek(strOffset);
    return stringFile.readUTF();
  }

  /**
   * Appends a string to the file and returns the offset of the start of the
   * string.
   *
   * @param str the string to be written.
   * @return the offset of the start of the string that was written.
   * @throws IOException if an I/O error occurs or the offset would be larger
   * than 2^32-1.
   */
  private long writeString(String str) throws IOException {
    // Write the string at the end of the file.
    long pos = stringFile.length();
    if (pos > MASK32) throw new IOException("String file too large");

    stringFile.seek(pos);
    stringFile.writeUTF(str);
    return pos;
  }

  static final class Cache extends LinkedHashMap<String,Long> {

    /** serialization ID */
    private static final long serialVersionUID = -1793863465408688001L;

    public static final float LOAD_FACTOR = 0.75F;
    final int MAX_SIZE;

    public Cache(int maxSize) {
      super((int)Math.ceil(maxSize / LOAD_FACTOR + 1), LOAD_FACTOR, true);
      MAX_SIZE = maxSize;
    }

    protected boolean removeEldestEntry(Map.Entry<String,Long> eldest) {
      return size() > MAX_SIZE;
    }

    public void put(String s, long l) {
      assert s != null;
      super.put(s, new Long(l));
    }

    public Long get(String s) {
      assert s != null;
      return super.get(s);
    }

  }

}
