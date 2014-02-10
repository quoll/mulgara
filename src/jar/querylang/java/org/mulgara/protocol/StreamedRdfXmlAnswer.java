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

import org.mulgara.query.Answer;
import org.mulgara.query.GraphAnswer;
import org.mulgara.query.QueryException;
import org.mulgara.query.RdfXmlEmitter;
import org.mulgara.query.TuplesException;

/**
 * Represents a ConstructAnswer as RDF/XML.
 * This uses the {@link org.mulgara.query.RdfXmlEmitter} class to do the actual work,
 * hence, it does not manage XML formatting itself. This is why the interfaces do NOT
 * include StreamedXMLAnswer.
 *
 * @created Jan 29, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StreamedRdfXmlAnswer implements StreamedAnswer {

  /** The answer to convert to RDF/XML. */
  private final GraphAnswer ans;

  /** The stream to write to. */
  private final OutputStream out;

  public StreamedRdfXmlAnswer(Answer ans, OutputStream s) {
    if (!(ans instanceof GraphAnswer)) throw new IllegalArgumentException("RDF/XML constructor can only be constructed from a GraphAnswer");
    this.ans = (GraphAnswer)ans;
    out = s;
  }

  /**
   * Converts the Answer to a String and send to output.
   * @throws TuplesException Indicates an error accessing the Answer.
   */
  public void emit() throws TuplesException, IOException {
    try {
      RdfXmlEmitter.writeRdfXml(ans, out, true, false);
    } catch (QueryException e) {
      Throwable cause = e.getCause();
      if (cause instanceof TuplesException) throw (TuplesException)cause;
      throw new TuplesException(e.getMessage());
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
    out.close();
  }

  public void initOutput() {
    // do nothing
  }

}
