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
import java.net.URI;
import java.util.*;

// Third party packages
import junit.framework.*;        // JUnit
import org.apache.log4j.Logger;  // Log4J

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.tuples.*;

/**
 * Test case for {@link SubqueryAnswer}.
 *
 * @created 2004-11-01
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:24 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SubqueryAnswerUnitTest extends TestCase {

  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(SubqueryAnswerUnitTest.class.getName());

  /**
   * The URI of the {@link #database}: <code>local:database</code>.
   */
  @SuppressWarnings("unused")
  private static final URI databaseURI = URI.create("local:database");

  /**
   * The URI of the {@link #database}'s system model:
   * <code>local:database#</code>.
   */
  @SuppressWarnings("unused")
  private static final URI systemModelURI = URI.create("local:database#");

  /**
   * The URI of the {@link #database}'s system model type.
   */
  @SuppressWarnings("unused")
  private static final URI memoryModelURI = URI.create(Mulgara.NAMESPACE+"MemoryModel");

  /**
   * In-memory test {@link Database} used to generate {@link DatabaseSession}s
   * for testing.
   *
   * This is assigned a value by the {@link #setUp} method.
   */
  private Database database = null;

  /**
   * Constructs a new test with the given name.
   *
   * @param name  the test name
   */
  public SubqueryAnswerUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return the test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new SubqueryAnswerUnitTest("testVariableMappings"));

    return suite;
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

  /**
   * Create test objects.
   */
  public void setUp() throws Exception {
  }

  /**
   * The teardown method for JUnit
   */
  public void tearDown() {
  }

  //
  // Test cases
  //

  /**
   * Testing mapping tuples and answers with differing variable values.
   */
  public void testVariableMappings() throws Exception {
    ResolverSession testResolver = new TestResolverSession();
    OperationContext testContext = new TestOperationContext();

    Variable varX = new Variable("x");
    Variable varY = new Variable("y");
    Variable varZ = new Variable("z");

    // Create tuples with variables x, y, z.
    LiteralTuples tuples = new LiteralTuples(
        new Variable[] { varX, varY, varZ } );

    // Create variable list with differing order.
    ArrayList<Variable> variableList = new ArrayList<Variable>();
    variableList.add(varX);
    variableList.add(varZ);
    variableList.add(varY);

    // Create subquery.
    SubqueryAnswer answer = new SubqueryAnswer(testContext, testResolver, tuples, variableList);

    // Get column index of Z.
    int columnIndexZ = answer.getColumnIndex(varZ);

    // Column index should be equal to given variable list not underlying
    // tuples.
    assertEquals("Based on variable list Z should be", 1, columnIndexZ);
  }

  private class TestOperationContext implements OperationContext {
    public ResolverFactory findModelResolverFactory(long model)
      throws QueryException { return null; }

    public ResolverFactory findModelTypeResolverFactory(URI modelTypeURI)
      throws QueryException { return null; }

    public List<SecurityAdapter> getSecurityAdapterList() { return null; }

    public Resolver obtainResolver(ResolverFactory resolverFactory)
      throws QueryException { return null; }

    public long getCanonicalModel(long model) { return 0; }

    public Answer doQuery(Query query)
    throws Exception { return null; }

    public SystemResolver getSystemResolver() { return null; } // FIXME: Scaffolding for transactions.

    public Tuples resolve(Constraint constraint) throws QueryException { return null; }
  }
}
