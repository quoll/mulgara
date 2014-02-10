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
 * Retrieves metadata for a given document.
 *
 * @created 2001-10-17
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:08 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MetadataTag extends TagSupport {
  
  private static final boolean DEBUG = false;

  /** Serialization ID */
  private static final long serialVersionUID = -2907904987343759108L;

  /**
   * the name of the id attribute
   */
  protected final static String ATTR_ID = "id";

  /**
   * the name of the model attribute
   */
  protected final static String ATTR_MODEL = "model";

  /**
   * the name of the server attribute
   */
  protected final static String ATTR_SERVER = "server";

  /**
   * the name of the document attribute
   */
  protected final static String ATTR_DOCUMENT = "document";

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger log =
    Logger.getLogger(MetadataTag.class.getName());

  /**
   * the key to retrieve the document queries file
   */
  private final static String DOCUMENT_QUERIES_FILE = "mulgara.document.queries";

  /**
   * the marker of a model URI for replacement
   */
  private final static String MODEL_MARKER = "@@model@@";

  /**
   * the marker of a document URI for replacement
   */
  private final static String DOCUMENT_MARKER = "@@document@@";

  /**
   * the system property that overrides the the query file location
   */
  private final static String MULGARAV_QUERY = "MULGARAVQUERY";

  //
  // Document properties
  //

  /**
   * the title property
   */
  @SuppressWarnings("unused")
  private final static String TITLE_PROPERTY =
    "http://mulgara.org/mulgara/Document#title";

  /**
   * the HTML title property
   */
  private final static String HTML_TITLE_PROPERTY =
    "http://mulgara.org/mulgara/tool/HtmlExtractor#title";

  /**
   * the has HTML property
   */
  private final static String HAS_HTML_PROPERTY =
    "http://mulgara.org/mulgara/Document#hasHtml";

  /**
   * the date property
   */
  @SuppressWarnings("unused")
  private final static String DATE_PROPERTY =
    "http://mulgara.org/mulgara/Document#containsDate";

  /**
   * the processed property
   */
  @SuppressWarnings("unused")
  private final static String PROCESSED_PROPERTY =
    "http://mulgara.org/mulgara/Document#processed";

  /**
   * the company property
   */
  @SuppressWarnings("unused")
  private final static String COMPANY_PROPERTY =
    "http://mulgara.org/mulgara/Document#hasCompanyName";

  /**
   * the person property
   */
  @SuppressWarnings("unused")
  private final static String PERSON_PROPERTY =
    "http://mulgara.org/mulgara/Document#hasPersonalName";

  /**
   * the key phrase property
   */
  @SuppressWarnings("unused")
  private final static String KEYPHRASE_PROPERTY =
    "http://mulgara.org/mulgara/Document#hasKeyPhrase";

  /**
   * the highlight property
   */
  @SuppressWarnings("unused")
  private final static String HIGHLIGHT_PROPERTY =
    "http://mulgara.org/mulgara/Document#hasHighlight";

  /**
   * the link property
   */
  @SuppressWarnings("unused")
  private final static String LINK_PROPERTY = "";

  /**
   * the generic feature property
   */
  @SuppressWarnings("unused")
  private final static String GENERIC_FEATURE_PROPERTY =
    "http://mulgara.org/mulgara/Document#hasGenericFeature";

  /**
   * the geographic feature property
   */
  @SuppressWarnings("unused")
  private final static String GEO_FEATURE_PROPERTY =
    "http://mulgara.org/mulgara/Document#hasGeographicFeature";

  /**
   * the image property
   */
  @SuppressWarnings("unused")
  private final static String IMAGE_PROPERTY = "";

  /**
   * the email property
   */
  @SuppressWarnings("unused")
  private final static String EMAIL_PROPERTY =
    "http://mulgara.org/mulgara/Document#hasEmailAddress";

  /**
   * the case property
   */
  @SuppressWarnings("unused")
  private final static String CASE_PROPERTY =
    "http://mulgara.org/mulgara/Document#hasCaseName";

  //
  // Members
  //

  /**
   * the model to issue queries against
   */
  private URI model = null;

  /**
   * the Mulgara server containing metadata we're interested in, overrides the value
   * set using the {@link InitTag} tag
   */
  private URL server = null;

  /**
   * the URL of the document to retrieve metadata for
   */
  private URL document = null;

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
      log.error("Invalid model URI in metadata tag");

      // wrap it and re-throw!
      throw new JspTagException("metadata: Invalid URI specified as model " +
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
      log.error("Invalid SOAP endpoint URL in metadata tag");

      // wrap it and re-throw!
      throw new JspTagException("metadata: Invalid URL specified as server " +
        "attribute value");
    }

    // try-catch
  }

  // getServer()

  /**
   * Sets the URL of the document to retrieve metadata for.
   *
   * @param document the URL of the document to retrieve metadata for
   * @throws JspTagException if the <code>document</code> specified is not a
   *      valid URL
   */
  public void setDocument(String document) throws JspTagException {

    try {

      this.document = new URL(document);
    }
     catch (MalformedURLException mue) {

      // log the error
      log.error("Invalid document URL in metadata tag");

      // wrap it and re-throw!
      throw new JspTagException("metadata: Invalid URL specified as document " +
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

  // setServer()

  /**
   * Returns the URL of the document to retrieve metadata for.
   *
   * @return the URL of the document to retrieve metadata for, or <code>null</code>
   *      if the document hasn't been set
   */
  public String getDocument() {

    if (this.document == null) {

      return null;
    }
    else {

      return this.document.toString();
    }

    // end if
  }

  //
  // Methods overriding TagSupport
  //

  /**
   * Retrieves metadata for a given document.
   *
   * @return a response code informing the servlet container how to proceed with
   *      JSP tag execution
   * @throws JspTagException
   */
  public int doStartTag() throws JspTagException {

    try {

      // get the SOAP endpoint
      URL soapEndpoint = this.getSoapEndpoint();

      if (soapEndpoint == null) {

        throw new JspTagException("metadata:If invoked without \"server\" " +
          "attribute, metadata tag must be preceeded by init tag");
      }

      // end if
      // log that we've found the SOAP endpoint
      log.debug("Found Mulgara server SOAP enpoint - " + soapEndpoint);

      // retrieve the model we'll be looking for documents in
      URI tmexModel = this.getTmexModel();

      if (tmexModel == null) {

        throw new JspTagException("metadata: If invoked without \"model\" " +
          "attribute, metadata tag must be preceeded by init tag");
      }

      // end if
      // log what model we're looking for documents in
      log.debug("Retrieving document metadata from " + tmexModel);

      // get the document to retrieve metadata for
      String documentURL = this.getDocument();

      if (documentURL == null) {

        throw new JspTagException("metadata: Document URL cannot be null");
      }

      // end if
      // log what document we're retrieving metadata for
      log.debug("Retrieving metadata for " + document);

      // ask the Mulgara server for the metadata of the document
      Response metadataResponse =
        this.retrieveDocumentMetadata(soapEndpoint, tmexModel,
          new URL(documentURL));

      // save the response if an id was set, otherwise output it
      if (this.getId() != null) {

        // set a page context variable containing the document metadata
        pageContext.setAttribute(this.getId(), metadataResponse);
      }
      else {

        pageContext.getOut().println(TagSoapClient.getContent(metadataResponse));
      }

      // end if
    }
     catch (MalformedURLException mue) {

      // log the error
      log.error("Invalid SOAP endpoint or document URL in metadata tag");

      // wrap it and re-throw!
      throw new JspTagException("metadata: Invalid URL specified as server " +
        "or document attribute value");
    }
     catch (URISyntaxException use) {

      // log the error
      log.error("Invalid model URI in metadata tag");

      // wrap it and re-throw!
      throw new JspTagException("metadata: Invalid URI specified as model " +
        "attribute value");
    }
     catch (SOAPException se) {

      // log the error
      log.error("Unable to package document metadata query into a SOAP message",
        se);

      // wrap it and re-throw!
      throw new JspTagException("metadata: Unable to send documents metadata " +
        "query as a SOAP message");
    }
     catch (IOException ioe) {

      // log the error
      log.error("Error writing SOAP response to page context output stream", ioe);

      // wrap it and re-throw!
      throw new JspTagException("metadata: Unable to print document metadata " +
        "to JSP output stream");
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
    this.document = null;
  }

  // retrieveDocumentMetadata()

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
   * @throws URISyntaxException if the model URI violates <a
   *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
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

  // getTmexModel()

  /**
   * Returns a new-line terminated query to retreive the value of the HTML title
   * property.
   *
   * @param model the Mulgara model
   * @param document the URL of the document to find the title of
   * @return a query to retrieve the
   */
  @SuppressWarnings("unused")
  private String getTitleQuery(URI model, URL document) {

    return "select $docNode $predicate $classNode $propertyLabel $value " +
    "from <" + model.toString() + "> " + "where ( <" + document.toString() +
    "> <" + HAS_HTML_PROPERTY + "> $docNode) " + "and ( $docNode <" +
    HTML_TITLE_PROPERTY + "> $value ) " +
    "and ( $predicate <http://www.w3.org/2000/01/rdf-schema#label> $propertyLabel ) " +
    "and ( $docNode $predicate $value ) ;";
  }

  // getTitleQuery()

  /**
   * Retrieves the document properties queries from the query file.
   *
   * @param model the Mulgara model
   * @param document the URL of the document to find the title of
   * @return the queries to retrieve document properties
   */
  private String getQueries(URI model, URL document) {

    // intialise the query buffer
    StringBuffer queries = new StringBuffer();

    try {

      // get the location of the queries file
      String virtualLocation =
        this.pageContext.getServletContext().getInitParameter(DOCUMENT_QUERIES_FILE);
      virtualLocation = System.getProperty(MULGARAV_QUERY, virtualLocation);

      if (virtualLocation == null) {

        throw new IOException("Unable to locate queries file path for key " +
          DOCUMENT_QUERIES_FILE);
      }

      // end if
      // log that we found the queries file
      if (log.isDebugEnabled()) {

        log.debug("Found queries file location " + virtualLocation);
      }

      // end if
      // get the real location
      String queriesFileLocation =
        this.pageContext.getServletContext().getRealPath(virtualLocation);

      if (queriesFileLocation == null) {

        throw new IOException("Unable to retrieve real filename for virtual " +
          "path " + virtualLocation);
      }

      // end if
      // log that we found the queries file
      if (log.isDebugEnabled()) {

        log.debug("Found queries file at " + queriesFileLocation);
      }

      // end if
      // create a buffered reader to read the contents of the file
      BufferedReader in =
        new BufferedReader(new FileReader(queriesFileLocation));

      try {
        
        // read in the contents of the file into the queries buffer
        String line = in.readLine();

        while (line != null) {

          // log that we've read the line
          if (log.isDebugEnabled()) {

            log.debug("Read line: " + line);
          }

          // trim the line
          line = line.trim();

          // ignore comments and blank lines
          if (!line.startsWith("#") && !line.equals("")) {

            // replace any markers in the text with the real values
            line = line.replaceAll(MODEL_MARKER, model.toString());
            line = line.replaceAll(DOCUMENT_MARKER, document.toString());

            // log that we've replaced the markers in the line
            log.debug("Replaced markers in line: " + line);

            // save the line
            queries.append(line);
          }

          // end if
          // read the next one...
          line = in.readLine();
        }
      } finally {
        in.close();
      }

      // end while
    }
     catch (IOException ioe) {

      // log the error
      log.error("Error reading queries file", ioe);
    }

    // try-catch
    // return the queries
    return queries.toString();
  }

  // getDocument()
  //
  // Internal methods
  //

  /**
   * Issues a query to a Mulgara server asking for metadata on a given document.
   *
   * @param soapEndpoint the SOAP endpoint
   * @param model the Mulgara model
   * @param document the URL of the model to find metadata for
   * @return RETURNED VALUE TO DO
   * @throws SOAPException if an error occurs while sending information to, or
   *      retrieving information from the SOAP endpoint
   * @throws URISyntaxException if a document property URI is not a valid URI
   */
  private Response retrieveDocumentMetadata(URL soapEndpoint, URI model,
    URL document) throws SOAPException, URISyntaxException {

    // construct the query
    StringBuffer metadataQuery = new StringBuffer();

    if (DEBUG) {

      metadataQuery.append(
        "select $docNode $predicate $predicateLabel $classLabel $value $score $generator " +
        "from <" + model.toString() + "> " + "where ( <" + document.toString() +
        "> $predicate $docNode) " +
        "and  (( $predicate <http://www.w3.org/2000/01/rdf-schema#label>          $predicateLabel ) " +
        "and   ( $z         <http://www.w3.org/2000/01/rdf-schema#label>          $classLabel     ) " +
        "and   ( $docNode   $z                                                    $value          )) " +
        "or   (( $id        <http://www.w3.org/1999/02/22-rdf-syntax-ns#object>   $docNode ) " +
        "and   ( $id        <http://mulgara.org/mulgara/Document#score>     $score ) " +
        "and   ( $id        <http://mulgara.org/mulgara/Document#generator> $generator )) ;");
    }
    else {

      // append the queries
      metadataQuery.append(this.getQueries(model, document));

      /*
      metadataQuery.append(this.getPropertyQuery(
          model, document, new URI(DATE_PROPERTY)));
      metadataQuery.append(this.getPropertyQuery(
          model, document, new URI(PROCESSED_PROPERTY)));
      metadataQuery.append(this.getPropertyQuery(
          model, document, new URI(COMPANY_PROPERTY)));
      metadataQuery.append(this.getPropertyQuery(
          model, document, new URI(PERSON_PROPERTY)));
      metadataQuery.append(this.getPropertyQuery(
          model, document, new URI(KEYPHRASE_PROPERTY)));
      metadataQuery.append(this.getPropertyQuery(
          model, document, new URI(HIGHLIGHT_PROPERTY)));
      //metadataQuery.append(this.getPropertyQuery(
      //    model, document, new URI(LINK_PROPERTY)));
      metadataQuery.append(this.getPropertyQuery(
          model, document, new URI(GENERIC_FEATURE_PROPERTY)));
      metadataQuery.append(this.getPropertyQuery(
          model, document, new URI(GEO_FEATURE_PROPERTY)));
      //metadataQuery.append(this.getPropertyQuery(
      //    model, document, new URI(IMAGE_PROPERTY)));
      metadataQuery.append(this.getPropertyQuery(
          model, document, new URI(EMAIL_PROPERTY)));
      metadataQuery.append(this.getPropertyQuery(
          model, document, new URI(CASE_PROPERTY)));
      */
    }

    // end if
    // log the query we're sending
    if (log.isDebugEnabled()) {

      log.debug("Sending query: " + metadataQuery.toString());
      log.debug("Sending queries to " + soapEndpoint);
    }

    return TagSoapClient.invoke(soapEndpoint, metadataQuery.toString());
  }

  // getQueries()
}


// MetadataTag
