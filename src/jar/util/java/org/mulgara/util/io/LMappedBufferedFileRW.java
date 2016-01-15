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
 * A memory mapped read-write version of LBufferedFile. This is not safe for transactional work.
 * @author pag
 */
public class LMappedBufferedFileRW extends LMappedBufferedFile {

  /** Logger. */
  static final Logger logger = Logger.getLogger(LMappedBufferedFileRW.class);

  /** The page size to use. */
  public static final int PREFERRED_PAGE_SIZE;

  /** The size of the page for this object. */
  protected final int pageSize;

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
    PREFERRED_PAGE_SIZE = tmp;
  }
  
  /**
   * Create a new buffered file for Long seeks.
   * @param file The file to buffer.
   * @throws IOException There was an error setting up initial mapping of the file.
   */
  public LMappedBufferedFileRW(RandomAccessFile file, int recordSize) throws IOException {
    super(file);
    fc = file.getChannel();
    int recordsPerPage = PREFERRED_PAGE_SIZE / recordSize;
    pageSize = recordsPerPage * recordSize;
    mapFile();
    if (logger.isDebugEnabled()) {
      logger.debug("Mapping files for R/W with pages of " + pageSize + "bytes at "+ recordsPerPage + " records/page");
    }
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public ByteBuffer read(long offset, int length) throws IOException {
    // get the offset into the last buffer, this may be negative if before the last page
    long lastPageOffset = (offset + (long)length) - (pageSize * (long)(buffers.length - 1));
    // if the offset is larger than the final page, then remap
    if (lastPageOffset > pageSize || lastPageOffset > buffers[buffers.length - 1].limit() || buffers.length == 0) {
      mapFile();
    }
    int page = (int)(offset / pageSize);
    int page_offset = (int)(offset % pageSize);

    assert page_offset + length <= pageSize : "Access to block outside of record boundaries";

    ByteBuffer bb = buffers[page];
    bb.position(page_offset);
    bb.limit(page_offset + length);
    return bb.slice();
  }

  @Override
  FileChannel.MapMode getMode() {
    return FileChannel.MapMode.READ_WRITE;
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
