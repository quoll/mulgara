/**
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */
package org.mulgara.query.filter;

import java.net.URI;

import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.SimpleLiteral;
import static org.mulgara.query.rdf.XSD.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests binary logic operations.
 *
 * @created Apr 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractLogicUnitTest extends TestCase {

  protected URI xsdInt = INT_URI;
  protected URI xsdBool = BOOLEAN_URI;

  public AbstractLogicUnitTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite();
  }

  protected void basicTest(AbstractFilterValue op, Bool result) throws Exception {
    op.setContextOwner(new TestContextOwner(new TestContext()));
    assertTrue(op.equals(result));
    assertFalse(op.isBlank());
    assertFalse(op.isIRI());
    assertTrue(op.isLiteral());
    assertFalse(op.isURI());
    assertTrue(result.getType().equals(op.getType()));
    assertEquals(SimpleLiteral.EMPTY, op.getLang());
  }
}