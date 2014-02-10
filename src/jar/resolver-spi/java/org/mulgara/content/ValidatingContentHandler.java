/*
 * Copyright 2011 Revelytix, Inc.
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
package org.mulgara.content;

/**
 * Hack to account for the fact that the MBox content handler does a blind parse of the content
 * to determine whether it can parse it. This fell out of the inversion of logic in choosing a
 * content handler that resulted from eliminating the ContentHandler.canParse method.
 * 
 * @author Alex Hall
 * @created Jul 19, 2011
 */
public interface ValidatingContentHandler extends ContentHandler {

  /**
   * Validate the given content, most likely based on a blind parse.
   * @param content Some content.
   * @return <tt>true</tt> if we can parse this content.
   */
  public boolean validate(Content content) throws NotModifiedException;
}
