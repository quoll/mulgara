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

// Java 2 enterprise packages
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * Retrieves an answer to a query from a statement.
 *
 * @created 2001-11-19
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:09 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class AnswerTag extends BodyTagSupport {

  private static final long serialVersionUID = 3726055494484378655L;

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
   * the name of the id attribute
   */
  protected final static String ATTR_ID = "id";

  /**
   * the name of the statement attribute
   */
  protected final static String ATTR_STATEMENT = "statement";

  /**
   * the name of the query attribute
   */
  protected final static String ATTR_QUERY = "query";

  /**
   * the name of the scope attribute
   */
  protected final static String ATTR_SCOPE = "scope";

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger log = Logger.getLogger(AnswerTag.class.getName());

  //
  // Members
  //

  /**
   * the id of the statement to retrieve answers from
   *
   */
  private String statement = null;

  /**
   * the id of the query to retrieve the answer for
   *
   */
  private String query = null;

  /**
   * the scope to create the tag variables in
   *
   */
  private String scope = null;

  /**
   * the answers to the specified statement
   *
   */
  private ArrayList<Document> answers = null;

  /**
   * the mapping of query ids to answer list indices
   *
   */
  private HashMap<String,Integer> answerPositionMap = null;

  // release()
  //
  // Public API
  //

  /**
   * Sets the id of the statement to retrieve answers from.
   *
   * @param statement the id of the statement to retrieve answers from
   */
  public void setStatement(String statement) {

    this.statement = statement;
  }

  // getStatement()

  /**
   * Sets the id of the query to retrieve the answer for.
   *
   * @param query the id of the query to retrieve the answer for
   */
  public void setQuery(String query) {

    this.query = query;
  }

  // getQuery()

  /**
   * Sets the scope to create the tag variables in.
   *
   * @param scope the scope to create the tag variables in
   */
  public void setScope(String scope) {

    this.scope = scope;
  }

  // setStatement()

  /**
   * Returns the id of the statement to retrieve answers from.
   *
   * @return the id of the statement to retrieve answers from
   */
  public String getStatement() {

    return this.statement;
  }

  // setQuery()

  /**
   * Returns the id of the query to retrieve the answer for.
   *
   * @return the id of the query to retrieve the answer for
   */
  public String getQuery() {

    return this.query;
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
  // Methods overriding BodyTagSupport
  //

  /**
       * @return a response code informing the servlet container how to proceed with
   *      JSP tag execution
   * @throws JspException
   * @throws JspTagException EXCEPTION TO DO
   */
  @SuppressWarnings("unchecked")
  public int doStartTag() throws JspTagException {

    // log that we're starting execution of the start tag
    log.debug("Starting answer tag execution");

    // make sure that a statement has been specified
    if ( (this.getStatement() == null) || this.getStatement().equals("")) {

      throw new JspTagException("answer: Must contain a statement attribute.");
    }

    // end if
    // log that we've verified required attributes
    log.debug("Verified that statement attribute was set");

    // make sure that a query has been specified
    if ( (this.getQuery() == null) || this.getQuery().equals("")) {

      throw new JspTagException("answer: Must contain a query attribute.");
    }

    // end if
    // log that we've verified required attributes
    log.debug("Verified that query attribute was set");

    // get the list of answers
    ArrayList<Document> answers =
        (ArrayList<Document>)this.pageContext.getAttribute(this.getStatement());

    if (answers == null) {

      throw new JspTagException("answer: No answers where found for the " +
                                "statement with id " + this.getStatement());
    }

    // end if
    // set the answers
    this.answers = answers;

    // log that we've found the answers
    log.debug("Found " + answers.size() +
              " answers in the specified statement");

    // get mapping of answer positions
    HashMap<String, Integer> answerPositions =
        (HashMap<String, Integer>)this.pageContext.getAttribute(StatementTag.
                                               KEY_STATEMENT_PREFIX +
                                               "." + this.getStatement());

    if (answerPositions == null) {

      throw new JspTagException("answer: No answer list mapping found for " +
                                "statement with id " + this.getStatement());
    }

    // end if
    // add the answer position map
    this.answerPositionMap = answerPositions;

    // log that we've found the position map
    log.debug("Found query answers position map from specified statement " +
              "tag containing " + answerPositions.size() + " query id mappings");

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

      // find out the position of the answer to the query
      Integer position =
          this.answerPositionMap.get(this.getQuery());

      // only continue of we found its position (ie. there is an answer to
      // this query)
      if (position != null) {

        // get the answer to the query
        Document answer = this.answers.get(position.intValue());

        // log that we've found an answer
        log.debug("Found answer to specified query");

        // find out whether to return the answer to the stream or as a Document
        if (this.getId() != null) {

          // log that we're parsing results
          log.debug("Returning query answer as Document");

          // set a page context variable containing the query answer
          this.pageContext.setAttribute(this.getId(), answer,
                                        this.getVariableScope());
        }
        else {

          // log that we're returning the results to the stream
          log.debug("Returning raw answer to stream");

          // get the null transformer (performs a straight copy)
          TransformerFactory tf = TransformerFactory.newInstance();
          Transformer transformer = tf.newTransformer();

          // write the answer to a tmp buffer
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          transformer.transform(new DOMSource(answer), new StreamResult(out));

          // write the buffer to the output stream
          this.getPreviousOut().println(out.toString());

          log.debug("Successfully wrote raw answer to stream");
        }

        // end if
      }
      else {

        // log that we found no answer
        log.debug("No answer found to specified query");
      }

      // end if
    }
    catch (IOException ioe) {

      // log the error
      log.error("Error handling answer", ioe);

      // wrap it and re-throw!
      throw new JspTagException("answer: Unable to print query answer " +
                                "to JSP output stream");
    }
    catch (TransformerException te) {

      // log the error
      log.error("Error handling answer", te);

      // wrap it and re-throw!
      throw new JspTagException("answer: Unable to print query answer " +
                                "to JSP output stream");
    }
    catch (Exception e) {

      log.error("Unhandled exception in answers tag", e);
    }

    // try-catch
    // no need to re-execute the body
    return SKIP_BODY;
  }

  // doAfterBody()

  /**
   * Resets the tag to its default state.
   *
   */
  public void release() {

    this.id = null;
    this.statement = null;
    this.query = null;
    this.scope = null;
    this.answers = null;
    this.answerPositionMap = null;
  }

  // getVariableScope()

  /**
   * Returns the scope to create the variables in (as a PageContext constant).
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

  // getAnswerPositionMap()
}
