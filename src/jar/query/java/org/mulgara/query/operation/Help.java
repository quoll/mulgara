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
 * An AST element for the HELP command.
 *
 * @created 2007-08-09
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Help extends LocalCommand {

  private final String helpText;
  
  public Help(String helpText) {
    this.helpText = helpText;
  }
  
  /**
   * Indicates that this operation is for a UI.
   * @return <code>true</code> as operation is for UI output only.
   */
  public boolean isUICommand() {
    return true;
  }

  /**
   * Get the text that was requested from this object.
   * @return The help text requested.
   */
  public String getOutput() {
    // let the user know the help for the selected command
    return helpText;
  }

  /**
   * Asks for the help text associated with the creation of this object.
   * @param conn ignored.
   * @return The text of the help request.
   */
  public Object execute(Connection conn) {
    return setResultMessage(helpText);
  }

}
