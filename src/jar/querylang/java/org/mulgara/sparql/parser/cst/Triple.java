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

import java.util.Collections;
import java.util.List;


/**
 * Represents the standard subject/predicate/object of a triple.
 *
 * @created Feb 18, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class Triple extends BasicGraphPattern {

  /** First element of a triple */
  private Node subject;

  /** Second element of a triple */
  private Node predicate;

  /** Third element of a triple */
  private Node object;

  public Triple(Node s, Node p, Node o) {
    subject = s;
    predicate = p;
    object = o;
  }

  /**
   * @return the subject
   */
  public Node getSubject() {
    return subject;
  }

  /**
   * @return the predicate
   */
  public Node getPredicate() {
    return predicate;
  }

  /**
   * @return the object
   */
  public Node getObject() {
    return object;
  }

  /**
   * @return the modifier on the predicate, if one exists.
   */
  public Modifier getPredicateModifier() {
    if (predicate instanceof Verb) return ((Verb)predicate).getModifier();
    return Modifier.none;
  }

  /**
   * Returns this object as a list.
   * @see org.mulgara.sparql.parser.cst.GroupGraphPattern#getElements()
   */
  @Override
  public List<GroupGraphPattern> getElements() {
    return Collections.singletonList((GroupGraphPattern)this);
  }

  /**
   * Returns this object as a list.
   * @see org.mulgara.sparql.parser.cst.GroupGraphPattern#getAllTriples()
   */
  @Override
  public List<GroupGraphPattern> getAllTriples() {
    return Collections.singletonList((GroupGraphPattern)this);
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    StringBuffer result = new StringBuffer(subject.getImage());
    result.append(" ").append(predicate.getImage()).append(" ").append(object.getImage());
    return addPatterns(result.toString());
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return subject.toString() + " " + predicate.toString() + " " + object.toString();
  }
}
