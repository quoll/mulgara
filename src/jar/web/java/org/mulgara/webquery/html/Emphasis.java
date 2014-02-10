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
 * Represents an emphasis element.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Emphasis extends HtmlElement {

  /**
   * Creates an emphasis element.
   */
  public Emphasis() {
    super(-1);
  }


  /**
   * Creates an emphasis element with embedded text.
   * @param text The text to embed.
   */
  public Emphasis(String text) {
    super(-1, new Text(text));
  }


  /**
   * Always returns false, indicating that formatting is never used on this element.
   * @see org.mulgara.webquery.html.HtmlElement#shouldIndent()
   */
  protected boolean shouldIndent() {
    return false;
  }


  protected String getTag() {
    return "em";
  }

}
