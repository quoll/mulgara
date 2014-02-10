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

// Third party packages
import junit.framework.*;
import org.mulgara.query.rdf.*;
import org.mulgara.store.stringpool.*;

/**
 * Unit test for testing an xsd:hexBinary wrapper.
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
public class SPHexBinaryUnitTest
    extends TestCase {

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPHexBinaryUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new SPHexBinaryUnitTest("testValid"));
    suite.addTest(new SPHexBinaryUnitTest("testInvalid"));
    suite.addTest(new SPHexBinaryUnitTest("testCompare"));

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
   * Tests valid hex values
   *
   * @throws Exception
   */
  public void testValid() throws Exception {

    SPHexBinaryFactory factory = new SPHexBinaryFactory();

    //test characters
    SPTypedLiteral cafe = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "cafe");
    assertNotNull("SPHexBinaryFactory returned null.", cafe);
    assertEquals("cafe", cafe.getLexicalForm());
    SPTypedLiteral deadbeef = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI,
        "deadbeef");
    assertNotNull("SPHexBinaryFactory returned null.", deadbeef);
    assertEquals("deadbeef", deadbeef.getLexicalForm());

    //test with numbers
    SPTypedLiteral num1 = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "1");
    assertNotNull("SPHexBinaryFactory returned null.", num1);
    assertEquals("1", num1.getLexicalForm());
    SPTypedLiteral num12a34 = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI,
        "12a34");
    assertNotNull("SPHexBinaryFactory returned null.", num12a34);
    assertEquals("12a34", num12a34.getLexicalForm());

    //test upper/lower case
    SPTypedLiteral upperCase = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI,
        "CAFE");
    assertNotNull("SPHexBinaryFactory returned null.", upperCase);
    assertEquals("cafe", upperCase.getLexicalForm());
    SPTypedLiteral upperLowerCase = factory.newSPTypedLiteral(XSD.
        HEX_BINARY_URI, "CaFE01deAdBEEf69");
    assertNotNull("SPHexBinaryFactory returned null.", upperLowerCase);
    assertEquals("cafe01deadbeef69", upperLowerCase.getLexicalForm());
  }

  /**
   * Tests using invalid hex values
   *
   * @throws Exception
   */
  public void testInvalid() throws Exception {

    SPHexBinaryFactory factory = new SPHexBinaryFactory();

    //test characters
    try {
      factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "notValid");
      fail("'notValid' is not valid hex and should have caused an exception.");
    }
    catch (Exception e) {
      //expected result
    }
    try {
      factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "*&..-+=<<");
      fail("'*&..564<<' is not valid hex and should have caused an exception.");
    }
    catch (Exception e) {
      //expected result
    }

    //test numbers (and characters)
    try {
      factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "9ftt0");
      fail("'9ftt0' is not valid hex and should have caused an exception.");
    }
    catch (Exception e) {
      //expected result
    }
  }

  /**
   * Tests the comparison of hexBinary objects
   *
   * @throws Exception
   */
  public void testCompare() throws Exception {

    //compare equivalent values
    SPHexBinaryFactory factory = new SPHexBinaryFactory();

    //compare equivalants
    SPTypedLiteral cafe1 = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "cafe");
    SPTypedLiteral cafe2 = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "cafe");
    SPTypedLiteral cafe3 = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "CAFE");
    SPTypedLiteral one1 = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "1");
    SPTypedLiteral one2 = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "1");
    assertEquals("SPHexBinary should return 0 for equivalant SPHexBinary",
                 0, cafe1.compareTo(cafe2));
    assertEquals("SPHexBinary should return 0 for equivalant SPHexBinary",
                 0, cafe2.compareTo(cafe3));
    assertEquals("SPHexBinary should return 0 for equivalant SPHexBinary",
                 0, cafe3.compareTo(cafe1));
    assertEquals("SPHexBinary number should return 0 for equivalant SPHexBinary",
                 0, one1.compareTo(one2));
    assertEquals("SPHexBinary should return 0 when compared to itself",
                 0, cafe1.compareTo(cafe1));
    assertEquals("SPHexBinary number should return 0 when compared to itself",
                 0, one1.compareTo(one1));

    //compare non-equivalants
    SPTypedLiteral beef = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "beef");
    SPTypedLiteral beef1 = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "beef1");
    SPTypedLiteral dead = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "dead");
    SPTypedLiteral zero = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "0");
    SPTypedLiteral one = factory.newSPTypedLiteral(XSD.HEX_BINARY_URI, "1");
    assertTrue("should return a negative number for 'beef' compared to 'beef1' " +
        beef.compareTo(beef1), beef.compareTo(beef1) < 0);
    assertTrue("should return a positive number for 'beef1' compared to 'beef' " +
        beef1.compareTo(beef), beef1.compareTo(beef) > 0);
    assertTrue("should return a positive number for 'dead' compared to 'beef' " +
               dead.compareTo(beef), dead.compareTo(beef) > 0);
    assertTrue("should return a negative number for 'beef' compared to 'dead' " +
               beef.compareTo(dead), beef.compareTo(dead) < 0);
    assertTrue("should return a negative number for '0' compared to '1' " +
               zero.compareTo(one), zero.compareTo(one) < 0);
    assertTrue("should return a positive number for '1' compared to '0' " +
               one.compareTo(zero), one.compareTo(zero) > 0);
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
