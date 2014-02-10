package org.jrdf.util.param;

import junit.framework.TestCase;

/**
 * Unit test for {@link EmtpyStringChecker}.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public final class EmtpyStringCheckerUnitTest extends TestCase {

  public void testParamAllowed() {
    //checkParam(ParameterTestUtil.NULL_STRING); // assume null's are not handled by this checker
    checkParam(ParameterTestUtil.EMPTY_STRING);
    checkParam(ParameterTestUtil.SINGLE_SPACE);
  }

  private void checkParam(String param) {
    assertFalse(new EmtpyStringChecker().paramAllowed(param));
  }
}
