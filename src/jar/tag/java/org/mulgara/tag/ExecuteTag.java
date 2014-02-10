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
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

/**
 * Executes a statement.
 *
 * @created 2001-11-09
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:09 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ExecuteTag extends TagSupport {

  private static final long serialVersionUID = 3567013795216088561L;

  /**
   * the name of the query attribute
   */
  protected final static String ATTR_QUERY = "query";

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger log =
      Logger.getLogger(ExecuteTag.class.getName());

  //
  // Members
  //

  /**
   * the id of the query to execute
   */
  private String query = null;

  // release()
  //
  // Public API
  //

  /**
   * Sets the id of the query to execute.
   *
   * @param query the id of the query to execute
   */
  public void setQuery(String query) {

    this.query = query;
  }

  // setQuery()

  /**
   * Returns the id of the query to execute.
   *
   * @return the id of the query to execute
   */
  public String getQuery() {

    return this.query;
  }

  //
  // Methods overriding TagSupport
  //

  /**
   * Sets the URL of the Mulgara server's SOAP endpoint in the page context.
   *
   * @return a response code informing the servlet container how to proceed with
   *      JSP tag execution
   * @throws JspTagException if this tag is not contained within a statement
   *      tag, or the query(ies) in the enclosing statement tag cannot be
   *      executed
   */
  public int doStartTag() throws JspTagException {

    // log that we're executing the execute tag
    if (log.isDebugEnabled()) {

      log.debug("Starting execute tag execution");
    }

    try {

      // get the parent tag
      Tag parent = this.getParent();

      // make sure we're embedded in a statement tag
      if ( (parent == null) || ! (parent instanceof StatementTag)) {

        throw new JspTagException("execute: Execute tag must be contained " +
            "within a statement tag");
      }

      // end if
      // execute the query(ies)
      String queryId = this.getQuery();

      if ( (queryId == null) || queryId.equals("")) {

        // no query specified, execute all queries
        if (log.isDebugEnabled()) {

          log.debug("Executing all queries");
        }
        ( (StatementTag) parent).execute();
      }
      else {

        // execute a single query with the given id
        if (log.isDebugEnabled()) {

          log.debug("Executing query id - " + queryId);
        }
        ( (StatementTag) parent).execute(queryId);
      }

      // end if
      // log that we've finished executing the tag
      if (log.isDebugEnabled()) {

        log.debug("Finished execute tag execution");
      }
    }
    catch (Exception e) {

      log.error("Unhandled exception in query tag", e);
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

    this.query = null;
  }

  // getQuery()
}
