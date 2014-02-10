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
 * The Initial Developer of the Original Code is Edwin Shin.
 * Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.store.stringpool.xa;

// JUnit
import junit.framework.*;

// Internal Packages
import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.SPTypedLiteral;

/**
 * Unit test for SPObjectFactory.
 * TODO: Address all the public methods. This is only testing the recently modified methods.
 *
 * @created Jul 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SPObjectFactoryUnitTest extends TestCase {

  static final String INT_STR = "123";
  static final String FLOAT_STR = "123.4";
  static final String BOOL_STR = "true";
  static final String DATE_STR = "1971-12-20";

  SPObjectFactoryImpl factory;

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public SPObjectFactoryUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new SPObjectFactoryUnitTest("testValid"));
    suite.addTest(new SPObjectFactoryUnitTest("testInvalid"));
    return suite;
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   * @throws Exception
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Tests that valid xsd:decimal values are accepted and processed correctly.
   */
  public void testValid() throws Exception {
    SPDecimalImpl spi = (SPDecimalImpl)factory.newSPTypedLiteral(INT_STR, XSD.INTEGER_URI);
    SPFloatImpl spf = (SPFloatImpl)factory.newSPTypedLiteral(FLOAT_STR, XSD.FLOAT_URI);
    SPBooleanImpl sps = (SPBooleanImpl)factory.newSPTypedLiteral(BOOL_STR, XSD.BOOLEAN_URI);
    SPDateImpl spd = (SPDateImpl)factory.newSPTypedLiteral(DATE_STR, XSD.DATE_URI);

    assertEquals(INT_STR, spi.getLexicalForm());
    assertEquals(FLOAT_STR, spf.getLexicalForm());
    assertEquals(BOOL_STR, sps.getLexicalForm());
    assertEquals(DATE_STR, spd.getLexicalForm());
  }

  /**
   * Tests invalid xsd:decimal values.
   */
  public void testInvalid() throws Exception {
    SPTypedLiteral[] spi = new SPTypedLiteral[4];
    spi[0] = null;
    spi[1] = factory.newSPTypedLiteral(FLOAT_STR, XSD.INTEGER_URI);
    spi[2] = factory.newSPTypedLiteral(BOOL_STR, XSD.INTEGER_URI);
    spi[3] = factory.newSPTypedLiteral(DATE_STR, XSD.INTEGER_URI);

    SPTypedLiteral[] spf = new SPTypedLiteral[4];
    spf[0] = null;
    spf[1] = null;
    spf[2] = factory.newSPTypedLiteral(BOOL_STR, XSD.FLOAT_URI);
    spf[3] = factory.newSPTypedLiteral(DATE_STR, XSD.FLOAT_URI);

    SPTypedLiteral[] spb = new SPTypedLiteral[4];
    spb[0] = factory.newSPTypedLiteral(INT_STR, XSD.BOOLEAN_URI);
    spb[1] = factory.newSPTypedLiteral(FLOAT_STR, XSD.BOOLEAN_URI);
    spb[2] = null;
    spb[3] = factory.newSPTypedLiteral(DATE_STR, XSD.BOOLEAN_URI);

    SPTypedLiteral[] spd = new SPTypedLiteral[4];
    spd[0] = factory.newSPTypedLiteral(INT_STR, XSD.DATE_URI);
    spd[1] = factory.newSPTypedLiteral(FLOAT_STR, XSD.DATE_URI);
    spd[2] = factory.newSPTypedLiteral(BOOL_STR, XSD.DATE_URI);
    spd[3] = null;
    
    for (int i = 0; i < 4; i++) {
      if (spi[i] != null) {
        assertFalse("Wrong int type for i=" + i, spi[i] instanceof SPDecimalImpl);
        assertEquals(XSD.INTEGER_URI, spi[i].getTypeURI());
      }
      if (spf[i] != null) {
        assertFalse("Wrong float type for i=" + i, spf[i] instanceof SPFloatImpl);
        assertEquals(XSD.FLOAT_URI, spf[i].getTypeURI());
      }
      if (spb[i] != null) {
        assertFalse("Wrong boolean type for i=" + i, spb[i] instanceof SPBooleanImpl);
        assertEquals(XSD.BOOLEAN_URI, spb[i].getTypeURI());
      }
      if (spd[i] != null) {
        assertFalse("Wrong date type for i=" + i, spd[i] instanceof SPDateImpl);
        assertEquals(XSD.DATE_URI, spd[i].getTypeURI());
      }

    }
  }

  /**
   * Initialise members.
   *
   * @throws Exception if something goes wrong
   */
  public void setUp() throws Exception {
    super.setUp();
    factory = new SPObjectFactoryImpl();
  }

  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void tearDown() throws Exception {
    super.tearDown();
  }
}
