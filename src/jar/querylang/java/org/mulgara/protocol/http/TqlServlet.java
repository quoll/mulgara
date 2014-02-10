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

package org.mulgara.protocol.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mulgara.itql.TqlInterpreter;
import org.mulgara.parser.Interpreter;
import org.mulgara.protocol.StreamedAnswer;
import org.mulgara.protocol.StreamedSparqlJSONAnswer;
import org.mulgara.protocol.StreamedSparqlJSONObject;
import org.mulgara.protocol.StreamedSparqlXMLObject;
import org.mulgara.protocol.StreamedTqlXMLAnswer;
import org.mulgara.query.Answer;
import org.mulgara.query.Query;
import org.mulgara.query.TuplesException;
import org.mulgara.query.operation.Command;
import org.mulgara.server.SessionFactoryProvider;
import org.mulgara.util.functional.Pair;

/**
 * A query gateway for TQL.
 *
 * @created Sep 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class TqlServlet extends ProtocolServlet {

  /** Serialization ID */
  private static final long serialVersionUID = -72714067636720775L;


  /**
   * Creates the servlet for communicating with the given server.
   * @param server The server that provides access to the database.
   */
  public TqlServlet(SessionFactoryProvider server) throws IOException {
    super(server);
  }


  /**
   * Creates the servlet in a default application server environment.
   */
  public TqlServlet() throws IOException {
  }


  /** @see org.mulgara.protocol.http.ProtocolServlet#initializeBuilders() */
  protected void initializeBuilders() {
    // TODO: create a JSON answer and a XML object for TQL.
    AnswerStreamConstructor jsonBuilder = new AnswerStreamConstructor() {
      public StreamedAnswer fn(Answer ans, OutputStream s) { return new StreamedSparqlJSONAnswer(ans, s); }
    };
    AnswerStreamConstructor xmlBuilder = new AnswerStreamConstructor() {
      public StreamedAnswer fn(Answer ans, OutputStream s) { return new StreamedTqlXMLAnswer(ans, s); }
    };
    streamBuilders.put(Output.JSON, jsonBuilder);
    streamBuilders.put(Output.XML, xmlBuilder);
    streamBuilders.put(Output.RDFXML, xmlBuilder);  // TODO: create an RDF/XML builder
    streamBuilders.put(Output.N3, xmlBuilder);      // TODO: create an N3 builder

    ObjectStreamConstructor jsonObjBuilder = new ObjectStreamConstructor() {
      public StreamedAnswer fn(Object o, OutputStream s) { return new StreamedSparqlJSONObject(o, s); }
    };
    ObjectStreamConstructor xmlObjBuilder = new ObjectStreamConstructor() {
      public StreamedAnswer fn(Object o, OutputStream s) { return new StreamedSparqlXMLObject(o, s); }
    };
    objectStreamBuilders.put(Output.JSON, jsonObjBuilder);
    objectStreamBuilders.put(Output.XML, xmlObjBuilder);
    objectStreamBuilders.put(Output.RDFXML, xmlObjBuilder);  // TODO: create an RDF/XML object builder
    objectStreamBuilders.put(Output.N3, xmlObjBuilder);      // TODO: create an N3 object builder
  }


  /**
   * Provide a description for the servlet.
   * @see javax.servlet.GenericServlet#getServletInfo()
   */
  public String getServletInfo() {
    return "Mulgara TQL Query Endpoint";
  }


  /**
   * Gets the TQL interpreter for the current session,
   * creating it if it doesn't exist yet.
   * @param req The current request environment.
   * @return A connection that is tied to this HTTP session.
   */
  protected TqlInterpreter getInterpreter(HttpServletRequest req) throws BadRequestException {
    HttpSession httpSession = req.getSession();
    TqlInterpreter interpreter = (TqlInterpreter)httpSession.getAttribute(INTERPRETER);
    if (interpreter == null) {
      interpreter = new TqlInterpreter();
      httpSession.setAttribute(INTERPRETER, interpreter);
    }
    return interpreter;
  }

  /**
   * Respond to a request for the servlet.
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void doResponse(HttpServletRequest req, HttpServletResponse resp, RestParams params) throws ServletException, IOException {
    RestParams.ResourceType type = params.getType();

    if (type != RestParams.ResourceType.QUERY) {

      // This is a GET on a graph or a statement
      super.doResponse(req, resp, params);

    } else {

      // build a list of queries
      List<Query> queries = getQueries(params.getQuery(), req);
      boolean firstItem = true;
      Output outputType = null;
      StreamedAnswer output = null;
      // respond to everything in the list
      for (Query q: queries) {
        Answer result = null;
        try {
          result = executeQuery(q, req);
          
          if (firstItem) {
            firstItem = false;
            outputType = getOutputType(req, q);
            // start the response
            output = sendDocumentHeader(streamBuilders, outputType, resp);
          } else {
            assert outputType != null;
            if (outputType != getOutputType(req, q)) {
              throw new BadRequestException("Incompatible mixed response types requested");
            }
          }
          assert output != null;
          output.addAnswer(result);
        } catch (TuplesException e) {
          throw new InternalErrorException("Error reading answers: " + e.getMessage());
        } finally {
          try {
            if (result != null) result.close();
          } catch (TuplesException e) {
            logger.warn("Error closing: " + e.getMessage(), e);
          }
        }
      }
      if (output == null) {
        OutputStream s = resp.getOutputStream();
        if (s != null) s.close();
      } else {
        output.addDocFooter();
        output.close();
      }
    }
  }


  /**
   * Sends back a response when a list has been requested. This is separate from the doResponse
   * method above (which does something similar), because this is processing all results
   * after they have been completed. This happens during a POST, because many of the operations
   * may not have been queries, whereas the doResponse above is onyl dealing with a series of
   * read-only queries.
   * @param results The list of Answer/Output pairs.
   * @param resp The response object to send the data on.
   * @throws BadRequestException More than one type appeared in the request.
   * @throws InternalErrorException There was a problem processing the request.
   * @throws IOException There was an error responding to the client.
   */
  protected void doResponseList(List<Pair<Answer,Output>> results, HttpServletResponse resp) throws BadRequestException, InternalErrorException, IOException {
    // these variables are to remember state throughout the loop
    boolean firstItem = true;
    Output outputType = null;
    StreamedAnswer output = null;
    // respond to everything in the list
    for (Pair<Answer,Output> result: results) {
      try {
        if (firstItem) {
          firstItem = false;
          // start the response
          outputType = result.second();
          output = sendDocumentHeader(streamBuilders, outputType, resp);
        } else {
          assert outputType != null;
          if (outputType != result.second()) {
            throw new BadRequestException("Incompatible mixed response types requested");
          }
        }
        assert output != null;
        output.addAnswer(result.first());
      } catch (TuplesException e) {
        throw new InternalErrorException("Error reading answers: " + e.getMessage());
      } finally {
        try {
          if (result != null) result.first().close();
        } catch (TuplesException e) {
          logger.warn("Error closing: " + e.getMessage(), e);
        }
      }
    }
    if (output == null) {
      OutputStream s = resp.getOutputStream();
      if (s != null) s.close();
    } else {
      output.addDocFooter();
      output.close();
    }

  }


  /**
   * Converts a SPARQL query string into a Query object. This uses extra parameters from the
   * client where appropriate, such as the default graph.
   * @param query The query string issued by the client.
   * @param req The request from the client.
   * @return A new Query object, built from the query string.
   * @throws BadRequestException Due to an invalid command string.
   */
  List<Query> getQueries(String query, HttpServletRequest req) throws BadRequestException {
    if (query == null) throw new BadRequestException("Query must be supplied");
    try {
      Interpreter interpreter = getInterpreter(req);
      List<Command> cmdList = interpreter.parseCommands(query);
      List<Query> qList = new ArrayList<Query>();
      for (Command c: cmdList) {
        if (!(c instanceof Query)) throw new BadRequestException("Modifying command used instead of a query: " + c.getText());
        qList.add((Query)c);
      }
      return qList;
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }


  StreamedAnswer sendDocumentHeader(Map<Output,? extends StreamConstructor<Answer>> builders, Output type, HttpServletResponse resp) throws IOException, BadRequestException, InternalErrorException {
    // establish the output type
    if (type == null) type = DEFAULT_OUTPUT_TYPE;

    resp.setContentType(type.mimeText);
    resp.setHeader("pragma", "no-cache");

    // get the constructor for the stream outputter
    StreamConstructor<Answer> constructor = builders.get(type);
    if (constructor == null) throw new BadRequestException("Unknown result type: " + type);

    try {
      OutputStream out = resp.getOutputStream();
      StreamedAnswer strAns = constructor.fn(null, out);
      strAns.initOutput();
      strAns.addDocHeader();
      return strAns;
    } catch (IOException ioe) {
      // There's no point in telling the client if we can't talk to the client
      throw ioe;
    } catch (Exception e) {
      throw new InternalErrorException(e.getMessage());
    }
  }



}
