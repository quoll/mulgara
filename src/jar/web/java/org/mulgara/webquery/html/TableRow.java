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
 * Represents a row in a table. Any child elements will be wrapped in TableData elements
 * before being added.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class TableRow extends HtmlElement implements TableStructure {

  /**
   * Creates a row with a given indent.
   * @param indent The amount to indent by.
   */
  public TableRow(int indent) {
    super(indent);
  }


  /**
   * Creates a row with no indenting.
   */
  public TableRow() {
    this(0);
  }


  /**
   * Creates a row with a given indent, and a list of data elements.
   * @param indent The amount to indent by.
   * @param rowData a list of data elements to go inside this row.
   */
  public TableRow(int indent, HtmlElement... rowData) {
    super(indent, rowData);
  }


  /**
   * Creates an row with no initial indenting, and a list of data elements.
   * @param rowData an array of data elements to go inside this row.
   */
  public TableRow(HtmlElement... rowData) {
    this(0, rowData);
  }


  /**
   * Adds a sub element to the end of the list of sub elements for this element.
   * @param elt The new sub element to be added.
   * @return The current element.
   * @throws IllegalArgumentException If elt is anything other than TableData.
   */
  public HtmlElement add(HtmlElement elt) {
    if (!(elt instanceof TableData)) elt = new TableData(elt);
    return super.add(elt);
  }


  /**
   * Gets the width of this row.
   * @return The number of TD elements in this row.
   */
  public int getWidth() {
    return subElements.size();
  }


  /** @see org.mulgara.webquery.html.HtmlElement#getTag() */
  protected String getTag() {
    return "tr";
  }

}
