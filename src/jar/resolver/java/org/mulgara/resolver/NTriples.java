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

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;

// Local packages
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;

/**
 * Static library for converting N-Triples serialization to and from JRDF
 * {@link Node}s.
 *
 * @created 2004-09-22
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:24 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 * Portions by Paul Gearon.
 * @copyright &copy;2006 <a href="http://www.herzumsoftware.com/">Herzum Software LLC</a>
 */
abstract class NTriples
{
  /**
   * Logger.
   *
   * This is named after the class.
   */
  private static final Logger logger =
    Logger.getLogger(NTriples.class.getName());

  /**
   * A regular expression matching NTriples literals.
   *
   * In the following pattern:
   * <ul>
   * <li>Group 0 is the entire literal serialization</li>
   * <li>Group 1 is the lexical form</li>
   * <li>Group 3 is the language clause<li>
   * <li>Group 4 is the language code</li>
   * <li>Group 5 is the datatype clause</li>
   * <li>Group 6 is the datatype URI</li>
   * </ul>
   */
  private static final Pattern literalPattern = Pattern.compile(
    "\\x22(([^\\\\]|\\\\[tnr\\x22\\\\]|\\\\u\\p{XDigit}{2}|\\\\U\\p{XDigit}{4})*)\\x22" +  // lexical form
    "(@(\\w+))?" +        // optional language
    "(\\^\\^<([^>]*)>)?"  // optional datatype
  );

  /**
   * A regular expression to pick out characters needing escape from Unicode
   * to ASCII.
   */
  private static final Pattern escapedCharacterPattern = Pattern.compile(
    "[\ud800\udc00-\udbff\udfff]" +                // surrogate pairs
    "|" +                                          // ...or...
    "[\\x00-\\x1F\\x22\\\\\\x7F-\\uFFFF]"          // all other escaped chars
  );

  /**
   * A regular expression to pick out ASCII escapes for Unicode characters.
   *
   * In the following pattern:
   * <ul>
   * <li>Group 0 is the escaped lexical form</li>
   * <li>Group 2 is any single character escape</li>
   * <li>Group 3 is any 4-digit Unicode escape</li>
   * <li>Group 4 is any 8-digit Unicode escape</li>
   * </ul>
   */
  private static final Pattern escapePattern = Pattern.compile(
    "\\\\" +               // all escapes start with a backslash
    "(" +
    "([tnr\\\\\\\"])" +    // tab, newline, return, backslash, quote
    "|" +                  // ...or...
    "u(\\p{XDigit}{4})" +  // a 16-bit hexadecimal Unicode
    "|" +                  // ...or...
    "U(\\p{XDigit}{8})" +  // a 32-bit hexadecimal Unicode
    ")"
  );

  /**
   * Convert N-Triples to JRDF.
   *
   * @param string  a string in N-Triples format, never <code>null</code>
   * @param baseURI  the base URI against which to resolve relative URI
   *   references, which must be absolute
   * @return  a JRDF node equivalent to the <var>string</var>
   * @throws IllegalArgumentException if <var>string</var> is <code>null</code>
   * @throws ParseException if <var>string</var> isn't valid N-Triples
   */
  public static Node toNode(String string, URI baseURI) throws ParseException
  {
    // Validate "string" parameter
    if (string == null) {
      throw new IllegalArgumentException("Null \"string\" parameter");
    }

    // Validate "baseURI" parameter
    if (baseURI == null || !baseURI.isAbsolute()) {
      throw new IllegalArgumentException(
        "Illegal \"baseURI\" parameter: " + baseURI
      );
    }

    if (string.charAt(0) == '<') {
      // A named resource
      if (string.length() < 2 || string.charAt(string.length() - 1) != '>') {
        throw new ParseException("No terminating '>' in " + string, 1);
      }
      string = string.substring(1, string.length() - 1);

      URI uri;
      if (string.length() == 0) {
        // The URI.resolve() method does not work correctly in this case.
        // The absolute URI is the database URI.
        uri = baseURI;
      }
      else {
        // Resolve the (possibly) relative uri against the database URI.
        uri = baseURI.resolve(string);
      }
      assert uri != null;
      assert uri.isAbsolute() : uri + " is not absolute";

      return new URIReferenceImpl(uri);
    }
    else if (string.charAt(0) == '"') {
      Matcher matcher = literalPattern.matcher(string);
      if (!matcher.matches()) {
        throw new ParseException("Invalid literal: " + string, -1);
      }

      // Determine the datatype URI
      URI datatypeURI = null;
      if (matcher.group(6) != null) {
        try {
          datatypeURI = new URI(matcher.group(6));
        }
        catch (URISyntaxException e) {
          ParseException parseException =
            new ParseException("Invalid datatype URI", -1);
          parseException.initCause(e);
          throw parseException;
        }
      }

      // Determine the language code
      String language = matcher.group(4);
      if (datatypeURI == null && language == null) {
        language = "";
      }

      if (datatypeURI == null) {
        return new LiteralImpl(
          unescapeLexicalForm(matcher.group(1)),  // lexical form
          language                                // language code
        );
      }
      else {
        return new LiteralImpl(
          unescapeLexicalForm(matcher.group(1)),  // lexical form
          datatypeURI                             // datatype
        );
      }
    }
    else {
      throw new ParseException("Unrecognized initial character in" + string, 1);
    }
  }

