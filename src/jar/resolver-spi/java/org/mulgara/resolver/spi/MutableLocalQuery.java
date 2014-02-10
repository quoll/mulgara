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
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 *   under contract 4500430665
 *   contributed to the Mulgara Project under the
 *     Mozilla Public License version 1.1
 *   per clause 4.1.3 of the above contract.
 *
 * Modified 2006-04 by Netymon Pty Ltd under contract
 *   4500507038.  Contributed to the Mulgara Project
 *   (per clause 4.1.4) undr the Mozilla Public License
 *   version 1.1 per clause 4.1.3 of the above contract.
 *   
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.spi;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.Query;

/**
 * A local-coordinate counterpart of a global-coordinate {@link Query}.
 *
 * Unlike {@link Query}, implementations of {@link MutableLocalQuery} are
 * mutable.  {@link SymbolicTransformation}s are supposed to be the only
 * classes to perform these mutations.
 *
 * @created 2005-05-17
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/05/19 08:43:59 $ by $Author: raboczi $
 * @maintenanceAuthor $Author: raboczi $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface MutableLocalQuery
{
  /**
   * @return the <code>WHERE</code> clause of the query, expressed in local
   *   coordinates
   */
  public ConstraintExpression getConstraintExpression();

  /**
   * @param constraintExpression   a new <code>WHERE</code> clause, which must
   *   be expressed in local coordinates
   * @throws IllegalArgumentException if <var>constraintExpression</var> is
   *   <code>null</code>
   * @throws IllegalStateException if modification is not allowed at this time
   */
  public void setConstraintExpression(ConstraintExpression constraintExpression);
}
