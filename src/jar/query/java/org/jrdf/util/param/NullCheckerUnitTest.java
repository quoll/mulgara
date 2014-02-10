package org.jrdf.util.param;

import junit.framework.TestCase;

/**
 * Unit test for {@link NullChecker}.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public final class NullCheckerUnitTest extends TestCase {

  public void testParamAllowed() {
    assertFalse(new NullChecker().paramAllowed(ParameterTestUtil.NULL_STRING));
    assertTrue(new NullChecker().paramAllowed(ParameterTestUtil.SINGLE_SPACE));
  }
}
