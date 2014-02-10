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
 * A query syntax node, which has a series of triples related to it.
 * The related triples should be incorporated into a conjunction with anything
 * that uses the initial node.
 * An example is an RDF blank node with a property list attached.
 *
 * @created Feb 26, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class AnnotatedNode implements Node {
  
  /** The node of interest */
  private Node subject;
  
  /** A list of triples annotating the subject */
  private TripleList annotation;

  /**
   * Create an annotated subject node.
   * @param subject The subject of interest.
   * @param triples A list of triples annotating that node.
   */
  public AnnotatedNode(Node subject, TripleList triples) {
    this.subject = subject;
    annotation = triples;
    // could assert that subject appears somewhere in triples
  }

  /**
   * Create an annotated subject node from a collection.
   * @param list The RDF collection.
   */
  public AnnotatedNode(GraphList list) {
    TripleList tList = list.asTripleList();
    subject = tList.getSubject();
    annotation = tList;
  }

  /**
   * Create an annotated subject node from a set of properties.
   * @param subject The subject for every triple in the list.
   * @param properties The properties and values for the subject.
   */
  public AnnotatedNode(Node subject, PropertyList properties) {
    this.subject = subject;
    annotation = new TripleList();
    for (PropertyList.Property p: properties) {
      annotation.add(new Triple(subject, p.getPredicate(), p.getObject()));
    }
  }

  /**
   * @return The subject of interest.
   */
  public Node getSubject() {
    return subject;
  }

  /**
   * @return The list of triples annotating the subject node.
   */
  public TripleList getAnnotation() {
    return annotation;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    return annotation.getImage();
  }

}
