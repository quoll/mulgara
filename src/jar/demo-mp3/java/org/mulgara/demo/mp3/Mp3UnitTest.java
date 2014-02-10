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

package org.mulgara.demo.mp3;

// Java 2 standard packages
import java.net.*;

// Third party packages
import junit.framework.*;
import org.apache.log4j.*;
import org.mulgara.query.rdf.*;

import java.io.*;
import org.jrdf.graph.*;
import java.util.*;

// JRDF

// Mulgara


/**
 * Basic test of the Mp3 demo model.
 *
 * @created 2004-12-21
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/01/05 04:58:06 $
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
public class Mp3UnitTest extends TestCase {

  /**
   * init the logging class
   */
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(Mp3UnitTest.class.getName());

  /** Default config file location */
  private static final String CONFIG_FILE = System.getProperty("cvs.root") +
      "/conf/mp3-config.xml";

  /** Default Schema File */
  private static final String SCHEMA_FILE = System.getProperty("cvs.root") +
      "/conf/mp3.rdfs";

  /** Default Mp3 Directory (test data) */
  private static final String MP3_DIR = System.getProperty("cvs.root") +
      "/data/mp3";

  /** Property used in find */
  private static final String FIND_PROPERTY = "http://mulgara.org/mulgara/id3#title";

  /** Value used in find */
  private static final String FIND_VALUE = "Residence";

  /** Controls the Mp3 Models */
  private Mp3Controller controller = null;

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public Mp3UnitTest(String name) {
    super(name);
    //Logger.getRootLogger().setLevel(Level.ERROR);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new Mp3UnitTest("testCreateModels"));
    suite.addTest(new Mp3UnitTest("testGetModels"));
    suite.addTest(new Mp3UnitTest("testClearModels"));
    suite.addTest(new Mp3UnitTest("testLoadSchema"));
    suite.addTest(new Mp3UnitTest("testLoadDirectory"));
    suite.addTest(new Mp3UnitTest("testGetMp3s"));
    suite.addTest(new Mp3UnitTest("testFindMp3s"));
    suite.addTest(new Mp3UnitTest("testDeleteModels"));
    return suite;
  }

  /**
   * Creates the models.
   * @throws Exception
   */
  public void testCreateModels() throws Exception {
    controller.createModels();
  }

  /**
   * Ensures all models are obtainable
   * @throws Exception
   */
  public void testGetModels() throws Exception {
    controller.getFileSystemModel();
    controller.getSchemaModel();
    controller.getMp3Model();
  }

  /**
   * Clears the models.
   * @throws Exception
   */
  public void testClearModels() throws Exception {
    controller.getFileSystemModel().clear();
    controller.getSchemaModel().clear();
    controller.getMp3Model().clear();
  }

  /**
   * Loads a schema into the Schema model
   * @throws Exception
   */
  public void testLoadSchema() throws Exception {
    controller.getSchemaModel().loadSchema(new URIReferenceImpl(new File(
        SCHEMA_FILE).toURI()));
  }

  /**
   * Loads a Directory of mp3s into the mp3 model.
   * @throws Exception
   */
  public void testLoadDirectory() throws Exception {
    controller.getFileSystemModel().includeDirectory(new File(MP3_DIR).toURI().
        toURL());
    controller.getMp3Model().loadMp3s(controller.getFileSystemModel());
  }

  /**
   * Searches for all mp3.
   * @throws Exception
   */
  public void testGetMp3s() throws Exception {
    try {
      Mp3Iterator iter = controller.getMp3Model().getMp3s();
      assertTrue("Find returned no mp3s.", iter.hasNext());
      iter.close();
    }
    catch (Exception exception) {
      exception.printStackTrace();
      throw exception;
    }
  }

  /**
   * Searches for a specific mp3.
   * @throws Exception
   */
  public void testFindMp3s() throws Exception {
    try {
      PredicateNode property = new URIReferenceImpl(new URI(FIND_PROPERTY));
      ObjectNode value = new LiteralImpl(FIND_VALUE);
      Mp3Iterator iter = controller.getMp3Model().findMp3s(
          Collections.singleton(property).iterator(),
          Collections.singleton(value).iterator());
      assertTrue("Find returned no mp3s.", iter.hasNext());
      iter.close();
    }
    catch (Exception exception) {
      exception.printStackTrace();
      throw exception;
    }
  }

  /**
   * Deletes the models.
   * @throws Exception
   */
  public void testDeleteModels() throws Exception {
    controller.getFileSystemModel().delete();
    controller.getSchemaModel().delete();
    controller.getMp3Model().delete();
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
  @SuppressWarnings("finally")
  public void setUp() throws Exception {

    try {

      //create controller and load default configuration.
      controller = Mp3Controller.newInstance();
      controller.init(new Mp3ConfigFile(new File(CONFIG_FILE).toURI().toURL()));

      //let superclass set up too
      super.setUp();
    }
    catch (Exception exception) {

      exception.printStackTrace();

      //try to tear down first
      try {

        tearDown();
      } finally {

        throw exception;
      }
    }
  }

  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void tearDown() throws Exception {

    // it is ok to close the controller as the models are persistant
    if (controller != null) {
      controller.close();
    }

    //allow super to close down too
    super.tearDown();
  }
}
