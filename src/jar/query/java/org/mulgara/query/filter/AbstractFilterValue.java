/**
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */
package org.mulgara.query.filter;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.ComparableExpression;
import org.mulgara.query.filter.value.IRI;
import org.mulgara.query.filter.value.SimpleLiteral;
import org.mulgara.query.filter.value.ValueLiteral;


/**
 * A root for most values, for setting the context and giving default responses to many items.
 *
 * @created Apr 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractFilterValue extends AbstractContextOwner implements Filter, ValueLiteral {

  /** Serialization ID */
  private static final long serialVersionUID = 7034034367736302522L;

  /** The logger. */
  final static Logger logger = Logger.getLogger(AbstractFilterValue.class.getName());

  /** The operands for the operation */
  protected RDFTerm[] operands;

  /** The owner of the current context */
  private ContextOwner contextOwner = null;

  public AbstractFilterValue(RDFTerm... operands) {
    this.operands = operands;
    for (RDFTerm op: operands) op.setContextOwner(this);
  }

  /**
   * @see org.mulgara.query.filter.value.ValueLiteral#getLang()
   * @throws QueryException if this function does not resolve to a literal
   */
  public SimpleLiteral getLang() throws QueryException { return SimpleLiteral.EMPTY; }

  /** @see org.mulgara.query.filter.value.ValueLiteral#getType() */
  public IRI getType() throws QueryException { return Bool.IRI_TYPE; }

  /** @see org.mulgara.query.filter.value.ValueLiteral#isSimple() */
  public boolean isSimple() throws QueryException { return false; }

  /**
   * @see org.mulgara.query.filter.value.ValueLiteral#getLexical()
   * @throws QueryException if this function does not resolve to a literal
   */
  public String getLexical() throws QueryException { return getValue().toString(); }

  /**
   * @see org.mulgara.query.filter.value.ValueLiteral#getValue()
   * @return an object, for the result.  Never null.
   * @throws QueryException if this function does not resolve to a literal
   */
  public Object getValue() throws QueryException { return resolve().getValue(); }

  /**
   * @see org.mulgara.query.filter.value.ValueLiteral#getValue()
   * @return an object, for the result.  Never null.
   * @throws QueryException if this function does not resolve to a literal
   */
  public Node getJRDFValue() throws QueryException {return resolve().getJRDFValue(); }

  /** @see org.mulgara.query.filter.value.ValueLiteral#test(org.mulgara.query.filter.Context) */
  public boolean test(Context context) throws QueryException {
    setCurrentContext(context);
    RDFTerm term = resolve();
    if (term.isLiteral()) {
      return ((ValueLiteral)term).test(context);
    } else if (term instanceof Filter) {
      return ((Filter)term).test(context);
    }
    throw new QueryException("Type error. Cannot get a boolean from a: " + term.getClass().getSimpleName());
  }

  /** @see org.mulgara.query.filter.RDFTerm#equals(org.mulgara.query.filter.RDFTerm) */
  public boolean equals(RDFTerm v) throws QueryException { return resolve().equals(v); }

  /** @see org.mulgara.query.filter.RDFTerm#isBlank() */
  public boolean isBlank() throws QueryException { return false; }

  /** @see org.mulgara.query.filter.RDFTerm#isIRI() */
  public boolean isIRI() throws QueryException { return false; }

  /** @see org.mulgara.query.filter.RDFTerm#isLiteral() */
  public boolean isLiteral() throws QueryException { return true; }

  /** @see org.mulgara.query.filter.RDFTerm#isURI() */
  public boolean isURI() throws QueryException { return isIRI(); }

  /** {@inheritDoc} */
  public boolean isGrounded() throws QueryException {
    for (RDFTerm o: operands) if (!o.isGrounded()) return false;
    return true;
  }

  /**
   * Return all the variables in this term.
   * @see org.mulgara.query.filter.RDFTerm#getVariables()
   */
  public Set<Variable> getVariables() {
    Set<Variable> result = new HashSet<Variable>();
    for (RDFTerm term: operands) result.addAll(term.getVariables());
    return result;
  }

  /** @see org.mulgara.query.filter.RDFTerm#sameTerm(org.mulgara.query.filter.RDFTerm) */
  public boolean sameTerm(RDFTerm v) throws QueryException { return resolve().sameTerm(v); }

  /////////////////////////////////////////////////////////////////
  // The following all implement the ComparableExpression interface
  /////////////////////////////////////////////////////////////////

  /** @see org.mulgara.query.filter.value.ComparableExpression#greaterThan(org.mulgara.query.filter.value.ComparableExpression) */
  public boolean greaterThan(ComparableExpression v) throws QueryException {
    if (!isLiteral()) throw new QueryException("Type Error: cannot compare a: " + getClass().getSimpleName());
    return ((ValueLiteral)resolve()).greaterThan(v);
  }

  /** @see org.mulgara.query.filter.value.ComparableExpression#greaterThanEqualTo(org.mulgara.query.filter.value.ComparableExpression) */
  public boolean greaterThanEqualTo(ComparableExpression v) throws QueryException {
    if (!isLiteral()) throw new QueryException("Type Error: cannot compare a: " + getClass().getSimpleName());
    return ((ValueLiteral)resolve()).greaterThanEqualTo(v);
  }

  /** @see org.mulgara.query.filter.value.ComparableExpression#lessThan(org.mulgara.query.filter.value.ComparableExpression) */
  public boolean lessThan(ComparableExpression v) throws QueryException {
    if (!isLiteral()) throw new QueryException("Type Error: cannot compare a: " + getClass().getSimpleName());
    return ((ValueLiteral)resolve()).lessThan(v);
  }

  /** @see org.mulgara.query.filter.value.ComparableExpression#lessThanEqualTo(org.mulgara.query.filter.value.ComparableExpression) */
  public boolean lessThanEqualTo(ComparableExpression v) throws QueryException {
    if (!isLiteral()) throw new QueryException("Type Error: cannot compare a: " + getClass().getSimpleName());
    return ((ValueLiteral)resolve()).lessThanEqualTo(v);
  }

  /////////////////////////////////////////////////////////////////
  // End of the ComparableExpression interface
  /////////////////////////////////////////////////////////////////

  /** @see org.mulgara.query.filter.RDFTerm#getContextOwner() */
  public ContextOwner getContextOwner() { return contextOwner; }

  /** @see org.mulgara.query.filter.RDFTerm#setContextOwner(org.mulgara.query.filter.ContextOwner) */
  public void setContextOwner(ContextOwner owner) {
    contextOwner = owner;
    owner.addContextListener(this);
  }

  /** @see org.mulgara.query.filter.ContextOwner#getCurrentContext() */
  public Context getCurrentContext() { return contextOwner.getCurrentContext(); }

  /** @see org.mulgara.query.filter.ContextOwner#setCurrentContext(org.mulgara.query.filter.Context) */
  public void setCurrentContext(Context context) {
    super.setCurrentContext(context);
  }

  /**
   * Resolves this function into whatever the return type should be (Literal or URI).
   * @return The resolved value for the function.
   * @throws QueryException There was an error resolving the value against the context.
   */
  protected abstract RDFTerm resolve() throws QueryException;
}
