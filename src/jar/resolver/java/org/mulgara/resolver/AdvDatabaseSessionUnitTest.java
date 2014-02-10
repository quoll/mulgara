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
 *   All tests in this file (c) Netymon Pty Ltd 2006 All rights reserved.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Java 2 standard packages
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.mulgara.query.Answer;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintDisjunction;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.ConstraintIs;
import org.mulgara.query.GraphResource;
import org.mulgara.query.Order;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.SelectElement;
import org.mulgara.query.Subquery;
import org.mulgara.query.TuplesException;
import org.mulgara.query.UnconstrainedAnswer;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.TripleImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.rdf.VariableNodeImpl;
import org.mulgara.server.Session;
import org.mulgara.util.FileUtil;

/**
* Test case for {@link DatabaseSession}.
*
* @created 2006-11-20
* @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
* @company <a href="mailto:mail@netymon.com">Netymon Pty Ltd</a>
* @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
*      Software Pty Ltd</a>
* @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
*/
public class AdvDatabaseSessionUnitTest extends TestCase {
  /** Logger.  */
  private static Logger logger =
    Logger.getLogger(AdvDatabaseSessionUnitTest.class.getName());

  private static final URI databaseURI = URI.create("local:database");

  private static final URI systemModelURI = URI.create("local:database#");
  private static final URI modelURI = URI.create("local:database#model");
  private static final URI model2URI = URI.create("local:database#model2");
  private static final URI model3URI = URI.create("local:database#model3");
  private static final URI model4URI = URI.create("local:database#model4");
  private static final URI model5URI = URI.create("local:database#model5");

  private static final URI xsdModelTypeURI = URI.create(Mulgara.NAMESPACE + "XMLSchemaModel");

  private static Database database = null;

  public AdvDatabaseSessionUnitTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new AdvDatabaseSessionUnitTest("testSetModel"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testBasicQuery"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testConcurrentQuery"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testSubqueryQuery"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testConcurrentSubqueryQuery"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testConcurrentReadWrite"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testExplicitBasicQuery"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testAnswerWriteCloseIsolation"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testExplicitIsolationQuery"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testExplicitRollbackIsolationQuery"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testExplicitCommitIsolationQuery"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testImplicitCommitQuery"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testConcurrentExplicitTxn"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testConcurrentImplicitTxn"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testConcurrentImplicitRecovery"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testPrefixingWithUnbound"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testDatabaseDelete"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testCreateModel"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testInsertionBlankNodes"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testAutoCommitTransactionOps"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testExplicitTransactionFailure"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testAutoCommitTransactionFailure"));
    suite.addTest(new AdvDatabaseSessionUnitTest("testSessionClose"));

    return suite;
  }

