/*
 * Copyright 2009 Andrae Muys
 * Licensed under the Open Software Licence  3.0
 */

package org.mulgara.store.tuples;

// Java packages
import java.util.List;
import java.util.Set;

// Local packages
import org.mulgara.query.Variable;

/**
 * Allows a partial order to be specified over a given tuples.
 * The returned tuples variable ordering will comply with the specified partial-order.
 *
 * @author <a href="http://netymon.com/people/staff/andrae">Andrae Muys</a>
 * @copyright &copy;2009 <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 */

public class ReorderAnnotation implements Annotation {
  public ReorderAnnotation() {
  }

  /**
   * Returns a new tuples identical to this tuples, but with variables
   * reordered compliant with the specified partial order.
   */
  public Tuples reorder(List<Set<Variable>> partialOrder) {
    throw new UnsupportedOperationException("reorder unimplemented");
  }
}
