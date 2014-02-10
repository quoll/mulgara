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
 * The Initial Developer of the Original Code is Andrew Newman
 * Copyright (C) 2005. All Rights Reserved.
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

// Jena
import com.hp.hpl.jena.shared.JenaException;

/**
 * Contains a page of results retrieved from an
 * {@link com.hp.hpl.jena.util.iterator.ClosableIterator} object.
 *
 * @author Andrew Newman
 *
 * @created 2005-01-24
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/01/27 11:21:21 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy; 2005 Andrew Newman
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface IteratorPage extends Serializable {

  /** The name of the property to read to get the page size */
  public static final String PAGE_SIZE_PROPERTY = "mulgara.rmi.prefetchsize";

  /** The default number of rows to use for a page */
  public static final int DEFAULT_PAGE_SIZE = 1000;

  /**
   * Move the internal state of this object onto the next row in the iterator.
   *
   * @return True if there is a new row.  False if there is no data left.
   * @throws JenaException If there was an error in a wrapped iterator.
   */
  public boolean nextInPage() throws JenaException;

  /**
   * Gets the data for a particular column from the current row.
   *
   * @param column The column number of the data to retrieve.
   * @return The object found at the requested location.
   * @throws JenaException If there was an error in a wrapped iterator.
   */
  public Object getObjectFromPage(int column) throws JenaException;


  /**
   * Gets the data for a column of a particular name from the current row.
   *
   * @param name The column name of the data to retrieve.
   * @return The object found at the requested location.
   * @throws JenaException If there was an error in a wrapped iterator.
   */
  public Object getObjectFromPage(String name) throws JenaException;


  /**
   * Retrieves the number of rows in this page.
   *
   * @return The number of rows in the current page, this is less than or equal to the page size.
   * @throws JenaException If there was an error in a wrapped iterator.
   */
  public long getPageSize() throws JenaException;


  /**
   * Indicates that this is the last page constructable for the current answer.
   *
   * @return true if there are no more pages after this one.
   */
  public boolean isLastPage();

}

