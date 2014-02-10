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

package org.mulgara.client.jrdf.test;

// Java 2 standard packages
import java.io.*;
import java.security.*;
import java.net.*;

// third party packages
import junit.framework.*;

//Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;
import org.jrdf.util.*;

// Local packages
import org.mulgara.client.jrdf.*;
import org.mulgara.client.jrdf.writer.MemoryXMLWriter;
import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.query.Answer;

// local classes

/**
 * Unit test for client-side JRDF Graph.
 *
 * @created 2001-08-27
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/13 11:55:50 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ClientGraphUnitTest extends TestCase {

  /**
   * the category to send logging info to
   */
  private static Logger log = Logger.getLogger(ClientGraphUnitTest.class);

  /**
   * Server to query against.
   */
  protected final static String SERVER_NAME = "server1";

  /**
   * Description of the Field
   */
  protected URI serverURI = null;

  /**
   * Description of the Field
   */
  protected String hostname = null;

  /**
   * The URI of the test graph to create.
   */
  protected URI graphURI = null;


  /**
   * the ITQL command interpreter. Used to execute iTQL.
   */
  private static ItqlInterpreterBean interpreterBean = null;

  /**
   * the ITQL command interpreter. Used to create graph answers.
   */
  private static ItqlInterpreterBean answerBean = null;


  /** Small dataset used to test model */
  private static final String testStatements =
      "<http://mulgara.org/mulgara#test1> <http://mulgara.org/mulgara#test4> <http://mulgara.org/mulgara#test2> " +
      "<http://mulgara.org/mulgara#test1> <http://mulgara.org/mulgara#test5> <http://mulgara.org/mulgara#test3> " +
      "<http://mulgara.org/mulgara#test1> <http://mulgara.org/mulgara#test6> 'Test1' " +
      "<http://mulgara.org/mulgara#test2> <http://mulgara.org/mulgara#test4> <http://mulgara.org/mulgara#test1> " +
      "<http://mulgara.org/mulgara#test2> <http://mulgara.org/mulgara#test5> <http://mulgara.org/mulgara#test3> " +
      "<http://mulgara.org/mulgara#test2> <http://mulgara.org/mulgara#test6> 'Test2' " +
      "<http://mulgara.org/mulgara#test3> <http://mulgara.org/mulgara#test4> <http://mulgara.org/mulgara#test1> " +
      "<http://mulgara.org/mulgara#test3> <http://mulgara.org/mulgara#test5> <http://mulgara.org/mulgara#test2> " +
      "<http://mulgara.org/mulgara#test3> <http://mulgara.org/mulgara#test6> 'Test3' ";

  /**
   * Directory for test files
   */
  private static String TEST_DIR = System.getProperty("cvs.root") + "/test/";

  /**
   * Test file used for memory writer
   */
  private static String CLIENT_TEST_FILE1 = TEST_DIR + "ClientTest1.rdf";

  /**
   * Test file used for memory writer
   */
  private static String CLIENT_TEST_FILE2 = TEST_DIR + "ClientTest2.rdf";

  /**
   * Constructs a new ItqlInterpreter unit test.
   *
   * @param name the name of the test
   */
  public ClientGraphUnitTest(String name) throws Exception {

    // delegate to super class constructor
    super(name);
  }

  /**
   * Returns a test suite containing the tests to be run.
   *
   * @return the test suite
   */
  public static Test suite() throws Exception {

    TestSuite suite = new TestSuite();
//    TestSuite suite = (TestSuite) JRDFGraphUnitTest.suite();
    suite.addTest(new ClientGraphUnitTest("testCreate"));
    suite.addTest(new ClientGraphUnitTest("testFind"));
    suite.addTest(new ClientGraphUnitTest("testAnswerOutputRDF"));
    return suite;
  }

  /**
   * Default text runner.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {

    junit.textui.TestRunner.run(suite());
  }

  // suite()
  //
  // Test cases
  //

  /**
   * Tests the creation of a Client Graph by an AbstractGraphFactory.
   *
   * @throws Exception
   */
  @SuppressWarnings("deprecation") // avoiding the use of a connection
  public void testCreate() throws Exception {

    // log that we're executing the test
    log.debug("Starting Create test");

    //-- ANSWER IMPLEMENTATION --
    // log that we're executing the test
    log.debug("Testing Answer implementation");

    //get all statments from the graph
    Answer answer = getEntireGraph(graphURI);

    //create a Client Graph from it.
    ClientGraph client = AbstractGraphFactory.createGraph(answer, answerBean.getSession(serverURI));

    //ensure the client is not null
    assertNotNull("AbstractGraphFactory.createGraph(Answer) returned a null ClientGraph.", client);

    //close the graph
    client.close();

    // log that we're executing the test
    log.debug("Testing Answer implementation successful");
    //-- END ANSWER IMPLEMENTATION --

    // log that we've completed the test
    log.debug("Completed Create test");
  }

  /**
   * Tests the creation of a Client Graph by an AbstractGraphFactory.
   *
   * @throws Exception
   */
  public void testFind() throws Exception {

    try {

      // log that we're executing the test
      log.debug("Starting Find test");

      //Answer
      log.debug("Testing Answer implementation");
      testAnswerFind();
      log.debug("Testing Answer implementation successful");
    } catch (Exception exception) {
      exception.printStackTrace();
      throw exception;
    }
  }

  /**
   * Tests the creation of a Client Graph by an AbstractGraphFactory.
   *
   * @throws Exception
   */
  @SuppressWarnings("deprecation") // avoiding the use of a connection
  public void testAnswerFind() throws Exception {

    //create a Client Graph from it.
    Answer answer = getEntireGraph(graphURI);
    ClientGraph client = AbstractGraphFactory.createGraph(answer, answerBean.getSession(serverURI));
    GraphElementFactory factory = client.getElementFactory();

    //-- FIND ALL --

    //create a triple and use it to filter the objects
    Triple triple = factory.createTriple(null, null, null);
    ClosableIterator<Triple> iter = client.find(triple);

    //compare with answer
    compareAnswerToIterator(answer, iter, factory);

    //close iterator
    iter.close();

    //-- FIND SUBJECT (S) --
    testAnswerFindSubjects(client, factory);

    //-- FIND PREDICATE (S) --
    testAnswerFindPredicates(client, factory);

    //-- FIND OBJECT (S) --
    testAnswerFindObjects(client, factory);

    //close graph
    client.close();
  }

  /**
   * Tests Searching a graph by Subject.
   *
   * @throws Exception
   */
  public void testAnswerFindSubjects(ClientGraph client,
                                     GraphElementFactory factory) throws
      Exception {

    //subject 1
    String subject = "http://mulgara.org/mulgara#test1";
    Triple triple = factory.createTriple(factory.createResource(URI.create(
        subject)),
                                         null, null);
    ClosableIterator<Triple> iter = client.find(triple);
    compareAnswerToIterator(getSubject("<" + subject + ">"), iter,
                                 factory);
    iter.close();

    //subject 2
    subject = "http://mulgara.org/mulgara#test2";
    triple = factory.createTriple(factory.createResource(URI.create(subject)),
                                  null, null);
    iter = client.find(triple);
    compareAnswerToIterator(getSubject("<" + subject + ">"), iter,
                                 factory);
    iter.close();

    //subject 3
    subject = "http://mulgara.org/mulgara#test3";
    triple = factory.createTriple(factory.createResource(URI.create(subject)),
                                  null, null);
    iter = client.find(triple);
    compareAnswerToIterator(getSubject("<" + subject + ">"), iter,
                                 factory);
    iter.close();
  }

  /**
   * Tests Searching a graph by Predicate.
   *
   * @throws Exception
   */
  public void testAnswerFindPredicates(ClientGraph client,
                                       GraphElementFactory factory) throws
      Exception {

    //predicate 4
    String predicate = "http://mulgara.org/mulgara#test4";
    Triple triple = factory.createTriple(null,
                                         factory.createResource(URI.create(
        predicate)),
                                         null);
    ClosableIterator<Triple> iter = client.find(triple);
    compareAnswerToIterator(getPredicate("<" + predicate + ">"), iter,
                                 factory);
    iter.close();

    //predicate 5
    predicate = "http://mulgara.org/mulgara#test5";
    triple = factory.createTriple(null,
                                  factory.createResource(URI.create(predicate)),
                                  null);
    iter = client.find(triple);
    compareAnswerToIterator(getPredicate("<" + predicate + ">"), iter,
                                 factory);
    iter.close();

    //predicate 6
    predicate = "http://mulgara.org/mulgara#test6";
    triple = factory.createTriple(null,
                                  factory.createResource(URI.create(predicate)),
                                  null);
    iter = client.find(triple);
    compareAnswerToIterator(getPredicate("<" + predicate + ">"), iter,
                                 factory);
    iter.close();
  }

  /**
   * Tests Searching a graph by Predicate.
   *
   * @throws Exception
   */
  public void testAnswerFindObjects(ClientGraph client,
                                    GraphElementFactory factory) throws
      Exception {

    //object 1
    String object = "http://mulgara.org/mulgara#test1";
    Triple triple = factory.createTriple(null, null,
                                         factory.createResource(URI.
        create(object)));
    ClosableIterator<Triple> iter = client.find(triple);
    compareAnswerToIterator(getObject("<" + object + ">"), iter,
                                 factory);
    iter.close();

    //object 2
    object = "http://mulgara.org/mulgara#test2";
    triple = factory.createTriple(null, null,
                                  factory.createResource(URI.create(object)));
    iter = client.find(triple);
    compareAnswerToIterator(getObject("<" + object + ">"), iter,
                                 factory);
    iter.close();

    //object 3
    object = "http://mulgara.org/mulgara#test3";
    triple = factory.createTriple(null, null,
                                  factory.createResource(URI.create(object)));
    iter = client.find(triple);
    compareAnswerToIterator(getObject("<" + object + ">"), iter,
                                 factory);
    iter.close();

    //object test 1
    object = "Test1";
    triple = factory.createTriple(null, null, factory.createLiteral(object));
    iter = client.find(triple);
    compareAnswerToIterator(getObject("'" + object + "'"), iter,
                                 factory);
    iter.close();

    //object test 2
    object = "Test2";
    triple = factory.createTriple(null, null, factory.createLiteral(object));
    iter = client.find(triple);
    compareAnswerToIterator(getObject("'" + object + "'"), iter,
                                 factory);
    iter.close();

    //object test 3
    object = "Test3";
    triple = factory.createTriple(null, null, factory.createLiteral(object));
    iter = client.find(triple);
    compareAnswerToIterator(getObject("'" + object + "'"), iter,
                                 factory);
    iter.close();
  }

  /**
   * Compares the triples from a Closable Iterator to the triples from an Answer
   *
   * @param answer Answer
   * @param iterator ClosableITerator
   * @throws Exception
   */
  private void compareAnswerToIterator(Answer answer, ClosableIterator<Triple> iterator,
                                       GraphElementFactory factory) throws
      Exception {

    Triple iterTriple = null;

    //compare rows with answer
    Triple answerTriple = null;
    SubjectNode subject = null;
    PredicateNode predicate = null;
    ObjectNode object = null;

    //iterate to the end of the closable iterator (compare to Answer)
    while (iterator.hasNext()) {

      //check type and ensure not null
      iterTriple = (Triple) iterator.next();

      if (iterTriple == null) {

        fail("Result contains null Triple");
      }

      //get next triple from answer
      answer.next();
      subject = (SubjectNode) answer.getObject(0);
      predicate = (PredicateNode) answer.getObject(1);
      object = (ObjectNode) answer.getObject(2);
      answerTriple = factory.createTriple(subject, predicate, object);

      //compare to iterator
      assertTrue("Iterator Triple not equal to Answer Triple. " +
                 "\n\tAnswer: " + answerTriple + ", Iterator: " + iterTriple +
                 ".", (answerTriple.equals(iterTriple)));
    }
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @return Answer
   */
  private Answer getEntireGraph(URI modelURI) throws Exception {

    //select ALL query
    String query = "select $s $p $o " +
        "from <" + modelURI + "> " +
        "where $s $p $o " +
        //needed for comparision
        "order by $s $p $o ;";

    return answerBean.executeQuery(query);
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @return Answer
   */
  private Answer getColumn(String uri, String variable) throws Exception {

    //select ALL query
    String query = "select $s $p $o " +
        "from <" + graphURI + "> " +
        "where " + variable + "<http://mulgara.org/mulgara#is> " + uri + " " +
        "and $s $p $o " +
        //needed for comparision
        "order by $s $p $o ;";

    return interpreterBean.executeQuery(query);
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @return Answer
   */
  private Answer getSubject(String subject) throws Exception {

    return getColumn(subject, "$s");
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @return Answer
   */
  private Answer getPredicate(String predicate) throws Exception {

    return getColumn(predicate, "$p");
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @return Answer
   */
  private Answer getObject(String object) throws Exception {

    return getColumn(object, "$o");
  }

  /**
   * Tests the RDF output of a MemoryXMLWriter. The output is loaded back into
   * the graph.
   *
   * @throws Exception
   */
  @SuppressWarnings("deprecation") // avoiding the use of a Connection
  public void testAnswerOutputRDF() throws Exception {

    //create a Client Graph containing all statments
    ClientGraph client1 = AbstractGraphFactory.createGraph(getEntireGraph(graphURI), answerBean.getSession(serverURI));
    File rdfOutput1 = new File(CLIENT_TEST_FILE1);
    outputRDF(rdfOutput1, client1);

    //reload file into new model
    URI graph2 = new URI("" + graphURI + "2");
    createModel(graph2);
    interpreterBean.load(rdfOutput1, graph2);

    //output new model
    ClientGraph client2 = AbstractGraphFactory.createGraph(getEntireGraph(graph2), answerBean.getSession(serverURI));
    File rdfOutput2 = new File(CLIENT_TEST_FILE2);
    outputRDF(rdfOutput2, client2);

    //compare files
    assertTrue("RDF output file is not equal to file that was loaded.",
               compareFiles(rdfOutput1, rdfOutput2));

    //tidy up
    client1.close();
    client2.close();
    dropModel(graph2);
  }

  /**
   * Compares the contents of one file to another using an MD5 sum.
   *
   * @param file1 File
   * @param file2 File
   * @throws Exception
   * @return boolean
   */
  private boolean compareFiles(File file1, File file2) throws Exception {

    //value to be returned
    boolean compare = false;

    byte[] digest1 = digest(file1);
    byte[] digest2 = digest(file2);

    //compare the bytes
    if (digest1.length == digest2.length) {

      for (int i = 0; i < digest1.length; i++) {

        if (digest1[i] != digest2[i]) {

          return false;
        }
      }

      //got here, arrays are equal
      compare = true;
    }

    return compare;
  }

  /**
   * Returns an MD5 sum for the file
   *
   * @param file File
   * @throws Exception
   * @return byte[]
   */
  private byte[] digest(File file) throws Exception {

    //buffer
    final int bufferSize = 1048576;
    byte[] buffer = new byte[bufferSize];

    DigestInputStream digestIn = new DigestInputStream(new FileInputStream(file),
        MessageDigest.getInstance("MD5"));

    //read file
    int read = 0;
    do {

      read = digestIn.read(buffer);
    } while (read != -1);

    //digest
    return digestIn.getMessageDigest().digest();
  }

  /**
   * Outputs the contents of the graph to a file.
   *
   * @param file String
   * @param graph Graph
   *
   * @throws Exception
   */
  private void outputRDF(File file, Graph graph) throws Exception {

    FileOutputStream fileStream = new FileOutputStream(file, false);
    OutputStreamWriter outStream = new OutputStreamWriter(fileStream);

    //create writer and write
    new MemoryXMLWriter().write(graph, outStream);
    outStream.close();
    fileStream.close();
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @return Answer
   */
  private Answer createModel(URI modelURI) throws Exception {

    //select ALL query
    String query = "create <" + modelURI + "> ;";

    return interpreterBean.executeQuery(query);
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @return Answer
   */
  private Answer dropModel(URI modelURI) throws Exception {

    //select ALL query
    String query = "drop <" + modelURI + "> ;";

    return interpreterBean.executeQuery(query);
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @return Answer
   */
  private Answer populateModel() throws Exception {

    //select ALL query
    String query = "insert " + testStatements + "into <" + graphURI + "> ;";

    return interpreterBean.executeQuery(query);
  }

  //set up and tear down

  /**
   * Initialise members.
   *
   * @throws Exception if something goes wrong
   */
  public void setUp() throws Exception {

    // Store persistence files in the temporary directory
    try {

      hostname = InetAddress.getLocalHost().getCanonicalHostName();
      serverURI = new URI("rmi", hostname, "/" + SERVER_NAME, null);
      graphURI = new URI("rmi", hostname, "/" + SERVER_NAME, "clientJenaTest");

      //create an iTQLInterpreterBean for executing queries with
      interpreterBean = new ItqlInterpreterBean();
      answerBean = new ItqlInterpreterBean();

      //initialize model
      createModel(graphURI);
      populateModel();
    } catch (Exception exception) {
      //try to tear down first
      try {
        tearDown();
      } catch (Throwable t) {
        // going to throw the original
      }
      throw exception;
    }
  }

  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void tearDown() throws Exception {

    dropModel(graphURI);

    interpreterBean.close();
    answerBean.close();
  }
}
