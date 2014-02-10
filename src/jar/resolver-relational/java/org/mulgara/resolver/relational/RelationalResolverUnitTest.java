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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.relational;

// Java 2 standard packages
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.sql.*;

// Third party packages
import junit.framework.*;        // JUnit
import org.apache.log4j.Logger;  // Log4J
import org.jrdf.vocabulary.RDF;  // JRDF

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.XSD;
import org.mulgara.server.Session;
import org.mulgara.util.FileUtil;
import org.mulgara.util.TempDir;

import org.mulgara.resolver.JotmTransactionManagerFactory;
import org.mulgara.resolver.Database;

/**
* Test case for the {@link RelationalResolver}.
*
* @created 2004-04-27
* @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
* @version $Revision: 1.1.1.1 $
* @modified $Date: 2005/10/30 19:21:14 $ by $Author: prototypo $
*      All rights reserved.
* @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
*/
public class RelationalResolverUnitTest extends TestCase {

  private static final URI databaseURI;
  private static final URI systemModelURI;
  private static final URI testModelURI;
  private static final URI testModelDefURI;
  private static final URI testModel2URI;
  private static final URI testModel2DefURI;
  private static final URI relationalModelTypeURI;

  static {
    try {
      databaseURI    = new URI("rmi://localhost/database");
      systemModelURI = new URI("rmi://localhost/database#");
      
      testModelURI = new URI("rmi://localhost/database#test");
      testModelDefURI = new URI("rmi://localhost/database?def#test");

      testModel2URI = new URI("rmi://localhost/database#test2");
      testModel2DefURI = new URI("rmi://localhost/database?def#test2");

      relationalModelTypeURI = new URI("http://mulgara.org/mulgara#RelationalModel");
    } catch (URISyntaxException e) {
      throw new Error("Bad hardcoded URI", e);
    }
  }

  /** create the temporary directory */
  @SuppressWarnings("unused")
  private static final File tmpDir = TempDir.getTempDir();

  /** Logger.  */
  @SuppressWarnings("unused")
  private static Logger logger =
    Logger.getLogger(RelationalResolverUnitTest.class.getName());

  /**
  * In-memory test {@link Database} used to generate {@link DatabaseSession}s
  * for testing.
  *
  * This is assigned a value by the {@link #setUp} method.
  */
  private static Database database = null;

  /**
  * Constructs a new test with the given name.
  *
  * @param testcase  the bundled parameters for the test
  */
  public RelationalResolverUnitTest(String name) {
    super(name);
  }


  /**
  * Hook for test runner to obtain a test suite from.
  *
  * @return the test suite
  */
  public static Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTest(new RelationalResolverUnitTest("testSetupSQLDatabase"));

    suite.addTest(new RelationalResolverUnitTest("testCreateRelationalModel"));
    suite.addTest(new RelationalResolverUnitTest("testLoadRelationalDef"));

    suite.addTest(new RelationalResolverUnitTest("testBasicClassQuery"));
    suite.addTest(new RelationalResolverUnitTest("testBasicPropertyQuery"));
    suite.addTest(new RelationalResolverUnitTest("testDatePropertyQuery"));
    suite.addTest(new RelationalResolverUnitTest("testBoundPropertyQuery"));
    suite.addTest(new RelationalResolverUnitTest("testBoundSubjectQuery"));
    suite.addTest(new RelationalResolverUnitTest("testConjPropertyQuery"));

    suite.addTest(new RelationalResolverUnitTest("testURIPatternQuery"));
    suite.addTest(new RelationalResolverUnitTest("testURIPatternPropertyQuery"));

    suite.addTest(new RelationalResolverUnitTest("testBasicPredicateQuery"));
    suite.addTest(new RelationalResolverUnitTest("testBasicPredicateObjectQuery"));
    suite.addTest(new RelationalResolverUnitTest("testCompoundPredicateVoidQuery"));
    suite.addTest(new RelationalResolverUnitTest("testCompoundPredicateQuery"));
    suite.addTest(new RelationalResolverUnitTest("testCompoundPredicateObjectQuery"));

    suite.addTest(new RelationalResolverUnitTest("testBasicPatternPropertyQuery"));
    suite.addTest(new RelationalResolverUnitTest("testBoundPatternPropertyQuery"));
    suite.addTest(new RelationalResolverUnitTest("testBoundPatternSubjectQuery"));

