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

package org.mulgara.store.stringpool;

// Java 2 standard packages
import java.io.*;
import java.util.*;
import java.util.zip.*;

// third party packages
import junit.framework.*;

import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.xa.XAStringPool;

/**
 * Test case for {@link StringPool} implementations.
 *
 * @created 2001-10-05
 *
 * @author <a href="http://staff.pisoftware.com/mike">Michael Judd</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/20 10:26:19 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class StringPoolLoadTest extends TestCase {
  /** Get line separator. */
  private static final String eol = System.getProperty("line.separator");

  /** List of stored node/string pairs */
  protected static List<StringPoolTestEntry> testEntries;

  /** Subclasses must initialize this string pool field. */
  protected XAStringPool stringPool;

  /** Subclasses must initialize this factory for creating string pool objects. */
  protected SPObjectFactory spoFactory;

  /** Reads in test data from file */
  protected BufferedReader reader;

  /** maximum number of entries */
  protected int maxSize = 3000000;

  /** the number of queries to be made */
  protected int noQueries = 3000;

  /** the file containing test data */
  protected String dataFile = "data/testwords.txt.gz";

  /** This flag indicates data should be written to the string pool when it is read */
  protected boolean loadData = true;

  // these are using for thread testing (testThirdQuery)

  /** Query counter */
  private int numberOfQueries = 0;

  /** Random number generator */
  private Random random;

  /**
   * Constructor for the test object.
   * @param name Name of this test.
   */
  public StringPoolLoadTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain an empty test suite from, because this test
   * can't be run (it's abstract). This must be overridden in subclasses.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    return suite;
  }


  /**
   * Default text runner.
   * @param args The command line arguments
   */
  public static void main(SPString[] args) {
    junit.textui.TestRunner.run(suite());
  }


  /**
   * Reads in all (or a subset determined by maxSize) of the dataset. The total
   * data has approximately 3 million literals.
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
      if (loadData) stringPool.put(count, spObject);
      if ((count % gapSize) == 0) testEntries.add(new StringPoolTestEntry(count, spObject));
      line = reader.readLine();
      count++;
    }
    System.out.println(String.format("Loaded %d statements in %fsec", count, (System.currentTimeMillis() - start) / 1000.0));
  }


  /**
   * Go through the test entries and find graph node by passing in the SPObject
   * and assert the returned integer equals the original
   */
  public void testFirstQuery() throws Exception {

    for (int i = 0; i < testEntries.size(); i++) {
      StringPoolTestEntry entry = (StringPoolTestEntry) testEntries.get(i);
      long gNode = stringPool.findGNode(entry.spObject);
      assertTrue("StringPool.findGNode returned NONE for gNode/spObject: " + entry.gNode + "/" + entry.spObject,
                 gNode != NodePool.NONE);
      assertTrue("The graph node found for the SPObject " + entry.spObject + " was incorrect. Expected " + entry.gNode + " but found " + gNode,
                 gNode == entry.gNode);
    }
  }


  /**
   * Go through the test entries and find string by passing in the node and
   * assert the returned string equals the original
   */
  public void testSecondQuery() throws Exception {
    for (int i = 0; i < testEntries.size(); i++) {
      StringPoolTestEntry entry = (StringPoolTestEntry)testEntries.get(i);
      SPObject spObject = stringPool.findSPObject(entry.gNode);

      assertTrue("StringPool.findSPObject returned null for gNode/spObject: " + entry.gNode + "/" + entry.spObject,
                 spObject != null);
      assertTrue("The SPObject found for the graph node " + entry.gNode + " was incorrect. Expected " + entry.spObject + " but found " + spObject,
                 spObject.equals(entry.spObject));
    }
  }


  /**
   * This query will test threading capabilities of the string pool.
   */
  public void testThirdQuery() throws Exception {
    int numberOfThreads = 10;
    numberOfQueries = 0;

    ThreadGroup testGroup = new ThreadGroup("testThirdQuery");
    System.out.print(eol + "Starting StringPoolLoadTest query threads...>");

    for (int threadCount = 0; threadCount < numberOfThreads; threadCount++) {
      TestThirdThread thread = new TestThirdThread(testGroup, threadCount, this, testEntries);
      thread.start();
    }

    // Wait for the all the threads to stop.
    Thread.sleep(70 * 1000);

    System.out.println(eol + "Total number of queries performed in 60 " +
        "seconds with " + numberOfThreads + " threads is :" + numberOfQueries);
  }


  /**
   * Adds a feature to the NumberOfQueries attribute of the StringPoolLoadTest object
   * @param numberOfQueries The feature to be added to the NumberOfQueries attribute
   */
  public void addNumberOfQueries(int numberOfQueries) {
    this.numberOfQueries += numberOfQueries;
  }


  /**
   * get the BufferedReader ready for action
   */
  protected void setUp() throws Exception {
    random = new Random(System.currentTimeMillis());

    GZIPInputStream gzipStream = new GZIPInputStream(new FileInputStream(dataFile));
    DataInputStream inStream = new DataInputStream(gzipStream);
    reader = new BufferedReader(new InputStreamReader(inStream));
  }


  /**
   * The teardown method for JUnit
   */
  protected void tearDown() throws Exception {
    if (stringPool != null) stringPool.close();
  }


  class TestThirdThread extends Thread {

    private int numberOfStringPoolQueries = 0;
    private int threadNumber = 0;
    private StringPoolLoadTest parentTest = null;
    private List<StringPoolTestEntry> entries;

    /**
     * Constructs this thread's dta structure.
     * @param group The group for this thread.
     * @param threadNumber The id for this thread.
     * @param parentTest The test object that owns this object.
     * @param entries The data to query for.
     */
    public TestThirdThread(ThreadGroup group, int threadNumber,
      StringPoolLoadTest parentTest, List<StringPoolTestEntry> entries) {
      super(group, "Thread number :" + String.valueOf(threadNumber));
      this.threadNumber = threadNumber;
      numberOfStringPoolQueries = 0;
      this.parentTest = parentTest;
      this.entries = entries;
    }


    public void run() {
      try {

        boolean stopQueries = false;
        int i;

        System.out.print(String.valueOf(threadNumber) + "..>");

        long endTime = System.currentTimeMillis() + (60 * 1000);

        while (!stopQueries) {

          i = Math.abs(random.nextInt()) % testEntries.size();

          StringPoolTestEntry entry = entries.get(i);

          long gNode = stringPool.findGNode(entry.spObject);
          assertTrue("StringPool.findGNode returned NONE for gNode/spObject: " + entry.gNode + "/" + entry.spObject,
                     gNode != NodePool.NONE);
          assertTrue("The graph node found for the SPObject " + entry.spObject + " was incorrect. Expected " + entry.gNode + " but found " + gNode,
                     gNode == entry.gNode);

          i = Math.abs(random.nextInt()) % testEntries.size();

          entry = entries.get(i);

          SPObject spObject = stringPool.findSPObject(entry.gNode);
          assertTrue("StringPool.findSPObject returned null for node/SPObject: " + entry.gNode + "/" + entry.spObject,
                     spObject != null);
          assertTrue("The spObject found for the node " + entry.gNode + " was incorrect. Expected " + entry.spObject + " but found " + spObject,
                     spObject.equals(entry.spObject));

          numberOfStringPoolQueries++;

          if (System.currentTimeMillis() > endTime) {
            stopQueries = true;
            break;
          }
        }

        System.out.print("<..." + String.valueOf(threadNumber));
        parentTest.addNumberOfQueries(numberOfStringPoolQueries);
      } catch (StringPoolException spe) {
        System.out.println("string pool exception during multithreading query");
      }
    }
  }

  /**
   * this class is just a structure to store the node/string pair
   */
  static protected class StringPoolTestEntry {

    /** The node associated with the data. */
    public int gNode;

    /** The stored data. */
    public SPObject spObject;

    /**
     * Constructor.
     * @param gNode The node for this data
     * @param spObject The data for this node
     */
    public StringPoolTestEntry(int gNode, SPObject spObject) {
      this.gNode = gNode;
      this.spObject = spObject;
    }
}


}