  /**
   * Convert JRDF to N-Triples.
   *
   * @param node  a JRDF node, never <code>null</code>
   * @param baseURI  the base URI against which to relativize URI references,
   *   always absolute
   * @return  the N-Triples serialization for the <var>node</var>
   * @throws IllegalArgumentException if <var>node</var> is <code>null</code>
   *   or is neither a {@link URIReference} nor a {@link Literal}.
   */
  public static String toString(Node node, URI baseURI)
  {
    // Validate "node" parameter
    if (node == null) {
      throw new IllegalArgumentException("Null \"node\" parameter");
    }

    // Validate "baseURI" parameter
    if (baseURI != null && !baseURI.isAbsolute()) {
      throw new IllegalArgumentException(
        "Relative \"baseURI\" parameter: " + baseURI
      );
    }

    if (node instanceof URIReference) {
      URI uri = ((URIReference) node).getURI();
      URI relativeURI = (baseURI != null) ? baseURI.relativize(uri) : uri;

      // Be suspicious about relative URIs -- we're only expecting the
      // names of models from this server, or the name of the server itself
      if (!relativeURI.isAbsolute()) {
        if ((relativeURI.getAuthority() != null) ||
            ((relativeURI.getPath() != null) &&
             (relativeURI.getPath().length() > 0)) ||
            (relativeURI.getFragment() == null))
        {
          logger.warn("Unusual relative URI in backup: " + relativeURI +                      " authority=\"" + relativeURI.getAuthority() + "\"" +
              " path=\"" + relativeURI.getPath() + "\"" +
              " fragment=\"" +
              relativeURI.getFragment() + "\"");
        }
      }

      return "<" + relativeURI + ">";
    }
    else if (node instanceof Literal) {
      Literal literal = (Literal) node;

      // Lexical form
      StringBuffer buffer = new StringBuffer();
      buffer.append('"')
            .append(escapeLexicalForm(literal.getLexicalForm()))
            .append('"');

      // Language code
      String lang = literal.getLanguage();
      if (lang != null && !lang.equals("")) {
        buffer.append('@').append(lang);
      }

      // Datatype URI
      if (literal.getDatatypeURI() != null) {
        buffer.append("^^<")
              .append(literal.getDatatypeURI().toString())
              .append('>');
      }

      return buffer.toString();
    }
    else {
      throw new IllegalArgumentException(
        "Unsupported node of class " + node.getClass() + ": " + node
      );
    }
  }

  /**
   * Escape an arbitrary unicode lexical form into N-Triples serialization.
   *
   * @param string  a string to escape, never <code>null</code>
   * @return a version of the <var>string</var> with N-Triples escapes applied
   * @throws IllegalArgumentException if <var>string</var> is <code>null</code>
   */
  public static String escapeLexicalForm(String string)
  {
    // Validate "string" parameter
    if (string == null) {
      throw new IllegalArgumentException("Null \"string\" parameter");
    }

    // Obtain a matcher
    Matcher matcher = escapedCharacterPattern.matcher(string);

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
              String hexString =
                  Integer.toHexString(groupString.charAt(0)).toUpperCase();
              escapeString =
                  "\\\\u0000".substring(0, 7 - hexString.length()) + hexString;

              assert escapeString.length() == 7;
              assert escapeString.startsWith("\\\\u");
            break;
          }
        break;

