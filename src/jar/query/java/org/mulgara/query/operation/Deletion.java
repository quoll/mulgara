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
import java.util.Set;

import org.jrdf.graph.Triple;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;

/**
 * An AST element for deleting from a graph.
 * @created Aug 15, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Deletion extends Modification {
  
  /**
   * Create a deletion command for deleting a set of statements from a graph.
   * @param graph The graph to delete from.
   * @param statements The data to be deleted.
   */
  public Deletion(URI graph, Set<Triple> statements){
    super(graph, statements);
  }

  /**
   * Create an deletion command for deleting the results of a query from a graph.
   * @param graph The graph to delete from.
   * @param selectQuery The query to get data from for deletion.
   */
  public Deletion(URI graph, Query selectQuery){
    super(graph, selectQuery);
  }

  /* (non-Javadoc)
   * @see org.mulgara.query.operation.SessionCommand#getExecutable()
   */
  @Override
  protected SessionOp<String,QueryException> getExecutable() {
    return new SessionOp<String,QueryException>() {
      public String fn(Session session) throws QueryException {
        if (isSelectBased()) session.delete(graph, getSelectQuery());
        else session.delete(graph, getStatements());
        return setResultMessage("Successfully deleted statements from " + graph);
      }
    };
  }

}
