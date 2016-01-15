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

package org.mulgara.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Represents an Answer as TQL XML.
 *
 * @created Jul 8, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StreamedTqlXMLAnswer extends AbstractStreamedXMLAnswer {

  /**
   * Creates an XML Answer for XML results. Pretty printing is off by default.
   * @param answer The Answer to wrap.
   */
  public StreamedTqlXMLAnswer(Answer answer, OutputStream output) {
    super(answer, output);
    setPrettyPrint(false);
  }

  /**
   * Creates an XML Answer for XML results. Pretty printing is off by default.
   * @param answer The Answer to wrap.
   */
  public StreamedTqlXMLAnswer(List<Answer> answers, OutputStream output) {
    super(answers, output);
    setPrettyPrint(false);
  }

  /** {@inheritDoc} */
  public void addDocHeader() throws IOException {
    s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    s.append("<answer xmlns=\"http://mulgara.org/tql#\">");
  }

  /** {@inheritDoc} */
  public void addDocFooter() throws IOException {
    s.append(i(0)).append("</answer>");
  }

  /** {@inheritDoc} */
  protected void addHeader(Answer answer) throws IOException {
    s.append(i(1)).append("<query>");
    addHeader(answer, 0);
  }

  void addHeader(Answer a, int indent) throws IOException {
    s.append(i(indent + 2)).append("<variables>");
    if (a != null && a.getVariables() != null) for (Variable v: a.getVariables()) addHeaderVariable(v, indent);
    s.append(i(indent + 2)).append("</variables>");
  }

  protected void addFooter(Answer answer) throws IOException {
    addFooter(answer, 0);
  }

  void addFooter(Answer answer, int indent) throws IOException {
    s.append(i(indent + 1)).append("</query>");
  }

  /** {@inheritDoc} */
  protected void addHeaderVariable(Variable var) throws IOException {
    addHeaderVariable(var, 0);
  }

  /** {@inheritDoc} */
  protected void addHeaderVariable(Variable var, int indent) throws IOException {
    s.append(i(indent + 3)).append("<").append(var.getName()).append("/>");
  }

  /** {@inheritDoc} */
  protected void addResults(Answer a) throws TuplesException, IOException {
    a.beforeFirst();
    while (a.next()) addResult(a);
  }

  /**
   * Just like {@link #addResults(Answer)} only this is used for subanswers,
   * so it may be indented, and does not have a trailing end of query section.
   * @param a The current answer to get results from.
   * @param indent The level of indentation to use.
   */
  protected void addResults(Answer a, int indent) throws TuplesException, IOException {
    a.beforeFirst();
    while (a.next()) addResult(a, indent);
  }

  /** {@inheritDoc} */
  protected void addResult(Answer a) throws TuplesException, IOException {
    addResult(a, 0);
  }

  /** 
   * Prints a single row from an Answer, using the given indent.
   * @param a The answer to get the row from.
   * @param indent The indentation to use on the answer.
   */
  protected void addResult(Answer a, int indent) throws TuplesException, IOException {
    int width = (a != null) ? a.getNumberOfVariables() : 0;
    Variable[] vars = (a != null) ? a.getVariables() : null;

    s.append(i(indent + 2)).append("<solution>");
    for (int c = 0; c < width; c++) addBinding(vars[c], a.getObject(c), indent);
    s.append(i(indent + 2)).append("</solution>");
  }

  /**
   * {@inheritDoc}
   * No binding will be emitted if the value is null (unbound).
   */
  protected void addBinding(Variable var, Object value) throws TuplesException, IOException {
    addBinding(var, value, 0);
  }

  /**
   * {@inheritDoc}
   * No binding will be emitted if the value is null (unbound).
   * @throws TuplesException Indicates an error accessing the Answer.
   */
  protected void addBinding(Variable var, Object value, int indent) throws TuplesException, IOException {
    if (value != null) {
      s.append(i(indent + 3)).append("<").append(var.getName());
      // no dynamic dispatch, so use if/then
      if (value instanceof URIReference) {
        addURI((URIReference)value);
      } else if (value instanceof BlankNode) {
        addBNode((BlankNode)value);
      } else if (value instanceof Literal) {
        addLiteral((Literal)value);
        s.append("</").append(var.getName()).append(">");
      } else if (value instanceof Answer) {
        s.append(">");
        addHeader((Answer)value, indent + 4);
        addResults((Answer)value, indent + 4);
        s.append("</").append(var.getName()).append(">");
        ((Answer)value).close();
      } else throw new IllegalArgumentException("Unable to create a SPARQL response with an answer containing: " + value.getClass().getSimpleName());
    }
  }

  /** {@inheritDoc} */
  protected void addURI(URIReference uri) throws IOException {
    s.append(" resource=\"").append(uri.getURI().toString()).append("\"/>");
  }

  /** {@inheritDoc} */
  protected void addBNode(BlankNode bnode) throws IOException {
    s.append(" blank-node=\"").append(bnode.toString()).append("\"/>");
  }

  /** {@inheritDoc} */
  protected void addLiteral(Literal literal) throws IOException {
    if (literal.getLanguage() != null) s.append(" language=\"").append(literal.getLanguage()).append("\"");
    else if (literal.getDatatype() != null) s.append(" datatype=\"").append(literal.getDatatype().toString()).append("\"");
    s.append(">").append(literal.getLexicalForm());
  }

  /**
   * @see org.mulgara.protocol.StreamedAnswer#addAnswer(org.mulgara.query.Answer)
   */
  public void addAnswer(Answer data) throws TuplesException, IOException {
    addHeader(data);
    addResults(data);
    addFooter(data);
  }

}
