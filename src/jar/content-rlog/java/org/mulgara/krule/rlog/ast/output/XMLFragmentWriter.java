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

import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.Literal;
import org.mulgara.krule.rlog.rdf.RDFNode;
import org.mulgara.krule.rlog.rdf.URIReference;
import org.mulgara.krule.rlog.rdf.Var;

/**
 * Outputs an element of the AST into XML.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public abstract class XMLFragmentWriter {

  /**
   * Outputs the data for this object into a stream.
   * @param out The stream to write to.
   * @throws URIParseException Constructing URIs for the output resulted in an invalid URI.
   */
  public abstract void emit(PrintStream out) throws URIParseException;

  /**
   * Create a string representation of the RDF/XML needed for a node.
   * @param n The node to write.
   * @return An XML fragment string representing the node.
   */
  protected String nodeString(RDFNode n) {
    StringBuilder sb;
    if (n.isReference()) {
      URIReference ref = (URIReference)n;
      sb = new StringBuilder("<krule:URIReference rdf:about=\"");
      sb.append(ref.getRdfLabel()).append("\"/>");
    } else if (n.isVariable()) {
      Var v = (Var)n;
      sb = new StringBuilder("<krule:Variable rdf:about=\"");
      sb.append(v.getRdfLabel()).append("\"><name>").append(v.getName()).append("</name></krule:Variable>");
    } else {
      sb = new StringBuilder("<krule:Literal><rdf:value rdf:parseType=\"Literal\">");
      sb.append(((Literal)n).getLexical()).append("</rdf:value></krule:Literal>");
    }
    return sb.toString();
  }

}
