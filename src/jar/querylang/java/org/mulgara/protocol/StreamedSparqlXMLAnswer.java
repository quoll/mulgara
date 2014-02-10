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
import java.net.URI;
import java.util.Map;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.mulgara.query.Answer;
import org.mulgara.query.BooleanAnswer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.util.StringUtil;

/**
 * Represents an Answer as XML.
 * The format is specified at: {@link http://www.w3.org/TR/rdf-sparql-XMLres/}
 *
 * @created Jul 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StreamedSparqlXMLAnswer extends AbstractStreamedXMLAnswer {

  /** Indent to use for namespaces in the document header. */
  private static final String HEADER_INDENT = "\n        ";

  /** Indicates that the W3C Schema should be used. */
  boolean useW3CSchema = false;

  /** Additional metadata about the results. */
  URI additionalMetadata = null;
  
  /** Boolean answer. */
  boolean booleanResult = false;

  /**
   * Creates an XML Answer conforming to SPARQL XML results.
   * @param answer The Answer to wrap.
   */
  public StreamedSparqlXMLAnswer(Answer answer, OutputStream output) {
    super((answer instanceof BooleanAnswer) ? null : answer, output);
    if (answer instanceof BooleanAnswer) booleanResult = ((BooleanAnswer)answer).getResult();
  }

  /**
   * Creates an XML Answer with additional metadata.
   * @param answer The Answer to wrap.
   * @param metadata Additional metadata for the answer.
   */
  public StreamedSparqlXMLAnswer(Answer answer, URI metadata, OutputStream output) {
    this(answer, output);
    additionalMetadata = metadata;
  }

  /**
   * Creates an XML Answer conforming to SPARQL XML results.
   * @param result The boolean result to encode.
   */
  public StreamedSparqlXMLAnswer(boolean result, OutputStream output) {
    super((Answer)null, output);
    booleanResult = result;
  }

  /**
   * Creates an XML Answer with additional metadata.
   * @param result The boolean result to encode.
   * @param metadata Additional metadata for the answer.
   */
  public StreamedSparqlXMLAnswer(boolean result, URI metadata, OutputStream output) {
    super((Answer)null, output);
    booleanResult = result;
    additionalMetadata = metadata;
  }

  /**
   * Set this XMLAnswer to use the W3C Schema for SPARQL results.
   * @param use Set to <code>true</code> if the W3C schema should be used.
   */
  public void useW3CSchema(boolean use) {
    useW3CSchema = use;
    s = null;
  }

  /** {@inheritDoc} */
  public void addDocHeader() throws IOException {
    s.append("<?xml version=\"1.0\" encoding=\"");
    s.append(charset.name());
    s.append("\"?>\n");
    s.append("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"");
    for (Map.Entry<String,URI> ns: namespaces.entrySet()) {
      s.append(prettyPrint ? HEADER_INDENT : " ");
      s.append(ns.getKey()).append("=\"").append(ns.getValue().toString()).append("\"");
    }
    if (useW3CSchema) s.append(prettyPrint ? HEADER_INDENT : " ").append("xsi:schemaLocation=\"http://www.w3.org/2007/SPARQL/result.xsd\"");
    s.append(">");
  }

  /** {@inheritDoc} */
  public void addDocFooter() throws IOException {
    s.append(i(0)).append("</sparql>");
  }

  /** {@inheritDoc} */
  protected void addHeader(Answer a) throws IOException {
    s.append(i(1)).append("<head>");
    if (a != null && a.getVariables() != null) {
      for (Variable v: a.getVariables()) addHeaderVariable(v);
    }
    if (additionalMetadata != null) {
      s.append(i(2)).append("<link href=\"").append(additionalMetadata.toString()).append("\"/>");
    }
    s.append(i(1)).append("</head>");
  }

  /**
   * No Answer footer needed for this type of document
   */
  protected void addFooter(Answer answer) throws IOException {
  }

  /** {@inheritDoc} */
  protected void addHeaderVariable(Variable var) throws IOException {
    s.append(i(2)).append("<variable name=\"");
    s.append(var.getName()).append("\"/>");
  }

  /** {@inheritDoc} */
  protected void addResults(Answer a) throws TuplesException, IOException {
    if (a != null) {
      s.append(i(1)).append("<results>");
      a.beforeFirst();
      while (a.next()) addResult(a);
      s.append(i(1)).append("</results>");
    } else {
      s.append(i(1)).append("<boolean>").append(Boolean.toString(booleanResult)).append("</boolean>");
    }
  }

  /** {@inheritDoc} */
  protected void addResult(Answer a) throws TuplesException, IOException {
    s.append(i(2)).append("<result>");
    for (int c = 0; c < width; c++) addBinding(vars[c], a.getObject(c));
    s.append(i(2)).append("</result>");
  }

  /**
   * {@inheritDoc}
   * No binding will be emitted if the value is null (unbound).
   */
  protected void addBinding(Variable var, Object value) throws IOException {
    if (value != null) {
      s.append(i(3)).append("<binding name=\"").append(var.getName()).append("\">");
      // no dynamic dispatch, so use if/then
      if (value instanceof URIReference) addURI((URIReference)value);
      else if (value instanceof Literal) addLiteral((Literal)value);
      else if (value instanceof BlankNode) addBNode((BlankNode)value);
      else throw new IllegalArgumentException("Unable to create a SPARQL response with an answer containing: " + value.getClass().getSimpleName());
      s.append(i(3)).append("</binding>");
    }
  }

  /** {@inheritDoc} */
  protected void addURI(URIReference uri) throws IOException {
    s.append(i(4)).append("<uri>").append(uri.getURI().toString()).append("</uri>");
  }

  /** {@inheritDoc} */
  protected void addBNode(BlankNode bnode) throws IOException {
    s.append(i(4)).append("<bnode>").append(bnode.toString()).append("</bnode>");
  }

  /** {@inheritDoc} */
  protected void addLiteral(Literal literal) throws IOException {
    s.append(i(4)).append("<literal");
    if (literal.getLanguage() != null) s.append(" xml:lang=\"").append(literal.getLanguage()).append("\"");
    else if (literal.getDatatype() != null) s.append(" datatype=\"").append(literal.getDatatype().toString()).append("\"");
    s.append(">").append(StringUtil.quoteAV(literal.getLexicalForm())).append("</literal>");
  }

  /**
   * @see org.mulgara.protocol.StreamedAnswer#addAnswer(org.mulgara.query.Answer)
   */
  public void addAnswer(Answer data) throws TuplesException, IOException {
    addResults(data);
  }

}
