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

package org.mulgara.store.tuples;

// Third party packages
import junit.framework.*;          // JUnit
import org.apache.log4j.Logger;  // Log4J

// Locally written packages
import org.mulgara.query.Variable;

/**
 * Test case for {@link DistinctTuples}.
 *
 * @created 2003-02-05
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:10 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class DistinctTuplesUnitTest extends TestCase {

  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(DistinctTuplesUnitTest.class);

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public DistinctTuplesUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();

     suite.addTest(new DistinctTuplesUnitTest("test1"));
     return suite;
  }

  /**
   * Create test instance.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void setUp() throws Exception {

    // null implementation
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
   * Test.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void test1() throws Exception {

    Variable x = new Variable("x");

    assertEquals(new MemoryTuples(TuplesOperations.empty()),
        new MemoryTuples(TuplesOperations.removeDuplicates(TuplesOperations.empty())));

    assertEquals(new MemoryTuples(TuplesOperations.unconstrained()),
        new MemoryTuples(TuplesOperations.removeDuplicates(
        TuplesOperations.unconstrained())));

    MemoryTuples expectedTuples = new MemoryTuples(x, 2).or(x, 4);

    MemoryTuples test1 = new MemoryTuples(x,2).or(x,4);
    MemoryTuples test2 = new MemoryTuples(x,2).or(x,2).or(x, 4);

    Tuples undup1 = TuplesOperations.removeDuplicates(test1);
    assertEquals(expectedTuples, new MemoryTuples(undup1));
    undup1.close();

    Tuples undup2 = TuplesOperations.removeDuplicates(test2);
    assertEquals(expectedTuples, new MemoryTuples(undup2));
    undup2.close();
  }
}
