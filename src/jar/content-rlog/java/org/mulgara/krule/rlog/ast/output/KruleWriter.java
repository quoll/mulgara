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

import org.mulgara.krule.rlog.Interpreter;
import org.mulgara.krule.rlog.parser.URIParseException;

/**
 * Writes a set of rules to an RDF/XML file in Krule format.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class KruleWriter extends RDFXMLWriter {

  private Interpreter interpreter;

  /**
   * Construct a writer for writing Krule RDF/XML files.
   * @param interpreter The object used for parsing the RLog text.
   */
  public KruleWriter(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  // inheritdoc
  public void emit(PrintStream out) throws URIParseException {
    // pull in all the elements, and give them to writers
    // XMLFragmentWriter varWriter = new VariableWriter(interpreter.getVariables());
    XMLFragmentWriter refWriter = new ReferenceWriter(interpreter.getReferences());
    XMLFragmentWriter axiomWriter = new AxiomWriter(interpreter.getAxioms());
    XMLFragmentWriter ruleWriter = new RuleWriter(interpreter.getRules());
    
    // send to stdout
    writeHeader(out);
    // varWriter.emit(System.out);
    refWriter.emit(System.out);
    axiomWriter.emit(System.out);
    ruleWriter.emit(System.out);
    writeFooter(out);
  }

}
