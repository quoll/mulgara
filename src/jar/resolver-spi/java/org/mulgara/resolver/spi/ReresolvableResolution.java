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

import java.util.Map;

import org.mulgara.query.ConstraintElement;
import org.mulgara.query.TuplesException;

/**
 * A Resolution that can be reresolved cheaper than the cost of joining
 * against an equivalent set of assignments.
 *
 * @created 2004-04-22
 * @author <a href="http://staff.tucanatech.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface ReresolvableResolution extends Resolution
{
  /**
   * Reresolve resolution given additional knowledge derived from other constraints in the query.
   *
   * Note: assign/x and Resolution &lt;=&gt; assign/x and Resolution.reresolve(x).
   *
   * Reresolve is analogous to beta-reduction in the lambda calculus, it is defined as
   * project({this.vars - bindings.vars}, restrict(bindings, this))
   *
   * @param bindings map of Variable to Long that defines the variable bindings known so far.
   * @return null if resolution was not able to be simplified using bindings otherwise a new 
   *         ReresolvableResolution with any variables bound in the reresolution removed.
   */
  public ReresolvableResolution reresolve(Map<? extends ConstraintElement,Long> bindings) throws TuplesException;
}
