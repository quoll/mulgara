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
 *   The copywrite in this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 *   under contract 4500430665
 *   contributed to the Mulgara Project under the
 *     Mozilla Public License version 1.1
 *   per clause 4.1.3 of the above contract.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Java 2 standard packages
// Java 2 enterprise packages
// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.GraphExpression;
import org.mulgara.resolver.spi.ConstraintDescriptor;
import org.mulgara.resolver.spi.ConstraintGraphRewrite;
import org.mulgara.resolver.spi.ConstraintResolutionHandler;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.store.tuples.Tuples;

/**
 * A {@link ConstraintDescriptor} that aggregates the various handlers.
 *
 * @created 2005-05-03
 * @author <a href="http://netymon.com/people/staff/andrae">Andrae Muys</a>
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/05/03 08:09:08 $ by $Author: amuys $
 * @maintenanceAuthor $Author: amuys $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class PrimitiveConstraintDescriptor implements ConstraintDescriptor {
  /** Logger.  */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(PrimitiveConstraintDescriptor.class.getName());

  private Class<? extends Constraint> constraintClass;
  private ConstraintResolutionHandler resolutionHandler;
  private ConstraintGraphRewrite rewriteHandler;

  /**
   * @param constraintClass  the class of the constraint described
   * @param resolutionHandler the resolutionHandler for the constraint or
   *         null if resolution is an error.
   * @param rewriteHandler the modelRewrite Handler for the constraint or
   *         null if rewriting is an error.
   * @throws IllegalArgumentException if <var>constraintClass</var> is <code>null</code>
   */
  PrimitiveConstraintDescriptor(Class<? extends Constraint> constraintClass,
        ConstraintResolutionHandler resolutionHandler, ConstraintGraphRewrite rewriteHandler) {

    // Validate parameters
    if (!ConstraintExpression.class.isAssignableFrom(constraintClass)) {
      throw new IllegalArgumentException("'constraintClass' not a ConstraintExpression");
    }

    this.constraintClass = constraintClass;
    this.resolutionHandler = resolutionHandler;
    this.rewriteHandler = rewriteHandler;
  }


  public Class<? extends Constraint> getConstraintClass() {
    return constraintClass;
  }


  public Constraint rewrite(ConstraintElement newModel, Constraint constraint) throws Exception {
    if (rewriteHandler == null) {
      throw new IllegalStateException("Attempt to rewrite model for " + constraintClass + " no handler registered");
    }
    return rewriteHandler.rewrite(newModel, constraint);
  }


  public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
    if (rewriteHandler == null) {
      throw new IllegalStateException("Attempt to resolve model for " + constraintClass + " no handler registered");
    }
    return resolutionHandler.resolve(context, modelExpr, constraintExpr);
  }
}
