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
 * Represents data in a table.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class TableData extends HtmlElement {

  /**
   * Creates a data element with a given indent.
   * @param indent The amount to indent by.
   */
  public TableData(int indent) {
    super(indent);
  }


  /**
   * Creates a data element with no indenting.
   */
  public TableData() {
    this(0);
  }


  /**
   * Creates a data element with a given indent, and a list of sub elements.
   * @param indent The amount to indent by.
   * @param subElements a list of sub elements inside this table cell.
   */
  public TableData(int indent, HtmlElement... subElements) {
    super(indent, subElements);
  }


  /**
   * Creates a data element with no initial indenting, and a list of sub elements.
   * @param subElements a list of sub elements inside this table cell.
   */
  public TableData(HtmlElement... subElements) {
    this(0, subElements);
  }


  /**
   * Creates a data element with a given indent, and embedded text.
   * @param indent The amount to indent by.
   * @param text The text to set for this element.
   */
  public TableData(int indent, String text) {
    super(indent, new Text(text));
  }


  /**
   * Creates a data element with no initial indenting, and embedded text.
   * @param text The text to set for this element.
   */
  public TableData(String text) {
    this(0, new Text(text));
  }


  /** @see org.mulgara.webquery.html.HtmlElement#getTag() */
  protected String getTag() {
    return "td";
  }

}
