/*
 * Copyright 2008 Fedora Commons, Inc.
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

package org.mulgara.krule.rlog.ast;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.mulgara.krule.rlog.rdf.RDFNode;
import org.mulgara.krule.rlog.rdf.URIReference;
import org.mulgara.krule.rlog.rdf.Var;

/**
 * This class represents a predicate place holder that is not occupied.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class NullPredicate extends Predicate {

  public static final NullPredicate NULL = new NullPredicate();

  /**
   * Creates a null predicate.
   */
  private NullPredicate() {
    super(null);
  }

  /** {@inheritDoc} */
  public void accept(TreeWalker walker) { }

  /** {@inheritDoc} */
  public boolean containsVariables() {
    return false;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  public Collection<Var> getVariables() {
    return (Collection<Var>)Collections.EMPTY_SET;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  public Set<URIReference> getReferences() {
    return (Set<URIReference>)Collections.EMPTY_SET;
  }

  /**
   * {@inheritDoc}
   * Presumed to always match.
   */
  public boolean matches(RDFNode s, RDFNode p, RDFNode o) {
    return true;
  }

  /** {@inheritDoc} */
  public void print(int indent) {
    System.out.println(sp(indent) + "<null>");
  }

  /** Get the subject of this predicate. */
  public RDFNode getSubject() {
    throw new UnsupportedOperationException();
  }
  
  /** Get the subject of this predicate. */
  public RDFNode getPredicate() {
    throw new UnsupportedOperationException();
  }

  /** Get the subject of this predicate. */
  public RDFNode getObject() {
    throw new UnsupportedOperationException();
  }

  /** @see java.lang.Object#toString() */
  public String toString() {
    return "";
  }

  @Override
  CanonicalPredicate getCanonical() {
    return new CanonicalPredicate();
  }

}

