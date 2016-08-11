/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The JRDF Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        the JRDF Project (http://jrdf.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The JRDF Project" and "JRDF" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, please contact
 *    newmana@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "JRDF"
 *    nor may "JRDF" appear in their names without prior written
 *    permission of the JRDF Project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the JRDF Project.  For more
 * information on JRDF, please see <http://jrdf.sourceforge.net/>.
 */

package org.jrdf.graph.mem;

import org.jrdf.graph.*;
import org.mulgara.util.UIDGenerator;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A SkipListNode Factory is a class which create the various components of a graph.
 * It is tied to a specific instance of GraphImpl.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 */
public class GraphElementFactoryImpl implements GraphElementFactory {

  /** The pool of all nodes, mapped from their ids. */
  private Map<Long,MemNode> nodePool;

  /** A reverse mapping of all ids, mapped from their string. */
  private Map<String,Long> stringPool;

  /** The graph that this factory constructs nodes for. */
  @SuppressWarnings("unused")
  private Graph graph;

  /** The next available node id. */
  private long nextNode;

  /**
   * Package scope constructor.
   *
   * @param newGraph The GraphImpl that this class is attached to.
   */
  GraphElementFactoryImpl(Graph newGraph) throws TripleFactoryException {
    graph = newGraph;
    nodePool = new HashMap<Long,MemNode>();
    stringPool = new HashMap<String,Long>();
    nextNode = 1;
  }


  /**
   * Create a blank nodes that is associated with a specific graph.
   * @return A new blank node within the graph.
   * @return the newly created blank node value.
   * @throws GraphElementFactoryException If anonymous resources can't be generated.
   */
  public BlankNode createResource() throws GraphElementFactoryException {
    Long id = new Long(nextNode);

    //get an Unique Identifier
    String uid = "";
    try {
      uid = UIDGenerator.generateUID();
    } catch (Exception exception) {
      throw new GraphElementFactoryException(
          "Could not generate Unique Identifier for BlankNode.", exception);
    }

    // create the new node
    BlankNodeImpl node = new BlankNodeImpl(id, uid);

    // put the node in the pool
    nodePool.put(id, node);

    // go on to the next node id
    nextNode++;
    return node;
  }


  /**
   * Create a URI reference.
   * @param uri The URI of the resource.
   * @return the newly created URI reference value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  public URIReference createResource(URI uri)
      throws GraphElementFactoryException {
    if (null == uri) {
      throw new GraphElementFactoryException("URI may not be null for a URIReference");
    }

    // check if the node already exists in the string pool
    Long nodeid = getNodeIdByString(uri.toString());

    if (null != nodeid) {
      return (URIReference)getNodeById(nodeid);
    }

    // create the node identifier and increment the node
    nodeid = new Long(nextNode++);

    // create the new node
    URIReferenceImpl node = new URIReferenceImpl(uri, nodeid);

    // put the node in the pool
    nodePool.put(nodeid, node);

    // put the URI string into the pool
    // TODO: This could conflict with a literal
    stringPool.put(uri.toString(), nodeid);
    return node;
  }


  /**
   * Create a URI reference without checking if the URI given is a valid RDF
   * URI, currently if the URI is absolute.
   * @param uri The URI of the resource.
   * @param validate true if we disbale checking to see if the URI is valid.
   * @return The newly created URI reference value.
   * @throws GraphElementFactoryException
   */
  public URIReference createResource(URI uri, boolean validate) throws GraphElementFactoryException {

    if (null == uri) {
      throw new GraphElementFactoryException("URI may not be null for a URIReference");
    }

    // check if the node already exists in the string pool
    Long nodeid = getNodeIdByString(uri.toString());
    if (null != nodeid) {
      return (URIReference)getNodeById(nodeid);
    }

    // create the node identifier and increment the node
    nodeid = new Long(nextNode++);

    // create the new node
    URIReferenceImpl node = new URIReferenceImpl(uri, validate, nodeid);

    // put the node in the pool
    nodePool.put(nodeid, node);

    // put the URI string into the pool
    // TODO: This could conflict with a literal
    stringPool.put(uri.toString(), nodeid);
    return node;
  }


  /**
   * Creates a new literal with the given lexical value, with no language or datatype.
   * @param lexicalValue The lexical value for the literal.
   * @return the newly created literal value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  public Literal createLiteral(String lexicalValue) throws GraphElementFactoryException {
    LiteralImpl literal = new LiteralImpl(lexicalValue);
    addNodeId(literal);
    return literal;
  }


  /**
   * Creates a new literal with the given lexical value, with a given language
   * but no datatype.
   * @param lexicalValue The lexical value for the literal.  Cannot be null.
   * @param languageType The language of the literal or null if not required.
   * @return the newly created literal value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  public Literal createLiteral(String lexicalValue, String languageType) throws GraphElementFactoryException {
    LiteralImpl newLiteral = new LiteralImpl(lexicalValue, languageType);
    addNodeId(newLiteral);
    return newLiteral;
  }


  /**
   * Creates a new literal with the given lexical value and given datatype.
   * @param lexicalValue The lexical value for the literal.  Cannot be null.
   * @param datatypeURI The URI of the datatype of the literal or null if not required.
   * @return the newly created literal value.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  public Literal createLiteral(String lexicalValue, URI datatypeURI) throws GraphElementFactoryException {
    // create the node identifier
    LiteralImpl newLiteral = new LiteralImpl(lexicalValue, datatypeURI);
    addNodeId(newLiteral);
    return newLiteral;
  }


  /**
   * Creates a new node id for the given Literal.  Sets the node id of the given newLiteral.
   * @param newLiteral A newly created newLiteral.
   */
  private void addNodeId(LiteralImpl newLiteral) {

    // find the string identifier for this node
    String strId = newLiteral.getEscapedForm();

    // check if the node already exists in the string pool
    Long tmpNodeId = stringPool.get(strId);

    if (null != tmpNodeId) {
      // return the existing node instead
      newLiteral.setId(tmpNodeId);
      return;
    } else {

      // create the node identifier
      Long nodeId = new Long(nextNode);
      newLiteral.setId(nodeId);

      // put the node in the pool
      nodePool.put(nodeId, newLiteral);

      // put the URI string into the pool
      stringPool.put(strId, nodeId);

      // increment the node, since we used it
      nextNode++;
    }
  }


  /**
   * Creates a new triple to be used in the graph.  Does not add it to the
   * graph.  Use @see Graph#add.
   * @param subject The subject of the statement.
   * @param predicate The predicate of the statement.
   * @param object The object of the statement.
   * @return the newly created triple object.
   * @throws GraphElementFactoryException If the resource failed to be created.
   */
  public Triple createTriple(SubjectNode subject, PredicateNode predicate,
      ObjectNode object) throws GraphElementFactoryException {
    return new TripleImpl(subject, predicate, object);
  }


  /**
   * Package method for adding in a node that was not created by this factory.
   * Used by GraphImpl for deserializing.
   * @param node The node to add.
   * @throws IllegalArgumentException The node conflicts with one already in use.
   */
  void registerNode(MemNode node) {
    // get the id for this node
    Long id = node.getId();

    // look the node up to see if it already exists in the graph
    MemNode existingNode = nodePool.get(id);
    if (null != existingNode) {
      // check that the node is equal to the one that is already in the graph
      if (existingNode.equals(node)) return;
      // node does not match
      throw new IllegalArgumentException("SkipListNode conflicts with one already in the graph");
    }
    // add the node
    nodePool.put(id, node);

    // check if the node has a string representation
    if (!(node instanceof BlankNode)) {

      if (node instanceof Literal) {
        stringPool.put(((Literal) node).getEscapedForm(), node.getId());
      } else {
        stringPool.put(node.toString(), node.getId());
      }
    }

    // update the nextNode counter to a unique number
    if (id.longValue() >= nextNode) nextNode = id.longValue() + 1;
  }


  /**
   * Package scope method to get all the nodes in the node pool.  Used by GraphImpl for serializing.
   * @return The node pool.
   */
  java.util.Collection<MemNode> getNodePool() {
    return nodePool.values();
  }


  /**
   * Package method to find a node in the node pool by its id.
   *
   * @param id The id of the node to search for.
   * @return The node referred to by the id, null if not found.
   */
  Node getNodeById(Long id) {
    return nodePool.get(id);
  }


  /**
   * Package method to find a node id based on its string representation.
   *
   * @param str The string representation of a node.
   * @return The id of the node with the given string.
   */
  Long getNodeIdByString(String str) {
    return stringPool.get(str);
  }

}
