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

import org.mulgara.util.StringUtil;

/**
 * Represents free floating text. Indent is irrelevant for this class.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Text extends HtmlElement {

  private StringBuilder buffer;

  /**
   * Creates a text element with no initial text.
   */
  public Text() {
    super(-1);
    buffer = new StringBuilder();
  }


  /**
   * Creates a text element from an entity object.
   * @param e The entity to use in this object.
   */
  public Text(HtmlElement.Entity e) {
    super(-1);
    buffer = new StringBuilder(e.toString());
  }


  /**
   * Appends new text from an entity to this element.
   * @param e The entity to append to this element.
   * @return The current instance.
   */
  public Text append(HtmlElement.Entity e) {
    buffer.append(e.toString());
    return this;
  }


  /**
   * Creates a text element from an arbitrary object.
   * @param obj The object to convert to text for this element.
   */
  public Text(Object obj) {
    super(-1);
    buffer = new StringBuilder(StringUtil.quoteAV(obj.toString()));
  }


  /**
   * Appends new text from an arbitrary object to this element.
   * @param obj The object to convert to text to append to this element.
   * @return The current instance.
   */
  public Text append(Object obj) {
    buffer.append(StringUtil.quoteAV(obj.toString()));
    return this;
  }


  /**
   * Not used for this implementation.
   * @see org.mulgara.webquery.html.HtmlElement#getTag()
   */
  protected String getTag() {
    return "";
  }


  /**
   * Always returns false, indicating that formatting is never used on this element.
   * @see org.mulgara.webquery.html.HtmlElement#shouldIndent()
   */
  protected boolean shouldIndent() {
    return false;
  }


  /**
   * Appends this text raw to the writer. There are no tags, or formating.
   * @see org.mulgara.webquery.html.HtmlElement#sendTo(java.io.PrintWriter)
   */
  public void sendTo(PrintWriter out) {
    out.append(buffer);
  }

}
