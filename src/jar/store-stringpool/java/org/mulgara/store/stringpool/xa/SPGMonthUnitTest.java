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
 * Unit test for testing an xsd:gMonth data type representation.
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
public class SPGMonthUnitTest extends TestCase {

  /** Logger */
  private static Logger log = Logger.getLogger(SPGMonthUnitTest.class);

  /** Parser for our expected data return values */
  private static SimpleDateFormat format;

  /** Constant valid test date */
  private static final String VALID_DATE = "--04";

  /** Constant valid test date, west of UTC */
  private static final String VALID_DATE_WEST = "--03";

  /** Constant valid test date (Timezone added) */
  private static final String VALID_DATE2 = "--04-04:00";

  /** Constant valid test date (date with UTC timezone) */
  private static final String VALID_DATE3 = "--04Z";

  /** Constant valid test date (Different format for same value) */
  private static final String VALID_DATE4 = "--04--";

  /** Invalid date 1 (non-numeric characters) */
  private static final String INVALID_DATE_1 = "--2g";

  /** Invalid date 2 (Valid date, but not gMonth format) */
  private static final String INVALID_DATE_2 = "2004-03-24";

  /** Invalid date 3 (Valid date, invalid timezone) */
  private static final String INVALID_DATE_3 = "--04+400";

  /** Invalid date 4 (Invalid date) */
  private static final String INVALID_DATE_4 = "--13";

  /** Invalid date 5 (Preceding '-') */
  private static final String INVALID_DATE_5 = "---04";

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPGMonthUnitTest(String name) {

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

    // Set up our parser to format dates to a "MM" (gMonth) style format
    format = new SimpleDateFormat("--MM");

    TestSuite suite = new TestSuite();
    suite.addTest(new SPGMonthUnitTest("testValid"));
    suite.addTest(new SPGMonthUnitTest("testInvalid"));
    suite.addTest(new SPGMonthUnitTest("testCompare"));

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
   * Tests that valid xsd:gMonth values are accepted and processed correctly.
   */
  public void testValid() {

    // Create a new factory
    SPGMonthFactory factory = new SPGMonthFactory();

    // Create a gMonth object by lexical string
    SPGMonthImpl gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.
        GMONTH_URI, VALID_DATE);

    // Test that the lexical form of the date is correct
    assertTrue("GMonth lexical form was not " + VALID_DATE +
               " as expected. was:" + gMonth.getLexicalForm(),
               gMonth.getLexicalForm().equals(VALID_DATE));

    // Retrieve the byte data of the gMonth object
    ByteBuffer monthBytes = gMonth.getData();

    // Retrieve the long value from the buffer
    long monthLong = monthBytes.getLong();

    // Create a date object from the month's long
    Date monthDate = new Date(monthLong);

    // Format the resulting month
    String month = format.format(monthDate);

    // Test the correct value is stored
    assertTrue("GMonth byte buffer value was not " + VALID_DATE +
               " as expected, was: " + month,
               ("" + month).equals(VALID_DATE));

    // Byte buffer to hold our date information
    ByteBuffer buffer = ByteBuffer.wrap(new byte[SPGMonthImpl.getBufferSize()]);

    // If the previous step passed then we know the long value is what we want,
    // so store it in our buffer
    buffer.putLong(monthLong);
    buffer.put((byte)1);

    // Reset the buffer for reading
    buffer.flip();

    if (log.isDebugEnabled()) {

      log.debug("Creating gMonth from byte buffer storing value: " +
                format.format(new Date(monthLong)));

      log.debug("Original month long vs. stored long: " + monthLong + " vs. " +
                buffer.getLong());
      buffer.get();

      // Reset the buffer
      buffer.flip();
    }

    // Create a gMonth object by byte buffer
    gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(0, buffer);

    // Test that the lexical form of the date is correct
    assertTrue("GMonth lexical form was not " + VALID_DATE +
               " as expected. was:" +
               gMonth.getLexicalForm(),
               gMonth.getLexicalForm().equals(VALID_DATE));

    // Retrieve the byte data of the gMonth object
    monthBytes = gMonth.getData();

    // Retrieve the long value from the buffer
    monthLong = monthBytes.getLong();

    // Create a date object from the month's long
    monthDate = new Date(monthLong);

    // Format the resulting month
    month = format.format(monthDate);

    // Test the correct value is stored
    assertTrue("GMonth byte buffer value was not " + VALID_DATE +
               " as expected, was: " + month,
               ("" + month).equals(VALID_DATE));

    // Create a gMonth object by lexical string (testing range acceptance)
    gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.GMONTH_URI, VALID_DATE2);

    // Test that the lexical form of the date is correct
    assertTrue("GMonth lexical form was not " + VALID_DATE2 +
               " as expected. was:" + gMonth.getLexicalForm(),
               gMonth.getLexicalForm().equals(VALID_DATE2));

    // Retrieve the byte data of the gMonth object
    monthBytes = gMonth.getData();

    // Retrieve the long value from the buffer
    monthLong = monthBytes.getLong();

    // Create a date object from the month's long
    monthDate = new Date(monthLong);

    // Format the resulting month
    month = format.format(monthDate);

    boolean westOfGMT = TimeZone.getDefault().getRawOffset() < 0;

