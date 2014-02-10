package org.mulgara.protocol.http;

import java.net.URI;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for LocalTriple and the inner class Literal.
 *
 * @created Feb 15, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class LocalTripleUnitTest  extends TestCase {

  public LocalTripleUnitTest(String name) {
    super(name);
  }

  /**
   * Default text runner.
   * @param args The command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new LocalTripleUnitTest("testUriConstructor"));
    suite.addTest(new LocalTripleUnitTest("testBlanks"));
    suite.addTest(new LocalTripleUnitTest("testLiterals"));
    suite.addTest(new LocalTripleUnitTest("testLiteralTypes"));
    suite.addTest(new LocalTripleUnitTest("testLiteralLang"));
    suite.addTest(new LocalTripleUnitTest("testLiteralQuote"));
    return suite;
  }

  public void testUriConstructor() throws Exception {
    LocalTriple t = new LocalTriple("foo:sub", "foo:pred", "foo:obj");
    assertTrue(t.getSubject() instanceof URIReference);
    assertTrue(t.getPredicate() instanceof URIReference);
    assertTrue(t.getObject() instanceof URIReference);
    assertEquals("foo:sub", ((URIReference)t.getSubject()).toString());
    assertEquals("foo:pred", ((URIReference)t.getPredicate()).toString());
    assertEquals("foo:obj", ((URIReference)t.getObject()).toString());
  }

  public void testBlanks() throws Exception {
    LocalTriple t = new LocalTriple(null, "foo:pred", "foo:obj");
    assertTrue(t.getSubject() instanceof BlankNode);
    assertTrue(t.getPredicate() instanceof URIReference);
    assertTrue(t.getObject() instanceof URIReference);
    assertEquals("foo:pred", ((URIReference)t.getPredicate()).toString());
    assertEquals("foo:obj", ((URIReference)t.getObject()).toString());

    t = new LocalTriple("foo:sub", "foo:pred", null);
    assertTrue(t.getSubject() instanceof URIReference);
    assertTrue(t.getPredicate() instanceof URIReference);
    assertTrue(t.getObject() instanceof BlankNode);
    assertEquals("foo:sub", ((URIReference)t.getSubject()).toString());
    assertEquals("foo:pred", ((URIReference)t.getPredicate()).toString());

    try {
      t = new LocalTriple("foo:sub", null, "foo:obj");
      fail("Illegal blank node in predicate");
    } catch (BadRequestException e) { }
  }


  public void testLiterals() throws Exception {
    LocalTriple t = new LocalTriple("foo:bar", "foo:pred", "foo bar");
    assertTrue(t.getSubject() instanceof URIReference);
    assertTrue(t.getPredicate() instanceof URIReference);
    assertTrue(t.getObject() instanceof Literal);
    assertEquals("foo:bar", ((URIReference)t.getSubject()).toString());
    assertEquals("foo:pred", ((URIReference)t.getPredicate()).toString());
    assertEquals("foo bar", ((Literal)t.getObject()).getLexicalForm());

    try {
      t = new LocalTriple("foo:sub", "foo pred", "foo:bar");
      fail("Illegal literal in predicate: " + t.getPredicate());
    } catch (BadRequestException e) { }

    try {
      t = new LocalTriple("foo sub", "foo:pred", "foo:obj");
      fail("Illegal literal in subject: " + t.getSubject());
    } catch (BadRequestException e) { }
  }


  public void testLiteralTypes() throws Exception {
    LocalTriple t = new LocalTriple("foo:bar", "foo:pred", "foo bar");
    assertTrue(t.getObject() instanceof Literal);
    Literal l = (Literal)t.getObject();
    assertEquals("foo bar", l.getLexicalForm());
    assertNull(l.getLanguage());
    assertNull(l.getDatatype());

    t = new LocalTriple("foo:bar", "foo:pred", "'foo bar'^^foo:bar");
    assertTrue(t.getObject() instanceof Literal);
    l = (Literal)t.getObject();
    assertEquals("foo bar", l.getLexicalForm());
    assertNull(l.getLanguage());
    assertEquals(URI.create("foo:bar").toString(), l.getDatatype().toString());

    t = new LocalTriple("foo:bar", "foo:pred", "'foo bar'^^foo bar");
    assertTrue(t.getObject() instanceof Literal);
    l = (Literal)t.getObject();
    assertEquals("'foo bar'^^foo bar", l.getLexicalForm());
    assertNull(l.getLanguage());
    assertNull(l.getDatatype());

    t = new LocalTriple("foo:bar", "foo:pred", "foobar'^^foo:bar");
    assertTrue(t.getObject() instanceof Literal);
    l = (Literal)t.getObject();
    assertEquals("foobar'^^foo:bar", l.getLexicalForm());
    assertNull(l.getLanguage());
    assertNull(l.getDatatype());

    t = new LocalTriple("foo:bar", "foo:pred", "'foobar^^foo:bar");
    assertTrue(t.getObject() instanceof Literal);
    l = (Literal)t.getObject();
    assertEquals("'foobar^^foo:bar", l.getLexicalForm());
    assertNull(l.getLanguage());
    assertNull(l.getDatatype());

  }


  public void testLiteralLang() throws Exception {
    LocalTriple t = new LocalTriple("foo:bar", "foo:pred", "'foobar'@en");
    assertTrue(t.getObject() instanceof Literal);
    Literal l = (Literal)t.getObject();
    assertEquals("foobar", l.getLexicalForm());
    assertEquals("en", l.getLanguage());
    assertNull(l.getDatatype());

    t = new LocalTriple("foo:bar", "foo:pred", "'foobar'@en ");
    assertTrue(t.getObject() instanceof Literal);
    l = (Literal)t.getObject();
    assertEquals("'foobar'@en ", l.getLexicalForm());
    assertNull(l.getLanguage());
    assertNull(l.getDatatype());
  }


  public void testLiteralQuote() throws Exception {
    LocalTriple t = new LocalTriple("foo:bar", "foo:pred", "'''foobar'''");
    assertTrue(t.getObject() instanceof Literal);
    Literal l = (Literal)t.getObject();
    assertEquals("foobar", l.getLexicalForm());
    assertNull(l.getLanguage());
    assertNull(l.getDatatype());

    t = new LocalTriple("foo:bar", "foo:pred", "'''foobar'''@en");
    assertTrue(t.getObject() instanceof Literal);
    l = (Literal)t.getObject();
    assertEquals("foobar", l.getLexicalForm());
    assertEquals("en", l.getLanguage());
    assertNull(l.getDatatype());

    t = new LocalTriple("foo:bar", "foo:pred", "''foobar''");
    assertTrue(t.getObject() instanceof Literal);
    l = (Literal)t.getObject();
    assertEquals("''foobar''", l.getLexicalForm());
    assertNull(l.getLanguage());
    assertNull(l.getDatatype());
  }
}
