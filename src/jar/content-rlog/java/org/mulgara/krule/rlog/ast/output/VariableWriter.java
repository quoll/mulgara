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

import org.mulgara.krule.rlog.rdf.Var;

import java.io.PrintStream;
import java.util.Collection;

/**
 * Writes variables to an XML stream as a set of declarations.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class VariableWriter extends XMLFragmentWriter {

  /** The collection of variables that this class emits. */
  public Collection<Var> vars;

  /**
   * Creates a new writer for a collection of variables.
   * @param vars The variables to be written.
   */
  public VariableWriter(Collection<Var> vars) {
    this.vars = vars;
  }

  /** {@inheritDoc} */
  public void emit(PrintStream out) {
    for (Var v: vars) emitVar(out, v);
  }

  /**
   * Prints a variable declaration to the stream
   * @param out The print stream to print to.
   * @param v The variable to emit.
   */
  private void emitVar(PrintStream out, Var v) {
    out.println("  " + nodeString(v) + "\n");
  }
}
