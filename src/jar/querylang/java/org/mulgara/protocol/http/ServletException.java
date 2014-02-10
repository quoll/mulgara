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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * Encodes the condition of a bad HTTP servlet request.
 *
 * @created Sep 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class ServletException extends Exception {

  /** The serialization ID */
  private static final long serialVersionUID = -3284974975526320276L;

  private final int errorCode;

  /** An default constructor to indicate a problem. */
  public ServletException(int code) {
    errorCode = code;
  }

  /**
   * @param message The message to send with a bad request code.
   */
  public ServletException(int code, String message) {
    super(message);
    errorCode = code;
  }

  /**
   * Sends this exception to a client through a response object.
   * @param resp The object to respond through.
   * @throws IOException If there was an error sending to the client.
   */
  public void sendResponseTo(HttpServletResponse resp) throws IOException {
    String msg = getMessage();
    if (msg == null) resp.sendError(errorCode);
    else resp.sendError(errorCode, msg);
  }
}
