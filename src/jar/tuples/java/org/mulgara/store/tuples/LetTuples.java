/*
 * Copyright 2009 Fedora Commons, Inc.
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

package org.mulgara.store.tuples;

// Java 2 standard packages
import java.util.*;

// Third party packages
import org.apache.log4j.*;

// Locally written packages
import org.jrdf.graph.Node;
import org.mulgara.query.Constraint;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.ContextOwner;
import org.mulgara.query.filter.RDFTerm;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.resolver.spi.TuplesContext;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.tuples.AbstractTuples;

/**
 * Variable binding operation. This class wraps another Tuples, binding a variable based on what
 * the context provided by that Tuples.
 * 
 * @created July 1, 2009
 * @author Paula Gearon
 * @copyright &copy; 2009 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class LetTuples extends AbstractTuples implements ContextOwner {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(LetTuples.class.getName());

  /** The inner tuples to filter. */
  protected Tuples innerTuples;

  /** The expression to bind the variable to. */
  protected RDFTerm expr;

  /** The variable to be bound by the expression. */
  protected Variable var;

  /** The tuples context */
  protected TuplesContext context;

  /** A list of context owners that this owner provides the context for. */
  private List<ContextOwner> contextListeners = new ArrayList<ContextOwner>();

  /** A convenience for remembering the last column in this tuples. */
  private int lastCol;

  /**
   * Configure a tuples for binding a variable.
   *
   * @param innerTuples The original tuples.
   * @param var the variable to bind.
   * @param expr The expression to bind the variable to.
   * @param queryContext The context to evaluate the tuples in.
   * @throws IllegalArgumentException If the <var>innerTuples</var> is null.
   */
  LetTuples(Tuples innerTuples, Variable var, RDFTerm expr, QueryEvaluationContext queryContext) throws IllegalArgumentException {
    // store the operands
    this.var = var;
    this.expr = expr;
    this.innerTuples = (Tuples)innerTuples.clone();
    this.context = new TuplesContext(this.innerTuples, queryContext.getResolverSession());
    expr.setContextOwner(this);

    // duplicate the inner variables, and add the new variable to it
    Variable[] innerVars = this.innerTuples.getVariables();
    this.lastCol = innerVars.length;
    Variable[] vars = new Variable[this.lastCol + 1];
    for (int i = 0; i < innerVars.length; i++) {
      if (var.equals(innerVars[i])) throw new IllegalArgumentException("Variable ?" + var + " is already bound");
      vars[i] = innerVars[i];
    }
    vars[innerVars.length] = var;

    setVariables(vars);
  }


  /** {@inheritDoc} */
  public long getColumnValue(int column) throws TuplesException {
    if (column < lastCol) return innerTuples.getColumnValue(column);
    // re-root the expression to this Tuples
    expr.setContextOwner(this);
    try {
      Node val = expr.getJRDFValue();
      return context.localize(val);
    } catch (QueryException e) {
      return UNBOUND;
    }
  }

  
  /** {@inheritDoc} */
  public long getRawColumnValue(int column) throws TuplesException {
    if (column < lastCol) return innerTuples.getRawColumnValue(column);
    // re-root the expression to this Tuples
    expr.setContextOwner(this);
    try {
      Node val = expr.getJRDFValue();
      return context.localize(val);
    } catch (QueryException e) {
      return UNBOUND;
    }
  }


  /** {@inheritDoc} */
  public long getRowUpperBound() throws TuplesException {
    return innerTuples.getRowUpperBound();
  }


  /** {@inheritDoc} */
  public long getRowExpectedCount() throws TuplesException {
    return innerTuples.getRowExpectedCount();
  }


  /** {@inheritDoc} */
  public boolean isEmpty() throws TuplesException {
    return innerTuples.isEmpty();
  }


  /** {@inheritDoc} */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    return column == lastCol || innerTuples.isColumnEverUnbound(column);
  }


  /** {@inheritDoc} */
  public Variable[] getVariables() {
    return super.getVariables();
  }


  /** {@inheritDoc} */
  public int getColumnIndex(Variable variable) throws TuplesException {
    return variable.equals(var) ? lastCol : innerTuples.getColumnIndex(variable);
  }


  /** {@inheritDoc} */
  public boolean isMaterialized() {
    return innerTuples.isMaterialized();
  }


  /** {@inheritDoc} */
  public boolean hasNoDuplicates() throws TuplesException {
    return innerTuples.hasNoDuplicates();
  }


  /** {@inheritDoc} */
  public RowComparator getComparator() {
    return innerTuples.getComparator();
  }


  /** {@inheritDoc} */
  public List<Tuples> getOperands() {
    return Collections.unmodifiableList(Arrays.asList(new Tuples[] {innerTuples}));
  }


  /** {@inheritDoc} */
  public boolean isUnconstrained() throws TuplesException {
    return innerTuples.isUnconstrained();
  }


  /** {@inheritDoc} */
  public void renameVariables(Constraint constraint) {
    innerTuples.renameVariables(constraint);
    for (int i = 0; i < StatementStore.VARIABLES.length; i++) {
      if (var.equals(StatementStore.VARIABLES[i])) {
        var = (Variable)constraint.getElement(i);
        break;
      }
    }
  }


  /**
   * {@inheritDoc}
   * We are not going to extend this operation to localized values.
   */
  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    innerTuples.beforeFirst(prefix, suffixTruncation);
  }

  
  /**
   * @return {@inheritDoc}
   * @throws TuplesException {@inheritDoc}
   */
  public boolean next() throws TuplesException {
    return innerTuples.next();
  }


  /** {@inheritDoc} */
  public void close() throws TuplesException {
    innerTuples.close();
  }


  /** @return {@inheritDoc} */
  public Object clone() {
    LetTuples cloned = (LetTuples)super.clone();

    // Clone the mutable fields as well
    cloned.innerTuples = (Tuples)innerTuples.clone();
    cloned.context = new TuplesContext(cloned.innerTuples, context);
    return cloned;
  }


  /**
   * Tells a filter what the current context is.
   * @see org.mulgara.query.filter.ContextOwner#getCurrentContext()
   */
  public Context getCurrentContext() {
    return context;
  }


  /**
   * Allows the context to be set manually. This is not expected.
   * @see org.mulgara.query.filter.ContextOwner#setCurrentContext(org.mulgara.query.filter.Context)
   */
  public void setCurrentContext(Context context) {
    if (!(context instanceof TuplesContext)) throw new IllegalArgumentException("FilteredTuples can only accept a TuplesContext.");
    this.context = (TuplesContext)context;
    for (ContextOwner l: contextListeners) l.setCurrentContext(context);
  }


  /**
   * This provides a context, and does not need to refer to a parent.
   * @see org.mulgara.query.filter.ContextOwner#getContextOwner()
   */
  public ContextOwner getContextOwner() {
    throw new IllegalStateException("Should never be asking for the context owner of a Tuples");
  }


  /**
   * The owner of the context for a Tuples is never needed, since it is always provided by the Tuples.
   * @see org.mulgara.query.filter.ContextOwner#setContextOwner(org.mulgara.query.filter.ContextOwner)
   */
  public void setContextOwner(ContextOwner owner) {
  }


  /**
   * Adds a context owner as a listener so that it will be updated with its context
   * when this owner gets updated.
   * @param l The context owner to register.
   */
  public void addContextListener(ContextOwner l) {
    contextListeners.add(l);
  }

}
