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

import java.io.IOException;

import org.mulgara.server.SessionFactoryProvider;

/**
 * A tutorial Web UI for the server. This is almost the same as the standard
 * WebUI, but includes a series of selectable commands.
 *
 * @created Dec 4, 2008
 * @author Paul Gearon
 */
public class TutorialServlet extends QueryServlet {

  /** Generated serialization ID. */
  private static final long serialVersionUID = 1959974464706199835L;

  /** The name of the template HTML file. */
  public static final String TUTORIAL = "/tutorial.html";
  
  /** The name of the HTML file containing the head of a result. */
  public static final String TUTORIAL_HEAD = "/tutorial_head.html";
  
  /** The name of the HTML file containing the tail of a result. Same as the standard template. */
  public static final String TUTORIAL_TAIL = "/template_tail.html";
  
  /**
   * Creates the tutorial servlet.
   * @param hostname
   * @param servername
   * @param server
   * @throws IOException
   */
  public TutorialServlet(String hostname, String servername, SessionFactoryProvider server) {
    super(hostname, servername, server);
  }


  /**
   * Creates the tutorial servlet in a managed environment.
   */
  public TutorialServlet() {
  }


  /**
   * Provide a description for the servlet.
   * @see javax.servlet.GenericServlet#getServletInfo()
   */
  public String getServletInfo() {
    return "Mulgara Query Tutorial";
  }


  /**
   * Get the name of the file to be used for the tutorial template.
   * @return The absolute file path, with a root set at the resource directory.
   */
  protected String getTemplateFile() {
    return TUTORIAL;
  }


  /**
   * Get the name of the file to be used for the tutorial header template.
   * @return The absolute file path, with a root set at the resource directory.
   */
  protected String getTemplateHeaderFile() {
    return TUTORIAL_HEAD;
  }


  /**
   * Get the name of the file to be used for the tutorial footer template.
   * @return The absolute file path, with a root set at the resource directory.
   */
  protected String getTemplateTailFile() {
    return TUTORIAL_TAIL;
  }

}
