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

// Java 2 standard packages
import java.util.*;

// JUnit
import junit.framework.*;

// Log4J
import org.apache.log4j.Logger;

// locally written packages
import org.mulgara.query.Variable;

/**
 * Test case for {@link OrderedProjection}.
 *
 * @created 2003-01-10
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
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class OrderedProjectionUnitTest extends TestCase {

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(OrderedProjectionUnitTest.class);

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public OrderedProjectionUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    return new TestSuite(OrderedProjectionUnitTest.class);
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
   * Test {@link OrderedProjection}. When passed a single argument, the result
   * should be identical to that argument.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testOneOperand() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");

    assertEquals(new MemoryTuples(y, 2).or(y, 4),
        new MemoryTuples(
        new OrderedProjection(new MemoryTuples(x, 1).and(y, 2).or(x, 3).and(y, 4),
        Arrays.asList(new Variable[] {
        y}))));
  }
}
