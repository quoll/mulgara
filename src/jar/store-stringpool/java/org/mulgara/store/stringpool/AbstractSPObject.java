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

package org.mulgara.store.stringpool;

// Java 2 standard packages
import java.nio.charset.Charset;

// Third party packages


/**
 *
 * @created 2002-03-07
 *
 * @author David Makepeace
 *
 * @version $Revision$
 *
 * @modified $Date$
 *
 * @maintenanceAuthor $Author$
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class AbstractSPObject implements SPObject {

  public static final Charset CHARSET = Charset.forName("UTF-8");


  protected AbstractSPObject() {
    // NO-OP
  }


  /* from Comparable interface. */

  public int compareTo(SPObject o) {
    if (compareOverride() || o.compareOverride()) {
      // start by checking if these are of differing types
      int c = getTypeCategory().ID - o.getTypeCategory().ID; 
      if (0 != c) {
        return c;
      }
      if (compareOverride()) {
        return isSmallest() ? -1 : 1;
      } else {
        return o.isSmallest() ? 1 : -1;
      }
    }
    // Compare the type category ids.
    return getTypeCategory().ID - o.getTypeCategory().ID;
  }


  /* from Object. */

  public String toString() {
    return getEncodedString();
  }


  /**
   * Escapes quote, carriage return, linefeed and backslash characters in the
   * specified string.
   *
   * @param str the string to be escaped.
   * @return the escaped string.
   */
  protected static String escapeString(String str) {
    StringBuffer sb = new StringBuffer(str);
    escapeString(sb);
    return sb.toString();
  }


  /**
   * Unescapes characters in the specified string.
   *
   * @param str the string to be unescaped.
   * @return the unescaped string.
   */
  public static String unescapeString(String str) {
    StringBuffer sb = new StringBuffer(str);
    unescapeString(sb);
    return sb.toString();
  }


  /**
   * Escapes quote, carriage return, linefeed and backslash characters in the
   * specified string buffer.
   *
   * @param sb the string to be escaped.
   */
  public static void escapeString(StringBuffer sb) {
    for (int i = 0; i < sb.length(); ++i) {
      switch (sb.charAt(i)) {
        case '"':
        case '\\':
          sb.insert(i++, '\\');
          break;
        case '\r':
          sb.replace(i, ++i, "\\r");
          break;
        case '\n':
          sb.replace(i, ++i, "\\n");
          break;
      }
    }
  }


  /**
   * Unescapes characters in the specified string buffer.
   *
   * @param sb the string to be unescaped.
   */
  public static void unescapeString(StringBuffer sb) {
    for (int i = 0; i < sb.length(); ++i) {
      if (sb.charAt(i) == '\\') {
        if (sb.charAt(i + 1) == 'r') {
          sb.replace(i, i + 2, "\r");
        } else if (sb.charAt(i + 1) == 'n') {
          sb.replace(i, i + 2, "\n");
        } else {
          sb.delete(i, i + 1);
        }
      }
    }
  }


  /**
   * Method used to test if a different comparison operation should be performed at this level.
   *
   * @return <code>true</code> if a different comparison operation should be used for this object.
   */
  public boolean compareOverride() {
    return false;
  }

  /**
   * Declares that this object is to be considered to be the smallest of its type.
   * Only valid if compareOverride returns <code>true</code>.  If this object is the largest
   * then return <code>false</code> instead.
   *
   * @return <code>true</code> if this object should be considered the smallest of its type,
   *         or <code>false</code> if it should be considered the largest.  Unspecified if
   *         The compareOverride method returns <code>false</code>.
   */
  public boolean isSmallest() {
    return false;
  }


  /**
   * Comparison used for inequalities on numerical values for an object.
   * If one of these objects is not a number, then use the standard comparison.
   * @return -1 if this is smaller than o, +1 if larger, 0 if equal, or the result of compareTo
   *         if not a number.
   */
  public int numericalCompare(SPObject o) {
    assert !isNumber();
    return compareTo(o);
  }


  /**
   * Indicates if this object is a number. Not usually, so returns <code>false</code> in this
   * abstract class. XSD extensions the represent numerical values should return true.
   * @return <code>true</code> if this object is a number. False otherwise.
   */
  public boolean isNumber() {
    return false;
  }


  /**
   * Utility for long comparisons
   * @param a The first long value
   * @param b The second long value
   * @return +1 if a &gt; b, -1 if a &lt; b, 0 if a == b
   */
  public static final int compare(long a, long b) {
    return a == b ? 0 : (a < b ? -1 : 1);
  }

  /**
   * Default equality test. Based on the data type and the raw buffer.
   */
  public boolean equals(Object o) {
    return o != null && o.getClass() == getClass() && ((SPObject)o).getData().equals(getData());
  }

  /**
   * Default hashCode. Based on the raw buffer.
   */
  public int hashCode() {
    return getData().hashCode();
  }
}
