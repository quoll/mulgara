/*
 * Copyright 2010 Revelytix.
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

public abstract class AbstractCommand implements Command {

  /** The text used to create this command. */
  private String textualForm;
  /** The message set by the result of this command. */
  private String resultMessage = "";

  /**
   * Indicates that the command modifies the state in a transaction.
   * @return <code>true</code> If the transaction state is to be modified.
   */
  public boolean isTxCommitRollback() {
    return false;
  }

  /**
   * Indicates that this command cannot return an Answer.
   * @return <code>false</code> by default.
   */
  public boolean isAnswerable() {
    return false;
  }

  /** @see org.mulgara.query.operation.Command#setText(java.lang.String) */
  public void setText(String text) {
    textualForm = text;
  }

  /**
   * Returns the textual representation of this Command. Same as {@link #toString()}.
   * @return The text of the command.
   */
  public String getText() {
    return textualForm;
  }

  /**
   * Returns the textual representation of this Command.
   * @return The text of the command.
   */
  public String toString() {
    return textualForm;
  }

  /**
   * Sets message text relevant to the operation.  Useful for the UI.
   * @return The set text.
   */
  public String setResultMessage(String resultMessage) {
    return this.resultMessage = resultMessage;
  }
  
  /**
   * Gets a message text relevant to the operation.  Useful for the UI.
   * @return A text message associated with the result of this
   * operation.
   */
  public String getResultMessage() {
    return resultMessage;
  }

}