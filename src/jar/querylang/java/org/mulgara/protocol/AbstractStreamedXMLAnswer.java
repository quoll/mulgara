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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Basic functionality for emitting Answers as XML using raw text methods.
 *
 * @created Jul 9, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public abstract class AbstractStreamedXMLAnswer extends AbstractStreamedAnswer implements StreamedXMLAnswer {

  /** A single indent for use when pretty printing. */
  static final private String SINGLE_INDENT_STR = "  ";

  /** The size of indents to use for pretty printing. */
  static final private int SINGLE_INDENT_SIZE = SINGLE_INDENT_STR.length();

  /** The largest indent string allowed. */
  static final private String MAX_INDENT = "\n            ";

  /**
   * An array of indent strings used by {@link #i(int)}.
   * This is pre-generated to avoid creating them while emiting xml.
   */
  static final String[] INDENT = new String[MAX_INDENT.length() / SINGLE_INDENT_SIZE];

  // Build the indents using the MAX_INDENT as a raw data source
  static {
    for (int i = 0; i < INDENT.length; i++) INDENT[i] = MAX_INDENT.substring(0, i * SINGLE_INDENT_SIZE + 1);
  }

  /** All the namespaces explicitly requested. */
  Map<String,URI> namespaces = new HashMap<String,URI>();

  /** Indicates that pretty printing should be used. */
  boolean prettyPrint = true;
 
  /**
   * Create an XMLAnswer based on a given {@link Answer}.
   * @param answer The Answer with the data to convert.
   */
  public AbstractStreamedXMLAnswer(Answer answer, OutputStream output) {
    super(answer, output);
  }

  /**
   * Create an XMLAnswer based on a given {@link Answer}.
   * @param answer The Answer with the data to convert.
   */
  public AbstractStreamedXMLAnswer(Answer answer, OutputStream output, String charsetName) {
    super(answer, output, charsetName);
  }

  /**
   * Create an XMLAnswer based on a given {@link Answer}.
   * @param answers The list of Answers with the data to convert.
   */
  public AbstractStreamedXMLAnswer(List<Answer> answers, OutputStream output) {
    super(answers, output);
  }

  /**
   * Create an XMLAnswer based on a given {@link Answer}.
   * @param answers The list of Answers with the data to convert.
   */
  public AbstractStreamedXMLAnswer(List<Answer> answers, OutputStream output, String charsetName) {
    super(answers, output, charsetName);
  }

  /**
   * @see org.mulgara.protocol.StreamedXMLAnswer#setCharacterEncoding(java.lang.String)
   */
  public void setCharacterEncoding(String encoding) {
    charset = Charset.forName(encoding);
  }

  /**
   * @see org.mulgara.protocol.StreamedXMLAnswer#setCharacterEncoding(java.nio.Charset)
   */
  public void setCharacterEncoding(Charset charset) {
    this.charset = charset;
  }

  /**
   * @see org.mulgara.protocol.StreamedXMLAnswer#addNamespace(java.lang.String, java.net.URI)
   */
  public void addNamespace(String name, URI nsValue) {
    namespaces.put(name, nsValue);
    s = null;
  }

  /**
   * @see org.mulgara.protocol.StreamedXMLAnswer#clearNamespaces()
   */
  public void clearNamespaces() {
    namespaces.clear();
    s = null;
  }

  /**
   * @see org.mulgara.protocol.StreamedXMLAnswer#setPrettyPrint(boolean)
   */
  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
    s = null;
  }

  /**
   * When {@link #setPrettyPrint(boolean)} is set, returns a string containing a new line and an indentation.
   * When pretty printing is off, then return an empty string.
   * @param indent The number of indentations to use.
   * @return A String containing the appropriate indentation, or an empty string if not pretty printing.
   */
  String i(int indent) {
    if (!prettyPrint) return "";
    if (indent < INDENT.length) return INDENT[indent];
    StringBuilder sbi = new StringBuilder(MAX_INDENT);
    for (int i = INDENT.length; i < indent; i++) sbi.append(SINGLE_INDENT_STR);
    return sbi.toString();
  }

  /**
   * Adds the document header to the buffer.
   */
  @Override
  public abstract void addDocHeader() throws IOException;

  /**
   * Adds the document footer to the buffer.
   */
  @Override
  public abstract void addDocFooter() throws IOException;

  /**
   * Adds the header to the document data.
   * @param a The answer to add a header for.
   */
  @Override
  protected abstract void addHeader(Answer a) throws IOException;

  /**
   * Adds the results to the document data.
   * @param a The answer to add the results for.
   * @throws TuplesException Indicates an error accessing the Answer.
   */
  @Override
  protected abstract void addResults(Answer a) throws TuplesException, IOException;

  /**
   * Adds a variable to the header.
   * @param var The variable to add.
   */
  @Override
  protected abstract void addHeaderVariable(Variable var) throws IOException;

  /**
   * Adds a single result, based on the current result in the {@link #answer}.
   * @param a The answer to get the result from
   * @throws TuplesException Indicates an error accessing the Answer.
   */
  @Override
  protected abstract void addResult(Answer a) throws TuplesException, IOException;

  /**
   * Adds a single binding from the current result in the {@link #answer}.
   * @param var The bound variable.
   * @param value The value bound to the variable.
   * @throws TuplesException Indicates an error accessing the Answer.
   */
  @Override
  protected abstract void addBinding(Variable var, Object value) throws TuplesException, IOException;

  /**
   * Adds a single URI to the buffer.
   * @param uri The URIReference for the URI to add.
   */
  @Override
  protected abstract void addURI(URIReference uri) throws IOException;

  /**
   * Adds a single blank node to the buffer.
   * @param uri The blank node to add.
   */
  @Override
  protected abstract void addBNode(BlankNode bnode) throws IOException;

  /**
   * Adds a single Literal to the buffer.
   * @param literal The Literal to add.
   */
  @Override
  protected abstract void addLiteral(Literal literal) throws IOException;

}
