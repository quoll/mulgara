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
package org.mulgara.content.rdfxml.writer;

// Java 2 standard packages
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xerces.util.EncodingMap;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.URIReference;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.StatementsWrapperTuples;
import org.mulgara.resolver.spi.TuplesWrapperStatements;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.util.StringUtil;

/**
 * Local space RDFXML writer that generates RDF/XML from Statements and writes
 * to a Writer (supports character encoding).
 *
 * @created 2004-10-21
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:03 $
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
public class RDFXMLWriter {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(RDFXMLWriter.class.getName());

  /** Convenience reference to the new line character */
  private static final String NEWLINE = System.getProperty("line.separator");

  /** Map used to replace namespaces with prefixes */
  protected NamespaceMap namespaces = null;

  /** Key used by the Namespace Map to represent the RDF namespace */
  private String RDF_PREFIX = null;

  /** Variables used to convert Statements to Tuples */
  private Variable[] vars = null;

  /**
   * Default Constructor
   */
  public RDFXMLWriter() {

    vars = new Variable[] {

        StatementStore.VARIABLES[0],
        StatementStore.VARIABLES[1],
        StatementStore.VARIABLES[2],
    };
  }
  
  /**
   * Writes the contents of the JRDF Graph to a Writer in RDF/XML
   * format with the encoding specified in the opening XML tag.
   *
   * @param statements Statements The RDF to be written.
   * @param session ResolverSession Used to globalize nodes
   * @param writer OutputStreamWriter Destination of the RDF/XML (supports
   * character encoding)
   * @throws QueryException
   */
  synchronized public void write(Statements statements, ResolverSession session,
      OutputStreamWriter writer) throws QueryException {
    write(statements, session, writer, null);
  }

  /**
   * Writes the contents of the JRDF Graph to a Writer in RDF/XML
   * format with the encoding specified in the opening XML tag.
   *
   * @param statements Statements The RDF to be written.
   * @param session ResolverSession Used to globalize nodes
   * @param writer OutputStreamWriter Destination of the RDF/XML (supports
   * character encoding)
   * @param initialPrefixes A set of user-supplied namespace prefix mappings.
   * @throws QueryException
   */
  synchronized public void write(Statements statements, ResolverSession session,
      OutputStreamWriter writer, Map<String,URI> initialPrefixes) throws QueryException {

    //validate
    if (statements == null) {

      throw new IllegalArgumentException("Statements cannot be null.");
    }
    if (session == null) {

      throw new IllegalArgumentException("ResolverSession cannot be null.");
    }
    if (writer == null) {

      throw new IllegalArgumentException("OutputStreamWriter cannot be null.");
    }

    //write Header, Body and Footer
    PrintWriter out = null;
    try {

      //wrap writer and enable auto flushing
      out = new PrintWriter(writer, true);

      //ensure statements are writable
      statements = prepareStatements(statements);

      //initialize the namespaces first
      namespaces = new NamespaceMap(statements, session, initialPrefixes);
      RDF_PREFIX = namespaces.getRDFPrefix();

      //write document
      writeHeader(writer);
      writeBody(statements, session, out);
      writeFooter(out);
    } catch (Exception exception) {

      log.error("Failed to write Statements.", exception);

      throw new QueryException("Failed to write Statements.", exception);
    } finally {
      if (out != null) out.close();
    }
  }

  /**
   * Prepares a Set of Statements for writing.
   *
   * @param statements Statements
   * @throws Exception
   * @return Statements
   */
  protected Statements prepareStatements(Statements statements) throws Exception {

    // Statements may be lazily evaluated. Materializing caches them
    Tuples tuples = new StatementsWrapperTuples(statements);
    Tuples materializedTuples = TuplesOperations.materialize(tuples);

    // ensure variables are in the right order
    Tuples projectedTuples = TuplesOperations.project(materializedTuples,
        Arrays.asList(vars), true);
    materializedTuples.close();

    // tuples must be sorted by subject for writing
    Tuples sortedTuples = TuplesOperations.sort(projectedTuples);
    projectedTuples.close();

    return new TuplesWrapperStatements(sortedTuples, StatementStore.VARIABLES[0],
        StatementStore.VARIABLES[1], StatementStore.VARIABLES[2]);
  }

  /**
   * Writes the XML Declaration and the opening RDF tag to the print Writer.
   * Encoding attribute is specified as the encoding argument.
   *
   * @param out OutputStreamWriter
   * @throws IOException
   */
  protected void writeHeader(OutputStreamWriter out) throws IOException {

    assert out != null:"OutputStreamWriter is null";

    //wrapper for output stream writer (enable autoflushing)
    PrintWriter writer = new PrintWriter(out, true);

    //get encoding from the Encoding map
    String encoding = EncodingMap.getJava2IANAMapping(out.getEncoding());

    //only insert encoding if there is a value
    if (encoding != null) {

      //print opening tags <?xml version="1.0" encoding=*encoding*?>
      writer.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
    }
    else {

      //print opening tags <?xml version="1.0"?>
      writer.println("<?xml version=\"1.0\"?>");
    }

    //print the Entities
    writeXMLEntities(writer);

    //print the opening RDF tag (including namespaces)
    writeRDFHeader(writer);
  }

  /**
   * Writes the XML Entities (used for namespaces) to the print Writer.
   *
   * @param out PrintWriter
   * @throws IOException
   */
  protected void writeXMLEntities(PrintWriter out) throws IOException {

    assert out != null:"PrintWriter is null";

    //print opening DOCTYPE DECLARATION tag
    out.print(NEWLINE + "<!DOCTYPE " + RDF_PREFIX + ":RDF [");

    //print namespaces
    Set<String> keys = namespaces.keySet();

    if (keys != null) {

      Iterator<String> keyIter = keys.iterator();
      String currentKey = null;
      Object currentValue = null;

      while (keyIter.hasNext()) {

        currentKey = keyIter.next();
        currentValue = namespaces.get(currentKey);

        if ((currentKey != null) && (currentKey.length() > 0) && (currentValue != null)) {

          //write as: <!ENTITY ns 'http://example.org/abc#'>
          out.print(NEWLINE + "  <!ENTITY " + currentKey + " '" + currentValue + "'>");
        }
      }
    }

    //close the opening tag (add a space for readability)
    out.print("]>" + NEWLINE + NEWLINE);
  }

  /**
   * Writes the opening RDF tag (with namespaces) to the print Writer.
   *
   * @param out PrintWriter
   * @throws IOException
   */
  protected void writeRDFHeader(PrintWriter out) throws IOException {

    assert out != null:"PrintWriter is null";

    //print opening RDF tag (including namespaces)
    out.print("<" + RDF_PREFIX + ":RDF ");

    //print namespaces
    Set<String> keys = namespaces.keySet();

    if (keys != null) {

      Iterator<String> keyIter = keys.iterator();
      String currentKey = null;
      Object currentValue = null;

      while (keyIter.hasNext()) {

        currentKey = keyIter.next();
        currentValue = namespaces.get(currentKey);

        if ((currentKey != null) && (currentValue != null)) {
          // For default namespace, write 'xmlns="[namespace]"'
          // For other namespaces, write 'xmlns:[prefix]="&[prefix];"' (XML entity)
          String xmlnsPart = "xmlns";
          if (currentKey.length() > 0) {
            xmlnsPart += ":" + currentKey;
          }
          out.print(NEWLINE + "  " + xmlnsPart + "=\"" + 
              ((currentKey.length() > 0) ? ("&" + currentKey + ";") : currentValue) + "\"");
        }
      }
    }

    //close the opening tag (add a space for readability)
    out.print(">" + NEWLINE + NEWLINE);
  }

  /**
   * Writes the Graph's statements as RDF/XML to the print Writer.
   *
   * @param statements Statements
   * @param session ResolverSession
   * @param out PrintWriter
   * @throws Exception
   */
  protected void writeBody(Statements statements, ResolverSession session,
      PrintWriter out) throws TuplesException, GlobalizeException, QueryException {

    assert statements != null:"Statements is null";
    assert session != null:"ResolverSession is null";
    assert out != null:"PrinterWriter is null";

    //current subject
    long subject = NodePool.NONE;
    long newSubject = NodePool.NONE;
    SubjectNode subjectNode = null;

    //write the subjects
    statements.beforeFirst();
    while (statements.next()) {

      newSubject = statements.getSubject();

      //has the subject changed?
      if (newSubject != subject) {

        //close last and open new
        if (subject != NodePool.NONE) {
          writeClosingSubjectTag(out);
        }
        subject = newSubject;
        subjectNode = (SubjectNode) session.globalize(subject);
        //validate
        if (subjectNode == null) {
          throw new QueryException("subject is null");
        }
        writeOpeningSubjectTag(subjectNode, out);
      }

      //get other two nodes and validate
      Node predicateNode = session.globalize(statements.getPredicate());
      Node objectNode = session.globalize(statements.getObject());
      if (predicateNode == null) {
        throw new QueryException("predicate is null");
      }
      if (!(predicateNode instanceof URIReference)) {
        throw new QueryException("PredicateNode should be of type: " +
            "URIReference, was: " + predicateNode.getClass().getName());
      }
      if (objectNode == null) {
        throw new QueryException("object is null");
      }
      if (!(objectNode instanceof ObjectNode)) {
        throw new QueryException("ObjectNode should be of type: " +
            "ObjectNode, was: " + objectNode.getClass().getName());
      }

      //write the current statement
      writeStatement(namespaces, out, subjectNode,
          (URIReference) predicateNode, (ObjectNode) objectNode);
    }

    //write the final closing tag
    if (subject != NodePool.NONE) {
      writeClosingSubjectTag(out);
    }
  }

  /**
   * Creates the appropriate statement Object and calls write on it.
   *
   * @param namespaces NamespaceMap
   * @param writer PrintWriter
   * @param subject SubjectNode
   * @param predicate PredicateNode
   * @param object ObjectNode
   * @throws QueryException
   */
  protected void writeStatement(NamespaceMap namespaces, PrintWriter writer,
      SubjectNode subject, URIReference predicate, ObjectNode object) throws QueryException {

    AbstractWritableStatement statement = null;

    //type of statement is determined by the object type
    if (object instanceof URIReference) {

      statement = new URIReferenceWritableStatement(subject, predicate,
          (URIReference) object);
    } else if (object instanceof BlankNode) {
      statement = new BlankNodeWritableStatement(subject, predicate, (BlankNode)object);
    } else if (object instanceof Literal) {
      statement = new LiteralWritableStatement(subject, predicate, (Literal)object);
    } else {
      assert(object != null):"Object should not be null";
      throw new QueryException("Unknown ObjectNode type: " + object.getClass().getName());
    }

    assert statement != null : "WritableStatement should not be null";
    statement.write(namespaces, writer);
  }

  /**
   * Writes an opening Tag for the subject.
   *
   * @param subject Node
   * @param out PrintWriter
   * @throws Exception
   */
  protected void writeOpeningSubjectTag(SubjectNode subject, PrintWriter out) {

    assert out != null : "PrintWriter is null";

    //write
    if (subject instanceof URIReference) {

      //print as: <rdf:Description rdf:about="http://example.org#subject">
      URI uri = ((URIReference) subject).getURI();
      String rdf = namespaces.getRDFPrefix();
      out.print("  <" + rdf + ":Description " + rdf + ":about=\"" +
          uri.toString() + "\">" + NEWLINE);
    } else if (subject instanceof BlankNode) {
      //get an identifier for the BlankNode
      String nodeString = subject.toString();
      nodeString = StringUtil.quoteAV(nodeString);
      String rdf = namespaces.getRDFPrefix();

      //print as: <rdf:Description rdf:nodeID="blankNodeID">
      out.print("  <" + rdf + ":Description " + rdf + ":nodeID=\"" +
          nodeString + "\">" + NEWLINE);
    } else {
      throw new IllegalArgumentException("Unknown SubjectNode type: " + subject.getClass().getName());
    }
  }

  /**
   * Writes a closing Tag for the subject.
   *
   * @param out PrintWriter
   * @throws Exception
   */
  protected void writeClosingSubjectTag(PrintWriter out) {

    assert out != null:"PrintWriter is null";
    //print as: </rdf:Description>
    out.print("  </" + namespaces.getRDFPrefix() + ":Description>" + NEWLINE);
  }

  /**
   * Writes the closing RDF tag to the writer.
   *
   * @param out PrintWriter
   * @throws IOException
   */
  protected void writeFooter(PrintWriter out) throws IOException {

    assert out != null:"PrintWriter is null";
    //print closing RDF tag
    out.println("</" + RDF_PREFIX + ":RDF>");
  }
}
