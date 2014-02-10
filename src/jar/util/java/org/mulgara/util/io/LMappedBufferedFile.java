/*
 * Copyright 2011 Paul Gearon
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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Represents a memory mapped file.
 */
public abstract class LMappedBufferedFile extends LBufferedFile {

  /** Logger. */
  static final Logger logger = Logger.getLogger(LMappedBufferedFile.class);

  /** The name of the property for setting the page size */
  protected static final String PAGE_SIZE_PROP = "org.mulgara.io.pagesize";

  /** The objects that want to know when the file gets remapped */
  protected List<Runnable> listeners = new ArrayList<Runnable>();

  /** The channel to the file being accessed */
  protected FileChannel fc;

  /** All the pages of mapped buffers */
  protected MappedByteBuffer[] buffers;

  /**
   * Create a mapped file.
   * @param file The open file to be accessed by mapping.
   */
  public LMappedBufferedFile(RandomAccessFile file) {
    super(file);
  }

  /**
   * Gets a buffer that reflects the actual contents within a file.
   * @see org.mulgara.util.io.LBufferedFile#read(long, int)
   */
  @Override
  abstract public ByteBuffer read(long offset, int length) throws IOException;

  /**
   * Gets a buffer that reflects the actual contents within a file without trying to read it.
   * Identical to {@link #read(long, int)} for a mapped file.
   * @see org.mulgara.util.io.LBufferedFile#read(long, int)
   */
  @Override
  public ByteBuffer allocate(long offset, int length) throws IOException {
    return read(offset, length);
  }

  /**
   * Pushes the buffer data to disk
   * @see org.mulgara.util.io.LBufferedFile#write(java.nio.ByteBuffer)
   */
  @Override
  public void write(ByteBuffer data) throws IOException {
    // no-op, even if read/write
  }

  /**
   * Controls where in a file to read or write a buffer. Irrelevant for mapped buffers.
   * @see org.mulgara.util.io.LBufferedFile#seek(long)
   */
  @Override
  public void seek(long offset) throws IOException {
    // no-op
  }

  /**
   * Doesn't actually truncate the file, but unmaps the file so that it can be truncated.
   * @param offset The offset into the file to trancate the size to.
   */
  public void truncate(long size) throws IOException {
    if (logger.isDebugEnabled()) logger.debug("Truncating file to " + size + ". Current size is " + fc.size());
    
    if (size == fc.size()) return;
    if (size > fc.size()) throw new IOException("Unable to truncate a mapped file larger");
    
    // Windows doesn't want to truncate a file with any portion mapped, so unmap the whole thing.
    if (MappingUtil.isWindows()) {
      if (logger.isDebugEnabled()) logger.debug("Unmapping entire file for Windows.");
      MappingUtil.release(buffers);
      buffers = new MappedByteBuffer[0];
      for (Runnable listener: listeners) listener.run();
      return;
    }
  
    // if truncating to an address above the mapping, then return
    int fullBuffers = buffers.length - 1;
    int pageSize = getPageSize();
    if (fullBuffers >= 0 && size >= fullBuffers * pageSize + buffers[fullBuffers].limit()) return;
  
    // get all the pages, including a possible partial page
    int pages = (int)((size + pageSize - 1) / pageSize);
    // get all the full pages. Either pages or pages-1
    int fullPages = (int)(size / pageSize);
  
    if (logger.isDebugEnabled()) logger.debug("Existing file holds " + buffers.length + " pages. Truncated file needs " + pages + " pages (" + fullPages + " full pages)");
    // if the data is fully mapped then there is nothing to do
    if (fullPages == buffers.length) return;
  
    // check that this really is a truncation
    assert pages <= buffers.length;
  
    // This will be the set of buffers to use in the end
    MappedByteBuffer[] newBuffers;
  
    if (pages == buffers.length) {
      // can't be on a page boundary, else that would mean there was no truncation
      // since it would either be truncated to the same size, or larger.
      assert fullPages < pages;
      // need to remap the final page
      // leave the last page, to cover any reads that may still be trying to read it
      // buffers[pages - 1] = null;
      newBuffers = buffers;
      if (logger.isDebugEnabled()) logger.debug("Remapping final page only");
    } else {
      assert pages < buffers.length;
      // need to drop all of the last pages
      // leave the pages in the buffer to cover any reads that may still be active on them
      // for (int b = fullPages; b < buffers.length; b++) buffers[b] = null;
      // truncate the buffers array
      newBuffers = new MappedByteBuffer[pages];
      System.arraycopy(buffers, 0, newBuffers, 0, fullPages);
      if (logger.isDebugEnabled()) logger.debug("dropped " + (buffers.length - fullPages) + " pages, saved " + fullPages);
    }
  
    // if there's a partial page at the end, then map it
    if (fullPages < pages) {
      assert fullPages == pages - 1;
      newBuffers[fullPages] = fc.map(getMode(), fullPages * pageSize, size % pageSize);
      if (logger.isDebugEnabled()) logger.debug("Remapped final partial page");
    }
  
    // switch over from the previous buffers to the new ones
    // anyone reading from anything but the partial buffer at the end is
    // accessing the wrong transaction.
    MappedByteBuffer[] tmpBuffers = buffers;
    buffers = newBuffers;
  
    // We're about to lose the last reference to these buffers when tmpBuffers
    // goes away, but by putting the following code here anyone looking can see
    // which buffers we're trying to eliminate.
    // Note that this will remove any partial buffer at the end, even if most
    // of it has been remapped into a new partial buffer.
  
    // Setting these buffers to null is a belt and suspenders approach.
    // This code is informative, rather than necessary
    // Update: commented out, to reduce the risk of out-of-order access on buffers, without needing to declare them "volatile"
    // if (tmpBuffers != buffers) {
    //   for (int b = fullPages; b < tmpBuffers.length; b++) tmpBuffers[b] = null;
    // }
    if (logger.isDebugEnabled()) logger.debug("Removed " + (tmpBuffers.length - fullPages) + " pages");
  
    // tell the listeners that we've remapped
    for (Runnable listener: listeners) listener.run();
  }

