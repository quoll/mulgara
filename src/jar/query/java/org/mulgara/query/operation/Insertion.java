/*
 * Copyright 2009 DuraSpace.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.query.operation;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.jrdf.graph.Triple;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;

/**
 * An AST element for inserting into a graph.
 * @created Aug 14, 2007
 * @author Paula Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Insertion extends Modification {
  
  /**
   * A redundant constructor to handle some broken reflection code.
   * @param graph The graph to insert into.
   * @param statements The data to be inserted.
   */
  public Insertion(URI graph, HashSet<Triple> statements) {
    super(graph, (Set<Triple>)statements);
  }

  /**
   * Create an insertion command for inserting a set of statements into a graph.
   * @param graph The graph to insert into.
   * @param statements The data to be inserted.
   */
  public Insertion(URI graph, Set<Triple> statements) {
    super(graph, statements);
  }

  /**
   * Create an insertion command for inserting the results of a query into a graph.
   * @param graph The graph to insert into.
   * @param selectQuery The query to get data from for insertion.
   */
  public Insertion(URI graph, Query selectQuery) {
    super(graph, selectQuery);
  }

  /* (non-Javadoc)
   * @see org.mulgara.query.operation.SessionCommand#getExecutable()
   */
  @Override
  protected SessionOp<String,QueryException> getExecutable() {
    return new SessionOp<String,QueryException>() {
      public String fn(Session session) throws QueryException {
        if (isSelectBased()) session.insert(graph, getSelectQuery());
        else session.insert(graph, getStatements());
        return setResultMessage("Successfully inserted statements into " + graph);
      }
    };
  }

}
