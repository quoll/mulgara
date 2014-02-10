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
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.mulgara.util.io.MappingUtil;

/**
 * @created 2003-01-09
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
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class MappedIntFile extends IntFile {

  private static final Logger logger = Logger.getLogger(MappedIntFile.class);

  private final static long REGION_SIZE_B = 8 * 1024 * 1024;
  private final static long REGION_SIZE_I = REGION_SIZE_B / SIZEOF_INT;
  private final static long REGION_SIZE = REGION_SIZE_B / SIZEOF_LONG;

  /* The initial size of each of the Buffer arrays. */
  private final static int INITIAL_NR_REGIONS = 1024;

  private MappedByteBuffer[] mappedByteBuffers;
  private IntBuffer[] intBuffers;
  private LongBuffer[] longBuffers;

  /* The number of regions that are currently mapped. */
  private int nrMappedRegions = 0;

  /**
   * Constructs a MappedIntFile for the specified file.
   *
   * @param file The file to open.
   * @throws IOException if an I/O error occurs.
   */
  MappedIntFile(File file) throws IOException {
    super(file);

    int nrRegions = (int) (((size + REGION_SIZE) - 1) / REGION_SIZE);
    int maxNrRegions = INITIAL_NR_REGIONS;

    while (maxNrRegions < nrRegions) {
      maxNrRegions *= 2;
    }

    mappedByteBuffers = new MappedByteBuffer[maxNrRegions];
    intBuffers = new IntBuffer[maxNrRegions];
    longBuffers = new LongBuffer[maxNrRegions];

    if (nrRegions > 0) {
      mapFile(nrRegions);
    }
  }

  /**
   * Sets the size of the file in longs. The file will not be expanded if it is
   * already large enough. The file will be truncated to the correct length
   * when {@link #close} is called.
   *
   * @param newSize the new size of the file in longs.
   * @throws IOException if an I/O error occurs.
   */
  public void setSize(long newSize) throws IOException {
    if (newSize < 0) {
      throw new IllegalArgumentException("newSize is negative.");
    }

    long prevSize = size;
    super.setSize(newSize);

    if (newSize <= prevSize) {
      return;
    }

    // Call mapFile() if the file must grow in size.
    int nrRegions = (int) (((newSize + REGION_SIZE) - 1) / REGION_SIZE);

    if (nrRegions > nrMappedRegions) {
      mapFile(nrRegions);
    }

    long key = prevSize;
    try {
      for (; key < newSize; ++key) {
        putLong(key, 0);
      }
    } catch (NullPointerException e) {
      logger.error("Out of range during resize. prevSize=" + prevSize + ", newSize=" + newSize + ", bad_offset=" + key);
      throw new IOException("Out of range during resize. prevSize=" + prevSize + ", newSize=" + newSize + ", bad_offset=" + key);
    }
  }

  /**
   * Reads the long at the specified offset into the file.
   *
   * @param key The offset into the file in longs.
   * @return the long at the specified offset.
   */
  public long getLong(long key) {
    assert key >= 0;

    if (key >= size) {
      return 0;
    }

    int regionNr = (int) (key / REGION_SIZE);
    int offset = (int) (key % REGION_SIZE);

    return longBuffers[regionNr].get(offset);
  }

  /**
   * Reads the int at the specified offset into the file.
   *
   * @param key The offset into the file in ints.
   * @return the int at the specified offset.
   */
  public int getInt(long key) {
    assert key >= 0;

    if ((key / (SIZEOF_LONG / SIZEOF_INT)) >= size) {
      return 0;
    }

    int regionNr = (int) (key / REGION_SIZE_I);
    int offset = (int) (key % REGION_SIZE_I);

    return intBuffers[regionNr].get(offset);
  }

  /**
   * Reads the byte at the specified offset into the file.
   *
   * @param key The offset into the file in bytes.
   * @return the byte at the specified offset.
   */
  public byte getByte(long key) {
    assert key >= 0;

    if ((key / SIZEOF_LONG) >= size) {
      return 0;
    }

    int regionNr = (int) (key / REGION_SIZE_B);
    int offset = (int) (key % REGION_SIZE_B);

    return mappedByteBuffers[regionNr].get(offset);
  }

  /**
   * Writes a long to the file at the specified offset.
   *
   * @param key The offset into the file in longs.
   * @param l The long to write.
   * @throws IOException if an I/O error occurs.
   */
  public void putLong(long key, long l) throws IOException {
    assert key >= 0;

    // Auto-expand.
    if (key >= size) {
      if (l == 0) {
        return;
      }

      try {
        setSize(key + 1);
      } catch (IOException e) {
        String m = "Exception mapping " + key + "=>" + l + ". ";
        logger.fatal(m, e);
        throw new IOException(m + e.getMessage());
      }
    }

    int regionNr = (int) (key / REGION_SIZE);
    int offset = (int) (key % REGION_SIZE);
    longBuffers[regionNr].put(offset, l);
  }

  /**
   * Writes an int to the file at the specified offset.
   *
   * @param key The offset into the file in ints.
   * @param i The int to write.
   * @throws IOException if an I/O error occurs.
   */
  public void putInt(long key, int i) throws IOException {
    assert key >= 0;

    // Auto-expand.
    long lKey = key / (SIZEOF_LONG / SIZEOF_INT);

    if (lKey >= size) {
      if (i == 0) {
        return;
      }

      setSize(lKey + 1);
    }

    int regionNr = (int) (key / REGION_SIZE_I);
    int offset = (int) (key % REGION_SIZE_I);
    intBuffers[regionNr].put(offset, i);
  }

  /**
   * Writes a byte to the file at the specified offset.
   *
   * @param key The offset into the file in bytes.
   * @param b The byte to write.
   * @throws IOException if an I/O error occurs.
   */
  public void putByte(long key, byte b) throws IOException {
    assert key >= 0;

    // Auto-expand.
    long lKey = key / SIZEOF_LONG;

    if (lKey >= size) {
      if (b == 0) {
        return;
      }

      setSize(lKey + 1);
    }

    int regionNr = (int) (key / REGION_SIZE_B);
    int offset = (int) (key % REGION_SIZE_B);
    mappedByteBuffers[regionNr].put(offset, b);
  }

  /**
   * Ensures that all written data has been forced to physical storage
   * before returning.
   *
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void force() throws IOException {
    for (int i = 0; i < nrMappedRegions; ++i) {
      mappedByteBuffers[i].force();
    }
  }

  /**
   * Truncates the file to zero length.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void clear() throws IOException {
    int maxNrRegions = INITIAL_NR_REGIONS;
    mappedByteBuffers = new MappedByteBuffer[maxNrRegions];
    intBuffers = new IntBuffer[maxNrRegions];
    longBuffers = new LongBuffer[maxNrRegions];
    nrMappedRegions = 0;

    super.clear();
  }

  /**
   * Sets references to MappedByteBuffers and any other Buffers that directly
   * or indirectly wrap a MappedByteBuffer to null so that the
   * MappedByteBuffers can be garbage collected and cause the underlying
   * OS file mappings to be unmapped.  Some operating systems will not allow
   * a file to be deleted or truncated while it is mapped.
   */
  public synchronized void unmap() {
    // Discard the file mappings.
    MappingUtil.release(mappedByteBuffers);
    mappedByteBuffers = null;
    intBuffers = null;
    longBuffers = null;
    nrMappedRegions = 0;
  }


  /**
   * Expands the file to contain nrRegions regions and maps the additional
   * regions.
   *
   * @param nrRegions PARAMETER TO DO
   * @throws IOException if an I/O error occurs.
   */
  private synchronized void mapFile(int nrRegions) throws IOException {
    assert nrRegions > nrMappedRegions;

    // Check if the buffer arrays need to be increased in size.
    int maxNrRegions = mappedByteBuffers.length;

    if (maxNrRegions < nrRegions) {
      do {
        maxNrRegions *= 2;
      } while (maxNrRegions < nrRegions);

      MappedByteBuffer[] mbbs = new MappedByteBuffer[maxNrRegions];
      IntBuffer[] ibs = new IntBuffer[maxNrRegions];
      LongBuffer[] lbs = new LongBuffer[maxNrRegions];
      System.arraycopy(mappedByteBuffers, 0, mbbs, 0, nrMappedRegions);
      System.arraycopy(intBuffers, 0, ibs, 0, nrMappedRegions);
      System.arraycopy(longBuffers, 0, lbs, 0, nrMappedRegions);
      mappedByteBuffers = mbbs;
      intBuffers = ibs;
      longBuffers = lbs;
    }

    long mappedSize = nrMappedRegions * REGION_SIZE_B;

    // Get the current file size.
    long currentFileSize = fc.size();

    if (currentFileSize < mappedSize) {
      throw new Error("File has shrunk: " + file);
    }

    long fileSize = nrRegions * REGION_SIZE_B;

    // Expand the file.
    raf.setLength(fileSize);

    for (int regionNr = nrMappedRegions; regionNr < nrRegions; ++regionNr) {
      int retries = 10;

      for (;;) {
        try {
          MappedByteBuffer mbb = fc.map(
              FileChannel.MapMode.READ_WRITE, regionNr * REGION_SIZE_B,
              REGION_SIZE_B
          );
          mbb.order(byteOrder);
          mappedByteBuffers[regionNr] = mbb;
          mbb.rewind();
          intBuffers[regionNr] = mbb.asIntBuffer();
          longBuffers[regionNr] = mbb.asLongBuffer();

          break;
        } catch (IOException ex) {
          // Rethrow the exception if we are out of retries.
          if (retries-- == 0) throw ex;

          // Let some old mappings go away and try again.
          MappingUtil.systemCleanup();
        }
      }
    }

    nrMappedRegions = nrRegions;
  }
  
}
