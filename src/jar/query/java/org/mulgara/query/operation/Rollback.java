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


import org.mulgara.connection.Connection;
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;


/**
 * An AST element for the ROLLBACK command. Not called directly for a TQL rollback, as
 * that would have to go to all open connections. Consequently, TqlAutoInterpreter handles
 * rollbacks through a different method to the standard call path, while the normal
 * call will pass in a {@link org.mulgara.connection.DummyConnection}.
 *
 * After performing a rollback, all connections are removed and
 * the transaction is closed {@link #stayInTx()}.
 * 
 * @created 2007-08-09
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Rollback extends TransactionCommand {

  /**
   * Commits the transaction on a connection.
   * @param conn Contains the session to commit. 
   * @throws QueryException There was a server error commiting the transaction.
   */
  public TxExecutable getExecutable(final Connection conn) {
    return new TxExecutable() {
      public Object fn(Session session) throws QueryException {
        if (session != null) {
          session.rollback();
          conn.setAutoCommit(true);  // this is called because stayInTx returns false
          return setResultMessage("Successfully rolled back changes");
        } else {
          assert conn instanceof org.mulgara.connection.DummyConnection;
          return setResultMessage("Skipped rollback for internal connection");
        }
      }
    };
  }


  /**
   * {@inheritDoc}
   */
  public boolean stayInTx() {
    return false;
  }

}
