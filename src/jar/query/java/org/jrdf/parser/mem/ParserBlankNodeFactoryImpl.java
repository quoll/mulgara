/*
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

package org.jrdf.parser.mem;

import java.util.Map;
import java.util.HashMap;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.parser.ParserBlankNodeFactory;

/**
 * A factory for BlankNodes that uses a Map to keep track of the BlankNodes
 * that have been allocated by {@link #createBlankNode(String)} so that the
 * same BlankNode object can be returned for a given <code>nodeID</code>.
 *
 * @author David Makepeace
 */
public class ParserBlankNodeFactoryImpl implements ParserBlankNodeFactory {

  /**
   * A factory for creating BlankNodes (as well as resources and literals).
   */
  private GraphElementFactory valueFactory;

  /**
   * Mapping from bNode ID's as used in the RDF document to the
   * object created for it by the GraphElementFactory.
   */
  private Map<String,BlankNode> bNodeIdMap = new HashMap<String,BlankNode>();


  /**
   * Create a new blank node factory with the given value factory.
   *
   * @param newValueFactory factory to create nodes with.
   */
  public ParserBlankNodeFactoryImpl(GraphElementFactory newValueFactory) {
    valueFactory = newValueFactory;
  }

  /**
   * Always creates a new BlankNode object from the GraphElementFactory.
   *
   * @return the new BlankNode object.
   * @throws GraphElementFactoryException if it fails to create a new blank node.
   */
  public BlankNode createBlankNode() throws GraphElementFactoryException {
    return valueFactory.createResource();
  }

  /**
   * Returns the BlankNode for a <code>nodeID</code> that has not been seen
   * before or calls the GraphElementFactory to create a new BlankNode
   * otherwise.
   *
   * @param nodeID the node that labels the bNode in the file being parsed.
   * @return the BlankNode object.
   * @throws GraphElementFactoryException if it fails to create a new blank node.
   */
  public BlankNode createBlankNode(String nodeID) throws GraphElementFactoryException {
    // Maybe the node ID has been used before:
    BlankNode result = bNodeIdMap.get(nodeID);

    if (null == result) {
      // This is a new node ID, create a new BNode object for it
      result = valueFactory.createResource();

      // Remember it, the nodeID might occur again.
      bNodeIdMap.put(nodeID, result);
    }

    return result;
  }

  /**
   * Clears the internal Map.
   */
  public void clear() {
    bNodeIdMap.clear();
  }

}
