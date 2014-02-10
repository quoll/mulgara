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

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.*;
import java.util.*;

// Log4J
import org.apache.log4j.*;

// JRDF
import org.jrdf.graph.*;

// Jena
import com.hp.hpl.jena.graph.impl.*;

// Local packages
import org.mulgara.server.*;

/**
 * Converts Jena objects to JRDF objects.
 *
 * @created 2004-02-17
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/01/27 11:50:56 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class JRDFFactoryImpl implements org.mulgara.server.JRDFFactory {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger logger =
      Logger.getLogger(JRDFFactory.class.getName());

  /**
   * An in memory hash map of anonymous node keys to node objects.
   */
  private Map<String,SubjectNode> anonMap = new HashMap<String,SubjectNode>();

  /**
   * Constructs a JRDF Factory that is not tied to a Jena Factory.
   */
  public JRDFFactoryImpl() {
  }

  /**
   * Converts a Jena Graph node to JRDF subject node.
   *
   * @param databaseGraph the JRDF database graph to create objects from.
   * @param node the Jena Graph node to convert.
   * @throws GraphElementFactoryExcpetion if it failed to create the resource
   *   or blank node.
   * @throws URISyntaxException if the URI in the node was incorrect.
   * @return the JRDF subject node.
   */
  public SubjectNode convertNodeToSubject(Graph databaseGraph,
      com.hp.hpl.jena.graph.Node node) throws GraphElementFactoryException,
      URISyntaxException {

    SubjectNode subjectNode = null;

    if (node == null || node.equals(com.hp.hpl.jena.graph.Node.ANY)) {
      subjectNode = null;
    }
    else if (node.isBlank()) {
      subjectNode = getNode(databaseGraph, node);
    }
    else if (node.isURI()) {

      // Node is a URI.
      subjectNode = databaseGraph.getElementFactory().createResource(
          new URI(node.getURI()));
    }
    else {
      throw new GraphElementFactoryException("Illegal Node type: " +
          node.getClass());
    }

    return subjectNode;
  }

  /**
   * Converts a Jena Graph node to JRDF predicate node.
   *
   * @param databaseGraph the JRDF database graph to create objects from.
   * @param node the Jena Graph node to convert.
   * @throws GraphElementFactoryException if it failed to create the resource.
   * @throws URISyntaxException if the URI in the node was incorrect.
   * @return the JRDF subject node.
   */
  public PredicateNode convertNodeToPredicate(
      Graph databaseGraph, com.hp.hpl.jena.graph.Node node)
      throws GraphElementFactoryException, URISyntaxException {

    PredicateNode predicateNode = null;

    if (node == null || node.equals(com.hp.hpl.jena.graph.Node.ANY)) {

      predicateNode = null;
    }
    // Node is a URI.
    else if (node.isURI()) {

      predicateNode = databaseGraph.getElementFactory().createResource(
          new URI(node.getURI()));
    }
    else {
      throw new GraphElementFactoryException("Illegal Node type: " + node);
    }

    return predicateNode;
  }

  /**
   * Converts a Jena Graph node to JRDF object node.
   *
   * @param databaseGraph the JRDF database graph to create objects from.
   * @param node the Jena Graph node to convert.
   * @throws GraphElementFactoryException if it failed to create the resource,
   *   literal or blank node.
   * @throws URISyntaxException if the URI in the node was incorrect.
   * @return the JRDF object node.
   */
  public ObjectNode convertNodeToObject(Graph databaseGraph,
      com.hp.hpl.jena.graph.Node node) throws GraphElementFactoryException,
      URISyntaxException {
    ObjectNode objectNode = null;

    if (node == null || node.equals(com.hp.hpl.jena.graph.Node.ANY)) {

    }
    else if (node.isBlank()) {
      objectNode = getNode(databaseGraph, node);
    }
    else if (node.isLiteral()) {
      LiteralLabel label = node.getLiteral();

      // Ensure that we put either null or the URI for the datatype.
      URI dataTypeURI;
      if (label.getDatatypeURI() == null) {
        dataTypeURI = null;
      }
      else {
        dataTypeURI = new URI(label.getDatatypeURI());
      }

      // Determine if language, datatype or neither are specified
      String language = label.language();

      // cannot have both a language and datatype
      if (((language != null) && !language.equals("")) &&
          ((dataTypeURI != null) && !dataTypeURI.toString().equals(""))) {
        throw new GraphElementFactoryException("Node cannot have both " +
            "Datatype and Language specified.  Node: " + node + ", " +
            "language: [" + language + "], datatype: [" + dataTypeURI + "]");
      }

      //Instantiate label depending on it's properties
      if (language != null) {

        //create Literal with language
        objectNode = databaseGraph.getElementFactory().createLiteral(
            label.getLexicalForm(), language);
      }
      else if (dataTypeURI != null) {

        //create Literal with datatype
        objectNode = databaseGraph.getElementFactory().createLiteral(
            label.getLexicalForm(), dataTypeURI);
      }
      else {

        //create regular Literal
        objectNode = databaseGraph.getElementFactory().createLiteral(
          label.getLexicalForm());
      }
    }
    else if (node.isURI()) {

      // Node is a URI.
      objectNode = databaseGraph.getElementFactory().createResource(
          new URI(node.getURI()));
    }
    else {

      throw new GraphElementFactoryException("Illegal Node type");
    }

    return objectNode;
  }

  /**
   * Returns true if the given key has been mapped to a JRDF node.
   *
   * @param key the unique string identifier of an anonymous JRDF node.
   * @return true if the given key has been mapped to a JRDF node.
   */
  public boolean hasNode(com.hp.hpl.jena.graph.Node key) {

    return anonMap.containsKey(key.toString());
  }

  /**
   * This is used by the JenaFactory telling us about a new anonymous resource.
   *
   * @param node the node to map to the subjectNode.
   * @param subjectNode The subjectNode being stored.
   */
  public void addAnon(com.hp.hpl.jena.graph.Node node, SubjectNode subjectNode) {
    anonMap.put(node.toString(), subjectNode);
  }

  /**
   * Add an anonymous JRDF node.
   *
   * @param key the anonymous Jena node.  The string for this node is stored, but
            the node is needed to supply to Jena
   * @param value the JRDF node value to map it to.
   */
  public void addNode(com.hp.hpl.jena.graph.Node key, BlankNode value) {
    anonMap.put(key.toString(), value);
  }


  /**
   * Retrieve an anonymous JRDF node.
   *
   * @param key String the unique string identifier of the anonymous JRDF node.
   * @throws GraphElementFactoryException if it failed to create the resource,
   *   literal or blank node.
   * @return Node that the JRDF node value is mapped to, or null if it is not
   *   yet mapped.
   */
  public BlankNode getNode(com.hp.hpl.jena.graph.Node key)
      throws GraphElementFactoryException {

    if (anonMap.containsKey(key.toString())) {
      return (BlankNode) anonMap.get(key.toString());
    } else {
      return null;
    }
  }

  /**
   * Retrieve an anonymous JRDF node.
   *
   * @param databaseGraph the database graph to use to create a new blank node.
   * @param key String the unique string identifier of the anonymous JRDF node.
   * @throws GraphElementFactoryException if it failed to create the resource,
   *   literal or blank node.
   * @return Node that the JRDF node value is mapped to.  This is a new node
   *   if it didn't previously exist.
   */
  private BlankNode getNode(Graph databaseGraph,
      com.hp.hpl.jena.graph.Node key) throws GraphElementFactoryException {

    BlankNode node = null;
    if (anonMap.containsKey(key.toString())) {
      node = (BlankNode) anonMap.get(key.toString());
    }
    else {
      node = databaseGraph.getElementFactory().createResource();
      addNode(key, node);
    }
    return node;
  }
}
