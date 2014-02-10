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

package org.mulgara.webquery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mulgara.util.StackTrace;

/**
 * Loads a resource file, replaces any tags, and sends the results to an output stream.
 *
 * @created Aug 1, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class ResourceTextFile extends ResourceFile {

  /** The marker for tags in a resource file. */
  private static final String MARKER = "@@";

  /** A mapping of tags in the file to their replacement text. */
  Map<String,String> tagMap;

  /**
   * Loads a text file with no tag replacement.
   * @param resourceFile The path of the resource to load.
   */
  public ResourceTextFile(String resourceFile) {
    super(resourceFile);
    tagMap = Collections.emptyMap();
  }

  /**
   * Loads a text file with a map for replacing tags.
   * @param resourceFile The path of the resource to load.
   * @param tagMap A mapping of tags to their replacement text.
   */
  public ResourceTextFile(String resourceFile, Map<String,String> tagMap) {
    super(resourceFile);
    setTags(tagMap);
  }

  /**
   * Loads a text file with a map for replacing tags.
   * @param resourceFile The path of the resource to load.
   * @param tagMap Pairs of tagging strings to the values they are to be replaced with.
   */
  public ResourceTextFile(String resourceFile, String[][] tagMap) {
    super(resourceFile);
    setTags(tagMap);
  }

  /**
   * Set the tags and the values they map to.
   * @param tagMap A map of simple tag strings to the values they are to be replaced with.
   * @return The current object.
   */
  public ResourceFile setTags(Map<String,String> tagMap) {
    this.tagMap = new HashMap<String,String>();
    for (Map.Entry<String,String> tag: tagMap.entrySet()) {
      this.tagMap.put(MARKER + tag.getKey() + MARKER, tag.getValue());
    }
    return this;
  }

  /**
   * Set the tags and the values they map to.
   * @param tagPairs Pairs of tagging strings to the values they are to be replaced with.
   * @return The current object.
   */
  public ResourceFile setTags(String[][] tagPairs) {
    tagMap = new HashMap<String,String>();
    for (String[] tag: tagPairs) {
      if (tag.length != 2) throw new IllegalArgumentException("Require pairs of values for mapping tags. Got " + tag.length + " values.");
      tagMap.put(MARKER + tag[0] + MARKER, tag[1]);
    }
    return this;
  }

  /**
   * Sends the resource to a print writer after replacing all detected tags.
   * @param out The PrintWriter that will receive the file.
   * @return The provided PrintWriter.
   */
  public PrintWriter sendTo(PrintWriter out) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(getStream()));
    String line;
    while ((line = reader.readLine()) != null) {
      for (Map.Entry<String,String> tag: tagMap.entrySet()) line = line.replaceAll(tag.getKey(), tag.getValue());
      out.println(line);
    }
    reader.close();
    return out;
  }

  /**
   * Loads the resource, replaces any detected tags, and returns the result as a string.
   * @return The string containing the resource, with all known tags replaced.
   * @throws IOException The resource could not be read.
   */
  public String toString() {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(getStream()));
      StringBuilder buffer = new StringBuilder(reader.readLine());
      String line;
      while ((line = reader.readLine()) != null) {
        for (Map.Entry<String,String> tag: tagMap.entrySet()) line = line.replaceAll(tag.getKey(), tag.getValue());
        buffer.append(line).append("\n");
      }
      reader.close();
      return buffer.toString();
    } catch (IOException ioe) {
      return StackTrace.throwableToString(ioe);
    }
  }
}
