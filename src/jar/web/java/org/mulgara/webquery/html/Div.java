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
 * Represents a div element. This implementation always uses an id attribute.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Div extends HtmlElement {

  /**
   * Creates a div with a given indent.
   * @param indent The amount to indent by.
   * @param id The name of the id for the div.
   */
  public Div(int indent, String id) {
    super(indent);
    this.addAttr(Attr.CLASS, id);
  }


  /**
   * Creates a div with no indenting.
   * @param id The name of the id for the div.
   */
  Div(String id) {
    this(0, id);
  }


  /**
   * Creates a div with a given indent, and a list of sub elements.
   * @param indent The amount to indent by.
   * @param id The name of the id for the div.
   * @param subElements a list of sub elements inside this div.
   */
  Div(int indent, String id, HtmlElement... subElements) {
    super(indent, subElements);
    this.addAttr(Attr.CLASS, id);
  }


  /**
   * Creates a div with no initial indenting, and a list of sub elements.
   * @param id The name of the id for the div.
   * @param subElements a list of sub elements inside this div.
   */
  Div(String id, HtmlElement... subElements) {
    this(0, id, subElements);
  }


  /** @see org.mulgara.webquery.html.HtmlElement#getTag() */
  protected String getTag() {
    return "div";
  }

}
