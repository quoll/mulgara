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

package org.mulgara.store.tuples;

// Java 2 standard packages
import java.util.*;

// Third party packages
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 *
 * @created 2003-01-09
 *
 * @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/03/07 17:28:28 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LiteralTuples extends AbstractTuples {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(LiteralTuples.class.getName());

  private List<long[]> tuples;

  private int currentTuple;
  private boolean[] columnContainsUnbound;
  private long[] prefix;
  private boolean sorted;
  private boolean strictClose = false;
  private boolean closed = false;

  /**
   * Creates an optionally sorted LiteralTuples instance, with an option for strict handling of
   * the close operation.
   * @param variableNames The variables for this tuples.
   * @param sorted <code>true</code> if the rows in this tuples will be sorted.
   * @param strictClose If <code>true</code>, an IllegalStateException will be thrown if the
   *   tuples is closed more than once.
   */
  public LiteralTuples(String[] variableNames, boolean sorted, boolean strictClose) {
    List<Variable> vars = new ArrayList<Variable>();
    for (int i = 0; i < variableNames.length; i++) {
      Variable v = new Variable(variableNames[i]);
      assert!vars.contains(v);
      vars.add(v);
    }
    init((Variable[]) vars.toArray(new Variable[0]), sorted);
    this.strictClose = strictClose;
  }
  
  /**
   * Creates an optionally sorted LiteralTuples instance, which will not throw an exception
   * if closed multiple times.  Equivalent to calling <code>LiteralTuples(variableNames, sorted, false)</code>.
   * @param variableNames The variables for this tuples.
   * @param sorted <code>true</code> if the rows in the tuples will be sorted.
   */
  public LiteralTuples(String[] variableNames, boolean sorted) {
    this(variableNames, sorted, false);
  }

  /**
   * Creates a literal tuples with specified variables.
   */
  public LiteralTuples(Variable[] variables) {
    init(variables, false);
  }

  /**
   * Creates a literal tuples with specified variables.
   * Variables created to match variableNames[].
   */
  public LiteralTuples(String[] variableNames) {
    this(variableNames, false);
  }

  private void init(Variable[] variables, boolean sorted) {
    tuples = new ArrayList<long[]>();
    currentTuple = 0;
    setVariables(Arrays.asList(variables));
    columnContainsUnbound = new boolean[variables.length];
    Arrays.fill(columnContainsUnbound, false);
    this.sorted = sorted;
  }

  /**
   * Create a new set of tuples.
   *
   * @param vars String[] the variable columns.
   * @param tuplesValues long[][] the values.
   * @return LiteralTuples the newly created tuples.
   * @throws TuplesException if there was an error creating them.
   */
  public static LiteralTuples create(String[] vars, long[][] tuplesValues)
      throws TuplesException {
    LiteralTuples tuples = new LiteralTuples(vars);
    for (int i = 0; i < tuplesValues.length; i++) {
      tuples.appendTuple(tuplesValues[i]);
    }
    return tuples;
  }


  public void appendTuple(long[] tuple) throws TuplesException {
    if (tuple.length != getNumberOfVariables()) {
      throw new TuplesException("Arity of rows dosn't match arity of tuples");
    }

    for (int i = 0; i < tuple.length; i++) {
      if (tuple[i] == Tuples.UNBOUND) {
        columnContainsUnbound[i] = true;
      }
    }

    tuples.add(tuple.clone());
  }

  public long getColumnValue(int column) throws TuplesException {
    assert column >= 0;
    if (tuples == null) {
      throw new TuplesException("getColumnValue called before beforeFirst()");
    }

    return tuples.get(currentTuple)[column];
  }

  public long getRowCount() throws TuplesException {
    return tuples.size();
  }

  public long getRowUpperBound() throws TuplesException {
    return getRowCount();
  }

  public long getRowExpectedCount() throws TuplesException {
    return getRowCount();
  }

  public boolean isEmpty() throws TuplesException {
    return tuples.isEmpty();
  }

  public boolean isColumnEverUnbound(int column) throws TuplesException {
    return columnContainsUnbound[column];
  }

  public boolean isMaterialized() {
    return true;
  }

  public List<Tuples> getOperands() {
    return new ArrayList<Tuples>(0);
  }

  public RowComparator getComparator() {
    if (sorted) {
      return DefaultRowComparator.getInstance();
    } else {
      return null;
    }
  }

  //
  // Methods implementing Tuples interface
  //

  public void beforeFirst() throws TuplesException {
    beforeFirst(Tuples.NO_PREFIX, 0);
  }

  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
//    StackTrace th = new StackTrace();
//    th.fillInStackTrace();
//    logger.debug("LiteralTuples[" + Arrays.asList(getVariables()) + "].beforeFirst called with prefix " + toString(prefix) + "\n" + th);
//    logger.debug("LiteralTuples[" + Arrays.asList(getVariables()) + "].beforeFirst called with prefix " + toString(prefix));

    assert suffixTruncation == 0;

    this.prefix = (long[]) prefix.clone();

    search:for (currentTuple = 0; currentTuple < tuples.size(); currentTuple++) {
      for (int j = 0; j < prefix.length; j++) {
        if (tuples.get(currentTuple)[j] != prefix[j]) {
          continue search;
        }
      }
      // Found prefix;
      currentTuple -= 1;
      break;
    }
  }

  public boolean next() throws TuplesException {
    if (++currentTuple < tuples.size()) {
      for (int j = 0; j < prefix.length; j++) {
        if (tuples.get(currentTuple)[j] != prefix[j]) {
          return false;
        }
      }

      return true;
    }

    return false;
  }

  public void close() throws TuplesException {
    if (closed && strictClose) {
      throw new IllegalStateException("Attempt to close a LiteralTuples twice.");
    }
    closed = true;
  }

  public boolean hasNoDuplicates() throws TuplesException {
    return sorted || isUnconstrained();
  }

  public Object clone() {
    return super.clone();
  }
}
