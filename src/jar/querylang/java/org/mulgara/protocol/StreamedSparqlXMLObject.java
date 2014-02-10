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
import java.nio.charset.Charset;
import java.util.Map;

import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.util.StringUtil;


/**
 * Represents a data object as XML.
 *
 * @created Jul 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StreamedSparqlXMLObject implements StreamedXMLAnswer {

  /** A single indent for use when pretty printing. */
  static final private String INDENT_STR = "  ";

  /** Indicates that pretty printing should be used. */
  boolean prettyPrint = true;

  /** The encoded data. */
  final Object objectData;

  /** The writer used for creating the XML. */
  protected OutputStreamWriter s = null;

  /** The byte output stream used for creating the XML. */
  protected OutputStream output = null;

  /** The charset encoding to use when writing to the output stream. */
  Charset charset = Charset.defaultCharset();

  /**
   * Creates an XML object encoding.
   * @param objectData The data to encode.
   * @param output Where to send the output.
   */
  public StreamedSparqlXMLObject(Object objectData, OutputStream output) {
    this.objectData = objectData;
    this.output = output;
  }

  /** @see org.mulgara.protocol.StreamedXMLAnswer#setCharacterEncoding(java.lang.String) */
  public void setCharacterEncoding(String encoding) {
    charset = Charset.forName(encoding);
  }

  /** @see org.mulgara.protocol.StreamedXMLAnswer#setCharacterEncoding(java.nio.Charset) */
  public void setCharacterEncoding(Charset charset) {
    this.charset = charset;
  }

  /** {@inheritDoc} */
  public void addDocHeader() throws IOException {
    s.append("<?xml version=\"1.0\"?>\n");
    s.append("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">");
    if (prettyPrint) s.append("\n");
  }

  /** {@inheritDoc} */
  public void addDocFooter() throws IOException {
    s.append("</sparql>");
  }

  /** {@inheritDoc} */
  protected void addResults() throws IOException {
    if (prettyPrint) s.append(INDENT_STR);
    s.append("<data>");
    if (objectData != null) {
      if (objectData instanceof Map<?,?>) s.append(encodeMap((Map<?,?>)objectData, INDENT_STR));
      else s.append(StringUtil.quoteAV(objectData.toString()));
    }
    s.append("</data>");
    if (prettyPrint) s.append("\n");
  }


  /**
   * Put the parts of the document together, and close the stream.
   * @see org.mulgara.protocol.StreamedXMLAnswer#emit()
   */
  public void emit() throws IOException {
    s = new OutputStreamWriter(output, charset);
    addDocHeader();
    addResults();
    addDocFooter();
    s.flush();
  }


  /**
   * @see org.mulgara.protocol.XMLAnswer#addNamespace(java.lang.String, java.net.URI)
   * Ignored.
   */
  public void addNamespace(String name, URI nsValue) {
  }

  /**
   * @see org.mulgara.protocol.XMLAnswer#addNamespace(java.lang.String, java.net.URI)
   * Ignored.
   */
  public void clearNamespaces() {
  }

  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  public String encodeMap(Map<?,?> map, String indent) {
    String i2 = prettyPrint ? "\n" + indent + INDENT_STR : "";
    StringBuilder s = new StringBuilder();
    for (Map.Entry<?,?> entry: map.entrySet()) {
      s.append(i2).append("<key>");
      s.append(StringUtil.quoteAV(entry.getKey().toString()));
      s.append("</key>");
      s.append(i2).append("<value>");
      s.append(StringUtil.quoteAV(entry.getValue().toString()));
      s.append("</value>");
    }
    if (prettyPrint) s.append("\n").append(indent);
    return s.toString();
  }

  public void addAnswer(Answer data) throws TuplesException, IOException {
    throw new UnsupportedOperationException();
  }

  public void close() throws IOException {
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
