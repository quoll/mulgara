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

package org.mulgara.util;

// Third party packages
import junit.framework.*;

import java.io.*;

/**
 * Test cases for IntFile.
 *
 * @created 2001-09-20
 * @author David Makepeace
 * @version $Revision: 1.9 $
     * @modified $Date: 2005/01/05 04:59:29 $ @maintenanceAuthor $Author: newmana $
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class IntFileTest extends TestCase {

  /**
   * Description of the Field
   *
   */
  private IntFile intFile;

  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public IntFileTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new IntFileTest("testStore"));
    suite.addTest(new IntFileTest("testSize"));
    suite.addTest(new IntFileTest("testBulkWrite"));
    suite.addTest(new IntFileTest("testPersist"));

    return suite;
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }

  /**
   * Creates a new file required to do the testing.
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void setUp() throws IOException {

    intFile = IntFile.open(new File(TempDir.getTempDir().getPath(), "iftest"));
  }

  /**
   * Closes the file used for testing.
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void tearDown() throws IOException {

    intFile.close();
  }

  /**
   * Test get/put
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testStore() throws IOException {

    intFile.putInt(10, 42);
    intFile.putInt(11, 43);
    intFile.putInt(12, 44);
    intFile.putInt(5, 5);

    assertEquals(43, intFile.getInt(11));
    assertEquals(42, intFile.getInt(10));
    assertEquals(5, intFile.getInt(5));
    assertEquals(44, intFile.getInt(12));
  }

  /**
   * Tests that the size of the file changes with large put()s
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testSize() throws IOException {

    intFile.clear();
    intFile.putInt(10240, 15);
    intFile.putInt(0, 42);

    assertEquals(5121, intFile.getSize());

    intFile.clear();

    assertEquals(0, intFile.getSize());

    assertEquals(0, intFile.getInt(0));
  }

  /**
   * A unit test for JUnit
   *
   * @throws IOException EXCEPTION TO DO
   */
  public void testBulkWrite() throws IOException {

    for (int i = 0; i < 10000; i++) {

      intFile.putInt(i, 5000 - i);
    }
  }

  /**
   * A unit test for JUnit
   *
   */
  public void testPersist() {

    for (int i = 0; i < 10000; i++) {

      assertEquals(5000 - i, intFile.getInt(i));
    }
  }
}
