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
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.server.Session;
import org.mulgara.util.FileUtil;

/**
* Test case for {@link DatabaseSession}.
*
* @created 2004-04-27
* @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
* @version $Revision: 1.10 $
* @modified $Date: 2005/06/26 12:48:11 $ by $Author: pgearon $
* @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
* @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
*      Software Pty Ltd</a>
* @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
*/
public class XADatabaseSessionUnitTest extends TestCase {
  /** The URI of the {@link #database}: <code>local:database</code>.  */
  private static final URI databaseURI = URI.create("local:database");

  /**
  * The URI of the {@link #database}'s system model:
  * <code>local:database#</code>.
  */
  @SuppressWarnings("unused")
  private static final URI systemModelURI = URI.create("local:database#");

  /** The URI of the {@link #database}'s system model type.  */
  @SuppressWarnings("unused")
  private static final URI memoryModelURI = URI.create(Mulgara.NAMESPACE+"MemoryModel");

  /** Logger.  */
  private static Logger logger = Logger.getLogger(XADatabaseSessionUnitTest.class.getName());

  /**
  * In-memory test {@link Database} used to generate {@link DatabaseSession}s
  * for testing.
  *
  * This is assigned a value by the {@link #setUp} method.
  */
  private Database database = null;

  private TestDef test;

  protected static final URI[] modelDataURIs;
  static {
    modelDataURIs = new URI[] {
      null, // No data to load for system model.
      new File("data/test-model1.rdf").toURI(),
      new File("data/test-model2.rdf").toURI(),
      new File("data/test-model3.rdf").toURI(),
      new File("data/test-model4.rdf").toURI(),
      new File("data/test-model5.rdf").toURI(),
      new File("data/test-model6.rdf").toURI(),
      new File("data/test-model7.rdf").toURI(),
      new File("data/test-model8.rdf").toURI(),
      new File("data/test-model9.rdf").toURI(),
      new File("data/test-model10.rdf").toURI(),
      new File("data/test-model11.rdf").toURI(),
    };
  };


  protected static final URI[] modelURIs;
  static {
    try {
      //String baseuri = "rmi://" + InetAddress.getLocalHost().getCanonicalHostName() + "/server1#";
      String baseuri = "local:database#";
      modelURIs = new URI[] {
        new URI(baseuri), //new URI("local:database#"),
        new URI(baseuri + "test-model1"),
        new URI(baseuri + "test-model2"),
        new URI(baseuri + "test-model3"),
        new URI(baseuri + "test-model4"),
        new URI(baseuri + "test-model5"),
        new URI(baseuri + "test-model6"),
        new URI(baseuri + "test-model7"),
        new URI(baseuri + "test-model8"),
        new URI(baseuri + "test-model9"),
        new URI(baseuri + "test-model10"),
        new URI(baseuri + "test-model11"),
      };
    } catch (URISyntaxException eu) {
      throw new IllegalArgumentException("Initialising ModelDataURIs");
    //} catch (java.net.UnknownHostException eu) {
    //  throw new IllegalArgumentException("Failed to identify hostname");
    }
  };


  protected static final GraphResource[] models;
  static {
    models = new GraphResource[modelURIs.length];
    for (int i = 0; i < modelURIs.length; i++) {
      models[i] = new GraphResource(modelURIs[i]);
    }
  };


  protected static final ConstraintElement[] elements;
  static {
    try {
      elements = new ConstraintElement[] {
        new Variable("subject"),                         // V0
        new Variable("predicate"),                       // V1
        new Variable("object"),                          // V2
        new URIReferenceImpl(new URI("test:s02")),       // V3
        new URIReferenceImpl(new URI("test:p01")),       // V4
        new URIReferenceImpl(new URI("test:p02")),       // V5
        new URIReferenceImpl(new URI("test:p03")),       // V6
        new LiteralImpl("o01"),                          // V7
      };
    } catch (URISyntaxException eu) {
      throw new IllegalArgumentException("Initialising ConstraintElements");
    }
  };


  static TestDef.Parser parser = new TestDef.Parser(elements, models);


