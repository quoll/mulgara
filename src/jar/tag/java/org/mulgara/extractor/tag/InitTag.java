/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.extractor.tag;

// Java 2 enterprise packages
import java.net.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

// Third party packages
import org.apache.log4j.*;

/**
 * Initialises the JSP environment.
 *
 * @created 2001-10-15
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:08 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class InitTag extends TagSupport {

  /** Serialization ID */
  private static final long serialVersionUID = 403333945644243185L;

  /**
   * variables to have page scope
   */
  protected final static String PAGE_SCOPE = "page";

  /**
   * variables to have request scope
   */
  protected final static String REQUEST_SCOPE = "request";

  /**
   * variables to have session scope
   */
  protected final static String SESSION_SCOPE = "session";

  /**
   * variables to have application scope
   */
  protected final static String APPLICATION_SCOPE = "application";

  /**
   * the name of the server attribute - used for validating
   */
  protected final static String ATTR_SERVER = "server";

  /**
   * the name of the model attribute
   */
  protected final static String ATTR_MODEL = "model";

  /**
   * the name of the scope attribute
   */
  protected final static String ATTR_SCOPE = "scope";

  /**
   * the key to retrieve the URL of the Mulgara server's SOAP endpoint
   */
  protected final static String KEY_SERVER = "mulgara.server.soapendpoint";

  /**
   * the key to retrieve the URI of the Mulgara model
   */
  protected final static String KEY_MODEL = "mulgara.server.model";

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger log =
      Logger.getLogger(InitTag.class.getName());

  //
  // Members
  //

  /**
       * the URL of the SOAP endpoint of the Mulgara server containing metadata we're
   * interested in
   */
  private URL server = null;

  /**
   * the model to issue queries against
   */
  private URI model = null;

  /**
   * the scope to create the tag variables in
   */
  private String scope = null;

  // release()
  //
  // Public API
  //

  /**
   * Sets the URL of the SOAP endpoint of the Mulgara server containing metadata
   * we're interested in.
   *
   * @param server the URL of the SOAP endpoint of the Mulgara server containing
   *      metadata we're interested in
   * @throws JspTagException if <code>server</code> specified is not a valid URL
   */
  public void setServer(String server) throws JspTagException {

    try {

      this.server = new URL(server);
    }
    catch (MalformedURLException mue) {

      // log the error
      log.error("Invalid SOAP endpoint URL in init tag");

      // wrap it and re-throw!
      throw new JspTagException("init: Invalid URL specified as server " +
          "attribute value");
    }

    // try-catch
  }

  // getServer()

  /**
   * Sets the model to issue queries against.
   *
   * @param model the model to issue queries against
   * @throws JspTagException if <code>model</code> violates
   *      <a href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
   */
  public void setModel(String model) throws JspTagException {

    try {

      this.model = new URI(model);
    }
    catch (URISyntaxException use) {

      // log the error
      log.error("Invalid model URI in init tag");

      // wrap it and re-throw!
      throw new JspTagException("init: Invalid URI specified as model " +
          "attribute value");
    }

    // try-catch
  }

  // getModel()

  /**
   * Sets the scope to create the tag variables in.
   *
   * @param scope the scope to create the tag variables in
   */
  public void setScope(String scope) {

    this.scope = scope;
  }

  // setServer()

  /**
   * Returns the URL of the SOAP endpoint of the Mulgara server containing metadata
   * we're interested in.
   *
   * @return the URL of the SOAP endpoint of the Mulgara server containing metadata
   *      we're interested in, or <code>null</code> if no server was specified
   */
  public String getServer() {

    if (this.server == null) {

      return null;
    }
    else {

      return this.server.toString();
    }

    // end if
  }

  // setModel()

  /**
   * Returns the model to issue queries against.
   *
   * @return the model to issue queries against, or <code>null</code> if no
   *      model was specified
   */
  public String getModel() {

    if (this.model == null) {

      return null;
    }
    else {

      return this.model.toString();
    }

    // end if
  }

  // setScope()

  /**
   * Returns the scope to create the tag variables in.
   *
   * @return the scope to create the tag variables in
   */
  public String getScope() {

    return this.scope;
  }

  //
  // Methods overriding TagSupport
  //

  /**
   * Sets the URL of the Mulgara server's SOAP endpoint in the page context.
   *
   * @return a response code informing the servlet container how to proceed with
   *      JSP tag execution
   * @throws JspTagException if SOAP endpoint URL or the model URI is invalid
   */
  public int doStartTag() throws JspTagException {

    try {

      // find the scope to set the variables in
      int scope = this.getVariableScope();

      // get the server
      String server = this.getServer();

      if (server == null) {

        throw new JspTagException("init: Server URL cannot be null");
      }

      // end if
      // get the model
      String model = this.getModel();

      if (model == null) {

        throw new JspTagException("init: Graph URI cannot be null");
      }

      // end if
      // set the variables
      this.pageContext.setAttribute(InitTag.KEY_SERVER, new URL(server), scope);
      this.pageContext.setAttribute(InitTag.KEY_MODEL, new URI(model), scope);

      // log that we've just set the SOAP endpoint
      log.debug("Mulgara server SOAP enpoint set to " + this.getServer());
    }
    catch (MalformedURLException mue) {

      // log the error
      log.error("Invalid SOAP endpoint URL in init tag");

      // wrap it and re-throw!
      throw new JspTagException("init: Invalid URL specified as server " +
          "attribute value");
    }
    catch (URISyntaxException use) {

      // log the error
      log.error("Invalid model URI in init tag");

      // wrap it and re-throw!
      throw new JspTagException("init: Invalid URI specified as model " +
          "attribute value");
    }

    // try-catch
    // skip the body of the tag (which should be empty anyway...)
    return SKIP_BODY;
  }

  // doStartTag()

  /**
   * Resets the tag to its default state.
   */
  public void release() {

    this.server = null;
    this.model = null;
    this.scope = null;
  }

  // getScope()
  //
  // Internal methods
  //

  /**
   * Returns the scope to create the variables in.
   *
   * @return the scope to create the variables in
   */
  private int getVariableScope() {

    // get the scope specified as a tag attribute
    String requestedScope = this.getScope();

    // default to page scope
    int scope = PageContext.PAGE_SCOPE;

    if ( (requestedScope == null) || requestedScope.equalsIgnoreCase("")) {

      scope = PageContext.PAGE_SCOPE;
    }
    else if (requestedScope.equalsIgnoreCase(PAGE_SCOPE)) {

      scope = PageContext.PAGE_SCOPE;
    }
    else if (requestedScope.equalsIgnoreCase(REQUEST_SCOPE)) {

      scope = PageContext.REQUEST_SCOPE;
    }
    else if (requestedScope.equalsIgnoreCase(SESSION_SCOPE)) {

      scope = PageContext.SESSION_SCOPE;
    }
    else if (requestedScope.equalsIgnoreCase(APPLICATION_SCOPE)) {

      scope = PageContext.APPLICATION_SCOPE;
    }

    // end if
    // return the scope
    return scope;
  }

  // getVariableScope()
}
