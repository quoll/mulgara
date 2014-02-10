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

package org.mulgara.content.mbox.parser;

// Java 2 standard packages
import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// Third party packages
import junit.framework.*;
import org.apache.log4j.*;
import org.apache.log4j.xml.*;
import org.jrdf.graph.*;
import org.jrdf.util.ClosableIterator;
import org.jrdf.vocabulary.RDF;

// Local packages
import org.mulgara.content.Content;
import org.mulgara.content.NotModifiedException;
import org.mulgara.content.mbox.parser.model.*;
import org.mulgara.content.mbox.parser.model.exception.*;

/**
 * Unit testing for the mime message to RDF parser.
 *
 * @created 2004-08-31
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:40 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003
 *   <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 *
 * @licence <A href="{@docRoot}/../../LICENCE">Licence description</A>
 */
public class MBoxParserUnitTest extends TestCase {

  /** Logger. */
  private static Logger log = Logger.getLogger(MBoxParserUnitTest.class);

  /**
   * Constructor as required by JUnit's TestCase.
   */
  public MBoxParserUnitTest(String name) {

    super(name);

    // load the logging configuration
    BasicConfigurator.configure();
    try {

      DOMConfigurator.configure(new URL(System.getProperty(
          "log4j.configuration")));
    } catch (MalformedURLException mue) {

      log.error(
          "Unable to configure logging service from XML configuration " +
          "file", mue);
    }
  }

  /**
   * Setup data which will be used by all the test cases in this class.
   * Overrides TestCase.setUp().
   */
  protected void setUp() {

  }

  /**
   * Clean up data set up by setUp().
   * Overrides TestCase.tearDown().
   */
  protected void tearDown() {

  }

  /**
   * Creates a test suite with various different output and compares the output.
   */
  public static TestSuite suite() {

    TestSuite suite = new TestSuite();

    // The test cases we want run during testing
    suite.addTest(new MBoxParserUnitTest("testEmptyMBox"));
    suite.addTest(new MBoxParserUnitTest("testBadSubjectMBox"));
    suite.addTest(new MBoxParserUnitTest("testEmptyHeadersMBox"));
    suite.addTest(new MBoxParserUnitTest("testInvalidMBox"));
    suite.addTest(new MBoxParserUnitTest("testCouldBeMBox"));
    suite.addTest(new MBoxParserUnitTest("testNormalMBox"));
    suite.addTest(new MBoxParserUnitTest("testAttachments"));
    suite.addTest(new MBoxParserUnitTest("testInvalidAttachments"));

    return suite;
  }

  /**
   * Test an mbox file that has no content.
   */
  public void testEmptyMBox() {

    // Create a file to store our test data in
    File empty = new File("data/mbox/Empty");

    // Create a content object for our file
    Content content = createContentObject(empty);

    // Since we require an absolute file to process, we need to make our
    // relative path absolute
    empty = empty.getAbsoluteFile();

    //Container for our mbox
    MBox mbox = null;

    // Container for our mbox manager
    MBoxManager mboxManager = null;

    if (mboxManager == null) {

      // Container for the model factory
      ModelFactory factory = null;

      try {

        // Try to initialise the factory
        factory = ModelFactory.getInstance();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to initialise factory to create MBox " +
                  "parser.", factoryException);

        // Fail the test
        fail("Unable to initialise factory to create MBox parser.");
      }

      try {

        // Initialise the mbox object using the factory
        mboxManager = factory.createMBoxManager();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to create a new mbox manager.", factoryException);

        // Fail the test
        fail("Unable to create a new mbox manager.");
      }
    }

    try {

      // Get the mbox for our file
      mbox = mboxManager.getMBox(content);
    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to create/retrieve MBox for URL " +
                empty.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to create/retrieve MBox for URL " + empty.getAbsolutePath());
    }

    // Set up a monitor to indicate if we fail in the correct area (Assumed
    // false)
    boolean invalid = false;

