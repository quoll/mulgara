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

package org.mulgara.query;


/**
 * A variable and its ordering, for composing <code>ORDER BY</code> clauses.
 *
 * @created 2002-02-28
 *
 * @author <a href="http://staff.pisoftware.com/david">David Makepeace</a>
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface Constraint extends ConstraintExpression {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.l
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 8168059073438682618L;

  /**
   * Get a constraint element by index.
   *
   * @param index The constraint element to retrieve, from 0 to 3.
   * @return The constraint element referred to by index.
   */
  public ConstraintElement getElement(int index);

  /**
   * Get the model element of this constraint.
   */
  public ConstraintElement getModel();

  /**
   * Returns true if there is are repeating constraint elements.
   *
   * For example, <code>$s $p $s</code>.
   *
   * @return true if there is are repeating constraint elements
   */
  public boolean isRepeating();
}
