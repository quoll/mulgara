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

package org.mulgara.protocol;


// JUnit
import junit.framework.*;

// Java 2 standard packages
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Locally written packages
import org.mulgara.query.AnswerImpl;
import org.mulgara.query.BooleanAnswer;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.util.ResultSetRow;
import org.mulgara.util.MemoryResultSet;

/**
 * Test case for {@link StreamedSparqlXMLAnswer}.
 *
 * @created Jul 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StreamedSparqlXMLAnswerUnitTest extends TestCase {

  /**
   * Test instance.
   * <table>
   *   <thead>
   *     <tr><th>x</th> <th>y</th></tr>
   *   </thead>
   *   <tbody>
   *     <tr><td>X1</td><td>Y1</td></tr>
   *     <tr><td>X2</td><td>Y2</td></tr>
   *   </tbody>
   * </table>
   */
  private AnswerImpl answer;

  private AnswerImpl empty;

  private Map<String,String> namesp = new HashMap<String,String>();

  private ByteArrayOutputStream output;

  private ByteArrayOutputStream outputb;

  private ByteArrayOutputStream outputa;

  private static final URI REL_URI = URI.create("rel/uri");
  
  private static final String NAME1 = "ns1";

  private static final String NAME2 = "ns2";

  private static final URI NS1 = URI.create("http://mulgara.org/test/ns1");

  private static final URI NS2 = URI.create("http://mulgara.org/test/ns2");

  /**
   * Constructs a new answer test with the given name.
   * @param name the name of the test
   */
  public StreamedSparqlXMLAnswerUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new StreamedSparqlXMLAnswerUnitTest("testEmptyConstructor"));
    suite.addTest(new StreamedSparqlXMLAnswerUnitTest("testEmptyConstructorPretty"));
    suite.addTest(new StreamedSparqlXMLAnswerUnitTest("testBooleanAnswer"));
    suite.addTest(new StreamedSparqlXMLAnswerUnitTest("testBooleanAnswerPretty"));
    suite.addTest(new StreamedSparqlXMLAnswerUnitTest("testPrettyPrint"));
    suite.addTest(new StreamedSparqlXMLAnswerUnitTest("testCompactPrint"));
    suite.addTest(new StreamedSparqlXMLAnswerUnitTest("testPrettyPrintVariations"));
    suite.addTest(new StreamedSparqlXMLAnswerUnitTest("testCompactPrintVariations"));
    return suite;
  }

  /**
   * Default text runner.
   * @param args The command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }


  /**
   * Tests an empty answer.
   * @throws Exception On error
   */
  public void testEmptyConstructor() throws Exception {
    StreamedSparqlXMLAnswer a = new StreamedSparqlXMLAnswer(empty, output);
    a.setPrettyPrint(false);
    a.emit();
    assertEquals(getEmpty(), output.toString());
  }


  /**
   * Tests an empty answer.
   * @throws Exception On error
   */
  public void testEmptyConstructorPretty() throws Exception {
    StreamedSparqlXMLAnswer a = new StreamedSparqlXMLAnswer(empty, output);
    a.emit();
    assertEquals(getEmptyP(), output.toString());

    a.setPrettyPrint(false);
    output.reset();
    a.emit();
    assertEquals(getEmpty(), output.toString());
  }


  /**
   * Tests a boolean answer.
   * @throws Exception On error
   */
  public void testBooleanAnswer() throws Exception {
    StreamedSparqlXMLAnswer a = new StreamedSparqlXMLAnswer(true, output);
    a.setPrettyPrint(false);
    a.emit();
    assertEquals(getTrue(), output.toString());

    a = new StreamedSparqlXMLAnswer(new BooleanAnswer(true), output);
    a.setPrettyPrint(false);
    output.reset();
    a.emit();
    assertEquals(getTrue(), output.toString());
  }


  /**
   * Tests a boolean answer.
   * @throws Exception On error
   */
  public void testBooleanAnswerPretty() throws Exception {
    StreamedSparqlXMLAnswer a = new StreamedSparqlXMLAnswer(true, output);
    a.emit();
    assertEquals(getTrueP(), output.toString());
    a.setPrettyPrint(false);
    output.reset();
    a.emit();
    assertEquals(getTrue(), output.toString());
  }


  /**
   * Test main structure.
   */
  public void testPrettyPrint() throws Exception {
    StreamedSparqlXMLAnswer a = new StreamedSparqlXMLAnswer(answer, output);
    a.emit();
    assertEquals(getAnswerP(), output.toString());
  }


  /**
   * Test main structure.
   */
  public void testCompactPrint() throws Exception {
    StreamedSparqlXMLAnswer a = new StreamedSparqlXMLAnswer(answer, output);
    a.setPrettyPrint(false);
    a.emit();
    assertEquals(getAnswer(), output.toString());
  }


  /**
   * Tests the variations of answers.
   * @throws Exception On error
   */
  public void testCompactPrintVariations() throws Exception {
    StreamedSparqlXMLAnswer e = new StreamedSparqlXMLAnswer(empty, REL_URI, output);
    StreamedSparqlXMLAnswer b = new StreamedSparqlXMLAnswer(true, REL_URI, outputb);
    StreamedSparqlXMLAnswer a = new StreamedSparqlXMLAnswer(answer, REL_URI, outputa);
    e.setPrettyPrint(false);
    b.setPrettyPrint(false);
    a.setPrettyPrint(false);

    // No namespaces, no schema, meta set
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(null, false, true), output.toString());
    assertEquals(getTrue(null, false, true), outputb.toString());
    assertEquals(getAnswer(null, false, true), outputa.toString());

    e.useW3CSchema(true);
    b.useW3CSchema(true);
    a.useW3CSchema(true);

    // No namespaces, schema set, meta set
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(null, true, true), output.toString());
    assertEquals(getTrue(null, true, true), outputb.toString());
    assertEquals(getAnswer(null, true, true), outputa.toString());

    e.addNamespace(NAME1, NS1);
    e.addNamespace(NAME2, NS2);
    b.addNamespace(NAME1, NS1);
    b.addNamespace(NAME2, NS2);
    a.addNamespace(NAME1, NS1);
    a.addNamespace(NAME2, NS2);

    // Namespaces set, schema set, meta set
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(namesp, true, true), output.toString());
    assertEquals(getTrue(namesp, true, true), outputb.toString());
    assertEquals(getAnswer(namesp, true, true), outputa.toString());

    e.useW3CSchema(false);
    b.useW3CSchema(false);
    a.useW3CSchema(false);

    // Namespaces set, no schema, meta set
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(namesp, false, true), output.toString());
    assertEquals(getTrue(namesp, false, true), outputb.toString());
    assertEquals(getAnswer(namesp, false, true), outputa.toString());
    
    e = new StreamedSparqlXMLAnswer(empty, output);
    b = new StreamedSparqlXMLAnswer(true, outputb);
    a = new StreamedSparqlXMLAnswer(answer, outputa);

    e.setPrettyPrint(false);
    b.setPrettyPrint(false);
    a.setPrettyPrint(false);

    e.useW3CSchema(true);
    b.useW3CSchema(true);
    a.useW3CSchema(true);

    // No namespaces, schema set, no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(null, true, false), output.toString());
    assertEquals(getTrue(null, true, false), outputb.toString());
    assertEquals(getAnswer(null, true, false), outputa.toString());

    e.addNamespace(NAME1, NS1);
    e.addNamespace(NAME2, NS2);
    b.addNamespace(NAME1, NS1);
    b.addNamespace(NAME2, NS2);
    a.addNamespace(NAME1, NS1);
    a.addNamespace(NAME2, NS2);

    // Namespaces set, schema set, no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(namesp, true, false), output.toString());
    assertEquals(getTrue(namesp, true, false), outputb.toString());
    assertEquals(getAnswer(namesp, true, false), outputa.toString());

    e.useW3CSchema(false);
    b.useW3CSchema(false);
    a.useW3CSchema(false);

    // Namespaces set, no schema, no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(namesp, false, false), output.toString());
    assertEquals(getTrue(namesp, false, false), outputb.toString());
    assertEquals(getAnswer(namesp, false, false), outputa.toString());

    e.clearNamespaces();
    b.clearNamespaces();
    a.clearNamespaces();

    // No Namespaces, no schema, no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(null, false, false), output.toString());
    assertEquals(getTrue(null, false, false), outputb.toString());
    assertEquals(getAnswer(null, false, false), outputa.toString());

    e.useW3CSchema(true);
    b.useW3CSchema(true);
    a.useW3CSchema(true);

    // No namespaces, schema set, no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(null, true, false), output.toString());
    assertEquals(getTrue(null, true, false), outputb.toString());
    assertEquals(getAnswer(null, true, false), outputa.toString());
  }


  /**
   * Tests the variations of answers.
   * @throws Exception On error
   */
  public void testPrettyPrintVariations() throws Exception {
    StreamedSparqlXMLAnswer e = new StreamedSparqlXMLAnswer(empty, REL_URI, output);
    StreamedSparqlXMLAnswer b = new StreamedSparqlXMLAnswer(true, REL_URI, outputb);
    StreamedSparqlXMLAnswer a = new StreamedSparqlXMLAnswer(answer, REL_URI, outputa);
    e.setPrettyPrint(true);
    b.setPrettyPrint(true);
    a.setPrettyPrint(true);

    // No namespaces, no schema, meta set
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmptyP(null, false, true), output.toString());
    assertEquals(getTrueP(null, false, true), outputb.toString());
    assertEquals(getAnswerP(null, false, true), outputa.toString());

    e.useW3CSchema(true);
    b.useW3CSchema(true);
    a.useW3CSchema(true);

    // No namespaces, schema set, meta set
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmptyP(null, true, true), output.toString());
    assertEquals(getTrueP(null, true, true), outputb.toString());
    assertEquals(getAnswerP(null, true, true), outputa.toString());

    e.addNamespace(NAME1, NS1);
    e.addNamespace(NAME2, NS2);
    b.addNamespace(NAME1, NS1);
    b.addNamespace(NAME2, NS2);
    a.addNamespace(NAME1, NS1);
    a.addNamespace(NAME2, NS2);

    // Namespaces set, schema set, meta set
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmptyP(namesp, true, true), output.toString());
    assertEquals(getTrueP(namesp, true, true), outputb.toString());
    assertEquals(getAnswerP(namesp, true, true), outputa.toString());

    e.useW3CSchema(false);
    b.useW3CSchema(false);
    a.useW3CSchema(false);

    // Namespaces set, no schema, meta set
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmptyP(namesp, false, true), output.toString());
    assertEquals(getTrueP(namesp, false, true), outputb.toString());
    assertEquals(getAnswerP(namesp, false, true), outputa.toString());
    
    e = new StreamedSparqlXMLAnswer(empty, output);
    b = new StreamedSparqlXMLAnswer(true, outputb);
    a = new StreamedSparqlXMLAnswer(answer, outputa);

    e.useW3CSchema(true);
    b.useW3CSchema(true);
    a.useW3CSchema(true);

    // No namespaces, schema set, no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmptyP(null, true, false), output.toString());
    assertEquals(getTrueP(null, true, false), outputb.toString());
    assertEquals(getAnswerP(null, true, false), outputa.toString());

    e.addNamespace(NAME1, NS1);
    e.addNamespace(NAME2, NS2);
    b.addNamespace(NAME1, NS1);
    b.addNamespace(NAME2, NS2);
    a.addNamespace(NAME1, NS1);
    a.addNamespace(NAME2, NS2);

    // Namespaces set, schema set, no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmptyP(namesp, true, false), output.toString());
    assertEquals(getTrueP(namesp, true, false), outputb.toString());
    assertEquals(getAnswerP(namesp, true, false), outputa.toString());

    e.useW3CSchema(false);
    b.useW3CSchema(false);
    a.useW3CSchema(false);

    // Namespaces set, no schema, no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmptyP(namesp, false, false), output.toString());
    assertEquals(getTrueP(namesp, false, false), outputb.toString());
    assertEquals(getAnswerP(namesp, false, false), outputa.toString());

    e.clearNamespaces();
    b.clearNamespaces();
    a.clearNamespaces();

    // No Namespaces, no schema, no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmptyP(null, false, false), output.toString());
    assertEquals(getTrueP(null, false, false), outputb.toString());
    assertEquals(getAnswerP(null, false, false), outputa.toString());

    e.useW3CSchema(true);
    b.useW3CSchema(true);
    a.useW3CSchema(true);

    // No namespaces, schema set, no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmptyP(null, true, false), output.toString());
    assertEquals(getTrueP(null, true, false), outputb.toString());
    assertEquals(getAnswerP(null, true, false), outputa.toString());
  }


  /**
   * Populate the test answer.
   * @throws Exception Error setting up the ResultSet
   */
  protected void setUp() throws Exception {
    MemoryResultSet trs1 = new MemoryResultSet(new String[] { "x", "y" });
    ResultSetRow row;
    row = new ResultSetRow(trs1);
    row.setObject("x", new LiteralImpl("X1"));
    row.setObject("y", new URIReferenceImpl(URI.create("urn:y1")));
    trs1.addRow(row);
    row = new ResultSetRow(trs1);
    row.setObject("x", new LiteralImpl("X2", "en"));
    row.setObject("y", new BlankNodeImpl(42));
    trs1.addRow(row);
    answer = new AnswerImpl(trs1);

    List<Variable> variables = Arrays.asList(new Variable[] { new Variable("x") });
    empty = new AnswerImpl(variables);
    
    namesp.put(NAME1, NS1.toString());
    namesp.put(NAME2, NS2.toString());

    output = new ByteArrayOutputStream();
    outputb = new ByteArrayOutputStream();
    outputa = new ByteArrayOutputStream();
  }

  /**
   * Clean up the test answer.
   */
  public void tearDown() {
    answer.close();
    empty.close();
  }

  /////////////////////////////
  // start of getEmpty variants
  /////////////////////////////

  private static String getEmpty() {
    return getEmpty(null, false, false);
  }

  private static String getEmptyP() {
    return getEmptyP(null, false, false);
  }

  private static String getEmpty(Map<String,String> ns, boolean schema, boolean meta) {
    return getCommonStart(ns, schema, meta) + EMPTY_BODY;
  }

  private static String getEmptyP(Map<String,String> ns, boolean schema, boolean meta) {
    return getCommonStartP(ns, schema, meta) + EMPTY_BODY_P;
  }

  ////////////////////////////
  // start of getTrue variants
  ////////////////////////////

  private static String getTrue() {
    return getTrue(null, false, false);
  }

  private static String getTrueP() {
    return getTrueP(null, false, false);
  }

  private static String getTrue(Map<String,String> ns, boolean schema, boolean meta) {
    return getCommonStart(ns, schema, meta) + TRUE_BODY;
  }

  private static String getTrueP(Map<String,String> ns, boolean schema, boolean meta) {
    return getCommonStartP(ns, schema, meta) + TRUE_BODY_P;
  }

  //////////////////////////////
  // start of getAnswer variants
  //////////////////////////////

  private static String getAnswer() {
    return getAnswer(null, false, false);
  }

  private static String getAnswerP() {
    return getAnswerP(null, false, false);
  }

  private static String getAnswer(Map<String,String> ns, boolean schema, boolean meta) {
    return getShortCommonStart(ns, schema) + ANSWER_VARS + getMeta(meta) + ANSWER_BODY;
  }

  private static String getAnswerP(Map<String,String> ns, boolean schema, boolean meta) {
    return getShortCommonStartP(ns, schema) + ANSWER_VARS_P + getMetaP(meta) + ANSWER_BODY_P;
  }

  ////////////////
  // common header
  ////////////////

  private static String getCommonStart(Map<String,String> ns, boolean schema, boolean meta) {
    return getShortCommonStart(ns, schema) + getMeta(meta);
  }

  private static String getCommonStartP(Map<String,String> ns, boolean schema, boolean meta) {
    return getShortCommonStartP(ns, schema) + getMetaP(meta);
  }

  private static String getShortCommonStart(Map<String,String> ns, boolean schema) {
    String result = DOC_HEAD + SPARQL_HEAD;
    if (ns != null) {
      for (String k: ns.keySet()) result += " " + k + "=\"" + ns.get(k) + "\"";
    }
    if (schema) result += " " + SPARQL_HEAD_ATTR;
    result += ">" + EMPTY_HEAD;
    return result;
  }

  private static String getShortCommonStartP(Map<String,String> ns, boolean schema) {
    String result = DOC_HEAD + SPARQL_HEAD;
    if (ns != null) {
      for (String k: ns.keySet()) result += "\n" + SPARQL_HEAD_INDENT + k + "=\"" + ns.get(k) + "\"";
    }
    if (schema) result += "\n" + SPARQL_HEAD_INDENT + SPARQL_HEAD_ATTR;
    result += ">\n  " + EMPTY_HEAD + "\n";
    return result;
  }

  private static String getMeta(boolean meta) {
    return meta ? HEAD_META : "";
  }

  private static String getMetaP(boolean meta) {
    return meta ? HEAD_META_INDENT + HEAD_META + "\n" : "";
  }

  static final String DOC_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

  static final String SPARQL_HEAD = "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"";

  static final String SPARQL_HEAD_ATTR = "xsi:schemaLocation=\"http://www.w3.org/2007/SPARQL/result.xsd\"";

  static final String SPARQL_HEAD_INDENT = "        ";
  
  static final String HEAD_META = "<link href=\"" + REL_URI + "\"/>";

  static final String HEAD_META_INDENT = "    ";

  static final String EMPTY_HEAD = "<head>";

  static final String EMPTY_BODY = "</head><results></results></sparql>";

  static final String EMPTY_BODY_P = "  </head>\n" +
      "  <results>\n" +
      "  </results>\n" +
      "</sparql>";

  static final String TRUE_BODY = "</head><boolean>true</boolean></sparql>";

  static final String TRUE_BODY_P = "  </head>\n" +
      "  <boolean>true</boolean>\n" +
      "</sparql>";

  static final String ANSWER_VARS = "<variable name=\"x\"/><variable name=\"y\"/>";

  static final String ANSWER_BODY = "</head>" +
      "<results>" +
      "<result><binding name=\"x\"><literal>X1</literal></binding>" +
      "<binding name=\"y\"><uri>urn:y1</uri></binding></result>" +
      "<result><binding name=\"x\"><literal xml:lang=\"en\">X2</literal></binding>" +
      "<binding name=\"y\"><bnode>_node42</bnode></binding></result>" +
      "</results>" +
      "</sparql>";

  static final String ANSWER_VARS_P = "    <variable name=\"x\"/>\n" +
      "    <variable name=\"y\"/>\n";

  static final String ANSWER_BODY_P = "  </head>\n" +
      "  <results>\n" +
      "    <result>\n" +
      "      <binding name=\"x\">\n" +
      "        <literal>X1</literal>\n" +
      "      </binding>\n" +
      "      <binding name=\"y\">\n" +
      "        <uri>urn:y1</uri>\n" +
      "      </binding>\n" +
      "    </result>\n" +
      "    <result>\n" +
      "      <binding name=\"x\">\n" +
      "        <literal xml:lang=\"en\">X2</literal>\n" +
      "      </binding>\n" +
      "      <binding name=\"y\">\n" +
      "        <bnode>_node42</bnode>\n" +
      "      </binding>\n" +
      "    </result>\n" +
      "  </results>\n" +
      "</sparql>";
}
