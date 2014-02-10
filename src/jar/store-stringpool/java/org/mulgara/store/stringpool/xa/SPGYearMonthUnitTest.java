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
 * Unit test for testing an xsd:gYearMonth data type representation.
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
public class SPGYearMonthUnitTest extends TestCase {

  /** Logger */
  private static Logger log = Logger.getLogger(SPGYearMonthUnitTest.class);

  /** Parser for our expected data return values */
  private static SimpleDateFormat format;

  /** Constant valid test date */
  private static final String VALID_DATE = "2004-01";

  /** Constant valid test date (date outside of 0001 - 9999) */
  private static final String VALID_DATE2 = "200400-01";

  /** Constant valid test date (date with timezone) */
  private static final String VALID_DATE3 = "2004-01-04:00";

  /** Constant valid test date (date with timezone) */
  private static final String VALID_DATE4 = "2004-01Z";

  /** Constant valid test date (date with timezone) */
  private static final String VALID_DATE4_WEST = "2003-12";

  /** Invalid date 1 (non-numeric characters) */
  private static final String INVALID_DATE_1 = "2004g0-01";

  /** Invalid date 2 (Valid date, but not gYearMonth format) */
  private static final String INVALID_DATE_2 = "2004-03-24";

  /** Invalid date 3 (Valid date, but not gYearMonth format) */
  private static final String INVALID_DATE_3 = "2004-01+400";

  /** Invalid date 4 (non-numeric characters) */
  private static final String INVALID_DATE_4 = "2004-0g";

  /** Invalid date 5 (Invalid month) */
  private static final String INVALID_DATE_5 = "2004-13";