  protected static final TestDef[] tests = {
    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V1 (| V3 V1 V2))",
                 "M4",
                 "(result p04 p05 p06 p08)",
                 "testModelPrimitive"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V0 (| V0 V1 V2))",
                 "M4",
                 "(result s01 s02)",
                 "testSubjectModelPrimitive"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V0 (| V0 V4 V2))",
                 "M10",
                 "(result s01 s02)",
                 "testSubjectVarObjModelPrimitive"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V0 (| V0 V4 V7))",
                 "M10",
                 "(result s01)",
                 "testSubjectLitObjModelPrimitive"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V0 (| V0 V4 V2))",
                 "M11",
                 "(result _node85 _node86)",
                 "testSubjectVarObjModelPrimitive"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V0 (| V0 V4 V7))",
                 "M11",
                 "(result _node85)",
                 "testSubjectLitObjModelPrimitive"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (| V3 V5 V2)))",
                 "M6",
                 "(result o04 o05)",
                 "testConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (| V3 V5 V2)))",
                 "M6",
                 "(result o01 o02 o04 o05 o06 o07)",
                 "testDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V1 (| V3 V1 V2))",
                 "(union M4 M5)",
                 "(result p04 p05 p06 p07 p08 p09)",
                 "testUnion"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V1 (| V3 V1 V2))",
                 "(intersect M4 M5)",
                 "(result p04 p05)",
                 "testIntersect"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (| V3 V5 V2))",
                 "(union M7 M8)",
                 "(result o02 o05 o08)",
                 "testUnionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (| V3 V5 V2)))",
                 "(intersect M7 M8)",
                 "(result o05)",
                 "testIntersectConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (| V3 V5 V2)))",
                 "(union M7 M8)",
                 "(result o01 o02 o03 o04 o05 o06 o07 o08 o09)",
                 "testUnionDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (| V3 V5 V2)))",
                 "(intersect M7 M8)",
                 "(result o04 o05 o06)",
                 "testIntersectDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V1 (| V3 V1 V2))",
                 "(union M4 (union M5 M9))",
                 "(result p04 p05 p06 p07 p08 p09 p10)",
                 "testModelUnionUnion"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V1 (| V3 V1 V2))",
                 "(union M4 (intersect M5 M9))",
                 "(result p04 p05 p06 p07 p08)",
                 "testModelUnionIntersect"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V1 (| V3 V1 V2))",
                 "(intersect M4 (union M5 M9))",
                 "(result p04 p05 p06)",
                 "testModelIntersectUnion"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V1 (| V3 V1 V2))",
                 "(intersect M4 (intersect M5 M9))",
                 "(result p04)",
                 "testModelIntersectIntersect"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (and (| V3 V5 V2) (| V3 V6 V2))))",
                 "M6",
                 "(result o04)",
                 "testConjunctionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (or (| V3 V5 V2) (| V3 V6 V2))))",
                 "M6",
                 "(result o04 o05 o06)",
                 "testConjunctionDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (and (| V3 V5 V2) (| V3 V6 V2))))",
                 "M6",
                 "(result o01 o04 o05 o06 o07)",
                 "testDisjunctionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (or (| V3 V5 V2) (| V3 V6 V2))))",
                 "M6",
                 "(result o01 o02 o03 o04 o05 o06 o07)",
                 "testDisjunctionDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (and (| V3 V5 V2) (| V3 V6 V2))))",
                 "(union M1 (union M2 M3))",
                 "(result o43 o44 o45 o46 o47 o48 o49)",
                 "testUnionUnionConjunctionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (or (| V3 V5 V2) (| V3 V6 V2))))",
                 "(union M1 (union M2 M3))",
                 "(result o22 o23 o24 o25 o26 o27 o28 " +
                         "o29 o30 o31 o32 o33 o34 o35 " +
                         "o43 o44 o45 o46 o47 o48 o49)",
                 "testUnionUnionConjunctionDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (and (| V3 V5 V2) (| V3 V6 V2))))",
                 "(union M1 (union M2 M3))",
                 "(result o01 o02 o03 o04 o05 o06 o07 " +
                         "o22 o23 o24 o25 o26 o27 o28 " +
                         "o29 o30 o31 o32 o33 o34 o35 " +
                         "o36 o37 o38 o39 o40 o41 o42 " +
                         "o43 o44 o45 o46 o47 o48 o49)",
                 "testUnionUnionDisjunctionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (or (| V3 V5 V2) (| V3 V6 V2))))",
                 "(union M1 (union M2 M3))",
                 "(result o01 o02 o03 o04 o05 o06 o07 " +
                         "o08 o09 o10 o11 o12 o13 o14 " +
                         "o15 o16 o17 o18 o19 o20 o21 " +
                         "o22 o23 o24 o25 o26 o27 o28 " +
                         "o29 o30 o31 o32 o33 o34 o35 " +
                         "o36 o37 o38 o39 o40 o41 o42 " +
                         "o43 o44 o45 o46 o47 o48 o49)",
                 "testUnionUnionDisjunctionDisjunction"),
