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
 * Unit test for testing an xsd:base64Binary wrapper.
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
public class SPBase64BinaryUnitTest
    extends TestCase {

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPBase64BinaryUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new SPBase64BinaryUnitTest("testValid"));
    suite.addTest(new SPBase64BinaryUnitTest("testInvalid"));
    suite.addTest(new SPBase64BinaryUnitTest("testCompare"));

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

    SPBase64BinaryFactory factory = new SPBase64BinaryFactory();

    SPTypedLiteral abc = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "abcdabc=");
    assertEquals("Expected: abcdabc=, was: " + abc.getLexicalForm(),
                 "abcdabc=", abc.getLexicalForm());

    SPTypedLiteral ab = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "ab==");
    assertEquals("Expected: ab==, was: " + ab.getLexicalForm(),
                 "ab==", ab.getLexicalForm());


    SPTypedLiteral abcZ = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "abcZ");
    assertEquals("Expected: abcZ, was: " + abcZ.getLexicalForm(),
                 "abcZ", abcZ.getLexicalForm());

    SPTypedLiteral num1 = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "GsdfETSGSDGVCX9A");
    assertEquals("Expected: GsdfETSGSDGVCX9A, was: " + num1.getLexicalForm(),
                 "GsdfETSGSDGVCX9A", num1.getLexicalForm());

    SPTypedLiteral num2 = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "++///+/+0900");
    assertEquals("Expected: ++///+/+0900, was: " + num2.getLexicalForm(),
                 "++///+/+0900", num2.getLexicalForm());
  }

  /**
   * Tests using invalid hex values
   *
   * @throws Exception
   */
  public void testInvalid() throws Exception {

    SPBase64BinaryFactory factory = new SPBase64BinaryFactory();

    try {
      factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "&*%");
      fail("&*% should have trhwon an exception.");
    } catch (Exception e) {
      //expected result
    }

    try {
      factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "@GT");
      fail("@GT should have trhwon an exception.");
    } catch (Exception e) {
      //expected result
    }

    try {
      factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "-,>");
      fail("-,> should have trhwon an exception.");
    } catch (Exception e) {
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
    SPBase64BinaryFactory factory = new SPBase64BinaryFactory();

    //test equal
    SPTypedLiteral abc1 = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "abc");
    SPTypedLiteral abc2 = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "abc");
    SPTypedLiteral num1 = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "+0CfsR9");
    assertTrue("'abc' compared to 'abc' should return 0",
               abc1.compareTo(abc2) == 0);
    assertTrue("Object should return 0 when compared to itself.",
               num1.compareTo(num1) == 0);

    //test unequal
    SPTypedLiteral num2 = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "ABC123");
    SPTypedLiteral num3 = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "XYZ789");
    SPTypedLiteral num4 = factory.newSPTypedLiteral(XSD.BASE64_BINARY_URI, "+99999");
    assertTrue("'ABC123' compared to 'XYZ789' should return a negative number. "
               + num2.compareTo(num3), num2.compareTo(num3) < 0);
    assertTrue("'XYZ789' compared to 'ABC123' should return a positive number. "
               + num3.compareTo(num2), num3.compareTo(num2) > 0);
    assertTrue("'XYZ789' compared to '+99999' should return a negative number. "
               + num3.compareTo(num4), num3.compareTo(num4) < 0);
    assertTrue("'+99999' compared to 'XYZ789' should return a positive number. "
               + num4.compareTo(num3), num4.compareTo(num3) > 0);
    assertTrue("'+99999' compared to 'ABC123' should return a positive number. "
               + num4.compareTo(num1), num4.compareTo(num1) > 0);
    assertTrue("'ABC123' compared to '+99999' should return a negative number. "
               + num1.compareTo(num4), num1.compareTo(num4) < 0);
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
