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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.XSD;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.StringPoolAbstractTest;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.xa.SimpleXAResource;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.util.TempDir;


/**
 * Test case for {@link XAStringPoolImpl}.
 *
 * @created 2002-03-07
 *
 * @author <a href="http://staff.pisoftware.com/david">David Makepeace</a>
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/07/21 19:16:31 $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class XAStringPoolImplTest extends StringPoolAbstractTest {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(XAStringPoolImplTest.class);


  /**
   * Constructor.
   *
   * @param name The name of the test. Used for logging.
   */
  public XAStringPoolImplTest(String name) {
    super(name);
    spoFactory = SPObjectFactoryImpl.getInstance();
  }


  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {
    //return new TestSuite(StringPoolImplTest.class);
    TestSuite suite = new TestSuite();
    suite.addTest(new XAStringPoolImplTest("testPut"));
    suite.addTest(new XAStringPoolImplTest("testFindNode"));
    suite.addTest(new XAStringPoolImplTest("testFindString"));
    suite.addTest(new XAStringPoolImplTest("testRemove"));
    suite.addTest(new XAStringPoolImplTest("testPersistence1"));
    suite.addTest(new XAStringPoolImplTest("testPersistence2"));
    suite.addTest(new XAStringPoolImplTest("testNewPhase"));
    suite.addTest(new XAStringPoolImplTest("testAllTypes"));
    suite.addTest(new XAStringPoolImplTest("testFindNodes"));
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
    ((SimpleXAResource) stringPool).clear();
    super.setUp();
    super.testPut();
  }


  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFindNode() throws Exception {
    ((SimpleXAResource) stringPool).clear();
    super.setUp();
    super.testFindNode();
  }


  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testFindString() throws Exception {
    ((SimpleXAResource) stringPool).clear();
    super.setUp();
    super.testFindString();
  }


  /**
   * A unit test for JUnit
   *
   * @throws StringPoolException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws InterruptedException EXCEPTION TO DO
   */
  public void testRemove() throws Exception {
    ((SimpleXAResource) stringPool).clear();

    assertTrue(!stringPool.remove(13));
    assertTrue(!stringPool.remove(15));

    stringPool.put(11, spoFactory.newSPString("alpha"));
    stringPool.put(12, spoFactory.newSPString("bravo"));
    stringPool.put(13, spoFactory.newSPString("charlie"));
    stringPool.put(14, spoFactory.newSPString("delta"));
    stringPool.put(15, spoFactory.newSPString("echo"));
    stringPool.put(16, spoFactory.newSPString("foxtrot"));

    assertEquals(13, stringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(14, stringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(15, stringPool.findGNode(spoFactory.newSPString("echo")));

    assertTrue(stringPool.remove(13));
    assertTrue(stringPool.remove(15));

    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(14, stringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("echo")));

    assertTrue(!stringPool.remove(13));
    assertTrue(!stringPool.remove(15));

    ((SimpleXAResource) stringPool).prepare();
    ((SimpleXAResource) stringPool).commit();

    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(14, stringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("echo")));

    assertTrue(stringPool.remove(14));

    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("echo")));

    assertTrue(!stringPool.remove(14));
  }


  /**
   * A unit test for JUnit
   *
   * @throws StringPoolException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws InterruptedException EXCEPTION TO DO
   */
  public void testPersistence1() throws Exception {
    ((SimpleXAResource) stringPool).clear();

    stringPool.put(2, spoFactory.newSPURI(XSD.DOUBLE_URI));
    stringPool.put(3, spoFactory.newSPURI(XSD.DATE_TIME_URI));

    stringPool.put(11, spoFactory.newSPString("alpha"));
    stringPool.put(12, spoFactory.newSPString("bravo"));
    stringPool.put(13, spoFactory.newSPString("charlie"));
    stringPool.put(14, spoFactory.newSPString("delta"));
    stringPool.put(15, spoFactory.newSPString("echo"));
    stringPool.put(16, spoFactory.newSPString("foxtrot"));

    ((SimpleXAResource) stringPool).prepare();
    ((SimpleXAResource) stringPool).commit();
  }


  /**
   * A unit test for JUnit
   *
   * @throws StringPoolException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws InterruptedException EXCEPTION TO DO
   */
  public void testPersistence2() throws Exception {
    int[] phases = ((SimpleXAResource) stringPool).recover();
    assertEquals(1, phases.length);
    assertEquals(1, phases[0]);
    ((SimpleXAResource) stringPool).selectPhase(phases[0]);

    assertEquals(11, stringPool.findGNode(spoFactory.newSPString("alpha")));
    assertEquals(12, stringPool.findGNode(spoFactory.newSPString("bravo")));
    assertEquals(13, stringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(14, stringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(15, stringPool.findGNode(spoFactory.newSPString("echo")));
    assertEquals(16, stringPool.findGNode(spoFactory.newSPString("foxtrot")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("golf")));

    assertEquals(spoFactory.newSPString("alpha"), stringPool.findSPObject(11));
    assertEquals(spoFactory.newSPString("bravo"), stringPool.findSPObject(12));
    assertEquals(spoFactory.newSPString("charlie"), stringPool.findSPObject(13));
    assertEquals(spoFactory.newSPString("delta"), stringPool.findSPObject(14));
    assertEquals(spoFactory.newSPString("echo"), stringPool.findSPObject(15));
    assertEquals(spoFactory.newSPString("foxtrot"), stringPool.findSPObject(16));
    assertNull(stringPool.findSPObject(17));

    stringPool.put(17, spoFactory.newSPString("golf"));

    ((SimpleXAResource) stringPool).prepare();
    ((SimpleXAResource) stringPool).commit();
  }


  /**
   * A unit test for JUnit
   *
   * @throws StringPoolException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws InterruptedException EXCEPTION TO DO
   */
  public void testNewPhase() throws Exception {
    int[] phases = ((SimpleXAResource) stringPool).recover();
    assertEquals(1, phases.length);
    assertEquals(2, phases[0]);
    ((SimpleXAResource) stringPool).selectPhase(phases[0]);

    assertEquals(spoFactory.newSPString("golf"), stringPool.findSPObject(17));
    assertEquals(17, stringPool.findGNode(spoFactory.newSPString("golf")));

    XAStringPoolImpl.ReadOnlyStringPool roStringPool =
        ((XAStringPoolImpl) stringPool).new ReadOnlyStringPool();
    roStringPool.refresh();

    stringPool.put(18, spoFactory.newSPString("hotel"));
    stringPool.put(19, spoFactory.newSPString("india"));
    stringPool.put(20, spoFactory.newSPString("juliet"));
    stringPool.put(21, spoFactory.newSPString("kilo"));
    stringPool.put(22, spoFactory.newSPString("lima"));

    assertEquals(11, roStringPool.findGNode(spoFactory.newSPString("alpha")));
    assertEquals(12, roStringPool.findGNode(spoFactory.newSPString("bravo")));
    assertEquals(13, roStringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(14, roStringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(15, roStringPool.findGNode(spoFactory.newSPString("echo")));
    assertEquals(16, roStringPool.findGNode(spoFactory.newSPString("foxtrot")));
    assertEquals(17, roStringPool.findGNode(spoFactory.newSPString("golf")));

    assertEquals(spoFactory.newSPString("alpha"), roStringPool.findSPObject(11));
    assertEquals(spoFactory.newSPString("bravo"), roStringPool.findSPObject(12));
    assertEquals(spoFactory.newSPString("charlie"), roStringPool.findSPObject(13));
    assertEquals(spoFactory.newSPString("delta"), roStringPool.findSPObject(14));
    assertEquals(spoFactory.newSPString("echo"), roStringPool.findSPObject(15));
    assertEquals(spoFactory.newSPString("foxtrot"), roStringPool.findSPObject(16));
    assertEquals(spoFactory.newSPString("golf"), roStringPool.findSPObject(17));

    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("hotel")));
    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("india")));
    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("juliet")));
    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("kilo")));
    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("lima")));

    ((SimpleXAResource) stringPool).prepare();
    ((SimpleXAResource) stringPool).commit();

    assertEquals(11, roStringPool.findGNode(spoFactory.newSPString("alpha")));
    assertEquals(12, roStringPool.findGNode(spoFactory.newSPString("bravo")));
    assertEquals(13, roStringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(14, roStringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(15, roStringPool.findGNode(spoFactory.newSPString("echo")));
    assertEquals(16, roStringPool.findGNode(spoFactory.newSPString("foxtrot")));
    assertEquals(17, roStringPool.findGNode(spoFactory.newSPString("golf")));

    assertEquals(spoFactory.newSPString("alpha"), roStringPool.findSPObject(11));
    assertEquals(spoFactory.newSPString("bravo"), roStringPool.findSPObject(12));
    assertEquals(spoFactory.newSPString("charlie"), roStringPool.findSPObject(13));
    assertEquals(spoFactory.newSPString("delta"), roStringPool.findSPObject(14));
    assertEquals(spoFactory.newSPString("echo"), roStringPool.findSPObject(15));
    assertEquals(spoFactory.newSPString("foxtrot"), roStringPool.findSPObject(16));
    assertEquals(spoFactory.newSPString("golf"), roStringPool.findSPObject(17));

    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("hotel")));
    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("india")));
    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("juliet")));
    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("kilo")));
    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("lima")));

    roStringPool.refresh();

    assertEquals(11, roStringPool.findGNode(spoFactory.newSPString("alpha")));
    assertEquals(12, roStringPool.findGNode(spoFactory.newSPString("bravo")));
    assertEquals(13, roStringPool.findGNode(spoFactory.newSPString("charlie")));
    assertEquals(14, roStringPool.findGNode(spoFactory.newSPString("delta")));
    assertEquals(15, roStringPool.findGNode(spoFactory.newSPString("echo")));
    assertEquals(16, roStringPool.findGNode(spoFactory.newSPString("foxtrot")));
    assertEquals(17, roStringPool.findGNode(spoFactory.newSPString("golf")));
    assertEquals(18, roStringPool.findGNode(spoFactory.newSPString("hotel")));
    assertEquals(19, roStringPool.findGNode(spoFactory.newSPString("india")));
    assertEquals(20, roStringPool.findGNode(spoFactory.newSPString("juliet")));
    assertEquals(21, roStringPool.findGNode(spoFactory.newSPString("kilo")));
    assertEquals(22, roStringPool.findGNode(spoFactory.newSPString("lima")));

    assertEquals(spoFactory.newSPString("alpha"), roStringPool.findSPObject(11));
    assertEquals(spoFactory.newSPString("bravo"), roStringPool.findSPObject(12));
    assertEquals(spoFactory.newSPString("charlie"), roStringPool.findSPObject(13));
    assertEquals(spoFactory.newSPString("delta"), roStringPool.findSPObject(14));
    assertEquals(spoFactory.newSPString("echo"), roStringPool.findSPObject(15));
    assertEquals(spoFactory.newSPString("foxtrot"), roStringPool.findSPObject(16));
    assertEquals(spoFactory.newSPString("golf"), roStringPool.findSPObject(17));
    assertEquals(spoFactory.newSPString("hotel"), roStringPool.findSPObject(18));
    assertEquals(spoFactory.newSPString("india"), roStringPool.findSPObject(19));
    assertEquals(spoFactory.newSPString("juliet"), roStringPool.findSPObject(20));
    assertEquals(spoFactory.newSPString("kilo"), roStringPool.findSPObject(21));
    assertEquals(spoFactory.newSPString("lima"), roStringPool.findSPObject(22));

  }


  /**
   * A unit test for JUnit
   *
   * @throws StringPoolException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws InterruptedException EXCEPTION TO DO
   * @throws URISyntaxException EXCEPTION TO DO
   */
  public void testAllTypes() throws Exception {
    ((SimpleXAResource) stringPool).clear();

    stringPool.put(2, spoFactory.newSPURI(XSD.DOUBLE_URI));
    stringPool.put(3, spoFactory.newSPURI(XSD.DATE_TIME_URI));

    assertTrue(!stringPool.remove(13));
    assertTrue(!stringPool.remove(15));

    stringPool.put(11, spoFactory.newSPString("alpha"));
    stringPool.put(12, spoFactory.newSPString("bravo"));
    stringPool.put(13, spoFactory.newSPURI(new URI("http://charlie/")));
    stringPool.put(14, spoFactory.newSPURI(new URI("http://delta/")));
    stringPool.put(15, spoFactory.newSPDouble(42));
    stringPool.put(16, spoFactory.newSPDouble(123));
    stringPool.put(17, spoFactory.newSPTypedLiteral(
        "1966-09-18T15:00:00", XSD.DATE_TIME_URI
    ));
    stringPool.put(18, spoFactory.newSPTypedLiteral(
        "1971-12-20T00:20:00", XSD.DATE_TIME_URI
    ));

    assertEquals(11, stringPool.findGNode(spoFactory.newSPString("alpha")));
    assertEquals(12, stringPool.findGNode(spoFactory.newSPString("bravo")));
    assertEquals(13, stringPool.findGNode(spoFactory.newSPURI(new URI("http://charlie/"))));
    assertEquals(14, stringPool.findGNode(spoFactory.newSPURI(new URI("http://delta/"))));
    assertEquals(15, stringPool.findGNode(spoFactory.newSPDouble(42)));
    assertEquals(16, stringPool.findGNode(spoFactory.newSPDouble(123)));
    assertEquals(17, stringPool.findGNode(spoFactory.newSPTypedLiteral(
        "1966-09-18T15:00:00", XSD.DATE_TIME_URI
    )));
    assertEquals(18, stringPool.findGNode(spoFactory.newSPTypedLiteral(
        "1971-12-20T00:20:00", XSD.DATE_TIME_URI
    )));

    // Make sure that URIs don't appear as strings.
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("http://charlie/")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("http://delta/")));

    assertTrue(stringPool.remove(13));
    assertTrue(stringPool.remove(15));

    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPURI(new URI("http://charlie/"))));
    assertEquals(14, stringPool.findGNode(spoFactory.newSPURI(new URI("http://delta/"))));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPDouble(42)));

    assertTrue(!stringPool.remove(13));
    assertTrue(!stringPool.remove(15));

    ((SimpleXAResource) stringPool).prepare();
    ((SimpleXAResource) stringPool).commit();

    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPURI(new URI("http://charlie/"))));
    assertEquals(14, stringPool.findGNode(spoFactory.newSPURI(new URI("http://delta/"))));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPDouble(42)));

    assertTrue(stringPool.remove(14));

    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPURI(new URI("http://charlie/"))));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPURI(new URI("http://delta/"))));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPDouble(42)));

    assertTrue(!stringPool.remove(14));
  }


  /**
   * A unit test for JUnit
   *
   * @throws StringPoolException EXCEPTION TO DO
   * @throws SimpleXAResourceException EXCEPTION TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws InterruptedException EXCEPTION TO DO
   * @throws URISyntaxException EXCEPTION TO DO
   */
  public void testFindNodes() throws Exception {
    ((SimpleXAResource) stringPool).clear();

    stringPool.put(2, spoFactory.newSPURI(XSD.DOUBLE_URI));
    stringPool.put(3, spoFactory.newSPURI(XSD.DATE_TIME_URI));

    // Graph nodes 3 and 5 shouldn't exist since super.setUp() has not been
    // called.
    assertTrue(!stringPool.remove(13));
    assertTrue(!stringPool.remove(15));

    // Populate the string pool.
    stringPool.put(11, spoFactory.newSPString("alpha"));
    stringPool.put(12, spoFactory.newSPString("bravo"));
    stringPool.put(13, spoFactory.newSPURI(new URI("http://charlie/")));
    stringPool.put(14, spoFactory.newSPURI(new URI("http://delta/")));
    stringPool.put(15, spoFactory.newSPDouble(42));
    stringPool.put(16, spoFactory.newSPDouble(123));
    stringPool.put(17, spoFactory.newSPTypedLiteral(
        "1966-09-18T15:00:00", XSD.DATE_TIME_URI
    ));
    stringPool.put(18, spoFactory.newSPTypedLiteral(
        "1971-12-20T00:20:00", XSD.DATE_TIME_URI
    ));
    stringPool.put(19, spoFactory.newSPString("foxtrot"));
    stringPool.put(20, spoFactory.newSPString("golf"));
    stringPool.put(21, spoFactory.newSPString("hotel"));
    stringPool.put(22, spoFactory.newSPString("charlie"));
    stringPool.put(23, spoFactory.newSPString("delta"));
    stringPool.put(24, spoFactory.newSPString("juliet"));
    stringPool.put(25, spoFactory.newSPString("kilo"));
    stringPool.put(26, spoFactory.newSPString("echo"));
    stringPool.put(27, spoFactory.newSPString("india"));
    stringPool.put(28, spoFactory.newSPString("lima"));
    stringPool.put(29, spoFactory.newSPDouble(3.14159265358979323846));
    stringPool.put(30, spoFactory.newSPDouble(-10));
    stringPool.put(31, spoFactory.newSPDouble(99999));
    stringPool.put(32, spoFactory.newSPDouble(1000));
    stringPool.put(33, spoFactory.newSPDouble(1000.001));
    stringPool.put(34, spoFactory.newSPDouble(321));
    stringPool.put(35, spoFactory.newSPDouble(1234));
    stringPool.put(36, spoFactory.newSPDouble(1111));
    stringPool.put(37, spoFactory.newSPDouble(1001));
    stringPool.put(38, spoFactory.newSPDouble(1002));
    stringPool.put(39, spoFactory.newSPDouble(1003));
    stringPool.put(40, spoFactory.newSPDouble(90));
    stringPool.put(41, spoFactory.newSPTypedLiteral(
        "1977-01-01T00:00:00", XSD.DATE_TIME_URI
    ));
    stringPool.put(42, spoFactory.newSPTypedLiteral(
        "1968-07-05T00:00:00", XSD.DATE_TIME_URI
    ));
    stringPool.put(43, spoFactory.newSPTypedLiteral(
        "1981-01-10T00:00:00", XSD.DATE_TIME_URI
    ));
    stringPool.put(44, spoFactory.newSPTypedLiteral(
        "1999-09-09T00:00:00", XSD.DATE_TIME_URI
    ));
    stringPool.put(45, spoFactory.newSPTypedLiteral(
        "1977-01-01T00:00:01", XSD.DATE_TIME_URI
    ));
    stringPool.put(46, spoFactory.newSPTypedLiteral(
        "2000-01-01T00:00:00", XSD.DATE_TIME_URI
    ));
    stringPool.put(47, spoFactory.newSPTypedLiteral(
        "1999-12-31T23:59:59", XSD.DATE_TIME_URI
    ));
    stringPool.put(48, spoFactory.newSPTypedLiteral(
        "1977-01-01T00:00:02", XSD.DATE_TIME_URI
    ));
    stringPool.put(49, spoFactory.newSPTypedLiteral(
        "1970-01-01T00:00:00", XSD.DATE_TIME_URI
    ));
    stringPool.put(50, spoFactory.newSPTypedLiteral(
        "1969-12-31T23:59:59", XSD.DATE_TIME_URI
    ));

    List<Long> allStrings = new ArrayList<Long>();
    allStrings.add(new Long(11)); // alpha
    allStrings.add(new Long(12)); // bravo
    allStrings.add(new Long(22)); // charlie
    allStrings.add(new Long(23)); // delta
    allStrings.add(new Long(26)); // echo
    allStrings.add(new Long(19)); // foxtrot
    allStrings.add(new Long(20)); // golf
    allStrings.add(new Long(21)); // hotel
    allStrings.add(new Long(27)); // india
    allStrings.add(new Long(24)); // juliet
    allStrings.add(new Long(25)); // kilo
    allStrings.add(new Long(28)); // lima

    // Get all SPString objects.
    Tuples t = stringPool.findGNodes(
        SPObject.TypeCategory.UNTYPED_LITERAL, null
    );
    assertEquals(allStrings, asList(t));
    assertEquals(t.getRowCardinality(), Cursor.MANY);
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPString("delta"), true,
        spoFactory.newSPString("hotel"), true
    );
    assertEquals(allStrings.subList(3, 8), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPString("delt"), true,
        spoFactory.newSPString("hotels"), true
    );
    assertEquals(allStrings.subList(3, 8), asList(t));
    t.close();

    // SPStrings objects are case insensitive.
    t = stringPool.findGNodes(
        spoFactory.newSPString("DELT"), true,
        spoFactory.newSPString("HOTELS"), true
    );
    assertEquals(allStrings.subList(3, 8), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPString("delt"), false,
        spoFactory.newSPString("hotels"), false
    );
    assertEquals(allStrings.subList(3, 8), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPString("delta"), false,
        spoFactory.newSPString("hotel"), true
    );
    assertEquals(allStrings.subList(4, 8), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPString("deltas"), true,
        spoFactory.newSPString("hotel"), true
    );
    assertEquals(allStrings.subList(4, 8), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPString("delta"), true,
        spoFactory.newSPString("hotel"), false
    );
    assertEquals(allStrings.subList(3, 7), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPString("delta"), false,
        spoFactory.newSPString("hotel"), false
    );
    assertEquals(allStrings.subList(4, 7), asList(t));
    t.close();

    List<Long> allDoubles = new ArrayList<Long>();
    allDoubles.add(new Long(30)); // -10
    allDoubles.add(new Long(29)); // 3.14159265358979323846
    allDoubles.add(new Long(15)); // 42
    allDoubles.add(new Long(40)); // 90
    allDoubles.add(new Long(16)); // 123
    allDoubles.add(new Long(34)); // 321
    allDoubles.add(new Long(32)); // 1000
    allDoubles.add(new Long(33)); // 1000.001
    allDoubles.add(new Long(37)); // 1001
    allDoubles.add(new Long(38)); // 1002
    allDoubles.add(new Long(39)); // 1003
    allDoubles.add(new Long(36)); // 1111
    allDoubles.add(new Long(35)); // 1234
    allDoubles.add(new Long(31)); // 99999

    t = stringPool.findGNodes(
        SPObject.TypeCategory.TYPED_LITERAL, XSD.DOUBLE_URI
    );
    assertEquals(allDoubles, asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPDouble(90), true,
        spoFactory.newSPDouble(1003), true
    );
    assertEquals(allDoubles.subList(3, 11), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPDouble(89.99999), true,
        spoFactory.newSPDouble(1003.00001), true
    );
    assertEquals(allDoubles.subList(3, 11), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPDouble(89.99999), false,
        spoFactory.newSPDouble(1003.00001), false
    );
    assertEquals(allDoubles.subList(3, 11), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPDouble(90), false,
        spoFactory.newSPDouble(1003), true
    );
    assertEquals(allDoubles.subList(4, 11), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPDouble(90.00001), true,
        spoFactory.newSPDouble(1003), true
    );
    assertEquals(allDoubles.subList(4, 11), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPDouble(90), true,
        spoFactory.newSPDouble(1003), false
    );
    assertEquals(allDoubles.subList(3, 10), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPDouble(90), false,
        spoFactory.newSPDouble(1003), false
    );
    assertEquals(allDoubles.subList(4, 10), asList(t));
    t.close();

    List<Long> allDates = new ArrayList<Long>();
    allDates.add(new Long(17)); // 18/09/1966 15:00
    allDates.add(new Long(42)); // 05/05/1968
    allDates.add(new Long(50)); // 31/12/1969 23:59:59
    allDates.add(new Long(49)); // 01/01/1970 00:00:00
    allDates.add(new Long(18)); // 20/12/1971 00:20
    allDates.add(new Long(41)); // 01/01/1977
    allDates.add(new Long(45)); // 01/01/1977 00:00:01
    allDates.add(new Long(48)); // 01/01/1977 00:00:02
    allDates.add(new Long(43)); // 10/01/1981
    allDates.add(new Long(44)); // 09/09/1999
    allDates.add(new Long(47)); // 31/12/1999 23:59:59
    allDates.add(new Long(46)); // 01/01/2000 00:00:00

    t = stringPool.findGNodes(
        SPObject.TypeCategory.TYPED_LITERAL, XSD.DATE_TIME_URI
    );
    assertEquals(allDates, asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPTypedLiteral(
            "1971-12-20T00:20:00", XSD.DATE_TIME_URI
        ), true,
        spoFactory.newSPTypedLiteral(
            "1999-09-09T00:00:00", XSD.DATE_TIME_URI
        ), true
    );
    assertEquals(allDates.subList(4, 10), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPTypedLiteral(
            "1971-12-19T00:00:00", XSD.DATE_TIME_URI
        ), true,
        spoFactory.newSPTypedLiteral(
            "1999-09-10T00:00:00", XSD.DATE_TIME_URI
        ), true
    );
    assertEquals(allDates.subList(4, 10), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPTypedLiteral(
            "1971-12-19T00:00:00", XSD.DATE_TIME_URI
        ), false,
        spoFactory.newSPTypedLiteral(
            "1999-09-10T00:00:00", XSD.DATE_TIME_URI
        ), false
    );
    assertEquals(allDates.subList(4, 10), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPTypedLiteral(
            "1971-12-20T00:20:00", XSD.DATE_TIME_URI
        ), false,
        spoFactory.newSPTypedLiteral(
            "1999-09-09T00:00:00", XSD.DATE_TIME_URI
        ), true
    );
    assertEquals(allDates.subList(5, 10), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPTypedLiteral(
            "1971-12-21T00:00:00", XSD.DATE_TIME_URI
        ), true,
        spoFactory.newSPTypedLiteral(
            "1999-09-09T00:00:00", XSD.DATE_TIME_URI
        ), true
    );
    assertEquals(allDates.subList(5, 10), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPTypedLiteral(
            "1971-12-20T00:20:00", XSD.DATE_TIME_URI
        ), true,
        spoFactory.newSPTypedLiteral(
            "1999-09-09T00:00:00", XSD.DATE_TIME_URI
        ), false
    );
    assertEquals(allDates.subList(4, 9), asList(t));
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPTypedLiteral(
            "1971-12-20T00:20:00", XSD.DATE_TIME_URI
        ), false,
        spoFactory.newSPTypedLiteral(
            "1999-09-09T00:00:00", XSD.DATE_TIME_URI
        ), false
    );
    assertEquals(allDates.subList(5, 9), asList(t));
    t.close();

    // Matching high value on last node in index (inclusive).
    t = stringPool.findGNodes(
        spoFactory.newSPTypedLiteral(
            "1977-01-01T00:00:01", XSD.DATE_TIME_URI
        ), false,
        spoFactory.newSPTypedLiteral(
            "2000-01-01T00:00:00", XSD.DATE_TIME_URI
        ), true
    );
    assertEquals(allDates.subList(7, 12), asList(t));
    t.close();

    // Regression test for NPE when explicitly matching lowValue (exclusive) on
    // last node in index.
    t = stringPool.findGNodes(
        spoFactory.newSPTypedLiteral(
            "2000-01-01T00:00:00", XSD.DATE_TIME_URI
        ), false, null, false
    );
    assertTrue(asList(t).isEmpty());
    assertEquals(t.getRowCardinality(), Cursor.ZERO);
    // Regression test.
    t.beforeFirst(new long[] {31}, 0);
    assertTrue(!t.next());
    t.close();

    t = stringPool.findGNodes(
        spoFactory.newSPString("alpha"), true,
        spoFactory.newSPString("apple"), false
    );
    assertEquals(t.getRowCardinality(), Cursor.ONE);
    t.beforeFirst();
    assertTrue(t.next());
    t.close();
  }

  /**
   * Converts a single column Tuples to a List of Longs.
   */
  static List<Long> asList(Tuples t) throws TuplesException {
    List<Long> l = new ArrayList<Long>();
    long rowCount = t.getRowCount();

    t.beforeFirst();
    while (t.next()) {
      l.add(new Long(t.getColumnValue(0)));
    }

    if (rowCount != l.size()) {
      throw new AssertionError(
          "Actual number of rows (" + l.size() +
          ") is not equal to reported row count (" + rowCount + ")"
      );
    }

    return l;
  }

  /**
   * Method to set up the string pool before testing.
   *
   * @throws Exception EXCEPTION TO DO
   */
  protected void setUp() throws Exception {
    boolean exceptionOccurred = true;
    try {
      // create a new string pool, building new files in the process.
      stringPool = new XAStringPoolImpl(
          TempDir.getTempDir().getPath() + File.separatorChar + "stringpooltest"
      );

      exceptionOccurred = false;
    }
    finally {
      if (exceptionOccurred) {
        tearDown();
      }
    }
  }


  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

}
