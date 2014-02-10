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

import org.mulgara.krule.rlog.ast.Axiom;
import org.mulgara.krule.rlog.parser.URIParseException;

import java.io.PrintStream;
import java.util.Collection;

/**
 * Writes variables to an XML stream as a set of declarations.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class AxiomWriter extends XMLFragmentWriter {

  /** The collection of axioms that this class emits. */
  public Collection<Axiom> axioms;

  /**
   * Creates a new writer for a collection of axioms.
   * @param axioms The axioms to be written.
   */
  public AxiomWriter(Collection<Axiom> axioms) {
    this.axioms = axioms;
  }

  /**
   * {@inheritDoc} 
   * @throws URIParseException Constructing URIs for the output resulted in an invalid URI.
   */
  public void emit(PrintStream out) throws URIParseException {
    for (Axiom a: axioms) emitAxiom(out, a);
  }

  /**
   * Write the the RDF/XML representation of an axiom to the print stream.
   * @param out The print stream to send the axiom to.
   * @param a The axiom to emit.
   * @throws URIParseException If any references in the axiom are invalid URIs.
   */
  private void emitAxiom(PrintStream out, Axiom a) throws URIParseException {
    StringBuilder sb = new StringBuilder("  <krule:Axiom>\n");
    sb.append("    <subject>\n");
    sb.append("      ").append(nodeString(a.getSubject())).append("\n");
    sb.append("    </subject>\n");
    sb.append("    <predicate>\n");
    sb.append("      ").append(nodeString(a.getPredicate())).append("\n");
    sb.append("    </predicate>\n");
    sb.append("    <object>\n");
    sb.append("      ").append(nodeString(a.getObject())).append("\n");
    sb.append("    </object>\n");
    sb.append("  </krule:Axiom>\n\n");
    out.print(sb.toString());
  }

}
