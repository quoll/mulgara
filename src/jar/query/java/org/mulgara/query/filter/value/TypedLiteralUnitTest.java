/**
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */
package org.mulgara.query.filter.value;

import java.net.URI;

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.TestContext;

import static org.mulgara.query.rdf.XSD.NAMESPACE;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the typed literal class.
 *
 * @created Mar 31, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class TypedLiteralUnitTest extends TestCase {

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public TypedLiteralUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new TypedLiteralUnitTest("testValues"));
    suite.addTest(new TypedLiteralUnitTest("testFilter"));
    suite.addTest(new TypedLiteralUnitTest("testType"));
    suite.addTest(new TypedLiteralUnitTest("testProperties"));
    return suite;
  }


  public void testValues() throws Exception {
    String str = "test";
    TypedLiteral l = (TypedLiteral)TypedLiteral.newLiteral(str, URI.create(NAMESPACE + "string"), null);
    assertEquals(str, l.getValue());

    l = (TypedLiteral)TypedLiteral.newLiteral(str);
    assertEquals(str, l.getValue());

    String s2 = "foobar";
    l = (TypedLiteral)TypedLiteral.newLiteral(s2, URI.create("foo:bar"), null);
    assertEquals(s2, l.getValue());

    Long v = Long.valueOf(5);
    l = (TypedLiteral)TypedLiteral.newLiteral(v);
    assertEquals(v, l.getValue());
  }

  public void testFilter() throws Exception {
    Context c = new TestContext();
    TypedLiteral l = (TypedLiteral)TypedLiteral.newLiteral("test", URI.create(NAMESPACE + "string"), null);
    assertTrue(l.test(c));

    l = (TypedLiteral)TypedLiteral.newLiteral("test");
    assertTrue(l.test(c));
    l = (TypedLiteral)TypedLiteral.newLiteral("");
    assertFalse(l.test(c));

    l = (TypedLiteral)TypedLiteral.newLiteral("foobar", URI.create("foo:bar"), null);
    try {
      l.test(c);
      fail("Got an EBV from an unknown literal type");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error:"));
    }

    l = (TypedLiteral)TypedLiteral.newLiteral(Long.valueOf(5));
    assertTrue(l.test(c));
    l = (TypedLiteral)TypedLiteral.newLiteral(Long.valueOf(0));
    assertFalse(l.test(c));
  }

  public void testType() throws Exception {
    String str = "test";
    URI t = URI.create(NAMESPACE + "string");
    TypedLiteral l = (TypedLiteral) TypedLiteral.newLiteral(str, t, null);
    assertTrue(l.getType().isIRI());
    assertEquals(t, l.getType().getValue());

    l = (TypedLiteral)TypedLiteral.newLiteral(str);
    assertEquals(t, l.getType().getValue());

    String s2 = "foobar";
    URI t2 = URI.create(NAMESPACE + "foo:bar");
    l = (TypedLiteral)TypedLiteral.newLiteral(s2, t2, null);
    assertEquals(t2, l.getType().getValue());

    Long v = Long.valueOf(5);
    l = (TypedLiteral)TypedLiteral.newLiteral(v);
    assertEquals(URI.create(NAMESPACE + "long"), l.getType().getValue());
  }

  public void testProperties() throws Exception {
    TypedLiteral l = (TypedLiteral) TypedLiteral.newLiteral("test", URI.create(NAMESPACE + "string"), null);
    assertFalse(l.isBlank());
    assertFalse(l.isIRI());
    assertTrue(l.isLiteral());
    assertFalse(l.isURI());
  }
}
