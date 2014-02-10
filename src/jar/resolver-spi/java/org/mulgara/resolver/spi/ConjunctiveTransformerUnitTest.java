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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.spi;

// Java 2 standard packages
import java.net.*;
import java.util.*;

// Third party packages
// import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;          // JUnit
import junit.framework.TestSuite;

// Local packages
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintDisjunction;
import org.mulgara.query.ConstraintOperation;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.util.NVPair;

public class ConjunctiveTransformerUnitTest extends TestCase {

  // /** Logger */
  // private static final Logger logger = Logger.getLogger(ConjunctiveTransformerUnitTest.class.getName());

  private static final URI matchModelType;
  private static final URI nomatchModelType;
  private static final URI matchModel1;
  private static final URI matchModel2;
  private static final URI nomatchModel;

  static {
    try {
      matchModelType = new URI("test:modeltype:match");
      nomatchModelType = new URI("test:modeltype:nomatch");
      matchModel1 = new URI("test:model:model1");
      matchModel2 = new URI("test:model:model2");
      nomatchModel = new URI("test:model:model3");
    } catch (URISyntaxException eu) {
      throw new IllegalStateException("Invalid uri initialiser");
    }
  }

  

  private static final Variable v1;
  private static final Variable v2;
  private static final URIReferenceImpl u1;
  private static final URIReferenceImpl u2;
  private static final URIReferenceImpl u3;
  private static final URIReferenceImpl u4;
  private static final URIReferenceImpl u5;
  private static final URIReferenceImpl u6;
  private static final URIReferenceImpl u7;
  private static final URIReferenceImpl u8;
  private static final URIReferenceImpl mm1;
  private static final URIReferenceImpl mm2;
  private static final URIReferenceImpl nm1;

  static {
    try {
      v1 = new Variable("v1");
      v2 = new Variable("v2");
      
      u1 = new URIReferenceImpl(new URI("test:uri:1"));
      u2 = new URIReferenceImpl(new URI("test:uri:2"));
      u3 = new URIReferenceImpl(new URI("test:uri:3"));
      u4 = new URIReferenceImpl(new URI("test:uri:4"));
      u5 = new URIReferenceImpl(new URI("test:uri:5"));
      u6 = new URIReferenceImpl(new URI("test:uri:6"));
      u7 = new URIReferenceImpl(new URI("test:uri:7"));
      u8 = new URIReferenceImpl(new URI("test:uri:8"));

      mm1 = new URIReferenceImpl(matchModel1);
      mm2 = new URIReferenceImpl(matchModel2);
      nm1 = new URIReferenceImpl(nomatchModel);
    } catch (URISyntaxException eu) {
      throw new IllegalStateException("Invalid uri initialiser");
    }
  }

  private static final ConjunctiveTransformer transformer = 
      new TestConjunctiveTransformer(matchModelType);

  @SuppressWarnings("unchecked")
  private static final SymbolicTransformationContext context = 
      new TestSymbolicTransformationContext(NVPair.nvPairsToMap(new NVPair[] {
          new NVPair<URI,URI>(matchModel1, matchModelType),
          new NVPair<URI,URI>(matchModel2, matchModelType),
          new NVPair<URI,URI>(nomatchModel, nomatchModelType),
      }));


