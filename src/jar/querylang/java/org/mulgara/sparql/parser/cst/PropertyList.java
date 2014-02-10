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

import java.util.List;
import java.util.LinkedList;

/**
 * Represents a list of properties for a subject.
 *
 * @created Feb 26, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class PropertyList extends LinkedList<PropertyList.Property> implements Node {

  /** Serialization ID */
  private static final long serialVersionUID = -83780094455353423L;
  
  /**
   * Adds a new Property to the list.  This creates a new entry for each property/object pair.
   * @param property The name of the property
   * @param objects a list of objects for the property
   */
  public void add(Node property, List<Node> objects) {
    for (Node o: objects) add(new Property(property, o));
  }

  /**
   * Retrieves the internal list.
   * @return The property list
   */
  public List<Property> getElements() {
    return this;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    StringBuffer sb = new StringBuffer(" ");
    for (Property p: this) sb.append(p.getPredicate()).append(" ").append(p.getObject()).append(" ; ");
    return sb.toString();
  }

  public class Property {
    /** The name of the property */
    private Node predicate;
    
    /** The value of the property */
    private Node object;
    
    /**
     * Creates a new predicate/object pair
     * @param predicate The name of the property
     * @param object The value of the property
     */
    public Property(Node predicate, Node object) {
      this.predicate = predicate;
      this.object = object;
    }
    
    /**
     * @return The predicate of this pair
     */
    public Node getPredicate() {
      return predicate;
    }
    
    /**
     * @return The value of this pair
     */
    public Node getObject() {
      return object;
    }
  }
}
