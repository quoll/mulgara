/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.*;
import java.util.*;

// Logging classes
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.Tuples;

/**
 * This wrapper doesn't just globalize columns, it also evaluates subqueries
 * in aggregate-valued columns.
 *
 * @created 2003-12-02
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Andrew Newman
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/05/06 04:07:57 $
 *
 * @maintenanceAuthor $Author: amuys $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SubqueryAnswer extends GlobalizedAnswer {

  /** Logger. This is named after the class.  */
  private final static Logger logger = Logger.getLogger(SubqueryAnswer.class.getName());

  /** The target <code>SELECT</code> clause */
  private List<? extends SelectElement> variableList;

  /** The variables which are not bound */
  private List<Variable> unboundVars = new ArrayList<Variable>();

  /** All selected variables */
  private Variable[] variables;

  /** The current database session for this query. */
  protected OperationContext operationContext;

  /**
   * Assignment property.
   *
   * This ought to be replaced by owl:sameIndividualAs.
   */
  static final URIReferenceImpl MULGARA_IS = new URIReferenceImpl(URI.create(Mulgara.NAMESPACE+"is"));

  //
  // Constructor
  //

  /**
   * Construct a wrapper around the <var>tuples</var> parameter.
   *
   * @param operationContext  the session from which to globalize the local
   *                          nodes within the <var>tuples</var> parameter
   * @param resolverSession  the session used to globalize the <var>tuples</var>
   * @param tuples  the resolved answer to the <code>WHERE<code> clause,
   *                providing variable bindings for the variables that aren't
   *                subqueries
   * @param variableList  the <code>SELECT<code> clause, including
   *                      subquery-valued clauses to be resolved
   * @throws IllegalArgumentException if <var>variableList</var> is <code>null</code>
   * @throws TuplesException if it fails to get the row cardinality of the given tuples.
   */
  SubqueryAnswer(OperationContext operationContext, ResolverSession resolverSession,
      Tuples tuples, List<? extends SelectElement> variableList) throws TuplesException {
    super(tuples, resolverSession);

    this.operationContext = operationContext;
    assignVariables(tuples, variableList);
  }

  /**
   * Assigns the member variables based on the variables passed and the variable
   * bindings in the tuples.
   *
   * @param tuples the tuples to test against the corret binding in the variable list.
   * @param variableList the list of variables from the SELECT clause.
   * @throws IllegalArgumentException if <var>variableList</var> is <code>null</code>
   * @throws TuplesException if it fails to get the row cardinality of the given tuples.
   */
  private void assignVariables(Tuples tuples, List<? extends SelectElement> variableList) throws TuplesException, IllegalArgumentException {
    boolean empty = (tuples.isEmpty());
    this.variableList = variableList;
    this.variables = new Variable[variableList.size()];
    for (int i = 0; i < variableList.size(); i++) {
      SelectElement element = variableList.get(i);
      if (element instanceof Variable) {

        variables[i] = (Variable)element;

        // Validate the variable
        try {
          // if (!empty && !variables[i].isBnodeVar()) tuples.getColumnIndex(variables[i]);
          if (!empty) tuples.getColumnIndex(variables[i]);
        } catch (TuplesException e) {
          unboundVars.add(variables[i]);
          if (logger.isDebugEnabled()) logger.debug(variables[i] + " does not appear in the \"tuples\" parameter");
        }
      } else if (element instanceof ConstantValue) {
        variables[i] = ((ConstantValue) element).getVariable();
      } else if (element instanceof AggregateFunction) {
        variables[i] = ((AggregateFunction) element).getVariable();
      } else {
        throw new IllegalArgumentException("Unknown type in SELECT clause: " + element.getClass());
      }
    }

    if (logger.isDebugEnabled()) logger.debug("Constructed for " + tuples + " around " + variableList);
  }

  /**
   * Clone the current object.  Relies on the clone from the superclass.
   */
  public Object clone() {
    SubqueryAnswer cloned = (SubqueryAnswer)super.clone();
    cloned.variableList = this.variableList;
    cloned.variables = new Variable[this.variables.length];
    System.arraycopy(this.variables, 0, cloned.variables, 0, this.variables.length);
    return cloned;
  }

  //
  // Methods overriding GlobalizedAnswer's implementation of Answer
  //


  public int getColumnIndex(Variable variable) throws TuplesException {
    if (variable == null) throw new IllegalArgumentException("Null \"variable\" parameter");

    int index = variableList.indexOf(variable);
    if (index >= 0) {
      return index;
    } else {
      throw new TuplesException("No such variable " + variable + " in tuples " + variableList + " (" + getClass() + ")");
    }
  }

  public int getNumberOfVariables() {
    return variableList.size();
  }

  public Object getObject(int column) throws TuplesException {
    if (logger.isDebugEnabled()) {
      logger.debug("Getting object " + column + " from variableList " + variableList);
    }
    SelectElement element = variableList.get(column);

    if (element instanceof Variable) {
      Variable var = (Variable)element;
      //if (unboundVars.contains(var) || var.isBnodeVar()) return null;
      if (unboundVars.contains(var)) return null;
      return super.getObject(super.getColumnIndex(var));
    } else if (element instanceof ConstantValue) {
      return ((ConstantValue) element).getValue();
    } else if (element instanceof Count) {
      // Atomic aggregate, already resolved by SelectedTuples
      return super.getObject(super.getColumnIndex(((Count) element).getVariable()));
    } else if (element instanceof Subquery) {
      // Answer-valued aggregate, not yet resolved by SelectedTuples
      try {
        if (logger.isDebugEnabled()) logger.debug("Resolving Subquery in SubqueryAnswer: " + element);
        return resolveSubquery((Subquery) element);
      } catch (QueryException e) {
        throw new TuplesException("Couldn't evaluate aggregate", e);
      } catch (RuntimeException t) {
        logger.error("RuntimeException thrown from resolveAggregate", t);
        throw t;
      }
    } else {
      throw new TuplesException("Unknown type in SELECT clause: " + element.getClass());
    }
  }

  /**
   * Return the column with a given name. This only applies to variables.
   * @see org.mulgara.resolver.GlobalizedAnswer#getObject(java.lang.String)
   * @return The bound value for the variable column with that name, or <code>null</code>
   *         if that variable is unbound.
   * @throws TuplesException If there is no variable with that name.
   */
  public Object getObject(String columnName) throws TuplesException {
    for (Variable v: unboundVars) if (v.getName().equals(columnName)) return null;
    for (Variable v: variables) if (v.getName().equals(columnName)) return super.getObject(super.getColumnIndex(v));
    throw new TuplesException("Variable not found");
  }

  public Variable getVariable(int column) {
    return variables[column];
  }

  public Variable[] getVariables() {
    return (Variable[])variables.clone();
  }

  //
  // Internal methods
  //

  /**
   * Evaluate an aggregate-valued columns's function for the current tuples
   * row.
   *
   * @param subquery  the column aggregate function
   * @throws QueryException if the <var>aggregateFunctions</var>'s embedded
   *   query can't be resolved
   */
  private Object resolveSubquery(Subquery subquery) throws QueryException {
    if (logger.isDebugEnabled())  logger.debug("Resolving subquery function " + subquery);

    try {
      Query query = subquery.getQuery();

      Map<Variable,Value> bindings = createBindingMap(tuples, resolverSession);

      ConstraintExpression where = new ConstraintConjunction(
          ConstraintOperations.bindVariables(bindings, query.getConstraintExpression()),
          constrainBindings(bindings));

      query = new Query(query, where);

      if (logger.isDebugEnabled()) logger.debug("Generated subquery: " + query);

      return operationContext.doQuery(query);
    } catch (Exception e) {
      throw new QueryException("Failed to resolve subquery", e);
    }
  }

  protected Map<Variable,Value> createBindingMap(Tuples tuples, ResolverSession resolverSession)
        throws TuplesException, GlobalizeException {
    Map<Variable,Value> bindings = new HashMap<Variable,Value>();
    for (Variable var: tuples.getVariables()) {
      int index = tuples.getColumnIndex(var);
      if (tuples.getColumnValue(index) != Tuples.UNBOUND) {
        // globalize returns a Node, but all our node implementations also implement Value
        bindings.put(var, (Value)resolverSession.globalize(tuples.getColumnValue(index)));
      }
    }
    return bindings;
  }

  protected ConstraintExpression constrainBindings(Map<Variable,Value> bindings) throws TuplesException {
    List<ConstraintExpression> args = new ArrayList<ConstraintExpression>();
    for (Map.Entry<Variable,Value> entry: bindings.entrySet()) {
      args.add(new ConstraintIs(entry.getKey(), entry.getValue()));
    }
    return new ConstraintConjunction(args);
  }
}
