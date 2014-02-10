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
 *   Various modifications to this file copyright:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 *   under contract 4500430665
 *   contributed to the Kowari Project under the
 *     Mozilla Public License version 1.1
 *   per clause 4.1.3 of the above contract.
 *
 *   Various modifications to this file copyright:
 *     2005-2006 Netymon Pty Ltd: mail@netymon.com
 *
 *   Various modifications to this file copyright:
 *     2005-2006 Andrae Muys: andrae@muys.id.au
 *
 *   getModel() contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *   ConstraintLocalization contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Java 2 standard packages
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.RestrictPredicateFactory;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.util.StackTrace;

/**
 * Localized version of a global {@link Query}.
 *
 * As well as providing coordinate transformation from global to local
 * coordinates, this adds methods to partially resolve the query.
 *
 * @created 2004-05-06
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.12 $
 * @modified $Date: 2005/05/16 11:07:07 $
 * @maintenanceAuthor $Author: amuys $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class LocalQueryResolver implements QueryEvaluationContext {
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(LocalQueryResolver.class.getName());

  private final DatabaseOperationContext operationContext;

  private final ResolverSession resolverSession;

  private boolean distinctQuery = false;

  // Constructor
  LocalQueryResolver(DatabaseOperationContext operationContext, ResolverSession resolverSession) {
    if (operationContext == null) {
      throw new IllegalArgumentException("Null 'operationContext' parameter");
    } else if (resolverSession == null) {
      throw new IllegalArgumentException("Null 'resolverSession' parameter");
    }

    this.operationContext = operationContext;
    this.resolverSession = resolverSession;
  }

  public List<Tuples> resolveConstraintOperation(GraphExpression modelExpr,
                                         ConstraintOperation constraintOper)
      throws QueryException {

    LinkedList<Tuples> result = new LinkedList<Tuples>();
    for (ConstraintExpression constraintExpr: constraintOper.getElements()) {
      result.add(ConstraintOperations.resolveConstraintExpression(this, modelExpr, constraintExpr));
    }

    return result;
  }


  /**
   * Returns either a variable or the LocalNode local equivalent of the
   * ConstraintElement.
   *
   * @param constraintElement  a global constraint element
   * @return the localized equivalent to the global <var>constraintElement</var>
   */
  public ConstraintElement localize(ConstraintElement constraintElement)
    throws LocalizeException
  {
    if (constraintElement instanceof Node) {
      return new LocalNode(resolverSession.localize((Node)constraintElement));
    } else if (constraintElement instanceof Variable) {
      return constraintElement;
    } else if (constraintElement instanceof LocalNode) {
      return (LocalNode)constraintElement;
    } else {
      throw new IllegalArgumentException("Not a global constraint element: " + constraintElement +
                                         "::" + constraintElement.getClass());
    }
  }

  public ConstraintElement globalize(ConstraintElement constraintElement)
    throws GlobalizeException
  {
    Node node;
    if (constraintElement instanceof LocalNode) {
      node = resolverSession.globalize(((LocalNode)constraintElement).getValue());
      if (node instanceof URIReferenceImpl ||
          node instanceof LiteralImpl ||
          node instanceof BlankNodeImpl) {
        return (Value)node;
      } else {
        throw new GlobalizeException(((LocalNode)constraintElement).getValue(),
            "Globalize of non-internal Nodes not supported by LocalQueryResolver: " + constraintElement + " -> " + node);
      }
    } else {
      return constraintElement;  // Either Variable or GlobalNode
    }
  }


  /**
   * Localize and resolve the leaf node of the <code>FROM</code> and
   * <code>WHERE</code> clause product.
   *
   * @param graphResource  the <code>FROM<code> clause to resolve, never
   *   <code>null</codE>
   * @param constraint  the <code>WHERE</code> clause to resolve, which must
   *   have {@link Variable#FROM} as its fourth element, and never be
   *   <code>null</code>
   * @throws QueryException if resolution can't be obtained
   */
  public Tuples resolve(GraphResource graphResource, Constraint constraint) throws QueryException
  {
    assert graphResource != null || !constraint.getModel().equals(Variable.FROM);
    assert constraint != null;

    // Delegate constraint resolution back to the database session
    try {
      Constraint localized = ConstraintOperations.localize(this, constraint);

      if (localized.getModel().equals(Variable.FROM)) {
        // create the URIReferenceImpl without checking if it is absolute
        localized = ConstraintOperations.rewriteConstraintModel(
            localize(new URIReferenceImpl(graphResource.getURI(), false)), localized);
      }

      Tuples result = operationContext.resolve(localized);

      return result;
    } catch (LocalizeException e) {
      throw new QueryException("Unable to resolve FROM " + graphResource +
                               " WHERE " + constraint, e);
    } catch (QueryException eq) {
      throw new QueryException("Error resolving " + constraint + " from " + graphResource, eq);
    } catch (Exception e) {
      throw new QueryException("Unexpected error resolving " + constraint + " from " + graphResource, e);
    }
  }


  public Tuples resolve(GraphExpression graphExpression, ConstraintExpression constraintExpression) throws QueryException {
    return ConstraintOperations.resolveConstraintExpression(this, graphExpression, constraintExpression);
  }


  public ResolverSession getResolverSession() {
    return resolverSession;
  }

  /**
   * Indicates that the query being run in this context should return distinct results.
   * @return If <code>true</code>, then return distinct results. Otherwise allow for duplicates.
   */
  public boolean isDistinctQuery() {
    return distinctQuery;
  }

  /**
   * Sets the "distinct" status of a query context, returning the previous value.
   * @param newValue The new value to set the distinct status to. 
   * @return The previous value of the distinct status.
   */
  public boolean setDistinctQuery(boolean newValue) {
    boolean oldValue = distinctQuery;
    distinctQuery = newValue;
    return oldValue;
  }

  Tuples resolveMap(Query query, Map<Variable,Value> outerBindings) throws QueryException {
    try {
      Query newQuery = new Query(
          query.getVariableList(),
          query.getModelExpression(),
          new ConstraintConjunction(
              ConstraintOperations.bindVariables(outerBindings, query.getConstraintExpression()),
              constrainBindings(outerBindings)),
          query.getHavingExpression(),
          query.getOrderList(),
          query.getLimit(),
          query.getOffset(),
          query.isDistinct(),
          (Answer)query.getGiven().clone());
          
      return operationContext.innerCount(newQuery);
    } catch (LocalizeException el) {
      throw new QueryException("Failed to resolve inner local query", el);
    }
  }


  // FIXME: This method should be using a LiteralTuples.  Also I believe MULGARA_IS is now preallocated.
  // Someone needs to try making the change and testing.
  private ConstraintExpression constrainBindings(Map<Variable,Value> bindings) throws LocalizeException {
    List<ConstraintExpression> args = new ArrayList<ConstraintExpression>();
    Iterator<Map.Entry<Variable,Value>> i = bindings.entrySet().iterator();
    logger.info("FIXME:localize should be lookup, need to preallocate MULGARA_IS");
    while (i.hasNext()) {
      Map.Entry<Variable,Value> entry = i.next();
      args.add(ConstraintIs.newLocalConstraintIs(
                  entry.getKey(),
                  new LocalNode(resolverSession.localize(ConstraintIs.MULGARA_IS)),
                  entry.getValue(),
                  null));
    }

    return new ConstraintConjunction(args);
  }

  /**
   * @return the solution to this query
   * @throws QueryException if resolution can't be obtained
   */
  Tuples resolveE(Query query) throws QueryException
  {
    if (query == null) {
      throw new IllegalArgumentException("Query null in LocalQuery::resolveE");
    }

    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Resolving query " + query);
      }

      if (logger.isDebugEnabled()) {
        logger.debug(new StackTrace().toString());
      }

      distinctQuery = query.isDistinct();
      Tuples result = ConstraintOperations.resolveConstraintExpression(this,
          query.getModelExpression(), query.getConstraintExpression());

      if (logger.isDebugEnabled()) {
        logger.debug("Tuples result = " + TuplesOperations.formatTuplesTree(result));
      }

      result = projectSelectClause(query, result);
      result = appendAggregates(query, result);
      result = applyHaving(query, result);
      result = orderResult(query, result);
      result = offsetResult(query, result);
      result = limitResult(query, result);

      return result;
    } catch (TuplesException et) {
      throw new QueryException("Failed to resolve query", et);
    }
  }


  private Tuples projectSelectClause(Query query, Tuples result) throws TuplesException
  {
    if (!result.isEmpty()) {
      Tuples tmp = result;
      try {
        List<Variable> variables = new ArrayList<Variable>();

      /*
       * Note that this code need not concern itself with the order of the select-list,
       * only the contents.  The mapping is handled by the subsequent Answer object,
       * and only becomes important if the row-order is important and is therefore
       * deferred to order-by resolution.
       */
        Variable[] vars = result.getVariables();
        for (int i = 0; i < vars.length; i++) {
          if (query.getVariableList().contains(vars[i])) {
            variables.add(vars[i]);
          }
        }

        result = TuplesOperations.project(result, variables, query.isDistinct());
      } catch (TuplesException t) {
        try {
          tmp.close();
        } catch (TuplesException e) { /* Already throwing an exception. Ignore. */ }
        throw t;
      }
      tmp.close();
    }

    return result;
  }


  private Tuples appendAggregates(Query query, Tuples result) throws TuplesException
  {
    if (!result.isEmpty()) {
      Tuples tmp = result;
      result = new AppendAggregateTuples(resolverSession, this, result,
          filterSubqueries(query.getVariableList()));
      tmp.close();
    }

    return result;
  }

  private List<SelectElement> filterSubqueries(List<SelectElement> select) {
    List<SelectElement> result = new ArrayList<SelectElement>();
    for (SelectElement o : select) {
      if (!(o instanceof Subquery)) {
        result.add(o);
      }
    }

    return result;
  }


  private Tuples applyHaving(Query query, Tuples result) throws TuplesException {
    ConstraintHaving having = query.getHavingExpression();
    Tuples tmp = result;
    if (having != null) {
      result = TuplesOperations.restrict(
                  result, RestrictPredicateFactory.getPredicate(having, resolverSession));
      tmp.close();
    }

    return result;
  }

  private Tuples orderResult(Query query, Tuples result) throws TuplesException, QueryException {
    List<Order> orderList = query.getOrderList();
    if (orderList.size() > 0 && result.getRowCardinality() > Cursor.ONE) {
      Tuples tmp = result;
      result = TuplesOperations.sort(result,
                 new OrderByRowComparator(result, orderList, resolverSession));
      tmp.close();
    }

    return result;
  }

  private Tuples offsetResult(Query query, Tuples result) throws TuplesException
  {
    int offset = query.getOffset();
    if (offset > 0) {
      Tuples tmp = result;
      result = TuplesOperations.offset(result, offset);
      tmp.close();
    }

    return result;
  }


  private Tuples limitResult(Query query, Tuples result)  throws TuplesException
  {
    Integer limit = query.getLimit();
    if (limit != null) {
      Tuples tmp = result;
      result = TuplesOperations.limit(result, limit.intValue());
      tmp.close();
    }

    return result;
  }
}
