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

/**
 * Represents a span element. This implementation always uses a class attribute.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Span extends HtmlElement {

  /**
   * Creates a span with a given indent.
   * @param indent The amount to indent by.
   * @param spanClass The name of the class for the span.
   */
  public Span(int indent, String spanClass) {
    super(indent);
    this.addAttr(Attr.CLASS, spanClass);
  }


  /**
   * Creates a span with no indenting.
   * @param spanClass The name of the class for the span.
   */
  public Span(String spanClass) {
    this(-1, spanClass);
  }


  /**
   * Creates a span with a given indent, and a list of sub elements.
   * @param indent The amount to indent by.
   * @param spanClass The name of the class for the span.
   * @param subElements a list of sub elements inside this span.
   */
  public Span(int indent, String spanClass, HtmlElement... subElements) {
    super(indent, subElements);
    this.addAttr(Attr.CLASS, spanClass);
  }


  /**
   * Creates a span with no initial indenting, and a list of sub elements.
   * @param spanClass The name of the class for the span.
   * @param subElements a list of sub elements inside this span.
   */
  public Span(String spanClass, HtmlElement... subElements) {
    this(-1, spanClass, subElements);
  }


  /**
   * Creates a span with a given indent, and a list of sub elements.
   * @param indent The amount to indent by.
   * @param spanClass The name of the class for the span.
   * @param text The text for a Text subElement.
   */
  public Span(int indent, String spanClass, String text) {
    super(indent, new Text(text));
    this.addAttr(Attr.CLASS, spanClass);
  }


  /**
   * Creates a span with no initial indenting, and a list of sub elements.
   * @param spanClass The name of the class for the span.
   * @param text The text for a Text subElement.
   */
  public Span(String spanClass, String text) {
    this(-1, spanClass, new Text(text));
  }


  protected String getTag() {
    return "span";
  }

}
