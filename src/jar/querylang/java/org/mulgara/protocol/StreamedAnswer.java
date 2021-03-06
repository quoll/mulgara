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

import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;

/**
 * Represents an Answer that can be emitted to a stream.
 *
 * @created Sep 8, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public interface StreamedAnswer {

  /**
   * Converts the Answer to a String and send to output.
   * @throws TuplesException Indicates an error accessing the Answer.
   */
  public void emit() throws TuplesException, IOException;

  public void initOutput();

  /**
   * Closes all resources.
   * @throws IOException If there was an error closing any resources.
   */
  public void close() throws IOException;

  public void addDocHeader() throws IOException;
  
  public void addAnswer(Answer data) throws TuplesException, IOException;
  
  public void addDocFooter() throws IOException;
}
