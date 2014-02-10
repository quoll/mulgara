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

package org.mulgara.resolver.test;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.GraphExpression;
import org.mulgara.resolver.spi.ConstraintDescriptor;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.store.tuples.Tuples;

/**
 * @created 2005-05-04
 * @author <a href="http://netymon.com/people/staff/andrae">Andrae Muys</a>
 * @version $Revision: 1.4 $
 * @modified $Date: 2005/05/16 11:07:09 $ by $Author: amuys $
 * @maintenanceAuthor $Author: amuys $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class TestConstraintDescriptor implements ConstraintDescriptor {
  /** Logger */
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(TestConstraintDescriptor.class);


  public TestConstraintDescriptor() { }


  public Class<TestConstraint> getConstraintClass() {
    return TestConstraint.class;
  }


  public Constraint rewrite(ConstraintElement newModel, Constraint constraint) throws Exception {
    throw new IllegalStateException("Unable to rewrite model on TestConstraint");
  }


  public Tuples resolve(QueryEvaluationContext context, GraphExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
    if (constraintExpr instanceof TestConstraint) {
      // FIXME: Need to find a clean way of enlisting the resolver in the transaction.
      // This will involve changes to LocalQueryResolver (the QEC/context passed to this method).
      return new TestResolver(context.getResolverSession()).resolve((TestConstraint)constraintExpr);
    } else {
      throw new IllegalArgumentException("Attempt to resolve non TestConstraint with a TestConstraintDescriptor");
    }
  }
}
