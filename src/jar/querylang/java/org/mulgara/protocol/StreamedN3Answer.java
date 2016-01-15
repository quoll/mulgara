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

import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.TypedNodeVisitable;
import org.jrdf.graph.TypedNodeVisitor;
import org.jrdf.graph.URIReference;
import org.mulgara.query.Answer;
import org.mulgara.query.GraphAnswer;
import org.mulgara.query.TuplesException;

/**
 * Represents a ConstructAnswer as N3.
 * This is a very primitive implementation, with no attempt to use [] for blank nodes
 * list structures, or even namespaces.
 * TODO: Add prefixes
 *
 * @created Jan 29, 2009
 * @author Paula Gearon
 * @copyright &copy; 2009 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StreamedN3Answer implements StreamedAnswer {

  /** Logger. */
  private final static Logger logger = Logger.getLogger(StreamedN3Answer.class);

  /** The answer to convert to RDF/XML. */
  private final GraphAnswer ans;

  /** The writer to send the data to. */
  private final PrintWriter p;

  /** The charset encoding to use when writing to the output stream. */
  static final String UTF8 = "UTF-8";

  /**
   * Constructs the object and prepares to writing.
   * @param ans The answer to emit.
   * @param s The stream to write the answer to.
   */
  public StreamedN3Answer(Answer ans, OutputStream s) {
    this(ans, s, UTF8);
  }

  /**
   * Constructs the object and prepares to writing.
   * @param ans The answer to emit.
   * @param s The stream to write the answer to.
   */
  public StreamedN3Answer(Answer ans, OutputStream s, String charsetName) {
    if (!(ans instanceof GraphAnswer)) throw new IllegalArgumentException("N3 constructor can only be constructed from a GraphAnswer");
    this.ans = (GraphAnswer)ans;
    assert ans.getVariables().length == 3;
    Charset charset = null;
    try {
      charset = Charset.forName(charsetName);
    } catch (Exception e) {
      logger.error("Invalid charset. Using UTF-8: " + charsetName);
      charset = Charset.forName(UTF8);
    }
    p = new PrintWriter(new OutputStreamWriter(s, charset));
  }

  /**
   * Converts the Answer to a String and send to output.
   * @throws TuplesException Indicates an error accessing the Answer.
   */
  public void emit() throws TuplesException, IOException {
    NodePrinter np = new NodePrinter();
    ans.beforeFirst();
    try {
      while (ans.next()) {
        ((TypedNodeVisitable)ans.getObject(0)).accept(np);
        ((TypedNodeVisitable)ans.getObject(1)).accept(np);
        ((TypedNodeVisitable)ans.getObject(2)).accept(np);
        p.println(".");
      }
    } catch (ClassCastException e) {
      throw new TuplesException("Data in graph is not a node: " + e.getMessage());
    } finally {
      p.close();
    }
  }


  /**
   * Prints out the nodes as per N3.
   */
  private class NodePrinter implements TypedNodeVisitor {

    /**
     * Prints the blank node and a space to the stream.
     * @param blankNode The blank node to write.
     */
    public void visitBlankNode(BlankNode blankNode) {
      p.print("_:");
      p.print(blankNode.toString().substring(1));
      p.print(" ");
    }

    /**
     * Prints the literal and a space to the stream.
     * @param literal The literal to write.
     */
    public void visitLiteral(Literal literal) {
      p.print(literal.getEscapedForm());
      p.print(" ");
    }

    /**
     * Prints the uri surrounded by <> and a space to the stream.
     * @param uriReference The uri to write.
     */
    public void visitURIReference(URIReference uriReference) {
      p.print("<");
      p.print(uriReference);
      p.print("> ");
    }
    
  }


  public void addAnswer(Answer data) throws TuplesException, IOException {
    throw new UnsupportedOperationException();
  }

  public void addDocFooter() throws IOException {
    throw new UnsupportedOperationException();
  }

  public void addDocHeader() throws IOException {
    throw new UnsupportedOperationException();
  }

  public void close() throws IOException {
    p.close();
  }

  public void initOutput() {
    //  do nothing
  }
}
