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

import org.mulgara.krule.rlog.rdf.URIReference;

import java.io.PrintStream;
import java.util.Collection;

/**
 * Writes variables to an XML stream as a set of declarations.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class ReferenceWriter extends XMLFragmentWriter {

  /** The collection of variables that this class emits. */
  public Collection<URIReference> refs;

  /**
   * Creates a new writer for a collection of references.
   * @param refs The references to be written.
   */
  public ReferenceWriter(Collection<URIReference> refs) {
    this.refs = refs;
  }

  /** {@inheritDoc} */
  public void emit(PrintStream out) {
    for (URIReference r: refs) emitRef(out, r);
  }

  private void emitRef(PrintStream out, URIReference r) {
    out.println("  <krule:URIReference rdf:about=\"" + r.getRdfLabel() + "\">\n" +
        "    <rdf:value rdf:resource=\"" + r.getURI() + "\"/>\n" +
        "  </krule:URIReference>\n");
  }
}
