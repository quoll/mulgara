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

package org.mulgara.store.xa;

// Java 2 standard packages
import java.io.*;

// Third party packages
import junit.framework.*;
import org.apache.log4j.Logger;

import org.mulgara.util.TempDir;


/**
 * Test cases for MappedBlockFile.
 *
 * @created 2001-09-20
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:31 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MappedBlockFileTest extends BlockFileTest {

  /**
   * Logger.
   *
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(MappedBlockFileTest.class);

  /**
   * Named constructor.
   *
   * @param name The name of the test.
   */
  public MappedBlockFileTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new MappedBlockFileTest("testAllocate"));
    suite.addTest(new MappedBlockFileTest("testWrite"));
    suite.addTest(new MappedBlockFileTest("testPersist"));

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
   * @throws IOException EXCEPTION TO DO
   */
  public void setUp() throws IOException {

    boolean exceptionOccurred = true;

    try {

      File dir = TempDir.getTempDir();
      blockFile = new MappedBlockFile(new File(dir, "mbftest"), BLOCK_SIZE);
      exceptionOccurred = false;
    }
    finally {

      if (exceptionOccurred) {

        tearDown();
      }
    }
  }
}
