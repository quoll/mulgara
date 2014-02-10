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

/**
 * Indicates a UI request to automatically commit after executing a write operation.
 * {@link #isOn()} indicates that a transaction is being closed.
 * 
 * @created Aug 17, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SetAutoCommit extends BooleanSetCommand implements TxOp {

  private static final String MESSAGE = "Auto commit is ";

  /**
   * Create a command to set autocommit on or off.
   * @param option The value to set the time recording to.
   */
  public SetAutoCommit(boolean option) {
    super(option);
  }


  /**
   * Set the autocommit state on a connection.
   * @param conn The connection to set the state of.
   * @return The resulting message text.
   * @throws QueryException if unable to set the autocommit state.
   */
  public Object execute(Connection conn) throws QueryException {
    boolean on = isOn();
    if (conn != null) conn.setAutoCommit(on);
    return setResultMessage(MESSAGE + (on ? ON : OFF));
  }


  /**
   * {@inheritDoc}
   */
  public boolean stayInTx() {
    // only stay in a transaction if autocommit is not on
    return !isOn();
  }


  /**
   * Sets message text relevant to the operation.  Exposes this publicly, but only for internal use.
   * @return The set text.
   */
  public String setResultMessage(String resultMessage) {
    return super.setResultMessage(resultMessage);
  }
}
