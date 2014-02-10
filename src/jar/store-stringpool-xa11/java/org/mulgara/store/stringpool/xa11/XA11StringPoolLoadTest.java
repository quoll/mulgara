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

package org.mulgara.store.stringpool.xa11;

import java.io.*;
import java.util.ArrayList;

// third party packages
import junit.framework.*;
import org.apache.log4j.Logger;

// locally written packages
import org.mulgara.store.stringpool.*;
import org.mulgara.store.stringpool.xa.SPObjectFactoryImpl;
import org.mulgara.util.TempDir;

/**
 * Test case for {@link XA11StringPoolImpl}.
 *
 * @created 2008-08-20
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 */
public class XA11StringPoolLoadTest extends StringPoolLoadTest {

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(XA11StringPoolLoadTest.class);

  /** The start of the filenames to use for the string pool. */
  private static String dbFileName = TempDir.getTempDir().getPath() + File.separator + "xaloadtest";

  /** Flag to indicate if the string pool files already exist. */
  private static boolean dbExists = false;

  /** Instance of a string pool */
  private XA11StringPoolImpl stringPoolImpl = null;


  /**
   * Constructor.
   * @param name The name of the test. Used for logging.
   */
  public XA11StringPoolLoadTest(String name) {
    super(name);
    spoFactory = SPObjectFactoryImpl.getInstance();
  }


  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite to run.
   */
  public static Test suite() {
    //return new TestSuite(XAStringPoolLoadTest.class);
    TestSuite suite = new TestSuite();
    suite.addTest(new XA11StringPoolLoadTest("testLoadStringPool"));
    suite.addTest(new XA11StringPoolLoadTest("testFirstQuery"));
    suite.addTest(new XA11StringPoolLoadTest("testSecondQuery"));
    return suite;
  }


  /**
   * Default test runner.
   * @param args The command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }


  /**
   * Load the data.
   */
  public void testLoadStringPool() throws Exception {
    // adjust noQueries if the maxSize is less than it
    noQueries = (maxSize < noQueries) ? maxSize : noQueries;

    // create the array of test entries
    testEntries = new ArrayList<StringPoolTestEntry>();

    String line = reader.readLine();

    int count = 1;
    int gapSize = maxSize / noQueries;

    long start = System.currentTimeMillis();
    while ((line != null) && (count < maxSize)) {
      SPObject spObject = spoFactory.newSPString(line);
      long node = stringPool.put(spObject);
      if (node > Integer.MAX_VALUE) throw new IllegalStateException("Unable to load data that doesn't fit nodes into integers");
      if ((count % gapSize) == 0) testEntries.add(new StringPoolTestEntry((int)node, spObject));
      line = reader.readLine();
      count++;
    }
    System.out.println(String.format("Loaded %d statements in %fsec", count, (System.currentTimeMillis() - start) / 1000.0));
  }


  /**
   * Method to set up the string pool before testing.
   */
  protected void setUp() throws Exception {
    // create a new string pool, building new files in the process.
    stringPoolImpl = new XA11StringPoolImpl(new String[]{dbFileName});
    stringPool = stringPoolImpl;

    try {
      if (dbExists) {
        int[] phaseNumbers = stringPoolImpl.recover();
        if (phaseNumbers.length > 0) stringPoolImpl.selectPhase(phaseNumbers[0]);
        else stringPoolImpl.clear();
      } else {
        stringPoolImpl.clear();
      }
    } catch (Exception ex) {
      super.tearDown();
      throw ex;
    }
    super.setUp();
  }


  /**
   * The teardown method for JUnit
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
    } finally {
      super.tearDown();
    }
  }
}
