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
import java.io.Serializable;

// Mulgara
import org.mulgara.query.TuplesException;

/**
 * Contains a page of answers retrieved from an {@link org.mulgara.query.Answer} object.
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 *
 * @created 2004-03-26
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/01/27 11:55:18 $
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
public interface AnswerPage extends Serializable {
  /** The name of the property to read to get the page size */
  public static final String PAGE_SIZE_PROPERTY = "mulgara.rmi.prefetchsize";

  /** The default number of rows to use for a page */
  public static final int DEFAULT_PAGE_SIZE = 1000;

  /**
   * Move the internal state of this object onto the next row in the answer.
   * @return True if there is a new row.  False if there is no data left.
   * @throws TuplesException If there was an error in a wrapped answer.
   */
  public boolean nextInPage() throws TuplesException;


  /**
   * Gets the data for a particular column from the current row.
   * @param column The column number of the data to retrieve.
   * @return The object found at the requested location.
   * @throws TuplesException If there was an error in a wrapped answer.
   */
  public Object getObjectFromPage(int column) throws TuplesException;


  /**
   * Gets the data for a column of a particular name from the current row.
   * @param name The column name of the data to retrieve.
   * @return The object found at the requested location.
   * @throws TuplesException The name was not a valid column.
   */
  public Object getObjectFromPage(String name) throws TuplesException;


  /**
   * Retrieves the number of rows in this page.
   * @return The number of rows in the current page, this is less than or equal to the page size.
   * @throws TuplesException If there was an error in a wrapped answer.
   */
  public long getPageSize() throws TuplesException;


  /**
   * Resets the current row to the beginning.  Hopefully not needed, but included for completeness.
   * @throws TuplesException If there was an error in a wrapped answer.
   */
  public void beforeFirstInPage() throws TuplesException;


  /**
   * Indicates that this is the last page constructable for the current answer.
   *
   * @return true if there are no more pages after this one.
   */
  public boolean isLastPage();

}

