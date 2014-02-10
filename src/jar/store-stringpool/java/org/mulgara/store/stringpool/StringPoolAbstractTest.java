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


// third party packages
import junit.framework.*;

// Log4J
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.xa.XAStringPool;


/**
 * Test case for {@link StringPool} implementations.
 *
 * @created 2001-10-05
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
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
public abstract class StringPoolAbstractTest extends TestCase {

  /**
   * The category to log to.
   */
  @SuppressWarnings("unused")
  private static final Logger log = Logger.getLogger(StringPoolAbstractTest.class);

  /**
   * Subclasses must initialize this field.
   */
  protected XAStringPool stringPool;

  /**
   * Subclasses must initialize this field.
   */
  protected SPObjectFactory spoFactory;

  /**
   * CONSTRUCTOR StringPoolAbstractTest TO DO
   *
   * @param name PARAMETER TO DO
   */
  public StringPoolAbstractTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain an empty test suite from, because this test
   * can't be run (it's abstract). This must be overridden by subclasses.
   *
   * @return The test suite
   */
  public static Test suite() {

    return new TestSuite();
  }

  //
  // Test cases
  //

  /**
   * Test {@link StringPool#put}.
   *
   * @throws StringPoolException if the method fails
   * @throws Exception EXCEPTION TO DO
   */
  public void testPut() throws Exception {
    try {
      stringPool.put(2, spoFactory.newSPString("foo"));
      fail("Successfully added 2:\"foo\", but both node 2 and object \"foo\" already exist");
    } catch (StringPoolException e) {
      // NO-OP
    }

    try {
      stringPool.put(3, spoFactory.newSPString("bar"));
      fail("Successfully added 3:\"bar\", but both node 3 and object \"bar\" already exist");
    } catch (StringPoolException e) {
      // NO-OP
    }

    try {
      stringPool.put(2, spoFactory.newSPString("quux"));
      fail("Successfully added node 2, but node 2 already exists");
    } catch (StringPoolException e) {
      // NO-OP
    }

    try {
      stringPool.put(3, spoFactory.newSPString("xyzzy"));
      fail("Successfully added node 3, but node 3 already exists");
    } catch (StringPoolException e) {
      // NO-OP
    }

    try {
      stringPool.put(8, spoFactory.newSPString("foo"));
      fail("Successfully added \"foo\", but \"foo\" already exists");
    } catch (StringPoolException e) {
      // NO-OP
    }

    try {
      stringPool.put(9, spoFactory.newSPString("bar"));
      fail("Successfully added \"bar\", but \"bar\" already exists");
    } catch (StringPoolException e) {
      // NO-OP
    }

    stringPool.put(4, spoFactory.newSPString("quux"));
    stringPool.put(5, spoFactory.newSPString(""));
  }

  /**
   * Test {@link StringPool#findGNode}.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFindNode() throws Exception {

    assertEquals(2, stringPool.findGNode(spoFactory.newSPString("foo")));
    assertEquals(3, stringPool.findGNode(spoFactory.newSPString("bar")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("quux")));
  }

  /**
   * Test {@link StringPool#findSPObject}.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFindString() throws Exception {

    assertEquals(stringPool.findSPObject(2), spoFactory.newSPString("foo"));
    assertEquals(stringPool.findSPObject(3), spoFactory.newSPString("bar"));
    assertEquals(stringPool.findSPObject(4), null);
  }

  /**
   * Populate the test graph.
   *
   * @throws IllegalStateException if the subclass hasn't initialized {@link
   *      #stringPool}
   * @throws StringPoolException if the test pool can't be populated
   * @throws Exception EXCEPTION TO DO
   */
  protected void setUp() throws Exception {

    // Make sure the subclass initialization was done
    if (stringPool == null) {

      throw new IllegalStateException("Subclass didn't set stringPool field");
    }

    // Populate the string pool
    stringPool.put(2, spoFactory.newSPString("foo"));
    stringPool.put(3, spoFactory.newSPString("bar"));
  }

  /**
   * Close the test string pool.
   *
   * @throws Exception EXCEPTION TO DO
   */
  protected void tearDown() throws Exception {

    if (stringPool != null) {

      try {

        stringPool.close();
      }
       finally {

        stringPool = null;
      }
    }
  }
}