        case 2: // surrogate pairs are represented as 8-digit hex escapes
          assert Character.getType(groupString.charAt(0)) == Character.SURROGATE;
          assert Character.getType(groupString.charAt(1)) == Character.SURROGATE;

          String hexString = Integer.toHexString(
              ( (groupString.charAt(0) & 0x3FF) << 10) + // high surrogate
              (groupString.charAt(1) & 0x3FF) + // low surrogate
              0x10000 // base codepoint U+10000
              ).toUpperCase();
          escapeString =
              "\\\\U00000000".substring(0, 11 - hexString.length()) + hexString;
          assert escapeString.length() == 11;
          assert escapeString.startsWith("\\\\U000");
        break;

        default:
          throw new Error("Escape sequence " + groupString + " has no handler");      }
      assert escapeString != null;

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
   * Unescape N-Triples serialization of a lexical form back to unicode.
   *
   * @param string  an ASCII string formatted with N-Triples lexical form
   *   escape codes, never <code>null</code>
   * @return a version of the <var>string</var> with N-Triples escapes
   *   evaluated
   * @throws IllegalArgumentException if <var>string</var> is <code>null</code>
   */
  public static String unescapeLexicalForm(String string)
  {
    // Validate "string" parameter
    if (string == null) {
      throw new IllegalArgumentException("Null \"string\" parameter");
    }

    // Obtain a matcher
    Matcher matcher = escapePattern.matcher(string);

    // Try to short-circuit the whole process -- maybe nothing needs escaping?
    if (!matcher.find()) {
      return string;
    }

    // Perform unescape character substitutions on each match found by the
    // matcher, accumulating the unescaped text into a stringBuffer
    StringBuffer stringBuffer = new StringBuffer();
    do {
      // The escape text with which to replace the current match
      String unescapedString;

      if (matcher.group(2) != null) {
        switch (matcher.group(2).charAt(0)) {
          case 't':   // tab
            unescapedString = "\t";
          break;
          case 'n':   // newline
            unescapedString = "\n";
          break;
          case 'r':   // return
            unescapedString = "\r";
          break;
          case '"':   // quote
            unescapedString = "\"";
          break;
          case '\\':  // backslash
            unescapedString = "\\\\";  // this has to be escaped because
                                       // Matcher.appendReplacement tries to
                                       // find capturing group references
          break;
          default:
            throw new Error("Impossible condition in unescape parsing");
        }
      }
      else if (matcher.group(3) != null) {
        try {
          unescapedString =
            Character.toString((char) Integer.parseInt(matcher.group(3), 16));
        }
        catch (NumberFormatException e) {
          Error error = new Error("Impossible condition in unescape parsing");
          error.initCause(e);
          throw error;
        }
      }
      else if (matcher.group(4) != null) {
        try {
          int unicode = Integer.parseInt(matcher.group(4), 16);

          int highSurrogate = 0xD800 + ((unicode-0x10000) >> 10);
          assert highSurrogate >= 0xD800 && highSurrogate < 0xDC00:
            "Bad high surrogate U+" + Integer.toHexString(highSurrogate);

          int lowSurrogate  = 0xDC00 + ((unicode-0x10000) & 0x3FF);
          assert lowSurrogate >= 0xDC00 && lowSurrogate < 0xE000:
            "Bad low surrogate U+" + Integer.toHexString(lowSurrogate);

          unescapedString = Character.toString((char) highSurrogate) +
                            Character.toString((char) lowSurrogate);
        }
        catch (NumberFormatException e) {
          Error error = new Error("Impossible condition in unescape parsing");
          error.initCause(e);
          throw error;
        }
      }
      else {
        throw new Error("Impossible condition in unescape parsing");
      }
      assert unescapedString != null;

      // Having determined an appropriate unescapedString, add it to the
      // stringBuffer
      matcher.appendReplacement(stringBuffer, unescapedString);
    }
    while (matcher.find());

    // Finish off by appending any remaining text that didn't require escaping,
    // and return the assembled buffer
    matcher.appendTail(stringBuffer);
    return stringBuffer.toString();
  }
}
