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

import org.apache.log4j.Logger;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.XSD;

/**
 * An Answer that represents a simple true/false result.
 *
 * @created Jun 26, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class BooleanAnswer extends AbstractAnswer implements Answer, Serializable {

  /** Required ID for serialization */
  private static final long serialVersionUID = -4548465246790083233L;

  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(BooleanAnswer.class.getName());

  /** A default variable name. This matches the default names used elsewhere. */
  private static final String KONSTANT_VAR_NAME = "k0";

  /** The default variable. */
  private static final Variable KONSTANT_VAR = new Variable(KONSTANT_VAR_NAME);

  /** An array containing the default variable. */
  private static final Variable[] KONSTANT_VAR_ARR = new Variable[] { KONSTANT_VAR };

  /** Used to simulate a cursored result. */
  private boolean beforeFirstState = false;

  /** The actual result to be returned, and wrapped by this Answer */
  private boolean result;

  /** The Literal expression for the result */
  private LiteralImpl literalResult;

  /**
   * Constructs a new BooleanAnswer.
   * @param result The result this answer represents.
   */
  public BooleanAnswer(boolean result) {
    this.result = result;
    literalResult = new LiteralImpl(Boolean.toString(result), XSD.BOOLEAN_URI);
  }

  /**
   * Gets the result this answer represents.
   * @return The result of this answer.
   */
  public boolean getResult() {
    return result;
  }

  /**
   * @see org.mulgara.query.Answer#getObject(int)
   */
  public Object getObject(int column) throws TuplesException {
    if (column == 0) return literalResult;
    throw new TuplesException("Invalid column: " + column);
  }

  /**
   * @see org.mulgara.query.Answer#getObject(java.lang.String)
   */
  public Object getObject(String columnName) throws TuplesException {
    if (KONSTANT_VAR_NAME.equals(columnName)) return literalResult;
    throw new TuplesException("Unknown variable");
  }

  /** @see org.mulgara.query.Cursor#beforeFirst() */
  public void beforeFirst() throws TuplesException {
    beforeFirstState = true; 
  }

  /** @see org.mulgara.query.Cursor#close() */
  public void close() throws TuplesException { /* no op */ }

  /**
   * @see org.mulgara.query.Cursor#getColumnIndex(org.mulgara.query.Variable)
   */
  public int getColumnIndex(Variable column) throws TuplesException {
    if (KONSTANT_VAR.equals(column)) return 0;
    throw new TuplesException("Unknown variable");
  }

  /**
   * @see org.mulgara.query.Cursor#getNumberOfVariables()
   */
  public int getNumberOfVariables() {
    return 1;
  }

  /**
   * @see org.mulgara.query.Cursor#getRowCardinality()
   */
  public int getRowCardinality() throws TuplesException {
    return 1;
  }

  /**
   * @see org.mulgara.query.Cursor#getRowCount()
   */
  public long getRowCount() throws TuplesException {
    return 1;
  }

  /**
   * @see org.mulgara.query.Cursor#isEmpty()
   */
  public boolean isEmpty() throws TuplesException {
    return false;
  }

  /**
   * @see org.mulgara.query.Cursor#getRowUpperBound()
   */
  public long getRowUpperBound() throws TuplesException {
    return 1;
  }

  /**
   * @see org.mulgara.query.Cursor#getRowExpectedCount()
   */
  public long getRowExpectedCount() throws TuplesException {
    return 1;
  }

  /**
   * @see org.mulgara.query.Cursor#getVariables()
   */
  public Variable[] getVariables() {
    return KONSTANT_VAR_ARR;
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
    if (beforeFirstState) {
      beforeFirstState = false;
      return true;
    }
    return false;
  }

  /** @see java.lang.Object#clone() */
  public Object clone() {
    return super.clone();
  }
}
