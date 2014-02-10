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

package org.mulgara.store.stringpool.xa;

// Java 2 standard packages
import java.nio.ByteBuffer;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.store.stringpool.*;


/**
 * An SPObject that represents untyped string literals.
 *
 * @created 2002-03-07
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/03/11 04:15:22 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPStringImpl extends AbstractSPObject implements SPString {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPStringImpl.class);

  private static final int LANG_SEPARATOR = '\0';

  private String str;

  private String lang;

  SPStringImpl(String str) {
    if (str == null) throw new IllegalArgumentException("Null \"str\" parameter");
    this.str = str;
    lang = "";
  }


  SPStringImpl(String str, String lang) {
    if (str == null) throw new IllegalArgumentException("Null \"str\" parameter");
    if (lang == null) lang = "";
    this.str = str;
    this.lang = lang;
  }


  SPStringImpl(ByteBuffer data) {
    String fullStr = CHARSET.decode(data).toString();
    int sep = fullStr.indexOf(LANG_SEPARATOR);
    if (sep < 0) {
      str = fullStr;
      lang = "";
    } else {
      lang = fullStr.substring(0, sep);
      str = fullStr.substring(sep + 1);
    }
  }


  static SPObject newSPObject(String str) {
    return new SPStringImpl(str);
  }


  static SPObject newSPObject(String str, String language) {
    if (language == null || language.length() == 0) return new SPStringImpl(str);
    if (language.charAt(0) == '@') language = language.substring(1);
    if (!checkLangChars(language)) throw new IllegalArgumentException("Invalid language code characters: " + language);
    return new SPStringImpl(str, language);
  }


  public String getLexicalForm() {
    return str;
  }


  public String getLanguageCode() {
    return lang;
  }


  /* from SPObject interface. */

  public TypeCategory getTypeCategory() {
    return TypeCategory.UNTYPED_LITERAL;
  }


  public ByteBuffer getData() {
    StringBuilder sb = new StringBuilder(lang);
    sb.appendCodePoint(LANG_SEPARATOR).append(str);
    return CHARSET.encode(sb.toString());
  }


  public SPComparator getSPComparator() {
    return SPCaseInsensitiveStringComparator.getInstance();
  }


  public String getEncodedString() {
    StringBuffer sb = new StringBuffer(str.length() + 8);
    sb.append(str);
    escapeString(sb);
    sb.insert(0, '"').append('"');
    if (lang.length() > 0) sb.append("@").append(lang);
    return sb.toString();
  }


  public org.jrdf.graph.Node getRDFNode() {
    if (lang.length() > 0) return new LiteralImpl(str, lang);
    return new LiteralImpl(str);
  }


  /* from Comparable interface. */

  public int compareTo(SPObject o) {
    // Compare types.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // Compare the language Strings.
    c = lang.compareToIgnoreCase(((SPStringImpl)o).lang);
    if (c != 0) return c;
    
    // Compare the Strings.
    return str.compareToIgnoreCase(((SPStringImpl)o).str);
  }


  /* from Object. */

  public int hashCode() {
    return str.hashCode() + lang.hashCode() * 13;
  }


  public boolean equals(Object obj) {
    // Check for null.
    if (obj == null) return false;
    // short circuit if it is the same object
    if (this == obj) return true;
    // check for type
    if (!(obj instanceof SPStringImpl)) return false;

    SPStringImpl other = (SPStringImpl)obj;
    return str.equals(other.str) && lang.equals(other.lang);
  }

  /**
   * Test if a language code is valid. Language codes are guaranteed to be ASCII
   * and must meet the pattern: [a-zA-Z]+ ('-' [a-zA-Z0-9]+)*
   * Also permits empty language tags to pass.
   * @param lang The language string to test.
   * @return <code>true</code> if the string passes the test, or <code>false</code> if it doesn't.
   */
  public static boolean checkLangChars(String lang) {
    // a flag to indicate when the first '-' character has been passed
    boolean extension = false;
    // check each character in the string
    for (int i = 0; i < lang.length(); i++) {
      char current = lang.charAt(i);
      // check for starting an extension
      if (current == '-') {
        // must have more characters
        if (++i == lang.length()) return false;
        extension = true;
        // the very next character can only be a letter or digit, and not a '-'
        if (!isAsciiLetterDigit(lang.charAt(i))) return false;
        continue;
      }
      // before the first '-' character, only letters are accepted.
      if (extension) {
        if (!isAsciiLetterDigit(current)) return false;
      } else {
        if (!isAsciiLetter(current)) return false;
      }
    }
    return true;
  }

  /**
   * Check if a character is an ASCII letter or digit.
   * @param c The character to test.
   * @return <code>true</code> if the character meets the pattern [a-zA-Z0-9]
   */
  private static boolean isAsciiLetterDigit(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
  }

  /**
   * Check if a character is an ASCII letter.
   * @param c The character to test.
   * @return <code>true</code> if the character meets the pattern [a-zA-Z]
   */
  private static boolean isAsciiLetter(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
  }
}

