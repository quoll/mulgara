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

package org.mulgara.itql;

// Java 2 standard packages
import java.net.*;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;

// Automatically generated packages (SableCC)
import org.mulgara.itql.node.AAndModelTerm;
import org.mulgara.itql.node.AExpressionModelFactor;
import org.mulgara.itql.node.AOrModelExpression;
import org.mulgara.itql.node.AFactorModelTerm;
import org.mulgara.itql.node.AResourceModelFactor;
import org.mulgara.itql.node.ATermModelExpression;
import org.mulgara.itql.node.PModelExpression;
import org.mulgara.itql.node.PModelFactor;
import org.mulgara.itql.node.PModelTerm;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.GraphIntersection;
import org.mulgara.query.GraphResource;
import org.mulgara.query.GraphUnion;
import org.mulgara.query.QueryException;
import org.mulgara.util.ServerURIHandler;
import org.mulgara.util.URIUtil;

/**
 * Builds model expressions using input from the iTQL command interpreter.
 *
 * @created 2001-09-11
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/04/04 11:30:11 $ by $Author: tomadams $
 *
 * @maintenanceAuthor $Author: tomadams $
 *
 * @copyright &copy;2001-2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class GraphExpressionBuilder {

  //
  // Constants
  //

  /**
   * the category to log to
   */
  private final static Logger logger =
    Logger.getLogger(GraphExpressionBuilder.class.getName());

  //
  // Public API (methods overridden from ExpressionBuilder)
  //

  /**
   * Builds a {@link org.mulgara.query.GraphExpression} object from a {@link
   * org.mulgara.itql.node.PModelExpression}, using an <code>aliasMap</code>
   * to resolve aliases.
   *
   * TODO: aliasMap is currently ignored!
   * 
   * @param aliasMap the map from targets to aliases
   * @param expression a model expression from the parser
   * @return RETURNED VALUE TO DO
   * @throws QueryException if <code>rawModelExpression</code> does not
   *      represent a valid query
   * @throws URISyntaxException if the <code>rawModelExpression</code> contains
   *      a resource whose text violates <a
   *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
   */
  public static GraphExpression build(Map<String,URI> aliasMap,
    PModelExpression expression) throws QueryException, URISyntaxException {

    // validate aliasMap parameter
    if (aliasMap == null) {

      throw new IllegalArgumentException("Null \"aliasMap\" parameter");
    }

    // end if
    // validate expression parameter
    if (expression == null) {

      throw new IllegalArgumentException("Null \"expression\" parameter");
    }

    // end if
    // logger that we're building a model expression
    if (logger.isDebugEnabled()) {
      logger.debug("Building model expression from " + expression);
    }

    // build the model expression from the parser input
    GraphExpression graphExpression = buildModelExpression(expression, aliasMap);

    // logger that we've building successfully built a model expression
    if (logger.isDebugEnabled()) {
      logger.debug("Successfully built model expression from " + expression);
    }

    // return the model expression
    return graphExpression;
  }

  // build()
  //
  // Internal methods
  //

  /**
   * Recursively builds a {@link org.mulgara.query.GraphExpression} from a
   * {@link org.mulgara.itql.node.PModelExpression}.
   *
   * @param rawModelExpression a raw model expression from the parser
   * @return a {@link org.mulgara.query.GraphExpression} suitable for use in
   *      creating a {@link org.mulgara.query.Query}
   * @throws QueryException if <code>rawModelExpression</code> does not
   *      represent a valid query
   * @throws URISyntaxException if the <code>rawModelExpression</code> contains
   *      a resource whose text violates <a
   *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
   */
  private static GraphExpression buildModelExpression(
    PModelExpression rawModelExpression, Map<String,URI> aliasMap)
    throws QueryException, URISyntaxException {

    // validate the rawModelExpression parameter
    if (rawModelExpression == null) {

      throw new IllegalArgumentException("Null \"rawModelExpression\" " +
        "parameter");
    }

    // end if
    // logger that we're building a model expression
    if (logger.isDebugEnabled()) {
      logger.debug("Building model expression from " + rawModelExpression);
    }

    // create a new model expression that we can return
    GraphExpression graphExpression = null;

    // drill down to find its constituents
    if (rawModelExpression instanceof AOrModelExpression) {

      // logger that we've found a OR model expression
      if (logger.isDebugEnabled()) {
        logger.debug("Found OR model expression " + rawModelExpression);
      }

      // get the OR model expression
      PModelExpression orModelExpression =
        ((AOrModelExpression) rawModelExpression).getModelExpression();

      // get the model term
      PModelTerm modelTerm =
        ((AOrModelExpression) rawModelExpression).getModelTerm();

      // logger that we've found the operands of the union
      if (logger.isDebugEnabled()) {
        logger.debug("Recursing with model expression " + orModelExpression +
            " & model term " + modelTerm);
      }

      // get the LHS and RHS operands of the union
      GraphExpression lhs = buildModelExpression(orModelExpression, aliasMap);
      GraphExpression rhs = buildModelExpression(modelTerm, aliasMap);

      // logger that we've resolved the operands
      if (logger.isDebugEnabled()) {
        logger.debug("Resolved LHS union operand " + lhs);
        logger.debug("Resolved RHS union operand " + rhs);
      }

      // apply the union
      graphExpression = new GraphUnion(lhs, rhs);
    } else if (rawModelExpression instanceof ATermModelExpression) {

      // logger that we've got a term model expression
      if (logger.isDebugEnabled()) {
        logger.debug("Found term model expression " + rawModelExpression);
      }

      // get the model term
      PModelTerm modelTerm = ((ATermModelExpression)rawModelExpression).getModelTerm();

      // logger that we're about to resolve the term into an expression
      if (logger.isDebugEnabled()) {
        logger.debug("Recursing with model term " + modelTerm);
      }

      // drill down into the model term
      graphExpression = buildModelExpression(modelTerm, aliasMap);
    }

    // end if
    // we should not be returning null
    if (graphExpression == null) {

      throw new QueryException("Unable to parse ITQL model expression " +
        "into a valid model expression");
    }

    // end if
    // logger that we've created a model expression
    if (logger.isDebugEnabled()) {
      logger.debug("Created model expression " + graphExpression);
    }

    // return the built up expression
    return graphExpression;
  }

  // buildModelExpression()

  /**
   * Recursively builds a {@link org.mulgara.query.GraphExpression} from a
   * {@link org.mulgara.itql.node.PModelTerm}.
   *
   * @param rawModelTerm a raw model term from the parser
   * @return a {@link org.mulgara.query.GraphExpression} suitable for use in
   *      creating a {@link org.mulgara.query.Query}
   * @throws QueryException if <code>rawModelExpression</code> does not
   *      represent a valid query
   * @throws URISyntaxException if the <code>rawModelExpression</code> contains
   *      a resource whose text violates <a
   *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
   */
  private static GraphExpression buildModelExpression(
      PModelTerm rawModelTerm, Map<String,URI> aliasMap
    ) throws QueryException, URISyntaxException {

    // validate the rawModelTerm parameter
    if (rawModelTerm == null) {

      throw new IllegalArgumentException("Null \"rawModelTerm\" " +
        "parameter");
    }

    // end if
    // logger that we're building a model expression
    if (logger.isDebugEnabled()) {
      logger.debug("Building model expression from " + rawModelTerm);
    }

    // create a new model expression that we can return
    GraphExpression graphExpression = null;

    // drill down into the model term
    if (rawModelTerm instanceof AFactorModelTerm) {

      // logger that we've got a factor model term
      if (logger.isDebugEnabled()) {
        logger.debug("Found factor contraint term " + rawModelTerm);
      }

      // get the model factor
      PModelFactor modelFactor = ((AFactorModelTerm) rawModelTerm).getModelFactor();

      // logger that we're recursing with a model part
      if (logger.isDebugEnabled()) {
        logger.debug("Recursing with model part " + modelFactor);
      }

      // drill down into the model part
      graphExpression = buildModelExpression(modelFactor, aliasMap);

    } else if (rawModelTerm instanceof AAndModelTerm) {

      // logger that we've got a AND model term
      if (logger.isDebugEnabled()) {
        logger.debug("Found AND contraint term " + rawModelTerm);
      }

      // get the model term
      PModelTerm modelTerm = ((AAndModelTerm)rawModelTerm).getModelTerm();

      // get the model part
      PModelFactor modelFactor = ((AAndModelTerm)rawModelTerm).getModelFactor();

      // logger that we've found the operands of the union
      if (logger.isDebugEnabled()) {
        logger.debug("Recursing with model term " + modelTerm +
            " & model factor " + modelFactor);
      }

      // get the LHS and RHS operands of the intersection
      GraphExpression lhs = buildModelExpression(modelTerm, aliasMap);
      GraphExpression rhs = buildModelExpression(modelFactor, aliasMap);

      // logger that we've resolved the operands
      if (logger.isDebugEnabled()) {
        logger.debug("Resolved LHS intersection operand " + lhs);
        logger.debug("Resolved RHS intersection operand " + rhs);
      }

      // apply the intersection
      graphExpression = new GraphIntersection(lhs, rhs);
    }

    // end if
    // we should not be returning null
    if (graphExpression == null) {

      throw new QueryException("Unable to parse ITQL model term into a valid model expression");
    }

    // end if
    // logger that we've created a model expression
    if (logger.isDebugEnabled()) {
      logger.debug("Created model expression " + graphExpression);
    }

    // return the built up expression
    return graphExpression;
  }

  // buildModelExpression()

  // buildModelExpression()

  /**
   * Recursively builds a {@link org.mulgara.query.GraphExpression} from a
   * {@link org.mulgara.itql.node.PModelFactor}.
   *
   * @param rawModelFactor a raw model factor from the parser
   * @return a {@link org.mulgara.query.GraphExpression} suitable for use in
   *      creating a {@link org.mulgara.query.Query}
   * @throws QueryException if <code>rawModelExpression</code> does not
   *      represent a valid query
   * @throws URISyntaxException if the <code>rawModelExpression</code> contains
   *      a resource whose text violates <a
   *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
   */
  private static GraphExpression buildModelExpression(
        PModelFactor rawModelFactor, Map<String,URI> aliasMap
      ) throws QueryException, URISyntaxException {

    // validate the rawModelFactor parameter
    if (rawModelFactor == null) {

      throw new IllegalArgumentException("Null \"rawModelFactor\" parameter");
    }

    // end if
    // logger that we're building a model expression
    if (logger.isDebugEnabled()) {
      logger.debug("Building model expression from " + rawModelFactor);
    }

    // create a new model expression that we can return
    GraphExpression graphExpression = null;

    // drill down into the model term
    if (rawModelFactor instanceof AResourceModelFactor) {

      // logger that we've got a model model factor
      if (logger.isDebugEnabled()) {
        logger.debug("Found resource model factor " + rawModelFactor);
      }

      // get the resource
      String resource = ((AResourceModelFactor)rawModelFactor).getResource().getText();

      // logger that we've found a resource
      if (logger.isDebugEnabled()) {
        logger.debug("Found resource " + resource);
      }

      // this resource is what we're looking for
      URI modelURI = URIUtil.convertToURI(resource, aliasMap);
      graphExpression = new GraphResource(ServerURIHandler.removePort(modelURI));
    } else if (rawModelFactor instanceof AExpressionModelFactor) {

      // logger that we've got an expression model factor
      if (logger.isDebugEnabled()) {
        logger.debug("Found factor expression model factor " + rawModelFactor);
      }

      // get the model expression
      PModelExpression embeddedModelExpression = ((AExpressionModelFactor)rawModelFactor).getModelExpression();

      // logger that we're recursing with a model expression
      if (logger.isDebugEnabled()) {
        logger.debug("Recursing with model factor " + graphExpression);
      }

      // build the model expression
      graphExpression = buildModelExpression(embeddedModelExpression, aliasMap);
    }

    // end if
    // we should not be returning null
    if (graphExpression == null) {

      throw new QueryException("Unable to parse ITQL model factor " +
        "into a valid model expression");
    }

    // end if
    // logger that we've created a model expression
    if (logger.isDebugEnabled()) {
      logger.debug("Created model expression " + graphExpression);
    }

    // return the built up expression
    return graphExpression;
  }

  // buildModelExpression()
}


// GraphExpressionBuilder
