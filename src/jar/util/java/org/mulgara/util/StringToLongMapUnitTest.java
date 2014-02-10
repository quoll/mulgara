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
 * Test cases for StringToLongMap.
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
public class StringToLongMapUnitTest extends TestCase {

  private StringToLongMap strToLongMap;

  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public StringToLongMapUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new StringToLongMapUnitTest("testPut"));
    suite.addTest(new StringToLongMapUnitTest("testGetAndPut"));
    suite.addTest(new StringToLongMapUnitTest("testBulkPut"));
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
    strToLongMap = new StringToLongMap();
  }

  /**
   * Closes the file used for testing.
   *
   * @throws IOException if an I/O error occurs
   */
  public void tearDown() throws IOException {
    strToLongMap.delete();
  }

  /**
   * Test put() and get()
   *
   * @throws IOException if an I/O error occurs
   */
  public void testPut() throws IOException {
    strToLongMap.put("minus one", -1);
    strToLongMap.put("one", 1);
    strToLongMap.put("two", 2);
    strToLongMap.put("three", 3);
    strToLongMap.put("four", 4);
    strToLongMap.put("five", 5);
    strToLongMap.put("forty", 0);

    assertEquals(-1, strToLongMap.get("minus one"));
    assertEquals(1, strToLongMap.get("one"));
    assertEquals(2, strToLongMap.get("two"));
    assertEquals(3, strToLongMap.get("three"));
    assertEquals(4, strToLongMap.get("four"));
    assertEquals(5, strToLongMap.get("five"));

    assertEquals(0, strToLongMap.get("ten"));
    assertEquals(0, strToLongMap.get("twenty"));
    assertEquals(0, strToLongMap.get("thirty"));
    assertEquals(0, strToLongMap.get("forty"));
  }

  /**
   * Test getAndPut()
   *
   * @throws IOException if an I/O error occurs
   */
  public void testGetAndPut() throws IOException {
    assertEquals(0, strToLongMap.getAndPut("one", 101));
    assertEquals(0, strToLongMap.getAndPut("two", 2));
    assertEquals(0, strToLongMap.getAndPut("three", 39));
    assertEquals(0, strToLongMap.getAndPut("four", 24));
    assertEquals(0, strToLongMap.getAndPut("five", 5));

    assertEquals(0, strToLongMap.get("ten"));

    assertEquals(101, strToLongMap.getAndPut("one", 1));
    assertEquals(2, strToLongMap.getAndPut("two", 0)); // No change
    assertEquals(39, strToLongMap.getAndPut("three", 3));
    assertEquals(24, strToLongMap.getAndPut("four", 4));
    assertEquals(5, strToLongMap.getAndPut("five", 0)); // No change

    assertEquals(0, strToLongMap.get("twenty"));

    assertEquals(1, strToLongMap.get("one"));
    assertEquals(2, strToLongMap.get("two"));
    assertEquals(3, strToLongMap.get("three"));
    assertEquals(4, strToLongMap.get("four"));
    assertEquals(5, strToLongMap.get("five"));

    assertEquals(0, strToLongMap.get("thirty"));
  }

  /**
   * Tests lots of put()s and get()s.
   *
   * @throws IOException if an I/O error occurs
   */
  public void testBulkPut() throws IOException {
    // Get a list of random numbers.
    List<Long> numbersList = new ArrayList<Long>();
    Random rand = new Random(12345);
    for (int i = 0; i < 100000; ++i) {
      long value = rand.nextLong();
      numbersList.add(new Long(value));
      long oldValue = strToLongMap.getAndPut(Long.toString(value), value);
      assertTrue(
          "bad oldValue at index " + i + ": " + oldValue +
          ", should be 0 or " + value,
          oldValue == 0 || oldValue == value
      );
    }

    List<Long> moreNumbersList = new ArrayList<Long>();
    for (int i = 0; i < 100; ++i) {
      long value = rand.nextLong();
      moreNumbersList.add(new Long(value));
      long oldValue = strToLongMap.getAndPut("N" + value, value);
      assertTrue(
          "Bad oldValue at index " + i + ": " + oldValue +
          ", should be 0 or " + value,
          oldValue == 0 || oldValue == value
      );
    }

    // Check that all the numbers are still present.
    for (int i = 0; i < numbersList.size(); ++i) {
      long value = ((Long)numbersList.get(i)).longValue();
      assertEquals(
          "Bad value at index: " + i, value,
          strToLongMap.get(Long.toString(value))
      );
    }

    // Check that all the moreNumbers are still present.
    for (int i = 0; i < moreNumbersList.size(); ++i) {
      long value = ((Long)moreNumbersList.get(i)).longValue();
      assertEquals(
          "Bad value at index: " + i, value,
          strToLongMap.get("N" + value)
      );
    }
  }

}
