package org.jrdf.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * A utility which applies N-Triples escaping.
 *
 * @author Andrew Newman
 * @author Paula Gearon
 * @version $Revision: 624 $
 */
public class EscapeUtil {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(EscapeUtil.class.getName());

  /**
   * A regular expression to pick out characters needing escape from Unicode to
   * ASCII.  A different regular expression is used depending on which version of the JDK is detected - Java 1.4 has
   * different character support compared with 1.5 and above.
   * <p/>
   * This is used by the {@link #escape} method.
   */
  private static Pattern pattern;

  static {
      try {
          if (System.getProperty("java.version").indexOf("1.4") >= 0) {
              pattern = Pattern.compile("[\\uD800-\\uDBFF][\\uDC00-\\uDFFF]" +
                      "|" +
                      "[\\x00-\\x1F\\x22\\\\\\x7F-\\uFFFF]");
          } else {
              pattern = Pattern.compile("[\uD800\uDC00-\uDBFF\uDFFF]" +
                      "|" +
                      "[\\x00-\\x1F\\x22\\\\\\x7F-\\uFFFF]");
          }
      } catch (Exception e) {
          logger.error("Unable to initialize Regex pattern", e);
      }
  }

  /**
   * Base UTF Code point.
   */
  private static final int UTF_BASE_CODEPOINT = 0x10000;

  /**
   * How shift to get UTF-16 to character codes.
   */
  private static final int CHARACTER_CODE_OFFSET = 0x3FF;

  /**
   * How many characters at a time to decode for 8 bit encoding.
   */
  private static final int CHARACTER_LENGTH_8_BIT = 11;

  /**
   * How many characters at a time to decode for 16 bit encoding.
   */
  private static final int CHARACTER_LENGTH_16_BIT = 7;

  private EscapeUtil() {
  }

  /**
   * Escapes a string literal to a string that is N-Triple escaped.
   *
   * @param string a string to escape, never <code>null</code>.
   * @return a version of the <var>string</var> with N-Triples escapes applied.
   */
  public static final String escape(String string) {
    assert null != string;

    // Obtain a fresh matcher
    Matcher matcher = pattern.matcher(string);

    // Try to short-circuit the whole process -- maybe nothing needs escaping?
    if (!matcher.find()) {
      return string;
    }

    // Perform escape character substitutions on each match found by the
    // matcher, accumulating the escaped text into a stringBuffer
    StringBuffer stringBuffer = new StringBuffer();
    do {
      // The escape text with which to replace the current match
      String escapeString;

      // Depending of the character sequence we're escaping, determine an
      // appropriate replacement
      String groupString = matcher.group();
      switch (groupString.length()) {
        case 1: // 16-bit characters requiring escaping
          switch (groupString.charAt(0)) {
            case '\t': // tab
              escapeString = "\\\\t";
              break;
            case '\n': // newline
              escapeString = "\\\\n";
              break;
            case '\r': // carriage return
              escapeString = "\\\\r";
              break;
            case '"':  // quote
              escapeString = "\\\\\\\"";
              break;
            case '\\': // backslash
              escapeString = "\\\\\\\\";
              break;
            default:   // other characters use 4-digit hex escapes
              String hexString = Integer.toHexString(groupString.charAt(0)).toUpperCase();

              escapeString = "\\\\u0000".substring(0, CHARACTER_LENGTH_16_BIT - hexString.length()) + hexString;

              assert CHARACTER_LENGTH_16_BIT == escapeString.length();
              assert escapeString.startsWith("\\\\u");
              break;
          }
          break;

        case 2: // surrogate pairs are represented as 8-digit hex escapes
          assert Character.SURROGATE == Character.getType(groupString.charAt(0));
          assert Character.SURROGATE == Character.getType(groupString.charAt(1));

          int highSurrogate = ((groupString.charAt(0) & CHARACTER_CODE_OFFSET) << 10);
          int lowSurrogate = (groupString.charAt(1) & CHARACTER_CODE_OFFSET);
          String hexString = Integer.toHexString(highSurrogate + lowSurrogate + UTF_BASE_CODEPOINT).
                  toUpperCase();
          escapeString = "\\\\U00000000".substring(0, CHARACTER_LENGTH_8_BIT - hexString.length()) +
                  hexString;

          assert CHARACTER_LENGTH_8_BIT == escapeString.length();
          assert escapeString.startsWith("\\\\U00") : "Expected a start of \\\\U00, but got " + escapeString;
          break;

        default:
          throw new Error("Escape sequence " + groupString + " has no handler");
      }
      assert null != escapeString;

      // Having determined an appropriate escapeString, add it to the
      // stringBuffer
      matcher.appendReplacement(stringBuffer, escapeString);
    }
    while (matcher.find());

    // Finish off by appending any remaining text that didn't require escaping,
    // and return the assembled buffer
    matcher.appendTail(stringBuffer);
    return stringBuffer.toString();
  }


