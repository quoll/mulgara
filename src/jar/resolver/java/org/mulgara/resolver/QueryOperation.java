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

package org.mulgara.resolver;

// Java 2 standard packages
import java.util.ArrayList;
import java.util.List;

// Third party packages
// import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.*;

/**
 * An {@link Operation} that implements the {@link org.mulgara.server.Session#query(Query)} method.
 *
 * @created 2004-11-25
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class QueryOperation implements Operation {
  /** Logger. */
  // private static final Logger logger = Logger.getLogger(QueryOperation.class.getName());

  private final Query query;
  private final List<Query> queryList;

  /**
   * Answer to the query, or <code>null</code> if the {@link #execute} method
   * hasn't yet been invoked.
   */
  private Answer answer = null;

  /**
   * List of {@link Answer}s to a list of queries, or <code>null</code> if the
   * {@link #execute} method hasn't yet been invoked.
   */
  private List<Answer> answerList = null;

  //
  // Constructor
  //

  /**
   * Constructor for {@link org.mulgara.server.Session#query(Query)} calls.
   *
   * @param query  the query to be evaluated, never <code>null</code>
   * @param databaseSession  never <code>null</code>
   * @throws IllegalArgumentException if <var>query</var> or
   *   <var>databaseSession</var> are <code>null</code>
   */
  QueryOperation(Query query, DatabaseSession databaseSession)
  {
    // Validate "query" parameter
    if (query == null) {
      throw new IllegalArgumentException("Null \"query\" parameter");
    }

    // Validate "databaseSession" parameter
    if (databaseSession == null) {
      throw new IllegalArgumentException("Null \"databaseSession\" parameter");
    }

    // Initialize fields
    this.query           = query;
    this.queryList       = null;
  }

  /**
   * Constructor for {@link org.mulgara.server.Session#query(List)} calls.
   *
   * @param queryList  the {@link Query} instances to be evaluated, never <code>null</code>
   * @param databaseSession  never <code>null</code>
   * @throws IllegalArgumentException if <var>queryList</var> or
   *   <var>databaseSession</var> are <code>null</code>
   */
  QueryOperation(List<Query> queryList, DatabaseSession databaseSession) {
    // Validate "query" parameter
    if (queryList == null) {
      throw new IllegalArgumentException("Null \"query\" parameter");
    }

    // Validate "databaseSession" parameter
    if (databaseSession == null) {
      throw new IllegalArgumentException("Null \"databaseSession\" parameter");
    }

    // Initialize fields
    this.query           = null;
    this.queryList       = queryList;
  }

  //
  // Methods implementing Operation
  //

  public void execute(OperationContext       operationContext,
                      SystemResolver         systemResolver,
                      DatabaseMetadata       metadata) throws Exception
  {
    if (query != null) {
      assert queryList == null;

      answer = operationContext.doQuery(query);
    }
    else {
      assert queryList != null;

      answerList = new ArrayList<Answer>(queryList.size());
      for (Query query: queryList) {
        answerList.add(operationContext.doQuery(query));
      }
    }
  }

  /**
   * @return <code>true</code>
   */
  public boolean isWriteOperation() {
    return false;
  }

  
  //
  // Operation result accessors
  //

  /**
   * @return the answer to the query, never <code>null</code>
   * @throws IllegalStateException  if {@link #execute} hasn't yet been called
   */
  Answer getAnswer() {
    if (answer == null) {
      throw new IllegalStateException("Answer not available before execution.");
    }

    return answer;
  }

  /**
   * @return a list of {@link Answer}s to the list of queries, never
   *   <code>null</code>
   * @throws IllegalStateException  if {@link #execute} hasn't yet been called
   */
  List<Answer> getAnswerList() {
    if (answerList == null) {
      throw new IllegalStateException("Answers not available before execution.");
    }

    return answerList;
  }
}
