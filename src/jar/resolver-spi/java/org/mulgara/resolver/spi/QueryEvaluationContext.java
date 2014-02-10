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

package org.mulgara.resolver.spi;

// Java 2 standard packages
import java.util.*;

// Third party packages
// import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.Tuples;

/**
 * Context for evaluating FROM and WHERE clauses.
 *
 * @created 2004-05-06
 * @author <a href="http://www.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.11 $
 * @modified $Date: 2005/05/10 22:47:02 $
 * @maintenanceAuthor $Author: raboczi $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface QueryEvaluationContext {

  /**
   * Returns either a variable or the LocalNode local equivalent of the
   * ConstraintElement.
   *
   * @param constraintElement  a global constraint element
   * @return the localized equivalent to the global <var>constraintElement</var>
   */
  public ConstraintElement localize(ConstraintElement constraintElement)
    throws LocalizeException;

  public ConstraintElement globalize(ConstraintElement constraintElement)
    throws GlobalizeException;

  /**
   * Localize and resolve the leaf node of the <code>FROM</code> and
   * <code>WHERE</code> clause product.
   *
   * @param graphResource  the <code>FROM</code> clause to resolve, never
   *   <code>null</code>
   * @param constraint  the <code>WHERE</code> clause to resolve, which must
   *   have {@link Variable#FROM} as its fourth element, and never be
   *   <code>null</code>
   * @throws QueryException if resolution can't be obtained
   */
  public Tuples resolve(GraphResource graphResource, Constraint constraint) throws QueryException;

  public Tuples resolve(GraphExpression graphExpression, ConstraintExpression constraintExpression) throws QueryException;

  public ResolverSession getResolverSession();

  public List<Tuples> resolveConstraintOperation(GraphExpression modelExpr,
                                        ConstraintOperation constraintOper) throws QueryException;

  /**
   * Indicates that the query being run in this context should return distinct results.
   * @return If <code>true</code>, then return distinct results. Otherwise allow for duplicates.
   */
  public boolean isDistinctQuery();

  /**
   * Sets the "distinct" status of a query context, returning the previous value.
   * @param newValue The new value to set the distinct status to. 
   * @return The previous value of the distinct status.
   */
  public boolean setDistinctQuery(boolean newValue);

}
