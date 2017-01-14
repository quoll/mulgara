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
 * Contributor(s):
 * The copyright to this file is held by:
 *   The Australian Commonwealth Government
 *   Department of Defense
 * Developed by Netymon Pty Ltd
 * under contract 4500430665
 * contributed to the Mulgara Project under the
 *   Mozilla Public License version 1.1
 * per clause 4.1.3 of the above contract.
 *
 * SymbolicTransformation context and test's 6 and 7
 * contributed by Netymon Pty Ltd on behalf of
 * The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.xsd;

// Java 2 standard packages
import java.net.URI;
import java.util.Arrays;

// Third party packages
import junit.framework.*;        // JUnit
import org.apache.log4j.Logger;  // Apache Log4J

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.SymbolicTransformationContext;

/**
 * Unit testing suite for {@link IntervalTransformation}.
 *
 * @created 2005-05-25
 *
 * @author <a href="mailto:raboczi@itee.uq.edu.au">Simon Raboczi</a>
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/06/09 09:26:20 $ @maintenanceAuthor $Author: raboczi $
 *
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class IntervalTransformationUnitTest extends TestCase {
  /** Logger */
  private static Logger logger = Logger.getLogger(IntervalTransformationUnitTest.class);

  private URIReferenceImpl greaterThan;

  private URIReferenceImpl lessThan;

  /**
   * Test instance.
   */
  private IntervalTransformation intervalTransformation;

  /**
   * Test context for transformation.
   */
  private SymbolicTransformationContext context;

  /**
   * ModelTypeURI for XSD resolver.
   */
  private URI modelTypeURI;

  /**
   * ModelTypeURI for non XSD resolver.
   */
  private URI nonXSDTypeURI;

  /**
   * model that is an XSD model.
   */
  private URIReferenceImpl xsdModel;

  /**
   * Fake model that isn't an XSD model.
   */
  private URIReferenceImpl nonXSDModel;

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public IntervalTransformationUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return the test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTest(new IntervalTransformationUnitTest("test1Transform"));
    suite.addTest(new IntervalTransformationUnitTest("test2Transform"));
    suite.addTest(new IntervalTransformationUnitTest("test3Transform"));
    suite.addTest(new IntervalTransformationUnitTest("test4Transform"));
    suite.addTest(new IntervalTransformationUnitTest("test5Transform"));
    suite.addTest(new IntervalTransformationUnitTest("test6IgnoreNonXSD"));
    suite.addTest(new IntervalTransformationUnitTest("test7PreserveNonXSD"));
    suite.addTest(new IntervalTransformationUnitTest("test8IgnoreNoVars"));
    suite.addTest(new IntervalTransformationUnitTest("test9IgnoreAllVars"));

    return suite;
  }

  /**
   * Create test instance.
   */
  public void setUp() throws Exception {
    greaterThan     = new URIReferenceImpl(new URI(Mulgara.NAMESPACE + "lt"));
    lessThan        = new URIReferenceImpl(new URI(Mulgara.NAMESPACE + "gt"));

    modelTypeURI = new URI(Mulgara.NAMESPACE + "XMLSchemaModel");
    nonXSDTypeURI = new URI(Mulgara.NAMESPACE + "NotAXSDModel");

    nonXSDModel = new URIReferenceImpl(new URI("non:xsd:model"));
    xsdModel = new URIReferenceImpl(new URI("xsd:model"));

    intervalTransformation = new IntervalTransformation(modelTypeURI, lessThan, greaterThan);
    context = new SymbolicTransformationContext() {
        public URI mapToModelTypeURI(URI modelURI) throws QueryException {
          if (logger.isDebugEnabled()) {
            logger.debug("comparing " + modelURI + " against " + xsdModel);
          }
          if (modelURI.equals(xsdModel.getURI())) {
            return modelTypeURI;
          } else {
            return nonXSDTypeURI;
          }
        }
      };
  }

  /**
   * Default test runner.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  //
  // Test cases
  //

  /**
   * Test #1 for the {@link IntervalTransformation#transform} method.
   */
  public void test1Transform() throws Exception {
    try {
      assertNull(intervalTransformation.transformExpression(context, (ConstraintExpression) null));
    }
    catch (IllegalArgumentException e) {
      // expected
    }
  }

  /**
   * Test #2 for the {@link IntervalTransformation#transform} method.
   */
  public void test2Transform() throws Exception {
    assertSame(ConstraintTrue.INSTANCE, intervalTransformation.transformExpression(context, ConstraintTrue.INSTANCE));
  }

  /**
   * Test #3 for the {@link IntervalTransformation#transform} method.
   *
   * This transforms a single {@link ConstraintImpl}.
   */
  public void test3Transform() throws Exception {
    Variable x = new Variable("x");

    assertEquals(
      new IntervalConstraint(
        x,
        null,
        new Bound(3, false),
        xsdModel
      ),
      intervalTransformation.transformExpression(context, new ConstraintImpl(x, lessThan, new LiteralImpl(3), xsdModel)
      )
    );
  }

  /**
   * Test #4 for the {@link IntervalTransformation#transform} method.
   *
   * This transforms a single {@link ConstraintConjunction}.
   */
  public void test4Transform() throws Exception {
    Variable x = new Variable("x");

    assertEquals(new IntervalConstraint(
        x,
        new Bound(2, false),
        new Bound(3, false),
        xsdModel
      ),
      intervalTransformation.transformExpression(context, new ConstraintConjunction(
          new ConstraintImpl(x, greaterThan, new LiteralImpl(2), xsdModel),
          new ConstraintImpl(x, lessThan, new LiteralImpl(3), xsdModel)
        )
      )
    );
  }

  /**
   * Test #5 for the {@link IntervalTransformation#transform} method.
   *
   * This transforms a {@link ConstraintConjunction} containing
   * {@link IntervalConstraint}s over different variables.
   */
  public void test5Transform() throws Exception {
    Variable x = new Variable("x");
    Variable y = new Variable("y");

    assertEquals(new ConstraintConjunction(
        new IntervalConstraint(x, new Bound(2, false), null, xsdModel),
        new IntervalConstraint(y, null, new Bound(3, false), xsdModel)
      ),
      intervalTransformation.transformExpression(context, new ConstraintConjunction(
          new ConstraintImpl(x, greaterThan, new LiteralImpl(2), xsdModel),
          new ConstraintImpl(y, lessThan, new LiteralImpl(3), xsdModel)
        )
      )
    );
  }

  /**
   * Test #6 for the {@link IntervalTransformation#transform} method.
   *
   * This checks that non-xsd models will not be transformed.
   */
  public void test6IgnoreNonXSD() throws Exception {
    Variable x = new Variable("x");
    Variable y = new Variable("y");

    ConstraintConjunction cc = new ConstraintConjunction(
          new ConstraintImpl(x, greaterThan, new LiteralImpl(2), nonXSDModel),
          new ConstraintImpl(y, lessThan, new LiteralImpl(3), nonXSDModel)
        );
    assertSame(cc, intervalTransformation.transformExpression(context, cc));
  }

  /**
   * Test #7 for the {@link IntervalTransformation#transform} method.
   *
   * This checks that xsd models will be transformed in the presence of
   * non-xsd models which will be preserved.
   */
  public void test7PreserveNonXSD() throws Exception {
    Variable x = new Variable("x");

    assertEquals(
      new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
          new ConstraintImpl(x, greaterThan, new LiteralImpl(3), nonXSDModel),
          new ConstraintImpl(x, lessThan, new LiteralImpl(4), nonXSDModel),
          new IntervalConstraint(
            x,
            new Bound(2, false),
            new Bound(3, false),
            xsdModel)
      })),
      intervalTransformation.transformExpression(context,
        new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
          new ConstraintImpl(x, greaterThan, new LiteralImpl(3), nonXSDModel),
          new ConstraintImpl(x, lessThan, new LiteralImpl(4), nonXSDModel),
          new ConstraintImpl(x, greaterThan, new LiteralImpl(2), xsdModel),
          new ConstraintImpl(x, lessThan, new LiteralImpl(3), xsdModel)
        }))
      )
    );
  }

  /**
   * Test #8 for the {@link IntervalTransformation#transform} method.
   *
   * This checks that constraints with no variables will not be transformed.
   */
  public void test8IgnoreNoVars() throws Exception {
    ConstraintImpl ci = new ConstraintImpl(new LiteralImpl(2), lessThan, new LiteralImpl(3), xsdModel);
    assertSame(ci, intervalTransformation.transformExpression(context, ci));
  }

  /**
   * Test #9 for the {@link IntervalTransformation#transform} method.
   *
   * This checks that constraints with variables in both subj and obj will not be transformed.
   */
  public void test9IgnoreAllVars() throws Exception {
    ConstraintImpl ci = new ConstraintImpl(new Variable("x"), lessThan, new Variable("y"), xsdModel);
    assertSame(ci, intervalTransformation.transformExpression(context, ci));
  }
}
