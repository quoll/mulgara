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

package org.mulgara.content.n3;

// Java 2 standard packages
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

// Third party packages
import junit.framework.*;        // JUnit unit testing framework
import org.apache.log4j.Logger;  // Apache Log4J
import org.jrdf.vocabulary.RDF;  // Java RDF API

// Locally written packages
import org.mulgara.content.Content;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.TestResolverSession;

/**
 * Test suite for {@link N3Statements}.
 *
 * @created 2004-09-17
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:02 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.tucanatech.com/">Tucan
 *   Technology Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class N3StatementsUnitTest extends TestCase {
  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(N3StatementsUnitTest.class.getName());

  //
  // Constructors
  //

  /**
   * Construct a test.
   *
   * @param name  the name of the test to construct
   */
  public N3StatementsUnitTest(String name)
  {
    super(name);
  }

  //
  // Methods implementing TestCase
  //

  public void setup()
  {
  }

  /**
   * Hook from which the test runner can obtain a test suite.
   *
   * @return the test suite
   */
  public static Test suite()
  {
    return new TestSuite(N3StatementsUnitTest.class);
  }

  //
  // Tests
  //

  /**
   * Test {@link N3Statements} parsing the file <tt>test0014.rdf</tt>..
   *
   * @throws Exception if there's an error running the test (note that if the
   *   test merely fails, this should <em>not</em> throw any exception
   */
  public void test1() throws Exception
  {
    File file = new File(
                  new File(new File(System.getProperty("cvs.root")), "data"),
                  "test0014.nt"
                );

    // Obtain a content handler for the test file
    Content content = (Content)
      Class.forName("org.mulgara.resolver.file.FileContent")
           .getConstructor(new Class[] { File.class })
           .newInstance(new Object[] { file });
    assert content != null;

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Seed the resolver session with the nodes occurring in the test document
    long s1 = resolverSession.localize(new URIReferenceImpl(new URI(
      "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdf-ns-prefix-confusion/test0014.rdf#container"
    )));
    long p1 = resolverSession.localize(new URIReferenceImpl(RDF.TYPE));
    long o1 = resolverSession.localize(new URIReferenceImpl(RDF.SEQ));

    long p2 = resolverSession.localize(new URIReferenceImpl(new URI(
                                         RDF.BASE_URI + "_1")));
    long o2 = resolverSession.localize(new LiteralImpl("bar"));

    try {
      N3Statements n3Statements =
        new N3Statements(content, resolverSession);

      // Validate first statement
      n3Statements.beforeFirst();
      assertTrue(n3Statements.next());
      assertEquals(s1, n3Statements.getSubject());
      assertEquals(p1, n3Statements.getPredicate());
      assertEquals(o1, n3Statements.getObject());

      // Validate second statement
      assertTrue(n3Statements.next());
      assertEquals(s1, n3Statements.getSubject());
      assertEquals(p2, n3Statements.getPredicate());
      assertEquals(o2, n3Statements.getObject());

      // No more statements
      assertFalse(n3Statements.next());
    }
    catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test {@link N3Statements} parsing the file <tt>test001.rdf</tt>..
   *
   * This is intended to exercise the requirement that bnodes within the
   * document be recognized as equal to themselves on subsequent parses.
   *
   * @throws Exception if there's an error running the test (note that if the
   *   test merely fails, this should <em>not</em> throw any exception
   */
  /*
  public void test2() throws Exception
  {
    File file = new File(
                  new File(new File(System.getProperty("cvs.root")), "data"),
                  "test001.nt"
                );

    // Obtain a content handler for the test file
    Content content = (Content)
      Class.forName("org.mulgara.resolver.file.FileContent")
           .getConstructor(new Class[] { File.class })
           .newInstance(new Object[] { file });
    assert content != null;

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Seed the resolver session with the nodes occurring in the test document
    long p1 = resolverSession.localize(new URIReferenceImpl(new URI(
                                         "http://example.org/property")));
    long o1 = resolverSession.localize(new LiteralImpl("property value"));

    try {
      N3Statements n3Statements = new N3Statements(content, resolverSession);

      // Validate first statement
      n3Statements.beforeFirst();
      assertTrue(n3Statements.next());
      long s1 = n3Statements.getSubject();  // read the bnode
      assertEquals(p1, n3Statements.getPredicate());
      assertEquals(o1, n3Statements.getObject());

      // No more statements
      assertFalse(n3Statements.next());

      // Validate second statement
      n3Statements.beforeFirst();
      assertTrue(n3Statements.next());
      assertEquals(s1, n3Statements.getSubject());  // must match the bnode
      assertEquals(p1, n3Statements.getPredicate());
      assertEquals(o1, n3Statements.getObject());

      // No more statements
      assertFalse(n3Statements.next());
    }
    catch (Exception e) {
      fail(e);
    }
  }
  */

  //
  // Internal methods
  //

  /**
   * Fail with an unexpected exception
   */
  private void fail(Throwable throwable)
  {
    fail(throwable.getMessage(), throwable);
  }

  private void fail(String test, Throwable throwable)
  {
    StringWriter stringWriter = new StringWriter();

    if (test != null) {
      stringWriter.write(test + ": ");
    }

    throwable.printStackTrace(new PrintWriter(stringWriter));
    fail(stringWriter.toString());
  }
}
