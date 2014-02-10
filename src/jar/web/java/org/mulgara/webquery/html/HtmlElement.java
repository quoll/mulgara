/*
 * Copyright 2008 Fedora Commons, Inc.
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

package org.mulgara.webquery.html;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mulgara.util.functional.C;

/**
 * Represents an HTML element that can be written to an output stream.
 * Indents are hard coded to 2 characters.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public abstract class HtmlElement {

  /** The amount to indent by. */
  private static final String INDENT_CHARS = "  ";

  /** The number of indents to use for this element. */
  protected int indent;

  /** The current indent string, where indentStr = {@link #INDENT_CHARS}*{@link #indent}. */
  private String indentStr;

  /** The list of elements under this element */
  protected List<HtmlElement> subElements;

  /** The attributes for this element */
  protected Map<Attr,String> attributes;

  /** A limited list of attributes. Expand on this as required. */
  public enum Attr {
    CLASS,
    ID,
    STYLE,
    TITLE,
    NAME,
    HEIGHT,
    WIDTH,
    SIZE,
    ALIGN,
    VALIGN,
    BORDER,
    CELLPADDING,
    CELLSPACING,
    COLS,
    ROWS,
    COLSPAN,
    ALT,
    HREF,
    SELECTED,
    VALUE,
    METHOD,
    SUMMARY;

    public String toString() { return this.name().toLowerCase(); };
  };

  /** A limited list of entities. Expand on this as required. */
  public enum Entity {
    AMP("&amp;"),
    GT("&gt;"),
    LT("&lt;"),
    QUOT("&quot;"),
    APOS("&#39;"),  // &apos; does not work in IE
    NBSP("&nbsp;");

    private final String t;
    private Entity(String t) { this.t = t; }
    public String toString() { return t; }
  };

  /**
   * Creates an empty element with a given indent.
   * @param indent The amount to indent by.
   */
  HtmlElement(int indent) {
    this.indent = indent;
    indentStr = generateIndent(indent);
    subElements = new LinkedList<HtmlElement>();
    attributes = new LinkedHashMap<Attr,String>();
  }


  /**
   * Creates an empty element with no initial indenting.
   */
  HtmlElement() {
    this(0);
  }


  /**
   * Creates an element with a given indent, and a list of sub-elements.
   * @param indent The amount to indent by.
   * @param subElements a list of elements directly subordinate to this element.
   */
  HtmlElement(int indent, HtmlElement... subElements) {
    this(indent);
    for (HtmlElement e: subElements) add(e);
  }


  /**
   * Creates an empty element with no indenting.
   * @param subElements a list of elements directly subordinate to this element.
   */
  HtmlElement(HtmlElement... subElements) {
    this(0, subElements);
  }


  /**
   * Adds a sub element to the end of the list of sub elements for this element.
   * @param elt The new sub element to be added.
   * @return The current element.
   */
  public HtmlElement add(HtmlElement elt) {
    subElements.add(elt);
    elt.setIndent(indent < 0 ? indent : indent + 1);
    return this;
  }


  /**
   * Adds an attribute to the set of attributes. This will replace any existing values
   * for this attribute.
   * @param attr The attribute to add.
   * @param val The value for the attribute.
   * @return The current element.
   */
  public HtmlElement addAttr(Attr attr, Object val) {
    attributes.put(attr, val.toString());
    return this;
  }


  /**
   * This method sends the given text to the output stream.
   * For indented text, this is always called when a new line has just been started.
   * The method always finishes while at the end of a line.
   * @param out the writer to send the HTML to.
   */
  public void sendTo(PrintWriter out) {
    if (shouldIndent()) out.append(indentStr);
    out.append('<').append(getTag());
    for (Map.Entry<Attr,String> attr: attributes.entrySet()) out.append(' ').append(attr.getKey().toString()).append("=\"").append(attr.getValue()).append('\"');
    if (subElements.isEmpty()) {
      // no sub elements, so close the tag
      out.append("/>");
    } else {
      out.append('>');
      // iterate over the sub elements
      for (HtmlElement e: subElements) {
        if (e.shouldIndent()) out.append('\n');
        e.sendTo(out);
      }
      if (C.last(subElements).shouldIndent()) out.append('\n').append(indentStr);
      out.append("</").append(getTag()).append('>');
    }
  }


  /**
   * Tests if this element should be indented in the current output.
   * @return <code>true</code> if indenting should occur.
   */
  protected boolean shouldIndent() {
    return indentStr != null;
  }


  /**
   * Sets the indent of this element. Used by the parent.
   * @param indent The new indent to use.
   */
  private void setIndent(int indent) {
    this.indent = indent;
    if (indent < 0) {
      indentStr = null;
      for (HtmlElement e: subElements) e.setIndent(indent);
    } else {
      indentStr = generateIndent(indent);
      for (HtmlElement e: subElements) e.setIndent(indent + 1);
    }
  }


  /**
   * Create a string of spaces for indenting.
   * @param i The number of indents to use.
   * @return A string of spaces, equal to i * {@link #INDENT_CHARS}, or <code>null</code>
   *         if i &lt; 0.
   */
  private static String generateIndent(int i) {
    if (i < 0) return null;
    StringBuilder s = new StringBuilder();
    while (--i > 0) s.append(INDENT_CHARS);
    return s.toString();
  }


  /**
   * Any implementing classes must implement this method to create the text representation
   * of the tag in the emitted HTML.
   * @return The string for this element.
   */
  protected abstract String getTag();

}
