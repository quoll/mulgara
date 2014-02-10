/*
 * Copyright 2009 DuraSpace.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.query.xpath;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathFunctionException;

import org.apache.xerces.impl.xpath.regex.RegularExpression;
import org.mulgara.query.functions.MulgaraFunction;
import org.mulgara.query.functions.MulgaraFunctionGroup;
import org.mulgara.util.NumberUtil;

/**
 * Container for functions in the fn domain.
 *
 * @created Oct 5, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class FnFunctionGroup implements MulgaraFunctionGroup {

  /** The prefix for the fn: namespace */
  static final String PREFIX = "fn";

  /** The fn: namespace */
  static final String NAMESPACE = "http://www.w3.org/2005/xpath-functions/#";

  /** The name of the UTF-8 encoding scheme */
  static private final String UTF8 = "UTF-8";

  /**
   * Get the prefix used for the namespace of these operations.
   * @return The short string used for a prefix in a QName.
   */
  public String getPrefix() {
    return PREFIX;
  }

  /**
   * Get the namespace of these operations.
   * @return The string of the namespace URI.
   */
  public String getNamespace() {
    return NAMESPACE;
  }

  /**
   * Get the set of SPARQL functions.
   * @return A set of MulgaraFunction for this entire group.
   */
  public Set<MulgaraFunction> getAllFunctions() {
    Set<MulgaraFunction> functions = new HashSet<MulgaraFunction>();
    functions.add(new Matches2());
    functions.add(new Matches3());
    functions.add(new FnBoolean());
    functions.add(new Not());
    functions.add(new Concat());
    functions.add(new Substring2());
    functions.add(new Substring3());
    functions.add(new StringLength());
    functions.add(new NormalizeSpace());
    functions.add(new UpperCase());
    functions.add(new LowerCase());
    functions.add(new Translate());
    functions.add(new EncodeForUri());
    functions.add(new IriToUri());
    functions.add(new EscapeHtmlUri());
    functions.add(new Contains());
    functions.add(new StartsWith());
    functions.add(new EndsWith());
    functions.add(new StringJoin());
    functions.add(new Round());
    functions.add(new Abs());
    functions.add(new Floor());
    functions.add(new Ceiling());
    return functions;
  }

  /**
   * Function to evaluate if a string matches a pattern.
   * @see http://www.w3.org/TR/xpath-functions/#func-matches
   */
  static private class Matches2 extends MulgaraFunction {
    public String getName() { return "matches/2"; }
    public int getArity() { return 2; }
    public Object eval(List<?> args) {
      String str = (String)args.get(0);
      String pattern = (String)args.get(1);
      return new RegularExpression(pattern).matches(str);
    }
  }

  /**
   * Function to evaluate if a string matches a pattern, with a set of modifying flags.
   * @see http://www.w3.org/TR/xpath-functions/#func-matches
   */
  static private class Matches3 extends MulgaraFunction {
    public String getName() { return "matches/3"; }
    public int getArity() { return 3; }
    public Object eval(List<?> args) {
      String str = (String)args.get(0);
      String pattern = (String)args.get(1);
      String flags = (String)args.get(2);
      return new RegularExpression(pattern, flags).matches(str);
    }
  }

  /**
   * Function to compute the EBV of a sequence. Unfortunately, no sequence info is available at this level.
   * @see http://www.w3.org/TR/xpath-functions/#func-boolean
   */
  static private class FnBoolean extends MulgaraFunction {
    public String getName() { return "boolean"; }
    public int getArity() { return 1; }
    public Object eval(List<?> args) throws XPathFunctionException {
      // no sequence info available here. Look at singleton only for the moment
      return toBool(args.get(0));
    }
  }

  // No implementation of fn:compare

  /**
   * Function to compute the inverse EBV of a sequence. See FnBoolean for restrictions.
   * @see http://www.w3.org/TR/xpath-functions/#func-not
   */
  static private class Not extends MulgaraFunction {
    public int getArity() { return 1; }
    public Object eval(List<?> args) throws XPathFunctionException {
      return !toBool(args.get(0));
    }
  }

  /**
   * Concatenates two or more arguments cast to string.
   * @see http://www.w3.org/TR/xpath-functions/#func-concat
   */
  static private class Concat extends MulgaraFunction {
    public int getArity() { return -1; }
    public String getName() { return "concat/*"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      StringBuilder s = new StringBuilder();
      for (Object o: args) s.append(o);
      return s.toString();
    }
  }

  /**
   * Returns the string located at a specified place within an argument string.
   * @see http://www.w3.org/TR/xpath-functions/#func-substring
   */
  static private class Substring2 extends MulgaraFunction {
    public int getArity() { return 2; }
    public String getName() { return "substring/2"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      String source = args.get(0).toString();
      int start = ((Number)args.get(1)).intValue() - 1;
      // perform boundary checking
      int len = source.length();
      if (start < 0) start = 0;
      if (start > len) start = len;
      return source.substring(start);
    }
  }

  /**
   * Returns the string located at a specified place within an argument string.
   * @see http://www.w3.org/TR/xpath-functions/#func-substring
   */
  static private class Substring3 extends MulgaraFunction {
    public int getArity() { return 3; }
    public String getName() { return "substring/3"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      String source = args.get(0).toString();
      int start = ((Number)args.get(1)).intValue() - 1;
      int end = ((Number)args.get(2)).intValue() + start;
      // perform boundary checking
      int len = source.length();
      if (start < 0) start = 0;
      if (start > len) {
        start = len;
        end = len;
      }
      if (end > len) end = len;
      if (end < start) end = start;
      
      return source.substring(start, end);
    }
  }

  /**
   * Returns the length of the argument.
   * @see http://www.w3.org/TR/xpath-functions/#func-string-length
   */
  static private class StringLength extends MulgaraFunction {
    public String getName() { return "string-length/1"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      return args.get(0).toString().length();
    }
  }

  /**
   * Returns the whitespace-normalized value of the argument.
   * @see http://www.w3.org/TR/xpath-functions/#func-normalize-space
   */
  static private class NormalizeSpace extends MulgaraFunction {
    public String getName() { return "normalize-space/1"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      String str = args.get(0).toString().trim();
      return str.replaceAll(" +", " ");
    }
  }

  /**
   * Returns the upper-cased value of the argument.
   * @see http://www.w3.org/TR/xpath-functions/#func-upper-case
   */
  static private class UpperCase extends MulgaraFunction {
    public String getName() { return "upper-case/1"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      return args.get(0).toString().toUpperCase();
    }
  }

  /**
   * Returns the lower-cased value of the argument.
   * @see http://www.w3.org/TR/xpath-functions/#func-lower-case
   */
  static private class LowerCase extends MulgaraFunction {
    public String getName() { return "lower-case/1"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      return args.get(0).toString().toLowerCase();
    }
  }

  /**
   * Returns the first xs:string argument with occurrences of characters contained
   * in the second argument replaced by the character at the corresponding position
   * in the third argument.
   */
  static private class Translate extends MulgaraFunction {
    public int getArity() { return 3; }
    public Object eval(List<?> args) throws XPathFunctionException {
      String str = args.get(0).toString();
      String mapStr = args.get(1).toString();
      String transStr = args.get(2).toString();
      // iterate through the map chars
      for (int i = 0; i < mapStr.length(); i++) {
        char c = mapStr.charAt(i);
        if (i < transStr.length()) str = replaceChars(str, c, transStr.charAt(i));
        else str = removeChars(str, c);
      }
      return str;
    }

    private static String replaceChars(String str, char c, char r) {
      StringBuilder s = new StringBuilder(str);
      for (int i = 0; i < s.length(); i++) {
        if (s.charAt(i) == c) s.setCharAt(i, r);
      }
      return s.toString();
    }

    private static String removeChars(String str, char c) {
      StringBuilder s = new StringBuilder(str);
      for (int i = 0; i < s.length(); i++) {
        if (s.charAt(i) == c) {
          s.replace(i, i + 1, "");
          i--;
        }
      }
      return s.toString();
    }
  }

  /**
   * Returns the xs:string argument with certain characters escaped to enable the
   * resulting string to be used as a path segment in a URI.
   * @see http://www.w3.org/TR/xpath-functions/#func-encode-for-uri
   */
  static private class EncodeForUri extends MulgaraFunction {
    public String getName() { return "encode-for-uri/1"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      try {
        return URLEncoder.encode(args.get(0).toString(), UTF8);
      } catch (UnsupportedEncodingException e) {
        throw new XPathFunctionException("Unable to encode string for URL: " + e.getMessage());
      }
    }
  }

  /**
   * Returns the xs:string argument with certain characters escaped to enable the
   * resulting string to be used as (part of) a URI.
   * @see http://www.w3.org/TR/xpath-functions/#func-iri-to-uri
   */
  static private class IriToUri extends MulgaraFunction {
    public String getName() { return "iri-to-uri/1"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      StringBuilder str = new StringBuilder(args.get(0).toString());
      for (int i = 0; i < str.length(); i++) {
        char c = str.charAt(i);
        if (outOfRange(c)) str.replace(i, i + 1, escape(str.substring(i, i + 1)));
      }
      return str.toString();
    }
    /**
     * Test for URI compatibility. See Errata note:
     * @see http://www.w3.org/XML/2007/qt-errata/xpath-functions-errata.html#E8
     * @param c The character to test.
     * @return <code>true</code> if the character is out of range for a URI and must be encoded.
     */
    static private final boolean outOfRange(char c) {
      return c < 0x20 || c > 0x7E || c == '<' || c == '>' || c == '"' ||
             c == '{' || c == '}' || c == '|' || c == '\\' || c == '^' || c == '`';
    }
  }

  /**
   * Returns the string argument with certain characters escaped in the manner that html user agents
   * handle attribute values that expect URIs.
   * @see http://www.w3.org/TR/xpath-functions/#func-escape-html-uri
   */
  static private class EscapeHtmlUri extends MulgaraFunction {
    public String getName() { return "escape-html-uri/1"; }
    public Object eval(List<?> args) throws XPathFunctionException {
      StringBuilder str = new StringBuilder(args.get(0).toString());
      for (int i = 0; i < str.length(); i++) {
        char c = str.charAt(i);
        if (outOfRange(c)) str.replace(i, i + 1, escape(str.substring(i, i + 1)));
      }
      return str.toString();
    }
    static private final boolean outOfRange(char c) {
      return c < 0x20 || c > 0x7E;
    }
  }

  /**
   * Converts a single character to a string containing escaped UTF-8 encoding.
   * A utility used by both EscapeHtmlUri and IriToUri.
   * @param c The character to escape.
   * @return A string containing a sequence of %HH representing the UTF-8 encoding of <var>c</var>
   * @throws XPathFunctionException If UTF-8 encoding fails.
   */
  static private final String escape(String c) throws XPathFunctionException {
    byte[] bytes = null;
    try {
      bytes = c.getBytes(UTF8);
    } catch (UnsupportedEncodingException e) {
      throw new XPathFunctionException("Unable to encode string for URL: " + e.getMessage());
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < bytes.length; i++) result.append(String.format("%%%02X", bytes[i]));
    return result.toString();
  }

  /**
   * Test whether a substring occurs in a string
   * fn:contains(string,substr)
   * @see http://www.w3.org/TR/xpath-functions/#contains
   */
  static private class Contains extends MulgaraFunction {
    public int getArity() { return 2; }
    public Object eval(List<?> args) {
      String str = (String)args.get(0);
      String substr = (String)args.get(1);
      return str.contains(substr);
    }
  }

  /**
   * Test whether a string starts with substr
   * fn:starts-with(string,substr)
   * @see http://www.w3.org/TR/xpath-functions/#starts-with
   */
  static private class StartsWith extends MulgaraFunction {
    public String getName() { return "starts-with/2"; }
    public int getArity() { return 2; }
    public Object eval(List<?> args) {
      String str = (String)args.get(0);
      String substr = (String)args.get(1);
      return str.startsWith(substr);
    }
  }

  /**
   * Test whether a string ends with substr
   * fn:ends-with(string,substr)
   * @see http://www.w3.org/TR/xpath-functions/#ends-with
   */
  static private class EndsWith extends MulgaraFunction {
    public String getName() { return "ends-with/2"; }
    public int getArity() { return 2; }
    public Object eval(List<?> args) {
      String str = (String)args.get(0);
      String substr = (String)args.get(1);
      return str.endsWith(substr);
    }
  }

  /**
   * Join all the arguments except the last, using the last argument as a separator.
   * fn:string-join(sequence..., separator)
   * @see http://www.w3.org/TR/xpath-functions/#string-join
   */
  static private class StringJoin extends MulgaraFunction {
    public String getName() { return "string-join/*"; }
    public int getArity() { return -1; }
    public Object eval(List<?> args) throws XPathFunctionException {
      StringBuilder s = new StringBuilder();
      int lastIndex = args.size() - 1;
      String separator = (String)args.get(lastIndex);
      for (int i = 0; i < lastIndex; i++) {
        if (i != 0) s.append(separator);
        s.append(args.get(i));
      }
      return s.toString();
    }
  }

  /**
   * Return the nearest integer value to the argument.
   * fn:round(x)
   * @see http://www.w3.org/TR/xpath-functions/#round
   */
  static private class Round extends MulgaraFunction {
    public Object eval(List<?> args) throws XPathFunctionException {
      Number x = (Number)args.get(0);
      return Math.round(x.doubleValue());
    }
  }

  /**
   * Return the absolute value.
   * fn:abs(x)
   * @see http://www.w3.org/TR/xpath-functions/#abs
   */
  static private class Abs extends MulgaraFunction {
    public Object eval(List<?> args) throws XPathFunctionException {
      Number x = (Number)args.get(0);
      return x.doubleValue() < 0 ? NumberUtil.minus(x) : x;
    }
  }

  /**
   * Return the greatest integer value less than the argument (as a double).
   * fn:floor(x)
   * @see http://www.w3.org/TR/xpath-functions/#floor
   */
  static private class Floor extends MulgaraFunction {
    public Object eval(List<?> args) throws XPathFunctionException {
      Number x = (Number)args.get(0);
      return Math.floor(x.doubleValue());
    }
  }

  /**
   * Return the smallest integer value greater than the argument (as a double).
   * fn:ceiling(x)
   * @see http://www.w3.org/TR/xpath-functions/#ceiling
   */
  static private class Ceiling extends MulgaraFunction {
    public Object eval(List<?> args) throws XPathFunctionException {
      Number x = (Number)args.get(0);
      return Math.ceil(x.doubleValue());
    }
  }
}
