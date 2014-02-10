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

/**
 * TQL answer.
 *
 * An answer is a set of solutions, where a solution is a mapping of
 * {@link Variable}s to {@link Value}s.
 *
 * @created 2001-07-31
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/28 00:28:37 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001-2003 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface Answer extends Cursor, Cloneable {

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Object clone();

  /**
   * Return the object at the given index (at the given row).
   * <p>
   *   The object returned will be one of:
   * </p>
   * <ul>
   *   <li>{@link org.jrdf.graph.Literal} - for literals;</li>
   *   <li>{@link org.jrdf.graph.BlankNode} - for blank nodes;</li>
   *   <li>{@link org.jrdf.graph.URIReference} - for a named resource;</li>
   *   <li>{@link org.mulgara.query.Answer} - where the column is the result of a
   *     subquery;</li>
   *   <li><code>null</code> - if this row solution is independent of the column
   *     variable.</li>
   * </ul>
   *
   * @param column  column numbering starts from zero
   * @return the value at the given index
   * @throws SQLException on failure
   * @throws TuplesException EXCEPTION TO DO
   */
  public Object getObject(int column) throws TuplesException;

  /**
   * Return the object at the given column name (at the given row).
   * <p>
   *   The object returned will be one of:
   * </p>
   * <ul>
   *   <li>{@link org.jrdf.graph.Literal} - for literals;</li>
   *   <li>{@link org.jrdf.graph.BlankNode} - for blank nodes;</li>
   *   <li>{@link org.jrdf.graph.URIReference} - for a concrete resoures;</li>
   *   <li>{@link org.mulgara.query.Answer} - where the column is the result of a subquery.</li>
   * </ul>
   *
   * @param columnName the index of the object to retrieve
   * @return the value at the given index
   * @throws SQLException on failure
   * @throws TuplesException EXCEPTION TO DO
   */
  public Object getObject(String columnName) throws TuplesException;
}
