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

import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.QueryException;

/**
 * An AST element for controlling transactions.  These commands are considered
 * local, as they do not establish a new connection to a server.  However, if
 * there are any known connections in the current transaction, then the
 * command will update them.
 *
 * @created 2007-08-09
 * @author Paula Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class TransactionCommand extends LocalCommand implements TxOp {

  /**
   * Indicates that the command modifies the state in a transaction.
   * @return <code>true</code> If the transaction state is to be modified.
   */
  public final boolean isTxCommitRollback() {
    return true;
  }
  
  /**
   * Indicates that this operation is not specific to a UI.
   * @return <code>false</code> as operation is not specific to UIs.
   */
  public boolean isUICommand() {
    return false;
  }

  /**
   * Indicates that this command cannot return an Answer
   * @return <code>false</code>.
   */
  public boolean isAnswerable() {
    return false;
  }

  /**
   * Requests a server URI for this operation.  None available, as it
   * operates on the local connection.
   * @return <code>null</code>
   */
  public URI getServerURI() {
    return null;
  }

  /**
   * Sets message text relevant to the operation.  Exposes this publicly, but only for internal use.
   * @return The set text.
   */
  public String setResultMessage(String resultMessage) {
    return super.setResultMessage(resultMessage);
  }
  
  /* (non-Javadoc)
   * @see org.mulgara.query.operation.Command#execute(org.mulgara.connection.Connection)
   */
  final public Object execute(Connection conn) throws QueryException {
    return conn.execute(getExecutable(conn));
  }

  /**
   * Gets the operation to perform on the given connection.
   * @param conn The connection.
   * @return The operation that implements this transactional command.
   */
  protected abstract TxExecutable getExecutable(Connection conn);
  
  /**
   * Shorthand for an operation that returns an Object and throws a QueryException.
   */
  protected interface TxExecutable extends SessionOp<Object,QueryException> {}
}
