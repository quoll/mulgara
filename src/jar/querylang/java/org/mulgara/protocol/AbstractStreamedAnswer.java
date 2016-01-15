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
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Converts an answer to a stream, acccording to the protocol of the implementing class.
 *
 * @created Sep 1, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public abstract class AbstractStreamedAnswer {

  /** Logger. */
  private final static Logger logger = Logger.getLogger(AbstractStreamedAnswer.class);

  /** The API {@link Answer}s to convert to the stream. */
  protected final List<Answer> answers;

  /** The number of columns in the current Answer. */
  protected int width;

  /** The {@link Variable}s in the current Answer. */
  protected Variable[] vars;

  /** The writer used for creating the XML. */
  protected OutputStreamWriter s = null;

  /** The byte output stream used for creating the XML. */
  protected OutputStream output = null;

  /** The charset encoding to use when writing to the output stream. */
  Charset charset = Charset.forName("UTF-8");

  /** Adds a literal to the stream */
  protected abstract void addLiteral(Literal literal) throws IOException;

  /** Adds a blank node to the stream */
  protected abstract void addBNode(BlankNode bnode) throws IOException;

  /** Adds a URI reference to the stream */
  protected abstract void addURI(URIReference uri) throws IOException;

  /** Adds a variable binding to the stream */
  protected abstract void addBinding(Variable var, Object value) throws TuplesException, IOException;

  /** Adds a single result to the stream */
  protected abstract void addResult(Answer a) throws TuplesException, IOException;

  /** Adds all results to the stream */
  protected abstract void addResults(Answer a) throws TuplesException, IOException;

  /** Adds a variable in the header to the stream */
  protected abstract void addHeaderVariable(Variable var) throws IOException;

  /** Adds the entire header to the stream */
  protected abstract void addHeader(Answer a) throws IOException;
  
  /** Adds the answer footer to the stream */
  protected abstract void addFooter(Answer a) throws IOException;

  /** Closes the document in the stream */
  protected abstract void addDocFooter() throws IOException;

  /** Oopens the document in the stream */
  protected abstract void addDocHeader() throws IOException;


  /**
   * Creates the object around the answer and output stream.
   * @param answer The answer to encode.
   * @param output The stream to write to.
   */
  public AbstractStreamedAnswer(Answer answer, OutputStream output) {
    this(Collections.singletonList(answer), output);
  }

  /**
   * Creates the object around the answer and output stream.
   * @param answer The answer to encode.
   * @param output The stream to write to.
   */
  public AbstractStreamedAnswer(Answer answer, OutputStream output, String charsetName) {
    this(Collections.singletonList(answer), output, charsetName);
  }

  /**
   * Creates the object around the answer and output stream.
   * @param answer The answer to encode.
   * @param output The stream to write to.
   */
  public AbstractStreamedAnswer(List<Answer> answers, OutputStream output) {
    this.answers = answers;
    this.output = output;
  }

  /**
   * Creates the object around the answer and output stream.
   * @param answer The answer to encode.
   * @param output The stream to write to.
   */
  public AbstractStreamedAnswer(List<Answer> answers, OutputStream output, String charsetName) {
    this(answers, output);
    try {
      charset = Charset.forName(charsetName);
    } catch (Exception e) {
      logger.error("Invalid charset. Using UTF-8: " + charsetName);
    }
  }

  /**
   * @see org.mulgara.protocol.StreamedXMLAnswer#emit()
   */
  public void emit() throws TuplesException, IOException {
    if (s == null) {
      s = new OutputStreamWriter(output, charset);
      generate();
      s.flush();
    }
  }

  /**
   * Generates the XML document in the {@link #s} buffer.
   * @throws TuplesException Indicates an error accessing the Answer.
   */
  void generate() throws TuplesException, IOException {
    addDocHeader();
    for (Answer a: answers) {
      width = (a != null) ? a.getNumberOfVariables() : 0;
      vars = (a != null) ? a.getVariables() : null;
      addHeader(a);
      addResults(a);
      addFooter(a);
    }
    addDocFooter();
  }

  /**
   * Frees resources.
   * @throws IOException
   */
  public void close() throws IOException {
    s.flush();
    output.close();
  }

  /**
   * Not to be called for when doing an "emit". Only used when more manual control is needed
   * due to streaming multiple answers.
   */
  public void initOutput() {
    if (s == null) s = new OutputStreamWriter(output, charset);
  }

}