/*
    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (and (| V3 V5 V2) (| V3 V6 V2))))",
                 "(union M1 (intersect M2 M3))",
                 "(result o43 o46 o47 o48 o49)",
                 "testUnionIntersectConjunctionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (or (| V3 V5 V2) (| V3 V6 V2))))",
                 "(union M1 (intersect M2 M3))",
                 "(result o22 o25 o26 o27 o28 " +
                         "o29 o32 o33 o34 o35 " +
                         "o43 o46 o47 o48 o49)",
                 "testUnionIntersectConjunctionDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (and (| V3 V5 V2) (| V3 V6 V2))))",
                 "(union M1 (intersect M2 M3))",
                 "(result o01 o04 o05 o06 o07 " +
                         "o22 o25 o26 o27 o28 " +
                         "o29 o32 o33 o34 o35 " +
                         "o36 o39 o40 o41 o42 " +
                         "o43 o46 o47 o48 o49)",
                 "testUnionIntersectDisjunctionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (or (| V3 V5 V2) (| V3 V6 V2))))",
                 "(union M1 (intersect M2 M3))",
                 "(result o01 o04 o05 o06 o07 " +
                         "o08 o11 o12 o13 o14 " +
                         "o15 o18 o19 o20 o21 " +
                         "o22 o25 o26 o27 o28 " +
                         "o29 o32 o33 o34 o35 " +
                         "o36 o39 o40 o41 o42 " +
                         "o43 o46 o47 o48 o49)",
                 "testUnionIntersectDisjunctionDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (and (| V3 V5 V2) (| V3 V6 V2))))",
                 "(intersect M1 (union M2 M3))",
                 "(result o46 o47 o49)",
                 "testIntersectUnionConjunctionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (or (| V3 V5 V2) (| V3 V6 V2))))",
                 "(intersect M1 (union M2 M3))",
                 "(result o25 o26 o28 " +
                         "o32 o33 o35 " +
                         "o46 o47 o49)",
                 "testIntersectUnionConjunctionDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (and (| V3 V5 V2) (| V3 V6 V2))))",
                 "(intersect M1 (union M2 M3))",
                 "(result o04 o05 o07 " +
                         "o25 o26 o28 " +
                         "o32 o33 o35 " +
                         "o39 o40 o42 " +
                         "o46 o47 o49)",
                 "testIntersectUnionDisjunctionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (or (| V3 V5 V2) (| V3 V6 V2))))",
                 "(intersect M1 (union M2 M3))",
                 "(result o04 o05 o07 " +
                         "o11 o12 o14 " +
                         "o18 o19 o21 " +
                         "o25 o26 o28 " +
                         "o32 o33 o35 " +
                         "o39 o40 o42 " +
                         "o46 o47 o49)",
                 "testIntersectUnionDisjunctionDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (and (| V3 V5 V2) (| V3 V6 V2))))",
                 "(intersect M1 (intersect M2 M3))",
                 "(result o49)",
                 "testIntersectIntersectConjunctionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (and (| V3 V4 V2) (or (| V3 V5 V2) (| V3 V6 V2))))",
                 "(intersect M1 (intersect M2 M3))",
                 "(result o28 " +
                         "o35 " +
                         "o49)",
                 "testIntersectIntersectConjunctionDisjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (and (| V3 V5 V2) (| V3 V6 V2))))",
                 "(intersect M1 (intersect M2 M3))",
                 "(result o07 " +
                         "o28 " +
                         "o35 " +
                         "o42 " +
                         "o49)",
                 "testIntersectIntersectDisjunctionConjunction"),

    parser.parse("testModel",
                 new String[] { "org.mulgara.resolver.store.StatementStoreResolverFactory" },
                 "(query V2 (or (| V3 V4 V2) (or (| V3 V5 V2) (| V3 V6 V2))))",
                 "(intersect M1 (intersect M2 M3))",
                 "(result o07 " +
                         "o14 " +
                         "o21 " +
                         "o28 " +
                         "o35 " +
                         "o42 " +
                         "o49)",
                 "testIntersectIntersectDisjunctionDisjunction"),
*/
    };

  /**
  * Constructs a new test with the given name.
  *
  * @param testcase  the bundled parameters for the test
  */
  public XADatabaseSessionUnitTest(TestDef testcase) {
    super(testcase.name);

    this.test = testcase;
  }


  /**
  * Hook for test runner to obtain a test suite from.
  *
  * @return the test suite
  */
  public static Test suite() {
    TestSuite suite = new TestSuite();

    for (int i = 0; i < tests.length; i++) {
      suite.addTest(new XADatabaseSessionUnitTest(tests[i]));
    }

    return suite;
  }

  /**
  * Create test objects.
  */
  public void setUp() throws Exception {
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
      throw new Exception("Unable to create directory "+persistenceDirectory);
    }

    // Define the the node pool factory
    String nodePoolFactoryClassName =
      "org.mulgara.store.nodepool.xa.XANodePoolFactory";

    // Define the string pool factory
    String stringPoolFactoryClassName =
      "org.mulgara.store.stringpool.xa.XAStringPoolFactory";

    String tempNodePoolFactoryClassName =
      "org.mulgara.store.nodepool.memory.MemoryNodePoolFactory";

    // Define the string pool factory
    String tempStringPoolFactoryClassName =
      "org.mulgara.store.stringpool.memory.MemoryStringPoolFactory";

    // Define the resolver factory used to manage system models
    String systemResolverFactoryClassName =
      "org.mulgara.resolver.store.StatementStoreResolverFactory";

    // Define the resolver factory used to manage system models
    String tempResolverFactoryClassName =
      "org.mulgara.resolver.memory.MemoryResolverFactory";

    @SuppressWarnings("unused")
    String ruleLoaderFactoryClassName = "org.mulgara.rules.RuleLoaderFactory";

    // Create a database which keeps its system models on the Java heap
    database = new Database(
                 databaseURI,
                 persistenceDirectory,
                 null,                            // no security domain
                 new JotmTransactionManagerFactory(),
                 0,                               // default transaction timeout
                 0,                               // default idle timeout
                 nodePoolFactoryClassName,        // persistent
                 new File(persistenceDirectory, "xaNodePool"),
                 stringPoolFactoryClassName,      // persistent
                 new File(persistenceDirectory, "xaStringPool"),
                 systemResolverFactoryClassName,  // persistent
                 new File(persistenceDirectory, "xaStatementStore"),
                 tempNodePoolFactoryClassName,    // temporary nodes
                 null,                            // no dir for temp nodes
                 tempStringPoolFactoryClassName,  // temporary strings
                 null,                            // no dir for temp strings
                 tempResolverFactoryClassName,    // temporary models
                 null,                            // no dir for temp models
                 "",                              // no rule loader
                 "org.mulgara.content.rdfxml.RDFXMLContentHandler");

    database.addResolverFactory("org.mulgara.resolver.url.URLResolverFactory", null);

    try {
      URI modelTypeURI = new URI(Mulgara.NAMESPACE+"Model");
      Session session = database.newSession();
      for (int i = 1; i < modelURIs.length; i++) {
        session.createModel(modelURIs[i], modelTypeURI);
        session.setModel(modelURIs[i], modelDataURIs[i]);
      }
    } catch (URISyntaxException e) {
      throw new Error("Bad hardcoded XA store model URI", e);
    }
  }

  /**
  * The teardown method for JUnit
  */
  public void tearDown() {
    logger.debug("TearDown Database");
    database.delete();
  }

  //
  // Test cases
  //

  public void testModel() {
    try {
      logger.debug("Testing: " + test.errorString);
      Session session = database.newSession();
      try {
        List<Order> orderList = new ArrayList<Order>();
        for (Variable v: test.selectList) orderList.add(new Order(v, true));

        // Evaluate the query
        Answer answer = new ArrayAnswer(session.query(new Query(
          test.selectList,          // SELECT
          test.model,               // FROM
          test.query,               // WHERE
          null,                     // HAVING
          orderList,                // ORDER BY
          null,                     // LIMIT
          0,                        // OFFSET
          true,                     // DISTINCT
          new UnconstrainedAnswer() // GIVEN
        )));

        logger.debug("Results Expected in " + test.errorString + " = " + test.results);
        logger.debug("Results Received in " + test.errorString + " = " + answer);
        Iterator<List<Object>> i = test.results.iterator();
        answer.beforeFirst();
        while (true) {
          boolean hasAnswer = answer.next();
          boolean hasResult = i.hasNext();
          if (!hasAnswer && !hasResult) break;
          int c = 0;
          for (Object obj: i.next()) {
            assertEquals(test.errorString, obj.toString(), answer.getObject(c++).toString());
          }
        }
      } finally {
        session.close();
      }
    } catch (Exception e) {
      fail(test.errorString, e);
    }
  }

  //
  // Internal methods
  //

  /**
  * Fail with an unexpected exception
  */
  @SuppressWarnings("unused")
  private void fail(Throwable throwable) {
    fail(null, throwable);
  }


  private void fail(String test, Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    if (test != null) {
      stringWriter.write(test + ": ");
    }
    throwable.printStackTrace(new PrintWriter(stringWriter));
    fail(stringWriter.toString());
  }
}
