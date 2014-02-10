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

// Java 2 standard packages
import java.util.*;

/**
 * Test cases for LongToStringMap.
 *
 * @created 2004-05-11
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:29 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LongToStringMapUnitTest extends TestCase {

  private LongToStringMap map;

  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public LongToStringMapUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new LongToStringMapUnitTest("testPutAndGet"));
    suite.addTest(new LongToStringMapUnitTest("testBulkPut"));
//    suite.addTest(new LongToStringMapUnitTest("testStress"));
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
   * @throws IOException if an I/O error occurs
   */
  public void setUp() throws IOException {
    map = new LongToStringMap();
  }

  /**
   * Closes the file used for testing.
   *
   * @throws IOException if an I/O error occurs
   */
  public void tearDown() throws IOException {
    map.delete();
  }

  /**
   * Test put() and get()
   *
   * @throws IOException if an I/O error occurs
   */
  public void testPutAndGet() throws IOException {

    //populate
    map.put("one", 1);
    map.put("four", 4);
    map.put("three", 3);
    map.put("two", 2);
    map.put("five", 5);

    //test
    assertEquals("one", map.get(1));
    assertEquals("two", map.get(2));
    assertEquals("three", map.get(3));
    assertEquals("four", map.get(4));
    assertEquals("five", map.get(5));

    //lookup values that are not in the map
    assertEquals(null, map.get(10));
    assertEquals(null, map.get(20));
    assertEquals(null, map.get(30));
    assertEquals(null, map.get(40));
  }

  /**
   * Tests lots of put()s and get()s. Tests the mapping between long values and
   * their hex String representations.
   *
   * @throws IOException if an I/O error occurs
   */
  public void testBulkPut() throws IOException {

    //number of mappings to insert
    int NUM_MAPPINGS = 10000;

    //populate - must start at 1 (0 is invalid node)
    for (long i = 1; i < NUM_MAPPINGS; i++) {

      map.put(Long.toHexString(i), i);
    }

    //read
    long currentHex = 0;
    for (long i = 1; i < NUM_MAPPINGS; i++) {

      //parse the String value as a hex string (hex has a radix of 16)
      currentHex = Long.parseLong(map.get(i), 16);

      assertEquals(i, currentHex);
    }
  }

  /**
   * Stress test for StringToLongMap. Creates multiple maps and deletes them.
   *
   * @throws Exception
   */
  public void testStress() throws Exception {

    int OPEN_MAPS = 25;
    int ITERATIONS = 25;

    //open maps, load data, delete maps (repeat)
    for (int i = 0; i < ITERATIONS; i++) {

      List<LongToStringMap> openMaps = new ArrayList<LongToStringMap>();

      //create and populate tham all
      for (int j = 0; j < OPEN_MAPS; j++) {
        LongToStringMap map = new LongToStringMap();
        populate(map);
        openMaps.add(map);
      }

      //delete them all
      for (int j = 0; j < OPEN_MAPS; j++) {
        LongToStringMap map = (LongToStringMap) openMaps.get(j);
        map.delete();

        //ensure it is deleted
        try {
          map.put("one", 1);
          fail("LongToStringMap allowed put after file was deleted.");
        } catch (Exception exception) {
          //expected result
        }
      }
    }
  }

  /**
   * Adds mappings to the map.
   *
   * @param map LongToStringMap
   * @throws Exception
   */
  public void populate(LongToStringMap map) throws Exception {

    int NUM_MAPPINGS = 100;

    for (long i = 0; i < NUM_MAPPINGS; i++) {

      map.put(Long.toHexString(i), i);
    }
  }

}
