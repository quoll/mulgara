/*
 * Copyright 2008 The Topaz Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
 */

package org.mulgara.content.n3;

import java.io.IOException;
import java.io.Writer;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;

import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.query.TuplesException;

/**
 * A helper that generates N3 from Statements and writes to a {@link Writer}. Currently only
 * generates NTriples.
 *
 * @created 2009-02-15
 * @author Ronald Tschalär
 * @licence Apache License v2.0
 */
public class N3Writer {
  /**
   * Write out the given statements to the given writer in NTriples format.
   *
   * @param statements the statements to write out
   * @param session    the session to use for globalizing
   * @param writer     where to write the results
   */
  public void write(Statements statements, ResolverSession session, Writer writer)
      throws IOException {
    //validate
    if (statements == null) throw new IllegalArgumentException("Statements cannot be null.");
    if (session == null) throw new IllegalArgumentException("ResolverSession cannot be null.");
    if (writer == null) throw new IllegalArgumentException("Writer cannot be null.");

    // write
    try {
      statements.beforeFirst();
      while (statements.next()) {
        writer.write(toN3String(session.globalize(statements.getSubject())));
        writer.write(" ");
        writer.write(toN3String(session.globalize(statements.getPredicate())));
        writer.write(" ");
        writer.write(toN3String(session.globalize(statements.getObject())));
        writer.write(" .\n");
      }
    } catch (TuplesException te) {
      throw (IOException) new IOException("Error reading statements").initCause(te);
    } catch (GlobalizeException ge) {
      throw (IOException) new IOException("Error globalizing node").initCause(ge);
    }
  }

  private String toN3String(Node node) {
    if (node instanceof URIReference) {
      return "<" + ((URIReference)node).getURI().toASCIIString() + ">";
    } else if (node instanceof Literal) {
      return ((Literal)node).getEscapedForm();
    } else if (node instanceof BlankNode) {
      return "_:" + ((BlankNode)node).getID();
    } else {
      throw new RuntimeException("Unknown node type found: " + node.getClass() + ": " + node);
    }
  }
}
