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

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Unit test for testing the Unique Identifier Generator (UIDGenerator).
 *
 * @created 2004-09-29
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner </a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/02/03 20:07:37 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software </A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *            Software Pty Ltd </a>
 *
 * @licence <a href=" {@docRoot}/../../LICENCE">Mozilla Public License v1.1
 *          </a>
 */
public class UIDGeneratorUnitTest extends TestCase {

  /** Set of UIDs that have been generated */
  private Set<String> uids = null;

  /** Number of UIDs generated */
  private static final int NUM_UIDS = 10000;

  /** Number of Threads concurrently generating UIDs */
  private static final int NUM_THREADS = 10;

  /** Number of ClassLoaders using UIDGenerator classes to genreateUIDs */
  private static final int NUM_CLASSLOADERS = 10;

  /** Short name of the UIDGenerator class */
  private static final String CLASS_NAME = "UIDGenerator";

  /** Fully qualified name of the UIDGenerator class */
  private static final String FULL_CLASS_NAME = "org.mulgara.util." + CLASS_NAME;

  /**
   * Constructs a new test with the given name.
   *
   * @param name
   *            the name of the test
   */
  public UIDGeneratorUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new UIDGeneratorUnitTest("testUID"));
    suite.addTest(new UIDGeneratorUnitTest("testConcurrency"));
    suite.addTest(new UIDGeneratorUnitTest("testMultiClassLoader"));
    return suite;
  }

  /**
   * Default test runner.
   *
   * @param args
   *            The command line arguments
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    junit.textui.TestRunner.run(suite());
  }

  /**
   * Tests that UID are unique.
   *
   * @throws Exception
   */
  public void testUID() throws Exception {

    String currentUID = "";

    for (int i = 0; i < NUM_UIDS; i++) {

      currentUID = UIDGenerator.generateUID();

      //is it unique??
      if (uids.contains(currentUID)) {

        fail("UID set already contains UID [" + i + "]: " + currentUID);
      }

      uids.add(currentUID);
    }
  }

  /**
   * Tests that UID are unique when generated from differnet Threads.
   *
   * @throws Exception
   */
  public void testConcurrency() throws Exception {

    //threads that have to complete before the test finished
    List<Thread> threadList = new ArrayList<Thread>();
    Thread currentThread = null;

    //start threads that generate UIDs
    for (int i = 0; i < NUM_THREADS; i++) {
      //start a new thread that inserts and checks UIDS
      currentThread = new Thread(new Runnable() {

        /** run test */
        public void run() {
          try {
            String currentUID = "";
            int numUIDS = NUM_UIDS / NUM_THREADS;
            for (int i = 0; i < numUIDS; i++) {
              currentUID = UIDGenerator.generateUID();
              synchronized (UIDGeneratorUnitTest.class) {
                //is it unique??
                if (uids.contains(currentUID)) {
                  fail("UID set already contains UID [" + i
                      + "]: " + currentUID);
                }
                uids.add(currentUID);
              }
            }
          } catch (Exception exception) {
            throw new RuntimeException(
                "Error occurred while testing concurrency.",
                exception);
          }
        }
      });
      //end thread

      //keep reference
      threadList.add(currentThread);

      //start it
      currentThread.start();
    }

    //must wait for all threads to finish
    for (int i = 0; i < NUM_THREADS; i++) {

      threadList.get(i).join();
    }
  }

  /**
   * Tests the corner case of having multiple applications loaded by different
   * ClassLoaders.
   *
   * @throws Exception
   */
  public void testMultiClassLoader() throws Exception {

    //load UIDGenerator from multiple classes
    URLClassLoader currentLoader = null;
    Class<?> currentUIDGeneratorClass = null;

    //get an URL to the UIDGenerator class file
    URL[] uidClass = new URL[] {
        ClassLoader.getSystemClassLoader()
        .getResource(CLASS_NAME)};

    for (int i = 0; i < NUM_CLASSLOADERS; i++) {
      currentLoader = new URLClassLoader(uidClass);
      currentUIDGeneratorClass = currentLoader.loadClass(FULL_CLASS_NAME);
      testUIDGeneratorClass(currentUIDGeneratorClass);
    }

  }

  /**
   * Uses the supplied UIDGenerator class to generate UIDs.
   *
   * @param uidGenerator
   * @throws Exception
   */
  private void testUIDGeneratorClass(Class<?> uidGenerator) throws Exception {
    //get the UIDGenerator's generateUID method
    Method generateUID = uidGenerator.getMethod("generateUID", (Class[])null);
    String currentUID = "";
    for (int i = 0; i < NUM_UIDS; i++) {
      currentUID = (String) generateUID.invoke(null, (Object[])null);
      //is it unique??
      if (uids.contains(currentUID)) {
        fail("UID set already contains UID [" + i + "]: " + currentUID);
      }
      uids.add(currentUID);
    }
  }

  //set up and tear down

  /**
   * Initialise members.
   *
   * @throws Exception
   *             if something goes wrong
   */
  public void setUp() throws Exception {

    try {

      uids = new HashSet<String>();

      super.setUp();
    } catch (Exception exception) {

      //try to tear down first
      tearDown();

      //then throw
      throw exception;
    }
  }

  /**
   * The teardown method for JUnit
   *
   * @throws Exception
   *             EXCEPTION TO DO
   */
  public void tearDown() throws Exception {

    uids.clear();

    //allow super to close down
    super.tearDown();
  }
}
