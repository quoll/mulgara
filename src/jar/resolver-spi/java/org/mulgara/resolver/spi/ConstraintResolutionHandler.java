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
import javax.transaction.xa.XAResource;
import java.net.URI;

// Local packages
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.QueryException;
import org.mulgara.resolver.spi.QueryEvaluationContext;
import org.mulgara.store.tuples.Tuples;

/**
 * Instances of this interface define resolution for each implementation of
 * ConstraintExpression.
 *
 * @created 2003-12-01
 * @author <a href="http://www.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.3 $
 * @modified $Date: 2005/05/03 08:11:44 $ 
 * @maintenanceAuthor $Author: amuys $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2003-2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface ConstraintResolutionHandler
{
  /**
   * Resolve this constraintExpression within a given queryContext.
   *
   * @param queryContext  the context of the query
   * @param modelExpr  the from clause of the query
   * @param constraintExpr the constraintExpression to resolve
   */
  public Tuples resolve(QueryEvaluationContext queryContext,
                        GraphExpression modelExpr,
                        ConstraintExpression constraintExpr) throws Exception;
}
