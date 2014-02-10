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

// Java 2 standard packages
import java.net.URI;
import java.nio.charset.Charset;

// Third party packages
import junit.framework.*;

/**
 * Unit test for {@link UnknownSPTypedLiteralImpl}
 *
 * @created 2004-12-09
 *
 * @author Simon Raboczi
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/03/11 04:15:22 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class UnknownSPTypedLiteralUnitTest extends TestCase
{
  /**
   * Factory test instance.
   */
  UnknownSPTypedLiteralFactory factory =
    new UnknownSPTypedLiteralFactory();

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public UnknownSPTypedLiteralUnitTest(String name)
  {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite()
  {
    return new TestSuite(UnknownSPTypedLiteralUnitTest.class);
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Tests the {@link UnknownSPTypedLiteralFactory#newSPTypedLiteral} with
   * valid input, and the {@link UnknownSPTypedLiteralImpl#getLexicalForm} and
   * {@link UnknownSPTypedLiteralImpl#getTypeURI} methods on the generated
   * instance.
   *
   * @throws Exception if the test can't be run, but not if the test merely
   *   fails
   */
  public void testConstructorValid() throws Exception
  {
    // Construct test objects
    URI unknownTypeURI = new URI("dummy:type");
    Charset utf8 = Charset.forName("UTF-8");

    try {
      // Typical lexical form
      UnknownSPTypedLiteralImpl text = (UnknownSPTypedLiteralImpl)
        factory.newSPTypedLiteral(unknownTypeURI, "dummy lexical form");

      assertEquals(unknownTypeURI, text.getTypeURI());
      assertEquals("dummy lexical form", text.getLexicalForm());
      assertEquals(utf8.encode(unknownTypeURI + "\tdummy lexical form"),
                   text.getData());

      // Zero-length lexical form
      text = (UnknownSPTypedLiteralImpl)
        factory.newSPTypedLiteral(unknownTypeURI, "");

      assertEquals(unknownTypeURI, text.getTypeURI());
      assertEquals("", text.getLexicalForm());
      assertEquals(utf8.encode(unknownTypeURI + "\t"), text.getData());

      // Lexical form including the delimiter character used when serializing
      // into bytes for filesystem storage (tab)
      text = (UnknownSPTypedLiteralImpl)
        factory.newSPTypedLiteral(unknownTypeURI, "\t");

      assertEquals(unknownTypeURI, text.getTypeURI());
      assertEquals("\t", text.getLexicalForm());
      assertEquals(utf8.encode(unknownTypeURI + "\t\t"), text.getData());

      // Construction from bytes
      text = (UnknownSPTypedLiteralImpl)
        factory.newSPTypedLiteral(0, utf8.encode(unknownTypeURI + "\tfoo"));

      assertEquals(unknownTypeURI, text.getTypeURI());
      assertEquals("foo", text.getLexicalForm());
      assertEquals(utf8.encode(unknownTypeURI + "\tfoo"), text.getData());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Tests the {@link UnknownSPTypedLiteralFactory#newSPTypedLiteral} with
   * invalid input.
   *
   * @throws Exception if the test can't be run, but not if the test merely
   *   fails
   */
  public void testConstructorInvalid() throws Exception
  {
    // Construct test objects
    URI unknownTypeURI = new URI("dummy:type");
    URI relativeTypeURI = new URI("type");

    try {
      try {
        factory.newSPTypedLiteral(null, "dummy lexical form");
        fail("Datatype URI should be required");
      }
      catch (IllegalArgumentException e) {
        // correct behavior
      }

      try {
        factory.newSPTypedLiteral(unknownTypeURI, null);
        fail("Lexical form should be required");
      }
      catch (IllegalArgumentException e) {
        // correct behavior
      }

      try {
        factory.newSPTypedLiteral(relativeTypeURI, "dummy lexical form");
        fail("Relative datatype URI should not be permitted");
      }
      catch (IllegalArgumentException e) {
        // correct behavior
      }
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Tests the {@link UnknownSPTypedLiteralFactory#equals} and
   * {@link UnknownSPTypedLiteralFactory#hashCode} methods.
   */
  public void testEquals() throws Exception
  {
    // Construct test objects
    URI unknownTypeURI  = new URI("dummy:type");
    URI unknownTypeURI2 = new URI("dummy:type2");

    try {
      UnknownSPTypedLiteralImpl test = (UnknownSPTypedLiteralImpl)
        factory.newSPTypedLiteral(unknownTypeURI, "dummy lexical form");

      UnknownSPTypedLiteralImpl test2 = (UnknownSPTypedLiteralImpl)
        factory.newSPTypedLiteral(unknownTypeURI, "dummy lexical form");

      assertFalse(test.equals(null));

      assertTrue(test.equals(test2));
      assertEquals(test.hashCode(), test2.hashCode());

      assertFalse(test.equals(
        factory.newSPTypedLiteral(unknownTypeURI2, "dummy lexical form")
      ));

      assertFalse(test.equals(
        factory.newSPTypedLiteral(unknownTypeURI, "mismatched lexical form")
      ));
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
