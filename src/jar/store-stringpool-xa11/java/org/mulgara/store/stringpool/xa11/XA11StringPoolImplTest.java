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
import java.net.*;

// Java 2 standard packages
import java.util.*;

// third party packages
import junit.framework.*;
import org.apache.log4j.Logger;


// locally written packages
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.*;
import org.mulgara.store.*;
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.stringpool.*;
import org.mulgara.store.stringpool.xa.SPObjectFactoryImpl;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.xa.SimpleXAResource;
import org.mulgara.store.xa.XAStringPool;
import org.mulgara.util.TempDir;


/**
 * Test case for {@link XA11StringPoolImpl}.
 *
 * @created 2008-08-20
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 */
public class XA11StringPoolImplTest extends TestCase {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(XA11StringPoolImplTest.class);

  /** Persistent data to hand dataToNodes from one test to the next. */
  private static Map<SPObject,Long> persistedDataToNodes;

  /** Persistent data to hand nodesToData from one test to the next. */
  private static Map<Long,SPObject> persistedNodesToData;

  /** The data pool being tested. */
  protected XAStringPool stringPool;

  /** A record of the data stored in the data pool, mapped to the nodes. */
  protected Map<SPObject,Long> dataToNodes;

  /** A record of the nodes stored in the data pool, mapped to the data. */
  protected Map<Long,SPObject> nodesToData;

  /** The Factory building objects for this data pool. */
  protected SPObjectFactory spoFactory;

  /**
   * Constructor.
   * @param name The name of the test. Used for logging.
   */
  public XA11StringPoolImplTest(String name) {
    super(name);
    spoFactory = SPObjectFactoryImpl.getInstance();
  }


  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite to run.
   */
  public static Test suite() {
    //return new TestSuite(StringPoolImplTest.class);
    TestSuite suite = new TestSuite();
    suite.addTest(new XA11StringPoolImplTest("testPut"));
    suite.addTest(new XA11StringPoolImplTest("testFindNode"));
    suite.addTest(new XA11StringPoolImplTest("testFindString"));
    suite.addTest(new XA11StringPoolImplTest("testPersistence1"));
    suite.addTest(new XA11StringPoolImplTest("testPersistence2"));
    suite.addTest(new XA11StringPoolImplTest("testNewPhase"));
    suite.addTest(new XA11StringPoolImplTest("testAllTypes"));
    suite.addTest(new XA11StringPoolImplTest("testFindNodes"));
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
   * Test {@link StringPool#put}.
   */
  public void testPut() throws Exception {
    ((SimpleXAResource) stringPool).clear();
    simpleConfig();

    try {
      put("foo");
      fail("Successfully added \"foo\", but object \"foo\" already exists");
    } catch (StringPoolException e) { }

    try {
      put("bar");
      fail("Successfully added 3:\"bar\", but object \"bar\" already exists");
    } catch (StringPoolException e) { }


    long node = stringPool.put(spoFactory.newSPString("quux"));
    assertFalse(alreadyAssigned(node));
    node = stringPool.put(spoFactory.newSPString(""));
    assertFalse(alreadyAssigned(node));
  }


  /**
   * Test {@link StringPool#findGNode}.
   */
  public void testFindNode() throws Exception {
    ((SimpleXAResource) stringPool).clear();
    simpleConfig();

    testNodeRetrieval("foo");
    testNodeRetrieval("bar");
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("quux")));
  }

  /**
   * Test {@link StringPool#findSPObject}.
   */
  public void testFindString() throws Exception {
    ((SimpleXAResource) stringPool).clear();
    simpleConfig();

    testObjRetrieval("foo");
    testObjRetrieval("bar");
    assertEquals(stringPool.findSPObject(10000), null);
  }

  /**
   * Tests persistence procedure
   */
  public void testPersistence1() throws Exception {
    ((SimpleXAResource) stringPool).clear();

    put(spoFactory.newSPURI(XSD.DOUBLE_URI));
    put(spoFactory.newSPURI(XSD.DATE_TIME_URI));

    put("alpha");
    put("bravo");
    put("charlie");
    put("delta");
    put("echo");
    put("foxtrot");

    ((SimpleXAResource) stringPool).prepare();
    ((SimpleXAResource) stringPool).commit();
    saveState();
  }


