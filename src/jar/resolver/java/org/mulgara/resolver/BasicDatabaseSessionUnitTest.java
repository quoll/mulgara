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

package org.mulgara.resolver;

// Java 2 standard packages
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.vocabulary.RDF;
import org.mulgara.query.Answer;
import org.mulgara.query.ArrayAnswer;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.GraphResource;
import org.mulgara.query.GraphUnion;
import org.mulgara.query.Order;
import org.mulgara.query.Query;
import org.mulgara.query.SelectElement;
import org.mulgara.query.UnconstrainedAnswer;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.MimeTypes;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.server.Session;
import org.mulgara.util.FileUtil;

/**
* Test case for {@link DatabaseSession}.
*
* @created 2004-04-27
* @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
* @version $Revision: 1.11 $
* @modified $Date: 2005/06/26 12:48:11 $ by $Author: pgearon $
* @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
* @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
*      Software Pty Ltd</a>
* @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
*/
public class BasicDatabaseSessionUnitTest extends TestCase {
  /** The URI of the {@link #database}: <code>local:database</code>.  */
  private static final URI databaseURI = URI.create("local:database");

  /**
   * The URI of the {@link #database}'s system model:
   * <code>local:database#</code>.
   */
  private static final URI systemModelURI = URI.create("local:database#");

  /**
    * The URI of the {@link #database}'s default graph:
    * <code>sys:default</code>.
    */
   private static final URI defaultGraphURI = URI.create("sys:default");

  /** The URI of the {@link #database}'s system model type.  */
  private static final URI memoryModelURI = URI.create(Mulgara.NAMESPACE+"MemoryModel");

  /** Logger.  */
  private static Logger logger =
    Logger.getLogger(BasicDatabaseSessionUnitTest.class.getName());

  /**
   * In-memory test {@link Database} used to generate {@link DatabaseSession}s
   * for testing.
   *
   * This is assigned a value by the {@link #setUp} method.
   */
  private Database database = null;

  /**
   * Constructs a new test with the given name.
   *
   * @param name  the test name
   */
  public BasicDatabaseSessionUnitTest(String name) {
    super(name);
  }


