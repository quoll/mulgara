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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.relational;

import org.apache.log4j.*;

// Standard Java packages.
import java.sql.Connection;
import java.util.List;
import java.util.Iterator;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

// Local packages
import org.mulgara.query.AbstractAnswer;
import org.mulgara.query.Answer;
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

public class RelationalAnswer extends AbstractAnswer implements Answer {
  private static final Logger logger = Logger.getLogger(RelationalAnswer.class.getName());

  private Connection conn;
  private Statement statement;
  private ResultSet result;
  private List<Variable> variables;
  private RelationalQuery query;
  private List<String> queryList;
  private Iterator<String> queries;

  public RelationalAnswer(RelationalQuery query, Connection conn) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("Creating RelationalAnswer with query = " + query);
    }
    this.query = query;
    this.variables = query.getVariables();
    this.conn = conn;
  }


  public Object clone() {
    RelationalAnswer cloned = (RelationalAnswer)super.clone();
    cloned.result = null;

    return cloned;
  }

  public Object getObject(int column) throws TuplesException {
    return getObject(variables.get(column));
  }

  public Object getObject(String columnName) throws TuplesException {
    return getObject(new Variable(columnName));
  }

  private Object getObject(Variable column) throws TuplesException {
    try {
      VariableDesc desc = query.getVariableDesc(column);

      Object o = desc.getNode(result);

      if (logger.isDebugEnabled()) {
        logger.debug("Getting object: " + column + " using desc " + desc + " obtained " + o);
      }
      return o;
    } catch (SQLException es) {
      throw new TuplesException("Failed to obtain object from ResultSet", es);
    }
  }

  public void beforeFirst() throws TuplesException {
    try {
      statement = conn.createStatement();
      queryList = query.getQuery();
      if (logger.isDebugEnabled()) {
        logger.debug("Executing query: " + queryList);
      }

      queries = queryList.iterator();
      if (queries.hasNext()) {
        result = statement.executeQuery(queries.next());
      }
    } catch (SQLException es) {
      throw new TuplesException("Failed to resolve query '" + query.getQuery() + "'", es);
    }
  }

  public void close()  throws TuplesException {
    try {
      if (result != null) {
        result.close();
      }
    } catch (SQLException es) {
      throw new TuplesException("Error closing resultset.", es);
    }
  }

  public int getColumnIndex(Variable column) throws TuplesException {
    int index = variables.indexOf(column);
    if (index == -1) {
      throw new TuplesException("Variable not found: " + column);
    }

    return index;
  }

  public int getNumberOfVariables() {
    return variables.size();
  }

  public Variable[] getVariables() {
    return variables.toArray(new Variable[] {});
  }

  public boolean isUnconstrained() {
    return false;
  }

  public long getRowCount() {
    // Technically incorrect, but this should never be used except by debugging stubs.
    return Long.MAX_VALUE;
  }

  public long getRowUpperBound() {
    return getRowCount();
  }

  public long getRowExpectedCount() {
    return getRowCount();
  }

  public int getRowCardinality() {
    return Cursor.MANY;
  }
  
  public boolean isEmpty() {
    return false;
  }

  public boolean next() throws TuplesException {
    if (queryList == null) {
      throw new TuplesException("next() called before beforeFirst()");
    }

    try {
      boolean n = result.next();
      if (n) {
        return true;
      } else {
        if (queries.hasNext()) {
          result = statement.executeQuery((String)queries.next());

          return this.next();
        } else {
          queryList = null;

          return false;
        }
      }
    } catch (SQLException es) {
      throw new TuplesException("Failed to advance resultset", es);
    }
  }
}
