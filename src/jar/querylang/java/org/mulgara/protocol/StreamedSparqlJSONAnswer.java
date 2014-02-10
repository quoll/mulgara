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
import java.io.OutputStreamWriter;
import java.net.URI;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.mulgara.query.Answer;
import org.mulgara.query.BooleanAnswer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Represents an Answer as JSON.
 * The format is specified at: {@link http://www.w3.org/TR/rdf-sparql-json-res/}
 *
 * @created Sep 1, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StreamedSparqlJSONAnswer extends AbstractStreamedAnswer implements StreamedJSONAnswer {

  /** Additional metadata about the results. */
  URI additionalMetadata = null;
  
  /** Boolean answer. */
  boolean booleanResult = false;

  /** Internal flag to indicate that a comma may be needed. */
  boolean prependComma = false;

  /**
   * Creates a JSON Answer conforming to SPARQL JSON results.
   * @param answer The Answer to wrap.
   * @param output The stream to write to.
   */
  public StreamedSparqlJSONAnswer(Answer answer, OutputStream output) {
    super((answer instanceof BooleanAnswer) ? null : answer, output);
    if (answer instanceof BooleanAnswer) booleanResult = ((BooleanAnswer)answer).getResult();
  }

  /**
   * Creates an JSON Answer with additional metadata.
   * @param answer The Answer to wrap.
   * @param metadata Additional metadata for the answer.
   * @param output The stream to write to.
   */
  public StreamedSparqlJSONAnswer(Answer answer, URI metadata, OutputStream output) {
    this(answer, output);
    additionalMetadata = metadata;
  }

  /**
   * Creates a JSON Answer conforming to SPARQL JSON results.
   * @param result The boolean result to encode.
   * @param output The stream to write to.
   */
  public StreamedSparqlJSONAnswer(boolean result, OutputStream output) {
    super((Answer)null, output);
    booleanResult = result;
  }

  /**
   * Creates a JSON Answer with additional metadata.
   * @param result The boolean result to encode.
   * @param metadata Additional metadata for the answer.
   * @param output The stream to write to.
   */
  public StreamedSparqlJSONAnswer(boolean result, URI metadata, OutputStream output) {
    super((Answer)null, output);
    booleanResult = result;
    additionalMetadata = metadata;
  }

  /**
   * Creates a JSON Answer conforming to SPARQL JSON results.
   * @param answer The Answer to wrap.
   * @param output The stream to write to.
   * @param charsetName The name of the character set to use, if not the default of UTF-8.
   */
  public StreamedSparqlJSONAnswer(Answer answer, OutputStream output, String charsetName) {
    super((answer instanceof BooleanAnswer) ? null : answer, output, charsetName);
    if (answer instanceof BooleanAnswer) booleanResult = ((BooleanAnswer)answer).getResult();
  }

  /**
   * Creates an JSON Answer with additional metadata.
   * @param answer The Answer to wrap.
   * @param metadata Additional metadata for the answer.
   * @param output The stream to write to.
   * @param charsetName The name of the character set to use, if not the default of UTF-8.
   */
  public StreamedSparqlJSONAnswer(Answer answer, URI metadata, OutputStream output, String charsetName) {
    this(answer, output, charsetName);
    additionalMetadata = metadata;
  }

  /**
   * Creates a JSON Answer conforming to SPARQL JSON results.
   * @param result The boolean result to encode.
   * @param output The stream to write to.
   * @param charsetName The name of the character set to use, if not the default of UTF-8.
   */
  public StreamedSparqlJSONAnswer(boolean result, OutputStream output, String charsetName) {
    super((Answer)null, output, charsetName);
    booleanResult = result;
  }

  /**
   * Creates a JSON Answer with additional metadata.
   * @param result The boolean result to encode.
   * @param metadata Additional metadata for the answer.
   * @param output The stream to write to.
   * @param charsetName The name of the character set to use, if not the default of UTF-8.
   */
  public StreamedSparqlJSONAnswer(boolean result, URI metadata, OutputStream output, String charsetName) {
    super((Answer)null, output, charsetName);
    booleanResult = result;
    additionalMetadata = metadata;
  }

  /** {@inheritDoc} */
  public void addDocHeader() throws IOException {
    s.append("{ ");
  }

  /** {@inheritDoc} */
  public void addDocFooter() throws IOException {
    s.append(" }");
  }

  /** {@inheritDoc} */
  protected void addHeader(Answer answer) throws IOException {
    s.append("\"head\": {");
    boolean wroteVars = false;
    if (answer != null && answer.getVariables() != null) {
      s.append("\"vars\": [");
      prependComma = false;
      for (Variable v: answer.getVariables()) addHeaderVariable(v);
      s.append("]");
      wroteVars = true;
    }
    if (additionalMetadata != null) {
      if (wroteVars) s.append(", ");
      s.append("\"link\": [\"").append(additionalMetadata.toString()).append("\"]");
    }
    s.append("}");
    prependComma = true;
  }

  /** {@inheritDoc} */
  protected void addHeaderVariable(Variable var) throws IOException {
    comma().append("\"").append(var.getName()).append("\"");
  }

  /**
   * No Answer footer needed for this type of document
   */
  protected void addFooter(Answer answer) throws IOException {
  }

  /** {@inheritDoc} */
  protected void addResults(Answer answer) throws TuplesException, IOException {
    if (answer != null) {
      comma().append("\"results\": { ");
      s.append("\"bindings\": [ ");
      answer.beforeFirst();
      prependComma = false;
      while (answer.next()) addResult(answer);
      s.append(" ] }");
    } else {
      comma().append("\"boolean\": ").append(Boolean.toString(booleanResult));
    }
  }

  /** {@inheritDoc} */
  protected void addResult(Answer answer) throws TuplesException, IOException {
    comma().append("{ ");
    prependComma = false;
    for (int c = 0; c < width; c++) addBinding(vars[c], answer.getObject(c));
    s.append(" }");
  }

  /**
   * {@inheritDoc}
   * No binding will be emitted if the value is null (unbound).
   */
  protected void addBinding(Variable var, Object value) throws IOException {
    if (value != null) {
      comma().append("\"").append(var.getName()).append("\": { ");
      // no dynamic dispatch, so use if/then
      if (value instanceof URIReference) addURI((URIReference)value);
      else if (value instanceof Literal) addLiteral((Literal)value);
      else if (value instanceof BlankNode) addBNode((BlankNode)value);
      else throw new IllegalArgumentException("Unable to create a SPARQL response with an answer containing: " + value.getClass().getSimpleName());
      s.append(" }");
    }
  }

  /** {@inheritDoc} */
  protected void addURI(URIReference uri) throws IOException {
    s.append("\"type\": \"uri\", \"value\": \"").append(uri.getURI().toString()).append("\"");
  }

  /** {@inheritDoc} */
  protected void addBNode(BlankNode bnode) throws IOException {
    s.append("\"type\": \"bnode\", \"value\": \"").append(bnode.toString()).append("\"");
  }

  /** {@inheritDoc} */
  protected void addLiteral(Literal literal) throws IOException {
    if (literal.getDatatype() != null) {
      s.append("\"type\": \"typed-literal\", \"datatype\": \"").append(literal.getDatatype().toString()).append("\", ");
    } else {
      s.append("\"type\": \"literal\", ");
      if (literal.getLanguage() != null) s.append("\"xml:lang\": \"").append(literal.getLanguage()).append("\", ");
    }
    s.append("\"value\": \"").append(jsonEscape(literal.getLexicalForm())).append("\"");
  }


  /**
   * Escapes strings to be JSON compatible. JSON only expects 16 bit characters.
   * @param in The string to be escaped.
   * @return The escaped string.
   */
  protected String jsonEscape(String in) {
    StringBuffer out = new StringBuffer();
    for (int i = 0; i < in.length(); i++) {
      char c = in.charAt(i);
      if (c == '/') out.append("\\/");
      else if (c == '\\') out.append("\\\\");
      else if (c == '"') out.append("\\\"");
      else  if (Character.isISOControl(c)) {
        if (c == '\b') out.append("\\b");
        else if (c == '\t') out.append("\\t");
        else if (c == '\n') out.append("\\n");
        else if (c == '\f') out.append("\\f");
        else if (c == '\r') out.append("\\r");
        else out.append("\\u").append(String.format("%04x", (int)c));
      } else {
        out.append(c);
      }
    }
    return out.toString();
  }


  /**
   * Adds a comma if needed at this point. Commas are usually needed.
   * @throws IOException An error writing to the stream.
   */
  protected OutputStreamWriter comma() throws IOException {
    if (prependComma) s.append(", ");
    prependComma = true;
    return s;
  }

  public void addAnswer(Answer data) throws TuplesException, IOException {
    addResults(data);
  }

}