  /**
  * Hook for test runner to obtain a test suite from.
  *
  * @return the test suite
  */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new BasicDatabaseSessionUnitTest("testQuery1"));
    suite.addTest(new BasicDatabaseSessionUnitTest("testSetModel"));
    suite.addTest(new BasicDatabaseSessionUnitTest("testQuery2"));
    suite.addTest(new BasicDatabaseSessionUnitTest("testSetModelRdfXml"));
    suite.addTest(new BasicDatabaseSessionUnitTest("testSetModelN3"));

    return suite;
  }

  /**
  * Create test objects.
  */
  public void setUp() throws Exception {
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
      "org.mulgara.store.nodepool.memory.MemoryNodePoolFactory";

    // Define the string pool factory
    String stringPoolFactoryClassName =
      "org.mulgara.store.stringpool.memory.MemoryStringPoolFactory";

    // Define the resolver factory used to manage system models
    String systemResolverFactoryClassName =
      "org.mulgara.resolver.memory.MemoryResolverFactory";

    // Create a database which keeps its system models on the Java heap
    database = new Database(
        databaseURI,
        persistenceDirectory,
        null,                             // no security domain
        new JotmTransactionManagerFactory(),
        0,                                // default transaction timeout
        0,                                // default idle timeout
        nodePoolFactoryClassName,         // persistent
        null,
        stringPoolFactoryClassName,       // persistent
        null,
        systemResolverFactoryClassName,   // persistent
        null,
        nodePoolFactoryClassName,         // temporary
        null,
        stringPoolFactoryClassName,       // temporary
        null,
        systemResolverFactoryClassName,   // temporary
        null,
        "org.mulgara.content.rdfxml.RDFXMLContentHandler");
  }

  /**
  * The teardown method for JUnit
  */
  public void tearDown() {
    database.delete();
  }

  //
  // Test cases
  //

  /**
  * Test the {@link DatabaseSession#query} method, querying out the contents
  * of the system model in the newly-created {@link Database}.
  */
  public void testQuery1() {
    logger.info("TestQuery1");
    try {
      // Test querying the system model (#)
      Session session = database.newSession();
      try {
        Variable subjectVariable   = new Variable("subject");
        Variable predicateVariable = new Variable("predicate");
        Variable objectVariable    = new Variable("object");

        List<SelectElement> selectList = new ArrayList<SelectElement>(3);
        selectList.add(subjectVariable);
        selectList.add(predicateVariable);
        selectList.add(objectVariable);

        // Evaluate the query
        Answer answer = new ArrayAnswer(session.query(new Query(
            selectList,                         // SELECT
            new GraphResource(systemModelURI),  // FROM
            new ConstraintImpl(subjectVariable, // WHERE
                           predicateVariable,
                           objectVariable),
            null,                               // HAVING
            Collections.singletonList(          // ORDER BY
              new Order(subjectVariable, true)
            ),
            null,                               // LIMIT
            0,                                  // OFFSET
            true,                               // DISTINCT
            new UnconstrainedAnswer()           // GIVEN
          )));


        // Compose the expected result of the query
        Answer expectedAnswer = new ArrayAnswer(
            new Variable[] { subjectVariable, predicateVariable, objectVariable },
            new Object[] {
              new URIReferenceImpl(systemModelURI),
              new URIReferenceImpl(RDF.TYPE),
              new URIReferenceImpl(memoryModelURI),

              new URIReferenceImpl(defaultGraphURI),
              new URIReferenceImpl(RDF.TYPE),
              new URIReferenceImpl(memoryModelURI)
            });

        // Verify that the query result is as expected
        assertEquals(expectedAnswer, answer);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
    assert database != null;
  }

  /**
  * Test the {@link DatabaseSession#setModel} method.
  */
  public void testSetModel() throws URISyntaxException {
    logger.info("testSetModel");
    URI fileURI  = new File("data/dc.rdfs").toURI();
    URI modelURI = new URI("local:database#model");

    try {
      // Register the URL resolver so we can load test data
      database.addResolverFactory("org.mulgara.resolver.url.URLResolverFactory", null);

      // Load some test data
      Session session = database.newSession();
      try {
        session.createModel(modelURI, null);
        session.setModel(modelURI, fileURI);
        session.removeModel(modelURI);
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
  * Test {@link DatabaseSession#query} against a query with a union in the
  * <code>FROM</code> clause.
  */

  public void testQuery2() throws URISyntaxException {
    logger.info("Testing testQuery2");
    URI dcFileURI   = new File("data/dc.rdfs").toURI();
    URI rdfsFileURI = new File("data/rdfs.rdfs").toURI();

    try {
      // Register the URL resolver so we can query the test data
      database.addResolverFactory("org.mulgara.resolver.url.URLResolverFactory", null);

      // Load some test data
      Session session = database.newSession();
      try {
        Variable subjectVariable   = new Variable("subject");
        Variable predicateVariable = new Variable("predicate");
        Variable objectVariable    = new Variable("object");

        List<SelectElement> selectList = new ArrayList<SelectElement>(3);
        selectList.add(subjectVariable);
        selectList.add(predicateVariable);
        selectList.add(objectVariable);

        // Evaluate the query
        new ArrayAnswer(session.query(new Query(
          selectList,                                       // SELECT
          new GraphUnion(new GraphResource(dcFileURI),      // FROM
                         new GraphResource(rdfsFileURI)),
          new ConstraintImpl(subjectVariable,               // WHERE
                         predicateVariable,
                         objectVariable),
          null,                                             // HAVING
          Collections.singletonList(                        // ORDER BY
            new Order(subjectVariable, true)
          ),
          null,                                             // LIMIT
          0,                                                // OFFSET
          true,                                             // DISTINCT
          new UnconstrainedAnswer()                         // GIVEN
        )));
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  private static SubjectNode TEST_SUBJECT = new URIReferenceImpl(URI.create("http://example.org/base/x"));
  private static PredicateNode TEST_PREDICATE = new URIReferenceImpl(URI.create("http://example.org/ns#p"));
  private static ObjectNode TEST_OBJECT = new LiteralImpl("foo");
  
  public void testSetModelRdfXml() throws Exception {
    URI graph = URI.create("test:graph");
    URI base = URI.create("http://example.org/base/");
    Session session = database.newSession();
    try {
      session.createModel(graph, null);
      FileInputStream in = new FileInputStream("data/rel-uri.rdf");
      try {
        session.setModel(in, graph, base, MimeTypes.APPLICATION_RDF_XML);
      } finally {
        in.close();
      }
      
      assertHasData(session, graph, TEST_SUBJECT, TEST_PREDICATE, TEST_OBJECT);
    } finally {
      session.close();
    }
  }
  
  public void testSetModelN3() throws Exception {
    database.addContentHandler("org.mulgara.content.n3.N3ContentHandler");
    
    URI graph = URI.create("test:graph");
    URI base = URI.create("http://example.org/base/");
    Session session = database.newSession();
    try {
      session.createModel(graph, null);
      FileInputStream in = new FileInputStream("data/rel-uri.ttl");
      try {
        session.setModel(in, graph, base, MimeTypes.TEXT_TURTLE);
      } finally {
        in.close();
      }
      
      assertHasData(session, graph, TEST_SUBJECT, TEST_PREDICATE, TEST_OBJECT);
      
      // Verify we can export as Turtle.
      File f = File.createTempFile("results", "ttl");
      f.deleteOnExit();
      FileOutputStream out = new FileOutputStream(f);
      try {
        session.export(graph, out, MimeTypes.TEXT_TURTLE);
      } finally {
        out.close();
      }
      
      URI graph2 = URI.create("test:graph2");
      session.createModel(graph2, null);
      
      in = new FileInputStream(f);
      try {
        session.setModel(in, graph2, null, MimeTypes.TEXT_TURTLE);
      } finally {
        in.close();
      }
      
      assertHasData(session, graph2, TEST_SUBJECT, TEST_PREDICATE, TEST_OBJECT);
    } finally {
      session.close();
    }
  }
  
  private static void assertHasData(Session session, URI graph, SubjectNode s, PredicateNode p, ObjectNode o) throws Exception {
    Variable subjectVariable   = new Variable("subject");
    Variable predicateVariable = new Variable("predicate");
    Variable objectVariable    = new Variable("object");

    List<SelectElement> selectList = new ArrayList<SelectElement>(3);
    selectList.add(subjectVariable);
    selectList.add(predicateVariable);
    selectList.add(objectVariable);

    // Evaluate the query
    Answer a = session.query(new Query(
      selectList,                                       // SELECT
      new GraphResource(graph),                         // FROM
      new ConstraintImpl(subjectVariable,               // WHERE
                     predicateVariable,
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
    
    try {
      assertTrue(a.next());
      assertEquals(s, a.getObject(0));
      assertEquals(p, a.getObject(1));
      assertEquals(o, a.getObject(2));
      assertFalse(a.next());
    } finally {
      a.close();
    }
  }

  //
  // Internal methods
  //

  /**
  * Fail with an unexpected exception
  */
  private void fail(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    fail(stringWriter.toString());
  }
}
