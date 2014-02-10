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
 * The Initial Developer of the Original Code is Edwin Shin.
 * Copyright (C) 2005. All Rights Reserved.
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

// Standard Java
import java.util.Date;
import java.text.SimpleDateFormat;
import java.nio.ByteBuffer;

// JUnit
import junit.framework.*;

// Log4j
import org.apache.log4j.*;

// Internal Packages
import org.mulgara.query.rdf.XSD;
import org.mulgara.util.Constants;
import org.mulgara.util.LexicalDateTime;

/**
 * Unit test for testing an xsd:datetime wrapper.
 *
 * @created 2005-02-26
 *
 * @author Edwin Shin
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/03/11 04:15:22 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @copyright &copy;2005
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SPDateTimeUnitTest extends TestCase {

  /** Logger */
  private static Logger log = Logger.getLogger(SPDateTimeUnitTest.class);

  /** Parser for our expected data return values */
  private static SimpleDateFormat format;

  private static final String VALID_XSD_DATETIME = "2005-01-19T20:40:17";

  private static final String VALID_XSD_DATETIME2 = "2005-01-19T20:40:17.001";

  private static final String VALID_XSD_DATETIME3 = "2005-01-19T20:40:17.1";

  private static final String VALID_XSD_DATETIME4 = "123456789-01-19T20:40:17.123";

  private static final String VALID_XSD_DATETIME5 = "-123456789-01-19T20:40:17.123";

  private static final String VALID_XSD_DATETIME6 = "0123-01-19T20:40:17.456";
  private static final String VALID_JAVA_DATETIME6 = VALID_XSD_DATETIME6;

  private static final String INVALID_XSD_DATETIME = "2005-01-19T20:40:17,001";
  private static final String INVALID_XSD_DATETIME2 = "2005-01-19T20:40";
  private static final String INVALID_XSD_DATETIME3 = "2005";
  private static final String INVALID_XSD_DATETIME4 = "-01-19T20:40:17";
  private static final String INVALID_XSD_DATETIME5 = "2005-01-19T20:40:17.";

  /** Constant valid test date (1 CE) */
  private static final String XSD_1CE = "0001-01-01T00:00:00";

  // Not valid in XSD-2
  /** Constant invalid test date (0 CE in XSD-2, 1 BCE and valid in later revisions) */
  private static final String XSD_0CE = "0000-01-01T00:00:00";

  /** Constant valid test date (1 BCE in XSD-2) */
  private static final String XSD_1BCE = "-0001-01-01T00:00:00";

  /** Constant valid test date (2 BCE in XSD-2) */
  private static final String XSD_2BCE = "-0002-01-01T00:00:00";

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPDateTimeUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {
    format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    TestSuite suite = new TestSuite();
    suite.addTest(new SPDateTimeUnitTest("testValid"));
    suite.addTest(new SPDateTimeUnitTest("testInvalid"));
    suite.addTest(new SPDateTimeUnitTest("testCompare"));
    suite.addTest(new SPDateTimeUnitTest("testBoundaryDates"));

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
   * Tests that valid xsd:dateTime values are accepted and processed correctly.
   */
  public void testValid() {
    // Create a new factory
    SPDateTimeFactory factory = new SPDateTimeFactory();

    // Create a dateTime object by lexical string
    SPDateTimeImpl dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.
        DATE_TIME_URI, VALID_XSD_DATETIME);

    // Test that the lexical form of the date is correct
    assertEquals(VALID_XSD_DATETIME, dateTime.getLexicalForm());

    // Retrieve the byte data of the dateTime object
    ByteBuffer dtBytes = dateTime.getData();

    // Retrieve the long value from the buffer
    long dtLong = dtBytes.getLong();

    // Create a date object from the dateTime's long
    Date dtDate = new Date(dtLong);

    // Format the resulting day
    format.format(dtDate);

    // Test the correct value is stored
    assertEquals(VALID_XSD_DATETIME, getDateString(dateTime));

    // Byte buffer to hold our date information
    ByteBuffer buffer = ByteBuffer.wrap(new byte[Constants.SIZEOF_LONG + Constants.SIZEOF_INT]);

    // If the previous step passed then we know the long value is what we want,
    // so store it in our buffer
    buffer.putLong(dtLong);
    buffer.put((byte)0x02);  // The value of the "local" flag
    buffer.put((byte)0);

    // Reset the buffer for reading
    buffer.flip();

    if (log.isDebugEnabled()) {

      log.debug("Creating dateTime from byte buffer storing value: " +
          format.format(new Date(dtLong)));

      log.debug("Original dateTime long vs. stored long: " + dtLong + " vs. " +
          buffer.getLong());
      log.debug("Stored timezone code: " + buffer.get());
      log.debug("Stored decimal places: " + buffer.get());

      // Reset the buffer
      buffer.flip();
    }

    // Create a dateTime object by byte buffer
    dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(0, buffer);

    // Test that the lexical form of the date is correct
    assertEquals(VALID_XSD_DATETIME, dateTime.getLexicalForm());

    // Test the correct value is stored
    assertEquals(VALID_XSD_DATETIME, getDateString(dateTime));

    // Create a dateTime object by lexical string
    dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI,
        VALID_XSD_DATETIME2);

    // Test that the lexical form of the date is correct
    assertEquals(VALID_XSD_DATETIME2, dateTime.getLexicalForm());

    // Test the correct value is stored
    assertEquals(VALID_XSD_DATETIME2, getDateString(dateTime));

    // Create a dateTime object by lexical string
    dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI,
        VALID_XSD_DATETIME3);

    // Test that the lexical form of the date is correct
    assertEquals(VALID_XSD_DATETIME3, dateTime.getLexicalForm());

    // Test the correct value is stored
    assertEquals(VALID_XSD_DATETIME3, getDateString(dateTime));

    // Create a dateTime object by lexical string
    dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI,
        VALID_XSD_DATETIME4);

    // Test that the lexical form of the date is correct
    assertEquals(VALID_XSD_DATETIME4, dateTime.getLexicalForm());

    // Test the correct value is stored
    assertEquals(VALID_XSD_DATETIME4, getDateString(dateTime));

    // Create a dateTime object by lexical string
    dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI,
        VALID_XSD_DATETIME5);

    // Test that the lexical form of the date is correct
    assertEquals(VALID_XSD_DATETIME5, dateTime.getLexicalForm());

    // Test the correct value is stored
    assertEquals(VALID_XSD_DATETIME5, getDateString(dateTime));

    // Create a dateTime object by lexical string
    dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI,
        VALID_XSD_DATETIME6);

    // Test that the lexical form of the date is correct
    assertEquals(VALID_XSD_DATETIME6, dateTime.getLexicalForm());

    // Test the correct value is stored
    assertEquals(VALID_JAVA_DATETIME6, getDateString(dateTime));
  }

  /**
   * Tests invalid xsd:dateTime values.
   */
  public void testInvalid() {
// Create a new factory
    SPDateTimeFactory factory = new SPDateTimeFactory();

// Create a dateTime object by lexical string
    try {
      SPDateTimeImpl dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.
          DATE_TIME_URI, INVALID_XSD_DATETIME);
      fail("Successfully parsed an invalid date: " + INVALID_XSD_DATETIME + " -> " + dateTime);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }

    try {
      SPDateTimeImpl dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.
          DATE_TIME_URI, INVALID_XSD_DATETIME2);
      fail("Successfully parsed an invalid date: " + INVALID_XSD_DATETIME2 + " -> " + dateTime);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }

    try {
      SPDateTimeImpl dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.
          DATE_TIME_URI, INVALID_XSD_DATETIME3);
      fail("Successfully parsed an invalid date: " + INVALID_XSD_DATETIME3 + " -> " + dateTime);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }

    try {
      SPDateTimeImpl dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.
          DATE_TIME_URI, INVALID_XSD_DATETIME4);
      fail("Successfully parsed an invalid date: " + INVALID_XSD_DATETIME4 + " -> " + dateTime);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }

    try {
      SPDateTimeImpl dateTime = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.
          DATE_TIME_URI, INVALID_XSD_DATETIME5);
      fail("Successfully parsed an invalid date: " + INVALID_XSD_DATETIME5 + " -> " + dateTime);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }

  }

  public void testCompare() throws Exception {
    // Create a new factory
    SPDateTimeFactory factory = new SPDateTimeFactory();

    SPDateTimeImpl t1, t2, t3;

    t1 = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI,
        VALID_XSD_DATETIME);
    t2 = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI,
        VALID_XSD_DATETIME2);
    t3 = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI,
        VALID_XSD_DATETIME3);

    assertTrue(t1.compareTo(t1) == 0);
    assertTrue(t1.compareTo(t2) == -1);
    assertTrue(t1.compareTo(t3) == -1);

    assertTrue(t2.compareTo(t1) == 1);
    assertTrue(t2.compareTo(t2) == 0);
    assertTrue(t2.compareTo(t3) == -1);

    assertTrue(t3.compareTo(t1) == 1);
    assertTrue(t3.compareTo(t2) == 1);
    assertTrue(t3.compareTo(t3) == 0);
  }

  public void testBoundaryDates() throws Exception {
    // Create a new factory
    SPDateTimeFactory factory = new SPDateTimeFactory();
    SPDateTimeImpl oneCE, oneBCE, twoBCE, invalidBCE;

    // Test 1 CE
    oneCE = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.
        DATE_TIME_URI, XSD_1CE);
    assertEquals(XSD_1CE, oneCE.getLexicalForm());

    // Test 0 CE
    try {
      invalidBCE = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI, XSD_0CE);
      fail("Year 0000 incorrectly accepted: " + invalidBCE.toString());
    } catch (Throwable t) {
    }

    // Test 1 BCE
    oneBCE = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI, XSD_1BCE);

    // Test that the lexical form of the date is correct
    assertEquals(XSD_1BCE, oneBCE.getLexicalForm());

    // Test 2 BCE
    twoBCE = (SPDateTimeImpl) factory.newSPTypedLiteral(XSD.DATE_TIME_URI, XSD_2BCE);

    // Test that the lexical form of the date is correct
    assertEquals(XSD_2BCE, twoBCE.getLexicalForm());

    // Compare
    assertTrue(oneCE.compareTo(oneCE) == 0);
    assertTrue(oneCE.compareTo(oneBCE) == 1);
    assertTrue(oneCE.compareTo(twoBCE) == 1);
    assertTrue(oneBCE.compareTo(oneBCE) == 0);
    assertTrue(oneBCE.compareTo(oneCE) == -1);
    assertTrue(oneBCE.compareTo(twoBCE) == 1);
    assertTrue(twoBCE.compareTo(twoBCE) == 0);
    assertTrue(twoBCE.compareTo(oneCE) == -1);
    assertTrue(twoBCE.compareTo(oneBCE) == -1);
  }

  private Date getDate(SPDateTimeImpl dateTime) {
    return new Date(dateTime.getData().getLong());
  }

  private String getDateString(SPDateTimeImpl dateTime) {
    return new LexicalDateTime(getDate(dateTime).getTime()).toString();
  }



  /**
   * Initialise members.
   *
   * @throws Exception if something goes wrong
   */
  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void tearDown() throws Exception {

    super.tearDown();
  }
}
