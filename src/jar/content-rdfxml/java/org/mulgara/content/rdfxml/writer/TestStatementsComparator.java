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
package org.mulgara.content.rdfxml.writer;

// Java 2 standard packages

// Apache packages
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.BlankNode;

// Local packages
import java.util.Map;
import java.util.HashMap;
import org.jrdf.graph.Node;
import java.util.Arrays;

import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.StatementsWrapperTuples;
import org.mulgara.resolver.spi.TuplesWrapperStatements;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/**
 * Compares one set of Statements to another. Intended as a Test Comparator
 * for unit tests that need to compare Statements.
 *
 * <p>Ignores BlankNodes.
 *
 * @created 2004-10-25
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:03 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class TestStatementsComparator {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(
      TestStatementsComparator.class.getName());

  /**
   * Tests two sets of statements for equivelance.
   *
   * @param statements1 Statements
   * @param session1 ResolverSession
   * @param statements2 Statements
   * @param session2 ResolverSession
   * @throws Exception
   * @return boolean
   */
  public boolean areEquivelant(Statements statements1,
      ResolverSession session1, Statements statements2,
      ResolverSession session2) throws Exception {

    //assume they are not equal
    boolean equal = false;

    //check if they are the same object, if not compare for equivelance
    equal = (compareReference(statements1, statements2))
        || (compareCount(statements1, statements2)
        && compareStatementNodes(statements1, session1, statements2, session2,
        new HashMap<String,Object>()));

    return equal;
  }

  /**
   * Do they have the same number of statements.
   *
   * @param statements1 Statements
   * @param statements2 Statements
   * @throws Exception
   * @return boolean
   */
  private static boolean compareReference(Statements statements1,
      Statements statements2) throws Exception {

    return statements1 == statements2;
  }

  /**
   * Compares the number of rows.
   *
   * @param statements1 Statements
   * @param statements2 Statements
   * @throws Exception
   * @return boolean
   */
  private static boolean compareCount(Statements statements1,
      Statements statements2) throws Exception {

    return statements1.getRowCount() == statements2.getRowCount();
  }

  /**
   * Does a deep comparison of Nodes.
   *
   * @param statements1 Statements
   * @param session1 ResolverSession
   * @param statements2 Statements
   * @param session2 ResolverSession
   * @param environment Map
   * @throws Exception
   * @return boolean
   */
  private boolean compareStatementNodes(Statements statements1,
      ResolverSession session1, Statements statements2, ResolverSession session2,
      Map<String,Object> environment) throws Exception {

    statements1 = prepareStatements(statements1);
    statements2 = prepareStatements(statements2);
    statements1.beforeFirst();
    statements2.beforeFirst();

    //current values
    Node s1 = null;
    Node p1 = null;
    Node o1 = null;
    Node s2 = null;
    Node p2 = null;
    Node o2 = null;

    //iterate over the tuples and compare their global values
    while(statements1.next()
        && statements2.next()) {

      //get next "triple" pair
      s1 = session1.globalize(statements1.getSubject());
      p1 = session1.globalize(statements1.getPredicate());
      o1 = session1.globalize(statements1.getObject());
      s2 = session2.globalize(statements2.getSubject());
      p2 = session2.globalize(statements2.getPredicate());
      o2 = session2.globalize(statements2.getObject());

      //dont compare BlankNodes
      if ((s1 instanceof BlankNode)
          && (s2 instanceof BlankNode)) {

        continue;
      }

      //compare each node
      if (!equal(s1, s2, session1, session2)
          || !equal(p1, p2, session1, session2)
          || !equal(o1, o2, session1, session2)) {

        //TEST CODE
        System.out.println("" + s1 + ", " + p1 + ", " + o1 + " != " + s2 + ", " +
            p2 + ", " + o2);
        //END TEST
        return false;
      }
    }

    return true;
  }

  /**
   * Compares two local nodes.
   *
   * @param node1 long
   * @param node2 long
   * @param session1 ResolverSession
   * @param session2 ResolverSession
   * @throws Exception
   * @return boolean
   */
  private boolean equal(Node node1, Node node2, ResolverSession session1,
      ResolverSession session2) throws Exception {

    //short cicuit if equal (or both null)
    if (node1 == node2) {

      return true;
    }
    else if ((node1 == null) ^ (node2 == null)) {

      //if one is null and not the other, they are not equal
      return false;
    }

    //don't compare BlankNodes
    if ((node1 instanceof BlankNode)
        && (node2 instanceof BlankNode)) {

      return true;
    }

    //ask the nodes
    return node1.equals(node2);
  }

  /**
   * Materializes, sorts and calls before first.
   * @param statements Statements
   * @throws Exception
   * @return Statements
   */
  private Statements prepareStatements(Statements statements) throws Exception {

    //prepare the statements
    Tuples tuples = new StatementsWrapperTuples(statements);
    Tuples materializedTuples = TuplesOperations.materialize(tuples);
    tuples.close();

    Tuples sortedTuples = TuplesOperations.sort(materializedTuples);
    materializedTuples.close();

    Tuples projectedTuples = TuplesOperations.project(sortedTuples,
        Arrays.asList(new Variable[] {
        StatementStore.VARIABLES[0], StatementStore.VARIABLES[1],
        StatementStore.VARIABLES[2]}), true);
    sortedTuples.close();

    return new TuplesWrapperStatements(projectedTuples,
        StatementStore.VARIABLES[0], StatementStore.VARIABLES[1],
        StatementStore.VARIABLES[2]);
  }

}
