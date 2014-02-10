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
import java.util.Map;

import org.mulgara.connection.Connection;


/**
 * An AST element for the ALIAS command.
 *
 * @created 2009-10-19
 * @author Paul Gearon
 */
public class ListAlias extends LocalCommand {

  private final Map<String,URI> aliases;

  /**
   * Creates a new list alias command.
   */
  public ListAlias(Map<String,URI> aliases) {
    this.aliases = aliases;
    setResultMessage(buildAliasList(aliases));
  }
  
  /**
   * Indicates that this operation is for a UI.
   * @return <code>true</code> as operation is for UI output only.
   */
  public boolean isUICommand() {
    return true;
  }

  /**
   * Asks for the dictionary of aliases.
   * @param conn ignored.
   * @return A Map object that maps namespace prefixes to the namespace URI.
   */
  public Object execute(Connection conn) {
    return aliases;
  }

  /**
   * Writes the alias map as a set of URI/namespace pairs to a string for printing.
   * @return A String containing all the alias mappings.
   */
  private static String buildAliasList(Map<String,URI> aliases) {
    StringBuilder buffer = new StringBuilder();
    for (Map.Entry<String,URI> alias: aliases.entrySet()) {
      buffer.append(alias.getKey()).append(":  <").append(alias.getValue()).append(">\n");
    }
    return buffer.toString();
  }

}
