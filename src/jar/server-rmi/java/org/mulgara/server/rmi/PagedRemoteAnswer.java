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

package org.mulgara.server.rmi;

// Java 2 standard packages
import java.rmi.*;
import java.util.*;

// Third party packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.util.MulgaraResultSet;

/**
 * Remote ITQL answer. An answer is a set of solutions, where a solution is a
 * mapping of {@link Variable}s to {@link org.mulgara.query.Value}s.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @created 2001-07-31
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:02 $
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
public interface PagedRemoteAnswer extends RemoteAnswer
{
  /**
   * Returns the page as given by the index into the result set.
   *
   * @param index PARAMETER TO DO
   * @return ResultSet a page of results.
   * @throws RemoteException
   * @throws TuplesException EXCEPTION TO DO
   */
  public MulgaraResultSet getPage(int index) throws TuplesException,
      RemoteException;

  /**
   * Returns the number of rows that are returned on a getResultSet call.
   *
   * @return the number of rows that are returned on a getResultSet call.
   * @throws RemoteException
   */
  public long getPageSize() throws RemoteException;

  /**
   * Returns the total number of pages that can be requested.
   *
   * @return the total number of pages that can be requested.
   * @throws RemoteException
   */
  public int getTotalNumberOfPages() throws RemoteException;
}
