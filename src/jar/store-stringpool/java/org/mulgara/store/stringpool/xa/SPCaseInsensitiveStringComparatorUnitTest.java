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

// Java 2 standard packages
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mulgara.store.stringpool.AbstractSPObject;
import org.mulgara.store.stringpool.SPComparator;

/**
 * Unit test for testing the case-insensitive string comparator.
 *
 * @created 2004-11-23
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/03/12 02:49:47 $
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
public class SPCaseInsensitiveStringComparatorUnitTest extends TestCase {

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPCaseInsensitiveStringComparatorUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new SPCaseInsensitiveStringComparatorUnitTest("testPrefix"));

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

  /**
   * Tests the comparison of different strings (when compared
   * case-insensitively) that compare equal (case-insensitively) for the
   * initial substring that is of the prefix length.
   * This is a regression test for a comparator bug.
   *
   * @throws Exception
   */
  public void testPrefix() throws Exception {
    final int prefixLength = 32;
    final String s1 = "http://test.domain2.com/heteroside.htm";
    final String s2 = "http://test.domain2.com/Heterosiphonales.htm";
    final String s3 = "http://test.domain2.com/heterosis.htm";

    ByteBuffer bb1 = AbstractSPObject.CHARSET.encode(CharBuffer.wrap(s1));
    ByteBuffer bb2 = AbstractSPObject.CHARSET.encode(CharBuffer.wrap(s2));
    ByteBuffer bb3 = AbstractSPObject.CHARSET.encode(CharBuffer.wrap(s3));

    SPComparator spc = new SPCaseInsensitiveStringComparator();
    // First test the compare() method.
    bb1.clear();
    bb2.clear();
    assertTrue(spc.compare(bb1, 0, bb2, 0) < 0);
    bb2.clear();
    bb1.clear();
    assertTrue(spc.compare(bb2, 0, bb1, 0) > 0);
    bb2.clear();
    bb3.clear();
    assertTrue(spc.compare(bb2, 0, bb3, 0) < 0);
    bb3.clear();
    bb2.clear();
    assertTrue(spc.compare(bb3, 0, bb2, 0) > 0);
    bb1.clear();
    bb3.clear();
    assertTrue(spc.compare(bb1, 0, bb3, 0) < 0);
    bb3.clear();
    bb1.clear();
    assertTrue(spc.compare(bb3, 0, bb1, 0) > 0);

    // The comparePrefix() method should return 0 in all these cases.
    bb1.clear();
    bb2.rewind().limit(prefixLength);
    assertTrue(spc.comparePrefix(bb1, bb2, bb2.capacity()) == 0);
    bb2.clear();
    bb1.rewind().limit(prefixLength);
    assertTrue(spc.comparePrefix(bb2, bb1, bb1.capacity()) == 0);
    bb2.clear();
    bb3.rewind().limit(prefixLength);
    assertTrue(spc.comparePrefix(bb2, bb3, bb3.capacity()) == 0);
    bb3.clear();
    bb2.rewind().limit(prefixLength);
    assertTrue(spc.comparePrefix(bb3, bb2, bb2.capacity()) == 0);
    bb1.clear();
    bb3.rewind().limit(prefixLength);
    assertTrue(spc.comparePrefix(bb1, bb3, bb3.capacity()) == 0);
    bb3.clear();
    bb1.rewind().limit(prefixLength);
    assertTrue(spc.comparePrefix(bb3, bb1, bb1.capacity()) == 0);
  }


  //set up and tear down

  /**
   * Initialise members.
   *
   * @throws Exception if something goes wrong
   */
  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * The teardown method for JUnit
   *
   * @throws Exception if something goes wrong
   */
  public void tearDown() throws Exception {
    super.tearDown();
  }

}
