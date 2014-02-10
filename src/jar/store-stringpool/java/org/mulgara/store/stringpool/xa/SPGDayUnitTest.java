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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.SPComparator;
import org.mulgara.util.Constants;

/**
 * Unit test for testing an xsd:gDay data type representation.
 *
 * @created 2004-10-06
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
public class SPGDayUnitTest extends TestCase {

  /** Logger */
  private static Logger log = Logger.getLogger(SPGDayUnitTest.class);

  /** Parser for our expected data return values */
  private static SimpleDateFormat format;

  /** Constant valid test date */
  private static final String VALID_DATE = "---04";
  
  /** A valid value for the VALID_DATE in TimeZones west of GMT **/ 
  private static final String VALID_DATE_WEST = "---03";

  /** Constant valid test date (Timezone added) */
  private static final String VALID_DATE2 = "---04-04:00";

  /** Constant valid test date (date with UTC timezone) */
  private static final String VALID_DATE3 = "---04Z";

  /** Constant valid test date (Upper/Lower bounds test) */
  private static final String VALID_DATE4 = "---31Z";

  /** A valid value for the VALID_DATE4 in TimeZones west of GMT **/ 
  private static final String VALID_DATE4_WEST = "---30";

  /** A valid value for the VALID_DATE4 in TimeZones west of GMT **/ 
  private static final String VALID_DATE4_EAST = "---31";

  /** Constant valid test date (Upper/Lower bounds test) */
  private static final String VALID_DATE5 = "---01Z";

  /** A valid value for the VALID_DATE5 in TimeZones west of GMT **/ 
  private static final String VALID_DATE5_WEST = "---31";

  /** A valid value for the VALID_DATE5 in TimeZones west of GMT **/ 
  private static final String VALID_DATE5_EAST = "---01";

  /** Invalid date 1 (non-numeric characters) */
  private static final String INVALID_DATE_1 = "---2g";

  /** Invalid date 2 (Valid date, but not gDay format) */
  private static final String INVALID_DATE_2 = "2004-03-24";

  /** Invalid date 3 (Valid date, invalid timezone) */
  private static final String INVALID_DATE_3 = "---04+400";

  /** Invalid date 4 (Invalid date) */
  private static final String INVALID_DATE_4 = "---32";

  /** Invalid date 5 (Preceding '-') */
  private static final String INVALID_DATE_5 = "----04";

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPGDayUnitTest(String name) {

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

    // Set up our parser to format dates to a "MM" (gDay) style format
    format = new SimpleDateFormat("---dd");

    TestSuite suite = new TestSuite();
    suite.addTest(new SPGDayUnitTest("testValid"));
    suite.addTest(new SPGDayUnitTest("testInvalid"));
    suite.addTest(new SPGDayUnitTest("testCompare"));

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
   * Tests that valid xsd:gDay values are accepted and processed correctly.
   */
  public void testValid() {

  	// Get a TimeZone instance which will help us interpret the results.
  	TimeZone tz = TimeZone.getDefault();
  	boolean westOfGMT = tz.getRawOffset() < 0;
	
    // Create a new factory
    SPGDayFactory factory = new SPGDayFactory();

    // Create a gDay object by lexical string
    SPGDayImpl gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.
        GDAY_URI, VALID_DATE);

    // Because the underlying implementation runs through Calendar 
    // instances which muck with the results based on the TimeZone
    // where the tests are run, we just try to check them against
    // that context.
    
    // Test that the lexical form of the date is correct
    assertTrue("GDay lexical form was not " + VALID_DATE +
               " as expected. was:" + gDay.getLexicalForm(),
               gDay.getLexicalForm().equals(VALID_DATE));

    // Retrieve the byte data of the gDay object
    ByteBuffer dayBytes = gDay.getData();

    // Retrieve the long value from the buffer
    long dayLong = dayBytes.getLong();

    // Create a date object from the day's long
    Date dayDate = new Date(dayLong);

    // Format the resulting day
    String day = format.format(dayDate);

    // Test the correct value is stored
    assertTrue("GDay byte buffer value was not " + VALID_DATE +
               " as expected, was: " + day,
            	("" + day).equals(VALID_DATE));

    // Byte buffer to hold our date information
    ByteBuffer buffer = ByteBuffer.wrap(new byte[SPGDayImpl.getBufferSize()]);

    // If the previous step passed then we know the long value is what we want,
    // so store it in our buffer
    buffer.putLong(dayLong);
    buffer.put((byte)1);

    // Reset the buffer for reading
    buffer.flip();

    if (log.isDebugEnabled()) {

      log.debug("Creating gDay from byte buffer storing value: " +
                format.format(new Date(dayLong)));

      log.debug("Original day long vs. stored long: " + dayLong + " vs. " +
                buffer.getLong());
      buffer.get();

      // Reset the buffer
      buffer.flip();
    }

    // Create a gDay object by byte buffer
    gDay = (SPGDayImpl) factory.newSPTypedLiteral(0, buffer);

    // Test that the lexical form of the date is correct
    assertTrue("GDay lexical form was not " + VALID_DATE +
               " as expected. was:" + gDay.getLexicalForm(),
                  gDay.getLexicalForm().equals(VALID_DATE));

    // Retrieve the byte data of the gDay object
    dayBytes = gDay.getData();

    // Retrieve the long value from the buffer
    dayLong = dayBytes.getLong();

    // Create a date object from the day's long
    dayDate = new Date(dayLong);

    // Format the resulting day
    day = format.format(dayDate);

    // Test the correct value is stored
    assertTrue("GDay byte buffer value was not " + VALID_DATE +
               " as expected, was: " + day,
               ("" + day).equals(VALID_DATE));

    // Create a gDay object by lexical string (testing range acceptance)
    gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.GDAY_URI,
                                                  VALID_DATE2);

    // Test that the lexical form of the date is correct
    assertTrue("GDay lexical form was not " + VALID_DATE2 +
               " as expected. was:" + gDay.getLexicalForm(),
               gDay.getLexicalForm().equals(VALID_DATE2));

    // Retrieve the byte data of the gDay object
    dayBytes = gDay.getData();

    // Retrieve the long value from the buffer
    dayLong = dayBytes.getLong();

    // Create a date object from the day's long
    dayDate = new Date(dayLong);

    // Format the resulting day
    day = format.format(dayDate);

    // Test the correct value is stored
    assertTrue("GDay byte buffer value was not " + VALID_DATE +
               " as expected, was: " + day,
               !westOfGMT ? ("" + day).equals(VALID_DATE)
            		   : ("" + day).equals(VALID_DATE_WEST) );

    // Create a gDay object by lexical string (testing timezone acceptance)
    gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.GDAY_URI,
                                                  VALID_DATE3);

    // Test that the lexical form of the date is correct
    assertTrue("GDay lexical form was not " + VALID_DATE3 +
               " as expected. was:" + gDay.getLexicalForm(),
               gDay.getLexicalForm().equals(VALID_DATE3));               

    // Retrieve the byte data of the gDay object
    dayBytes = gDay.getData();

    // Retrieve the long value from the buffer
    dayLong = dayBytes.getLong();

    // Create a date object from the day's long
    dayDate = new Date(dayLong);

    // Format the resulting day
    day = format.format(dayDate);

    // Test the correct value is stored
    assertTrue("GDay byte buffer value was not " + VALID_DATE +
               " as expected, was: " + day,
               ! westOfGMT ? ("" + day).equals(VALID_DATE)
            		: ("" + day).equals(VALID_DATE_WEST) );

    // Create a gDay object by lexical string (testing timezone acceptance)
    gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.GDAY_URI,
                                                  VALID_DATE4);

    // Test that the lexical form of the date is correct
    assertTrue("GDay lexical form was not " + VALID_DATE4 +
               " as expected. was:" + gDay.getLexicalForm(),
               gDay.getLexicalForm().equals(VALID_DATE4));

    // Retrieve the byte data of the gDay object
    dayBytes = gDay.getData();

    // Retrieve the long value from the buffer
    dayLong = dayBytes.getLong();

    // Create a date object from the day's long
    dayDate = new Date(dayLong);

    // Format the resulting day
    day = format.format(dayDate);

    // Test the correct value is stored
    assertTrue("GDay byte buffer value was not " + VALID_DATE4 +
               " as expected, was: " + day,
               !westOfGMT ? ("" + day).equals(VALID_DATE4_EAST)
            	 : ("" + day).equals(VALID_DATE4_WEST) );

    // Create a gDay object by lexical string (testing timezone acceptance)
    gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.GDAY_URI, VALID_DATE5);

    // Test that the lexical form of the date is correct
    assertTrue("GDay lexical form was not " + VALID_DATE5 +
               " as expected. was:" + gDay.getLexicalForm(),
               gDay.getLexicalForm().equals(VALID_DATE5));

    // Retrieve the byte data of the gDay object
    dayBytes = gDay.getData();

    // Retrieve the long value from the buffer
    dayLong = dayBytes.getLong();

    // Create a date object from the day's long
    dayDate = new Date(dayLong);

    // Format the resulting day
    day = format.format(dayDate);

    // Test the correct value is stored
    assertTrue("GDay byte buffer value was not " + VALID_DATE5 +
               " as expected, was: " + day,
               !westOfGMT ? ("" + day).equals(VALID_DATE5_EAST)
               : ("" + day).equals(VALID_DATE5_WEST) );
  }

  /**
   * Tests invalid xsd:gDay values.
   */
  public void testInvalid() {

    // Create a new factory
    SPGDayFactory factory = new SPGDayFactory();

    // Container for our gDay object
    @SuppressWarnings("unused")
    SPGDayImpl gDay = null;

    // Indicator of whether an exception occurred or not.  Assumed false.
    boolean failed = false;

    try {

      // Create a gDay object by lexical string
      gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.GDAY_URI, INVALID_DATE_1);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gDay with non-numeric characters",
               failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gDay object by lexical string
      gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.GDAY_URI,
          INVALID_DATE_2);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gDay with invalid lexical format.",
               failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gDay object by lexical string
      gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.GDAY_URI,
          INVALID_DATE_3);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gDay with invalid timezone format.",
               failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gDay object by lexical string
      gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.GDAY_URI,
          INVALID_DATE_4);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gDay with invalid date value.",
               failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gDay object by lexical string
      gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.GDAY_URI,
          INVALID_DATE_5);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gDay with preceding '-'.",
               failed);
  }

  /**
   * Tests that gDay objects a compared properly.
   */
  public void testCompare() {

    // Create a new factory
    SPGDayFactory factory = new SPGDayFactory();

    // Create a gDay object by lexical string
    SPGDayImpl gDay = (SPGDayImpl) factory.newSPTypedLiteral(XSD.
        GDAY_URI, VALID_DATE);

    // Create a gDay object that is different
    SPGDayImpl gDay2 = (SPGDayImpl) factory.newSPTypedLiteral(XSD.
        GDAY_URI, "---06");

    // Test that two same objects will be equal
    assertTrue("Same object did not register as equal.", gDay.equals(gDay));

    // Test that two different objects will be inequal
    assertTrue("Different object was unexpectedly found to be equal.",
               !gDay.equals(gDay2));

    // Test that two same objects will compare equally
    assertTrue("Same object did not compare equally.",
               gDay.compareTo(gDay) == 0);

    // Test that two different objects will compare inequally
    assertTrue("Different object was unexpectedly found to compare equally.",
               gDay.compareTo(gDay2) != 0);

    // Obtain the comparator for the first object
    SPComparator comparator = gDay.getSPComparator();

    // Test that two same objects will compare equally by prefix
    assertTrue("Same object did not compare equally by prefix.",
               comparator.comparePrefix(gDay.getData(),
                                        gDay.getData(),
                                        Constants.SIZEOF_INT) == 0);

    // Test that two different objects will compare inequally by prefix
    assertTrue(
        "Different object was unexpectedly found to compare inequally by prefix.",
        comparator.comparePrefix(gDay.getData(),
                                 gDay2.getData(),
                                 Constants.SIZEOF_INT) == 0);

    // Test that two same objects will compare equally by comparator
    assertTrue("Same object did not compare equally by comparator.",
               comparator.compare(gDay.getData(), 0, gDay.getData(), 0) == 0);

    // Test that two different objects will compare inequally by comparator
    assertTrue(
        "Different object was unexpectedly found to compare inequally by comparator.",
        comparator.compare(gDay.getData(), 0, gDay2.getData(), 0) != 0);

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
      fail("Failed to tear down members after testing. " +
           exception.getMessage());
    }
  }
}
