/*
 * Copyright 2008 Fedora Commons
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.sparql.parser.cst;

import java.util.LinkedList;
import java.util.List;

import org.mulgara.sparql.parser.VarNameAllocator;

import static org.mulgara.sparql.parser.cst.IRIReference.*;

/**
 * Represents an RDF "Collection" list.
 *
 * @created Feb 15, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class GraphList implements Node {

  /** The list of nodes that is being represented */
  List<Node> nodes = new LinkedList<Node>();
  
  /** The variable name allocator for generating blank nodes */
  VarNameAllocator bNodeAllocator;

  public GraphList(VarNameAllocator allocator) {
    bNodeAllocator = allocator;    
  }

  /**
   * Appends a new node to the end of this list.
   * @param n The node to append
   */
  public void add(Node n) {
    nodes.add(n);
  }

  /**
   * Return a Collections list of triples
   * @return a list of Triple
   */
  public TripleList asTripleList() {
    List<Triple> result = new LinkedList<Triple>();
    Node lastNode = null;
    for (Node n: nodes) {
      Node currentNode = bNodeAllocator.allocate();
      if (lastNode != null) result.add(new Triple(lastNode, RDF_REST, currentNode));
      result.add(new Triple(currentNode, RDF_FIRST, n));
      lastNode = currentNode;
    }
    if (lastNode != null) result.add(new Triple(lastNode, RDF_REST, RDF_NIL));
    return new TripleList(result);
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    StringBuffer result = new StringBuffer("( ");
    boolean first = true;
    for (Node n: nodes) {
      if (!first) result.append(", "); else first = false;
      result.append(n.getImage());
    }
    result.append(" )");
    return result.toString();
  }

}