  public ConjunctiveTransformerUnitTest(String name) {
    super(name);
  }

  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(new ConjunctiveTransformerUnitTest("testStraightCImpl"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testSingleMatch"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testStraightConj"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testPartialConj"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testFullConj"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testExtendedConj"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testNestedConj"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testStraightDisj"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testPartialDisj"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testFullDisj"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testPartialProdSum"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testFullSumProd"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testMultipleVariables"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testMultVarPlusConst"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testMultVarMultPred"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testMultVarMultPredMultModel"));
    suite.addTest(new ConjunctiveTransformerUnitTest("testMultVarMultPredMultModelPlusConst"));

    return suite;
  }

  public void setUp() throws Exception { }

  //
  // Test cases
  //

  public void testStraightCImpl() throws Exception {
    ConstraintExpression initial = new ConstraintImpl(v1, u1, u2, nm1);
    ConstraintExpression expected = initial;

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testSingleMatch() throws Exception {
    ConstraintExpression initial = new ConstraintImpl(v1, u1, u2, mm1);
    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2]}}|C{}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testStraightConj() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, nm1),
        new ConstraintImpl(v1, u1, u2, nm1),
      }));
    ConstraintExpression expected = initial;

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testPartialConj() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, nm1),
        new ConstraintImpl(v1, u1, u2, mm1),
      }));
    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, nm1),
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2]}}|C{}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testFullConj() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, mm1),
        new ConstraintImpl(v1, u1, u3, mm1),
      }));
    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2, test:uri:3]}}|C{}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testExtendedConj() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, mm1),
        new ConstraintImpl(v1, u1, u3, mm1),
        new ConstraintImpl(v1, u1, u2, nm1),
      }));
    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, nm1),
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2, test:uri:3]}}|C{}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testNestedConj() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, mm1),
        new ConstraintConjunction(
          new ConstraintImpl(v1, u1, u3, mm1),
          new ConstraintImpl(v1, u1, u2, nm1)),
      }));
    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, nm1),
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2, test:uri:3]}}|C{}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testStraightDisj() throws Exception {
    ConstraintExpression initial = new ConstraintDisjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, nm1),
        new ConstraintImpl(v1, u1, u2, nm1),
      }));
    ConstraintExpression expected = initial;

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testPartialDisj() throws Exception {
    ConstraintExpression initial = new ConstraintDisjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, nm1),
        new ConstraintImpl(v1, u1, u2, mm1),
      }));
    ConstraintExpression expected = new ConstraintDisjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, nm1),
        new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
          new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2]}}|C{}"),
        })),
      }));
    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testFullDisj() throws Exception {
    ConstraintExpression initial = new ConstraintDisjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, mm1),
        new ConstraintImpl(v1, u1, u3, mm1),
      }));
    ConstraintExpression expected = new ConstraintDisjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
          new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2]}}|C{}"),
        })),
        new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
          new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:3]}}|C{}"),
        })),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testPartialProdSum() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintDisjunction(Arrays.asList(new ConstraintExpression[] {
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
            new ConstraintImpl(v1, u1, u2, mm1),
            new ConstraintImpl(v1, u1, u2, nm1),
          })),
          new ConstraintImpl(v1, u1, u3, mm1),
        })),
        new ConstraintImpl(v1, u1, u3, mm1),
        new ConstraintImpl(v1, u1, u4, mm1),
      }));

    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintDisjunction(Arrays.asList(new ConstraintExpression[] {
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
            new ConstraintImpl(v1, u1, u2, nm1),
            new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2]}}|C{}"),
          })),
          new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
            new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:3]}}|C{}"),
          })),
        })),
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:3, test:uri:4]}}|C{}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testFullSumProd() throws Exception {
    ConstraintExpression initial = new ConstraintDisjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
          new ConstraintImpl(v1, u2, u1, mm1),
          new ConstraintImpl(v1, u2, u3, mm1),
          new ConstraintImpl(v1, u2, u4, nm1),
        })),
        new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
          new ConstraintImpl(v1, u1, u2, mm1),
          new ConstraintImpl(v1, u1, u3, mm1),
          new ConstraintImpl(v1, u1, u4, nm1),
        })),
      }));

    ConstraintExpression expected = new ConstraintDisjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
          new ConstraintImpl(v1, u2, u4, nm1),
          new TestConstraint("test:model:model1|V{$v1={test:uri:2=[test:uri:1, test:uri:3]}}|C{}"),
        })),
        new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
          new ConstraintImpl(v1, u1, u4, nm1),
          new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2, test:uri:3]}}|C{}"),
        })),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testMultipleVariables() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, mm1),
        new ConstraintImpl(v1, u1, u3, mm1),
        new ConstraintImpl(v2, u4, u5, mm1),
        new ConstraintImpl(v2, u4, u6, mm1),
      }));
    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2, test:uri:3]}, $v2={test:uri:4=[test:uri:5, test:uri:6]}}|C{}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testMultVarPlusConst() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u2, mm1),
        new ConstraintImpl(v1, u1, u3, mm1),
        new ConstraintImpl(v2, u4, u5, mm1),
        new ConstraintImpl(v2, u4, u6, mm1),
        new ConstraintImpl(u7, u2, u1, mm1),
        new ConstraintImpl(u8, u3, u4, mm1),
        new ConstraintImpl(u8, u3, u5, mm1),
      }));
    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:2, test:uri:3]}, $v2={test:uri:4=[test:uri:5, test:uri:6]}}|C{test:uri:7={test:uri:2=[test:uri:1]}, test:uri:8={test:uri:3=[test:uri:4, test:uri:5]}}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testMultVarMultPred() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u1, mm1),
        new ConstraintImpl(v1, u1, u2, mm1),
        new ConstraintImpl(v1, u2, u3, mm1),
        new ConstraintImpl(v1, u2, u4, mm1),
        new ConstraintImpl(v2, u3, u5, mm1),
        new ConstraintImpl(v2, u3, u6, mm1),
        new ConstraintImpl(v2, u4, u7, mm1),
        new ConstraintImpl(v2, u4, u8, mm1),
      }));
    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:1, test:uri:2], test:uri:2=[test:uri:3, test:uri:4]}, $v2={test:uri:3=[test:uri:5, test:uri:6], test:uri:4=[test:uri:7, test:uri:8]}}|C{}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testMultVarMultPredMultModel() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u1, mm1),
        new ConstraintImpl(v1, u1, u2, mm1),
        new ConstraintImpl(v1, u2, u3, mm1),
        new ConstraintImpl(v1, u2, u4, mm1),
        new ConstraintImpl(v2, u3, u5, mm1),
        new ConstraintImpl(v2, u3, u6, mm1),
        new ConstraintImpl(v2, u4, u7, mm1),
        new ConstraintImpl(v2, u4, u8, mm1),
        new ConstraintImpl(v1, u5, u1, mm2),
        new ConstraintImpl(v1, u5, u2, mm2),
        new ConstraintImpl(v1, u6, u3, mm2),
        new ConstraintImpl(v1, u6, u4, mm2),
        new ConstraintImpl(v2, u7, u5, mm2),
        new ConstraintImpl(v2, u7, u6, mm2),
        new ConstraintImpl(v2, u8, u7, mm2),
        new ConstraintImpl(v2, u8, u8, mm2),
      }));
    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:1, test:uri:2], test:uri:2=[test:uri:3, test:uri:4]}, $v2={test:uri:3=[test:uri:5, test:uri:6], test:uri:4=[test:uri:7, test:uri:8]}}|C{}"),
        new TestConstraint("test:model:model2|V{$v1={test:uri:5=[test:uri:1, test:uri:2], test:uri:6=[test:uri:3, test:uri:4]}, $v2={test:uri:7=[test:uri:5, test:uri:6], test:uri:8=[test:uri:7, test:uri:8]}}|C{}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  public void testMultVarMultPredMultModelPlusConst() throws Exception {
    ConstraintExpression initial = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new ConstraintImpl(v1, u1, u1, mm1),
        new ConstraintImpl(v1, u1, u2, mm1),
        new ConstraintImpl(v1, u2, u3, mm1),
        new ConstraintImpl(v1, u2, u4, mm1),
        new ConstraintImpl(v2, u3, u5, mm1),
        new ConstraintImpl(v2, u3, u6, mm1),
        new ConstraintImpl(v2, u4, u7, mm1),
        new ConstraintImpl(v2, u4, u8, mm1),
        new ConstraintImpl(u1, u3, u5, mm1),
        new ConstraintImpl(v1, u5, u1, mm2),
        new ConstraintImpl(v1, u5, u2, mm2),
        new ConstraintImpl(v1, u6, u3, mm2),
        new ConstraintImpl(v1, u6, u4, mm2),
        new ConstraintImpl(v2, u7, u5, mm2),
        new ConstraintImpl(v2, u7, u6, mm2),
        new ConstraintImpl(v2, u8, u7, mm2),
        new ConstraintImpl(v2, u8, u8, mm2),
        new ConstraintImpl(u2, u4, u6, mm2),
      }));
    ConstraintExpression expected = new ConstraintConjunction(Arrays.asList(new ConstraintExpression[] {
        new TestConstraint("test:model:model1|V{$v1={test:uri:1=[test:uri:1, test:uri:2], test:uri:2=[test:uri:3, test:uri:4]}, $v2={test:uri:3=[test:uri:5, test:uri:6], test:uri:4=[test:uri:7, test:uri:8]}}|C{test:uri:1={test:uri:3=[test:uri:5]}}"),
        new TestConstraint("test:model:model2|V{$v1={test:uri:5=[test:uri:1, test:uri:2], test:uri:6=[test:uri:3, test:uri:4]}, $v2={test:uri:7=[test:uri:5, test:uri:6], test:uri:8=[test:uri:7, test:uri:8]}}|C{test:uri:2={test:uri:4=[test:uri:6]}}"),
      }));

    TestMutableLocalQuery lc = new TestMutableLocalQuery(initial);
    transformer.transform(context, lc);

    compareResult(expected, lc.getConstraintExpression());
  }


  private void compareResult(ConstraintExpression expected, ConstraintExpression result)
      throws Exception {

    assertEquals("Classes match: " + expected + " - " + result, expected.getClass(), result.getClass());
    if (result instanceof ConstraintImpl) {
      assertEquals(expected, result);
    } else if (result instanceof ConstraintOperation) {
      Collection<ConstraintExpression> ce = ((ConstraintOperation)expected).getElements();
      Collection<ConstraintExpression> cr = ((ConstraintOperation)result).getElements();
      assertEquals("Operation cardinality: " + result, ce.size(), cr.size());
      Iterator<ConstraintExpression> ie = ce.iterator();
      Iterator<ConstraintExpression> ir = cr.iterator();
      while (ie.hasNext()) {
        compareResult(ie.next(), ir.next());
      }
    } else if (result instanceof TestConstraint) {
      assertEquals(expected, result);
    } else {
      // Failure.
      assertTrue("Expected: " + expected + " :: " + expected.getClass() + " , result: " + result + " :: " + result.getClass(), false);
    }
  }


  static class TestSymbolicTransformationContext implements SymbolicTransformationContext {
    private Map<URI,URI> mappings;

    public TestSymbolicTransformationContext(Map<URI,URI> mappings) {
      this.mappings = mappings;
    }

    public URI mapToModelTypeURI(URI modelURI) throws QueryException {
      URI typeURI = (URI)mappings.get(modelURI);
      if (typeURI == null) {
        throw new QueryException("modelType not found for model: " + modelURI);
      }
      return typeURI;
    }
  }

  static class TestConjunctiveTransformer extends ConjunctiveTransformer {
    public TestConjunctiveTransformer(URI modelTypeURI) {
      super(modelTypeURI);
    }

    public ConstraintExpression constructConstraintExpression(ConstraintElement model,
        Map<ConstraintElement,Map<ConstraintElement,List<ConstraintElement>>> byVarSubject,
        Map<ConstraintElement,Map<ConstraintElement,List<ConstraintElement>>> byConstSubject) throws SymbolicTransformationException {

      String id = model.toString() +
          "|V" + sortedMap(byVarSubject) +
          "|C" + sortedMap(byConstSubject);
      return new TestConstraint(id);
    }

    String sortedMap(Map<ConstraintElement,?> map) {
      if (map == null) return "{}";
      return createSortedMap(map).toString();
    }
    
    @SuppressWarnings("unchecked")
    SortedMap<ConstraintElement,?> createSortedMap(Map<ConstraintElement,?> m) {
      SortedMap<ConstraintElement,Object> sm = new TreeMap<ConstraintElement,Object>(m);
      for (Map.Entry<ConstraintElement,Object> entry: sm.entrySet()) {
        Object v = entry.getValue();
        if (v instanceof Map) {
          entry.setValue(createSortedMap((Map<ConstraintElement,?>)v));
        }
      }
      return sm;
    }
  }

  static class TestMutableLocalQuery implements MutableLocalQuery {
    ConstraintExpression expr;
    boolean modified;

    public TestMutableLocalQuery(ConstraintExpression expr) {
      this.expr = expr;
      this.modified = false;
    }

    public ConstraintExpression getConstraintExpression() {
      return expr;
    }

    public boolean isModified() {
      return modified;
    }

    public void setConstraintExpression(ConstraintExpression newExpr) {
      if (newExpr != expr) {
        expr = newExpr;
        modified = true;
      }
    }
  }

  @SuppressWarnings("serial")
  static class TestConstraint implements ConstraintExpression {
    private String id;

    TestConstraint(String id) {
      this.id = id;
    }

    public Set<Variable> getVariables() {
      return null;
    }

    public boolean equals(Object o) {
      if (!getClass().equals(o.getClass())) {
        return false;
      }
      boolean result = id.equals(((TestConstraint)o).id);
      return result;
    }

    public String toString() {
      return "[TC:" + id + "]";
    }

    public boolean isAssociative() {
      return false;
    }
  }
}
