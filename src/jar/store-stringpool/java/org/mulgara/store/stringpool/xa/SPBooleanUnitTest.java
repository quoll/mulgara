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

import java.nio.*;

// Third party packages
import junit.framework.*;

import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.*;

/**
 * Unit test for testing an xsd:boolean wrapper.
 *
 * @created 2004-10-04
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/03/11 04:15:22 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SPBooleanUnitTest
    extends TestCase {

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPBooleanUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new SPBooleanUnitTest("testValid"));
    suite.addTest(new SPBooleanUnitTest("testInvalid"));
    suite.addTest(new SPBooleanUnitTest("testByteData"));
    suite.addTest(new SPBooleanUnitTest("testCompare"));

    return suite;
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    junit.textui.TestRunner.run(suite());
  }

  /**
   * Tests the valid xsd:boolean values
   *
   * @throws Exception
   */
  public void testValid() throws Exception {

    SPBooleanFactory factory = new SPBooleanFactory();

    //test variations of 'true'
    SPBooleanImpl booleanTrue = (SPBooleanImpl) factory.newSPTypedLiteral(
        XSD.BOOLEAN_URI, "true");
    assertTrue("'true' should return a true SPBoolean", booleanTrue.getBoolean());
    booleanTrue = (SPBooleanImpl) factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "TRUE");
    assertTrue("'TRUE' should return a true SPBoolean", booleanTrue.getBoolean());
    booleanTrue = (SPBooleanImpl) factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "tRuE");
    assertTrue("'tRuE' should return a true SPBoolean", booleanTrue.getBoolean());

    //test variations of 'false'
    SPBooleanImpl booleanFalse = (SPBooleanImpl) factory.newSPTypedLiteral(
        XSD.BOOLEAN_URI, "false");
    assertFalse("'false' should return a false SPBoolean",
                booleanFalse.getBoolean());
    booleanFalse = (SPBooleanImpl) factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "FALSE");
    assertFalse("'FALSE' should return a false SPBoolean",
                booleanFalse.getBoolean());
    booleanFalse = (SPBooleanImpl) factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "fAlsE");
    assertFalse("'fAlsE' should return a false SPBoolean",
                booleanFalse.getBoolean());

    //test values 0 and 1
    SPBooleanImpl boolean1 = (SPBooleanImpl) factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "1");
    assertTrue("'1' should return a true SPBoolean", boolean1.getBoolean());
    SPBooleanImpl boolean0 = (SPBooleanImpl) factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "0");
    assertFalse("'0' should return a false SPBoolean", boolean0.getBoolean());
  }

  /**
   * Tests some invalid xsd:boolean values
   *
   * @throws Exception
   */
  public void testInvalid() throws Exception {

    SPBooleanFactory factory = new SPBooleanFactory();

    try {
      factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "BAD STRING");
      fail(
          "factory.newSPTypedLiteral(XSD.BOOLEAN_URI, 'BAD STRING') should have thrown an exception.");
    }
    catch (Exception e) {
      //expected result
    }

    try {
      factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "6");
      fail("factory.newSPTypedLiteral(XSD.BOOLEAN_URI, '6') should have thrown an exception.");
    }
    catch (Exception e) {
      //expected result
    }

    try {
      factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "null");
      fail("factory.newSPTypedLiteral(XSD.BOOLEAN_URI, 'null') should have thrown an exception.");
    }
    catch (Exception e) {
      //expected result
    }

    try {
      factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "00");
      fail("factory.newSPTypedLiteral(XSD.BOOLEAN_URI, '00') should have thrown an exception.");
    }
    catch (Exception e) {
      //expected result
    }

    try {
      factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "true_not");
      fail(
          "factory.newSPTypedLiteral(XSD.BOOLEAN_URI, 'true_not') should have thrown an exception.");
    }
    catch (Exception e) {
      //expected result
    }
  }

  /**
   * Tests SPBoolean using values as byte buffers
   *
   * @throws Exception
   */
  public void testByteData() throws Exception {

    SPBooleanFactory factory = new SPBooleanFactory();

    //test 'true'
    ByteBuffer trueBuffer = ByteBuffer.wrap(new byte[] {1});
    SPBooleanImpl booleanTrue = (SPBooleanImpl) factory.newSPTypedLiteral(
        0, trueBuffer);
    assertTrue("ByteBuffer with value '1' should return a 'true' SPBoolean",
               booleanTrue.getBoolean());

    //test 'false'
    ByteBuffer falseBuffer = ByteBuffer.wrap(new byte[] {0});
    SPBooleanImpl booleanFalse = (SPBooleanImpl) factory.newSPTypedLiteral(
        0, falseBuffer);
    assertFalse("ByteBuffer with value '0' should return a 'false' SPBoolean",
                booleanFalse.getBoolean());

    //test 'invalid'
    try {
      factory.newSPTypedLiteral(0, ByteBuffer.wrap(new byte[] {69}));
      fail(
          "factory.newSPTypedLiteral(0, ByteBuffer.wrap(new byte[] {69})) should " +
          "have thrown an exception.");
    }
    catch (Exception e) {
      //expected result
    }
  }

  /**
   * Compares SPBoolean objects.
   *
   * @throws Exception
   */
  public void testCompare() throws Exception {

    SPBooleanFactory factory = new SPBooleanFactory();

    //test 'true' compareTo 'true'
    ByteBuffer trueBuffer = ByteBuffer.wrap(new byte[] {1});
    SPBooleanImpl true1 = (SPBooleanImpl) factory.newSPTypedLiteral(0, trueBuffer);
    SPBooleanImpl true2 = (SPBooleanImpl) factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "true");
    assertTrue("SPBoolean.compareTo() should return 0 for two true SPBooleans.",
               true1.compareTo(true2) == 0);
    true1 = (SPBooleanImpl) factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "TRUE");
    assertTrue("SPBoolean.compareTo() should return 0 for two true SPBooleans.",
               true1.compareTo(true2) == 0);

    //test 'false' compareTo 'false'
    ByteBuffer falseBuffer = ByteBuffer.wrap(new byte[] {0});
    SPBooleanImpl false1 = (SPBooleanImpl) factory.newSPTypedLiteral(
        0, falseBuffer);
    SPBooleanImpl false2 = (SPBooleanImpl) factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "false");
    assertTrue(
        "SPBoolean.compareTo() should return 0 for two false SPBooleans.",
        false1.compareTo(false2) == 0);
    false1 = (SPBooleanImpl) factory.newSPTypedLiteral(XSD.BOOLEAN_URI, "FALSE");
    assertTrue(
        "SPBoolean.compareTo() should return 0 for two false SPBooleans.",
        false1.compareTo(false2) == 0);

    //test 'false' compareTo 'true' is negative
    assertTrue(
        "'false' SPBoolean compared to 'true' SPBoolean should return a " +
        "negative number. " + false1.compareTo(true1), false1.compareTo(true1) < 0);

    //test 'true' compareTo 'false' is negative
    assertTrue(
        "'true' SPBoolean compared to 'false' SPBoolean should return a " +
        "positive number. " + true2.compareTo(false1), true2.compareTo(false1) > 0);

    //ensure comparator is consistent with compareTo
    SPComparator comparator = false1.getSPComparator();
    assertTrue("'false' SPBoolean compared to 'true' SPBoolean should return " +
               "a negative number. ",
               comparator.compare(false1.getData(), 0, true1.getData(), 0) < 0);
    comparator = true2.getSPComparator();
    assertTrue("'true' SPBoolean compared to 'false' SPBoolean should return " +
               "a positive number.",
               comparator.compare(true2.getData(), 0, false1.getData(), 0) > 0);
  }

  //set up and tear down

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
