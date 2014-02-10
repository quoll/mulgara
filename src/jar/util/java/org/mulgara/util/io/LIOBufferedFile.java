/*
 * Copyright 2010 Paul Gearon
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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

/**
 * Object for reading and writing to files using ByteBuffers and standard IO. 
 * @author pag
 */
public class LIOBufferedFile extends LBufferedFile {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(LIOBufferedFile.class);

  private WeakHashMap<SystemBuffer,Long> offsets = new WeakHashMap<SystemBuffer,Long>();

  public LIOBufferedFile(RandomAccessFile file) {
    super(file);
  }

  @Override
  public ByteBuffer read(long offset, int length) throws IOException {
    ByteBuffer data = IOUtil.allocate(length);
    synchronized (file) {
      file.seek(offset);
      file.readFully(data.array());
    }
    return data;
  }

  @Override
  public ByteBuffer allocate(long offset, int length) {
    ByteBuffer data = IOUtil.allocate(length);
    offsets.put(new SystemBuffer(data), offset);
    return data;
  }

  @Override
  public void write(ByteBuffer data) throws IOException {
    synchronized (file) {
      Long offset = offsets.get(new SystemBuffer(data));
      // if the offset is unknown, then the caller must have done a seek()
      if (offset != null) file.seek(offset);
      file.write(data.array());
    }
  }

  @Override
  public void seek(long offset) throws IOException {
    file.seek(offset);
  }

  @Override
  public void registerRemapListener(Runnable l) {
    /* no-op */
  }

  @Override
  public void truncate(long offset) throws IOException {
    if (logger.isDebugEnabled()) logger.debug("Truncating IO file to " + offset);
    file.seek(offset - 1);
  }

  @Override
  public int getPageSize() {
    return 0;
  }

  /**
   * A wrapper class for allowing ByteBuffers to be mapped by == instead of their internal method.
   */
  class SystemBuffer {
    ByteBuffer buffer;
    public SystemBuffer(ByteBuffer b) { buffer = b; }
    public boolean equals(Object o) { return o != null && buffer == ((SystemBuffer)o).buffer; }
    public int hashCode() { return System.identityHashCode(buffer); }
  }

  // debug
  byte[] dump() throws IOException {
    byte[] d = new byte[(int)file.length()];
    long offset = file.getFilePointer();
    file.seek(0);
    file.read(d);
    file.seek(offset);
    return d;
  }
}
