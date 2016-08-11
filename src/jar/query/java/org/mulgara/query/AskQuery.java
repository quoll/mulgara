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

package org.mulgara.query;

import java.util.Collections;
import java.util.List;

import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.server.Session;

/**
 * A query type to indicate that the result should be boolean. A true result indicates
 * that the query would return more than 0 rows.
 *
 * @created Jun 26, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class AskQuery extends Query {

  /** Required serialization ID */
  private static final long serialVersionUID = -6024259961466362580L;

  /**
   * Creates an ASK query.
   * @param variableList The variables in the result to check for.
   * @param graphExpression The source of the data to query.
   * @param constraintExpression The WHERE clause to test.
   */
  @SuppressWarnings("unchecked")
  public AskQuery(List<? extends SelectElement> variableList, GraphExpression graphExpression,
        ConstraintExpression constraintExpression) {
    super(variableList, graphExpression, constraintExpression,
        null, // no having
        (List<Order>)Collections.EMPTY_LIST, // no ordering
        null, // no limit
        0, // zero offset
        true, // distinct TODO: change to false when non-distinct is tested
        new UnconstrainedAnswer());
  }

  /**
   * Executes this query on a connection.
   * @param conn The connection to a database session to execute the query against.
   * @return The answer to this query.  Closing is optional.
   */
  public BooleanAnswer execute(Connection conn) throws QueryException, TuplesException {
    boolean answer = conn.execute(new SessionOp<Boolean,QueryException>() {
      public Boolean fn(Session session) throws QueryException {
        return session.query(AskQuery.this);
      }
    });
    return new BooleanAnswer(answer);
  }

}
