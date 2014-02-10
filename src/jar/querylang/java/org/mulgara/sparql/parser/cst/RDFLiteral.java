/*
 * Copyright 2008 Fedora Commons
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.sparql.parser.cst;

import java.util.HashMap;
import java.util.Map;

/**
 * Object for representing RDF literals.
 *
 * @created February 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class RDFLiteral implements Node, PrimaryExpression, LogicExpression {

  /** The string data for this literal */
  private String value;

  /** The datatype */
  private IRIReference datatype;

  /** The language code */
  private String language;
  
  /** Characters that get escaped for printing */
  private static final HashMap<String,String> escapeMap = new HashMap<String,String>();
  static {
    // escape the following: tbnrf\"'
    escapeMap.put("\\t", "\\\\t");
    escapeMap.put("\b", "\\\\b");
    escapeMap.put("\\n", "\\\\n");
    escapeMap.put("\\r", "\\\\r");
    escapeMap.put("\\f", "\\\\f");
    escapeMap.put("\\\\", "\\\\\\\\");
    escapeMap.put("\"", "\\\\\"");
    escapeMap.put("'", "\\\\'");
  }


  /**
   * Constructor for an untyped literal.
   * @param value The literal data.
   */
  public RDFLiteral(String value) {
    this.value = unescape(value);
    this.datatype = null;
    this.language = null;
  }

  /**
   * Constructor for a typed literal.
   * @param value The literal data.
   * @param datatype The type of the literal.
   */
  public RDFLiteral(String value, IRIReference datatype) {
    this.value = unescape(value);
    this.datatype = datatype;
    this.language = null;
  }

  /**
   * Constructor for a typed literal.
   * @param value The literal data.
   * @param language The language code for the literal.
   */
  public RDFLiteral(String value, String language) {
    this.value = unescape(value);
    this.datatype = null;
    setLanguage(language);
  }

  /**
   * Gets the string representation of the literal - regardless of datatype.
   * @return the string representation of the value of this literal
   */
  public String getValue() {
    return value;
  }

  /**
   * Gets the datatype for the literal.
   * @return the datatype
   */
  public IRIReference getDatatype() {
    return datatype;
  }

  /**
   * @param datatype the datatype to set
   */
  public void setDatatype(IRIReference datatype) {
    this.datatype = datatype;
  }

  /**
   * Gets the language code.
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    if (language.charAt(0) == '@') language = language.substring(1);
    this.language = language;
  }

  /**
   * Indicates that the literal has a datatype.
   * @return <code>true</code> if the literal is typed.
   */
  public boolean isTyped() {
    return datatype != null;
  }
  
  /**
   * Indicates that the literal has a language code.
   * @return <code>true</code> if the literal has a language code.
   */
  public boolean isLanguageCoded() {
    return language != null;
  }

  /**
   * Convert an escaped string into it's normal form
   * @param s The string to convert
   * @return The unescaped version of the string.
   */
  private static String unescape(String s) {
    for (Map.Entry<String,String> escapeEntry: escapeMap.entrySet()) {
      s = s.replaceAll(escapeEntry.getValue(), escapeEntry.getKey());
    }
    return s;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    if (isTyped()) return "'" + escapedValue() + "'^^" + datatype.toString();
    if (isLanguageCoded()) return "'" + escapedValue() + "'@" + language;
    else return "'" + escapedValue() + "'";
  }

  /**
   * Escape the characters in the value of a literal for printing
   * @return The literal value, with escaped characters.
   */
  private String escapedValue() {
    String escaped = value;
    for (Map.Entry<String,String> escapeEntry: escapeMap.entrySet()) {
      escaped = escaped.replaceAll(escapeEntry.getKey(), escapeEntry.getValue());
    }
    return "'" + escaped + "'";
  }
}
