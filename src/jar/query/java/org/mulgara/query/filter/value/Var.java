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
package org.mulgara.query.filter.value;

import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.AbstractContextOwner;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.ContextOwner;
import org.mulgara.query.filter.RDFTerm;


/**
 * Marks the use of a variable.
 * This class needs a reference to the calling resolved Tuples to extract the variable
 * bindings for the current row.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Var extends AbstractContextOwner implements ComparableExpression, ValueLiteral, NumericExpression {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 7024038517344320135L;

  /** Logger */
  private static final Logger logger = Logger.getLogger(Var.class.getName());

  /** The name of this variable */
  private final String name;

  /** An invalid index to indicate that a variable is not initialized */
  private static final int NOT_INITIALIZED = -2;

  /** The index of this variable in the context */
  private int varIndex = NOT_INITIALIZED;

  /** The owner of the current context */
  private ContextOwner contextOwner = null;

  /**
   * Instantiate a variable.
   *
   * @param name The variable's name
   */
  public Var(String name) {
    this.name = name;
  }

  /** @see org.mulgara.query.filter.RDFTerm#getVariables() */
  public Set<Variable> getVariables() {
    return Collections.singleton(new Variable(name));
  }

  /**
   * Tests if this variable is bound.
   * @return <code>true</code> if the variable is unknown, or known and not bound.
   */
  public boolean isBound() throws QueryException {
    // this will set varIndex if it is not set yet
    Context context = getLocalContext();
    // now that varIndex is set, we can use it in the context
    return context.isBound(varIndex);
  }

  /** {@inheritDoc} */
  public Object getValue() throws QueryException {
    return resolve().getValue();
  }

  /** {@inheritDoc} */
  public Node getJRDFValue() throws QueryException {
    return resolve().getJRDFValue();
  }

  /** {@inheritDoc} */
  public Number getNumber() throws QueryException {
    Object result = getValue();
    if (result instanceof Number) return (Number)result;
    throw new QueryException("Variable '" + name + "' did not resolve to a number. Got: " + result);
  }

  /**
   * {@inheritDoc}
   * Uses {@link #resolveComparable()} for throwing the QueryException if this variable
   * does not resolve to a literal, and hence does not have a lexical form.
   */
  public String getLexical() throws QueryException {
    ComparableExpression e = resolveComparable();
    if (e.isLiteral()) return ((ValueLiteral)e).getLexical();
    // fall back to getting a string out of a complex expression
    return e.getValue().toString();
  }

  /**@see org.mulgara.query.filter.value.ValueLiteral#getType() */
  public IRI getType() throws QueryException {
    ComparableExpression e = resolveComparable();
    if (!e.isLiteral()) throw new QueryException("Only literals are typed");
    return ((ValueLiteral)e).getType();
  }

  /** @see org.mulgara.query.filter.value.ValueLiteral#isSimple() */
  public boolean isSimple() throws QueryException {
    return ((ValueLiteral)resolve()).isSimple();
  }

  /** {@inheritDoc} */
  public boolean isGrounded() throws QueryException { return false; }

  /** {@inheritDoc} */
  public boolean equals(RDFTerm v) throws QueryException {
    return resolve().equals(v);
  }

  /** {@inheritDoc} */
  public boolean greaterThan(ComparableExpression v) throws QueryException {
    return resolveComparable().greaterThan(v);
  }

  /** {@inheritDoc} */
  public boolean greaterThanEqualTo(ComparableExpression v) throws QueryException {
    return resolveComparable().greaterThanEqualTo(v);
  }

  /** {@inheritDoc} */
  public boolean lessThan(ComparableExpression v) throws QueryException {
    return resolveComparable().lessThan(v);
  }

  /** {@inheritDoc} */
  public boolean lessThanEqualTo(ComparableExpression v) throws QueryException {
    return resolveComparable().lessThanEqualTo(v);
  }

  /**
   * Resolve this variable in its current context
   * @return An expression value wrapping the data bound to this point
   * @throws QueryException Indicates an error getting data out of the context, or globalizing.
   */
  public ComparableExpression resolveComparable() throws QueryException {
    RDFTerm v = resolve();
    if (!(v instanceof ComparableExpression)) throw new QueryException("Type Error: Cannot compare against a: " + v.getClass().getSimpleName());
    return (ComparableExpression)v;
  }

  /** {@inheritDoc} */
  public boolean isBlank() throws QueryException {
    return resolve().isBlank();
  }

  /** {@inheritDoc} */
  public boolean isIRI() throws QueryException {
    return resolve().isIRI();
  }

  /**
   * {@inheritDoc}
   * The operation of this method is depended on the context in which it was called.
   * If it is called without a context owner, then this means it was called during
   * Filter construction, and we want to indicate that it is valid to treat this as a literal.
   * @return <code>true</code> if there is no context, or else it calls isLiteral on the resolved value.
   */
  public boolean isLiteral() throws QueryException {
    return contextOwner == null ? true : resolve().isLiteral();
  }

  /** {@inheritDoc} */
  public boolean isURI() throws QueryException {
    return resolve().isURI();
  }

  /** {@inheritDoc} */
  public boolean sameTerm(RDFTerm v) throws QueryException {
    if (Var.class == v.getClass()) return resolveLocal() == ((Var)v).resolveLocal();
    return resolve().sameTerm(v);
  }

  /**
   * {@inheritDoc}
   * @throws QueryException If there was an error resolving the variable, or the variable does not
   *         resolve to a literal.
   */
  public SimpleLiteral getLang() throws QueryException {
    RDFTerm term = resolve();
    if (!term.isLiteral()) throw new QueryException("Cannot get a language tag on a non-literal: " + term.getClass().getSimpleName());
    return ((ValueLiteral)term).getLang();
  }

  public boolean test(Context context) throws QueryException {
    setCurrentContext(context);
    RDFTerm term = resolve();
    if (!term.isLiteral()) throw new QueryException("Cannot get an effective boolean value for a non-literal: " + term.getClass().getSimpleName());
    return ((ComparableExpression)term).test(context);
  }

  /** @see org.mulgara.query.filter.RDFTerm#getContextOwner() */
  public ContextOwner getContextOwner() {
    return contextOwner;
  }

  /** @see org.mulgara.query.filter.RDFTerm#setContextOwner(org.mulgara.query.filter.ContextOwner) */
  public void setContextOwner(ContextOwner owner) {
    this.contextOwner = owner;
    owner.addContextListener(this);
  }

  /**
   * Resolve this variable in its current context
   * @return An expression value wrapping the data bound to this point
   * @throws QueryException Indicates an error getting data out of the context, or globalizing.
   */
  public RDFTerm resolve() throws QueryException {
    long gNode = resolveLocal();

    Node node;
    try {
      node = getContextOwner().getCurrentContext().globalize(gNode);
    } catch (QueryException qe) {
      throw new QueryException("Unable to globalize variable " + name + " from id <" + gNode + ">", qe.getCause());
    }

    return convertToExpr(node);
  }

  /**
   * Resolve this variable to the internal gNode.
   * @return A gNode that this variable resolves to.
   * @throws QueryException Indicates an error getting data out of the context.
   */
  public long resolveLocal() throws QueryException {
    Context localContext = getLocalContext();
    if (varIndex == Context.NOT_BOUND) throw new QueryException("Resolving unbound variable: " + name);
    long result = localContext.getColumnValue(varIndex);
    if (result == localContext.getUnboundVal()) throw new QueryException("Resolving unbound variable: " + name);
    return result;
  }

  /**
   * Converts a node to an appropriate ExpressionValue
   * @param node The node to convert
   * @return A new ExpressionValue
   */
  private RDFTerm convertToExpr(Node node) throws QueryException {
    if (node instanceof BlankNode) return new BlankNodeValue((BlankNode)node);
    
    if (node instanceof URIReference) return new IRI(((URIReference)node).getURI());
    
    if (!(node instanceof Literal)) throw new QueryException("Unknown type for: " + node);

    Literal l = (Literal)node;
    return TypedLiteral.newLiteral(l.getLexicalForm(), l.getDatatypeURI(), l.getLanguage());
  }

  /**
   * Return the current context, using a test context over that provided by the context owner if possible
   * Updates the value of varIndex.
   * @return The current context to use.
   */
  private Context getLocalContext() {
    Context localContext = getCurrentContext();
    if (localContext == null) {
      localContext = getContextOwner().getCurrentContext();
    } else {
      if (localContext != getContextOwner().getCurrentContext()) logger.debug("Changed context. This should be in a cloned filter.");
    }
    return setVarIndex(localContext);
  }

  /**
   * Sets the varIndex if (and only if) it has not been set before.
   * @param context The context to use to get the variable index.
   * @return The context used, so it can be reused for the next operation.
   */
  private Context setVarIndex(Context context) {
    if (varIndex == NOT_INITIALIZED) varIndex = context.getInternalColumnIndex(name);
    return context;
  }

}
