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
 * Test case for {@link StreamedTqlXMLAnswer}.
 *
 * @created Jul 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StreamedTqlXMLAnswerUnitTest extends TestCase {

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

  /**
   * Constructs a new answer test with the given name.
   * @param name the name of the test
   */
  public StreamedTqlXMLAnswerUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new StreamedTqlXMLAnswerUnitTest("testEmptyConstructor"));
    suite.addTest(new StreamedTqlXMLAnswerUnitTest("testEmptyConstructorPretty"));
    suite.addTest(new StreamedTqlXMLAnswerUnitTest("testBooleanAnswer"));
    suite.addTest(new StreamedTqlXMLAnswerUnitTest("testBooleanAnswerPretty"));
    suite.addTest(new StreamedTqlXMLAnswerUnitTest("testPrettyPrint"));
    suite.addTest(new StreamedTqlXMLAnswerUnitTest("testCompactPrint"));
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
    StreamedTqlXMLAnswer a = new StreamedTqlXMLAnswer(empty, output);
    a.emit();
    assertEquals(EMPTY, output.toString());
  }


  /**
   * Tests an empty answer.
   * @throws Exception On error
   */
  public void testEmptyConstructorPretty() throws Exception {
    StreamedTqlXMLAnswer a = new StreamedTqlXMLAnswer(empty, output);
    a.setPrettyPrint(true);
    a.emit();
    assertEquals(EMPTY_P, output.toString());
    a.setPrettyPrint(false);
    output.reset();
    a.emit();
    assertEquals(EMPTY, output.toString());
  }


  /**
   * Tests a boolean answer.
   * @throws Exception On error
   */
  public void testBooleanAnswer() throws Exception {
    StreamedTqlXMLAnswer a = new StreamedTqlXMLAnswer(new BooleanAnswer(true), output);
    a.emit();
    assertEquals(TRUE, output.toString());
  }


  /**
   * Tests a boolean answer.
   * @throws Exception On error
   */
  public void testBooleanAnswerPretty() throws Exception {
    StreamedTqlXMLAnswer a = new StreamedTqlXMLAnswer(new BooleanAnswer(true), output);
    a.emit();
    assertEquals(TRUE, output.toString());
    a.setPrettyPrint(true);
    output.reset();
    a.emit();
    assertEquals(TRUE_P, output.toString());
    a.setPrettyPrint(false);
    output.reset();
    a.emit();
    assertEquals(TRUE, output.toString());
  }


  /**
   * Test main structure.
   */
  public void testPrettyPrint() throws Exception {
    StreamedTqlXMLAnswer a = new StreamedTqlXMLAnswer(answer, output);
    a.setPrettyPrint(true);
    a.emit();
    assertEquals(ANSWER_P, output.toString());
  }


  /**
   * Test main structure.
   */
  public void testCompactPrint() throws Exception {
    StreamedTqlXMLAnswer a = new StreamedTqlXMLAnswer(answer, output);
    a.emit();
    assertEquals(ANSWER, output.toString());
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
  }

  /**
   * Clean up the test answer.
   */
  public void tearDown() {
    answer.close();
    empty.close();
  }

  static final String EMPTY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<answer xmlns=\"http://mulgara.org/tql#\"><query><variables></variables></query></answer>";

  static final String EMPTY_P = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<answer xmlns=\"http://mulgara.org/tql#\">\n" +
      "  <query>\n" +
      "    <variables>\n" +
      "    </variables>\n" +
      "  </query>\n" +
      "</answer>";

  static final String TRUE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<answer xmlns=\"http://mulgara.org/tql#\">" +
      "<query><variables><k0/></variables>" +
      "<solution><k0 datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</k0></solution>" +
      "</query></answer>";

  static final String TRUE_P = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<answer xmlns=\"http://mulgara.org/tql#\">\n" +
      "  <query>\n" +
      "    <variables>\n" +
      "      <k0/>\n" +
      "    </variables>\n" +
      "    <solution>\n" +
      "      <k0 datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</k0>\n" +
      "    </solution>\n" +
      "  </query>\n" +
      "</answer>";

  static final String ANSWER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<answer xmlns=\"http://mulgara.org/tql#\">" +
      "<query><variables><x/><y/></variables>" +
      "<solution><x>X1</x><y resource=\"urn:y1\"/></solution>" +
      "<solution><x language=\"en\">X2</x><y blank-node=\"_node42\"/></solution>" +
      "</query>" +
      "</answer>";

  static final String ANSWER_P = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<answer xmlns=\"http://mulgara.org/tql#\">\n" +
      "  <query>\n" +
      "    <variables>\n" +
      "      <x/>\n" +
      "      <y/>\n" +
      "    </variables>\n" +
      "    <solution>\n" +
      "      <x>X1</x>\n" +
      "      <y resource=\"urn:y1\"/>\n" +
      "    </solution>\n" +
      "    <solution>\n" +
      "      <x language=\"en\">X2</x>\n" +
      "      <y blank-node=\"_node42\"/>\n" +
      "    </solution>\n" +
      "  </query>\n" +
      "</answer>";
}
