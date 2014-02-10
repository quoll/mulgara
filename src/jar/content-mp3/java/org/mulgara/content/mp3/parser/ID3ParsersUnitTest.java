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

package org.mulgara.content.mp3.parser;

// Standard Java
import java.io.*;

// Apache logging
import org.apache.log4j.*;

// Mp3 Library
import org.blinkenlights.id3.*;
import org.blinkenlights.id3.v1.*;

// JRDF
import org.jrdf.graph.mem.*;
import org.jrdf.graph.*;
import org.jrdf.util.ClosableIterator;
import org.jrdf.vocabulary.RDF;

// JUnit
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal packages
import org.mulgara.content.mp3.parser.api.*;
import org.mulgara.content.mp3.parser.exception.*;

/**
 * Unit testing for the ID3 tag parsing classes.
 *
 * @created 2004-08-19
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/11 07:02:24 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ID3ParsersUnitTest extends TestCase {

  /** The category to log to. */
  private static Logger log = Logger.getLogger(ID3ParsersUnitTest.class);

  /**
   * Constructor as required by JUnit's TestCase.
   */
  public ID3ParsersUnitTest(String name) {

    super(name);

    // load the logging configuration
//    BasicConfigurator.configure();
//    try {
//
//      DOMConfigurator.configure(new URL(System.getProperty(
//          "log4j.configuration")));
//    }
//    catch (MalformedURLException mue) {
//
//      log.error(
//          "Unable to configure logging service from XML configuration " +
//          "file", mue);
//    }
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
    suite.addTest(new ID3ParsersUnitTest("testProcessID3v1"));
    suite.addTest(new ID3ParsersUnitTest("testProcessID3v2"));

    return suite;
  }

  /**
   * Test the process method of an MP3ToRdf entity extraction to XML conversion.
   */
  public void testProcessID3v1() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting processing ID3v1 test");
    }

    try {

      // Initialise the parser factory
      ParserFactory.getInstance().initialiseFactory();
    }
    catch (FactoryException factoryException) {

      // If we can't initialise the factory log the error
      log.error("Unable to initialise factory for ID3v1 parsing.",
          factoryException);

      // Fail the test
      fail("Unable to initialise factory for ID3v1 parsing.");
    }

    // Create a file pointing to an mp3
    File mp3File = new File(System.getProperty("cvs.root") +
        "/tmp/mp3/kp068-karaoke-tundra-02-green-parrot.mp3");

    // Container for an MP3 file
    MP3File file = null;
    ID3V1Tags id3v1 = null;

    try {

      // Create an MP3File out of the file
      file = new MP3File(mp3File);

      // Get the v1 tag of the file
      id3v1 = file.getID3V1Tags();

    }
    catch (ID3Exception tagException) {

      // Log the error
      log.error("Invalid tags were found in MP3 file: " +
          mp3File.getAbsolutePath(),
          tagException);

      // Fail the test
      fail("Invalid tags were found in MP3 file: " + mp3File.getAbsolutePath());
    }

    if (id3v1 == null) {

      // If the tag does not exist then create a new one
      id3v1 = new ID3V1_0Tags();
    }

    // Set the tag values
    id3v1.setAlbum("Residence");
    id3v1.setArtist("Karaoke Tundra");
    id3v1.setComment("http://www.kikapu.com");
    id3v1.setGenre(ID3V1Tags.Genre.Electronic);
    id3v1.setTitle("Green & Parrot");
    id3v1.setYear("2004");

    // Store the tag in the mp3
    try {
      file.sync();
    }
    catch (ID3Exception tagException) {

      // Log the error
      log.error("Invalid tags were found in MP3 file: " +
          mp3File.getAbsolutePath(),
          tagException);

      // Fail the test
      fail("Invalid tags were found in MP3 file: " + mp3File.getAbsolutePath());
    }

    try {

      // Delete the v2 tag
      if (log.isDebugEnabled()) {

        try {
          log.debug("--  " + file.getID3V2Tags());
        }
        catch (ID3Exception tagException) {
          // Log the error
          log.error("Invalid tags were found in MP3 file: " +
              mp3File.getAbsolutePath(),
              tagException);

          // Fail the test
          fail("Invalid tags were found in MP3 file: " +
              mp3File.getAbsolutePath());
        }
      }

      file.removeID3V2Tags();
    }
    catch (ID3Exception tagException) {

      // Log the error
      log.error("Invalid tags were found in MP3 file: " +
          mp3File.getAbsolutePath(),
          tagException);

      // Fail the test
      fail("Invalid tags were found in MP3 file: " +
          mp3File.getAbsolutePath());
    }

    try {

      // Store the changes
      file.sync();
    }
    catch (ID3Exception tagException) {

      // Log the error
      log.error("Unable to save MP3 file: " + mp3File.getAbsolutePath(),
          tagException);

      // Fail the test
      fail("Unable to save MP3 file: " + mp3File.getAbsolutePath());
    }

    // Container for our graph
    Graph model = null;

    try {

      // Create a model to store our statements in
      model = new GraphImpl();
    }
    catch (GraphException graphException) {

      // Log the error
      log.error("Unable to create a new graph to store parsed RDF in.",
          graphException);

      // Fail the test
      fail("Unable to create a new graph to store parsed RDF in.");
    }

    // Container for the parsing results
    MP3Conversion conversion = new MP3Conversion(file, model,
        mp3File.toURI());

    // Container for our parser
    ID3Parser parser = null;

    try {

      // Create a parser for our test
      parser = ParserFactory.getInstance().createID3Parser();
    }
    catch (FactoryException factoryException) {

      // If we cannot create a parser then log the error
      log.error("Unable to create an ID3 Parser from the factory.",
          factoryException);

      // Fail the test
      fail("Unable to create an ID3 Parser from the factory.");
    }

    try {

      // Parse the tags into a model
      parser.parseTags(conversion);
    }
    catch (ParserException parserException) {

      // Log the error
      log.error("Unable to parse the ID3v1 tags to a model.", parserException);

      // Fail the test
      fail("Unable to parse the ID3v1 tags to a model.");
    }

    if (log.isDebugEnabled()) {

      try {

        // Get all statements in the graph
        ClosableIterator<Triple> iterator = model.find(null, null, null);

        while (iterator.hasNext()) {

          // Obtain the next triple
          Triple triple = (Triple) iterator.next();

          log.debug(">> Graph triple [" + triple.getSubject() +
              ", " + triple.getPredicate() + ", " +
              triple.getObject() + "]");
        }
      }
      catch (GraphException graphException) {

        // Log the error
        log.error("Unable to debug statements of graph.",
            graphException);

        // Fail the test
        fail("Unable to debug statements of graph.");
      }
    }

    // Container for our processor instance
    IdentifierProcessor processor = new IdentifierProcessor();

    try {

      // Populate the processor
      processor.createMappings(model);
    }
    catch (IdentifierException identifierException) {

      // Log the error
      log.error("Unable to initialise the identifier processor.",
          identifierException);

      // Fail the test
      fail("Unable to initialise the identifier processor.");
    }

    try {

      // Create a resource object to match the resource name we gave the mp3 data
      // plus the id3 v1 tag information
      model.getElementFactory().createResource();

      // Create a property to signify the type
      PredicateNode property = model.getElementFactory().createResource(RDF.TYPE);

      // create a resource to represent the ID3v1 type
      ObjectNode typeResource = (ObjectNode) processor.resolveIdentifier(
          IdentifierProcessor.MP3_TYPE);

      // Check that we have the correct type
      assertTrue("Type was not MP3 as expected for id3v1 resource.",
          model.contains(null, property, typeResource));

      // Create a property to signify the album
      property = processor.resolveIdentifier(IdentifierProcessor.TALB);

      // Check that we have the correct album
      assertTrue("Album was not 'Residence' as expected for id3v1 resource.",
          model.contains(null, property,
          model.getElementFactory().createLiteral(
          "Residence")));

      // Create a property to signify the artist
      property = processor.resolveIdentifier(IdentifierProcessor.TCOM);

      // Check that we have the correct artist
      assertTrue(
          "Artist was not 'Karaoke Tundra' as expected for id3v1 resource.",
          model.contains(null, property,
          model.getElementFactory().createLiteral(
          "Karaoke Tundra")));

      // Create a property to signify the comment
      property = processor.resolveIdentifier(IdentifierProcessor.COMM);

      // Check that we have the correct comment
      assertTrue(
          "Comment was not 'http://www.kikapu.com' as expected for id3v1 resource.",
          model.contains(null, property,
          model.getElementFactory().createLiteral(
          "http://www.kikapu.com")));

      // Create a property to signify the genre
      property = processor.resolveIdentifier(IdentifierProcessor.MCDI);

      // Check that we have the correct genre
      assertTrue("Genre was not " + ID3V1Tags.Genre.Electronic + " as " +
          "expected for id3v1 resource.",
          model.contains(null, property,
          model.getElementFactory().createLiteral(ID3V1Tags.Genre.Electronic.toString())));

      // Create a property to signify the title
      property = processor.resolveIdentifier(IdentifierProcessor.TOAL);

      // Check that we have the correct title
      assertTrue(
          "Title was not 'Green & Parrot' as expected for id3v1 resource.",
          model.contains(null, property,
          model.getElementFactory().createLiteral(
          "Green & Parrot")));

      // Create a property to signify the release year
      property = processor.resolveIdentifier(IdentifierProcessor.TYER);

      // Check that we have the correct release year
      assertTrue("Title was not '2004' as expected for id3v1 resource.",
          model.contains(null, property,
          model.getElementFactory().createLiteral("2004")));

      // Create a property to signify the uri
      property = processor.resolveIdentifier(IdentifierProcessor.MP3_URI);

      // Check that we have the correct uri
      assertTrue("Uri was not '" + mp3File.toURI() +
          "' as expected for id3v1 resource.",
          model.contains(null, property,
          model.getElementFactory().createResource(
          mp3File.toURI())));

    }
    catch (GraphException graphException) {

      // Log the error
      log.error("Unable to perform contains for graph content comparison.",
          graphException);

      // Fail the test
      fail("Unable to perform contains for graph content comparison.");
    }
    catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Unable to create node for graph element comparison.",
          graphElementFactoryException);

      // Fail the test
      fail("Unable to create node for graph element comparison.");
    }

    if (log.isDebugEnabled()) {

      log.debug("// Finished processing test");
    }
  }

  /**
   * Test the process method of an MP3ToRdf entity extraction to XML conversion.
   */
  public void testProcessID3v2() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting processing ID3v2 test");
    }

    try {

      // Initialise the parser factory
      ParserFactory.getInstance().initialiseFactory();
    }
    catch (FactoryException factoryException) {

      // If we can't initialise the factory log the error
      log.error("Unable to initialise factory for ID3v2 parsing.",
          factoryException);

      // Fail the test
      fail("Unable to initialise factory for ID3v2 parsing.");
    }

    // Create a file pointing to an mp3
    File mp3File = new File(System.getProperty("cvs.root") +
        "/tmp/mp3/Chrono_Trigger_600_AD_in_Piano.mp3");

    // Container for an MP3 file
    MP3File file = null;

    // Create an MP3File out of the file
    file = new MP3File(mp3File);

    // Container for our graph
    Graph model = null;

    try {

      // Create a model to store our statements in
      model = new GraphImpl();
    }
    catch (GraphException graphException) {

      // Log the error
      log.error("Unable to create a new graph to store parsed RDF in.",
                graphException);

      // Fail the test
      fail("Unable to create a new graph to store parsed RDF in.");
    }

    // Container for the parsing results
    MP3Conversion conversion = new MP3Conversion(file, model, mp3File.toURI());

    // Container for our parser
    ID3Parser parser = null;

    try {

      // Create a parser for our test
      parser = ParserFactory.getInstance().createID3Parser();
    } catch (FactoryException factoryException) {

      // If we cannot create a parser then log the error
      log.error("Unable to create an ID3 Parser from the factory.",
                factoryException);

      // Fail the test
      fail("Unable to create an ID3 Parser from the factory.");
    }

    try {

      // Parse the tags into a model
      parser.parseTags(conversion);
    } catch (ParserException parserException) {

      // Log the error
      log.error("Unable to parse the ID3v2 tags to a model.", parserException);

      // Fail the test
      fail("Unable to parse the ID3v2 tags to a model.");
    }

