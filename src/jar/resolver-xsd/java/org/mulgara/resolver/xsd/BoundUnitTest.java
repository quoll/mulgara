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
 * Contributor(s):
 * The copyright to this file is held by:
 *   The Australian Commonwealth Government
 *   Department of Defense
 * Developed by Netymon Pty Ltd
 * under contract 4500430665
 * contributed to the Mulgara Project under the
 *   Mozilla Public License version 1.1
 * per clause 4.1.3 of the above contract.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.xsd;

// Third party packages
import junit.framework.*;        // JUnit
import org.apache.log4j.Logger;  // Apache Log4J

// Local packages
import org.mulgara.query.Variable;

/**
 * Unit testing suite for {@link Bound}.
 *
 * @created 2005-05-25
 *
 * @author <a href="mailto:raboczi@itee.uq.edu.au">Simon Raboczi</a>
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/06/09 09:26:20 $ @maintenanceAuthor $Author: raboczi $
 *
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class BoundUnitTest extends TestCase
{
  /** Logger */
  private static Logger logger =
    Logger.getLogger(BoundUnitTest.class.getName());

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public BoundUnitTest(String name)
  {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return the test suite
   */
  public static Test suite()
  {
    TestSuite suite = new TestSuite();

    suite.addTest(new BoundUnitTest("test1Equals"));

    return suite;
  }

  /**
   * Default test runner.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //
  // Test cases
  //

  /**
   * Test #1 for the {@link Bound#equals} method.
   */
  public void test1Equals() throws Exception
  {
    Bound bound = new Bound(3, true);

    assertTrue(bound.equals(bound));
    assertTrue(bound.equals(new Bound(3, true)));

    assertFalse(bound.equals(new Bound(3, false)));
    assertFalse(bound.equals(new Bound(2, true)));
  }
}
