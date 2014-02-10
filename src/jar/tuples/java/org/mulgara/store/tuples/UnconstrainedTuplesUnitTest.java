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

// JUnit
import junit.framework.*;

// Log4J
import org.apache.log4j.Logger;

/**
 * Test case for {@link UnconstrainedTuplesUnitTest}.
 *
 * @created 2003-01-10
 *
 * @author Andrew Newman
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
public class UnconstrainedTuplesUnitTest extends TestCase {

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(UnconstrainedTuplesUnitTest.class);

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public UnconstrainedTuplesUnitTest(String name) {

    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(new UnconstrainedTuplesUnitTest("testGeneral"));
    return suite;
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
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void setUp() throws Exception {

    // null implementation
  }

  //
  // Test cases
  //

  /**
   * Tests general properties of Unconstrained Tuples.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testGeneral() throws Exception {

    // Should be materialized.
    assertTrue(UnconstrainedTuples.getInstance().isMaterialized());

    // Should have no variables.
    assertEquals(0, UnconstrainedTuples.getInstance().getNumberOfVariables());

    // Should be 1 row.
    assertEquals(1, UnconstrainedTuples.getInstance().getRowCount());

    // Test operation of before first and next.
    UnconstrainedTuples.getInstance().beforeFirst();

    // Should have one row.
    assertTrue(UnconstrainedTuples.getInstance().next());

    // And no more.
    assertTrue(!UnconstrainedTuples.getInstance().next());

    // Should be unconstrained.
    assertTrue(UnconstrainedTuples.getInstance().isUnconstrained());

    // No comparator
    assertTrue(UnconstrainedTuples.getInstance().getComparator() != null);

    // Singleton.
    Tuples test1 = UnconstrainedTuples.getInstance();
    assertEquals("Two objects should be the same", test1,
        UnconstrainedTuples.getInstance());
  }
}
