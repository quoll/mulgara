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

import java.util.Set;
import java.util.Iterator;

// Third party packages
// import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.Triple;
import gnu.trove.TObjectLongHashMap;

// Local packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.tuples.Tuples;

/**
 * A wrapper around {@link Tuples} to turn them into {@link Statements}.
 *
 * @created 2004-05-01
 * @author <a href="http://staff.tucanatech.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class TripleSetWrapperStatements implements Statements {
  // /** Logger.  */
  // private final Logger logger = Logger.getLogger(TripleSetWrapperStatements.class.getName());

  public static final int PERSIST = 0;
  public static final int TEMP = 1;

  static final Variable subject   = new Variable("subject");
  static final Variable predicate = new Variable("predicate");
  static final Variable object    = new Variable("object");

  ResolverSession resolverSession;
  
  TObjectLongHashMap<Node> nodeMap;

  Set<? extends Triple> triples;
  Iterator<? extends Triple> iter;
  Triple currentTriple;
  boolean persistent;

  //
  // Constructors
  //

  public TripleSetWrapperStatements(Set<? extends Triple> triples, ResolverSession resolverSession, int persistent)
      throws TuplesException {
    this.triples = triples;
    this.resolverSession = resolverSession;
    this.nodeMap = new TObjectLongHashMap<Node>();
    this.persistent = (persistent == PERSIST);
  }

  public void beforeFirst() {
    iter = triples.iterator();
    currentTriple = null;
  }

  public void close() {
    iter = null;
    triples = null;
  }

  public int getColumnIndex(Variable column) throws TuplesException {
    if (column.equals(subject)) {
      return 0;
    } else if (column.equals(predicate)) {
      return 1;
    } else if (column.equals(object)) {
      return 2;
    } else {
      throw new TuplesException("Variable " + column + " not valid on Statements");
    }
  }

  public int getNumberOfVariables() {
    return 3;
  }

  public Variable[] getVariables() {
    return new Variable[] { subject, predicate, object };
  }

  public boolean isUnconstrained() {
    return false;
  }

  public long getRowCount() {
    return triples.size();
  }

  public boolean isEmpty() throws TuplesException {
    return triples.isEmpty();
  }

  public long getRowUpperBound() {
    return triples.size();
  }

  public long getRowExpectedCount() {
    return triples.size();
  }

  public int getRowCardinality() {
    switch (triples.size()) {
      case 0: return ZERO;
      case 1: return ONE;
      default: return MANY;
    }
  }

  public boolean next() throws TuplesException {
    if (iter == null) {
      throw new TuplesException("next() called before beforeFirst()");
    }

    if (!iter.hasNext()) {
      currentTriple = null;
      return false;
    } else {
      currentTriple = iter.next();
      return true;
    }
  }

  public long getSubject() throws TuplesException {
    if (currentTriple == null) {
      throw new TuplesException("getSubject() called before next()");
    }
    return localize(currentTriple.getSubject());
  }

  public long getPredicate() throws TuplesException {
    if (currentTriple == null) {
      throw new TuplesException("getPredicate() called before next()");
    }
    return localize(currentTriple.getPredicate());
  }

  public long getObject() throws TuplesException {
    if (currentTriple == null) {
      throw new TuplesException("getObject() called before next()");
    }
    return localize(currentTriple.getObject());
  }

  private long localize(Node node) throws TuplesException {
    try {
      if (!nodeMap.contains(node)) {
        nodeMap.put(node, persistent ? resolverSession.localizePersistent(node)
                                     : resolverSession.localize(node));
      }

      return nodeMap.get(node);
    } catch (LocalizeException el) {
      throw new TuplesException("Failed to localize node", el);
    }
  }

  public Object clone() {
    try {
      TripleSetWrapperStatements stmts = (TripleSetWrapperStatements)super.clone();
      this.iter = null;
      this.currentTriple = null;

      return stmts;
    } catch (CloneNotSupportedException ec) {
      throw new Error("TripleSetWrapperStatements super.clone() failed");
    }
  }
}
