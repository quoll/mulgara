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
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.log4j.Logger;

/**
 * Adds an iTQL query to a statement.
 *
 * @created 2001-11-09
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
public class QueryTag extends BodyTagSupport {

  private static final long serialVersionUID = -2095876373529429038L;

  /**
   * the name of the id attribute
   *
   */
  protected final static String ATTR_ID = "id";

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger log =
      Logger.getLogger(QueryTag.class.getName());

  //
  // Methods overriding BodyTagSupport
  //

  /**
   * Adds the body of this tag to the enclosing statement tag.
   *
   * @return RETURNED VALUE TO DO
   * @throws JspTagException if this tag is not contained within a statement tag
   */
  public int doAfterBody() throws JspTagException {

    // log that we're executing the query tag
    log.debug("Starting query tag execution");

    try {

      // get the parent tag
      Tag parent = this.getParent();

      // make sure we're embedded in a statement tag
      if ( (parent == null) || ! (parent instanceof StatementTag)) {

        throw new JspTagException("query: Query tag must be contained within " +
                                  "a statement tag");
      }

      // end if
      // get the query
      String itqlQuery = this.getBodyContent().getString();

      // add it to the parent statement tag
      String queryId = this.getId();

      if ( (queryId == null) || queryId.equals("")) {

        log.debug("Adding query to parent statement");
        ( (StatementTag) parent).addQuery(itqlQuery.trim());
      }
      else {

        log.debug("Adding query with id " + queryId + " to parent statement");
        ( (StatementTag) parent).addQuery(queryId, itqlQuery.trim());
      }

      // end if
    }
    catch (Exception e) {

      log.error("Unhandled exception in query tag", e);
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
  }

  // release()
}
