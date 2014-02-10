/*
 * Copyright 2008 Fedora Commons, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.util;

// third party packages
import junit.framework.*;

import org.apache.log4j.Logger;

/**
 * Test case for {@link SparqlUtil}.
 *
 * @created Oct 13, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class SparqlUtilUnitTest extends TestCase {

  /** Logger. Named after the class. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(SparqlUtilUnitTest.class);


  /**
   * Creates the named test
   * @param name The test name
   */
  public SparqlUtilUnitTest(String name) {
    super(name);
  }


  /**
   * Creates a test suite with various different output and compares the output.
   * @return The test suite
   */
  public static TestSuite suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new SparqlUtilUnitTest("testSparqlSelect"));
    suite.addTest(new SparqlUtilUnitTest("testTqlSelect"));
    suite.addTest(new SparqlUtilUnitTest("testTql"));
    suite.addTest(new SparqlUtilUnitTest("testSparql"));
    return suite;
  }


  /**
   * Default test runner.
   * @param args The command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }


  /**
   * Test that {@link SparqlUtil#looksLikeSparql(String)} can recognize a simple SELECT.
   */
  public void testSparqlSelect() throws Exception {
    String s1 = "select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select ?x ?y ?z from <rmi://localhost/server1#> where{?x ?y ?z}";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select ?x ?y ?z where { ?x ?y ?z }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select ?x ?y ?z where{?x ?y ?z}";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select * where { ?x ?y ?z }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select *{?x ?y ?z}";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select * {?x ?y ?z}";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select * { ?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }' }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select*{?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }' }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select$x{?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }' }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select ?x ?y from <rmi://localhost/server1#> where { ?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }' }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select ?x ?y from <rmi://localhost/server1#> where {?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }'}";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select ?x ?y from <rmi://localhost/server1#>\nfrom named <http://example.org/alice>\nwhere { ?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }' }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select ?x ?y where { ?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }' }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select ?x ?y where {?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }'}";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));
  }


  /**
   * Test that {@link SparqlUtil#looksLikeSparql(String)} can avoid TQL.
   */
  public void testTqlSelect() throws Exception {
    String s1 = "select ?x ?y ?z from <rmi://localhost/server1#> where ?x ?y ?z;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toUpperCase()));
    
    s1 = "select ?x ?y from <rmi://localhost/server1#> where ?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }' ;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select ?x ?y from <rmi://localhost/server1#> where\n?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where { ?x ?y ?z }' ;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "select ?x ?y from <rmi://localhost/server1#> where ?x ?y 'select ?x ?y ?z from <rmi://localhost/server1#> where {?x ?y ?z}' ;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toUpperCase()));
  }


  /**
   * Test that {@link SparqlUtil#looksLikeSparql(String)} can see non-SELECT SPARQL.
   */
  public void testSparql() throws Exception {
    String s1 = "prefix foaf: <http://xmlns.com/foaf/0.1/> select ?x ?y ?z from <rmi://localhost/server1#> where ?x ?y ?z;";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toUpperCase()));

    s1 = "CONSTRUCT { <http://example.org/person#Alice> vcard:FN ?name } WHERE { ?x foaf:name ?name }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "CONSTRUCT{<http://example.org/person#Alice> vcard:FN ?name}WHERE{?x foaf:name ?name}";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "ASK  { ?x foaf:name  \"Alice\" }";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "ASK{?x foaf:name \"Alice\"}";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "DESCRIBE ?x ?y <http://example.org/>\nWHERE    {?x foaf:knows ?y}";
    assertTrue(SparqlUtil.looksLikeSparql(s1));
    assertTrue(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

  }


  /**
   * Test that {@link SparqlUtil#looksLikeSparql(String)} can avoid non-SELECT TQL.
   */
  public void testTql() throws Exception {
    String s1 = "alias <http://purl.org/dc/elements/1.1/> as dcns;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "create <rmi://mysite.com/server1#MyModel>;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "delete select $s $p $o from <rmi://mysite.com/server1#model1>\nwhere $s $p $o from <rmi://mysite.com/server1#model2>;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "drop <rmi://mysite.com/server1#model1>;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "execute <file:/usr/local/tks/sample/itql/loaddc.itql>;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "help create;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "insert <ns:subject> <ns:predicate> $node\n$node <ns:predicate2> 'object2' into <rmi://mysite.com/server1#model> ;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "insert select $s $p $o from <rmi://mysite.com/server1#model1>\nwhere $s $p $o into <rmi://mysite.com/server1#model2>;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "load remote <file:/tmp/mydata.rdf> into <rmi://mysite.com/Server1#Graph1>;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "quit;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "restore <rmi://mysite.com/server1> from remote <file:/tmp/serverbackup.gz>;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "rollback;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));

    s1 = "set time on;";
    assertFalse(SparqlUtil.looksLikeSparql(s1));
    assertFalse(SparqlUtil.looksLikeSparql(s1.toLowerCase()));
  }


  /**
   * Populate the test object.
   */
  protected void setUp() {
  }
}
