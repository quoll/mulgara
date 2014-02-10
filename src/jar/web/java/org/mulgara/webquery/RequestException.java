/*
 * Copyright 2008 Fedora Commons, Inc.
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

package org.mulgara.webquery;

/**
 * An internal exception for communicating issues that need to be sent out as HTTP responses.
 *
 * @created Aug 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class RequestException extends Exception {

  /** The generated serialization ID. */
  private static final long serialVersionUID = 1578918131388079524L;

  /**
   * @param message The description of the problem.
   */
  public RequestException(String message) {
    super(message);
  }

  /**
   * @param message The description of the problem.
   * @param cause A throwable that caused the problem.
   */
  public RequestException(String message, Throwable cause) {
    super(message, cause);
  }

}
