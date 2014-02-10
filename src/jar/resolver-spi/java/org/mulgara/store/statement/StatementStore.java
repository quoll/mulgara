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

package org.mulgara.store.statement;

// Locally written packages
import org.mulgara.query.Variable;
import org.mulgara.store.tuples.StoreTuples;

/**
 * A graph of labeled, directed edges. Equivalently, a set of 4-tuples of nodes
 * where nodes are 64-bit values (Java <code>long</code>s).
 *
 * @created 2001-07-12
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:52 $
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
public interface StatementStore {

  /**
   * Description of the Field
   */
  public Variable[] VARIABLES = {
      new Variable("Subject"), new Variable("Predicate"),
      new Variable("Object"), new Variable("Meta")
  };

  //
  // Mutators
  //

  /**
   * Adds a new triple to the graph if it doesn't already exist.
   *
   * @param node0 the first element of the new triple
   * @param node1 the second element of the new triple
   * @param node2 the third element of the new triple
   * @param node3 the fourth element of the new triple
   * @throws StatementStoreException EXCEPTION TO DO
   */
  public void addTriple(
      long node0, long node1, long node2, long node3
  ) throws StatementStoreException;

  /**
   * Removes all triples matching the given specification.
   *
   * @param node0 the value for the first element of the triples
   * @param node1 the value for the second element of the triples
   * @param node2 the value for the third element of the triples
   * @param node3 the value for the fourth element of the triples
   * @throws StatementStoreException if something exceptional happens
   */
  public void removeTriples(
      long node0, long node1, long node2, long node3
  ) throws StatementStoreException;

  //
  // Search methods
  //

  /**
   * Locate all triples matching the given specification.
   *
   * @param node0 the value for the first element of the triples
   * @param node1 the value for the second element of the triples
   * @param node2 the value for the third element of the triples
   * @param node3 the value for the fourth element of the triples
   * @return the {@link StoreTuples}
   * @throws StatementStoreException if something exceptional happens
   */
  public StoreTuples findTuples(
      long node0, long node1, long node2, long node3
  ) throws StatementStoreException;

  /**
   * Locate all triples matching the given specification using the given ordering mask.
   *
   * @param mask the mask indicating the index to use. This MUST be compatible with the
   *             following nodes.
   * @param node0 the value for the first element of the triples
   * @param node1 the value for the second element of the triples
   * @param node2 the value for the third element of the triples
   * @param node3 the value for the fourth element of the triples
   * @return the {@link StoreTuples}
   * @throws StatementStoreException if something exceptional happens
   */
  public StoreTuples findTuples(
      int mask,
      long node0, long node1, long node2, long node3
  ) throws StatementStoreException;

  /**
   * Returns a StoreTuples which contains all triples in the store.  The
   * parameters provide a hint about how the StoreTuples will be used.  This
   * information is used to select the index from which the StoreTuples will be
   * obtained.
   *
   * @param node0Bound specifies that node0 will be bound
   * @param node1Bound specifies that node1 will be bound
   * @param node2Bound specifies that node2 will be bound
   * @param node3Bound specifies that node3 will be bound
   * @return the {@link StoreTuples}
   * @throws StatementStoreException if something exceptional happens
   */
  public StoreTuples findTuples(
      boolean node0Bound, boolean node1Bound, boolean node2Bound,
      boolean node3Bound
  ) throws StatementStoreException;

  //
  // Existence tests
  //

  /**
   * Return <code>true</code> if the graph contains any triples which match the
   * given specification
   *
   * @param node0 the value for the first element of the triples
   * @param node1 the value for the second element of the triples
   * @param node2 the value for the third element of the triples
   * @param node3 the value for the fourth element of the triples
   * @return <code>true</code> if the graph contains any triples which match the
   *      given specification
   * @throws StatementStoreException if something exceptional happens
   */
  public boolean existsTriples(
      long node0, long node1, long node2, long node3
  ) throws StatementStoreException;

  //
  // Miscellaneous methods
  //

  /**
   * Returns <code>true</code> if the graph does not contain any triples.
   *
   * @return <code>true</code> if the graph does not contain any triples.
   */
  public boolean isEmpty();

  /**
   * Returns a count of triples in the graph.
   *
   * @return a count of triples in the graph.
   */
  public long getNrTriples();

}
