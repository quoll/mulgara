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
 *   03-05-2005:
 *      The Australian Commonwealth Government
 *      Department of Defense
 *    Developed by Netymon Pty Ltd
 *    under contract 4500430665
 *    contributed to the Mulgara Project under the
 *      Mozilla Public License version 1.1
 *    per clause 4.1.3 of the above contract.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.spi;

// Java 2 standard packages
import java.util.Map;

// Local packages
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.Value;
import org.mulgara.query.Variable;

/**
 * Instances of this interface define how to bind variables to known 
 * values.  
 *
 * This is used to implement Subqueries and Aggregates which have specific bindings
 * imported
 *
 * Note: This is not an identity operation like reresolve is.  Subqueries are evaluated in
 * a series of evaluation environments, similar to a closure.  Each result in the outer query
 * is an independent evaluation environment, and hence the appropriate analogy is
 * beta-reduction, NOT equivalence-transformation.
 *
 * @created 2005-05-05
 * @author <a href="http://netymon.com/people/staff/andrae">Andrae Muys</a>
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/05/06 04:07:58 $ by $Author: amuys $
 * @maintenanceAuthor $Author: amuys $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface ConstraintBindingHandler
{
  /**
   * Rebind ConstraintExpression given a known set of variable bindings.
   *
   */
  public ConstraintExpression bindVariables(Map<Variable,Value> bindings,
      ConstraintExpression constraintExpr) throws Exception;
}
