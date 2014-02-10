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
import java.util.Map;

import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;


/**
 * Represents a data object as JSON.
 *
 * @created Jul 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StreamedSparqlJSONObject implements StreamedAnswer {

  /** The encoded data. */
  final Object objectData;

  /** The writer used for creating the XML. */
  protected OutputStreamWriter s = null;

  /** The byte output stream used for creating the XML. */
  protected OutputStream output = null;

  /**
   * Creates an XML object encoding.
   * @param objectData The data to encode.
   * @param output Where to send the output.
   */
  public StreamedSparqlJSONObject(Object objectData, OutputStream output) {
    this.objectData = objectData;
    this.output = output;
  }

  /**
   * Put the parts of the document together, and close the stream.
   * @see org.mulgara.protocol.StreamedXMLAnswer#emit()
   */
  public void emit() throws IOException {
    s = new OutputStreamWriter(output);
    s.append("{ \"data\": ");
    s.append(jsonEscape(objectData));
    s.append(" }");
    s.flush();
  }

  /**
   * Create a JSON string for a single Map object.
   * @param o The object to convert to a JSON string.
   * @return The JSON string representing the parameter.
   */
  static String jsonHash(Map<?,?> o) {
    StringBuilder s = new StringBuilder("{ ");
    boolean first = true;
    for (Map.Entry<?,?> entry: o.entrySet()) {
      if (!first) s.append(", ");
      else first = false;
      s.append("\"");
      s.append(entry.getKey());
      s.append("\": ");
      s.append(jsonEscape(entry.getValue()));
    }
    s.append(" }");
    return s.toString();
  }

  /** Trivial escaping. */
  static String jsonEscape(Object o) {
    if (o instanceof Number) return o.toString();
    if (o instanceof Map<?,?>) return jsonHash((Map<?,?>)o);
    String data = o.toString();
    data = data.replace("\"", "\\\"");
    data = data.replace("\\", "\\\\");
    data = data.replace("/", "\\/");
    data = data.replace("\b", "\\b");
    data = data.replace("\f", "\\f");
    data = data.replace("\n", "\\n");
    data = data.replace("\r", "\\r");
    data = data.replace("\t", "\\t");
    return "\"" + data + "\"";
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
    s.flush();
    output.close();
  }

  /**
   * Not to be called for when doing an "emit". Only used when more manual control is needed
   * due to streaming multiple answers.
   */
  public void initOutput() {
    if (s == null) s = new OutputStreamWriter(output);
  }
}
