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
package org.mulgara.worm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.mulgara.util.io.FileHashMap;
import org.mulgara.util.io.IOUtil;

import static org.mulgara.util.io.Bytes.LONG_SIZE;
import static org.mulgara.util.io.Bytes.INT_SIZE;
import static org.mulgara.util.io.Constants.UTF8_CHARSET;

/**
 * Maps a ByteBuffer to Long and back.
 */
public class DataMap {

  /** The internal map that maps ByteBuffer to ByteBuffer */
  private final FileHashMap map;

  private static final int HASH_CODE_OFFSET = 0;
  private static final int LENGTH_OFFSET = HASH_CODE_OFFSET + INT_SIZE;
  private static final int STR_START_OFFSET = LENGTH_OFFSET + INT_SIZE;
  
  private static final int KEY_LENGTH = STR_START_OFFSET + LONG_SIZE;

  
  /**
   * Creates a map for Long to ByteBuffer and back.
   * @param f The file to put the table into.
   * @throws IOException If there is an error accessing the file.
   */
  public DataMap(File f) throws IOException {
    map = new FileHashMap(f, LONG_SIZE, LONG_SIZE);
  }

  /**
   * Creates a hash map for Long to Long.
   * @param f The file to put the table into.
   * @param loadFactor The fraction of the table above with the table will get expanded.
   * @param initialSize The initial number of slots available to put data into before being rehashed.
   * @throws IOException If there is an error accessing the file.
   */
  public DataMap(File f, float loadFactor, long initialSize) throws IOException {
    map = new FileHashMap(f, LONG_SIZE, LONG_SIZE, loadFactor, initialSize);
  }


  public long getId(String data) {
    byte[] b = data.getBytes(UTF8_CHARSET);
    return getId(b);
  }
  
  public long getId(byte[] data) {
    ByteBuffer keyBuffer = IOUtil.allocate(KEY_LENGTH);
    IntBuffer kib = keyBuffer.asIntBuffer();
    kib.put(data.hashCode());
    kib.put(data.length);
    keyBuffer.position(STR_START_OFFSET);
    keyBuffer.put(data);
    map.get(keyBuffer);
    return 0;
  }
  
}