  /** Invalid date 6 (invalid format) */
  private static final String INVALID_DATE_6 = "2004-001";

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPGYearMonthUnitTest(String name) {

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

    // Set up our parser to format dates to a "CCYY-MM" (gYearMonth) style format
    format = new SimpleDateFormat("yyyy-MM");

    TestSuite suite = new TestSuite();
    suite.addTest(new SPGYearMonthUnitTest("testValid"));
    suite.addTest(new SPGYearMonthUnitTest("testInvalid"));
    suite.addTest(new SPGYearMonthUnitTest("testCompare"));

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
   * Tests that valid xsd:gYearMonth values are accepted and processed correctly.
   */
  public void testValid() {

    // Create a new factory
    SPGYearMonthFactory factory = new SPGYearMonthFactory();

    // Create a gYearMonth object by lexical string
    SPGYearMonthImpl gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(
        XSD.GYEARMONTH_URI,
        VALID_DATE);

    // Test that the lexical form of the date is correct
    assertTrue("GYearMonth lexical form was not " + VALID_DATE +
               " as expected. was:" +
               gYearMonth.getLexicalForm(),
               gYearMonth.getLexicalForm().equals(VALID_DATE));

    // Retrieve the byte data of the gYearMonth object
    ByteBuffer yearMonthBytes = gYearMonth.getData();

    // Retrieve the long value from the buffer
    long yearMonthLong = yearMonthBytes.getLong();

    // Create a date object from the yearMonth's long
    Date yearMonthDate = new Date(yearMonthLong);

    // Format the resulting yearMonth
    String yearMonth = format.format(yearMonthDate);

    // Test the correct value is stored
    assertTrue("GYearMonth byte buffer value was not " + VALID_DATE +
               " as expected, was: " + yearMonth,
               ("" + yearMonth).equals(VALID_DATE));

    // Byte buffer to hold our date information
    ByteBuffer buffer = ByteBuffer.wrap(new byte[SPGYearMonthImpl.getBufferSize()]);

    // If the previous step passed then we know the long value is what we want,
    // so store it in our buffer
    buffer.putLong(yearMonthLong);
    buffer.put((byte)1);

    // Reset the buffer for reading
    buffer.flip();

    if (log.isDebugEnabled()) {

      log.debug("Creating gYearMonth from byte buffer storing value: " +
                format.format(new Date(yearMonthLong)));

      log.debug("Original yearMonth long vs. stored long: " + yearMonthLong +
                " vs. " +
                buffer.getLong());
      buffer.get();

      // Reset the buffer
      buffer.flip();
    }

    // Create a gYearMonth object by byte buffer
    gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(0, buffer);

    // Test that the lexical form of the date is correct
    assertTrue("GYearMonth lexical form was not " + VALID_DATE +
               " as expected. was:" +
               gYearMonth.getLexicalForm(),
               gYearMonth.getLexicalForm().equals(VALID_DATE));

    // Retrieve the byte data of the gYearMonth object
    yearMonthBytes = gYearMonth.getData();

    // Retrieve the long value from the buffer
    yearMonthLong = yearMonthBytes.getLong();

    // Create a date object from the yearMonth's long
    yearMonthDate = new Date(yearMonthLong);

    // Format the resulting yearMonth
    yearMonth = format.format(yearMonthDate);

    // Test the correct value is stored
    assertTrue("GYearMonth byte buffer value was not " + VALID_DATE +
               " as expected, was: " + yearMonth,
               ("" + yearMonth).equals(VALID_DATE));

    // Create a gYearMonth object by lexical string (testing range acceptance)
    gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(XSD.
        GYEARMONTH_URI, VALID_DATE2);

    // Test that the lexical form of the date is correct
    assertTrue("GYearMonth lexical form was not " + VALID_DATE2 +
               " as expected. was:" +
               gYearMonth.getLexicalForm(),
               gYearMonth.getLexicalForm().equals(VALID_DATE2));

    // Retrieve the byte data of the gYearMonth object
    yearMonthBytes = gYearMonth.getData();

    // Retrieve the long value from the buffer
    yearMonthLong = yearMonthBytes.getLong();

    // Create a date object from the yearMonth's long
    yearMonthDate = new Date(yearMonthLong);

    // Format the resulting yearMonth
    yearMonth = format.format(yearMonthDate);

    // Test the correct value is stored
    assertTrue("GYearMonth byte buffer value was not " + VALID_DATE2 +
               " as expected, was: " + yearMonth,
               ("" + yearMonth).equals(VALID_DATE2));

    // Create a gYearMonth object by lexical string (testing timezone acceptance)
    gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(XSD.
        GYEARMONTH_URI, VALID_DATE3);

    // Test that the lexical form of the date is correct
    assertTrue("GYearMonth lexical form was not " + VALID_DATE3 +
               " as expected. was:" + gYearMonth.getLexicalForm(),
               gYearMonth.getLexicalForm().equals(VALID_DATE3));

    // Create a gYearMonth object by lexical string (testing 'Z' acceptance)
    gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(XSD.
        GYEARMONTH_URI, VALID_DATE4);

    // Test that the lexical form of the date is correct
    assertTrue("GYearMonth lexical form was not " + VALID_DATE4 +
               " as expected. was:" +
               gYearMonth.getLexicalForm(),
               gYearMonth.getLexicalForm().equals(VALID_DATE4));

    // Retrieve the byte data of the gYearMonth object
    yearMonthBytes = gYearMonth.getData();

    // Retrieve the long value from the buffer
    yearMonthLong = yearMonthBytes.getLong();

    // Create a date object from the yearMonth's long
    yearMonthDate = new Date(yearMonthLong);

    // Format the resulting yearMonth
    yearMonth = format.format(yearMonthDate);

    boolean westOfGMT = TimeZone.getDefault().getRawOffset() < 0;

    // Test the correct value is stored
    assertTrue("GYearMonth byte buffer value was not " + VALID_DATE +
               " as expected, was: " + yearMonth,
               !westOfGMT ? ("" + yearMonth).equals(VALID_DATE)
                   : ("" + yearMonth).equals(VALID_DATE4_WEST));
  }

  /**
   * Tests invalid xsd:gYearMonth values.
   */
  public void testInvalid() {

    // Create a new factory
    SPGYearMonthFactory factory = new SPGYearMonthFactory();

    // Container for our gYearMonth object
    @SuppressWarnings("unused")
    SPGYearMonthImpl gYearMonth = null;

    // Indicator of whether an exception occurred or not.  Assumed false.
    boolean failed = false;

    try {

      // Create a gYearMonth object by lexical string
      gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(XSD.
          GYEARMONTH_URI, INVALID_DATE_1);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gYearMonth with non-numeric year value.",
               failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gYearMonth object by lexical string
      gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(XSD.
          GYEARMONTH_URI, INVALID_DATE_2);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue("Unexpectedly created a gYearMonth with invalid lexical format.",
               failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gYearMonth object by lexical string
      gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(XSD.
          GYEARMONTH_URI, INVALID_DATE_3);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue(
        "Unexpectedly created a gYearMonth with invalid timezone format.",
        failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gYearMonth object by lexical string
      gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(XSD.
          GYEARMONTH_URI, INVALID_DATE_4);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue(
        "Unexpectedly created a gYearMonth with non-numeric month value.",
        failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gYearMonth object by lexical string
      gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(XSD.
          GYEARMONTH_URI, INVALID_DATE_5);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue(
        "Unexpectedly created a gYearMonth with invalid month value.",
        failed);

    // Reset the failure flag
    failed = false;

    try {

      // Create a gYearMonth object by lexical string
      gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(XSD.
          GYEARMONTH_URI, INVALID_DATE_6);
    } catch (IllegalArgumentException illegalArgumentException) {

      // We are expecting an exception so indicate it with the flag
      failed = true;
    }

    // Test that we failed to create the object
    assertTrue(
        "Unexpectedly created a gYearMonth with invalid lexical month format.",
        failed);

  }

  /**
   * Tests that gYearMonth objects a compared properly.
   */
  public void testCompare() {

    // Create a new factory
    SPGYearMonthFactory factory = new SPGYearMonthFactory();

    // Create a gYearMonth object by lexical string
    SPGYearMonthImpl gYearMonth = (SPGYearMonthImpl) factory.newSPTypedLiteral(
        XSD.GYEARMONTH_URI,
        VALID_DATE);

    // Create a gYearMonth object that is different
    SPGYearMonthImpl gYearMonth2 = (SPGYearMonthImpl) factory.newSPTypedLiteral(
        XSD.GYEARMONTH_URI,
        VALID_DATE2);

    // Test that two same objects will be equal
    assertTrue("Same object did not register as equal.",
               gYearMonth.equals(gYearMonth));

    // Test that two different objects will be inequal
    assertTrue("Different object was unexpectedly found to be equal.",
               !gYearMonth.equals(gYearMonth2));

    // Test that two same objects will compare equally
    assertTrue("Same object did not compare equally.",
               gYearMonth.compareTo(gYearMonth) == 0);

    // Test that two different objects will compare inequally
    assertTrue("Different object was unexpectedly found to compare equally.",
               gYearMonth.compareTo(gYearMonth2) != 0);

    // Obtain the comparator for the first object
    SPComparator comparator = gYearMonth.getSPComparator();

    // Test that two same objects will compare equally by prefix
    assertTrue("Same object did not compare equally by prefix.",
               comparator.comparePrefix(gYearMonth.getData(),
                                        gYearMonth.getData(),
                                        Constants.SIZEOF_INT) == 0);

    // Test that two different objects will compare inequally by prefix
    assertTrue(
        "Different object was unexpectedly found to compare inequally by prefix.",
        comparator.comparePrefix(gYearMonth.getData(),
                                 gYearMonth2.getData(),
                                 Constants.SIZEOF_INT) == 0);

    // Test that two same objects will compare equally by comparator
    assertTrue("Same object did not compare equally by comparator.",
               comparator.compare(gYearMonth.getData(), 0, gYearMonth.getData(), 0) == 0);

    // Test that two different objects will compare inequally by comparator
    assertTrue(
        "Different object was unexpectedly found to compare inequally by comparator.",
        comparator.compare(gYearMonth.getData(), 0, gYearMonth2.getData(), 0) != 0);

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
