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

package org.mulgara.resolver.jrdf;


//JRDF
import org.jrdf.graph.mem.BlankNodeImpl;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.mem.GraphImpl;

// Third party packages
import junit.framework.*;

/**
 * Unit test for testing a BlankNodeImpl to long Map.
 *
 * @created 2004-08-24
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:24 $
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
public abstract class AbstractBlankNodeMapUnitTest extends TestCase {

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public AbstractBlankNodeMapUnitTest(String name) {
    super(name);
  }

  /**
   * Implemented by extending classes to return the Map to be tested.
   *
   * @throws Exception
   * @return BlankNodeMap
   */
  public abstract BlankNodeMap getMap() throws Exception;

  /**
   * Creates and closes the BlankNodeMap.
   *
   * @throws Exception
   */
  public void testCreate() throws Exception {
    getMap().close();
  }

  /**
   * Tests insert operations.
   *
   * @throws Exception
   */
  public void testPut() throws Exception {

    BlankNodeMap map = null;

    try {

      //number of mappings to insert and delete
      int NUM_MAPPINGS = 5000;
      map = getMap();
      BlankNodeImpl currentNode = null;

      //creates the BlankNodeImpl objects
      GraphElementFactory factory = new GraphImpl().getElementFactory();

      //insert
      for (long i = 1; i < NUM_MAPPINGS; i++) {

        //get a new node
        currentNode = (BlankNodeImpl) factory.createResource();
        //insert node using it's local Id
        map.put(currentNode, i);
      }

      //ensure mappings are consistent
      long currentId = -1;
      for (long i = 1; i < NUM_MAPPINGS; i++) {

        currentNode = map.get(i);
        currentId = map.get(currentNode);
        assertEquals("ID used to get Node is not equal to the ID returned by " +
                     "get(Node) ", i, currentId);
      }
    } finally {

      if (map != null) {

        map.close();
      }
    }
  }

  /**
   * Tests insert and delete operations.
   *
   * @throws Exception
   */
  public void testPutAndRemove() throws Exception {

    BlankNodeMap map = null;

    try {

      //number of mappings to insert and delete
      int NUM_MAPPINGS = 5000;
      map = getMap();
      BlankNodeImpl currentNode = null;

      //creates the BlankNodeImpl objects
      GraphElementFactory factory = new GraphImpl().getElementFactory();

      //insert
      for (long i = 1; i < NUM_MAPPINGS; i++) {

        //get a new node
        currentNode = (BlankNodeImpl) factory.createResource();
        //insert node using it's local Id
        map.put(currentNode, i);
      }

      //ensure mappings are consistent
      long currentId = -1;
      for (long i = 1; i < NUM_MAPPINGS; i++) {

        currentNode = map.get(i);
        currentId = map.get(currentNode);
        assertEquals("ID used to get Node is not equal to the ID returned by " +
                     "get(Node) ", i, currentId);
      }

      //remove the mappings
      for (long i = 1; i < NUM_MAPPINGS; i++) {

        //used for checking that the remove was successfull
        currentNode = map.get(i);
        assertNotNull("map should contain node for id: " + i, currentNode);

        map.remove(i);

        //ensure that the node and id were removed
        assertNull("Map still contains ID after it was removed.", map.get(i));
        assertTrue("Map still contains Node after it was removed.",
                   map.get(currentNode) == -1);
      }
    } finally {

      if (map != null) {

        map.close();
      }
    }
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    //dont run any test. this is an abstract class.
    return new TestSuite();
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

  //set up and tear down

  /**
   * Initialise members.
   *
   * @throws Exception if something goes wrong
   */
  public void setUp() throws Exception {
  }

  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void tearDown() throws Exception {
  }
}
