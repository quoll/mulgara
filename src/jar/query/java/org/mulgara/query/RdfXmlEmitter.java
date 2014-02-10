/*
 * Copyright 2009 Revelytix.
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
package org.mulgara.query;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.mulgara.query.rdf.TripleImpl;
import org.mulgara.util.StringUtil;
import org.openrdf.model.URI;

/**
 * <p>
 * Static utility class for emitting RDF/XML that represents the results of a
 * SPARQL CONSTRUCT or DESCRIBE query.
 * </p>
 * <p>
 * <b>Note:</b> This utility builds up the entire set of statements in-memory to sort
 * them, so is not suitable for very large graphs.
 * </p>
 * <p>
 * This class borrows heavily from the org.mulgara.content.rdfxml.writer.RDFXMLWriter class.
 * </p>
 * 
 * @created Jul 10, 2008
 * @author Alex Hall
 * @copyright &copy; 2008 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class RdfXmlEmitter {
  
  private static final Logger logger = Logger.getLogger(RdfXmlEmitter.class);
  
  /** Comparator for sorting statements in the graph. */
  private static final TripleComparator TRIPLE_COMPARATOR = new TripleComparator();
  
  /** Index used to access the subject column of the GraphAnswer */
  private static final int SUBJECT_INDEX = 0;
  /** Index used to access the predicate column of the GraphAnswer */
  private static final int PREDICATE_INDEX = 1;
  /** Index used to access the object column of the GraphAnswer */
  private static final int OBJECT_INDEX = 2;
  
  /** Prefix to use for the RDF namespace. */
  private static final String RDF_PREFIX = "rdf";
  /** The RDF namespace. */
  private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  
  /** Convenience reference to the new line character(s) */
  private static final String NEWLINE = System.getProperty("line.separator");
  
  /**
   * Compares RDF triples in subject, predicate, object order.  The order for individual
   * nodes is determined as follows:
   * <ol>
   * <li>URI References (ordered based on the natural ordering of the component URI's)</li>
   * <li>Blank nodes (ordered based on the natural ordering of the blank node labels)</li>
   * <li>
   * Literals, ordered as follows:
   *   <ul>
   *   <li>Datatype URI (untyped literals precede typed literals)</li>
   *   <li>Language tag (literals without a language tag precede those with a tag)</li>
   *   <li>Lexical form</li>
   *   </ul>
   * </li>
   * </ol>
   */
  private static class TripleComparator implements Comparator<Triple> {

    public int compare(Triple triple1, Triple triple2) {
      int c = compare(triple1.getSubject(), triple2.getSubject());
      if (c != 0) return c;
      
      c = compare(triple1.getPredicate(), triple2.getPredicate());
      if (c != 0) return c;
      
      return compare(triple1.getObject(), triple2.getObject());
    }
    
    private int compare(Node n1, Node n2) {
      if (n1 instanceof URIReference) {
        return compareUri((URIReference)n1, n2);
      } else if (n1 instanceof BlankNode) {
        return compareBlankNode((BlankNode)n1, n2);
      } else if (n1 instanceof Literal) {
        return compareLiteral((Literal)n1, n2);
      }
      return 0;
    }
    
    private int compareUri(URIReference n1, Node n2) {
      return (n2 instanceof URIReference) ? n1.getURI().compareTo(((URIReference)n2).getURI()) : -1;
    }
    
    private int compareBlankNode(BlankNode n1, Node n2) {
      int c = 0;
      if (n2 instanceof URIReference) {
        c = 1;
      } else if (n2 instanceof BlankNode) {
        c = n1.getID().compareTo(((BlankNode)n2).getID());
      } else {
        c = -1;
      }
      return c;
    }
    
    private int compareLiteral(Literal n1, Node n2) {
      if (n2 instanceof Literal) {
        Literal n2lit = (Literal)n2;
        int c = compareComponent(n1.getDatatypeURI(), n2lit.getDatatypeURI());
        if (c != 0) return c;
        
        c = compareComponent(n1.getLanguage(), n2lit.getLanguage());
        if (c != 0) return c;

        return n1.getLexicalForm().compareTo(n2lit.getLexicalForm());
      } else {
        return 1;
      }
    }
    
    private <T extends Comparable<T>> int compareComponent(T t1, T t2) {
      if (t1 == null) {
        return (t2 == null) ? 0 : -1;
      } else {
        return (t2 == null) ? 1 : t1.compareTo(t2);
      }
    }
    
  }
  
  /**
   * Writes the RDF graph described by the specified GraphAnswer to an output stream.
   * This method consumes and closes the supplied Answer, so calling code should pass in
   * a clone if it needs to access the Answer after this method finishes.  This method will
   * include the XML document headers and is equivalent to calling:
   * <pre>
   * RdfXmlEmitter.writeRdfXml(answer, output, true);
   * </pre>
   * @see #writeRdfXml(GraphAnswer, OutputStream, boolean)
   * @param answer The answer object that contains the RDF graph statements.
   * @param output The output stream to write to.
   * @throws QueryException If there was an error accessing statements from the answer.
   */
  public static void writeRdfXml(GraphAnswer answer, OutputStream output) throws QueryException {
    writeRdfXml(answer, output, true, true);
  }
  
  /**
   * Writes the RDF graph described by the specified GraphAnswer to an output stream.
   * This method consumes and closes the supplied Answer, so calling code should pass in
   * a clone if it needs to access the Answer after this method finishes.
   * @param answer The answer object that contains the RDF graph statements.
   * @param output The output stream to write to.
   * @param includeDocHeader If <code>true</code>, the RDF/XML output will contain an XML
   *        header with entity definitions, and the body will use the entities to abbreviate
   *        RDF resource URI's.
   * @param closeAns Close the Answer when it has been finished with.
   * @throws QueryException If there was an error accessing statements from the answer.
   */
  public static void writeRdfXml(GraphAnswer answer, OutputStream output, boolean includeDocHeader, boolean closeAns) throws QueryException {
    PrintWriter writer = new PrintWriter(new BufferedOutputStream(output));
    
    Map<String,String> nsMap = createInitialNsMap();
    List<Triple> statements = getStatementList(answer, nsMap, closeAns);
    Collections.sort(statements, TRIPLE_COMPARATOR);
    
    if (includeDocHeader) {
      writeDocHeader(nsMap, writer);
    }
    
    writeRdfHeader(nsMap, writer, includeDocHeader);
    writeRdfBody(statements, nsMap, writer, includeDocHeader);
    writeRdfFooter(nsMap, writer);
    
    writer.flush();
  }
  
  /**
   * Extracts a list of RDF statements from the GraphAnswer, consuming and closing the answer.
   * This method also builds up a mapping of namespace URI's to prefixes for all URIReferences
   * that it encounters.
   * @param answer The graph to extract statements from.
   * @param nsMap The mapping of namespace URI to prefix.
   * @return An unsorted list of RDF statements.
   * @throws QueryException if there was an error accessing the statements.
   */
  private static List<Triple> getStatementList(GraphAnswer answer, Map<String,String> nsMap, boolean closeAns) throws QueryException {
    assert answer != null;
    
    List<Triple> statements = new ArrayList<Triple>();
    int nsCounter = 0;
    
    try {
      answer.beforeFirst();
      while (answer.next()) {
        Object subject = answer.getObject(SUBJECT_INDEX);
        if (!(subject instanceof SubjectNode)) {
          throw new QueryException("Illegal value in subject position: " + subject);
        }
        
        Object predicate = answer.getObject(PREDICATE_INDEX);
        if (!(predicate instanceof PredicateNode)) {
          throw new QueryException("Illegal value in predicate position: " + predicate);
        }
        
        Object object = answer.getObject(OBJECT_INDEX);
        if (!(object instanceof ObjectNode)) {
          throw new QueryException("Illegal value in object position: " + object);
        }
        
        nsCounter = addNamespaceToMap(subject, nsMap, nsCounter);
        nsCounter = addNamespaceToMap(predicate, nsMap, nsCounter);
        nsCounter = addNamespaceToMap(object, nsMap, nsCounter);
        
        statements.add(new TripleImpl((SubjectNode)subject, (PredicateNode)predicate, (ObjectNode)object));
      }
    } catch (TuplesException te) {
      throw new QueryException("Error accessing statements from GraphAnswer", te);
    } finally {
      try {
        if (closeAns) answer.close();
      } catch (TuplesException te) {
        logger.warn("Error closing GraphAnswer", te);
      }
    }
    
    return statements;
  }
  
  /**
   * Writes the XML document header to the specified writer.
   * @param nsMap The namespace prefix mappings.
   * @param out The writer.
   */
  private static void writeDocHeader(Map<String,String> nsMap, PrintWriter out) {
    assert out != null;
    String rdf = getRdfPrefix(nsMap);
    
    out.println("<?xml version=\"1.0\"?>");
    
    //print opening DOCTYPE DECLARATION tag
    out.print(NEWLINE + "<!DOCTYPE " + rdf + ":RDF [");
    
    for (Map.Entry<String,String> entry : nsMap.entrySet()) {
      String ns = entry.getKey();
      String prefix = entry.getValue();
      if (ns != null && prefix != null) {
        //write as: <!ENTITY ns 'http://example.org/abc#'>
        out.print(NEWLINE + "  <!ENTITY " + prefix + " '" + ns + "'>");        
      }
    }
    
    //close the opening tag (add a space for readability)
    out.println("]>" + NEWLINE);
  }
  
  /**
   * Writes the opening tag for the root element of the RDF/XML document.  The root element
   * will define XML namespaces for entries in the namespace prefix mapping.
   * @param nsMap The namespace prefix mapping.
   * @param out The writer.
   * @param useEntities Determines whether to abbreviate namespaces using XML entities.
   */
  private static void writeRdfHeader(Map<String,String> nsMap, PrintWriter out, boolean useEntities) {
    //print opening RDF tag (including namespaces)
    out.print("<" + getRdfPrefix(nsMap) + ":RDF");
    
    //print namespaces
    for (Map.Entry<String,String> entry : nsMap.entrySet()) {
      String prefix = entry.getValue();
      String ns = useEntities ? "&" + prefix + ";" : entry.getKey();
      if (prefix != null && ns != null) {
        out.print(NEWLINE + "  xmlns:" + prefix + "=\"" + ns + "\"");
      }
    }
    
    //close the opening tag (add a space for readability)
    out.println(">" + NEWLINE);
  }
  
  /**
   * Writes the closing tag for the root element of the RDF/XML document.
   * @param nsMap The namespace prefix mapping.
   * @param out The writer.
   */
  private static void writeRdfFooter(Map<String,String> nsMap, PrintWriter out) {
    out.println("</" + getRdfPrefix(nsMap) + ":RDF>");
  }
  
  /**
   * Writes the given statements as RDF/XML, using the supplied namespace prefix mappings.
   * @param statements The statements to write.
   * @param nsMap The namespace prefix mappings.
   * @param out The writer.
   * @param useEntities Determines whether to abbreviate URIReferences using XML entities.
   */
  private static void writeRdfBody(List<Triple> statements, Map<String,String> nsMap, PrintWriter out, boolean useEntities) {
    SubjectNode subject = null;
    SubjectNode newSubject = null;
    
    for (Triple statement : statements) {
      newSubject = statement.getSubject();
      assert newSubject != null;
      
      if (!newSubject.equals(subject)) {
        if (subject != null) {
          writeClosingSubjectTag(nsMap, out);
        }

        subject = newSubject;
        writeOpeningSubjectTag(subject, nsMap, out, useEntities);
      }
      
      ObjectNode obj = statement.getObject();
      if (obj instanceof URIReference) {
        writeUriStatement(statement, nsMap, out, useEntities);
      } else if (obj instanceof BlankNode) {
        writeBlankNodeStatement(statement, nsMap, out);
      } else if (obj instanceof Literal) {
        writeLiteralStatement(statement, nsMap, out, useEntities);
      }
    }
    
    if (subject != null) {
      writeClosingSubjectTag(nsMap, out);
    }
  }
  
  /**
   * Writes an opening <code>rdf:Description</code> tag for the given resource.
   * @param subject The subject of an RDF statement.
   * @param nsMap The namespace prefix mappings for the document.
   * @param out The writer.
   * @param useEntities Determines whether to abbreviate URIReferences using XML entities.
   */
  private static void writeOpeningSubjectTag(SubjectNode subject, Map<String,String> nsMap, PrintWriter out, boolean useEntities) {
    String idAttr = null;
    String rdf = getRdfPrefix(nsMap);
    
    if (subject instanceof URIReference) {
      URIReference subjUri = (URIReference)subject;
      idAttr = rdf + ":about=\"" + (useEntities ? abbreviateUriWithEntity(subjUri, nsMap) : subjUri.getURI().toString()) + "\"";
    } else if (subject instanceof BlankNode) {
      idAttr = rdf + ":nodeID=\"" + StringUtil.quoteAV(((BlankNode)subject).getID()) + "\"";
    } else {
      throw new IllegalArgumentException("Unrecognized SubjectNode type: " + subject.getClass());
    }
    
    out.println("  <" + rdf + ":Description " + idAttr + ">");
  }
  
  /**
   * Writes the closing <code>rdf:Description</code> tag for a resource.
   * @param nsMap The namespace prefix mappings for the document.
   * @param out The writer.
   */
  private static void writeClosingSubjectTag(Map<String,String> nsMap, PrintWriter out) {
    out.println("  </" + getRdfPrefix(nsMap) + ":Description>" + NEWLINE);
  }
  
  /**
   * Writes the predicate/object for an RDF statement whose object is a URIReference.
   * Does not write the subject, as it is written in the containing <code>rdf:Description</code> element.
   * @param statement An RDF statement.
   * @param nsMap The namespace prefix mappings for the document.
   * @param out The writer.
   * @param useEntities Determines whether to abbreviate URIReferences using XML entities.
   */
  private static void writeUriStatement(Triple statement, Map<String,String> nsMap, PrintWriter out, boolean useEntities) {
    assert statement.getObject() instanceof URIReference;
    
    URIReference objUri = (URIReference)statement.getObject();
    String resource = useEntities ? abbreviateUriWithEntity(objUri, nsMap) : objUri.getURI().toString();
    String predicate = prefixPredicateUri(statement.getPredicate(), nsMap);
    
    out.println("    <" + predicate + " " + getRdfPrefix(nsMap) + ":resource=\"" + resource + "\"/>");
  }
  
  /**
   * Writes the predicate/object for an RDF statement whose object is a BlankNode.
   * Does not write the subject, as it is written in the containing <code>rdf:Description</code> element.
   * @param statement An RDF statement.
   * @param nsMap The namespace prefix mappings for the document.
   * @param out The writer.
   */
  private static void writeBlankNodeStatement(Triple statement, Map<String,String> nsMap, PrintWriter out) {
    assert statement.getObject() instanceof BlankNode;
    
    String nodeId = StringUtil.quoteAV(((BlankNode)statement.getObject()).getID());
    String predicate = prefixPredicateUri(statement.getPredicate(), nsMap);
    
    out.println("    <" + predicate + " " + getRdfPrefix(nsMap) + ":nodeID=\"" + nodeId + "\"/>");
  }
  
  /**
   * Writes the predicate/object for an RDF statement whose object is a Literal.
   * Does not write the subject, as it is written in the containing <code>rdf:Description</code> element.
   * @param statement An RDF statement.
   * @param nsMap The namespace prefix mappings for the document.
   * @param out The writer.
   * @param useEntities Determines whether to abbreviate URIReferences using XML entities.
   */
  private static void writeLiteralStatement(Triple statement, Map<String,String> nsMap, PrintWriter out, boolean useEntities) {
    assert statement.getObject() instanceof Literal;
    
    Literal objLiteral = (Literal)statement.getObject();
    String predicate = prefixPredicateUri(statement.getPredicate(), nsMap);
    out.print("    <" + predicate);
    
    URI datatype = objLiteral.getDatatype();
    if (datatype != null) {
      out.print(" " + getRdfPrefix(nsMap) + ":datatype=\"" + 
          (useEntities ? abbreviateUriWithEntity(datatype, nsMap) : datatype.toString()) + "\"");
    }
    
    String lang = objLiteral.getLanguage();
    if (lang != null) {
      out.print(" xml:lang=\"" + lang + "\"");
    }
    
    out.println(">" + StringUtil.quoteAV(objLiteral.getLexicalForm()) + "</" + predicate + ">");
  }
  
  /**
   * Gets a string representation of an RDF predicate suitable for use in RDF/XML, substituting
   * a namespace prefix where appropriate.
   * @param predicate The RDF predicate node.
   * @param nsMap The namespace prefix mappings for the document.
   * @return The prefixed predicate URI.
   */
  private static String prefixPredicateUri(PredicateNode predicate, Map<String,String> nsMap) {
    if (!(predicate instanceof URIReference)) {
      throw new IllegalArgumentException("Invalid predicate type: " + predicate.getClass());
    }
    
    URIReference predUri = (URIReference)predicate;
    String value = predUri.getURI().toString();
    assert value != null;
    
    String ns = predUri.getNamespace();
    String prefix = nsMap.get(ns);
    
    // Substitute the namespace prefix
    if (prefix != null) {
      assert value.startsWith(ns);
      value = value.replaceFirst(ns, prefix + ":");
    }
    
    // Handle the RDF container predicates.
    if (value.startsWith(getRdfPrefix(nsMap))) {
      value = value.replaceAll("_[0-9]+", "li");
    }
    
    return value;
  }
  
  /**
   * Abbreviate an RDF resource URI using an XML entity taken from the specified prefix mappings,
   * for use in an <code>rdf:about</code> or <code>rdf:resource</code> RDF/XML attribute.
   * @param uriRef The resource URI.
   * @param nsMap The namespace prefix mappings for the document.
   * @return The abbreviated URI reference.
   */
  private static String abbreviateUriWithEntity(URI uriRef, Map<String,String> nsMap) {
    String value = uriRef.toString();
    assert value != null;
    String ns = uriRef.getNamespace();
    String prefix = nsMap.get(ns);
    return (prefix != null) ? value.replaceFirst(ns, "&" + prefix + ";") : value;
  }
  
  /**
   * If the given node is a URIReference or a literal with a datatype URI, then get the namespace URI 
   * and check if it in the supplied namespace mappings.  If not, allocate a new namespace prefix 
   * and increment the counter.
   * @param node The RDF node to check.
   * @param nsMap The mapping from namespace URI to namespace prefix.
   * @param nsCounter The counter, used to allocate new namespaces.
   * @return The supplied counter value, incremented if a new entry was added to the mapping.
   */
  private static int addNamespaceToMap(Object node, Map<String,String> nsMap, int nsCounter) {
    URI uriToAdd = null;
    if (node instanceof URIReference) {
      uriToAdd = (URIReference)node;
    } else if (node instanceof Literal) {
      uriToAdd = ((Literal)node).getDatatype();
    }
    
    if (uriToAdd != null) {
      String ns = uriToAdd.getNamespace();
      if (!nsMap.containsKey(ns)) {
        String nsPrefix = "ns" + nsCounter++;
        nsMap.put(ns, nsPrefix);
      }
    }
    
    return nsCounter;
  }
  
  /**
   * Gets the RDF namespace prefix from the mappings.  Does basic error checking to verify
   * that the prefix was not reassigned.
   * @param nsMap The prefix mappings.
   * @return The RDF namespace prefix.
   */
  private static String getRdfPrefix(Map<String,String> nsMap) {
    String prefix = nsMap.get(RDF_NAMESPACE);
    assert prefix != null && prefix.equals(RDF_PREFIX) : "RDF prefix was reassigned";
    return prefix;
  }
  
  /**
   * Creates a new mapping of namespace URI to prefix string, pre-populated with mappings for the
   * RDF, RDFS, OWL, and DC namespaces.
   * @return An initial namespace prefix mapping.
   */
  private static Map<String,String> createInitialNsMap() {
    Map<String,String> nsMap = new HashMap<String,String>();
    nsMap.put(RDF_NAMESPACE, RDF_PREFIX);
    nsMap.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
    nsMap.put("http://www.w3.org/2002/07/owl#", "owl");
    nsMap.put("http://purl.org/dc/elements/1.1/", "dc");
    nsMap.put("http://www.w3.org/2001/XMLSchema#", "xsd");
    return nsMap;
  }
}
