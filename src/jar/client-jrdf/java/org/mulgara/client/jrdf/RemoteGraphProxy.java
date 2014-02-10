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

package org.mulgara.client.jrdf;

//Java 2 packages
import java.util.Iterator;

// JRDF
import org.jrdf.graph.*;
import org.jrdf.util.ClosableIterator;

//local packages
import org.mulgara.client.jrdf.exception.JRDFClientException;
import org.mulgara.server.Session;

/**
 * A Remote JRDF Graph Proxy used on the client side to access data. Subset of
 * Graph (org.jrdf.graph.Graph).</p>
 *
 * <p>Data Access Object (DAO).</p>
 *
 * @created 2004-07-29
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/01/26 21:04:48 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface RemoteGraphProxy {

  /**
   * Test the graph for the occurrence of the triple.  A null value for any
   * of the parts of a triple are treated as unconstrained, any values will be
   * returned.
   *
   * @param triple The triple to find.
   * @return True if the triple is found in the graph, otherwise false.
   * @throws GraphException If there was an error accessing the graph.
   */
  public boolean contains(Triple triple) throws GraphException;

  /**
   * Returns an iterator to a set of statements that match a given subject,
   * predicate and object.  A null value for any of the parts of a triple are
   * treated as unconstrained, any values will be returned.
   *
   * @param triple The triple to find.
   * @throws GraphException If there was an error accessing the graph.
   */
  public ClosableIterator<Triple> find(Triple triple) throws GraphException;

  /**
   * Adds an iterator containing triples into the graph.
   *
   * @param triples The triple iterator.
   * @throws GraphExcepotion If the statements can't be made.
   */
  public void add(Iterator<Triple> triples) throws GraphException;

  /**
   * Removes an iterator containing triples from the graph.
   *
   * @param triples The triple iterator.
   * @throws GraphExcepotion If the statements can't be revoked.
   */
  public void remove(Iterator<Triple> triples) throws GraphException;

  /**
   * Returns the node factory for the graph, or creates one.
   *
   * @return the node factory for the graph, or creates one.
   */
  public GraphElementFactory getElementFactory();

  /**
   * Returns the number of triples in the graph.
   *
   * @return the number of triples in the graph.
   */
  public long getNumberOfTriples();

  /**
   * Returns true if the graph is empty i.e. the number of triples is 0.
   *
   * @return true if the graph is empty i.e. the number of triples is 0.
   */
  public boolean isEmpty();

  /**
   * Closes any underlying data source and any unclosed Iterators created by
   * the graph.
   *
   * @throws JRDFClientException
   */
  public void close() throws JRDFClientException;

  /**
   * Removes the iterator from the list to be closed with the graph.
   *
   * @param iter Iterator
   */
  public void unregister(Iterator<?> iter);

  /**
   * Factory method used to create an iterator for the Triples.
   *
   * @param triples Triple[]
   * @return ClientClosableIterator
   */
  public ClientClosableIterator<Triple> createClosableIterator(Triple [] triples);

  /**
   * Returns the session associated with the graph.
   *
   * @return the session associated with the graph.
   */
  public Session getSession();
}
