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
import java.nio.*;

// Third party packages
import org.apache.log4j.*;

/**
 * An IntFile that is accessed using explicit I/O.
 *
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
public final class ExplicitIntFile extends IntFile {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(ExplicitIntFile.class.getName());

  private ByteBuffer byteBuffer;
  private IntBuffer intBuffer;
  private LongBuffer longBuffer;
  private long fileSize; // The current size of the file in longs.

  /**
   * Constructs an ExplicitIntFile for the specified file.
   *
   * @param file The file to open.
   * @throws IOException if an I/O error occurs.
   */
  ExplicitIntFile(File file) throws IOException {
    super(file);
    fileSize = size;
    byteBuffer = ByteBuffer.allocateDirect(SIZEOF_LONG);
    intBuffer = byteBuffer.asIntBuffer();
    longBuffer = byteBuffer.asLongBuffer();
  }

  /**
   * Sets the size of the file in longs. The file will be truncated if
   * <code>newSize</code> is less than the current size.
   *
   * @param newSize the new size of the file in longs.
   * @throws IOException if an I/O error occurs.
   */
  public void setSize(long newSize) throws IOException {
    super.setSize(newSize);

    if (newSize <= fileSize) {
      fileSize = newSize;
      truncate(fileSize * SIZEOF_LONG);
    }
  }

  /**
   * Reads the long at the specified offset into the file.
   *
   * @param key The offset into the file in longs.
   * @return the long at the specified offset.
   */
  public synchronized long getLong(long key) {
    assert key >= 0;

    if (key >= fileSize) {
      return 0;
    }

    byteBuffer.clear();

    try {
      // TODO Handle ClosedChannelException and reopen FileChannel.
      if (fc.read(byteBuffer, key * SIZEOF_LONG) != SIZEOF_LONG) {
        throw new RuntimeException("Short read of IntFile: " + file);
      }
    } catch (IOException ex) {
      throw new RuntimeException("IOException: " + file, ex);
    }

    return longBuffer.get(0);
  }

  /**
   * Reads the int at the specified offset into the file.
   *
   * @param key The offset into the file in ints.
   * @return the int at the specified offset.
   */
  public synchronized int getInt(long key) {
    assert key >= 0;

    if ((key / (SIZEOF_LONG / SIZEOF_INT)) >= fileSize) {
      return 0;
    }

    byteBuffer.limit(SIZEOF_INT);
    byteBuffer.rewind();

    try {
      // TODO Handle ClosedChannelException and reopen FileChannel.
      if (fc.read(byteBuffer, key * SIZEOF_INT) != SIZEOF_INT) {
        throw new RuntimeException("Short read of IntFile: " + file);
      }
    } catch (IOException ex) {
      throw new RuntimeException("IOException: " + file, ex);
    }

    return intBuffer.get(0);
  }

  /**
   * Reads the byte at the specified offset into the file.
   *
   * @param key The offset into the file in bytes.
   * @return the byte at the specified offset.
   */
  public synchronized byte getByte(long key) {
    assert key >= 0;

    if ((key / SIZEOF_LONG) >= fileSize) {
      return 0;
    }

    byteBuffer.limit(1);
    byteBuffer.rewind();

    try {
      // TODO Handle ClosedChannelException and reopen FileChannel.
      if (fc.read(byteBuffer, key) != 1) {
        throw new RuntimeException("Short read of IntFile: " + file);
      }
    } catch (IOException ex) {
      throw new RuntimeException("IOException: " + file, ex);
    }

    return byteBuffer.get(0);
  }

  /**
   * Writes a long to the file at the specified offset.
   *
   * @param key The offset into the file in longs.
   * @param l The long to write.
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void putLong(long key, long l) throws IOException {
    assert key >= 0;

    // Auto-expand.
    if (key >= fileSize) {
      if (l == 0) {
        return;
      }

      fileSize = key + 1;

      if (fileSize > size) {
        size = fileSize;
      }
    }

    byteBuffer.clear();
    longBuffer.put(0, l);

    // TODO Handle ClosedChannelException and reopen FileChannel.
    if (fc.write(byteBuffer, key * SIZEOF_LONG) != SIZEOF_LONG) {
      throw new RuntimeException("Short write of IntFile: " + file);
    }
  }

  /**
   * Writes an int to the file at the specified offset.
   *
   * @param key The offset into the file in ints.
   * @param i The int to write.
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void putInt(long key, int i) throws IOException {
    assert key >= 0;

    // Auto-expand.
    long lKey = key / (SIZEOF_LONG / SIZEOF_INT);

    if (lKey >= fileSize) {
      if (i == 0) {
        return;
      }

      putLong(lKey, 0);
    }

    byteBuffer.limit(SIZEOF_INT);
    byteBuffer.rewind();
    intBuffer.put(0, i);

    // TODO Handle ClosedChannelException and reopen FileChannel.
    if (fc.write(byteBuffer, key * SIZEOF_INT) != SIZEOF_INT) {
      throw new RuntimeException("Short write of IntFile: " + file);
    }
  }

  /**
   * Writes a byte to the file at the specified offset.
   *
   * @param key The offset into the file in bytes.
   * @param b The byte to write.
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void putByte(long key, byte b) throws IOException {
    assert key >= 0;

    // Auto-expand.
    long lKey = key / SIZEOF_LONG;

    if (lKey >= fileSize) {
      if (b == 0) {
        return;
      }

      putLong(lKey, 0);
    }

    byteBuffer.limit(1);
    byteBuffer.rewind();
    byteBuffer.put(0, b);

    // TODO Handle ClosedChannelException and reopen FileChannel.
    if (fc.write(byteBuffer, key) != 1) {
      throw new RuntimeException("Short write of IntFile: " + file);
    }
  }

  /**
   * Truncates the file to zero length.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void clear() throws IOException {
    fileSize = 0;
    super.clear();
  }

  /**
   * Closes the file.
   *
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void close() throws IOException {
    byteBuffer = null;
    intBuffer = null;
    longBuffer = null;
    super.close();
  }

}
