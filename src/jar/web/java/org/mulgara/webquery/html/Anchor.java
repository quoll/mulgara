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

import java.net.URI;
import java.net.URL;

/**
 * Represents an Anchor element. The constructors expect an HREF or a name.
 * The constructors ar duplicated between URL and URI parameters, as URL
 * objects do not hold relative URLs, while URIs can.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Anchor extends HtmlElement {

  /**
   * Creates an anchor with a given indent.
   * @param indent The amount to indent by.
   * @param ref The reference to point to.
   * @param subElements The list of subElements to embed. Probably just a {@link Text}
   */
  public Anchor(int indent, URL ref, HtmlElement... subElements) {
    super(indent, subElements);
    this.addAttr(Attr.HREF, ref);
  }


  /**
   * Creates an anchor with no indenting.
   * @param ref The reference to point to.
   * @param subElements The list of subElements to embed. Probably just a {@link Text}
   */
  public Anchor(URL ref, HtmlElement... subElements) {
    this(-1, ref, subElements);
  }


  /**
   * Creates an anchor with a given indent.
   * @param indent The amount to indent by.
   * @param ref The reference to point to.
   * @param subElements The list of subElements to embed. Probably just a {@link Text}
   */
  public Anchor(int indent, URI ref, HtmlElement... subElements) {
    super(indent, subElements);
    this.addAttr(Attr.HREF, ref);
  }


  /**
   * Creates an anchor with no indenting.
   * @param ref The reference to point to.
   * @param subElements The list of subElements to embed. Probably just a {@link Text}
   */
  public Anchor(URI ref, HtmlElement... subElements) {
    this(-1, ref, subElements);
  }


  /**
   * Creates an anchor with a given indent, and a list of sub elements.
   * @param indent The amount to indent by.
   * @param name The name of the anchor.
   * @param subElements a list of sub elements inside this anchor.
   */
  public Anchor(int indent, String name, HtmlElement... subElements) {
    super(indent, subElements);
    this.addAttr(Attr.NAME, name);
  }


  /**
   * Creates an anchor with no initial indenting, and a list of sub elements.
   * @param name The name of the anchor.
   * @param subElements a list of sub elements inside this anchor.
   */
  public Anchor(String name, HtmlElement... subElements) {
    this(-1, name, subElements);
  }


  /**
   * Creates an anchor with a given indent, and the text for a subelement.
   * @param indent The amount to indent by.
   * @param name The name of the anchor.
   * @param text The text for a Text subElement.
   */
  public Anchor(int indent, String name, String text) {
    super(indent, new Text(text));
    this.addAttr(Attr.NAME, name);
  }


  /**
   * Creates an anchor with no initial indenting, and the text for a subelement.
   * @param name The name of the anchor.
   * @param text The text for a Text subElement.
   */
  public Anchor(String name, String text) {
    this(-1, name, new Text(text));
  }


  /**
   * Creates an anchor with a given indent, and the text for a subelement.
   * @param indent The amount to indent by.
   * @param ref The reference to point to.
   * @param text The text for a Text subElement.
   */
  public Anchor(int indent, URL ref, String text) {
    super(indent, new Text(text));
    this.addAttr(Attr.HREF, ref);
  }


  /**
   * Creates an anchor with no initial indenting, the text for a subelement.
   * @param ref The reference to point to.
   * @param text The text for a Text subElement.
   */
  public Anchor(URL ref, String text) {
    this(-1, ref, new Text(text));
  }


  /**
   * Creates an anchor with a given indent, and the text for a subelement.
   * @param indent The amount to indent by.
   * @param ref The reference to point to.
   * @param text The text for a Text subElement.
   */
  public Anchor(int indent, URI ref, String text) {
    super(indent, new Text(text));
    this.addAttr(Attr.HREF, ref);
  }


  /**
   * Creates an anchor with no initial indenting, and the text for a subelement.
   * @param ref The reference to point to.
   * @param text The text for a Text subElement.
   */
  public Anchor(URI ref, String text) {
    this(-1, ref, new Text(text));
  }


  protected String getTag() {
    return "a";
  }

}
