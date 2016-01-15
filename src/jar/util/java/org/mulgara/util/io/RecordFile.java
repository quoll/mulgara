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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A file for storing records of fixed length.
 */
public interface RecordFile extends Closeable {

  /**
   * Retrieves a ByteBuffer containing a given record number.
   * This is a read-only operation, and will not grow the file
   * if the record does not exist.
   * @param id The record number.
   * @return The record data.
   * @throws IndexOutOfBoundsException if the id is out of the range of the file.
   * @throws IOException if there is an error accessing the file.
   */
  public ByteBuffer get(long id) throws IndexOutOfBoundsException, IOException;

  /**
   * Retrieves a block associated with a record in the file.
   * The data in the ByteBuffer is not defined.
   * @param id The record number.
   * @return A ByteBuffer associated with the record. The contents may be anything.
   * @throws IndexOutOfBoundsException if the if is out of the range of the file.
   * @throws IOException if there is an error accessing the file.
   */
  public ByteBuffer getBuffer(long id) throws IOException;

  /**
   * Puts a record into a file. The ByteBuffer <em>must</em> already be associated
   * with the block ID. This is not checked, due to performance, but if it is not
   * true, then the operation of this method is undefined.
   * @param buffer The data to write to the record.
   * @param id The record number.
   * @return The ByteBuffer that was written.
   * @throws IndexOutOfBoundsException If the id is out of range for the file.
   * @throws IOException If there is an error writing to the file.
   */
  public ByteBuffer put(ByteBuffer buffer, long id) throws IndexOutOfBoundsException, IOException;
  
  /**
   * Changes the file size to contain the number of request records.
   * Any buffer associations may be invalidated.
   * @param records The number of records to hold in the file.
   * @return The original RecordFile object.
   * @throws IOException If there is an error modifying the file.
   */
  public RecordFile resize(long records) throws IOException;
}