    suite.addTest(new RelationalResolverUnitTest("testBasicReferWithJoinQuery"));
    suite.addTest(new RelationalResolverUnitTest("testBasicReferWithJoinQueryAndPropPattern"));
    suite.addTest(new RelationalResolverUnitTest("testBasicReferWithPropPattern"));
    suite.addTest(new RelationalResolverUnitTest("testObjectReferWithMNJoin"));
    suite.addTest(new RelationalResolverUnitTest("testObjectReferWithPK"));

    suite.addTest(new RelationalResolverUnitTest("testBasicQueryWithAdditionalProperty"));

    suite.addTest(new RelationalResolverUnitTest("testCreateRelationalModel2"));
    suite.addTest(new RelationalResolverUnitTest("testLoadRelationalDef2"));

    suite.addTest(new RelationalResolverUnitTest("testDualDatabaseQuery"));

    suite.addTest(new RelationalResolverUnitTest("testDeleteDatabase"));

    return suite;
  }

  /**
  * Create test objects.
  */
  @SuppressWarnings("deprecation")
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
                   "",                              // no rule loader
                   "org.mulgara.content.rdfxml.RDFXMLContentHandler");

      database.addResolverFactory("org.mulgara.resolver.url.URLResolverFactory", null);
      database.addResolverFactory("org.mulgara.resolver.relational.RelationalResolverFactory", null);
    }
  }

  /**
  * The teardown method for JUnit
  */
  public void tearDown() {
//    database.delete();
  }

  //
  // Test cases
  //

  public void testSetupSQLDatabase() throws Exception {
    File sqlDir = new File(new File(System.getProperty("cvs.root")), "sqltest");
    if (sqlDir.isDirectory()) {
      if (!FileUtil.deleteDirectory(sqlDir)) {
        throw new RuntimeException("Unable to remove old directory " + sqlDir);
      }
    }

//    Class.forName( "smallsql.database.SSDriver" );
    Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
    String url = "jdbc:derby:sqltest;create=true";
    Connection con = DriverManager.getConnection(url);
    Statement stmt = con.createStatement();
    stmt.execute("CREATE TABLE Persons (" +
                 "PersonID integer, " +
                 "FirstName varchar(50), " +
                 "LastName varchar(50), " +
                 "Address varchar(50), " +
                 "Email varchar(50), " +
                 "Homepage varchar(50), " +
                 "Phone varchar(50), " +
                 "URI varchar(50), " +
                 "Photo varchar(50))");

    stmt.execute("INSERT INTO Persons (PersonID, FirstName, LastName, URI, Email) " +
                 " VALUES ( 1, 'Albert', 'Smith', 'http://www.smith.id/albert', 'albert@email.com')");
    stmt.execute("INSERT INTO Persons (PersonID, FirstName, LastName, URI, Email) " +
                 " VALUES ( 2, 'Brian', 'Carson', 'http://www.carson.id/brian', 'brian@email.com')");

    stmt.execute("CREATE TABLE Conferences (" +
                 "ConfID integer, " +
                 "Name varchar(50), " +
                 "URI varchar(50), " +
                 "Date date, " +
                 "Location varchar(50))");
    stmt.execute("INSERT INTO Conferences (ConfID, Name, URI, Date, Location) " +
                 " VALUES (1, 'Apes and Bears', 'http://www.apes.conf.au/bears', '2008-02-01', 'Brisbane')");
    stmt.execute("INSERT INTO Conferences (ConfID, Name, URI, Date, Location) " +
                 " VALUES (2, 'Cats and Donkeys', 'http://www.cats.conf.au/donkeys', '2008-04-03', 'Dublin')");

    stmt.execute("CREATE TABLE Papers (" +
                 "PaperID integer, " +
                 "Title varchar(50), " +
                 "Abstract varchar(50), " +
                 "URI varchar(50), " +
                 "PubYear varchar(50), " +
                 "Conference integer, " +
                 "Publish integer)");
    stmt.execute("INSERT INTO Papers (PaperID, Title, Abstract, URI, PubYear, Conference, Publish) " +
                 "VALUES (1, 'Apes and their Friends', 'We like Apes', 'http://www.welikeapes.com/', '2008', 1, 1)");
    stmt.execute("INSERT INTO Papers (PaperID, Title, Abstract, URI, PubYear, Conference, Publish) " +
                 "VALUES (2, 'Bears like us too', 'Do we like Bears?', 'http://www.bearspetition.com/', '2008', 1, 1)");
    stmt.execute("INSERT INTO Papers (PaperID, Title, Abstract, URI, PubYear, Conference, Publish) " +
                 "VALUES (3, 'Why Cats?', 'I prefer Donkeys', 'http://www.bizare.org/', '2007', 2, 1)");
    stmt.execute("INSERT INTO Papers (PaperID, Title, Abstract, URI, PubYear, Conference, Publish) " +
                 "VALUES (4, 'Miscellaneous Donkeys', 'Donkeys in Myth and Legend', 'http://www.bray.org/', '2009', 2, 0)");

    stmt.execute("CREATE TABLE Topics (" +
                 "TopicID integer, " +
                 "TopicName varchar(50), " +
                 "URI varchar(50))");
    stmt.execute("INSERT INTO Topics (TopicID, TopicName, URI) " +
                 "VALUES (1, 'Apes', 'http://topics.org/apes')");
    stmt.execute("INSERT INTO Topics (TopicID, TopicName, URI) " +
                 "VALUES (2, 'Bears', 'http://topics.org/bears')");
    stmt.execute("INSERT INTO Topics (TopicID, TopicName, URI) " +
                 "VALUES (3, 'Cats', null)");
    stmt.execute("INSERT INTO Topics (TopicID, TopicName, URI) " +
                 "VALUES (4, 'Donkeys', 'http://topics.org/donkeys')");

    stmt.execute("CREATE TABLE Rel_Person_Paper (" +
                 "PersonID integer, " +
                 "PaperID integer)");
    stmt.execute("INSERT INTO Rel_Person_Paper (PersonID, PaperID) " +
                 "VALUES (1, 1)");
    stmt.execute("INSERT INTO Rel_Person_Paper (PersonID, PaperID) " +
                 "VALUES (1, 2)");
    stmt.execute("INSERT INTO Rel_Person_Paper (PersonID, PaperID) " +
                 "VALUES (2, 2)");
    stmt.execute("INSERT INTO Rel_Person_Paper (PersonID, PaperID) " +
                 "VALUES (2, 3)");

    ResultSet test;
    
    test = stmt.executeQuery("SELECT Persons.URI FROM Persons WHERE Persons.PersonID = 2");
    assertTrue(test.next());
    assertEquals("http://www.carson.id/brian", test.getString(1));
  }


  public void testCreateRelationalModel() throws Exception {
    Session session = database.newSession();
    try {
      session.createModel(testModelURI, relationalModelTypeURI);
    } finally {
      session.close();
    }
  }


  public void testLoadRelationalDef() throws Exception {
    Session session = database.newSession();
    try {
      session.setModel(testModelDefURI, new File("data/ISWC-d2rq.rdf").toURI());
    } finally {
      session.close();
    }
  }


  public void testBasicClassQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Collections.singletonList(new Variable("s"));


      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintImpl(new Variable("s"),
                             new URIReferenceImpl(RDF.TYPE),
                             new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                             new URIReferenceImpl(testModelURI)),
          null,                     // HAVING
          Collections.singletonList(new Order(new Variable("s"), true)),     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.smith.id/albert")), answer.getObject(0));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBasicPropertyQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s"), new Variable("fn") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
          new Order(new Variable("fn"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#firstName")),
                                 new Variable("fn"),
                                 new URIReferenceImpl(testModelURI))),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new LiteralImpl("Brian"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.smith.id/albert")), answer.getObject(0));
      assertEquals(new LiteralImpl("Albert"), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testDatePropertyQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("title"), new Variable("date") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("title"), true), 
          new Order(new Variable("date"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
              new ConstraintImpl(new Variable("conf"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Conference")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("conf"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#eventTitle")),
                                 new Variable("title"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("conf"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#date")),
                                 new Variable("date"),
                                 new URIReferenceImpl(testModelURI))
          })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Apes and Bears"), answer.getObject(0));
      assertEquals(new LiteralImpl("2008-02-01", XSD.DATE_URI), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Cats and Donkeys"), answer.getObject(0));
      assertEquals(new LiteralImpl("2008-04-03", XSD.DATE_URI), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBoundPropertyQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[]
            {
                new ConstraintImpl(new Variable("s"),
                                   new URIReferenceImpl(RDF.TYPE),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new Variable("s"),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#lastName")),
                                   new LiteralImpl("Smith"),
                                   new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.smith.id/albert")), answer.getObject(0));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBoundSubjectQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("o") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("o"), true), 
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[]
            {
                new ConstraintImpl(new URIReferenceImpl(new URI("http://www.smith.id/albert")),
                                   new URIReferenceImpl(RDF.TYPE),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new URIReferenceImpl(new URI("http://www.smith.id/albert")),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#lastName")),
                                   new Variable("o"),
                                   new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Smith"), answer.getObject(0));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testConjPropertyQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s"), new Variable("fn") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
          new Order(new Variable("fn"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[]
            {
                new ConstraintImpl(new Variable("s"),
                                   new URIReferenceImpl(RDF.TYPE),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new Variable("s"),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#firstName")),
                                   new Variable("fn"),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new Variable("s"),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#lastName")),
                                   new LiteralImpl("Smith"),
                                   new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
//      assertTrue(answer.next());
//      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
//      assertEquals(new LiteralImpl("Brian"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.smith.id/albert")), answer.getObject(0));
      assertEquals(new LiteralImpl("Albert"), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testURIPatternQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Collections.singletonList(new Variable("s"));


      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintImpl(new Variable("s"),
                             new URIReferenceImpl(RDF.TYPE),
                             new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Conference")),
                             new URIReferenceImpl(testModelURI)),
          null,                     // HAVING
          Collections.singletonList(new Order(new Variable("s"), true)),     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://conferences.org/comp/confno1")), answer.getObject(0));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://conferences.org/comp/confno2")), answer.getObject(0));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testURIPatternPropertyQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s"), new Variable("loc") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
          new Order(new Variable("loc"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Conference")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#location")),
                                 new Variable("loc"),
                                 new URIReferenceImpl(testModelURI))),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://conferences.org/comp/confno1")), answer.getObject(0));
      assertEquals(new LiteralImpl("Brisbane"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://conferences.org/comp/confno2")), answer.getObject(0));
      assertEquals(new LiteralImpl("Dublin"), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBasicPredicateQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s"), new Variable("p") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
          new Order(new Variable("p"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new Variable("p"),
                                 new LiteralImpl("Brian"),
                                 new URIReferenceImpl(testModelURI))),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#firstName")), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBasicPredicateObjectQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s"), new Variable("p"), new Variable("o") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
          new Order(new Variable("p"), true),
          new Order(new Variable("o"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new Variable("p"),
                                 new Variable("o"),
                                 new URIReferenceImpl(testModelURI))),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#author_of")), answer.getObject(1));
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper2")), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#author_of")), answer.getObject(1));
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper3")), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#eMail")), answer.getObject(1));
      assertEquals(new URIReferenceImpl(new URI("mailto:brian@email.com")), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#firstName")), answer.getObject(1));
      assertEquals(new LiteralImpl("Brian"), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#lastName")), answer.getObject(1));
      assertEquals(new LiteralImpl("Carson"), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.smith.id/albert")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#author_of")), answer.getObject(1));
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper1")), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.smith.id/albert")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#author_of")), answer.getObject(1));
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper2")), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.smith.id/albert")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#eMail")), answer.getObject(1));
      assertEquals(new URIReferenceImpl(new URI("mailto:albert@email.com")), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.smith.id/albert")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#firstName")), answer.getObject(1));
      assertEquals(new LiteralImpl("Albert"), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.smith.id/albert")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#lastName")), answer.getObject(1));
      assertEquals(new LiteralImpl("Smith"), answer.getObject(2));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testCompoundPredicateVoidQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s"), new Variable("p"), new Variable("q") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
          new Order(new Variable("p"), true),
          new Order(new Variable("q"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[]
              {
                new ConstraintImpl(new Variable("s"),
                                   new URIReferenceImpl(RDF.TYPE),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new Variable("s"),
                                   new Variable("p"),
                                   new LiteralImpl("Brian"),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new Variable("s"),
                                   new Variable("q"),
                                   new LiteralImpl("Smith"),
                                   new URIReferenceImpl(testModelURI)),
              })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testCompoundPredicateQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s"), new Variable("p"), new Variable("q") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
          new Order(new Variable("p"), true),
          new Order(new Variable("q"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[]
              {
                new ConstraintImpl(new Variable("s"),
                                   new URIReferenceImpl(RDF.TYPE),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new Variable("s"),
                                   new Variable("p"),
                                   new LiteralImpl("Brian"),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new Variable("s"),
                                   new Variable("q"),
                                   new LiteralImpl("Carson"),
                                   new URIReferenceImpl(testModelURI)),
              })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#firstName")), answer.getObject(1));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#lastName")), answer.getObject(2));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testCompoundPredicateObjectQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s"), new Variable("p"), new Variable("o") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
          new Order(new Variable("p"), true),
          new Order(new Variable("o"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[]
              {
                new ConstraintImpl(new Variable("s"),
                                   new URIReferenceImpl(RDF.TYPE),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new Variable("s"),
                                   new Variable("p"),
                                   new Variable("o"),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new Variable("s"),
                                   new Variable("q"),
                                   new LiteralImpl("Carson"),
                                   new URIReferenceImpl(testModelURI)),
              })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#author_of")), answer.getObject(1));
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper2")), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#author_of")), answer.getObject(1));
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper3")), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#eMail")), answer.getObject(1));
      assertEquals(new URIReferenceImpl(new URI("mailto:brian@email.com")), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#firstName")), answer.getObject(1));
      assertEquals(new LiteralImpl("Brian"), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.carson.id/brian")), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#lastName")), answer.getObject(1));
      assertEquals(new LiteralImpl("Carson"), answer.getObject(2));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBasicPatternPropertyQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s"), new Variable("title") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
          new Order(new Variable("title"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#InProceedings")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#title")),
                                 new Variable("title"),
                                 new URIReferenceImpl(testModelURI))),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper1")), answer.getObject(0));
      assertEquals(new LiteralImpl("Titel of the Paper: Apes and their Friends", "en"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper2")), answer.getObject(0));
      assertEquals(new LiteralImpl("Titel of the Paper: Bears like us too", "en"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper3")), answer.getObject(0));
      assertEquals(new LiteralImpl("Titel of the Paper: Why Cats?", "en"), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBoundPatternPropertyQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[]
            {
                new ConstraintImpl(new Variable("s"),
                                   new URIReferenceImpl(RDF.TYPE),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#InProceedings")),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new Variable("s"),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#title")),
                                   new LiteralImpl("Titel of the Paper: Why Cats?"),
                                   new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper3")), answer.getObject(0));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBoundPatternSubjectQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("o") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("o"), true), 
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[]
            {
                new ConstraintImpl(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper3")),
                                   new URIReferenceImpl(RDF.TYPE),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#InProceedings")),
                                   new URIReferenceImpl(testModelURI)),
                new ConstraintImpl(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper3")),
                                   new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#title")),
                                   new Variable("o"),
                                   new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Titel of the Paper: Why Cats?", "en"), answer.getObject(0));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }



  public void testBasicReferWithJoinQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("name"), new Variable("abstract") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("name"), true), 
          new Order(new Variable("abstract"), true), 
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Conference")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#eventTitle")),
                                 new Variable("name"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("p"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#InProceedings")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("p"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#conference")),
                                 new Variable("s"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("p"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#abstract")),
                                 new Variable("abstract"),
                                 new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Apes and Bears"), answer.getObject(0));
      assertEquals(new LiteralImpl("Do we like Bears?"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Apes and Bears"), answer.getObject(0));
      assertEquals(new LiteralImpl("We like Apes"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Cats and Donkeys"), answer.getObject(0));
      assertEquals(new LiteralImpl("I prefer Donkeys"), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBasicReferWithJoinQueryAndPropPattern() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] {
          new Variable("name"),
          new Variable("abstract"),
          new Variable("title"),
        });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("name"), true), 
          new Order(new Variable("abstract"), true), 
          new Order(new Variable("title"), true),
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Conference")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#eventTitle")),
                                 new Variable("name"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("p"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#InProceedings")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("p"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#conference")),
                                 new Variable("s"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("p"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#abstract")),
                                 new Variable("abstract"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("p"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#title")),
                                 new Variable("title"),
                                 new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Apes and Bears"), answer.getObject(0));
      assertEquals(new LiteralImpl("Do we like Bears?"), answer.getObject(1));
      assertEquals(new LiteralImpl("Titel of the Paper: Bears like us too", "en"), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Apes and Bears"), answer.getObject(0));
      assertEquals(new LiteralImpl("We like Apes"), answer.getObject(1));
      assertEquals(new LiteralImpl("Titel of the Paper: Apes and their Friends", "en"), answer.getObject(2));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Cats and Donkeys"), answer.getObject(0));
      assertEquals(new LiteralImpl("I prefer Donkeys"), answer.getObject(1));
      assertEquals(new LiteralImpl("Titel of the Paper: Why Cats?", "en"), answer.getObject(2));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBasicReferWithPropPattern() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] {
          new Variable("abstract"),
          new Variable("title"),
        });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("abstract"), true), 
          new Order(new Variable("title"), true),
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
              new ConstraintImpl(new Variable("p"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#InProceedings")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("p"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#abstract")),
                                 new Variable("abstract"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("p"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#title")),
                                 new Variable("title"),
                                 new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Do we like Bears?"), answer.getObject(0));
      assertEquals(new LiteralImpl("Titel of the Paper: Bears like us too", "en"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("I prefer Donkeys"), answer.getObject(0));
      assertEquals(new LiteralImpl("Titel of the Paper: Why Cats?", "en"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("We like Apes"), answer.getObject(0));
      assertEquals(new LiteralImpl("Titel of the Paper: Apes and their Friends", "en"), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testObjectReferWithMNJoin() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("title"), new Variable("author") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("title"), true), 
          new Order(new Variable("author"), true), 
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
              new ConstraintImpl(new Variable("per"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("pap"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#InProceedings")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("pap"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#title")),
                                 new Variable("title"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("per"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#author_of")),
                                 new Variable("pap"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("per"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#lastName")),
                                 new Variable("author"),
                                 new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Titel of the Paper: Apes and their Friends", "en"), answer.getObject(0));
      assertEquals(new LiteralImpl("Smith"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Titel of the Paper: Bears like us too", "en"), answer.getObject(0));
      assertEquals(new LiteralImpl("Carson"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Titel of the Paper: Bears like us too", "en"), answer.getObject(0));
      assertEquals(new LiteralImpl("Smith"), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Titel of the Paper: Why Cats?", "en"), answer.getObject(0));
      assertEquals(new LiteralImpl("Carson"), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testObjectReferWithPK() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("author"), new Variable("paper") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("author"), true), 
          new Order(new Variable("paper"), true), 
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
              new ConstraintImpl(new Variable("per"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Person")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("per"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#lastName")),
                                 new Variable("author"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("per"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#author_of")),
                                 new Variable("paper"),
                                 new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Carson"), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper2")), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Carson"), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper3")), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Smith"), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper1")), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Smith"), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://www.conference.org/conf02004/paper#Paper2")), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testBasicQueryWithAdditionalProperty() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("title"), new Variable("seeAlso") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("title"), true), 
          new Order(new Variable("seeAlso"), true), 
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Conference")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#seeAlso")),
                                 new Variable("seeAlso"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#eventTitle")),
                                 new Variable("title"),
                                 new URIReferenceImpl(testModelURI)),
            })),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Apes and Bears"), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml")), answer.getObject(1));
      assertTrue(answer.next());
      assertEquals(new LiteralImpl("Cats and Donkeys"), answer.getObject(0));
      assertEquals(new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml")), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }


  public void testCreateRelationalModel2() throws Exception {
    Session session = database.newSession();
    try {
      session.createModel(testModel2URI, relationalModelTypeURI);
    } finally {
      session.close();
    }
  }


  public void testLoadRelationalDef2() throws Exception {
    Session session = database.newSession();
    try {
      session.setModel(testModel2DefURI, new File("data/ISWC-d2rq.rdf").toURI());
    } finally {
      session.close();
    }
  }


  public void testDualDatabaseQuery() throws Exception {
    Session session = database.newSession();
    try {
      List<Variable> selectList = Arrays.asList(new Variable[] { new Variable("s"), new Variable("loc") });
      List<Order> orderList = Arrays.asList(new Order[] {
          new Order(new Variable("s"), true), 
          new Order(new Variable("loc"), true)
        });

      Answer answer = session.query(new Query(
          selectList,          // SELECT
          new GraphResource(systemModelURI),               // FROM
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Conference")),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#eventTitle")),
                                 new LiteralImpl("Apes and Bears"),
                                 new URIReferenceImpl(testModelURI)),
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(RDF.TYPE),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#Conference")),
                                 new URIReferenceImpl(testModel2URI)),
              new ConstraintImpl(new Variable("s"),
                                 new URIReferenceImpl(new URI("http://annotation.semanticweb.org/iswc/iswc.daml#location")),
                                 new Variable("loc"),
                                 new URIReferenceImpl(testModel2URI))})),
          null,                     // HAVING
          orderList,     // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        ));

      answer.beforeFirst();
      assertTrue(answer.next());
      assertEquals(new URIReferenceImpl(new URI("http://conferences.org/comp/confno1")), answer.getObject(0));
      assertEquals(new LiteralImpl("Brisbane"), answer.getObject(1));
      assertFalse(answer.next());
      answer.close();
        
    } finally {
      session.close();
    }
  }



  public void testDeleteDatabase() {
    database.delete();
    database = null;
  }
}
