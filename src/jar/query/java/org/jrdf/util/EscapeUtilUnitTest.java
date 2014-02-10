package org.jrdf.util;

import junit.framework.TestCase;

/**
 * Unit test for {@link EscapeUtil}.
 *
 * @author Andrew Newman
 */
public class EscapeUtilUnitTest extends TestCase {

    public void testSurrgates() {
        // ISO 10646 Amendment 1
        // High surrogates from D800...DBFF, low surrogates from DC00...DFFF.
        testEscapedValue("\\U00010000", "\uD800\uDC00");
        testEscapedValue("\\U00010001", "\uD800\uDC01");
        testEscapedValue("\\U00010401", "\uD801\uDC01");
        testEscapedValue("\\U00020000", "\uD840\uDC00");
        testEscapedValue("\\U00030000", "\uD880\uDC00");
        testEscapedValue("\\U00100000", "\uDBC0\uDC00");
        testEscapedValue("\\U0010FFFF", "\uDBFF\uDFFF");
    }

    public void testNearSurrogates() {
        // ISO 10646 Amendment 1
        // High surrogates from D800...DBFF, low surrogates from DC00...DFFF.
        testEscapedValue("\\uD799\\uDC00", "\uD799\uDC00");
        testEscapedValue("\\uD799\\uDC01", "\uD799\uDC01");
        testEscapedValue("\\uD800\\uDBFF", "\uD800\uDBFF");
        testEscapedValue("\\uD801\\uDBFF", "\uD801\uDBFF");
        testEscapedValue("\\uDC00\\uDFFF", "\uDC00\uDFFF");
        testEscapedValue("\\uDBFF\\uE000", "\uDBFF\uE000");
    }

    public void testExampleCodePoints() {
        testEscapedValue("\\U000233B4", "\uD84C\uDFB4");
        testEscapedValue("\\u2260", "\u2260");
        testEscapedValue("q", "\u0071");
        testEscapedValue("\\u030C", "\u030c");
        testEscapedValue("\\u00E9", "\u00E9");
    }

    public void testControlCharacters() {
        testEscapedValue("\\\\", new String(new char[] {(char) 92}));
        testEscapedValue("\\\"", new String(new char[] {(char) 34}));
        testEscapedValue("\\r", new String(new char[] {(char) 13}));
        testEscapedValue("\\n", new String(new char[] {(char) 10}));
        testEscapedValue("\\t", new String(new char[] {(char) 9}));
    }

    private void testEscapedValue(String expectedValue, String testString) {
        assertEquals(expectedValue, EscapeUtil.escape(testString));
    }
}