  /**
   * Create test objects.
   */
  public void setUp() throws Exception {
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

      // Define the resolver factory used to manage system models
      String tempResolverFactoryClassName =
        "org.mulgara.resolver.memory.MemoryResolverFactory";

      // Create a database which keeps its system models on the Java heap
      database = new Database(
                   databaseURI,
                   persistenceDirectory,
                   null,                            // no security domain
                   new JotmTransactionManagerFactory(),
                   0,                               // default transaction timeout
                   0,                               // default idle timeout
                   nodePoolFactoryClassName,        // persistent
                   new File(persistenceDirectory, "xaNodePool"),
                   stringPoolFactoryClassName,      // persistent
                   new File(persistenceDirectory, "xaStringPool"),
                   systemResolverFactoryClassName,  // persistent
                   new File(persistenceDirectory, "xaStatementStore"),
                   tempNodePoolFactoryClassName,    // temporary nodes
                   null,                            // no dir for temp nodes
                   tempStringPoolFactoryClassName,  // temporary strings
                   null,                            // no dir for temp strings
                   tempResolverFactoryClassName,    // temporary models
                   null,                            // no dir for temp models
                   "org.mulgara.content.rdfxml.RDFXMLContentHandler");

      database.addResolverFactory("org.mulgara.resolver.url.URLResolverFactory", null);
      database.addResolverFactory("org.mulgara.resolver.xsd.XSDResolverFactory", null);
      database.addResolverFactory("org.mulgara.resolver.MockResolverFactory", null);
    }
  }


  /**
  * The teardown method for JUnit
  */
  public void tearDown() {
  }

  //
  // Test cases
  //

  /**
  * Test the {@link DatabaseSession#setModel} method.
  */
  public void testSetModel() throws URISyntaxException {
    logger.info("testSetModel");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      // Load some test data
      Session session = database.newSession();
      try {
        session.createModel(modelURI, null);
        session.setModel(modelURI, fileURI);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testBasicQuery() throws URISyntaxException {
    logger.info("Testing basicQuery");

    try {
      // Load some test data
      Session session = database.newSession();
      try {
        // Evaluate the query
        Answer answer = session.query(createQuery(modelURI));
        compareResults(expectedResults(), answer);
        answer.close();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testConcurrentQuery() throws URISyntaxException {
    logger.info("Testing concurrentQuery");

    try {
      // Load some test data
      Session session = database.newSession();
      try {
        // Evaluate the query
        Answer answer1 = session.query(createQuery(modelURI));

        Answer answer2 = session.query(createQuery(modelURI));

        compareResults(answer1, answer2);

        answer1.close();
        answer2.close();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testSubqueryQuery() throws URISyntaxException {
    logger.info("Testing subqueryQuery");

    try {
      // Load some test data
      Session session = database.newSession();
      try {
        Variable subjectVariable   = new Variable("subject");
        Variable predicateVariable = new Variable("predicate");
        Variable objectVariable    = new Variable("object");

        List<SelectElement> selectList = new ArrayList<SelectElement>(3);
        selectList.add(subjectVariable);
        selectList.add(new Subquery(new Variable("k0"), new Query(
          Collections.singletonList(objectVariable),
          new GraphResource(modelURI),                      // FROM
          new ConstraintImpl(subjectVariable,               // WHERE
                         predicateVariable,
                         objectVariable),
          null,                                             // HAVING
          Collections.singletonList(                        // ORDER BY
            new Order(objectVariable, true)
          ),
          null,                                             // LIMIT
          0,                                                // OFFSET
          true,                                             // DISTINCT
          new UnconstrainedAnswer()                         // GIVEN
        )));


        // Evaluate the query
        Answer answer = session.query(new Query(
          selectList,                                       // SELECT
          new GraphResource(modelURI),                      // FROM
          new ConstraintImpl(subjectVariable,               // WHERE
              new URIReferenceImpl(new URI("test:p03")),
              objectVariable),
          null,                                             // HAVING
          Collections.singletonList(                        // ORDER BY
            new Order(subjectVariable, true)
          ),
          null,                                             // LIMIT
          0,                                                // OFFSET
          true,                                             // DISTINCT
          new UnconstrainedAnswer()                         // GIVEN
        ));

        answer.beforeFirst();

        assertTrue(answer.next());
        assertEquals(new URIReferenceImpl(new URI("test:s01")),
            answer.getObject(0));
        Answer sub1 = (Answer)answer.getObject(1);
        compareResults(new String[][] { new String[] { "test:o01" },
                                        new String[] { "test:o02" } }, sub1);
        sub1.close();

        assertTrue(answer.next());
        assertEquals(new URIReferenceImpl(new URI("test:s02")),
            answer.getObject(0));
        Answer sub2 = (Answer)answer.getObject(1);
        compareResults(new String[][] { new String[] { "test:o02" },
                                        new String[] { "test:o03" } }, sub2);
        // Leave sub2 open.

        assertFalse(answer.next());
        answer.close();
        sub2.close();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testConcurrentSubqueryQuery() throws URISyntaxException {
    logger.info("Testing concurrentSubqueryQuery");

    try {
      // Load some test data
      Session session = database.newSession();
      try {
        Variable subjectVariable   = new Variable("subject");
        Variable predicateVariable = new Variable("predicate");
        Variable objectVariable    = new Variable("object");

        List<SelectElement> selectList = new ArrayList<SelectElement>(3);
        selectList.add(subjectVariable);
        selectList.add(new Subquery(new Variable("k0"), new Query(
          Collections.singletonList(objectVariable),
          new GraphResource(modelURI),                      // FROM
          new ConstraintImpl(subjectVariable,               // WHERE
                         predicateVariable,
                         objectVariable),
          null,                                             // HAVING
          Collections.singletonList(                        // ORDER BY
            new Order(objectVariable, true)
          ),
          null,                                             // LIMIT
          0,                                                // OFFSET
          true,                                             // DISTINCT
          new UnconstrainedAnswer()                         // GIVEN
        )));


        // Evaluate the query
        Answer answer = session.query(new Query(
          selectList,                                       // SELECT
          new GraphResource(modelURI),                      // FROM
          new ConstraintImpl(subjectVariable,               // WHERE
              new URIReferenceImpl(new URI("test:p03")),
              objectVariable),
          null,                                             // HAVING
          Collections.singletonList(                        // ORDER BY
            new Order(subjectVariable, true)
          ),
          null,                                             // LIMIT
          0,                                                // OFFSET
          true,                                             // DISTINCT
          new UnconstrainedAnswer()                         // GIVEN
        ));

        answer.beforeFirst();

        assertTrue(answer.next());
        assertEquals(new URIReferenceImpl(new URI("test:s01")),
            answer.getObject(0));
        Answer sub1 = (Answer)answer.getObject(1);
        assertTrue(answer.next());
        assertEquals(new URIReferenceImpl(new URI("test:s02")),
            answer.getObject(0));
        Answer sub2 = (Answer)answer.getObject(1);
        assertFalse(answer.next());

        assertEquals(1, sub1.getNumberOfVariables());
        assertEquals(1, sub2.getNumberOfVariables());
        sub1.beforeFirst();
        sub2.beforeFirst();
        assertTrue(sub1.next());
        assertTrue(sub2.next());
        assertEquals(new URIReferenceImpl(new URI("test:o01")), sub1.getObject(0));
        assertEquals(new URIReferenceImpl(new URI("test:o02")), sub2.getObject(0));
        assertTrue(sub1.next());
        assertTrue(sub2.next());
        assertEquals(new URIReferenceImpl(new URI("test:o02")), sub1.getObject(0));
        assertEquals(new URIReferenceImpl(new URI("test:o03")), sub2.getObject(0));
        assertFalse(sub1.next());
        assertFalse(sub2.next());

        answer.close();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  /**
   * Note: What this test does is a really bad idea - there is no
   *       isolation provided as each operation is within its own
   *       transaction.  It does however provide a good test.
   */
  public void testConcurrentReadWrite() throws URISyntaxException {
    logger.info("Testing concurrentReadWrite");

    try {
      Session session = database.newSession();

      session.createModel(model2URI, null);

      try {
        // Evaluate the query
        Answer answer = session.query(createQuery(modelURI));

        answer.beforeFirst();
        while (answer.next()) {
          session.insert(model2URI, Collections.singleton((Triple)new TripleImpl(
              (SubjectNode)answer.getObject(0),
              (PredicateNode)answer.getObject(1),
              (ObjectNode)answer.getObject(2))));
        }
        answer.close();

        Answer answer2 = session.query(createQuery(model2URI));
        compareResults(expectedResults(), answer2);
        answer2.close();

        session.removeModel(model2URI);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testExplicitBasicQuery() throws URISyntaxException {
    logger.info("Testing basicQuery");

    try {
      // Load some test data
      Session session = database.newSession();
      try {
        session.setAutoCommit(false);

        // Evaluate the query
        Answer answer = session.query(createQuery(modelURI));
        compareResults(expectedResults(), answer);

        session.setAutoCommit(true);

        // Should throw an Exception here as commit should close answer
        boolean thrown = false;;
        try {
          answer.beforeFirst();
        } catch (TuplesException et) {
          thrown = true;
        } finally {
          assertTrue("Answer failed to throw exception, should be closed by commit",
              thrown);
        }
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testAnswerWriteCloseIsolation() throws URISyntaxException {
    logger.info("Testing AnswerWriteCloseIsolation");

    try {
      Session session = database.newSession();
      try {
        // Evaluate the query
        Answer answer = session.query(createQuery(systemModelURI));

        session.setAutoCommit(false);

        answer.close();

        session.commit();
        session.setAutoCommit(true);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testExplicitIsolationQuery() throws URISyntaxException {
    logger.info("testExplicitIsolationQuery");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        Session session2 = database.newSession();
        try {
          session1.createModel(model3URI, null);
          session1.setAutoCommit(false);
          session1.setModel(model3URI, fileURI);

          // Evaluate the query
          Answer answer = session2.query(createQuery(model3URI));
          answer.beforeFirst();
          assertFalse(answer.next());
          answer.close();

          session1.setAutoCommit(true);

          // Evaluate the query
          answer = session2.query(createQuery(model3URI));
          compareResults(expectedResults(), answer);
          answer.close();

          session1.removeModel(model3URI);
        } finally {
          session2.close();
        }
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testExplicitRollbackIsolationQuery() throws URISyntaxException {
    logger.info("testExplicitRollbackIsolationQuery");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        Session session2 = database.newSession();
        try {
          session1.createModel(model3URI, null);
          session1.setAutoCommit(false);
          session1.setModel(model3URI, fileURI);

          // Evaluate the query
          Answer answer = session2.query(createQuery(model3URI));
          answer.beforeFirst();
          assertFalse(answer.next());
          answer.close();

          session1.rollback();
          session1.setAutoCommit(true);

          // Evaluate the query
          answer = session2.query(createQuery(model3URI));

          answer.beforeFirst();
          assertFalse(answer.next());
          answer.close();
        } finally {
          session2.close();
        }
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testExplicitCommitIsolationQuery() throws URISyntaxException {
    logger.info("testExplicitCommitIsolationQuery");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        Session session2 = database.newSession();
        try {
          session1.createModel(model3URI, null);
          session1.setAutoCommit(false);
          session1.setModel(model3URI, fileURI);

          // Evaluate the query
          Answer answer = session2.query(createQuery(model3URI));
          answer.beforeFirst();
          assertFalse(answer.next());
          answer.close();

          session1.commit();

          // Evaluate the query
          answer = session2.query(createQuery(model3URI));

          compareResults(expectedResults(), answer);
          answer.close();

          session1.removeModel(model3URI);
          session1.createModel(model3URI, null);

          // Evaluate the query
          answer = session2.query(createQuery(model3URI));

          compareResults(expectedResults(), answer);
          answer.close();

          session1.setAutoCommit(true);

          // Evaluate the query
          answer = session2.query(createQuery(model3URI));
          answer.beforeFirst();
          assertFalse(answer.next());
          answer.close();
        } finally {
          session2.close();
        }
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testImplicitCommitQuery() throws URISyntaxException {
    logger.info("testImplicitCommitQuery");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        Session session2 = database.newSession();
        try {
          session1.createModel(model4URI, null);
          session1.setModel(model4URI, fileURI);

          // Check data loaded
          Answer answer = session1.query(createQuery(model4URI));

          compareResults(expectedResults(), answer);
          answer.close();

          // Delete all the data from the model
          session2.delete(model4URI, createQuery(model4URI));

          // Check all data removed from the model
          // This also checks that the delete successfully
          // performed the implicit commit.
          answer = session1.query(createQuery(model4URI));
          answer.beforeFirst();
          assertFalse(answer.next());
          answer.close();

          session1.removeModel(model4URI);
        } finally {
          session2.close();
        }
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test two insertions in the same transaction using the same variable name.
   * The variable blank node should map to different internal node ID's in each
   * insert operation.
   */
  public void testInsertionBlankNodes() {
    logger.info("testInsertionBlankNodes");

    try {
      Session session = database.newSession();
      try {
        session.createModel(model2URI, null);
        session.setAutoCommit(false);

        URIReference refA = new URIReferenceImpl(URI.create("test:a"));
        URIReference refP1 = new URIReferenceImpl(URI.create("test:p1"));
        URIReference refP2 = new URIReferenceImpl(URI.create("test:p2"));
        Literal o1 = new LiteralImpl("o1");
        Literal o2 = new LiteralImpl("o2");
        BlankNode bn = new VariableNodeImpl("bn");

        Set<Triple> insert1 = new HashSet<Triple>();
        insert1.add(new TripleImpl(refA, refP1, bn));
        insert1.add(new TripleImpl(bn, refP2, o1));

        Set<Triple> insert2 = new HashSet<Triple>();
        insert2.add(new TripleImpl(refA, refP1, bn));
        insert2.add(new TripleImpl(bn, refP2, o2));

        session.insert(model2URI, insert1);
        session.insert(model2URI, insert2);
        session.setAutoCommit(true);

        Answer answer = session.query(createQuery(model2URI));
        assertEquals(4, answer.getRowCount());
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test two simultaneous, explicit transactions, in two threads. The second one should block
   * until the first one sets auto-commit back to true.
   */
  public void testConcurrentExplicitTxn() throws URISyntaxException {
    logger.info("testConcurrentExplicitTxn");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        session1.createModel(model3URI, null);
        session1.setAutoCommit(false);
        session1.setModel(model3URI, fileURI);

        final boolean[] tx2Started = new boolean[] { false };

        Thread t2 = new Thread("tx2Test") {
          public void run() {
            try {
              Session session2 = database.newSession();
              try {
                session2.setAutoCommit(false);

                synchronized (tx2Started) {
                  tx2Started[0] = true;
                  tx2Started.notify();
                }

                // Evaluate the query
                Answer answer = session2.query(createQuery(model3URI));

                compareResults(expectedResults(), answer);
                answer.close();

                session2.commit();
                session2.setAutoCommit(true);
              } finally {
                session2.close();
              }
            } catch (Exception e) {
              fail(e);
            }
          }
        };
        t2.start();

        synchronized (tx2Started) {
          if (!tx2Started[0]) {
            try {
              tx2Started.wait(2000L);
            } catch (InterruptedException ie) {
              logger.error("wait for tx2-started interrupted", ie);
              fail(ie);
            }
          }
          assertFalse("second transaction should still be waiting for write lock", tx2Started[0]);
        }

        session1.commit();
        session1.setAutoCommit(true);

        synchronized (tx2Started) {
          if (!tx2Started[0]) {
            try {
              tx2Started.wait(2000L);
            } catch (InterruptedException ie) {
              logger.error("wait for tx2-started interrupted", ie);
              fail(ie);
            }
            assertTrue("second transaction should've started", tx2Started[0]);
          }
        }

        try {
          t2.join(2000L);
        } catch (InterruptedException ie) {
          logger.error("wait for tx2-terminated interrupted", ie);
          fail(ie);
        }
        assertFalse("second transaction should've terminated", t2.isAlive());

        session1.removeModel(model3URI);

      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  /**
   * Test two simultaneous transactions, the first one explicit and the second one in auto-commit,
   * in two threads. The second one should proceed but not see uncommitted data.
   */
  public void testConcurrentImplicitTxn() throws URISyntaxException {
    logger.info("testConcurrentImplicitTxn");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        session1.createModel(model3URI, null);
        session1.setAutoCommit(false);
        session1.setModel(model3URI, fileURI);

        Thread t2 = new Thread("tx2Test") {
          public void run() {
            try {
              Session session2 = database.newSession();
              try {
                // Evaluate the query
                Answer answer = session2.query(createQuery(model3URI));

                answer.beforeFirst();
                assertFalse(answer.next());
                answer.close();

              } finally {
                session2.close();
              }
            } catch (Exception e) {
              fail(e);
            }
          }
        };
        t2.start();

        try {
          t2.join(2000L);
        } catch (InterruptedException ie) {
          logger.error("wait for tx2-terminated interrupted", ie);
          fail(ie);
        }
        assertFalse("second transaction should've terminated", t2.isAlive());

        session1.commit();

        t2 = new Thread("tx2Test") {
          public void run() {
            try {
              Session session2 = database.newSession();
              try {
                // Evaluate the query
                Answer answer = session2.query(createQuery(model3URI));
                compareResults(expectedResults(), answer);
                answer.close();

              } finally {
                session2.close();
              }
            } catch (Exception e) {
              fail(e);
            }
          }
        };
        t2.start();

        try {
          t2.join(2000L);
        } catch (InterruptedException ie) {
          logger.error("wait for tx2-terminated interrupted", ie);
          fail(ie);
        }
        assertFalse("second transaction should've terminated", t2.isAlive());

        session1.setAutoCommit(true);
        session1.removeModel(model3URI);

      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  /**
   * Test two simultaneous transactions, the first one obtains the write-lock
   * and sleeps longer than the recovery timeout.
   */
  public void testConcurrentImplicitRecovery() throws URISyntaxException {
    logger.info("testConcurrentImplicitRecovery");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    // test idle timeout
    try {
      Session session1 = database.newSession();
      session1.setIdleTimeout(1000);
      try {
        session1.createModel(model3URI, null);
        logger.debug("Obtaining autocommit for session1");
        session1.setAutoCommit(false);
        logger.debug("Obtained autocommit for session1");

        Thread t2 = new Thread("tx2IdleTest") {
          public void run() {
            try {
              Session session2 = database.newSession();
              try {
                logger.debug("Obtaining autocommit for session2");
                session2.setAutoCommit(false);
                logger.debug("Obtained autocommit for session2");

                // Evaluate the query
                Answer answer = session2.query(createQuery(model3URI));

                answer.beforeFirst();
                assertFalse(answer.next());
                answer.close();

                logger.debug("Releasing autocommit for session2");
                session2.setAutoCommit(true);
              } finally {
                session2.close();
              }
            } catch (Exception e) {
              fail(e);
            }
          }
        };
        t2.start();

        session1.setModel(model3URI, fileURI);
        logger.debug("Sleeping for 1sec");
        Thread.sleep(1000);
        logger.debug("Slept for 1sec");
        try {
          t2.join(2000L);
        } catch (InterruptedException ie) {
          logger.error("wait for tx2-terminated interrupted", ie);
          fail(ie);
        }
        assertFalse("second transaction should've terminated", t2.isAlive());

        rollbackTimedOutTxn(session1);
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }

    // test transaction timeout
    try {
      Session session1 = database.newSession();
      session1.setTransactionTimeout(1000);
      try {
        session1.createModel(model3URI, null);
        logger.debug("Obtaining autocommit for session1");
        session1.setAutoCommit(false);
        logger.debug("Obtained autocommit for session1");

        Thread t2 = new Thread("tx2TxToTest") {
          public void run() {
            try {
              Session session2 = database.newSession();
              try {
                logger.debug("Obtaining autocommit for session2");
                session2.setAutoCommit(false);
                logger.debug("Obtained autocommit for session2");

                // Evaluate the query
                Answer answer = session2.query(createQuery(model3URI));

                answer.beforeFirst();
                assertFalse(answer.next());
                answer.close();

                logger.debug("Releasing autocommit for session2");
                session2.setAutoCommit(true);
              } finally {
                session2.close();
              }
            } catch (Exception e) {
              fail(e);
            }
          }
        };
        t2.start();

        session1.setModel(model3URI, fileURI);
        logger.debug("Sleeping for 1sec");
        Thread.sleep(1000);
        logger.debug("Slept for 1sec");
        try {
          t2.join(2000L);
        } catch (InterruptedException ie) {
          logger.error("wait for tx2-terminated interrupted", ie);
          fail(ie);
        }
        assertFalse("second transaction should've terminated", t2.isAlive());

        rollbackTimedOutTxn(session1);
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }

    // test transaction timeout interrupting active operation
    try {
      Session session1 = database.newSession();
      try {
        session1.setTransactionTimeout(1000L);
        session1.setAutoCommit(false);
        logger.debug("Started transaction for session1");

        URI delayTwoSecs = new URI("foo://mulgara/timeoutTest?active=mr&wait=2000");
        session1.createModel(delayTwoSecs, new URI(Mulgara.NAMESPACE + "MockModel"));

        try {
          session1.query(createQuery(delayTwoSecs));
          fail("query should've gotten interrupted");
        } catch (QueryException qe) {
          logger.debug("query was interrupted", qe);
        }

        rollbackTimedOutTxn(session1);
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }

    // test transaction timeout timing out not-yet-active operation
    try {
      Session session1 = database.newSession();
      try {
        session1.setTransactionTimeout(1000L);
        session1.setAutoCommit(false);
        logger.debug("Started transaction for session1");

        logger.debug("Sleeping for 2sec");
        Thread.sleep(2000);
        logger.debug("Slept for 2sec");

        try {
          session1.query(createQuery(model3URI));
          fail("query should've gotten interrupted");
        } catch (QueryException qe) {
          logger.debug("query was interrupted", qe);
        }

        rollbackTimedOutTxn(session1);
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }

    // test transaction timeout while operation is waiting for mutex
    try {
      final Session session1 = database.newSession();
      try {
        session1.setTransactionTimeout(1000L);
        session1.setAutoCommit(false);
        logger.debug("Started transaction for session1");

        final URI delayTwoSecs = new URI("foo://mulgara/timeoutTest?active=mr&hardWait=2000");
        session1.createModel(delayTwoSecs, new URI(Mulgara.NAMESPACE + "MockModel"));

        Thread t2 = new Thread("timeoutTest") {
          public void run() {
            try {
              try {
                // this acquires mutex and holds it for 2s
                Answer answer = session1.query(createQuery(delayTwoSecs));
                Thread.sleep(100L);   // allow rollback to proceed
                answer.beforeFirst();
                assertFalse(answer.next());
                answer.close();
                fail("query should've gotten interrupted");
              } catch (TuplesException te) {
                logger.debug("query was interrupted", te);
              }
            } catch (Exception e) {
              fail(e);
            }
          }
        };
        t2.start();
        Thread.sleep(100L);

        // blocks, waiting for mutex; when it does get it, it should rollback or see a rb
        try {
          Answer answer = session1.query(createQuery(model3URI));
          Thread.sleep(100L);   // allow rollback to proceed
          answer.beforeFirst();
          assertFalse(answer.next());
          answer.close();
          fail("query should've gotten aborted");
        } catch (QueryException qe) {
          logger.debug("query was aborted", qe);
        } catch (TuplesException te) {
          logger.debug("query was aborted", te);
        }

        rollbackTimedOutTxn(session1);
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }

    // test transaction timeout on autocommit transaction
    try {
      final Session session1 = database.newSession();
      try {
        session1.setTransactionTimeout(1000L);

        final URI delayTwoSecs = new URI("foo://mulgara/timeoutTest?active=mr&hardWait=2000");
        session1.createModel(delayTwoSecs, new URI(Mulgara.NAMESPACE + "MockModel"));

        try {
          // this acquires mutex and holds it for 2s
          Answer answer = session1.query(createQuery(delayTwoSecs));
          Thread.sleep(100L);
          answer.beforeFirst();
          assertFalse(answer.next());
          answer.close();
          fail("query should've gotten interrupted");
        } catch (TuplesException te) {
          logger.debug("query was interrupted", te);
        }
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  private void rollbackTimedOutTxn(Session session) throws QueryException {
    try {
      session.commit();
      fail("Commit should have failed due to transaction timeout");
    } catch (QueryException qe) {
    }

    logger.debug("Rolling back transaction on session");
    session.rollback();
    session.setAutoCommit(true);
  }

  public void testPrefixingWithUnbound() throws URISyntaxException {
    logger.warn("testPrefixingWithUnbound");
    URI fileURI  = new File("data/prefix-unbound.rdf").toURI();

    try {
      Session session = database.newSession();
      try {
        session.createModel(model5URI, null);
        session.setModel(model5URI, fileURI);

        Variable varA   = new Variable("a");
        Variable varB   = new Variable("b");
        Variable varT = new Variable("t");

        List<SelectElement> selectList = new ArrayList<SelectElement>(2);
        selectList.add(varA);
        selectList.add(varT);

        // Check data loaded
        Answer answer = session.query(new Query(
          selectList,                                       // SELECT
          new GraphResource(model5URI),                      // FROM
          new ConstraintConjunction(Arrays.asList(
              new ConstraintExpression[] {
                  new ConstraintImpl(varB,
                      new URIReferenceImpl(new URI("test:p01")),
                      new URIReferenceImpl(new URI("test:o01"))),
                  new ConstraintImpl(varB,
                      new URIReferenceImpl(new URI("test:p02")),
                      varA),
                  new ConstraintDisjunction(
                      new ConstraintConjunction(
                          new ConstraintImpl(varB,
                              new URIReferenceImpl(new URI("test:p03")),
                              new URIReferenceImpl(new URI("test:o03"))),
                          new ConstraintIs(varT,
                              new URIReferenceImpl(new URI("result:0")))),
                      new ConstraintIs(varT,
                              new URIReferenceImpl(new URI("result:1")))),
              })),                                          // WHERE
          null,                                             // HAVING
          Arrays.asList(new Order[] {                       // ORDER BY
            new Order(varA, true),
            new Order(varT, true),
          }),
          null,                                             // LIMIT
          0,                                                // OFFSET
          true,                                             // DISTINCT
          new UnconstrainedAnswer()                         // GIVEN
        ));

        String[][] results = {
          { "test:o02", "result:0" },
          { "test:o02", "result:1" },
          { "test:o04", "result:0" },
          { "test:o04", "result:1" },
        };
        answer.beforeFirst();
        String s = "comparing results:\n" + answer.getVariables();
        while (answer.next()) {
          s += "\n[";
          for (int i = 0; i < answer.getNumberOfVariables(); i++) {
            s += " " + answer.getObject(i);
          }
          s += " ]";
        }

        compareResults(results, answer);
        answer.close();

        session.removeModel(model5URI);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testDatabaseDelete() {
    database.delete();
    database = null;
  }


  /**
   * Test various transaction operations in auto-commit mode.
   */
  public void testAutoCommitTransactionOps() {
    logger.info("Testing autoCommitTransactionOps");

    try {
      Session session = database.newSession();
      try {
        // should be a no-op
        session.setAutoCommit(true);

        // commit should not be allowed
        try {
          session.commit();
          fail("Commit did not fail in auto-commit mode");
        } catch (QueryException qe) {
        }

        // rollback should not be allowed
        try {
          session.rollback();
          fail("Rollback did not fail in auto-commit mode");
        } catch (QueryException qe) {
        }

        // verify we're still good to go
        session.query(createQuery(modelURI)).close();

        // verify second setAutoCommit(false) is a no-op
        session.createModel(model3URI, null);
        session.setAutoCommit(false);
        session.setModel(model3URI, new File("data/xatest-model1.rdf").toURI());
        session.setAutoCommit(false);
        session.commit();

        Answer answer = session.query(createQuery(model3URI));
        compareResults(expectedResults(), answer);
        answer.close();

        session.removeModel(model3URI);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  /**
   * Test various operations after an explicit transaction fails.
   */
  public void testExplicitTransactionFailure() {
    logger.info("Testing transactionExplicitFailure");

    try {
      // query after failure should fail
      shouldFailExplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.query(createQuery(modelURI)).close();
        }
      }, "Query in failed transaction did not fail");

      // insert after failure should fail
      shouldFailExplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.insert(modelURI, Collections.singleton((Triple)new TripleImpl(
                                                    new URIReferenceImpl(URI.create("test:a")),
                                                    new URIReferenceImpl(URI.create("test:b")),
                                                    new URIReferenceImpl(URI.create("test:c")))));
        }
      }, "Insert in failed transaction did not fail");

      // commit after failure should fail
      shouldFailExplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.commit();
        }
      }, "Commit in failed transaction did not fail");

      // rollback after failure should succeed
      shouldSucceedExplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.rollback();
        }
      });

      // setAutoCommit(false) after failure should fail
      shouldFailExplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.setAutoCommit(false);
        }
      }, "setAutoCommit(false) in failed transaction did not fail");

      // setAutoCommit(true) after failure should succeed
      shouldSucceedExplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.setAutoCommit(true);
        }
      });
    } catch (Exception e) {
      fail(e);
    }
  }

  private void shouldFailExplicit(TestOp op, String msg) throws Exception {
    testExplicitTransactionFailureOp(op, true, msg);
  }

  private void shouldSucceedExplicit(TestOp op) throws Exception {
    testExplicitTransactionFailureOp(op, false, null);
  }

  private void testExplicitTransactionFailureOp(TestOp op, boolean shouldFail, String msg) throws Exception {
    Session session = database.newSession();
    try {
      // start tx
      session.setAutoCommit(false);

      // run bad query -> failed tx
      try {
        session.query(createQuery(URI.create("urn:no:such:model")));
        fail("Bad query failed to throw exception");
      } catch (QueryException qe) {
      }

      // run test op, verify it succeeds/fails, and reset
      if (shouldFail) {
        try {
          op.run(session);
          fail(msg);
        } catch (QueryException qe) {
        }
        session.setAutoCommit(true);
      } else {
        op.run(session);
      }

      // verify we're good to go
      session.query(createQuery(modelURI)).close();

      session.setAutoCommit(false);
      session.query(createQuery(modelURI)).close();
      session.commit();
    } finally {
      session.close();
    }
  }

  /**
   * Test various operations after an operation in auto-commit fails.
   */
  public void testAutoCommitTransactionFailure() {
    logger.info("Testing transactionAutoCommitFailure");

    try {
      // query after failure should succeed
      shouldSucceedImplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.query(createQuery(modelURI)).close();
        }
      });

      // insert after failure should succeed
      shouldSucceedImplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.insert(modelURI, Collections.singleton((Triple)new TripleImpl(
                                                    new URIReferenceImpl(URI.create("test:a")),
                                                    new URIReferenceImpl(URI.create("test:b")),
                                                    new URIReferenceImpl(URI.create("test:c")))));
        }
      });

      // commit should fail
      shouldFailImplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.commit();
        }
      }, "Commit after failed query did not fail");

      // rollback should fail
      shouldFailImplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.rollback();
        }
      }, "Rollback after failed query did not fail");

      // setAutoCommit(false) after failure should succeed
      shouldSucceedImplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.setAutoCommit(false);
          session.setAutoCommit(true);
        }
      });

      // setAutoCommit(true) after failure should succeed
      shouldSucceedImplicit(new TestOp() {
        public void run(Session session) throws Exception {
          session.setAutoCommit(true);
        }
      });
    } catch (Exception e) {
      fail(e);
    }
  }

  private void shouldFailImplicit(TestOp op, String msg) throws Exception {
    testImplicitTransactionFailureOp(op, true, msg);
  }

  private void shouldSucceedImplicit(TestOp op) throws Exception {
    testImplicitTransactionFailureOp(op, false, null);
  }

  private void testImplicitTransactionFailureOp(TestOp op, boolean shouldFail, String msg) throws Exception {
    Session session = database.newSession();
    try {
      // run bad query -> failed tx
      try {
        session.query(createQuery(URI.create("urn:no:such:model")));
        fail("Bad query failed to throw exception");
      } catch (QueryException qe) {
      }

      // run test op, verify it succeeds/fails
      if (shouldFail) {
        try {
          op.run(session);
          fail(msg);
        } catch (QueryException qe) {
        }
      } else {
        op.run(session);
      }

      // verify we're good to go
      session.query(createQuery(modelURI)).close();

      session.setAutoCommit(false);
      session.query(createQuery(modelURI)).close();
      session.setAutoCommit(true);
    } finally {
      session.close();
    }
  }

  private static interface TestOp {
    public void run(Session session) throws Exception;
  }

  /**
   * Test session close on still-active session.
   */
  public void testSessionClose() {
    logger.info("Testing sessionClose");

    // test close while waiting for write-lock
    try {
      Session session1 = database.newSession();
      session1.setAutoCommit(false);

      try {
        final Session session2 = database.newSession();

        Thread t1 = new Thread("closeTest") {
          public void run() {
            try {
              session2.setAutoCommit(false);
              fail("Acquired write-lock unexpectedly");
            } catch (QueryException qe) {
              logger.debug("Caught expected exception", qe);
            } finally {
              try {
                session2.close();
              } catch (QueryException qe) {
                logger.debug("Caught expected exception", qe);
              }
            }
          }
        };
        t1.start();
        Thread.sleep(100L);     // give thread some time to start and block

        assertTrue("second session should still be active", t1.isAlive());

        session2.close();

        try {
          t1.join(100L);
        } catch (InterruptedException ie) {
          // this could be the interrupt from the close(), so try again
          try {
            t1.join(100L);
          } catch (InterruptedException ie2) {
            logger.error("wait for thread-termination interrupted", ie2);
            fail(ie2);
          }
        }
        assertFalse("second session should've terminated", t1.isAlive());

      } finally {
        logger.debug("Releasing autocommit for session1");
        session1.setAutoCommit(true);
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }

    // test close while operation active in auto-commit
    try {
      final Session session1 = database.newSession();
      final URI delayTwoSecs = new URI("foo://mulgara/closeTest?active=l&hardWait=1000");
      session1.createModel(delayTwoSecs, new URI(Mulgara.NAMESPACE + "MockModel"));

      final boolean[] closing = new boolean[1];

      try {
        Thread t1 = new Thread("closeTest") {
          public void run() {
            try {
              Answer answer = session1.query(createQuery(delayTwoSecs));
              answer.close();
              synchronized (closing) {
                assertTrue("close didn't block", closing[0]);
              }
            } catch (Exception e) {
              fail(e);
            }
          }
        };
        t1.start();
        Thread.sleep(100L);     // give thread some time to start and block

        assertTrue("query should still be active", t1.isAlive());

        synchronized (closing) { closing[0] = true; }
        session1.close();
        synchronized (closing) { closing[0] = false; }

        try {
          t1.join(100L);
        } catch (InterruptedException ie) {
          // this could be the interrupt from the close(), so try again
          try {
            t1.join(100L);
          } catch (InterruptedException ie2) {
            logger.error("wait for thread-termination interrupted", ie2);
            fail(ie2);
          }
        }
        assertFalse("second session should've terminated", t1.isAlive());

      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  //
  // Internal methods
  //

  private Query createQuery(URI model) {
    Variable subjectVariable   = new Variable("subject");
    Variable predicateVariable = new Variable("predicate");
    Variable objectVariable    = new Variable("object");

    List<SelectElement> selectList = new ArrayList<SelectElement>(3);
    selectList.add(subjectVariable);
    selectList.add(predicateVariable);
    selectList.add(objectVariable);

    return new Query(
      selectList,                                       // SELECT
      new GraphResource(model),                         // FROM
      new ConstraintImpl(subjectVariable,               // WHERE
                     predicateVariable,
                     objectVariable),
      null,                                             // HAVING
      Arrays.asList(new Order[] {                       // ORDER BY
        new Order(subjectVariable, true),
        new Order(predicateVariable, true),
        new Order(objectVariable, true)
      }),
      null,                                             // LIMIT
      0,                                                // OFFSET
      true,                                             // DISTINCT
      new UnconstrainedAnswer()                         // GIVEN
    );
  }

  private String[][] expectedResults() {
    return new String[][] {
          { "test:s01", "test:p01", "test:o01" },
          { "test:s01", "test:p02", "test:o01" },
          { "test:s01", "test:p02", "test:o02" },
          { "test:s01", "test:p03", "test:o02" },
          { "test:s02", "test:p03", "test:o02" },
          { "test:s02", "test:p04", "test:o02" },
          { "test:s02", "test:p04", "test:o03" },
          { "test:s02", "test:p05", "test:o03" },
          { "test:s03", "test:p01", "test:o01" },
          { "test:s03", "test:p05", "test:o03" },
          { "test:s03", "test:p06", "test:o01" },
          { "test:s03", "test:p06", "test:o03" },
        };
  }

  private void compareResults(String[][] expected, Answer answer) throws Exception {
    try {
      answer.beforeFirst();
      for (int i = 0; i < expected.length; i++) {
        assertTrue("Answer short at row " + i, answer.next());
        assertEquals(expected[i].length, answer.getNumberOfVariables());
        for (int j = 0; j < expected[i].length; j++) {
          URIReferenceImpl uri = new URIReferenceImpl(new URI(expected[i][j]));
          assertEquals(uri, answer.getObject(j));
        }
      }
      assertFalse(answer.next());
    } catch (Exception e) {
      logger.error("Failed test - " + answer);
      answer.close();
      throw e;
    }
  }

  private void compareResults(Answer answer1, Answer answer2) throws Exception {
    answer1.beforeFirst();
    answer2.beforeFirst();
    assertEquals(answer1.getNumberOfVariables(), answer2.getNumberOfVariables());
    while (answer1.next()) {
      assertTrue(answer2.next());
      for (int i = 0; i < answer1.getNumberOfVariables(); i++) {
        assertEquals(answer1.getObject(i), answer2.getObject(i));
      }
    }
    assertFalse(answer2.next());
  }

  public void testCreateModel() throws URISyntaxException {
    logger.info("testCreateModel");

    try {
      // Load some test data
      Session session = database.newSession();
      try {
        session.createModel(modelURI, null);
        try {
          session.createModel(modelURI, xsdModelTypeURI);
          assertFalse("createModel should have thrown QueryException", true);
        } catch (QueryException eq) {
          // We expect this to be thrown.
        }
        session.createModel(modelURI, null);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  /**
   * Fail with an unexpected exception
   */
  private void fail(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    fail(stringWriter.toString());
  }
}
