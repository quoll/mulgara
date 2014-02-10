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

package org.mulgara.protocol;

import java.io.IOException;
import java.nio.charset.Charset;

import org.mulgara.query.TuplesException;

/**
 * Represents an Answer that can be emitted as XML to a stream.
 *
 * @created Jul 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public interface StreamedXMLAnswer extends XMLAnswer, StreamedAnswer {

  /**
   * Converts the Answer to an XML String and send to output.
   * @throws TuplesException Indicates an error accessing the Answer.
   */
  public void emit() throws TuplesException, IOException;

  /**
   * Sets the character encoding when writing XML text to a byte stream.
   * @param encoding The encoding to use.
   */
  public void setCharacterEncoding(String encoding);

  /**
   * Sets the character encoding when writing XML text to a byte stream.
   * @param encoding The charset encoding to use.
   */
  public void setCharacterEncoding(Charset charset);
}
