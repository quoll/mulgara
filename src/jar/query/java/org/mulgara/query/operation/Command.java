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

/**
 * A general Abstract Syntax Tree for TQL commands.
 *
 * @created 2007-08-09
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public interface Command {

  /**
   * Indicates that the command modifies the state in a transaction.
   * @return <code>true</code> If the transaction state is to be modified.
   */
  boolean isTxCommitRollback();
  
  /**
   * Indicates if an AST element is an operation for a local client.
   * Local operations do not create a connection to a server, though they
   * can modify the state of existing connections.
   * @return <code>true</code> if the operation is only relevant to a client.
   */
  boolean isLocalOperation();
  
  /**
   * Indicates if an AST represents a command for a user interface.
   * @return <code>true</code> if the operation is only relevant to a user interface
   */
  boolean isUICommand();
  
  /**
   * Gets the associated server for a non-local operation.
   * @return the server URI if one can be determined,
   *  or <code>null</code> if this command is local or the uri
   *  is unknown for some other reason.
   */
  URI getServerURI();

  /**
   * Executes the operation. This is highly specific to each operation.
   * @return Data specific to the operation.
   * @throws Exception specific to the operation.
   */
  Object execute(Connection conn) throws Exception;

  /**
   * Gets a message text relevant to the operation.  Useful for the UI.
   * @return A text message associated with the result of this
   * operation.
   */
  String getResultMessage();

  /**
   * Indicates that this command returns an Answer. Saves the overhead of checking
   * the return type of execute.
   * @return <code>true</code> if the result of execute is an Answer.
   */
  boolean isAnswerable();

  /**
   * Sets the textual representation of this command. This may not be the original command
   * but will be equivalent.
   * @param text A textual representation of the command.
   */
  void setText(String text);


  /**
   * Returns the textual representation of this Command.
   * @return The text of the command.
   */
  public String getText();

  /**
   * Sets message text relevant to the operation.  Useful for the UI.
   * @return The set text.
   */
  String setResultMessage(String resultMessage);
}
