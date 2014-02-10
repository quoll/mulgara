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
package org.mulgara.resolver.spi;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.filter.Context;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.Tuples;


/**
 * A {@link org.mulgara.query.filter.Context} based on {@link org.mulgara.store.tuples.Tuples}.
 * This is used to resolve values out of a given Tuples in a given Context. The Tuples used
 * must be the Tuples being iterated on by the Filter, else synchronization will be lost
 * and the results will be wrong.
 *
 * @created Mar 24, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class TuplesContext implements Context {

  /** The logger. */
  final static Logger logger = Logger.getLogger(TuplesContext.class.getName());

  /** The tuples containing variables and bindings. */
  private final Tuples tuples;

  /** The session used for globalizing info. */
  private final ResolverSession session;

  /**
   * Creates a new context for processing a filter.
   * @param tuples The variables and bindings to use in filtering. Not all variables need be bound.
   * @param session The session used for globalizing the local data found in the filter.
   */
  public TuplesContext(Tuples tuples, ResolverSession session) {
    this.tuples = tuples;
    this.session = session;
  }


  /**
   * Creates a new context for processing a filter, based on an existing context and a new Tuples.
   * @param tuples The variables and bindings to use in filtering. Not all variables need be bound.
   * @param otherContext An existing context to get session information from.
   */
  public TuplesContext(Tuples tuples, TuplesContext otherContext) {
    this.tuples = tuples;
    this.session = otherContext.session;
  }


  /**
   * Gets the current binding to a local value (a long) for a given internal column number.
   * @return the value in the column number specified in the current context.
   */
  public long getColumnValue(int columnNumber) throws QueryException {
    try {
      return tuples.getRawColumnValue(columnNumber);
    } catch (Exception te) {  // TuplesException
      throw new QueryException("Error resolving value", te);
    }
  }

  
  /**
   * Tests if a given column is bound in the current context.
   * @return <code>true</code> iff the column exists and is bound.
   */
  public boolean isBound(int columnNumber) throws QueryException {
    try {
      return columnNumber != NOT_BOUND && tuples.getRawColumnValue(columnNumber) != Tuples.UNBOUND;
    } catch (Exception te) {  // TuplesException
      logger.error("Exception getting column value", te);
      throw new QueryException("Error resolving column", te);
    }
  }

  
  /**
   * Gets the internal column number for a column with the given name
   * @param name The name of the column to search for.
   * @return The column number for the column with the given name.
   */
  public int getInternalColumnIndex(String name) {
    try {
      return tuples.getColumnIndex(new Variable(name));
    } catch (Exception te) {  // TuplesException
      return NOT_BOUND;
    }
  }

  /**
   * Globalize a gNode into a data object.
   * @param gNode The graph node to globalize
   * @return A {@link org.jrdf.graph.Node} for the given graph node.
   * @throws QueryException If a globalize exception is encountered.
   */
  public Node globalize(long gNode) throws QueryException {
    try {
      return session.globalize(gNode);
    } catch (GlobalizeException te) {
      throw new QueryException("Unable to globalize id <" + gNode + ">", te);
    }
  }


  /**
   * Localize a data object into a gNode.
   * @param o The object to localize.
   * @return a gNode representing the node.
   * @throws QueryException If a localize exception is encountered.
   */
  public long localize(Node node) throws QueryException {
    try {
      return session.localize(node);
    } catch (LocalizeException te) {
      throw new QueryException("Unable to localize: " + node, te);
    }
  }


  /**
   * @see org.mulgara.query.filter.Context#getUnboundVal()
   */
  public long getUnboundVal() {
    return Tuples.UNBOUND;
  }

  public boolean equals(Object o) {
    return this == o;
  }

  public int hashCode() {
    return super.hashCode();
  }
}
