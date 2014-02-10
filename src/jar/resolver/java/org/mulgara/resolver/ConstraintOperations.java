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
 * Contributor(s):
 *   ConstraintLocalizations contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *   getModel() contributed by Netymon Pty Ltd on behalf of
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

// Local packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.ConstraintBindingHandler;
import org.mulgara.resolver.spi.ConstraintLocalization;
import org.mulgara.resolver.spi.ConstraintGraphRewrite;
import org.mulgara.resolver.spi.ConstraintResolutionHandler;
import org.mulgara.resolver.spi.ConstraintVariableRewrite;
import org.mulgara.resolver.spi.GraphResolutionHandler;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.util.NVPair;
import org.mulgara.util.ThreadUtil;

/**
 * Localized version of a global {@link Query}.
 *
 * As well as providing coordinate transformation from global to local
 * coordinates, this adds methods to partially resolve the query.
 *
 * @created 2004-05-06
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.13 $
 * @modified $Date: 2005/05/15 04:12:15 $
 * @maintenanceAuthor $Author: pgearon $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ConstraintOperations
{
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(ConstraintOperations.class.getName());

  static Map<Class<? extends ConstraintExpression>,Object> modelResolutionHandlers =
      new HashMap<Class<? extends ConstraintExpression>,Object>();
  static Map<Class<? extends ConstraintExpression>,Object> constraintResolutionHandlers =
      new HashMap<Class<? extends ConstraintExpression>,Object>();
  static Map<Class<? extends ConstraintExpression>,Object> constraintBindingHandlers =
      new HashMap<Class<? extends ConstraintExpression>,Object>();
  static Map<Class<? extends ConstraintExpression>,Object> constraintLocalizations =
      new HashMap<Class<? extends ConstraintExpression>,Object>();
  static Map<Class<? extends ConstraintExpression>,Object> constraintModelRewrites =
      new HashMap<Class<? extends ConstraintExpression>,Object>();
  static Map<Class<? extends ConstraintExpression>,Object> constraintVariableRewrites =
    new HashMap<Class<? extends ConstraintExpression>,Object>();


  static {
    DefaultConstraintHandlers.initializeHandlers();
  }

  static void addConstraintResolutionHandlers(NVPair<Class<? extends ConstraintExpression>,Object>[] resolutionHandlers) throws RuntimeException {
    addToMap(resolutionHandlers, constraintResolutionHandlers,
             ConstraintExpression.class, ConstraintResolutionHandler.class);
  }


  static void addConstraintBindingHandlers(NVPair<Class<? extends ConstraintExpression>,Object>[] bindingHandlers) throws RuntimeException {
    addToMap(bindingHandlers, constraintBindingHandlers,
             ConstraintExpression.class, ConstraintBindingHandler.class);
  }


  static void addModelResolutionHandlers(NVPair<Class<? extends ConstraintExpression>,Object>[] resolutionHandlers) throws RuntimeException {
    addToMap(resolutionHandlers, modelResolutionHandlers,
             GraphExpression.class, GraphResolutionHandler.class);
  }

  static void addConstraintModelRewrites(NVPair<Class<? extends ConstraintExpression>,Object>[] resolutionHandlers) throws RuntimeException {
    addToMap(resolutionHandlers, constraintModelRewrites,
             ConstraintExpression.class, ConstraintGraphRewrite.class);
  }

  static void addConstraintVariableRewrites(NVPair<Class<? extends ConstraintExpression>,Object>[] resolutionHandlers) throws RuntimeException {
    addToMap(resolutionHandlers, constraintVariableRewrites,
             ConstraintExpression.class, ConstraintVariableRewrite.class);
  }

  static void addConstraintLocalizations(NVPair<Class<? extends ConstraintExpression>,Object>[] resolutionHandlers) throws RuntimeException {
    addToMap(resolutionHandlers, constraintLocalizations,
             Constraint.class, ConstraintLocalization.class);
  }

  static boolean constraintRegistered(Class<? extends ConstraintExpression> constraintClass) {
  return modelResolutionHandlers.containsKey(constraintClass) ||
         constraintResolutionHandlers.containsKey(constraintClass) ||
         constraintLocalizations.containsKey(constraintClass) ||
         constraintModelRewrites.containsKey(constraintClass);
  }

  static void addToMap(NVPair<Class<? extends ConstraintExpression>,Object>[] pairs, Map<Class<? extends ConstraintExpression>,Object> dest, Class<?> keyClass, Class<?> valueClass) throws ClassCastException {
    // Type check array.
    for (int i = 0; i < pairs.length; i++) {
      Class<?> key = pairs[i].getName();
      Object value = pairs[i].getValue();
      if (!keyClass.isAssignableFrom(key)) {
        throw new ClassCastException(key + " is not assignable to " + keyClass);
      }
      if (!valueClass.isAssignableFrom(value.getClass())) {
        throw new ClassCastException(value.getClass() + " is not assignable to " + valueClass);
      }
    }
    // Insert array into map.
    for (int i = 0; i < pairs.length; i++) {
      dest.put(pairs[i].getName(), pairs[i].getValue());
    }
  }


  public static Tuples resolveModelExpression(QueryEvaluationContext context, GraphExpression modelExpr,
                                      Constraint constraint) throws QueryException {
    try {
      ThreadUtil.checkForInterrupt(QueryException.class);
      
      if (logger.isDebugEnabled()) {
        logger.debug("Resolving " + constraint + " against GraphExpression[" + modelExpr.getClass() + "]");
      }

      GraphResolutionHandler op = (GraphResolutionHandler)modelResolutionHandlers.get(modelExpr.getClass());
      if (op == null) {
        throw new QueryException("Unknown GraphExpression type: " + modelExpr.getClass() + " known types: " + modelResolutionHandlers.keySet());
      }
      Tuples result = op.resolve(context, modelExpr, constraint);

      if (logger.isDebugEnabled()) {
        logger.debug("Resolved " + constraint + " against GraphExpression[" + modelExpr.getClass() + "] to: " + result);
      }

      return result;
    } catch (QueryException eq) {
      throw eq;
    } catch (Exception e) {
      throw new QueryException("Resolving model expression failed", e);
    }
  }


  public static Tuples resolveConstraintExpression(QueryEvaluationContext context,
      GraphExpression modelExpr, ConstraintExpression constraintExpr) throws QueryException {
    try {
      ThreadUtil.checkForInterrupt(QueryException.class);
      
      if (logger.isDebugEnabled()) {
        logger.debug("Resolving ConstraintExpression[" + constraintExpr.getClass() + "]");
      }

      ConstraintResolutionHandler op = (ConstraintResolutionHandler)constraintResolutionHandlers.get(constraintExpr.getClass());
      if (op == null) {
        throw new QueryException("Unknown ConstraintExpression type: " + constraintExpr.getClass() + " known types: " + constraintResolutionHandlers.keySet());
      }

      Tuples result = op.resolve(context, modelExpr, constraintExpr);

      if (logger.isDebugEnabled()) {
        logger.debug("Resolved ConstraintExpression[" + constraintExpr.getClass() + "] to: " + result);
      }

      return result;
    } catch (QueryException eq) {
      throw eq;
    } catch (Exception e) {
      throw new QueryException("Failed to resolve constraintExpression", e);
    }
  }


  public static ConstraintExpression bindVariables(Map<Variable,Value> bindings, ConstraintExpression constraintExpr) throws QueryException {
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Binding Variables in ConstraintExpression[" + constraintExpr.getClass() + "]");
        logger.debug("binding variables for: " + constraintExpr + " with " + bindings);
      }

      ConstraintBindingHandler op = (ConstraintBindingHandler)constraintBindingHandlers.get(constraintExpr.getClass());
      if (op == null) {
        throw new QueryException("Unknown ConstraintExpression type: " + constraintExpr.getClass() + " known types: " + constraintBindingHandlers.keySet());
      }

      ConstraintExpression result = op.bindVariables(bindings, constraintExpr);

      if (logger.isDebugEnabled()) {
        logger.debug("Bound variables in ConstraintExpression[" + constraintExpr.getClass() + "] to: " + result);
      }

      return result;
    } catch (QueryException eq) {
      throw eq;
    } catch (Exception e) {
      throw new QueryException("Failed to bind variables in constraintExpression", e);
    }
  }


  public static Constraint localize(QueryEvaluationContext context,
                                    Constraint constraint) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("Localizing Constraint[" + constraint.getClass() + "]");
    }

    ConstraintLocalization op = (ConstraintLocalization)constraintLocalizations.get(constraint.getClass());
    if (op == null) {
      throw new QueryException("Unknown Constraint type: " + constraint.getClass() + " known types: " + constraintLocalizations.keySet());
    }

    Constraint result = op.localize(context, constraint);

    if (logger.isDebugEnabled()) {
      logger.debug("Localized Constraint[" + constraint.getClass() + "] to: " + result);
    }

    return result;
  }


  public static Constraint rewriteConstraintModel(ConstraintElement newModel,
      Constraint constraint) throws QueryException {
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Rewriting Graph" + newModel + " in " + constraint);
      }

      ConstraintGraphRewrite op = (ConstraintGraphRewrite)constraintModelRewrites.get(constraint.getClass());
      if (op == null) {
        throw new QueryException("Unknown Constraint type: " + constraint.getClass() + " known types: " + constraintModelRewrites.keySet());
      }
      Constraint result = op.rewrite(newModel, constraint);

      if (logger.isDebugEnabled()) {
        logger.debug("Rewrote Graph " + newModel + " in " + constraint + " to " + result);
      }

      return result;
    } catch (QueryException eq) {
      throw eq;
    } catch (Exception e) {
      throw new QueryException("Rewriting constraint failed", e);
    }
  }


  public static Constraint rewriteConstraintVariable(Variable modelVar, Variable newVar, Constraint constraint) throws QueryException {
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Rewriting variable " + modelVar + " in " + constraint + " and setting graph");
      }

      ConstraintVariableRewrite op = (ConstraintVariableRewrite)constraintVariableRewrites.get(constraint.getClass());
      if (op == null) {
        throw new QueryException("Unknown Constraint type: " + constraint.getClass() + " known types: " + constraintVariableRewrites.keySet());
      }
      Constraint result = op.rewrite(modelVar, newVar, constraint);

      if (logger.isDebugEnabled()) {
        logger.debug("Rewrote Graph " + modelVar + " in " + constraint + " to " + result);
      }

      return result;
    } catch (QueryException eq) {
      throw eq;
    } catch (Exception e) {
      throw new QueryException("Rewriting constraint failed", e);
    }
  }


  public static Constraint replace(Map<Variable,Value> bindings, Constraint constraint) throws QueryException {
    return ConstraintFactory.newConstraint(replace(bindings, constraint.getElement(0)),
                                           replace(bindings, constraint.getElement(1)),
                                           replace(bindings, constraint.getElement(2)),
                                           replace(bindings, constraint.getModel()));
  }


  public static ConstraintElement replace(Map<Variable,Value> bindings, ConstraintElement element) {
    if (element instanceof Variable && bindings.containsKey(element)) {
      return (ConstraintElement)bindings.get(element);
    } else {
      return element;
    }
  }

  public static List<ConstraintExpression> replaceOperationArgs(Map<Variable,Value> bindings, ConstraintOperation constraint) throws QueryException {
    List<ConstraintExpression> newArgs = new ArrayList<ConstraintExpression>();
    for (ConstraintExpression expr: constraint.getElements()) {
      newArgs.add(bindVariables(bindings, expr));
    }

    return newArgs;
  }
}
