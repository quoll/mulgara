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
import java.math.BigDecimal;
import java.nio.ByteBuffer;

// JUnit
import junit.framework.*;

// Internal Packages
import org.mulgara.query.rdf.XSD;
import org.mulgara.util.Constants;

/**
 * Unit test for testing an xsd:decimal wrapper.
 *
 * @created Jul 15, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SPDecimalUnitTest extends TestCase {

  private static final String VALID_XSD_DECIMAL1 = "101";

  private static final String VALID_XSD_DECIMAL2 = "1001.";

  private static final String VALID_XSD_DECIMAL3 = "1021.0";

  private static final String VALID_XSD_DECIMAL4 = "-1";

  private static final String VALID_XSD_DECIMAL5 = "0";

  private static final String VALID_XSD_DECIMAL6 = "+101";

  private static final String INVALID_XSD_DECIMAL = "x";
  private static final String INVALID_XSD_DECIMAL2 = "0x";
  private static final String INVALID_XSD_DECIMAL3 = "10.10.";

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPDecimalUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new SPDecimalUnitTest("testValid"));
    suite.addTest(new SPDecimalUnitTest("testInvalid"));
    suite.addTest(new SPDecimalUnitTest("testCompare"));
    suite.addTest(new SPDecimalUnitTest("testCompareFractions"));

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
   * Tests that valid xsd:decimal values are accepted and processed correctly.
   */
  public void testValid() throws Exception {
    // Create a new factory
    SPDecimalFactory factory = new SPDecimalFactory();

    validTest(VALID_XSD_DECIMAL1, factory);
    validTest(VALID_XSD_DECIMAL2, factory);
    validTest(VALID_XSD_DECIMAL3, factory);
    validTest(VALID_XSD_DECIMAL4, factory);
    validTest(VALID_XSD_DECIMAL5, factory);
    validTest(VALID_XSD_DECIMAL6, factory);

    SPDecimalImpl dec = (SPDecimalImpl)factory.newSPTypedLiteral(XSD.INT_URI, VALID_XSD_DECIMAL1);

    // Retrieve the byte data of the decimal object
    ByteBuffer dtBytes = dec.getData();

    long dLong = dtBytes.getLong();

    // Test the correct value is stored
    assertEquals(VALID_XSD_DECIMAL1, Long.toString(dLong));

    // Byte buffer to hold our decimal information
    ByteBuffer buffer = ByteBuffer.wrap(new byte[Constants.SIZEOF_LONG]);

    // If the previous step passed then we know the long value is what we want,
    // so store it in our buffer
    buffer.putLong(dLong);

    // Reset the buffer for reading
    buffer.flip();

    // Create a decimal object by byte buffer
    dec = (SPDecimalImpl)factory.newSPTypedLiteral(1, buffer);

    // Test that the lexical form of the decimal is correct
    assertEquals(VALID_XSD_DECIMAL1, dec.getLexicalForm());
  }

  private void validTest(String number, SPDecimalFactory factory) throws Exception {
    // Create a decimal object by lexical string
    SPDecimalImpl d = (SPDecimalImpl)factory.newSPTypedLiteral(XSD.DECIMAL_URI, number);

    // Test that the lexical form of the decimal is correct
    assertEquals(number, d.getLexicalForm());

    // Retrieve the byte data of the decimal object
    ByteBuffer dtBytes = d.getData();

    // Create a decimal object from the decimal's data - may not be identical, but the data is equivalent
    SPDecimalBaseImpl dDec = new SPDecimalBaseImpl(0, XSD.DECIMAL_URI, SPDecimalImpl.decode(dtBytes));

    BigDecimal bdNum;
    if (d instanceof SPDecimalBaseImpl) bdNum = ((SPDecimalBaseImpl)d).val;
    else bdNum = BigDecimal.valueOf(((SPDecimalExtImpl)d).l);
    assertEquals(bdNum, dDec.val);

    dtBytes.rewind();
    SPDecimalImpl rebuilt = new SPDecimalBaseImpl(0, XSD.DECIMAL_URI, dtBytes);
    assertEquals(d, rebuilt);
  }

  /**
   * Tests invalid xsd:decimal values.
   */
  public void testInvalid() throws Exception {
    // Create a new factory
    SPDecimalFactory factory = new SPDecimalFactory();

    invalidTest(INVALID_XSD_DECIMAL, factory);
    invalidTest(INVALID_XSD_DECIMAL2, factory);
    invalidTest(INVALID_XSD_DECIMAL3, factory);
  }

  private void invalidTest(String dec, SPDecimalFactory factory) throws Exception {
    try {
      SPDecimalImpl d = (SPDecimalImpl)factory.newSPTypedLiteral(XSD.DECIMAL_URI, dec);
      fail("Successfully parsed an invalid decimal: " + dec + " -> " + d);
    } catch (IllegalArgumentException e) {
    }
  }

  public void testCompare() throws Exception {
    // Create a new factory
    SPDecimalFactory factory = new SPDecimalFactory();

    SPDecimalImpl t1, t2, t3;

    t1 = (SPDecimalImpl) factory.newSPTypedLiteral(XSD.DECIMAL_URI, VALID_XSD_DECIMAL1);
    t2 = (SPDecimalImpl) factory.newSPTypedLiteral(XSD.DECIMAL_URI, VALID_XSD_DECIMAL2);
    t3 = (SPDecimalImpl) factory.newSPTypedLiteral(XSD.DECIMAL_URI, VALID_XSD_DECIMAL3);

    assertTrue(t1.compareTo(t1) == 0);
    assertTrue(t1.compareTo(t2) == -1);
    assertTrue(t1.compareTo(t3) == -1);

    assertTrue(t2.compareTo(t1) == 1);
    assertTrue(t2.compareTo(t2) == 0);
    assertTrue(t2.compareTo(t3) == -1);

    assertTrue(t3.compareTo(t1) == 1);
    assertTrue(t3.compareTo(t2) == 1);
    assertTrue(t3.compareTo(t3) == 0);

    t1 = (SPDecimalImpl) factory.newSPTypedLiteral(XSD.INTEGER_URI, "101");
    t2 = (SPDecimalImpl) factory.newSPTypedLiteral(XSD.INTEGER_URI, "1001");
    t3 = (SPDecimalImpl) factory.newSPTypedLiteral(XSD.INTEGER_URI, "1021");

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

  public void testCompareFractions() throws Exception {
    // Create a new factory
    SPDecimalFactory factory = new SPDecimalFactory();

    SPDecimalImpl t1, t2, t3;

    t1 = (SPDecimalImpl) factory.newSPTypedLiteral(XSD.DECIMAL_URI, "1");
    t2 = (SPDecimalImpl) factory.newSPTypedLiteral(XSD.DECIMAL_URI, "1.5");
    t3 = (SPDecimalImpl) factory.newSPTypedLiteral(XSD.DECIMAL_URI, "123456.7890");

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
