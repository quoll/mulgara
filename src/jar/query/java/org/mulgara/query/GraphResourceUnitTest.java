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

package org.mulgara.query;

// Java packages
import java.net.URI;

// JUnit
import junit.framework.*;

// Log4J
import org.apache.log4j.Logger;

/**
 * Tests the functionality of GraphResource.
 *
 * @created 2004-04-15
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class GraphResourceUnitTest extends TestCase {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(GraphResourceUnitTest.class);

  /**
   * Construct a new unit test.
   *
   * @param name the name of the test
   */
  public GraphResourceUnitTest(String name) {

    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new GraphResourceUnitTest("testEquals"));
    suite.addTest(new GraphResourceUnitTest("testClone"));
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

  //
  // Test cases
  //

  /**
   * Test equality of two model resource.
   */
  public void testEquals() {

    try {

      GraphResource res1 = new GraphResource(new URI("rmi://foo/server1#_"));
      GraphResource res2 = new GraphResource(new URI("rmi://foo/server1#"));
      GraphResource res3 = new GraphResource(new URI("rmi://foo/server1#"));

      assertEquals("Two GraphResource with the same RMI protcol should " +
          "resolve to the same server", res1.getDatabaseURIs(),
          res2.getDatabaseURIs());

      assertFalse("Resource should be unequal", res1.equals(res2));

      assertNotSame("Resources should be different instances", res2, res3);
      assertTrue("Resource should be equal", res2.equals(res3));
    } catch (Exception e) {

      e.printStackTrace();
    }
  }

  /**
   * Test clone of two resources.
   */
  public void testClone() {

    try {

      GraphResource res1 = new GraphResource(new URI("rmi://foo/server1#_"));
      GraphResource res2 = (GraphResource) res1.clone();

      assertNotSame("Resources should be different instances", res1, res2);
      assertTrue("Resources should be equal", res1.equals(res2));
    }
    catch (Exception e) {

      e.printStackTrace();
    }
  }
}

