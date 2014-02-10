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

/**
 * Contains data for the template responses.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Template {

  /** Session variable for the set of results that have not been finished. */
  public static final String UNFINISHED_RESULTS = "unfinishedRsults";

  /** The name of the template HTML file. */
  public static final String TEMPLATE = "/template.html";
  
  /** The name of the HTML file containing the head of a result. */
  public static final String TEMPLATE_HEAD = "/template_head.html";
  
  /** The name of the HTML file containing the tail of a result. */
  public static final String TEMPLATE_TAIL = "/template_tail.html";
  
  /** The tag to replace in the the template file for the host name. */
  public static final String HOSTNAME_TAG = "hostname";

  /** The tag to replace in the the template file for the server name. */
  public static final String SERVERNAME_TAG = "servername";

  /** The tag to replace in the the template file for the jar file path. */
  public static final String JARURL_TAG = "jarurl";

  /** The tag to replace in the the template file for the execute link. */
  public static final String EXECUTE_TAG = "execute";

  /** The tag to replace in the the template file for debugging info. */
  public static final String DEBUG_TAG = "debug";

  /** The tag to replace in the template file for the user-set graph URI. */
  public static final String GRAPH_TAG = "graph";

  /** The tag to replace in the template file for the base path of the application. */
  public static final String BASE_PATH_TAG = "base";

  /** The amount of indenting to use for the rows in the template. */
  public static final int ROW_INDENT = 8;

  /** The link used for executing a query. */
  public static final String EXECUTE_LINK = "ExecuteQuery";

  /** The name of the field holding the query text to execute. */
  public static final String QUERY_TEXT_ARG = "QueryText";

  /** The GraphURI argument for URL parameters. */
  public static final String GRAPH_ARG = "GraphURI";

  /** The QueryResource argument for URL parameters. */
  public static final String QUERY_RESOURCE_ARG = "QueryResource";

  /** The QueryLiteral argument for URL parameters. */
  public static final String QUERY_LITERAL_ARG = "QueryLiteral";

  /** The ResultOrdinal argument for URL parameters. */
  public static final String RESULT_ORD_ARG = "ResultOrdinal";

  /** The URI of a graph to be uploaded into. */
  public static final String UPLOAD_GRAPH_ARG = "UploadGraph";

  /** The path of a file to be uploaded. */
  public static final String UPLOAD_FILE_ARG = "UploadFile";

  /** A label out of the CSS file. That file could be structurally generated, but for no good reason. */
  public static final String CSS_LARGE = "styleLarge";

  /** A CSS label for cells holding a result table. */
  public static final String CSS_RESULT_TABLE_CELL = "resultTableCell";

  /** A CSS label for  a result table. */
  public static final String CSS_RESULT_TABLE = "resultTable";

}
