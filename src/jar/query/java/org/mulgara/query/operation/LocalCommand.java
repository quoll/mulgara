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

/**
 * An AST element for non-server commands.
 *
 * @created 2007-08-09
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class LocalCommand extends AbstractCommand {

  /**
   * Indicates that this operation is local.
   * @return Always <code>true</code> to indicate this command is local.
   */
  public final boolean isLocalOperation() {
    return true;
  }

  /**
   * Queries if this command is a request to quit.
   * @return <code>false</code> for most operations, but <code>true</code> if quitting.
   */
  public boolean isQuitCommand() {
    return false;
  }


  /**
   * Gets the associated server for a non-local operation.
   * @return <code>null</code>
   */
  public URI getServerURI() {
    return null;
  }


  /**
   * Executes the operation. This is highly specific to each operation.
   * @return Data specific to the operation.
   * @throws Exception specific to the operation.
   */
  public Object execute() throws Exception {
    return execute(null);
  }


}
