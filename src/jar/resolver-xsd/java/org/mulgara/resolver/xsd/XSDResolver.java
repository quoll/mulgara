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
 *   getModel() contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.xsd;

// Java 2 standard packages;
import java.net.*;
import java.text.ParseException;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;
import javax.transaction.xa.XAResource;

// Locally written packages
import org.mulgara.content.Content;
import org.mulgara.query.*;
import org.mulgara.query.rdf.XSD;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.stringpool.*;
import org.mulgara.store.stringpool.SPObject.TypeCategory;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/**
 * Resolves XML Schema datatyping constraints from the Mulgara string pool.
 *
 * @created 2004-10-28
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Mark Ludlow
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/05/02 20:07:59 $ by $Author: raboczi $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2003 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class XSDResolver implements Resolver {

  /** Logger */
  private static final Logger logger = Logger.getLogger(XSDResolver.class);

  /**
   * This flag indicates that we should reinterpret literals that appear as
   * arguments to binary operations to be of the type which that operation
   * supports.
   *
   * Without his flag set, a more strict view is taken when an inappropriate
   * datatype is passed and a {@link QueryException} will be thrown.
   */
  private static final boolean PERMISSIVE_PARAMETER_KLUDGE = true;

  /** Description of the Field */
  static long MULGARA_AFTER;

  /** Description of the Field */
  static long MULGARA_BEFORE;

  /** Description of the Field */
  static long MULGARA_LT;

  /** Description of the Field */
  static long MULGARA_GT;

  /** Description of equals field */
  static long MULGARA_IS;

  /** Description of the Field */
  static final Map<LocalNode,LocalNode> oppositePropertyMap = new HashMap<LocalNode,LocalNode>();

  /** The session that this resolver is associated with */
  private final ResolverSession resolverSession;

  /**
   * Construct a local query.
   *
   * @param resolverSession  the session which this query is local to
   * @param systemResolver The resolver used to resolver system model queries
   * @param rdfType The type of the model
   * @param systemModel The model which represents the system model
   *
   * @throws IllegalArgumentException if <var>variableList</var> is
   *   <code>null</code> or empty
   */
  XSDResolver(ResolverSession resolverSession, Resolver systemResolver,
              long rdfType, long systemModel) throws ResolverFactoryException {

    // Validate "resolverSession" parameter
    if (resolverSession == null) {
      throw new IllegalArgumentException("Null \"resolverSession\" parameter");
    }

    // Initialize fields
    this.resolverSession = resolverSession;
  }

  /**
   * @param graphResource  a model identified in the <code>FROM</code> clause
   * @return <code>true</code> only if this resource is the unique XML Schema
   *   model
   * @throws QueryException always, because it's not implemented
   * @deprecated XSDResolver.canResolve is not implemented
   */
  public boolean canResolve(GraphResource graphResource) throws QueryException {

    throw new QueryException(getClass() + ".canResolve not implemented");
  }

  /**
   * Create a model by creating empty {@link Content}.
   *
   * @param model  {@inheritDoc}.  In this case it should always be a URL
   *  referencing {@link Content} outside the database.
   *
   * @param modelTypeURI  {@inheritDoc}.  This field is ignored, because the
   *   {@link Content} is external.
   */
  public void createModel(long model, URI modelTypeURI) throws
                          ResolverException, LocalizeException {

    if (logger.isDebugEnabled()) {

      logger.debug("Create content model " + model);
    }
  }

  /**
   * @return a {@link DummyXAResource} with a 10 second transaction timeout
   */
  public XAResource getXAResource() {

    return new DummyXAResource(10);
  }

  /**
   * Insert or delete RDF statements from a model stored in a file.
   */
  public void modifyModel(long model, Statements statements, boolean occurs)
                          throws ResolverException {

    if (logger.isDebugEnabled()) {

      logger.debug("Modify external document model " + model);
    }

    throw new ResolverException("Modification of external documents not " +
                                "implemented");
  }

  /**
   * Remove the file containing the model.
   */
  public void removeModel(long model) throws ResolverException {

    if (logger.isDebugEnabled()) {

      logger.debug("Remove model " + model);
    }
  }

  /**
   * Resolve a constraint based on special knowledge of a datatype.
   *
   * @param constraint  the constraint to resolve; this has the side effect of
   *                    setting the constraint's row count estimate
   * @return the statements which are true of the datatype, but only of
   *         instances of the datatype present in the string pool
   */
  public Resolution resolve(Constraint constraint) throws QueryException {

    if (logger.isDebugEnabled()) {
      logger.debug("!! Resolving " + constraint);
    }

    if (constraint.getModel() instanceof Variable) {
      if (logger.isDebugEnabled()) logger.debug("Ignoring solutions for " + constraint);
      return new EmptyResolution(constraint, false);
    }

    try {
      Tuples gNodeTuples = null;

      // Reorder constraints from "value op var" to "var op value"
      if (!(constraint.getElement(0) instanceof Variable) &&
          oppositePropertyMap.keySet().contains(constraint.getElement(1)) &&
          constraint.getElement(2) instanceof Variable) {

        constraint = new ConstraintImpl(constraint.getElement(2),
                                        oppositePropertyMap.get(constraint.getElement(1)),
                                        constraint.getElement(0),
                                        constraint.getModel());
      }

      long property = ((LocalNode) constraint.getElement(1)).getValue();

      if ((constraint.getElement(0) instanceof Variable) &&
          !(constraint.getElement(2) instanceof Variable)) {
        if (property == MULGARA_LT) {
          if (logger.isDebugEnabled()) {
            logger.debug("Evaluating " + constraint.getElement(0) + " less than " + constraint.getElement(2));
          }

          // Evaluate "less than" for xsd:double
          gNodeTuples = resolverSession.findStringPoolRange(
              null, // low value
              true, // include low value
              getBoundDouble(constraint.getElement(2)), // high value
              false // exclude high value
              );
        } else if (property == MULGARA_GT) {

          if (logger.isDebugEnabled()) {
            logger.debug("Evaluating " + constraint.getElement(0) + "(" + constraint.getElement(0).getClass() + ") " +
                         " greater than " + constraint.getElement(2) + "(" + constraint.getElement(2).getClass() + ") ");
          }

          // Evaluate "greater than" for xsd:double
          gNodeTuples = resolverSession.findStringPoolRange(
              getBoundDouble(constraint.getElement(2)), // low value
              false, // exclude low value
              null, // high value
              true // include high value
              );
        } else if (property == MULGARA_BEFORE) {
          if (logger.isDebugEnabled()) {
            logger.debug("Evaluating " + constraint.getElement(0) + " before " + constraint.getElement(2));
          }


          // Evaluate "before" for xsd:date
          gNodeTuples = resolverSession.findStringPoolRange(
              null, // lowest value
              true, // include low value
              getBoundDate(constraint.getElement(2)), // high value
              false // exclude high value
              );
        } else if (property == MULGARA_AFTER) {
          // Evaluate "less than" for xsd:double
          if (logger.isDebugEnabled()) {
            logger.debug("Evaluating " + constraint.getElement(0) + " after " + constraint.getElement(2));
          }

          // Evaluate "after" for xsd:date
          gNodeTuples = resolverSession.findStringPoolRange(
              getBoundDate(constraint.getElement(2)), // low value
              false, // exclude low value
              null, // high value
              true // include high value
              );
        } else {
          throw new QueryException("Unsupported XSD predicate: " + property);
        }
        assert gNodeTuples != null;

        if (logger.isDebugEnabled()) {
          logger.debug("-- Renaming tuples of type: " + gNodeTuples.getClass());
        }

        // Generate the solution
        gNodeTuples.renameVariables(constraint);

        if (logger.isDebugEnabled()) {
          logger.debug("Finished renaming.");
          logger.debug("Evaluated " + constraint.getElement(0) +
                       " less than " + constraint.getElement(2) + ": " + gNodeTuples);
        }

        return new XSDResolution(constraint, gNodeTuples);
      } else if (!(constraint.getElement(0) instanceof Variable) &&
                 !(constraint.getElement(2) instanceof Variable)) {

        // Evaluate the cases where neither subject nor object are variables
        boolean condition;

        if (property == MULGARA_LT) {
          SPObject lhs = getBoundDouble(constraint.getElement(0));
          SPObject rhs = getBoundDouble(constraint.getElement(2));
          condition = lhs.compareTo(rhs) < 0;
        } else if (property == MULGARA_GT) {
          SPObject lhs = getBoundDouble(constraint.getElement(0));
          SPObject rhs = getBoundDouble(constraint.getElement(2));
          condition = lhs.compareTo(rhs) > 0;
        } else if (property == MULGARA_AFTER) {
          SPObject lhs = getBoundDate(constraint.getElement(0));
          SPObject rhs = getBoundDate(constraint.getElement(2));
          condition = lhs.compareTo(rhs) > 0;
        } else if (property == MULGARA_BEFORE) {
          SPObject lhs = getBoundDate(constraint.getElement(0));
          SPObject rhs = getBoundDate(constraint.getElement(2));
          condition = lhs.compareTo(rhs) < 0;
        } else {
          throw new QueryException("Invalid property: " + constraint);
        }

        return condition ? new XSDResolution(constraint, TuplesOperations.unconstrained())
                         : new XSDResolution(constraint, TuplesOperations.empty());
      } else if (constraint.getElement(0) instanceof Variable &&
                 constraint.getElement(2) instanceof Variable) {
        throw new QueryException("Can't resolve constraint with 2 variables: " + constraint);
      } else {
        throw new Error("Don't know how to handle constraint: " + constraint);
      }
    } catch (TuplesException e) {
      throw new QueryException("Couldn't query constraint", e);
    } catch (ParseException e) {
      throw new QueryException("Couldn't query constraint", e);
    } catch (StringPoolException e) {
      throw new QueryException("Couldn't query constraint", e);
    }
  }

  /**
   * Gets the BoundDate attribute of the XSDTuples object
   *
   * @param constraintElement PARAMETER TO DO
   * @return The BoundDate value
   * @throws ParseException EXCEPTION TO DO
   * @throws QueryException EXCEPTION TO DO
   * @throws StringPoolException EXCEPTION TO DO
   */
  private SPObject getBoundDate(ConstraintElement constraintElement)
                                throws ParseException, QueryException, StringPoolException {

    if (constraintElement instanceof LocalNode) {
      SPObject spo = resolverSession.findStringPoolObject(((LocalNode)constraintElement).getValue());
      if (PERMISSIVE_PARAMETER_KLUDGE && spo.getTypeCategory() == TypeCategory.UNTYPED_LITERAL) {
        // Try to parse the string as a date or a dateTime.
        try {
          return resolverSession.getSPObjectFactory().newSPTypedLiteral(spo.getLexicalForm(), XSD.DATE_URI);
        } catch (IllegalArgumentException e) {
          try {
            return resolverSession.getSPObjectFactory().newSPTypedLiteral(spo.getLexicalForm(), XSD.DATE_TIME_URI);
          } catch (IllegalArgumentException e2) {
            // fall through and let the string be returned unchanged.
          }
        }
      }
      return spo;
    } else {
      throw new Error("Unsupported constraint element: " + constraintElement);
    }
  }

  /**
   * Gets the BoundDouble attribute of the XSDTuples object
   *
   * @param constraintElement PARAMETER TO DO
   * @return The BoundDouble value
   * @throws QueryException EXCEPTION TO DO
   * @throws StringPoolException EXCEPTION TO DO
   */
  private SPObject getBoundDouble(ConstraintElement constraintElement) throws QueryException, StringPoolException {
    if (constraintElement instanceof LocalNode) {
      SPObject spo = resolverSession.findStringPoolObject(((LocalNode) constraintElement).getValue());
      if (logger.isDebugEnabled()) {
        logger.debug("!! Local node constraint element: " + ((LocalNode) constraintElement).getValue());
        logger.debug("!! SPObject returned: " + spo);
      }

      if (PERMISSIVE_PARAMETER_KLUDGE && spo.getTypeCategory() == TypeCategory.UNTYPED_LITERAL) {
        // Try to parse the string as a Double.
        try {
          return resolverSession.getSPObjectFactory().newSPTypedLiteral(spo.getLexicalForm(), XSD.DOUBLE_URI);
        } catch (NumberFormatException e) {
          // fall through and let the string be returned unchanged.
        }
      }

      return spo;
    } else {
      throw new Error("Unsupported constraint element: " + constraintElement);
    }
  }

  public void abort() {}
}
