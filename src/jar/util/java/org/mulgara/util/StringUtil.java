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

package org.mulgara.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Reader;
import java.util.*;

/**
 * Purpose: A utility class for Strings. <P>
 *
 * Creation Date: 16 January 2001 <P>
 *
 * Original Author: Ben Warren (ben.warren@pisoftware.com) Original Author:
 * David Makepeace (david@pisoftware.com) <P>
 *
 * Last-modified Date: $Date: 2005/01/05 04:59:29 $ <P>
 *
 * Maintenance Author: $Author: newmana $ <P>
 *
 * Company: Plugged In Software <info@pisoftware.com> <P>
 *
 * Copyright &copy; 2000 Plugged In Software Pty Ltd
 * (http://www.pisoftware.com/) <BR>
 * License: LGPL by default, or as assigned
 */
public class StringUtil {

  /** Back slash and quote char used by {@link #quoteString(String)}. */
  private final static String TOKENS = "\\'";

  /** Quote chars and space char used by {@link #parseString(String)}. */
  private final static String SEPARATORS = "\\\"' ";

  /** Buffer size when reading chars or bytes for a string. */
  private final static int BUFFER_SIZE = 10240;

  /**
   * Return the first prefix element of a string.
   *
   * For example, <code>getPrefixElements("a.b.c", 2)</code> returns
   * <code>"a.b"</code>.
   *
   * @param prefix  The string to get the prefix elements from.
   * @param n  The number of prefix elements.
   * @return The first n prefix elements, or the original string if there
   *         weren't n elements. If n <= 0 an empty string is returned.
   */
  public static String getPrefixElements(String prefix, int n) {

    int dotIndex = 0;

    if (n <= 0) return "";

    //Try to get all n elements.
    while (n > 0) {

      //May be more keep going
      if (dotIndex != -1) {
        dotIndex = prefix.indexOf(".", dotIndex + 1);
        n--;
        //Not enough
      } else {
        return prefix;
      }
    }

    //Got all n elements - return the substring.
    return prefix.substring(0, dotIndex);
  }


  /**
   * Checks to see if a wildcard string pattern matches a string.
   *
   * For example, <code>*gh*</code> would match <code>gh</code>,
   * <code>tough</code>, <code>tougher</code> or <code>ghost</code>.
   *
   * @param wildcardString The string containing the pattern.
   * @param matchString The string to match the pattern against.
   * @return true If the pattern was found, false otherwise.
   */
  public static boolean isMatch(String wildcardString, String matchString) {

    // The portion of the string that has not been matched with the pattern.
    String notYetMatched = matchString;

    // Tokenizer will throw away * characters.
    StringTokenizer strTok = new StringTokenizer(wildcardString, "*", false);

    // Store the tokens
    Vector<String> tokens = new Vector<String>();

    String token = "";

    int matchIndex = -1;

    // Always match same string.
    if (wildcardString.equals(matchString)) return true;

    // Never match empty string
    // (if matchString is empty will return true above)
    if (wildcardString.equals("")) return false;

    // No wildcards and strings are not equal.
    if (wildcardString.indexOf('*') == -1) return false;

    // Get all the tokens
    while (strTok.hasMoreTokens()) tokens.add(strTok.nextToken());

    // If there were characters to match - do they have to match the start or end?
    if (tokens.size() > 0) {

      if (!wildcardString.startsWith("*") &&
          !matchString.startsWith( (String) tokens.firstElement())) {
        return false;
      }

      if (!wildcardString.endsWith("*") &&
          !matchString.endsWith( (String) tokens.lastElement())) {
        return false;
      }
    }

    // Loop through the tokens.
    while (tokens.size() > 0) {

      token = (String) tokens.remove(0);

      // Find the token
      matchIndex = notYetMatched.indexOf(token);

      // Not found
      if (matchIndex < 0) return false;

      //Reduce the not matched portion up to the end of the token  match.
      notYetMatched = notYetMatched.substring(matchIndex + token.length());
    }

    // We matched all the tokens successfully.
    return true;
  }


  /**
   * Determines if the string is null or does not contain any characters.
   *
   * @param valueStr String that is to be checked
   * @param allowSpaces boolean indicating whether empty spaces are valid or not
   * @return true if empty, false if not
   */
  public static boolean isEmpty(String valueStr, boolean allowSpaces) {

    boolean isEmpty = false;

    if (valueStr == null) isEmpty = true;
    else if (valueStr.length() == 0) isEmpty = true;
    else if (!allowSpaces && (valueStr.trim().length() == 0)) isEmpty = true;

    return isEmpty;
  }


  /**
   * Determines if the string is null or does not contain any characters
   *
   * @param valueStr String that is to be checked
   * @return true if empty, false if not
   */
  public static boolean isEmpty(String valueStr) {

    return isEmpty(valueStr, false);
  }


