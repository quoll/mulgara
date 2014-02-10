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

package org.mulgara.resolver;

// Java 2 standard packages
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.text.ParseException;
import java.util.*;

// Third party packages
import junit.framework.*;        // JUnit
import org.apache.log4j.Logger;  // Log4J
import org.jrdf.graph.Literal;   // JRDF
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;

// Locally written packages
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.rdf.XSD;

/**
 * Test suite for {@link NTriples}.
 *
 * @created 2004-09-22
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.10 $
 * @modified $Date: 2005/03/02 11:22:41 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class NTriplesUnitTest extends TestCase {
  /** Logger. */
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(NTriplesUnitTest.class.getName());

  /**
   * Constructs a new test with the given name.
   *
   * @param name  the name of the test
   */
  public NTriplesUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return the test suite
   */
  public static Test suite() {
    return new TestSuite(NTriplesUnitTest.class);
  }

  /**
   * Default text runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Create test instance.
   */
  public void setUp() {
    // null implementation
  }

  /**
   * The teardown method for JUnit
   */
  public void tearDown() {
    // null implementation
  }

  //
  // Test cases
  //

  /**
   * Test {@link NTriples#escapeLexicalForm} method.
   */
  public void testEscapeLexicalForm() {
    try {
      // no escaped characters
      escapeAndUnescape("foo", "foo");

      // embedded quote
      escapeAndUnescape("\"Foo!\", he said.", "\\\"Foo!\\\", he said.");

      // embedded backslash
      escapeAndUnescape("back\\slash", "back\\\\slash");

      // embedded non-ASCII character
      escapeAndUnescape(
        "\u00a92004 Mulgara Unicode Test",
        "\\u00A92004 Mulgara Unicode Test"
      );

      // embedded Unicode surrogate pair
      escapeAndUnescape(
        "Deseret short ah: \ud801\udc09",
        "Deseret short ah: \\U00010409"
      );

      // embedded broken Unicode surrogates -- they should be
      // treated the same way non-ASCII characters are (4-digit hex)
      escapeAndUnescape(
        "high: \ud801, low: \udc09",
        "high: \\uD801, low: \\uDC09"
      );
    }
    catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test {@link NTriples#toNode} and {@link NTriples#toString} method for
   * good inputs.
   */
  public void testConvert() throws Exception {
    URI          baseURI = new URI("foo://auth/");
    Literal      cat     = new LiteralImpl("cat");
    Literal      catEn   = new LiteralImpl("cat", "en");
    URIReference barBaz  = new URIReferenceImpl(new URI("bar:baz"));
    URIReference fooAuth = new URIReferenceImpl(baseURI);
    URIReference fooBaz  = new URIReferenceImpl(new URI("foo://auth/#baz"));
    Literal      seven   = new LiteralImpl(7);
    Literal      pi      = new LiteralImpl(3.14);
    Literal      quote   = new LiteralImpl("\"");
    Literal      xmas2k  = new LiteralImpl(
      (new GregorianCalendar(2000, Calendar.DECEMBER, 25, 9, 0, 0)).getTime()
    );
    Literal      trill   = new LiteralImpl("pronounce with a trill, of the phoneme \\r\\; \"Some spekaers trill their r''s\"");


    try {
      // Named resources
      convert(barBaz,  "<bar:baz>",  baseURI);
      convert(fooBaz,  "<#baz>",     baseURI);
      convert(fooAuth, "<>",         baseURI);

      // Plain literals
      convert(cat,     "\"cat\"",    baseURI);
      convert(catEn,   "\"cat\"@en", baseURI);
      convert(quote,   "\"\\\"\"",   baseURI);
      convert(trill,   "\"pronounce with a trill, of the phoneme \\\\r\\\\; \\\"Some spekaers trill their r''s\\\"\"", baseURI);

      // Datatyped literals
      convert(seven,
              "\"7\"^^<" + XSD.INTEGER_URI + ">",
              baseURI);

      convert(pi,
              "\"3.14\"^^<" + XSD.DOUBLE_URI + ">",
              baseURI);

      convert(xmas2k,
              "\"2000-12-25T09:00:00\"^^<" + XSD.DATE_TIME_URI + ">",
              baseURI);
    }
    catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test {@link NTriples#toNode} method for bad inputs.
   */
  public void testToNode() throws Exception {
    URI baseURI = new URI("foo://auth/");

    try {
      try {
        NTriples.toNode("cat", baseURI);
        fail("Expected " + ParseException.class);
      }
      catch (ParseException e) { /* correct behavior */ }

      try {
        NTriples.toNode("\"cat", baseURI);
        fail("Expected " + ParseException.class);
      }
      catch (ParseException e) { /* correct behavior */ }

      try {
        NTriples.toNode("<foo:bar\"", baseURI);
        fail("Expected " + ParseException.class);
      }
      catch (ParseException e) { /* correct behavior */ }
    }
    catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test {@link NTriples#toString} method for bad inputs.
   */
  public void testToString() throws Exception {
    URI baseURI = new URI("foo://auth/");

    try {
      try {
        NTriples.toString(null, baseURI);
        fail("Expected " + IllegalArgumentException.class);
      }
      catch (IllegalArgumentException e) { /* correct behavior */ }
    }
    catch (Exception e) {
      fail(e);
    }
  }

  //
  // Internal methods
  //

  /**
   * Test {@link NTriples#escapeLexicalForm} and
   * {@link NTriples#unescapeLexicalForm}.
   *
   * @param unescaped  some arbitrary Unicode string
   * @param escaped  an ASCII string comprising the <var>unescaped</var> text
   *   with N-Triples escaping applied
   */
  private static void escapeAndUnescape(String unescaped, String escaped) {
    assertEquals(escaped, NTriples.escapeLexicalForm(unescaped));
    assertEquals(unescaped, NTriples.unescapeLexicalForm(escaped));
  }

  /**
   * Test that {@link NTriples#toString} maps <var>node</var> to
   * <var>string</var> and {@link NTriples#toNode} maps <var>string</var> to
   * <var>node</var>
   *
   * @param node  a JRDF node
   * @param string  the N-Triples serialization of <var>node</var>
   * @param baseURI  the base URI to use in both conversions
   */
  private static void convert(Node node, String string, URI baseURI)
      throws ParseException {
    assertEquals(node, NTriples.toNode(string, baseURI));
    assertEquals(string, NTriples.toString(node, baseURI));
  }

  /**
   * Fail with an unexpected exception
   */
  private void fail(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    fail(stringWriter.toString());
  }
}
