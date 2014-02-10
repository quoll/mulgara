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

package org.mulgara.resolver.jrdf;


// Third party packages
import junit.framework.*;



/**
 * Unit test for testing a BlankNodeImpl to long Map.
 *
 * @created 2004-08-24
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:24 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class DiskBlankNodeMapUnitTest extends AbstractBlankNodeMapUnitTest {

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public DiskBlankNodeMapUnitTest(String name) {
    super(name);
  }

  /**
   * Returns an DiskBlankNodeMap for testing.
   *
   * @throws Exception
   * @return BlankNodeMap
   */
  public BlankNodeMap getMap() throws Exception {

    return new DiskBlankNodeMap();
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new DiskBlankNodeMapUnitTest("testCreate"));
    suite.addTest(new DiskBlankNodeMapUnitTest("testPut"));
    return suite;
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    junit.textui.TestRunner.run(suite());
  }

}
