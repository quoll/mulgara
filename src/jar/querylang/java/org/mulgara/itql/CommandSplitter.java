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

package org.mulgara.itql;

import java.util.List;

/**
 * This interface splits queries for the appropriate query type
 * @created Sep 11, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
interface CommandSplitter {
  
  /**
   * Split the given string into an array of strings containing individual elements.
   * @param commands All the commands in a long string.
   * @return An array of individual commands.
   */
  List<String> split(String commands);
}
