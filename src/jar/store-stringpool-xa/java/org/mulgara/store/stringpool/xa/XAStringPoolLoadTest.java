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

package org.mulgara.store.stringpool.xa;

import java.io.*;

// third party packages
import junit.framework.*;
import org.apache.log4j.Logger;

// locally written packages
import org.mulgara.store.stringpool.*;
import org.mulgara.store.stringpool.xa.SPObjectFactoryImpl;
import org.mulgara.util.TempDir;

/**
 * Test case for {@link XAStringPoolImpl}.
 *
 * @created 2002-03-07
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paula Gearon</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/20 11:06:06 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class XAStringPoolLoadTest extends StringPoolLoadTest {

  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(XAStringPoolLoadTest.class);

  /**
   * Description of the Field
   */
  @SuppressWarnings("unused")
  private static File dbDirName = new File(
      System.getProperty("java.io.tmpdir"), System.getProperty("user.name")
      );

  /**
   * The start of the filenames to use for the string pool.
   */
  private static String dbFileName =
      TempDir.getTempDir().getPath() + File.separator + "xaloadtest";

  /**
   * Flag to indicate if the string pool files already exist.
   */
  //private static boolean dbExists = new File(dbFileName + ".sp").exists();
  private static boolean dbExists = false;

  /**
   * Instance of a string pool
   */
  private XAStringPoolImpl stringPoolImpl = null;


  /**
   * Constructor.
   *
   * @param name The name of the test. Used for logging.
   */
  public XAStringPoolLoadTest(String name) {
    super(name);
    spoFactory = SPObjectFactoryImpl.getInstance();
  }


  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {
    //return new TestSuite(XAStringPoolLoadTest.class);
    TestSuite suite = new TestSuite();
    suite.addTest(new XAStringPoolLoadTest("testLoadStringPool"));
    suite.addTest(new XAStringPoolLoadTest("testFirstQuery"));
    suite.addTest(new XAStringPoolLoadTest("testSecondQuery"));
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
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testLoadStringPool() throws Exception {
    loadData = !dbExists;
    super.testLoadStringPool();
  }


  /**
   * Method to set up the string pool before testing.
   *
   * @throws Exception EXCEPTION TO DO
   */
  protected void setUp() throws Exception {
    // create a new string pool, building new files in the process.
    stringPoolImpl = new XAStringPoolImpl(dbFileName);
    stringPool = stringPoolImpl;

    try {
      if (dbExists) {
        int[] phaseNumbers = stringPoolImpl.recover();
        if (phaseNumbers.length > 0) {
          stringPoolImpl.selectPhase(phaseNumbers[0]);
        }
        else {
          stringPoolImpl.clear();
        }
      }
      else {
        stringPoolImpl.clear();
      }
    }
    catch (Exception ex) {
      super.tearDown();
      throw ex;
    }
    super.setUp();
  }


  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  protected void tearDown() throws Exception {
    dbExists = true;
    try {
      if (stringPoolImpl != null) {
        stringPoolImpl.prepare();
        stringPoolImpl.commit();
        stringPoolImpl.unmap();
        if (System.getProperty("os.name").startsWith("Win")) {
          // Need this for Windows or truncate() always fails for mapped files.
          System.gc();
          System.runFinalization();
        }
      }
    }
    finally {
      super.tearDown();
    }
  }
}
