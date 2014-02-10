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

package org.mulgara.query.rdf;

// Java 2 standard packages
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

// Third party packages
import junit.framework.*;          // JUnit
import org.apache.log4j.Logger;    // Log4J

/**
 * Test case for {@link LiteralImpl}.
 *
 * @created 2004-03-23
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.11 $
 *
 * @modified $Date: 2005/03/02 11:21:26 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LiteralImplUnitTest extends TestCase {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(LiteralImplUnitTest.class);

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public LiteralImplUnitTest(String name) {

    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {
    return new TestSuite(LiteralImplUnitTest.class);
  }

  /**
   * Default text runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  //
  // Test cases
  //

  /**
   * Test the {@link LiteralImpl#toString} method.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testConstructor() throws Exception {
    try {
      new LiteralImpl((String) null);
      fail("Shouldn't be able to construct a literal with null lexical form");
    }
    catch (Throwable e) {
      // correct behavior
    }

    try {
      new LiteralImpl("foo", (String) null);
      fail("Shouldn't be able to construct a literal with null language");
    }
    catch (Throwable e) {
      // correct behavior
    }

    try {
      new LiteralImpl((String) null, (URI) null);
      fail("Shouldn't be able to construct a literal with null lexical form");
    }
    catch (Throwable e) {
      // correct behavior
    }

    // This should work
    new LiteralImpl("2000-12-25", XSD.DATE_URI);
  }

  /**
   * Test the {@link LiteralImpl#toString} method.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testToString() throws Exception {
    // Test a plain literal
    assertEquals(
        "foo",
        (new LiteralImpl("foo").getLexicalForm())
        );

    // Test a plain literal with a language code
    assertEquals(
        "foo",
        (new LiteralImpl("foo", "en")).getLexicalForm()
        );

    // Test a plain literal with an embedded quote
    assertEquals(
        "\"\\\"Foo!\\\", he said.\"@en",
        (new LiteralImpl("\"Foo!\", he said.", "en")).getEscapedForm()
        );

    // Test a plain literal with an embedded backslash
    assertEquals(
        "\"back\\\\slash\"",
        (new LiteralImpl("back\\slash", "")).getEscapedForm()
        );

    // Test a plain literal with an embedded non-ASCII character
    assertEquals(
        "\"\\u00A92004 Tucana Technology\"@en",
        (new LiteralImpl("\u00a92004 Tucana Technology", "en")).getEscapedForm()
        );

    // Test a plain literal with an embedded Unicode surrogate pair
    assertEquals(
        "\"Deseret short ah: \\U00010409\"",
        (new LiteralImpl("Deseret short ah: \ud801\udc09", "")).getEscapedForm()
        );

    // Test a plain literal with an embedded 4 byte UTF-8 encoding
    assertEquals(
        "\"Deseret short ah: \\U00010409\"",
        (new LiteralImpl("Deseret short ah: \u00f0\u0090\u0090\u0089", "")).getEscapedForm()
        );

    // Test a plain literal with an embedded 3 byte UTF-8 encoding
    assertEquals(
        "\"Devanagari letter i: \\u0907\"",
        (new LiteralImpl("Devanagari letter i: \u00e0\u00a4\u0087", "")).getEscapedForm()
        );

    // Test a plain literal with broken Unicode surrogates -- they should be
    // formatted the same way non-ASCII characters are (4-digit hex)
    assertEquals(
        "\"high: \\uD801, low: \\uDC09\"",
        (new LiteralImpl("high: \ud801, low: \udc09", "")).getEscapedForm()
        );

    // Test an xsd:dateTime datatyped literal
    Date christmas = (new GregorianCalendar(2000, Calendar.DECEMBER, 25,
        9, 0, 0)).getTime();
    assertEquals(
        "\"2000-12-25T09:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>",
        (new LiteralImpl(christmas)).getEscapedForm()
        );

    // Test an xsd:double datatyped literal
    assertEquals(
        "\"3.14\"^^" + "<" + XSD.DOUBLE_URI + ">",
        (new LiteralImpl(3.14)).getEscapedForm()
        );
    assertEquals(
        "\"3.14\"^^" + "<" + XSD.DOUBLE_URI + ">",
        (new LiteralImpl("3.14", XSD.DOUBLE_URI)).getEscapedForm()
        );
  }
}
