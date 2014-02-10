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


/**
 * Object for representing RDF blank nodes.
 * This will be emitted as a variable for use in queries.
 *
 * @created February 11, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class BlankNode implements Node {

  private String label;

  /**
   * Creates a blank node with a given label
   * @param s The string containing the label
   */
  public BlankNode(String s) {
    label = s;
  }

  /**
   * Get the label for this node.
   * @return The label for this node.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Get a printable representation of this class.
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    return label;
  }

  /**
   * Check if this BlankNode is equivalent to another.
   * @param o The other BlankNode
   * @return <code>true</code> iff o is a BlankNode, and has the same label.
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    return (o instanceof BlankNode && ((BlankNode)o).label.equals(label));
  }

  /** @see java.lang.Object#hashCode() */
  public int hashCode() {
    return label.hashCode();
  }

}
