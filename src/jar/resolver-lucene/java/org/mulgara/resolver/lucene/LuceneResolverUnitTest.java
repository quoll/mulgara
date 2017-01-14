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

package org.mulgara.resolver.lucene;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.mulgara.itql.TqlInterpreter;
import org.mulgara.query.Answer;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.Query;
import org.mulgara.query.Variable;
import org.mulgara.query.operation.Modification;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.Database;
import org.mulgara.resolver.JotmTransactionManagerFactory;
import org.mulgara.resolver.spi.MutableLocalQuery;
import org.mulgara.resolver.spi.SymbolicTransformationContext;
import org.mulgara.resolver.spi.SymbolicTransformationException;
import org.mulgara.server.Session;
import org.mulgara.util.FileUtil;

/**
 * Unit tests for the lucene resolver.
 *
 * @created 2008-10-13
 * @author Ronald Tschalär
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence Apache License v2.0
 */
public class LuceneResolverUnitTest extends TestCase {
  private static final Logger logger = Logger.getLogger(LuceneResolverUnitTest.class);

  private static final URI databaseURI = URI.create("local:database");
  private static final URI modelURI = URI.create("local:lucene");
  private static final URI luceneModelType = URI.create(Mulgara.NAMESPACE + "LuceneModel");
  private final static String textDirectory =
      System.getProperty("cvs.root") + File.separator + "data" + File.separator + "fullTextTestData";

  private static Database database = null;
  private static TqlInterpreter ti = null;

