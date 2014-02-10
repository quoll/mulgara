/**
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */
package org.mulgara.query.filter;

import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;


/**
 * A context for processing filters and resolving variables. Implementing classes are immutable.
 *
 * @created Mar 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public interface Context {

  /** An invalid index to indicate that a variable is not bound */
  static final int NOT_BOUND = -1;

  /**
   * Gets the current binding to a local value (a long) for a given internal column number.
   * @return the value in the column number specified in the current context.
   */
  public long getColumnValue(int columnNumber) throws QueryException;

  
  /**
   * Tests if a given column is bound in the current context.
   * @return <code>true</code> iff the column exists and is bound.
   */
  public boolean isBound(int columnNumber) throws QueryException;

  /**
   * Returns the internal value used to indicate an unbound variable.
   * This is to facilitate faster lookups, while still keeping the value
   * in Tuples opaque.
   * @return The value representing an unbound value after looking it up.
   */
  public long getUnboundVal();
  
  /**
   * Gets the internal column number for a column with the given name
   * @param name The name of the column to search for.
   * @return The column number for the column with the given name.
   */
  public int getInternalColumnIndex(String name);

  /**
   * Globalize a gNode into a data object.
   * @param gNode The graph node to globalize
   * @return A {@link org.jrdf.graph.Node} for the given graph node.
   * @throws QueryException If a globalize exception is encountered.
   */
  public Node globalize(long gNode) throws QueryException;
  
  /**
   * Localize a data object into a gNode.
   * @param o The object to localize.
   * @return a gNode representing the node.
   * @throws QueryException If a localize exception is encountered.
   */
  public long localize(Node node) throws QueryException;

}
