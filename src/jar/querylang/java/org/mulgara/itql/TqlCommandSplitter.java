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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Splits strings with multiple commands into lists of strings containing single commands.
 * @created Sep 11, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class TqlCommandSplitter implements CommandSplitter {
  /** The logger. */
  private final static Logger logger = Logger.getLogger(TqlCommandSplitter.class.getName());

  /**
   * Split a string into individual commands.
   * @param commands A single string containing multiple commands.
   * @return A list of strings each containing single commands.
   */
  public List<String> split(String commands) {
    List<String> singleQueryList = new ArrayList<String>();

    // Inside a URI?
    boolean inUrl = false;
    // Inside a text literal?
    boolean inText = false;

    // Start index for next single query
    int startIndex = 0;

    if (logger.isDebugEnabled()) logger.debug("About to break up query: " + commands);

    commands = commands.trim();

    // Iterate along the multi query and strip out the single queries.
    for (int lineIndex = 0; lineIndex < commands.length(); lineIndex++) {

      char currentChar = commands.charAt(lineIndex);

      switch (currentChar) {

        // Quote - end or start of a literal if not in a URI
        // (OK so maybe it won't appear in a legal URI but let things further
        // down handle this)
        case '\'':
          if (!inUrl) {
            if (inText) {
              // Check for an \' inside a literal
              if ((lineIndex > 1) && (commands.charAt(lineIndex - 1) != '\\')) inText = false;
            } else {
              inText = true;
            }
          }
          break;

          // URI start - if not in a literal
        case '<':
          if (!inText) inUrl = true;
          break;

          // URI end - if not in a literal
        case '>':
          if (!inText) inUrl = false;
          break;

        case ';':
          if (!inText && !inUrl) {
            String singleQuery = commands.substring(startIndex, lineIndex + 1).trim();
            startIndex = lineIndex + 1;
            singleQueryList.add(singleQuery);
            if (logger.isDebugEnabled()) logger.debug("Found single query: " + singleQuery);
          }
          break;

        default:
      }
    }
    
    // Lasy query is not terminated with a ';'
    if (startIndex < commands.length()) singleQueryList.add(commands.substring(startIndex, commands.length()));

    return singleQueryList;
  }

}
