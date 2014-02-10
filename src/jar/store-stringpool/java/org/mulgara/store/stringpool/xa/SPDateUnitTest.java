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

// Internal Packages
import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.SPComparator;
import org.mulgara.util.Constants;

/**
 * Unit test for testing an xsd:date wrapper.Unit test for testing an xsd:date wrapper.
 *
 * @created Jul 15, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SPDateUnitTest extends TestCase {

  /** Parser for our expected data return values */
  private static SimpleDateFormat format;

  private static final String VALID_XSD_DATE1 = "2005-01-19";

  private static final String VALID_XSD_DATE2 = "2005-01-19+00:00";

  private static final String VALID_XSD_DATE3 = "2005-01-19+23:59";

  private static final String VALID_XSD_DATE4 = "2005-01-19-01:00";

  private static final String VALID_XSD_DATE5 = "2005-01-19-23:59";

  private static final String VALID_XSD_DATE6 = "9999-01-19";

  private static final String VALID_XSD_DATE7 = "-9999-01-19";

  private static final String VALID_XSD_DATE8 = "0123-01-19";

  private static final String INVALID_XSD_DATE = "2005-01-19+20";
  private static final String INVALID_XSD_DATE2 = "2005-01-19T20:40";
  private static final String INVALID_XSD_DATE3 = "2005";
  private static final String INVALID_XSD_DATE4 = "2005+00:00";
  private static final String INVALID_XSD_DATE5 = "-01-19T20:40:17";
  private static final String INVALID_XSD_DATE6 = "-01-19+20:40";
  private static final String INVALID_XSD_DATE7 = "2005-01-19+20:40:17.";
  private static final String INVALID_XSD_DATE8 = "2005-01-19+20:40.";

  private static final String XSD_1CE = "0001-01-01";

  // Not valid in XSD-2
  /** Constant invalid test date (0 CE in XSD-2, 1 BCE and valid in later revisions) */
  private static final String XSD_0CE = "0000-01-01";

  /** Constant valid test date (1 BCE in XSD-2) */
  private static final String XSD_1BCE = "-0001-01-01";

  /** Constant valid test date (2 BCE in XSD-2) */
  private static final String XSD_2BCE = "-0002-01-01";

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPDateUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {
    format = new SimpleDateFormat("yyyy-MM-dd");

    TestSuite suite = new TestSuite();
    suite.addTest(new SPDateUnitTest("testValid"));
    suite.addTest(new SPDateUnitTest("testInvalid"));
    suite.addTest(new SPDateUnitTest("testCompare"));
    suite.addTest(new SPDateUnitTest("testAvlCompare"));
    suite.addTest(new SPDateUnitTest("testBoundaryDates"));

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
  public void testValid() throws Exception {
    // Create a new factory
    SPDateFactory factory = new SPDateFactory();

    validTest(VALID_XSD_DATE1, factory);
    validTest(VALID_XSD_DATE2, factory);
    validTest(VALID_XSD_DATE3, factory);
    validTest(VALID_XSD_DATE4, factory);
    validTest(VALID_XSD_DATE5, factory);
    validTest(VALID_XSD_DATE6, factory);
    validTest(VALID_XSD_DATE7, factory);
    validTest(VALID_XSD_DATE8, factory);

    // Create a dateTime object by lexical string
    SPDateImpl date = (SPDateImpl)factory.newSPTypedLiteral(XSD.DATE_URI, VALID_XSD_DATE1);

    // Retrieve the byte data of the dateTime object
    ByteBuffer dtBytes = date.getData();

    long dtLong = dtBytes.getLong();
    Date dtDate = new Date(dtLong);
    format.format(dtDate);

    // Test the correct value is stored
    assertEquals(VALID_XSD_DATE1, getDateString(dtDate));

    // Byte buffer to hold our date information
    ByteBuffer buffer = ByteBuffer.wrap(new byte[Constants.SIZEOF_LONG + Constants.SIZEOF_INT]);

    // If the previous step passed then we know the long value is what we want,
    // so store it in our buffer
    buffer.putLong(dtLong);
    buffer.putInt(Integer.MIN_VALUE);

    // Reset the buffer for reading
    buffer.flip();

    // Create a dateTime object by byte buffer
    date = (SPDateImpl)factory.newSPTypedLiteral(0, buffer);

    // Test that the lexical form of the date is correct
    assertEquals(VALID_XSD_DATE1, date.getLexicalForm());

  }

  private static String getDateString(Date date) {
    java.text.DateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
    f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    return f.format(date);
  }

  private void validTest(String date, SPDateFactory factory) throws Exception {
    // Create a dateTime object by lexical string
    SPDateImpl d = (SPDateImpl)factory.newSPTypedLiteral(XSD.DATE_URI, date);

    // Test that the lexical form of the date is correct
    assertEquals(date, d.getLexicalForm());

    // Retrieve the byte data of the dateTime object
    ByteBuffer dtBytes = d.getData();

    // Retrieve the long and in values from the buffer
    long dLong = dtBytes.getLong();
    int tzInt = dtBytes.getInt();

    // Create a date object from the dateTime's long
    SPDateImpl dDate = new SPDateImpl(dLong, tzInt);

    assertEquals(d, dDate);

    dtBytes.rewind();
    SPDateImpl rebuilt = new SPDateImpl(dtBytes);
    assertEquals(d, rebuilt);
  }

  /**
   * Tests invalid xsd:dateTime values.
   */
  public void testInvalid() throws Exception {
    // Create a new factory
    SPDateFactory factory = new SPDateFactory();

    invalidTest(INVALID_XSD_DATE, factory);
    invalidTest(INVALID_XSD_DATE2, factory);
    invalidTest(INVALID_XSD_DATE3, factory);
    invalidTest(INVALID_XSD_DATE4, factory);
    invalidTest(INVALID_XSD_DATE5, factory);
    invalidTest(INVALID_XSD_DATE6, factory);
    invalidTest(INVALID_XSD_DATE7, factory);
    invalidTest(INVALID_XSD_DATE8, factory);
  }

  private void invalidTest(String date, SPDateFactory factory) throws Exception {
    try {
      SPDateImpl d = (SPDateImpl) factory.newSPTypedLiteral(XSD.DATE_URI, date);
      fail("Successfully parsed an invalid date: " + date + " -> " + d);
    } catch (IllegalArgumentException e) {
    }
  }

  public void testCompare() throws Exception {
    // Create a new factory
    SPDateFactory factory = new SPDateFactory();

    // 3 < 1 < 4
    SPDateImpl t1, t2, t3;

    t1 = (SPDateImpl) factory.newSPTypedLiteral(XSD.DATE_URI, VALID_XSD_DATE3);
    t2 = (SPDateImpl) factory.newSPTypedLiteral(XSD.DATE_URI, VALID_XSD_DATE1);
    t3 = (SPDateImpl) factory.newSPTypedLiteral(XSD.DATE_URI, VALID_XSD_DATE4);

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

  public void testAvlCompare() throws Exception {
    // Create a new factory
    SPDateFactory factory = new SPDateFactory();

    SPDateImpl t1, t2;

    t1 = (SPDateImpl) factory.newSPTypedLiteral(XSD.DATE_URI, "2006-08-23+00:00");
    t2 = (SPDateImpl) factory.newSPTypedLiteral(XSD.DATE_URI, "2006-08-23");

    assertEquals(0, t1.compareTo(t2));

    SPComparator comparator = SPDateImpl.SPDateComparator.getInstance();
    assertEquals(1, comparator.compare(t1.getData(), 0, t2.getData(), 0));
    assertEquals(-1, comparator.compare(t2.getData(), 0, t1.getData(), 0));
  }

  public void testBoundaryDates() throws Exception {
    // Create a new factory
    SPDateFactory factory = new SPDateFactory();
    SPDateImpl oneCE, oneBCE, twoBCE, invalidBCE;

    // Test 1 CE
    oneCE = (SPDateImpl) factory.newSPTypedLiteral(XSD.DATE_URI, XSD_1CE);
    assertEquals(XSD_1CE, oneCE.getLexicalForm());

    // Test 0 CE
    try {
      invalidBCE = (SPDateImpl) factory.newSPTypedLiteral(XSD.DATE_URI, XSD_0CE);
      fail("Year 0000 incorrectly accepted: " + invalidBCE.toString());
    } catch (Throwable t) {
    }

    // Test 1 BCE
    oneBCE = (SPDateImpl) factory.newSPTypedLiteral(XSD.DATE_URI, XSD_1BCE);

    // Test that the lexical form of the date is correct
    assertEquals(XSD_1BCE, oneBCE.getLexicalForm());

    // Test 2 BCE
    twoBCE = (SPDateImpl) factory.newSPTypedLiteral(XSD.DATE_URI, XSD_2BCE);

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
