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
import java.nio.ByteBuffer;

// Third party packages
import junit.framework.*;
import org.apache.log4j.*;

import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.*;
import org.mulgara.util.Constants;

/**
 * Unit test for testing an xsd:gMonthDay data type representation.
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
public class SPGMonthDayUnitTest extends TestCase {

  /** Logger */
  private static Logger log = Logger.getLogger(SPGMonthDayUnitTest.class);

  /** Parser for our expected data return values */
  private static SimpleDateFormat format;

  /** Constant valid test date */
  private static final String VALID_DATE = "--04-01";

  /** A valid value for the VALID_DATE in TimeZones west of GMT **/ 
  private static final String VALID_DATE_WEST = "--03-31";  

  /** Constant valid test date (Timezone added) */
  private static final String VALID_DATE2 = "--04-01-04:00";

  /** Constant valid test date (date with UTC timezone) */
  private static final String VALID_DATE3 = "--04-01Z";

  /** A valid value for the VALID_DATE3 in TimeZones west of GMT **/ 
  private static final String VALID_DATE3_WEST = "--03-31Z";

  /** Invalid date 1 (non-numeric month) */
  private static final String INVALID_DATE_1 = "--2g-01";

  /** Invalid date 2 (Valid date, but not gMonthDay format) */
  private static final String INVALID_DATE_2 = "2004-03-24";

  /** Invalid date 3 (Valid date, invalid timezone) */
  private static final String INVALID_DATE_3 = "--04-01+400";

  /** Invalid date 4 (Invalid month) */
  private static final String INVALID_DATE_4 = "--13-01";

  /** Invalid date 5 (Preceding '-') */
  private static final String INVALID_DATE_5 = "---04-01";

  /** Invalid date 6 (Non-numeric day) */
  private static final String INVALID_DATE_6 = "--04-0f";

  /** Invalid date 7 (Invalid day) */
  private static final String INVALID_DATE_7 = "--04-32";

  /** Invalid date 8 (Invalid month format) */
  private static final String INVALID_DATE_8 = "--004-02";

  /** Invalid date 9 (Invalid day format) */
  private static final String INVALID_DATE_9 = "--04-002";

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPGMonthDayUnitTest(String name) {

    super(name);

    // Load the logging configuration
    BasicConfigurator.configure();

//    try {
//      DOMConfigurator.configure(new URL(System.getProperty("log4j.configuration")));
//    } catch (MalformedURLException mue) {
//      log.error("Unable to configure logging service from XML configuration file", mue);
//    }
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    // Set up our parser to format dates to a "MM" (gMonthDay) style format
    format = new SimpleDateFormat("--MM-dd");

    TestSuite suite = new TestSuite();
    suite.addTest(new SPGMonthDayUnitTest("testValid"));
    suite.addTest(new SPGMonthDayUnitTest("testInvalid"));
    suite.addTest(new SPGMonthDayUnitTest("testCompare"));

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
   * Tests that valid xsd:gMonthDay values are accepted and processed correctly.
   */
  public void testValid() {
	// Get a TimeZone instance which will help us interpret the results.
	TimeZone tz = TimeZone.getDefault();
		
	boolean westOfGMT = tz.getRawOffset() < 0;
		
    // Create a new factory
    SPGMonthDayFactory factory = new SPGMonthDayFactory();

    // Create a gMonthDay object by lexical string
    SPGMonthDayImpl gMonthDay = (SPGMonthDayImpl)factory.newSPTypedLiteral(XSD.GMONTHDAY_URI, VALID_DATE);

    // Test that the lexical form of the date is correct
    assertEquals(VALID_DATE, gMonthDay.getLexicalForm());

    // Retrieve the byte data of the gMonthDay object
    ByteBuffer monthDayBytes = gMonthDay.getData();

    // Retrieve the long value from the buffer
    long monthDayLong = monthDayBytes.getLong();

    // Create a date object from the monthDay's long
    Date monthDayDate = new Date(monthDayLong);

    // Format the resulting monthDay
    String monthDay = format.format(monthDayDate);

    // Test the correct value is stored
    assertTrue("GMonthDay byte buffer value was not " + VALID_DATE +
       " as expected, was: " + monthDay, ("" + monthDay).equals(VALID_DATE));

    // Byte buffer to hold our date information
    ByteBuffer buffer = ByteBuffer.wrap(new byte[SPGMonthDayImpl.getBufferSize()]);

    // If the previous step passed then we know the long value is what we want,
    // so store it in our buffer
    buffer.putLong(monthDayLong);
    buffer.put((byte)1);

    // Reset the buffer for reading
    buffer.flip();

    if (log.isDebugEnabled()) {

      log.debug("Creating gMonthDay from byte buffer storing value: " +
                format.format(new Date(monthDayLong)));

      log.debug("Original monthDay long vs. stored long: " + monthDayLong +
                " vs. " +
                buffer.getLong());
      buffer.get();

      // Reset the buffer
      buffer.flip();
    }

    // Create a gMonthDay object by byte buffer
    gMonthDay = (SPGMonthDayImpl) factory.newSPTypedLiteral(0, buffer);

    // Test that the lexical form of the date is correct
    assertTrue("GMonthDay lexical form was not " + VALID_DATE +
               " as expected. was:" + gMonthDay.getLexicalForm(),
               gMonthDay.getLexicalForm().equals(VALID_DATE));

    // Retrieve the byte data of the gMonthDay object
    monthDayBytes = gMonthDay.getData();

    // Retrieve the long value from the buffer
    monthDayLong = monthDayBytes.getLong();

    // Create a date object from the monthDay's long
    monthDayDate = new Date(monthDayLong);

    // Format the resulting monthDay
    monthDay = format.format(monthDayDate);

    // Test the correct value is stored
    assertTrue("GMonthDay byte buffer value was not " + VALID_DATE +
               " as expected, was: " + monthDay,
               ("" + monthDay).equals(VALID_DATE));

    // Create a gMonthDay object by lexical string (testing range acceptance)
    gMonthDay = (SPGMonthDayImpl) factory.newSPTypedLiteral(XSD.GMONTHDAY_URI, VALID_DATE2);

    // Test that the lexical form of the date is correct
    assertTrue("GMonthDay lexical form was not " + VALID_DATE2 +
               " as expected. was:" + gMonthDay.getLexicalForm(),
               gMonthDay.getLexicalForm().equals(VALID_DATE2));

    // Retrieve the byte data of the gMonthDay object
    monthDayBytes = gMonthDay.getData();

    // Retrieve the long value from the buffer
    monthDayLong = monthDayBytes.getLong();

    // Create a date object from the monthDay's long
    monthDayDate = new Date(monthDayLong);

    // Format the resulting monthDay
    monthDay = format.format(monthDayDate);

    // Test the correct value is stored
    assertTrue("GMonthDay byte buffer value was not " + VALID_DATE +
               " as expected, was: " + monthDay,
               !westOfGMT ? ("" + monthDay).equals(VALID_DATE)
            	 : ("" + monthDay).equals(VALID_DATE_WEST) );

    // Create a gMonthDay object by lexical string (testing timezone acceptance)
    gMonthDay = (SPGMonthDayImpl) factory.newSPTypedLiteral(XSD.GMONTHDAY_URI, VALID_DATE3);

    // Test that the lexical form of the date is correct
    assertTrue("GMonthDay lexical form was not " + VALID_DATE3 +
               " as expected. was:" + gMonthDay.getLexicalForm(),
               gMonthDay.getLexicalForm().equals(VALID_DATE3));

    // Retrieve the byte data of the gMonthDay object
    monthDayBytes = gMonthDay.getData();

    // Retrieve the long value from the buffer
    monthDayLong = monthDayBytes.getLong();

    // Create a date object from the monthDay's long
    monthDayDate = new Date(monthDayLong);

    // Format the resulting monthDay
    monthDay = format.format(monthDayDate);

    // Test the correct value is stored
    assertTrue("GMonthDay byte buffer value was not " + VALID_DATE +
               " as expected, was: " + monthDay,
               !westOfGMT ? ("" + monthDay).equals(VALID_DATE)
        	     : ("" + monthDay).equals(VALID_DATE_WEST ) );
  }

  /**
   * Tests invalid xsd:gMonthDay values.
   */
  public void testInvalid() {

    // Create a new factory
    SPGMonthDayFactory factory = new SPGMonthDayFactory();

    // Container for our gMonthDay object
    @SuppressWarnings("unused")
    SPGMonthDayImpl gMonthDay = null;

    // Indicator of whether an exception occurred or not.  Assumed false.
    boolean failed = false;

    try {

      // Create a gMonthDay object by lexical string
      gMonthDay = (SPGMonthDayImpl)factory.newSPTypedLiteral(XSD.GMONTHDAY_URI, INVALID_DATE_1);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonthDay with non-numeric month value.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonthDay object by lexical string
      gMonthDay = (SPGMonthDayImpl)factory.newSPTypedLiteral(XSD.GMONTHDAY_URI, INVALID_DATE_2);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonthDay with invalid lexical format.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonthDay object by lexical string
      gMonthDay = (SPGMonthDayImpl)factory.newSPTypedLiteral(XSD.GMONTHDAY_URI,
          INVALID_DATE_3);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonthDay with invalid timezone format.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonthDay object by lexical string
      gMonthDay = (SPGMonthDayImpl)factory.newSPTypedLiteral(XSD.GMONTHDAY_URI, INVALID_DATE_4);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonthDay with invalid month value.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonthDay object by lexical string
      gMonthDay = (SPGMonthDayImpl) factory.newSPTypedLiteral(XSD.GMONTHDAY_URI,
          INVALID_DATE_5);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonthDay with preceding '-'.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonthDay object by lexical string
      gMonthDay = (SPGMonthDayImpl) factory.newSPTypedLiteral(XSD.GMONTHDAY_URI,
          INVALID_DATE_6);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonthDay with non-numeric day.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonthDay object by lexical string
      gMonthDay = (SPGMonthDayImpl)factory.newSPTypedLiteral(XSD.GMONTHDAY_URI,
          INVALID_DATE_7);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonthDay with invalid day value.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonthDay object by lexical string
      gMonthDay = (SPGMonthDayImpl)factory.newSPTypedLiteral(XSD.GMONTHDAY_URI, INVALID_DATE_8);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonthDay with invalid day lexical value.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonthDay object by lexical string
      gMonthDay = (SPGMonthDayImpl) factory.newSPTypedLiteral(XSD.GMONTHDAY_URI, INVALID_DATE_9);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonthDay with invalid day lexical value.",
               failed);
  }

  /**
   * Tests that gMonthDay objects a compared properly.
   */
  public void testCompare() {

    // Create a new factory
    SPGMonthDayFactory factory = new SPGMonthDayFactory();

    // Create a gMonthDay object by lexical string
    SPGMonthDayImpl gMonthDay = (SPGMonthDayImpl) factory.newSPTypedLiteral(XSD.
        GMONTHDAY_URI, VALID_DATE);

    // Create a gMonthDay object that is different
    SPGMonthDayImpl gMonthDay2 = (SPGMonthDayImpl) factory.newSPTypedLiteral(
        XSD.GMONTHDAY_URI, "--06-01");

    // Test that two same objects will be equal
    assertTrue("Same object did not register as equal.",
               gMonthDay.equals(gMonthDay));

    // Test that two different objects will be inequal
    assertTrue("Different object was unexpectedly found to be equal.",
               !gMonthDay.equals(gMonthDay2));

    // Test that two same objects will compare equally
    assertTrue("Same object did not compare equally.",
               gMonthDay.compareTo(gMonthDay) == 0);

    // Test that two different objects will compare inequally
    assertTrue("Different object was unexpectedly found to compare equally.",
               gMonthDay.compareTo(gMonthDay2) != 0);

    // Obtain the comparator for the first object
    SPComparator comparator = gMonthDay.getSPComparator();

    // Test that two same objects will compare equally by prefix
    assertTrue("Same object did not compare equally by prefix.",
               comparator.comparePrefix(gMonthDay.getData(),
                                        gMonthDay.getData(),
                                        Constants.SIZEOF_INT) == 0);

    // Test that two different objects will compare inequally by prefix
    assertTrue(
        "Different object was unexpectedly found to compare inequally by prefix.",
        comparator.comparePrefix(gMonthDay.getData(),
                                 gMonthDay2.getData(),
                                 Constants.SIZEOF_INT) == 0);

    // Test that two same objects will compare equally by comparator
    assertTrue("Same object did not compare equally by comparator.",
               comparator.compare(gMonthDay.getData(), 0, gMonthDay.getData(), 0) == 0);

    // Test that two different objects will compare inequally by comparator
    assertTrue(
        "Different object was unexpectedly found to compare inequally by comparator.",
        comparator.compare(gMonthDay.getData(), 0, gMonthDay2.getData(), 0) != 0);

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
