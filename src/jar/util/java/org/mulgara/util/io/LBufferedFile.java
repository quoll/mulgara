/*
 * Copyright 2010 Paula Gearon
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

import org.apache.log4j.Logger;

/**
 * An abstraction for reading and writing to files with Long offsets.
 * @author Paula Gearon
 *
 */
public abstract class LBufferedFile {

  private final static Logger logger = Logger.getLogger(LBufferedFile.class);

  /** The property for the io type. May be "mapped" or "explicit" */
  public static final String IO_TYPE_PROP = "mulgara.xa.forceIOType";

  /** Enumeration of the different io types */
  enum IOType { MAPPED, EXPLICIT };

  private static final IOType ioType;

  static {
    // initialize the type of file access to use: memory mapped files, or explicit read/write operations
    // configured with mulgara.xa.forceIOType
    String forceIOTypeProp = System.getProperty(IO_TYPE_PROP, "mapped");

    if (forceIOTypeProp.equalsIgnoreCase(IOType.MAPPED.name())) {
      ioType = IOType.MAPPED;
    } else if (forceIOTypeProp.equalsIgnoreCase(IOType.EXPLICIT.name())) {
      ioType = IOType.EXPLICIT;
    } else {
      logger.warn("Invalid value for property mulgara.xa.forceIOType: " + forceIOTypeProp);
      ioType = IOType.MAPPED;
    }

  }


  /** The file being accessed */
  protected RandomAccessFile file;

  /**
   * Creates new buffered access for a file.
   * @param file the file to provide buffered access for.
   */
  LBufferedFile(RandomAccessFile file) {
    this.file = file;
  }

  /**
   * Reads a buffer from a file, at a given position.
   * @param offset The offset to get the buffer from.
   * @param length The required size of the buffer.
   * @return The buffer that was read.
   */
  public abstract ByteBuffer read(long offset, int length) throws IOException;

  /**
   * Create a buffer that will be used for writing to a file at a given location.
   * @param offset The location in the file that the buffer will write to.
   * @param length The size of the buffer.
   * @return The buffer for accessing that part of the file.
   */
  public abstract ByteBuffer allocate(long offset, int length) throws IOException;

  /**
   * Puts the contents of a buffer into the file. This will go back to wherever
   * it was supposed to go.
   * @param data The buffer to be written.
   */
  public abstract void write(ByteBuffer data) throws IOException;

  /**
   * Moves the current file position.
   * @param offset The new position in the file.
   */
  public abstract void seek(long offset) throws IOException;

  /**
   * @return The page size used internally by the implementation, or 0 if not paged.
   */
  public abstract int getPageSize();

  /**
   * Truncates the file to a given size, or prepares the file for truncation
   * if it is read-only.
   * @param offset The offset into the file to trancate the size to.
   */
  public abstract void truncate(long offset) throws IOException;

  /**
   * Closes the file resource.
   */
  public void close() throws IOException {
    file.close();
  }

  /**
   * Ensures that all data written to this file is forced to disk.
   * @throws IOException If there is an IO error accessing the disk.
   */
  public void force() throws IOException {
    file.getChannel().force(false);
  }

  /**
   * Register a listener that will be called when Remaps occur.
   * The implementation of this class may not do any remapping.
   * @param l The listener to register.
   */
  public abstract void registerRemapListener(Runnable l);

  static LBufferedFile createWritable(RandomAccessFile f, int recordSize) throws IOException {
    if (ioType == IOType.MAPPED) {
      return new LMappedBufferedFileRW(f, recordSize);
    } else if (ioType == IOType.EXPLICIT) {
      return new LIOBufferedFile(f);
    } else {
      throw new IllegalArgumentException("Invalid BlockFile ioType.");
    }
  }

  /**
   * Create a buffered file, taking over the RandomAccessFile that is provided.
   * @param f The file to provide buffered access to.
   * @return The buffered file.
   * @throws IOException An error opening the file.
   */
  static LBufferedFile createReadOnly(RandomAccessFile f) throws IOException {
    if (ioType == IOType.MAPPED) {
      return new LMappedBufferedFileRO(f);
    } else if (ioType == IOType.EXPLICIT) {
      return new LReadOnlyIOBufferedFile(f);
    } else {
      throw new IllegalArgumentException("Invalid BlockFile ioType.");
    }
  }

  /**
   * Create a buffered file using a filename.
   * @param fileName The name of the file to provide buffered access to.
   * @return The readonly buffered file.
   * @throws IOException An error opening the file.
   */
  public static LBufferedFile createWritable(String fileName, int recordSize) throws IOException {
    return createWritable(new RandomAccessFile(fileName, "rw"), recordSize);
  }

  /**
   * Create a buffered file using a filename.
   * @param fileName The file to provide buffered access to.
   * @return The readonly buffered file.
   * @throws IOException An error opening the file.
   */
  public static LBufferedFile createWritable(File file, int recordSize) throws IOException {
    return createWritable(new RandomAccessFile(file, "rw"), recordSize);
  }

  /**
   * Create a buffered file using a filename.
   * @param fileName The name of the file to provide buffered access to.
   * @return The readonly buffered file.
   * @throws IOException An error opening the file.
   */
  public static LBufferedFile createReadOnly(String fileName) throws IOException {
    return createReadOnly(new RandomAccessFile(fileName, "r"));
  }

  /**
   * Create a buffered file using a filename.
   * @param file The file to provide buffered access to.
   * @return The readonly buffered file.
   * @throws IOException An error opening the file.
   */
  public static LBufferedFile createReadOnly(File file) throws IOException {
    return createReadOnly(new RandomAccessFile(file, "r"));
  }

  // debug
  abstract byte[] dump() throws IOException;
}
