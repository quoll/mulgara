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

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

// Java 2 standard packages
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;
import org.mulgara.util.io.MappingUtil;

/**
 * An abstract class that represents a file which consists of a number of
 * blocks that are all the same size. <p>
 *
 * The implementations are only thread-safe if there is no more than one thread
 * writing to the file at any given time and if the writing thread does not
 * write to blocks being read by the reading threads and does not make the file
 * smaller than the size required by the reading threads.
 *
 * @created 2001-09-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/06/30 01:14:39 $ @maintenanceAuthor $Author: pgearon $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class AbstractBlockFile implements BlockFile {

  /** The endianness of this computer. */
  public final static ByteOrder byteOrder;

  /** The logger. */
  private final static Logger logger = Logger.getLogger(AbstractBlockFile.class);

  /** All the open files accessed as block files. */
  private static Set<File> openFiles = new HashSet<File>();

  /** Determine the byte order of this machine, and select an ordering to use. */
  static {
    String useByteOrderProp = System.getProperty("mulgara.xa.useByteOrder");
    ByteOrder bo = ByteOrder.nativeOrder();

    // Default.
    if (useByteOrderProp != null) {
      if (useByteOrderProp.equalsIgnoreCase("native")) {
        bo = ByteOrder.nativeOrder();
      } else if (useByteOrderProp.equalsIgnoreCase("big_endian")) {
        bo = ByteOrder.BIG_ENDIAN;
      } else if (useByteOrderProp.equalsIgnoreCase("little_endian")) {
        bo = ByteOrder.LITTLE_ENDIAN;
      } else {
        logger.warn(
            "Invalid value for property mulgara.xa.useByteOrder: " +
            useByteOrderProp
        );
      }
    }

    byteOrder = bo;
  }

  /** The file of the BlockFile. */
  protected File file;

  /** A flag to indicate that the file is currently open. */
  protected boolean isOpen = true;

  /** The RandomAccessFile for this BlockFile. */
  protected RandomAccessFile raf;

  /** The FileChannel for this BlockFile. */
  protected volatile FileChannel fc;

  /** The size of a block in bytes. */
  protected int blockSize;

  /** The size of the file in blocks. */
  protected long nrBlocks;

  /**
   * This constructor is only used by subclasses.  Opens the file and will round it
   * to an integral number of blocks if necessary.
   *
   * @param file The block file.
   * @param blockSize The size of blocks in the block file.
   * @throws IOException If an I/O error occurs.
   */
  protected AbstractBlockFile(File file, int blockSize) throws IOException {
    if (blockSize <= 0) {
      throw new IllegalArgumentException("blockSize is zero or negative.");
    }

    this.file = file;
    this.blockSize = blockSize;

    synchronized (openFiles) {
      if (openFiles.contains(file)) {
        throw new IllegalArgumentException("File already open: " + file);
      }

      ensureOpen();

      // Truncate the file to a multiple of blockSize if its size is not
      // already a multiple of blockSize.
      long fileSize = nrBlocks * blockSize;

      if (fileSize < fc.size()) {
        if (logger.isInfoEnabled()) {
          logger.info(
              "File size was not a multiple of blockSize: \"" + file + "\"."
          );
        }
        MappingUtil.truncate(fc, fileSize);
      }

      openFiles.add(file);
    }
  }

  /**
   * Returns a BlockFile which represents an open file with the specified file
   * name. If the file does not exist it will be created. This factory method
   * allows the type of implementation to be selected.
   *
   * @param file the file to open.
   * @param blockSize the size of a block in bytes.
   * @param ioType The type of access to use on the file, memory mapped or with explicit IO.
   * @return the open BlockFile.
   * @throws IOException if an I/O error occurs.
   */
  public static BlockFile openBlockFile(
      File file, int blockSize, IOType ioType
  ) throws IOException {
    String forceIOTypeProp = System.getProperty("mulgara.xa.forceIOType");

    if (forceIOTypeProp != null) {
      if (forceIOTypeProp.equalsIgnoreCase("mapped")) {
        ioType = IOType.MAPPED;
      } else if (forceIOTypeProp.equalsIgnoreCase("explicit")) {
        ioType = IOType.EXPLICIT;
      } else {
        logger.warn(
            "Invalid value for property mulgara.xa.forceIOType: " +
            forceIOTypeProp
        );
      }
    }

    if (ioType == IOType.MAPPED) {
      return new MappedBlockFile(file, blockSize);
    } else if (ioType == IOType.EXPLICIT) {
      return new IOBlockFile(file, blockSize);
    } else {
      throw new IllegalArgumentException("Invalid BlockFile ioType.");
    }
  }

  /**
   * Returns a BlockFile which represents an open file with the specified file
   * name. If the file does not exist it will be created. This factory method
   * allows the type of implementation to be selected.
   *
   * @param fileName the name of the file to open.
   * @param blockSize the size of a block in bytes.
   * @param ioType The type of access to use on the file, memory mapped or with explicit IO.
   * @return the open BlockFile.
   * @throws IOException if an I/O error occurs.
   */
  public static BlockFile openBlockFile(
      String fileName, int blockSize, IOType ioType
  ) throws IOException {
    return openBlockFile(new File(fileName), blockSize, ioType);
  }

  /**
   * Sets the length of the file in blocks. An implementation may defer
   * changing the file size until a read or write operation is performed or the
   * file is closed and may allow the actual disk file to be larger or smaller
   * than this size while it is open but will set the file to this size when it
   * is closed.
   *
   * @param nrBlocks the length of the file in blocks.
   * @throws IOException if an I/O error occurs.
   * @throws IllegalArgumentException if nrBlocks is invalid.
   */
  public void setNrBlocks(long nrBlocks) throws IOException {
    if (nrBlocks < 0) {
      throw new IllegalArgumentException("nrBlocks is negative.");
    }

    this.nrBlocks = nrBlocks;
  }

  /**
   * Gets the current length of the BlockFile in blocks. An implementation may
   * allow the actual disk file to be larger or smaller than this size while it
   * is open but will set the file to this size when it is closed.
   *
   * @return the current length of the BlockFile in blocks.
   */
  public final long getNrBlocks() {
    return nrBlocks;
  }

  /**
   * Truncates the file to zero length.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void clear() throws IOException {
    this.nrBlocks = 0;

    try {
      MappingUtil.truncate(fc, 0L);
      fc.force(true);
    } catch (ClosedChannelException ex) {
      // The Channel may have been inadvertently closed by another thread
      // being interrupted.  Attempt to reopen the channel.
      if (!ensureOpen()) {
        throw ex;
      }
    }
  }

  /**
   * Ensures that all data for this BlockFile is stored in persistent storage
   * before returning.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void force() throws IOException {
    try {
      fc.force(true);
    } catch (ClosedChannelException ex) {
      // The Channel may have been inadvertently closed by another thread
      // being interrupted.  Attempt to reopen the channel.
      if (!ensureOpen()) {
        throw ex;
      }
    }
  }

  /**
   * Normally used for copy-on-write, but not implemented for non-ManagedBlockFiles.
   *
   * @param block The Block to modify.  Ignored.
   */
  public void modifyBlock(Block block) {
    throw new UnsupportedOperationException(
        "modifyBlock() can only be applied to Blocks obtained from" +
        " a ManagedBlockFile."
    );
  }

  /**
   * Normally used for freeing blocks that are no longer used, but not implemented for
   * non-ManagedBlockFiles.
   *
   * @param blockId The block to free.  Ignored.
   */
  public void freeBlock(long blockId) {
    throw new UnsupportedOperationException(
        "Only Blocks obtained from a ManagedBlockFile can be freed."
    );
  }


  /**
   * Unmaps a file.  Only applies to mapped files, as used by {@link MappedBlockFile}.
   */
  public void unmap() {
    // NO-OP
  }


  /**
   * Closes the block file.
   *
   * @throws IOException if an I/O error occurs
   */
  public synchronized void close() throws IOException {
    if (!isOpen || (raf == null)) {
      if (logger.isInfoEnabled()) {
        logger.info("Attempt to close BlockFile that is already closed.");
      }
      return;
    }

    try {
      unmap();

      try {
        MappingUtil.truncate(fc, nrBlocks * blockSize);
      } catch (ClosedChannelException ex) {
        // The Channel may have been inadvertently closed by another thread
        // being interrupted.  Attempt to reopen the channel.
        if (!ensureOpen()) {
          throw ex;
        }
      } catch (IOException ex) {
        // The size should be corrected the next time the file is opened.
        if (logger.isInfoEnabled()) {
          logger.info(
              "NOTE: Could not truncate file: \"" + file +
              "\" to size: " + (nrBlocks * blockSize) +
              " - deferring until next time the file is opened."
          );
        }
      }
    } finally {
      try {
        try {
          if (fc != null && fc.isOpen()) fc.close();
        } finally {
          fc = null;
          raf.close();
        }
      } finally {
        raf = null;

        synchronized (openFiles) {
          openFiles.remove(file);
        }
      }
    }
  }


  /**
   * Close and delete the block file.
   *
   * @throws IOException if an I/O error occurs
   */
  public synchronized void delete() throws IOException {
    try {
      close();
    } finally {
      if (file != null) {
        int retries = 10;

        while (!file.delete() && file.isFile() && retries-- > 0) {
          // Causing any MappedByteBuffers to be unmapped may allow the
          // file to be deleted.  This may be needed for Windows.
          MappingUtil.systemCleanup();
        }

        if (retries < 0) {
          logger.warn("Failed to delete: " + file);
        }

        file = null;
      }
    }
  }


  /**
   * Checks that a file is open.  If it is not then attempts to open the file.
   * Opening the file will also determine the size and number of blocks in the file.
   *
   * @return <code>true</code> if the file is open as a post-condition.i
   *         <code>false</code> if the file was not open and could not be opened.
   * @throws IOException If there was an I/O error opening the file or getting its size.
   */
  protected synchronized boolean ensureOpen() throws IOException {
    // FIXME reenable aborting on interrupt when rollback is fixed.
    // consume interrupt.
    Thread.interrupted();

    // if (Thread.interrupted()) throw new InterruptedIOException();
    if (fc != null && fc.isOpen()) {
      return true;
    }

    // Already open.
    if (!isOpen) {
      // File was closed deliberately or a previous reopen attempt failed.
      return false;
    }

    // Make sure that the RandomAccessFile is closed.
    isOpen = false;

    if (raf != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Reopening: " + file);
      }

      try {
        raf.close();
      } finally {
        raf = null;
      }
    }

    // Try to open the file.
    raf = new RandomAccessFile(file, "rw");

    FileChannel newFC = raf.getChannel();

    nrBlocks = newFC.size() / blockSize;
    fc = newFC;
    isOpen = true;

    return true;
  }

}
