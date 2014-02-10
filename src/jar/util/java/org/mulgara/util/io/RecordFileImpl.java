/*
 * Copyright 2010 Paul Gearon.
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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * An implementation of RecordFile.
 * @author Paul Gearon
 */
public class RecordFileImpl implements RecordFile {

  /** The buffered file containing the records. */
  private final LBufferedFile recordFile;

  /** The RandomAccessFile the recordFile is based on. */
  private final RandomAccessFile raf;

  /** The size of the file records. */
  private final int recordSize;

  /**
   * Creates a new record file.
   * @param filename The name of the file to create.
   * @param recordSize The size of the records in the file.
   * @param initialBlocks The initial size of the file, in records.
   * @throws IOException If there was an error creating the file.
   */
  public RecordFileImpl(String filename, int recordSize, long initialBlocks) throws IOException {
    this(new File(filename), recordSize, initialBlocks);
  }

  /**
   * Creates a new record file.
   * @param file The file to create.
   * @param recordSize The size of the records in the file.
   * @param initialBlocks The initial size of the file, in records.
   * @throws IOException If there was an error creating the file.
   */
  public RecordFileImpl(File file, int recordSize, long initialBlocks) throws IOException {
    raf = new RandomAccessFile(file, "rw");
    recordFile = LBufferedFile.createWritable(raf, recordSize);
    this.recordSize = recordSize;
    resize(initialBlocks);
  }

  /**
   * @see java.io.Closeable#close()
   */
  public void close() throws IOException {
    raf.getChannel().force(true);
    raf.close();
  }

  /**
   * @see org.mulgara.util.io.RecordFile#get(long)
   */
  @Override
  public ByteBuffer get(long id) throws IndexOutOfBoundsException, IOException {
    return recordFile.read(id * recordSize, recordSize);
  }

  /**
   * @see org.mulgara.util.io.RecordFile#getBuffer(long)
   */
  @Override
  public ByteBuffer getBuffer(long id) throws IOException {
    return recordFile.allocate(id * recordSize, recordSize);
  }

  /**
   * @see org.mulgara.util.io.RecordFile#put(java.nio.ByteBuffer, long)
   */
  public ByteBuffer put(ByteBuffer buffer, long id)
      throws IndexOutOfBoundsException, IOException {
    recordFile.write(buffer);
    return buffer;
  }

  /**
   * @see org.mulgara.util.io.RecordFile#resize(long)
   */
  public RecordFile resize(long records) throws IOException {
    long fileSize = records * recordSize;
    // if the file is to be shrunk, then prepare the file for truncation
    if (fileSize < raf.length()) recordFile.truncate(fileSize);
    raf.setLength(fileSize);
    return this;
  }

  // debug
  byte[] dump() throws IOException {
    return recordFile.dump();
  }
}
