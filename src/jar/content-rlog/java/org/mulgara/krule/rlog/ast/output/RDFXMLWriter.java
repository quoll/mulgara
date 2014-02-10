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

package org.mulgara.krule.rlog.ast.output;

import java.io.PrintStream;

/**
 * Writes an RDF/XML file to a stream.
 *
 * @created May 3, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="http://www.opensource.org/licenses/apache2.0.php">Apache License, Version 2.0</a>
 *
 */
public abstract class RDFXMLWriter {

  /**
   * Build the required XML headers.
   * @param out The stream to write to.
   */
  protected void writeHeader(PrintStream out) {
    StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append("<!DOCTYPE rdf:RDF [\n");
    sb.append("<!ENTITY owl     \"http://www.w3.org/2002/07/owl#\">\n");
    sb.append("<!ENTITY rdf     \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n");
    sb.append("<!ENTITY rdfs    \"http://www.w3.org/2000/01/rdf-schema#\">\n");
    sb.append("<!ENTITY xsd     \"http://www.w3.org/2001/XMLSchema#\">\n");
    sb.append("<!ENTITY mulgara \"http://mulgara.org/mulgara#\">\n");
    sb.append("<!ENTITY krule   \"http://mulgara.org/owl/krule/#\">\n");
    sb.append("]>\n");
    sb.append("\n");
    sb.append("<rdf:RDF xmlns:rdf=\"&rdf;\"\n");
    sb.append("         xmlns:rdfs=\"&rdfs;\"\n");
    sb.append("         xmlns:owl=\"&owl;\"\n");
    sb.append("         xmlns:xsd=\"&xsd;\"\n");
    sb.append("         xmlns=\"&krule;\"\n");
    sb.append("         xmlns:krule=\"&krule;\"\n");
    sb.append("         xml:base=\"http://mulgara.org/owl/krule/\">\n");
    out.println(sb.toString());
  }

  /**
   * Build the required XML footers.
   * @param out The stream to write to.
   */
  protected void writeFooter(PrintStream out) {
    out.println("</rdf:RDF>");
  }

}
