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

package org.mulgara.store.xa;

// Java 2 standard packages
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;

import org.mulgara.util.io.MappingUtil;

/**
 * An implementation of BlockFile which uses memory mapped file IO.
 * Rather than mapping the entire file in one go, it gets mapped into
 * a series of regions.
 *
 * @created 2001-09-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/06/30 01:14:40 $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class MappedBlockFile extends AbstractBlockFile {

  /**
   * The size of each mapped region of the file. By mapping all files in
   * regions of this size we avoid fragmenting virtual memory.
   */
  public final static long REGION_SIZE = 8 * 1024 * 1024;

  /**
   * The system page size. The offset into a file that a mapped region starts
   * at will be a multiple of this value. If this value is not equal to or
   * greater than the actual hardware page size then attempts to map regions
   * from overlapping byte ranges of the file (i.e. if the block size is not a
   * power of two) may fail.
   */
  private final static long PAGE_SIZE = 8192;

  /** An initial number of mapped regions in the file. */
  private final static int INITIAL_NR_REGIONS = 1024;

  /** An array of mapped region buffers in the file. */
  private MappedByteBuffer[] mappedByteBuffers;

  /** Used as the source buffer when doing block moves between buffers. */
  private ByteBuffer[] srcByteBuffers;

  /** The mappedByteBuffers cast as integer buffers. */
  private IntBuffer[] intBuffers;

  /** The mappedByteBuffers cast as long buffers. */
  private LongBuffer[] longBuffers;

  /** The number of mapped regions in the file.  Equal to mappedByteBuffers.length.  */
  private int nrMappedRegions = 0;

  /** The distance from the beginning of one mapped region to the next. */
  private long stride;

  /**
   * Constructs a MappedBlockFile for the file with the specified file name.
   *
   * @param file the block file.
   * @param blockSize the size of blocks in the block file.
   * @throws IOException if an I/O error occurs.
   */
  MappedBlockFile(File file, int blockSize) throws IOException {
    super(file, blockSize);

    stride = REGION_SIZE;

    if (blockSize != (1 << XAUtils.log2(blockSize))) {
      // blockSize is not a power of 2.
      if (blockSize > (REGION_SIZE / 2)) {
        throw new IllegalArgumentException(
            "blockSize for " + file + " is too large: " + blockSize
        );
      }

      // Adjust stride so the mappings overlap by at least a blockSize.
      stride -= (((blockSize + PAGE_SIZE) - 1) & ~ (PAGE_SIZE - 1));
    } else if (blockSize > REGION_SIZE) {
      throw new IllegalArgumentException(
          "blockSize for " + file + " is too large: " + blockSize
      );
    }

    int nrRegions = (nrBlocks > 0) ?
        (int) ((((nrBlocks - 1) * blockSize) / stride) + 1) : 0;

    int maxNrRegions = INITIAL_NR_REGIONS;

    while (maxNrRegions < nrRegions) {
      maxNrRegions *= 2;
    }

    mappedByteBuffers = new MappedByteBuffer[maxNrRegions];
    srcByteBuffers = new ByteBuffer[maxNrRegions];
    intBuffers = new IntBuffer[maxNrRegions];
    longBuffers = new LongBuffer[maxNrRegions];

    if (nrRegions > 0) {
      mapFile(nrRegions);
    }
  }

  /**
   * Constructs a MappedBlockFile for the file with the specified file name.
   *
   * @param fileName the file name of the block file.
   * @param blockSize the size of blocks in the block file.
   * @throws IOException if an I/O error occurs.
   */
  MappedBlockFile(String fileName, int blockSize) throws IOException {
    this(new File(fileName), blockSize);
  }

  /**
   * Sets the length of the file in blocks. The file will not be expanded if it
   * is already large enough. The file will be truncated to the correct length
   * when {@link #close} is called.
   *
   * @param nrBlocks the length of the file in blocks.
   * @throws IOException if an I/O error occurs.
   */
  public void setNrBlocks(long nrBlocks) throws IOException {
    if (nrBlocks == this.nrBlocks) return;

    long prevNrBlocks = this.nrBlocks;
    super.setNrBlocks(nrBlocks);

    if (nrBlocks <= prevNrBlocks) return;

    // Call mapFile() if the file must grow in size.
    int nrRegions =
        (nrBlocks > 0) ? (int) ((((nrBlocks - 1) * blockSize) / stride) + 1) :
        0;

    if (nrRegions > nrMappedRegions) {
      mapFile(nrRegions);
    }
  }

  /**
   * Truncates the file to zero length.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void clear() throws IOException {
    int maxNrRegions = INITIAL_NR_REGIONS;
    mappedByteBuffers = new MappedByteBuffer[maxNrRegions];
    srcByteBuffers = new ByteBuffer[maxNrRegions];
    intBuffers = new IntBuffer[maxNrRegions];
    longBuffers = new LongBuffer[maxNrRegions];
    nrMappedRegions = 0;

    /*
    if (System.getProperty("os.name").startsWith("Win")) {
      // This is needed for Windows.
      System.gc();
      try { Thread.sleep(100); } catch (InterruptedException ie) { }
      System.runFinalization();
    }
    */

    super.clear();
  }

  /**
   * Ensures that all data for this BlockFile is stored in persistent storage
   * before returning.
   *
   * @throws IOException if an I/O error occurs.
   */
  public synchronized void force() throws IOException {
    for (int i = 0; i < nrMappedRegions; ++i) {
      mappedByteBuffers[i].force();
    }
  }

  /**
   * Allocates a ByteBuffer to be used for writing to the specified block. The
   * contents of the ByteBuffer are undefined.
   *
   * @param blockId The ID of the block that this buffer will be written to.
   * @return a ByteBuffer to be used for writing to the specified block.
   */
  public Block allocateBlock(long blockId) {
    return readBlock(blockId);
  }

  /**
   * Frees a buffer that was allocated by calling either {@link #allocateBlock}
   * or {@link #readBlock}. The buffer should not be used after it has been
   * freed.
   *
   * @param block the buffer to be freed.
   */
  public void releaseBlock(Block block) {
  }

  /**
   * Allocates a ByteBuffer which is filled with the contents of the specified
   * block. If the buffer is modified then the method {@link #writeBlock}
   * should be called to write the buffer back to the file.
   * @param blockId the block to read into the ByteBuffer.
   * @return The block that was read.
   */
  public Block readBlock(long blockId) {
    if ((blockId < 0) || (blockId >= nrBlocks)) {
      throw new IllegalArgumentException("blockId: " + blockId + " of " + nrBlocks);
    }

    long fileOffset = blockId * blockSize;
    int regionNr = (int) (fileOffset / stride);
    int offset = (int) (fileOffset % stride);
    assert mappedByteBuffers != null;
    assert srcByteBuffers != null;
    assert intBuffers != null;
    assert longBuffers != null;

    return Block.newInstance(
        this, blockSize, blockId, offset,
        mappedByteBuffers[regionNr], srcByteBuffers[regionNr],
        intBuffers[regionNr], longBuffers[regionNr]
    );
  }

  /**
   * Writes a buffer that was allocated by calling either {@link
   * #allocateBlock} or {@link #readBlock} to the specified block. The buffer
   * may only be written to the same block as was specified when the buffer was
   * allocated.
   *
   * @param block the buffer to write to the file.
   */
  public void writeBlock(Block block) {
    assert(block.getBlockId() >= 0) && (block.getBlockId() < nrBlocks);

    // NO-OP - this is because mapped buffers are automatically written
  }

  /**
   * Allocates a new block for dstBlockId, copies the contents of srcBlock to
   * it, releases srcBlock and returns the new Block.
   *
   * @param block The block to copy data from.  This block will be set to the
   *        destination block before returning, and acts as the return value.
   * @param dstBlockId the ID of the new Block to be allocated.
   */
  public void copyBlock(Block block, long dstBlockId) {
    assert(block.getBlockId() >= 0) && (block.getBlockId() < nrBlocks);
    assert(dstBlockId >= 0) && (dstBlockId < nrBlocks);
    assert block.getBlockId() != dstBlockId;

    int byteOffset = block.getByteOffset();
    ByteBuffer srcBuffer = block.getSourceBuffer();
    srcBuffer.limit(byteOffset + blockSize);
    srcBuffer.position(byteOffset);

    long dstFileOffset = dstBlockId * blockSize;
    int dstRegionNr = (int) (dstFileOffset / stride);
    int dstByteOffset = (int) (dstFileOffset % stride);
    ByteBuffer dstBuffer = mappedByteBuffers[dstRegionNr];
    dstBuffer.position(dstByteOffset);

    // Copy the bytes.
    dstBuffer.put(srcBuffer);

    block.init(
        dstBlockId, dstByteOffset, dstBuffer,
        srcByteBuffers[dstRegionNr], intBuffers[dstRegionNr],
        longBuffers[dstRegionNr]
    );
  }


  /**
   * Attempt to re-use the given Block and wrapped ByteBuffer to read the indicated block.
   * null ByteBuffer will behave like readBlock.
   * @author barmintor
   * @param blockId The block to read into the ByteBuffer.
   * @param block The ByteBuffer to attempt to re-use
   * @return The buffer that was read.
   * @throws IOException if an I/O error occurs.
   */
  public Block recycleBlock(long blockId, Block block) throws IOException {
   return readBlock(blockId);
  }


  /**
   * Discards all file mappings, allowing the garbage collector to unmap the file.
   */
  public synchronized void unmap() {
    // Discard the file mappings.
    MappingUtil.release(mappedByteBuffers);
    
    mappedByteBuffers = null;
    srcByteBuffers = null;
    intBuffers = null;
    longBuffers = null;
    nrMappedRegions = 0;
  }

  /**
   * Expands the file to contain nrRegions regions and maps the additional
   * regions.
   *
   * @param nrRegions The number of regions to expand to.  Must be greater than the current
   *        number of regions.
   * @throws IOException if an I/O error occurs.
   */
  private synchronized void mapFile(int nrRegions) throws IOException {
    assert nrRegions > nrMappedRegions;

    // Check if the buffer arrays need to be increased in size.
    int maxNrRegions = mappedByteBuffers.length;

    if (maxNrRegions < nrRegions) {
      do {
        maxNrRegions *= 2;
      } while (maxNrRegions < nrRegions);

      MappedByteBuffer[] mbbs = new MappedByteBuffer[maxNrRegions];
      ByteBuffer[] sbbs = new ByteBuffer[maxNrRegions];
      IntBuffer[] ibs = new IntBuffer[maxNrRegions];
      LongBuffer[] lbs = new LongBuffer[maxNrRegions];
      System.arraycopy(mappedByteBuffers, 0, mbbs, 0, nrMappedRegions);
      System.arraycopy(srcByteBuffers, 0, sbbs, 0, nrMappedRegions);
      System.arraycopy(intBuffers, 0, ibs, 0, nrMappedRegions);
      System.arraycopy(longBuffers, 0, lbs, 0, nrMappedRegions);
      mappedByteBuffers = mbbs;
      srcByteBuffers = sbbs;
      intBuffers = ibs;
      longBuffers = lbs;
    }

    long mappedSize =
        (nrMappedRegions > 0) ?
        (((nrMappedRegions - 1) * stride) + REGION_SIZE) : 0;

    // Get the current file size.
    long currentFileSize = 0L;

    for (;;) {
      try {
        currentFileSize = fc.size();

        break;
      } catch (ClosedChannelException ex) {
        // The Channel may have been inadvertently closed by another thread
        // being interrupted.  Attempt to reopen the channel.
        if (!ensureOpen()) {
          throw ex;
        }

        // Loop back and retry the size().
      }
    }

    if (currentFileSize < mappedSize) {
      throw new Error("File has shrunk: " + file);
    }

    long fileSize = ((nrRegions - 1) * stride) + REGION_SIZE;

    // Expand the file.
    for (;;) {
      try {
        raf.setLength(fileSize);

        break;
      } catch (ClosedChannelException ex) {
        // The Channel may have been inadvertently closed by another thread
        // being interrupted.  Attempt to reopen the channel.
        if (!ensureOpen()) {
          throw ex;
        }

        // Loop back and retry the setLength().
      }
    }

    for (int regionNr = nrMappedRegions; regionNr < nrRegions; ++regionNr) {
      int retries = 10;

      for (;;) {
        try {
          MappedByteBuffer mbb = fc.map(
              FileChannel.MapMode.READ_WRITE, regionNr * stride, REGION_SIZE
          );
          mbb.order(byteOrder);
          mappedByteBuffers[regionNr] = mbb;
          srcByteBuffers[regionNr] = mbb.asReadOnlyBuffer();
          mbb.rewind();
          intBuffers[regionNr] = mbb.asIntBuffer();
          mbb.rewind();
          longBuffers[regionNr] = mbb.asLongBuffer();

          break;
        } catch (ClosedChannelException ex) {
          // The Channel may have been inadvertently closed by another thread
          // being interrupted.  Attempt to reopen the channel.
          if (!ensureOpen()) {
            throw ex;
          }

          // Loop back and retry the map().
        } catch (IOException ex) {
          // Rethrow the exception if we are out of retries.
          if (retries-- == 0) {
            throw ex;
          }

          // Let some old mappings go away and try again.
          MappingUtil.systemCleanup();
        }
      }
    }

    nrMappedRegions = nrRegions;
  }
  
}
