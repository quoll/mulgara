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
import java.util.List;

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
 * Test case for {@link StreamedSparqlJSONAnswer}.
 *
 * @created Sep 1, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StreamedSparqlJSONAnswerUnitTest extends TestCase {

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

  private ByteArrayOutputStream output;

  private ByteArrayOutputStream outputb;

  private ByteArrayOutputStream outputa;

  private static final URI REL_URI = URI.create("rel/uri");
  
  /**
   * Constructs a new answer test with the given name.
   * @param name the name of the test
   */
  public StreamedSparqlJSONAnswerUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new StreamedSparqlJSONAnswerUnitTest("testEmptyConstructor"));
    suite.addTest(new StreamedSparqlJSONAnswerUnitTest("testBooleanAnswer"));
    suite.addTest(new StreamedSparqlJSONAnswerUnitTest("testCompactPrint"));
    suite.addTest(new StreamedSparqlJSONAnswerUnitTest("testCompactPrintVariations"));
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
    StreamedSparqlJSONAnswer a = new StreamedSparqlJSONAnswer(empty, output);
    a.emit();
    assertEquals(getEmpty(), output.toString());
  }


  /**
   * Tests a boolean answer.
   * @throws Exception On error
   */
  public void testBooleanAnswer() throws Exception {
    StreamedSparqlJSONAnswer a = new StreamedSparqlJSONAnswer(true, output);
    a.emit();
    assertEquals(getTrue(), output.toString());

    a = new StreamedSparqlJSONAnswer(new BooleanAnswer(true), output);
    output.reset();
    a.emit();
    assertEquals(getTrue(), output.toString());
  }


  /**
   * Test main structure.
   */
  public void testCompactPrint() throws Exception {
    StreamedSparqlJSONAnswer a = new StreamedSparqlJSONAnswer(answer, output);
    a.emit();
    assertEquals(getAnswer(), output.toString());
  }


  /**
   * Tests the variations of answers.
   * @throws Exception On error
   */
  public void testCompactPrintVariations() throws Exception {
    StreamedSparqlJSONAnswer e = new StreamedSparqlJSONAnswer(empty, REL_URI, output);
    StreamedSparqlJSONAnswer b = new StreamedSparqlJSONAnswer(true, REL_URI, outputb);
    StreamedSparqlJSONAnswer a = new StreamedSparqlJSONAnswer(answer, REL_URI, outputa);

    // meta set
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(true), output.toString());
    assertEquals(getTrue(true), outputb.toString());
    assertEquals(getAnswer(true), outputa.toString());

    e = new StreamedSparqlJSONAnswer(empty, output);
    b = new StreamedSparqlJSONAnswer(true, outputb);
    a = new StreamedSparqlJSONAnswer(answer, outputa);

    // no meta
    output.reset();
    outputb.reset();
    outputa.reset();
    e.emit();
    b.emit();
    a.emit();
    assertEquals(getEmpty(false), output.toString());
    assertEquals(getTrue(false), outputb.toString());
    assertEquals(getAnswer(false), outputa.toString());

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
    return getEmpty(false);
  }

  private static String getEmpty(boolean meta) {
    return getCommonStart(meta) + EMPTY_BODY;
  }

  ////////////////////////////
  // start of getTrue variants
  ////////////////////////////

  private static String getTrue() {
    return getTrue(false);
  }

  private static String getTrue(boolean meta) {
    return getCommonStart(meta) + TRUE_BODY;
  }

  //////////////////////////////
  // start of getAnswer variants
  //////////////////////////////

  private static String getAnswer() {
    return getAnswer(false);
  }

  private static String getAnswer(boolean meta) {
    String result = getShortCommonStart() + ANSWER_VARS;
    if (meta) result += ", ";
    return result + getMeta(meta) + ANSWER_BODY;
  }

  ////////////////
  // common header
  ////////////////

  private static String getCommonStart(boolean meta) {
    return getShortCommonStart() + getMeta(meta);
  }

  private static String getShortCommonStart() {
    String result = DOC_HEAD + SPARQL_HEAD;
    return result;
  }

  private static String getMeta(boolean meta) {
    return meta ? HEAD_META : "";
  }

  static final String DOC_HEAD = "{ ";

  static final String SPARQL_HEAD = "\"head\": {";

  static final String HEAD_META = "\"link\": [\"" + REL_URI + "\"]";

  static final String EMPTY_BODY = "}, \"results\": { \"bindings\": [  ] } }";

  static final String TRUE_BODY = "}, \"boolean\": true }";

  static final String ANSWER_VARS = "\"vars\": [\"x\", \"y\"]";

  static final String ANSWER_BODY = "}, " +
      "\"results\": { " +
      "\"bindings\": [ " +
      "{ \"x\": { \"type\": \"literal\", \"value\": \"X1\" }, " +
      "\"y\": { \"type\": \"uri\", \"value\": \"urn:y1\" } }, " +
      "{ \"x\": { \"type\": \"literal\", \"xml:lang\": \"en\", \"value\": \"X2\" }, " +
      "\"y\": { \"type\": \"bnode\", \"value\": \"_node42\" } }" +
      " ] } }";
}
