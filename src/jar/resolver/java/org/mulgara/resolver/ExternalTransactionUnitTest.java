/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.rosenlaw.com/OSL3.0.htm
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * This file is an original work developed by Netymon Pty Ltd
 * (http://www.netymon.com, mailto:mail@netymon.com) under contract to 
 * Topaz Foundation. Portions created under this contract are
 * Copyright (c) 2007 Topaz Foundation
 * All Rights Reserved.
 *
 * scaffolding based on AdvDatabaseSessionUnitTest.java
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

// Third party packages
import junit.framework.*;        // JUnit
import org.apache.log4j.Logger;  // Log4J
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.ObjectNode;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.rdf.TripleImpl;
import org.mulgara.resolver.spi.DummyXAResource;
import org.mulgara.server.Session;
import org.mulgara.util.FileUtil;

/**
 * Testing Externally Mediated Transactions. 
 *
 * @created 2007-11-27
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 * @company <a href="http://www.netymon.com/">Netymon Pty Ltd</a>
 *      Software Pty Ltd</a>
 * @copyright &copy;2006 <a href="http://www.topazproject.org/">Topaz Project
 * Foundation</a>
 *
 * @licence Open Software License v3.0</a>
 */

public class ExternalTransactionUnitTest extends TestCase {
  /** Logger.  */
  private static Logger logger =
    Logger.getLogger(ExternalTransactionUnitTest.class.getName());

  private static final URI databaseURI;


  private static final URI modelURI;
  private static final URI model2URI;
  private static final URI model3URI;
  private static final URI model4URI;

  static {
    try {
      databaseURI    = new URI("local://database");
      modelURI       = new URI("local://database#model");
      model2URI      = new URI("local://database#model2");
      model3URI      = new URI("local://database#model3");
      model4URI      = new URI("local://database#model4");
    } catch (URISyntaxException e) {
      throw new Error("Bad hardcoded URI", e);
    }
  }

  private static Database database = null;

  public ExternalTransactionUnitTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new ExternalTransactionUnitTest("testSimpleOnePhaseCommit"));
    suite.addTest(new ExternalTransactionUnitTest("testSimpleTwoPhaseCommit"));
    suite.addTest(new ExternalTransactionUnitTest("testBasicQuery"));
    suite.addTest(new ExternalTransactionUnitTest("testBasicUpdate"));
    suite.addTest(new ExternalTransactionUnitTest("testMultipleQuery"));
    suite.addTest(new ExternalTransactionUnitTest("testBasicReadOnlyQuery"));
    suite.addTest(new ExternalTransactionUnitTest("testConcurrentQuery"));
    suite.addTest(new ExternalTransactionUnitTest("testConcurrentReadWrite"));
    suite.addTest(new ExternalTransactionUnitTest("testSubqueryQuery"));
    suite.addTest(new ExternalTransactionUnitTest("testConcurrentSubqueryQuery"));
    suite.addTest(new ExternalTransactionUnitTest("testExplicitIsolationQuerySingleSession"));
    suite.addTest(new ExternalTransactionUnitTest("testConcurrentExplicitTxn"));
    suite.addTest(new ExternalTransactionUnitTest("testExplicitRollbackIsolationQuery"));
    suite.addTest(new ExternalTransactionUnitTest("testExternalInternalIsolation"));
    suite.addTest(new ExternalTransactionUnitTest("testInternalExternalIsolation"));
    suite.addTest(new ExternalTransactionUnitTest("testInternalExternalConcurrentTxn"));
    suite.addTest(new ExternalTransactionUnitTest("testExternalInternalConcurrentTxn"));
    suite.addTest(new ExternalTransactionUnitTest("testInternalExternalConcurrentTxnRollback"));
    suite.addTest(new ExternalTransactionUnitTest("testExternalInternalConcurrentTxnRollback"));
    suite.addTest(new ExternalTransactionUnitTest("testInternalSerialMultipleSessions"));
    suite.addTest(new ExternalTransactionUnitTest("testTransactionTimeout"));
    suite.addTest(new ExternalTransactionUnitTest("testTransactionFailure"));
    suite.addTest(new ExternalTransactionUnitTest("testSessionClose"));
    suite.addTest(new ExternalTransactionUnitTest("testResourceActivation"));

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
   * Test single-phase commit.
   * As a side-effect, creates the model required by the next tests.
   */
  public void testSimpleOnePhaseCommit() throws URISyntaxException {
    logger.info("testSimpleOnePhaseCommit");

    try {
      DatabaseSession session = (DatabaseSession)database.newSession();
      XAResource resource = session.getXAResource();
      Xid xid = new TestXid(1);
      resource.start(xid, XAResource.TMNOFLAGS);
      try {
        session.createModel(modelURI, null);
        resource.end(xid, XAResource.TMSUCCESS);
        resource.commit(xid, true);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  /**
   * Test two phase commit.
   * As a side-effect, loads the model required by the next tests.
   */
  public void testSimpleTwoPhaseCommit() throws URISyntaxException {
    logger.info("testSimpleTwoPhaseCommit");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      DatabaseSession session = (DatabaseSession)database.newSession();
      XAResource resource = session.getXAResource();
      Xid xid = new TestXid(1);
      resource.start(xid, XAResource.TMNOFLAGS);
      try {
        session.setModel(modelURI, fileURI);
        resource.end(xid, XAResource.TMSUCCESS);
        resource.prepare(xid);
        resource.commit(xid, false);
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
      DatabaseSession session = (DatabaseSession)database.newSession();
      try {
        XAResource resource = session.getXAResource();
        Xid xid = new TestXid(1);
        resource.start(xid, XAResource.TMNOFLAGS);

        // Evaluate the query
        Answer answer = session.query(createQuery(modelURI));
        compareResults(expectedResults(), answer);
        answer.close();

        resource.end(xid, XAResource.TMSUCCESS);
        resource.commit(xid, true);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testBasicUpdate() throws URISyntaxException {
    logger.info("Testing basicUpdate");

    // straight insert and delete
    try {
      DatabaseSession session = (DatabaseSession)database.newSession();
      try {
        // start txn
        XAResource resource = session.getXAResource();
        Xid xid = new TestXid(1);
        resource.start(xid, XAResource.TMNOFLAGS);

        session.createModel(model2URI, null);

        // insert data
        session.insert(model2URI, Collections.singleton(new TripleImpl(
            new URIReferenceImpl(URI.create("test:a")),
            new URIReferenceImpl(URI.create("test:b")),
            new URIReferenceImpl(URI.create("test:c")))));

        // check it
        Answer answer = session.query(createQuery(model2URI));
        answer.beforeFirst();
        assertTrue(answer.next());
        assertEquals(new URIReferenceImpl(new URI("test:a")), answer.getObject(0));
        assertEquals(new URIReferenceImpl(new URI("test:b")), answer.getObject(1));
        assertEquals(new URIReferenceImpl(new URI("test:c")), answer.getObject(2));
        assertFalse(answer.next());
        answer.close();

        // delete it
        session.delete(model2URI, Collections.singleton(new TripleImpl(
            new URIReferenceImpl(URI.create("test:a")),
            new URIReferenceImpl(URI.create("test:b")),
            new URIReferenceImpl(URI.create("test:c")))));

        // check it
        answer = session.query(createQuery(model2URI));
        answer.beforeFirst();
        assertFalse(answer.next());
        answer.close();

        session.removeModel(model2URI);

        resource.end(xid, XAResource.TMSUCCESS);
        resource.commit(xid, true);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }

    // insert-select and delete-select
    try {
      DatabaseSession session = (DatabaseSession)database.newSession();
      try {
        // start txn
        XAResource resource = session.getXAResource();
        Xid xid = new TestXid(1);
        resource.start(xid, XAResource.TMNOFLAGS);

        session.createModel(model2URI, null);
        session.createModel(model3URI, null);

        // insert data
        session.insert(model2URI, Collections.singleton(new TripleImpl(
            new URIReferenceImpl(URI.create("test:a")),
            new URIReferenceImpl(URI.create("test:b")),
            new URIReferenceImpl(URI.create("test:c")))));

        // insert-select
        session.insert(model3URI, createQuery(model2URI));

        // check it
        Answer answer = session.query(createQuery(model3URI));
        answer.beforeFirst();
        assertTrue(answer.next());
        assertEquals(new URIReferenceImpl(new URI("test:a")), answer.getObject(0));
        assertEquals(new URIReferenceImpl(new URI("test:b")), answer.getObject(1));
        assertEquals(new URIReferenceImpl(new URI("test:c")), answer.getObject(2));
        assertFalse(answer.next());
        answer.close();

        // delete it
        session.delete(model3URI, createQuery(model2URI));

        // check it
        answer = session.query(createQuery(model3URI));
        answer.beforeFirst();
        assertFalse(answer.next());
        answer.close();

        // clean up
        session.removeModel(model2URI);
        session.removeModel(model3URI);

        resource.end(xid, XAResource.TMSUCCESS);
        resource.commit(xid, true);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testMultipleQuery() throws URISyntaxException {
    logger.info("Testing MultipleQuery");

    try {
      // Load some test data
      Session session = database.newSession();
      XAResource resource = session.getXAResource();
      Xid xid = new TestXid(1);
      resource.start(xid, XAResource.TMNOFLAGS);
      try {
        // Evaluate the query
        Answer answer1 = session.query(createQuery(modelURI));
        Answer answer2 = session.query(createQuery(modelURI));

        compareResults(answer1, answer2);

        answer1.close();
        answer2.close();

        resource.end(xid, XAResource.TMSUCCESS);
        resource.commit(xid, true);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  public void testBasicReadOnlyQuery() throws URISyntaxException {
    logger.info("Testing basicReadOnlyQuery");

    try {
      // Load some test data
      DatabaseSession session = (DatabaseSession)database.newSession();
      try {
        XAResource resource = session.getReadOnlyXAResource();
        Xid xid = new TestXid(1);
        resource.start(xid, XAResource.TMNOFLAGS);

        // Evaluate the query
        Answer answer = session.query(createQuery(modelURI));
        compareResults(expectedResults(), answer);
        answer.close();

        resource.end(xid, XAResource.TMSUCCESS);
        resource.commit(xid, true);
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
      XAResource resource = session.getReadOnlyXAResource();
      Xid xid1 = new TestXid(1);
      Xid xid2 = new TestXid(2);
      resource.start(xid1, XAResource.TMNOFLAGS);
      try {
        // Evaluate the query
        Answer answer1 = session.query(createQuery(modelURI));
        resource.end(xid1, XAResource.TMSUSPEND);
        resource.start(xid2, XAResource.TMNOFLAGS);

        Answer answer2 = session.query(createQuery(modelURI));
        resource.end(xid2, XAResource.TMSUSPEND);

        compareResults(answer1, answer2);

        answer1.close();
        answer2.close();

        resource.start(xid1, XAResource.TMRESUME);
        resource.end(xid1, XAResource.TMSUCCESS);
        resource.end(xid2, XAResource.TMSUCCESS);
        resource.commit(xid1, true);
        resource.commit(xid2, true);
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
      XAResource roResource = session.getReadOnlyXAResource();
      XAResource rwResource = session.getXAResource();
      Xid xid1 = new TestXid(1);

      rwResource.start(xid1, XAResource.TMNOFLAGS);
      session.createModel(model2URI, null);
      rwResource.end(xid1, XAResource.TMSUSPEND);

      try {
        Xid xid2 = new TestXid(2);
        roResource.start(xid2, XAResource.TMNOFLAGS);

        // Evaluate the query
        Answer answer = session.query(createQuery(modelURI));

        roResource.end(xid2, XAResource.TMSUSPEND);
        answer.beforeFirst();
        while (answer.next()) {
          rwResource.start(xid1, XAResource.TMRESUME);
          session.insert(model2URI, Collections.singleton(new TripleImpl(
              (SubjectNode)answer.getObject(0),
              (PredicateNode)answer.getObject(1),
              (ObjectNode)answer.getObject(2))));
          rwResource.end(xid1, XAResource.TMSUSPEND);
        }
        answer.close();

        rwResource.end(xid1, XAResource.TMSUCCESS);
        rwResource.commit(xid1, true);

        Xid xid3 = new TestXid(3);
        roResource.start(xid3, XAResource.TMNOFLAGS);

        Answer answer2 = session.query(createQuery(model2URI));

        roResource.end(xid3, XAResource.TMSUSPEND);
        compareResults(expectedResults(), answer2);
        answer2.close();

        Xid xid4 = new TestXid(4);
        rwResource.start(xid4, XAResource.TMNOFLAGS);
        session.removeModel(model2URI);
        rwResource.end(xid4, XAResource.TMSUCCESS);
        rwResource.commit(xid4, true);

        roResource.end(xid2, XAResource.TMSUCCESS);
        roResource.commit(xid2, true);
        roResource.end(xid3, XAResource.TMSUCCESS);
        roResource.commit(xid3, true);
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
      XAResource roResource = session.getReadOnlyXAResource();
      Xid xid1 = new TestXid(1);
      roResource.start(xid1, XAResource.TMNOFLAGS);

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

        roResource.end(xid1, XAResource.TMSUSPEND);

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

  public void testConcurrentSubqueryQuery() throws URISyntaxException {
    logger.info("Testing concurrentSubqueryQuery");

    try {
      Session session = database.newSession();
      XAResource rwResource = session.getXAResource();
      Xid xid1 = new TestXid(1);
      rwResource.start(xid1, XAResource.TMNOFLAGS);

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
        session.setModel(model3URI, fileURI);
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
          XAResource rwResource = session1.getXAResource();
          Xid xid1 = new TestXid(1); // Initial create model.
          Xid xid2 = new TestXid(2); // Main Test.

          rwResource.start(xid1, XAResource.TMNOFLAGS);
          session1.createModel(model3URI, null);
          rwResource.end(xid1, XAResource.TMSUCCESS);
          rwResource.commit(xid1, true);

          // Nothing visible.
          assertChangeNotVisible(session2);

          // Perform update
          rwResource.start(xid2, XAResource.TMNOFLAGS);
          session1.setModel(model3URI, fileURI);
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
          session1.setModel(model3URI, fileURI);

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
        XAResource resource1 = session1.getXAResource();
        resource1.start(new TestXid(1), XAResource.TMNOFLAGS);
        session1.createModel(model3URI, null);
        resource1.end(new TestXid(1), XAResource.TMSUCCESS);
        resource1.commit(new TestXid(1), true);

        resource1.start(new TestXid(2), XAResource.TMNOFLAGS);
        session1.setModel(model3URI, fileURI);

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

  /**
   * Test two simultaneous transactions, in two threads. The second one should block
   * until the first one sets auto-commit back to true.
   */
  public void testExternalInternalConcurrentTxn() throws URISyntaxException {
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


  /**
   * Test two simultaneous transactions, in two threads. The second one should block
   * until the first one sets auto-commit back to true.
   */
  public void testInternalExternalConcurrentTxn() throws URISyntaxException {
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

  /**
   * Test two simultaneous transactions, in two threads. The second one should block
   * until the first one sets auto-commit back to true.
   */
  public void testExternalInternalConcurrentTxnRollback() throws URISyntaxException {
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


  /**
   * Test two simultaneous transactions, in two threads. The second one should block
   * until the first one sets auto-commit back to true.
   */
  public void testInternalExternalConcurrentTxnRollback() throws URISyntaxException {
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
        session.setModel(model3URI, fileURI);
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


  /**
   * Tests cleaning up a transaction on close.  This test added in the process
   * of fixing a bug reported by Ronald on the JTA-beta.
   */
  public void testInternalSerialMultipleSessions() throws URISyntaxException {
    logger.info("testInternalSerialMultipleSessions");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    try {
      Session session1 = database.newSession();
      Session session2 = database.newSession();
      try {
        session1.createModel(model4URI, null);

        session1.setAutoCommit(false);
        session1.setModel(model4URI, fileURI);

        session1.commit();
        session1.close();

        session2.setAutoCommit(false);
      } finally {
        session2.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  /**
   * Test transaction timeout.
   */
  public void testTransactionTimeout() throws URISyntaxException {
    logger.info("testTransactionTimeout");
    URI fileURI  = new File("data/xatest-model1.rdf").toURI();

    // test idle timeout
    try {
      Session session1 = database.newSession();
      session1.setIdleTimeout(1000);

      try {
        XAResource resource = session1.getXAResource();
        Xid xid = new TestXid(1);
        resource.start(xid, XAResource.TMNOFLAGS);

        session1.createModel(model3URI, null);

        resource.end(xid, XAResource.TMSUCCESS);
        resource.commit(xid, true);

        logger.debug("Starting transaction for session1");
        resource.start(xid, XAResource.TMNOFLAGS);
        logger.debug("Started transaction for session1");

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

        rollbackTimedOutTxn(resource, xid, true);
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }

    // test transaction timeout
    try {
      Session session1 = database.newSession();
      try {
        XAResource resource = session1.getXAResource();
        Xid xid = new TestXid(1);
        resource.start(xid, XAResource.TMNOFLAGS);

        session1.createModel(model3URI, null);

        resource.end(xid, XAResource.TMSUCCESS);
        resource.commit(xid, true);

        logger.debug("Starting transaction for session1");
        assertTrue(resource.setTransactionTimeout(1));
        assertEquals(1, resource.getTransactionTimeout());
        resource.start(xid, XAResource.TMNOFLAGS);
        logger.debug("Started transaction for session1");

        Thread t2 = new Thread("tx2Test") {
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

        rollbackTimedOutTxn(resource, xid, true);
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
        XAResource resource = session1.getXAResource();

        assertTrue(resource.setTransactionTimeout(1));
        assertEquals(1, resource.getTransactionTimeout());

        Xid xid = new TestXid(1);
        resource.start(xid, XAResource.TMNOFLAGS);
        logger.debug("Started transaction for session1");

        URI delayTwoSecs = new URI("foo://mulgara/timeoutTest?active=mr&hardWait=2000");
        session1.createModel(delayTwoSecs, new URI(Mulgara.NAMESPACE + "MockModel"));

        try {
          Answer answer = session1.query(createQuery(delayTwoSecs));
          Thread.sleep(100L);
          answer.beforeFirst();
          assertFalse(answer.next());
          answer.close();
          fail("query should've gotten interrupted");
        } catch (QueryException qe) {
          logger.debug("query was interrupted", qe);
        } catch (TuplesException te) {
          logger.debug("query was interrupted", te);
        }

        rollbackTimedOutTxn(resource, xid, true);
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
        XAResource resource = session1.getXAResource();

        assertTrue(resource.setTransactionTimeout(1));
        assertEquals(1, resource.getTransactionTimeout());

        Xid xid = new TestXid(1);
        resource.start(xid, XAResource.TMNOFLAGS);
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
              } catch (QueryException qe) {
                logger.debug("query was interrupted", qe);
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

        t2.join(500);
        assertFalse("timeout-test thread should've terminated", t2.isAlive());

        rollbackTimedOutTxn(resource, xid, true);
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  private void rollbackTimedOutTxn(XAResource resource, Xid xid, boolean tryCommit) throws XAException {
    try {
      resource.end(xid, XAResource.TMSUCCESS);
      if (tryCommit) {
        resource.commit(xid, true);
        fail("Commit should have failed due to transaction timeout");
      }
    } catch (XAException xae) {
    }

    logger.debug("Rolling back transaction");
    try {
      resource.rollback(xid);
      fail("Rollback after timeout should have thrown XA_HEURRB");
    } catch (XAException xae) {
      assertEquals("Rollback after timeout should have thrown XA_HEURRB",
                   XAException.XA_HEURRB, xae.errorCode);
      resource.forget(xid);
    }
  }

  /**
   * Test various operations after a transaction fails.
   */
  public void testTransactionFailure() {
    logger.info("Testing transactionFailure");

    try {
      // query after failure should fail
      shouldFailQE(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.query(createQuery(modelURI)).close();
        }
      }, "Query in failed transaction did not fail");

      // insert after failure should fail
      shouldFailQE(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.insert(modelURI, Collections.singleton(new TripleImpl(
                                                    new URIReferenceImpl(URI.create("test:a")),
                                                    new URIReferenceImpl(URI.create("test:b")),
                                                    new URIReferenceImpl(URI.create("test:c")))));
        }
      }, "Insert in failed transaction did not fail");

      // start w/o end after failure should fail
      shouldFailXAErr(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.getXAResource().start(xid, XAResource.TMNOFLAGS);
        }
      }, XAException.XAER_DUPID, true, "Start w/o end in failed transaction did not fail");

      shouldFailXAErr(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.getXAResource().start(xid, XAResource.TMRESUME);
        }
        // XXX: shouldn't this be XAER_PROTO ?
      }, XAException.XA_RBROLLBACK, true, "Start w/o end in failed transaction did not fail");

      shouldFailXAErr(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.getXAResource().start(xid, XAResource.TMJOIN);
        }
      }, XAException.XAER_PROTO, true, "Start w/o end in failed transaction did not fail");

      // prepare w/o end after failure should fail
      shouldFailXAErr(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.getXAResource().prepare(xid);
        }
        // XXX: shouldn't this be XAER_PROTO ?
      }, XAException.XA_RBROLLBACK, true, "Prepare w/o end in failed transaction did not fail");

      // commit w/o end after failure should fail
      shouldFailXAErr(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.getXAResource().commit(xid, true);
        }
        // XXX: shouldn't this be XAER_PROTO ?
      }, XAException.XA_RBROLLBACK, true, "Commit w/o end in failed transaction did not fail");

      shouldFailXAErr(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.getXAResource().commit(xid, false);
        }
        // XXX: shouldn't this be XAER_PROTO ?
      }, XAException.XA_RBROLLBACK, true, "Commit w/o end in failed transaction did not fail");

      // rollback w/o end after failure should fail
      shouldFailXAErr(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.getXAResource().rollback(xid);
        }
        // XXX: shouldn't this be XAER_PROTO ?
      }, XAException.XA_HEURRB, true, "Rollback w/o end in failed transaction did not fail");

      // prepare after failure should fail
      shouldFailXAErr(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.getXAResource().prepare(xid);
        }
      }, XAException.XA_RBROLLBACK, false, "Prepare in failed transaction did not fail");

      // commit after failure should fail
      shouldFailXAErr(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.getXAResource().commit(xid, true);
        }
      }, XAException.XA_RBROLLBACK, false, "Commit in failed transaction did not fail");

      shouldFailXAErr(new TestOp() {
        public void run(Session session, Xid xid) throws Exception {
          session.getXAResource().commit(xid, false);
        }
        // XXX: shouldn't this be XAER_PROTO ?
      }, XAException.XA_RBROLLBACK, false, "Commit w/o prepare in failed transaction did not fail");
    } catch (Exception e) {
      fail(e);
    }
  }

  private void shouldFailQE(TestOp op, String msg) throws Exception {
    testTransactionFailureOp(op, true, 0, true, msg);
  }

  private void shouldFailXAErr(TestOp op, int expXAErr, boolean beforeEnd, String msg) throws Exception {
    testTransactionFailureOp(op, false, expXAErr, beforeEnd, msg);
  }

  /*
  private void shouldSucceed(TestOp op) throws Exception {
    testTransactionFailureOp(op, false, null);
  }
  */

  private void testTransactionFailureOp(TestOp op, boolean shouldFailQE, int expXAErr,
                                        boolean beforeEnd, String msg) throws Exception {
    Session session = database.newSession();
    try {
      boolean shouldFail = shouldFailQE || expXAErr != 0;

      // start tx
      XAResource resource = session.getXAResource();

      Xid xid = new TestXid(1);
      resource.start(xid, XAResource.TMNOFLAGS);

      // run bad query -> failed tx
      try {
        session.query(createQuery(URI.create("urn:no:such:model")));
        fail("Bad query failed to throw exception");
      } catch (QueryException qe) {
      }

      // run test op, verify it succeeds/fails, and reset
      if (!beforeEnd) {
        try {
          resource.end(xid, XAResource.TMSUCCESS);
        } catch (XAException xae) {
          if (!isRollback(xae))
            throw xae;
        }
      }

      try {
        op.run(session, xid);
        if (shouldFail)
          fail(msg);
      } catch (QueryException qe) {
        if (!shouldFailQE)
          throw qe;
      } catch (XAException xae) {
        assertTrue(msg + ": " + xae.errorCode,
                   xae.errorCode == expXAErr || xae.errorCode == XAException.XAER_NOTA);
      }

      try {
        if (beforeEnd) {
          try {
            resource.end(xid, XAResource.TMSUCCESS);
          } catch (XAException xae) {
            if (!isRollback(xae))
              throw xae;
          }
        }

        try {
          resource.rollback(xid);
        } catch (XAException xae) {
          if (xae.errorCode == XAException.XA_HEURRB)
            resource.forget(xid);
          else if (!isRollback(xae))
            throw xae;
        }
      } catch (XAException xae) {
        if (!shouldFail || xae.errorCode != XAException.XAER_NOTA)
          throw xae;
      }

      // verify we're good to go
      resource.start(xid, XAResource.TMNOFLAGS);
      session.query(createQuery(modelURI)).close();
      resource.end(xid, XAResource.TMSUCCESS);
      resource.commit(xid, true);
    } finally {
      session.close();
    }
  }

  private static boolean isRollback(XAException xae) {
    return xae.errorCode >= XAException.XA_RBBASE && xae.errorCode <= XAException.XA_RBEND;
  }

  private static interface TestOp {
    public void run(Session session, Xid xid) throws Exception;
  }


  /**
   * Test session close on still-active session.
   */
  public void testSessionClose() {
    logger.info("Testing sessionClose");

    // test close while waiting for write-lock
    try {
      Session session1 = database.newSession();

      XAResource resource1 = session1.getXAResource();
      Xid xid1 = new TestXid(1);
      resource1.start(xid1, XAResource.TMNOFLAGS);

      try {
        final Session session2 = database.newSession();

        Thread t1 = new Thread("closeTest") {
          public void run() {
            try {
              XAResource resource2 = session2.getXAResource();
              Xid xid2 = new TestXid(2);
              resource2.start(xid2, XAResource.TMNOFLAGS);

              fail("Acquired write-lock unexpectedly");
            } catch (XAException xae) {
              logger.debug("Caught expected exception", xae);
            } catch (QueryException qe) {
              logger.error("Caught unexpected exception", qe);
              fail("Caught unexpected exception " + qe);
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
        logger.debug("closing session1");
        resource1.end(xid1, XAResource.TMSUCCESS);
        resource1.commit(xid1, true);
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }


  /**
   * Test that the underlying xa-resources (from resolvers) are properly activated and deactivated.
   */
  public void testResourceActivation() {
    logger.info("Testing resourceActivation");

    try {
      Session session1 = database.newSession();
      try {
        // two commands in transaction
        MockXAResource mockRes = new MockXAResource();
        MockResolver.setNextXAResource(mockRes);

        XAResource resource1 = session1.getXAResource();
        Xid xid1 = new TestXid(1);
        resource1.start(xid1, XAResource.TMNOFLAGS);

        final URI testModel = new URI("foo://mulgara/resourceActivationTest");
        session1.createModel(testModel, URI.create(Mulgara.NAMESPACE + "MockModel"));

        assertEquals(1, mockRes.startCnt);
        assertEquals(1, mockRes.suspendCnt);
        assertEquals(0, mockRes.resumeCnt);
        assertEquals(0, mockRes.endCnt);

        session1.query(createQuery(testModel)).close();
        assertEquals(1, mockRes.startCnt);
        assertEquals(3, mockRes.suspendCnt);
        assertEquals(2, mockRes.resumeCnt);
        assertEquals(0, mockRes.endCnt);

        resource1.end(xid1, XAResource.TMSUCCESS);
        resource1.commit(xid1, true);
        assertEquals(1, mockRes.startCnt);
        assertEquals(3, mockRes.suspendCnt);
        assertEquals(2, mockRes.resumeCnt);
        assertEquals(1, mockRes.endCnt);
        assertEquals(1, mockRes.prepareCnt);
        assertEquals(1, mockRes.commitCnt);

        // two commands in transaction, where one resolver is enlisted later
        mockRes = new MockXAResource();
        MockResolver.setNextXAResource(mockRes);

        xid1 = new TestXid(2);
        resource1.start(xid1, XAResource.TMNOFLAGS);

        session1.query(createQuery(modelURI)).close();
        assertEquals(0, mockRes.startCnt);
        assertEquals(0, mockRes.suspendCnt);
        assertEquals(0, mockRes.resumeCnt);
        assertEquals(0, mockRes.endCnt);

        session1.query(createQuery(testModel)).close();
        assertEquals(1, mockRes.startCnt);
        assertEquals(2, mockRes.suspendCnt);
        assertEquals(1, mockRes.resumeCnt);
        assertEquals(0, mockRes.endCnt);

        resource1.end(xid1, XAResource.TMSUCCESS);
        resource1.commit(xid1, true);
        assertEquals(1, mockRes.startCnt);
        assertEquals(2, mockRes.suspendCnt);
        assertEquals(1, mockRes.resumeCnt);
        assertEquals(1, mockRes.endCnt);
        assertEquals(1, mockRes.prepareCnt);
        assertEquals(1, mockRes.commitCnt);

        // two threads, 2 commands in each thread, all one transaction (e.g. RMI)
        mockRes = new MockXAResource();
        MockResolver.setNextXAResource(mockRes);

        xid1 = new TestXid(3);
        resource1.start(xid1, XAResource.TMNOFLAGS);

        session1.query(createQuery(testModel)).close();
        assertEquals(1, mockRes.startCnt);
        assertEquals(2, mockRes.suspendCnt);
        assertEquals(1, mockRes.resumeCnt);
        assertEquals(0, mockRes.endCnt);

        final boolean[] steps = new boolean[3];
        final Xid theXid = xid1;
        final XAResource theRes = resource1;
        final Session theSession = session1;
        final MockXAResource theMockRes = mockRes;

        Thread t1 = new Thread() {
          public void run() {
            try {
              theSession.query(createQuery(testModel)).close();
              assertEquals(1, theMockRes.startCnt);
              assertEquals(4, theMockRes.suspendCnt);
              assertEquals(3, theMockRes.resumeCnt);
              assertEquals(0, theMockRes.endCnt);

              synchronized (this) {
                steps[0] = true;
                notify();

                try {
                  wait(2000L);
                } catch (InterruptedException ie) {
                  logger.error("wait for tx step1 interrupted", ie);
                  fail(ie);
                }
                assertTrue("transaction should've completed step1", steps[1]);
              }

              theRes.end(theXid, XAResource.TMSUCCESS);
              theRes.rollback(theXid);
              assertEquals(1, theMockRes.startCnt);
              assertEquals(6, theMockRes.suspendCnt);
              assertEquals(5, theMockRes.resumeCnt);
              assertEquals(1, theMockRes.endCnt);
              assertEquals(0, theMockRes.prepareCnt);
              assertEquals(0, theMockRes.commitCnt);
              assertEquals(1, theMockRes.rollbackCnt);

              synchronized (this) {
                steps[2] = true;
                notify();
              }
            } catch (Exception e) {
              fail(e);
            }
          }
        };

        synchronized (t1) {
          t1.start();

          try {
            t1.wait(2000L);
          } catch (InterruptedException ie) {
            logger.error("wait for tx step0 interrupted", ie);
            fail(ie);
          }
          assertTrue("transaction should've completed step0", steps[0]);
        }

        session1.query(createQuery(testModel)).close();
        assertEquals(1, mockRes.startCnt);
        assertEquals(6, mockRes.suspendCnt);
        assertEquals(5, mockRes.resumeCnt);
        assertEquals(0, mockRes.endCnt);

        synchronized (t1) {
          steps[1] = true;
          t1.notify();

          try {
            t1.wait(2000L);
          } catch (InterruptedException ie) {
            logger.error("wait for tx step2 interrupted", ie);
            fail(ie);
          }
          assertTrue("transaction should've completed step2", steps[2]);
        }

        int RB = XAException.XA_RBOTHER;
        // res.start fails immediately with no error-code
        doResourceFailureTest(4, session1, resource1, testModel, 1, -1, -1, -1, false, 0, true,
                              1, 0, 0, 0,
                              1, 0, 0, 0, 0, 0, 0);

        // res.start fails immediately with rollback error-code
        doResourceFailureTest(5, session1, resource1, testModel, 1, -1, -1, -1, false, RB, true,
                              1, 0, 0, 0,
                              1, 0, 0, 0, 0, 0, 1);

        // res.end fails on suspend with unspecified error-code
        doResourceFailureTest(6, session1, resource1, testModel, -1, 1, -1, -1, false, 0, true,
                              1, 1, 0, 0,
                              1, 1, 0, 0, 0, 0, 0);

        // res.end fails on suspend with rollback error-code
        doResourceFailureTest(7, session1, resource1, testModel, -1, 1, -1, -1, false, RB, true,
                              1, 1, 0, 0,
                              1, 1, 0, 0, 0, 0, 1);

        // res.start fails on resume with unspecified error-code
        doResourceFailureTest(8, session1, resource1, testModel, -1, -1, 1, -1, false, 0, true,
                              1, 1, 1, 0,
                              1, 1, 1, 0, 0, 0, 0);

        // res.start fails on resume with rollback error-code
        doResourceFailureTest(9, session1, resource1, testModel, -1, -1, 1, -1, false, RB, true,
                              1, 1, 1, 0,
                              1, 1, 1, 0, 0, 0, 1);

        // res.end fails on end with unspecified error-code
        doResourceFailureTest(10, session1, resource1, testModel, -1, -1, -1, 1, false, 0, false,
                              1, 2, 1, 0,
                              1, 2, 1, 1, 0, 0, 0);

        // res.end fails on end with rollback error-code
        doResourceFailureTest(11, session1, resource1, testModel, -1, -1, -1, 1, false, RB, false,
                              1, 2, 1, 0,
                              1, 2, 1, 1, 0, 0, 1);

        // res.prepare fails with unspecified error-code
        doResourceFailureTest(12, session1, resource1, testModel, -1, -1, -1, -1, true, 0, false,
                              1, 2, 1, 0,
                              1, 2, 1, 1, 1, 0, 1);

        // res.prepare fails with rollback error-code
        doResourceFailureTest(13, session1, resource1, testModel, -1, -1, -1, -1, true, RB, false,
                              1, 2, 1, 0,
                              1, 2, 1, 1, 1, 0, 0);
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  private void doResourceFailureTest(int testNum, Session session, XAResource resource, URI testModel,
                                     int failStartAfter, int failSuspendAfter, int failResumeAfter,
                                     int failEndAfter, boolean failPrepare, int errorCode, boolean qryFails,
                                     int pfStartCnt, int pfSuspendCnt, int pfResumeCnt, int pfEndCnt,
                                     int endStartCnt, int endSuspendCnt, int endResumeCnt, int endEndCnt,
                                     int endPrepareCnt, int endCommitCnt, int endRollbackCnt)
      throws Exception {
    MockFailingXAResource mockRes = new MockFailingXAResource();
    mockRes.failStartAfter = failStartAfter >= 0 ? failStartAfter : Integer.MAX_VALUE;
    mockRes.failSuspendAfter = failSuspendAfter >= 0 ? failSuspendAfter : Integer.MAX_VALUE;
    mockRes.failResumeAfter = failResumeAfter >= 0 ? failResumeAfter : Integer.MAX_VALUE;
    mockRes.failEndAfter = failEndAfter >= 0 ? failEndAfter : Integer.MAX_VALUE;
    mockRes.failPrepare = failPrepare;
    mockRes.errorCode = errorCode;
    MockResolver.setNextXAResource(mockRes);

    TestXid xid1 = new TestXid(testNum);
    resource.start(xid1, XAResource.TMNOFLAGS);

    try {
      session.query(createQuery(testModel)).close();
      if (qryFails)
        fail("query should have failed");
    } catch (TuplesException te) {
      if (!qryFails)
        throw te;
      logger.debug("Caught expected exception", te);
    } catch (QueryException qe) {
      if (!qryFails)
        throw qe;
      logger.debug("Caught expected exception", qe);
    }

    assertEquals(pfStartCnt, mockRes.startCnt);
    assertEquals(pfSuspendCnt, mockRes.suspendCnt);
    assertEquals(pfResumeCnt, mockRes.resumeCnt);
    assertEquals(pfEndCnt, mockRes.endCnt);

    try {
      resource.end(xid1, qryFails ? XAResource.TMFAIL : XAResource.TMSUCCESS);
    } catch (XAException xae) {
      if (!isRollback(xae) && xae.errorCode != XAException.XA_HEURRB)
        throw xae;
    }
    try {
      if (qryFails)
        resource.rollback(xid1);
      else
        resource.commit(xid1, true);
    } catch (XAException xae) {
      if (xae.errorCode == XAException.XA_HEURRB)
        resource.forget(xid1);
      else if (!isRollback(xae) && qryFails)
        throw xae;
    }

    assertEquals(endStartCnt, mockRes.startCnt);
    assertEquals(endSuspendCnt, mockRes.suspendCnt);
    assertEquals(endResumeCnt, mockRes.resumeCnt);
    assertEquals(endEndCnt, mockRes.endCnt);
    assertEquals(endPrepareCnt, mockRes.prepareCnt);
    assertEquals(endCommitCnt, mockRes.commitCnt);
    assertEquals(endRollbackCnt, mockRes.rollbackCnt);
  }

  private static class MockXAResource extends DummyXAResource {
    protected static enum State { IDLE, ACTIVE, SUSPENDED, ENDED, PREPARED, FINISHED };

    protected final ThreadLocal<Xid> currTxn = new ThreadLocal<Xid>();
    protected State state = State.IDLE;

    public int startCnt = 0;
    public int resumeCnt = 0;
    public int suspendCnt = 0;
    public int endCnt = 0;
    public int prepareCnt = 0;
    public int commitCnt = 0;
    public int rollbackCnt = 0;

    public void start(Xid xid, int flags) throws XAException {
      super.start(xid, flags);

      if (currTxn.get() != null) {
        throw new XAException("transaction already active: " + currTxn.get());
      }
      currTxn.set(xid);

      if (flags == XAResource.TMNOFLAGS && state == State.ACTIVE) {
        throw new XAException("resource already active: " + state);
      }
      if (flags == XAResource.TMRESUME && state != State.SUSPENDED) {
        throw new XAException("resource not suspended: " + state);
      }
      state = State.ACTIVE;

      if (flags == XAResource.TMNOFLAGS) startCnt++;
      if (flags == XAResource.TMRESUME) resumeCnt++;
    }

    public void end(Xid xid, int flags) throws XAException {
      super.end(xid, flags);

      if (!(state == State.SUSPENDED && (flags == XAResource.TMSUCCESS || flags == XAResource.TMFAIL))) {
        if (!xid.equals(currTxn.get())) {
          throw new XAException("mismatched transaction end");
        }
        currTxn.set(null);

        if (state != State.ACTIVE) {
          throw new XAException("resource not active: " + state);
        }
      }
      state = (flags == XAResource.TMSUSPEND) ? State.SUSPENDED : State.ENDED;

      if (flags == XAResource.TMSUSPEND) suspendCnt++;
      if (flags != XAResource.TMSUSPEND) endCnt++;
    }

    public int prepare(Xid xid) throws XAException {
      super.prepare(xid);

      if (currTxn.get() != null) {
        throw new XAException("transaction still active: " + currTxn.get());
      }
      if (state != State.ENDED) {
        throw new XAException("resource not ended: " + state);
      }
      state = State.PREPARED;

      prepareCnt++;
      return XA_OK;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
      super.commit(xid, onePhase);

      if (currTxn.get() != null) {
        throw new XAException("transaction still active: " + currTxn.get());
      }
      if (onePhase && state != State.ENDED) {
        throw new XAException("resource not ended: " + state);
      }
      if (!onePhase && state != State.PREPARED) {
        throw new XAException("resource not prepared: " + state);
      }
      state = State.FINISHED;

      commitCnt++;
    }

    public void rollback(Xid xid) throws XAException {
      super.rollback(xid);

      if (currTxn.get() != null) throw new XAException("transaction still active: " + currTxn.get());
      if (state != State.ENDED && state != State.PREPARED) {
        throw new XAException("resource not ended or prepared: " + state);
      }
      state = State.FINISHED;

      rollbackCnt++;
    }
  }

  private static class MockFailingXAResource extends MockXAResource {
    public int failStartAfter = Integer.MAX_VALUE;
    public int failSuspendAfter = Integer.MAX_VALUE;
    public int failResumeAfter = Integer.MAX_VALUE;
    public int failEndAfter = Integer.MAX_VALUE;
    public int errorCode = 0;
    public boolean failPrepare = false;

    public void start(Xid xid, int flags) throws XAException {
      super.start(xid, flags);
      if (startCnt >= failStartAfter || resumeCnt >= failResumeAfter) {
        currTxn.set(null);
        state = State.ENDED;
        throw (errorCode != 0) ? new XAException(errorCode) : new XAException("Testing start failure");
      }
    }

    public void end(Xid xid, int flags) throws XAException {
      super.end(xid, flags);
      if (endCnt >= failEndAfter || suspendCnt >= failSuspendAfter) {
        state = State.ENDED;
        throw (errorCode != 0) ? new XAException(errorCode) : new XAException("Testing end failure");
      }
    }

    public int prepare(Xid xid) throws XAException {
      super.prepare(xid);
      if (failPrepare) {
        throw (errorCode != 0) ? new XAException(errorCode) : new XAException("Testing prepare failure");
      }
      return XA_OK;
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


  /**
   * Fail with an unexpected exception
   */
  private void fail(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    fail(stringWriter.toString());
  }
}
