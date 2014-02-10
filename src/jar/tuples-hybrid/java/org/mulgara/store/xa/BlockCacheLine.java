package org.mulgara.store.xa;

import org.apache.log4j.Logger;

import java.io.IOException;

import org.mulgara.query.TuplesException;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.DenseLongMatrix;
import org.mulgara.store.xa.Block;
import org.mulgara.store.xa.BlockFile;

/**
 * Split memory backed from file backed cache lines.
 *
 * @created 2004-03-24
 *
 * @author Andrae Muys
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:12 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class BlockCacheLine extends CacheLine {
  protected long[] currentTuple;
  protected long[] previousTuple;
  protected long[] prefix;
  protected BlockFile file;
  protected Block block;
  protected long initialBlockId;
  protected long nextBlockId;
  protected int nrBlocks;
  protected int blockSize;
  protected int offset;
  protected int width;
  protected int nextTuple;
  protected int tuplesPerBlock;

  private final static Logger logger = Logger.getLogger(BlockCacheLine.class);


  public BlockCacheLine(BlockFile file,
                        int blockSize,
                        DenseLongMatrix buffer,
                        int size) throws TuplesException {
    super(size);

    this.file = file;
    this.width = buffer.getWidth();

    if (width == 0) {
      throw new IllegalArgumentException("Attempt to materialize a tuple " +
          "with no variables.");
    }

    this.blockSize = blockSize;
    this.initialBlockId = file.getNrBlocks() - 1;
    if (this.initialBlockId < 0) {
      this.initialBlockId = 0;
    }
    this.currentTuple = new long[width];
    this.previousTuple = new long[width];
    try {
      appendBufferToFile(file, blockSize, buffer, size);
    } catch (IOException ie) {
      logger.warn("Failed to write Temporary File");
      throw new TuplesException("Failed to write Temporary File", ie);
    }

    this.nextBlockId = this.initialBlockId;
    try {
      this.block = file.readBlock(this.nextBlockId++);
    } catch (IOException ie) {
      logger.warn("Failed to read Temporary File");
      throw new TuplesException("Failed to read Temporary File", ie);
    }
    this.offset = 0;
  }


  public boolean isEmpty() {
    if (prefix == null) {
      return nextTuple > segmentSize;
    } else {
      return nextTuple > segmentSize || matchPrefix(currentTuple, prefix) != 0;
    }
  }


  public void advance() throws TuplesException {
    assert file != null;  // Indicates close() has been called.
    try {
      if (isEmpty()) {
        logger.debug("advancing empty tuples");
        block = null;
        return;
      }

      // If just cloned, then read in previous block.
      if (block == null) {
        if (logger.isDebugEnabled()) logger.debug("BlockCache " + this + " Refreshing from clone block " + (nextBlockId - 1));
        block = file.readBlock(nextBlockId - 1);
      }

      if (endOfBlock(offset)) {
        block = file.readBlock(nextBlockId++);
        offset = 0;
      }

      long[] tmp = previousTuple;
      previousTuple = currentTuple;
      currentTuple = tmp;
      offset = loadTupleFromBlock(currentTuple, block, offset);
      if (logger.isDebugEnabled()) {
        logger.debug("currentTuple: " + AbstractTuples.toString(currentTuple) + " new offset: " + offset);
      }

      nextTuple++;
    } catch (IOException ie) {
      logger.warn("IO Error accessing temporary file");
      throw new TuplesException("IO Error accessing temporary file", ie);
    }
  }


  public void reset(long[] prefix) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("Entering reset with prefix: " + AbstractTuples.toString(prefix));
    }
    super.reset(prefix);

    assert prefix.length <= width;

    this.prefix = prefix.length > 0 ? (long[])prefix.clone() : null;

    try {
      if (this.prefix == null) {
        block = file.readBlock(initialBlockId);
        offset = 0;
        nextTuple = 0;
      } else {
        block = findBlock(this.prefix);
        offset = findOffset(this.prefix, block);
        loadTupleFromBlock(currentTuple, block, offset);
        nextTuple = (int)(block.getBlockId() - initialBlockId) * tuplesPerBlock + (offset / width);
      }

      nextBlockId = block.getBlockId() + 1;
    } catch (IOException ie) {
      logger.warn("IO Error accessing temporary file", ie);
      throw new TuplesException("IO Error accessing temporary file", ie);
    }
  }


  public long[] getCurrentTuple(long[] tuple) {
    return currentTuple;
  }


  public long[] getPreviousTuple(long[] tuple) {
    return previousTuple;
  }


  public void close(int closer) throws TuplesException {
    super.close(closer);
    if (block != null) {
      block = null;
      file = null;
    }
  }


  public Object clone() {
    BlockCacheLine b = (BlockCacheLine)super.clone();
    b.block = null;
    b.currentTuple =  (long[])currentTuple.clone();
    b.previousTuple = (long[])previousTuple.clone();

    return b;
  }


  private int appendBufferToFile(BlockFile file, int blockSize,
                                  DenseLongMatrix buffer, int size)
                                  throws IOException {
    tuplesPerBlock = blockSize / (this.width * SIZEOF_LONG);
    // (n + d - 1) / d rounds up n/d.
    nrBlocks = (size + tuplesPerBlock - 1) / tuplesPerBlock;
    file.setNrBlocks(this.initialBlockId + nrBlocks + 1);

    long blockId = this.initialBlockId;
    Block block = file.allocateBlock(blockId++);
    int offset = 0;

    for (int i = 0; i < size; i++) {
      if (endOfBlock(offset)) {
        block.write();
        block = file.allocateBlock(blockId++);
        offset = 0;
      }
      for (int j = 0; j < this.width; j++) {
        block.putLong(offset++, buffer.get(i, j));
      }
    }
    block.write();

    return offset;
  }


  /**
   * @return true if there is no more room in block for another tuple.
   */
  private boolean endOfBlock(int offset) {
    return (offset + width) * SIZEOF_LONG > blockSize;
  }


  private int loadTupleFromBlock(long[] currentTuple, Block block, int offset) {
    for (int i = 0; i < currentTuple.length; i++) {
      currentTuple[i] = block.getLong(offset++);
    }

    return offset;
  }


  /**
   * Finds the block in the current cacheline containing the first tuple matching prefix.
   *
   * @param prefix Prefix to match
   * @return The block containing the current prefix.
   */
  private Block findBlock(long[] prefix) throws TuplesException {
    if (logger.isDebugEnabled()) logger.debug("Finding block matching prefix");
    try {
      assert prefix.length > 0 && prefix.length <= width;

      Block first = file.readBlock(initialBlockId);
      long[] tmp = new long[width];
      loadTupleFromBlock(tmp, first, 0);
      if (logger.isDebugEnabled()) {
        logger.debug("Initial tuple for block " + first.getBlockId() + " : " + AbstractTuples.toString(tmp));
      }
      if (compareBlockWithPrefix(first, prefix) >= 0) return first;

      final long lastBlockId = initialBlockId + nrBlocks - 1;
      Block last = file.readBlock(lastBlockId);
      boolean found;
      switch (compareBlockWithPrefix(last, prefix)) {
        case -1:
          return last;

        case 0:
          found = true;
          break;

        case +1:
          found = false;
          break;
          
        default:
          throw new IllegalStateException("compareBlockWithPrefix returned non-comparison");
      }

      Block result = findBlock(prefix, found, initialBlockId, first, lastBlockId, last, null);
      if (result == null) {
        throw new TuplesException("Failed to find block within valid range, is BlockCacheLine sorted?");
      }

      return result;
    } catch (IOException ei) {
      throw new TuplesException("IO Error while searching BlockCacheLine", ei);
    }
  }


  /**
   * Find the block with the smallest blockId containing a possible match on the prefix.
   *
   * INV: 1 &lt;= prefix.length &lt;= width.
   *      if !found then lowBlock &lt; prefix &lt; highBlock
   *      if found then lowBlock &lt; prefix == highBlock
   * @maintenanceAuthor barmintor
   */
  private Block findBlock(long[] prefix, boolean found, long lowBound, Block lowBlock, long highBound, Block highBlock, Block recycleBlock) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("finding Block with prefix: " + AbstractTuples.toString(prefix) + " found: " + found +
          " lowBound: " + lowBound + " highBound: " + highBound);
    }
    try {
      if (highBound - lowBound <= 1) return highBlock;

      long midBound = (int)((lowBound + highBound) / 2);
      Block midBlock = file.recycleBlock(midBound, recycleBlock);

      switch (compareBlockWithPrefix(midBlock, prefix)) {
        case -1:
          return findBlock(prefix, found, midBound, midBlock, highBound, highBlock, lowBlock);

        case 0:
          return findBlock(prefix, true, lowBound, lowBlock, midBound, midBlock, highBlock);

        case +1:
          assert !found;
          return findBlock(prefix, false, lowBound, lowBlock, midBound, midBlock, highBlock);

        default:
          throw new IllegalStateException("compareBlockWithPrefix returned non-comparison");
      }
    } catch (IOException ei) {
      throw new TuplesException("IO Error while searching BlockCacheLine", ei);
    }
  }


  private int compareBlockWithPrefix(Block block, long[] prefix) {
    assert prefix.length <= width;

    long[] first = new long[width];
    loadTupleFromBlock(first, block, 0);
    switch (matchPrefix(first, prefix)) {
      case -1:  // Smallest tuple in block < prefix
        long[] last = new long[width];
        loadTupleFromBlock(last, block, tupleIdToOffset(tuplesPerBlock - 1));
        switch (matchPrefix(last, prefix)) {
          case -1:   // Largest tuple in block < prefix
            return -1;  // Entire block < prefix
            
          case 0:    // Largest tuple in block == prefix
          case +1:   // Largest tuple in block > prefix
            if (logger.isDebugEnabled()) {
              logger.debug("Found prefix in block: " + block.getBlockId() + " prefix: " +
                  AbstractTuples.toString(prefix) + " first: " + AbstractTuples.toString(first) +
                  " last: " + AbstractTuples.toString(last));
            }
            return 0;   // Block ranges over prefix

          default:
            throw new IllegalStateException("matchPrefix returned non-comparison");
        }

      case 0:   // Smallest tuple in block == prefix
        return 0;   // Block includes prefix

      case +1:  // Smallest tuple in block > prefix
        return +1;  // Entire block > prefix

      default:
        throw new IllegalStateException("matchPrefix returned non-comparison");
    }
  }


  long[] readTupleFromBlock(long[] tuple, Block block, int tupleId) throws TuplesException {
    if (tupleId < 0) {
      throw new TuplesException("Error tupleId < 0 :" + tupleId);
    }
    if (tupleId > tuplesPerBlock) {
      throw new TuplesException("Error tupleId(" + tupleId + ") > tuplesPerBlock(" + tuplesPerBlock + ")");
    }

    if (tuple == null) {
      tuple = new long[width];
    }

    loadTupleFromBlock(tuple, block, tupleIdToOffset(tupleId));

    return tuple;
  }


  int tupleIdToOffset(int tupleId) {
    return tupleId * width;
  }

  /**
   * Returns the offset of the first tuple in a block matching the given prefix.
   *
   * @param prefix Prefix to match
   * @param block  Block to search
   * @return Offset of first match of prefix in block
   */
  private int findOffset(long[] prefix, Block block) throws TuplesException {
    assert prefix.length > 0 && prefix.length <= width;

    long[] first = readTupleFromBlock(null, block, 0);
    if (matchPrefix(first, prefix) >= 0) {
        return 0;
    }
    
    int highBound;

    long lastBlockId = initialBlockId + nrBlocks - 1;  // Note blockId's are 0-indexed.
    if (block.getBlockId() < lastBlockId) {
      highBound = tuplesPerBlock - 1;       // Last tuple in a full block.
    } else if (block.getBlockId() == lastBlockId) {
      int tupleIndexOfFirstTupleInLastBlock = tuplesPerBlock * (nrBlocks - 1);

      int numberOfTuplesInLastBlock = segmentSize - tupleIndexOfFirstTupleInLastBlock;

      highBound = numberOfTuplesInLastBlock - 1;
    } else {
      throw new TuplesException("BlockId past end of BlockCacheLine");
    }

    long[] last = readTupleFromBlock(null, block, highBound);
    boolean found;
    switch (matchPrefix(last, prefix)) {
      case -1:
        return tupleIdToOffset(highBound);

      case 0:
        found = true;
        break;

      case +1:
        found = false;
        break;
        
      default:
        throw new IllegalStateException("compareBlockWithPrefix returned non-comparison");
    }

    return findOffset(prefix, block, found, 0, highBound);
  }


  /**
   * Find the smallest offset with tuple &lt; prefix
   *
   * INV: 1 &lt;= prefix.length &lt;= width.
   *      if !found then lowBlock &lt; prefix &lt; highBlock
   *      if found then lowBlock &lt; prefix == highBlock
   */
  private int findOffset(long[] prefix, Block block, boolean found, int lowBound, int highBound) throws TuplesException {
    if (highBound - lowBound <= 1) {
      return tupleIdToOffset(highBound);
    }

    int midBound = (int)((lowBound + highBound) / 2);
    long[] tuple = readTupleFromBlock(null, block, midBound);

    switch (matchPrefix(tuple, prefix)) {
      case -1:
        return findOffset(prefix, block, found, midBound, highBound);

      case 0:
        return findOffset(prefix, block, true, lowBound, midBound);

      case +1:
        assert !found;
        return findOffset(prefix, block, false, lowBound, midBound);

      default:
        throw new IllegalStateException("compareBlockWithPrefix returned non-comparison");
    }
  }
}
