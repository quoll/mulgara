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

package org.mulgara.connection;

/**
 * An exception indicating a connection problem.
 *
 * @created 2007-08-22
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ConnectionException extends Exception {

  /** Regenerate this ID if non-private methods are added or removed. */
  private static final long serialVersionUID = 3768510944925963668L;


  /**
   * Create an exception with a message.
   * @param message The message to use.
   */
  public ConnectionException(String message) {
    super(message);
  }


  /**
   * Create an exception caused by another exception, and with a message.
   * @param message The message to use.
   * @param cause The original throwable causing this exception.
   */
  public ConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
