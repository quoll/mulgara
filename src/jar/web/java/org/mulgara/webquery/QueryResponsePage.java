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

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.query.operation.Command;
import org.mulgara.util.QueryParams;
import org.mulgara.util.functional.Pair;
import org.mulgara.webquery.html.Anchor;
import org.mulgara.webquery.html.HtmlElement;
import org.mulgara.webquery.html.Span;
import org.mulgara.webquery.html.Strong;
import org.mulgara.webquery.html.Table;
import org.mulgara.webquery.html.TableData;
import org.mulgara.webquery.html.TableHeader;
import org.mulgara.webquery.html.TableRow;
import org.mulgara.webquery.html.Text;
import org.mulgara.webquery.html.HtmlElement.Attr;
import org.mulgara.webquery.html.HtmlElement.Entity;

import static org.mulgara.webquery.Template.*;

/**
 * Constructs and emits the response page for a set of queries.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class QueryResponsePage {

  /** The name of the system property that can override the page size. */
  public static final String PAGE_SIZE_PROP = "webui.page.size";

  /** The default number of results per page or per cell. */
  public static final int DEFAULT_RESULTS_PER_PAGE = 250;

  /** The number of results per page or per cell. */
  static int resultsPerPage;

    /** The request that asked for this page. */
  HttpServletRequest request;

  /** The structure for sending the page back to the client. */
  HttpServletResponse response;

  /** The absolute file path of the template header, with the root set to the resource directory. */
  final String templateHeadFile;

  /** The absolute file path of the template footer, with the root set to the resource directory. */
  final String templateTailFile;

  /** A map of tags to the values that should replace them. */
  Map<String,String> tagMap;

  /** The object for accepting the output stream. */
  PrintWriter out = null;

  /** The set of unfinished results to render, mapped to the remaining sizes. */
  Map<Answer,Pair<Long,Command>> unfinishedResults;


  static {
    try {
      resultsPerPage = Integer.parseInt(System.getProperty(PAGE_SIZE_PROP));
    } catch (Exception e) {
      resultsPerPage = DEFAULT_RESULTS_PER_PAGE;
    }
  }


  /**
   * Construct this page for responsing to a particular request environment.
   * @param req The request that asked for this page.
   * @param resp The structure for sending the page back to the client.
   * @param tagMap A map of tags to the values that should replace them
   * @param headFile the header
   * @param tailFile the footer
   */
  @SuppressWarnings("unchecked")
  public QueryResponsePage(HttpServletRequest req, HttpServletResponse resp, Map<String,String> tagMap, String headFile, String tailFile) {
    this.request = req;
    this.response = resp;
    this.tagMap = tagMap;
    this.templateHeadFile = headFile;
    this.templateTailFile = tailFile;
    this.unfinishedResults = (Map<Answer,Pair<Long,Command>>)req.getSession().getAttribute(UNFINISHED_RESULTS);
  }

  
  /**
   * Send a result page to the client without any stats. This starts with a form template, and then
   * follows up with a single result.
   * @param cmd The command that was run.
   * @param result The result returned from running the command.
   * @throws IOException There was an error writing to the client, or reading resources.
   */
  public void writeResult(Command cmd, Answer result) throws IOException {
    List<Command> commands = Collections.singletonList(cmd);
    List<Object> results = Collections.singletonList((Object)result);
    writeResults(-1L, commands, results);
  }


  /**
   * Send a result page to the client. This starts with a form template, and then
   * follows up with the results.
   * @param time The time take to execute the commands to get these results.
   * @param cmds The commands that were run.
   * @param results The result list returned from running each command.
   * @throws IOException There was an error writing to the client, or reading resources.
   */
  public void writeResults(long time, List<Command> cmds, List<Object> results) throws IOException {
    response.setContentType("text/html");
    response.setHeader("pragma", "no-cache");
    PrintWriter output = getOutput();
    // write the head of the page
    new ResourceTextFile(templateHeadFile, tagMap).sendTo(output);

    // summarise the results first
    writeResultSummary(time, results.size());

    // write the results
    Iterator<Command> c = cmds.iterator();
    Iterator<Object> r = results.iterator();
    while (c.hasNext()) writeResult(c.next(), r.next());

    // Check that we exhausted the results, like we were supposed to
    if (r.hasNext()) response.sendError(SC_INTERNAL_SERVER_ERROR, "Internal error: results do not match queries.");

    // write the tail of the page
    new ResourceTextFile(templateTailFile).sendTo(output);

    output.close();
  }


  /**
   * Write a summary for the results of all the executed commands.
   * @param time The time taken to execute the commands, in milliseconds.
   * @param nrCommands The number of commands executed.
   */
  private void writeResultSummary(long time, int nrCommands) throws IOException {
    // short circuit if we don't have a summary
    if (time < 0) return;
    HtmlElement row = new TableRow(ROW_INDENT);
    row.add(new Span(CSS_LARGE, "Results:"));
    Text t = new Text("(" + nrCommands);
    t.append(nrCommands == 1 ? " query, " : " queries, ");
    t.append(String.format("%.3f", (time / 1000.0))).append(" seconds)");
    row.add(t);

    emitTopLevelElement(row);
  }


  /**
   * Write a single result into a row of an existing table.
   * @param cmd The command executed to give this result.
   * @param result The result for the command cmd.
   * @throws IOException Occurs for an error writing the response.
   */
  private void writeResult(Command cmd, Object result) throws IOException {
    if (result instanceof Answer) writeAnswerResult(cmd, (Answer)result);
    else writeSimpleResult(cmd);
  }


  /**
   * Write a simple result into a row of an existing table.
   * @param cmd The command executed to give this result.
   * @throws IOException Occurs for an error writing the response.
   */
  private void writeSimpleResult(Command cmd) throws IOException {
    HtmlElement row = new TableRow(ROW_INDENT);
    row.add(new Span(CSS_LARGE, "Query Executed:"));
    row.add(new TableData(cmd.getText()).addAttr(Attr.VALIGN, "top").addAttr(Attr.WIDTH, "100%"));
    
    emitTopLevelElement(row);

    row = new TableRow(ROW_INDENT);
    row.add(new Span(CSS_LARGE, "Result Message:"));
    row.add(new TableData(cmd.getResultMessage()).addAttr(Attr.VALIGN, "top").addAttr(Attr.WIDTH, "100%"));

    emitTopLevelElement(row);
  }


  /**
   * Write a complex result into a row of an existing table.
   * @param cmd The command executed to give this result.
   * @param result The result for the command cmd.
   * @throws IOException Occurs for an error writing the response.
   */
  private void writeAnswerResult(Command cmd, Answer result) throws IOException {
    // create a row with the query that was executed
    TableRow row = new TableRow(ROW_INDENT);
    row.add(new Span(CSS_LARGE, "Query Executed:"));
    row.add(new TableData(cmd.getText()).addAttr(Attr.VALIGN, "top").addAttr(Attr.WIDTH, "100%"));

    emitTopLevelElement(row);

    // create the result table, and put it in a cell
    TableData resultData = new TableData(createResultTable(result, cmd));
    resultData.addAttr(Attr.COLSPAN, 2).addAttr(Attr.CLASS, CSS_RESULT_TABLE_CELL);
    // create a new row to hold the result table
    row = new TableRow(ROW_INDENT, resultData);

    emitTopLevelElement(row);
  }


  /**
   * Convert a result into an HTML table.
   * @param result The result to convert.
   * @return A new table containing all the requested results.
   * @throws IOException If there was an error reading the results
   */
  private Table createResultTable(Answer result, Command cmd) throws IOException {
    // create the table for the results
    Table resultTable = new Table();
    resultTable.addAttr(Attr.CLASS, CSS_RESULT_TABLE);

    // Get the columns to be displayed
    Variable[] vars = result.getVariables();

    // add the headers for the table
    TableRow headerRow = new TableRow();
    for (Variable v: vars) headerRow.add(new TableHeader(new Strong(v.getName())));
    resultTable.add(headerRow);

    // add the result data
    try {
      int rowsLeft = resultsPerPage;
      // if this is the first time we've seen this result, then go to the start
      if (!isResuming(result)) result.beforeFirst();

      while (result.next()) {
        TableRow row = new TableRow();
        for (int c = 0; c < vars.length; c++) row.add(createEmbeddableElement(result.getObject(c), cmd));
        resultTable.add(row);
        // exit early if this is too big
        if (--rowsLeft == 0) {
          addUnfinishedResult(result, resultsPerPage, cmd);
          appendNextPageLink(result, resultTable);
          return resultTable;
        }
      }
      // got to the end of this result, so clean it up
      resultFinished(result);
    } catch (TuplesException e) {
      throw (IOException)
        new IOException("Error accessing the results of the query: " + e.getMessage()).initCause(e);
    }
    return resultTable;
  }


  /**
   * Converts an object returned from a query into an element in a table.
   * If the object is a simple type, then it returns text linked to a query
   * for that element. For Answer object, it returns a new table.
   * @param obj The object to convert into a table element.
   * @param cmd The command that gave rise to the object.
   * @return A new table data element.
   * @throws IOException If the data could not be read.
   */
  private TableData createEmbeddableElement(Object obj, Command cmd) throws IOException {
    HtmlElement result = null;
    if (obj instanceof Answer) result = createResultTable((Answer)obj, cmd);
    if (obj instanceof BlankNode) result = new Text(obj.toString());
    if (obj instanceof URIReference || obj instanceof Literal) result = getElementQuery((Node)obj);

    // only null should make it this far without setting the result
    if (result == null) {
      if (obj != null) throw new IllegalArgumentException("Unknown type in result data");
      result = new TableData(new Text(Entity.NBSP));
    }

    if (!(result instanceof TableData)) result = new TableData(result);
    result.addAttr(Attr.CLASS, "rtd");  // set the style to Result-Table-Data
    return (TableData)result;
  }


  /**
   * Gets the URL used for the sample data.
   * @return A URL for the sample data, expressed by default in the template as:
   *         rmi://@@hostname@@/@@servername@@#sampledata
   */
  private Anchor getElementQuery(Node n) throws IOException {
    try {
      // borrow this info out of the tag map
      URI graphUri = new URI(tagMap.get(GRAPH_TAG));
      QueryParams params = new QueryParams();
      params.add(GRAPH_ARG, graphUri.toString());
      String text;
      if (n instanceof URIReference) {
        params.add(QUERY_RESOURCE_ARG, n.toString());
        text = n.toString();
      } else {
        params.add(QUERY_LITERAL_ARG, parameterizeLiteral((Literal)n));
        text = ((Literal)n).getEscapedForm();
      }
      return new Anchor(new URI(EXECUTE_LINK + "?" + params), text);
    } catch (URISyntaxException e) {
      throw (IOException) new IOException("Bad data returned from server").initCause(e);
    }
  }


  /**
   * Adds on a row without borders that links to the next page in this result.
   * @param result The result being displayed.
   * @param resultTable The existing page of data representing the result.
   * @return The table with a new row appended to it, containing the required link.
   * @throws IOException If there was an error structuring the table data.
   */
  private Table appendNextPageLink(Answer result, Table resultTable) throws IOException {
    if (isResuming(result)) {
      TableRow tr = createNextLinkRow(result, resultTable.getWidth());
      tr.addAttr(Attr.CLASS, "borderLess");
      resultTable.add(tr);
    }
    return resultTable;
  }


  /**
   * Creates a link that will take the user to the next page for this data.
   * @param result The result to create the link for.
   * @param width The width the row has to fill.
   * @return A table row containing the link to the next unfinished result.
   * @throws IOException If there was an error structuring the table data.
   */
  private TableRow createNextLinkRow(Answer result, int width) throws IOException {
    try {
      QueryParams param = new QueryParams(RESULT_ORD_ARG, getNrToBeResumed(result));
      Anchor a = new Anchor(new URI(EXECUTE_LINK + "?" + param), "Next page >");
      a.addAttr(Attr.TITLE, "Forward to next page of results");
      return new TableRow(new TableData(a).addAttr(Attr.COLSPAN, width));
    } catch (URISyntaxException e) {
      throw (IOException)
          new IOException("Unabled to emit a relative URL: " + e.getMessage()).initCause(e);
    }
  }


  /**
   * Writes a top-level element to the output stream.
   * @param elt The element to write.
   * @throws IOException If there was an error getting the stream or using it.
   */
  private void emitTopLevelElement(HtmlElement elt) throws IOException {
    PrintWriter output = getOutput();
    elt.sendTo(output);
    output.append("\n");
  }


  /**
   * Encode a Literal for use as a parameter in a query.
   * @param l The literal to encode.
   * @return An form of the literal that is safe to use in a parameter.
   */
  private String parameterizeLiteral(Literal l) {
    StringBuilder p = new StringBuilder("'");
    p.append(l.getLexicalForm().replaceAll("'", "\\\\'")).append('\'');

    URI datatype = l.getDatatypeURI();
    String language = l.getLanguage();

    if (datatype != null) {
      p.append("^^<").append(datatype).append(">");
    } else if (language != null && !language.equals("")) {
      p.append("@").append(language);
    }

    return p.toString();
  }


  /**
   * Gets the PrintWriter for this page.
   * @return The print writer to use for this page.
   * @throws IOException If there was a problem getting the object.
   */
  private PrintWriter getOutput() throws IOException {
    if (out == null) out = response.getWriter();
    return out;
  }


  /**
   * Determines how many results are to be resumed.
   * @return <code>true</code> if this result has already been partly resumed.
   */
  private int getNrToBeResumed(Answer ans) {
    if (unfinishedResults == null) throw new IllegalStateException("Should not be creating a link to a result when that result was not saved.");
    Iterator<Answer> answers = unfinishedResults.keySet().iterator();
    // should either be the first one added, or the last one
    return (answers.next() == ans) ? 1 : unfinishedResults.size();
  }


  /**
   * Tests a result to see it is being resumed.
   * @param result The Answer to test.
   * @return <code>true</code> if this result has already been partly resumed.
   */
  private boolean isResuming(Answer result) {
    return (unfinishedResults != null) && unfinishedResults.keySet().contains(result);
  }


  /**
   * Marks a result as finished.
   * @param result The Answer that just finished.
   * @throws TuplesException Error while cleaning up the result.
   */
  private void resultFinished(Answer result) throws TuplesException {
    if (unfinishedResults != null) unfinishedResults.remove(result);
    result.close();
  }


  /**
   * Adds an unfinished Answer to the current session for later display. The Answer is not
   * added if all the rows have been displayed.
   * @param result The Answer to add to the session.
   * @param numDisplayed The number of rows that have just been displayed from the result.
   * @param cmd The Command associated with the Answer in <var>result</var>.
   * @throws TuplesException If it is not possible to get the number of remaining rows.
   */
  private void addUnfinishedResult(Answer result, int numDisplayed, Command cmd) throws TuplesException {
    // Determine how many results have yet to be displayed from this Answer
    long remainingRows;
    if (unfinishedResults == null) remainingRows = result.getRowCount();
    else {
      // get the remaining from the session, or use the full value if not in the session
      Pair<Long,Command> lastRemainingRows = unfinishedResults.get(result);
      remainingRows = (lastRemainingRows == null) ? result.getRowCount() : lastRemainingRows.first();
    }
    // just diplayed a page, so decrement by a page
    remainingRows -= numDisplayed;
    if (remainingRows < 0) throw new IllegalStateException("Cannot display more rows than are available.");

    // No more to display, so clean up and leave
    if (remainingRows == 0) {
      result.close();
      unfinishedResults.remove(result);
      return;
    }

    // Remember that there is more for this result
    if (unfinishedResults == null) {
      unfinishedResults = new LinkedHashMap<Answer,Pair<Long,Command>>();
      request.getSession().setAttribute(UNFINISHED_RESULTS, unfinishedResults);
    }
    unfinishedResults.put(result, new Pair<Long,Command>(remainingRows, cmd));
  }

}
