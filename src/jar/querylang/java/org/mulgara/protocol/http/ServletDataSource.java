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
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;

/**
 * Wraps a servlet request to provide as a parameter to a MimeMultipart handler.
 *
 * @created Sep 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class ServletDataSource implements DataSource {

  /** The wrapped request to present as data. */
  private final HttpServletRequest req;

  /** The name of this data source/ */
  private final String name;

  /**
   * Create a data source that presents a servlet request.
   * @param req The servlet request to wrap.
   */
  public ServletDataSource(HttpServletRequest req, String name) {
    this.req = req;
    this.name = name;
  }

  /**
   * @see javax.activation.DataSource#getContentType()
   */
  public String getContentType() {
    return req.getContentType();
  }

  /**
   * @see javax.activation.DataSource#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return req.getInputStream();
  }

  /**
   * @see javax.activation.DataSource#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * @see javax.activation.DataSource#getOutputStream()
   * @throws UnsupportedOperationException This is a read-only resource.
   */
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException("This is a read-only data source.");
  }

}
