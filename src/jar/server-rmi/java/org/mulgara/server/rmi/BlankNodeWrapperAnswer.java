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
 * The Initial Developer of the Original Code is Andrew Newman
 * Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.server.rmi;

// Java 2 standard packages
import java.rmi.*;
import java.util.*;

// Log4j
import org.apache.log4j.*;

// JRDF
import org.jrdf.graph.*;

// Third party packages
import org.mulgara.query.AbstractAnswer;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Takes a regular answer and uses a map to convert blank nodes from server side
 * blank nodes to JRDF in memory blank nodes.
 *
 * @author Andrew Newman
 *
 * @created 2005-01-28
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/02/02 21:15:04 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy; 2005 Andrew Newman
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class BlankNodeWrapperAnswer extends AbstractAnswer implements Answer {

  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(BlankNodeWrapperAnswer.class.getName());

  /**
   * The wrapped instance.
   */
  protected Answer answer;

  /**
   * The blank node map.
   */
  protected Map<BlankNode,BlankNode> blankNodeMap = null;

  /**
   * Creates a new wrapper for converting answers with blank nodes.
   *
   * @param answer the instance to wrap.
   * @param newBlankNodeMap a map which converts server side blank nodes to
   *   in memory blank nodes.
   * @throws IllegalArgumentException if <var>answer</var> is <code>null</code>
   */
  public BlankNodeWrapperAnswer(Answer answer, Map<BlankNode,BlankNode> newBlankNodeMap)
      throws RemoteException {
    this.answer = answer;
    blankNodeMap = newBlankNodeMap;
  }

  public Object clone() {
    BlankNodeWrapperAnswer cloned = (BlankNodeWrapperAnswer) super.clone();
    cloned.answer = (Answer)this.answer.clone();
    return cloned;
  }

  public Object getObject(int column) throws TuplesException {
    Object obj = answer.getObject(column);

    // Convert server side blank nodes to in memory blank nodes.
    if (obj instanceof BlankNode) {
      Object newObj = getKey(obj);
      if (newObj != null) {
        obj = newObj;
      }
    }
    return obj;
  }

  public Object getObject(String columnName) throws TuplesException {
    Object obj = answer.getObject(columnName);

    // Convert server side blank nodes to in memory blank nodes.
    if (obj instanceof BlankNode) {
      Object newObj = getKey(obj);
      if (newObj != null) {
        obj = newObj;
      }
    }
    return obj;
  }

  /**
   * Returns the local value, the key of the hashmap, based on the given value
   * or null if not found.  Does so by getting the entry set of the map and
   * testing each for the value - very inefficient.
   *
   * @param serverBlankNode the server side blank node to convert to an in
   *   memory blank node.
   * @return the JRDF memory blank node or null if not found.
   */
  private BlankNode getKey(Object serverBlankNode) {
    Set<Map.Entry<BlankNode,BlankNode>> entrySet = blankNodeMap.entrySet();
    Iterator<Map.Entry<BlankNode,BlankNode>> iter = entrySet.iterator();
    while (iter.hasNext()) {
      Map.Entry<BlankNode,BlankNode> entry = (Map.Entry<BlankNode,BlankNode>) iter.next();
      if (entry.getValue().equals(serverBlankNode)) {
        return entry.getKey();
      }
    }
    return null;
  }

  public void beforeFirst() throws TuplesException {
    answer.beforeFirst();
  }

  public void close() throws TuplesException {
    answer.close();
  }

  public int getColumnIndex(Variable column) throws TuplesException {
    return answer.getColumnIndex(column);
  }

  public int getNumberOfVariables() {
    return answer.getNumberOfVariables();
  }

  public Variable[] getVariables() {
    return answer.getVariables();
  }

  public boolean isUnconstrained() throws TuplesException {
    return answer.isUnconstrained();
  }

  public long getRowCount() throws TuplesException {
    return answer.getRowCount();
  }

  public long getRowUpperBound() throws TuplesException {
    return answer.getRowUpperBound();
  }

  public long getRowExpectedCount() throws TuplesException {
    return answer.getRowExpectedCount();
  }

  public int getRowCardinality() throws TuplesException {
    return answer.getRowCardinality();
  }

  public boolean isEmpty() throws TuplesException {
    return answer.isEmpty();
  }

  public boolean next() throws TuplesException {
    return answer.next();
  }
}