  /**
   * Tests reads after persistence. Persisted data ws written in {@link #testPersistence1()}.
   */
  public void testPersistence2() throws Exception {
    loadState();
    int[] phases = ((SimpleXAResource) stringPool).recover();
    assertEquals(1, phases.length);
    assertEquals(1, phases[0]);
    ((SimpleXAResource) stringPool).selectPhase(phases[0]);

    testNodeRetrieval("alpha");
    testNodeRetrieval("bravo");
    testNodeRetrieval("charlie");
    testNodeRetrieval("delta");
    testNodeRetrieval("echo");
    testNodeRetrieval("foxtrot");
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("golf")));

    testObjRetrieval("alpha");
    testObjRetrieval("bravo");
    testObjRetrieval("charlie");
    testObjRetrieval("delta");
    testObjRetrieval("echo");
    testObjRetrieval("foxtrot");
    long lastNode = stringPool.findGNode(spoFactory.newSPString("foxtrot"));
    assertNull(stringPool.findSPObject(lastNode + 16));  // 16 is the on-disk size for foxtrot

    put("golf");

    ((SimpleXAResource) stringPool).prepare();
    ((SimpleXAResource) stringPool).commit();
    saveState();
  }


  /**
   * Tests read only phases.
   */
  public void testNewPhase() throws Exception {
    loadState();
    int[] phases = ((SimpleXAResource) stringPool).recover();
    assertEquals(1, phases.length);
    assertEquals(2, phases[0]);
    ((SimpleXAResource) stringPool).selectPhase(phases[0]);

    testObjRetrieval("golf");
    testNodeRetrieval("golf");

    XA11StringPoolImpl.ReadOnlyStringPool roStringPool =
        ((XA11StringPoolImpl)stringPool).new ReadOnlyStringPool();
    roStringPool.refresh();

    put("hotel");
    put("india");
    put("juliet");
    put("kilo");
    put("lima");

    testNodeRetrieval(roStringPool, "alpha");
    testNodeRetrieval(roStringPool, "bravo");
    testNodeRetrieval(roStringPool, "charlie");
    testNodeRetrieval(roStringPool, "delta");
    testNodeRetrieval(roStringPool, "echo");
    testNodeRetrieval(roStringPool, "foxtrot");
    testNodeRetrieval(roStringPool, "golf");

    testObjRetrieval(roStringPool, "alpha");
    testObjRetrieval(roStringPool, "bravo");
    testObjRetrieval(roStringPool, "charlie");
    testObjRetrieval(roStringPool, "delta");
    testObjRetrieval(roStringPool, "echo");
    testObjRetrieval(roStringPool, "foxtrot");
    testObjRetrieval(roStringPool, "golf");

    // Now that this is WORM, we are retrieving data from newer phases as well
    testNodeRetrieval(roStringPool, "hotel");
    testNodeRetrieval(roStringPool, "india");
    testNodeRetrieval(roStringPool, "juliet");
    testNodeRetrieval(roStringPool, "kilo");
    testNodeRetrieval(roStringPool, "lima");
    assertEquals(NodePool.NONE, roStringPool.findGNode(spoFactory.newSPString("mike")));

    ((SimpleXAResource) stringPool).prepare();
    ((SimpleXAResource) stringPool).commit();

    testNodeRetrieval(roStringPool, "alpha");
    testNodeRetrieval(roStringPool, "bravo");
    testNodeRetrieval(roStringPool, "charlie");
    testNodeRetrieval(roStringPool, "delta");
    testNodeRetrieval(roStringPool, "echo");
    testNodeRetrieval(roStringPool, "foxtrot");
    testNodeRetrieval(roStringPool, "golf");
    testNodeRetrieval(roStringPool, "hotel");
    testNodeRetrieval(roStringPool, "india");
    testNodeRetrieval(roStringPool, "juliet");
    testNodeRetrieval(roStringPool, "kilo");
    testNodeRetrieval(roStringPool, "lima");

    testObjRetrieval(roStringPool, "alpha");
    testObjRetrieval(roStringPool, "bravo");
    testObjRetrieval(roStringPool, "charlie");
    testObjRetrieval(roStringPool, "delta");
    testObjRetrieval(roStringPool, "echo");
    testObjRetrieval(roStringPool, "foxtrot");
    testObjRetrieval(roStringPool, "golf");
    testObjRetrieval(roStringPool, "hotel");
    testObjRetrieval(roStringPool, "india");
    testObjRetrieval(roStringPool, "juliet");
    testObjRetrieval(roStringPool, "kilo");
    testObjRetrieval(roStringPool, "lima");

    roStringPool.refresh();

    testNodeRetrieval(roStringPool, "alpha");
    testNodeRetrieval(roStringPool, "bravo");
    testNodeRetrieval(roStringPool, "charlie");
    testNodeRetrieval(roStringPool, "delta");
    testNodeRetrieval(roStringPool, "echo");
    testNodeRetrieval(roStringPool, "foxtrot");
    testNodeRetrieval(roStringPool, "golf");
    testNodeRetrieval(roStringPool, "hotel");
    testNodeRetrieval(roStringPool, "india");
    testNodeRetrieval(roStringPool, "juliet");
    testNodeRetrieval(roStringPool, "kilo");
    testNodeRetrieval(roStringPool, "lima");

    testObjRetrieval(roStringPool, "alpha");
    testObjRetrieval(roStringPool, "bravo");
    testObjRetrieval(roStringPool, "charlie");
    testObjRetrieval(roStringPool, "delta");
    testObjRetrieval(roStringPool, "echo");
    testObjRetrieval(roStringPool, "foxtrot");
    testObjRetrieval(roStringPool, "golf");
    testObjRetrieval(roStringPool, "hotel");
    testObjRetrieval(roStringPool, "india");
    testObjRetrieval(roStringPool, "juliet");
    testObjRetrieval(roStringPool, "kilo");
    testObjRetrieval(roStringPool, "lima");

  }


  /**
   * Tests storage and retrieval of several data types
   */
  public void testAllTypes() throws Exception {
    ((SimpleXAResource) stringPool).clear();

    put(spoFactory.newSPURI(XSD.DOUBLE_URI));
    put(spoFactory.newSPURI(XSD.DATE_TIME_URI));

    assertTrue(stringPool.remove(10013));
    assertTrue(stringPool.remove(10015));

    put(spoFactory.newSPString("alpha"));
    put(spoFactory.newSPString("bravo"));
    put(spoFactory.newSPURI(new URI("http://charlie/")));
    put(spoFactory.newSPURI(new URI("http://delta/")));
    put(spoFactory.newSPDouble(42));
    put(spoFactory.newSPDouble(123));
    put(spoFactory.newSPTypedLiteral("1966-09-18T15:00:00", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("1971-12-20T00:20:00", XSD.DATE_TIME_URI));

    testNodeRetrieval(spoFactory.newSPString("alpha"));
    testNodeRetrieval(spoFactory.newSPString("bravo"));
    testNodeRetrieval(spoFactory.newSPURI(new URI("http://charlie/")));
    testNodeRetrieval(spoFactory.newSPURI(new URI("http://delta/")));
    testNodeRetrieval(spoFactory.newSPDouble(42));
    testNodeRetrieval(spoFactory.newSPDouble(123));
    testNodeRetrieval(spoFactory.newSPTypedLiteral("1966-09-18T15:00:00", XSD.DATE_TIME_URI));
    testNodeRetrieval(spoFactory.newSPTypedLiteral("1971-12-20T00:20:00", XSD.DATE_TIME_URI));

    // Make sure that URIs don't appear as strings.
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("http://charlie/")));
    assertEquals(NodePool.NONE, stringPool.findGNode(spoFactory.newSPString("http://delta/")));

    assertTrue(stringPool.remove(13));
    assertTrue(stringPool.remove(15));

    ((SimpleXAResource) stringPool).prepare();
    ((SimpleXAResource) stringPool).commit();

    testNodeRetrieval(spoFactory.newSPURI(new URI("http://charlie/")));
    testNodeRetrieval(spoFactory.newSPURI(new URI("http://delta/")));
    testNodeRetrieval(spoFactory.newSPDouble(42));

    assertTrue(stringPool.remove(14));

    testNodeRetrieval(spoFactory.newSPURI(new URI("http://charlie/")));
    testNodeRetrieval(spoFactory.newSPURI(new URI("http://delta/")));
    testNodeRetrieval(spoFactory.newSPDouble(42));

  }


  /**
   * Tests range searching.
   */
  public void testFindNodes() throws Exception {
    ((SimpleXAResource) stringPool).clear();

    put(spoFactory.newSPURI(XSD.DOUBLE_URI));
    put(spoFactory.newSPURI(XSD.DATE_TIME_URI));

    // Populate the string pool.
    put(spoFactory.newSPString("alpha"));
    put(spoFactory.newSPString("bravo"));
    put(spoFactory.newSPURI(new URI("http://charlie/")));
    put(spoFactory.newSPURI(new URI("http://delta/")));
    put(spoFactory.newSPDouble(42));
    put(spoFactory.newSPDouble(123));
    put(spoFactory.newSPTypedLiteral("1966-09-18T15:00:00", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("1971-12-20T00:20:00", XSD.DATE_TIME_URI));
    put(spoFactory.newSPString("foxtrot"));
    put(spoFactory.newSPString("golf"));
    put(spoFactory.newSPString("hotel"));
    put(spoFactory.newSPString("charlie"));
    put(spoFactory.newSPString("delta"));
    put(spoFactory.newSPString("juliet"));
    put(spoFactory.newSPString("kilo"));
    put(spoFactory.newSPString("echo"));
    put(spoFactory.newSPString("india"));
    put(spoFactory.newSPString("lima"));
    put(spoFactory.newSPDouble(3.14159265358979323846));
    put(spoFactory.newSPDouble(-10));
    put(spoFactory.newSPDouble(99999));
    put(spoFactory.newSPDouble(1000));
    put(spoFactory.newSPDouble(1000.001));
    put(spoFactory.newSPDouble(321));
    put(spoFactory.newSPDouble(1234));
    put(spoFactory.newSPDouble(1111));
    put(spoFactory.newSPDouble(1001));
    put(spoFactory.newSPDouble(1002));
    put(spoFactory.newSPDouble(1003));
    put(spoFactory.newSPDouble(90));
    put(spoFactory.newSPTypedLiteral("1977-01-01T00:00:00", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("1968-07-05T00:00:00", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("1981-01-10T00:00:00", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("1999-09-09T00:00:00", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("1977-01-01T00:00:01", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("2000-01-01T00:00:00", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("1999-12-31T23:59:59", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("1977-01-01T00:00:02", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("1970-01-01T00:00:00", XSD.DATE_TIME_URI));
    put(spoFactory.newSPTypedLiteral("1969-12-31T23:59:59", XSD.DATE_TIME_URI));

    List<Long> allStrings = new ArrayList<Long>();
    allStrings.add(mappedVal("alpha"));
    allStrings.add(mappedVal("bravo"));
    allStrings.add(mappedVal("charlie"));
    allStrings.add(mappedVal("delta"));
    allStrings.add(mappedVal("echo"));
    allStrings.add(mappedVal("foxtrot"));
    allStrings.add(mappedVal("golf"));
    allStrings.add(mappedVal("hotel"));
    allStrings.add(mappedVal("india"));
    allStrings.add(mappedVal("juliet"));
    allStrings.add(mappedVal("kilo"));
    allStrings.add(mappedVal("lima"));

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
    allDoubles.add(mappedDbl(-10));
    allDoubles.add(mappedDbl(3.14159265358979323846));
    allDoubles.add(mappedDbl(42));
    allDoubles.add(mappedDbl(90));
    allDoubles.add(mappedDbl(123));
    allDoubles.add(mappedDbl(321));
    allDoubles.add(mappedDbl(1000));
    allDoubles.add(mappedDbl(1000.001));
    allDoubles.add(mappedDbl(1001));
    allDoubles.add(mappedDbl(1002));
    allDoubles.add(mappedDbl(1003));
    allDoubles.add(mappedDbl(1111));
    allDoubles.add(mappedDbl(1234));
    allDoubles.add(mappedDbl(99999));

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
    allDates.add(mappedDate("1966-09-18T15:00:00")); 
    allDates.add(mappedDate("1968-07-05T00:00:00")); 
    allDates.add(mappedDate("1969-12-31T23:59:59")); 
    allDates.add(mappedDate("1970-01-01T00:00:00")); 
    allDates.add(mappedDate("1971-12-20T00:20:00")); 
    allDates.add(mappedDate("1977-01-01T00:00:00"));
    allDates.add(mappedDate("1977-01-01T00:00:01")); 
    allDates.add(mappedDate("1977-01-01T00:00:02"));
    allDates.add(mappedDate("1981-01-10T00:00:00")); 
    allDates.add(mappedDate("1999-09-09T00:00:00")); 
    allDates.add(mappedDate("1999-12-31T23:59:59")); 
    allDates.add(mappedDate("2000-01-01T00:00:00")); 

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
    t.beforeFirst(new long[] {mappedDbl(99999)}, 0);
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
    while (t.next()) l.add(new Long(t.getColumnValue(0)));

    if (rowCount != l.size()) {
      throw new AssertionError("Actual number of rows (" + l.size() + ") is not equal to reported row count (" + rowCount + ")");
    }

    return l;
  }


  void testNodeRetrieval(String s) throws StoreException {
    testNodeRetrieval(spoFactory.newSPString(s));
  }


  void testNodeRetrieval(SPObject obj) throws StoreException {
    testNodeRetrieval(stringPool, obj);
  }


  void testNodeRetrieval(XA11StringPoolImpl.ReadOnlyStringPool sp, String s) throws StoreException {
    testNodeRetrieval(sp, spoFactory.newSPString(s));
  }


  void testNodeRetrieval(XAStringPool sp, SPObject obj) throws StoreException {
    assertEquals(dataToNodes.get(obj).longValue(), sp.findGNode(obj));
  }


  void testObjRetrieval(String s) throws StoreException {
    testObjRetrieval(spoFactory.newSPString(s));
  }


  void testObjRetrieval(SPObject obj) throws StoreException {
    testObjRetrieval(stringPool, obj);
  }


  void testObjRetrieval(XA11StringPoolImpl.ReadOnlyStringPool sp, String s) throws StoreException {
    testObjRetrieval(sp, spoFactory.newSPString(s));
  }


  void testObjRetrieval(XAStringPool sp, SPObject obj) throws StoreException {
    long node = dataToNodes.get(obj);
    assertEquals(nodesToData.get(node), sp.findSPObject(node));
  }


  boolean alreadyAssigned(long node) {
    return nodesToData.keySet().contains(node);
  }


  long put(String s) throws StoreException {
    return put(spoFactory.newSPString(s));
  }


  long put(SPObject data) throws StoreException {
    long node = stringPool.put(data);
    dataToNodes.put(data, node);
    nodesToData.put(node, data);
    return node;
  }

  long mappedVal(String s) {
    return dataToNodes.get(spoFactory.newSPString(s));
  }

  long mappedDbl(double d) {
    return dataToNodes.get(spoFactory.newSPDouble(d));
  }

  long mappedDate(String d) {
    return dataToNodes.get(spoFactory.newSPTypedLiteral(d, XSD.DATE_TIME_URI));
  }

  /** Method to set up the string pool before testing. */
  protected void setUp() throws Exception {
    boolean exceptionOccurred = true;
    try {
      // create a new string pool, building new files in the process.
      stringPool = new XA11StringPoolImpl(
          new String[] {TempDir.getTempDir().getPath() + File.separatorChar + "stringpooltest"}
      );
      dataToNodes = new HashMap<SPObject,Long>();
      nodesToData = new HashMap<Long,SPObject>();
      exceptionOccurred = false;
    } finally {
      if (exceptionOccurred) tearDown();
    }
  }


  /** A setup method for some of the tests. */
  void simpleConfig() throws Exception {
    // Make sure the subclass initialization was done
    if (stringPool == null) throw new IllegalStateException("Subclass didn't set stringPool field");

    // Populate the string pool
    put(spoFactory.newSPString("foo"));
    put(spoFactory.newSPString("bar"));
  }


  /** Saves the internal state so a later test can use it. */
  void saveState() {
    persistedDataToNodes = dataToNodes;
    persistedNodesToData = nodesToData;
  }


  /** Saves the internal state so a later test can use it. */
  void loadState() {
    dataToNodes = persistedDataToNodes;
    nodesToData = persistedNodesToData;
  }


  /** The teardown method for JUnit */
  protected void tearDown() throws Exception {
    if (stringPool != null) {
      try {
        stringPool.close();
      } finally {
        stringPool = null;
      }
    }
  }

}