//    if (log.isDebugEnabled()) {

      try {

        // Get all statements in the graph
        ClosableIterator<Triple> iterator = model.find(null, null, null);

        while (iterator.hasNext()) {

          // Get the next triple
          Triple triple = (Triple) iterator.next();
          System.err.println(">> Graph triple [" + triple.getSubject() + ", " +
                    triple.getPredicate() + ", " + triple.getObject() + "]");
        }
      } catch (GraphException graphException) {

        // Log the error
        log.error("Unable to debug statements of graph.",
                  graphException);

        // Fail the test
        fail("Unable to debug statements of graph.");
      }
//    }

    // Container for our processor instance
    IdentifierProcessor processor = new IdentifierProcessor();

    try {

      // Populate the processor
      processor.createMappings(model);
    } catch (IdentifierException identifierException) {

      // Log the error
      log.error("Unable to initialise the identifier processor.",
                identifierException);

      // Fail the test
      fail("Unable to initialise the identifier processor.");
    }

    try {

      // Create a property to signify the type
      PredicateNode property = model.getElementFactory().createResource(RDF.
          TYPE);

      // Create a resource to represent the MP3 type
      ObjectNode typeResource = (ObjectNode) processor.resolveIdentifier(
          IdentifierProcessor.MP3_TYPE);

      // Check that we have the correct type
      assertTrue("Type was not MP3 as expected for id3v2 resource.",
                 model.contains(null, property, typeResource));

      // Create a property to signify the artist
      property = processor.resolveIdentifier(IdentifierProcessor.TCOM);

      // Check that we have the correct artist
      assertTrue(
          "Artist was not 'Yasunori Mitsuda' as expected for id3v2 resource.",
          model.contains(null, property,
          model.getElementFactory().createLiteral(
          "Yasunori Mitsuda")));

      // Create a property to signify the title description
      property = processor.resolveIdentifier(IdentifierProcessor.TIT2);

      // Check that we have the correct title description
      assertTrue("Title description was not 'Chrono Trigger 600 A.D. in Piano OC Remix' as expected for id3v2 resource.",
                 model.contains(null, property,
                                model.getElementFactory().createLiteral(
          "Chrono Trigger 600 A.D. in Piano OC Remix")));

//      // Create a property to signify the tagging time
//      property = processor.resolveIdentifier(processor.TDTG);
//
//      // Check that we have the correct tagging time
//      assertTrue(
//          "Tagging time was not '2003-09-16T22:20:22' as expected for id3v2 resource.",
//          model.contains(null, property,
//                         model.getElementFactory().createLiteral(
//          "2003-09-16T22:20:22")));

      // Create a property to signify the copyright message
      property = processor.resolveIdentifier(IdentifierProcessor.TCOP);

      // Check that we have the correct copyright message
      assertTrue(
          "Copyright message was not 'Squaresoft' as expected for id3v2 resource.",
          model.contains(null, property,
                         model.getElementFactory().createLiteral("Squaresoft")));

//      // Create a property to signify the unknown
//      property = processor.resolveIdentifier("unknown");
//
//      // Check that we have the correct unknown
//      assertTrue("Unknown was not 'Helium12ID' as expected for id3v2 resource.",
//          model.contains(null, property,
//          model.getElementFactory().createLiteral("Helium12ID")));

//      // Create a property to signify the subtitle
//      property = processor.resolveIdentifier(processor.TIT3);
//
//      // Check that we have the correct subtitle
//      assertTrue("Subtitle was not 'OCR01040' as expected for id3v2 resource.",
//                 model.contains(null, property,
//                                model.getElementFactory().createLiteral(
//          "OCR01040")));

      // Create a property to signify the release type
      property = processor.resolveIdentifier(IdentifierProcessor.TCON);

      // Check that we have the correct release type
      assertTrue("Release type was not 'Game' as expected for id3v2 resource.",
                 model.contains(null, property,
                                model.getElementFactory().createLiteral("Game")));

      // Create a property to signify the BPM
      property = processor.resolveIdentifier(IdentifierProcessor.TBPM);

      // Check that we have the correct BPM
      assertTrue("BPM was not '00000' as expected for id3v2 resource.",
                 model.contains(null, property,
                                model.getElementFactory().createLiteral("0")));

      // Create a property to signify the lead performer
      property = processor.resolveIdentifier(IdentifierProcessor.TPE1);

      if (log.isDebugEnabled()) {

        log.debug(">> Searching for triple [blank_node, " + property + ", " +
                  model.getElementFactory().createLiteral(
            "kLuTz") + "]");
      }

      // Check that we have the correct lead performer
      assertTrue(
          "Lead performer was not 'kLuTz' as expected for id3v2 resource.",
          model.contains(null, property,
                         model.getElementFactory().createLiteral("kLuTz")));

      // Create a property to signify the original performer
      property = processor.resolveIdentifier(IdentifierProcessor.TOPE);

      // Check that we have the correct original performer
      assertTrue(
          "Original performer was not 'SNES' as expected for id3v2 resource.",
          model.contains(null, property,
                         model.getElementFactory().createLiteral("SNES")));

      // Create a property to signify the album
      property = processor.resolveIdentifier(IdentifierProcessor.TALB);

      // Check that we have the correct album
      assertTrue(
          "Album was not 'http://www.ocremix.org' as expected for id3v2 resource.",
          model.contains(null, property,
                         model.getElementFactory().createLiteral(
          "http://www.ocremix.org")));

//      // Create a property to signify the recording time
//      property = processor.resolveIdentifier(processor.TDRC);
//
//      // Check that we have the correct recording time
//      assertTrue(
//          "Recording time was not '2003-09-16' as expected for id3v2 resource.",
//          model.contains(null, property,
//                         model.getElementFactory().createLiteral("2003-09-16")));

      // Create a property to signify the original title
      property = processor.resolveIdentifier(IdentifierProcessor.TOAL);

      // Check that we have the correct original title
      assertTrue(
          "Original title was not 'Chrono Trigger' as expected for id3v2 resource.",
          model.contains(null, property,
                         model.getElementFactory().createLiteral(
          "Chrono Trigger")));

      // Create a property to signify the uri
      property = processor.resolveIdentifier(IdentifierProcessor.MP3_URI);

      // Check that we have the correct uri
      assertTrue("Uri was not '" + mp3File.toURI() +
                 "' as expected for id3v2 resource.",
                 model.contains(null, property,
                                model.getElementFactory().createResource(
                                mp3File.toURI())));
    } catch (GraphException graphException) {

      // Log the error
      log.error("Unable to perform contains for graph content comparison.",
                graphException);

      // Fail the test
      fail("Unable to perform contains for graph content comparison.");
    } catch (GraphElementFactoryException graphElementFactoryException) {

      // Log the error
      log.error("Unable to create node for graph element comparison.",
                graphElementFactoryException);

      // Fail the test
      fail("Unable to create node for graph element comparison.");
    }

    if (log.isDebugEnabled()) {

      log.debug("// Finished processing test");
    }
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
