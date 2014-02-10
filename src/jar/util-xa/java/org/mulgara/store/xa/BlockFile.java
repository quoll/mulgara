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
import java.io.*;

// Third party packages
import org.apache.log4j.Logger;

/**
 * This interface provides access to files using {@link Block}s of data which are
 * all the same size.
 * <p>
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
 * @modified $Date: 2005/06/30 01:14:40 $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001-2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface BlockFile {

  /**
   * Sets the length of the file in blocks. An implementation may defer changing
   * the file size until a read or write operation is performed or the file is
   * closed and may allow the actual disk file to be larger or smaller than this
   * size while it is open but will set the file to this size when it is closed.
   *
   * @param nrBlocks the length of the file in blocks.
   * @throws IOException if an I/O error occurs.
   * @throws IllegalArgumentException if nrBlocks is invalid.
   */
  public void setNrBlocks(long nrBlocks) throws IOException;

  /**
   * Gets the current length of the BlockFile in blocks. An implementation may
   * allow the actual disk file to be larger or smaller than this size while it
   * is open but will set the file to this size when it is closed.
   *
   * @return the current length of the BlockFile in blocks.
   */
  public long getNrBlocks();

  /**
   * Truncates the file to zero length.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void clear() throws IOException;

  /**
   * Ensures that all data for this BlockFile is stored in persistent storage
   * before returning.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void force() throws IOException;

  /**
   * Allocates a ByteBuffer to be used for writing to the specified block. The
   * contents of the ByteBuffer are undefined. The method {@link #writeBlock}
   * should be called to write the buffer to the block but, depending on the
   * BlockFile implementation, changes to the ByteBuffer may take effect even if
   * {@link #writeBlock} is never called.
   *
   * @param blockId The ID of the block that this buffer will be written to.
   * @return a ByteBuffer to be used for writing to the specified block.
   * @throws IOException if an I/O error occurs.
   */
  public Block allocateBlock(long blockId) throws IOException;

  /**
   * Allocates a ByteBuffer which is filled with the contents of the specified
   * block. If the buffer is modified then the method {@link #writeBlock} should
   * be called to write the buffer back to the file but, depending on the
   * BlockFile implementation, changes to the ByteBuffer may take effect even if
   * {@link #writeBlock} is never called.
   *
   * @param blockId the block to read into the ByteBuffer.
   * @return The buffer that was read.
   * @throws IOException if an I/O error occurs.
   */
  public Block readBlock(long blockId) throws IOException;

  /**
   * Writes a buffer that was allocated by calling either {@link #allocateBlock}
   * or {@link #readBlock} to the specified block. The buffer may only be
   * written to the same block as was specified when the buffer was allocated.
   *
   * @param block the buffer to write to the file.
   * @throws IOException if an I/O error occurs.
   */
  public void writeBlock(Block block) throws IOException;

  /**
   * Changes the block ID of the specified Block. This method is called
   * copyBlock because a call to copyBlock() followed by a call to writeBlock()
   * can be used to copy the contents of a block to a new location in the block
   * file.
   *
   * @param block the Block to be copied.
   * @param dstBlockId the ID of the block to which the Block will be written
   *      when writeBlock() is called.
   * @throws IOException if an I/O error occurs.
   */
  public void copyBlock(Block block, long dstBlockId) throws IOException;

  /**
   * Copies the internal buffer of a block to a new part of the file, and modifies
   * the block to refer to the new buffer instead of the original.  Used for
   * implementing copy-on-write functionality.
   *
   * @param block The block to be duplicated.
   * @throws IOException if an I/O error occurs.
   */
  public void modifyBlock(Block block) throws IOException;

  /**
   * Attempt to re-use the given Block and wrapped ByteBuffer to read the indicated block.
   * null ByteBuffer will behave like readBlock.
   * @author barmintor
   * @param blockId The block to read into the ByteBuffer.
   * @param block The ByteBuffer to attempt to re-use
   * @return The buffer that was read.
   * @throws IOException if an I/O error occurs.
   */
  public Block recycleBlock(long blockId, Block block) throws IOException;

  /**
   * Releases a block.  This is used when a block is no longer needed.
   *
   * @param blockId The block to free.
   * @throws IOException if an I/O error occurs.
   */
  public void freeBlock(long blockId) throws IOException;

  /**
   * Tries to unmap a file from memory, if the file is memory mapped.
   */
  public void unmap();

  /**
   * Closes the block file.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void close() throws IOException;

  /**
   * Closes and deletes the block file.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void delete() throws IOException;

  /**
   * An enumeration that represents a type of BlockFile implementation.
   */
  public static class IOType {

    /** Specifies that memory-mapped file IO should be used.  */
    public final static IOType MAPPED = new IOType("Mapped");

    /** Specifies that explicit file IO should be used.  */
    public final static IOType EXPLICIT = new IOType("Explicit");

    /** Describes a default IO type to use. */
    public final static IOType DEFAULT;

    /** Describes the IO type to use when set to auto.  This is calculated based on the system type. */
    public final static IOType AUTO;

    /**
     * Logger.
     */
    private final static Logger logger = Logger.getLogger(IOType.class);

    /** Calculate the automatic and default IO types. */
    static {
      String archProp = System.getProperty("sun.arch.data.model");
      AUTO = ((archProp != null) && archProp.equals("64")) ? MAPPED : EXPLICIT;

      IOType defIOType = AUTO;

      String defIOTypeProp = System.getProperty("mulgara.xa.defaultIOType");
      if (defIOTypeProp != null) {
        if (defIOTypeProp.equalsIgnoreCase("mapped")) {
          defIOType = MAPPED;
        } else if (defIOTypeProp.equalsIgnoreCase("explicit")) {
          defIOType = EXPLICIT;
        } else if (defIOTypeProp.equalsIgnoreCase("auto")) {
          defIOType = AUTO;
        } else {
          logger.warn(
              "Invalid value for property mulgara.xa.defaultIOType: " +
              defIOTypeProp
          );
        }
      }

      DEFAULT = defIOType;
      logger.info("Default IO Type: " + DEFAULT);
    }

    /** The string representation of an instance. */
    private String str;

    /**
     * Constructs a Type instance with the specified string representation.
     *
     * @param str The string representation of this type in the enumeration.
     */
    private IOType(String str) {

      this.str = str;
    }

    /**
     * Returns a string representation of the IOType instance.
     *
     * @return the string representation of the IOType instance.
     */
    public String toString() {

      return str;
    }
  }
}
