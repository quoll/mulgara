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

package org.mulgara.query;

import java.net.*;
import java.util.*;
import java.io.Serializable;

/**
 * An expression whose leaves are the {@link URL}s of RDF models.
 *
 * Currently this doesn't handle compound expressions, only simple models.
 *
 * @created 12/Aug/2001
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $ by $Author: newmana $
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface GraphExpression extends Cloneable, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = -7760184436366431348L;

  /**
   * Calculate the databases involved in this expression. Bear in mind that
   * different models may reside on the same database. The idea of this function
   * is to test for when only one database is involved, and we're able to
   * transmit a query to that database for remove processing.
   *
   * @return a set containing the {@link URI}s of the databases
   */
  public Set<URI> getDatabaseURIs();

  /**
   * Calculate the graphs involved in this expression.
   *
   * @return a set containing the {@link URI}s of the graphs
   */
  public Set<URI> getGraphURIs();

  /**
   * Clones sets of models in the rhs and lhs objects.
   */
  public Object clone();
}
