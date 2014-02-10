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

package org.mulgara.util;

import java.util.ArrayList;

/**
 * This class is a kludge to get <code>public</code> access to the {@link
 * ArrayList#removeRange} method.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Andrew Newman
 *
 * @created 2003-09-11
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:29 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RowList<T> extends ArrayList<T> {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = -9156982465064168124L;

  /**
   * Create a row list - null constructor.
   */
  public RowList() {

    // Do nothing
  }

  /**
   * Create a row list with the given size.
   *
   * @param size the initial size of the row.
   */
  public RowList(int size) {
    super(size);
  }

  /**
   * Removes the elements between two indexes into the array.
   *
   * @param begin the start of where the removal should take place.
   * @param end the end of where the removal should take place.
   */
  public void removeRange(int begin, int end) {

    super.removeRange(begin, end);
  }
}
