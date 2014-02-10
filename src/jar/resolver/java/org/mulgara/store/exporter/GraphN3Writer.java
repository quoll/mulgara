/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.store.exporter;

// Java 2 standard packages
import java.io.*;
import java.net.*;
import java.util.*;

// Apache packages
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.vocabulary.*;

// Local packages
import org.jrdf.graph.GraphException;
import org.jrdf.util.ClosableIterator;
import org.jrdf.graph.*;
import org.mulgara.store.*;

/**
 * A Writer used to write N3 for a Mulgara model.
 *
 * @created 2004-07-26
 *
 * @author <a href="mailto:pag@pisoftware.com">Paul Gearon</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:25 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class GraphN3Writer implements GraphWriter {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(GraphN3Writer.class.
      getName());


  /**
   * Default Constructor
   */
  public GraphN3Writer() {
  }


  /**
   * Writes the contents of the JRDFGraph to a PrintWriter in N3 format.
   *
   * @param graph Graph Graph containing the Statements to be written.
   * @param writer PrintWriter Where to write the statements.
   * @throws GraphException
   */
  synchronized public void write(Graph graph, PrintWriter writer) throws
      GraphException {

    //write Header, Body and Footer
    try {

      //write document
      writeHeader(writer);
      writeBody(graph, writer);
    }
    catch (IOException ioException) {

      throw new GraphException("Could not write Graph.", ioException);
    }
  }


  /**
   * Writes the contents of the JRDFGraph to a PrintWriter in RDF/XML format
   * with the encoding specified in the opening XML tag.
   *
   * @param graph Graph Graph containing the Statements to be written.
   * @param writer PrintWriter
   * @throws GraphException
   */
  synchronized public void write(Graph graph, OutputStreamWriter writer)
      throws GraphException {

    //wrap writer and enable auto flushing
    PrintWriter out = new PrintWriter(writer, true);

    write(graph, out);
  }


  /**
   * Writes any desired comments as a header.
   *
   * @param out the writer to use to output.
   * @throws IOException if there was an IO exception.
   * @throws IllegalArgumentException if the given writer is null.
   */
  private void writeHeader(PrintWriter out) throws IOException {

    //validate
    if (out != null) {
      out.println("# Mulgara graph in N3");
    } else {
      throw new IllegalArgumentException("Cannot write to null Writer.");
    }
  }


  /**
   * Writes the Graph's statements as N3 to the print Writer.
   *
   * @param graph Graph Graph containing the Statements to be written.
   * @param out PrintWriter
   * @throws IOException
   */
  private void writeBody(Graph graph, PrintWriter out) throws IOException,
      GraphException {

    //validate
    if ( (out != null)
        && (graph != null)) {

      // iterator to access entire graph
      ClosableIterator graphIter = graph.find(null, null, null);

      while (graphIter.hasNext()) {
        // extract the triple
        Triple triple = (Triple)graphIter.next();
        SubjectNode s = triple.getSubject();
        PredicateNode p = triple.getPredicate();
        ObjectNode o = triple.getObject();

        out.println("<" + s.toString() + "> <" + ((URIReference)p).getURI().toString() + "> "+ getLabel(o));
      }

    } else {

      //message for exception to be thrown
      String message = "Could not write Graph. Invlaid arguments provided. ";

      if (out == null) {
        message += "Writer is null. ";
      }

      if (graph == null) {
        message += "Graph is null. ";
      }

      throw new IllegalArgumentException(message);
    }
  }


  /**
   * Formats an object node appropriately at either a literal or a resource.
   * For efficiency, a line termination character is appended.
   *
   * @param node The object node to format
   * @throws GraphException
   */
  private String getLabel(ObjectNode node) {
    if (node instanceof Literal) {
      return "\"" + ((Literal)node).getLexicalForm() + "\" .";
    } else if (node instanceof URIReference) {
      return "<" + ((URIReference)node).getURI().toString() + "> .";
    }
    // node is a blank node
    return "<" + node.toString() + "> .";
  }

}
