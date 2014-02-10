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

package org.mulgara.store.stringpool.memory;

// Java 2 standard packages
import java.net.*;

// third party packages
import junit.framework.*;

// Locally written packages
import org.mulgara.query.rdf.XSD;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.stringpool.*;
import org.mulgara.store.stringpool.xa.SPObjectFactoryImpl;

/**
 * Test case for {@link MemoryStringPoolImpl}.
 *
 * @created 2003-09-11
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/20 11:06:03 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MemoryStringPoolImplTest extends StringPoolAbstractTest {

  /**
   * CONSTRUCTOR MemoryStringPoolImplTest TO DO
   *
   * @param name PARAMETER TO DO
   */
  public MemoryStringPoolImplTest(String name) {
    super(name);
    spoFactory = SPObjectFactoryImpl.getInstance();
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new MemoryStringPoolImplTest("testPut"));
    suite.addTest(new MemoryStringPoolImplTest("testFindNode"));
    suite.addTest(new MemoryStringPoolImplTest("testFindString"));
    suite.addTest(new MemoryStringPoolImplTest("testRemove"));
    suite.addTest(new MemoryStringPoolImplTest("testAllTypes"));
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
  public void testPut() throws Exception {

    super.setUp();
    super.testPut();
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFindNode() throws Exception {

    super.setUp();
    super.testFindNode();
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFindString() throws Exception {

    super.setUp();
    super.testFindString();
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testRemove() throws Exception {

    assertTrue(!stringPool.remove(4));
    assertTrue(!stringPool.remove(6));

    stringPool.put(2, spoFactory.newSPString("alpha"));
    stringPool.put(3, spoFactory.newSPString("bravo"));
    stringPool.put(4, spoFactory.newSPString("charlie"));
    stringPool.put(5, spoFactory.newSPString("delta"));
    stringPool.put(6, spoFactory.newSPString("echo"));
    stringPool.put(7, spoFactory.newSPString("foxtrot"));

    assertEquals(4, stringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(5, stringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(6, stringPool.findGNode(spoFactory.newSPString("echo")));

    assertTrue(stringPool.remove(4));
    assertTrue(stringPool.remove(6));

    assertEquals(NodePool.NONE,
        stringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(5, stringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("echo")));

    assertTrue(!stringPool.remove(4));
    assertTrue(!stringPool.remove(6));

    assertEquals(NodePool.NONE,
        stringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(5, stringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("echo")));

    assertTrue(stringPool.remove(5));

    assertEquals(NodePool.NONE,
        stringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(NodePool.NONE,
        stringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("echo")));

    assertTrue(!stringPool.remove(5));
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testAllTypes() throws Exception {

    assertTrue(!stringPool.remove(4));
    assertTrue(!stringPool.remove(6));

    stringPool.put(100, spoFactory.newSPURI(XSD.DOUBLE_URI));
    stringPool.put(101, spoFactory.newSPURI(XSD.DATE_TIME_URI));

    stringPool.put(2, spoFactory.newSPString("alpha"));
    stringPool.put(3, spoFactory.newSPString("bravo"));
    stringPool.put(4, spoFactory.newSPURI(new URI("http://charlie/")));
    stringPool.put(5, spoFactory.newSPURI(new URI("http://delta/")));
    stringPool.put(6, spoFactory.newSPDouble(42));
    stringPool.put(7, spoFactory.newSPDouble(123));
    stringPool.put(8, spoFactory.newSPTypedLiteral(
        "1966-09-18T15:00:00", XSD.DATE_TIME_URI
    ));
    stringPool.put(9, spoFactory.newSPTypedLiteral(
        "1971-12-20T00:20:00", XSD.DATE_TIME_URI
    ));

    assertEquals(2, stringPool.findGNode(spoFactory.newSPString("alpha")));
    assertEquals(3, stringPool.findGNode(spoFactory.newSPString("bravo")));
    assertEquals(4,
        stringPool.findGNode(spoFactory.newSPURI(new URI("http://charlie/"))));
    assertEquals(5,
        stringPool.findGNode(spoFactory.newSPURI(new URI("http://delta/"))));
    assertEquals(6, stringPool.findGNode(spoFactory.newSPDouble(42)));
    assertEquals(7, stringPool.findGNode(spoFactory.newSPDouble(123)));
    assertEquals(8, stringPool.findGNode(spoFactory.newSPTypedLiteral(
        "1966-09-18T15:00:00", XSD.DATE_TIME_URI
    )));
    assertEquals(9, stringPool.findGNode(spoFactory.newSPTypedLiteral(
        "1971-12-20T00:20:00", XSD.DATE_TIME_URI
    )));

    // Make sure that URIs don't appear as strings.
    assertEquals(NodePool.NONE,
        stringPool.findGNode(spoFactory.newSPString("http://charlie/")));
    assertEquals(NodePool.NONE,
        stringPool.findGNode(spoFactory.newSPString("http://delta/")));

    assertTrue(stringPool.remove(4));
    assertTrue(stringPool.remove(6));

    assertEquals(NodePool.NONE,
        stringPool.findGNode(spoFactory.newSPURI(new URI("http://charlie/"))));
    assertEquals(5,
        stringPool.findGNode(spoFactory.newSPURI(new URI("http://delta/"))));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPDouble(42)));

    assertTrue(!stringPool.remove(4));
    assertTrue(!stringPool.remove(6));

    assertEquals(NodePool.NONE,
        stringPool.findGNode(spoFactory.newSPURI(new URI("http://charlie/"))));
    assertEquals(5,
        stringPool.findGNode(spoFactory.newSPURI(new URI("http://delta/"))));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPDouble(42)));

    assertTrue(stringPool.remove(5));

    assertEquals(NodePool.NONE,
        stringPool.findGNode(spoFactory.newSPURI(new URI("http://charlie/"))));
    assertEquals(NodePool.NONE,
        stringPool.findGNode(spoFactory.newSPURI(new URI("http://delta/"))));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPDouble(42)));

    assertTrue(!stringPool.remove(5));
  }

  /**
   * Method to set up the string pool before testing.
   *
   * @throws Exception EXCEPTION TO DO
   */
  protected void setUp() throws Exception {

    // create a new string pool, building new files in the process.
    stringPool = new MemoryStringPoolImpl();
  }
}
