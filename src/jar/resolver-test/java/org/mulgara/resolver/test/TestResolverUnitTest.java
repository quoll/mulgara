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
 * Contributor(s):
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 *   under contract 4500430665
 *   contributed to the Mulgara Project under the
 *     Mozilla Public License version 1.1
 *   per clause 4.1.3 of the above contract.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.test;

// Java 2 standard packages
import java.beans.Beans;
import java.io.File;
import java.util.*;
import java.net.URI;

// JUnit
import junit.framework.*;

// log4j
import org.apache.log4j.*;

// locally written packages
import org.mulgara.query.Answer;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.GraphResource;
import org.mulgara.query.Order;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.UnconstrainedAnswer;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.resolver.Database;
import org.mulgara.server.Session;
import org.mulgara.transaction.TransactionManagerFactory;
import org.mulgara.util.*;


/**
 * @created 2005-05-03
 * @author <a href="http://netymon.com/people/staff/andrae">Andrae Muys</a>
 * @version $Revision: 1.5 $
 * @modified $Date: 2005/06/26 12:48:12 $ by $Author: pgearon $
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class TestResolverUnitTest extends TestCase {

  /** Logger.  */
  private final static Logger logger = Logger.getLogger(TestResolverUnitTest.class);

  /**
   * Test {@link Database} used to generate
   * {@link org.mulgara.resolver.DatabaseSession}s for testing.
   *
   * This is assigned a value by the {@link #setUp} method.
   */
  private static Database database = null;

  /** The URI of the {@link #database}: <code>local:database</code>.  */
  private static final URI databaseURI = URI.create("local:database");
  private static final URI systemModelURI = URI.create("local:database#");


  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public TestResolverUnitTest(String name) {
    super(name);
  }


  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {
    return new TestSuite(TestResolverUnitTest.class);
  }


  /**
   * Default test runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }


  /**
   * Test the return from TestConstraint::test:a
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testConstraintTestA() throws Exception {
    logger.warn("Testing: testConstraintTestA");
    Session session = database.newSession();

    Variable[] varArray = new Variable[] { new Variable("a"), new Variable("b") };
    List<Variable> variables = Arrays.asList((Variable[])varArray);

    Answer answer = session.query(new Query(
      variables,                                                                // SELECT
      new GraphResource(systemModelURI),                                        // FROM
      new TestConstraint(new Variable("a"), new Variable("b"), "test:a", ""),   // WHERE
      null,                                                                     // HAVING
      Arrays.asList(new Order[]
          { new Order(varArray[0], true), new Order(varArray[1], true), }),     // ORDER BY
      null,                                                                     // LIMIT
      0,                                                                        // OFFSET
      true,                                                                     // DISTINCT
      new UnconstrainedAnswer()                                                 // GIVEN
    ));

    String[][] result = new String[][] {
      new String[] { "A", "A" },
      new String[] { "A", "B" },
      new String[] { "B", "A" },
      new String[] { "B", "B" },
    };

    answer.beforeFirst();
    for (int i = 0; i < result.length; i++) {
      assertTrue(answer.next());
      logger.warn("returned: " + answer.getObject(0) + ", " + answer.getObject(1));
      for (int j = 0; j < result[i].length; j++) {
        assertEquals(answer.getObject(j), new LiteralImpl(result[i][j]));
      }
    }

    assertFalse(answer.next());

    answer.close();
    session.close();
  }


  /**
   * Test the return from TestConstraint::test:b
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testConstraintTestB() throws Exception {
    logger.warn("Testing: testConstraintTestB");
    Session session = database.newSession();

    Variable[] varArray = new Variable[] { new Variable("a"), new Variable("c") };
    List<Variable> variables = Arrays.asList((Variable[])varArray);

    Answer answer = session.query(new Query(
      variables,                                                                // SELECT
      new GraphResource(systemModelURI),                                        // FROM
      new TestConstraint(new Variable("a"), new Variable("c"), "test:b", ""),   // WHERE
      null,                                                                     // HAVING
      Arrays.asList(new Order[]
          { new Order(varArray[0], true), new Order(varArray[1], true), }),     // ORDER BY
      null,                                                                     // LIMIT
      0,                                                                        // OFFSET
      true,                                                                     // DISTINCT
      new UnconstrainedAnswer()                                                 // GIVEN
    ));
    
    String[][] result = new String[][] {
      new String[] { "A", "C" },
      new String[] { "A", "D" },
    };

    answer.beforeFirst();
    for (int i = 0; i < result.length; i++) {
      assertTrue(answer.next());
      logger.warn("returned: " + answer.getObject(0) + ", " + answer.getObject(1));
      for (int j = 0; j < result[i].length; j++) {
        assertEquals(answer.getObject(j), new LiteralImpl(result[i][j]));
      }
    }

    assertFalse(answer.next());

    answer.close();
    session.close();
  }


  /**
   * Test a simple join between test:a and test:b
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testBasicConjunction() throws Exception {
    logger.warn("Testing: testBasicConjunction");
    Session session = database.newSession();

    Variable[] varArray = new Variable[] { new Variable("a"), new Variable("b"), new Variable("c") };
    List<Variable> variables = Arrays.asList((Variable[])varArray);

    Answer answer = session.query(new Query(
      variables,                                                                // SELECT
      new GraphResource(systemModelURI),                                        // FROM
      new ConstraintConjunction( 																								// WHERE
				new TestConstraint(new Variable("a"), new Variable("c"), "test:b", ""),
				new TestConstraint(new Variable("a"), new Variable("b"), "test:a", "")),
      null,                                                                     // HAVING
      Arrays.asList(new Order[] {     																					// ORDER BY
				new Order(varArray[0], true),
				new Order(varArray[1], true),
				new Order(varArray[2], true) }),
      null,                                                                     // LIMIT
      0,                                                                        // OFFSET
      true,                                                                     // DISTINCT
      new UnconstrainedAnswer()                                                 // GIVEN
    ));
    
    String[][] result = new String[][] {
      new String[] { "A", "A", "C" },
      new String[] { "A", "A", "D" },
      new String[] { "A", "B", "C" },
      new String[] { "A", "B", "D" },
    };

    answer.beforeFirst();
    for (int i = 0; i < result.length; i++) {
      assertTrue(answer.next());
      logger.warn("returned: " + answer.getObject(0) + ", " + answer.getObject(1));
      for (int j = 0; j < result[i].length; j++) {
        assertEquals(answer.getObject(j), new LiteralImpl(result[i][j]));
      }
    }

    assertFalse(answer.next());

    answer.close();
    session.close();
  }


  /**
   * Test a simple join between test:a and test:b reordering the join using an annotation
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testAnnotatedConjunction() throws Exception {
    logger.warn("Testing: testAnnotatedConjunction");
    Session session = database.newSession();

    Variable[] varArray = new Variable[] { new Variable("a"), new Variable("b"), new Variable("c") };
    List<Variable> variables = Arrays.asList(varArray);

    Answer answer = session.query(new Query(
      variables,                                                                // SELECT
      new GraphResource(systemModelURI),                                        // FROM
      new ConstraintConjunction( 																								// WHERE
				new TestConstraint(new Variable("a"), new Variable("c"), "test:b", "a"),
				new TestConstraint(new Variable("a"), new Variable("b"), "test:a", "")),
      null,                                                                     // HAVING
      Arrays.asList(new Order[] {     																					// ORDER BY
				new Order(varArray[0], true),
				new Order(varArray[1], true),
				new Order(varArray[2], true) }),
      null,                                                                     // LIMIT
      0,                                                                        // OFFSET
      true,                                                                     // DISTINCT
      new UnconstrainedAnswer()                                                 // GIVEN
    ));
    
    String[][] result = new String[][] {
      new String[] { "A", "A", "C" },
      new String[] { "A", "A", "D" },
      new String[] { "A", "B", "C" },
      new String[] { "A", "B", "D" },
    };

    answer.beforeFirst();
    for (int i = 0; i < result.length; i++) {
      assertTrue(answer.next());
      logger.warn("returned: " + answer.getObject(0) + ", " + answer.getObject(1));
      for (int j = 0; j < result[i].length; j++) {
        assertEquals(answer.getObject(j), new LiteralImpl(result[i][j]));
      }
    }

    assertFalse(answer.next());

    answer.close();
    session.close();
  }


  /**
   * Test a failing join between test:a and test:b with the reordering criterion is not met
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFailedAnnotatedConjunction() throws Exception {
    logger.warn("Testing: testFailedAnnotatedConjunction");
    Session session = database.newSession();

    Variable[] varArray = new Variable[] { new Variable("a"), new Variable("b"), new Variable("c") };
    List<Variable> variables = Arrays.asList(varArray);

		try {
			session.query(new Query(
				variables,                                                                // SELECT
				new GraphResource(systemModelURI),                                        // FROM
				new ConstraintConjunction( 																								// WHERE
					new TestConstraint(new Variable("a"), new Variable("c"), "test:b", "b"), // non-prefix variable causes failure
					new TestConstraint(new Variable("a"), new Variable("b"), "test:a", "")),
				null,                                                                     // HAVING
				Arrays.asList(new Order[] {     																					// ORDER BY
					new Order(varArray[0], true),
					new Order(varArray[1], true),
					new Order(varArray[2], true) }),
				null,                                                                     // LIMIT
				0,                                                                        // OFFSET
        true,                                                                     // DISTINCT
				new UnconstrainedAnswer()                                                 // GIVEN
			));

			assertTrue(false);
		} catch (QueryException et) {
			// Expect this.
		} finally {
			session.close();
		}
  }


  /**
   * A method to call for each graph before running tests on it.
   *
   * @throws Exception EXCEPTION TO DO
   */
  @SuppressWarnings("deprecation")
  protected void setUp() throws Exception {
		if (database == null) {
			// Create the persistence directory
			File persistenceDirectory =
				new File(new File(System.getProperty("cvs.root")), "testDatabase");
			if (persistenceDirectory.isDirectory()) {
				if (!FileUtil.deleteDirectory(persistenceDirectory)) {
					throw new RuntimeException(
							"Unable to remove old directory " + persistenceDirectory
							);
				}
			}
			if (!persistenceDirectory.mkdirs()) {
				throw new Exception("Unable to create directory "+persistenceDirectory);
			}

			// Define the the node pool factory
			String nodePoolFactoryClassName =
				"org.mulgara.store.nodepool.xa.XANodePoolFactory";

			// Define the string pool factory
			String stringPoolFactoryClassName =
				"org.mulgara.store.stringpool.xa.XAStringPoolFactory";

			String tempNodePoolFactoryClassName =
				"org.mulgara.store.nodepool.memory.MemoryNodePoolFactory";

			// Define the string pool factory
			String tempStringPoolFactoryClassName =
				"org.mulgara.store.stringpool.memory.MemoryStringPoolFactory";

			// Define the resolver factory used to manage system models
			String systemResolverFactoryClassName =
				"org.mulgara.resolver.store.StatementStoreResolverFactory";

			String tmfClassName = "org.mulgara.resolver.JotmTransactionManagerFactory";

			TransactionManagerFactory transactionManagerFactory =
				(TransactionManagerFactory) Beans.instantiate(null, tmfClassName);

			// Create a database which keeps its system models on the Java heap
			database = new Database(
					databaseURI,
					persistenceDirectory,
					null,                             // no security domain
					transactionManagerFactory,
					0,                                // default transaction timeout
					0,                                // default idle timeout
					nodePoolFactoryClassName,         // persistent
					new File(persistenceDirectory, "xaNodePool"),
					stringPoolFactoryClassName,       // persistent
					new File(persistenceDirectory, "xaStringPool"),
					systemResolverFactoryClassName,   // persistent
					new File(persistenceDirectory, "xaStatementStore"),
					tempNodePoolFactoryClassName,     // temporary
					null,
					tempStringPoolFactoryClassName,   // temporary
					null,
					systemResolverFactoryClassName,   // temporary
					new File(persistenceDirectory, "cache"),
          "",                               // no rule loader
					null);                            // no default content handler

			database.addResolverFactory("org.mulgara.resolver.test.TestResolverFactory", null);
		}
  }


  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
//  public void testTearDown() throws Exception {
//    database.delete();
//  }
}
