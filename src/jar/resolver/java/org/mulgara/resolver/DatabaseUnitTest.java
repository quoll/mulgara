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

package org.mulgara.resolver;

// Java 2 standard packages
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

// Third party packages
import junit.framework.*;        // JUnit
import org.apache.log4j.Logger;  // Log4J

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.util.FileUtil;

/**
 * Test case for {@link Database}.
 *
 * @created 2004-04-27
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.10 $
 * @modified $Date: 2005/06/26 12:48:11 $
 * @maintenanceAuthor $Author: pgearon $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class DatabaseUnitTest extends TestCase {
  /** Logger.  */
  @SuppressWarnings("unused")
  private Logger logger = Logger.getLogger(DatabaseUnitTest.class.getName());

  /**
   * Constructs a new test with the given name.
   *
   * @param name  the name of the test
   */
  public DatabaseUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return the test suite
   */
  public static Test suite() {
    return new TestSuite(DatabaseUnitTest.class);
  }

  /**
   * Create test instance.
   */
  public void setUp() {
    // null implementation
  }

  /**
   * The teardown method for JUnit
   */
  public void tearDown() {
    // null implementation
  }

  //
  // Test cases
  //

  /**
   * Test {@link Database} constructor with invalid parameters.
   */
  public void test1Constructor() {
    try {
      new Database(null, (File)null, null, null, 0, 0, null, null, null, null, null,
                   null, null, null, null, null, null, null, null);
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // correct behavior
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Test {@link Database} constructor with valid parameters.
   */
  public void testQuery()
    throws ClassNotFoundException, IllegalAccessException,
           InstantiationException, URISyntaxException {
    URI databaseURI    = new URI("local:database");
    URI systemModelURI = new URI("local:database#");

    // Create the persistence directory
    File persistenceDirectory =
      new File(new File(System.getProperty("cvs.root")), "testDatabase");
    if (persistenceDirectory.isDirectory()) {
      if (!FileUtil.deleteDirectory(persistenceDirectory)) {
        throw new RuntimeException(
          "Unable to remove old directory " + persistenceDirectory
        );
      }
    }
    if (!persistenceDirectory.mkdirs()) {
      throw new RuntimeException(
        "Unable to create directory " + persistenceDirectory
      );
    }

    // Construct a database
    Database database = null;
    try {

      // Create a database which keeps its system models on the Java heap
      database = new Database(
          databaseURI,
          persistenceDirectory,
          null,  // no security domain
          new JotmTransactionManagerFactory(),
          0,  // default transaction timeout
          0,  // default idle timeout
          "org.mulgara.store.nodepool.memory.MemoryNodePoolFactory",
          null,
          "org.mulgara.store.stringpool.memory.MemoryStringPoolFactory",
          null,
          "org.mulgara.resolver.memory.MemoryResolverFactory",
          null,
          "org.mulgara.store.nodepool.memory.MemoryNodePoolFactory",
          null,
          "org.mulgara.store.stringpool.memory.MemoryStringPoolFactory",
          null,
          "org.mulgara.resolver.memory.MemoryResolverFactory",
          null,
          null);  // no default content handler
    } catch (Exception e) {
      fail(e);
    }
  }

  //
  // Internal methods
  //

  /**
   * Fail with an unexpected exception
   */
  private void fail(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    fail(stringWriter.toString());
  }
}
