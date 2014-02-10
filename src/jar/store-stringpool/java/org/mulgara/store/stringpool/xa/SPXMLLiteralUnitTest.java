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

// JRDF
import org.jrdf.vocabulary.RDF;

// Third party packages
import junit.framework.*;

/**
 * Unit test for {@link SPXMLLiteralImpl}
 *
 * @created 2004-12-02
 *
 * @author Andrew Newman
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
public class SPXMLLiteralUnitTest extends TestCase {

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPXMLLiteralUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new SPXMLLiteralUnitTest("testValid"));

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

    SPXMLLiteralFactory factory = new SPXMLLiteralFactory();
    SPXMLLiteralImpl text;

    // Test normal lexical value.
    try {
      text = (SPXMLLiteralImpl) factory.newSPTypedLiteral(
          RDF.XML_LITERAL, "Hello there");
      assertEquals("Should have correct lexical form", "Hello there",
          text.getLexicalForm());
    }
    catch (IllegalArgumentException iae) {
      iae.printStackTrace();
      fail("Should be valid");
    }

    // Test with a tag.
    try {
      text = (SPXMLLiteralImpl) factory.newSPTypedLiteral(
          RDF.XML_LITERAL, "<foo>Hello there</foo>");
      assertEquals("Should have correct lexical form", "<foo>Hello there</foo>",
          text.getLexicalForm());
    }
    catch (IllegalArgumentException iae) {
      iae.printStackTrace();
      fail("Should be valid");
    }

    // Test nested tags
    try {
      text = (SPXMLLiteralImpl) factory.newSPTypedLiteral(
          RDF.XML_LITERAL, "<foo><bar>Hello there</bar></foo>");
      assertEquals("Should have correct lexical form",
          "<foo><bar>Hello there</bar></foo>", text.getLexicalForm());
    }
    catch (IllegalArgumentException iae) {
      iae.printStackTrace();
      fail("Should be valid");
    }

    // Test XML escaping.
    try {
      text = (SPXMLLiteralImpl) factory.newSPTypedLiteral(
          RDF.XML_LITERAL, "This is fun &lt; &gt; &quot; &amp; &apos; &quot;");
      assertEquals("Should have correct lexical form",
          "This is fun &lt; &gt; &quot; &amp; &apos; &quot;",
          text.getLexicalForm());
    }
    catch (IllegalArgumentException iae) {
      iae.printStackTrace();
      fail("Should be valid");
    }

    // Test XML escaping.
    try {
      text = (SPXMLLiteralImpl) factory.newSPTypedLiteral(
          RDF.XML_LITERAL, "<foo>This is fun &lt; &gt; &quot; &amp; &apos; &quot;</foo>");
      assertEquals("Should have correct lexical form",
          "<foo>This is fun &lt; &gt; &quot; &amp; &apos; &quot;</foo>",
          text.getLexicalForm());
    }
    catch (IllegalArgumentException iae) {
      iae.printStackTrace();
      fail("Should be valid");
    }
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
