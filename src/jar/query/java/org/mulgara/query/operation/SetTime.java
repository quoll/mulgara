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
 * Indicates a UI request to record timing information for executing an operation.
 * @created Aug 17, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SetTime extends BooleanSetCommand {
  
  private static final String SET_TIME = "Time keeping has been set: ";

  /**
   * Create a command to set timing on or off.
   * @param option The value to set the time recording to.
   */
  public SetTime(boolean option) {
    super(option);
  }

  /**
   * Does nothing at the client, except to indicate that time keeping records are required.
   */
  public Object execute(Connection conn) {
    return setResultMessage(SET_TIME + (isOn() ? ON : OFF));
  }
}
