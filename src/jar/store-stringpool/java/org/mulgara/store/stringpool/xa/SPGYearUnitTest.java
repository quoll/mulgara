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

package org.mulgara.store.stringpool.xa;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;

// Third party packages
import junit.framework.*;
import org.apache.log4j.*;
import org.apache.log4j.xml.*;

import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.*;
import org.mulgara.util.Constants;

/**
 * Unit test for testing an xsd:gYear data type representation.
 *
 * @created 2004-10-04
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/03/11 04:15:22 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SPGYearUnitTest extends TestCase {

  /** Logger */
  private static Logger log = Logger.getLogger(SPGYearUnitTest.class);

  /** Parser for our expected data return values */
  private static SimpleDateFormat format;

  /** Constant valid test date */
  private static final String VALID_DATE = "2004";

  /** Constant valid test date (relative value for VALID_DATE3) */
  private static final String VALID_DATE_WEST = "2003";

  /** Constant valid test date (date outside of 0001 - 9999) */
  private static final String VALID_DATE2 = "200400";

  /** Constant valid test date (date with timezone) */
  private static final String VALID_DATE3 = "2004-04:00";

  /** Constant valid test date (date with timezone) */
  private static final String VALID_DATE4 = "2004Z";

  /** Invalid date 1 (non-numeric characters) */
  private static final String INVALID_DATE_1 = "2004g0";

  /** Invalid date 2 (Valid date, but not gYear format) */
  private static final String INVALID_DATE_2 = "2004-03-24";

  /** Invalid date 2 (Valid date, but not gYear format) */
  private static final String INVALID_DATE_3 = "2004+400";

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPGYearUnitTest(String name) {

    super(name);

    // Load the logging configuration
    BasicConfigurator.configure();

    try {

      DOMConfigurator.configure(new URL(System.getProperty(
          "log4j.configuration")));
    } catch (MalformedURLException mue) {

      log.error("Unable to configure logging service from XML configuration " +
                "file", mue);
    }
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    // Set up our parser to format dates to a "CCYY" (gYear) style format
    format = new SimpleDateFormat("yyyy");

    TestSuite suite = new TestSuite();
    suite.addTest(new SPGYearUnitTest("testValid"));
    suite.addTest(new SPGYearUnitTest("testInvalid"));
    suite.addTest(new SPGYearUnitTest("testCompare"));

    return suite;
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   * @throws Exception
   */
  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }

  /**
   * Tests that valid xsd:gYear values are accepted and processed correctly.
   */
  public void testValid() {

    boolean westOfGMT = TimeZone.getDefault().getRawOffset() < 0;

    // Create a new factory
    SPGYearFactory factory = new SPGYearFactory();

    // Create a gYear object by lexical string
    SPGYearImpl gYear = (SPGYearImpl) factory.newSPTypedLiteral(XSD.GYEAR_URI,
        VALID_DATE);

    // Test that the lexical form of the date is correct
    assertTrue("GYear lexical form was not " + VALID_DATE +
               " as expected. was:" +
               gYear.getLexicalForm(), gYear.getLexicalForm().equals(VALID_DATE));

    // Retrieve the byte data of the gYear object
    ByteBuffer yearBytes = gYear.getData();

    // Retrieve the long value from the buffer
    long yearLong = yearBytes.getLong();

    // Create a date object from the year's long
    Date yearDate = new Date(yearLong);

    // Format the resulting year
    String year = format.format(yearDate);

    // Test the correct value is stored - relative year, so will always be 2004
    assertTrue("GYear byte buffer value was not " + VALID_DATE +
               " as expected, was: " + year,
               ("" + year).equals(VALID_DATE));

    // Byte buffer to hold our date information
    ByteBuffer buffer = ByteBuffer.wrap(new byte[SPGYearImpl.getBufferSize()]);

    // If the previous step passed then we know the long value is what we want,
    // so store it in our buffer
    buffer.putLong(yearLong);
    buffer.put((byte)1);

    // Reset the buffer for reading
    buffer.flip();

    if (log.isDebugEnabled()) {

      log.debug("Creating gYear from byte buffer storing value: " +
                format.format(new Date(yearLong)));

      log.debug("Original year long vs. stored long: " + yearLong + " vs. " +
                buffer.getLong());
      buffer.get();

      // Reset the buffer
      buffer.flip();
    }

    // Create a gYear object by byte buffer
    gYear = (SPGYearImpl) factory.newSPTypedLiteral(0, buffer);

    // Test that the lexical form of the date is correct
    assertTrue("GYear lexical form was not " + VALID_DATE +
               " as expected. was:" +
               gYear.getLexicalForm(), gYear.getLexicalForm().equals(VALID_DATE));

    // Retrieve the byte data of the gYear object
    yearBytes = gYear.getData();

    // Retrieve the long value from the buffer
    yearLong = yearBytes.getLong();

    // Create a date object from the year's long
    yearDate = new Date(yearLong);

    // Format the resulting year
    year = format.format(yearDate);

    // Test the correct value is stored
    assertTrue("GYear byte buffer value was not " + VALID_DATE +
               " as expected, was: " + year,
               ("" + year).equals(VALID_DATE));

    // Create a gYear object by lexical string (testing range acceptance)
    gYear = (SPGYearImpl) factory.newSPTypedLiteral(XSD.GYEAR_URI, VALID_DATE2);

    // Test that the lexical form of the date is correct
    assertTrue("GYear lexical form was not " + VALID_DATE2 +
               " as expected. was:" +
               gYear.getLexicalForm(),
               gYear.getLexicalForm().equals(VALID_DATE2));

    // Retrieve the byte data of the gYear object
    yearBytes = gYear.getData();

    // Retrieve the long value from the buffer
    yearLong = yearBytes.getLong();

    // Create a date object from the year's long
    yearDate = new Date(yearLong);

    // Format the resulting year
    year = format.format(yearDate);

    // Test the correct value is stored
    assertTrue("GYear byte buffer value was not " + VALID_DATE2 +
               " as expected, was: " + year,
               ("" + year).equals(VALID_DATE2));

    // Create a gYear object by lexical string (testing timezone acceptance)
    gYear = (SPGYearImpl) factory.newSPTypedLiteral(XSD.GYEAR_URI, VALID_DATE3);

    // Test that the lexical form of the date is correct
    assertTrue("GYear lexical form was not " + VALID_DATE3 +
               " as expected. was:" + gYear.getLexicalForm(),
               gYear.getLexicalForm().equals(VALID_DATE3));

    // Retrieve the byte data of the gYear object
    yearBytes = gYear.getData();

    // Retrieve the long value from the buffer
    yearLong = yearBytes.getLong();

    // Create a date object from the year's long
    yearDate = new Date(yearLong);

    // Format the resulting year
    year = format.format(yearDate);

    // Test the correct value is stored
    assertTrue("GYear byte buffer value was not " + VALID_DATE +
               " as expected, was: " + year,
               !westOfGMT ? ("" + year).equals(VALID_DATE)
               : ("" + year).equals(VALID_DATE_WEST));

    // Create a gYear object by lexical string (testing 'Z' acceptance)
    gYear = (SPGYearImpl) factory.newSPTypedLiteral(XSD.GYEAR_URI, VALID_DATE4);

    // Test that the lexical form of the date is correct
    assertTrue("GYear lexical form was not " + VALID_DATE4 +
               " as expected. was:" +
               gYear.getLexicalForm(),
               gYear.getLexicalForm().equals(VALID_DATE4));

    // Retrieve the byte data of the gYear object
    yearBytes = gYear.getData();

    // Retrieve the long value from the buffer
    yearLong = yearBytes.getLong();

    // Create a date object from the year's long
    yearDate = new Date(yearLong);

    // Format the resulting year
    year = format.format(yearDate);

    // Test the correct value is stored
    assertTrue("GYear byte buffer value was not " + VALID_DATE +
               " as expected, was: " + year,
               !westOfGMT ? ("" + year).equals(VALID_DATE)
                   : ("" + year).equals(VALID_DATE_WEST));
  }

  /**
   * Tests invalid xsd:gYear values.
   */
  public void testInvalid() {

    // Create a new factory
    SPGYearFactory factory = new SPGYearFactory();

    // Container for our gYear object
    @SuppressWarnings("unused")
    SPGYearImpl gYear = null;

    // Indicator of whether an exception occurred or not.  Assumed false.
    boolean failed = false;

    try {

      // Create a gYear object by lexical string
      gYear = (SPGYearImpl) factory.newSPTypedLiteral(XSD.GYEAR_URI, INVALID_DATE_1);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gYear with non-numeric characters", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gYear object by lexical string
      gYear = (SPGYearImpl) factory.newSPTypedLiteral(XSD.GYEAR_URI, INVALID_DATE_2);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gYear with invalid lexical format.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gYear object by lexical string
      gYear = (SPGYearImpl) factory.newSPTypedLiteral(XSD.GYEAR_URI, INVALID_DATE_3);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gYear with invalid timezone format.", failed);

  }

  /**
   * Tests that gYear objects a compared properly.
   */
  public void testCompare() {

    // Create a new factory
    SPGYearFactory factory = new SPGYearFactory();

    // Create a gYear object by lexical string
    SPGYearImpl gYear = (SPGYearImpl) factory.newSPTypedLiteral(XSD.GYEAR_URI, VALID_DATE);

    // Create a gYear object that is different
    SPGYearImpl gYear2 = (SPGYearImpl) factory.newSPTypedLiteral(XSD.GYEAR_URI, VALID_DATE2);

    // Test that two same objects will be equal
    assertTrue("Same object did not register as equal.", gYear.equals(gYear));

    // Test that two different objects will be inequal
    assertTrue("Different object was unexpectedly found to be equal.", !gYear.equals(gYear2));

    // Test that two same objects will compare equally
    assertTrue("Same object did not compare equally.", gYear.compareTo(gYear) == 0);

    // Test that two different objects will compare inequally
    assertTrue("Different object was unexpectedly found to compare equally.",
               gYear.compareTo(gYear2) != 0);

    // Obtain the comparator for the first object
    SPComparator comparator = gYear.getSPComparator();

    // Test that two same objects will compare equally by prefix
    assertTrue("Same object did not compare equally by prefix.",
               comparator.comparePrefix(gYear.getData(),
                                        gYear.getData(),
                                        Constants.SIZEOF_INT) == 0);

    // Test that two different objects will compare inequally by prefix
    assertTrue(
        "Different object was unexpectedly found to compare inequally by prefix.",
        comparator.comparePrefix(gYear.getData(),
                                 gYear2.getData(),
                                 Constants.SIZEOF_INT) == 0);

    // Test that two same objects will compare equally by comparator
    assertTrue("Same object did not compare equally by comparator.",
               comparator.compare(gYear.getData(), 0, gYear.getData(), 0) == 0);

    // Test that two different objects will compare inequally by comparator
    assertTrue(
        "Different object was unexpectedly found to compare inequally by comparator.",
        comparator.compare(gYear.getData(), 0, gYear2.getData(), 0) != 0);

  }

  /**
   * Initialise members.
   */
  public void setUp() {

    try {

      // Set up the super class' members
      super.setUp();
    } catch (Exception exception) {

      // Fail the test if we cannot set up
      fail("Failed to set up members for testing. " + exception.getMessage());
    }
  }

  /**
   * The teardown method for JUnit
   */
  public void tearDown() {

    try {

      // Tear down the framework for the super class
      super.tearDown();
    } catch (Exception exception) {

      // Fail the test if we cannot tear down
      fail("Failed to tear down members after testing. " + exception.getMessage());
    }
  }
}