    try {

      // Perform the parsing and prepare for reading triples
      mbox.start();
    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to parse mbox file: " +
                empty.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to parse mbox file: " +
           empty.getAbsolutePath());
    } catch (InvalidMBoxException invalidMBoxException) {

      // Set the invalid indicator to true
      invalid = true;
    } catch (VocabularyException vocabularyException) {

      // Log the exception
      log.error("Unable to set up vocabulary for mbox parsing.",
                vocabularyException);

      // Fail the test
      fail("Unable to set up vocabulary for mbox parsing.");
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Mbox claimed to be cached.", notModifiedException);

      // Fail the test
      fail("Mbox claimed to be cached.");
    }

    // Check that we had an invalid mbox as expected
    assertTrue("The mbox was not detected as invalid as expected.", invalid);
  }

  /**
   * Test an mbox that has an empty subject header.
   */
  public void testBadSubjectMBox() {

    // Create a file to store our test data in
    File badSubject = new File("data/mbox/BadSubject");

    // Create a content object for our file
    Content content = createContentObject(badSubject);

    // Since we require an absolute file to process, we need to make our
    // relative path absolute
    badSubject = badSubject.getAbsoluteFile();

    //Container for our mbox
    MBox mbox = null;

    // Container for our mbox manager
    MBoxManager mboxManager = null;

    if (mboxManager == null) {

      // Container for the model factory
      ModelFactory factory = null;

      try {

        // Try to initialise the factory
        factory = ModelFactory.getInstance();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to initialise factory to create MBox " +
                  "parser.", factoryException);

        // Fail the test
        fail("Unable to initialise factory to create MBox parser.");
      }

      try {

        // Initialise the mbox object using the factory
        mboxManager = factory.createMBoxManager();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to create a new mbox manager.", factoryException);

        // Fail the test
        fail("Unable to create a new mbox manager.");
      }
    }

    try {

      // Get the mbox for our file
      mbox = mboxManager.getMBox(content);
    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to create/retrieve MBox for URL " +
                badSubject.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to create/retrieve MBox for URL " +
           badSubject.getAbsolutePath());
    }

    try {

      // Perform the parsing and prepare for reading triples
      mbox.start();
    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to parse mbox file: " +
                badSubject.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to parse mbox file: " +
           badSubject.getAbsolutePath());
    } catch (InvalidMBoxException invalidMBoxException) {

      // Log the exception
      log.error("The mbox '" + badSubject.getAbsolutePath() + "' was invalid.",
                invalidMBoxException);

      // Fail the test
      fail("The mbox '" + badSubject.getAbsolutePath() + "' was invalid.");
    } catch (VocabularyException vocabularyException) {

      // Log the exception
      log.error("Unable to set up vocabulary for mbox parsing.",
                vocabularyException);

      // Fail the test
      fail("Unable to set up vocabulary for mbox parsing.");
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Mbox claimed to be cached.", notModifiedException);

      // Fail the test
      fail("Mbox claimed to be cached.");
    }

    // Check that the graph is not null (ie populated)
    assertTrue("MBox graph was not populated as expected.",
               mbox.getGraph() != null);

    // Retrieve the graph from the mbox
    Graph graph = mbox.getGraph();

    // Container for our vocab
    Properties vocab = null;

    try {

      // Create the map of properties for the graph
      vocab = EmailVocab.createVocabulary(graph);
    } catch (VocabularyException vocabularyException) {

      // Log the error
      log.error("Failed to create a vocabulary for the mbox graph.",
                vocabularyException);

      // Fail the test
      fail("Failed to create a vocabulary for the mbox graph.");
    }

    // Container for our graph element factory
    GraphElementFactory elementFactory = graph.getElementFactory();

    try {

      // Create a literal to represent the empty subject
      ObjectNode subject = elementFactory.createLiteral("");

      // Check that we have the correct subject
      assertTrue("Subject was not blank as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.SUBJECT),
                            subject).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find subject in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find subject in mbox graph.");
    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create subject literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create subject literal.");
    }
  }

  /**
   * Test an mbox that has no headers.
   */
  public void testEmptyHeadersMBox() {

    // Create a file to store our test data in
    File emptyHeader = new File("data/mbox/EmptyHeader");

    // Create a content object for our file
    Content content = createContentObject(emptyHeader);

    // Since we require an absolute file to process, we need to make our
    // relative path absolute
    emptyHeader = emptyHeader.getAbsoluteFile();

    //Container for our mbox
    MBox mbox = null;

    // Container for our mbox manager
    MBoxManager mboxManager = null;

    if (mboxManager == null) {

      // Container for the model factory
      ModelFactory factory = null;

      try {

        // Try to initialise the factory
        factory = ModelFactory.getInstance();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to initialise factory to create MBox " +
                  "parser.", factoryException);

        // Fail the test
        fail("Unable to initialise factory to create MBox parser.");
      }

      try {

        // Initialise the mbox object using the factory
        mboxManager = factory.createMBoxManager();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to create a new mbox manager.", factoryException);

        // Fail the test
        fail("Unable to create a new mbox manager.");
      }
    }

    try {

      // Get the mbox for our file
      mbox = mboxManager.getMBox(content);

    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to create/retrieve MBox for URL " +
                emptyHeader.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to create/retrieve MBox for URL " +
           emptyHeader.getAbsolutePath());
    }

    // Flag to assert whether the mbox has failed as expected or not
    // (Assumed false)
    boolean hasFailed = false;

    try {

      // Perform the parsing and prepare for reading triples
      mbox.start();
    } catch (ModelException modelException) {

      // Log the exception
      log.error("The mbox '" + emptyHeader.getAbsolutePath() + "' was unable" +
                "to be set up for processing.",
                modelException);

      // Fail the test
      fail("The mbox '" + emptyHeader.getAbsolutePath() + "' was unable" +
           "to be set up for processing.");
    } catch (InvalidMBoxException invalidMBoxException) {

      // This is the expected failure so mark that we have failed as predicted
      hasFailed = true;
    } catch (VocabularyException vocabularyException) {

      // Log the exception
      log.error("Unable to set up vocabulary for mbox parsing.",
                vocabularyException);

      // Fail the test
      fail("Unable to set up vocabulary for mbox parsing.");
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Mbox claimed to be cached.", notModifiedException);

      // Fail the test
      fail("Mbox claimed to be cached.");
    }

    // Check that the graph is not null (ie populated)
    assertTrue("MBox was not invalid as expected.", hasFailed);
  }

  /**
   * Test an mbox that is not actually an mbox.  (Does not start with 'From ')
   */
  public void testInvalidMBox() {

    // Create a file to store our test data in
    File invalidMBox = new File("data/mbox/Invalid");

    // Create a content object for our file
    Content content = createContentObject(invalidMBox);

    // Since we require an absolute file to process, we need to make our
    // relative path absolute
    invalidMBox = invalidMBox.getAbsoluteFile();

    //Container for our mbox
    MBox mbox = null;

    // Container for our mbox manager
    MBoxManager mboxManager = null;

    if (mboxManager == null) {

      // Container for the model factory
      ModelFactory factory = null;

      try {

        // Try to initialise the factory
        factory = ModelFactory.getInstance();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to initialise factory to create MBox " +
                  "parser.", factoryException);

        // Fail the test
        fail("Unable to initialise factory to create MBox parser.");
      }

      try {

        // Initialise the mbox object using the factory
        mboxManager = factory.createMBoxManager();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to create a new mbox manager.", factoryException);

        // Fail the test
        fail("Unable to create a new mbox manager.");
      }
    }

    try {

      // Get the mbox for our file
      mbox = mboxManager.getMBox(content);

    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to create/retrieve MBox for URL " +
                invalidMBox.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to create/retrieve MBox for URL " +
           invalidMBox.getAbsolutePath());
    }

    // Set up a monitor to indicate if we fail in the correct area (Assumed
    // false)
    boolean invalid = false;

    try {

      // Perform the parsing and prepare for reading triples
      mbox.start();
    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to parse mbox file: " +
                invalidMBox.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to parse mbox file: " +
           invalidMBox.getAbsolutePath());
    } catch (InvalidMBoxException invalidMBoxException) {

      // Set the invalid indicator to true
      invalid = true;
    } catch (VocabularyException vocabularyException) {

      // Log the exception
      log.error("Unable to set up vocabulary for mbox parsing.",
                vocabularyException);

      // Fail the test
      fail("Unable to set up vocabulary for mbox parsing.");
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Mbox claimed to be cached.", notModifiedException);

      // Fail the test
      fail("Mbox claimed to be cached.");
    }

    // Check that we had an invalid mbox as expected
    assertTrue("The mbox was not detected as invalid as expected.", invalid);
  }

  /**
   * Test an mbox that might be an mbox.  (Starts with 'From ' but is not an
   * mbox)
   */
  public void testCouldBeMBox() {

    // Create a file to store our test data in
    File couldBeMBox = new File("data/mbox/CouldBe");

    // Create a content object for our file
    Content content = createContentObject(couldBeMBox);

    // Since we require an absolute file to process, we need to make our
    // relative path absolute
    couldBeMBox = couldBeMBox.getAbsoluteFile();

    //Container for our mbox
    MBox mbox = null;

    // Container for our mbox manager
    MBoxManager mboxManager = null;

    if (mboxManager == null) {

      // Container for the model factory
      ModelFactory factory = null;

      try {

        // Try to initialise the factory
        factory = ModelFactory.getInstance();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to initialise factory to create MBox " +
                  "parser.", factoryException);

        // Fail the test
        fail("Unable to initialise factory to create MBox parser.");
      }

      try {

        // Initialise the mbox object using the factory
        mboxManager = factory.createMBoxManager();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to create a new mbox manager.", factoryException);

        // Fail the test
        fail("Unable to create a new mbox manager.");
      }
    }

    try {

      // Get the mbox for our file
      mbox = mboxManager.getMBox(content);

    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to create/retrieve MBox for URL " +
                couldBeMBox.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to create/retrieve MBox for URL " +
           couldBeMBox.getAbsolutePath());
    }

    // Set up a monitor to indicate if we fail in the correct area (Assumed
    // false)
    boolean invalid = false;

    try {

      // Perform the parsing and prepare for reading triples
      mbox.start();
    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to parse mbox file: " +
                couldBeMBox.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to parse mbox file: " +
           couldBeMBox.getAbsolutePath());
    } catch (InvalidMBoxException invalidMBoxException) {

      // Set the invalid indicator to true
      invalid = true;
    } catch (VocabularyException vocabularyException) {

      // Log the exception
      log.error("Unable to set up vocabulary for mbox parsing.",
                vocabularyException);

      // Fail the test
      fail("Unable to set up vocabulary for mbox parsing.");
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Mbox claimed to be cached.", notModifiedException);

      // Fail the test
      fail("Mbox claimed to be cached.");
    }

    // Check that we had an invalid mbox as expected
    assertTrue("The mbox was not detected as invalid as expected.", invalid);
  }

  /**
   * Test a normal mbox.
   */
  public void testNormalMBox() {

    // Create a file to store our test data in
    File normal = new File("data/mbox/Normal");

    // Create a content object for our file
    Content content = createContentObject(normal);

    // Since we require an absolute file to process, we need to make our
    // relative path absolute
    normal = normal.getAbsoluteFile();

    //Container for our mbox
    MBox mbox = null;

    // Container for our mbox manager
    MBoxManager mboxManager = null;

    if (mboxManager == null) {

      // Container for the model factory
      ModelFactory factory = null;

      try {

        // Try to initialise the factory
        factory = ModelFactory.getInstance();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to initialise factory to create MBox " +
                  "parser.", factoryException);

        // Fail the test
        fail("Unable to initialise factory to create MBox parser.");
      }

      try {

        // Initialise the mbox object using the factory
        mboxManager = factory.createMBoxManager();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to create a new mbox manager.", factoryException);

        // Fail the test
        fail("Unable to create a new mbox manager.");
      }
    }

    try {

      // Get the mbox for our file
      mbox = mboxManager.getMBox(content);

    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to create/retrieve MBox for URL " +
                normal.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to create/retrieve MBox for URL " +
           normal.getAbsolutePath());
    }

    try {

      // Perform the parsing and prepare for reading triples
      mbox.start();
    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to parse mbox file: " +
                normal.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to parse mbox file: " +
           normal.getAbsolutePath());
    } catch (InvalidMBoxException invalidMBoxException) {

      // Log the exception
      log.error("The mbox '" + normal.getAbsolutePath() + "' was invalid.",
                invalidMBoxException);

      // Fail the test
      fail("The mbox '" + normal.getAbsolutePath() + "' was invalid.");
    } catch (VocabularyException vocabularyException) {

      // Log the exception
      log.error("Unable to set up vocabulary for mbox parsing.",
                vocabularyException);

      // Fail the test
      fail("Unable to set up vocabulary for mbox parsing.");
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Mbox claimed to be cached.", notModifiedException);

      // Fail the test
      fail("Mbox claimed to be cached.");
    }

    // Check that the graph is not null (ie populated)
    assertTrue("MBox graph was not populated as expected.",
               mbox.getGraph() != null);

    // Retrieve the graph from the mbox
    Graph graph = mbox.getGraph();

    // Container for our vocab
    Properties vocab = null;

    try {

      // Create the map of properties for the graph
      vocab = EmailVocab.createVocabulary(graph);
    } catch (VocabularyException vocabularyException) {

      // Log the error
      log.error("Failed to create a vocabulary for the mbox graph.",
                vocabularyException);

      // Fail the test
      fail("Failed to create a vocabulary for the mbox graph.");
    }

    // Container for our graph element factory
    GraphElementFactory elementFactory = graph.getElementFactory();

    try {

      // Check that we have no bcc fields
      assertTrue("Unexpected BCC found in mbox.",
                 !graph.find(null,
                             (PredicateNode) vocab.get(EmailVocab.BCC),
                             null).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find bcc triples in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find bcc triples in mbox graph.");
    }

    try {

      // Check that we have no cc fields
      assertTrue("Unexpected CC found in mbox.",
                 !graph.find(null,
                             (PredicateNode) vocab.get(EmailVocab.CC),
                             null).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find cc triples in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find cc triples in mbox graph.");
    }

    // Container for object nodes
    ObjectNode objectNode = null;

    try {

      // Create a literal to represent the to value
      objectNode = elementFactory.createLiteral("mludlow@pisoftware.com");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create to literal.", graphElementFactoryException);

      // Fail the test
      fail("Failed to create to literal.");
    }

    try {

      // Check that we have the correct to value
      assertTrue("To field was not 'mludlow@pisoftware.com' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.TO),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find to in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to in mbox graph.");
    }

    try {

      // Create a literal to represent the from value
      objectNode = elementFactory.createLiteral("alford_py@mvv.de");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create from literal.", graphElementFactoryException);

      // Fail the test
      fail("Failed to create from literal.");
    }

    try {

      // Check that we have the correct from value
      assertTrue("From field was not 'alford_py@mvv.de' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.FROM),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find from in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to from mbox graph.");
    }

    try {

      // Create a literal to represent the message-ID value
      objectNode = elementFactory.createLiteral(
          "<c23d01c48999$ed68184c$e2f9cbb3@csaa.fr>");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create message-ID literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create message-ID literal.");
    }

    try {

      // Check that we have the correct message-ID value
      assertTrue(
          "Message-ID field was not " +
          "'<c23d01c48999$ed68184c$e2f9cbb3@csaa.fr>' as expected.",
          graph.find(null,
                     (PredicateNode) vocab.get(EmailVocab.MESSAGE_ID),
                     objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find message-ID in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to message-ID mbox graph.");
    }

    try {

      // Create a literal to represent the subject value
      objectNode = elementFactory.createLiteral(
          "[SPAM] Lose your weight. New weightloss loses up to 19%.");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create subject literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create subject literal.");
    }

    try {

      // Check that we have the correct subject value
      assertTrue("Subject field was not '[SPAM] Lose your weight. New" +
                 " weightloss loses up to 19%.' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.SUBJECT),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find subject in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to subject mbox graph.");
    }

    // Container for our type predicate
    PredicateNode typeNode = null;

    try {

      // Create a predicate to represent the type URL
      typeNode = elementFactory.createResource(RDF.TYPE);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create type predicate.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create type predicate.");
    }

    try {

      // Check that we have the correct type value
      assertTrue("Type was not message as expected.",
                 graph.find(null, typeNode,
                            (ObjectNode) vocab.get(EmailVocab.MESSAGE)).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find message type in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to message type in mbox graph.");
    }

    try {

      // Check that we have no attachment types in our graph
      assertTrue("Unexpectedly found attachments in graph.",
                 !graph.find(null, typeNode,
                             (ObjectNode) vocab.get(EmailVocab.ATTACHMENT)).
                 hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find attachment type in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to attachment type in mbox graph.");
    }
  }

  /**
   * Test an mbox with attachments.
   */
  public void testAttachments() {

    // Create a file to store our test data in
    File attachments = new File("data/mbox/Attachments");

    // Create a content object for our file
    Content content = createContentObject(attachments);

    // Since we require an absolute file to process, we need to make our
    // relative path absolute
    attachments = attachments.getAbsoluteFile();

    //Container for our mbox
    MBox mbox = null;

    // Container for our mbox manager
    MBoxManager mboxManager = null;

    if (mboxManager == null) {

      // Container for the model factory
      ModelFactory factory = null;

      try {

        // Try to initialise the factory
        factory = ModelFactory.getInstance();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to initialise factory to create MBox " +
                  "parser.", factoryException);

        // Fail the test
        fail("Unable to initialise factory to create MBox parser.");
      }

      try {

        // Initialise the mbox object using the factory
        mboxManager = factory.createMBoxManager();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to create a new mbox manager.", factoryException);

        // Fail the test
        fail("Unable to create a new mbox manager.");
      }
    }

    try {

      // Get the mbox for our file
      mbox = mboxManager.getMBox(content);

    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to create/retrieve MBox for URL " +
                attachments.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to create/retrieve MBox for URL " +
           attachments.getAbsolutePath());
    }

    try {

      // Perform the parsing and prepare for reading triples
      mbox.start();
    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to parse mbox file: " +
                attachments.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to parse mbox file: " +
           attachments.getAbsolutePath());
    } catch (InvalidMBoxException invalidMBoxException) {

      // Log the exception
      log.error("The mbox '" + attachments.getAbsolutePath() + "' was invalid.",
                invalidMBoxException);

      // Fail the test
      fail("The mbox '" + attachments.getAbsolutePath() + "' was invalid.");
    } catch (VocabularyException vocabularyException) {

      // Log the exception
      log.error("Unable to set up vocabulary for mbox parsing.",
                vocabularyException);

      // Fail the test
      fail("Unable to set up vocabulary for mbox parsing.");
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Mbox claimed to be cached.", notModifiedException);

      // Fail the test
      fail("Mbox claimed to be cached.");
    }

    // Check that the graph is not null (ie populated)
    assertTrue("MBox graph was not populated as expected.",
               mbox.getGraph() != null);

    // Retrieve the graph from the mbox
    Graph graph = mbox.getGraph();

    // Container for our vocab
    Properties vocab = null;

    try {

      // Create the map of properties for the graph
      vocab = EmailVocab.createVocabulary(graph);
    } catch (VocabularyException vocabularyException) {

      // Log the error
      log.error("Failed to create a vocabulary for the mbox graph.",
                vocabularyException);

      // Fail the test
      fail("Failed to create a vocabulary for the mbox graph.");
    }

    // Container for our graph element factory
    GraphElementFactory elementFactory = graph.getElementFactory();

    try {

      // Check that we have no bcc fields
      assertTrue("Unexpected BCC found in mbox.",
                 !graph.find(null,
                             (PredicateNode) vocab.get(EmailVocab.BCC),
                             null).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find bcc triples in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find bcc triples in mbox graph.");
    }

    try {

      // Check that we have no cc fields
      assertTrue("Unexpected CC found in mbox.",
                 !graph.find(null,
                             (PredicateNode) vocab.get(EmailVocab.CC),
                             null).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find cc triples in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find cc triples in mbox graph.");
    }

    // Container for object nodes
    ObjectNode objectNode = null;

    try {

      // Create a literal to represent the to value
      objectNode = elementFactory.createLiteral("mark.ludlow@pisoftware.com");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create to literal.", graphElementFactoryException);

      // Fail the test
      fail("Failed to create to literal.");
    }

    try {

      // Check that we have the correct to value
      assertTrue("To field was not 'mark.ludlow@pisoftware.com' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.TO),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find to in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to in mbox graph.");
    }

    try {

      // Create a literal to represent the from value
      objectNode = elementFactory.createLiteral("eamon@t-online.de");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create from literal.", graphElementFactoryException);

      // Fail the test
      fail("Failed to create from literal.");
    }

    try {

      // Check that we have the correct from value
      assertTrue("From field was not 'eamon@t-online.de' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.FROM),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find from in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to from mbox graph.");
    }

    try {

      // Create a literal to represent the message-ID value
      objectNode = elementFactory.createLiteral(
          "<1beb01c489c3$f3d67a74$2aad99eb@t-online.de>");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create message-ID literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create message-ID literal.");
    }

    try {

      // Check that we have the correct message-ID value
      assertTrue(
          "Message-ID field was not " +
          "'<1beb01c489c3$f3d67a74$2aad99eb@t-online.de>' as expected.",
          graph.find(null,
                     (PredicateNode) vocab.get(EmailVocab.MESSAGE_ID),
                     objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find message-ID in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to message-ID mbox graph.");
    }

    try {

      // Create a literal to represent the subject value
      objectNode = elementFactory.createLiteral(
          "[SPAM] On-line software store. Great prices. - 9972008");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create subject literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create subject literal.");
    }

    try {

      // Check that we have the correct subject value
      assertTrue("Subject field was not '[SPAM] On-line software store. " +
                 "Great prices. - 9972008' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.SUBJECT),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find subject in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to subject mbox graph.");
    }

    // Container for our type predicate
    PredicateNode typeNode = null;

    try {

      // Create a predicate to represent the type URL
      typeNode = elementFactory.createResource(RDF.TYPE);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create type predicate.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create type predicate.");
    }

    try {

      // Check that we have the correct type value
      assertTrue("Type was not message as expected.",
                 graph.find(null, typeNode,
                            (ObjectNode) vocab.get(EmailVocab.MESSAGE)).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find message type in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to message type in mbox graph.");
    }

    try {

      // Check that we have no attachment types in our graph
      assertTrue("Unable to find attachments expected in graph.",
                 graph.find(null, typeNode,
                            (ObjectNode) vocab.get(EmailVocab.ATTACHMENT)).
                 hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find attachment type in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to attachment type in mbox graph.");
    }

    try {

      // Create a literal to represent the attachment filename
      objectNode = elementFactory.createLiteral("ynfwdtka.jpg");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create attachment filename literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create attachment filename literal.");
    }

    try {

      // Check that we have the correct attachment filename value
      assertTrue("Could not find attachment 'ynfwdtka.jpg' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.FILE_NAME),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find attachment filename in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find to attachment filename mbox graph.");
    }

    try {

      // Create a literal to represent the attachment filename
      objectNode = elementFactory.createLiteral("bdqcwfvi.jpg");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create attachment filename literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create attachment filename literal.");
    }

    try {

      // Check that we have the correct attachment filename value
      assertTrue("Could not find attachment 'bdqcwfvi.jpg' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.FILE_NAME),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find attachment filename in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find to attachment filename mbox graph.");
    }

    try {

      // Create a literal to represent the attachment filename
      objectNode = elementFactory.createLiteral("wqlelzxf.jpg");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create attachment filename literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create attachment filename literal.");
    }

    try {

      // Check that we have the correct attachment filename value
      assertTrue("Could not find attachment 'wqlelzxf.jpg' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.FILE_NAME),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find attachment filename in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find to attachment filename mbox graph.");
    }

    try {

      // Create a literal to represent the attachment filename
      objectNode = elementFactory.createLiteral("cainanbp.jpg");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create attachment filename literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create attachment filename literal.");
    }

    try {

      // Check that we have the correct attachment filename value
      assertTrue("Could not find attachment 'cainanbp.jpg' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.FILE_NAME),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find attachment filename in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find to attachment filename mbox graph.");
    }
  }

  /**
   * Test an mbox with invalid attachments.
   */
  public void testInvalidAttachments() {

    // Create a file to store our test data in
    File invalidAttachments = new File("data/mbox/InvalidAttachments");

    // Create a content object for our file
    Content content = createContentObject(invalidAttachments);

    // Since we require an absolute file to process, we need to make our
    // relative path absolute
    invalidAttachments = invalidAttachments.getAbsoluteFile();

    //Container for our mbox
    MBox mbox = null;

    // Container for our mbox manager
    MBoxManager mboxManager = null;

    if (mboxManager == null) {

      // Container for the model factory
      ModelFactory factory = null;

      try {

        // Try to initialise the factory
        factory = ModelFactory.getInstance();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to initialise factory to create MBox " +
                  "parser.", factoryException);

        // Fail the test
        fail("Unable to initialise factory to create MBox parser.");
      }

      try {

        // Initialise the mbox object using the factory
        mboxManager = factory.createMBoxManager();
      } catch (FactoryException factoryException) {

        // Log the exception
        log.error("Unable to create a new mbox manager.", factoryException);

        // Fail the test
        fail("Unable to create a new mbox manager.");
      }
    }

    try {

      // Get the mbox for our file
      mbox = mboxManager.getMBox(content);

    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to create/retrieve MBox for URL " +
                invalidAttachments.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to create/retrieve MBox for URL " +
           invalidAttachments.getAbsolutePath());
    }

    try {

      // Perform the parsing and prepare for reading triples
      mbox.start();
    } catch (ModelException modelException) {

      // Log the exception
      log.error("Failed to parse mbox file: " +
                invalidAttachments.getAbsolutePath(), modelException);

      // Fail the test
      fail("Failed to parse mbox file: " +
           invalidAttachments.getAbsolutePath());
    } catch (InvalidMBoxException invalidMBoxException) {

      // Log the exception
      log.error("The mbox '" + invalidAttachments.getAbsolutePath() +
                "' was invalid.",
                invalidMBoxException);

      // Fail the test
      fail("The mbox '" + invalidAttachments.getAbsolutePath() +
           "' was invalid.");
    } catch (VocabularyException vocabularyException) {

      // Log the exception
      log.error("Unable to set up vocabulary for mbox parsing.",
                vocabularyException);

      // Fail the test
      fail("Unable to set up vocabulary for mbox parsing.");
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Mbox claimed to be cached.", notModifiedException);

      // Fail the test
      fail("Mbox claimed to be cached.");
    }

    // Check that the graph is not null (ie populated)
    assertTrue("MBox graph was not populated as expected.",
               mbox.getGraph() != null);

    // Retrieve the graph from the mbox
    Graph graph = mbox.getGraph();

    // Container for our vocab
    Properties vocab = null;

    try {

      // Create the map of properties for the graph
      vocab = EmailVocab.createVocabulary(graph);
    } catch (VocabularyException vocabularyException) {

      // Log the error
      log.error("Failed to create a vocabulary for the mbox graph.",
                vocabularyException);

      // Fail the test
      fail("Failed to create a vocabulary for the mbox graph.");
    }

    // Container for our graph element factory
    GraphElementFactory elementFactory = graph.getElementFactory();

    try {

      // Check that we have no bcc fields
      assertTrue("Unexpected BCC found in mbox.",
                 !graph.find(null,
                             (PredicateNode) vocab.get(EmailVocab.BCC),
                             null).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find bcc triples in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find bcc triples in mbox graph.");
    }

    try {

      // Check that we have no cc fields
      assertTrue("Unexpected CC found in mbox.",
                 !graph.find(null,
                             (PredicateNode) vocab.get(EmailVocab.CC),
                             null).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find cc triples in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find cc triples in mbox graph.");
    }

    // Container for object nodes
    ObjectNode objectNode = null;

    try {

      // Create a literal to represent the to value
      objectNode = elementFactory.createLiteral("mludlow@pisoftware.com");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create to literal.", graphElementFactoryException);

      // Fail the test
      fail("Failed to create to literal.");
    }

    try {

      // Check that we have the correct to value
      assertTrue("To field was not 'mludlow@pisoftware.com' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.TO),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find to in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to in mbox graph.");
    }

    try {

      // Create a literal to represent the from value
      objectNode = elementFactory.createLiteral("1soloman@ubi.com");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create from literal.", graphElementFactoryException);

      // Fail the test
      fail("Failed to create from literal.");
    }

    try {

      // Check that we have the correct from value
      assertTrue("From field was not '1soloman@ubi.com' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.FROM),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find from in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to from mbox graph.");
    }

    try {

      // Create a literal to represent the message-ID value
      objectNode = elementFactory.createLiteral(
          "<dc3e01c489af$0a674dfd$32e35234@ubi.com>");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create message-ID literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create message-ID literal.");
    }

    try {

      // Check that we have the correct message-ID value
      assertTrue(
          "Message-ID field was not " +
          "'<dc3e01c489af$0a674dfd$32e35234@ubi.com>' as expected.",
          graph.find(null,
                     (PredicateNode) vocab.get(EmailVocab.MESSAGE_ID),
                     objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find message-ID in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to message-ID mbox graph.");
    }

    try {

      // Create a literal to represent the subject value
      objectNode = elementFactory.createLiteral(
          "[SPAM] Software. Save up to 80%. - 535818");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create subject literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create subject literal.");
    }

    try {

      // Check that we have the correct subject value
      assertTrue("Subject field was not '[SPAM] Software. Save up to 80%. -" +
                 " 535818' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.SUBJECT),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find subject in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to subject mbox graph.");
    }

    // Container for our type predicate
    PredicateNode typeNode = null;

    try {

      // Create a predicate to represent the type URL
      typeNode = elementFactory.createResource(RDF.TYPE);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create type predicate.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create type predicate.");
    }

    try {

      // Check that we have the correct type value
      assertTrue("Type was not message as expected.",
                 graph.find(null, typeNode,
                            (ObjectNode) vocab.get(EmailVocab.MESSAGE)).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find message type in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to message type in mbox graph.");
    }

    try {

      // Check that we have no attachment types in our graph
      assertTrue("Unable to find attachments expected in graph.",
                 graph.find(null, typeNode,
                            (ObjectNode) vocab.get(EmailVocab.ATTACHMENT)).
                 hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find attachment type in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find to attachment type in mbox graph.");
    }

    try {

      // Create a literal to represent the attachment filename
      objectNode = elementFactory.createLiteral("ddsiepmw.jpg");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create attachment filename literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create attachment filename literal.");
    }

    try {

      // Check that we have the correct attachment filename value
      assertTrue("Could not find attachment 'ddsiepmw.jpg' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.FILE_NAME),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find attachment filename in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find to attachment filename mbox graph.");
    }

    try {

      // Create a literal to represent the attachment filename
      objectNode = elementFactory.createLiteral("osfixlcr.jpg");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create attachment filename literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create attachment filename literal.");
    }

    try {

      // Check that we have the correct attachment filename value
      assertTrue("Could not find attachment 'osfixlcr.jpg' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.FILE_NAME),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find attachment filename in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find to attachment filename mbox graph.");
    }

    try {

      // Create a literal to represent the attachment filename
      objectNode = elementFactory.createLiteral("fgibuhho.jpg");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create attachment filename literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create attachment filename literal.");
    }

    try {

      // Check that we have the correct attachment filename value
      assertTrue("Could not find attachment 'fgibuhho.jpg' as expected.",
                 graph.find(null,
                            (PredicateNode) vocab.get(EmailVocab.FILE_NAME),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find attachment filename in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find to attachment filename mbox graph.");
    }

    if (log.isDebugEnabled()) {

      // Container for our iterator
      ClosableIterator<Triple> attachmentIterator = null;

      try {

        // Get all "filename" triples
        attachmentIterator = graph.find(null, (PredicateNode)vocab.get(EmailVocab.SIZE), null);

      } catch (GraphException graphException) {

        // Log the error
        log.debug("Unable to find attachment filenames in mbox graph.", graphException);

        // Since this debugging we don't really need to do anything
        // with the exception
      }

      while (attachmentIterator.hasNext()) {

        log.debug("Invalid Attachment filename: " + (Triple)attachmentIterator.next());
      }
    }

    try {

      // Create a literal to represent an unnamed file attachment
      objectNode = elementFactory.createLiteral("attachment.atmt");

    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Failed to create unnamed attachment filename literal.",
                graphElementFactoryException);

      // Fail the test
      fail("Failed to create unnamed attachment filename literal.");
    }

    try {

      // Check that we have the correct attachment filename value
      assertTrue("Could not find attachment 'attachment.atmt' as expected.",
                 graph.find(null, (PredicateNode)vocab.get(EmailVocab.FILE_NAME),
                            objectNode).hasNext());
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to find unnamed attachment filename in mbox graph.",
                graphException);

      // Fail the test
      fail("Unable to find to unnamed attachment filename mbox graph.");
    }

    // Container for our iterator
    ClosableIterator<Triple> attachmentIterator = null;

    try {

      // Get all "filename" triples
      attachmentIterator = graph.find(null, (PredicateNode)vocab.get(EmailVocab.FILE_NAME), null);

    } catch (GraphException graphException) {

      // Log the error
      log.debug("Unable to find attachment filenames in mbox graph.", graphException);

      // Fail the test
      fail("Unable to find attachment filenames in mbox graph.");
    }

    // Counter for attachments
    int attachmentCount = 0;

    // Iterate through the attachments and count them
    for (; attachmentIterator.hasNext(); attachmentIterator.next()) {

      // Increment the number of attachments
      attachmentCount++;
    }

    // Check we have the right number of attachments
    assertTrue("Unexpected number of attachments.  Was " + attachmentCount +
               ", expected 4.", attachmentCount == 4);
  }

  /**
   * Creates a file content object which represents the file given.
   *
   * @param contentFile The file to represent
   *
   * @return The file content representation of the given file
   */
  public Content createContentObject(File contentFile) {

    // Containers for construction of content object
    Class<?> contentClass = null;
    Constructor<?> constructor = null;
    Content content = null;

    try {

      // Obtain the class for the FileContent object
      contentClass = Class.forName("org.mulgara.resolver.file.FileContent");
    } catch (ClassNotFoundException classNotFoundException) {

      // Log the exception
      log.error("Unable to find class [org.mulgara.resolver.file.FileContent] " +
                "for instantiation.", classNotFoundException);

      // Fail the test
      fail("Unable to find class [org.mulgara.resolver.file.FileContent] " +
           "for instantiation.");
    }

    try {

      // Obtain the Content object constructor
      constructor = contentClass.getConstructor(new Class[] {File.class});
    } catch (NoSuchMethodException noSuchMethodException) {

      // Log the exception
      log.error("Unable to find constructor for FileContent class.",
                noSuchMethodException);

      // Fail the test
      fail("Unable to find constructor for FileContent class.");
    }

    try {

      // Obtain a content handler for the test file
      content = (Content) constructor.newInstance(new Object[] {contentFile});
    } catch (InstantiationException instantiationException) {

      // Log the exception
      log.error("Unable to construct an instance of FileContent class.",
                instantiationException);

      // Fail the test
      fail("Unable to construct an instance of FileContent class.");
    } catch (IllegalAccessException illegalAccessException) {

      // Log the exception
      log.error("Unable to access FileContent class for construction.",
                illegalAccessException);

      // Fail the test
      fail("Unable to access FileContent class for construction.");
    } catch (InvocationTargetException invocationTargetException) {

      // Log the exception
      log.error(
          "Exception occurred during construction of a FileContent object.",
          invocationTargetException);

      // Fail the test
      fail("Exception occurred during construction of a FileContent object.");
    }

    return content;
  }

  /**
   * Default text runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }
}
