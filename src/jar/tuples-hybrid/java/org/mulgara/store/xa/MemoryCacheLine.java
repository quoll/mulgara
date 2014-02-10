package org.mulgara.store.xa;

import org.apache.log4j.Logger;

import org.mulgara.query.TuplesException;
import org.mulgara.store.tuples.DenseLongMatrix;

/**
 *
 *
 * @created 2004-03-22
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
public class MemoryCacheLine extends CacheLine {
  protected DenseLongMatrix cache;

  protected long[] currentTuple;
  protected int    current;
  protected int    previous;
  protected long[] prefix;
  protected int    index;
  protected int    width;
  private   long[] pivotTuple;

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(MemoryCacheLine.class);


  public MemoryCacheLine(DenseLongMatrix buffer, int size) {
    super(size);

    this.cache = buffer;
    this.index = 0;
    this.currentTuple = new long[buffer.getWidth()];
    this.current = 0;
//    this.previousTuple = null;
    this.previous = -1;
    this.width = buffer.getLength();
    this.pivotTuple = new long[buffer.getWidth()];
  }


  public boolean isEmpty() {
    if (prefix == null) {
      return index >= segmentSize;
    } else {
      return index >= segmentSize || matchPrefix(currentTuple, prefix) != 0;
    }
  }


  public void advance() {
//    logger.warn("advance() called on " + System.identityHashCode(this));
    index++;
    previous = current;
    current = (index < segmentSize) ? index : 0;
    currentTuple = getCurrentTuple(currentTuple);
  }


  public void reset(long[] prefix) throws TuplesException {
    super.reset(prefix);
//    logger.warn("reset() called on " + System.identityHashCode(this));

    assert prefix.length <= cache.getWidth();

    this.prefix = prefix.length > 0 ? (long[])prefix.clone() : null;

    if (this.prefix == null) {
      index = -1;
    } else {
      index = findPrefix(this.prefix) - 1;
    }
  }


  public long[] getCurrentTuple(long[] tuple) {
    return cache.loadRow(tuple, current);
  }

  public long[] getPreviousTuple(long[] tuple) {
    return cache.loadRow(tuple, previous);
  }

  public void close(int closer) throws TuplesException {
    super.close(closer);
//    logger.warn("Closed " + System.identityHashCode(this));
    cache = null;
    current = -1;
    previous = -1;
  }


  public Object clone() {
    // FIXME: This is probably not correct behaviour, there is probabaly state that needs duplicating.
    MemoryCacheLine copy = (MemoryCacheLine)super.clone();
    copy.currentTuple = (long[])currentTuple.clone();
    copy.pivotTuple = (long[])pivotTuple.clone();

//    logger.warn("Cloned " + System.identityHashCode(this) + " -> " + System.identityHashCode(copy));
    return copy;
  }


  private int findPrefix(long[] prefix) {
    int index = findPrefix(prefix, 0, segmentSize - 1, -1);

    return index >= 0 ? index : segmentSize;
  }

  private int findPrefix(long[] prefix, int left, int right, int found) {
    if (left > right) {
      return found;
    }

    int pivot = left + ((right - left) / 2);

    switch (matchPrefix(cache.loadRow(pivotTuple, pivot), prefix)) {
      case -1:
        return findPrefix(prefix, pivot + 1, right, found);
      case 0:
        return findPrefix(prefix, left, pivot - 1, pivot);
      case +1:
        return findPrefix(prefix, left, pivot - 1, found);
    }

    return 0;
  }
}
