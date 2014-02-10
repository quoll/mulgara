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


/**
 * An AST element for the QUIT command.
 *
 * @created 2007-08-09
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Quit extends LocalCommand {
  
  static final String finalMessage = "Exiting.";

  /**
   * Indicates that this operation is for a UI.
   * @return <code>true</code> as operation is for UI output only.
   */
  public boolean isUICommand() {
    return true;
  }

  /**
   * No specific action to be taken here.  The client needs to know that this message
   * requires exiting of the main process.
   */
  public Object execute(Connection conn) {
    return finalMessage;
  }

  /**
   * Indicates that this command is a request to quit.
   * @return <code>true</code> to indicate that a client should quit.
   */
  public boolean isQuitCommand() {
    return true;
  }
}
