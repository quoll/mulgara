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
import java.net.URI;
import java.util.*;

// Third party packages
import junit.framework.*; // JUnit
import org.apache.log4j.Logger; // Log4J
import org.jrdf.vocabulary.RDF; // JRDF
import org.jrdf.vocabulary.RDFS; // JRDF

// Locally written packages
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.mulgara.query.*;
import org.mulgara.query.rdf.TripleImpl;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.server.Session;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.util.FileUtil;

/**
 * Test case for {@link DatabaseSession}.
 *
 * @created 2004-04-27
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.10 $
 * @modified $Date: 2005/06/26 12:48:11 $ by $Author: pgearon $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class DatabaseSessionListQueryUnitTest extends TestCase {
  /** The URI of the {@link #database}: <code>local:database</code>.  */
  private static final URI databaseURI = URI.create("local:database");

  /** The URI of the {@link #database}'s system model type.  */
  private static final URI memoryModelURI = URI.create(Mulgara.NAMESPACE + "MemoryModel");

  /** A list of model URIs that can be queried.
   * local:database#model0 -> local:database#modelNUM_MODELS
   */
  private static final List<URI> modelURIs;

  /** Number of models to be queried */
  private static final int NUM_MODELS = 10;

  /** Number of test statements to be inserted into each model */
  private static final int NUM_STATEMENTS = 20;

  static {
    //generate a number of models to be 'batch' queried
    modelURIs = new ArrayList<URI>();
    for (int i = 0; i < NUM_MODELS; i++) {
      modelURIs.add(URI.create("local:database#model" + i));
    }
  }

  /** Logger.  */
  private static Logger logger = Logger.getLogger(DatabaseSessionListQueryUnitTest.class.getName());

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
  public DatabaseSessionListQueryUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return the test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new DatabaseSessionListQueryUnitTest("testListQuery"));

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
      throw new Exception("Unable to create directory " + persistenceDirectory);
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
        null, // no security domain
        new JotmTransactionManagerFactory(),
        0, // default transaction timeout
        0, // default idle timeout
        nodePoolFactoryClassName, // persistent
        null,
        stringPoolFactoryClassName, // persistent
        null,
        systemResolverFactoryClassName, // persistent
        null,
        nodePoolFactoryClassName, // temporary
        null,
        stringPoolFactoryClassName, // temporary
        null,
        systemResolverFactoryClassName, // temporary
        null,
        null);  // no default content handler

    //create and populate models
    Session session = null;
    try {
      session = database.newSession();
      for (int i = 0; i < modelURIs.size(); i++) {
        createModel((URI) modelURIs.get(i), session);
        populateModel((URI) modelURIs.get(i), session);
      }
    } finally {
      if (session != null) {
        session.close();
      }
    }
  }

  /**
   * The teardown method for JUnit
   */
  public void tearDown() throws Exception{

    //drop models
    Session session = null;
    try {
      session = database.newSession();
      for (int i = 0; i < modelURIs.size(); i++) {
        dropModel((URI) modelURIs.get(i), session);
      }
    } finally {
      if (session != null) {
        session.close();
      }
    }
    database.delete();
  }

  //
  // Test cases
  //

  /**
   * Test the {@link DatabaseSession#query} method, querying out the contents
   * of the system model in the newly-created {@link Database}.
   */
  public void testListQuery() throws Exception {
    logger.debug("Testing testListQuery");

    //used to execute the query
    Session session = null;
    try {

      session = database.newSession();
      List<Query> queryList = generateQueries();
      List<Answer> answerList = session.query(queryList);

      //perform operations on the answers
      assertNotNull("Session.query(List) returned null.", answerList);
      final int numAnswers = answerList.size();
      //ensure the number of answers match the number of queries
      assertEquals("Number of Answers did not match the number of Queries.",
          numAnswers, queryList.size());

      //check each answer
      for (int i = 0; i < numAnswers; i++) {

        //each model should have been populated with NUM_STATEMENTS, statements
        assertEquals("Answer does not contain expected row count.",
            ((Answer) answerList.get(i)).getRowCount(), NUM_STATEMENTS);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    finally {
      if (session != null) {
        session.close();
      }
    }
    assert database != null;
  }

  /**
   * Returns a List of Query Objects for a 'batch' query.
   *
   * @throws Exception
   * @return List
   */
  private List<Query> generateQueries() throws Exception {

    List<Query> list = new ArrayList<Query>();

    //create a selectAll query for each modelURI
    for (int i = 0; i < modelURIs.size(); i++) {

      list.add(selectAll(modelURIs.get(i)));
    }

    return list;
  }

  /**
   * Returns a query to select all statements from the model.
   *
   * @param modelURI URI
   * @throws Exception
   * @return Query
   */
  @SuppressWarnings("unchecked")
  private Query selectAll(URI modelURI) throws Exception {

    //select $s $p $o
    Variable[] vars = new Variable[] {
        StatementStore.VARIABLES[0],
        StatementStore.VARIABLES[1],
        StatementStore.VARIABLES[2]
    };

    //from <modelURI>
    GraphResource model = new GraphResource(modelURI);

    //where $s $p $o
    ConstraintImpl varConstraint = new ConstraintImpl(vars[0], vars[1], vars[2]);

    return new Query(
        Arrays.asList((SelectElement[])vars), // variable list
        model, // model expression
        varConstraint, // constraint expr
        null, // no having
        (List<Order>)Collections.EMPTY_LIST, // no ordering
        null, // no limit
        0, // zero offset
        true, // distinct results
        new UnconstrainedAnswer() // nothing given
        );
  }

  /**
   * Inserts some data into the model.
   *
   * @param modelURI URI
   * @param session Session
   * @throws Exception
   */
  private void populateModel(URI modelURI, Session session) throws Exception {

    Set<Triple> statements = new HashSet<Triple>();

    //add some statements
    URIReference subject = null;
    URIReference predicate = new URIReferenceImpl(new URI(RDF.BASE_URI + "type"));
    URIReference object = new URIReferenceImpl(new URI(RDFS.BASE_URI + "Class"));
    for (int i = 0; i < NUM_STATEMENTS; i++) {

      subject = new URIReferenceImpl(URI.create(Mulgara.NAMESPACE + "subject" + i));
      statements.add(new TripleImpl(subject, predicate, object));
    }

    session.insert(modelURI, statements);
  }

  /**
   * Creates the Graph.
   *
   * @param modelURI URI
   * @param session Session
   * @throws Exception
   */
  private void createModel(URI modelURI, Session session) throws Exception {
//    session.createModel(modelURI, new URI(Mulgara.NAMESPACE + "Graph"));
    session.createModel(modelURI, memoryModelURI);
  }

  /**
   * Drops the Graph.
   *
   * @param modelURI URI
   * @param session Session
   * @throws Exception
   */
  private void dropModel(URI modelURI, Session session) throws Exception {
    session.removeModel(modelURI);
  }

}