    // Test the correct value is stored
    assertTrue("GMonth byte buffer value was not " + VALID_DATE +
               " as expected, was: " + month,
              !westOfGMT ? ("" + month).equals(VALID_DATE)
                  : ("" + month).equals(VALID_DATE_WEST) );

    // Create a gMonth object by lexical string (testing timezone acceptance)
    gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.GMONTH_URI,
        VALID_DATE3);

    // Test that the lexical form of the date is correct
    assertTrue("GMonth lexical form was not " + VALID_DATE3 +
               " as expected. was:" + gMonth.getLexicalForm(),
               gMonth.getLexicalForm().equals(VALID_DATE3));

    // Retrieve the byte data of the gMonth object
    monthBytes = gMonth.getData();

    // Retrieve the long value from the buffer
    monthLong = monthBytes.getLong();

    // Create a date object from the month's long
    monthDate = new Date(monthLong);

    // Format the resulting month
    month = format.format(monthDate);

    // Test the correct value is stored
    assertTrue("GMonth byte buffer value was not " + VALID_DATE +
               " as expected, was: " + month,
               !westOfGMT ? ("" + month).equals(VALID_DATE)
                   : ("" + month).equals(VALID_DATE_WEST) );

    // Create a gMonth object by lexical string (testing alternate format)
    gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.GMONTH_URI, VALID_DATE4);

    // Test that the lexical form of the date is correct
    assertTrue("GMonth lexical form was not " + VALID_DATE +
               " as expected. was:" + gMonth.getLexicalForm(),
               gMonth.getLexicalForm().equals(VALID_DATE));

    // Retrieve the byte data of the gMonth object
    monthBytes = gMonth.getData();

    // Retrieve the long value from the buffer
    monthLong = monthBytes.getLong();

    // Create a date object from the month's long
    monthDate = new Date(monthLong);

    // Format the resulting month
    month = format.format(monthDate);

    // Test the correct value is stored
    assertTrue("GMonth byte buffer value was not " + VALID_DATE +
               " as expected, was: " + month,
               ("" + month).equals(VALID_DATE));

  }

  /**
   * Tests invalid xsd:gMonth values.
   */
  public void testInvalid() {

    // Create a new factory
    SPGMonthFactory factory = new SPGMonthFactory();

    // Container for our gMonth object
    @SuppressWarnings("unused")
    SPGMonthImpl gMonth = null;

    // Indicator of whether an exception occurred or not.  Assumed false.
    boolean failed = false;

    try {

      // Create a gMonth object by lexical string
      gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.GMONTH_URI, INVALID_DATE_1);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonth with non-numeric characters", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonth object by lexical string
      gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.GMONTH_URI, INVALID_DATE_2);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonth with invalid lexical format.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonth object by lexical string
      gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.GMONTH_URI, INVALID_DATE_3);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonth with invalid timezone format.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonth object by lexical string
      gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.GMONTH_URI, INVALID_DATE_4);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonth with invalid date value.", failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gMonth object by lexical string
      gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.GMONTH_URI, INVALID_DATE_5);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gMonth with preceding '-'.", failed);
  }

  /**
   * Tests that gMonth objects a compared properly.
   */
  public void testCompare() {

    // Create a new factory
    SPGMonthFactory factory = new SPGMonthFactory();

    // Create a gMonth object by lexical string
    SPGMonthImpl gMonth = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.GMONTH_URI, VALID_DATE);

    // Create a gMonth object that is different
    SPGMonthImpl gMonth2 = (SPGMonthImpl) factory.newSPTypedLiteral(XSD.GMONTH_URI,"--06");

    // Test that two same objects will be equal
    assertTrue("Same object did not register as equal.", gMonth.equals(gMonth));

    // Test that two different objects will be inequal
    assertTrue("Different object was unexpectedly found to be equal.",
               !gMonth.equals(gMonth2));

    // Test that two same objects will compare equally
    assertTrue("Same object did not compare equally.",
               gMonth.compareTo(gMonth) == 0);

    // Test that two different objects will compare inequally
    assertTrue("Different object was unexpectedly found to compare equally.",
               gMonth.compareTo(gMonth2) != 0);

    // Obtain the comparator for the first object
    SPComparator comparator = gMonth.getSPComparator();

    // Test that two same objects will compare equally by prefix
    assertTrue("Same object did not compare equally by prefix.",
               comparator.comparePrefix(gMonth.getData(),
                                        gMonth.getData(),
                                        Constants.SIZEOF_INT) == 0);

    // Test that two different objects will compare inequally by prefix
    assertTrue(
        "Different object was unexpectedly found to compare inequally by prefix.",
        comparator.comparePrefix(gMonth.getData(),
                                 gMonth2.getData(),
                                 Constants.SIZEOF_INT) == 0);

    // Test that two same objects will compare equally by comparator
    assertTrue("Same object did not compare equally by comparator.",
               comparator.compare(gMonth.getData(), 0, gMonth.getData(), 0) == 0);

    // Test that two different objects will compare inequally by comparator
    assertTrue(
        "Different object was unexpectedly found to compare inequally by comparator.",
        comparator.compare(gMonth.getData(), 0, gMonth2.getData(), 0) != 0);

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
