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
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.mulgara.util.io.MappingUtil;

/**
 * A file consisting of longs, ints or bytes that can be accessed concurrently
 * by multiple readers and a single writer without additional thread
 * synchronization.
 *
 * @created 2001-09-20
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
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class IntFile implements LongMapper {

  protected final static int SIZEOF_LONG = 8;
  protected final static int SIZEOF_INT = 4;
  protected final static long MASK32 = 0xffffffffL;
  protected final static ByteOrder byteOrder; // The byte order to use.

  private static final Logger logger = Logger.getLogger(IntFile.class);

  static {
    String useByteOrderProp = System.getProperty("mulgara.xa.useByteOrder");
    ByteOrder bo = ByteOrder.nativeOrder();

    // Default.
    if (useByteOrderProp != null) {
      if (useByteOrderProp.equalsIgnoreCase("native")) {
        bo = ByteOrder.nativeOrder();
      } else if (useByteOrderProp.equalsIgnoreCase("big_endian")) {
        bo = ByteOrder.BIG_ENDIAN;
      } else if (useByteOrderProp.equalsIgnoreCase("little_endian")) {
        bo = ByteOrder.LITTLE_ENDIAN;
      } else {
        logger.warn("Invalid value for property mulgara.xa.useByteOrder: " +
            useByteOrderProp);
      }
    }

    byteOrder = bo;
  }

  /**
   * The name of the open file.
   */
  protected File file;

  /**
   * A RandomAccessFile object for the file.
   */
  protected RandomAccessFile raf;

  /**
   * A FileChannel object for the file.
   */
  protected FileChannel fc;

  /**
   * Number of valid long entries in the file.
   */
  protected long size;

  /**
   * The base class constructor.
   *
   * @param file The file to open.
   * @throws IOException if an I/O error occurs.
   */
  protected IntFile(File file) throws IOException {
    this.file = file;
    raf = new RandomAccessFile(file, "rw");
    fc = raf.getChannel();
    size = fc.size() / SIZEOF_LONG;

    // Truncate the file to a multiple of SIZEOF_LONG if its size is not
    // already.
    long fileSize = size * SIZEOF_LONG;

    if (fileSize < fc.size()) {
      truncate(fileSize);
      logger.warn("File size was not a multiple of SIZEOF_LONG: \"" + file +
          "\".");
    }
  }

  /**
   * Returns a newly constructed IntFile for the specified file.
   * The value of the system property mulgara.xa.forceIOType determines
   * if the file is accessed using mapped or explicit I/O.
   *
   * @param file The name of the file to open.
   * @return a newly created IntFile object.
   * @throws IOException if an I/O error occurs.
   */
  public static IntFile open(File file) throws IOException {
    boolean explicitIO = false;

    String forceIOTypeProp = System.getProperty("mulgara.xa.forceIOType");

    if (forceIOTypeProp != null) {
      if (forceIOTypeProp.equalsIgnoreCase("mapped")) {
        explicitIO = false;
      } else if (forceIOTypeProp.equalsIgnoreCase("explicit")) {
        explicitIO = true;
      } else {
        logger.warn("Invalid value for property mulgara.xa.forceIOType: " +
            forceIOTypeProp);
      }
    }

    return open(file, explicitIO);
  }

  /**
   * Returns a newly constructed IntFile for the specified file.
   *
   * @param file The name of the file to open.
   * @param explicitIO true if the file should be accessed using explicit I/O
   * and false if mapped I/O should be used.
   * @return a newly created IntFile object.
   * @throws IOException if an I/O error occurs.
   */
  public static IntFile open(File file, boolean explicitIO) throws IOException {
    // TODO fix bug in ExplicitIntFile.  Currently always use MappedIntFile.
    //if (explicitIO) {
    //  return new ExplicitIntFile(file);
    //} else {
      return new MappedIntFile(file);
    //}
  }

  /**
   * Returns a newly constructed IntFile for the specified file.
   * The value of the system property mulgara.xa.forceIOType determines
   * if the file is accessed using mapped or explicit I/O.
   *
   * @param fileName The name of the file to open.
   * @return a newly created IntFile object.
   * @throws IOException if an I/O error occurs.
   */
  public static IntFile open(String fileName) throws IOException {
    return open(new File(fileName));
  }

  /**
   * Sets the size of the file in longs.
   *
   * @param newSize the new size of the file in longs.
   * @throws IOException if an I/O error occurs.
   */
  public void setSize(long newSize) throws IOException {
    if (newSize < 0) {
      throw new IllegalArgumentException("newSize is negative.");
    }

    size = newSize;
  }

  /**
   * Reads the long at the specified offset into the file.
   *
   * @param key The offset into the file in longs.
   * @return the long at the specified offset.
   */
  public abstract long getLong(long key);

  /**
   * Reads the int at the specified offset into the file.
   *
   * @param key The offset into the file in ints.
   * @return the int at the specified offset.
   */
  public abstract int getInt(long key);

  /**
   * Reads the unsigned int at the specified offset into the file.
   *
   * @param key The offset into the file in ints.
   * @return the unsigned int at the specified offset.
   */
  public long getUInt(long key) {
    return (long) getInt(key) & MASK32;
  }

  /**
   * Reads the byte at the specified offset into the file.
   *
   * @param key The offset into the file in bytes.
   * @return the byte at the specified offset.
   */
  public abstract byte getByte(long key);

  /**
   * Gets the number of valid long entries in the file.
   *
   * @return the number of valid long entries in the file.
   */
  public long getSize() {
    return size;
  }

  /**
   * Writes a long to the file at the specified offset.
   *
   * @param key The offset into the file in longs.
   * @param l The long to write.
   * @throws IOException if an I/O error occurs.
   */
  public abstract void putLong(long key, long l) throws IOException;

  /**
   * Writes an int to the file at the specified offset.
   *
   * @param key The offset into the file in ints.
   * @param i The int to write.
   * @throws IOException if an I/O error occurs.
   */
  public abstract void putInt(long key, int i) throws IOException;

  /**
   * Writes an unsigned int to the file at the specified offset.
   *
   * @param key The offset into the file in ints.
   * @param ui The unsigned int to write.
   * @throws IOException if an I/O error occurs.
   */
  public void putUInt(long key, long ui) throws IOException {
    assert ui >= 0 && ui < (1L << 32);
    putInt(key, (int) ui);
  }

  /**
   * Writes a byte to the file at the specified offset.
   *
   * @param key The offset into the file in bytes.
   * @param b The byte to write.
   * @throws IOException if an I/O error occurs.
   */
  public abstract void putByte(long key, byte b) throws IOException;

  /**
   * Ensures that all written data has been forced to physical storage
   * before returning.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void force() throws IOException {
    if (fc == null) {
      throw new IllegalStateException("FileChannel is null");
    }
    fc.force(true);
  }

  /**
   * Truncates the file to zero length.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void clear() throws IOException {
    if (fc == null) {
      throw new IllegalStateException("FileChannel is null");
    }
    size = 0;
    truncate(0L);
    fc.force(true);
  }

  /**
   * Sets references to MappedByteBuffers and any other Buffers that directly
   * or indirectly wrap a MappedByteBuffer to null so that the
   * MappedByteBuffers can be garbage collected and cause the underlying
   * OS file mappings to be unmapped.  Some operating systems will not allow
   * a file to be deleted or truncated while it is mapped.
   */
  public void unmap() {
    // NO-OP
  }


  /**
   * Close the file after truncating it to the actual size.
   *
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void close() throws IOException {
    close(true);
  }


  /**
   * Close and delete the file.
   *
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void delete() throws IOException {
    try {
      // Close but don't bother truncating the file.
      close(false);
    } finally {
      if (file != null) {
        int retries = 10;

        while (!file.delete() && file.isFile() && retries-- > 0) {
          // Causing any MappedByteBuffers to be unmapped may allow the
          // file to be deleted.  This may be needed for Windows.
          MappingUtil.systemCleanup();
        }

        if (retries < 0) {
          logger.warn("NOTE: Failed to delete: " + file);
        }

        file = null;
      }
    }
  }


  /**
   * Create a temporary IntFile, using a name based on a given pattern.
   * @param namePattern The base for the name to use.
   * @return A new IntFile object.
   * @throws IOException Due to a file access error.
   */
  public static IntFile newTempIntFile(String namePattern) throws IOException {
    File file = null;
    try {
      file = TempDir.createTempFile(namePattern, null);
      return IntFile.open(file);
    } catch (IOException e) {
      if (file != null) file.delete();
      throw e;
    }
  }


  /**
   * Close and optionally truncate the file.
   * @throws IOException if an I/O error occurs.
   */
  private void close(boolean truncateFile) throws IOException {
    // Truncate the file to the correct size.
    boolean success = false;
    try {
      unmap();

      if (fc != null && fc.isOpen()) {
        try {
          if (truncateFile) {
            truncate(size * SIZEOF_LONG);
          }
        } finally {
          fc.close();
        }
      }
      success = true;
    } finally {
      fc = null;
      if (raf != null) {
        try {
          raf.close();
        } catch (IOException e) {
          if (success) throw e; // Everything else worked, rethrow this.
          else logger.info("Suppressing I/O exception closing failed IntFile", e); // Log and ignore, already got another exception.
        } finally {
          raf = null;
        }
      }
    }
  }


  /**
   * Truncates the file to the specified size.
   *
   * @param fileSize the size in bytes
   */
  protected void truncate(long fileSize) {
    if (fc == null) {
      throw new IllegalStateException("FileChannel is null");
    }

    try {
      MappingUtil.truncate(fc, fileSize);
    } catch (IOException ex) {
      logger.warn(
          "NOTE: Could not truncate file: \"" + file +
          "\" to size: " + fileSize +
          " - deferring until next time the file is opened."
      );
      logger.info("Could not truncate file", ex);
    }
  }

}
