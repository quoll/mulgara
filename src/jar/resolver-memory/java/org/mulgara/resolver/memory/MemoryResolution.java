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

package org.mulgara.resolver.memory;

// Java 2 standard packages
import java.util.*;

// Third party packages
// import org.apache.log4j.Logger;      // Apache Log4J

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Tuples;

/**
 * {@link Resolution} from the Java heap.
 *
 * @created 2004-04-28
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.10 $
 * @modified $Date: 2005/05/02 20:07:57 $ @maintenanceAuthor $Author: raboczi $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MemoryResolution extends AbstractTuples implements Resolution {
  /** Logger. */
  // private static Logger logger = Logger.getLogger(MemoryResolution.class.getName());

  /**
   * The constraint this instance resolves.
   */
  private final Constraint constraint;

  /**
   * The unmodifiable set of {@link Stating}s from which to filter the
   * resolution statements.
   *
   * This is cloned from the <var>statingSet</var> parameter passed to the
   * constructor, so as to avoid side effects when models are subsequently
   * modified.
   */
  private final Set<Stating> statingSet;

  /**
   * The {@link Iterator} use to obtain the {@link #stating} from the
   * {@link #statingSet}.
   */
  private Iterator<Stating> iterator;

  /**
   * The current stating, <code>null</code> if not on a row.
   */
  private Stating stating = null;

  /**
   * Which column of the current {@link #stating} provides the value of each
   * column of the current resolution tuple row.
   */
  private final int[] columnIndex;

  /**
   * The prefix passed into the most recent invocation of {@link #beforeFirst}
   */
  private long[] prefix;

  //
  // Constructors
  //

  /**
   * Construct the resolution to a constraint from a set of {@link Stating}s.
   *
   * @param constraint  the constraint to resolver, never <code>null</code>
   * @param statingSet  the {@link Stating}s to filter for constraint
   *   satisfaction, never <code>null</code>
   * @throws IllegalArgumentException if the <var>constraint</var> or
   *   <var>statingSet</var> are <code>null</code>
   */
  MemoryResolution(Constraint constraint, Set<Stating> statingSet) {
    // Validate "constraint" parameter
    if (constraint == null) {
      throw new IllegalArgumentException( "Null \"constraint\" parameter");
    }

    // Validate "statingSet" parameter
    if (statingSet == null) {
      throw new IllegalArgumentException( "Null \"statingSet\" parameter");
    }

    // Initialize fields
    this.constraint = constraint;

    // Clone the statings, for isolation from subsequent changes
    synchronized (statingSet) {
      this.statingSet = Collections.unmodifiableSet(new HashSet<Stating>(statingSet));
    }

    // Calculate columnIndex and set the variable list
    int length = 0;
    int[] temp = new int[4];
    List<Variable> variableList = new ArrayList<Variable>(4);
    for (int i=0; i<4; i++) {
      if (constraint.getElement(i) instanceof Variable) {
        temp[length++] = i;
        variableList.add((Variable)constraint.getElement(i));
      }
    }
    columnIndex = new int[length];
    for (int i=0; i<length; i++) columnIndex[i] = temp[i];
    setVariables(variableList);
  }

  //
  // Methods implementing Resolution
  //

  public Constraint getConstraint() {
    return constraint;
  }

  public boolean isComplete() {
    return false;
  }

  //
  // Methods implementing Cursor (superinterface of Statements)
  //

  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    // Validate "prefix" parameter
    if (prefix == null) throw new IllegalArgumentException("Null \"prefix\" parameter");
    if (prefix.length > 4) {
      throw new TuplesException("Bad prefix (>4): " + toString(prefix));
    }

    // Validate "suffixTruncation" parameter
    if (suffixTruncation != 0)  throw new IllegalArgumentException("Nonzero suffix truncationr");

    // Reset fields
    iterator    = statingSet.iterator();
    this.prefix = prefix;
    stating     = null;
  }

  public Object clone() {
    // Copy mutable fields by value (all fields are immutable)
    return (MemoryResolution) super.clone();
  }

  /**
   * Close the RDF/XML formatted input stream.
   */
  public void close() throws TuplesException {
    // null implementation
  }

  /**
   * @param column  0 for the subject, 1 for the predicate, 2 for the object
   */
  public long getColumnValue(int column) throws TuplesException {
    if (stating == null) throw new TuplesException("Not on a row");

    if (column < 0 || column >= columnIndex.length) {
      throw new TuplesException("No such column: " + column);
    }

    return stating.get(columnIndex[column]);
  }

  @SuppressWarnings("unchecked")
  public List<Tuples> getOperands() {
    return (List<Tuples>)Collections.EMPTY_LIST;
  }

  public long getRowCount() throws TuplesException {
    return statingSet.size();
  }

  public long getRowUpperBound() throws TuplesException {
    return getRowCount();
  }

  public long getRowExpectedCount() throws TuplesException {
    return getRowCount();
  }

  public boolean isEmpty() throws TuplesException {
    return statingSet.isEmpty();
  }

  public boolean hasNoDuplicates() throws TuplesException {
    return false;
  }

  public boolean isColumnEverUnbound(int column) throws TuplesException {
    switch (column) {
    case 0: case 1: case 2: case 3:
      return false;
    default:
      throw new TuplesException("No such column " + column);
    }
  }

  public boolean next() throws TuplesException {
    if (iterator == null) {
      throw new TuplesException("Haven't called beforeFirst");
    }

    // Advance to the next stating that satisfies the constraint
    filtering_statings: while (iterator.hasNext()) {
      stating = (Stating) iterator.next();
      for (int i = 0; i < prefix.length; i++) {
        if (prefix[i] != stating.get(columnIndex[i])) {
          continue filtering_statings;
        }
      }
      for (int i = 0; i < 4; i++) {
        if (constraint.getElement(i) instanceof LocalNode &&
            ((LocalNode)constraint.getElement(i)).getValue() != stating.get(i)) {
          continue filtering_statings;
        }
      }
      return true;
    }

    return false;
  }
}