  public LuceneResolverUnitTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new LuceneResolverUnitTest("testBasicQueries"));
    suite.addTest(new LuceneResolverUnitTest("testSubqueries"));
    suite.addTest(new LuceneResolverUnitTest("testSubqueries2"));
    suite.addTest(new LuceneResolverUnitTest("testConcurrentQueries"));
    suite.addTest(new LuceneResolverUnitTest("testConcurrentReadTransaction"));
    suite.addTest(new LuceneResolverUnitTest("testTransactionIsolation"));
    suite.addTest(new LuceneResolverUnitTest("testLuceneConstraint"));

    return suite;
  }

  /**
   * Create test objects.
   */
  public void setUp() throws Exception {
    if (database == null) {
      // Create the persistence directory
      File persistenceDirectory = new File(new File(System.getProperty("cvs.root")), "testDatabase");
      if (persistenceDirectory.isDirectory()) {
        if (!FileUtil.deleteDirectory(persistenceDirectory)) {
          throw new RuntimeException("Unable to remove old directory " + persistenceDirectory);
        }
      }
      if (!persistenceDirectory.mkdirs()) {
        throw new Exception("Unable to create directory " + persistenceDirectory);
      }

      // Define the the node pool factory
      String nodePoolFactoryClassName = "org.mulgara.store.stringpool.xa11.XA11StringPoolFactory";

      // Define the string pool factory
      String stringPoolFactoryClassName = "org.mulgara.store.stringpool.xa11.XA11StringPoolFactory";

      String tempNodePoolFactoryClassName = "org.mulgara.store.nodepool.memory.MemoryNodePoolFactory";

      // Define the string pool factory
      String tempStringPoolFactoryClassName = "org.mulgara.store.stringpool.memory.MemoryStringPoolFactory";

      // Define the resolver factory used to manage system models
      String systemResolverFactoryClassName = "org.mulgara.resolver.store.StatementStoreResolverFactory";

      // Define the resolver factory used to manage system models
      String tempResolverFactoryClassName = "org.mulgara.resolver.memory.MemoryResolverFactory";

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

      database.addContentHandler("org.mulgara.content.n3.N3ContentHandler");
      database.addResolverFactory("org.mulgara.resolver.lucene.LuceneResolverFactory", persistenceDirectory);

      ti = new TqlInterpreter();

      // Load some test data
      Session session = database.newSession();
      try {
        URI fileURI = new File(textDirectory + File.separator + "data.n3").toURI();

        if (session.modelExists(modelURI)) {
          session.removeModel(modelURI);
        }
        session.createModel(modelURI, luceneModelType);
        session.setModel(modelURI, fileURI);
      } finally {
        session.close();
      }
    }
  }

  private synchronized Query parseQuery(String q) throws Exception {
    return (Query) ti.parseCommand(q);
  }

  /**
   * The teardown method for JUnit
   */
  public void tearDown() {
  }

  /**
   * Basic queries.
   */
  public void testBasicQueries() throws Exception {
    logger.info("Testing basic queries");

    try {
      Session session = database.newSession();

      try {
        // Run simple query with variable subject and fixed predicate
        String q = "select $s from <foo:bar> where $s <foo:hasText> 'American' in <" + modelURI + ">;";
        Answer answer = session.query(parseQuery(q));
        compareResults(new String[][] { { "foo:node5" }, { "foo:node6" }, { "foo:node7" } }, answer);
        answer.close();

        // Run simple query with variable subject and predicate
        q = "select $s $p from <foo:bar> where $s $p 'American' in <" + modelURI + ">;";
        answer = session.query(parseQuery(q));
        compareResults(new String[][] { { "foo:node5", "foo:hasText" },
                                        { "foo:node6", "foo:hasText" },
                                        { "foo:node7", "foo:hasText" } },
                       answer);
        answer.close();

        // Run simple query with fixed subject and variable predicate
        q = "select $p from <foo:bar> where <foo:node6> $p 'American' in <" + modelURI + ">;";
        answer = session.query(parseQuery(q));
        compareResults(new String[][] { { "foo:hasText" } }, answer);
        answer.close();

        // Run simple query with fixed subject and variable predicate and object
        q = "select $p $o from <foo:bar> where <foo:node9> $p $o in <" + modelURI + ">;";
        answer = session.query(parseQuery(q));
        compareResults(new String[][] { { "foo:hasText", "Antibiotic Use Working Group" } }, answer, true);
        answer.close();

        // Run simple query with fixed predicate and variable subject and object
        q = "select $s $o from <foo:bar> where $s <foo:hasText> $o in <" + modelURI + "> order by $s limit 3;";
        answer = session.query(parseQuery(q));
        compareResults(new String[][] {
            { "foo:node1",  "AACP Pneumothorax Consensus Group" },
            { "foo:node10", "Atypical Squamous Cells Intraepithelial" },
            { "foo:node11", "Lesion Triage Study (ALTS) Group" },
          },
          answer, true);

        answer.close();

        // Run simple query with variable subject, predicate, and object
        q = "select $s $p $o from <foo:bar> where $s $p $o in <" + modelURI + "> order by $s limit 3;";
        answer = session.query(parseQuery(q));
        compareResults(new String[][] {
            { "foo:node1",  "foo:hasText", "AACP Pneumothorax Consensus Group" },
            { "foo:node10", "foo:hasText", "Atypical Squamous Cells Intraepithelial" },
            { "foo:node11", "foo:hasText", "Lesion Triage Study (ALTS) Group" },
          },
          answer, true);

        answer.close();

        // Run extended query with variable subject and fixed predicate
        q = "select $s from <foo:bar> where $s <mulgara:search> $b in <" + modelURI + "> and $b <foo:hasText> 'American' in <" + modelURI + ">;";
        answer = session.query(parseQuery(q));
        compareResults(new String[][] { { "foo:node5" }, { "foo:node6" }, { "foo:node7" } }, answer);
        answer.close();

        // Run extended query with variable subject and predicate
        q = "select $s $p from <foo:bar> where $s <mulgara:search> $b in <" + modelURI + "> and $b $p 'American' in <" + modelURI + ">;";
        answer = session.query(parseQuery(q));
        compareResults(new String[][] { { "foo:node5", "foo:hasText" },
                                        { "foo:node6", "foo:hasText" },
                                        { "foo:node7", "foo:hasText" } },
                       answer);
        answer.close();

        // Run extended query with fixed subject and variable predicate
        q = "select $p from <foo:bar> where <foo:node6> <mulgara:search> $b in <" + modelURI + "> and $b $p 'American' in <" + modelURI + ">;";
        answer = session.query(parseQuery(q));
        compareResults(new String[][] { { "foo:hasText" } }, answer);
        answer.close();

      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Subqueries.
   */
  public void testSubqueries() throws Exception {
    logger.info("Testing subqueries");

    try {
      Session session = database.newSession();

      try {
        // Lucene query in outer query
        String q = "select $s subquery(select $y $z from <" + modelURI + "> where $s $y $z)" +
                   " from <foo:bar> where $s $p 'b*' in <" + modelURI + "> order by $s;";

        Answer answer = session.query(parseQuery(q));
        compareResults(new Object[][] {
          { "foo:node13",  new Object[][] { { "foo:hasText", "Benefit Evaluation of Direct Coronary Stenting Study Group" } } },
          { "foo:node14",  new Object[][] { { "foo:hasText", "Biomarkers Definitions Working Group." } } },
        }, answer, true);
        answer.close();

        // Lucene query in both
        q = "select $x subquery(select $y from <foo:bar> where $x $y 'a*' in <" + modelURI + ">) " +
            "  from <foo:bar> where $x <foo:hasText> 'Group' in <" + modelURI + "> order by $x;";

        answer = session.query(parseQuery(q));
        compareResults(new Object[][] {
          { "foo:node1",  new Object[][] { { "foo:hasText" } } },
          { "foo:node11", new Object[][] { { "foo:hasText" } } },
          { "foo:node12", new Object[][] { { "foo:hasText" } } },
          { "foo:node13", new Object[][] { } },
          { "foo:node14", new Object[][] { } },
          { "foo:node18", new Object[][] { } },
          { "foo:node2",  new Object[][] { { "foo:hasText" } } },
          { "foo:node4",  new Object[][] { { "foo:hasText" } } },
          { "foo:node9",  new Object[][] { { "foo:hasText" } } },
        }, answer);
        answer.close();

      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Subqueries.
   */
  public void testSubqueries2() throws Exception {
    logger.info("Testing subqueries2");

    try {
      Session session = database.newSession();

      try {
        // create models
        URI dataModel = new URI("local:sampledata");
        URI textModel = new URI("local:sampletext");

        if (session.modelExists(dataModel)) session.removeModel(dataModel);
        if (session.modelExists(textModel)) session.removeModel(textModel);
        session.createModel(dataModel, null);
        session.createModel(textModel, luceneModelType);

        // load models
        URI fileURI = new File(new File(System.getProperty("cvs.root"), "data"), "w3c-news.rss").toURI();
        session.setModel(dataModel, fileURI);

        String q = "select $s $p $o from <local:sampledata> where $s $p $o and (" +
                     "  $p <mulgara:is> <http://purl.org/rss/1.0/description> or " +
                     "  $p <mulgara:is> <http://purl.org/rss/1.0/title>);";
        session.insert(textModel, parseQuery(q));

        // run queries
        q = "select $s subquery(select $z from <local:sampledata> where $s <http://purl.org/rss/1.0/title> $z) " +
            "  from <local:sampledata> where $s $p $o and $s $p 'W*' in <local:sampletext> order by $s;";
        Answer answer = session.query(parseQuery(q));
        compareResults(new Object[][] {
          { "http://www.w3.org/2000/08/w3c-synd/home.rss", new Object[][] { { "The World Wide Web Consortium" } } },
          { "http://www.w3.org/News/2002#item12", new Object[][] { { "W3C Launches Web Services Activity" } } },
          { "http://www.w3.org/News/2002#item13", new Object[][] { { "Platform for Privacy Preferences (P3P) Becomes a W3C Proposed Recommendation" } } },
          { "http://www.w3.org/News/2002#item14", new Object[][] { { "XHTML+SMIL Profile Published" } } },
          { "http://www.w3.org/News/2002#item15", new Object[][] { { "W3C Team Presentations in February" } } },
          { "http://www.w3.org/News/2002#item16", new Object[][] { { "QA Framework First Public Working Drafts Published" } } },
          { "http://www.w3.org/News/2002#item17", new Object[][] { { "DOM Level 3 Working Drafts Published" } } },
          { "http://www.w3.org/News/2002#item18", new Object[][] { { "P3P Deployment Guide Updated" } } },
        }, answer, true);
        answer.close();

        q = "select $s subquery(select $z from <local:sampledata> where $s <http://purl.org/rss/1.0/title> $z and $s <http://purl.org/rss/1.0/title> 'W*' in <local:sampletext>) " +
            "  from <local:sampledata> where $s <http://purl.org/rss/1.0/title> $o order by $s;";
        answer = session.query(parseQuery(q));
        compareResults(new Object[][] {
          { "http://www.w3.org/2000/08/w3c-synd/home.rss", new Object[][] { { "The World Wide Web Consortium" } } },
          { "http://www.w3.org/News/2002#item12", new Object[][] { { "W3C Launches Web Services Activity" } } },
          { "http://www.w3.org/News/2002#item13", new Object[][] { { "Platform for Privacy Preferences (P3P) Becomes a W3C Proposed Recommendation" } } },
          { "http://www.w3.org/News/2002#item14", new Object[][] { } },
          { "http://www.w3.org/News/2002#item15", new Object[][] { { "W3C Team Presentations in February" } } },
          { "http://www.w3.org/News/2002#item16", new Object[][] { { "QA Framework First Public Working Drafts Published" } } },
          { "http://www.w3.org/News/2002#item17", new Object[][] { { "DOM Level 3 Working Drafts Published" } } },
          { "http://www.w3.org/News/2002#item18", new Object[][] { } },
        }, answer, true);
        answer.close();

      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Two queries, in parallel.
   */
  public void testConcurrentQueries() throws Exception {
    logger.info("Testing concurrentQueries");

    try {
      Session session = database.newSession();

      // Run the queries
      try {
        String q = "select $x from <foo:bar> where $x <foo:hasText> 'American' in <" + modelURI + ">;";
        Query qry1 = parseQuery(q);
        Query qry2 = parseQuery(q);

        Answer answer1 = session.query(qry1);
        Answer answer2 = session.query(qry2);

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

  /**
   * Two queries, in concurrent transactions.
   */
  public void testConcurrentReadTransaction() throws Exception {
    logger.info("Testing concurrentReadTransaction");

    try {
      Session session1 = database.newSession();
      try {
        XAResource resource1 = session1.getReadOnlyXAResource();
        Xid xid1 = new TestXid(1);
        resource1.start(xid1, XAResource.TMNOFLAGS);

        final boolean[] flag = new boolean[] { false };

        Thread t2 = new Thread("tx2Test") {
          public void run() {
            try {
              Session session2 = database.newSession();
              try {
                XAResource resource2 = session2.getReadOnlyXAResource();
                Xid xid2 = new TestXid(2);
                resource2.start(xid2, XAResource.TMNOFLAGS);

                synchronized (flag) {
                  flag[0] = true;
                  flag.notify();
                }

                // Evaluate the query
                String q = "select $x from <foo:bar> where $x <foo:hasText> 'Study' in <" + modelURI + ">;";
                Answer answer = session2.query(parseQuery(q));

                compareResults(expectedStudyResults(), answer);
                answer.close();

                synchronized (flag) {
                  while (flag[0])
                    flag.wait();
                }

                resource2.end(xid2, XAResource.TMSUCCESS);
                resource2.commit(xid2, true);
              } finally {
                session2.close();
              }
            } catch (Exception e) {
              fail(e);
            }
          }
        };
        t2.start();

        synchronized (flag) {
          if (!flag[0]) {
            try {
              flag.wait(2000L);
            } catch (InterruptedException ie) {
              logger.error("wait for tx2-started interrupted", ie);
              fail(ie);
            }
          }
          assertTrue("second transaction should have proceeded", flag[0]);
        }

        String q = "select $x from <foo:bar> where $x <foo:hasText> 'Group' in <" + modelURI + ">;";
        Answer answer = session1.query(parseQuery(q));

        compareResults(expectedGroupResults(), answer);
        answer.close();

        synchronized (flag) {
          flag[0] = false;
          flag.notify();
        }

        try {
          t2.join(2000L);
        } catch (InterruptedException ie) {
          logger.error("wait for tx2-terminated interrupted", ie);
          fail(ie);
        }
        assertFalse("second transaction should've terminated", t2.isAlive());

        resource1.end(xid1, XAResource.TMSUCCESS);
        resource1.commit(xid1, true);
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Two concurrent transactions, one reader, one writer. Verify transaction isolation.
   */
  public void testTransactionIsolation() throws Exception {
    logger.info("Testing transactionIsolation");

    try {
      Session session1 = database.newSession();
      try {
        // start read-only txn
        XAResource resource1 = session1.getReadOnlyXAResource();
        Xid xid1 = new TestXid(1);
        resource1.start(xid1, XAResource.TMNOFLAGS);

        // run query before second txn starts
        String q = "select $x from <foo:bar> where $x <foo:hasText> 'Group' in <" + modelURI + ">;";
        Answer answer = session1.query(parseQuery(q));

        compareResults(expectedGroupResults(), answer);
        answer.close();

        // run a second transaction that writes new data
        final boolean[] flag = new boolean[] { false };

        Thread t2 = new Thread("tx2Test") {
          public void run() {
            try {
              Session session2 = database.newSession();
              try {
                XAResource resource2 = session2.getXAResource();
                Xid xid2 = new TestXid(2);
                resource2.start(xid2, XAResource.TMNOFLAGS);

                synchronized (flag) {
                  flag[0] = true;
                  flag.notify();

                  while (flag[0])
                    flag.wait();
                }

                String q = "insert <foo:nodeX> <foo:hasText> 'Another Group text' into <" + modelURI + ">;";
                synchronized (LuceneResolverUnitTest.this) {
                  session2.insert(modelURI, ((Modification) ti.parseCommand(q)).getStatements());
                }

                synchronized (flag) {
                  flag[0] = true;
                  flag.notify();

                  while (flag[0])
                    flag.wait();
                }

                resource2.end(xid2, XAResource.TMSUCCESS);
                resource2.commit(xid2, true);
              } finally {
                session2.close();
              }
            } catch (Exception e) {
              fail(e);
            }
          }
        };
        t2.start();

        // wait for 2nd txn to have started
        synchronized (flag) {
          while (!flag[0])
            flag.wait();
        }

        // run query before insert
        answer = session1.query(parseQuery(q));
        compareResults(expectedGroupResults(), answer);
        answer.close();

        // wait for insert to complete
        synchronized (flag) {
          flag[0] = false;
          flag.notify();

          while (!flag[0])
            flag.wait();
        }

        // run query after insert and before commit
        answer = session1.query(parseQuery(q));
        compareResults(expectedGroupResults(), answer);
        answer.close();

        // wait for commit to complete
        synchronized (flag) {
          flag[0] = false;
          flag.notify();
        }

        try {
          t2.join(2000L);
        } catch (InterruptedException ie) {
          logger.error("wait for tx2-terminated interrupted", ie);
          fail(ie);
        }
        assertFalse("second transaction should've terminated", t2.isAlive());

        // run query after commit
        answer = session1.query(parseQuery(q));
        compareResults(expectedGroupResults(), answer);
        answer.close();

        // clean up
        resource1.end(xid1, XAResource.TMSUCCESS);
        resource1.commit(xid1, true);

        // start new tx - we should see new data now
        xid1 = new TestXid(3);
        resource1.start(xid1, XAResource.TMNOFLAGS);

        answer = session1.query(parseQuery(q));
        compareResults(concat(expectedGroupResults(), new String[][] { { "foo:nodeX" } }), answer);
        answer.close();

        resource1.end(xid1, XAResource.TMSUCCESS);
        resource1.commit(xid1, true);
      } finally {
        session1.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test LuceneConstraint generation.
   */
  public void testLuceneConstraint() throws Exception {
    logger.info("Testing LuceneConstraint generation");

    LuceneTransformer transf = new LuceneTransformer(LuceneResolverFactory.modelTypeURI,
                                                     LuceneResolverFactory.searchURI,
                                                     LuceneResolverFactory.scoreURI);

    Map<URI,URI> modelsToTypes = new HashMap<URI,URI>();
    modelsToTypes.put(URI.create("test:lucene"), LuceneResolverFactory.modelTypeURI);
    SymbolicTransformationContext context = new TestSymbolicTransformationContext(modelsToTypes);

    try {
      // simple query
      MutableLocalQuery q = new TestMutableLocalQuery(parseQuery(
            "select $foo from <test:bar> where $foo <test:title> 'blah' in <test:lucene>;"));

      transf.transform(context, q);

      ConstraintExpression ce = q.getConstraintExpression();
      checkConstraint(ce, "foo", "test:title", "blah", null, null);

      // basic complex query
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo from <test:bar> where " +
              "$foo <mulgara:search> $search1 in <test:lucene> and " +
              "$search1 <test:title> 'blah' in <test:lucene>;"));

      transf.transform(context, q);

      ConstraintConjunction cc = checkConstraint(q.getConstraintExpression(), 1);
      checkConstraint(cc.getElements().get(0), "foo", "test:title", "blah", "search1", null);

      // complex query with score
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score1 from <test:bar> where " +
              "$foo <mulgara:search> $search1 in <test:lucene> and " +
              "$search1 <test:title> 'blah' in <test:lucene> and " +
              "$search1 <mulgara:score> $score1 in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 1);
      checkConstraint(cc.getElements().get(0), "foo", "test:title", "blah", "search1", "score1");

      // complex query with score, different constraint order
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score1 from <test:bar> where " +
              "$search1 <test:title> 'blah' in <test:lucene> and " +
              "$foo <mulgara:search> $search1 in <test:lucene> and " +
              "$search1 <mulgara:score> $score1 in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 1);
      checkConstraint(cc.getElements().get(0), "foo", "test:title", "blah", "search1", "score1");

      // complex query with score, another different constraint order
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score1 from <test:bar> where " +
              "$search1 <mulgara:score> $score1 in <test:lucene> and " +
              "$search1 <test:title> 'blah' in <test:lucene> and " +
              "$foo <mulgara:search> $search1 in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 1);
      checkConstraint(cc.getElements().get(0), "foo", "test:title", "blah", "search1", "score1");

      // two simple queries, shared var
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo from <test:bar> where " +
              "$foo <test:title> 'blah' in <test:lucene> and " +
              "$foo <test:author> 'Smith' in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 2);
      checkConstraint(cc.getElements().get(0), "foo", "test:title", "blah", null, null);
      checkConstraint(cc.getElements().get(1), "foo", "test:author", "Smith", null, null);

      // two simple queries, shared var and predicate
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo from <test:bar> where " +
              "$foo <test:title> 'blah' in <test:lucene> and " +
              "$foo <test:title> 'Smith' in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 2);
      checkConstraint(cc.getElements().get(0), "foo", "test:title", "blah", null, null);
      checkConstraint(cc.getElements().get(1), "foo", "test:title", "Smith", null, null);

      // two simple queries, separate vars
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $bar from <test:bar> where " +
              "$foo <test:title> 'blah' in <test:lucene> and " +
              "$bar <test:author> 'Smith' in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 2);
      checkConstraint(cc.getElements().get(0), "bar", "test:author", "Smith", null, null);
      checkConstraint(cc.getElements().get(1), "foo", "test:title", "blah", null, null);

      // two complex queries with scores but shared var
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score1 $score2 from <test:bar> where " +
              "$foo <mulgara:search> $search1 in <test:lucene> and " +
              "$search1 <test:title> 'blah' in <test:lucene> and " +
              "$search1 <mulgara:score> $score1 in <test:lucene> and " +
              "$foo <mulgara:search> $search2 in <test:lucene> and " +
              "$search2 <test:author> 'Smith' in <test:lucene> and " +
              "$search2 <mulgara:score> $score2 in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 2);
      checkConstraint(cc.getElements().get(0), "foo", "test:author", "Smith", "search2", "score2");
      checkConstraint(cc.getElements().get(1), "foo", "test:title", "blah", "search1", "score1");

      // two complex queries with scores and separate vars
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score1 $bar $score2 from <test:bar> where " +
              "$foo <mulgara:search> $search1 in <test:lucene> and " +
              "$search1 <test:title> 'blah' in <test:lucene> and " +
              "$search1 <mulgara:score> $score1 in <test:lucene> and " +
              "$bar <mulgara:search> $search2 in <test:lucene> and " +
              "$search2 <test:author> 'Smith' in <test:lucene> and " +
              "$search2 <mulgara:score> $score2 in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 2);
      checkConstraint(cc.getElements().get(0), "bar", "test:author", "Smith", "search2", "score2");
      checkConstraint(cc.getElements().get(1), "foo", "test:title", "blah", "search1", "score1");

      // a simple query and a complex query, shared var
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score2 from <test:bar> where " +
              "$foo <test:title> 'blah' in <test:lucene> and " +
              "$foo <mulgara:search> $search2 in <test:lucene> and " +
              "$search2 <test:author> 'Smith' in <test:lucene> and " +
              "$search2 <mulgara:score> $score2 in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 2);
      checkConstraint(cc.getElements().get(0), "foo", "test:title", "blah", null, null);
      checkConstraint(cc.getElements().get(1), "foo", "test:author", "Smith", "search2", "score2");

      // a simple query and a complex query, shared var, different constraint order
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score2 from <test:bar> where " +
              "$foo <mulgara:search> $search2 in <test:lucene> and " +
              "$search2 <test:author> 'Smith' in <test:lucene> and " +
              "$foo <test:title> 'blah' in <test:lucene> and " +
              "$search2 <mulgara:score> $score2 in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 2);
      checkConstraint(cc.getElements().get(0), "foo", "test:title", "blah", null, null);
      checkConstraint(cc.getElements().get(1), "foo", "test:author", "Smith", "search2", "score2");

      // a simple query and a complex query, separate vars
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $bar $score2 from <test:bar> where " +
              "$foo <test:title> 'blah' in <test:lucene> and " +
              "$bar <mulgara:search> $search2 in <test:lucene> and " +
              "$search2 <test:author> 'Smith' in <test:lucene> and " +
              "$search2 <mulgara:score> $score2 in <test:lucene>;"));

      transf.transform(context, q);

      cc = checkConstraint(q.getConstraintExpression(), 2);
      checkConstraint(cc.getElements().get(0), "foo", "test:title", "blah", null, null);
      checkConstraint(cc.getElements().get(1), "bar", "test:author", "Smith", "search2", "score2");

      // invalid: complex query with multiple different predicates
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score1 from <test:bar> where " +
              "$search1 <test:author> 'Smith' in <test:lucene> and " +
              "$foo <mulgara:search> $search1 in <test:lucene> and " +
              "$search1 <test:title> 'blah' in <test:lucene> and " +
              "$search1 <mulgara:score> $score1 in <test:lucene>;"));

      try {
        transf.transform(context, q);
        fail("query transform should've failed: " + q);
      } catch (SymbolicTransformationException ste) {
        logger.debug("Caught expected transformation exception", ste);
      }

      // invalid: complex query with multiple same predicates
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score1 from <test:bar> where " +
              "$search1 <test:author> 'Smith' in <test:lucene> and " +
              "$foo <mulgara:search> $search1 in <test:lucene> and " +
              "$search1 <test:author> 'Jones' in <test:lucene> and " +
              "$search1 <mulgara:score> $score1 in <test:lucene>;"));

      try {
        transf.transform(context, q);
        fail("query transform should've failed: " + q);
      } catch (SymbolicTransformationException ste) {
        logger.debug("Caught expected transformation exception", ste);
      }

      // invalid: complex query with multiple scores
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score1 from <test:bar> where " +
              "$search1 <mulgara:score> $score2 in <test:lucene> and " +
              "$foo <mulgara:search> $search1 in <test:lucene> and " +
              "$search1 <test:author> 'Jones' in <test:lucene> and " +
              "$search1 <mulgara:score> $score1 in <test:lucene>;"));

      try {
        transf.transform(context, q);
        fail("query transform should've failed: " + q);
      } catch (SymbolicTransformationException ste) {
        logger.debug("Caught expected transformation exception", ste);
      }

      // invalid: complex query with binder and subject shared
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score2 from <test:bar> where " +
              "$foo <mulgara:search> $foo in <test:lucene> and " +
              "$foo <test:author> 'Smith' in <test:lucene> and " +
              "$foo <mulgara:score> $score2 in <test:lucene>;"));

      try {
        transf.transform(context, q);
        fail("query transform should've failed: " + q);
      } catch (SymbolicTransformationException ste) {
        logger.debug("Caught expected transformation exception", ste);
      }

      // invalid: complex query with binder not a variable
      q = new TestMutableLocalQuery(parseQuery(
            "select $foo $score2 from <test:bar> where " +
              "$foo <mulgara:search> <test:it> in <test:lucene> and " +
              "<test:it> <test:author> 'Smith' in <test:lucene> and " +
              "<test:it> <mulgara:score> $score2 in <test:lucene>;"));

      try {
        transf.transform(context, q);
        fail("query transform should've failed: " + q);
      } catch (SymbolicTransformationException ste) {
        logger.debug("Caught expected transformation exception", ste);
      }

      // invalid: complex query with missing predicate
      q = new TestMutableLocalQuery(parseQuery(
            "select $bar $score2 from <test:bar> where " +
              "$bar <mulgara:search> $search2 in <test:lucene> and " +
              "$search2 <mulgara:score> $score2 in <test:lucene>;"));

      try {
        transf.transform(context, q);
        fail("query transform should've failed: " + q);
      } catch (SymbolicTransformationException ste) {
        logger.debug("Caught expected transformation exception", ste);
      }

      // invalid: complex query with missing <mulgara:search>
      q = new TestMutableLocalQuery(parseQuery(
            "select $score2 from <test:bar> where " +
              "$search2 <test:author> 'Smith' in <test:lucene> and " +
              "$search2 <mulgara:score> $score2 in <test:lucene>;"));

      try {
        transf.transform(context, q);
        fail("query transform should've failed: " + q);
      } catch (SymbolicTransformationException ste) {
        logger.debug("Caught expected transformation exception", ste);
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  /*
   * Internal helpers
   */

  private static ConstraintConjunction checkConstraint(ConstraintExpression ce, int numConstr) {
    assertTrue(ce instanceof ConstraintConjunction);

    ConstraintConjunction cc = (ConstraintConjunction)ce;
    assertEquals(numConstr, cc.getElements().size());

    return cc;
  }

  private static void checkConstraint(ConstraintExpression ce, String expSubj, String expPred,
                                      String expObj, String expBind, String expScore)
      throws Exception {
    assertTrue(ce instanceof LuceneConstraint);
    LuceneConstraint lc = (LuceneConstraint)ce;

    assertEquals(expSubj, ((Variable)lc.getSubject()).getName());

    assertTrue(lc.getPredicate() instanceof URIReference);
    assertEquals(URI.create(expPred), ((URIReference)lc.getPredicate()).getURI());

    assertTrue(lc.getObject() instanceof Literal);
    assertEquals(expObj, ((Literal)lc.getObject()).getLexicalForm());

    if (expBind != null) {
      assertEquals(expBind, lc.getBindingVar().getName());
    } else {
      assertNull(lc.getBindingVar());
    }

    if (expScore != null) {
      assertEquals(expScore, lc.getScoreVar().getName());
    } else {
      assertNull(lc.getScoreVar());
    }
  }

  private String[][] expectedStudyResults() {
    return new String[][] {
        { "foo:node3" }, { "foo:node4" }, { "foo:node11" }, { "foo:node13" }, { "foo:node19" },
        { "foo:node22" },
    };
  }

  private String[][] expectedGroupResults() {
    return new String[][] {
        { "foo:node1" }, { "foo:node2" }, { "foo:node4" }, { "foo:node9" }, { "foo:node11" },
        { "foo:node12" }, { "foo:node13" }, { "foo:node14" }, { "foo:node18" },
    };
  }

  private static String[][] concat(String[][] a1, String[][] a2) {
    String[][] res = new String[a1.length + a2.length][];
    System.arraycopy(a1, 0, res, 0, a1.length);
    System.arraycopy(a2, 0, res, a1.length, a2.length);
    return res;
  }

  private void compareResults(Object[][] expected, Answer answer) throws Exception {
    compareResults(expected, answer, false);
  }

  private void compareResults(Object[][] expected, Answer answer, boolean lastIsLiteral)
      throws Exception {
    try {
      answer.beforeFirst();

      for (int i = 0; i < expected.length; i++) {
        assertTrue("Answer short at row " + i, answer.next());
        assertEquals(expected[i].length, answer.getNumberOfVariables());
        for (int j = 0; j < expected[i].length; j++) {
          if (expected[i][j] == null) {
            assertNull(answer.getObject(j));
          } else if (expected[i][j] instanceof String) {
            Object exp = (lastIsLiteral && j == expected[i].length - 1) ?
                            new LiteralImpl((String) expected[i][j]) :
                            new URIReferenceImpl(new URI((String) expected[i][j]));
            assertEquals(exp, answer.getObject(j));
          } else if (expected[i][j] instanceof Object[][]) {
            compareResults((Object[][]) expected[i][j], (Answer) answer.getObject(j), lastIsLiteral);
          } else {
            throw new IllegalArgumentException("Don't know how to handle expected value '" +
                                               expected[i][j] + "' of type " +
                                               expected[i][j].getClass() + "' at index " + i +
                                               "," + j);
          }
        }
      }

      assertFalse("Answer too long", answer.next());
    } catch (Exception e) {
      logger.error("Failed test - \n" + answer);
      throw e;
    } catch (Error e) {
      logger.error("Failed test - \n" + dumpAnswer(answer, "  "));
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

  private static String dumpAnswer(Answer answer, String indent) throws Exception {
    StringBuilder sb = new StringBuilder(500);

    answer.beforeFirst();
    while (answer.next()) {
      sb.append(indent).append("next-row\n");
      for (int j = 0; j < answer.getNumberOfVariables(); j++) {
        sb.append(indent).append("  column: " + answer.getObject(j) + "\n");
        if (answer.getObject(j) instanceof Answer) {
          sb.append(dumpAnswer((Answer) answer.getObject(j), indent + "    "));
        }
      }
    }

    sb.append(indent).append("end\n");
    return sb.toString();
  }


  private void fail(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    fail(stringWriter.toString());
  }

  private static class TestXid implements Xid {
    private final int xid;

    public TestXid(int xid) {
      this.xid = xid;
    }

    public int getFormatId() {
      return 'X';
    }

    public byte[] getBranchQualifier() {
      return new byte[] { (byte)(xid >> 0x00), (byte)(xid >> 0x08) };
    }

    public byte[] getGlobalTransactionId() {
      return new byte[] { (byte)(xid >> 0x10), (byte)(xid >> 0x18) };
    }
  }

  private static class TestSymbolicTransformationContext implements SymbolicTransformationContext {
    private final Map<URI,URI> mappings;

    public TestSymbolicTransformationContext(Map<URI,URI> mappings) {
      this.mappings = mappings;
    }

    public URI mapToModelTypeURI(URI modelURI) {
      return mappings.get(modelURI);
    }
  }

  private static class TestMutableLocalQuery implements MutableLocalQuery {
    private ConstraintExpression expr;

    public TestMutableLocalQuery(Query query) {
      expr = query.getConstraintExpression();
    }

    public ConstraintExpression getConstraintExpression() {
      return expr;
    }

    public void setConstraintExpression(ConstraintExpression newExpr) {
      expr = newExpr;
    }
  }

}
