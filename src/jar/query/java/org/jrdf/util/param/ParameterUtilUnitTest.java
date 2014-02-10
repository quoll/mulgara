package org.jrdf.util.param;

import junit.framework.TestCase;

/**
 * Unit test for {@link ParameterUtil}.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public final class ParameterUtilUnitTest extends TestCase {
    private static final String NULL = ParameterTestUtil.NULL_STRING;
    private static final String EMPTY_STRING = ParameterTestUtil.EMPTY_STRING;
    private static final String SINGLE_SPACE = ParameterTestUtil.SINGLE_SPACE;
    private static final String NON_EMPTY_STRING = ParameterTestUtil.NON_EMPTY_STRING;
    private static final Object NON_NULL_OBJECT = ParameterTestUtil.NON_NULL_OBJECT;
    private static final String DUMMY_PARAM_NAME = "foo";

    public void testNoNullsAllowed() {
        try {
            ParameterUtil.checkNotNull(DUMMY_PARAM_NAME, NULL);
            fail("Nulls should not be allowed");
        } catch (IllegalArgumentException expected) { }
    }

    public void testEmptyStringNotAllowed() {
        checkStringNotAllowed(NULL);
        checkStringNotAllowed(EMPTY_STRING);
        checkStringNotAllowed(SINGLE_SPACE);
    }

    public void testNonEmptyStringAllowed() {
        ParameterUtil.checkNotEmptyString(DUMMY_PARAM_NAME, NON_EMPTY_STRING);
    }

    public void testNonNullObjectAllowed() {
        ParameterUtil.checkNotNull(DUMMY_PARAM_NAME, NON_NULL_OBJECT);
    }

    private void checkStringNotAllowed(String param) {
        try {
            ParameterUtil.checkNotEmptyString(DUMMY_PARAM_NAME, param);
            fail("Empty strings should not be allowed");
        } catch (IllegalArgumentException expected) { }
    }
}
