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

package org.mulgara.resolver.spi;

import java.net.URI;

// Third party packages
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Node;

// Local packages
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.tuples.Tuples;

/**
 * A {@link Resolver}'s view of a particular session.
 *
 * This is the only way a Resolver can access the StringPool.
 *
 * @created 2004-03-28
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $
 * @maintenanceAuthor $Author: newmana $
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface ResolverSession extends BackupRestoreSession {
  /**
   * Convert session-local node numbers to globally valid RDF nodes.
   *
   * Every time a blank local node is globalized, it produces a distinct
   * {@link BlankNode} instance.  Repeated globalizations of the same local
   * blank node will not be equal.
   *
   * @param node  a node local to this {@link ResolverSession}
   * @return the global RDF {@link Node} corresponding to the local
   *   <var>node</var>, never <code>null</code>
   * @throws GlobalizeException if the lookup fails
   * @throws IllegalArgumentException if the <var>node</var> is
   *   {@link NodePool#NONE}
   */
  public Node globalize(long node) throws GlobalizeException;

  /**
   * Lookup a node in the current session.
   * Throws an exception if the node is not found.
   *
   * @param node  an RDF {@link Node}, never <code>null</code>
   * @return the equivalent local node number of the RDF <var>node</var>
   * @throws LocalizeException if the lookup fails
   */
  public long lookup(Node node) throws LocalizeException;

  /**
   * Lookup a node in the current session.
   * Only looks in the Persistent String Pool.
   * Throws an exception if the node is not found.
   * Every time a {@link BlankNode} is localized, it produces a distinct local
   * node.  Repeated localizations of the same {@link BlankNode} will not be
   * equal.
   *
   * @param node  an RDF {@link Node}, never <code>null</code>
   * @return the equivalent local node number of the RDF <var>node</var>
   * @throws LocalizeException if the lookup fails
   */
  public long lookupPersistent(Node node) throws LocalizeException;

  /**
   * Lookup a node in the current session.
   * Inserts the node in the Temporary String-pool if the node is not found.
   * Throws an exception only on StringPool failure.
   *
   * Every time a {@link BlankNode} is localized, it produces a distinct local
   * node.  Repeated localizations of the same {@link BlankNode} will not be
   * equal.
   *
   * @param node  an RDF {@link Node}, never <code>null</code>
   * @return the equivalent local node number of the RDF <var>node</var>
   * @throws LocalizeException if the lookup fails
   */
  public long localize(Node node) throws LocalizeException;

  /**
   * Lookup a node in the current session.
   * Inserts the node in the Persistent String-pool if the node is not found.
   * Throws an exception only on StringPool failure.
   *
   * Every time a {@link BlankNode} is localized, it produces a distinct local
   * node.  Repeated localizations of the same {@link BlankNode} will not be
   * equal.
   *
   * @param node  an RDF {@link Node}, never <code>null</code>
   * @return the equivalent local node number of the RDF <var>node</var>
   * @throws LocalizeException if the lookup fails
   */
  public long localizePersistent(Node node) throws LocalizeException;

  /**
   * Allocates a new blank node in a session.  Unlike localization, blank nodes
   * do not need to be inserted into the string pool.
   * 
   * @return A new local node number for this session.
   * @throws NodePoolException 
   */
  public long newBlankNode() throws NodePoolException;
  
  /**
   * Get a range of nodes from the string pools used in the current session.
   *
   * @param lowValue The low end of the range to select. Pass <code>null</code>
   * to specify that the range starts at the lowest SPObject for the .
   * @param inclLowValue <code>true</code> if range includes the
   * <var>lowValue</var> object.  Ignored if <var>lowValue</var> is
   * <code>null</code>.
   * @param highValue The high end of the range to select. Pass
   * <code>null</code> to specify that the range ends at the highest SPObject.
   * @param inclHighValue <code>true</code> if range includes the
   * <var>highValue</var> object.  Ignored if <var>highValue</var> is
   * <code>null</code>.
   * @return the list of string pool nodes as a Tuples with one column.
   * @throws StringPoolException if the SPObjects are not of the same type or
   * an internal error occurs.
   */
  public Tuples findStringPoolRange(
      SPObject lowValue, boolean inclLowValue,
      SPObject highValue, boolean inclHighValue
  ) throws StringPoolException;

  /**
   * Get all string pool nodes which match a particular type.
   *
   * @param typeCategory The type category of SPObjects to select.  Pass
   * <code>null</code> to match all SPObjects.
   * @param typeURI The type URI of a typed literal or <code>null</code> to
   * match all SPObjects with the TypeCategory specified by
   * <var>typeCategory</var>.  This parameter must be <code>null</code> unless
   * <var>typeCategory</var> is equal to SPObject.TypeCategory.TYPED_LITERAL.
   * @throws StringPoolException if an internal error occurs.
   */
  public Tuples findStringPoolType(
      SPObject.TypeCategory typeCategory, URI typeURI
  ) throws StringPoolException;

  /**
   * Find a single object in the string pools, or <code>null</code> if not found.
   *
   * @param gNode A graph node to search for within the pool.
   * @return the SPObject corresponding to <var>gNode</var>, or
   * <code>null</code> if no such graph node is in the pool.
   * @throws StringPoolException if an internal error occurs.
   */
  public SPObject findStringPoolObject(long gNode) throws StringPoolException;

  /**
   * Retrieve the SPObject factory from the stringpool to allow for the creation
   * of new SPObjects.
   *
   * @return The factory to allow for creation of SPObjects
   */
  public SPObjectFactory getSPObjectFactory();

  /**
   * DO NOT USE: This is solely for the use of RestoreOperation.
   * Will be removed from this interface as soon as RestoreOperation can be
   * refactored to no longer require it.
   */
  public long findGNode(SPObject spObject) throws StringPoolException;

}
