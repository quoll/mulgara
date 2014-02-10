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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mulgara.connection.Connection;
import org.mulgara.itql.TqlInterpreter;
import org.mulgara.parser.Interpreter;
import org.mulgara.parser.MulgaraLexerException;
import org.mulgara.parser.MulgaraParserException;
import org.mulgara.protocol.http.MimeMultiNamedPart;
import org.mulgara.protocol.http.MulgaraServlet;
import org.mulgara.protocol.http.ServletDataSource;
import org.mulgara.query.Answer;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.operation.Command;
import org.mulgara.query.operation.CreateGraph;
import org.mulgara.query.operation.Load;
import org.mulgara.server.SessionFactoryProvider;
import org.mulgara.sparql.SparqlInterpreter;
import org.mulgara.util.ObjectUtil;
import org.mulgara.util.SparqlUtil;
import org.mulgara.util.StackTrace;
import org.mulgara.util.functional.C;
import org.mulgara.util.functional.Fn;
import org.mulgara.util.functional.Fn1E;
import org.mulgara.util.functional.Pair;

import static org.mulgara.webquery.Template.*;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

/**
 * A web UI for the server.
 *
 * @created Jul 28, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class QueryServlet extends MulgaraServlet {

  /** Serialization by default */
  private static final long serialVersionUID = -8407263937557243990L;

  /** This path is needed to help with variations in different servlet environments. */
  public static final String SERVLET_PATH = "/webui";

  /** This tutorial path is needed to help with variations in different servlet environments. */
  public static final String TUTORIAL_PATH = "/tutorial";

  /** Session value for the TQL interpreter. */
  private static final String TQL_INTERPRETER = "session.tql.interpreter";

  /** Session value for the SPARQL interpreter. */
  private static final String SPARQL_INTERPRETER = "session.sparql.interpreter";

  /** Posted RDF data content type. */
  protected static final String POSTED_DATA_TYPE = "multipart/form-data;";

  /** A made-up scheme for data uploaded through http-put, since http means "downloaded". */
  protected static final String HTTP_PUT_NS = "http-put://upload/";

  /** A string used for debugging purposes. */
  protected static final String DEBUG_HOOK = "# backdoor";

  /** A string used for debugging purposes. */
  protected static final String DEBUG_PAGE = "/example.html";

  /** A flag to indicate that debugging needs to be run. */
  protected static final boolean DEBUG = true;

  /** The name of the host for the application. */
  private String hostname;

  /** The name of the server for the application. */
  private String servername;

  /** The default graph URI to use. */
  private String defaultGraphUri;

  /** The path down to the template resource. */
  private String templatePath;

  /** The path prefix for resources. */
  private String resourcePath;

  /** Debugging text. */
  private String debugText = "";

  /** The path of the base servlet. */
  private String basePath = SERVLET_PATH;

  /** Indicates if this servlet has been initialized. */
  private boolean initialized = false;

  /**
   * Creates the servlet for the named host.
   * @param hostname The host name to use, or <code>null</code> if this is not known.
   * @param servername The name of the current server.
   * @param server the server
   */
  public QueryServlet(String hostname, String servername, SessionFactoryProvider server) {
    super(server);
    init(hostname, servername);
  }


  /**
   * Creates the servlet for the current host.
   */
  public QueryServlet() {
    initialized = false;
  }


  /**
   * Called by a servlet environment, particularly when this is in a Web ARchive (WAR) file.
   * @see org.mulgara.protocol.http.MulgaraServlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig config) {
    super.init(config);
    ServletContext context = config.getServletContext();
    init(context.getInitParameter(HOST_NAME_PARAM), context.getInitParameter(SERVER_NAME_PARAM));
  }


  /**
   * Initialize this class with parameters passed from either a Servlet environment,
   * or a constructing class.
   * @param hostname The name of this host. If null then the localhost is presumed.
   * @param servername The name of this service. If null then the default of server1 is used.
   */
  private void init(String hostname, String servername) {
    if (!initialized) {
      URL path = ObjectUtil.getClassLoader(this).getResource(ResourceFile.RESOURCES + getTemplateFile());
      if (path == null) throw new MissingResourceException("Missing template file", getClass().getName(), ResourceFile.RESOURCES + getTemplateFile());
      templatePath = path.toString();
      resourcePath = templatePath.split("!")[0];
      this.hostname = (hostname != null) ? hostname : DEFAULT_HOSTNAME;
      this.servername = (servername != null) ? servername : DEFAULT_SERVERNAME;
      defaultGraphUri = "rmi://" + hostname + "/" + servername + "#sampledata";
      initialized = true;
    }
  }


  /**
   * Respond to a request for the servlet.
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String path = req.getRequestURI();
    basePath = calcBasePath(path);
    debugText = path;
    // case analysis for request type
    String ext = getExtension(path);
    if (ext.equals(".jpg") || ext.equals(".png") || ext.equals(".jpeg")) {
      resp.setContentType("image/jpeg");
      new ResourceBinaryFile(relPath(path)).sendTo((OutputStream)resp.getOutputStream());
    } else if (ext.equals(".css")) {
      resp.setContentType("text/css");
      new ResourceBinaryFile(relPath(path)).sendTo(resp.getOutputStream());
    } else {

      // file request
      resp.setContentType("text/html");
      resp.setHeader("pragma", "no-cache");

      // check for some parameters
      String resultOrdinal = req.getParameter(RESULT_ORD_ARG);
      String queryGetGraph = req.getParameter(GRAPH_ARG);

      // return the appropriate page for the given parameters
      if (resultOrdinal != null) {
        doNextPage(req, resp, resultOrdinal);
      } else if (queryGetGraph != null) {
        doQuery(req, resp, queryGetGraph);
      } else {
        clearOldResults(req);
        outputStandardTemplate(resp);
      }
    }
  }


  /**
   * Respond to a request for the servlet.
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    basePath = calcBasePath(req.getRequestURI());
    if (!req.getRequestURI().contains("/" + EXECUTE_LINK)) {
      resp.sendError(SC_BAD_REQUEST, "Sent a command to the wrong page.");
      return;
    }

    String type = req.getContentType();
    if (type != null && type.startsWith(POSTED_DATA_TYPE)) handleDataUpload(req, resp);
    else doQuery(req, resp, req.getParameter(GRAPH_ARG));
  }


  /**
   * Provide a description for the servlet.
   * @see javax.servlet.GenericServlet#getServletInfo()
   */
  public String getServletInfo() {
    return "Mulgara Query UI";
  }


  /**
   * Generates the standard page from the template HTML file.
   * @param resp The object used to respond to the client.
   * @throws IOException Caused by an error writing to the client.
   */
  private void outputStandardTemplate(HttpServletResponse resp) throws IOException {
    PrintWriter out = resp.getWriter();
    new ResourceTextFile(getTemplateFile(), getTemplateTags()).sendTo(out);
    out.close();
  }


  /**
   * Generates a debugging page.
   * @param resp The object used to respond to the client.
   * @throws IOException Caused by an error writing to the client.
   */
  private void outputFile(HttpServletResponse resp, String filename) throws IOException {
    PrintWriter out = resp.getWriter();
    new ResourceTextFile(filename).sendTo(out);
    out.close();
  }


  /**
   * Execute the appropriate query, and display the results.
   * @param req The user request.
   * @param resp The response object for output to the user.
   * @param graphUri The URI in a user request.
   * @throws IOException Error sending a response to the client.
   */
  private void doQuery(HttpServletRequest req, HttpServletResponse resp, String graphUri) throws IOException {
    clearOldResults(req);

    // work out which commands to run
    String command = generateCommand(req, graphUri);

    if (DEBUG && command != null && command.contains(DEBUG_HOOK)) {
      outputFile(resp, DEBUG_PAGE);
      return;
    }

    // No command to run, so show the entry page
    if (command == null || command.length() == 0) {
      outputStandardTemplate(resp);
      return;
    }

    // execute all the commands, and accumulate the results
    List<Object> results = null;
    List<Command> cmds = null;
    long time = 0;
    try {
      // record how long this takes
      time = System.currentTimeMillis();
      final Connection c = getConnection(req);
      cmds = getInterpreter(req, command, graphUri).parseCommands(command);
      results = C.map(cmds, new Fn1E<Command,Object,Exception>() { public Object fn(Command cmd) throws Exception { return cmd.execute(c); } });
      time = System.currentTimeMillis() - time;
    } catch (MulgaraParserException mpe) {
      resp.sendError(SC_BAD_REQUEST, "Error parsing command: " + mpe.getMessage());
      return;
    } catch (RequestException re) {
      resp.sendError(SC_BAD_REQUEST, "Error processing request: " + re.getMessage());
      return;
    } catch (IllegalStateException ise) {
      resp.sendError(SC_SERVICE_UNAVAILABLE, ise.getMessage());
      return;
    } catch (Exception e) {
      // resp.sendError(SC_BAD_REQUEST, "Error executing command. Reason: " + StackTrace.getReasonMessage(e));
      resp.sendError(SC_BAD_REQUEST, "Error executing command. Reason: " + StackTrace.throwableToString(e));
      return;
    }

    // Use the first graph mentioned as the future default
    for (Command c: cmds) {
      if (c instanceof Query) {
        updateDefaultGraph(c);
        break;
      }
    }

    // Get the tags to use in the page template
    Map<String,String> templateTags = getTemplateTagMap();
    templateTags.put(GRAPH_TAG, defaultGraph(graphUri));
    // Generate the page
    QueryResponsePage page = new QueryResponsePage(req, resp, templateTags, getTemplateHeaderFile(), getTemplateTailFile());
    page.writeResults(time, cmds, results);
  }


  /**
   * Print out the next page of a set of results, specified by the ordinal number
   * @param req The request environment.
   * @param resp The response object.
   * @param ordinalStr The result number to display the next page for.
   * @throws IOException Error responding to the client.
   */
  private void doNextPage(HttpServletRequest req, HttpServletResponse resp, String ordinalStr) throws IOException {
    // get the session/request parameters, and validate
    Map<Answer,Pair<Long,Command>> unfinishedResults = getUnfinishedResults(req);
    int ordinal = 0;
    try {
      ordinal = Integer.parseInt(ordinalStr);
    } catch (NumberFormatException nfe) {
      ordinal = -1;
    }

    if (ordinal <= 0 || unfinishedResults == null || ordinal > unfinishedResults.size()) {
      resp.sendError(SC_BAD_REQUEST, "Result not available. Did you use the \"Back button\"? (result " + ordinalStr +
                 " of " + ((unfinishedResults == null) ? 0 : unfinishedResults.size()) + ")");
      clearOldResults(req);
      return;
    }

    // Close and remove all the results we don't need
    Answer remaining = closeExcept(unfinishedResults.keySet(), ordinal);
    
    // Get the tags to use in the page template
    Map<String,String> templateTags = getTemplateTagMap();
    templateTags.put(GRAPH_TAG, defaultGraph(req.getParameter(GRAPH_ARG)));
    
    // Generate the page
    QueryResponsePage page = new QueryResponsePage(req, resp, templateTags, getTemplateHeaderFile(), getTemplateTailFile());
    page.writeResult(unfinishedResults.get(remaining).second(), remaining);
  }


  /**
   * Do the work of extracting data to be uploaded, and put it in the requested graph. Create the graph if needed.
   * @param req The HTTP request containing the file.
   * @param resp The response back to the submitting client.
   * @throws IOException Due to an error reading the input stream, or writing to the response stream.
   */
  private void handleDataUpload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      // parse in the data to be uploaded
      MimeMultiNamedPart mime = new MimeMultiNamedPart(new ServletDataSource(req, UPLOAD_GRAPH_ARG));

      // validate the request
      if (mime.getCount() == 0) throw new RequestException("Request claims to have posted data, but none was supplied.");

      long time = System.currentTimeMillis();

      // Get the destination graph, and ensure it exists
      URI destGraph = getRequestedGraph(req, mime);
      Connection conn = getConnection(req);
      try {
        new CreateGraph(destGraph).execute(conn);
      } catch (QueryException e) {
        throw new RequestException("Unable to create graph <" + destGraph + ">: " + e.getMessage());
      }

      // upload the data
      Command loadCommand = null;
      for (int partNr = 0; partNr < mime.getCount(); partNr++) {
        BodyPart part = mime.getBodyPart(partNr);
        String partName = mime.getPartName(partNr);
        try {
          if (UPLOAD_FILE_ARG.equalsIgnoreCase(partName)) {
            loadCommand = loadData(destGraph, part, conn);
            break;
          }
        } catch (QueryException e) {
          String filename = part.getFileName();
          String message = "Unable to load data: " + (filename == null ? partName : filename);
          message += ".  " + StackTrace.getReasonMessage(e);
          throw new RequestException(message);
        }
      }

      time = System.currentTimeMillis() - time;

      // Get the tags to use in the page template
      Map<String,String> templateTags = getTemplateTagMap();
      templateTags.put(GRAPH_TAG, defaultGraph(destGraph.toString()));

      // Generate the page
      QueryResponsePage page = new QueryResponsePage(req, resp, templateTags, getTemplateHeaderFile(), getTemplateTailFile());
      page.writeResults(time, Collections.singletonList(loadCommand), Collections.singletonList((Object)""));

    } catch (MessagingException e) {
      resp.sendError(SC_BAD_REQUEST, "Unable to process received MIME data: " + e.getMessage());
    } catch (RequestException re) {
      resp.sendError(SC_BAD_REQUEST, re.getMessage());
    }
  }


  /**
   * Gets the graph parameter from the MIME data.
   * @param req The request object from the user.
   * @return The URI for the requested graph.
   * @throws RequestException If a graph name was an invalid URI or was not present in the request.
   */
  protected URI getRequestedGraph(HttpServletRequest req, MimeMultiNamedPart mime) throws RequestException {
    // look in the parameters
    String[] graphArgs = req.getParameterValues(UPLOAD_GRAPH_ARG);
    if (graphArgs != null) {
      if (graphArgs.length != 1) throw new RequestException("Multiple graphs requested.");
      try {
        return new URI(graphArgs[0]);
      } catch (URISyntaxException e) {
        throw new RequestException("Invalid URI for upload graph. " + e.getInput());
      }
    }
    // look in the mime data
    if (mime != null) {
      try {
        String result = mime.getParameterString(UPLOAD_GRAPH_ARG);
        if (result != null) {
          try {
            return new URI(result);
          } catch (URISyntaxException e) {
            throw new RequestException("Invalid URI for upload graph <" + e.getInput() + ">: " + result);
          }
        }
      } catch (Exception e) {
        throw new RequestException("Bad MIME data in upload request: " + e.getMessage());
      }
    }
    throw new RequestException("No graph argument provided.");
  }


  /**
   * Load MIME data into a graph.
   * @param graph The graph to load into.
   * @param data The data to be loaded, with associated meta-data.
   * @param cxt The connection to the database.
   * @return The Command that did the loading.
   * @throws IOException error reading from the client.
   * @throws RequestException Bad data passed to the load request.
   * @throws QueryException A query exception occurred during the load operation.
   */
  protected Load loadData(URI graph, BodyPart data, Connection cxt) throws RequestException, IOException, QueryException {
    String contentType = "";
    try {
      contentType = data.getContentType();
      Load loadCmd = new Load(graph, data.getInputStream(), new MimeType(contentType), data.getFileName());
      loadCmd.execute(cxt);
      return loadCmd;
    } catch (MessagingException e) {
      throw new RequestException("Unable to process data for loading: " + e.getMessage());
    } catch (MimeTypeParseException e) {
      throw new RequestException("Bad Content Type in request: " + contentType + " (" + e.getMessage() + ")");
    }
  }


  /**
   * Close all but one answer.
   * @param answers The answers to close.
   * @param ordinal The ordinal (1-based) of the answer to NOT close.
   * @return The Answer that did NOT get removed.
   */
  private Answer closeExcept(Set<Answer> answers, int ordinal) {
    Answer excludedResult = null;

    Iterator<Answer> i = answers.iterator();
    int nrResults = answers.size();
    for (int r = 0; r < nrResults; r++) {
      assert i.hasNext();
      if (r == ordinal - 1) {
        excludedResult = i.next();
      } else {
        try {
          i.next().close();
        } catch (TuplesException e) { /* ignore */ }
        i.remove();
      }
    }
    assert !i.hasNext();
    assert excludedResult != null;

    return excludedResult;
  }


  /**
   * Clears out any old results found in the session.
   * @param req The current environment.
   */
  private void clearOldResults(HttpServletRequest req) {
    Map<Answer,Pair<Long,Command>> results = getUnfinishedResults(req);
    try {
      if (results != null) {
        for (Answer a: results.keySet()) a.close();
        results.clear();
      }
    } catch (TuplesException e) {
      // ignoring these problems, since the answer is being thrown away
    }
  }


  /**
   * Finds any unfinished data in the current session.
   * @param req The current request environment.
   * @return The unfinished results that were recorded in this session, or <code>null</code>
   *         if there were no unfinished results.
   */
  @SuppressWarnings("unchecked")
  private Map<Answer,Pair<Long,Command>> getUnfinishedResults(HttpServletRequest req) {
    Map<Answer,Pair<Long,Command>> oldResultData = (Map<Answer,Pair<Long,Command>>)req.getSession().getAttribute(UNFINISHED_RESULTS);
    return (oldResultData == null) ? null : oldResultData;
  }


  /**
   * Analyse the request parameters and work out what kind of query to generate.
   * Resource and Literal queries are mutually exclusive, and both override
   * an explicit query argument.
   * @param req The request environment.
   * @param graphUri The graphUri set for this request.
   * @return The command or commands to execute.
   */
  private String generateCommand(HttpServletRequest req, String graphUri) {
    String queryResource = req.getParameter(QUERY_RESOURCE_ARG);
    if (queryResource != null) return buildResourceQuery(graphUri, queryResource);

    String queryLiteral = req.getParameter(QUERY_LITERAL_ARG);
    if (queryLiteral != null) return buildLiteralQuery(graphUri, queryLiteral);

    return req.getParameter(QUERY_TEXT_ARG);
  }


  /**
   * Get a query string to display a resource.
   * @param graph The name of the graph to get the resource from.
   * @param resource The name of the resource.
   * @return The query string.
   */
  private String buildResourceQuery(String graph, String resource) {
    StringBuilder urlString = new StringBuilder("select $Predicate $Object from <");
    urlString.append(graph).append("> where <").append(resource).append("> $Predicate $Object;");
    urlString.append("select $Subject $Predicate from <").append(graph);
    urlString.append("> where $Subject $Predicate <").append(resource).append(">;");
    urlString.append("select $Subject $Object from <").append(graph);
    urlString.append("> where $Subject <").append(resource).append("> $Object;");
    return urlString.toString();
  }


  /**
   * Get a query string to display a literal.
   * @param graph The name of the graph to get the resource from.
   * @param literal The value of the literal.
   * @return The query string.
   */
  private String buildLiteralQuery(String graph, String literal) {
    return "select $Subject $Predicate  from <" + graph + "> where $Subject $Predicate " + literal + ";";
  }


  /**
   * Takes each of the template tags and creates a map out of them.
   * @return A map of all tags to the data to replace them.
   */
  private Map<String,String> getTemplateTagMap() {
    Map<String,String> tagMap = new HashMap<String,String>();
    String[][] source = getTemplateTags();
    for (String[] tag: source) tagMap.put(tag[0], tag[1]);
    return tagMap;
  }


  /**
   * Gets the list of tags to be replaced in a template document, along with the values
   * to replace them with.
   * @return An array of string pairs. The first string in the pair is the tag to replace,
   *         the second string is the value to repace the tag with.
   */
  private String[][] getTemplateTags() {
   return new String[][] {
        new String[] {HOSTNAME_TAG, hostname},
        new String[] {SERVERNAME_TAG, servername},
        new String[] {JARURL_TAG, resourcePath},
        new String[] {EXECUTE_TAG, EXECUTE_LINK},
        new String[] {DEBUG_TAG, debugText},
        new String[] {BASE_PATH_TAG, basePath}
    };
  }


  /**
   * Updates the default graph URI to one found in a Query.
   * @param cmd A Command that contains a query.
   */
  private void updateDefaultGraph(Command cmd) {
    assert cmd instanceof Query;
    Set<URI> graphs = ((Query)cmd).getModelExpression().getGraphURIs();
    if (!graphs.isEmpty()) defaultGraphUri = C.first(graphs).toString();
  }

  /**
   * Creates the default graph name for the sample data.
   * @param graphParam The graph name that the user has already set.
   * @return The default graph name to use when no graph has been set.
   */
  private String defaultGraph(String graphParam) {
    if (graphParam != null && graphParam.length() > 0) return graphParam;
    return defaultGraphUri;
  }


  /**
   * Gets the interpreter for the current session, creating it if it doesn't exist yet.
   * @param req The current request environment.
   * @param cmd The command the interpreter will be used on.
   * @param graphUri The string form of the URI for the default graph to use in the interpreter.
   * @return A connection that is tied to this HTTP session.
   * @throws RequestException An internal error occured with the default graph URI.
   */
  private Interpreter getInterpreter(HttpServletRequest req, String cmd, String graphUri) throws RequestException {
    RegInterpreter ri = getRegInterpreter(cmd);
    HttpSession httpSession = req.getSession();
    Interpreter interpreter = (Interpreter)httpSession.getAttribute(ri.getRegString());
    if (interpreter == null) {
      interpreter = ri.getInterpreterFactory().fn();
      httpSession.setAttribute(ri.getRegString(), interpreter);
    }
    setDefaultGraph(interpreter, graphUri);
    return interpreter;
  }


  /**
   * Sets the default graph on an interpreter
   * @param i The interpreter to set the default graph for.
   * @param graph The graph to use with the interpreter.
   * @throws RequestException An internal error where a valid graph could not be refered to.
   */
  private void setDefaultGraph(Interpreter i, String graph) throws RequestException {
    // set the default graph, if applicable
    try {
      i.setDefaultGraphUri(graph);
    } catch (Exception e) {
      try {
        i.setDefaultGraphUri(defaultGraphUri);
      } catch (URISyntaxException e1) {
        throw new RequestException("Unable to create URI for: " + defaultGraphUri, e1);
      }
    }
  }

  /**
   * Gets a factory for creating an interpreter, along with the name for that type of interpreter
   * to be registered under
   * @param query The query to determine the interpreter type
   * @return An interpreter constructor and name
   */
  private RegInterpreter getRegInterpreter(String query) {
    Fn<Interpreter> factory = null;
    String attr = null;
    if (SparqlUtil.looksLikeSparql(query)) {
      factory = new Fn<Interpreter>(){ public Interpreter fn(){ return new SparqlInterpreter(); }};
      attr = SPARQL_INTERPRETER;
    } else {
      factory = new Fn<Interpreter>(){ public Interpreter fn() { return new TerminatingTqlInterpreter(); }};
      attr = TQL_INTERPRETER;
    }
    return new RegInterpreter(factory, attr);
  }


  /**
   * Get the name of the file to be used for the template.
   * @return The absolute file path, with a root set at the resource directory.
   */
  protected String getTemplateFile() {
    return TEMPLATE;
  }


  /**
   * Get the name of the file to be used for the header template.
   * @return The absolute file path, with a root set at the resource directory.
   */
  protected String getTemplateHeaderFile() {
    return TEMPLATE_HEAD;
  }


  /**
   * Get the name of the file to be used for the footer template.
   * @return The absolute file path, with a root set at the resource directory.
   */
  protected String getTemplateTailFile() {
    return TEMPLATE_TAIL;
  }


  /**
   * Compare a parameter name to a set of known parameter names used for uploading.
   * @param name The name to check.
   * @return <code>true</code> if the name is known. <code>false</code> if not known or <code>null</code>.
   */
  @SuppressWarnings("unused")
  private boolean knownUploadParam(String name) {
    final String[] knownParams = new String[] { UPLOAD_GRAPH_ARG };
    for (String p: knownParams) if (p.equalsIgnoreCase(name)) return true;
    return false;
  }


  /**
   * Returns the filename extension for a given path.
   * @param path The path to get the extension for.
   * @return The extension, including the . character. If there is no extension, then an empty string.
   */
  private static String getExtension(String path) {
    if (path == null) return "";
    int dot = path.lastIndexOf('.');
    if (dot < 0) return "";
    return path.substring(dot);
  }



  private String calcBasePath(String fullpath) {
    if (fullpath.contains(SERVLET_PATH)) {
      return fullpath.substring(0, fullpath.indexOf(SERVLET_PATH) + SERVLET_PATH.length()) + "/";
    }
    if (fullpath.contains(TUTORIAL_PATH)) {
      return fullpath.substring(0, fullpath.indexOf(TUTORIAL_PATH) + TUTORIAL_PATH.length()) + "/";
    }
    return "/";
  }


  /**
   * Returns a relative path, starting from a given base.
   * @param full The full path to be truncated.
   * @return The new relative path.
   */
  private String relPath(String full) {
    if (full.startsWith(basePath)) {
      String path = full.substring(basePath.length());
      return path.startsWith("/") ? path : "/" + path;
    }
    return full;
  }


  /**
   * Registerable Interpreter. This contains a factory for an interpreter, plus the name it should
   * be registered under.
   */
  private static class RegInterpreter {
    /** The interpreter factory */
    private final Fn<Interpreter> intFactory;

    /** The registration name for the interpreter built from the factory */
    private final String regString;

    /** Create a link between an interpreter factory and the name it should be registered under */
    public RegInterpreter(Fn<Interpreter> intFactory, String regString) {
      this.intFactory = intFactory;
      this.regString = regString;
    }

    /** Get the method for creating an interpreter */
    public Fn<Interpreter> getInterpreterFactory() {
      return intFactory;
    }

    /** Get the name constructed interpreters should be created under */
    public String getRegString() {
      return regString;
    }
  }


  /**
   * Extension of TQL interpreter that will automatically terminate any
   * commands that are not already terminated.
   */
  private static class TerminatingTqlInterpreter extends TqlInterpreter {

    /** The terminating character. */
    private static final String TERMINATOR = ";";

    /**
     * Calls TqlInterpreter#parseCommands(String) with a guaranteed termination of ";".
     * @see TqlInterpreter#parseCommands(String)
     */
    public List<Command> parseCommands(String command) throws MulgaraParserException, MulgaraLexerException, IOException {
      command = command.trim();
      if (!command.endsWith(TERMINATOR)) command += TERMINATOR;
      return super.parseCommands(command);
    }
  }

}