  /**
   * Escapes a string which contains a UTF-8 encoding in the internal array of char.
   * If a UTF-8 encoding is found to be invalid, then this will drop back to
   * escaping the data as a normal string. Escaping is performed with the NTriples
   * encoding recommendation:
   * <a href="http://www.w3.org/TR/2004/REC-rdf-testcases-20040210/#ntrip_strings">&sect;3.2</a>
   * @param string The string to escape.
   * @return An escaped version of the string.
   */
  public static final String escapeUTF8(String string) {
    assert null != string;

    // Perform escape character substitutions on each match found by the
    // matcher, accumulating the escaped text into a stringBuilder
    StringBuilder buffer = new StringBuilder();

    try {
      int i = 0;
      while (i < string.length()) {
        char c = string.charAt(i);
        int bytes = getByteCount(c);
        if (bytes == 4) {
          int codepoint = getCodepoint(string, i, c);
          buffer.append(String.format("\\U%08X", codepoint));
        } else {
          if (bytes != 1) c = getChar(string, i, bytes, c);
  
          switch (c) {
            case 0x9:
              buffer.append("\\t");
              break;
            case 0xA:
              buffer.append("\\n");
              break;
            case 0xD:
              buffer.append("\\r");
              break;
            case 0x22:
              buffer.append("\\\"");
              break;
            case 0x5C:
              buffer.append("\\\\");
              break;
            default:
              if (c <= 0x1F || c >= 0x7F) {
                buffer.append(String.format("\\u%04X", (int)c));
              } else {
                buffer.append(c);
              }
          }
        }
        i += bytes;
      }
  
      return buffer.toString();
    } catch (Exception e) {
      // This is not a sequence of UTF-8 characters. Fall back to the old escape algorithm.
      return escape(string);
    }
  }


  /**
   * Determine the number of characters in a UTF-8 sequence, based on the start of the sequence.
   * @param c The first byte from the sequence, held in a char.
   * @return The number of bytes in the sequence.
   * @throws IllegalArgumentException If the bit pattern in the character does not represent a valid sequence.
   */
  static final int getByteCount(char c) {
    if ((c & 0xFF80) == 0) return 1;
    if ((c & 0xFFE0) == 0xC0) return 2;
    if ((c & 0xFFF0) == 0xE0) return 3;
    if ((c & 0xFFF8) != 0xF0) throw new IllegalArgumentException("Not a character from a UTF-8 sequence.");
    return 4;
  }


  /**
   * Calculate the codepoint (a character that doesn't fit into a char) represented
   * by a 4 byte UTF-8 encoding.
   * @param s The string containing the encoding. Each char in the string contains
   *        a single byte from the sequence.
   * @param offset The start of the 4 byte sequence.
   * @param startChar The first byte (retrieved as a char) in the sequence.
   *        This is identical to s.charAt(offset) but this was already called
   *        for {@link #getByteCount(char)}, so we reuse it here.
   * @return The Unicode codepoint represented by the 4 byte sequence.
   * @throws IllegalArgumentException If the bit pattern in the character does not represent a valid sequence.
   */
  static final int getCodepoint(String s, int offset, char startChar) {
    int secondChar = s.charAt(offset + 1);
    int thirdChar = s.charAt(offset + 2);
    int fourthChar = s.charAt(offset + 3);

    // byte sequence is: 11110zzz, 10zzyyyy, 10yyyyxx, 10xxxxxx
    // check that the trailing bytes all start correctly
    if ((secondChar & 0xC0) != 0x80 || (thirdChar & 0xC0) != 0x80 || (fourthChar & 0xC0) != 0x80) {
      throw new IllegalArgumentException("Not a character from a UTF-8 sequence.");
    }
    int x = fourthChar & 0x3F;
    int yx = thirdChar & 0x3F;
    int zy = secondChar & 0x3F;
    int z = (startChar & 0x07) << 2 | zy >> 4;
    x |= (yx & 0x03) << 6;
    int y = yx >> 2 | (zy & 0x0F) << 4;
    return (z << 16) | (y << 8) | x;
  }


  /**
   * Calculate the character represented by a 2 byte or 3 byte UTF-8 encoding.
   * @param s The string containing the encoding. Each char in the string contains
   *        a single byte from the sequence.
   * @param offset The start of the 2 or 3 byte sequence.
   * @param count The number of bytes in the sequence
   *        (already determined through {@link #getByteCount(char)}).
   * @param startChar The first byte (retrieved as a char) in the sequence.
   *        This is identical to s.charAt(offset) but this was already called
   *        for {@link #getByteCount(char)}, so we reuse it here.
   * @return The Unicode character represented by the 2 or 3 byte sequence.
   */
  static final char getChar(String s, int offset, int count, char startChar) {
    assert count == 2 || count == 3;
    int lastPos = offset + count - 1;
    int lastChar = s.charAt(lastPos);

    // check that the last byte matches 10xxxxxx
    if ((lastChar & 0xC0) != 0x80) throw new IllegalArgumentException("Not a character from a UTF-8 sequence.");
    int x = lastChar & 0x3F;
    int yx;
    int y;
    if (count == 2) {
      // 2 byte sequence. First byte is 110yyyxx, second is 10xxxxxx
      yx = startChar & 0x3F;
      y = yx >> 2;
    } else {
      // 3 byte sequence. First byte is 1110yyyy, Second byte is 10yyyyxx
      int secondChar = s.charAt(offset + 1);
      // check that second byte starts correctly 
      if ((secondChar & 0xC0) != 0x80) throw new IllegalArgumentException("Not a character from a UTF-8 sequence.");
      yx = secondChar & 0x3F;
      y = (yx >> 2) | (startChar & 0x0F) << 4;
    }
    x |= (yx & 0x03) << 6;
    return (char)(y << 8 | x);
  }

}
