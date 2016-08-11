/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.log4j.Logger;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.mulgara.query.rdf.BlankNodeImpl;

/**
 * An Answer that represents a graph.
 *
 * @created Jun 30, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class GraphAnswer extends AbstractAnswer implements Answer, Serializable {

  /** The serialization ID. */
  private static final long serialVersionUID = -5499236950928116988L;

  /** Logger */
  private static final Logger logger = Logger.getLogger(GraphAnswer.class.getName());

  /** The variable name for the first column. */
  private static final String CONSTANT_VAR_SUBJECT = "subject";

  /** The variable name for the second column. */
  private static final String CONSTANT_VAR_PREDICATE = "predicate";

  /** The variable name for the third column. */
  private static final String CONSTANT_VAR_OBJECT = "object";

  /** The first column variable. */
  private static final Variable SUBJECT_VAR = new Variable(CONSTANT_VAR_SUBJECT);

  /** The second column variable. */
  private static final Variable PREDICATE_VAR = new Variable(CONSTANT_VAR_PREDICATE);

  /** The third column variable. */
  private static final Variable OBJECT_VAR = new Variable(CONSTANT_VAR_OBJECT);

  /** An array containing the variable. */
  private static final Variable[] CONSTANT_VAR_ARR = new Variable[] { SUBJECT_VAR, PREDICATE_VAR, OBJECT_VAR };

  /** The raw answer to wrap. */
  private Answer rawAnswer;

  /** The column counter for emulating rows. */
  private int colOffset = 0;

  /** The number of rows per solution. A solution is a row from the raw answer. */
  private final int rowsPerSoln;

  /** The normal variables that must be bound. */
  private Map<Variable,List<Integer>> stdVarIndexes;

  /** The bnode generating variables. */
  private Map<Variable,List<Integer>> bnodeVars;

  /** All the nodes generated for a single solution */
  private Object[] rowNodes;

  /** Internal counter for generating blank node identifiers */
  private long blankNodeId = 0;

  /**
   * Constructs a new BooleanAnswer.
   * @param rawAnswer The result this answer represents.
   */
  public GraphAnswer(Answer rawAnswer) {
    int cols = rawAnswer.getNumberOfVariables();
    if (cols % 3 != 0) throw new IllegalArgumentException("Cannot construct a graph with " + cols + " columns.");
    rowsPerSoln = cols / 3;
    this.rawAnswer = rawAnswer;
    rowNodes = new Object[cols];

    stdVarIndexes = new HashMap<Variable,List<Integer>>();
    bnodeVars = new HashMap<Variable,List<Integer>>();
    Variable[] vars = rawAnswer.getVariables();
    for (int i = 0; i < vars.length; i++) {
      Variable v = vars[i];
      List<Integer> nodeColumns;
      if (v.isBnodeVar()) {
        // record which columns this bnode will generate
        nodeColumns = bnodeVars.get(v);
        if (nodeColumns == null) {
          // first time this bnode was encountered
          nodeColumns = new ArrayList<Integer>();
          bnodeVars.put(v, nodeColumns);
        }
      } else {
        // record which columns this bnode will generate
        nodeColumns = stdVarIndexes.get(v);
        if (nodeColumns == null) {
          // first time this variable was encountered
          nodeColumns = new ArrayList<Integer>();
          stdVarIndexes.put(v, nodeColumns);
        }
      }
      nodeColumns.add(i);
    }
    resetBlankNodes();
  }

  /**
   * @see org.mulgara.query.Answer#getObject(int)
   */
  public Object getObject(int column) throws TuplesException {
    int c = column + colOffset;
    assert (rawAnswer.getVariables()[c].isBnodeVar() && rowNodes[c] instanceof BlankNode) ||
           !rawAnswer.getVariables()[c].isBnodeVar();
    return rowNodes[c];
  }

  /**
   * @see org.mulgara.query.Answer#getObject(java.lang.String)
   */
  public Object getObject(String columnName) throws TuplesException {
    // use an unrolled loop
    if (CONSTANT_VAR_SUBJECT.equals(columnName)) return getObject(0);
    if (CONSTANT_VAR_PREDICATE.equals(columnName)) return getObject(1);
    if (CONSTANT_VAR_OBJECT.equals(columnName)) return getObject(2);
    throw new TuplesException("Unknown variable: " + columnName);
  }

  /** @see org.mulgara.query.Cursor#beforeFirst() */
  public void beforeFirst() throws TuplesException {
    rawAnswer.beforeFirst();
    colOffset = (rowsPerSoln - 1) * 3;
    resetBlankNodes();
  }

  /** @see org.mulgara.query.Cursor#close() */
  public void close() throws TuplesException {
    rawAnswer.close();
  }

  /**
   * @see org.mulgara.query.Cursor#getColumnIndex(org.mulgara.query.Variable)
   */
  public int getColumnIndex(Variable column) throws TuplesException {
    // use an unrolled loop
    if (SUBJECT_VAR.equals(column)) return 0;
    if (PREDICATE_VAR.equals(column)) return 1;
    if (OBJECT_VAR.equals(column)) return 2;
    throw new TuplesException("Unknown variable: " + column);
  }

  /**
   * @see org.mulgara.query.Cursor#getNumberOfVariables()
   */
  public int getNumberOfVariables() {
    return 3;
  }

  /**
   * @see org.mulgara.query.Cursor#getRowCardinality()
   */
  public int getRowCardinality() throws TuplesException {
    int rawCardinality = rawAnswer.getRowCardinality() * rowsPerSoln;
    if (rawCardinality == 0) return 0;
    // get a copy to work with
    GraphAnswer answerCopy = (GraphAnswer)clone();
    try {
      answerCopy.beforeFirst();
      // test if one row
      if (!answerCopy.next()) return 0;
      // test if we know it can't be more than 1, or if there is no second row
      if (rawCardinality == 1) return 1;
      if (!answerCopy.next()) return rowsPerSoln;
      // Return the raw cardinality
      return rawCardinality;
    } finally {
      try {
        answerCopy.close();
      } catch (TuplesException e) {
        logger.warn("Exception closing cloned answer", e);
      }
    }
  }

  /**
   * @see org.mulgara.query.Cursor#isEmpty()
   */
  public boolean isEmpty() throws TuplesException {
    return rawAnswer.isEmpty();
  }

  /**
   * @see org.mulgara.query.Cursor#getRowCount()
   */
  public long getRowCount() throws TuplesException {
    // Urk. Doing this the hard way...
    // get a copy to work with
    GraphAnswer answerCopy = (GraphAnswer)clone();
    try {
      answerCopy.beforeFirst();
      long result = 0;
      while (answerCopy.next()) result++;
      return result * rowsPerSoln;
    } finally {
      try {
        answerCopy.close();
      } catch (TuplesException e) {
        logger.warn("Exception closing cloned answer", e);
      }
    }
  }

  /**
   * @see org.mulgara.query.Cursor#getRowUpperBound()
   */
  public long getRowUpperBound() throws TuplesException {
    return rawAnswer.getRowUpperBound() * rowsPerSoln;
  }

  /**
   * @see org.mulgara.query.Cursor#getRowExpectedCount()
   */
  public long getRowExpectedCount() throws TuplesException {
    return rawAnswer.getRowExpectedCount() * rowsPerSoln;
  }

  /**
   * @see org.mulgara.query.Cursor#getVariables()
   */
  public Variable[] getVariables() {
    return CONSTANT_VAR_ARR;
  }

  /**
   * Since the returned variables are static, provide them statically as well.
   */
  public static Variable[] getGraphVariables() {
    return CONSTANT_VAR_ARR;
  }

  /**
   * @see org.mulgara.query.Cursor#isUnconstrained()
   */
  public boolean isUnconstrained() throws TuplesException {
    return false;
  }


  /**
   * @see org.mulgara.query.Cursor#next()
   */
  public boolean next() throws TuplesException {
    boolean nextAvailable;
    do {
      nextAvailable = internalNext();
    } while (nextAvailable && !graphable());
    return nextAvailable;
  }


  /** @see java.lang.Object#clone() */
  public Object clone() {
    GraphAnswer a = (GraphAnswer)super.clone();
    a.rawAnswer = (Answer)rawAnswer.clone();
    return a;
  }


  public boolean equals(Object object) {
    if ((object != null) && (object instanceof Answer)) {
      try {
        return AnswerOperations.equal(this, (Answer) object);
      } catch (TuplesException e) {
        logger.fatal("Couldn't test equality of answers", e);
      }
    }
    return false;
  }

  /**
   * Added to match {@link #equals(Object)}.
   */
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * An internal method for moving on to the next row, without testing validity.
   * @return <code>true</code> if this call has not exhausted the rows.
   * @throws TuplesException Due to an error in the underlying rawAnswer.
   */
  private boolean internalNext() throws TuplesException {
    if ((colOffset += 3) < (rowsPerSoln * 3)) return true;
    colOffset = 0;
    // test if the next solution can be used
    // this requires that there are no unbound variables
    boolean nextResult;
    do {
      nextResult = rawAnswer.next();
    } while (nextResult && hasUnboundVar());
    generateBlanks();
    return nextResult;
  }

  /**
   * Tests if the current row of the raw answer has any unbound variables.
   * This has a side effect of filling the row with all the tested values,
   * meaning that there is no longer a need to call rawAnswer.getObject on any values.
   * @return <code>true</code> if at least one variable is unbound. If there is an
   *         unbound variable then the contents of rowNodes are not defined.
   * @throws TuplesException If there is an error reading a variable.
   */
  private boolean hasUnboundVar() throws TuplesException {
    for (Map.Entry<Variable,List<Integer>> varNodes: stdVarIndexes.entrySet()) {
      List<Integer> objIndexes = varNodes.getValue();
      // get the value for the first occurrence
      Object o = rawAnswer.getObject(objIndexes.get(0));
      // short circuit on invalid rows
      if (o == null) return true;
      // fill in the row
      for (int v: objIndexes) rowNodes[v] = o;
    }
    return false;
  }

  /**
   * Test if the current row is expressible as a graph row.
   * @return <code>true</code> if the subject-predicate-object have valid node types.
   * @throws TuplesException The row could not be accessed.
   */
  private boolean graphable() throws TuplesException {
    if (rowNodes[colOffset] instanceof Literal) return false;
    Object predicate = rowNodes[1 + colOffset];
    return !(predicate instanceof Literal || predicate instanceof BlankNode);
  }

  /**
   * Resets the blank node identifier to the start of the document.
   */
  private  void resetBlankNodes() {
    blankNodeId = 1;
  }

  /**
   * Creates all the generated blank nodes for this row.
   */
  private void generateBlanks() {
    for (Map.Entry<Variable,List<Integer>> varNodes: bnodeVars.entrySet()) {
      // generate the blank node for this variable
      BlankNode b = new BlankNodeImpl(blankNodeId++);
      // put this blank node in every position the variable appears in
      for (int i: varNodes.getValue()) rowNodes[i] = b;
    }
  }
}
