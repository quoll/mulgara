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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mulgara.server.SessionFactoryProvider;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;

/**
 * A publicly facing endpoint for SPARQL. The private one permits write commands
 * via POST and GET.
 *
 * @created Feb 5, 2009
 * @author Paula Gearon
 */
public class PublicSparqlServlet extends SparqlServlet {


  /** serialization id */
  private static final long serialVersionUID = 1L;

  /** Logger */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(PublicSparqlServlet.class.getName());

  /**
   * Creates the servlet for communicating with the given server.
   * @param server The server that provides access to the database.
   * @throws IOException If configuring network writers fails.
   */
  public PublicSparqlServlet(SessionFactoryProvider server) throws IOException {
    super(server);
  }

  /**
   * Respond to a request for the servlet. This may handle update queries.
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.sendError(SC_METHOD_NOT_ALLOWED, "This is a Read Only service.");
  }

  /**
   * Respond to a request for the servlet. This may handle update queries.
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.sendError(SC_METHOD_NOT_ALLOWED, "This is a Read Only service.");
  }

  /**
   * Respond to a request for the servlet. This may handle update queries.
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.sendError(SC_METHOD_NOT_ALLOWED, "This is a Read Only service.");
  }

}
