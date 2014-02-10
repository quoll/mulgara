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

package org.mulgara.tag;

//Mulgara utilities

// Java 2 enterprise packages
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.mulgara.util.TagSoapClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Sends queries to a Mulgara server.
 *
 * @created 2001-11-07
 *
 * @author Tom Adams
 * @author Tate Jones
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/03/12 02:54:05 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2001-2003 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class StatementTag extends BodyTagSupport {

  private static final long serialVersionUID = -7419226348089456963L;

  /**
   * the name of the id attribute
   */
  protected final static String ATTR_ID = "id";

  /**
   * the name of the server attribute
   */
  protected final static String ATTR_SERVER = "server";

  /**
   * the prefix of the key to retrieve the map of query answers from
   */
  protected final static String KEY_STATEMENT_PREFIX =
      "mulgara.tag.statement.answers";

  //
  // Constants
  //

  /**
   * the category to log to
   *
   */
  private final static Logger log =
      Logger.getLogger(StatementTag.class.getName());

  //
  // Members
  //

  /**
   * the Mulgara server containing metadata we're interested in, overrides the value
   * set using the {@link InitTag} tag
   */
  private URL server = null;

  /**
   * the (ordered) map of queries to send to a Mulgara server
   */
  private LinkedHashMap<String,String> queries = null;

  /**
   * the URL of the SOAP endpoint of the Mulgara server
   */
  private URL soapEndpoint = null;

  /**
   * the list of answers ({@link org.apache.soap.rpc.Response }s) to the queries
   * in this tag, in the order of execution specified by the {@link ExecuteTag}s
   * in the body
   */
  private ArrayList<Response> answers = null;

  /**
   * mapping of query ids to answer list indices
   */
  private HashMap<String,Integer> answerPositionMap = null;

  // findSoapEndpoint()

  /**
   * Formats a {@link org.apache.soap.rpc.Response} into a {@link
   * org.w3c.dom.Document}.
   *
   * @param answer the answer to a query
   * @return the <code>answer</code> as an {@link org.w3c.dom.Document}
   * @throws SOAPException if the SOAP message cannot be externalised
   * @throws ParserConfigurationException if a Document builder cannot be
   *      created
   * @throws SAXException if the contents of the SOAP message cannot be parsed
   * @throws IOException if an I/O error occurs
   */
  private static Document formatSoapResponse(Response answer) throws
      SOAPException,
      ParserConfigurationException,
      SAXException,
      IOException {

    // parse the answer into a document
    DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();

    Parameter result = answer.getReturnValue();

    Document doc =
        builder.parse(new InputSource(
        new StringReader(result.getValue().toString())));

    // return the document
    return doc;
  }

  // execute()

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
      throw new JspTagException("statement: Invalid URL specified as server " +
                                "attribute value");
    }

    // try-catch
  }

  // setServer()

  /**
   * Returns the URL of the SOAP endpoint of the Mulgara server containing metadata
   * we're interested in. <p>
   *
   * Note. This method may return null if this tag has not been invoked with the
   * <code>server</code> attribute set. If this is the case, the server URL may
   * be obtained by retrieving the value of the attribute
   * &quot;mulgara.server.soapendpoint&quot;. </p>
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
  // Methods overriding BodyTagSupport
  //

  /**
   * Initialises the query map and the SOAP endpoint.
   *
   * @return a response code informing the servlet container how to proceed with
   *      JSP tag execution
   * @throws JspException if no SOAP endpoint was specified either as an
   *      attribute to this tag, or in a preceeding init tag, or if the SOAP
   *      endpoint specified was invalid
   * @throws JspTagException EXCEPTION TO DO
   */
  public int doStartTag() throws JspTagException {

    // log that we're starting execution of the start tag
    log.debug("Starting statement tag execution");

    // intialise the members
    this.queries = new LinkedHashMap<String,String>();
    this.answers = new ArrayList<Response>();
    this.answerPositionMap = new HashMap<String,Integer>();

    try {

      // get the SOAP endpoint
      URL soapEndpoint = this.findSoapEndpoint();

      if (soapEndpoint == null) {

        throw new JspTagException("statement: If invoked without \"server\" " +
            "attribute, statement tag must be preceeded by init tag");
      }

      // end if
      // log that we've found the SOAP endpoint
      log.debug("Found Mulgara server SOAP enpoint - " + soapEndpoint);

      // set the SOAP endpoint
      this.soapEndpoint = soapEndpoint;
    }
    catch (MalformedURLException mue) {

      // log the error
      log.error("Invalid SOAP endpoint or document URL in statement tag");

      // wrap it and re-throw!
      throw new JspTagException("statement: Invalid URL specified as server " +
                                "attribute value");
    }

    // try-catch
    // continue to evaluate the body
    return EVAL_BODY_BUFFERED;
  }

  // doStartTag()

  /**
   * Returns the results of the nested queries.
   *
   * @return RETURNED VALUE TO DO
   * @throws JspTagException if an the content cannot be rendered
   */
  public int doAfterBody() throws JspTagException {

    // log what we're doing
    log.debug("Returning query results");

    try {

      // parse the Reponse answers into a list of Answers
      if ( (this.answers != null) && (this.answers.size() > 0)) {

        // find out whether to return the answers in the raw (as SOAP messages)
        // or parsed into a nice list of Documents
        if (this.getId() != null) {

          // log that we're parsing results
          log.debug("Returning results as list of Documents");

          // create a list for the answers
          ArrayList<Document> formattedAnswers = new ArrayList<Document>();

          // parse the Response answers into a list of Answers
          for (Iterator<Response> ansIterator = this.answers.iterator();
               ansIterator.hasNext(); ) {

            // get the answer as a document
            Document document =
                StatementTag.formatSoapResponse( ansIterator.next());

            // add it to the
            formattedAnswers.add(document);
          }

          // end for
          // set a page context variable containing the query answers
          this.pageContext.setAttribute(this.getId(), formattedAnswers);

          // log that we've added the answers
          log.debug("Added formatted answers to " + formattedAnswers.size() +
                    " queries to page context");

          // set a page context variable containing the answer position mappings
          this.pageContext.setAttribute(StatementTag.KEY_STATEMENT_PREFIX +
                                        "." + this.getId(),
                                        this.answerPositionMap);

          // log that we've added the answer position map
          log.debug("Added position mappings to " +
                    this.answerPositionMap.size() + " queries");
        }
        else {

          // log that we're returning the results to the stream
          log.debug("Returning raw results to stream");

          // create a String buffer to hold the answers
          StringBuffer output = new StringBuffer();

          // print each answer to this buffer
          for (Iterator<Response> ansIterator = this.answers.iterator();
               ansIterator.hasNext(); ) {

            pageContext.getOut().println(TagSoapClient.getContent(ansIterator.next()));
          }

          // end for
          // log that we're about to print the query answers
          log.debug("Printing " + this.answers.size() + " answers");

          // write the answers to the output stream
          this.getPreviousOut().println(output.toString());
        }

        // end if
      }
      else {

        log.debug("No answers found -> returning nothing");
      }

      // end if
    }
    catch (SOAPException se) {

      // log the error
      log.error("Unable to externalise SOAP answer", se);

      // wrap it and re-throw!
      throw new JspTagException("statement: Unable to externalise SOAP " +
                                "answer to query");
    }
    catch (ParserConfigurationException pce) {

      // log the error
      log.error("Unable create parser to parse SOAP message", pce);

      // wrap it and re-throw!
      throw new JspTagException("statement: Unable to create XML parser for " +
                                "SOAP answer");
    }
    catch (SAXException saxe) {

      // log the error
      log.error("Unable to parse SOAP answer into a document", saxe);

      // wrap it and re-throw!
      throw new JspTagException("statement: Unable to parse SOAP " +
                                "answer into an XML document");
    }
    catch (IOException ioe) {

      // log the error
      log.error("Error handling SOAP asnwer", ioe);

      // wrap it and re-throw!
      throw new JspTagException("statement: Unable to print query answer " +
                                "to JSP output stream");
    }
    catch (Exception e) {

      log.error("Unhandled exception in statement tag", e);
    }

    // try-catch
    // no need to re-execute the body
    return SKIP_BODY;
  }

  // doAfterBody()

  /**
   * Resets the tag to its default state.
   */
  public void release() {

    this.id = null;
    this.server = null;
    this.queries = null;
    this.soapEndpoint = null;
    this.answers = null;
    this.answerPositionMap = null;
  }

  // release()
  //
  // Public API
  //

  /**
   * Adds a query to the query map using an auto-generated id (key).
   *
   * @param query the query to add to the map
   */
  public void addQuery(String query) {

    // no id was specified, make one up
    String id = "Query-" + Integer.toString(this.queries.size());

    while (this.queries.containsKey(id)) {

      // we should never get duplicate keys as we cannot remove queries, but
      // just in case
      id += ("-" + Integer.toString(this.queries.size()));
    }

    // end if
    // log that we're about to add a new query
    log.debug("Adding new query with id " + id);

    // add the query
    this.addQuery(id, query);
  }

  // addQuery()

  /**
   * Adds a query to the query map.
   *
   * @param queryId the id of the query (its key in the map)
   * @param query the query to add to the map
   */
  public void addQuery(String queryId, String query) {

    log.debug("Adding new query (" + queryId + ") - " + query);
    this.queries.put(queryId, query);
  }

  // addQuery()

  /**
   * Executes all the queries in this statement.
   *
   * @throws JspTagException if the batch of queries cannot be sent as a
   *      Response
   */
  public void execute() throws JspTagException {

    try {

      // log that we're executing a query
      log.debug("Executing all queries in batch mode");

      // create a buffer to hold the queries
      StringBuffer queryBatch = new StringBuffer();

      // build up the batch query
      for (Iterator<String> ids = this.queries.keySet().iterator(); ids.hasNext(); ) {

        // add the query to the batch buffer
        String query = this.queries.get(ids.next());
        queryBatch.append(query + "\n");
      }

      // end for
      // log that we're retrieving the answers to the batch query
      log.debug("Finding answer to batch query");

      // send the query batch
      Response batchAnswer = this.sendQuery(queryBatch.toString());

      // log that we're adding the answers
      log.debug("Adding answers to batch query to answer list");

      // add the answer to the list of answers
      this.answers.add(batchAnswer);

      // log that we've added the answers
      log.debug("Added answers to batch query to answer list");
    }
    catch (SOAPException se) {

      // log the error
      log.error("Unable to send query batch as a SOAP message", se);

      // wrap it and re-throw!
      throw new JspTagException("statement: Unable to send query batch as a " +
                                "SOAP message");
    }

    // try-catch
  }

  // execute()

  /**
   * Executes the query with the given id.
   *
   * @param queryId the id (key) of the query to execute
   * @throws JspTagException if a query with the id specified does not exist
   */
  public void execute(String queryId) throws JspTagException {

    try {

      // make sure the query id is valid
      if (!this.queries.containsKey(queryId)) {

        throw new JspTagException("statement: No query exists with id " +
                                  queryId);
      }

      // end if
      // log that we're executing the query
      log.debug("Executing query id - " + queryId);

      // get the query to execute
      String query = this.queries.get(queryId);

      // send the query
      Response answer = this.sendQuery(query);

      // log that we're executing the query
      log.debug("Adding answer to query " + queryId + " to answer list");

      // add the SOAP answer to the answers list
      this.answers.add(answer);

      // save the position of the answer to this query
      this.answerPositionMap.put(queryId,
                                      new Integer(this.answers.size() - 1));
    }
    catch (SOAPException se) {

      // log the error
      log.error("Unable to send query into a SOAP message", se);

      // wrap it and re-throw!
      throw new JspTagException("statement: Unable to send query as a SOAP " +
                                "message");
    }

    // end if
  }

  // formatSoapResponse()

  /**
   * Sends a query to a Mulgara server.
   *
   * @param query the query to send
   * @return the answer to the query
   * @throws SOAPException if the query cannot be sent, or the response cannot
   *      be received
   */
  private Response sendQuery(String query) throws SOAPException {

    // log that we're about to send the query
    log.debug("Executing query - \n" + query);

    URL url = null;

    try {

      //Create a URL to the SOAP end - point
      url = new URL(this.soapEndpoint.toString());
    }
    catch (MalformedURLException mue) {

      // log the error
      log.error("Invalid SOAP endpoint URL in metadata tag");
      throw new SOAPException(org.apache.soap.Constants.FAULT_CODE_SERVER,
                              "statement: Invalid URL specified as server " +
                              "attribute value", mue);
    }

    // try-catch
    return TagSoapClient.invoke(url, query);
  }

  // sendQuery()

  /**
   * Finds the URL of the SOAP endpoint of the Mulgara server containing metadata
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
  private URL findSoapEndpoint() throws MalformedURLException {

    URL soapEndpoint = null;

    // look for the endpoint as an attribute first, then in the page context
    if (this.getServer() != null) {

      // override the default server with the URL specified as an attribute
      soapEndpoint = new URL(this.getServer());
    }
    else {

      // get the endpoint set in the init tag (we don't know the scope...)
      soapEndpoint = (URL)this.pageContext.findAttribute(InitTag.KEY_SERVER);
    }

    // end if
    // return the endpoint
    return soapEndpoint;
  }

  // setAnswerPositionMap()
}
