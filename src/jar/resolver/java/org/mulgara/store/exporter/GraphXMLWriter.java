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
import org.apache.xerces.util.EncodingMap;

// JRDF
import org.jrdf.graph.GraphException;
import org.jrdf.util.ClosableIterator;
import org.jrdf.vocabulary.*;

// Local packages
/*
import org.mulgara.resolver.jrdf.ClosableIteratorImpl;
import org.mulgara.resolver.jrdf.JRDFGraph;
*/
import org.jrdf.graph.*;
import org.mulgara.util.*;

/**
 * A Writer used to write RDF/XML for a Mulgara model.
 *
 * @created 2004-02-20
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
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
public class GraphXMLWriter implements GraphWriter {

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(GraphXMLWriter.class.getName());

  /** Prefix used to abbreviate RDF Namespace */
  private static final String RDF_PREFIX = "rdf";

  /** Prefix used to abbreviate RDFS Namespace */
  private static final String RDFS_PREFIX = "rdfs";

  /** Convenience reference to the new line character */
  private static final String NEWLINE = System.getProperty("line.separator");

  /** Map used to replace commonly used namespaces with prefixes */
  protected Map<String,String> namespaces = null;

  /**
   * Default Constructor
   */
  public GraphXMLWriter() {
  }

  /**
   * Writes the contents of the JRDFGraph to a PrintWriter in RDF/XML format.
   *
   * @param graph JRDFGraph Graph containing the Statements to be written.
   * @param writer PrintWriter Where to write the statements.
   * @throws GraphException
   */
  synchronized public void write(Graph graph, PrintWriter writer) throws
      GraphException {

    /*
    if (! (graph instanceof JRDFGraph)) {
      throw new IllegalArgumentException("Graph must be of type JRDFGraph");
    }
    */

    //write Header, Body and Footer
    try {

      //initialize the namespaces first
      this.populateNamespaces(graph);

      //write document
      this.writeHeader(writer);
      this.writeBody(graph, writer);
      this.writeFooter(writer);
    }
    catch (IOException ioException) {

      throw new GraphException("Could not write Graph.", ioException);
    }
  }

  /**
   * Writes the contents of the JRDF Graph to a PrintWriter in RDF/XML format
   * with the encoding specified in the opening XML tag.
   *
   * @param graph JRDFGraph
   * @param writer PrintWriter
   * @throws GraphException
   */
  synchronized public void write(Graph graph, OutputStreamWriter writer) throws
      GraphException {

    /*
    if (! (graph instanceof JRDFGraph)) {
      throw new IllegalArgumentException("Graph must be of type JRDFGraph");
    }
    */

    //write Header, Body and Footer
    try {

      //wrap writer and enable auto flushing
      PrintWriter out = new PrintWriter(writer, true);

      //initialize the namespaces first
      this.populateNamespaces(graph);

      //write document
      this.writeHeader(writer);
      this.writeBody(graph, out);
      this.writeFooter(out);
    }
    catch (IOException ioException) {

      throw new GraphException("Could not write Graph.", ioException);
    }
  }

  /**
   * Writes the XML Declaration and the opening RDF tag to the print Writer.
   * Encoding not specified.
   *
   * @param out PrintWriter
   * @throws IOException
   */
  protected void writeHeader(PrintWriter out) throws IOException {

    //validate
    if (out != null) {

      //print opening tags
      out.println("<?xml version=\"1.0\"?>");

      //print the opening RDF tag (including namespaces)
      this.writeRDFHeader(out);
    }
    else {

      throw new IllegalArgumentException("Cannot write to null Writer.");
    }
  }

  /**
   * Writes the XML Declaration and the opening RDF tag to the print Writer.
   * Encoding attribute is specified as the encoding argument.
   *
   * @param out PrintWriter
   * @throws IOException
   */
  protected void writeHeader(OutputStreamWriter out) throws IOException {

    //validate
    if (out != null) {

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
      this.writeXMLEntities(writer);

      //print the opening RDF tag (including namespaces)
      this.writeRDFHeader(writer);
    }
    else {

      throw new IllegalArgumentException("Cannot write to null Writer.");
    }
  }

  /**
   * Writes the XML Entities (used for namespaces) to the print Writer.
   *
   * @param out PrintWriter
   * @throws IOException
   */
  protected void writeXMLEntities(PrintWriter out) throws IOException {

    //validate
    if (out != null) {

      //print opening DOCTYPE DECLARATION tag
      out.print(NEWLINE + "<!DOCTYPE rdf:RDF [");

      //print namespaces
      Set<String> keys = namespaces.keySet();

      if (keys != null) {

        for(String currentKey: keys) {
          String currentValue = namespaces.get(currentKey);

          if ((currentKey != null) && (currentValue != null)) {

            //write as: <!ENTITY ns 'http://example.org/abc#'>
            out.print(NEWLINE + "  <!ENTITY " + currentKey + " '" + currentValue + "'>");
          }
        }
      }

      //close the opening tag (add a space for readability)
      out.print("]>" + NEWLINE + NEWLINE);
    } else {

      throw new IllegalArgumentException("Cannot write to null Writer.");
    }
  }

  /**
   * Writes the opening RDF tag (with namespaces) to the print Writer.
   *
   * @param out PrintWriter
   * @throws IOException
   */
  protected void writeRDFHeader(PrintWriter out) throws IOException {

    //validate
    if (out != null) {

      //print opening RDF tag (including namespaces)
      out.print("<rdf:RDF ");

      //print namespaces
      Set<String> keys = namespaces.keySet();

      if (keys != null) {

        for(String currentKey: keys) {
          String currentValue = namespaces.get(currentKey);

          if ((currentKey != null) && (currentValue != null)) {

           //use entities: xmlns:ns="&ns;"
            out.print(NEWLINE + "  xmlns:" + currentKey + "=\"&" + currentKey + ";\"");
          }
        }
      }

      //close the opening tag (add a space for readability)
      out.print(">" + NEWLINE + NEWLINE);
    } else {

      throw new IllegalArgumentException("Cannot write to null Writer.");
    }
  }

  /**
   * Writes the Graph's statements as RDF/XML to the print Writer.
   *
   * @param out PrintWriter
   * @throws IOException
   */
  protected void writeBody(Graph graph, PrintWriter out) throws IOException, GraphException {

    //validate
    if ((out != null) && (graph != null)) {

      //iterator used to access subjects
      ClosableIterator<Triple> subjectIter = graph.find(null, null, null);

      //write every (unique) subject
      if (subjectIter != null) {

        Set<SubjectNode> writtenSubjectSet = new HashSet<SubjectNode>();

        while (subjectIter.hasNext()) {

          //get the next triple
          Triple triple = subjectIter.next();

          if (triple != null) {

            SubjectNode subject = ((Triple)triple).getSubject();

            if (!writtenSubjectSet.contains(subject)) {
              this.writeSubject(graph, subject, out);
              writtenSubjectSet.add(subject);
            }
          }
        }

        //close the Iterator
        subjectIter.close();
      }
    } else {

      //message for exception to be thrown
      String message = "Could not write Graph. Invlaid arguments provided. ";

      if (out == null) message += "Writer is null. ";
      if (graph == null) message += "Graph is null. ";

      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Writes the closing RDF tag to the writer.
   *
   * @param out PrintWriter
   * @throws IOException
   */
  protected void writeFooter(PrintWriter out) throws IOException {

    //validate
    if (out != null) {
      //print closing RDF tag
      out.println("</" + RDF_PREFIX + ":RDF>");
    } else {
      throw new IllegalArgumentException("Cannot write to null Writer.");
    }
  }

  /**
   * Writes a Subject to the writer
   *
   * @param graph JRDFGraph
   * @param subject SubjectNode
   * @param writer PrintWriter
   * @throws GraphException
   */
  protected void writeSubject(Graph graph, SubjectNode subject,
                              PrintWriter writer) throws GraphException {

    //opening subject tag (includes literals)
    this.writeOpeningTag(graph, subject, writer);

    //subject contents
    this.writeSubjectBody(graph, subject, writer);

    //closing subject tag
    this.writeClosingTag(graph, subject, writer);

    //put a space between Subjects
    writer.println("");
  }

  /**
   * Writes the opening tag for the subject
   *
   * @param graph JRDFGraph
   * @param subject SubjectNode
   * @param writer PrintWriter
   * @throws GraphException
   */
  protected void writeOpeningTag(Graph graph, SubjectNode subject,
                                 PrintWriter writer) throws
      GraphException {

    if (writer != null) {

      //write the subject type and rdf:about
      if (subject != null) {

        //open tag - BlankNodes are a special case
        if (subject instanceof BlankNode) {

          this.writeOpeningTag(graph, (BlankNode) subject, writer);
        } else {

          writer.print("  <" + RDF_PREFIX + ":Description " + RDF_PREFIX +
                       ":about=\"" + this.getNodeString(subject) + "\">" + NEWLINE);
        }
      } else {
        throw new IllegalArgumentException("Could not write opening tag for subject. Subject Node is null.");
      }
    }
  }

  /**
   * Writes the opening tag for a blank node.
   *
   * @param graph JRDFGraph
   * @param subject BlankNode
   * @param writer PrintWriter
   * @throws GraphException
   */
  protected void writeOpeningTag(Graph graph, BlankNode subject,
                                 PrintWriter writer) throws GraphException {

    ObjectNode subjectType = this.getSubjectType(graph, subject);

    //only display as a subject if it can be found
    if (subjectType != null) {

      //opening tag
      writer.print("  <" + this.getURI(subjectType) + ">" + NEWLINE);
    } else {

      //opening tag
      writer.print("  <" + RDF_PREFIX + ":Description>" + NEWLINE);
    }
  }

  /**
   * Writes the opening tag for the subject
   *
   * @param graph JRDFGraph
   * @param subject SubjectNode
   * @param writer PrintWriter
   * @throws GraphException
   */
  protected void writeClosingTag(Graph graph, SubjectNode subject,
                                 PrintWriter writer) throws
      GraphException {

    //Blank Nodes are written differently
    if (subject instanceof BlankNode) {

      this.writeClosingTag(graph, (BlankNode) subject, writer);
    }
    else {

      //closing tag
      writer.print("  </" + RDF_PREFIX + ":Description>" + NEWLINE);
    }
  }

  /**
   * Writes the opening tag for the subject
   *
   * @param graph JRDFGraph
   * @param subject SubjectNode
   * @param writer PrintWriter
   * @throws GraphException
   */
  protected void writeClosingTag(Graph graph, BlankNode subject,
                                 PrintWriter writer) throws
      GraphException {

    ObjectNode subjectType = this.getSubjectType(graph, subject);

    //only display as a subject if it can be found
    if (subjectType != null) {

      //closing tag
      writer.print("  </" + this.getURI(subjectType) + ">" + NEWLINE);
    } else {

      //closing tag
      writer.print("  </" + RDF_PREFIX + ":Description>" + NEWLINE);
    }
  }

  /**
   * Writes the Resources for a subject (one per line).
   *
   * eg. &lt;predicateURI rdf:resource="resourceURI"/&gt;
   * (&lt;predicateURI rdf:nodeID="resourceURI"/&gt; for Blank Nodes)
   *
   * @param graph Graph
   * @param subject SubjectNode
   * @param writer PrintWriter
   * @throws GraphException
   */
  protected void writeSubjectBody(Graph graph, SubjectNode subject,
                                  PrintWriter writer) throws GraphException {

    //validate graph
    /*
    if (! (graph instanceof JRDFGraph)) {

      throw new IllegalArgumentException("Graph must be of type JRDFGraph");
    }
    */

    //get all statements for the Subject
    ClosableIterator<Triple> tripleIter = graph.find(subject, null, null);

    if (tripleIter != null) {

      //current Triple
      Object triple = null;

      //evaluate all triples
      while (tripleIter.hasNext()) {

        triple = tripleIter.next();

        //validate triple
        if (triple != null) {

          //retrieve Predicate and Object
          PredicateNode predicate = ((Triple)triple).getPredicate();
          ObjectNode object = ((Triple)triple).getObject();

          //Literals and Resources are written differently
          if (object != null) {
            writeStatement(graph, subject, predicate, object, writer);
          }
        }
      }

      //close the Iterator
      tripleIter.close();
    }
  }

  /**
   * Used to write Resources for a Subject. Literals will by-pass this method
   * and use "Literal" method.
   *
   * @param predicate PredicateNode
   * @param object ObjectNode
   * @param writer PrintWriter
   * @throws GraphException
   */
  protected void writeStatement(Graph graph, SubjectNode subject,
                                PredicateNode predicate, ObjectNode object,
                                PrintWriter writer) throws GraphException {

    //Literals are written differently
    if (object instanceof Literal) {

      this.writeStatement(graph, subject, predicate, (Literal) object, writer);
    } else if (object instanceof BlankNode) {

      //write as:  <predicateURI> *blank node as subject* </predicateURI>
      writer.println("    <" + this.getURI(predicate) + ">");

      //write blank node as a "subject"
      this.writeSubject(graph, (BlankNode) object, writer);

      writer.println("    </" + this.getURI(predicate) + ">");
    } else if (subject instanceof BlankNode) {

      //predicatNode representing RDF Type
      PredicateNode rdfTypeNode = null;

      try {

        rdfTypeNode = graph.getElementFactory().createResource(RDF.TYPE);
      } catch (GraphElementFactoryException factoryException) {

        throw new GraphException("Could not create RDF Type node.", factoryException);
      }

      //do not write the RDF Type element
      if (!rdfTypeNode.equals(predicate)) {

        //write as:  <predicateURI rdf:resource="resourceURI"/>
        writer.println("    <" + this.getURI(predicate) + " " + RDF_PREFIX +
                       ":resource=\"" + this.getNodeString(object) + "\"/>");
      }
    } else {

      //write as:  <predicateURI rdf:resource="resourceURI"/>
      writer.println("    <" + this.getURI(predicate) + " " + RDF_PREFIX +
                     ":resource=\"" + this.getNodeString(object) + "\"/>");
    }
  }

  /**
   * Used to write Resources for a Subject. Resources will use "ObjectNode"
   * method.
   *
   * @param predicate PredicateNode
   * @param object Literal
   * @param writer PrintWriter
   * @throws GraphException
   */
  protected void writeStatement(Graph graph, SubjectNode subject,
                                PredicateNode predicate, Literal object,
                                PrintWriter writer) throws GraphException {

    //determine if the Literal has a datatype
    URI datatype = object.getDatatypeURI();

    // Get the lexical form of the literal
    String literalObject = object.getLexicalForm();

    // Create the StringBuffer to hold the resultant string
    StringBuffer buffer = new StringBuffer();

    // Escape the XML string
    StringUtil.quoteAV(literalObject, buffer);

    if (datatype != null) {

      //write as:  <predicateURI rdf:datatype="datatype">"Literal value"
      //           </predicateURI>
      writer.println("    <" + this.getURI(predicate) + " " + RDF_PREFIX +
                     ":datatype=\"" + datatype + "\">" +
                     buffer.toString() +
                     "</" + this.getURI(predicate) + ">");
    } else {

      //write as:  <predicateURI>"Literal value"</predicateURI>
      writer.println("    <" + this.getURI(predicate) + ">" +
                     buffer.toString() + "</" +
                     this.getURI(predicate) + ">");
    }
  }

  /**
   * Finds the RDF Type for a given subject.
   *
   * @param graph Graph
   * @param subject SubjectNode
   * @throws GraphException
   * @return ObjectNode
   */
  protected ObjectNode getSubjectType(Graph graph, SubjectNode subject) throws GraphException {

    //value to be returned
    ObjectNode type = null;

    //validate graph
    if ((graph == null)) {
      throw new IllegalArgumentException("Graph argument must not be null.");
    }

    //predicatNode representing RDF Type
    PredicateNode rdfType = null;

    try {
      rdfType = graph.getElementFactory().createResource(RDF.TYPE);
    } catch (GraphElementFactoryException factoryException) {

      throw new GraphException("Could not create RDF Type node.", factoryException);
    }

    //get the Subject's RDF type
    ClosableIterator<Triple> typeIter = graph.find(subject, rdfType, null);

    if (typeIter != null) {

      //validate "first" triple and extract it's object (rdf type)
      if (typeIter.hasNext()) {

        Triple typeTriple = typeIter.next();

        if (typeTriple != null) {
          type = ((Triple)typeTriple).getObject();
        } else {
          throw new GraphException("Could not find RDF type for Subject: " +
                                   subject + " . Invalid Triple returned.");
        }
      }

      //close the Iterator
      typeIter.close();
    }

    return type;
  }

  /**
   * Returns a URI that represents the Node.
   *
   * @param node the node representing the URI.
   * @throws GraphException
   * @return URI
   */
  protected String getURI(Node node) throws GraphException {

    //value to be returned
    String uri = null;

    //determine type of subject node and create uri from it
    if (node != null) {

      try {

        if (node instanceof URIReference) {
          uri = ( (URIReference) node).getURI().toString();
        } else if (node instanceof BlankNode) {
          uri = new URI("#" + ((BlankNode)node).toString()).toString();
        } else {
          uri = node.toString();
        }
      } catch (URISyntaxException uriException) {
        throw new GraphException("Could not get URI for Node: " + node + ".", uriException);
      }
    } else {
      throw new GraphException("Could not get URI for Node: " + node + ". Node is null.");
    }

    //return the URI with any namespaces replaced with prefixes
    return this.replaceNamespace(uri);
  }

  /**
   * Returns a String representation of an ObjectNode. Object values do not have
   * to be escaped.
   *
   * @param node Node
   * @throws GraphException
   * @return String
   */
  protected String getNodeString(Node node) throws GraphException {

    //value to be returned
    String object = null;

    //determine type of subject node and create uri from it
    if (node != null) {

      try {

        if (node instanceof URIReference) {
          object = ((URIReference)node).getURI().toString();
        } else if (node instanceof BlankNode) {
          object = new URI("#" + ((BlankNode) node).toString()).toString();
        } else if (node instanceof Literal) {
          object = ((Literal)node).getLexicalForm();
        } else {
          object = node.toString();
        }
      } catch (URISyntaxException uriException) {
        throw new GraphException("Could not get String for ObjectNode: " + node + ".", uriException);
      }
    } else {
      throw new GraphException("Could not get String for ObjectNode: " + node + ". ObjectNode is null.");
    }

    return object;
  }

  /**
   * If the URI contains the URI of a known namespace, it is replaced.
   *
   * @param original original URI.
   * @return new URI with namespace references.
   */
  protected String replaceNamespace(String original) throws GraphException {

    //value to be returned
    String uri = original;

    //validate URI (only replace uri's with fragments)
    if (original != null) {

      //replace any URI occurances with namespace prefixes
      for (String currentKey: namespaces.keySet()) {

        String currentValue = namespaces.get(currentKey);

        //validate the Objects
        if ((currentKey != null) && (currentValue != null)) {

          //if the entire namespace is used, replace it with an entity
          if (original.equals(currentValue)) {
            uri = "&" + currentKey + ";";
          } else if (original.startsWith(currentValue.toString())) {
            //replace with namespace
            uri = original.replaceAll(currentValue.toString(), currentKey + ":");
          }
        }
      }
    }

    //return the URI with any collection/container items renamed
    return replaceCollection(uri);
  }

  /**
   * If the URI has a fragment representing a collection (eg. Bag) item, it is
   * replaced with li.
   *
   * @param original original URI.
   * @return new URI with any necessary li.
   */
  protected String replaceCollection(String original) throws GraphException {

    //value to be returned
    String uri = original;

    //validate URI
    if (original != null) {
      uri = original.replaceAll("_[0-9]+", "li");
    }

    return uri;
  }

  /**
   * Populates the namespaces map with default namespaces and namespaces used
   * by the graph.
   *
   * @param graph Graph
   * @throws GraphException
   */
  protected void populateNamespaces(Graph graph) throws GraphException {

    //default namespaces
    namespaces = new HashMap<String,String>();
    namespaces.put(RDF_PREFIX, RDF.BASE_URI.toString());
    namespaces.put(RDFS_PREFIX, RDFS.BASE_URI.toString());
    namespaces.put("owl", "http://www.w3.org/2002/07/owl#");
    namespaces.put("dc", "http://purl.org/dc/elements/1.1/");

    //validate graph before reading
    if (graph == null) {
      throw new IllegalArgumentException("Graph argument is null.");
    }

    //get all statements
    ClosableIterator<Triple> tripleIter = graph.find(null, null, null);

    if (tripleIter != null) {

      while (tripleIter.hasNext()) {

        //get the next triple
        Triple triple = tripleIter.next();

        if (triple != null) {

          //evaluate subject
          SubjectNode subject = triple.getSubject();
          if (subject instanceof URIReference) {
            addNamespaceURI(((URIReference)subject).getURI());
          }

          //evaluate predicate (must be URIReference)
          PredicateNode predicate = triple.getPredicate();
          addNamespaceURI(((URIReference)predicate).getURI());

          //evaluate object
          ObjectNode object = triple.getObject();
          if (object instanceof URIReference) {
            addNamespaceURI(((URIReference)object).getURI());
          }
        }
      }

      //close the Iterator
      tripleIter.close();
    }
  }

  /*
   * Populates the namespaces map with default namespaces and namespaces used
   * by the graph.
   *
   * @param graph Graph
   * @throws GraphException
   */
/*  protected void populateNamespaces(Graph graph) throws GraphException {

    //validate graph
    if (! (graph instanceof JRDFGraph)) {

      throw new IllegalArgumentException("Graph must be of type JRDFGraph");
    }

    //default namespaces
    namespaces = new HashMap();
    namespaces.put(RDF_PREFIX, RDF.baseURI);
    namespaces.put(RDFS_PREFIX, RDFS.baseURI);
    namespaces.put("owl", "http://www.w3.org/2002/07/owl#");
    namespaces.put("dc", "http://purl.org/dc/elements/1.1/");

    //validate graph before reading
    if (graph == null) {

      throw new IllegalArgumentException("Graph argument is null.");
    }

    //iterator used to access predicates
    ClosableIterator predicateIter = ( (JRDFGraph) graph).getUniquePredicates();

    if (predicateIter != null) {

      //current Triple/Predicate
      Object triple = null;
      PredicateNode predicate = null;
      String predicateURI = null;

      while (predicateIter.hasNext()) {

        //get the next triple
        triple = predicateIter.next();

        if ( (triple != null)
            && (triple instanceof Triple)) {

          //get the predicate and evaluate
          predicate = ( (Triple) triple).getPredicate();
          predicateURI = this.getURI(predicate);

          //add the namespace
          try {

            this.addNamespaceURI(new URI(predicateURI));
          }
          catch (URISyntaxException uriException) {

            throw new GraphException("Could not add Namespace: " + predicateURI,
                                     uriException);
          }
        }
      }

      //close the Iterator
      predicateIter.close();
    }
  } */

  /**
   * Evaluates a URI and adds it to the namespace map as a namespace.
   *
   * @param uri URI
   */
  protected void addNamespaceURI(URI uri) {

    if (uri == null) {
      throw new IllegalArgumentException("URI argument is null.");
    }

    //extract URI without fragment
    String uriString = uri.toString();
    String newURI = null;

    if (uriString != null) {

      //determine what comes last a '#' or '/'
      int hashindex = uriString.lastIndexOf('#');
      int slashindex = uriString.lastIndexOf('/');

      //validate (URI must contain a forward slash)
      if (slashindex == -1) {
        //namespace may have been evaluated already
        return;
      }

      //is there a '/' after the '#'?
      if (slashindex > hashindex) {

        //remove everything after the last '/'
        int index = uriString.lastIndexOf('/');
        newURI = uriString.substring(0, index) + "/";
      } else {

        //'#' comes after last '/' (remove entire fragment)
        newURI = uriString.replaceAll(uri.getFragment(), "");
      }

      //only add namespace if it is new
      if ((newURI != null) && (!namespaces.containsValue(newURI))) {
        //add to namespaces
        namespaces.put("ns" + namespaces.size(), newURI);
      }
    }
  }
}
