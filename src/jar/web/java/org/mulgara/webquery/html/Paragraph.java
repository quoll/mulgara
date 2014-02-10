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
 * Represents a paragraph element.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Paragraph extends HtmlElement {

  /**
   * Creates a paragraph with a given indent.
   * @param indent The amount to indent by.
   */
  public Paragraph(int indent) {
    super(indent);
  }


  /**
   * Creates a paragraph with no indenting.
   */
  Paragraph() {
    this(-1);
  }


  /**
   * Creates a paragraph with a given indent, and a list of sub elements.
   * @param indent The amount to indent by.
   * @param subElements a list of sub elements inside this paragraph.
   */
  Paragraph(int indent, HtmlElement... subElements) {
    super(indent, subElements);
  }


  /**
   * Creates a paragraph with no initial indenting, and a list of sub elements.
   * @param subElements a list of sub elements inside this paragraph.
   */
  Paragraph( HtmlElement... subElements) {
    this(-1, subElements);
  }


  protected String getTag() {
    return "p";
  }

}
