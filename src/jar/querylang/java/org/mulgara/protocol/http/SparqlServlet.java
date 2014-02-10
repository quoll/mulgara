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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mulgara.protocol.StreamedAnswer;
import org.mulgara.protocol.StreamedN3Answer;
import org.mulgara.protocol.StreamedRdfXmlAnswer;
import org.mulgara.protocol.StreamedSparqlJSONAnswer;
import org.mulgara.protocol.StreamedSparqlJSONObject;
import org.mulgara.protocol.StreamedSparqlXMLAnswer;
import org.mulgara.protocol.StreamedSparqlXMLObject;
import org.mulgara.query.Answer;
import org.mulgara.server.SessionFactoryProvider;
import org.mulgara.sparql.SparqlInterpreter;

/**
 * A query gateway for SPARQL.
 *
 * @created Sep 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class SparqlServlet extends ProtocolServlet {

  /** Serialization ID */
  private static final long serialVersionUID = 5047396536306099528L;

  /**
   * Creates the servlet for communicating with the given server.
   * @param server The server that provides access to the database.
   */
  public SparqlServlet(SessionFactoryProvider server) throws IOException {
    super(server);
  }


  /**
   * Creates the servlet in a default application server environment.
   */
  public SparqlServlet() throws IOException {
  }


  /** @see org.mulgara.protocol.http.ProtocolServlet#initializeBuilders() */
  protected void initializeBuilders() {
    AnswerStreamConstructor jsonBuilder = new AnswerStreamConstructor() {
      public StreamedAnswer fn(Answer ans, OutputStream s) { return new StreamedSparqlJSONAnswer(ans, s); }
    };
    AnswerStreamConstructor xmlBuilder = new AnswerStreamConstructor() {
      public StreamedAnswer fn(Answer ans, OutputStream s) { return new StreamedSparqlXMLAnswer(ans, s); }
    };
    AnswerStreamConstructor rdfXmlBuilder = new AnswerStreamConstructor() {
      public StreamedAnswer fn(Answer ans, OutputStream s) { return new StreamedRdfXmlAnswer(ans, s); }
    };
    AnswerStreamConstructor n3Builder = new AnswerStreamConstructor() {
      public StreamedAnswer fn(Answer ans, OutputStream s) { return new StreamedN3Answer(ans, s); }
    };
    
    streamBuilders.put(Output.JSON, jsonBuilder);
    streamBuilders.put(Output.XML, xmlBuilder);
    streamBuilders.put(Output.RDFXML, rdfXmlBuilder);
    streamBuilders.put(Output.N3, n3Builder);

    ObjectStreamConstructor jsonObjBuilder = new ObjectStreamConstructor() {
      public StreamedAnswer fn(Object o, OutputStream s) { return new StreamedSparqlJSONObject(o, s); }
    };
    ObjectStreamConstructor xmlObjBuilder = new ObjectStreamConstructor() {
      public StreamedAnswer fn(Object o, OutputStream s) { return new StreamedSparqlXMLObject(o, s); }
    };
    objectStreamBuilders.put(Output.JSON, jsonObjBuilder);
    objectStreamBuilders.put(Output.XML, xmlObjBuilder);
    objectStreamBuilders.put(Output.RDFXML, xmlObjBuilder);  // TODO: create an RDF/XML Object Builder
    objectStreamBuilders.put(Output.N3, xmlObjBuilder);      // TODO: create an N3 Builder
  }


  /**
   * Provide a description for the servlet.
   * @see javax.servlet.GenericServlet#getServletInfo()
   */
  public String getServletInfo() {
    return "Mulgara SPARQL Query Endpoint";
  }


  /**
   * Gets the SPARQL interpreter for the current session,
   * creating it if it doesn't exist yet.
   * @param req The current request environment.
   * @return A connection that is tied to this HTTP session.
   */
  protected SparqlInterpreter getInterpreter(HttpServletRequest req) throws BadRequestException {
    HttpSession httpSession = req.getSession();
    SparqlInterpreter interpreter = (SparqlInterpreter)httpSession.getAttribute(INTERPRETER);
    if (interpreter == null) {
      interpreter = new SparqlInterpreter();
      httpSession.setAttribute(INTERPRETER, interpreter);
    }
    interpreter.setDefaultGraphUris(getRequestedDefaultGraphs(req));
    return interpreter;
  }

}
