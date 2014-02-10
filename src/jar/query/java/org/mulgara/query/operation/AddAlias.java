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
 * An AST element for the ALIAS command.
 *
 * @created 2007-11-14
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class AddAlias extends LocalCommand {
  
  private static final String SUCCESS_MSG_PREFIX = "Successfully aliased ";

  private static final String SUCCESS_MSG_AS = " as ";

  /** The new alias to set. */
  private final String aliasPrefix;
  
  /** The string for the URI of the alias. */
  private final String aliasTarget;

  /**
   * Creates a new alias command.
   * @param aliasPrefix The alias to set.
   * @param aliasTarget The URI that the alias applies to.
   */
  public AddAlias(String aliasPrefix, String aliasTarget) {
    this.aliasPrefix = aliasPrefix;
    this.aliasTarget = aliasTarget;
  }
  
  /**
   * Indicates that this operation is for a UI.
   * @return <code>true</code> as operation is for UI output only.
   */
  public boolean isUICommand() {
    return true;
  }

  /** @return the aliasPrefix */
  public String getAliasPrefix() {
    return aliasPrefix;
  }

  /** @return the aliasTarget */
  public String getAliasTarget() {
    return aliasTarget;
  }

  /**
   * Asks for the help text associated with the creation of this object.
   * @param conn ignored.
   * @return The text of the help request.
   */
  public Object execute(Connection conn) {
    return setResultMessage(SUCCESS_MSG_PREFIX + aliasTarget + SUCCESS_MSG_AS + aliasPrefix);
  }

}
