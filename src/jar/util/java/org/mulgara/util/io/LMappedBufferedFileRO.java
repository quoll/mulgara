/*
 * Copyright 2011 Paula Gearon
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
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

/**
 * A memory mapped read-only version of LBufferedFile
 * @author pag
 */
public class LMappedBufferedFileRO extends LMappedBufferedFile {

  /** Logger. */
  static final Logger logger = Logger.getLogger(LMappedBufferedFileRO.class);

  /** The page size to use. */
  public static final int PAGE_SIZE;

  /** The size of a page to be mapped */
  private static final int DEFAULT_PAGE_SIZE = 33554432; // 32 MB

  static {
    String pageSizeStr = System.getProperty(PAGE_SIZE_PROP);
    int tmp = DEFAULT_PAGE_SIZE;
    try {
      if (pageSizeStr != null) tmp = Integer.parseInt(pageSizeStr);
    } catch (NumberFormatException e) {
      logger.warn("Property [" + PAGE_SIZE_PROP + "] is not a number [" + pageSizeStr + "]. Using default: " + tmp);
    }
    PAGE_SIZE = tmp;
  }
  
  /**
   * Create a new buffered file for Long seeks.
   * @param file The file to buffer.
   * @throws IOException There was an error setting up initial mapping of the file.
   */
  public LMappedBufferedFileRO(RandomAccessFile file) throws IOException {
    super(file);
    fc = file.getChannel();
    mapFile();
    if (logger.isDebugEnabled()) logger.debug("Mapping files with pages of: " + PAGE_SIZE);
  }

  @Override
  public int getPageSize() {
    return PAGE_SIZE;
  }

  @Override
  public ByteBuffer read(long offset, int length) throws IOException {
    // get the offset into the last buffer, this may be negative if before the last page
    long lastPageOffset = (offset + length) - (PAGE_SIZE * (buffers.length - 1));
    // if the offset is larger than the final page, then remap
    if (lastPageOffset > PAGE_SIZE || lastPageOffset > buffers[buffers.length - 1].limit()) {
      mapFile();
    }
    int page = (int)(offset / PAGE_SIZE);
    int page_offset = (int)(offset % PAGE_SIZE);
    if (page_offset + length <= PAGE_SIZE) {
      ByteBuffer bb = buffers[page].asReadOnlyBuffer();
      bb.position(page_offset);
      bb.limit(page_offset + length);
      if (page == buffers.length - 1) {
        // In the final page, so make a copy
        // This is needed in case of rollback, because the final page
        // has the potential of being needed by a read-only transaction
        // even when we need to remove that page to truncate the file.
        // We have not synched on this page, but the GC will retry removing it
        // with short delays, so it should get picked up despite our brief use.
        ByteBuffer data = ByteBuffer.allocate(length);
        bb.get(data.array(), 0, length);
        return data;
      } else {
        // normal return of the mapped region slice
        return bb.slice();
      }
    } else {
      // TODO data must become a new write-through type
      ByteBuffer data = ByteBuffer.allocate(length);
      byte[] dataArray = data.array();
      ByteBuffer tmp = buffers[page].asReadOnlyBuffer();
      tmp.position(page_offset);
      int firstSlice = PAGE_SIZE - page_offset;
      tmp.get(dataArray, 0, firstSlice);
      tmp = buffers[page + 1].asReadOnlyBuffer();
      tmp.get(dataArray, firstSlice, length - firstSlice);
      return data;
    }
  }

  @Override
  FileChannel.MapMode getMode() {
    return FileChannel.MapMode.READ_ONLY;
  }

  byte[] dump() {
    byte[] d = new byte[buffers[0].capacity()];
    int p = buffers[0].position();
    buffers[0].position(0);
    buffers[0].get(d);
    buffers[0].position(p);
    return d;
  }
}
