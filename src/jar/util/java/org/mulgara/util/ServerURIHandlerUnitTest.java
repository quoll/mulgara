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

// Standard Java
import java.net.*;

// JUnit
import junit.framework.*;

// Logging
import org.apache.log4j.Logger;


/**
 * A test case for {@link ServerURIHandler}.
 *
 * @created 2004-05-11
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:29 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class ServerURIHandlerUnitTest extends TestCase {

  /**
   * Logger. Named after the class.
   */
  @SuppressWarnings("unused")
  private Logger logger =
      Logger.getLogger(ServerURIHandlerUnitTest.class.getName());

  /**
   * Default constructor.
   *
   * @param name name of tests.
   */
  public ServerURIHandlerUnitTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite with various different output and compares the output.
   *
   * @return The test suite
   */
  public static TestSuite suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new ServerURIHandlerUnitTest("testRemovePort"));
    suite.addTest(new ServerURIHandlerUnitTest("testConvertToServerURI"));

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
   * Test {@link ServerURIHandler#removePort}.
   *
   * @throws Exception
   */
  public void testRemovePort() throws Exception {

    URI testModelURI = new URI("rmi://foo:1099/server1#model");
    URI testServerURI = new URI("rmi://foo:1099/server1");

    URI expectedModelURI = new URI("rmi://foo/server1#model");
    URI expectedServerURI = new URI("rmi://foo/server1");

    URI testNullSchemeURI = new URI("@server2@sub-model1");

    assertEquals("Failed to strip known port", expectedModelURI,
        ServerURIHandler.removePort(testModelURI));
    assertEquals("Failed to string known port", expectedServerURI,
        ServerURIHandler.removePort(testServerURI));
    assertEquals("Failed to handle null scheme URI", testNullSchemeURI,
        ServerURIHandler.removePort(testNullSchemeURI));
  }

  /**
   * Test {@link ServerURIHandler#convertToServerURI}.
   *
   * @throws Exception
   */
  public void testConvertToServerURI() throws Exception {

    URI testModelURI = new URI("rmi://foo:1099/server1#model");
    URI testModelURI2 = new URI("rmi://foo:1199/server1#model");
    URI testServerURI = new URI("rmi://foo:1099/server1");
    URI testServerURI2 = new URI("rmi://foo:1199/server1");

    URI expectedModelURI = new URI("rmi://foo/server1");
    URI expectedModelURI2 = new URI("rmi://foo:1199/server1");
    URI expectedServerURI = new URI("rmi://foo/server1");
    URI expectedServerURI2 = new URI("rmi://foo:1199/server1");

    assertEquals("Failed to create server URI", expectedModelURI,
        ServerURIHandler.convertToServerURI(testModelURI));
    assertEquals("Failed to create server URI", expectedModelURI2,
        ServerURIHandler.convertToServerURI(testModelURI2));
    assertEquals("Failed to create server URI", expectedServerURI,
        ServerURIHandler.convertToServerURI(testServerURI));
    assertEquals("Failed to create server URI", expectedServerURI2,
        ServerURIHandler.convertToServerURI(testServerURI2));
  }
}
