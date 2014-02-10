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
package org.mulgara.query.filter;

import java.util.Collections;
import java.util.Set;
import org.jrdf.graph.Node;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.XSD;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;


/**
 * Filters the iteration of a Constraint.
 * TODO: This is more properly called an Expression, since they can be used in non-filter roles.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public interface Filter extends ContextOwner, RDFTerm {

  /**
   * Tests a binding according to the filter.
   * @param context The context to resolve the filter in.
   * @return <code>true</code> when the filter is matched.
   * @throws QueryException The filter found an error during testing.
   */
  public boolean test(Context context) throws QueryException;

  /** A filter that does no filtering. */
  public static final Filter NULL = new Filter() {
    private static final long serialVersionUID = -1561779107566375359L;
    public boolean test(Context context) { return true; }
    public void setCurrentContext(Context context) { }
    public Context getCurrentContext() { return null; }
    // RDFTerm methods
    public boolean equals(RDFTerm v) throws QueryException { return v == this; }
    public ContextOwner getContextOwner() { return null; }
    public Object getValue() throws QueryException { return true; }
    public Node getJRDFValue() throws QueryException { return new LiteralImpl("true", XSD.BOOLEAN_URI); }
    public boolean isBlank() throws QueryException { return false; }
    public boolean isIRI() throws QueryException { return false; }
    public boolean isLiteral() throws QueryException { return true; }
    public boolean isURI() throws QueryException { return false; }
    public boolean isGrounded() throws QueryException { return true; }
    public boolean sameTerm(RDFTerm v) throws QueryException { return equals(v); }
    public void setContextOwner(ContextOwner owner) { }
    public void addContextListener(ContextOwner l) { }
    public Set<Variable> getVariables() { return Collections.emptySet(); }
  };

}