  /**
   * Splits a String into a String array. The String is split at every separator
   * character. The separator characters do not appear in the String objects in
   * the String array.
   *
   * @param str the String to be split.
   * @param sep the String containing a list of separator characters.
   * @return an array of String objects.
   */
  public static String[] split(String str, String sep) {

    StringTokenizer st = new StringTokenizer(str, sep);
    String[] tokens = new String[st.countTokens()];

    for (int i = 0; i < tokens.length; ++i) {
      tokens[i] = st.nextToken();
    }

    return tokens;
  }

  /**
   * Converts an array of Strings to a single String consisting of a
   * space-separated list of strings. Individual strings will be quoted if they
   * contain spaces or quote characters.
   *
   * @param strings the array of Strings.
   * @return the resulting string.
   */
  public static String toString(String... strings) {

    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < strings.length; ++i) {

      String s = strings[i];

      // Add a separator.
      if (i > 0) sb.append(' ');

      // Quote the URI if it contains a space or a quote.
      if ((s.indexOf(' ') != -1) ||
          (s.indexOf('"') != -1) ||
          (s.indexOf('\'') != -1) ||
          (s.indexOf('\\') != -1)) {

        sb.append(quoteString(s));
      } else {
        sb.append(s);
      }
    }

    return sb.toString();
  }


  /**
   * Quotes a String by enclosing it in single quotes and escaping all single
   * quotes and back slashes.
   *
   * @param str the string to be quoted.
   * @return the quoted string.
   */
  public static String quoteString(String str) {

    StringTokenizer st = new StringTokenizer(str, TOKENS, true);
    StringBuffer sb = new StringBuffer(str.length() + 4);

    sb.append('\'');

    while (st.hasMoreTokens()) {

      String token = st.nextToken();
      char firstChar = token.charAt(0);

      if ((token.length() == 1) && (TOKENS.indexOf(firstChar) != -1)) {

        sb.append('\\');
        sb.append(firstChar);
      } else {

        // Token does not contain a back slash or quote char.
        sb.append(token);
      }
    }

    sb.append('\'');

    return sb.toString();
  }


  /**
   * Quotes a String by enclosing it in single quotes and escaping all single
   * quotes and back slashes.
   *
   * @param str the string to be unescaped.
   * @return the resulting string.
   */
  public static String unescapeString(String str) {

    StringTokenizer st = new StringTokenizer(str, "\\", true);
    StringBuffer sb = new StringBuffer(str.length());
    boolean escaped = false;

    while (st.hasMoreTokens()) {

      String token = st.nextToken();
      char firstChar = token.charAt(0);

      if (!escaped && (token.length() == 1) && (firstChar == '\\')) {
        escaped = true;
      } else {
        // Token does not contain a back slash or is an escaped back slash.
        sb.append(token);
        escaped = false;
      }
    }

    return sb.toString();
  }


  /**
   * Separates a String consisting of a space-separated list of substrings into
   * an array of Strings. Substrings in the input String may be quoted with
   * either single or double quotes.
   *
   * @param string the space-separated list of strings.
   * @return the string array.
   */
  public static String[] parseString(String string) {

    StringTokenizer st = new StringTokenizer(string.trim(), SEPARATORS, true);
    List<String> strings = new ArrayList<String>();
    StringBuffer sb = new StringBuffer(string.length());
    boolean inQuotedString = false;
    boolean escaped = false;
    char quoteChar = '\000';

    while (st.hasMoreTokens()) {

      String token = st.nextToken();
      char firstChar = token.charAt(0);

      if (!escaped &&
          (token.length() == 1) &&
          (SEPARATORS.indexOf(firstChar) != -1)) {

        // Quote or space character.
        if (firstChar == ' ') {

          if (inQuotedString) {
            // Space in quoted string.
            sb.append(firstChar);
          } else if (sb.length() > 0) {
            // Output string to List.
            strings.add(sb.toString());
            sb.setLength(0);
          }
        } else {

          if (inQuotedString) {

            if (firstChar == '\\') {
              // Escape the next character
              escaped = true;
            } else if (firstChar == quoteChar) {
              // End quote.
              inQuotedString = false;
            } else {
              // Quote within quoted string.
              sb.append(firstChar);
            }
          } else {
            // Begin quote.
            quoteChar = firstChar;
            inQuotedString = true;
          }
        }
      } else {
        // Token does not contain a space or quote char.
        sb.append(token);
        escaped = false;
      }
    }

    if (sb.length() > 0) {
      // Output final string to List.
      strings.add(sb.toString());
      sb.setLength(0);
    }

    return strings.toArray(new String[strings.size()]);
  }


  /**
   * Quotes chars in the attribute value source string by replacing them with
   * the corresponding XML entities.
   *
   * @param str the source string.
   * @param sb the StringBuffer to append to.
   */
  public static void quoteAV(String str, StringBuffer sb) {
    quoteXML(str, sb, "&<>'\"");
  }


  /**
   * Quotes chars in the attribute value source string by replacing them with
   * the corresponding XML entities.
   *
   * @param str the source string.
   * @return the quoted string.
   */
  public static String quoteAV(String str) {

    StringBuffer sb = new StringBuffer();
    quoteAV(str, sb);

    return sb.toString();
  }


  /**
   * Quotes chars in the CDATA source string by replacing them with the
   * corresponding XML entities.
   *
   * @param str the source string.
   * @param sb the StringBuffer to append to.
   */
  public static void quoteCDATA(String str, StringBuffer sb) {
    quoteXML(str, sb, "&<");
  }


  /**
   * Quotes chars in the CDATA source string by replacing them with the
   * corresponding XML entities.
   *
   * @param str the source string.
   * @return the quoted string.
   */
  public static String quoteCDATA(String str) {
    StringBuffer sb = new StringBuffer();
    quoteCDATA(str, sb);

    return sb.toString();
  }


  /**
   * Removes a substring from a string. Only the first instance of the substring
   * will be removed. Returns the original string if it does not contain the
   * substring.
   *
   * @param substring The substring to remove.
   * @param string The string to remove the substring from.
   * @return The string with substring removed.
   */
  public static String removeSubstring(String substring, String string) {

    StringBuffer buffer = new StringBuffer(string);

    int beginIndex = string.indexOf(substring);
    int endIndex;

    if (beginIndex != -1) {

      endIndex = substring.length() + beginIndex;
      buffer.delete(beginIndex, endIndex);

      return buffer.toString();
    } else {

      return string;
    }
  }


  /**
   * Left justify a string, inserting newlines at appropriate places to get
   * lines approximately the requested length.
   *
   * @param source The string data to re-format.
   * @param width The requested lenght of the lines.
   * @return The re-formatted text.
   */
  public static String justifyLeft(String source, int width) {

    // Check for null param or string length less than given width, there is
    // nothing to do for these cases.
    if ( (source == null) || (source.length() <= width)) {
      return source;
      // Exit - nothing to do
    }

    // Copy the source string into a stringbuffer so we can edit it, and
    // initialise the counters/pointers.
    StringBuffer result = new StringBuffer(source);
    int lastSpace = -1;
    int lineStart = 0;
    int charPosition = 0;

    // Go through every character in the buffer
    while (charPosition < result.length()) {

      // Keep track of the last whitespace seen.
      if (result.charAt(charPosition) == ' ') {
        lastSpace = charPosition;
      }

      // Restart counting if we find a newline already in the string.
      if (result.charAt(charPosition) == '\n') {
        lastSpace = -1;
        lineStart = charPosition + 1;
      }

      // Ok, we have reached the required width...
      if (charPosition > ( (lineStart + width) - 1)) {

        // Did we pass some whitespace on the way here?
        if (lastSpace != -1) {
          // Yes - set the last space to be a linefeed and reset counters
          result.setCharAt(lastSpace, '\n');
          lineStart = lastSpace + 1;
          lastSpace = -1;
        } else {
          // No - insert the linefeed right here, right now.
          result.insert(charPosition, '\n');
          lineStart = charPosition + 1;
        }
      }

      charPosition++;
    }

    return result.toString();
  }


  /**
   * Substitutes text in inputStr from values in substituteArray.
   *
   * @param inputStr A String containing text mixed in with ~1,~2,...~n tags.
   * @param substituteArray An array of strings which will replace the tags in
   *      inputStr.
   * @return inputStr with tags replaced by the appropriate index from
   *      substituteArray.
   */
  public static String substituteStrings(String inputStr, String... substituteArray) {

    String resultStr = inputStr;

    if (substituteArray != null) {

      // Loop thru the substurion string array, replacing the "~n" values with
      // the array element.
      for (int tagInt = 0; tagInt < substituteArray.length; tagInt++) {

        if ((tagInt + 1) > 9) {

          resultStr =
              replaceStringWithString(resultStr, "~" + (tagInt + 1),
              substituteArray[tagInt]);
        } else {

          resultStr =
              replaceStringWithString(resultStr, "~0" + (tagInt + 1),
              substituteArray[tagInt]);
        }
      }
    }

    return resultStr;
  }

  /**
   * Substitutes one value with another throughout the string.
   *
   * @param sourceStr the string that contains the tokens
   * @param tokenStr the string that acts as the token to be replaced
   * @param replacementStr the string containing the value to be substituted
   * @param ignoreCase boolean indicating whether to ignore the case during the compare
   * @return String containing the string with all the values replaced
   */
  public static String replaceStringWithString(String sourceStr,
      String tokenStr, String replacementStr, boolean ignoreCase) {

    if ((sourceStr == null) || (tokenStr == null) || tokenStr.equals("")) {
      return sourceStr;
    }

    StringBuffer resultStr = new StringBuffer();
    String indexStr = sourceStr;
    int tokenStrLength = tokenStr.length();
    int tokenStrIndex;
    int pos = 0;

    if (ignoreCase) {
      tokenStr = tokenStr.toUpperCase();
      indexStr = sourceStr.toUpperCase();
    }

    while ((tokenStrIndex = indexStr.indexOf(tokenStr, pos)) != -1) {
      resultStr.append(sourceStr.substring(pos, tokenStrIndex));
      resultStr.append(replacementStr);
      pos = tokenStrIndex + tokenStrLength;
    }

    resultStr.append(sourceStr.substring(pos));

    return resultStr.toString();
  }


  /**
   * Substitutes one value with another throughout the string
   *
   * @param sourceStr the string that contains the tokens
   * @param tokenStr the string that acts as the token to be replaced
   * @param replacementStr the string containing the value to be substituted
   * @return String containing the string with all the values replaced
   */
  public static String replaceStringWithString(String sourceStr, String tokenStr, String replacementStr) {
    return replaceStringWithString(sourceStr, tokenStr, replacementStr, false);
  }


  /**
   * Converts the data available through a {@link java.io.Reader} into a String.
   * @param reader The {@link java.io.Reader} to be converted.
   * @return The string that comes from the reader.
   * @throws IOException If there was an error accessing the Reader.
   */
  public static String toString(Reader reader) throws IOException {
    char[] buffer = new char[BUFFER_SIZE];
    int len;
    StringBuilder result = new StringBuilder();
    while ((len = reader.read(buffer)) >= 0) {
      result.append(buffer, 0, len);
    }
    return result.toString();
  }


  /**
   * Quotes the specified chars in the source string by replacing them with the
   * corresponding XML entities.
   *
   * @param str the source string.
   * @param sb the StringBuffer to append to.
   * @param chars the chars to quote.
   */
  private static void quoteXML(String str, StringBuffer sb, String chars) {

    StringTokenizer st = new StringTokenizer(str, chars, true);

    while (st.hasMoreTokens()) {

      String token = st.nextToken();
      char firstCh = token.charAt(0);

      if ((token.length() == 1) && (chars.indexOf(firstCh) != -1)) {

        switch (firstCh) {

          case '&':
            sb.append("&amp;");
            break;

          case '<':
            sb.append("&lt;");
            break;

          case '\'':
            sb.append("&apos;");
            break;

          case '"':
            sb.append("&quot;");
            break;

          case '>':
            sb.append("&gt;");
            break;

          default:
            sb.append(token);
            break;
        }
      } else {

        int pos;

        while ( (pos = token.indexOf("]]>")) != -1) {
          if (pos > 0) sb.append(token.substring(0, pos));

          sb.append("]]&gt;");
          token = token.substring(pos + 3);
        }

        if (token.length() > 0) sb.append(token);
      }
    }
  }


  /** Map of escape characters to their character codes */
  private static Map<Character,String> map = new HashMap<Character,String>();
  static {
    map.put('t', "\t");
    map.put('t', "\t");
    map.put('b', "\b");
    map.put('n', "\n");
    map.put('r', "\r");
    map.put('f', "\f");
    map.put('\\', "\\");
    map.put('"', "\"");
    map.put('\'', "'");
  }


  /**
   * Search for escape characters in a string, and replace them with the request values.
   * @param s The string to search.
   * @return A new string with all escape characters replaced with the originals.
   */
  static final public String unescapeJavaString(String s) {
    StringBuilder sb = new StringBuilder();
    int last = 0;
    int pos = 0;
    while ((pos = s.indexOf('\\', pos)) >= 0) {
      sb.append(s.substring(last, pos));
      if (++pos == s.length()) break;
      char c = s.charAt(pos);
      String m = map.get(c);
      if (m != null) sb.append(m);
      else sb.append(c);
      last = ++pos;
    }
    sb.append(s.substring(last));
    return sb.toString();
  }


  /**
   * Returns a stack trace of a Throwable as a string, rather than the
   * default behaviour of sending it to stderr.
   * 
   * @param t The Throwable containing the stack trace.
   * @return A string containing the stack trace.
   */
  public static String strackTraceToString(Throwable t) {
    StringWriter strWriter = new StringWriter();
    t.printStackTrace(new PrintWriter(strWriter));
    return strWriter.toString();
  }
}
