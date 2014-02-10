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

//Mulgara utilities

// Java 2 enterprise packages
import java.io.*;
import java.net.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

// Third party packages
import org.apache.log4j.*;

// Apache Soap packages
import org.apache.soap.*;
import org.apache.soap.rpc.*;
import org.mulgara.util.*;

/**
 * Retrieves summary information on all the documents on a server processed
 * by the framework.
 *
 * @created 2001-10-15
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:08 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class AlldocsTag extends TagSupport {

  /** Serialization ID */
  private static final long serialVersionUID = 1797208266423531210L;

  /**
   * the name of the id attribute
   */
  protected final static String ATTR_ID = "id";

  /**
   * the name of the server attribute
   */
  protected final static String ATTR_SERVER = "server";

  /**
   * the name of the model attribute
   */
  protected final static String ATTR_MODEL = "model";

  /**
   * the Mulgara document property
   */
  protected final static String MULGARA_DOCUMENT_PROPERTY =
    "http://mulgara.org/mulgara/Document#Document";

  /**
   * the TMex HTML title property
   */
  protected final static String MULGARA_HTMLTITLE_PROPERTY =
    "http://mulgara.org/mulgara/tool/HtmlExtractor#title";

  /**
   * the TMex HTML property
   */
  protected final static String MULGARA_TITLE_PROPERTY =
    "http://mulgara.org/mulgara/Document#title";

  /**
   * the TMex has HTML property
   */
  protected final static String MULGARA_HASHHTML_PROPERTY =
    "http://mulgara.org/mulgara/Document#hasHtml";

  /**
   * the TMex processed property
   */
  protected final static String MULGARA_PROCESSED_PROPERTY =
    "http://mulgara.org/mulgara/Document#processed";

  /**
   * the Mulgara generic String property
   */
  protected final static String MULGARA_STRING_PROPERTY =
    "http://mulgara.org/mulgara/generic#string";

  /**
   * the RDF type property
   */
  protected final static String RDF_TYPE_PROPERTY =
    "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger log =
    Logger.getLogger(AlldocsTag.class.getName());

  //
  // Members
  //

  /**
   * the Mulgara server containing metadata we're interested in, overrides the value
   * set using the {@link InitTag} tag
   */
  private URL server = null;

  /**
   * the model to issue queries against, overrides the value set using the
   * {@link InitTag} tag
   */
  private URI model = null;

  // release()
  //
  // Public API
  //

  /**
   * Sets the model to issue queries against. <p>
   *
   * Note. This method will be called if this tag is invoked with the <code>model</code>
   * attribute set, overriding the model URI set using the <code>init</code>
   * tag. </p>
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
      log.error("Invalid model URI in alldocs tag");

      // wrap it and re-throw!
      throw new JspTagException("alldocs: Invalid URI specified as model " +
        "attribute value");
    }

    // try-catch
  }

  // getModel()

  /**
   * Sets the URL of the SOAP endpoint of the Mulgara server containing metadata
   * we're interested in. <p>
   *
   * Note. This method will be called if this tag is invoked with the <code>server</code>
   * attribute set, overriding the server name set using the <code>init</code>
   * tag. </p>
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

  // setModel()

  /**
   * Returns the model to issue queries against. <p>
   *
   * Note. This method may return null if this tag has not been invoked with the
   * <code>model</code> attribute set. If this is the case, the model URI may be
   * obtained by retrieving the value of the attribute
   * &quot;tmex.server.model&quot;. </p>
   *
   * @return the model to issue queries against, or <code>null</code> if the
   *      server hasn't been set
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

  // setServer()

  /**
   * Returns the URL of the SOAP endpoint of the Mulgara server containing metadata
   * we're interested in. <p>
   *
   * Note. This method may return null if this tag has not been invoked with the
   * <code>server</code> attribute set. If this is the case, the server URL may
   * be obtained by retrieving the value of the attribute
   * &quot;tmex.server.soapendpoint&quot;. </p>
   *
   * @return the URL of the SOAP endpoint of the Mulgara server containing metadata
   *      we're interested in, or <code>null</code> if the server hasn't been
   *      set
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

  //
  // Methods overriding TagSupport
  //

  /**
   * Retrieves summary information on all the documents on a Mulgara server
   * processed by the TMex framework.
   *
   * @return a response code informing the servlet container how to proceed with
   *      JSP tag execution
   * @throws JspTagException if an error occurs while trying to retrieve all the
   *      documents
   */
  public int doStartTag() throws JspTagException {

    try {

      // get the SOAP endpoint
      URL soapEndpoint = this.getSoapEndpoint();

      if (soapEndpoint == null) {

        throw new JspTagException("If invoked without \"server\" " +
          "attribute, alldocs tag must be preceeded by init tag");
      }

      // end if
      // log that we've found the SOAP endpoint
      log.debug("Found Mulgara server SOAP enpoint - " + soapEndpoint);

      // retrieve the model we'll be looking for documents in
      URI tmexModel = this.getTmexModel();

      if (tmexModel == null) {

        throw new JspTagException("If invoked without \"model\" " +
          "attribute, alldocs tag must be preceeded by init tag");
      }

      // end if
      // log what model we're looking for documents in
      log.debug("Retrieving all TMex documents from " + tmexModel);

      // ask the Mulgara server for all the documents processed by TMex
      Response allDocsResponse =
        this.retrieveAllDocuments(soapEndpoint, tmexModel);

      // save the response if an id was set, otherwise output it
      if (this.getId() != null) {

        // set a page context variable containing the
        pageContext.setAttribute(this.getId(), allDocsResponse);
      }
      else {

        pageContext.getOut().println(TagSoapClient.getContent(allDocsResponse));
      }

      // end if
    }
     catch (MalformedURLException mue) {

      // log the error
      log.error("Invalid SOAP endpoint URL in alldocs tag");

      // wrap it and re-throw!
      throw new JspTagException("alldocs: Invalid URL specified as server " +
        "attribute value");
    }
     catch (URISyntaxException use) {

      // log the error
      log.error("Invalid model URI in alldocs tag");

      // wrap it and re-throw!
      throw new JspTagException("alldocs: Invalid URI specified as model " +
        "attribute value");
    }
     catch (SOAPException se) {

      // log the error
      log.error("Unable to package all documents query into a SOAP message", se);

      // wrap it and re-throw!
      throw new JspTagException("alldocs: Unable to send all documents " +
        "query as a SOAP message");
    }
     catch (IOException ioe) {

      // log the error
      log.error("Error writing SOAP response to page context output stream", ioe);

      // wrap it and re-throw!
      throw new JspTagException("alldocs: Unable to print all documents " +
        "summary to JSP output stream");
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

    this.id = null;
    this.model = null;
    this.server = null;
  }

  // retrieveAllDocuments()

  /**
   * Returns the URL of the SOAP endpoint of the Mulgara server containing metadata
   * we're interested in. <p>
   *
   * This tag looks first for an attribute defined on this tag, and then in the
   * <code>pageContext</code> for an attribute set using the <code>init</code>
   * tag. </p>
   *
   * @return the URL of the SOAP endpoint of the Mulgara server containing metadata
   *      we're interested in, or null if the model URI has not been defined as
   *      an attribute to this tag or using the <code>init</code> tag
   * @throws MalformedURLException if the SOAP endpoint is not a valid URL
   */
  private URL getSoapEndpoint() throws MalformedURLException {

    URL soapEndpoint = null;

    // look for the endpoint as an attribute first, then in the page context
    if (this.getServer() != null) {

      // override the default server with the URL specified as an attribute
      soapEndpoint = new URL(this.getServer());
    }
    else {

      // get the endpoint set in the init tag (we don't know the scope...)
      soapEndpoint = (URL) this.pageContext.findAttribute(InitTag.KEY_SERVER);
    }

    // end if
    // return the endpoint
    return soapEndpoint;
  }

  // getSoapEndpoint()

  /**
   * Returns the model containing the documents processed by TMex. <p>
   *
   * This tag looks first for an attribute defined on this tag, and then in the
   * <code>pageContext</code> for an attribute set using the <code>init</code>
   * tag. </p>
   *
   * @return the model containing the documents processed by TMex, or null if
   *      the model URI has not been defined as an attribute to this tag or
   *      using the <code>init</code> tag
   * @throws URISyntaxException if the model URI violates
   *      <a href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
   */
  private URI getTmexModel() throws URISyntaxException {

    URI model = null;

    // look for the model as an attribute first, then in the page context
    if (this.getModel() != null) {

      // override the default server with the URL specified as an attribute
      model = new URI(this.getModel());
    }
    else {

      // get the model set in the init tag (we don't know the scope...)
      model = (URI) this.pageContext.findAttribute(InitTag.KEY_MODEL);
    }

    // end if
    // return the endpoint
    return model;
  }

  // getServer()
  //
  // Internal methods
  //

  /**
   * Issues a query to a Mulgara server asking for documents processed by TMex.
   *
   * @param soapEndpoint the SOAP endpoint
   * @param model the Mulgara model
   * @return RETURNED VALUE TO DO
   * @throws SOAPException if an error occurs while sending information to, or
   *      retrieving information from the SOAP endpoint
   */
  private Response retrieveAllDocuments(URL soapEndpoint, URI model)
    throws SOAPException {

    // construct the query
    String allDocsQuery =
      "select $url $title from <" + model.toString() + ">" + " where ($url <" +
      RDF_TYPE_PROPERTY + "> <" + MULGARA_DOCUMENT_PROPERTY + "> )" +
      " and ( $url <" + MULGARA_TITLE_PROPERTY + "> $title );";

    // log the query we're sending
    log.debug("Sending all documents query to " + soapEndpoint + ":\n" +
      allDocsQuery);

    return TagSoapClient.invoke(soapEndpoint, allDocsQuery);
  }

  // getTmexModel()
}


// AlldocsTag
