/*
 * Copyright 2008 The Topaz Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
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
import java.util.List;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

// Third party packages
import junit.framework.*;        // JUnit
import org.apache.log4j.Logger;  // Log4J
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.objectweb.jotm.Jotm;
import org.objectweb.transaction.jta.TMService;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.rdf.TripleImpl;
import org.mulgara.server.Session;

import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;

/**
 * Regression test to test JTA integration with external JOTM instance.
 *
 * @created 2008-01-11
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 * @company <A href="mailto:mail@netymon.com">Netymon Pty Ltd</A>
 * @copyright &copy;2008 <a href="http://www.topazproject.org/">The Topaz Foundation</a>
 * @licence Apache License v2.0
 */
public class JotmTransactionStandaloneIntTst extends TestCase {
  /** Logger.  */
  private static Logger logger =
    Logger.getLogger(JotmTransactionStandaloneIntTst.class.getName());

  private static final URI databaseURI;


  private static final URI modelURI;
  private static final URI model2URI;

  static {
    try {
      databaseURI    = new URI("rmi://localhost/server1");
      modelURI       = new URI("rmi://localhost/server1#jotmmodel");
      model2URI      = new URI("rmi://localhost/server1#jotmmodel2");
    } catch (URISyntaxException e) {
      throw new Error("Bad hardcoded URI", e);
    }
  }

  private static SessionFactory sessionFactory;
  private static TMService txService;
  private static TransactionManager txManager;