  /**
   * Map the entire file, using the given page size.
   * @param pageSize The size of the pages when mapping.
   * @throws IOException Due to an error in file access.
   */
  synchronized void mapFile() throws IOException {
    int pageSize = getPageSize();
    long size = fc.size();
    // get all the pages, including a possible partial page
    int pages = (int)((size + pageSize - 1) / pageSize);
    // get all the full pages. Either pages or pages-1
    int fullPages = (int)(size / pageSize);

    // create a larger buffers array, with all the original buffers in it
    // except any partial pages
    MappedByteBuffer[] newBuffers = new MappedByteBuffer[pages];
    int start = 0;
    if (buffers != null) {
      int topBuffer = buffers.length - 1;
      if (topBuffer == -1 || buffers[topBuffer].limit() == pageSize) {
        // last buffer full
        topBuffer++;
      } else {
        // last buffer is partial
      }
      System.arraycopy(buffers, 0, newBuffers, 0, topBuffer);
      start = topBuffer;
    }

    // fill in the rest of the new array
    FileChannel.MapMode mode = getMode();
    for (int page = start; page < fullPages; page++) {
      newBuffers[page] = fc.map(mode, page * pageSize, pageSize);
    }
    // if there's a partial page at the end, then map it
    if (fullPages < pages) newBuffers[fullPages] = fc.map(mode, fullPages * pageSize, size % pageSize);

    buffers = newBuffers;

    // tell the listeners that we've remapped
    for (Runnable listener: listeners) listener.run();
  }

  /**
   * Registers a listener for the remap event.
   * @see org.mulgara.util.io.LBufferedFile#registerRemapListener(java.lang.Runnable)
   */
  @Override
  public void registerRemapListener(Runnable l) {
    listeners.add(l);
  }

  /**
   * Returns the size of each block of mapped data.
   * @see org.mulgara.util.io.LBufferedFile#getPageSize()
   */
  @Override
  public abstract int getPageSize();

  /**
   * Describes the read/write mode of the file.
   * @return either {@link FileChannel.MapMode#READ_WRITE} or {@link FileChannel.MapMode#READ_ONLY}.
   */
  abstract FileChannel.MapMode getMode();
}
