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

import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.store.stringpool.*;

/**
 * Unit test for building SPStringImpl
 *
 * @created Jul 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SPStringUnitTest extends TestCase {

  static String S1 = "foo";

  static String S2 = "bar";

  static String LANG1 = "en";

  static String LANG2 = "af-dr2";

  static String BAD_LANG1 = "en1-uk";

  static String BAD_LANG2 = "en-";

  static String BAD_LANG3 = "@@en";

  SPObjectFactory factory;

  /**
   * Constructs a new test with the given name.
   * @param name the name of the test
   */
  public SPStringUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new SPStringUnitTest("testValid"));
    suite.addTest(new SPStringUnitTest("testInvalid"));
    suite.addTest(new SPStringUnitTest("testByteData"));
    suite.addTest(new SPStringUnitTest("testCompare"));

    return suite;
  }

  /**
   * Default test runner.
   * @param args The command line arguments
   */
  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Tests the valid xsd:boolean values
   */
  public void testValid() throws Exception {
    SPString s1 = factory.newSPString(S1);
    assertEquals("\"" + S1 + "\"", s1.getEncodedString());
    assertEquals(S1, s1.getLexicalForm());

    s1 = (SPString)factory.newSPObject(new LiteralImpl(S1, LANG1));
    assertEquals("\"" + S1 + "\"@" + LANG1, s1.getEncodedString());
    assertEquals(S1, s1.getLexicalForm());

    s1 = (SPString)factory.newSPObject(new LiteralImpl(S1, LANG2));
    assertEquals("\"" + S1 + "\"@" + LANG2, s1.getEncodedString());
    assertEquals(S1, s1.getLexicalForm());
  }

  /**
   * Tests some invalid xsd:boolean values
   *
   * @throws Exception
   */
  public void testInvalid() throws Exception {
    SPString s1;

    try {
      s1 = (SPString)factory.newSPObject(new LiteralImpl(S1, BAD_LANG1));
      fail("Illegal language code accepted: " + s1.getLexicalForm());
    } catch (IllegalArgumentException e) { }

    try {
      s1 = (SPString)factory.newSPObject(new LiteralImpl(S1, BAD_LANG2));
      fail("Illegal language code accepted: " + s1.getLexicalForm());
    } catch (IllegalArgumentException e) { }

    try {
      s1 = (SPString)factory.newSPObject(new LiteralImpl(S1, BAD_LANG3));
      fail("Illegal language code accepted: " + s1.getLexicalForm());
    } catch (IllegalArgumentException e) { }
  }

  /**
   * Tests SPBoolean using values as byte buffers
   *
   * @throws Exception
   */
  public void testByteData() throws Exception {

    SPString s1 = factory.newSPString(S1);
    ByteBuffer bb = s1.getData();
    SPString rebuilt = (SPString)factory.newSPObject(SPObject.TypeCategory.UNTYPED_LITERAL, 0, 0, bb);
    assertEquals(s1, rebuilt);
    assertEquals("\"" + S1 + "\"", rebuilt.getEncodedString());

    s1 = (SPString)factory.newSPObject(new LiteralImpl(S1, LANG1));
    bb = s1.getData();
    rebuilt = (SPString)factory.newSPObject(SPObject.TypeCategory.UNTYPED_LITERAL, 0, 0, bb);
    assertEquals(s1, rebuilt);
    assertEquals("\"" + S1 + "\"@" + LANG1, rebuilt.getEncodedString());

    s1 = (SPString)factory.newSPObject(new LiteralImpl(S1, LANG2));
    bb = s1.getData();
    rebuilt = (SPString)factory.newSPObject(SPObject.TypeCategory.UNTYPED_LITERAL, 0, 0, bb);
    assertEquals(s1, rebuilt);
    assertEquals("\"" + S1 + "\"@" + LANG2, rebuilt.getEncodedString());
  }

  /**
   * Compares SPBoolean objects.
   *
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public void testCompare() throws Exception {

    SPString s1 = factory.newSPString(S1);
    SPString s2 = factory.newSPString(S1);
    assertEquals(0, s1.compareTo(s2));
    assertEquals(0, s2.compareTo(s1));
    s2 = factory.newSPString(S2);
    assertTrue(s1.compareTo(s2) > 0);
    assertTrue(s2.compareTo(s1) < 0);

    s2 = (SPString)factory.newSPObject(new LiteralImpl(S1, LANG1));
    assertTrue(s1.compareTo(s2) < 0);
    assertTrue(s2.compareTo(s1) > 0);

    s1 = (SPString)factory.newSPObject(new LiteralImpl(S1, LANG1));
    assertEquals(0, s1.compareTo(s2));
    assertEquals(0, s2.compareTo(s1));

    s2 = (SPString)factory.newSPObject(new LiteralImpl(S2, LANG1));
    assertTrue(s1.compareTo(s2) > 0);
    assertTrue(s2.compareTo(s1) < 0);
  }

  //set up and tear down

  /**
   * Initialise members.
   *
   * @throws Exception if something goes wrong
   */
  public void setUp() throws Exception {
    super.setUp();
    factory = new SPObjectFactoryImpl();
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