  public JotmTransactionStandaloneIntTst(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new JotmTransactionStandaloneIntTst("setup"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testTrivalExplicit"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testSessionCloseRollback"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testTrivialExplicitAgain"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testBasicQuery"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testMultipleEnlist"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testMultipleQuery"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testBasicReadOnlyQuery"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testConcurrentQuery"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testRepeatGetXAQuery"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testConcurrentReadWrite"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testSubqueryQuery"));
    suite.addTest(new JotmTransactionStandaloneIntTst("testTrivalImplicit"));
    suite.addTest(new JotmTransactionStandaloneIntTst("cleanup"));

    return suite;
  }


  public void setup() throws Exception {
    logger.info("Doing setup");
    sessionFactory = SessionFactoryFinder.newSessionFactory(databaseURI);
    txService = new Jotm(true, false); // local, unbound.
    txManager = txService.getTransactionManager();
  }

  public void cleanup() throws Exception {
    logger.info("Doing cleanup");
    txService.stop();
  }

  //
  // Test cases
  //

  @SuppressWarnings("unused")
  private static class TestXid implements Xid {
    private int xid;
    public TestXid(int xid) {
      this.xid = xid;
    }
    
    public int getFormatId() {
      return 'X';
    }

    public byte[] getBranchQualifier() {
      return new byte[] {
        (byte)(xid >> 0x00),
        (byte)(xid >> 0x08)
      };
    }

    public byte[] getGlobalTransactionId() {
      return new byte[] {
        (byte)(xid >> 0x10),
        (byte)(xid >> 0x18)
      };
    }
  }

  /**
   * Test explicit transaction.
   * As a side-effect, creates the model required by the next tests.
   */
  public void testTrivalExplicit() throws URISyntaxException {
    logger.info("testTrivalExplicit");
    try {
      txManager.begin();
      Session session = sessionFactory.newSession();
      txManager.getTransaction().enlistResource(session.getXAResource());

      try {
        session.createModel(modelURI, null);
        txManager.commit();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testSessionCloseRollback() throws URISyntaxException {
    logger.info("testSessionCloseRollback");
    try {
      URI fileURI  = new File("data/xatest-model1.rdf").toURI();
      txManager.begin();
      Session session = sessionFactory.newSession();
      txManager.getTransaction().enlistResource(session.getXAResource());

      try {
        try {
          session.setModel(modelURI, fileURI);
        } finally {
          session.close();
        }
      } finally {
        try {
          txManager.commit();
        } catch (HeuristicRollbackException eh) { // This is my expectation.
          logger.warn("HeuristicRollback detected successfully", eh);
        } catch (RollbackException er) {          // This would also meet the spec.
          logger.warn("Rollback detected successfully", er);
        } catch (Exception e) {
          logger.warn("Exception from Jotm", e);
          throw e;
        }
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testTrivialExplicitAgain() throws URISyntaxException {
    logger.info("testTrivialExplicitAgain");
    try {
      URI fileURI  = new File("data/xatest-model1.rdf").toURI();
      txManager.begin();
      Session session = sessionFactory.newSession();
      txManager.getTransaction().enlistResource(session.getXAResource());

      try {
        session.setModel(modelURI, fileURI);
        txManager.commit();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testBasicQuery() throws URISyntaxException {
    logger.info("testBasicQuery");

    try {
      txManager.begin();
      Session session = sessionFactory.newSession();
      try {
        txManager.getTransaction().enlistResource(session.getXAResource());

        // Evaluate the query
        Answer answer = session.query(createQuery(modelURI));
        compareResults(expectedResults(), answer);
        answer.close();

        txManager.commit();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testMultipleEnlist() throws URISyntaxException {
    logger.info("testMultipleEnlist");

    try {
      txManager.begin();
      Session session = sessionFactory.newSession();
      try {
        txManager.getTransaction().enlistResource(session.getXAResource());
        txManager.getTransaction().enlistResource(session.getXAResource());
        txManager.getTransaction().enlistResource(session.getXAResource());

        // Evaluate the query
        Answer answer = session.query(createQuery(modelURI));
        compareResults(expectedResults(), answer);
        answer.close();

        txManager.commit();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  public void testMultipleQuery() throws URISyntaxException {
    logger.info("testMultipleQuery");

    try {
      txManager.begin();
      Session session = sessionFactory.newSession();
      txManager.getTransaction().enlistResource(session.getXAResource());
      try {
        // Evaluate the query
        Answer answer1 = session.query(createQuery(modelURI));

        Answer answer2 = session.query(createQuery(modelURI));

        compareResults(answer1, answer2);

        answer1.close();
        answer2.close();

        txManager.commit();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testBasicReadOnlyQuery() throws URISyntaxException {
    logger.info("testBasicReadOnlyQuery");

    try {
      txManager.begin();
      Session session = sessionFactory.newSession();
      try {
        txManager.getTransaction().enlistResource(session.getReadOnlyXAResource());

        // Evaluate the query
        Answer answer = session.query(createQuery(modelURI));
        compareResults(expectedResults(), answer);
        answer.close();

        txManager.commit();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testConcurrentQuery() throws URISyntaxException {
    logger.info("testConcurrentQuery");

    try {
      txManager.begin();
      Session session = sessionFactory.newSession();
      XAResource roResource = session.getReadOnlyXAResource();
      Transaction tx1 = txManager.getTransaction();
      tx1.enlistResource(roResource);

      try {
        // Evaluate the query
        Answer answer1 = session.query(createQuery(modelURI));

        tx1 = txManager.suspend();

        txManager.begin();
        Transaction tx2 = txManager.getTransaction();
        tx2.enlistResource(roResource);

        Answer answer2 = session.query(createQuery(modelURI));

        tx2 = txManager.suspend();

        compareResults(answer1, answer2);

        answer1.close();
        answer2.close();

        txManager.resume(tx1);
        txManager.commit();
        // I believe JTA requires me to call end here - our implementation doesn't care.
        tx2.commit();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testRepeatGetXAQuery() throws URISyntaxException {
    logger.info("testRepeatGetXAQuery");

    try {
      txManager.begin();
      Session session = sessionFactory.newSession();
      Transaction tx1 = txManager.getTransaction();
      tx1.enlistResource(session.getReadOnlyXAResource());

      try {
        // Evaluate the query
        Answer answer1 = session.query(createQuery(modelURI));

        tx1 = txManager.suspend();

        txManager.begin();
        Transaction tx2 = txManager.getTransaction();
        tx2.enlistResource(session.getReadOnlyXAResource());

        Answer answer2 = session.query(createQuery(modelURI));

        tx2 = txManager.suspend();

        compareResults(answer1, answer2);

        answer1.close();
        answer2.close();

        txManager.resume(tx1);
        txManager.commit();
        // I believe JTA requires me to call end here - our implementation doesn't care.
        tx2.commit();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  //
  // Note: What this test does is a really bad idea - there is no
  //       isolation provided as each operation is within its own
  //       transaction.  It does however provide a good test.
  //
  public void testConcurrentReadWrite() throws URISyntaxException {
    logger.info("testConcurrentReadWrite");

    try {
      txManager.begin();
      Session session = sessionFactory.newSession();
      XAResource roResource = session.getReadOnlyXAResource();
      XAResource rwResource = session.getXAResource();

      txManager.getTransaction().enlistResource(rwResource);
      session.createModel(model2URI, null);
      Transaction tx1 = txManager.suspend();

      try {
        txManager.begin();
        txManager.getTransaction().enlistResource(roResource);

        // Evaluate the query
        Answer answer = session.query(createQuery(modelURI));

        Transaction tx2 = txManager.suspend();

        answer.beforeFirst();
        while (answer.next()) {
          txManager.resume(tx1);
          session.insert(model2URI, Collections.singleton(new TripleImpl(
              (SubjectNode)answer.getObject(0),
              (PredicateNode)answer.getObject(1),
              (ObjectNode)answer.getObject(2))));
          tx1 = txManager.suspend();
        }
        answer.close();

        txManager.resume(tx1);
        txManager.commit();

        txManager.begin();
        txManager.getTransaction().enlistResource(roResource);

        Answer answer2 = session.query(createQuery(model2URI));

        Transaction tx3 = txManager.suspend();

        compareResults(expectedResults(), answer2);
        answer2.close();

        txManager.begin();
        txManager.getTransaction().enlistResource(rwResource);
        session.removeModel(model2URI);
        txManager.commit();

        txManager.resume(tx2);
        txManager.commit();
        txManager.resume(tx3);
        txManager.commit();
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testSubqueryQuery() throws URISyntaxException {
    logger.info("testSubqueryQuery");

    try {
      txManager.begin();
      Session session = sessionFactory.newSession();
      txManager.getTransaction().enlistResource(session.getReadOnlyXAResource());

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

        txManager.suspend();

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

        // Leave transaction to be closed on session close.
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

/*

  public void testConcurrentSubqueryQuery() throws URISyntaxException {
    logger.info("testConcurrentSubqueryQuery");

    try {
      Session session = database.newSession();
      XAResource rwResource = session.getXAResource();
      Xid xid1 = new TestXid(1);
      rwResource.start(xid1, XAResource.TMNOFLAGS);

      try {
        Variable subjectVariable   = new Variable("subject");
        Variable predicateVariable = new Variable("predicate");
        Variable objectVariable    = new Variable("object");

        List selectList = new ArrayList(3);
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

        rwResource.end(xid1, XAResource.TMSUSPEND);

        assertTrue(sub1.next());
        assertTrue(sub2.next());
        assertEquals(new URIReferenceImpl(new URI("test:o02")), sub1.getObject(0));
        assertEquals(new URIReferenceImpl(new URI("test:o03")), sub2.getObject(0));
        assertFalse(sub1.next());
        assertFalse(sub2.next());

        answer.close();

        rwResource.end(xid1, XAResource.TMSUCCESS);
        rwResource.commit(xid1, true);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testExplicitIsolationQuerySingleSession() throws URISyntaxException {
    logger.info("testExplicitIsolationQuery");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session = database.newSession();
      try {
        XAResource roResource = session.getReadOnlyXAResource();
        XAResource rwResource = session.getXAResource();
        Xid xid1 = new TestXid(1); // Initial create model.
        Xid xid2 = new TestXid(2); // Started before setModel.
        Xid xid3 = new TestXid(3); // setModel.
        Xid xid4 = new TestXid(4); // Started before setModel prepares
        Xid xid5 = new TestXid(5); // Started before setModel commits
        Xid xid6 = new TestXid(6); // Started after setModel commits
        Xid xid7 = new TestXid(7); // Final remove model.

        rwResource.start(xid1, XAResource.TMNOFLAGS);
        session.createModel(model3URI, null);
        rwResource.end(xid1, XAResource.TMSUCCESS);
        rwResource.commit(xid1, true);

        // Nothing visible.
        roResource.start(xid2, XAResource.TMNOFLAGS);
        assertChangeNotVisible(session);
        roResource.end(xid2, XAResource.TMSUSPEND);

        // Perform update
        rwResource.start(xid3, XAResource.TMNOFLAGS);
        session.setModel(model3URI, new GraphResource(fileURI));
        rwResource.end(xid3, XAResource.TMSUSPEND);

        // Check uncommitted change not visible
        roResource.start(xid4, XAResource.TMNOFLAGS);
        assertChangeNotVisible(session);
        roResource.end(xid4, XAResource.TMSUSPEND);

        // Check original phase unaffected.
        roResource.start(xid2, XAResource.TMRESUME);
        assertChangeNotVisible(session);
        roResource.end(xid2, XAResource.TMSUSPEND);

        // Check micro-commit visible to current-phase
        rwResource.start(xid3, XAResource.TMRESUME);
        assertChangeVisible(session);
        // Perform prepare
        rwResource.end(xid3, XAResource.TMSUCCESS);
        rwResource.prepare(xid3);

        // Check original phase unaffected
        roResource.start(xid2, XAResource.TMRESUME);
        assertChangeNotVisible(session);
        roResource.end(xid2, XAResource.TMSUSPEND);

        // Check pre-prepare phase unaffected
        roResource.start(xid4, XAResource.TMRESUME);
        assertChangeNotVisible(session);
        roResource.end(xid4, XAResource.TMSUSPEND);

        // Check committed phase unaffected.
        roResource.start(xid5, XAResource.TMNOFLAGS);
        assertChangeNotVisible(session);
        roResource.end(xid5, XAResource.TMSUSPEND);

        // Do commit
        rwResource.commit(xid3, false);

        // Check original phase
        roResource.start(xid2, XAResource.TMRESUME);
        assertChangeNotVisible(session);
        roResource.end(xid2, XAResource.TMSUSPEND);

        // Check pre-prepare
        roResource.start(xid4, XAResource.TMRESUME);
        assertChangeNotVisible(session);
        roResource.end(xid4, XAResource.TMSUSPEND);

        // Check pre-commit
        roResource.start(xid5, XAResource.TMRESUME);
        assertChangeNotVisible(session);
        roResource.end(xid5, XAResource.TMSUSPEND);

        // Check committed phase is now updated
        roResource.start(xid6, XAResource.TMNOFLAGS);
        assertChangeVisible(session);

        // Cleanup transactions.
        roResource.end(xid6, XAResource.TMSUCCESS);
        roResource.end(xid2, XAResource.TMSUCCESS);
        roResource.end(xid4, XAResource.TMSUCCESS);
        roResource.end(xid5, XAResource.TMSUCCESS);
        roResource.commit(xid2, true);
        roResource.commit(xid4, true);
        roResource.commit(xid5, true);
        roResource.commit(xid6, true);

        // Cleanup database
        rwResource.start(xid7, XAResource.TMNOFLAGS);
        session.removeModel(model3URI);
        rwResource.end(xid7, XAResource.TMSUCCESS);
        rwResource.commit(xid7, true);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testExternalInternalIsolation() throws URISyntaxException {
    logger.info("testExplicitIsolationQuery");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        Session session2 = database.newSession();
        try {
          XAResource roResource = session1.getReadOnlyXAResource();
          XAResource rwResource = session1.getXAResource();
          Xid xid1 = new TestXid(1); // Initial create model.
          Xid xid2 = new TestXid(2); // Main Test.
          Xid xid3 = new TestXid(3); // Cleanup test.

          rwResource.start(xid1, XAResource.TMNOFLAGS);
          session1.createModel(model3URI, null);
          rwResource.end(xid1, XAResource.TMSUCCESS);
          rwResource.commit(xid1, true);

          // Nothing visible.
          assertChangeNotVisible(session2);

          // Perform update
          rwResource.start(xid2, XAResource.TMNOFLAGS);
          session1.setModel(model3URI, new GraphResource(fileURI));
          rwResource.end(xid2, XAResource.TMSUSPEND);

          // Check uncommitted change not visible
          assertChangeNotVisible(session2);

          // Check micro-commit visible to current-phase
          rwResource.start(xid2, XAResource.TMRESUME);
          assertChangeVisible(session1);
          // Perform prepare
          rwResource.end(xid2, XAResource.TMSUCCESS);
          rwResource.prepare(xid2);

          // Check original phase unaffected
          assertChangeNotVisible(session2);

          // Do commit
          rwResource.commit(xid2, false);

          // Check committed phase is now updated
          assertChangeVisible(session2);

          // Cleanup database
          session2.removeModel(model3URI);
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

  public void testInternalExternalIsolation() throws URISyntaxException {
    logger.info("testExplicitIsolationQuery");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        Session session2 = database.newSession();
        try {
          XAResource roResource = session2.getReadOnlyXAResource();
          XAResource rwResource = session2.getXAResource();
          Xid xid1 = new TestXid(1); // Pre-update
          Xid xid2 = new TestXid(2); // Post-update/Pre-commit
          Xid xid3 = new TestXid(3); // Post-commit

          session1.createModel(model3URI, null);

          // Nothing visible.
          roResource.start(xid1, XAResource.TMNOFLAGS);
          assertChangeNotVisible(session2);
          roResource.end(xid1, XAResource.TMSUSPEND);

          // Perform update with autocommit off
          session1.setAutoCommit(false);
          session1.setModel(model3URI, new GraphResource(fileURI));

          // Check uncommitted change not visible
          roResource.start(xid2, XAResource.TMNOFLAGS);
          assertChangeNotVisible(session2);
          roResource.end(xid2, XAResource.TMSUSPEND);

          // Check original phase unaffected.
          roResource.start(xid1, XAResource.TMRESUME);
          assertChangeNotVisible(session2);
          roResource.end(xid1, XAResource.TMSUSPEND);

          // Check micro-commit visible to current-phase
          assertChangeVisible(session1);
          session1.setAutoCommit(true);

          // Check original phase unaffected
          roResource.start(xid1, XAResource.TMRESUME);
          assertChangeNotVisible(session2);
          roResource.end(xid1, XAResource.TMSUSPEND);

          // Check pre-commit phase unaffected
          roResource.start(xid2, XAResource.TMRESUME);
          assertChangeNotVisible(session2);
          roResource.end(xid2, XAResource.TMSUSPEND);

          // Check committed phase is now updated and write-lock available
          rwResource.start(xid3, XAResource.TMNOFLAGS);
          assertChangeVisible(session2);
          
          // Check internal transaction read-only
          assertChangeVisible(session1);

          // Cleanup transactions.
          rwResource.end(xid3, XAResource.TMSUCCESS);
          roResource.end(xid2, XAResource.TMSUCCESS);
          roResource.end(xid1, XAResource.TMSUCCESS);
          roResource.commit(xid1, true);
          roResource.commit(xid2, true);
          rwResource.commit(xid3, true);

          // Cleanup database (check write-lock available again)
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

  private void assertChangeVisible(Session session) throws Exception {
    // Evaluate the query
    Answer answer = session.query(createQuery(model3URI));

    compareResults(expectedResults(), answer);
    answer.close();
  }

  private void assertChangeNotVisible(Session session) throws Exception {
    // Evaluate the query
    Answer answer = session.query(createQuery(model3URI));
    answer.beforeFirst();
    assertFalse(answer.next());
    answer.close();
  }

  //
  // Test two simultaneous, explicit transactions, in two threads. The second one should block
  // until the first one sets auto-commit back to true.
  //
  public void testConcurrentExplicitTxn() throws URISyntaxException {
    logger.info("testConcurrentExplicitTxn");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        XAResource resource1 = session1.getXAResource();
        resource1.start(new TestXid(1), XAResource.TMNOFLAGS);
        session1.createModel(model3URI, null);
        resource1.end(new TestXid(1), XAResource.TMSUCCESS);
        resource1.commit(new TestXid(1), true);

        resource1.start(new TestXid(2), XAResource.TMNOFLAGS);
        session1.setModel(model3URI, new GraphResource(fileURI));

        final boolean[] tx2Started = new boolean[] { false };

        Thread t2 = new Thread("tx2Test") {
          public void run() {
            try {
              Session session2 = database.newSession();
              XAResource resource2 = session2.getXAResource();
              try {
                resource2.start(new TestXid(3), XAResource.TMNOFLAGS);

                synchronized (tx2Started) {
                  tx2Started[0] = true;
                  tx2Started.notify();
                }

                // Evaluate the query
                Answer answer = session2.query(createQuery(model3URI));

                compareResults(expectedResults(), answer);
                answer.close();

                resource2.end(new TestXid(3), XAResource.TMSUCCESS);
                resource2.commit(new TestXid(3), true);
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

        resource1.commit(new TestXid(2), true);

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

        resource1.start(new TestXid(4), XAResource.TMNOFLAGS);
        session1.removeModel(model3URI);
        resource1.end(new TestXid(4), XAResource.TMSUCCESS);
        resource1.commit(new TestXid(4), true);

      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  //*
  // Test two simultaneous transactions, in two threads. The second one should block
  // until the first one sets auto-commit back to true.
  ///
  public void testExternalInternalConcurrentTxn() throws URISyntaxException {
    logger.info("testExternalInternalConcurrentTxn");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        XAResource resource1 = session1.getXAResource();
        resource1.start(new TestXid(1), XAResource.TMNOFLAGS);
        session1.createModel(model3URI, null);
        resource1.end(new TestXid(1), XAResource.TMSUCCESS);
        resource1.commit(new TestXid(1), true);

        resource1.start(new TestXid(2), XAResource.TMNOFLAGS);
        session1.setModel(model3URI, new GraphResource(fileURI));

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

        resource1.commit(new TestXid(2), true);

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

        resource1.start(new TestXid(4), XAResource.TMNOFLAGS);
        session1.removeModel(model3URI);
        resource1.end(new TestXid(4), XAResource.TMSUCCESS);
        resource1.commit(new TestXid(4), true);

      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  //*
  // Test two simultaneous transactions, in two threads. The second one should block
  // until the first one sets auto-commit back to true.
  ///
  public void testInternalExternalConcurrentTxn() throws URISyntaxException {
    logger.info("testInternalExternalConcurrentTxn");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        session1.createModel(model3URI, null);

        session1.setAutoCommit(false);
        session1.setModel(model3URI, new GraphResource(fileURI));

        final boolean[] tx2Started = new boolean[] { false };

        Thread t2 = new Thread("tx2Test") {
          public void run() {
            try {
              Session session2 = database.newSession();
              try {
                XAResource resource = session2.getXAResource();
                resource.start(new TestXid(1), XAResource.TMNOFLAGS);

                synchronized (tx2Started) {
                  tx2Started[0] = true;
                  tx2Started.notify();
                }

                // Evaluate the query
                Answer answer = session2.query(createQuery(model3URI));

                compareResults(expectedResults(), answer);
                answer.close();

                resource.end(new TestXid(1), XAResource.TMSUCCESS);
                resource.rollback(new TestXid(1));
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

  //*
  // Test two simultaneous transactions, in two threads. The second one should block
  // until the first one sets auto-commit back to true.
  ///
  public void testExternalInternalConcurrentTxnRollback() throws URISyntaxException {
    logger.info("testExternalInternalConcurrentTxnRollback");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        XAResource resource1 = session1.getXAResource();
        resource1.start(new TestXid(1), XAResource.TMNOFLAGS);
        session1.createModel(model3URI, null);
        resource1.end(new TestXid(1), XAResource.TMSUCCESS);
        resource1.commit(new TestXid(1), true);

        resource1.start(new TestXid(2), XAResource.TMNOFLAGS);
        session1.setModel(model3URI, new GraphResource(fileURI));

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

                answer.beforeFirst();
                assertFalse(answer.next());
                answer.close();

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

        resource1.rollback(new TestXid(2));

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

        resource1.start(new TestXid(4), XAResource.TMNOFLAGS);
        session1.removeModel(model3URI);
        resource1.end(new TestXid(4), XAResource.TMSUCCESS);
        resource1.commit(new TestXid(4), true);

      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  //*
  // Test two simultaneous transactions, in two threads. The second one should block
  // until the first one sets auto-commit back to true.
  ///
  public void testInternalExternalConcurrentTxnRollback() throws URISyntaxException {
    logger.info("testInternalExternalConcurrentTxnRollback");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      try {
        session1.createModel(model3URI, null);

        session1.setAutoCommit(false);
        session1.setModel(model3URI, new GraphResource(fileURI));

        final boolean[] tx2Started = new boolean[] { false };

        Thread t2 = new Thread("tx2Test") {
          public void run() {
            try {
              Session session2 = database.newSession();
              try {
                XAResource resource = session2.getXAResource();
                resource.start(new TestXid(1), XAResource.TMNOFLAGS);

                synchronized (tx2Started) {
                  tx2Started[0] = true;
                  tx2Started.notify();
                }

                // Evaluate the query
                Answer answer = session2.query(createQuery(model3URI));

                answer.beforeFirst();
                assertFalse(answer.next());
                answer.close();

                resource.end(new TestXid(1), XAResource.TMFAIL);
                resource.rollback(new TestXid(1));
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

        session1.rollback();

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


  public void testExplicitRollbackIsolationQuery() throws URISyntaxException {
    logger.info("testExplicitRollbackIsolationQuery");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session = database.newSession();
      XAResource roResource = session.getReadOnlyXAResource();
      XAResource rwResource = session.getXAResource();
      try {
        rwResource.start(new TestXid(1), XAResource.TMNOFLAGS);
        session.createModel(model3URI, null);
        rwResource.end(new TestXid(1), XAResource.TMSUCCESS);
        rwResource.commit(new TestXid(1), true);

        rwResource.start(new TestXid(2), XAResource.TMNOFLAGS);
        session.setModel(model3URI, new GraphResource(fileURI));
        rwResource.end(new TestXid(2), XAResource.TMSUSPEND);

        roResource.start(new TestXid(3), XAResource.TMNOFLAGS);

        // Evaluate the query
        Answer answer = session.query(createQuery(model3URI));
        answer.beforeFirst();
        assertFalse(answer.next());
        answer.close();

        roResource.end(new TestXid(3), XAResource.TMSUCCESS);
        roResource.commit(new TestXid(3), true);

        rwResource.end(new TestXid(2), XAResource.TMFAIL);
        rwResource.rollback(new TestXid(2));

        roResource.start(new TestXid(4), XAResource.TMNOFLAGS);
        selectList = new ArrayList(3);
        selectList.add(subjectVariable);
        selectList.add(predicateVariable);
        selectList.add(objectVariable);

        // Evaluate the query
        answer = session.query(createQuery(model3URI));

        answer.beforeFirst();
        assertFalse(answer.next());
        answer.close();

        roResource.end(new TestXid(4), XAResource.TMFAIL);
        roResource.rollback(new TestXid(4));
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }
*/

  public void testTrivalImplicit() throws URISyntaxException {
    logger.info("testTrivialImplicit");
    try {
      Session session = sessionFactory.newSession();

      try {
        session.removeModel(modelURI);
      } finally {
        session.close();
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
      logger.info("Failed test - " + answer);
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

  /**
   * Fail with an unexpected exception
   */
  private void fail(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    fail(stringWriter.toString());
  }

  @SuppressWarnings("unused")
  private static class DummyXAResource implements XAResource {
    public void end(Xid xid, int flags) throws XAException {}
    public void forget(Xid xid) throws XAException {}
    public int getTransactionTimeout() throws XAException { return 0; }
    public int prepare(Xid xid) throws XAException { return 0; }
    public Xid[] recover(int flag) throws XAException { return new Xid[] {}; }
    public void rollback(Xid xid) throws XAException {}
    public boolean setTransactionTimeout(int seconds) throws XAException { return false; }
    public void start(Xid xid, int flags) throws XAException {}
    public void commit(Xid xid, boolean twophase) throws XAException {}
    public boolean isSameRM(XAResource xa) { return xa == this; }
  }
}
