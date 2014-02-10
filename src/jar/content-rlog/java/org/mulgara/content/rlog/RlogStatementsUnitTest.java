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

package org.mulgara.content.rlog;

// Third party packages
import junit.framework.*;        // JUnit unit testing framework
import org.apache.log4j.Logger;  // Apache Log4J


/**
 * @created Feb 24, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class RlogStatementsUnitTest extends TestCase {
  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(RlogStatementsUnitTest.class.getName());

  //
  // Constructors
  //

  /**
   * Construct a test.
   *
   * @param name  the name of the test to construct
   */
  public RlogStatementsUnitTest(String name) {
    super(name);
  }

  //
  // Methods implementing TestCase
  //

  public void setup() {
  }

  /**
   * Hook from which the test runner can obtain a test suite.
   *
   * @return the test suite
   */
  public static Test suite() {
    return new TestSuite(RlogStatementsUnitTest.class);
  }


  /**
   * Test {@link RlogStatements} parsing a file.
   *
   * @throws Exception if there's an error running the test (note that if the
   *   test merely fails, this should <em>not</em> throw any exception
   */
  public void test1() throws Exception {
  }

}
