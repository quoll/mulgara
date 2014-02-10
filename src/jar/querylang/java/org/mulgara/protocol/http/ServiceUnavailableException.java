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

package org.mulgara.protocol.http;

import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

/**
 * Encodes the condition of an unavailable service.
 *
 * @created Sep 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class ServiceUnavailableException extends ServletException {

  /** The serialization ID */
  private static final long serialVersionUID = 3424641674531209118L;

  /** An default constructor to indicate a problem. */
  public ServiceUnavailableException() {
    super(SC_SERVICE_UNAVAILABLE);
  }

  /**
   * @param message The message to send with a server error code.
   */
  public ServiceUnavailableException(String message) {
    super(SC_SERVICE_UNAVAILABLE, message);
  }

}
