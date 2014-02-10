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

/**
 * Object for reading and writing to files using ByteBuffers and standard IO. 
 * @author pag
 */
public class LReadOnlyIOBufferedFile extends LBufferedFile {

  public LReadOnlyIOBufferedFile(RandomAccessFile file) {
    super(file);
  }

  @Override
  public ByteBuffer read(long offset, int length) throws IOException {
    ByteBuffer data = ByteBuffer.allocate(length);
    synchronized (file) {
      file.seek(offset);
      file.readFully(data.array());
    }
    return data;
  }

  @Override
  public ByteBuffer allocate(long offset, int length) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void write(ByteBuffer data) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void seek(long offset) throws IOException {
    file.seek(offset);
  }

  @Override
  public void truncate(long offset) throws IOException {
    /* This API does not modify the file. */
  }

  @Override
  public void registerRemapListener(Runnable l) {
    /* no-op */
  }

  @Override
  public int getPageSize() {
    return 0;
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
