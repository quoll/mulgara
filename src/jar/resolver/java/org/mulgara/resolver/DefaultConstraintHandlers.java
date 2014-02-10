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
 *     2005-2007 Andrae Muys: andrae@muys.id.au
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
import org.jrdf.graph.URIReference;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.filter.SameTerm;
import org.mulgara.query.filter.value.Var;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.ConstraintBindingHandler;
import org.mulgara.resolver.spi.ConstraintLocalization;
import org.mulgara.resolver.spi.ConstraintGraphRewrite;
import org.mulgara.resolver.spi.ConstraintResolutionHandler;
import org.mulgara.resolver.spi.ConstraintVariableRewrite;
import org.mulgara.resolver.spi.GraphResolutionHandler;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;
import org.mulgara.util.NVPair;

/**
 * Provides handlers for the standard constraints included in mulgara.
 *
 * @created 2007-11-09
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 * @company <a href="http://www.netymon.com">Netymon Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class DefaultConstraintHandlers
{
  /** Logger.  */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(DefaultConstraintHandlers.class.getName());

  static void initializeHandlers() {
    initializeModelResolutionHandlers();
    initializeConstraintResolutionHandlers();
    initializeConstraintBindingHandlers();
    initializeConstraintModelRewrites();
    initializeConstraintVariableRewrites();
    initializeConstraintLocalizations();
  }

  @SuppressWarnings("unchecked")
  static void initializeModelResolutionHandlers() {
    ConstraintOperations.addModelResolutionHandlers(new NVPair[]
      {
        new NVPair<Class<GraphUnion>, GraphResolutionHandler>(GraphUnion.class, new GraphResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr,
                                Constraint constraint) throws Exception {
            Tuples lhs = ConstraintOperations.
                resolveModelExpression(context, ((GraphOperation)modelExpr).getLHS(), constraint);
            Tuples rhs = ConstraintOperations.
                resolveModelExpression(context, ((GraphOperation)modelExpr).getRHS(), constraint);
            Tuples result = null;
            try {
              result = TuplesOperations.append(lhs, rhs);
              lhs.close();
              lhs = null;
              rhs.close();
              return result;
            } finally {
              if (lhs != null) {
                // result != null means failed during lhs.close()
                if (result == null) lhs.close();
                rhs.close();
              } // else failed during rhs.close()
            }
          }
        }),
        new NVPair<Class<GraphIntersection>, GraphResolutionHandler>(GraphIntersection.class, new GraphResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr,
                                Constraint constraint) throws Exception {
            Tuples lhs = ConstraintOperations.
                resolveModelExpression(context, ((GraphOperation)modelExpr).getLHS(), constraint);
            Tuples rhs = ConstraintOperations.
                resolveModelExpression(context, ((GraphOperation)modelExpr).getRHS(), constraint);
            Tuples result = null;
            try {
              result = TuplesOperations.join(lhs, rhs);
              lhs.close();
              lhs = null;
              rhs.close();
              return result;
            } catch (TuplesException e) {
              if (lhs != null) { // haven't closed anything yet
                if (result == null) lhs.close(); // if result is set then exception due to lhs.close 
                rhs.close();
              } // else means failed during rhs.close
              throw e;
            }
          }
        }),
        new NVPair<Class<GraphResource>, GraphResolutionHandler>(GraphResource.class, new GraphResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr,
                                Constraint constraint) throws Exception {
            return context.resolve((GraphResource)modelExpr, (Constraint)constraint);
          }
        }),
        new NVPair<Class<GraphVariable>, GraphResolutionHandler>(GraphVariable.class, new GraphResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr,
                                Constraint constraint) throws Exception {
            Variable modelVar = ((GraphVariable)modelExpr).getVariable();
            if (constraint.getVariables().contains(modelVar)) {
              // need to change the re-write and wrap the result in a filter
              Variable newVar = new Variable("*" + modelVar.getName() + "0");
              constraint = ConstraintOperations.rewriteConstraintVariable(modelVar, newVar, constraint);
              Tuples result = context.resolve(null, constraint);
              return TuplesOperations.filter(result, new SameTerm(convert(newVar), convert(modelVar)), context);
            }
            return context.resolve(null, ConstraintOperations.rewriteConstraintModel(modelVar, constraint));
          }
        })
      });
  }

  /** Utility for converting a Variable to a filterable Var */
  static Var convert(Variable v) {
    return new Var(v.getName());
  }

  @SuppressWarnings("unchecked")
  static void initializeConstraintResolutionHandlers() {
    ConstraintOperations.addConstraintResolutionHandlers(new NVPair[]
      {
        new NVPair<Class<ConstraintConjunction>, ConstraintResolutionHandler>(ConstraintConjunction.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            List<Tuples> l =
                context.resolveConstraintOperation(modelExpr, (ConstraintOperation)constraintExpr);
            try {
              return TuplesOperations.join(l);
            } finally {
              Iterator<?> i = l.iterator();
              while (i.hasNext()) {
                ((Tuples)i.next()).close();
              }
            }
          }
        }),
        new NVPair<Class<ConstraintDisjunction>, ConstraintResolutionHandler>(ConstraintDisjunction.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            List<Tuples> l =
                context.resolveConstraintOperation(modelExpr, (ConstraintOperation)constraintExpr);
            try {
              if (context.isDistinctQuery()) return TuplesOperations.append(l);
              else return TuplesOperations.unorderedAppend(l);
            } finally {
              Iterator<?> i = l.iterator();
              while (i.hasNext()) {
                ((Tuples)i.next()).close();
              }
            }
          }
        }),
        new NVPair<Class<ConstraintDifference>, ConstraintResolutionHandler>(ConstraintDifference.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            List<ConstraintExpression> constraints = ((ConstraintOperation)constraintExpr).getElements();
            assert constraints.size() == 2;
            Tuples lhs = ConstraintOperations.resolveConstraintExpression(context, modelExpr, constraints.get(0));
            // The RHS must be searchable, so it must return a distinct result
            // since DISTINCT forces sorting
            boolean distinct = context.setDistinctQuery(true);
            Tuples rhs = ConstraintOperations.resolveConstraintExpression(context, modelExpr, constraints.get(1));
            context.setDistinctQuery(distinct);
            Tuples result = null;
            try {
              result = TuplesOperations.subtract(lhs, rhs);
              lhs.close();
              lhs = null;
              rhs.close();
              return result;
            } finally {
              if (lhs != null) {
                // result != null means failed during lhs.close()
                if (result == null) lhs.close();
                rhs.close();
              } // else failed during rhs.close()
            }
          }
        }),
        new NVPair<Class<ConstraintOptionalJoin>, ConstraintResolutionHandler>(ConstraintOptionalJoin.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            List<Tuples> args = context.resolveConstraintOperation(modelExpr, (ConstraintOperation)constraintExpr);
            assert args.size() == 2;
            Tuples result = null, a0 = (Tuples)args.get(0), a1 = (Tuples)args.get(1);
            try {
              result = TuplesOperations.optionalJoin(a0, a1, ((ConstraintOptionalJoin)constraintExpr).getFilter(), context);
              a0.close();
              a0 = null;
              a1.close();
              return result;
            } finally {
              if (a0 != null) {
                // result != null means failed during a0.close()
                if (result == null) a0.close();
                a1.close();
              } // else failed during a1.close()
            }
          }
        }),
        new NVPair<Class<ConstraintIs>, ConstraintResolutionHandler>(ConstraintIs.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            ConstraintIs constraint = (ConstraintIs)constraintExpr;
            return TuplesOperations.assign((Variable)context.localize(constraint.getVariable()),
                                           ((LocalNode)context.localize(constraint.getValueNode())).getValue());
          }
        }),
        new NVPair<Class<ConstraintAssignment>, ConstraintResolutionHandler>(ConstraintAssignment.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            ConstraintAssignment assignment = (ConstraintAssignment)constraintExpr;
            Tuples arg = ConstraintOperations.resolveConstraintExpression(context, modelExpr, assignment.getContextConstraint());
            return TuplesOperations.assign(arg, assignment.getVariable(), assignment.getExpression(), context);
          }
        }),
        new NVPair<Class<ConstraintImpl>, ConstraintResolutionHandler>(ConstraintImpl.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            ConstraintImpl constraint = (ConstraintImpl)constraintExpr;
            ConstraintElement constraintElem = constraint.getModel();
            assert constraintElem != null;
            if (constraintElem.equals(Variable.FROM)) {
              return ConstraintOperations.resolveModelExpression(context, modelExpr, constraint);
            } else if (constraintElem instanceof URIReference) {
              return ConstraintOperations.resolveModelExpression(context, new GraphResource(((URIReference)constraintElem).getURI()), constraint);
            } else if (constraintElem instanceof LocalNode) {
              return context.resolve(null, constraint);
            } else if (constraintElem instanceof Variable) {
              for (int i = 0; i < 3; i++) {
                if (constraintElem.equals(constraint.getElement(i))) {
                  GraphVariable modelVar = new GraphVariable((Variable)constraintElem);
                  return ConstraintOperations.resolveModelExpression(context, modelVar, constraint);
                }
              }
              return context.resolve(null, (ConstraintImpl)constraintExpr);
            }
            else {
              throw new QueryException("Specified model not a URIReference/Variable: " + constraintElem +" is a " + constraintElem.getClass().getName() );
            }
          }
        }),
        new NVPair<Class<WalkConstraint>, ConstraintResolutionHandler>(WalkConstraint.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            return WalkFunction.walk(context, (WalkConstraint)constraintExpr, modelExpr, context.getResolverSession());
          }
        }),
        new NVPair<Class<SingleTransitiveConstraint>, ConstraintResolutionHandler>(SingleTransitiveConstraint.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            SingleTransitiveConstraint constraint = (SingleTransitiveConstraint)constraintExpr;
            if (constraint.isAnchored()) {
              return DirectTransitiveFunction.infer(context, constraint, modelExpr, context.getResolverSession());
            } else {
              return ExhaustiveTransitiveFunction.infer(context, constraint, modelExpr, context.getResolverSession());
            }
          }
        }),
        new NVPair<Class<TransitiveConstraint>, ConstraintResolutionHandler>(TransitiveConstraint.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            return ExhaustiveTransitiveFunction.infer(context, (TransitiveConstraint)constraintExpr, modelExpr, context.getResolverSession());
          }
        }),
        new NVPair<Class<ConstraintFilter>, ConstraintResolutionHandler>(ConstraintFilter.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            Tuples unfiltered = ConstraintOperations.resolveConstraintExpression(context, modelExpr, ((ConstraintFilter)constraintExpr).getUnfilteredConstraint());
            Tuples result = null;
            try {
              result = TuplesOperations.filter(unfiltered, ((ConstraintFilter)constraintExpr).getFilter(), context);
              unfiltered.close();
              return result;
            } catch (TuplesException e) {
              // result != null means that exception occurred during unfiltered.close()
              if (result == null) unfiltered.close();
              throw e;
            }
          }
        }),
        new NVPair<Class<ConstraintIn>, ConstraintResolutionHandler>(ConstraintIn.class, new ConstraintResolutionHandler() {
          public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
            ConstraintIn constraint = (ConstraintIn)constraintExpr;
            GraphExpression graph;
            if (constraint.getGraph() instanceof URIReferenceImpl) {
              graph = new GraphResource(((URIReferenceImpl)constraint.getGraph()).getURI());
            } else {
              assert constraint.getGraph() instanceof Variable;
              graph = new GraphVariable((Variable)constraint.getGraph());
            }
            return ConstraintOperations.resolveConstraintExpression(context, graph, constraint.getConstraintParam());
          }
        }),
      });
  }

  @SuppressWarnings("unchecked")
  static void initializeConstraintBindingHandlers() {
    ConstraintOperations.addConstraintBindingHandlers(new NVPair[]
      {
        new NVPair<Class<ConstraintTrue>, ConstraintBindingHandler>(ConstraintTrue.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            return constraintExpr;
          }
        }),
        new NVPair<Class<ConstraintFalse>, ConstraintBindingHandler>(ConstraintFalse.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            return constraintExpr;
          }
        }),
        new NVPair<Class<ConstraintImpl>, ConstraintBindingHandler>(ConstraintImpl.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            return ConstraintOperations.replace(bindings, (Constraint)constraintExpr);
          }
        }),
        new NVPair<Class<ConstraintIs>, ConstraintBindingHandler>(ConstraintIs.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            return ConstraintOperations.replace(bindings, (Constraint)constraintExpr);
          }
        }),
        new NVPair<Class<SingleTransitiveConstraint>, ConstraintBindingHandler>(SingleTransitiveConstraint.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            return new SingleTransitiveConstraint(ConstraintOperations.replace(bindings, (Constraint)constraintExpr));
          }
        }),
        new NVPair<Class<TransitiveConstraint>, ConstraintBindingHandler>(TransitiveConstraint.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            TransitiveConstraint tc = (TransitiveConstraint)constraintExpr;
            return new TransitiveConstraint(ConstraintOperations.replace(bindings, tc.getAnchoredConstraint()),
                                            ConstraintOperations.replace(bindings, tc.getUnanchoredConstraint()));
          }
        }),
        new NVPair<Class<WalkConstraint>, ConstraintBindingHandler>(WalkConstraint.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            WalkConstraint wc = (WalkConstraint)constraintExpr;
            return new WalkConstraint(ConstraintOperations.replace(bindings, wc.getAnchoredConstraint()),
                                      ConstraintOperations.replace(bindings, wc.getUnanchoredConstraint()));
          }
        }),
        new NVPair<Class<ConstraintFilter>, ConstraintBindingHandler>(ConstraintFilter.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            return new ConstraintFilter(ConstraintOperations.replace(bindings, (Constraint)constraintExpr), ((ConstraintFilter)constraintExpr).getFilter());
          }
        }),
        new NVPair<Class<ConstraintConjunction>, ConstraintBindingHandler>(ConstraintConjunction.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            return new ConstraintConjunction(ConstraintOperations.replaceOperationArgs(bindings, (ConstraintOperation)constraintExpr));
          }
        }),
        new NVPair<Class<ConstraintDisjunction>, ConstraintBindingHandler>(ConstraintDisjunction.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            return new ConstraintDisjunction(ConstraintOperations.replaceOperationArgs(bindings, (ConstraintOperation)constraintExpr));
          }
        }),
        new NVPair<Class<ConstraintDifference>, ConstraintBindingHandler>(ConstraintDifference.class, new ConstraintBindingHandler() {
          public ConstraintExpression bindVariables(Map<Variable, Value> bindings, ConstraintExpression constraintExpr) throws Exception {
            List<?> args = ConstraintOperations.replaceOperationArgs(bindings, (ConstraintOperation)constraintExpr);
            return new ConstraintDifference((ConstraintExpression)args.get(0), (ConstraintExpression)args.get(1));
          }
        }),
      });
  }

  @SuppressWarnings("unchecked")
  static void initializeConstraintModelRewrites() {
    ConstraintOperations.addConstraintModelRewrites(new NVPair[]
      {
        new NVPair<Class<ConstraintImpl>, ConstraintGraphRewrite>(ConstraintImpl.class, new ConstraintGraphRewrite() {
          public Constraint rewrite(ConstraintElement newModel, Constraint constraint) throws Exception {
            return new ConstraintImpl(constraint.getElement(0), constraint.getElement(1), constraint.getElement(2), newModel);
          }
        }),
      });
  }

  @SuppressWarnings("unchecked")
  static void initializeConstraintVariableRewrites() {
    ConstraintOperations.addConstraintVariableRewrites(new NVPair[]
      {
        new NVPair<Class<ConstraintImpl>, ConstraintVariableRewrite>(ConstraintImpl.class, new ConstraintVariableRewrite() {
          public Constraint rewrite(Variable modelVar, Variable newVar, Constraint constraint) throws Exception {
            ConstraintElement[] ce = new ConstraintElement[3];
            for (int e = 0; e < ce.length; e++) {
              ce[e] = constraint.getElement(e);
              if (ce[e] instanceof Variable && ((Variable)ce[e]).getName().equals(modelVar.getName())) {
                ce[e] = newVar;
              }
            }
            return new ConstraintImpl(ce[0], ce[1], ce[2], modelVar);
          }
        }),
      });
  }

  @SuppressWarnings("unchecked")
  static void initializeConstraintLocalizations() {
    ConstraintOperations.addConstraintLocalizations(new NVPair[]
      {
        new NVPair<Class<ConstraintImpl>, ConstraintLocalization>(ConstraintImpl.class, new ConstraintLocalization() {
          public Constraint localize(QueryEvaluationContext context, Constraint constraint) throws Exception {
            return new ConstraintImpl(context.localize(constraint.getElement(0)),
                context.localize(constraint.getElement(1)),
                context.localize(constraint.getElement(2)),
                context.localize(constraint.getElement(3)));
          }
        }),
      });
  }
}
